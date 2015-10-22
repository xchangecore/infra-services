package com.leidos.xchangecore.core.infrastructure.dao;

import java.util.List;

import com.leidos.xchangecore.core.dao.GenericDAO;
import com.leidos.xchangecore.core.infrastructure.model.ExternalDataSourceConfig;

public interface ExternalDataSourceConfigDAO
    extends GenericDAO<ExternalDataSourceConfig, Integer> {

    public List<ExternalDataSourceConfig> findByUrn(String urn);

    public List<ExternalDataSourceConfig> findByCoreName(String coreName);
}
