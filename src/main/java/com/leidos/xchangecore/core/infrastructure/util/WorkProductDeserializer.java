package com.leidos.xchangecore.core.infrastructure.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leidos.xchangecore.core.infrastructure.model.WorkProduct;

public class WorkProductDeserializer {

    private static final Logger logger = LoggerFactory.getLogger(WorkProductSerializer.class);
    private static String fName = "./src/main/resources/product.ser";
    private ObjectInputStream ois = null;

    public WorkProductDeserializer() {

        this(fName);
    }

    public WorkProductDeserializer(String fName) {

        if (ois == null) {
            try {
                ois = new ObjectInputStream(new FileInputStream(fName));
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Cannot create the input stream for " + fName + ": " + e.getMessage());
            }
        }
    }

    public WorkProduct getProduct() {

        WorkProduct product = null;
        try {
            product = (WorkProduct) ois.readObject();
        } catch (Exception e) {
            logger.error("Cannot read Work Product: " + e.getMessage());
        }
        return product;
    }

    public void closeIt() {

        if (ois != null) {
            try {
                ois.close();
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("Cannot close the file: " + e.getMessage());
            }
        }
    }
}