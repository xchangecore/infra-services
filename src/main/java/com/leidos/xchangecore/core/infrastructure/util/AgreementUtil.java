package com.leidos.xchangecore.core.infrastructure.util;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uicds.agreementService.AgreementType;
import org.uicds.agreementService.AgreementType.Principals;
import org.uicds.agreementService.AgreementType.ShareRules;
import org.uicds.agreementService.AgreementType.ShareRules.ShareRule;
import org.uicds.agreementService.AgreementType.ShareRules.ShareRule.WorkProducts;
import org.uicds.agreementService.ConditionType;
import org.uicds.agreementService.ConditionType.RemoteCoreProximity;

import com.leidos.xchangecore.core.infrastructure.model.Agreement;
import com.leidos.xchangecore.core.infrastructure.model.ExtendedMetadata;
import com.saic.precis.x2009.x06.base.CodespaceValueType;
import com.saic.precis.x2009.x06.base.ExtendedMetadataType;
import com.saic.precis.x2009.x06.base.IdentifierType;

/**
 *
 * @author nathan
 *
 */
public class AgreementUtil {

    /**
     * Converts the persisted agreement to a xml type
     *
     * @param agreement
     * @return AgreementType
     */
    public static AgreementType copyProperties(Agreement agreement) {

        if (agreement == null) {
            return null;
        }

        final AgreementType agreementType = AgreementType.Factory.newInstance();
        agreementType.setId(agreement.getId());
        if (agreement.getDescription() != null) {
            agreementType.setDescription(agreement.getDescription());
        }

        // copy the consumer and provider
        if (agreement.getRemoteCore() != null && agreement.getLocalCore() != null) {

            final Principals principals = agreementType.addNewPrincipals();

            // add the remote core
            principals.addNewRemoteCore();
            final IdentifierType remoteCore = IdentifierType.Factory.newInstance();
            remoteCore.setStringValue(agreement.getRemoteCore().getValue());
            if (agreement.getRemoteCore().getLabel() != null) {
                remoteCore.setLabel(agreement.getRemoteCore().getLabel());
            }
            principals.setRemoteCore(remoteCore);

            // add the local core
            principals.addNewLocalCore();
            final IdentifierType localCore = IdentifierType.Factory.newInstance();
            localCore.setStringValue(agreement.getLocalCore().getValue());
            if (agreement.getLocalCore().getLabel() != null) {
                localCore.setLabel(agreement.getLocalCore().getLabel());
            }
            principals.setLocalCore(localCore);

            agreementType.setPrincipals(principals);
        }

        // copy the share rules
        if (agreement.getShareRules() != null && agreement.getShareRules().size() > 0) {

            // set the shareRulesEnabled field
            final ShareRules shareRules = ShareRules.Factory.newInstance();
            shareRules.setEnabled(agreement.isEnabled());

            final ShareRule[] shareRuleArray = new ShareRule[agreement.getShareRules().size()];

            int i = 0;

            for (final com.leidos.xchangecore.core.infrastructure.model.ShareRule rule : agreement.getShareRules()) {

                final ShareRule shareRule = AgreementType.ShareRules.ShareRule.Factory.newInstance();

                // copy id
                shareRule.setId(rule.getRuleID());

                // copy enabled
                shareRule.setEnabled(rule.isEnabled());

                // copy the interest group
                if (rule.getInterestGroup() != null) {
                    final ConditionType condition = shareRule.addNewCondition();
                    final CodespaceValueType interestGroup = condition.addNewInterestGroup();

                    if (rule.getInterestGroup().getLabel() != null) {
                        interestGroup.setLabel(rule.getInterestGroup().getLabel());
                    }
                    if (rule.getInterestGroup().getCodeSpace() != null) {
                        interestGroup.setCodespace(rule.getInterestGroup().getCodeSpace());
                    }
                    if (rule.getInterestGroup().getValue() != null) {
                        interestGroup.setStringValue(rule.getInterestGroup().getValue());
                    }
                    condition.setInterestGroup(interestGroup);

                    // copy extended metadata

                    if (rule.getExtendedMetadata() != null && rule.getExtendedMetadata().size() > 0) {
                        for (final ExtendedMetadata em : rule.getExtendedMetadata()) {
                            final ExtendedMetadataType extendedMetadata = condition.addNewExtendedMetadata();
                            extendedMetadata.setCode(em.getCode());
                            extendedMetadata.setCodespace(em.getCodespace());
                            extendedMetadata.setLabel(em.getLabel());
                            extendedMetadata.setStringValue(em.getValue());
                            /*
                            logger.debug("copying extended metadata: " +
                                         extendedMetadata.getCodespace() + " - " +
                                         extendedMetadata.getCode() + " = " +
                                         extendedMetadata.getStringValue());
                             */
                        }
                    }

                    if (rule.getRemoteCoreProximity() != null) {
                        /*
                        logger.debug("copying Proximity value: " +
                            rule.getRemoteCoreProximity().toString());
                         */
                        final RemoteCoreProximity proximity = condition.addNewRemoteCoreProximity();
                        proximity.setShareOnNoLoc(Boolean.valueOf(rule.getShareOnNoLoc()));
                        proximity.setStringValue(rule.getRemoteCoreProximity());
                    }

                    shareRule.setCondition(condition);
                }

                // copy work product types

                if (rule.getWorkProducts() != null && rule.getWorkProducts().size() > 0) {
                    final WorkProducts workProducts = shareRule.addNewWorkProducts();

                    for (final com.leidos.xchangecore.core.infrastructure.model.CodeSpaceValueType workProduct : rule.getWorkProducts()) {
                        final CodespaceValueType workProductType = workProducts.addNewType();
                        if (workProduct.getCodeSpace() != null) {
                            workProductType.setCodespace(workProduct.getCodeSpace());
                        }
                        if (workProduct.getLabel() != null) {
                            workProductType.setLabel(workProduct.getLabel());
                        }
                        workProductType.setStringValue(workProduct.getValue());

                    }

                    shareRule.setWorkProducts(workProducts);
                }

                shareRuleArray[i] = shareRule;
                i++;
            }
            shareRules.setShareRuleArray(shareRuleArray);

            agreementType.setShareRules(shareRules);
        } else {
            // set the shareRulesEnabled field
            final ShareRules shareRules = ShareRules.Factory.newInstance();
            shareRules.setEnabled(agreement.isEnabled());
            agreementType.setShareRules(shareRules);
        }

        // return the copy
        return agreementType;
    }

    public static Set<String> getIDs(String jid, String type) {

        final Set<String> idSet = new HashSet<String>();

        // ignore the core part
        final int index = jid.indexOf("?");
        if (index == -1) {
            return idSet;
        }

        final String jidString = jid.substring(index + 1);

        // separate each ids and groups
        final String[] list = jidString.split("&", -1);
        for (final String item : list) {
            final String[] parts = item.split("=");
            // if the type is not specified the continue
            if (parts[0].trim().equalsIgnoreCase(type) == false) {
                continue;
            }

            final String[] ids = parts[1].split(",", -1);
            for (final String id : ids) {
                idSet.add(id.trim());
            }
        }
        return idSet;
    }

    public static final String N_IDS = "ids";

    public static final String N_GROUPS = "groups";

    static Logger logger = LoggerFactory.getLogger(AgreementUtil.class);
}
