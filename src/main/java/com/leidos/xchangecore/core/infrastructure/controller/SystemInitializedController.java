package com.leidos.xchangecore.core.infrastructure.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.message.GenericMessage;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class SystemInitializedController
extends AbstractController {

    private final Logger logger = LoggerFactory.getLogger(SystemInitializedController.class);
    private MessageChannel systemInitializedChannel;

    public MessageChannel getSystemInitializedChannel() {

        return systemInitializedChannel;
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {

        return null;
    }

    public void setSystemInitializedChannel(MessageChannel systemInitializedChannel) {

        logger.debug("SystemInitializedController::setSystemInitializedChannel() - called");
        this.systemInitializedChannel = systemInitializedChannel;
        logger.debug("SystemInitializedController::send SystemInitilized message");
        String msg = "System Initialized";
        Message<String> response = new GenericMessage<String>(msg);
        try {
            systemInitializedChannel.send(response);
            logger.debug("SystemInitializedController::setSystemInitializedChannel() - completed");
        } catch (Throwable e) {
            logger.debug("====> Exception caught: exception=" + e.getMessage());
            e.printStackTrace();
        }
    }
}
