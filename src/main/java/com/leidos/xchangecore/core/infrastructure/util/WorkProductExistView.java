package com.leidos.xchangecore.core.infrastructure.util;

import java.util.List;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.view.AbstractView;

public class WorkProductExistView
    extends AbstractView {

    private MimetypesFileTypeMap mimeTypes = new MimetypesFileTypeMap();

    @SuppressWarnings({
        "rawtypes",
        "unchecked"
    })
    @Override
    protected void renderMergedOutputModel(Map model,
                                           HttpServletRequest request,
                                           HttpServletResponse response) throws Exception {

        String format = (String) model.get("format");
        if (StringUtils.isBlank(format)) {
            response.setContentType("application/octet-stream");
        } else if (format.equals("debug")) {
            response.setContentType("application/xml");
        } else {
            response.setContentType(mimeTypes.getContentType("response." + format));
        }

        if (model.get("output") != null) {
            List<Object> output = (List<Object>) model.get("output");
            for (Object out : output) {
                response.getOutputStream().write(((String) out.toString()).getBytes());
            }
            response.getOutputStream().close();
        } else {
            String tmp = "<html><h3>Controller DAO is null or Output is null.</h3></html>";
            response.getOutputStream().write(tmp.getBytes());
            response.getOutputStream().close();
        }
    }
}