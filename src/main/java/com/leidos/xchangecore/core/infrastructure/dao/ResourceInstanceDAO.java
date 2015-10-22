package com.leidos.xchangecore.core.infrastructure.dao;

import com.leidos.xchangecore.core.dao.GenericDAO;
import com.leidos.xchangecore.core.infrastructure.model.ResourceInstanceModel;

public interface ResourceInstanceDAO
    extends GenericDAO<ResourceInstanceModel, String> {

    public ResourceInstanceModel findByLabel(String label);

    public ResourceInstanceModel findByIdentifier(String identifier);

}