package com.leidos.xchangecore.core.infrastructure.dao.hb;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.leidos.xchangecore.core.dao.hb.GenericHibernateDAO;
import com.leidos.xchangecore.core.infrastructure.dao.ShareRuleDAO;
import com.leidos.xchangecore.core.infrastructure.model.ShareRule;

public class ShareRuleHibernateDAO
    extends GenericHibernateDAO<ShareRule, Integer>
    implements ShareRuleDAO {

    public Set<ShareRule> findAllShareRules() {

        List<ShareRule> shareRuleList = findAll();
        Set<ShareRule> shareRules = new HashSet<ShareRule>(shareRuleList);
        /*
        Set<ShareRule> shareRules = new HashSet<ShareRule>();
        for (ShareRule shareRule : shareRuleList) {
            shareRules.add(shareRule);
        }
        */

        return shareRules;
    }

}
