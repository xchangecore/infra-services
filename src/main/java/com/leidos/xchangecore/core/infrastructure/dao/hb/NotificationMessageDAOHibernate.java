/**
 * 
 */
package com.leidos.xchangecore.core.infrastructure.dao.hb;

import com.leidos.xchangecore.core.dao.GenericDAO;
import com.leidos.xchangecore.core.dao.hb.GenericHibernateDAO;
import com.leidos.xchangecore.core.infrastructure.model.NotificationMessage;

public class NotificationMessageDAOHibernate
    extends GenericHibernateDAO<NotificationMessage, String>
    implements GenericDAO<NotificationMessage, String> {

    static NotificationMessageDAOHibernate instance = null;

    public NotificationMessageDAOHibernate() {

        if (instance == null) {
            instance = this;
        }
    }

    public static NotificationMessageDAOHibernate getInstance() {

        return instance;
    }

}