package com.leidos.xchangecore.core.infrastructure.dao.hb;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.leidos.xchangecore.core.dao.hb.GenericHibernateDAO;
import com.leidos.xchangecore.core.infrastructure.dao.UserInterestGroupDAO;
import com.leidos.xchangecore.core.infrastructure.model.UserInterestGroup;

@Transactional
public class UserInterestGroupDAOHibernate
    extends GenericHibernateDAO<UserInterestGroup, String>
    implements UserInterestGroupDAO {

    private static Logger logger = LoggerFactory.getLogger(UserInterestGroupDAOHibernate.class);

    @Override
    public void addUser(final String user, final String interestGroupID) {

        logger.debug("User: " + user + " associating with IGID: " + interestGroupID);
        UserInterestGroup userInterestGroup = findByUser(user);
        if (userInterestGroup == null) {
            userInterestGroup = new UserInterestGroup();
            userInterestGroup.setUser(user);
        }

        userInterestGroup.addInterestGroup(interestGroupID);
        makePersistent(userInterestGroup);
        logger.debug("..... done .....");
    }

    private UserInterestGroup findByUser(final String user) {

        List<UserInterestGroup> userInterestGroupList = super.findAll();
        if (userInterestGroupList == null) {
            logger.debug("no UserInterestGroup");
            return null;
        }
        // logger.debug("# of UserInterestGroup: " + userInterestGroupList.size());
        for (UserInterestGroup interestGroup : userInterestGroupList) {
            // logger.debug("interestGroup.user: " + interestGroup.getUser());
            // logger.debug("interestGroup.interestGroupIDList: size: " + interestGroup.getInterestGroupIDList().size());
            if (interestGroup.getUser().equals(user)) {
                return interestGroup;
            }
        }
        return null;
    }

    @Override
    public List<String> getInterestGroupList(final String user) {

        final UserInterestGroup userInterestGroup = findByUser(user);

        return userInterestGroup != null ? userInterestGroup.getInterestGroupIDList() : null;
    }

    @Override
    public boolean isEligible(final String user, final String interestGroupID) {

        boolean eligible = false;

        final List<String> interestGroupIDList = getInterestGroupList(user);
        eligible = interestGroupIDList == null ? false : interestGroupIDList.contains(interestGroupID);
        // logger.debug("isEligible: user: " + user + " can " + (eligible ? "" : "not ") + "access IGID: " + interestGroupID);
        return eligible;
    }

    @Override
    public int removeInterestGroup(String igID) {

        int count = 0;
        List<UserInterestGroup> userInterestGroupList = super.findAll();
        for (UserInterestGroup userInterestGroup : userInterestGroupList) {
            List<String> igIDList = userInterestGroup.getInterestGroupIDList();
            if (igIDList.contains(igID)) {
                count++;
                logger.debug("remove IGID: " + igID + " for user: " + userInterestGroup.getUser());
                userInterestGroup.removeInterestGroup(igID);
            }
        }
        return count;
    }

    @Override
    public void removeUser(final String user, final String interestGoupID) {

        logger.debug("removeUser: User: " + user + " IGID: " + interestGoupID);
        final UserInterestGroup userInterestGroup = findByUser(user);

        if (userInterestGroup != null) {
            userInterestGroup.removeInterestGroup(interestGoupID);
            makePersistent(userInterestGroup);
        }
        logger.debug("..... done .....");
    }
}
