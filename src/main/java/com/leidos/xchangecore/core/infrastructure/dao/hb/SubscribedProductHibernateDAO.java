package com.leidos.xchangecore.core.infrastructure.dao.hb;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.leidos.xchangecore.core.dao.hb.GenericHibernateDAO;
import com.leidos.xchangecore.core.infrastructure.dao.SubscribedProductDAO;
import com.leidos.xchangecore.core.infrastructure.model.SubscribedProduct;

public class SubscribedProductHibernateDAO
    extends GenericHibernateDAO<SubscribedProduct, Integer>
    implements SubscribedProductDAO {

    public Set<SubscribedProduct> findByProductType(String productType) {

        Criterion criterion = Restrictions.eq("productType", productType);
        List<SubscribedProduct> subscribedProductList = findByCriteria(criterion);
        Set<SubscribedProduct> subscribedProducts = new HashSet<SubscribedProduct>(subscribedProductList);
        /*
        Set<SubscribedProduct> subscribedProducts = new HashSet<SubscribedProduct>();
        for (SubscribedProduct prod : subscribedProductList) {
            subscribedProducts.add(prod);
        }
        */
        return subscribedProducts;
    }

    public Set<SubscribedProduct> findAllSubscribedProducts() {

        List<SubscribedProduct> subscribedProductList = findAll();
        Set<SubscribedProduct> subscribedProducts = new HashSet<SubscribedProduct>(subscribedProductList);
        /*
        Set<SubscribedProduct> subscribedProducts = new HashSet<SubscribedProduct>();
        for (SubscribedProduct prod : subscribedProductList) {
            subscribedProducts.add(prod);
        }
        */
        return subscribedProducts;
    }
}
