package com.leidos.xchangecore.core.infrastructure.messages;

public class Core2CoreMessage {

    String fromCore;
    String toCore;
    String messageType;
    String message;
    String body;
    String xhtml;

    public String getBody() {

        return body;
    }

    public String getFromCore() {

        return fromCore;
    }

    public String getMessage() {

        return message;
    }

    public String getMessageType() {

        return messageType;
    }

    public String getToCore() {

        return toCore;
    }

    public String getXhtml() {

        return xhtml;
    }

    public void setBody(String body) {

        this.body = body;
    }

    public void setFromCore(String fromCore) {

        this.fromCore = fromCore;
    }

    public void setMessage(String message) {

        this.message = message;
    }

    public void setMessageType(String messageType) {

        this.messageType = messageType;
    }

    public void setToCore(String toCore) {

        this.toCore = toCore;
    }

    public void setXhtml(String xhtml) {

        this.xhtml = xhtml;
    }

    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append("[Core2CoreMessage]\n");
        sb.append("\tFrom: " + fromCore);
        sb.append("\tTo: " + toCore);
        sb.append("\tType: " + messageType);
        sb.append("\tMessage: " + message);
        return sb.toString();
    }
}
