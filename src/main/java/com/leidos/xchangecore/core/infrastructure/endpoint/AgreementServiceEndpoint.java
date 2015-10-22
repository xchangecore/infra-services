package com.leidos.xchangecore.core.infrastructure.endpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.uicds.agreementService.AgreementListType;
import org.uicds.agreementService.AgreementType;
import org.uicds.agreementService.CreateAgreementRequestDocument;
import org.uicds.agreementService.CreateAgreementResponseDocument;
import org.uicds.agreementService.GetAgreementListRequestDocument;
import org.uicds.agreementService.GetAgreementListResponseDocument;
import org.uicds.agreementService.GetAgreementRequestDocument;
import org.uicds.agreementService.GetAgreementResponseDocument;
import org.uicds.agreementService.RescindAgreementRequestDocument;
import org.uicds.agreementService.RescindAgreementResponseDocument;
import org.uicds.agreementService.UpdateAgreementRequestDocument;
import org.uicds.agreementService.UpdateAgreementResponseDocument;

import com.leidos.xchangecore.core.infrastructure.exceptions.AgreementWithCoreExists;
import com.leidos.xchangecore.core.infrastructure.exceptions.MissingConditionInShareRuleException;
import com.leidos.xchangecore.core.infrastructure.exceptions.MissingShareRulesElementException;
import com.leidos.xchangecore.core.infrastructure.exceptions.SOAPServiceException;
import com.leidos.xchangecore.core.infrastructure.service.AgreementService;
import com.leidos.xchangecore.core.infrastructure.util.ServiceNamespaces;

