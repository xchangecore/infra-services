package com.leidos.xchangecore.core.infrastructure.dao;

import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

import com.leidos.xchangecore.core.dao.GenericDAO;
import com.leidos.xchangecore.core.infrastructure.model.WorkProduct;
import com.saic.precis.x2009.x06.base.IdentificationType;

public interface WorkProductDAO
    extends GenericDAO<WorkProduct, Integer> {

    public void deleteProduct(Integer id);

    public List<WorkProduct> findAll(boolean isAscending);

    //public List<WorkProduct> findByCreatedBy(String createdBy);

    //public List<WorkProduct> findByCreatedDate(Date beginDate, Date endDate);

    public List<WorkProduct> findAllClosedVersionOfProduct(String productID);

    public List<WorkProduct> findAllVersionOfProduct(String productID);

    public List<WorkProduct> findByInterestGroup(String interestGroupID);

    //public WorkProduct findByProductIDAndChecksum(String productID, String checksum);

    //public WorkProduct findByProductIDAndType(String productID, String productType);

    public List<WorkProduct> findByInterestGroupAndType(String interestGroupID, String type);

    public WorkProduct findByProductID(String productID);

    //public List<WorkProduct> findBySize(Integer lowerBound, Integer upperBound);

    //public List<WorkProduct> findByTypeAndSize(String productType, Integer lowerBound, Integer upperBound);

    //public List<WorkProduct> findByUpdatedBy(String updatedBy);

    //public List<WorkProduct> findByUpdatedDate(Date beginDate, Date endDate);

    public WorkProduct findByProductIDAndVersion(String productID, Integer productVersion);

    public List<WorkProduct> findByProductType(String productType);

    public List<Object> findBySearchCritia(Map<String, String[]> params);

    public WorkProduct findByWorkProductIdentification(IdentificationType pkgId);

    public Document findDocsBySearchCriteria(Map<String, String[]> params);

    //public void markDeleted(String productID);
}
