package com.leidos.xchangecore.core.infrastructure.util;

import org.uicds.agreementService.AgreementType;
import org.uicds.agreementService.AgreementType.ShareRules;
import org.uicds.agreementService.ConditionType;

import com.saic.precis.x2009.x06.base.CodespaceValueType;

public class AgreementTestUtils {

    static public final String INTEREST_GROUP_ID = "IG";
    static public final String SHARE_RULE1_ID = "rule1";
    static public final String INCIDENT_WPTYPE = "Incident";
    static public final String INCIDENT_TYPE = "CBRNE";
    static public final String LOCAL_CORE1 = "local_core1";
    static public final String REMOTE_CORE1 = "remote_core1";
    static public final String UKNOWN_REMOTE_CORE = "UNKNOWN";

    public static AgreementType createAgreement() {

        AgreementType agreement = AgreementType.Factory.newInstance();
        agreement.addNewPrincipals();
        agreement.getPrincipals().addNewRemoteCore().setStringValue(AgreementTestUtils.REMOTE_CORE1);
        agreement.getPrincipals().addNewLocalCore().setStringValue(AgreementTestUtils.LOCAL_CORE1);
        return agreement;
    }

    public static AgreementType createAgreement(String localCore, String remoteCore) {

        AgreementType agreement = AgreementType.Factory.newInstance();
        agreement.addNewPrincipals();
        agreement.getPrincipals().addNewRemoteCore().setStringValue(remoteCore);
        agreement.getPrincipals().addNewLocalCore().setStringValue(localCore);
        agreement.addNewShareRules();
        agreement.getShareRules().setEnabled(true);
        return agreement;
    }

    public static void addShareRule(AgreementType agreement,
                                    String codespace,
                                    String incidentType,
                                    String ruleID,
                                    boolean enabled) {

        ShareRules shareRules = agreement.getShareRules();
        if (shareRules == null) {
            shareRules = agreement.addNewShareRules();
        }
        shareRules.setEnabled(true);

        ShareRules.ShareRule rule = shareRules.addNewShareRule();
        rule.setEnabled(enabled);
        rule.setId(ruleID);
        ConditionType condition = rule.addNewCondition();
        CodespaceValueType interestGroup = condition.addNewInterestGroup();
        interestGroup.setCodespace(codespace);
        interestGroup.setStringValue(incidentType);
    }

}
