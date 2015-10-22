package com.leidos.xchangecore.core.infrastructure.controller;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class SchemaController
    extends AbstractController {

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {

        String path = "schemaorg_apache_xmlbeans/src" + request.getServletPath();

        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        if (in != null) {
            try {
                response.setContentType("text/xml");
                IOUtils.copy(in, response.getOutputStream());
            } finally {
                IOUtils.closeQuietly(in);
            }

        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }

        return null;
    }
}