/**
 * The XchangeCore Agreement Service provides a means to define information sharing agreements between
 * XchangeCore Cores. These information sharing agreements define the terms and conditions under which
 * agencies will share information. These agreements must be mutually established prior to sharing
 * data. Once an agreement is in place the core servers will be allowed to share incidents.
 * <p/>
 * The AgreementType is defined as the following data structure:<br/>
 * <img src="doc-files/Agreement.png"/>
 * </p>
 * An instance of an information sharing agreement specifies the unique identity of the member
 * parties, the conditions for sharing of an incident, and the scope of the agreement. Each party to
 * the agreement is represented by the unique identity of their core which is the Jabber Identifier
 * assigned to their core. In general, this is of the form XchangeCore@hostname.domain.name. The share
 * conditions are based on the type of incident as designated in the ActivityCategoryText of the
 * incident document. The following examples of agreements show the different ways that sharing can
 * be specified.
 *
 * <h1>Share all incidents</h1> There are two ways to share all incidents, have an empty ShareRules
 * element or disable all the individual ShareRules. <h2>
 * Empty ShareRules</h2>
 *
 * <pre>
 *   &lt;as:Agreement&gt;
 *     &lt;as:Principals&gt;
 *       &lt;as:LocalCore&gt;XchangeCore@core.domain.com&lt;/as:LocalCore&gt;
 *       &lt;as:RemoteCore&gt;XchangeCore@core.otherdomain.com&lt;/as:RemoteCore&gt;
 *     &lt;/as:Principals&gt;
 *     &lt;as:ShareRules enabled=&quot;true&quot; xsi:nil=&quot;true&quot;/&gt;
 *   &lt;/as:Agreement&gt;
 * </pre>
 *
 * <h2>Disable All ShareRules</h2>
 *
 * <pre>
 *   &lt;as:Agreement&gt;
 *     &lt;as:Principals&gt;
 *       &lt;as:LocalCore&gt;XchangeCore@core.domain.com&lt;/as:LocalCore&gt;
 *       &lt;as:RemoteCore&gt;XchangeCore@core.otherdomain.com&lt;/as:RemoteCore&gt;
 *     &lt;/as:Principals&gt;
 *     &lt;as:ShareRules enabled=&quot;true&quot;&gt;
 *       &lt;as:ShareRule enabled=&quot;false&quot; id=&quot;rule1&quot;&gt;
 *         &lt;as:Condition&gt;
 *           &lt;as:InterestGroup precisb:codespace=&quot;http://uicds.org/interestgroup#Incident&quot; &gt;CBRNE&lt;/as:InterestGroup&gt;
 *         &lt;/as:Condition&gt;
 *       &lt;/as:ShareRule&gt;
 *     &lt;/as:ShareRules&gt;
 *   &lt;/as:Agreement&gt;
 * </pre>
 *
 * <h1>Share no incidents</h1> Once the ShareRules enabled attribute is set to false no more new
 * incidents will be shared with the other party. All incidents that are currently shared will
 * continue to be shared until they are closed and archived.
 *
 * <pre>
 *   &lt;as:Agreement&gt;
 *     &lt;as:Principals&gt;
 *       &lt;as:LocalCore&gt;XchangeCore@core.domain.com&lt;/as:LocalCore&gt;
 *       &lt;as:RemoteCore&gt;XchangeCore@core.otherdomain.com&lt;/as:RemoteCore&gt;
 *     &lt;/as:Principals&gt;
 *     &lt;as:ShareRules enabled=&quot;false&quot; xsi:nil=&quot;true&quot;/&gt;
 *   &lt;/as:Agreement&gt;
 * </pre>
 *
 * <h1>Share only Traffic incidents</h1>
 *
 * <pre>
 *   &lt;as:Agreement&gt;
 *     &lt;as:Principals&gt;
 *       &lt;as:LocalCore&gt;XchangeCore@core.domain.com&lt;/as:LocalCore&gt;
 *       &lt;as:RemoteCore&gt;XchangeCore@core.otherdomain.com&lt;/as:RemoteCore&gt;
 *     &lt;/as:Principals&gt;
 *     &lt;as:ShareRules enabled=&quot;true&quot;&gt;
 *       &lt;as:ShareRule enabled=&quot;true&quot; id=&quot;idvalue2&quot;&gt;
 *         &lt;as:Condition&gt;
 *           &lt;as:InterestGroup precisb:codespace=&quot;http://uicds.org/interestgroup#Incident&quot; &gt;Traffic&lt;/as:InterestGroup&gt;
 *         &lt;/as:Condition&gt;
 *       &lt;/as:ShareRule&gt;
 *     &lt;/as:ShareRules&gt;
 *   &lt;/as:Agreement&gt;
 *
 * </pre>
 *
 * <h1>Share only Geo, Traffic and CBRNE incidents</h1>
 *
 * <pre>
 *   &lt;as:Agreement&gt;
 *     &lt;as:Principals&gt;
 *       &lt;as:LocalCore&gt;XchangeCore@core.domain.com&lt;/as:LocalCore&gt;
 *       &lt;as:RemoteCore&gt;XchangeCore@core.otherdomain.com&lt;/as:RemoteCore&gt;
 *     &lt;/as:Principals&gt;
 *     &lt;as:ShareRules enabled=&quot;true&quot;&gt;
 *       &lt;as:ShareRule enabled=&quot;false&quot; id=&quot;idvalue0&quot;&gt;
 *         &lt;as:Condition&gt;
 *           &lt;as:InterestGroup precisb:codespace=&quot;http://uicds.org/interestgroup#Incident&quot; &gt;Geo&lt;/as:InterestGroup&gt;
 *         &lt;/as:Condition&gt;
 *       &lt;/as:ShareRule&gt;
 *       &lt;as:ShareRule enabled=&quot;true&quot; id=&quot;idvalue1&quot;&gt;
 *         &lt;as:Condition&gt;
 *           &lt;as:InterestGroup precisb:codespace=&quot;http://uicds.org/interestgroup#Incident&quot; &gt;CBRNE&lt;/as:InterestGroup&gt;
 *         &lt;/as:Condition&gt;
 *       &lt;/as:ShareRule&gt;
 *       &lt;as:ShareRule enabled=&quot;true&quot; id=&quot;idvalue2&quot;&gt;
 *         &lt;as:Condition&gt;
 *           &lt;as:InterestGroup precisb:codespace=&quot;http://uicds.org/interestgroup#Incident&quot; &gt;Traffic&lt;/as:InterestGroup&gt;
 *         &lt;/as:Condition&gt;
 *       &lt;/as:ShareRule&gt;
 *     &lt;/as:ShareRules&gt;
 *   &lt;/as:Agreement&gt;
 * </pre>
 *
 * <h1>Manual Sharing of Incidents</h1> This rule will not be processed by the automatic sharing
 * process. It will allow a call to the shareIncident operation on the Incident Management Service
 * to share an incident to any core that has an agreement. This rule may be used in addition to
 * other explicit incident type rules. The automatic sharing process will share incidents that match
 * the other rules and this rule will allow a call to shareIncident to share incidents that do not
 * match other explict rules. Setting the value of the InterestGroup element with this codespace to
 * false will turn off this ability.
 *
 * <pre>
 *    &lt;as:ShareRules enabled="true"&gt;
 *      &lt;as:ShareRule enabled="true" id="manual"&gt;
 *       &lt;as:Condition&gt;
 *         &lt;as:InterestGroup precisb:codespace="http://uicds.org/interestgroup#Manual"&gt;true&lt;/as:InterestGroup&gt;
 *       &lt;/as:Condition&gt;
 *    &lt;/as:ShareRule&gt;
 * </pre>
 *
 * <p>
 * Note that the time interval and polygon in the Condition element are currently not used.
 * <p>
 *
 * @author William Summers
 * @since 1.0
 * @see <a href="../../wsdl/AgreementService.wsdl">Appendix: AgreementService.wsdl</a>
 * @see <a href="../../services/Agreement/0.1/Agreement.xsd">Appendix: Agreement.xsd</a>
 * @see <a href="../../services/Agreement/0.1/AgreementService.xsd">Appendix: AgreementService.xsd</a>
 * @idd
 */
