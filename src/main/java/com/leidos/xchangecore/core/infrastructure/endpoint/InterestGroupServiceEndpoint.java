package com.leidos.xchangecore.core.infrastructure.endpoint;

import javax.xml.datatype.DatatypeConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;

import com.leidos.xchangecore.core.infrastructure.service.ConfigurationService;
import com.leidos.xchangecore.core.infrastructure.service.InterestGroupService;
import com.leidos.xchangecore.core.infrastructure.service.WorkProductService;
import com.leidos.xchangecore.core.infrastructure.util.ServiceNamespaces;
import com.leidos.xchangecore.core.infrastructure.util.WorkProductHelper;

import org.uicds.interestGroupService.ArchiveInterestGroupRequestDocument;
import org.uicds.interestGroupService.ArchiveInterestGroupResponseDocument;
import org.uicds.interestGroupService.CloseInterestGroupRequestDocument;
import org.uicds.interestGroupService.CloseInterestGroupResponseDocument;
import org.uicds.interestGroupService.CreateInterestGroupRequestDocument;
import org.uicds.interestGroupService.CreateInterestGroupResponseDocument;
import org.uicds.interestGroupService.GetInterestGroupRequestDocument;
import org.uicds.interestGroupService.GetInterestGroupResponseDocument;
import org.uicds.interestGroupService.GetListOfInterestGroupResponseDocument;
import org.uicds.interestGroupService.GetListOfWorkProductRequestDocument;
import org.uicds.interestGroupService.GetListOfWorkProductResponseDocument;
import org.uicds.interestGroupService.InterestGroupListType;
import org.uicds.interestGroupService.ShareInterestGroupRequestDocument;
import org.uicds.interestGroupService.ShareInterestGroupResponseDocument;
import org.uicds.interestGroupService.ShareInterestGroupResponseDocument.ShareInterestGroupResponse;
import org.uicds.interestGroupService.UnShareInterestGroupRequestDocument;
import org.uicds.interestGroupService.UnShareInterestGroupResponseDocument;
import org.uicds.interestGroupService.UnShareInterestGroupResponseDocument.UnShareInterestGroupResponse;
import org.uicds.interestGroupService.UpdateInterestGroupRequestDocument;
import org.uicds.interestGroupService.UpdateInterestGroupResponseDocument;
import org.uicds.workProductService.WorkProductListDocument.WorkProductList;

/**
 * The Interest Group Service allows clients to manage XchangeCore Interest Group. It includes services
 * to:
 * <ul>
 * <li>create an interest group</li>
 * <li>create an incident from a CAP version 1.1 alert document</li>
 * <li>get the interest group restricted document </li>
 * <li>update information about an interest group</li>
 * <li>share the interest group with other XchangeCore cores</li>
 * <li>close an interest group</li>
 * <li>archive an interest group</li>
 * </ul>
 * <p>
 * An interest group is defined as the following data structure:<br/>
 * 
 * <p>
 * <!-- NEWPAGE -->
 * <p>
 * 
 * 
 * @author Daphne Hurrell
 * @since 1.0
 * @see <a href="../../wsdl/InterestGroupService.wsdl">Appendix: InterestGroupService.wsdl</a>
 * @see <a href="../../services/InterestGroup/0.1/InterestGroup.xsd">Appendix: InterestGroup.xsd</a>
 * @see <a href="../../services/InterestGroup/0.1/InterestGroup.xsd">Appendix:
 *      InterestGroup.xsd</a>
 * @see <a href="../../services/InterestGroup/0.1/InterestGroupService.xsd">Appendix:
 *      InterestGroupService.xsd</a>
 * 
 * @idd
 * 
 */
