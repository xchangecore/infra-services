package com.leidos.xchangecore.core.infrastructure.dao;

import com.leidos.xchangecore.core.dao.GenericDAO;
import com.leidos.xchangecore.core.infrastructure.model.ResourceProfileModel;

public interface ResourceProfileDAO
    extends GenericDAO<ResourceProfileModel, String> {

    public ResourceProfileModel findByLabel(String label);

    public ResourceProfileModel findByIdentifier(String identifier);

}