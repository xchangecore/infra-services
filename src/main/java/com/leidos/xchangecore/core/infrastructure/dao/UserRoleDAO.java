package com.leidos.xchangecore.core.infrastructure.dao;

import java.util.List;

import com.leidos.xchangecore.core.dao.GenericDAO;
import com.leidos.xchangecore.core.infrastructure.model.UserRole;

public interface UserRoleDAO
    extends GenericDAO<UserRole, String> {

    public List<UserRole> findUsersByRole(String roleRefID);

}