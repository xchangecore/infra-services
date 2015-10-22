package com.leidos.xchangecore.core.infrastructure.dao.hb;

import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.leidos.xchangecore.core.dao.hb.GenericHibernateDAO;
import com.leidos.xchangecore.core.infrastructure.dao.ProductSubscriptionByTypeDAO;
import com.leidos.xchangecore.core.infrastructure.model.ProductSubscriptionByType;

public class ProductSubscriptionByTypeHibernateDAO
    extends GenericHibernateDAO<ProductSubscriptionByType, Integer>
    implements ProductSubscriptionByTypeDAO {

    @Override
    public List<ProductSubscriptionByType> findByProductType(String productType) {

        Criterion criterion = Restrictions.eq("productType", productType);
        List<ProductSubscriptionByType> subscriptions = findByCriteria(criterion);
        return subscriptions;
    }

    @Override
    public List<ProductSubscriptionByType> findByInterestGroupIdAndProductType(String interestGroupID,
                                                                               String productType) {

        ProductSubscriptionByType example = new ProductSubscriptionByType();
        example.setInterestGroupID(interestGroupID);
        example.setProductType(productType);
        List<ProductSubscriptionByType> subscriptions = findByExample(example);
        return subscriptions;
    }

    @Override
    public List<ProductSubscriptionByType> findByXPath(String xPath) {

        Criterion criterion = Restrictions.eq("xPath", xPath);
        List<ProductSubscriptionByType> subscriptions = findByCriteria(criterion);
        return subscriptions;
    }

    @Override
    public List<ProductSubscriptionByType> findBySubscriberName(String subscriberName) {

        Criterion criterion = Restrictions.eq("subscriberName", subscriberName);
        List<ProductSubscriptionByType> subscriptions = findByCriteria(criterion);
        return subscriptions;
    }

    @Override
    public List<ProductSubscriptionByType> findBySubscriptionId(Integer subscriptionId) {

        Criterion criterion = Restrictions.eq("subscriptionId", subscriptionId);
        List<ProductSubscriptionByType> subscriptions = findByCriteria(criterion);
        return subscriptions;
    }

}
