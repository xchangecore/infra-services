package com.leidos.xchangecore.core.infrastructure.dao;

import java.util.List;

import com.leidos.xchangecore.core.dao.GenericDAO;
import com.leidos.xchangecore.core.infrastructure.model.InterestGroup;

public interface InterestGroupDAO
    extends GenericDAO<InterestGroup, Integer> {

    public void delete(String interestGroupID, boolean isDelete);

    public InterestGroup findByInterestGroup(String interestGroupID);

    public List<InterestGroup> findByOwningCore(String owningCore);

    boolean ownedByCore(String interestGroupID, String corename);
}
