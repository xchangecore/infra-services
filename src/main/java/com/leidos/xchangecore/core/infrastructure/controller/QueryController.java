package com.leidos.xchangecore.core.infrastructure.controller;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.w3c.dom.Document;

import com.leidos.xchangecore.core.infrastructure.service.ISearchService;
import com.leidos.xchangecore.core.infrastructure.util.NodeRenderer;
import com.leidos.xchangecore.core.infrastructure.util.WorkProductView;

/**
 * The Search Service provides XchangeCore clients with services to discover and
 * access work products using OpenSearch enabled feeds.
 *
 * The Search Service accepts queries in the following format:
 *
 * <pre>
 *     ?productType=Incident&updatedBy=bob@core1
 * </pre>
 *
 * The table follow describes the supported query terms.
 * <table>
 * <thead>
 * <tr>
 * <td width="20%"><b>Term</b></td>
 * <td><b>Description</b></td></thead> <tbody>
 * <tr>
 * <td width="20%">productID</td>
 * <td>The identifier of a Work Product</td>
 * </tr>
 * <tr>
 * <td width="20%">productType</td>
 * <td>The type of a Work Product</td>
 * </tr>
 * <tr>
 * <td width="20%">productVersion</td>
 * <td>The version of a Work Product</td>
 * </tr>
 * <tr>
 * <td width="20%">productState</td>
 * <td>The Work Product status (Open, Closed, Archive)</td>
 * </tr>
 * <tr>
 * <td width="20%">createdBegin</td>
 * <td>The beginning date and time of a range in which the Work Product was
 * created</td>
 * </tr>
 * <tr>
 * <td width="20%">createdEnd</td>
 * <td>The ending date and time of a range in which the Work Product was created
 * </td>
 * </tr>
 * <tr>
 * <td width="20%">createdBy</td>
 * <td>The name of the user that created the Work Product</td>
 * </tr>
 * <tr>
 * <td width="20%">updatedBegin</td>
 * <td>The beginning date and time of a range in which the Work Product was last
 * updated</td>
 * </tr>
 * <tr>
 * <td width="20%">updatedEnd</td>
 * <td>The ending date and time of a range in which the Work Product was last
 * updated</td>
 * </tr>
 * <tr>
 * <td width="20%">updatedBy</td>
 * <td>The name of the user that last updated the Work Product</td>
 * </tr>
 * <tr>
 * <tr>
 * <td width="20%">bbox</td>
 * <td>A geospatial bounding box ( bbox=minX,minY,maxX,maxY ) intersecting or
 * containing the Work Product (based on the Digest)</td>
 * </tr>
 * <tr>
 * <td width="20%">what</td>
 * <td>The type of any "Thing" referenced within the Work Product metadata</td>
 * </tr>
 * <tr>
 * <td width="20%">mimetype</td>
 * <td>The mimetype of the Work Product payload</td>
 * </tr>
 * </tbody>
 * </table>
 *
 * The format of the results is controlled by the format parameter. For
 * instance:
 *
 * <pre>
 *     ?productType=Incident&updatedBy=bob@core1&format=rss
 * </pre>
 *
 * Currently supported formats:
 * <table>
 * <thead>
 * <tr>
 * <td width="20%"><b>Format</b></td>
 * <td><b>Description</b></td>
 * </tr>
 * </thead> <tbody>
 * <tr>
 * <td width="20%">kml</td>
 * <td>KML 2.2</td>
 * </tr>
 * <tr>
 * <td width="20%">w3crss</td>
 * <td>RSS 2.0 with W3C Basic Geo extensions</td>
 * </tr>
 * <tr>
 * <td width="20%">rss</td>
 * <td>RSS 2.0 with GeoRSS GML extensions</td>
 * </tr>
 * <tr>
 * <td width="20%">xml</td>
 * <td>XchangeCore Work Product Format (UCore 2.0 DataItemPackage)</td>
 * </tr>
 * </tbody>
 * </table>
 *
 * @see <a
 * href="http://www.opensearch.org/Specifications/OpenSearch/1.1">OpenSearch 1.1
 * Specification</a>
 * @see <a href="http://cyber.law.harvard.edu/rss/rss.html">RSS 2.0
 * Specification</a>
 * @see <a href="http://www.georss.org/">GeoRSS Specification</a>
 * @see <a href="http://www.georss.org/W3C_Basic">W3C Basic Geo Vocabulary</a>
 * @author William Summers
 * @idd
 */
public class QueryController
    extends AbstractController {

    private ISearchService service;
    private String serviceName;
    private NodeRenderer nodeRenderer;

    Logger log = LoggerFactory.getLogger(this.getClass());

    public NodeRenderer getNodeRenderer() {

        return this.nodeRenderer;
    }

    public ISearchService getService() {

        return this.service;
    }

    public String getServiceName() {

        return this.serviceName;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {

        // a copy must be made to avoid synchronization issues
        final HashMap<String, String[]> params = new HashMap<String, String[]>(request.getParameterMap());

        // we're already adding the remoteuser (no need for principal object since
        // this is all we need to filter feeds objects.
        if (request.getRemoteUser() != null) {
            final String[] remoteUser = {
                StringEscapeUtils.escapeXml(request.getRemoteUser())
            };
            params.put("req.remoteUser", remoteUser);
        }

        if (request.getPathInfo() != null) {
            final String[] resourcePath = {
                StringEscapeUtils.escapeXml(request.getPathInfo())
            };
            params.put("req.resourcePath", resourcePath);
        }

        if (request.getQueryString() != null) {
            final String[] queryString = {
                StringEscapeUtils.escapeXml(request.getQueryString())
            };
            params.put("req.queryString", queryString);
        }

        String format = request.getParameter("format");
        if (format == null) {
            format = "xml";
        }

        final Document workProductDoc = this.getService().searchAsDocument(params);

        final ModelAndView mav = new ModelAndView(new WorkProductView());
        mav.getModel().put("output", workProductDoc);
        mav.getModel().put("renderer", this.getNodeRenderer());
        mav.getModel().put("propertiesMap", request.getParameterMap());
        mav.getModel().put("format", format);

        return mav;
    }

    public void setNodeRenderer(NodeRenderer nodeRenderer) {

        this.nodeRenderer = nodeRenderer;
    }

    public void setService(ISearchService service) {

        this.service = service;
    }

    public void setServiceName(String serviceName) {

        this.serviceName = serviceName;
    }
}
