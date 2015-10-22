package com.leidos.xchangecore.core.infrastructure.dao;

import java.util.Set;

import com.leidos.xchangecore.core.dao.GenericDAO;
import com.leidos.xchangecore.core.infrastructure.model.PublishedProduct;

public interface PublishedProductDAO
    extends GenericDAO<PublishedProduct, Integer> {

    public Set<PublishedProduct> findByProductType(String productType);

    public Set<PublishedProduct> findAllPublishedProducts();
}
