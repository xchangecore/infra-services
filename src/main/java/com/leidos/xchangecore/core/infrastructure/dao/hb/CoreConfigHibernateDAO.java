package com.leidos.xchangecore.core.infrastructure.dao.hb;

import com.leidos.xchangecore.core.dao.hb.GenericHibernateDAO;
import com.leidos.xchangecore.core.infrastructure.dao.CoreConfigDAO;
import com.leidos.xchangecore.core.infrastructure.model.CoreConfig;

public class CoreConfigHibernateDAO
    extends GenericHibernateDAO<CoreConfig, Integer>
    implements CoreConfigDAO {

}
