package com.leidos.xchangecore.core.infrastructure.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringUtil {

    static Logger log = LoggerFactory.getLogger(StringUtil.class);

    private static final Pattern pattern = Pattern.compile("(.*)@(.*)");

    public static final String getSubmitterCoreName(String submitterID) {

        String coreName = null;
        if (submitterID != null) {
            Matcher matcher = pattern.matcher(submitterID);
            if (matcher.matches()) {
                log.info("******************* submitterID=" + submitterID + " userName=" +
                         matcher.group(1) + " coreName=" + matcher.group(2));
                coreName = matcher.group(2);
            } else {
                log.error("******* Receiving invalid submitterID [" + submitterID +
                          "]   expected format[userName@hostName]");
            }
        }
        return coreName;
    }

    public static final String getSubmitterResourceInstanceName(String submitterID) {

        String resourceInstanceName = null;
        if (submitterID != null) {
            Matcher matcher = pattern.matcher(submitterID);
            if (matcher.matches()) {
                log.info("******************* submitterID=" + submitterID + " userName=" +
                         matcher.group(1) + " coreName=" + matcher.group(2));
                resourceInstanceName = matcher.group(1);
            } else {
                log.error("******* Receiving invalid submitterID [" + submitterID +
                          "]   expected format[userName@hostName]");
            }
        }
        return resourceInstanceName;
    }

}
