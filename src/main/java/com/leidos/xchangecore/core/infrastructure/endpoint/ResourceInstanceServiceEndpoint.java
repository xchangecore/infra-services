package com.leidos.xchangecore.core.infrastructure.endpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.uicds.resourceInstanceService.CheckinRequestDocument;
import org.uicds.resourceInstanceService.CheckinRequestDocument.CheckinRequest;
import org.uicds.resourceInstanceService.CheckinResponseDocument;
import org.uicds.resourceInstanceService.CheckoutRequestDocument;
import org.uicds.resourceInstanceService.CheckoutRequestDocument.CheckoutRequest;
import org.uicds.resourceInstanceService.CheckoutResponseDocument;
import org.uicds.resourceInstanceService.GetResourceInstanceListRequestDocument;
import org.uicds.resourceInstanceService.GetResourceInstanceListResponseDocument;
import org.uicds.resourceInstanceService.GetResourceInstanceRequestDocument;
import org.uicds.resourceInstanceService.GetResourceInstanceResponseDocument;
import org.uicds.resourceInstanceService.RegisterRequestDocument;
import org.uicds.resourceInstanceService.RegisterRequestDocument.RegisterRequest;
import org.uicds.resourceInstanceService.RegisterResponseDocument;
import org.uicds.resourceInstanceService.ResourceInstance;
import org.uicds.resourceInstanceService.ResourceInstanceListType;
import org.uicds.resourceInstanceService.UnregisterRequestDocument;
import org.uicds.resourceInstanceService.UnregisterResponseDocument;
import org.uicds.resourceInstanceService.UpdateEndpointRequestDocument;
import org.uicds.resourceInstanceService.UpdateEndpointResponseDocument;

import com.leidos.xchangecore.core.infrastructure.exceptions.ResourceInstanceDoesNotExist;
import com.leidos.xchangecore.core.infrastructure.exceptions.ResourceProfileDoesNotExist;
import com.leidos.xchangecore.core.infrastructure.model.ResourceInstanceModel;
import com.leidos.xchangecore.core.infrastructure.service.NotificationService;
import com.leidos.xchangecore.core.infrastructure.service.ResourceInstanceService;
import com.leidos.xchangecore.core.infrastructure.util.ResourceInstanceUtil;
import com.leidos.xchangecore.core.infrastructure.util.ServiceNamespaces;
import com.saic.precis.x2009.x06.base.IdentifierType;

/**
 * The XchangeCore Resource Instance service provides a means to create, discover, update, and remove
 * XchangeCore resource instances. A XchangeCore resource instance represents a resource that is capable of
 * receiving notifications as described in
 * {@link com.leidos.xchangecore.core.infrastructure.endpoint.NotificationServiceEndpoint}. The notification
 * interests for a resource instance are expressed as resource profiles which represent a role that
 * the resource instance fulfills with respect to a particular Interest Group (i.e. incident).
 * <p>
 * A Resource Instance is defined as the following data structure:<br/>
 * <img src="doc-files/ResourceInstance.png"/>
 * <p>
 * <!-- NEWPAGE -->
 * <p>
 * Each XchangeCore resource instance is represented within XchangeCore by an identifier which is unique across
 * XchangeCore cores and has the following form:
 * <p>
 * instanceID@coreID where
 * <ul>
 * <li>instanceID represents a specific instance of an entity and is unique for the given coreID and
 * is the value contained in the ris:ID element. Clients should submit just this value when creating
 * a resource instance.</li>
 * <li>coreID represents a specific core instance and is unique across all cores</li>
 * </ul>
 * <p>
 * The SourceIdentification element contains a LocalResouceID that can be used to store an
 * application or locality specific identification string. The coreID value will be set to the core
 * where this resource instance was created.
 * <p>
 * The Keywords element can contain any codespace, code, value triples that the creator would like
 * to store with the resource instance. A particular NIMS resource typing set of triples for the
 * actual typing of the resource is an example.
 * <p>
 * The Endpoints element contains references to endpoints associated with this resource instance.
 * Currently the only endpoint that may be associated with a resource instance is its notification
 * endpoint. See the {@link com.leidos.xchangecore.core.infrastructure.endpoint.NotificationServiceEndpoint}
 * for more details.
 * <p>
 * The ProfileIDs element will contain a list of the Resource Profiles that have been applied to
 * this resource instance if any. Profiles get applied to application-type resource instances when
 * they register with this service or to individual resource instances when a domain service applies
 * a resource profile to the resource instance. For example, in the EM domain the ICS service would
 * apply profiles to individual resources when they are assigned a role in the ICS.
 * <p>
 * <b>Known Issues</b>
 * <ul>
 * <li>
 * The checkout operation is not operational. The work around is to use the unregister operation for
 * any resource instances created by using checkin.</li>
 * </ul>
 * 
 * @author William Summers
 * @see <a href="../../wsdl/ResourceInstanceService.wsdl">Appendix: ResourceInstanceService.wsdl</a>
 * @see <a href="../../services/ResourceInstance/0.1/ResourceInstanceService.xsd">Appendix:
 *      ResourceInstanceService.xsd</a>
 * @see com.leidos.xchangecore.core.infrastructure.endpoint.NotificationServiceEndpoint
 * @see com.leidos.xchangecore.core.infrastructure.endpoint.ResourceProfileServiceEndpoint
 * @idd
 */

