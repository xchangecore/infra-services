package com.leidos.xchangecore.core.infrastructure.service;

import java.util.List;

/**
 * Provides a method to search work products based on the indexing
 * of the digests, work product identification, and work product properties.
 * 
 * @ssdd
 */
public interface SearchService {

    public static final String SEARCH_SERVICE_NAME = "SearchService";

    /**
     * Find entities.
     * 
     * @param query the query
     * @param classes the classes
     * 
     * @return the list<?>
     * 
     * @ssdd
     */
    List<?> findEntities(String query, Class<?>... classes);

    /**
     * SystemIntialized Message Handler
     * 
     * @param message SystemInitialized message
     * @return void
     * @see applicationContext
     */
    public void systemInitializedHandler(String messgae);

}
