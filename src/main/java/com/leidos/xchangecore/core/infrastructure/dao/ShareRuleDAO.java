package com.leidos.xchangecore.core.infrastructure.dao;

import java.util.Set;

import com.leidos.xchangecore.core.dao.GenericDAO;
import com.leidos.xchangecore.core.infrastructure.model.ShareRule;

public interface ShareRuleDAO
    extends GenericDAO<ShareRule, Integer> {

    public Set<ShareRule> findAllShareRules();

}
