/**
 * 
 */
package com.leidos.xchangecore.core.infrastructure.dao.hb;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import com.leidos.xchangecore.core.dao.hb.GenericHibernateDAO;
import com.leidos.xchangecore.core.infrastructure.dao.ResourceProfileDAO;
import com.leidos.xchangecore.core.infrastructure.model.ResourceProfileModel;

public class ResourceProfileDAOHibernate
    extends GenericHibernateDAO<ResourceProfileModel, String>
    implements ResourceProfileDAO {

    @Override
    public ResourceProfileModel findByIdentifier(String identifier) {

        List<ResourceProfileModel> results = findByCriteria(Restrictions.eq("identifier",
            identifier));
        if (!results.isEmpty()) {
            return results.get(0);
        }
        return null;
    }

    @Override
    public ResourceProfileModel findByLabel(String label) {

        List<ResourceProfileModel> results = findByCriteria(Restrictions.eq("label", label));
        if (!results.isEmpty()) {
            return results.get(0);
        }
        return null;
    }

}