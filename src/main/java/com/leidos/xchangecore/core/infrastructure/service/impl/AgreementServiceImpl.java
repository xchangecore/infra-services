package com.leidos.xchangecore.core.infrastructure.service.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.message.GenericMessage;
import org.uicds.agreementService.AgreementListType;
import org.uicds.agreementService.AgreementType;
import org.uicds.directoryServiceData.WorkProductTypeListType;

import com.leidos.xchangecore.core.infrastructure.dao.AgreementDAO;
import com.leidos.xchangecore.core.infrastructure.exceptions.AgreementWithCoreExists;
import com.leidos.xchangecore.core.infrastructure.exceptions.MissingConditionInShareRuleException;
import com.leidos.xchangecore.core.infrastructure.exceptions.SOAPServiceException;
import com.leidos.xchangecore.core.infrastructure.messages.AgreementRosterMessage;
import com.leidos.xchangecore.core.infrastructure.messages.DeleteInterestGroupForRemoteCoreMessage;
import com.leidos.xchangecore.core.infrastructure.model.Agreement;
import com.leidos.xchangecore.core.infrastructure.model.CodeSpaceValueType;
import com.leidos.xchangecore.core.infrastructure.model.ExtendedMetadata;
import com.leidos.xchangecore.core.infrastructure.model.ShareRule;
import com.leidos.xchangecore.core.infrastructure.service.AgreementService;
import com.leidos.xchangecore.core.infrastructure.service.ConfigurationService;
import com.leidos.xchangecore.core.infrastructure.service.DirectoryService;
import com.leidos.xchangecore.core.infrastructure.util.AgreementUtil;
import com.leidos.xchangecore.core.infrastructure.util.LdapUtil;
import com.saic.precis.x2009.x06.base.CodespaceValueType;

/**
 * The AgreementsService implementation.
 *
 * @author William Summers
 * @since 1.0
 * @see com.leidos.xchangecore.core.infrastructure.model.Agreement Agreement Data Model
 * @see com.leidos.xchangecore.core.infrastructure.model.CodeSpaceValueType CodeSpaceValueType Data Model
 * @see com.leidos.xchangecore.core.infrastructure.model.ShareRule ShareRule Data Model
 * @ssdd
 */
