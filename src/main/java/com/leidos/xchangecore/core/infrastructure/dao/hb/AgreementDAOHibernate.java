/**
 * 
 */
package com.leidos.xchangecore.core.infrastructure.dao.hb;

import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.leidos.xchangecore.core.dao.hb.GenericHibernateDAO;
import com.leidos.xchangecore.core.infrastructure.dao.AgreementDAO;
import com.leidos.xchangecore.core.infrastructure.model.Agreement;
import com.leidos.xchangecore.core.infrastructure.model.ShareRule;

/**
 * @author summersw
 * 
 */
@Transactional
public class AgreementDAOHibernate
    extends GenericHibernateDAO<Agreement, Integer>
    implements AgreementDAO {

    Logger logger = LoggerFactory.getLogger(AgreementDAOHibernate.class);

    @Override
    public List<Agreement> findAll() {

        return super.findAll();
    }

    @Override
    public Agreement findByRemoteCoreName(String coreName) {

        Criterion criterion = Restrictions.like("remoteValue", coreName);
        List<Agreement> agreementList = findByCriteria(criterion);
        if (agreementList.size() == 1) {
            return agreementList.get(0);
        } else {
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Agreement> getAgreementsWithEnabledRules() {

        ShareRule rule = new ShareRule();
        rule.setEnabled(true);
        Criterion criterion = Restrictions.eq("shareRules", rule);

        List<Agreement> agreements = findByCriteria(criterion);
        System.out.println("FIND by crit END size: " + agreements.size());
        System.out.println("rules size: " + agreements.get(0).getShareRules().size());
        if (agreements.size() == 1) {
            return agreements;
        } else {
            System.out.println("none ageementDAOHib");
            return null;
        }
    }

    @Override
    public boolean isRemoteCoreMutuallyAgreed(String remoteJID) {

        Agreement agreement = findByRemoteCoreName(remoteJID);
        return agreement == null ? false : agreement.isMutuallyAgreed();
    }

    @Override
    public void setRemoteCoreMutuallyAgreed(String remoteJID, boolean isMutuallyAgreed) {

        logger.debug("setRemoteCoreMutuallyAgreed: [" + remoteJID + "] to " +
                     (isMutuallyAgreed ? "true" : "false"));
        Agreement agreement = findByRemoteCoreName(remoteJID);
        if (agreement != null) {
            agreement.setMutuallyAgreed(isMutuallyAgreed);
            makePersistent(agreement);
        } else {
            logger.debug("setRemoteCoreMutuallyAgreed: [" + remoteJID + "] ... not found ...");
        }
    }
}
