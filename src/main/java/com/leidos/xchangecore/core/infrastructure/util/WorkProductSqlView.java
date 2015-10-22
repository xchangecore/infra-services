package com.leidos.xchangecore.core.infrastructure.util;

import java.util.Map;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.view.AbstractView;
import org.w3c.dom.Document;

public class WorkProductSqlView
    extends AbstractView {

    private MimetypesFileTypeMap mimeTypes = new MimetypesFileTypeMap();

    @Override
    protected void renderMergedOutputModel(Map model,
                                           HttpServletRequest request,
                                           HttpServletResponse response) throws Exception {

        String format = (String) model.get("format");
        NodeRenderer renderer = (NodeRenderer) model.get("renderer");
        if (format.equalsIgnoreCase("debug")) {
            renderer.loadConfiguration();
        }
        if (StringUtils.isBlank(format)) {
            response.setContentType("application/octet-stream");
        } else if (format.equals("debug")) {
            response.setContentType("application/xml");
        } else {
            response.setContentType(mimeTypes.getContentType("response." + format));
        }

        String result = "";
        if (model.get("output") != null) {
            Document output = (Document) model.get("output");
            if (renderer.getPreparedQueries().containsKey(format)) {
                result = renderer.exec(output,
                    (Map<String, String[]>) model.get("propertiesMap"),
                    format).get("output").toString();
            } else {
                result = "<html><h3>No suitable formatter found.</h3></html>";
            }
        } else {
            result = "<html><h3>Controller DAO is null or Output is null.</h3></html>";
        }
        response.getOutputStream().write(result.getBytes());
        response.getOutputStream().close();
    }
}