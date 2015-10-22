package com.leidos.xchangecore.core.infrastructure.util;

public interface InfrastructureNamespaces {

    public static final String NS_NIEM_CORE = "http://niem.gov/niem/niem-core/2.0";
    public static final String NS_GML = "http://www.opengis.net/gml/3.2";
    public static final String NS_CAP = "urn:oasis:names:tc:emergency:cap:1.1";
    public static final String NS_PRECIS_STRUCTURES = "http://www.saic.com/precis/2009/06/structures";
    public static final String NS_PRECIS_BASE = "http://www.saic.com/precis/2009/06/base";
    public static final String NS_UCORE = "http://ucore.gov/ucore/2.0";
    public static final String NS_UCORE_CODESPACE = "http://ucore.gov/ucore/2.0/codespace/";
    public static final String NS_ULEX_STRUCTURE = "ulex:message:structure:1.0";
    public static final String NS_UICDS = "http://uicds.org";
    public static final String UICDS_WEB_SITE = "http://uicds.us";

    public static final String URI_UICDS = "ucids.org";
    public static final String VERS_UICDS = "0.8";

    public static final String UICDS_EVENT_CODESPACE = "http://uicds.gov/1.0/codespace/event";
    public static final String UICDS_EVENT_STATUS_CODESPACE = "http://uicds.gov/1.0/codespace/event/status/experimental";
    public static final String UICDS_WORK_PRODUCT_STATUS_CODE_SPACE = NS_UICDS +
                                                                      "/1.0/codespace/workproduct/status";

    public static final String S_DATA_ITEM_STATUS = "DataItemStatus";
    public static final String S_DIGEST = "Digest";
    public static final String S_PACKAGE_IDENTIFICATION = "WorkProductIdentification";
    public static final String S_PACKAGE_PROPERTIES = "WorkProductProperties";
    public static final String S_DATA_OWNER_CONTACT = "DataOwnerContact";
    public static final String S_DATA_OWNER_IDENTIFIER = "DataOwnerIdentifier";
    public static final String S_DATA_OWNER_METADATA_DOMAIN_ATTRIBUTE = "DataOwnerMetadataDomainAttribute";
    public static final String S_DISSEMINATION_CRITERIA = "DisseminationCriteria";

    public static final String GML_SRS_NAME = "urn:ogc:def:crs:EPSG:6.6:4326";
}
