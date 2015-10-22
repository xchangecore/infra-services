package com.leidos.xchangecore.core.infrastructure.service;

import org.springframework.transaction.annotation.Transactional;
import org.uicds.interestGroupService.InterestGroupListType;
import org.uicds.interestGroupService.ShareInterestGroupRequestDocument.ShareInterestGroupRequest;
import org.uicds.workProductService.WorkProductListDocument.WorkProductList;

import com.leidos.xchangecore.core.infrastructure.model.WorkProduct;
import com.leidos.xchangecore.core.infrastructure.service.impl.ProductPublicationStatus;
import com.saic.precis.x2009.x06.base.IdentificationListType;
import com.saic.precis.x2009.x06.structures.InterestGroupType;

/**
 * Manages interest group in the local core.
 * 
 * @author Daphne Hurrell
 * @since 1.0
 * @ssdd
 * 
 */

@Transactional
public interface InterestGroupService {

    public static final String INTEREST_GROUP_SERVICE_NAME = "InterestGroupService";
    public static final String InterestGroupTypeUrn = "urn:uicds:interestgroup:type";
    public static final String InterestGroupIdUrn = "urn:uicds:interestgroup:id";

    public String createInterestGroup(InterestGroupType interestGroup, IdentificationListType wpList);

    public ProductPublicationStatus archiveInterestGroup(String interestGroupId);

    public ProductPublicationStatus closeInterestGroup(String interestGroupId);

    public InterestGroupListType getInterestGroupList();

    public boolean shareInterestGroup(ShareInterestGroupRequest shareRequest);

    public boolean unShareInterestGroup();

    public boolean updateInterestGroup(InterestGroupType interestGroup, String interestGroupId);

    public WorkProductList getListOfWorkProduct(String interestGroupId);

    public WorkProduct getInterestGroup(String specializedWPId);

    public void systemInitializedHandler(String message);

}