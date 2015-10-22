package com.leidos.xchangecore.core.infrastructure.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leidos.xchangecore.core.infrastructure.model.WorkProduct;

public class WorkProductSerializer {

    private static final Logger logger = LoggerFactory.getLogger(WorkProductSerializer.class);
    private static String fName = "/uicds/data/products.ser";
    private static WorkProductSerializer instance = null;
    private static ObjectOutputStream oos = null;

    protected WorkProductSerializer() {

        if (oos == null) {
            try {
                logger.debug("create the serializer file ");
                oos = new ObjectOutputStream(new FileOutputStream(fName));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static WorkProductSerializer getInstance() {

        if (instance == null) {
            synchronized (WorkProductSerializer.class) {
                if (instance == null) {
                    instance = new WorkProductSerializer();
                }
            }
        }
        return instance;
    }

    public void addProduct(WorkProduct product) {

        try {
            oos.writeObject(product);
        } catch (IOException e) {
            logger.error("serialize Product: " + product.getProductID() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void closeIt() {

        try {
            if (oos != null) {
                oos.flush();
                oos.close();
            }
        } catch (IOException e) {
            logger.error("close the serializer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

        // TODO Auto-generated method stub

    }
}
