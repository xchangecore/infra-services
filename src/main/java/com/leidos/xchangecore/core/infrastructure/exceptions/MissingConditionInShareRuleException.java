package com.leidos.xchangecore.core.infrastructure.exceptions;

@SuppressWarnings("serial")
public class MissingConditionInShareRuleException
    extends UICDSException {

    public MissingConditionInShareRuleException() {

        super(MissingConditionInShareRuleException.class.getName());
    }

    public MissingConditionInShareRuleException(String message) {

        super(message);
    }
}