public class AgreementServiceImpl
implements AgreementService {

    Logger logger = LoggerFactory.getLogger(AgreementServiceImpl.class);

    private ConfigurationService configService;
    private DirectoryService directoryService;

    private AgreementDAO dao;

    private LdapUtil ldapUtil;

    private MessageChannel agreementRosterChannel;

    private MessageChannel deleteInterestGroupSharedFromRemoteCoreChannel;

    /**
     * Creates a core to core agreement and enables share rules if specified. Processes share rules
     * for specific work product types. Note that the remote core must also create an agreement with
     * the local core to be reciprocal.
     *
     * @param agreementType the agreement type
     *
     * @return the agreement type
     *
     * @throws MissingConditionInShareRuleException the missing condition in share rule exception
     * @throws AgreementWithCoreExists the agreement with core already exists
     * @ssdd
     */
    @Override
    public AgreementType createAgreement(AgreementType agreementType) throws SOAPServiceException {

        logger.debug("createAgreement: " + agreementType.getPrincipals().xmlText());

        if (agreementType.getPrincipals().getRemoteCore() == null ||
            agreementType.getPrincipals().getRemoteCore().isNil())
            throw new SOAPServiceException("Create Agreement: Remote core is null in agreement request");
        if (agreementType.getPrincipals().getLocalCore() == null ||
            agreementType.getPrincipals().getLocalCore().isNil())
            throw new SOAPServiceException("Create Agreement: Local core is null in agreement request");

        if (agreementType.getShareRules() == null)
            throw new SOAPServiceException("Create Agreement: missing Shared Rules");

        Agreement agreement = new Agreement();
        if (agreementType.getDescription() != null)
            agreement.setDescription(agreementType.getDescription());
        // Set the Consumer
        final CodeSpaceValueType remoteCore = new CodeSpaceValueType();
        if (agreementType.getPrincipals().getRemoteCore().getLabel() != null)
            remoteCore.setLabel(agreementType.getPrincipals().getRemoteCore().getLabel());
        remoteCore.setValue(agreementType.getPrincipals().getRemoteCore().getStringValue());
        agreement.setRemoteCore(remoteCore);

        // Set the Provider
        final CodeSpaceValueType localCore = new CodeSpaceValueType();
        if (agreementType.getPrincipals().getLocalCore().getLabel() != null)
            localCore.setLabel(agreementType.getPrincipals().getLocalCore().getLabel());
        localCore.setValue(agreementType.getPrincipals().getLocalCore().getStringValue());
        agreement.setLocalCore(localCore);

        isValidateJIDsAndGroups(agreement.getLocalValue(), agreement.getRemoteValue());

        // set enable field for ruleSet
        agreement.setEnabled(agreementType.getShareRules().getEnabled());

        if (agreementType.getShareRules() != null &&
            agreementType.getShareRules().sizeOfShareRuleArray() > 0) {

            // Set the Share Rules
            final HashSet<ShareRule> shareRules = new HashSet<ShareRule>();

            for (final AgreementType.ShareRules.ShareRule shareRule : agreementType.getShareRules().getShareRuleArray()) {
                final ShareRule rule = new ShareRule();
                if (shareRule.getId() == null) {
                    logger.error("createAgreement: shareRuleID: is null");
                    throw new SOAPServiceException("Create Agreement: shareRuleID not set");
                } else {
                    logger.info("createAgreement: shareRuleID: " + shareRule.getId());
                    rule.setRuleID(shareRule.getId());
                }
                rule.setEnabled(shareRule.getEnabled());

                if (shareRule.getCondition() == null)
                    throw new SOAPServiceException("Create Agreement: Shared Rule's condition is null");
                else {
                    final CodeSpaceValueType interestGroup = new CodeSpaceValueType();
                    if (shareRule.getCondition().getInterestGroup().getCodespace() != null)
                        interestGroup.setCodeSpace(shareRule.getCondition().getInterestGroup().getCodespace());
                    if (shareRule.getCondition().getInterestGroup().getLabel() != null)
                        interestGroup.setLabel(shareRule.getCondition().getInterestGroup().getLabel());
                    if (shareRule.getCondition().getInterestGroup().getStringValue() != null)
                        interestGroup.setValue(shareRule.getCondition().getInterestGroup().getStringValue());

                    logger.debug("createAgreement: creating extended Metadata: count: " +
                                 shareRule.getCondition().getExtendedMetadataArray().length);
                    final Set<ExtendedMetadata> extendedMetadataSet = new HashSet<ExtendedMetadata>();
                    if (shareRule.getCondition().getExtendedMetadataArray() != null &&
                        shareRule.getCondition().getExtendedMetadataArray().length > 0) {
                        ExtendedMetadata em = null;
                        for (int i = 0; i < shareRule.getCondition().getExtendedMetadataArray().length; i++) {

                            em = new ExtendedMetadata();
                            em.setCode(shareRule.getCondition().getExtendedMetadataArray(i).getCode());
                            em.setCodespace(shareRule.getCondition().getExtendedMetadataArray(i).getCodespace());
                            em.setLabel(shareRule.getCondition().getExtendedMetadataArray(i).getLabel());
                            em.setValue(shareRule.getCondition().getExtendedMetadataArray(i).getStringValue());

                            // logger.debug("createAgreement: create ExtendedMetadata: " + em);

                            extendedMetadataSet.add(em);
                        }
                        rule.setExtendedMetadata(extendedMetadataSet);
                        logger.debug("createAgreement: added extendedmetadata to the rule: count: " +
                                     rule.getExtendedMetadata().size());
                    } else
                        logger.debug("createAgreement: no extended metadata in share rule");

                    if (shareRule.getCondition().getRemoteCoreProximity() != null) {
                        if (agreement.getRemoteJIDs().size() > 0) {
                            final String message = "Create Agreement: Can only sepecify proximity when the target are groups";
                            logger.error(message);
                            throw new SOAPServiceException(message);
                        }
                        logger.debug("createAgreement: Share Rule has remote core proximity " +
                                     shareRule.getCondition().getRemoteCoreProximity().getStringValue());
                        logger.debug("createAgreement: Share Rule has share on no location - " +
                                     shareRule.getCondition().getRemoteCoreProximity().getShareOnNoLoc());
                        rule.setRemoteCoreProximity(shareRule.getCondition().getRemoteCoreProximity().getStringValue());
                        rule.setShareOnNoLoc(Boolean.valueOf(
                            shareRule.getCondition().getRemoteCoreProximity().getShareOnNoLoc()).toString());
                    } else
                        logger.debug("createAgreement: no remote core proximity in share rule");

                    rule.setInterestGroup(interestGroup);

                }

                if (shareRule.getWorkProducts() != null) {

                    final HashSet<CodeSpaceValueType> workProducts = new HashSet<CodeSpaceValueType>();
                    int j = 0;
                    for (final CodespaceValueType type : shareRule.getWorkProducts().getTypeArray()) {
                        final CodeSpaceValueType workProduct = new CodeSpaceValueType();

                        if (type.getCodespace() != null)
                            workProduct.setCodeSpace(type.getCodespace());
                        if (type.getLabel() != null)
                            workProduct.setLabel(type.getLabel());
                        if (type.getStringValue() != null)
                            workProduct.setValue(type.getStringValue());

                        workProducts.add(workProduct);
                        j++;
                    }

                    rule.setWorkProducts(workProducts);
                }

                shareRules.add(rule);

            }

            // add to model
            agreement.setShareRules(shareRules);
        }

        // persist the agreement
        AgreementType response = null;
        try {
            agreement = getDao().makePersistent(agreement);
            if (agreement != null) {
                logger.debug("createAgreement: " + agreement);
                response = AgreementUtil.copyProperties(agreement);

                // send a new agreement roster data to CommsXMPP
                sendAgreementRosterUpdate(agreement, AgreementRosterMessage.State.CREATE);
            } else {
                logger.error("createAgreement: error persisting agreement object");
                response = AgreementType.Factory.newInstance();
            }
        } catch (final Exception e) {
            logger.error("createAgreement: exception occurred persisting agreement object");
            throw new SOAPServiceException("Create Agreement: Cannot persist the model: " +
                e.getMessage());
        }

        return response;
    }

    @Override
    public void deleteInterestGroupSharedFromRemoteCoreHandler(DeleteInterestGroupForRemoteCoreMessage msg) {

        final String remoteJID = msg.getRemoteCoreName();
        logger.debug("deleteInterestGroupFromRemoteCoreHandler: remoteCore: " + remoteJID);
        if (!remoteJID.equalsIgnoreCase(getDirectoryService().getLocalCoreJid())) {
            final Agreement agreement = getDao().findByRemoteCoreName(remoteJID);
            if (agreement != null) {
                logger.debug("deleteInterestGroupFromRemoteCoreHandlerremoteJID: " + remoteJID +
                    " has rescinded the agreement and set mutuallyAgreed to false");
                agreement.setMutuallyAgreed(false);
                getDao().makePersistent(agreement);
            }
        }
    }

    /**
     * Gets the agreement.
     *
     * @param coreID the core id of the remote core
     *
     * @return the agreement
     * @ssdd
     */
    @Override
    public AgreementType getAgreement(int agreementID) {

        logger.debug("getAgreement: agreementID: " + agreementID);
        final Agreement agreement = getDao().findById(agreementID);
        AgreementType response = null;

        if (agreement != null)
            response = AgreementUtil.copyProperties(agreement);

        return response;
    }

    /**
     * Gets the list of agreements for all joined cores.
     *
     * @return the agreement list
     * @ssdd
     */
    @Override
    public AgreementListType getAgreementList() {

        final AgreementListType response = AgreementListType.Factory.newInstance();

        final List<Agreement> agreements = getDao().findAll();
        logger.debug("getAgreementList: lengthe: " + agreements.size());

        if (agreements.size() > 0) {
            final AgreementType[] agreementTypes = new AgreementType[agreements.size()];
            for (int i = 0; i < agreements.size(); i++)
                agreementTypes[i] = AgreementUtil.copyProperties(agreements.get(i));
            response.setAgreementArray(agreementTypes);
        }
        return response;
    }

    /** {@inheritDoc} */
    @Override
    public ConfigurationService getConfigurationService() {

        return configService;
    }

    /** {@inheritDoc} */
    public AgreementDAO getDao() {

        return dao;
    }

    public MessageChannel getDeleteInterestGroupSharedFromRemoteCoreChannel() {

        return deleteInterestGroupSharedFromRemoteCoreChannel;
    }

    /** {@inheritDoc} */
    @Override
    public DirectoryService getDirectoryService() {

        return directoryService;
    }

    public LdapUtil getLdapUtil() {

        return ldapUtil;
    }

    @Override
    public String[] getListOfUsersSharedWith(String user, String incdientType) {

        final List<Agreement> agreementList = dao.findAll();
        for (final Agreement agreement : agreementList) {
            if (!agreement.isEnabled())
                // if the agreement is inactive at this moment then ignore this agreement
                continue;
            if (!agreement.getLocalCorename().equalsIgnoreCase(agreement.getRemoteCorename())) {
                logger.debug("Inter-Core agreement: LocalJID: " + agreement.getLocalCorename() +
                             ", RemoteJID: " + agreement.getRemoteCorename());
                continue;
            }
        }
        return null;
    }

    // return the real core name from agreement's remote core
    private String getRealCoreName(String remoteCoreNameFQN) {

        final int index = remoteCoreNameFQN.indexOf("?");
        return index != -1 ? remoteCoreNameFQN.substring(0, index) : remoteCoreNameFQN;
    }

    // check whether this core name is same as local core name
    private boolean isLocalCore(String remoteCoreNameFQN) {

        return directoryService.getCoreName().equalsIgnoreCase(getRealCoreName(remoteCoreNameFQN));
    }

    private void isValidateJIDsAndGroups(String localJIDs, String remoteJIDs)
        throws SOAPServiceException {

        List<String> members = getLdapUtil().listOfUsers();
        isValidMember(AgreementUtil.getIDs(localJIDs, AgreementUtil.N_IDS), members, true);
        isValidMember(AgreementUtil.getIDs(remoteJIDs, AgreementUtil.N_IDS), members, true);
        members = getLdapUtil().listOfGroup();
        isValidMember(AgreementUtil.getIDs(localJIDs, AgreementUtil.N_GROUPS), members, false);
        isValidMember(AgreementUtil.getIDs(remoteJIDs, AgreementUtil.N_GROUPS), members, false);
    }

    private void isValidMember(Set<String> ids, List<String> members, boolean isUser)
        throws SOAPServiceException {

        for (final String id : ids) {
            boolean found = false;
            for (final String member : members)
                if (id.equalsIgnoreCase(member)) {
                    found = true;
                    break;
                }
            if (found == false) {
                final String message = (isUser ? "User: " : "Group: ") + id + " does not exist";
                logger.error(message);
                throw new SOAPServiceException(message);
            }
        }
    }

    /**
     * Rescind agreement.
     *
     * @param coreID the core id
     *
     * @return the string
     * @ssdd
     */
    @Override
    public boolean rescindAgreement(int agreementID) {

        logger.debug("rescindAgreement: " + agreementID);
        final Agreement agreement = getDao().findById(agreementID);
        if (agreement == null) {
            logger.error("Could not find agreement for: " + agreementID);
            return false;
        }

        // delete the model
        getDao().makeTransient(agreement);

        if (isLocalCore(agreement.getRemoteCorename())) {
            logger.debug("rescindAgreement: agreementID: " + agreementID +
                " is intra-core agreement");
            return true;
        }

        // send a rescinded agreement roster data to CommsXMPP
        sendAgreementRosterUpdate(agreement, AgreementRosterMessage.State.RESCIND);

        // request InterestGroupManagementComponent to remove all the interest groups
        // shared from remote core
        try {
            final DeleteInterestGroupForRemoteCoreMessage message = new DeleteInterestGroupForRemoteCoreMessage(agreement.getRemoteCorename());
            final Message<DeleteInterestGroupForRemoteCoreMessage> theMessage = new GenericMessage<DeleteInterestGroupForRemoteCoreMessage>(message);
            deleteInterestGroupSharedFromRemoteCoreChannel.send(theMessage);
        } catch (final Exception e) {
            logger.error("rescindAgreement: send DeleteInterestGroupForRemoteCoreMessage: " +
                agreement.getRemoteCorename() + ": " + e.getMessage());
        }

        return true;
    }

    /**
     * Send agreement roster update.
     *
     * @param agreement the agreement
     * @param state the state
     * @ssdd
     */
    public void sendAgreementRosterUpdate(Agreement agreement, AgreementRosterMessage.State state) {

        final String realRemoteCoreName = getRealCoreName(agreement.getRemoteCorename());
        logger.debug("sendAgreementRosterUpdate: Agreement's real remote core name: " +
            realRemoteCoreName);

        if (isLocalCore(realRemoteCoreName)) {
            logger.debug("sendAgreementRosterUpdate: the remote core: " +
                agreement.getRemoteCore().getValue() +
                " is the local core, no message need to be sent");
            return;
        }

        logger.debug("sendAgreementRosterUpdate: send status for Core: " + realRemoteCoreName);

        final Map<String, AgreementRosterMessage.State> cores = new HashMap<String, AgreementRosterMessage.State>();

        // First time we see this (even if it is from the database), so send a "CREATE" state
        cores.put(realRemoteCoreName, state);

        final AgreementRosterMessage message = new AgreementRosterMessage(agreement.getId(), cores);

        final Message<AgreementRosterMessage> notification = new GenericMessage<AgreementRosterMessage>(message);

        agreementRosterChannel.send(notification);
    }

    /**
     * Send initial agreement roster.
     *
     * @ssdd
     */
    public void sendInitialAgreementRoster() {

        final List<Agreement> agreements = getDao().findAll();

        final Set<String> remoteCoreSet = new HashSet<String>();
        for (final Agreement agreement : agreements) {
            final Map<String, AgreementRosterMessage.State> cores = new HashMap<String, AgreementRosterMessage.State>();

            final String realRemoteCoreName = getRealCoreName(agreement.getRemoteCorename());
            if (isLocalCore(realRemoteCoreName)) {
                logger.debug("sendInitialAgreementRoster: this is local core, no initial message needs to be sent");
                continue;
            }

            logger.debug("sendInitialAgreementRoster: send status for " + realRemoteCoreName);

            if (remoteCoreSet.contains(realRemoteCoreName)) {
                logger.error("sendInitialAgreementRoster: duplicate agreement for remote core:" +
                    realRemoteCoreName);
                continue;
            }

            // First time we see this (even if it's from the database), so send a "CREATE" state
            cores.put(realRemoteCoreName, AgreementRosterMessage.State.CREATE);

            // send out an intial roster for each agreement
            final AgreementRosterMessage message = new AgreementRosterMessage(agreement.getId(),
                                                                              cores);
            final Message<AgreementRosterMessage> notification = new GenericMessage<AgreementRosterMessage>(message);
            agreementRosterChannel.send(notification);
            // save the remote core
            remoteCoreSet.add(realRemoteCoreName);
        }
    }

    public void setAgreementRosterChannel(MessageChannel agreementRosterChannel) {

        this.agreementRosterChannel = agreementRosterChannel;
    }

    /** {@inheritDoc} */
    @Override
    public void setConfigurationService(ConfigurationService service) {

        configService = service;
    }

    /** {@inheritDoc} */
    public void setDao(AgreementDAO dao) {

        this.dao = dao;
    }

    public void setDeleteInterestGroupSharedFromRemoteCoreChannel(MessageChannel deleteInterestGroupSharedFromRemoteCoreChannel) {

        this.deleteInterestGroupSharedFromRemoteCoreChannel = deleteInterestGroupSharedFromRemoteCoreChannel;
    }

    /** {@inheritDoc} */
    @Override
    public void setDirectoryService(DirectoryService service) {

        directoryService = service;
    }

    public void setLdapUtil(LdapUtil ldapUtil) {

        this.ldapUtil = ldapUtil;
    }

    /** {@inheritDoc} */
    @Override
    public void systemInitializedHandler(String messgae) {

        logger.debug("systemInitializedHandler: ... start ...");
        final String urn = getConfigurationService().getServiceNameURN(AGREEMENT_SERVICE_NAME);
        final WorkProductTypeListType publishedProducts = WorkProductTypeListType.Factory.newInstance();
        final WorkProductTypeListType subscribedProducts = WorkProductTypeListType.Factory.newInstance();
        directoryService.registerUICDSService(urn, AGREEMENT_SERVICE_NAME, publishedProducts,
            subscribedProducts);
        sendInitialAgreementRoster();
        logger.debug("systemInitializedHandler: ... done ...");
    }

    /**
     * Update agreement to enable/disable share rules and to replace the list of share rules
     *
     * @param remoteCoreID the remote core id
     * @param agreementType the agreement type
     *
     * @return the agreement type
     * @ssdd
     */
    @Override
    public AgreementType updateAgreement(AgreementType agreementType) throws SOAPServiceException {

        logger.debug("updateAgreement: agreementID: " + agreementType.getId());

        final Agreement agreement = getDao().findById(agreementType.getId());
        if (agreement == null)
            return AgreementType.Factory.newInstance();

        isValidateJIDsAndGroups(agreementType.getPrincipals().getLocalCore().getStringValue(),
            agreementType.getPrincipals().getRemoteCore().getStringValue());

        if (agreementType.getDescription() != null && agreementType.getDescription().length() > 0)
            agreement.setDescription(agreementType.getDescription());

        // setup the local JIDs/Groups
        agreement.setLocalValue(agreementType.getPrincipals().getLocalCore().getStringValue());
        // setup the remote JIDs/Groups
        agreement.setRemoteValue(agreementType.getPrincipals().getRemoteCore().getStringValue());
        // validate the JIDs and Groups

        // set enable field for ruleSet
        agreement.setEnabled(agreementType.getShareRules().getEnabled());

        //TODO to check the difference instead of just set with the new values.

        if (agreementType.getShareRules() != null &&
            agreementType.getShareRules().sizeOfShareRuleArray() > 0) {

            // Set the Share Rules
            // ShareRule[] shareRulesArray = new ShareRule[length];
            final HashSet<ShareRule> shareRules = new HashSet<ShareRule>();

            for (final AgreementType.ShareRules.ShareRule shareRule : agreementType.getShareRules().getShareRuleArray()) {
                final ShareRule rule = new ShareRule();
                rule.setRuleID(shareRule.getId());
                rule.setEnabled(shareRule.getEnabled());

                final CodeSpaceValueType interestGroup = new CodeSpaceValueType();
                if (shareRule.getCondition().getInterestGroup().getCodespace() != null)
                    interestGroup.setCodeSpace(shareRule.getCondition().getInterestGroup().getCodespace());
                if (shareRule.getCondition().getInterestGroup().getLabel() != null)
                    interestGroup.setLabel(shareRule.getCondition().getInterestGroup().getLabel());
                if (shareRule.getCondition().getInterestGroup().getStringValue() != null)
                    interestGroup.setValue(shareRule.getCondition().getInterestGroup().getStringValue());

                logger.debug("updateAgreement: adding extended metadata: count: " +
                             shareRule.getCondition().getExtendedMetadataArray().length);

                final Set<ExtendedMetadata> extendedMetadataSet = new HashSet<ExtendedMetadata>();

                if (shareRule.getCondition().getExtendedMetadataArray() != null &&
                    shareRule.getCondition().getExtendedMetadataArray().length > 0) {

                    ExtendedMetadata em = null;
                    for (int i = 0; i < shareRule.getCondition().getExtendedMetadataArray().length; i++) {

                        em = new ExtendedMetadata();
                        em.setCode(shareRule.getCondition().getExtendedMetadataArray(i).getCode());
                        em.setCodespace(shareRule.getCondition().getExtendedMetadataArray(i).getCodespace());
                        em.setLabel(shareRule.getCondition().getExtendedMetadataArray(i).getLabel());
                        em.setValue(shareRule.getCondition().getExtendedMetadataArray(i).getStringValue());

                        // logger.debug("updateAgreement: adding: " + em);
                        extendedMetadataSet.add(em);
                    }
                    logger.debug("updateAgreement: added extended metadata: count: " +
                                 extendedMetadataSet.size());
                    rule.setExtendedMetadata(extendedMetadataSet);
                    logger.debug("updateAgreement: rule's extended metadata: " +
                        rule.getExtendedMetadata().size());
                }

                if (shareRule.getCondition().getRemoteCoreProximity() != null) {
                    if (agreement.getRemoteJIDs().size() > 0) {
                        final String message = "Update Agreement: Can only sepecify proximity when the target are groups";
                        logger.error(message);
                        throw new SOAPServiceException(message);
                    }
                    rule.setRemoteCoreProximity(shareRule.getCondition().getRemoteCoreProximity().getStringValue());
                    rule.setShareOnNoLoc(Boolean.valueOf(
                        shareRule.getCondition().getRemoteCoreProximity().getShareOnNoLoc()).toString());
                }

                rule.setInterestGroup(interestGroup);

                if (shareRule.getWorkProducts() != null) {
                    final HashSet<CodeSpaceValueType> workProducts = new HashSet<CodeSpaceValueType>();
                    int j = 0;
                    for (final CodespaceValueType type : shareRule.getWorkProducts().getTypeArray()) {
                        final CodeSpaceValueType workProduct = new CodeSpaceValueType();

                        if (type.getCodespace() != null)
                            workProduct.setCodeSpace(type.getCodespace());
                        if (type.getLabel() != null)
                            workProduct.setLabel(type.getLabel());
                        if (type.getStringValue() != null)
                            workProduct.setValue(type.getStringValue());

                        workProducts.add(workProduct);
                        j++;
                    }

                    rule.setWorkProducts(workProducts);
                }

                // shareRulesArray[i] = rule;
                // i++;
                shareRules.add(rule);
            }

            // add to model
            // if only it were this simple
            // model.setShareRules(shareRulesArray);
            agreement.setShareRules(shareRules);

        } else
            agreement.setShareRules(new HashSet<ShareRule>());

        // persist the agreement
        Agreement model2 = null;
        AgreementType response = null;
        try {
            model2 = getDao().makePersistent(agreement);
            if (model2 != null) {
                logger.debug("Updated Agreement: " + model2.getRemoteCore().getValue());
                response = AgreementUtil.copyProperties(model2);

                // send a new agreement roster data to CommsXMPP
                sendAgreementRosterUpdate(model2, AgreementRosterMessage.State.CREATE);
            } else {
                logger.error("error persisting agreement object");
                response = AgreementType.Factory.newInstance();
            }
        } catch (final Exception e) {
            logger.debug("exception occurred persisting agreement object");
            e.printStackTrace();
        }

        return response;
    }
}
