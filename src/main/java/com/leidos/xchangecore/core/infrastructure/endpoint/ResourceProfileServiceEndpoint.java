package com.leidos.xchangecore.core.infrastructure.endpoint;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.HttpServletConnection;
import org.uicds.resourceProfileService.AddInterestRequestDocument;
import org.uicds.resourceProfileService.AddInterestResponseDocument;
import org.uicds.resourceProfileService.CreateProfileRequestDocument;
import org.uicds.resourceProfileService.CreateProfileResponseDocument;
import org.uicds.resourceProfileService.CreateProfileResponseDocument.CreateProfileResponse;
import org.uicds.resourceProfileService.DeleteProfileRequestDocument;
import org.uicds.resourceProfileService.DeleteProfileResponseDocument;
import org.uicds.resourceProfileService.GetProfileListRequestDocument;
import org.uicds.resourceProfileService.GetProfileListResponseDocument;
import org.uicds.resourceProfileService.GetProfileListResponseDocument.GetProfileListResponse;
import org.uicds.resourceProfileService.GetProfileRequestDocument;
import org.uicds.resourceProfileService.GetProfileResponseDocument;
import org.uicds.resourceProfileService.GetProfileResponseDocument.GetProfileResponse;
import org.uicds.resourceProfileService.Interest;
import org.uicds.resourceProfileService.RemoveInterestRequestDocument;
import org.uicds.resourceProfileService.RemoveInterestResponseDocument;
import org.uicds.resourceProfileService.ResourceProfile;
import org.uicds.resourceProfileService.ResourceProfileListType;

import com.leidos.xchangecore.core.infrastructure.model.ResourceProfileModel;
import com.leidos.xchangecore.core.infrastructure.service.ResourceProfileService;
import com.leidos.xchangecore.core.infrastructure.util.ResourceProfileUtil;
import com.leidos.xchangecore.core.infrastructure.util.ServiceNamespaces;
import com.saic.precis.x2009.x06.base.IdentifierType;

/**
 * The XchangeCore Resource Profile Management Service provides a means to create, discover, and update
 * XchangeCore Resource Profiles. XchangeCore resource profiles contain resource typing information and interest
 * expressions for notifications that can be applied to resource instances. The resource profiles
 * generally represent a role that a potential resource instance will fulfill with respect to a
 * particular instantiation of an Interest Group (i.e. incident).
 * <p>
 * Resource profiles are applied to resource instances to subscribe a resource instance to the
 * interests expressed in the resource profile. Resource profiles can be applied to a resource
 * instance through the Resource Instance Service
 * <p>
 * A Resorce Profile is defined as the following data structure:
 * <p>
 * <img src="doc-files/ResourceProfile.png"/> <BR>
 * <p>
 * <!-- NEWPAGE -->
 * <p>
 * 
 * The ID is an identifier that is unique to the core where this profile was created and is meant to
 * be human readable.
 * <p>
 * The Resource Typing is a set of the following codes defined as a triple of codespace, code, and
 * value. The usage of the triple is meant to be domain independent. Currently, the Resource Profile
 * service supports NIMS resource typing using the codespace value of http://nimsonline.org/2.0 with
 * the following codes:
 * <ul>
 * <li>Resource</li>
 * <li>Category</li>
 * <li>Kind</li>
 * <li>MinimumCapabilities</li>
 * </ul>
 * <p>
 * The values for these codes would come from the NIMS 120 resource typing document.
 * <p>
 * Resource Profile interests are expressed as topic expressions as defined in the WS-Notification
 * 1.3 specification. See the
 * {@link com.leidos.xchangecore.core.infrastructure.endpoint.NotificationServiceEndpoint} for details on
 * how topic expressions can be formed to express interests in XchangeCore work products.
 * <p>
 * 
 * @author William Summers
 * @see <a href="../../wsdl/ResourceProfileService.wsdl">Appendix: ResourceProfileService.wsdl</a>
 * @see <a href="../../services/ResourceProfile/0.1/ResourceProfileService.xsd">Appendix:
 *      ResourceProfileService.xsd</a>
 * @see com.leidos.xchangecore.core.infrastructure.endpoint.ResourceInstanceServiceEndpoint
 * @see com.leidos.xchangecore.core.infrastructure.endpoint.NotificationServiceEndpoint
 * @idd
 */

