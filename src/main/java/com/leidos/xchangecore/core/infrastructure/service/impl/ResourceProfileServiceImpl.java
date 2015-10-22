package com.leidos.xchangecore.core.infrastructure.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.oasisOpen.docs.wsn.b2.FilterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uicds.directoryServiceData.WorkProductTypeListType;
import org.uicds.resourceProfileService.Interest;
import org.uicds.resourceProfileService.ResourceProfile;
import org.uicds.resourceProfileService.ResourceProfileListType;

import com.leidos.xchangecore.core.infrastructure.dao.ResourceProfileDAO;
import com.leidos.xchangecore.core.infrastructure.model.CodeSpaceValueType;
import com.leidos.xchangecore.core.infrastructure.model.ResourceProfileModel;
import com.leidos.xchangecore.core.infrastructure.service.DirectoryService;
import com.leidos.xchangecore.core.infrastructure.service.ResourceProfileService;
import com.leidos.xchangecore.core.infrastructure.service.WorkProductService;
import com.leidos.xchangecore.core.infrastructure.util.ResourceProfileUtil;
import com.leidos.xchangecore.core.infrastructure.util.ServiceNamespaces;
import com.saic.precis.x2009.x06.base.CodespaceValueType;
import com.saic.precis.x2009.x06.base.IdentifierType;

/**
 * Implements the ResourceProfileService interface.
 *
 * @see com.leidos.xchangecore.core.infrastructure.model.ResourceProfileModel ResourceProfile Data Model
 * @author Andre Bonner
 * @ssdd
 */
