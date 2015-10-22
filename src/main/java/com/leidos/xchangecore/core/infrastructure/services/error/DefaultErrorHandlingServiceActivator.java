package com.leidos.xchangecore.core.infrastructure.services.error;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.annotation.ServiceActivator;

public class DefaultErrorHandlingServiceActivator {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ServiceActivator
    public void handleThrowable(Message<Throwable> errorMessage) throws Throwable {

        Throwable throwable = errorMessage.getPayload();

        logger.error("Error message from error channel: " + throwable.getMessage());

        if (throwable instanceof MessagingException) {
            logger.error("MessagingException: ");
        } else {
            logger.error(throwable.toString());
        }
        logger.error(ExceptionUtils.getFullStackTrace(throwable));
    }

}
