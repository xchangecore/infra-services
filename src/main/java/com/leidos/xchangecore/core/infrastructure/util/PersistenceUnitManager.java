package com.leidos.xchangecore.core.infrastructure.util;

import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;

public class PersistenceUnitManager
    extends DefaultPersistenceUnitManager {

    static Logger log = LoggerFactory.getLogger(PersistenceUnitManager.class);

    @Override
    protected void postProcessPersistenceUnitInfo(MutablePersistenceUnitInfo pui) {

        try {
            super.postProcessPersistenceUnitInfo(pui);

            pui.addJarFileUrl(pui.getPersistenceUnitRootUrl());
            log.debug("postProcessPersistenceUnitInfo: - adding persistence unit root=" +
                      pui.getPersistenceUnitRootUrl());

            final MutablePersistenceUnitInfo oldPui = getPersistenceUnitInfo(pui.getPersistenceUnitName());
            log.debug("postProcessPersistenceUnitInfo: - get pui for persistence unit name=" +
                      pui.getPersistenceUnitName());

            if (oldPui != null) {
                final List<URL> urls = oldPui.getJarFileUrls();
                for (final URL url : urls) {
                    pui.addJarFileUrl(url);
                    log.debug("postProcessPersistenceUnitInfo: adding jar with url:" + url);

                }
            }

            log.debug("===> URLs in pui:");
            final List<URL> urls = pui.getJarFileUrls();
            for (final URL url : urls) {
                log.debug("             url:" + url);

            }

        } catch (final Throwable e) {
            log.debug("postProcessPersistenceUnitInfo: exception caught:" + e.getMessage());
            e.printStackTrace();
        }

        log.debug("postProcessPersistenceUnitInfo - leaving");
    }
}