/**
 *
 */
package com.leidos.xchangecore.core.infrastructure.dao.hb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leidos.xchangecore.core.dao.hb.GenericHibernateDAO;
import com.leidos.xchangecore.core.infrastructure.dao.NotificationDAO;
import com.leidos.xchangecore.core.infrastructure.model.Notification;

public class NotificationDAOHibernate
    extends GenericHibernateDAO<Notification, String>
    implements NotificationDAO {

    private static Logger logger = LoggerFactory.getLogger(NotificationDAOHibernate.class);

    @Override
    public List<Notification> findAll() {

        final List<Notification> all = super.findAll();
        final Set<Notification> set = new HashSet<Notification>(all);
        final List<Notification> ret = new ArrayList<Notification>(set);
        return ret;
    }

    @Override
    public Notification findByEntityId(final String entityID) {

        final Criterion criterion = Restrictions.eq("entityID", entityID);
        final List<Notification> notifications = findByCriteria(criterion);

        return (notifications != null) && (notifications.size() != 0) ? notifications.get(0) : null;
    }

    @Override
    public List<Notification> findBySubscriptionId(final Integer SubID) {

        /* hibernate associate property query not work well this way, but leave here as a future reference.
        List<Notification> nList = getSession().createCriteria(Notification.class)  //getPersistentClass())
        .createCriteria("subscriptions")
        	        .add(Restrictions.eq("subscriptionID", new Integer(SubID)))
        	    //    .setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP)
        	       .list();
         */

        //we use hibernate root sql call and do this way.

        final String sql_query = "SELECT ENTITY_ID FROM Notification, NotificationSubscription " +
                                 "WHERE Notification.NOTIFICATION_ID = NotificationSubscription.NOTIFICATION_ID " +
                                 "and NotificationSubscription.SUBSCRIPTION_ID=" + SubID;

        logger.debug("findBySubscriptionId: Query: " + sql_query);

        final Query query = getSession().createSQLQuery(sql_query);
        @SuppressWarnings("unchecked")
        final List<String> nList = query.list();

        //see we got or not
        //System.out.println("Size= " + nList.size());

        final List<Notification> list = new ArrayList<Notification>();
        final Iterator<String> it = nList.iterator();
        while (it.hasNext()) {
            final String obj = it.next();

            //  see we got the right one or not
            //  System.out.println("Obj id= " + obj);
            final Notification not = findByEntityId(obj);
            if (not != null) {
                list.add(not);
            }

        }

        return list;

    }

    @Override
    public int findMsgCountByEntityId(final String entityID) {

        //fli added 11/29/2011
        final Notification notfication = findByEntityId(entityID);
        return notfication.getMsgCount();

    }

}