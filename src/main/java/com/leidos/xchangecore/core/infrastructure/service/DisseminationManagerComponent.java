/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.leidos.xchangecore.core.infrastructure.service;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leidos.xchangecore.core.infrastructure.dao.UserInterestGroupDAO;
import com.leidos.xchangecore.core.infrastructure.messages.DisseminationManagerMessage;

/**
 *
 * @author vmuser
 */
public class DisseminationManagerComponent {

    /** The logger. */
    Logger logger = LoggerFactory.getLogger(DisseminationManagerComponent.class);

    private UserInterestGroupDAO userInterestGroupDAO;

    // add a single JID to the specified interest group
    public void addJID(String igid, String jid) {

        logger.debug("addJID: jid: " + jid + ", IGID: " + igid);
        getUserInterestGroupDAO().addUser(jid, igid);

        /*
        ArrayList value = manager.get(igid);
        // if the interest group is already in the map, add the jid to its jid list
        if (value != null) {
            value.add(jid);
        }
        // otherwise, create a new entry
        else {
            ArrayList jids = new ArrayList();
            jids.add(jid);
            manager.put(igid, jids);
            // notificationService.addUser(igid, jid);
        }
         */
    }

    /**
     *
     * handle message on DissMgr channel for add/remove users and groups
     * message origin from AutoShare.
     */
    public void disseminationManagerMessageHandler(DisseminationManagerMessage message) {

        logger.debug("Received Dissemination Manager Message\n" + "\tIGID: " +
                     message.getInterestGroupID() + "\n" + "\tADD: " +
                     printArrayList(message.getJidsToAdd()) + "\n" + "\tREMOVE: " +
                     printArrayList(message.getJidsToRemove()) + "\n");

        final String igid = message.getInterestGroupID();
        // make sure there is an IG id first...
        if (igid != null) {

            // process the jids to add
            if (!message.getJidsToAdd().isEmpty())
                for (final String jid : message.getJidsToAdd()) {
                    logger.debug("Adding user " + jid + " to IG " + igid);
                    addJID(igid, jid);
                }

            // process the jids to remove
            if (!message.getJidsToRemove().isEmpty())
                for (final String jid : message.getJidsToRemove()) {
                    logger.debug("Removing user " + jid + " from IG " + igid);
                    removeJID(igid, jid);
                }

            // TODO: process groups add/remove
            // TODO: dynamic lookup?  move to queue manager?
        } else
            logger.info("No interest group ID was found");
    }

    public UserInterestGroupDAO getUserInterestGroupDAO() {

        return userInterestGroupDAO;
    }

    // utility for printing array lists
    private String printArrayList(ArrayList<String> list) {

        String listString = "";
        for (final String s : list)
            listString += s + " ";
        return listString;
    }

    // remove a single JID from the specified interest group
    public void removeJID(String igid, String jid) {

        logger.debug("removeJID: jid" + jid + ", IGID: " + igid);
        getUserInterestGroupDAO().removeUser(jid, igid);

        /*
        ArrayList value = manager.get(igid);
        // if the igid exists, try to remove the jid
        if (value != null) {
            value.remove(jid);
            // if the jid list is empty, remove the igid from the dissemination manager
            if (value.isEmpty()) {
                manager.remove(igid);
                // notificationService.RemoveUser(igid, jid);
            }
        }
         */
    }

    public void setUserInterestGroupDAO(UserInterestGroupDAO userInterestGroupDAO) {

        this.userInterestGroupDAO = userInterestGroupDAO;
    }

    /**
     * System initialized handler.
     *
     * @param message the message
     */
    public void systemInitializedHandler(String message) {

        logger.debug("systemInitializedHandler: ... start ...");
        logger.debug("systemInitializedHandler: ... done ...");
    }
}
