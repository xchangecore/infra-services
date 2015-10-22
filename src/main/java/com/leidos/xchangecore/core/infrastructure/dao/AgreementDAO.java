/**
 * 
 */
package com.leidos.xchangecore.core.infrastructure.dao;

import java.util.List;

import com.leidos.xchangecore.core.dao.GenericDAO;
import com.leidos.xchangecore.core.infrastructure.model.Agreement;

/**
 * @author summersw
 * 
 */
public interface AgreementDAO
    extends GenericDAO<Agreement, Integer> {

    public Agreement findByRemoteCoreName(String coreName);

    public List<Agreement> getAgreementsWithEnabledRules();

    public boolean isRemoteCoreMutuallyAgreed(String remoteJID);

    void setRemoteCoreMutuallyAgreed(String remoteJID, boolean isMutuallyAgreed);
}
