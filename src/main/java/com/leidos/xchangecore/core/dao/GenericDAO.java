package com.leidos.xchangecore.core.dao;

import java.io.Serializable;
import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;

// @Transactional
public interface GenericDAO<T, ID extends Serializable> {

    // public List<T> find(Query query);

    /**
     * Check to see if the identified entity exists.
     * 
     * @return true if the entity exists, false otherwise.
     */
    boolean exists(ID id);

    /**
     * Find all instances.
     */
    List<T> findAll();

    List<T> findByCriteriaAndOrder(int startIndex,
                                   List<Order> orderList,
                                   List<Criterion> criterionList);

    /**
     * Find an entity based on example provided.
     * 
     * @param exampleInstance
     * @param excludeProperty The optional list of properties to exclude from comparison.
     * @return
     */
    List<T> findByExample(T exampleInstance, String... excludeProperty);

    /**
     * Find an entity by its identifier.
     * 
     * @param id
     * @return
     */
    T findById(ID id);

    /**
     * Find an entity by its identifier and acquire a UPGRADE lock on the instance if
     * <code>lock</code> is <code>true</code>
     * 
     * @param id
     * @param lock
     * @return
     */
    T findById(ID id, boolean lock);

    public boolean isSessionInitialized();

    /**
     * Make the entity persistent. If the entity is already persistent, then ensure that it is
     * saved.
     */
    T makePersistent(T entity);

    /**
     * Make the entity transient. This will cause the entity to be removed from the database.
     * 
     * @param entity
     */
    void makeTransient(T entity);
}
