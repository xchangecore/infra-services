package com.leidos.xchangecore.core.infrastructure.dao.hb;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.leidos.xchangecore.core.dao.hb.GenericHibernateDAO;
import com.leidos.xchangecore.core.infrastructure.dao.PublishedProductDAO;
import com.leidos.xchangecore.core.infrastructure.model.PublishedProduct;

public class PublishedProductHibernateDAO
    extends GenericHibernateDAO<PublishedProduct, Integer>
    implements PublishedProductDAO {

    public Set<PublishedProduct> findByProductType(String productType) {

        Criterion criterion = Restrictions.eq("productType", productType);
        List<PublishedProduct> publishedProductList = findByCriteria(criterion);

        //just direct convert list into set, no loop needed as below needed. FLi changed on 11/16/2011
        Set<PublishedProduct> publishedProducts = new HashSet<PublishedProduct>(publishedProductList);

        /*
        Set<PublishedProduct> publishedProducts = new HashSet<PublishedProduct>();        
        for (PublishedProduct prod : publishedProductList) {
            publishedProducts.add(prod);
        }
        */

        return publishedProducts;
    }

    public Set<PublishedProduct> findAllPublishedProducts() {

        List<PublishedProduct> publishedProductList = findAll();

        //just direct convert list into set, no loop needed as below needed. FLi changed on 11/16/2011
        Set<PublishedProduct> publishedProducts = new HashSet<PublishedProduct>(publishedProductList);

        /*
        Set<PublishedProduct> publishedProducts = new HashSet<PublishedProduct>();
        for (PublishedProduct prod : publishedProductList) {
            publishedProducts.add(prod);
        }
        */
        return publishedProducts;
    }

}
