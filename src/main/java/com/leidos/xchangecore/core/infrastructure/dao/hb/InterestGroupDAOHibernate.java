package com.leidos.xchangecore.core.infrastructure.dao.hb;

import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.leidos.xchangecore.core.dao.hb.GenericHibernateDAO;
import com.leidos.xchangecore.core.infrastructure.dao.InterestGroupDAO;
import com.leidos.xchangecore.core.infrastructure.model.InterestGroup;

@Transactional
public class InterestGroupDAOHibernate
    extends GenericHibernateDAO<InterestGroup, Integer>
    implements InterestGroupDAO {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void delete(String interestGroupID, boolean isDelete) {

        InterestGroup interestGroup = findByInterestGroup(interestGroupID);
        if (interestGroup == null) {
            return;
        }

        if (isDelete == true) {
            makeTransient(interestGroup);
        } else {
            interestGroup.setActive(false);
            makePersistent(interestGroup);
        }
    }

    @Override
    public InterestGroup findByInterestGroup(String interestGroupID) {

        logger.debug("findByInterestGroup: IGID: " + interestGroupID);
        Criterion criterion = Restrictions.eq("interestGroupID", interestGroupID);
        List<InterestGroup> interestGroups = findByCriteria(criterion);
        logger.debug("findByInterestGroup: found " + interestGroups.size() + " entries");

        return interestGroups != null && interestGroups.size() != 0 ? interestGroups.get(0) : null;
    }

    @Override
    public List<InterestGroup> findByOwningCore(String owningCore) {

        Criterion criterion = Restrictions.eq("owningCore", owningCore);
        List<InterestGroup> interestGroups = findByCriteria(criterion);

        return interestGroups;
    }

    @Override
    public boolean ownedByCore(String interestGroupID, String corename) {

        InterestGroup interestGroup = findByInterestGroup(interestGroupID);
        return interestGroup == null ? false : interestGroup.getOwningCore().equalsIgnoreCase(corename);
    }
}
