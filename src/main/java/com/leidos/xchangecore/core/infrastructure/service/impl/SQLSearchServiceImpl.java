package com.leidos.xchangecore.core.infrastructure.service.impl;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.leidos.xchangecore.core.infrastructure.dao.WorkProductDAO;
import com.leidos.xchangecore.core.infrastructure.service.ISearchService;

public class SQLSearchServiceImpl
    implements ISearchService {

    private static Logger logger = LoggerFactory.getLogger(SQLSearchServiceImpl.class);
    private WorkProductDAO workProductDAO;

    public WorkProductDAO getWorkProductDAO() {

        return workProductDAO;
    }

    @Override
    public Document searchAsDocument(HashMap<String, String[]> params) {

        return workProductDAO.findDocsBySearchCriteria(params);
    }

    public void setWorkProductDAO(WorkProductDAO workProductDAO) {

        this.workProductDAO = workProductDAO;
    }
}
