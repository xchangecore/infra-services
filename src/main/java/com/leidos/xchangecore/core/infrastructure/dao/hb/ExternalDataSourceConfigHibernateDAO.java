package com.leidos.xchangecore.core.infrastructure.dao.hb;

import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.leidos.xchangecore.core.dao.hb.GenericHibernateDAO;
import com.leidos.xchangecore.core.infrastructure.dao.ExternalDataSourceConfigDAO;
import com.leidos.xchangecore.core.infrastructure.model.ExternalDataSourceConfig;

public class ExternalDataSourceConfigHibernateDAO
    extends GenericHibernateDAO<ExternalDataSourceConfig, Integer>
    implements ExternalDataSourceConfigDAO {

    public List<ExternalDataSourceConfig> findByUrn(String urn) {

        Criterion criterion = Restrictions.eq("urn", urn);
        List<ExternalDataSourceConfig> externalDataSources = findByCriteria(criterion);
        return externalDataSources;
    }

    public List<ExternalDataSourceConfig> findByCoreName(String coreName) {

        Criterion criterion = Restrictions.eq("coreName", coreName);
        List<ExternalDataSourceConfig> externalDataSources = findByCriteria(criterion);
        return externalDataSources;
    }

}