@Endpoint
@Transactional
public class ResourceProfileServiceEndpoint
    implements ServiceNamespaces {

    Logger log = LoggerFactory.getLogger(ResourceProfileServiceEndpoint.class);

    @Autowired
    private ResourceProfileService profileService;

    public void setProfileService(ResourceProfileService p) {

        profileService = p;
    }

    /**
     * Create a resource profile.
     * 
     * @param CreateProfileRequestDocument
     * 
     * @return CreateProfileResponseDocument
     * @see <a href="../../wsdl/ResourceProfileService.wsdl">Appendix: ResourceProfileService.wsdl</a>
     * @see <a href="../../services/ResourceProfile/0.1/ResourceProfileService.xsd">Appendix:
     *      ResourceProfileService.xsd</a>
     * @idd
     */
    @PayloadRoot(namespace = NS_ResourceProfileService, localPart = "CreateProfileRequest")
    public CreateProfileResponseDocument createProfile(CreateProfileRequestDocument requestDoc) {

        CreateProfileResponseDocument document = CreateProfileResponseDocument.Factory.newInstance();
        ResourceProfileModel profileModel = profileService.createProfile(requestDoc.getCreateProfileRequest().getProfile());
        ResourceProfile profile = ResourceProfileUtil.copyProperties(profileModel);

        CreateProfileResponse response = document.addNewCreateProfileResponse();
        response.setProfile(profile);
        return document;
    }

    /**
     * Delete a resource profile.
     * 
     * @param DeleteProfileRequestDocument
     * 
     * @return DeleteProfileResponseDocument
     * @see <a href="../../wsdl/ResourceProfileService.wsdl">Appendix: ResourceProfileService.wsdl</a>
     * @see <a href="../../services/ResourceProfile/0.1/ResourceProfileService.xsd">Appendix:
     *      ResourceProfileService.xsd</a>
     * @idd
     */
    @PayloadRoot(namespace = NS_ResourceProfileService, localPart = "DeleteProfileRequest")
    public DeleteProfileResponseDocument deleteProfile(DeleteProfileRequestDocument requestDoc) {

        IdentifierType profileID = requestDoc.getDeleteProfileRequest().getID();

        log.debug("Delete Profile: (profileID: " + profileID.getStringValue() + ")");

        profileService.deleteProfile(profileID);

        DeleteProfileResponseDocument response = DeleteProfileResponseDocument.Factory.newInstance();

        response.addNewDeleteProfileResponse();
        response.getDeleteProfileResponse().setID(profileID);

        return response;
    }

    /**
     * Get a resource profile.
     * 
     * @param GetProfileRequestDocument
     * 
     * @return GetProfileResponseDocument
     * @see <a href="../../wsdl/ResourceProfileService.wsdl">Appendix: ResourceProfileService.wsdl</a>
     * @see <a href="../../services/ResourceProfile/0.1/ResourceProfileService.xsd">Appendix:
     *      ResourceProfileService.xsd</a>
     * @idd
     */
    @PayloadRoot(namespace = NS_ResourceProfileService, localPart = "GetProfileRequest")
    public GetProfileResponseDocument getProfile(GetProfileRequestDocument requestDoc) {

        GetProfileResponseDocument document = GetProfileResponseDocument.Factory.newInstance();
        ResourceProfileModel profileModel = profileService.getProfile(requestDoc.getGetProfileRequest().getID());

        ResourceProfile profile = ResourceProfileUtil.copyProperties(profileModel);

        GetProfileResponse response = document.addNewGetProfileResponse();
        response.setProfile(profile);
        return document;
    }

    private static String getPrincipalName() {

        // get the TransportContext for this call
        TransportContext transportContext = TransportContextHolder.getTransportContext();
        if (transportContext == null) {
            return null;
        }

        // Get the service connection from the context
        WebServiceConnection webServiceConnection = transportContext.getConnection();
        if (webServiceConnection == null) {
            return null;
        }

        // check to see if it is the request is HTTP
        if (!(webServiceConnection instanceof HttpServletConnection)) {
            return null;
        }

        // Cast it
        HttpServletConnection httpServletConnection = (HttpServletConnection) webServiceConnection;
        if (httpServletConnection == null) {
            return null;
        }

        // Get the request object
        HttpServletRequest request = httpServletConnection.getHttpServletRequest();
        if (request == null) {
            return null;
        }
        if (request.getUserPrincipal() == null) {
            return null;
        }

        // call a method and return
        return request.getUserPrincipal().getName();
    }

    /**
     * Get a list of resource profiles.
     * 
     * @param GetProfileListRequestDocument
     * 
     * @return GetProfileListResponseDocument
     * @see <a href="../../wsdl/ResourceProfileService.wsdl">Appendix: ResourceProfileService.wsdl</a>
     * @see <a href="../../services/ResourceProfile/0.1/ResourceProfileService.xsd">Appendix:
     *      ResourceProfileService.xsd</a>
     * @idd
     */
    @PayloadRoot(namespace = NS_ResourceProfileService, localPart = "GetProfileListRequest")
    public GetProfileListResponseDocument getProfileList(GetProfileListRequestDocument requestDoc) {

        log.info("Requested by: " + getPrincipalName());

        GetProfileListResponseDocument document = GetProfileListResponseDocument.Factory.newInstance();
        String queryString = requestDoc.getGetProfileListRequest().getQueryString();

        ResourceProfileListType profileList = profileService.getProfileList(queryString);
        if (profileList != null) {
            profileList = (ResourceProfileListType) profileList.copy();
        }

        GetProfileListResponse response = document.addNewGetProfileListResponse();
        response.setProfileList(profileList);

        return document;
    }

    /**
     * Adds an interest from the profile of the specified entityID (for example
     * johnsmith@core1.saic.com).
     * 
     * @param AddInterestRequestDocument
     * @return AddInterestResponseDocument
     * @see <a href="../../wsdl/ResourceProfileService.wsdl">Appendix: ResourceProfileService.wsdl</a>
     * @see <a href="../../services/ResourceProfile/0.1/ResourceProfileService.xsd">Appendix:
     *      ResourceProfileService.xsd</a>
     * @idd
     */
    @PayloadRoot(namespace = NS_ResourceProfileService, localPart = "AddInterestRequest")
    public AddInterestResponseDocument addInterest(AddInterestRequestDocument requestDoc) {

        // FIXME
        AddInterestResponseDocument response = AddInterestResponseDocument.Factory.newInstance();
        // Get the data from the request document
        IdentifierType profileID = requestDoc.getAddInterestRequest().getID();
        Interest interest = requestDoc.getAddInterestRequest().getInterest();

        // Add interest
        boolean success = profileService.addInterest(profileID, interest);
        ResourceProfileModel profileModel = null;

        if (success) {
            profileModel = profileService.getProfile(profileID);
        }

        if (profileModel != null) {
            ResourceProfile profile = ResourceProfileUtil.copyProperties(profileModel);
            response.addNewAddInterestResponse().setProfile(profile);
        }

        return response;
    }

    /**
     * Removes an interest from the profile of the specified entityID(for example
     * johnsmith@core1.saic.com).
     * 
     * @param RemoveInterestRequestDocument
     * @return RemoveInterestResponseDocument
     * @see <a href="../../wsdl/ResourceProfileService.wsdl">Appendix: ResourceProfileService.wsdl</a>
     * @see <a href="../../services/ResourceProfile/0.1/ResourceProfileService.xsd">Appendix:
     *      ResourceProfileService.xsd</a>
     * @idd
     */
    @PayloadRoot(namespace = NS_ResourceProfileService, localPart = "RemoveInterestRequest")
    public RemoveInterestResponseDocument removeInterest(RemoveInterestRequestDocument requestDoc) {

        RemoveInterestResponseDocument response = RemoveInterestResponseDocument.Factory.newInstance();
        // Get the data from the request document
        IdentifierType profileID = requestDoc.getRemoveInterestRequest().getID();
        Interest interest = requestDoc.getRemoveInterestRequest().getInterest();

        // Remove interest
        boolean success = profileService.removeInterest(profileID, interest);
        ResourceProfileModel profileModel = null;
        if (success) {
            profileModel = profileService.getProfile(profileID);
            if (profileModel != null) {
                ResourceProfile profile = ResourceProfileUtil.copyProperties(profileModel);
                response.addNewRemoveInterestResponse().setProfile(profile);
            }
        }

        return response;
    }

}