@Endpoint
@Transactional
public class AgreementServiceEndpoint
    implements ServiceNamespaces {

    Logger log = LoggerFactory.getLogger(AgreementServiceEndpoint.class);

    @Autowired
    private AgreementService agreementService;

    /**
     * Allows the client to create a new Agreement.
     *
     * @see <a href="../../services/Agreement/0.1/Agreement.xsd">Appendix: Agreement.xsd</a>
     * @see <a href="../../services/Agreement/0.1/AgreementService.xsd">Appendix: AgreementService.xsd</a>
     *
     * @param CreateAgreementRequestDocument
     *
     * @return CreateAgreementResponseDocument
     *
     * @throws MissingShareRulesElementException - Invalid request (ShareRules element is missing)
     * @throws MissingConditionInShareRuleException - Problem with the Rules elements
     * @throws AgreementWithCoreExists - An agreement with this core already exists
     * @idd
     */
    @PayloadRoot(namespace = NS_AgreementService, localPart = "CreateAgreementRequest")
    public CreateAgreementResponseDocument createAgreement(CreateAgreementRequestDocument requestDoc)
        throws SOAPServiceException {

        log.debug("createAgreement: id=" +
                  requestDoc.getCreateAgreementRequest().getAgreement().getPrincipals().getRemoteCore());

        // call create agreement
        final AgreementType agreement = agreementService.createAgreement(requestDoc.getCreateAgreementRequest().getAgreement());

        // return doc
        final CreateAgreementResponseDocument response = CreateAgreementResponseDocument.Factory.newInstance();
        response.addNewCreateAgreementResponse().setAgreement(agreement);
        return response;

    }

    /**
     * Allows the client to retrieve an agreement associated with a specific agreementID.
     *
     * @see <a href="../../services/Agreement/0.1/AgreementService.xsd">Appendix: AgreementService.xsd</a>
     *
     * @param GetAgreementRequestDocument
     *
     * @return GetAgreementResponseDocument
     * @idd
     */
    @PayloadRoot(namespace = NS_AgreementService, localPart = "GetAgreementRequest")
    public GetAgreementResponseDocument getAgreement(GetAgreementRequestDocument requestDoc)
        throws SOAPServiceException {

        log.debug("getAgreement: agreementID: " +
                  requestDoc.getGetAgreementRequest().getAgreementID());

        final AgreementType agreement = agreementService.getAgreement(requestDoc.getGetAgreementRequest().getAgreementID());
        if (agreement != null) {
            final GetAgreementResponseDocument response = GetAgreementResponseDocument.Factory.newInstance();
            response.addNewGetAgreementResponse().setAgreement(agreement);
            return response;
        } else {
            throw new SOAPServiceException("GetAgreement: " +
                                           requestDoc.getGetAgreementRequest().getAgreementID() +
                                           " not existed");
        }
    }

    /**
     * Allows the client to retrieve a list of existing Agreements.
     *
     * @see <a href="../../services/Agreement/0.1/AgreementService.xsd">Appendix: AgreementService.xsd</a>
     *
     * @param GetAgreementListRequestDocument
     *
     * @return GetAgreementListResponseDocument
     *
     * @idd
     */

    @PayloadRoot(namespace = NS_AgreementService, localPart = "GetAgreementListRequest")
    public GetAgreementListResponseDocument getAgreementList(GetAgreementListRequestDocument requestDoc) {

        final GetAgreementListResponseDocument response = GetAgreementListResponseDocument.Factory.newInstance();

        log.debug("getAgreementList");

        final AgreementListType agreementsList = agreementService.getAgreementList();

        response.addNewGetAgreementListResponse().setAgreementList(agreementsList);

        return response;
    }

    /**
     * Allows the client to delete an existing agreement.
     *
     * @see <a href="../../services/Agreement/0.1/AgreementService.xsd">Appendix: AgreementService.xsd</a>
     *
     * @param RescindAgreementRequestDocument
     *
     * @return RescindAgreementResponseDocument
     * @idd
     */
    @PayloadRoot(namespace = NS_AgreementService, localPart = "RescindAgreementRequest")
    public RescindAgreementResponseDocument rescindAgreement(RescindAgreementRequestDocument requestDoc) {

        log.debug("rescindAgreement: agreementID: " +
                  requestDoc.getRescindAgreementRequest().getAgreementID());

        final boolean isSuccess = agreementService.rescindAgreement(requestDoc.getRescindAgreementRequest().getAgreementID());
        final RescindAgreementResponseDocument response = RescindAgreementResponseDocument.Factory.newInstance();
        response.addNewRescindAgreementResponse().setStatus(isSuccess);

        return response;
    }

    public void setAgreementService(AgreementService service) {

        agreementService = service;
        log.debug("AgreementService set.");
    }

    /**
     * Allows the client to modify an existing Agreement.
     *
     * @see <a href="../../services/Agreement/0.1/Agreement.xsd">Appendix: Agreement.xsd</a>
     * @see <a href="../../services/Agreement/0.1/AgreementService.xsd">Appendix: AgreementService.xsd</a>
     *
     * @param UpdateAgreementRequestDocument
     *
     * @return UpdateAgreementResponseDocument
     *
     * @idd
     */
    @PayloadRoot(namespace = NS_AgreementService, localPart = "UpdateAgreementRequest")
    public UpdateAgreementResponseDocument updateAgreement(UpdateAgreementRequestDocument requestDoc)
        throws SOAPServiceException {

        log.debug("updateAgreement: agreementID: " +
                  requestDoc.getUpdateAgreementRequest().getAgreement().getId());

        final AgreementType agreement = agreementService.updateAgreement(requestDoc.getUpdateAgreementRequest().getAgreement());

        final UpdateAgreementResponseDocument response = UpdateAgreementResponseDocument.Factory.newInstance();
        response.addNewUpdateAgreementResponse().setAgreement(agreement);

        // return agreementService.amendAgreement(requestDoc);

        return response;
    }

}
