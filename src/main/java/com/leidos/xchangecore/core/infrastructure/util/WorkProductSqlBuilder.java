package com.leidos.xchangecore.core.infrastructure.util;

import com.leidos.xchangecore.core.infrastructure.dao.UserInterestGroupDAO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkProductSqlBuilder {

    Logger log = LoggerFactory.getLogger(getClass());

    private Map<String, String> tableParams = new HashMap<String, String>();

    private Map<String, String> tableOps = new HashMap<String, String>();

    // UserInterestGroupDAO
    private static UserInterestGroupDAO userInterestGroupDAO;

    private int startIndex;

    private int count;

    private String TABLE_NAME = "guest.workproducts";

    public WorkProductSqlBuilder() {

        tableParams.put("productid", "ProductID");
        tableParams.put("producttypeversion", "ProductTypeVersion");
        tableParams.put("productversion", "ProductVersion");
        tableParams.put("producttype", "WPType");
        tableParams.put("productstate", "State");
        tableParams.put("createdbegin", "Created");
        tableParams.put("createdend", "Created");
        tableParams.put("createdby", "CreatedBy");
        tableParams.put("updatedbegin", "LastUpdated");
        tableParams.put("updatedend", "LastUpdated");
        tableParams.put("updatedby", "LastUpdatedBy");
        tableParams.put("mimetype", "MimeType");
        tableParams.put("interestgroup", "AssociatedGroups");
        tableOps.put("productid", "=");
        tableOps.put("producttypeversion", "like");
        tableOps.put("productversion", "=");
        tableOps.put("producttype", "like");
        tableOps.put("productstate", "like");
        tableOps.put("createdbegin", ">=");
        tableOps.put("createdend", "<=");
        tableOps.put("createdby", "like");
        tableOps.put("updatedbegin", ">=");
        tableOps.put("updatedend", "<=");
        tableOps.put("updatedby", "like");
        tableOps.put("mimetype", "like");
        tableOps.put("interestgroup", "like");
    }

    private String buildParamQuery(Map<String, String[]> queryParams) {

        if (queryParams.isEmpty())
            return "";
        StringBuffer query = new StringBuffer();
        query.append(" AND ");
        int curParam = 0;
        for (String key : queryParams.keySet()) {
            query.append("(");
            query.append(buildSqlParamStatement(key.toLowerCase(), queryParams.get(key)));
            query.append(") ");
            if (queryParams.size() > 1 && curParam < queryParams.size() - 1) {
                query.append(" AND ");
            }
            curParam++;
        }
        return query.toString();
    }

    public String buildQuery(Map<String, String[]> params) {

        if (params.containsKey("startIndex")) {
            String startIndex = params.get("startIndex")[0];
            setStartIndex(Integer.parseInt(startIndex));
        } else {
            setStartIndex(1);
        }

        boolean hasCount = false;
        if (params.containsKey("count")) {
            String count = params.get("count")[0];
            setCount(Integer.parseInt(count));
            hasCount = true;
        }
        /*
         * DECLARE @startIndex int SET @startIndex = 26 DECLARE @count int SET
         * @count = 10 SELECT * FROM (SELECT TOP (@startIndex+@count)
         * ROW_NUMBER() OVER (ORDER BY ID ASC) AS Row, ID,RawXML FROM
         * guest.workproducts) AS WPwithRowNos WHERE Row Between @startIndex AND
         * (@startIndex+@count-1)
         */
        Map<String, String[]> queryParams = refineParams(params);
        StringBuilder query = new StringBuilder();
        // query.append(buildSqlBaseStatement());

        // new query base

        query.append("SELECT * FROM ( ");
        query.append("SELECT guest.workproducts.*,ROW_NUMBER() OVER (ORDER BY id asc) as [RowNo] ");
        query.append("FROM  USER_INTEREST_GROUP_interestGroupIDList  ");
        query.append("	INNER JOIN USER_INTEREST_GROUP ON USER_INTEREST_GROUP_interestGroupIDList.USER_INTEREST_GROUP_UIG_ID = USER_INTEREST_GROUP.UIG_ID  ");
        query.append("	INNER JOIN guest.workproducts ON USER_INTEREST_GROUP_interestGroupIDList.INTEREST_GROUP_ID_LIST = guest.workproducts.AssociatedGroups  ");
        query.append("  WHERE ( ");
        query.append("     USER_INTEREST_GROUP.USER_JID = '" + params.get("req.remoteUser")[0] +
                     "' ");
        String paramQuery = buildParamQuery(queryParams);
        query.append(paramQuery);
        query.append(") ) AS products ");
        query.append("WHERE RowNo >= ").append(getStartIndex());

        if (hasCount) {
            int count = getStartIndex() + getCount() - 1;
            query.append(" AND RowNo<=").append(count);
        }

        log.debug("Query: " + query.toString());

        return query.toString();
    }

    private Object buildSqlParamStatement(String key, String[] values) {

        StringBuilder paramQuery = new StringBuilder();
        String column = tableParams.get(key);
        String op = tableOps.get(key);
        int current = 0;
        int valuesLength = values.length;
        for (String value : values) {
            paramQuery.append(column);
            paramQuery.append(" " + op + " ");
            if (!op.equals("="))
                value = "'" + value + "'";
            if (op.equals("=") && key.equals("productid"))
                value = "'" + value + "'";
            paramQuery.append(value);
            if (valuesLength > 1 && current < valuesLength - 1)
                paramQuery.append(" OR ");
            current++;
        }
        return paramQuery;
    }

    public int getCount() {

        return count;
    }

    public int getStartIndex() {

        return startIndex;
    }

    private Map<String, String[]> refineParams(Map<String, String[]> params) {

        Set<String> tableKeys = tableParams.keySet();
        Map<String, String[]> result = new HashMap<String, String[]>();
        for (String key : params.keySet()) {
            if (tableKeys.contains(key.toLowerCase())) {
                result.put(key, params.get(key));
            }
        }
        return result;
    }

    public void setCount(int count) {

        this.count = count;
    }

    public void setStartIndex(int startIndex) {

        this.startIndex = startIndex;
    }

    public UserInterestGroupDAO getUserInterestGroupDAO() {

        return userInterestGroupDAO;
    }

    public void setUserInterestGroupDAO(UserInterestGroupDAO userInterestGroupDAO) {

        this.userInterestGroupDAO = userInterestGroupDAO;
    }
}
