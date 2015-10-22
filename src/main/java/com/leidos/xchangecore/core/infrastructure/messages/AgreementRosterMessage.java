package com.leidos.xchangecore.core.infrastructure.messages;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AgreementRosterMessage {

    public static enum State {
        CREATE, AMEND, RESCIND,
    }

    public static String NAME = "AgreementRosterMessage";;

    public static String getName() {

        return NAME;
    }

    public static void setName(String name) {

        NAME = name;
    }

    private int agreementID;

    // Map of <Key: coreName , Value: State>
    private Map<String, State> cores = new HashMap<String, State>();

    public AgreementRosterMessage(int agreementID, Map<String, State> cores) {

        setAgreementID(agreementID);
        setCores(cores);

    }

    public int getAgreementID() {

        return agreementID;
    }

    public Map<String, State> getCores() {

        return cores;
    }

    public void setAgreementID(int agreementID) {

        this.agreementID = agreementID;
    }

    public void setCores(Map<String, State> cores) {

        this.cores = cores;
    }

    @Override
    public String toString() {

        Set<String> keys = cores.keySet();
        String out = "";
        for (String value : keys) {
            out += ", " + value;
        }

        return out;
    }

}
