package com.leidos.xchangecore.core.infrastructure.dao.hb;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.leidos.xchangecore.core.dao.hb.GenericHibernateDAO;
import com.leidos.xchangecore.core.infrastructure.dao.QueuedMessageDAO;
import com.leidos.xchangecore.core.infrastructure.model.QueuedMessage;

public class QueuedMessageDAOHibernate
    extends GenericHibernateDAO<QueuedMessage, Integer>
    implements QueuedMessageDAO {

    private final Logger logger = LoggerFactory.getLogger(QueuedMessageDAOHibernate.class);

    private QueuedMessage getByCorename(String corename) {

        Criterion criterion = Restrictions.eq("corename", corename);
        List<QueuedMessage> recoverMessages = findByCriteria(criterion);
        return recoverMessages.size() == 1 ? recoverMessages.get(0) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getMessagesByCorename(String corename) {

        QueuedMessage recoverMessage = getByCorename(corename);
        ArrayList<String> messages = new ArrayList<String>();
        if (recoverMessage != null) {
            logger.debug("getMessagesByCorename: found messages for coreJID: " + corename);
            messages = new ArrayList<String>(recoverMessage.getMessageSet());
        }
        return messages;
    }

    @Override
    public boolean removeMessage(String corename, String message) {

        QueuedMessage recoverMessage = getByCorename(corename);
        if (recoverMessage == null) {
            logger.error("removeMessage: there is no message to be removed");
            return false;
        }
        logger.debug("removeMessage: # of messages: " + recoverMessage.getMessageSet().size());
        boolean isRemoved = recoverMessage.getMessageSet().remove(message);
        recoverMessage = makePersistent(recoverMessage);
        logger.debug("removeMessage:  # of messages: " + recoverMessage.getMessageSet().size() +
                     " left");
        return isRemoved;
    }

    @Override
    public void removeMessagesForCore(String corename) {

        QueuedMessage recoverMessage = getByCorename(corename);
        if (recoverMessage != null) {
            makeTransient(recoverMessage);
        }
    }

    @Override
    public void saveMessage(String corename, String message) {

        QueuedMessage recoverMessage = getByCorename(corename);
        if (recoverMessage == null) {
            recoverMessage = new QueuedMessage(corename);
            logger.debug("saveMessage: create entry for " + corename);
        }
        recoverMessage.addMessage(message);
        logger.debug("saveMessage: before persist it ");
        recoverMessage = makePersistent(recoverMessage);
        logger.debug("saveMessage: after persist it. id: " + recoverMessage.getId());
    }
}
