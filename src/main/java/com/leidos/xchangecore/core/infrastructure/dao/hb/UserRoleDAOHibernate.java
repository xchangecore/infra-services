/**
 * 
 */
package com.leidos.xchangecore.core.infrastructure.dao.hb;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import com.leidos.xchangecore.core.dao.hb.GenericHibernateDAO;
import com.leidos.xchangecore.core.infrastructure.dao.UserRoleDAO;
import com.leidos.xchangecore.core.infrastructure.model.UserRole;

public class UserRoleDAOHibernate
    extends GenericHibernateDAO<UserRole, String>
    implements UserRoleDAO {

    @Override
    public List<UserRole> findUsersByRole(String roleRefID) {

        return findByCriteria(Restrictions.eq("roleRefId", roleRefID));
    }

}