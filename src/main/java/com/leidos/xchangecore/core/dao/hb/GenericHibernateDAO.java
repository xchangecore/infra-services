package com.leidos.xchangecore.core.dao.hb;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Order;
import org.hibernate.transform.DistinctResultTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.leidos.xchangecore.core.dao.GenericDAO;

@Transactional(readOnly = true)
public abstract class GenericHibernateDAO<T, ID extends Serializable>
implements GenericDAO<T, ID> {

    Logger logger = LoggerFactory.getLogger(GenericHibernateDAO.class);

    private EntityManager em;

    private final Class<T> persistentClass;

    @SuppressWarnings("unchecked")
    public GenericHibernateDAO() {

        this.persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        setEntityManager(HibernateUtil.getCurrentEntityManager());
    }

    protected void clear() {

        getSession().clear();
    }

    @Override
    public boolean exists(ID id) {

        return findById(id) != null;
    }

    @Override
    public List<T> findAll() {

        return findByCriteria();
    }

    @SuppressWarnings("unchecked")
    protected List<T> findAllByOrder(Order... orders) {

        final Criteria crit = getSession().createCriteria(getPersistentClass());
        for (final Order order : orders) {
            crit.addOrder(order);
        }

        final List<T> list = crit.list();
        if ((list != null) && (list.size() > 1)) {
            return DistinctResultTransformer.INSTANCE.transformList(list);
        } else {
            return list;
        }
    }

    /**
     * Use this inside subclasses as a convenience method.
     */
    @SuppressWarnings("unchecked")
    protected List<T> findByCriteria(Criterion... criterion) {

        List<T> list = null;

        final int retries = 10;
        while (retries > 0) {
            try {
                final Criteria crit = getSession().createCriteria(getPersistentClass());
                for (final Criterion c : criterion) {
                    // logger.debug("findByCriteria: " + c);
                    crit.add(c);
                }
                list = crit.list();
                break;
            } catch (final Exception e) {
                e.printStackTrace();
                logger.warn("findByCriteria: " + e.getMessage() +
                    ", sleep half second then try again");
                try {
                    Thread.sleep(500);
                } catch (final Exception se) {
                    logger.error("Cannot sleep one second: " + se.getMessage());
                }
            }
        }
        // logger.debug("findByCriteria: found " + (list != null ? list.size() : 0) + " entries");
        if ((list != null) && (list.size() > 1)) {
            return DistinctResultTransformer.INSTANCE.transformList(list);
        } else {
            return list;
        }
    }

    @Override
    public List<T> findByCriteriaAndOrder(int startIndex,
        List<Order> orderList,
        List<Criterion> criterionList) {

        List<T> list = null;
        final int retries = 10;
        while (retries > 0) {
            try {
                final Criteria criteria = getSession().createCriteria(getPersistentClass());
                for (final Criterion c : criterionList) {
                    // logger.debug("findByCriteriaAndOrder: criterion: " + c);
                    criteria.add(c);
                }
                for (final Order o : orderList) {
                    // logger.debug("findByCriteriaAndOrder: order: " + o);
                    criteria.addOrder(o);
                }
                criteria.setFirstResult(startIndex);
                list = criteria.list();
                break;
            } catch (final Exception e) {
                e.printStackTrace();
                logger.warn("findByCriteriaAndOrder: " + e.getMessage() +
                    ", sleep half second then try again");
                try {
                    Thread.sleep(500);
                } catch (final Exception se) {
                    logger.error("Cannot sleep one second: " + se.getMessage());
                }
            }
        }

        // logger.debug("findByCriteriaAndOrder: found " + (list != null ? list.size() : 0) + " entries");
        return list;
    }

    @SuppressWarnings("unchecked")
    protected List<T> findByCriteriaInOrder(Order o, Criterion... criterions) {

        final Criteria crit = getSession().createCriteria(getPersistentClass());
        for (final Criterion c : criterions) {
            crit.add(c);
        }
        crit.addOrder(o);
        final List<T> list = crit.list();
        if ((list != null) && (list.size() > 1)) {
            return DistinctResultTransformer.INSTANCE.transformList(list);
        } else {
            return list;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> findByExample(T exampleInstance, String... excludeProperty)
        throws HibernateException {

        final Criteria crit = getSession().createCriteria(getPersistentClass());
        final Example example = Example.create(exampleInstance);
        for (final String exclude : excludeProperty) {
            example.excludeProperty(exclude);
        }
        crit.add(example);
        final List<T> list = crit.list();
        if ((list != null) && (list.size() > 1)) {
            return DistinctResultTransformer.INSTANCE.transformList(list);
        } else {
            return list;
        }
    }

    @Override
    public T findById(ID id) {

        final T entity = em.find(getPersistentClass(), id);
        return entity;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T findById(ID id, boolean lock) {

        T entity;
        if (lock) {
            entity = (T) getSession().load(getPersistentClass(), id, LockMode.UPGRADE);
        } else {
            entity = (T) getSession().load(getPersistentClass(), id);
        }

        return entity;
    }

    public void flush() {

        getSession().flush();
    }

    protected EntityManager getEntityManager() {

        if (em == null) {
            throw new IllegalStateException("EntityManager has not be set on DAO before usage");
        }
        return em;
    }

    protected Class<T> getPersistentClass() {

        return persistentClass;
    }

    protected Session getSession() {

        if (em == null) {
            throw new IllegalStateException("Session has not been set on DAO before usage");
        }
        final Session result = (Session) em.getDelegate();
        return result;
    }

    @Override
    public boolean isSessionInitialized() {

        if (logger.isDebugEnabled()) {
            logger.debug("isSessionInitialized=" + getSession().isOpen());
        }
        return getSession().isOpen();
    }

    @Override
    public T makePersistent(T entity) {

        try {
            getSession().saveOrUpdate(entity);
            // logger.debug("makePersistent: ... done ...");
        } catch (final Exception e) {
            logger.error("makePersistent: " + e.getMessage());
        }
        return entity;
    }

    @Override
    public void makeTransient(T entity) {

        try {
            getSession().delete(entity);
            // logger.debug("makeTransient: ... done ...");
        } catch (final Throwable e) {
            logger.warn("makeTransient: " + e.getMessage());
        }
    }

    @PersistenceContext
    public void setEntityManager(EntityManager em) {

        this.em = em;
    }
}
