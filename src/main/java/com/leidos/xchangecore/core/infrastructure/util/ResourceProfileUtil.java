package com.leidos.xchangecore.core.infrastructure.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.xmlbeans.XmlCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uicds.resourceProfileService.Interest;
import org.uicds.resourceProfileService.ResourceProfile;
import org.uicds.resourceProfileService.ResourceProfile.Interests;

import com.leidos.xchangecore.core.infrastructure.model.CodeSpaceValueType;
import com.leidos.xchangecore.core.infrastructure.model.InterestElement;
import com.leidos.xchangecore.core.infrastructure.model.InterestNamespaceType;
import com.leidos.xchangecore.core.infrastructure.model.NamespaceMap;
import com.leidos.xchangecore.core.infrastructure.model.ResourceProfileModel;
import com.saic.precis.x2009.x06.base.CodespaceValueType;
import com.saic.precis.x2009.x06.base.NamespaceMapItemType;

/**
 * 
 * @author bonnerad
 * 
 */
public class ResourceProfileUtil {

    static Logger log = LoggerFactory.getLogger(ResourceProfileUtil.class);

    static final String defaultProfileIDLabel = "ID";

    public static ResourceProfileModel copyProperties(ResourceProfile profile) {

        ResourceProfileModel profileModel = new ResourceProfileModel();

        profileModel.setIdentifier(profile.getID().getStringValue());
        if (profile.getID().getLabel() != null) {
            profileModel.setLabel(profile.getID().getLabel());
        } else {
            profileModel.setLabel(defaultProfileIDLabel);
        }
        profileModel.setDescription(profile.getDescription());

        // resource typing
        if (profile.getResourceTyping() != null &&
            profile.getResourceTyping().sizeOfTypeArray() > 0) {
            for (CodespaceValueType type : profile.getResourceTyping().getTypeArray()) {
                profileModel.addCVT(type.getCodespace(), type.getLabel(), type.getStringValue());
            }
        }
        // interests
        if (profile.getInterests() != null && profile.getInterests().sizeOfInterestArray() > 0) {
            for (Interest interest : profile.getInterests().getInterestArray()) {
                profileModel.addInterest(copyProperties(interest));
            }
        }
        return profileModel;
    }

    public static ResourceProfileModel copyProperties(ResourceProfile profile, Integer id) {

        ResourceProfileModel profileModel = new ResourceProfileModel();
        profileModel.setId(id);

        profileModel.setIdentifier(profile.getID().getStringValue());
        if (profile.getID().getLabel() != null) {
            profileModel.setLabel(profile.getID().getLabel());
        } else {
            profileModel.setLabel(defaultProfileIDLabel);
        }
        profileModel.setDescription(profile.getDescription());

        // resource typing
        if (profile.getResourceTyping() != null &&
            profile.getResourceTyping().sizeOfTypeArray() > 0) {
            for (CodespaceValueType type : profile.getResourceTyping().getTypeArray()) {
                profileModel.addCVT(type.getCodespace(), type.getLabel(), type.getStringValue());
            }
        }
        // interests
        if (profile.getInterests() != null && profile.getInterests().sizeOfInterestArray() > 0) {
            for (Interest interest : profile.getInterests().getInterestArray()) {
                profileModel.addInterest(copyProperties(interest));
            }
        }
        return profileModel;
    }

