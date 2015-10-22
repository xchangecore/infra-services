package com.leidos.xchangecore.core.infrastructure.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leidos.xchangecore.core.infrastructure.model.InterestGroup;
import com.leidos.xchangecore.core.infrastructure.service.impl.InterestGroupInfo;

public class InterestGroupInfoUtil {

    static Logger log = LoggerFactory.getLogger(InterestGroupInfoUtil.class);

    // XML tags used in formatting out-bound data
    private static final String interestGroupIDOpenTag = "<id>";
    private static final String interestGroupTypeOpenTag = "<type>";
    private static final String nameOpenTag = "<name>";
    private static final String descriptionOpenTag = "<description>";
    private static final String owningCoreOpenTag = "<owningCore>";
    private static final String detailedInfoOpenTag = "<detailedInfo>";

    private static final String interestGroupIDCloseTag = "</id>";
    private static final String interestGroupTypeCloseTag = "</type>";
    private static final String nameCloseTag = "</name>";
    private static final String descriptionCloseTag = "</description>";
    private static final String owningCoreCloseTag = "</owningCore>";
    private static final String detailedInfoCloseTag = "</detailedInfo>";

    // search patterns used to extract in-bound data
    private static final Pattern interestGroupIDPattern = Pattern.compile(interestGroupIDOpenTag +
                                                                          "(.+?)" +
                                                                          interestGroupIDCloseTag,
        Pattern.DOTALL | Pattern.MULTILINE);
    private static final Pattern interestGroupTypePattern = Pattern.compile(interestGroupTypeOpenTag +
                                                                            "(.+?)" +
                                                                            interestGroupTypeCloseTag,
        Pattern.DOTALL | Pattern.MULTILINE);
    private static final Pattern namePattern = Pattern.compile(nameOpenTag + "(.+?)" + nameCloseTag,
        Pattern.DOTALL | Pattern.MULTILINE);
    private static final Pattern descriptionPattern = Pattern.compile(descriptionOpenTag + "(.+?)" +
                                                                      descriptionCloseTag,
        Pattern.DOTALL | Pattern.MULTILINE);
    private static final Pattern owningCorePattern = Pattern.compile(owningCoreOpenTag + "(.+?)" +
                                                                     owningCoreCloseTag,
        Pattern.DOTALL | Pattern.MULTILINE);
    private static final Pattern detailedInfoPattern = Pattern.compile(detailedInfoOpenTag +
                                                                       "(.+?)" +
                                                                       detailedInfoCloseTag,
        Pattern.DOTALL | Pattern.MULTILINE);

    public static final String toInterestDetailedInfoString(String interestGroupInfoStr) {

        String detailedInfo = null;
        try {
            Matcher m = detailedInfoPattern.matcher(interestGroupInfoStr);
            if (m.find()) {
                detailedInfo = m.group(1);
            } else {
                log.error("InterestGroupInfoUtil:toInterestGroup: Can't find interestGroupID  in the received info string");
            }

        } catch (Throwable e) {
            log.error("Failure occurs while parsing InterestGroupInfo: str[" +
                      interestGroupInfoStr + "]");
            e.printStackTrace();
        }
        return detailedInfo;
    }

    public static final InterestGroupInfo toInterestGroupInfo(String interestGroupInfoStr) {

        InterestGroupInfo interestGroupInfo = new InterestGroupInfo();
        try {
            Matcher m;
            m = interestGroupIDPattern.matcher(interestGroupInfoStr);
            if (m.find()) {
                interestGroupInfo.setInterestGroupID(m.group(1));
            } else {
                log.error("InterestGroupInfoUtil:toInterestGroupInfo: Can't find interestGroupID  in the received info string");
            }

            m = interestGroupTypePattern.matcher(interestGroupInfoStr);
            if (m.find()) {
                interestGroupInfo.setInterestGroupType(m.group(1));
            } else {
                log.error("InterestGroupInfoUtil:toInterestGroupInfo: Can't find interestGroupType  in the received info string");
            }

            m = namePattern.matcher(interestGroupInfoStr);
            if (m.find()) {
                interestGroupInfo.setName(m.group(1));
            } else {
                log.warn("InterestGroupInfoUtil:toInterestGroupInfo: Can't find interestGroup name  in the received info string");
            }

            m = descriptionPattern.matcher(interestGroupInfoStr);
            if (m.find()) {
                interestGroupInfo.setDescription(m.group(1));
            } else {
                log.warn("InterestGroupInfoUtil:toInterestGroupInfo: Can't find interestGroup description  in the received info string");
            }

            m = owningCorePattern.matcher(interestGroupInfoStr);
            if (m.find()) {
                interestGroupInfo.setOwningCore(m.group(1));
            } else {
                log.error("InterestGroupInfoUtil:toInterestGroupInfo: Can't find interestGroup's owning core  in the received info string");
            }
        } catch (Throwable e) {
            log.error("Failure occurs while parsing InterestGroupInfo: str[" +
                      interestGroupInfoStr + "]");
            e.printStackTrace();
        }
        return interestGroupInfo;
    }

