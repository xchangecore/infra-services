package com.leidos.xchangecore.core.infrastructure.dao;

import java.util.Set;

import com.leidos.xchangecore.core.dao.GenericDAO;
import com.leidos.xchangecore.core.infrastructure.model.CodeSpaceValueType;

public interface CodeSpaceValueTypeDAO
    extends GenericDAO<CodeSpaceValueType, Integer> {

    public Set<CodeSpaceValueType> findAllCodeSpaceValueTypes();

}
