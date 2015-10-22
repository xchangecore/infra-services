package com.leidos.xchangecore.core.infrastructure.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringEscapeUtils;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.message.GenericMessage;
import org.uicds.agreementService.AgreementListType;
import org.uicds.agreementService.AgreementType;
import org.uicds.agreementService.AgreementType.ShareRules;
import org.uicds.agreementService.AgreementType.ShareRules.ShareRule;

import com.leidos.xchangecore.core.infrastructure.dao.AgreementDAO;
import com.leidos.xchangecore.core.infrastructure.dao.InterestGroupDAO;
import com.leidos.xchangecore.core.infrastructure.exceptions.InvalidInterestGroupIDException;
import com.leidos.xchangecore.core.infrastructure.exceptions.LocalCoreNotOnlineException;
import com.leidos.xchangecore.core.infrastructure.exceptions.NoShareAgreementException;
import com.leidos.xchangecore.core.infrastructure.exceptions.NoShareRuleInAgreementException;
import com.leidos.xchangecore.core.infrastructure.exceptions.RemoteCoreUnavailableException;
import com.leidos.xchangecore.core.infrastructure.exceptions.RemoteCoreUnknownException;
import com.leidos.xchangecore.core.infrastructure.exceptions.XMPPComponentException;
import com.leidos.xchangecore.core.infrastructure.messages.CoreStatusUpdateMessage;
import com.leidos.xchangecore.core.infrastructure.messages.DeleteInterestGroupForRemoteCoreMessage;
import com.leidos.xchangecore.core.infrastructure.messages.DeleteJoinedInterestGroupMessage;
import com.leidos.xchangecore.core.infrastructure.messages.InterestGroupStateNotificationMessage;
import com.leidos.xchangecore.core.infrastructure.messages.JoinedInterestGroupNotificationMessage;
import com.leidos.xchangecore.core.infrastructure.messages.UnSubscribeMessage;
import com.leidos.xchangecore.core.infrastructure.model.InterestGroup;
import com.leidos.xchangecore.core.infrastructure.service.AgreementService;
import com.leidos.xchangecore.core.infrastructure.service.DirectoryService;
import com.leidos.xchangecore.core.infrastructure.service.InterestGroupManagementComponent;
import com.leidos.xchangecore.core.infrastructure.util.InterestGroupInfoUtil;
import com.saic.precis.x2009.x06.base.CodespaceValueType;

/**
 * The InterestGroupManagementComponentImpl uses Spring Message Channels to communicate with the
 * communications infrastructure about sharing and updating Interest Groups between cores. It
 * maintains state about all the current Interest Groups in
 * {@link com.leidos.xchangecore.core.infrastructure.dao.InterestGroupDAO}.
 *
 *
 * @author Aruna Hau
 * @since 1.0
 * @see com.leidos.xchangecore.core.infrastructure.dao.InterestGroupDAO Interest Group DAO
 * @ssdd
 *
 */
