package com.leidos.xchangecore.core.infrastructure.dao.hb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;

import com.leidos.xchangecore.core.infrastructure.dao.WorkProductDAO;
import com.leidos.xchangecore.core.infrastructure.model.WorkProduct;
import com.leidos.xchangecore.core.infrastructure.util.XmlUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
    "classpath*:*/applicationContext-dataSrc.xml",
    "file:src/test/resources/contexts/test-WorkProductDAOContext.xml"
})
@TransactionConfiguration(transactionManager = "transactionManager")
@Transactional
public class WorkProductDAOTest {

    @Autowired
    WorkProductDAO productDAO;

    @Test
    public void testAll() {

        final List<WorkProduct> productList = productDAO.findAll();
        for (final WorkProduct product : productList) {
            System.out.println("+++++++++++++++++++++++++++++++++++++++");
            System.out.println(product.getMetadata());
            final List<WorkProduct> products = productDAO.findAllClosedVersionOfProduct(product.getProductID());
            for (final WorkProduct p : products) {
                System.out.println("----- closed version: " + p.getMetadata() + "\n-----");
            }
            System.out.println("+++++++++++++++++++++++++++++++++++++++\n");
        }
    }

    @Test
    public void testFindByInterestGroup() {

        final String igID = "IG-9f1d846c-6f3f-40e3-96ef-1dd12ab75103";
        final List<WorkProduct> productList = productDAO.findByInterestGroup(igID);
        for (final WorkProduct product : productList) {
            System.out.println(product.getMetadata());
        }
    }

    @Test
    public void testFindByProductType() {

        final String productType = "Incident";
        final List<WorkProduct> products = productDAO.findByProductType(productType);
        for (final WorkProduct product : products) {
            System.out.println("findByProductType(" + productType + "): " + product.getMetadata());
        }
    }

    @Test
    public void testFindDocBySearchCriteria() throws Exception {

        final Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("full", new String[] {
            "true",
            ""
        });
        params.put("format", new String[] {
            "xml"
        });
        params.put("what", new String[] {
            "Fire"
        });
        params.put("req.remoteUser", new String[] {
            "admin",
        });

        /*
        params.put("productType", new String[] {
            "incident",
            ""
        });
        params.put("productType", new String[] {
            "alert",
        });
        params.put("startIndex", new String[] {
            "1",
        });
        params.put("count", new String[] {
            "3",
        });

        params.put("bbox", new String[] {
            "-120,30,-80,50",
            ""
        });
        params.put("productVersion", new String[] {
            "1",
        });
        params.put("productID", new String[] {
            "Alert-afcb54aa-ea1b-41e2-8824-92134a11aead",
        });

         */

        final Document doc = productDAO.findDocsBySearchCriteria(params);
        if (doc != null) {
            System.out.println("Found:\n" + XmlUtil.getDOMString(doc));
        }
    }
}
