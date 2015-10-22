package com.leidos.xchangecore.core.infrastructure.util;

import java.util.Map;
import java.util.Properties;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.view.AbstractView;
import org.w3c.dom.Document;

public class WorkProductView
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

        String type = "";
        if (StringUtils.isBlank(format)) {
            type = "application/octet-stream";
        } else if (format.equals("debug")) {
            type = "application/xml";
        } else {
            type = mimeTypes.getContentType("response." + format);
        }

        String result = "";
        if (model.get("output") != null) {
            Document output = (Document) model.get("output");
            if (renderer.getPreparedQueries().containsKey(format)) {
                Map<String, Object> renderMap = renderer.exec(output,
                    (Map<String, String[]>) model.get("propertiesMap"),
                    format);
                result = (String) renderMap.get("output");
                Properties properties = null;
                if (renderMap.containsKey("properties"))
                    properties = (Properties) renderMap.get("properties");
                if (properties != null)
                    type = properties.get("mediatype").toString();
            } else {
                result = "<html><h3>No suitable formatter found.</h3></html>";
            }
        } else {
            result = "<html><h3>Controller DAO is null or Output is null.</h3></html>";
        }
        response.setContentType(type);
        response.getOutputStream().write(result.getBytes());
        response.getOutputStream().close();
    }
}