@Endpoint
@Transactional
public class ResourceInstanceServiceEndpoint
    implements ServiceNamespaces {

    Logger log = LoggerFactory.getLogger(ResourceInstanceServiceEndpoint.class);

    @Autowired
    private ResourceInstanceService risService;

    //@Autowired
    //private NotificationService notificationService;

    private void setResourceInstanceService(ResourceInstanceService p) {

        risService = p;
    }

    /**
     * Check in a particular resource instance.
     * 
     * @param CheckinRequestDocument
     * @return CheckinResponseDocument
     * @see <a href="../../services/ResourceInstance/0.1/ResourceInstanceService.xsd">Appendix:
     *      ResourceInstanceService.xsd</a>
     * @idd
     */
    @PayloadRoot(namespace = NS_ResourceInstanceService, localPart = "CheckinRequest")
    public CheckinResponseDocument checkin(CheckinRequestDocument requestDoc) {

        CheckinResponseDocument response = CheckinResponseDocument.Factory.newInstance();
        CheckinRequest request = requestDoc.getCheckinRequest();
        ResourceInstanceModel resourceModel = risService.checkin(request.getID(),
            request.getLocalResourceID(),
            request.getResourceProfileID());

        //since the checkin is not working so well yet, just use count=0 for now.
        int count = 0; //notificationService.findMsgCountByEntityId(resourceModel.get)
        response.addNewCheckinResponse().setResourceInstance(ResourceInstanceUtil.copyProperties(resourceModel,
            count));
        return response;
    }

    /**
     * Register an application as a resource instance.
     * 
     * @param RegisterRequestDocument
     * @return RegisterResponseDocument
     * @see <a href="../../services/ResourceInstance/0.1/ResourceInstanceService.xsd">Appendix:
     *      ResourceInstanceService.xsd</a>
     * @idd
     */
    @PayloadRoot(namespace = NS_ResourceInstanceService, localPart = "RegisterRequest")
    public RegisterResponseDocument register(RegisterRequestDocument requestDoc)
        throws ResourceProfileDoesNotExist {

        RegisterResponseDocument response = RegisterResponseDocument.Factory.newInstance();
        RegisterRequest request = requestDoc.getRegisterRequest();
        ResourceInstanceModel resourceModel = risService.register(request.getID(),
            request.getLocalResourceID(),
            request.getResourceProfileID());

        //since the checkin is not working so well yet, just use count=0 for now.
        int count = 0;
        response.addNewRegisterResponse().setResourceInstance(ResourceInstanceUtil.copyProperties(resourceModel,
            count));
        return response;
    }

    /**
     * Unregister an application as a resource instance. This will remove the resource instance and
     * its endpoint along all of the notification messages on that endpoint. Unregistering a
     * resource instance cleans up the resources on the core that was allocated to this resource
     * instance and allows a resource instance to then re-register with a different resource profile
     * in order to change interests.
     * 
     * @param RegisterRequestDocument
     * @return RegisterResponseDocument
     * @see <a href="../../services/ResourceInstance/0.1/ResourceInstanceService.xsd">Appendix:
     *      ResourceInstanceService.xsd</a>
     * @idd
     */
    @PayloadRoot(namespace = NS_ResourceInstanceService, localPart = "UnregisterRequest")
    public UnregisterResponseDocument unregister(UnregisterRequestDocument requestDoc)
        throws ResourceInstanceDoesNotExist {

        IdentifierType removedID = risService.unregister(requestDoc.getUnregisterRequest().getID());

        UnregisterResponseDocument response = UnregisterResponseDocument.Factory.newInstance();
        response.addNewUnregisterResponse().setID(removedID);

        return response;
    }

    /**
     * Check out a resource instance. (Not implemented yet)
     * 
     * @param CheckoutRequestDocument
     * @return CheckoutResponseDocument
     * @see <a href="../../services/ResourceInstance/0.1/ResourceInstanceService.xsd">Appendix:
     *      ResourceInstanceService.xsd</a>
     * @idd
     */
    @PayloadRoot(namespace = NS_ResourceInstanceService, localPart = "CheckoutRequest")
    public CheckoutResponseDocument checkout(CheckoutRequestDocument requestDoc) {

        CheckoutResponseDocument response = CheckoutResponseDocument.Factory.newInstance();
        CheckoutRequest request = requestDoc.getCheckoutRequest();
        ResourceInstance ris = risService.checkout(request.getResourceInstance());
        response.addNewCheckoutResponse().setResourceInstance(ris);
        return response;
    }

    /**
     * Get a list of the current resource instances.
     * 
     * @param GetResourceInstanceListRequestDocument
     * @return GetResourceInstanceListResponseDocument
     * @see <a href="../../services/ResourceInstance/0.1/ResourceInstanceService.xsd">Appendix:
     *      ResourceInstanceService.xsd</a>
     * @idd
     */
    @PayloadRoot(namespace = NS_ResourceInstanceService, localPart = "GetResourceInstanceListRequest")
    public GetResourceInstanceListResponseDocument getResourceInstanceList(GetResourceInstanceListRequestDocument requestDoc) {

        GetResourceInstanceListResponseDocument response = GetResourceInstanceListResponseDocument.Factory.newInstance();
        response.addNewGetResourceInstanceListResponse();

        ResourceInstanceListType list = risService.getResourceInstanceList(requestDoc.getGetResourceInstanceListRequest().getQueryString());
        if (list.sizeOfResourceInstanceArray() > 0) {
            response.getGetResourceInstanceListResponse().setResourceInstanceList(list);
        } else {
            response.getGetResourceInstanceListResponse().addNewResourceInstanceList();
        }
        return response;
    }

    /**
     * Get a specific resource instance.
     * 
     * @param GetResourceInstanceRequestDocument
     * @return GetResourceInstanceResponseDocument
     * @see <a href="../../services/ResourceInstance/0.1/ResourceInstanceService.xsd">Appendix:
     *      ResourceInstanceService.xsd</a>
     * @idd
     */
    @PayloadRoot(namespace = NS_ResourceInstanceService, localPart = "GetResourceInstanceRequest")
    public GetResourceInstanceResponseDocument getResourceInstance(GetResourceInstanceRequestDocument requestDoc) {

        GetResourceInstanceResponseDocument response = GetResourceInstanceResponseDocument.Factory.newInstance();
        response.addNewGetResourceInstanceResponse();

        ResourceInstance ri = risService.getResourceInstance(requestDoc.getGetResourceInstanceRequest().getID());
        if (ri != null) {
            response.getGetResourceInstanceResponse().setResourceInstance(ri);
        }

        return response;
    }

    /**
     * Update the endpoint for this resource instance. The new value of the endpoint must be a web
     * service that implements the WS-Notification Notify interface or an XMPP JID.
     * 
     * @param UpdateEndpointRequestDocument
     * @return UpdateEndpointResponseDocument
     * @see <a href="../../services/ResourceInstance/0.1/ResourceInstanceService.xsd">Appendix:
     *      ResourceInstanceService.xsd</a>
     * @idd
     */
    @PayloadRoot(namespace = NS_ResourceInstanceService, localPart = "UpdateEndpointRequest")
    public UpdateEndpointResponseDocument updateEndpoint(UpdateEndpointRequestDocument requestDoc) {

        UpdateEndpointResponseDocument response = UpdateEndpointResponseDocument.Factory.newInstance();
        response.addNewUpdateEndpointResponse();

        if (risService.updateEndpoint(requestDoc.getUpdateEndpointRequest().getID(),
            requestDoc.getUpdateEndpointRequest().getEndpoint(),
            requestDoc.getUpdateEndpointRequest().getIsWebService())) {
            ResourceInstance ris = risService.getResourceInstance(requestDoc.getUpdateEndpointRequest().getID());
            if (ris != null) {
                response.getUpdateEndpointResponse().setResourceInstance(ris);
            }
        }
        return response;
    }

}
