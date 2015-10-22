package com.leidos.xchangecore.core.infrastructure.util;

public interface WorkProductConstants {

    public static final String Key_OrderBy = "orderBy";
    public static final String Order_Desc = "desc";
    public static final String Order_Asc = "asc";
    public static final String State_Active = "Active";
    public static final String State_Inactive = "Inactive";
    public static final String State_Archive = "Archive";

    // WorkProduct table's column name
    public final static String C_ProductID = "productID";
    public final static String C_ProductTypeVersion = "productTypeVersion";
    public final static String C_ProductVersion = "productVersion";
    public final static String C_ProductType = "productType";
    public final static String C_State = "state";
    public final static String C_CreatedDate = "createdDate";
    public final static String C_UpdatedDate = "updatedDate";
    public final static String C_Checksum = "checksum";
    public final static String C_Mimetype = "mimeType";
    public final static String C_InterestGroupID = "interestGroup";
    public final static String C_AssociatedInterestGroupIDs = "associatedInterestGroupIDs";
}
