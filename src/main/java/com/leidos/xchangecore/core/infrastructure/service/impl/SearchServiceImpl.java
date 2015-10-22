package com.leidos.xchangecore.core.infrastructure.service.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uicds.directoryServiceData.WorkProductTypeListType;

import com.leidos.xchangecore.core.infrastructure.service.DirectoryService;
import com.leidos.xchangecore.core.infrastructure.service.SearchService;

/**
 * The SearchService Interface implementation.
 *
 * @ssdd
 */
public class SearchServiceImpl
implements SearchService {

    /** The log. */
    private final Logger logger = LoggerFactory.getLogger(SearchServiceImpl.class);

    /** The em. */
    @PersistenceContext
    EntityManager em;

    private DirectoryService directoryService;

    /**
     * Find entities.
     *
     * @param queryString the query string
     * @param classes the classes
     *
     * @return the list
     * @ssdd
     */
    @Override
    public List<?> findEntities(String queryString, Class<?>... classes) {

        List<?> results = null;

        try {
            final FullTextEntityManager textEm = Search.getFullTextEntityManager(em);
            final QueryParser parser = new QueryParser("name", new StopAnalyzer());
            final org.apache.lucene.search.Query luceneQuery = parser.parse(queryString);
            final FullTextQuery fullTextQuery = textEm.createFullTextQuery(luceneQuery, classes);
            results = fullTextQuery.getResultList();

            int count = 0;
            for (final Object o : results) {
                logger.debug("Result: " + o);
                ++count;
            }
            logger.debug("matches: " + count);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        return results;
    }

    /**
     * Sets the directory service.
     *
     * @param directoryService the new directory service
     */
    public void setDirectoryService(DirectoryService directoryService) {

        this.directoryService = directoryService;
    }

    @Override
    public void systemInitializedHandler(String messgae) {

        logger.debug("systemInitializedHandler: ... start ...");
        final WorkProductTypeListType typeList = WorkProductTypeListType.Factory.newInstance();
        directoryService.registerUICDSService("http://uicds.dctd.saic.com/searchService",
            SEARCH_SERVICE_NAME, typeList, typeList);
        logger.debug("systemInitializedHandler: ... done ...");
    }
}