// @Transactional
public class InterestGroupManagementComponentImpl
    implements InterestGroupManagementComponent {

    Logger logger = LoggerFactory.getLogger(InterestGroupManagementComponentImpl.class);

    private InterestGroupDAO interestGroupDAO;
    private MessageChannel interestGroupStateNotificationChannel;
    private MessageChannel newJoinedInterestGroupChannel;
    private MessageChannel deleteJoinedInterestGroupChannel;
    private MessageChannel unSubscribeChannel;

    private DirectoryService directoryService;
    private AgreementService agreementService;
    private AgreementDAO agreementDAO;

    @Override
    public void coreStatusUpdateHandler(CoreStatusUpdateMessage message) {

        String remoteJID = message.getCoreName();

        if (remoteJID.endsWith("/CoreConnection"))
            remoteJID = remoteJID.substring(0, remoteJID.indexOf("/CoreConnection"));
        logger.debug("coreStatusUpdateHandler: [" + remoteJID + "]" + message.getCoreStatus());

        if (message.getCoreStatus().equalsIgnoreCase(CoreStatusUpdateMessage.Status_Available) &&
            getAgreementDAO().isRemoteCoreMutuallyAgreed(remoteJID)) {
            final List<InterestGroup> interestGroupList = interestGroupDAO.findAll();
            logger.debug("coreStatusUpdateHandler: found " + interestGroupList.size() +
                         " interest groups in database");
            for (final InterestGroup interestGroup : interestGroupList)
                if (interestGroup.getOwningCore().equalsIgnoreCase(remoteJID)) {
                    logger.debug("coreStatusUpdateHandler: restore interestGroup owned by remoteJID: " +
                                 remoteJID);
                    sendInterestGroupStateNotificationMessage(interestGroup);
                }
        }
        logger.debug("coreStatusUpdateHandler: ... exit ...");
    }

    /**
     * Creates the interest group and sends a state change notification.j
     *
     * @param interestGroupInfo the interest group info
     *
     * @return the string
     * @ssdd
     */
    @Override
    public String createInterestGroup(InterestGroupInfo interestGroupInfo) {

        logger.debug("createInterestGroup: name=" + interestGroupInfo.getName() + " owningCore=" +
                     interestGroupInfo.getOwningCore());
        final String interestGroupID = "IG-" + UUID.randomUUID().toString();

        logger.debug("Creating interest group id=" + interestGroupID);

        InterestGroup interestGroup = new InterestGroup();
        interestGroup.setInterestGroupID(interestGroupID);
        interestGroup.setInterestGroupType(StringEscapeUtils.escapeXml(interestGroupInfo.getInterestGroupType()));
        interestGroup.setInterestGroupSubtype(StringEscapeUtils.escapeXml(interestGroupInfo.getInterestGroupSubType()));
        interestGroup.setDescription(StringEscapeUtils.escapeXml(interestGroupInfo.getDescription()));
        interestGroup.setName(StringEscapeUtils.escapeXml(interestGroupInfo.getName()));
        interestGroup.setOwningCore(interestGroupInfo.getOwningCore());
        interestGroup.setExtendedMetadata(interestGroupInfo.getExtendedMetadata());
        interestGroup.setSharingStatus(InterestGroupStateNotificationMessage.SharingStatus.None.toString());

        logger.debug("createInterestGroup: persist the created interest group id=" +
                     interestGroupID);

        try {
            interestGroup = interestGroupDAO.makePersistent(interestGroup);
        } catch (final Exception e) {
            logger.error("createInterestGroup: " + e.getMessage());
        }

        // List<InterestGroup> igList = interestGroupDAO.findAll();
        // log.debug("===> found " + igList.size() + " interest groups in database");
        // for (InterestGroup ig : igList) {
        // log.debug("========> igID=" + ig.getInterestGroupID());
        // }

        // send interest group state change to Comms
        logger.debug("===> notify Comms of new interest group");
        final InterestGroupStateNotificationMessage mesg = new InterestGroupStateNotificationMessage();
        mesg.setState(InterestGroupStateNotificationMessage.State.NEW);
        mesg.setInterestGroupID(interestGroupID);
        mesg.setInterestGroupType(interestGroupInfo.getInterestGroupType());
        mesg.setOwningCore(interestGroupInfo.getOwningCore());
        mesg.setSharingStatus(interestGroup.getSharingStatus());
        mesg.setExtendedMetadata(interestGroupInfo.getExtendedMetadata());
        // set to null those values not relevant when state=NEW
        mesg.setInterestGroupInfo(null);
        final Message<InterestGroupStateNotificationMessage> notification = new GenericMessage<InterestGroupStateNotificationMessage>(mesg);
        interestGroupStateNotificationChannel.send(notification);

        return interestGroupID;
    }

    /**
     * Delete interest group.
     *
     * @param interestGroupID the interest group id
     *
     * @throws InvalidInterestGroupIDException the invalid interest group id exception
     * @ssdd
     */
    @Override
    public void deleteInterestGroup(String interestGroupID) throws InvalidInterestGroupIDException {

        logger.debug("deleteInterestGroup: IGID: " + interestGroupID);
        final InterestGroup interestGroup = interestGroupDAO.findByInterestGroup(interestGroupID);
        if (interestGroup == null)
            throw new InvalidInterestGroupIDException(interestGroupID);
        else {
            logger.debug("deleteInterestGroup: notify Comms of deleted  interest group");
            final InterestGroupStateNotificationMessage mesg = new InterestGroupStateNotificationMessage();
            mesg.setState(InterestGroupStateNotificationMessage.State.DELETE);
            mesg.setInterestGroupID(interestGroupID);
            // set to null those values not relevant when state=NEW
            mesg.setInterestGroupType(null);
            mesg.setOwningCore(null);
            mesg.setSharingStatus(null);
            mesg.setInterestGroupInfo(null);
            mesg.setExtendedMetadata(null);
            final Message<InterestGroupStateNotificationMessage> notification = new GenericMessage<InterestGroupStateNotificationMessage>(mesg);
            interestGroupStateNotificationChannel.send(notification);

            logger.debug("deleteInterestGroup: remove interest group; ID=" + interestGroupID +
                         " from DB");
            try {
                interestGroupDAO.delete(interestGroupID, true);
            } catch (final HibernateException e) {
                logger.error("deleteInterestGroup: HibernateException makeTransient interestGroupDAO: " +
                             e.getMessage() + " from " + e.toString());
            } catch (final Exception e) {
                logger.error("deleteInterestGroup: Exception makeTransient interestGroupDAO: " +
                             e.getMessage() + " from " + e.toString());
            }
        }
    }

    @Override
    public void deleteInterestGroupSharedFromRemoteCoreHandler(DeleteInterestGroupForRemoteCoreMessage msg) {

        logger.debug("deleteInterestGroupFromRemoteCoreHandler: remoteCore: " +
                     msg.getRemoteCoreName());

        if (msg.getInterestGroupID() != null) {
            logger.debug("deleteInterestGroupFromRemoteCoreHandler: for remote site's IGID: " +
                         msg.getInterestGroupID());
            doDeleteInterestGroup(msg.getRemoteCoreName(), msg.getInterestGroupID());
        } else {
            final List<InterestGroup> interestGroupList = interestGroupDAO.findByOwningCore(msg.getRemoteCoreName());
            for (final InterestGroup interestGroup : interestGroupList)
                doDeleteInterestGroup(msg.getRemoteCoreName(), interestGroup.getInterestGroupID());
        }
    }

    /**
     * Delete joined interest group.
     *
     * @param message the message
     * @ssdd
     */
    @Override
    public void deleteJoinedInterestGroup(DeleteJoinedInterestGroupMessage message) {

        try {
            interestGroupDAO.delete(message.getInterestGroupID(), true);
        } catch (final HibernateException e) {
            logger.error("deleteJoinedInterestGroup: HibernateException makeTransient interestGroupDAO: " +
                         e.getMessage() + " from " + e.toString());
        } catch (final Exception e) {
            logger.error("deleteJoinedInterestGroup: Exception makeTransient interestGroupDAO: " +
                         e.getMessage() + " from " + e.toString());
        }

        // notify IMS of the deletion of the restored interest group
        deleteJoinedInterestGroupChannel.send(new GenericMessage<DeleteJoinedInterestGroupMessage>(message));

    }

    private void doDeleteInterestGroup(String remoteJID, String IGID) {

        logger.debug("doDeleteInterestGroup: delete IGID: " + IGID + " and unsubscribe from JID: " +
                     remoteJID);
        final DeleteJoinedInterestGroupMessage message = new DeleteJoinedInterestGroupMessage();
        logger.debug("deleteInterestGroupFromRemoteCoreHandler: delete IGID: " + IGID);
        message.setInterestGroupID(IGID);
        deleteJoinedInterestGroup(message);
        final UnSubscribeMessage unSubScribeMessage = new UnSubscribeMessage(remoteJID, IGID);
        unSubscribeChannel.send(new GenericMessage<UnSubscribeMessage>(unSubScribeMessage));
    }

    public AgreementDAO getAgreementDAO() {

        return agreementDAO;
    }

    public MessageChannel getDeleteJoinedInterestGroupChannel() {

        return deleteJoinedInterestGroupChannel;
    }

    public DirectoryService getDirectoryService() {

        return directoryService;
    }

    /**
     * Gets the interest group.
     *
     * @param interestGroupID the interest group id
     *
     * @return the interest group
     * @ssdd
     */
    @Override
    public InterestGroupInfo getInterestGroup(String interestGroupID) {

        // log.debug("getInterestGroup: id=" + interestGroupID);
        final InterestGroup ig = interestGroupDAO.findByInterestGroup(interestGroupID);
        return toInterestGroupInfo(ig);
    }

    @Override
    public List<InterestGroupInfo> getInterestGroupList() {

        final List<InterestGroupInfo> igInfoList = new ArrayList<InterestGroupInfo>();
        final List<InterestGroup> igList = interestGroupDAO.findAll();

        for (final InterestGroup ig : igList) {

            final InterestGroupInfo igInfo = toInterestGroupInfo(ig);
            if (igInfo != null)
                igInfoList.add(igInfo);
        }

        logger.debug("getInterestGroupList: " + igInfoList.size() + " entries");
        return igInfoList;
    }

    // returns list of work product types to be shared.
    // - empty list : share everything
    // - non-empty list : share work product types in list (not implemented - for now we share
    // everything)
    private List<String> getShareAgreement(InterestGroup interestGroup, String targetCore)
        throws NoShareAgreementException, NoShareRuleInAgreementException {

        logger.debug("getShareAgreement - interestGroupID=" + interestGroup.getInterestGroupID());

        final List<String> workProductTypesToShare = new ArrayList<String>();
        final String igCodespace = InterestGroupManagementComponent.CodeSpace +
                                   interestGroup.getInterestGroupType();

        // TODO: ticket #248
        // since agreement is stored by coreJID and share incident is by host name (for now)
        // we cannot get the agreement by coreJID. For now, we have to get all the agreements and
        // loop through to get the right one.
        // This can go back to just getting agreement by coreJID when ticket #248 is implemented
        boolean agreementFound = false;
        boolean shareRulesEnabled = false;
        boolean shareRuleMatched = false;
        boolean shareRuleFound = false;

        final AgreementListType agreementList = agreementService.getAgreementList();
        for (final AgreementType agreement : agreementList.getAgreementArray())
            // If there's no active agreement with this core, we share all types
            if (agreement != null &&
            agreement.getPrincipals().getRemoteCore().getStringValue().contains(targetCore)) {
                agreementFound = true;
                final ShareRules shareRules = agreement.getShareRules();
                if (shareRules != null)
                    // if share rules exist, there must be an exact rule match (interestGroup's
                    // codepsace/subtype)
                    // or there will be no sharing
                    if (shareRules.getEnabled()) {
                        shareRulesEnabled = true;
                        final ShareRule[] shareRuleArray = shareRules.getShareRuleArray();
                        if (shareRules.sizeOfShareRuleArray() > 0)
                            for (final ShareRule shareRule : shareRuleArray)
                                if (shareRule.getEnabled() && shareRule.getCondition() != null) {
                                    shareRuleFound = true;
                                    final CodespaceValueType cs = shareRule.getCondition().getInterestGroup();
                                    final String codeSpace = cs.getCodespace();
                                    logger.debug("===> codespace=[" + codeSpace + "]");
                                    logger.debug("===> igCodspce=[" + igCodespace + "]");
                                    final String codespaceValue = cs.getStringValue();
                                    if (codeSpace.equals(igCodespace) &&
                                        codespaceValue.equalsIgnoreCase(interestGroup.getInterestGroupSubtype()))
                                        shareRuleMatched = true;
                                    // for now: share everything - in the future add the work
                                    // product type to workProductTypesToShare
                                    else if (codeSpace.equals(InterestGroupManagementComponent.MANUAL_CODE_SPACE) &&
                                             codespaceValue.equalsIgnoreCase(Boolean.TRUE.toString()))
                                        shareRuleMatched = true;
                                }
                    } // end if shareRule enabled
            } // end of agreement not null

        if (!agreementFound)
            // no agreement between the core and the target core
            throw new NoShareAgreementException(interestGroup.getOwningCore(), targetCore);
        else if (!shareRulesEnabled || shareRuleFound && !shareRuleMatched)
            throw new NoShareRuleInAgreementException(interestGroup.getOwningCore(),
                                                      targetCore,
                                                      interestGroup.getInterestGroupType(),
                                                      interestGroup.getInterestGroupSubtype());

        return workProductTypesToShare;
    }

    public MessageChannel getUnSubscribeChannel() {

        return unSubscribeChannel;
    }

    /**
     * Received joined interest group.
     *
     * @param message the message
     * @ssdd
     */
    @Override
    public void receivedJoinedInterestGroup(JoinedInterestGroupNotificationMessage message) {

        logger.debug("receivedJoinedInterestGroup - received notification of joined interest group id=" +
                     message.interestGroupID +
                     " owner=" +
                     message.owner +
                     " ownerProps=" +
                     message.ownerProperties);

        InterestGroup interestGroup = null;

        try {
            // persist the new interest group
            logger.debug("receivedJoinedInterestGroup - persist the joined interest group id=" +
                         message.interestGroupID);
            interestGroup = InterestGroupInfoUtil.toInterestGroup(message.getInterestGroupInfo());
            interestGroup.setSharingStatus(InterestGroupStateNotificationMessage.SharingStatus.Joined.toString());
            logger.debug("receivedJoinedInterestGroup:  ==> sharing status=" +
                         interestGroup.getSharingStatus());
            interestGroup.setOwnerProperties(message.ownerProperties);
            interestGroup.setJoinedWpTypeList(message.joinedWPTypes);
            try {
                interestGroupDAO.makePersistent(interestGroup);
            } catch (final HibernateException e) {
                logger.error("receivedJoinedInterestGroup: HibernateException makePersistent interestGroupDAO: " +
                             e.getMessage() + " from " + e.toString());
            } catch (final Exception e) {
                logger.error("receivedJoinedInterestGroup: Exception makePersistent interestGroupDAO: " +
                             e.getMessage() + " from " + e.toString());
            }

            // send upstream to the domain services that manage interest groups
            // Replace the interest group info with detailed info specific to the domain
            // interest group management services,
            // e.g. incident data for IMS
            final String detailedInfo = InterestGroupInfoUtil.toInterestDetailedInfoString(message.getInterestGroupInfo());
            message.setInterestGroupInfo(detailedInfo);
            final Message<JoinedInterestGroupNotificationMessage> notification = new GenericMessage<JoinedInterestGroupNotificationMessage>(message);
            newJoinedInterestGroupChannel.send(notification);

        } catch (final Throwable e) {
            logger.error("receivedJoinedInterestGroupHandler: error parsing received incident info");
            e.printStackTrace();
        }

    }

    private void sendInterestGroupStateNotificationMessage(InterestGroup interestGroup) {

        logger.debug("sendInterestGroupStateNotificationMessage: IGID: " +
                     interestGroup.getInterestGroupID());

        final InterestGroupStateNotificationMessage mesg = new InterestGroupStateNotificationMessage();
        mesg.setState(InterestGroupStateNotificationMessage.State.RESTORE);
        mesg.setInterestGroupID(interestGroup.getInterestGroupID());
        mesg.setInterestGroupType(interestGroup.getInterestGroupType());
        mesg.setOwningCore(interestGroup.getOwningCore());
        mesg.setSharingStatus(interestGroup.getSharingStatus());
        mesg.setExtendedMetadata(interestGroup.getExtendedMetadata());
        mesg.setSharedCoreList(interestGroup.getSharedCoreList());
        mesg.setOwmnerProperties(interestGroup.getOwnerProperties());
        mesg.setJoinedWPTypes(interestGroup.getJoinedWpTypeList());

        // set to null those values not relevant when state=RESTORE
        mesg.setInterestGroupInfo(null);
        final Message<InterestGroupStateNotificationMessage> notification = new GenericMessage<InterestGroupStateNotificationMessage>(mesg);
        interestGroupStateNotificationChannel.send(notification);
    }

    public void setAgreementDAO(AgreementDAO agreementDAO) {

        this.agreementDAO = agreementDAO;
    }

    public void setAgreementService(AgreementService agreementService) {

        this.agreementService = agreementService;
    }

    public void setDeleteJoinedInterestGroupChannel(MessageChannel deleteJoinedInterestGroupChannel) {

        this.deleteJoinedInterestGroupChannel = deleteJoinedInterestGroupChannel;
    }

    public void setDirectoryService(DirectoryService directoryService) {

        this.directoryService = directoryService;
    }

    public void setInterestGroupDAO(InterestGroupDAO interestGroupDAO) {

        this.interestGroupDAO = interestGroupDAO;
    }

    public void setInterestGroupStateNotificationChannel(MessageChannel interestGroupStateNotificationChannel) {

        this.interestGroupStateNotificationChannel = interestGroupStateNotificationChannel;
    }

    public void setNewJoinedInterestGroupChannel(MessageChannel newJoinedInterestGroupChannel) {

        this.newJoinedInterestGroupChannel = newJoinedInterestGroupChannel;
    }

    public void setUnSubscribeChannel(MessageChannel unSubscribeChannel) {

        this.unSubscribeChannel = unSubscribeChannel;
    }

    /**
     * Share interest group.
     *
     * @param interestGroupID the interest group id
     * @param targetCore the target core
     * @param detailedInfo the detailed info
     * @param agreementChecked the agreement checked
     *
     * @throws InvalidInterestGroupIDException the invalid interest group id exception
     * @throws LocalCoreNotOnlineException the local core not online exception
     * @throws RemoteCoreUnavailableException the remote core unavailable exception
     * @throws XMPPComponentException the XMPP component exception
     * @throws NoShareAgreementException the no share agreement exception
     * @throws NoShareRuleInAgreementException the no share rule in agreement exception
     * @ssdd
     */
    @Override
    public void shareInterestGroup(String interestGroupID,
                                   String targetCore,
                                   String detailedInfo,
                                   boolean agreementChecked)
        throws InvalidInterestGroupIDException, LocalCoreNotOnlineException,
        RemoteCoreUnavailableException, RemoteCoreUnknownException, XMPPComponentException,
        NoShareAgreementException, NoShareRuleInAgreementException {

        logger.debug("shareInterestGroup: igID=" + interestGroupID + " targetCore=" + targetCore +
                     " detailedInfo=[" + detailedInfo + "]");
        final InterestGroup interestGroup = interestGroupDAO.findByInterestGroup(interestGroupID);
        if (interestGroup == null)
            throw new InvalidInterestGroupIDException(interestGroupID);

        // to let XMPP to handle the remote core is offline
        /*
        CoreConfigType coreConfig = directoryService.getCoreConfig(configurationService.getCoreName());
        if (coreConfig == null || coreConfig.getOnlineStatus() != CoreStatusType.ONLINE) {
            throw new LocalCoreNotOnlineException();
        } else {
        CoreConfigType remoteCoreConfig = directoryService.getCoreConfig(targetCore);

        if (remoteCoreConfig == null) {
            throw new RemoteCoreUnknownException(targetCore);
        } else if (remoteCoreConfig.getOnlineStatus() != CoreStatusType.ONLINE) {
            throw new RemoteCoreUnavailableException(targetCore);
        } else {
         */

        // NULL workProductTypesToShare list means agreement indicates no sharing
        List<String> workProductTypesToShare = new ArrayList<String>();
        if (!agreementChecked)
            workProductTypesToShare = getShareAgreement(interestGroup, targetCore);
        if (workProductTypesToShare == null)
            return;
        try {

            // send interest group state change to Comms
            final InterestGroupStateNotificationMessage mesg = new InterestGroupStateNotificationMessage();
            mesg.setState(InterestGroupStateNotificationMessage.State.SHARE);
            mesg.setInterestGroupID(interestGroup.getInterestGroupID());
            mesg.setInterestGroupType(interestGroup.getInterestGroupType());
            mesg.setOwningCore(interestGroup.getOwningCore());
            mesg.setExtendedMetadata(interestGroup.getExtendedMetadata());
            mesg.getSharedCoreList().add(targetCore);
            mesg.setWorkProductTypesToShare(workProductTypesToShare);
            mesg.setInterestGroupInfo(InterestGroupInfoUtil.toXMLString(interestGroup, detailedInfo));
            mesg.setSharingStatus(interestGroup.getSharingStatus());
            final Message<InterestGroupStateNotificationMessage> notification = new GenericMessage<InterestGroupStateNotificationMessage>(mesg);
            interestGroupStateNotificationChannel.send(notification);
        } catch (final IllegalStateException e) {
            throw new RemoteCoreUnavailableException("core unavailable");
        } catch (final Exception e) {
            logger.error("shareInterestGroup - exception caught from the XMPP component");
            e.printStackTrace();
            throw new XMPPComponentException(e.getMessage());
        }

        // TODO: RDW These next lines need to happen after getting the response from
        // interestGroupStateNotificationChannel.send(notification);
        interestGroup.setSharingStatus(InterestGroupStateNotificationMessage.SharingStatus.Shared.toString());
        interestGroup.getSharedCoreList().add(targetCore);

        logger.debug("shareInterestGroup: update sharingStatus [" +
                     InterestGroupStateNotificationMessage.SharingStatus.Shared.toString() +
                     "] and persist the shared interest group id=" +
                     interestGroup.getInterestGroupID());
        try {
            interestGroupDAO.makePersistent(interestGroup);
        } catch (final HibernateException e) {
            logger.error("shareInterestGroup: HibernateException makePersistent interestGroupDAO: " +
                         e.getMessage() + " from " + e.toString());
        } catch (final Exception e) {
            logger.error("shareInterestGroup: Exception makePersistent interestGroupDAO: " +
                         e.getMessage() + " from " + e.toString());
        }
        /* TODO - to let XMPP to handle remote core is offline }
        }
         */
    }

    /**
     * System initialized handler.
     *
     * @param message the message
     */
    @Override
    public void systemInitializedHandler(String message) {

        logger.debug("systemInitializedHandler: ... start ...");

        final List<InterestGroup> igList = interestGroupDAO.findAll();
        logger.debug("systemInitializedHandler: found " + igList.size() +
                     " interest groups in database");
        for (final InterestGroup ig : igList) {
            logger.debug("systemInitializedHandler: owningCoreJID: " + ig.getOwningCore() +
                         ", local coreJID: " + getDirectoryService().getLocalCoreJid());
            if (!ig.getOwningCore().equalsIgnoreCase(getDirectoryService().getLocalCoreJid())) {
                logger.debug("systemInitializedHandler: not to restore till the remote core: " +
                             ig.getOwningCore() + " is online");
                continue;
            }
            logger.debug("systemInitializedHandler: restore IGID: " + ig.getInterestGroupID());
            sendInterestGroupStateNotificationMessage(ig);
        }

        logger.debug("systemInitializedHandler: ... done ...");
    }

    private InterestGroupInfo toInterestGroupInfo(InterestGroup ig) {

        InterestGroupInfo info = null;
        if (ig != null) {
            info = new InterestGroupInfo();
            info.setInterestGroupID(ig.getInterestGroupID());
            info.setInterestGroupType(ig.getInterestGroupType());
            info.setInterestGroupSubType(ig.getInterestGroupSubtype());
            info.setName(ig.getName());
            info.setDescription(ig.getDescription());
            info.setOwningCore(ig.getOwningCore());
            info.setExtendedMetadata(ig.getExtendedMetadata());
        }
        return info;
    }

    @Override
    public void unshareInterestGroup(String interestGroupID, String targetCore) {

        // TODO Auto-generated method stub

    }

    /**
     * Update interest group.
     *
     * @param interestGroupInfo the interest group info
     *
     * @throws InvalidInterestGroupIDException the invalid interest group id exception
     * @ssdd
     */
    @Override
    public void updateInterestGroup(InterestGroupInfo interestGroupInfo)
        throws InvalidInterestGroupIDException {

        final InterestGroup interestGroup = interestGroupDAO.findByInterestGroup(interestGroupInfo.getInterestGroupID());
        if (interestGroup == null)
            throw new InvalidInterestGroupIDException(interestGroupInfo.getInterestGroupID());
        else {
            interestGroup.setInterestGroupSubtype(interestGroupInfo.getInterestGroupSubType());
            interestGroup.setDescription(interestGroupInfo.getDescription());
            interestGroup.setName(interestGroupInfo.getName());
            interestGroup.setExtendedMetadata(interestGroupInfo.getExtendedMetadata());

            logger.debug("updateInterestGroup: " + interestGroupInfo.toString());
            try {
                interestGroupDAO.makePersistent(interestGroup);
            } catch (final HibernateException e) {
                logger.error("updateInterestGroup: HibernateException makePersistent interestGroupDAO: " +
                             e.getMessage() + " from " + e.toString());
            } catch (final Exception e) {
                logger.error("updateInterestGroup: Exception makePersistent interestGroupDAO: " +
                             e.getMessage() + " from " + e.toString());
            }

            // send interest group state change to Comms
            // Note: although comms does not take any action when receiving this state change, we
            // send it anyway in case this needs to be handled differently in the future.
            final InterestGroupStateNotificationMessage mesg = new InterestGroupStateNotificationMessage();
            mesg.setState(InterestGroupStateNotificationMessage.State.UPDATE);
            mesg.setInterestGroupID(interestGroupInfo.getInterestGroupID());
            mesg.setInterestGroupType(interestGroupInfo.getInterestGroupType());
            mesg.setOwningCore(interestGroupInfo.getOwningCore());
            mesg.setSharingStatus(interestGroup.getSharingStatus());
            mesg.setExtendedMetadata(interestGroupInfo.getExtendedMetadata());
            // set to null those values not relevant when state=UPDATE
            mesg.setInterestGroupInfo(null);
            final Message<InterestGroupStateNotificationMessage> notification = new GenericMessage<InterestGroupStateNotificationMessage>(mesg);
            interestGroupStateNotificationChannel.send(notification);
        }
    }

}
