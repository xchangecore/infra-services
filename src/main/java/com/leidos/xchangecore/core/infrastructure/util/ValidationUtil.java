/**
 * 
 */
package com.leidos.xchangecore.core.infrastructure.util;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

/**
 * @author roger
 *
 */
public class ValidationUtil {

    public static boolean validate(XmlObject object, boolean printValidationErrors) {

        boolean valid = false;
        // Set up the validation error listener.
        ArrayList<String> validationErrors = new ArrayList<String>();
        XmlOptions validationOptions = new XmlOptions();
        validationOptions.setErrorListener(validationErrors);

        if (!object.validate(validationOptions) && printValidationErrors) {
            System.out.println("VALIDATION FAILED");
            Iterator<?> iter = validationErrors.iterator();
            while (iter.hasNext()) {
                System.out.println(">> " + iter.next() + "\n");
            }
        } else {
            valid = true;
        }
        return valid;
    }

}