@Endpoint
public class InterestGroupServiceEndpoint
    implements ServiceNamespaces {

    @Autowired
    InterestGroupService interestGroupService;

    @Autowired
    WorkProductService workProductService;

    @Autowired
    ConfigurationService configurationService;

    Logger log = LoggerFactory.getLogger(InterestGroupServiceEndpoint.class);

    void setInterestGroupService(InterestGroupService igs) {

        interestGroupService = igs;
    }

    void setWorkProductService(WorkProductService workProductService) {

        this.workProductService = workProductService;
    }

    /**
     * Archives an interest group by removing it and all of the associated work products. The interest
     * group must be closed before it is archived.
     * 
     * @param ArchiveInterestGroupRequestDocument
     * 
     * @return ArchiveInterestGroupResponseDocument
     * @see <a href="../../services/InterestGroup/0.1/InterestGroup.xsd">Appendix: InterestGroup.xsd</a>
     * @see <a href="../../services/InterestGroup/0.1/InterestGroupService.xsd">Appendix:
     *      IntersetGroupService.xsd</a>
     * @idd
     */
    @PayloadRoot(namespace = NS_InterestGroupService, localPart = "ArchiveInterestGroupRequest")
    public ArchiveInterestGroupResponseDocument archiveInterestGroup(ArchiveInterestGroupRequestDocument request)
        throws DatatypeConfigurationException {

        ArchiveInterestGroupResponseDocument response = ArchiveInterestGroupResponseDocument.Factory.newInstance();
        response.addNewArchiveInterestGroupResponse().set(WorkProductHelper.toWorkProductProcessingStatus(interestGroupService.archiveInterestGroup(request.getArchiveInterestGroupRequest().getInterestGroupId())));
        return response;
    }

    /**
     * Closes an incident by making all of the associated work products inactive.
     * 
     * @param CloseInterestGroupRequestDocument
     * 
     * @return CloseInterestGroupResponseDocument
     * @see <a href="../../services/InterestGroup/0.1/Incident.xsd">Appendix: InterestGroup.xsd</a>
     * @see <a href="../../services/InterestGroup/0.1/IncidentGroupService.xsd">Appendix:
     *      InterestGroup.xsd</a>
     * 
     * @throws DatatypeConfigurationException
     * 
     * @idd
     */
    @PayloadRoot(namespace = NS_InterestGroupService, localPart = "CloseInterestGroupRequest")
    public CloseInterestGroupResponseDocument closeInterestGroup(CloseInterestGroupRequestDocument request)
        throws DatatypeConfigurationException {

        CloseInterestGroupResponseDocument response = CloseInterestGroupResponseDocument.Factory.newInstance();
        response.addNewCloseInterestGroupResponse().set(WorkProductHelper.toWorkProductProcessingStatus(interestGroupService.closeInterestGroup(request.getCloseInterestGroupRequest().getInterestGroupId())));
        return response;
    }

    /**
     * Allows the client to create an interest Group.
     * 
     * @param CreateInterestGroupRequestDocument
     * 
     * @return CreateInterestGroupResponseDocument
     * @see <a href="../../services/InterestGroup/0.1/InterestGroup.xsd">Appendix: InterestGroup.xsd</a>
     * @see <a href="../../services/InterestGroup/0.1/InterestGroup.xsd">Appendix:
     *      InterestGroupService.xsd</a>
     *
     * 
     * @idd
     */
    @PayloadRoot(namespace = NS_InterestGroupService, localPart = "CreateInterestGroupRequest")
    public CreateInterestGroupResponseDocument createInterestGroup(CreateInterestGroupRequestDocument request)
        throws DatatypeConfigurationException {

        CreateInterestGroupResponseDocument response = CreateInterestGroupResponseDocument.Factory.newInstance();
        response.addNewCreateInterestGroupResponse().setInterestGroup(request.getCreateInterestGroupRequest().getInterestGroup());
        /**response.addNewCreateInterestGroupResponse().setInterestGroupId(interestGroupService.createInterestGroup(request.getCreateInterestGroupRequest().getInterestGroup(),request.getCreateInterestGroupRequest().getWorkProductIdentificationList()));**/
        response.addNewCreateInterestGroupResponse().setInterestGroupId(interestGroupService.createInterestGroup(request.getCreateInterestGroupRequest().getInterestGroup(),
            request.getCreateInterestGroupRequest().getWorkProductIdentificationList()));

        log.debug("CreateInterestGroupResponse: [ " + response.toString() + " ]");
        return response;
    }

    /**
     * Get a list of all the interest group on the core. The returned list contains the 
     * interest group id
     * 
     * @param GetListOfInterestGroupRequestDocument
     * 
     * @return GetListOfInterestGroupResponseDocument
     * 
     * @see <a href="../../services/InterestGroup/0.1/InterestGroup.xsd">Appendix: InterestGroup.xsd</a>
     * @see <a href="../../services/InterestGroup/0.1/InterestGroupService.xsd">Appendix:
     *      InterestGroupService.xsd</a>
     * 
     * @idd
     */

    @PayloadRoot(namespace = NS_InterestGroupService, localPart = "GetListOfInterestGroupRequest")
    public GetListOfInterestGroupResponseDocument getListOfInterestGroup()
        throws DatatypeConfigurationException {

        log.debug("GetListOfInterestGroup: ");

        InterestGroupListType interestGroupList = interestGroupService.getInterestGroupList();

        GetListOfInterestGroupResponseDocument response = GetListOfInterestGroupResponseDocument.Factory.newInstance();
        response.addNewGetListOfInterestGroupResponse().setInterestGroupList(interestGroupList);

        return response;
    }

    /**
     * Get a list of all the work products that belong to the interest group on the core. 
     * 
     * @param GetListOfWorkProductRequestDocument
     * 
     * @return GetListOfWorkProductResponseDocument
     * 
     * @see <a href="../../services/InterestGroup/0.1/InterestGroup.xsd">Appendix: InterestGroup.xsd</a>
     * @see <a href="../../services/InterestGroup/0.1/InterestGroupService.xsd">Appendix:
     *      InterestGroupService.xsd</a>
     * 
     * @idd
     */

    @PayloadRoot(namespace = NS_InterestGroupService, localPart = "GetListOfWorkProductRequest")
    public GetListOfWorkProductResponseDocument getListOfWorkProduct(GetListOfWorkProductRequestDocument request)
        throws DatatypeConfigurationException {

        log.debug("GetListOfWorkProduct: ");

        WorkProductList workProductList = interestGroupService.getListOfWorkProduct(request.getGetListOfWorkProductRequest().getInterestGroupId());

        GetListOfWorkProductResponseDocument response = GetListOfWorkProductResponseDocument.Factory.newInstance();
        response.addNewGetListOfWorkProductResponse().setWorkProductList(workProductList);

        return response;
    }

    /**
     * Allows the client to share an interest group with another core. The agreements are checked before the share
     * is allowed to take place.
     * 
     * @param ShareInterestGroupRequestDocument
     * 
     * @return ShareInterestGroupResponseDocument
     * @see <a href="../../services/InterestGroup/0.1/InterestGroup.xsd">Appendix: InterestGroup.xsd</a>
     * @see <a href="../../services/InterestGroup/0.1/InterestGroupService.xsd">Appendix:
     *      InterestGroupService.xsd</a>
     * 
     * @idd
     */
    @PayloadRoot(namespace = NS_InterestGroupService, localPart = "ShareIntersetGroupRequest")
    public ShareInterestGroupResponseDocument shareInterestGroup(ShareInterestGroupRequestDocument request)
        throws DatatypeConfigurationException {

        ShareInterestGroupResponseDocument response = ShareInterestGroupResponseDocument.Factory.newInstance();
        ShareInterestGroupResponse rsp = response.addNewShareInterestGroupResponse();
        boolean shared;
        try {
            shared = interestGroupService.shareInterestGroup(request.getShareInterestGroupRequest());
            rsp.setInterestGroupShareSucessful(shared);
        } catch (Exception e) {

            rsp.setInterestGroupShareSucessful(false);
            rsp.setErrorString(e.getMessage());
        }

        return response;
    }

    /**
     * Allows the client to unshare an interest group with another core. 
     * 
     * @param UnshareIncidentRequestDocument
     * 
     * @return UnshareIncidentResponseDocument
     * @see <a href="../../services/InterestGroup/0.1/InterestGroup.xsd">Appendix: InterestGroup.xsd</a>
     * @see <a href="../../services/IntersetGroup/0.1/InterestGroupService.xsd">Appendix:
     *      InterestGroupService.xsd</a>
     * 
     * @idd
     */
    @PayloadRoot(namespace = NS_InterestGroupService, localPart = "UnshareIntersetGroupRequest")
    public UnShareInterestGroupResponseDocument unshareInterestGroup(UnShareInterestGroupRequestDocument request)
        throws DatatypeConfigurationException {

        UnShareInterestGroupResponseDocument response = UnShareInterestGroupResponseDocument.Factory.newInstance();
        UnShareInterestGroupResponse rsp = response.addNewUnShareInterestGroupResponse();
        boolean unshared;
        try {
            unshared = interestGroupService.unShareInterestGroup();
            rsp.setInterestGroupUnShareSucessful(unshared);
        } catch (Exception e) {

            rsp.setInterestGroupUnShareSucessful(false);
            rsp.setErrorString(e.getMessage());
        }

        return response;
    }

    /**
     * Allows the client to update the interest group.
     * 
     * @param UpdateInterestGroupRequestDocument
     * 
     * @return UpdateInterestGroupResponseDocument
     * @see <a href="../../services/InterestGroup/0.1/InterestGroup.xsd">Appendix: InterestGroup.xsd</a>
     * @see <a href="../../services/InterestGroup/0.1/InterestGroupService.xsd">Appendix:
     *      InterestGroupService.xsd</a>
     * 
     * @idd
     */
    @PayloadRoot(namespace = NS_InterestGroupService, localPart = "UpdateInterestGroupRequest")
    public UpdateInterestGroupResponseDocument updateInterestGroup(UpdateInterestGroupRequestDocument request)
        throws DatatypeConfigurationException {

        UpdateInterestGroupResponseDocument response = UpdateInterestGroupResponseDocument.Factory.newInstance();
        log.debug("updateInterestGroup: id=" +
                  request.getUpdateInterestGroupRequest().getInterestGroupId());
        try {
            response.addNewUpdateInterestGroupResponse().setInterestGroup(request.addNewUpdateInterestGroupRequest().getInterestGroup());
        } catch (Exception e) {
        }

        return response;

    }

    @PayloadRoot(namespace = NS_InterestGroupService, localPart = "GetInterestGroupRequest")
    public GetInterestGroupResponseDocument getInterestGroup(GetInterestGroupRequestDocument request) {

        GetInterestGroupResponseDocument response = GetInterestGroupResponseDocument.Factory.newInstance();
        return response;

    }
}