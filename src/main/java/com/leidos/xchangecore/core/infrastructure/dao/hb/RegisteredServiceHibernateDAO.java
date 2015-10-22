package com.leidos.xchangecore.core.infrastructure.dao.hb;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.leidos.xchangecore.core.dao.hb.GenericHibernateDAO;
import com.leidos.xchangecore.core.infrastructure.dao.RegisteredServiceDAO;
import com.leidos.xchangecore.core.infrastructure.model.RegisteredService;

public class RegisteredServiceHibernateDAO
    extends GenericHibernateDAO<RegisteredService, Integer>
    implements RegisteredServiceDAO {

    public Set<RegisteredService> findByUrn(String urn) {

        Criterion criterion = Restrictions.eq("urn", urn);
        List<RegisteredService> registeredUICDSServiceList = findByCriteria(criterion);

        Set<RegisteredService> registeredUICDSServices = new HashSet<RegisteredService>(registeredUICDSServiceList);

        /*
        Set<RegisteredService> registeredUICDSServices = new HashSet<RegisteredService>();
        for (RegisteredService svc : registeredUICDSServiceList) {
        	registeredUICDSServices.add(svc);
        }
        */

        return registeredUICDSServices;
    }

    public Set<RegisteredService> findByServiceName(String serviceName) {

        Criterion criterion = Restrictions.eq("serviceName", serviceName);
        List<RegisteredService> registeredUICDSServiceList = findByCriteria(criterion);

        Set<RegisteredService> registeredUICDSServices = new HashSet<RegisteredService>(registeredUICDSServiceList);
        /*
        Set<RegisteredService> registeredUICDSServices = new HashSet<RegisteredService>();
        for (RegisteredService svc : registeredUICDSServiceList) {
        	registeredUICDSServices.add(svc);
        }
        */

        return registeredUICDSServices;
    }

    public Set<RegisteredService> findByServiceType(RegisteredService.SERVICE_TYPE serviceType) {

        Criterion criterion = Restrictions.eq("serviceType", serviceType);
        List<RegisteredService> registeredUICDSServiceList = findByCriteria(criterion);
        Set<RegisteredService> registeredUICDSServices = new HashSet<RegisteredService>(registeredUICDSServiceList);
        /*
        Set<RegisteredService> registeredUICDSServices = new HashSet<RegisteredService>();
        for (RegisteredService svc : registeredUICDSServiceList) {
        	registeredUICDSServices.add(svc);
        }
        */
        return registeredUICDSServices;
    }

    public Set<RegisteredService> findByCoreName(String coreName) {

        Criterion criterion = Restrictions.eq("coreName", coreName);
        List<RegisteredService> registeredUICDSServiceList = findByCriteria(criterion);
        Set<RegisteredService> registeredUICDSServices = new HashSet<RegisteredService>(registeredUICDSServiceList);
        /*
        Set<RegisteredService> registeredUICDSServices = new HashSet<RegisteredService>();
        for (RegisteredService svc : registeredUICDSServiceList) {
        	registeredUICDSServices.add(svc);
        }
        */
        return registeredUICDSServices;
    }

    public Set<RegisteredService> findByServiceTypeAndCoreName(RegisteredService.SERVICE_TYPE serviceType,
                                                               String coreName) {

        Criterion criterion1 = Restrictions.eq("serviceType", serviceType);
        Criterion criterion2 = Restrictions.eq("coreName", coreName);
        List<RegisteredService> registeredUICDSServiceList = findByCriteria(criterion1, criterion2);

        Set<RegisteredService> registeredUICDSServices = new HashSet<RegisteredService>(registeredUICDSServiceList);
        /*
        Set<RegisteredService> registeredUICDSServices = new HashSet<RegisteredService>();
        for (RegisteredService svc : registeredUICDSServiceList) {
        	registeredUICDSServices.add(svc);
        }
        */
        return registeredUICDSServices;
    }

    public Set<RegisteredService> findByUrnAndCoreName(String urn, String coreName) {

        Criterion criterion1 = Restrictions.eq("urn", urn);
        Criterion criterion2 = Restrictions.eq("coreName", coreName);
        List<RegisteredService> registeredUICDSServiceList = findByCriteria(criterion1, criterion2);

        Set<RegisteredService> registeredUICDSServices = new HashSet<RegisteredService>(registeredUICDSServiceList);
        /*
        Set<RegisteredService> registeredUICDSServices = new HashSet<RegisteredService>();
        for (RegisteredService svc : registeredUICDSServiceList) {
        	registeredUICDSServices.add(svc);
        }
        */

        return registeredUICDSServices;
    }

    public Set<RegisteredService> findByServiceNameAndCoreName(String serviceName, String coreName) {

        Criterion criterion1 = Restrictions.eq("serviceName", serviceName);
        Criterion criterion2 = Restrictions.eq("coreName", coreName);
        List<RegisteredService> registeredUICDSServiceList = findByCriteria(criterion1, criterion2);
        Set<RegisteredService> registeredUICDSServices = new HashSet<RegisteredService>(registeredUICDSServiceList);

        /*
        Set<RegisteredService> registeredUICDSServices = new HashSet<RegisteredService>();
        for (RegisteredService svc : registeredUICDSServiceList) {
        	registeredUICDSServices.add(svc);
        }
        */
        return registeredUICDSServices;

    }

    public Set<RegisteredService> findAllRegisteredServices() {

        List<RegisteredService> registeredUICDSServiceList = findAll();
        Set<RegisteredService> registeredUICDSServices = new HashSet<RegisteredService>(registeredUICDSServiceList);
        /*
        Set<RegisteredService> registeredUICDSServices = new HashSet<RegisteredService>();
        for (RegisteredService svc : registeredUICDSServiceList) {
        	registeredUICDSServices.add(svc);
        }
        */

        return registeredUICDSServices;
    }

}
