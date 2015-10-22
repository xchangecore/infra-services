package com.leidos.xchangecore.core.infrastructure.dao;

import java.util.Set;

import com.leidos.xchangecore.core.dao.GenericDAO;
import com.leidos.xchangecore.core.infrastructure.model.SubscribedProduct;

public interface SubscribedProductDAO
    extends GenericDAO<SubscribedProduct, Integer> {

    public Set<SubscribedProduct> findByProductType(String productType);

    public Set<SubscribedProduct> findAllSubscribedProducts();

}
