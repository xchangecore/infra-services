package com.leidos.xchangecore.core.dao.hb;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.hibernate.Session;

public class HibernateUtil {

    private static ThreadLocal<EntityManager> currentEntityManager = new ThreadLocal<EntityManager>();

    private static final String DEFAULT_EM = "repo";

    private static EntityManagerFactory factory = null;

    public static void destroy() {

        if (factory != null) {
            if (factory.isOpen()) {
                factory.close();
            }
        }
    }

    public static EntityManager getCurrentEntityManager() {

        return currentEntityManager.get();
    }

    public static Session getCurrentSession() {

        return (Session) getEntityManager().getDelegate();
    }

    public static EntityManager getEntityManager() {

        return getEntityManager(DEFAULT_EM);
    }

    public static EntityManager getEntityManager(String entityManagerName) {

        if (factory == null) {
            initFactory(entityManagerName);
        }

        return factory.createEntityManager();
    }

    public static void initFactory() {

        initFactory(DEFAULT_EM);
    }

    public static void initFactory(String entityManagerName) {

        if (factory == null) {
            factory = Persistence.createEntityManagerFactory(entityManagerName);
        }
    }

    public static void setCurrentEntityManager(EntityManager em) {

        currentEntityManager.set(em);
    }

}