    /**
     * Converts the persisted profile to a xml type
     * 
     * @param ResourceProfileModel profile
     * @return ResourceProfile
     */
    public static ResourceProfile copyProperties(ResourceProfileModel profileModel) {

        //        log.debug("getting profile from: " + profileModel);
        if (profileModel == null)
            return null;

        ResourceProfile profileType = ResourceProfile.Factory.newInstance();

        profileType.addNewID().setLabel(profileModel.getLabel());
        profileType.getID().setStringValue(profileModel.getIdentifier());
        profileType.setDescription(profileModel.getDescription());

        // resource typing
        //FLi redo the loop as the below old code does
        if (profileModel.getCvts() != null && profileModel.getCvts().size() > 0) {
            profileType.addNewResourceTyping();

            Set<CodeSpaceValueType> csets = profileModel.getCvts();

            Iterator<CodeSpaceValueType> it = csets.iterator();
            while (it.hasNext()) {
                CodeSpaceValueType obj = (CodeSpaceValueType) it.next();
                CodespaceValueType type = profileType.getResourceTyping().addNewType();
                type.setStringValue(obj.getValue());
                type.setCodespace(obj.getCodeSpace());
                type.setLabel(obj.getLabel());
            }

            /* old code
            for (String codespaceLabelStr : profileModel.getResourceTyping().keySet()) {
                CodespaceValueType type = profileType.getResourceTyping().addNewType();
                
                type.setStringValue(profileModel.getResourceTyping().get(codespaceLabelStr));

                String[] codespaceLabel = codespaceLabelStr.split(",");
                if (codespaceLabel.length == 1) {
                    type.setCodespace(codespaceLabel[0]);
                } else if (codespaceLabel.length == 2) {
                    type.setCodespace(codespaceLabel[0]);
                    type.setLabel(codespaceLabel[1]);
                }
            }
            */

        }

        // if interests are present, add them to profile
        if (profileModel.getInterests() != null && profileModel.getInterests().size() > 0) {
            profileType.addNewInterests();
            for (InterestElement interest : profileModel.getInterests()) {
                Interest newInterest = profileType.getInterests().addNewInterest();
                newInterest.setTopicExpression(interest.getTopicExpression());
                // newInterest.setTopicExpression(QName.valueOf(interest.getTopicExpression()));
                // set messageContent
                if (interest.getMessageContent() != null &&
                    interest.getMessageContent().length() > 0) {
                    newInterest.addNewMessageContent();
                    XmlCursor cursor = newInterest.getMessageContent().newCursor();
                    cursor.toNextToken();
                    cursor.insertChars(interest.getMessageContent());
                    cursor.dispose();
                    // newInterest.getMessageContent().setDialect(interest.getMessageContent());
                }
                // set namespace map
                if (interest.getNamespaces() != null && interest.getNamespaces().size() > 0) {
                    newInterest.addNewNamespaceMap();
                    for (InterestNamespaceType ns : interest.getNamespaces()) {
                        NamespaceMapItemType mapItem = newInterest.getNamespaceMap().addNewItem();
                        mapItem.setPrefix(ns.getPrefix());
                        mapItem.setURI(ns.getUri());
                    }
                }
            }
            // profileType.setSubscriptions(subscriptions);
        }

        return profileType;
    }

    public static InterestElement copyProperties(Interest interest) {

        if (interest == null)
            return null;

        InterestElement interestElement = new InterestElement();
        interestElement.setTopicExpression(interest.getTopicExpression());
        if (interest.getMessageContent() != null) {
            XmlCursor cursor = interest.getMessageContent().newCursor();
            cursor.toNextToken();
            String mc = cursor.getTextValue();
            cursor.dispose();
            interestElement.setMessageContent(mc);
        }
        if (interest.getNamespaceMap() != null) {
            Set<InterestNamespaceType> namespaces = new HashSet<InterestNamespaceType>();
            for (NamespaceMapItemType ns : interest.getNamespaceMap().getItemArray()) {
                InterestNamespaceType interestNS = new InterestNamespaceType();
                interestNS.setPrefix(ns.getPrefix());
                interestNS.setUri(ns.getURI());
                namespaces.add(interestNS);
            }
            interestElement.setNamespaces(namespaces);
        }
        return interestElement;
    }

    /**
     * Merges Interests xml into a Set<InterestElement> to be persisted to the database
     * 
     * @param interests
     * @param list
     */
    // TODO:: For each merged type, hashcode and equals must be defined
    public static void merge(Interests interests, Set<InterestElement> list) {

        // if interests are present, add them to profile
        if (interests != null && interests.sizeOfInterestArray() > 0) {
            // Subscription List

            for (Interest interest : interests.getInterestArray()) {
                // Read the data that is in the interest xml element
                InterestElement interestElement = new InterestElement();
                if (interest.getMessageContent() != null) {
                    XmlCursor cursor = interest.getMessageContent().newCursor();
                    cursor.toNextToken();
                    String mc = cursor.getTextValue();
                    cursor.dispose();
                    interestElement.setMessageContent(mc);
                }

                interestElement.setTopicExpression(interest.getTopicExpression());

                if (interest.getNamespaceMap() != null) {
                    Set<InterestNamespaceType> namespaces = new HashSet<InterestNamespaceType>();
                    for (NamespaceMapItemType ns : interest.getNamespaceMap().getItemArray()) {
                        InterestNamespaceType interestNS = new InterestNamespaceType();
                        interestNS.setPrefix(ns.getPrefix());
                        interestNS.setUri(ns.getURI());
                        namespaces.add(interestNS);
                    }
                    interestElement.setNamespaces(namespaces);
                }
                list.add(interestElement);
            }

        }

    }

    /**
     * 
     * @param name
     * @return
     */
    public static boolean validSearchKey(String name) {

        // FIXME::
        return name.equalsIgnoreCase(name);
    }
}