public class ResourceProfileServiceImpl
    implements ResourceProfileService, ServiceNamespaces {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private WorkProductService workProductService;
    private ResourceProfileDAO resourceProfileDAO;
    private DirectoryService directoryService;

    /**
     * Adds an interest to a profile
     *
     * @param profileID the profile id
     * @param interest the interest
     *
     * @return true, if successful
     * @ssdd
     */
    @Override
    public boolean addInterest(IdentifierType profileID, Interest interest) {

        boolean added = false;
        // Get the profile
        final ResourceProfileModel profile = resourceProfileDAO.findByIdentifier(profileID.getStringValue());
        if (profile != null)
            // if this returns true, then persist the profile
            if (profile.addInterest(ResourceProfileUtil.copyProperties(interest))) {
                added = true;
                resourceProfileDAO.makePersistent(profile);
            }

        return added;
    }

    /**
     * Creates the profile with subscriptions.
     *
     * @param profile the profile
     *
     * @return the resource profile model
     * @ssdd
     */
    @Override
    public ResourceProfileModel createProfile(ResourceProfile profile) {

        if (profile == null) {
            logger.error("Problem with profile in the request: no profile");
            return null;
        }
        // Create ResourceProfile model and create subscriptions
        ResourceProfileModel profileModel = ResourceProfileUtil.copyProperties(profile);

        // Persist profile to database
        profileModel = resourceProfileDAO.makePersistent(profileModel);

        return profileModel;
    }

    /**
     * Delete profile.
     *
     * @param profileID the profile id
     * @ssdd
     */
    @Override
    public void deleteProfile(IdentifierType profileID) {

        // Find the right object
        final ResourceProfileModel currentProfile = resourceProfileDAO.findByIdentifier(profileID.getStringValue());
        if (currentProfile == null)
            logger.error("ERROR: profile: " + profileID + " doesn't exist in database");
        else
            resourceProfileDAO.makeTransient(currentProfile);
    }

    private List<String> getAllProfileNames() {

        final List<ResourceProfileModel> profiles = resourceProfileDAO.findAll();
        final List<String> ids = new ArrayList<String>();
        if (profiles != null && profiles.size() > 0)
            for (final ResourceProfileModel profile : profiles)
                ids.add(profile.getIdentifier());
        return ids;
    }

    private FilterType getFilterFromInterest(Interest interest) throws XmlException {

        final XmlOptions xo = new XmlOptions();
        xo.setSaveInner();
        final XmlCursor ic = interest.newCursor();
        final FilterType filter = FilterType.Factory.parse(ic.xmlText(xo));
        ic.dispose();
        return filter;
    }

    /**
     * Gets the profile.
     *
     * @param profileID the profile id
     *
     * @return the profile
     * @ssdd
     */
    @Override
    public ResourceProfileModel getProfile(IdentifierType profileID) {

        logger.debug("Get Profile: " + profileID);
        final ResourceProfileModel profileModel = resourceProfileDAO.findByIdentifier(profileID.getStringValue());

        return profileModel;
    }

    /**
     * Gets the profile list.
     *
     * @param queryString the query string
     *
     * @return the profile list
     * @ssdd
     */
    @Override
    public ResourceProfileListType getProfileList(String queryString) {

        // TODO: use the queryString for finding profiles
        // ignore the queryString and get all the profiles
        final ResourceProfileListType response = ResourceProfileListType.Factory.newInstance();

        // List<String> profileIDList = getAllProfileNames();
        final List<ResourceProfileModel> profileList = resourceProfileDAO.findAll();
        if (profileList != null)
            if (profileList != null && profileList.size() > 0) {
                final ResourceProfile[] profiles = new ResourceProfile[profileList.size()];
                int i = 0;
                for (final ResourceProfileModel profileModel : profileList) {
                    final ResourceProfile profile = ResourceProfileUtil.copyProperties(profileModel);
                    if (profile != null)
                        // log.debug("getProfileList, ProfileName: "
                        // + profile.getID().getStringValue());
                        profiles[i++] = profile;
                }
                response.setResourceProfileArray(profiles);
            }
        return response;

    }

    public WorkProductService getWorkProductService() {

        return workProductService;
    }

    private void init() {

        final List<ResourceProfileModel> profiles = resourceProfileDAO.findAll();
        if (profiles != null && profiles.size() > 0)
            for (final ResourceProfileModel profile : profiles) {
                ResourceProfileUtil.copyProperties(profile);
                logger.debug("init: send [ " + profile.getId() + " ] to directory service");
            }
    }

    /**
     * Removes an interest from a profile.
     *
     * @param profileID the profile id
     * @param it the it
     *
     * @return true, if successful
     * @ssdd
     */
    @Override
    public boolean removeInterest(IdentifierType profileID, Interest it) {

        boolean removed = false;
        // Get profile
        final ResourceProfileModel profile = resourceProfileDAO.findByIdentifier(profileID.getStringValue());
        if (profile != null)
            if (profile.removeInterest(ResourceProfileUtil.copyProperties(it))) {
                logger.debug("Interest removed from profile: " + it.getTopicExpression());
                removed = true;
                resourceProfileDAO.makePersistent(profile);
            }
        // Interests interests = Interests.Factory.newInstance();
        // interests.getInterestArray()[0] = it;
        // Set<InterestElement> list = new HashSet<InterestElement>();
        // ResourceProfileUtil.merge(interests, list);
        // if (profile.removeInterest((InterestElement) list.toArray()[0])) {
        // removed = true;
        // resourceProfileDAO.makePersistent(profile);
        // }
        return removed;
    }

    @Override
    public void setDirectoryService(DirectoryService directoryService) {

        this.directoryService = directoryService;
    }

    public void setResourceProfileDAO(ResourceProfileDAO p) {

        resourceProfileDAO = p;
    }

    // private void createSubscription(Interest interest, EndpointReferenceType endpoint) {
    // // Are we interested
    // try {
    // FilterType filter = getFilterFromInterest(interest);
    // } catch (XmlException e) {
    // e.printStackTrace();
    // }
    // }

    public void setWorkProductService(WorkProductService workProductService) {

        this.workProductService = workProductService;
    }

    @Override
    public void systemInitializedHandler(String messgae) {

        logger.debug("systemInitializedHandler: ... start ...");
        final WorkProductTypeListType publishedProducts = WorkProductTypeListType.Factory.newInstance();
        final WorkProductTypeListType subscribedProducts = WorkProductTypeListType.Factory.newInstance();
        directoryService.registerUICDSService(NS_ResourceProfileService,
            RESOURCEPROFILE_SERVICE_NAME, publishedProducts, subscribedProducts);
        init();
        logger.debug("systemInitializedHandler: ... done ...");
    }

    /**
     * Update profile (description and resource typing)
     *
     * @param request the request
     *
     * @return the resource profile model
     * @ssdd
     */
    @Override
    public ResourceProfileModel updateProfile(ResourceProfile request) {

        final ResourceProfileModel currentModel = resourceProfileDAO.findByIdentifier(request.getID().getStringValue());
        ResourceProfileModel updatedModel = null;
        if (currentModel != null) {
            // currently only allow user to update Description and Resource Typing
            currentModel.setDescription(request.getDescription());

            // resource typing
            if (request.getResourceTyping() != null &&
                request.getResourceTyping().sizeOfTypeArray() > 0) {
                //   Map<String, String> mapTyping = new HashMap<String, String>();
                final Set<CodeSpaceValueType> cvts = new HashSet<CodeSpaceValueType>();

                for (final CodespaceValueType type : request.getResourceTyping().getTypeArray()) {
                    final CodeSpaceValueType newType = new CodeSpaceValueType();
                    newType.setCodeSpace(type.getCodespace());
                    newType.setLabel(type.getLabel());
                    newType.setValue(type.getStringValue());
                    //     mapTyping.put(type.getCodespace() + "," + type.getLabel(),
                    //         type.getStringValue());
                    cvts.add(newType);
                }

                // currentModel.setResourceTyping(mapTyping);
                currentModel.setCvts(cvts);
            }

            // Update a new copy of a profile model with the new profile information
            updatedModel = resourceProfileDAO.makePersistent(currentModel);
        }
        return updatedModel;
    }
}
