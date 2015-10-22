package com.leidos.xchangecore.core.infrastructure.dao.hb;

import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.leidos.xchangecore.core.dao.hb.GenericHibernateDAO;
import com.leidos.xchangecore.core.infrastructure.dao.ProductSubscriptionByIDDAO;
import com.leidos.xchangecore.core.infrastructure.model.ProductSubscriptionByID;

public class ProductSubscriptionByIDHibernateDAO
    extends GenericHibernateDAO<ProductSubscriptionByID, Integer>
    implements ProductSubscriptionByIDDAO {

    public List<ProductSubscriptionByID> findByProductID(String productId) {

        Criterion criterion = Restrictions.eq("productId", productId);
        List<ProductSubscriptionByID> subscriptions = findByCriteria(criterion);
        return subscriptions;
    }

    public List<ProductSubscriptionByID> findBySubscriberName(String subscriberName) {

        Criterion criterion = Restrictions.eq("subscriberName", subscriberName);
        List<ProductSubscriptionByID> subscriptions = findByCriteria(criterion);
        return subscriptions;
    }

    public List<ProductSubscriptionByID> findBySubscriptionId(Integer subscriptionId) {

        Criterion criterion = Restrictions.eq("subscriptionId", subscriptionId);
        List<ProductSubscriptionByID> subscriptions = findByCriteria(criterion);
        return subscriptions;
    }

}
