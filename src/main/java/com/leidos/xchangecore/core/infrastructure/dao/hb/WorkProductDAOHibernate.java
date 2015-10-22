package com.leidos.xchangecore.core.infrastructure.dao.hb;

import gov.ucore.ucore.x20.DigestDocument;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.leidos.xchangecore.core.dao.hb.GenericHibernateDAO;
import com.leidos.xchangecore.core.infrastructure.dao.UserInterestGroupDAO;
import com.leidos.xchangecore.core.infrastructure.dao.WorkProductDAO;
import com.leidos.xchangecore.core.infrastructure.model.WorkProduct;
import com.leidos.xchangecore.core.infrastructure.util.DigestHelper;
import com.leidos.xchangecore.core.infrastructure.util.GeometryUtil;
import com.leidos.xchangecore.core.infrastructure.util.WorkProductConstants;
import com.leidos.xchangecore.core.infrastructure.util.WorkProductHelper;
import com.leidos.xchangecore.core.infrastructure.util.WorkProductQueryBuilder;
import com.saic.precis.x2009.x06.base.IdentificationType;
import com.saic.precis.x2009.x06.structures.WorkProductDocument;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

@Transactional
public class WorkProductDAOHibernate
    extends GenericHibernateDAO<WorkProduct, Integer>
    implements WorkProductDAO, WorkProductConstants {

    private final static Logger logger = LoggerFactory.getLogger(WorkProductDAOHibernate.class);

    private final static String N_WorkProductList = "WorkProductList";
    private final static Order SortByVersion_Desc = Order.desc(C_ProductVersion);
    private final static Order SortByVersion_Asc = Order.asc(C_ProductVersion);
    private final static Order SortByLastUpdated_Desc = Order.desc(C_UpdatedDate);
    private final static Order SortByLastUpdated_Asc = Order.asc(C_UpdatedDate);
    private final static Order SortByProductID_Desc = Order.desc(C_ProductID);
    private final static Order SortByProductID_Asc = Order.asc(C_ProductID);
    private final static Criterion Criterion_State_Active = Restrictions.eq(C_State, State_Active);
    private UserInterestGroupDAO userInterestGroupDAO;

    /*
     * @Override public void delete(Integer id) {
     *
     * WorkProduct product = findById(id); if (product != null) { makeTransient(product); }
     *
     * }
     */

    @Override
    public void deleteProduct(Integer id) {

        final WorkProduct product = this.findById(id);
        if (product != null) {
            makeTransient(product);
        }
    }

    @Override
    public List<WorkProduct> findAll() {

        return this.findAll(false);
    }

    @Override
    public List<WorkProduct> findAll(boolean isAscending) {

        final List<WorkProduct> productList = findUniquProductList(isAscending ? SortByLastUpdated_Asc : SortByLastUpdated_Desc);
        logger.debug("findAll: " + (isAscending ? Order_Asc : Order_Desc) + ", found " +
                     (productList == null ? 0 : productList.size()) + " entries");
        return productList == null ? new ArrayList<WorkProduct>() : productList;
    }

    @Override
    public List<WorkProduct> findAllClosedVersionOfProduct(String productID) {

        logger.debug("findAllClosedVersionOfProduct: " + productID);
        final List<Criterion> criterionList = new ArrayList<Criterion>();
        criterionList.add(Restrictions.eq(C_ProductID, productID));
        criterionList.add(Restrictions.eq(C_State, State_Inactive));
        final List<Order> orderList = new ArrayList<Order>();
        orderList.add(SortByVersion_Desc);

        final List<WorkProduct> productList = findByCriteriaAndOrder(0, orderList, criterionList);
        return productList;
    }

    @Override
    public List<WorkProduct> findAllVersionOfProduct(String productID) {

        logger.debug("findAllVersionOfProduct: " + productID);

        final List<Criterion> criterionList = new ArrayList<Criterion>();
        criterionList.add(Restrictions.eq(C_ProductID, productID));
        final List<Order> orderList = new ArrayList<Order>();

        final List<WorkProduct> productList = findByCriteriaAndOrder(0, orderList, criterionList);
        return productList;
    }

    @Override
    public List<WorkProduct> findByInterestGroup(String interestGroupID) {

        logger.debug("findByInterestGroup: IGID: " + interestGroupID);
        final List<Criterion> criterionList = new ArrayList<Criterion>();
        criterionList.add(Restrictions.like(C_AssociatedInterestGroupIDs, "%" + interestGroupID +
                                                                          "%"));
        final List<Order> orderList = new ArrayList<Order>();
        orderList.add(SortByProductID_Desc);
        orderList.add(SortByVersion_Desc);
        final List<WorkProduct> productList = findByCriteriaAndOrder(0, orderList, criterionList);
        if ((productList != null) && (productList.size() > 0)) {
            final Hashtable<String, WorkProduct> productHash = new Hashtable<String, WorkProduct>();
            for (final WorkProduct product : productList) {
                final WorkProduct p = productHash.get(product.getProductID());
                if (p != null) {
                    continue;
                }
                productHash.put(product.getProductID(), product);
            }

            final List<WorkProduct> products = new ArrayList<WorkProduct>(productHash.values());
            logger.debug("findByInterestGroup: found " + products.size() + " entries");
            return products;
        } else {
            return productList;
        }
    }

    @Override
    public List<WorkProduct> findByInterestGroupAndType(String interestGroupID, String productType) {

        logger.debug("findByInterestGroupAndType: IGID: " + interestGroupID + ", ProductType: " +
                     productType);
        final List<WorkProduct> productList = findUniquProductList(null,
                                                                   Restrictions.eq(C_ProductType,
                                                                                   productType));
        final List<WorkProduct> products = new ArrayList<WorkProduct>();
        for (final WorkProduct product : productList) {
            if (product.getAssociatedInterestGroupIDs().contains(interestGroupID)) {
                products.add(product);
            }
        }

        return products;
    }

    @Override
    public WorkProduct findByProductID(String productID) {

        logger.debug("findByProductID: ProductID: " + productID);

        final List<Order> orders = new ArrayList<Order>();
        orders.add(SortByVersion_Desc);
        final List<Criterion> criterions = new ArrayList<Criterion>();
        criterions.add(Restrictions.eq(C_ProductID, productID));

        final List<WorkProduct> productList = findByCriteriaAndOrder(0, orders, criterions);

        logger.debug("findByProductID: " + productID + ", found " +
                     (productList == null ? 0 : productList.size()) + " entries");
        if ((productList == null) || (productList.size() == 0)) {
            return null;
        }

        return productList.get(0);
    }

    @Override
    public WorkProduct findByProductIDAndVersion(String productID, Integer productVersion) {

        logger.debug("findByProductID: ProductID: " + productID + ", ProductVersion: " +
                     productVersion);

        final List<Criterion> criterionList = new ArrayList<Criterion>();
        criterionList.add(Restrictions.eq(C_ProductID, productID));
        criterionList.add(Restrictions.eq(C_ProductVersion, productVersion));
        final List<Order> orderList = new ArrayList<Order>();

        final List<WorkProduct> productList = findByCriteriaAndOrder(0, orderList, criterionList);
        if ((productList == null) || (productList.size() == 0)) {
            return null;
        }

        return productList.get(0);
    }

    @Override
    public List<WorkProduct> findByProductType(String productType) {

        logger.debug("findByProductType: ProductType: " + productType);

        final List<WorkProduct> productList = findUniquProductList(null,
                                                                   Restrictions.eq(C_ProductType,
                                                                                   productType));
        logger.debug("findByProductType: " + productType + ", found " +
                     (productList != null ? productList.size() : 0) + " entries");

        return productList;
    }

    @Override
    public List<Object> findBySearchCritia(Map<String, String[]> params) {

        logger.debug("findBySearchCritia: not implemented");

        return null;
    }

    @Override
    public WorkProduct findByWorkProductIdentification(IdentificationType pkgId) {

        logger.debug("findByWorkProductIdentification: productID: " +
                     pkgId.getIdentifier().getStringValue() + ", productType: " +
                     pkgId.getType().getStringValue() + ", productVersion: " +
                     pkgId.getVersion().getStringValue() + ", checksum: " +
                     pkgId.getChecksum().getStringValue() + ", state: " +
                     pkgId.getState().toString());
        final Criterion c1 = Restrictions.eq(C_ProductID, pkgId.getIdentifier().getStringValue());
        final Criterion c2 = Restrictions.eq(C_Checksum, pkgId.getChecksum().getStringValue());
        final Criterion c3 = Restrictions.eq(C_ProductType, pkgId.getType().getStringValue());
        final Criterion c4 = Restrictions.eq(C_State, pkgId.getState().toString());
        final Criterion c5 = Restrictions.eq(C_ProductVersion,
                                             Integer.parseInt(pkgId.getVersion().getStringValue()));
        final List<WorkProduct> productList = findUniquProductList(null, c1, c2, c3, c4, c5);
        logger.debug("findByWorkProductIdentification found: " + productList.size() + " entries");
        return productList != null ? productList.get(0) : null;
    }

    @Override
    public Document findDocsBySearchCriteria(Map<String, String[]> params) {

        final WorkProductQueryBuilder queryBuilder = new WorkProductQueryBuilder(params);
        final String username = queryBuilder.getUsername();
        if (username == null) {
            logger.error("No user specfied");
            return null;
        }

        // only find active product
        // c.add(Criterion_State_Active);
        // dsh
        final List<Order> orderList = new ArrayList<Order>();
        orderList.add(SortByVersion_Desc);
        orderList.add(SortByProductID_Desc);
        orderList.add(queryBuilder.getOrder());

        final List<WorkProduct> products = findByCriteriaAndOrder(queryBuilder.getStartIndex(),
                                                                  orderList,
                                                                  queryBuilder.getCriterionList());

        final Hashtable<String, WorkProduct> productSet = new Hashtable<String, WorkProduct>();
        List<WorkProduct> productList = new ArrayList<WorkProduct>();
        // only save the lastest version either active or not
        for (final WorkProduct p : products) {
            if (productSet.containsKey(p.getProductID())) {
                continue;
            }
            productSet.put(p.getProductID(), p);
        }
        productList = new ArrayList<WorkProduct>(productSet.values());

        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document doc = builder.newDocument();
            final Element results = doc.createElement(N_WorkProductList);
            doc.appendChild(results);

            logger.debug("findDocsBySearchCriteria: for: " + username + ": found: " +
                         productList.size() + " entries");

            if (productList.size() == 0) {
                return doc;
            }

            int cnt = 0;
            for (final WorkProduct product : productList) {
                // logger.debug("findDocsBySearchCriteria: found: " + product.getMetadata());
                // filtered by access permission
                if (queryBuilder.getWhatClause() != null) {
                    final DigestDocument digest = product.getDigest();
                    if ((digest != null) &&
                        (DigestHelper.containWhatClause(digest.getDigest(),
                                                        queryBuilder.getWhatClause()) == false)) {
                        continue;
                    }
                }
                final String IGID = product.getFirstAssociatedInterestGroupID();
                if ((IGID == null) && (queryBuilder.getIGIDSet() != null)) {
                    continue;
                }
                if ((IGID != null) && (queryBuilder.getIGIDSet() != null) &&
                    (queryBuilder.getIGIDSet().contains(IGID) == false)) {
                    // not matching the query IGs
                    // logger.debug("mismatch InterestGroupID: " + queryBuilder.getIGID());
                    continue;
                }
                // if the product has IGID and query string contains IGID the we need to match for
                // eligibilty
                if ((IGID != null) &&
                    (getUserInterestGroupDAO().isEligible(username, IGID) == false)) {
                    // logger.warn(username + " cannot access " + IGID + " skip product: " +
                    // product.getProductID());
                    continue;
                }
                WorkProductDocument.WorkProduct productDocument = null;
                if (queryBuilder.isFull()) {
                    productDocument = WorkProductHelper.toWorkProductDocument(product).getWorkProduct();
                } else {
                    productDocument = WorkProductHelper.toWorkProductSummary(product);
                }
                // filtered by the bounding box
                if ((queryBuilder.getBoundingBox() != null) &&
                    (intersects(queryBuilder.getBoundingBox(),
                                DigestHelper.getFirstGeometry(WorkProductHelper.getDigestElement(productDocument))) == false)) {
                    continue;
                }

                final Node node = doc.importNode(productDocument.getDomNode(), true);
                results.appendChild(node);
                if ((queryBuilder.getCount() != -1) && (queryBuilder.getCount() == ++cnt)) {
                    break;
                }
            }

            // logger.debug("Document:\n" + XmlUtil.Document2String(doc) + "\n");
            return doc;
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<WorkProduct> findUniquProductList(Order order, Criterion... criterions) {

        // Criteria criteria = getSession().createCriteria(WorkProduct.class);
        final List<Criterion> criterionList = new ArrayList<Criterion>();
        for (final Criterion c : criterions) {
            criterionList.add(c); // criteria.add(c);
        }

        final List<Order> orderList = new ArrayList<Order>();
        if (order != null) {
            if (!order.equals(SortByVersion_Desc) && !order.equals(SortByVersion_Asc)) {
                orderList.add(SortByVersion_Desc); // criteria.addOrder(SortByVersion_Desc);
            }
            if (!order.equals(SortByProductID_Desc) && !order.equals(SortByProductID_Asc)) {
                orderList.add(SortByProductID_Desc); // criteria.addOrder(SortByProductID_Desc);
            }
            orderList.add(order); // criteria.addOrder(order);
        } else {
            orderList.add(SortByVersion_Desc); // criteria.addOrder(SortByVersion_Desc);
            orderList.add(SortByProductID_Desc); // criteria.addOrder(SortByProductID_Desc);
        }

        final List<WorkProduct> productList = findByCriteriaAndOrder(0, orderList, criterionList);

        final List<WorkProduct> products = new ArrayList<WorkProduct>();
        String productID = null;
        for (final WorkProduct p : productList) {
            if (!p.getProductID().equals(productID)) {
                productID = p.getProductID();
                products.add(p);
            }
        }
        return products;
    }

    public UserInterestGroupDAO getUserInterestGroupDAO() {

        return userInterestGroupDAO;
    }

    private boolean intersects(Double[][] boundingBox, Geometry geom) {

        logger.debug("intersects: ");
        for (int i = 0; i < 5; i++) {
            logger.debug("Coordinates: (" + boundingBox[i][0] + ", " + boundingBox[i][1] + ")");
        }
        boolean found = false;
        if (geom instanceof Polygon) {
            logger.debug("geo type isIt's a Polygon: ");
            found = GeometryUtil.intersects(boundingBox, (Polygon) geom);
        } else if (geom instanceof Point) {
            logger.debug("geo type is a Point: [" + geom + "]");
            found = GeometryUtil.contains(boundingBox, (Point) geom);
        }
        logger.debug("intersects: " + (found ? "true" : "false"));
        return found;
    }

    public void setUserInterestGroupDAO(UserInterestGroupDAO interestGroupDAO) {

        userInterestGroupDAO = interestGroupDAO;
    }
}