    public static final InterestGroup toInterestGroup(String interestGroupInfoStr) {

        InterestGroup interestGroup = new InterestGroup();;
        try {
            Matcher m;
            m = interestGroupIDPattern.matcher(interestGroupInfoStr);
            if (m.find()) {
                interestGroup.setInterestGroupID(m.group(1));
            } else {
                log.error("InterestGroupInfoUtil:toInterestGroup: Can't find interestGroupID  in the received info string");
            }

            m = interestGroupTypePattern.matcher(interestGroupInfoStr);
            if (m.find()) {
                interestGroup.setInterestGroupType(m.group(1));
            } else {
                log.error("InterestGroupInfoUtil:toInterestGroup: Can't find interestGroupType  in the received info string");
            }

            m = namePattern.matcher(interestGroupInfoStr);
            if (m.find()) {
                interestGroup.setName(m.group(1));
            } else {
                log.warn("InterestGroupInfoUtil:toInterestGroup: Can't find interestGroup name  in the received info string");
            }

            m = descriptionPattern.matcher(interestGroupInfoStr);
            if (m.find()) {
                interestGroup.setDescription(m.group(1));
            } else {
                log.warn("InterestGroupInfoUtil:toInterestGroup: Can't find interestGroup description  in the received info string");
            }

            m = owningCorePattern.matcher(interestGroupInfoStr);
            if (m.find()) {
                interestGroup.setOwningCore(m.group(1));
            } else {
                log.error("InterestGroupInfoUtil:toInterestGroup: Can't find interestGroup's owning core  in the received info string");
            }

        } catch (Throwable e) {
            log.error("Failure occurs while parsing InterestGroupInfo: str[" +
                      interestGroupInfoStr + "]");
            e.printStackTrace();
        }
        return interestGroup;
    }

    /**
     * Assumes that the incoming detailedInfo string is XML and already escaped
     * 
     * @param interestGroupInfo
     * @param detailedlInfo (XML string of more detailed info for the interest group)
     * @return
     */
    public static final String toXMLString(InterestGroupInfo interestGroupInfo, String detailedlInfo) {

        StringBuffer sb = new StringBuffer();
        sb.append(interestGroupIDOpenTag +
                  StringEscapeUtils.escapeXml(interestGroupInfo.getInterestGroupID()) +
                  interestGroupIDCloseTag);
        sb.append(interestGroupTypeOpenTag +
                  StringEscapeUtils.escapeXml(interestGroupInfo.getInterestGroupType()) +
                  interestGroupTypeCloseTag);
        sb.append(nameOpenTag + StringEscapeUtils.escapeXml(interestGroupInfo.getName()) +
                  nameCloseTag);
        sb.append(descriptionOpenTag +
                  StringEscapeUtils.escapeXml(interestGroupInfo.getDescription()) +
                  descriptionCloseTag);
        sb.append(owningCoreOpenTag +
                  StringEscapeUtils.escapeXml(interestGroupInfo.getOwningCore()) +
                  owningCoreCloseTag);
        sb.append(detailedInfoOpenTag + detailedlInfo + detailedInfoCloseTag);
        // log.debug("=====> toXMLString(interestGroupInfo,detailedlInfo) - sb=[" + sb.toString() + "]");
        return sb.toString();
    }

    /**
     * Assumes that the incoming detailedInfo string is XML and already escaped
     * 
     * @param interestGroup
     * @param detailedlInfo (XML string of more detailed info for the interest group)
     * @return
     */
    public static final String toXMLString(InterestGroup interestGroup, String detailedInfo) {

        StringBuffer sb = new StringBuffer();
        sb.append(interestGroupIDOpenTag +
                  StringEscapeUtils.escapeXml(interestGroup.getInterestGroupID()) +
                  interestGroupIDCloseTag);
        sb.append(interestGroupTypeOpenTag +
                  StringEscapeUtils.escapeXml(interestGroup.getInterestGroupType()) +
                  interestGroupTypeCloseTag);
        sb.append(nameOpenTag + StringEscapeUtils.escapeXml(interestGroup.getName()) + nameCloseTag);
        sb.append(descriptionOpenTag + StringEscapeUtils.escapeXml(interestGroup.getDescription()) +
                  descriptionCloseTag);
        sb.append(owningCoreOpenTag + StringEscapeUtils.escapeXml(interestGroup.getOwningCore()) +
                  owningCoreCloseTag);
        sb.append(detailedInfoOpenTag + detailedInfo + detailedInfoCloseTag);
        // log.debug("======> toXMLString(interestGroup,detailedlInfo) - sb=[" + sb.toString() + "]");
        return sb.toString();
    }
}
