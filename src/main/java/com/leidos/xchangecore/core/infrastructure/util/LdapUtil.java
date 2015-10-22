/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.leidos.xchangecore.core.infrastructure.util;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author vmuser
 */
public class LdapUtil {

    private static Logger logger = LoggerFactory.getLogger(LdapUtil.class);

    private static Hashtable<String, String> env = new Hashtable<String, String>();

    private static final String GroupName_USERS = "xchangecore-users";
    private static final String GroupName_ADMINS = "xchangecore-admins";
    private static final String UserObjectName = "objectClass=inetOrgPerson";
    private static final String GroupObjectName = "objectClass=groupOfUniqueNames";

    // ldap connection parameters
    private static String S_SecurityPrincipal = "cn=\"Directory Manager\"";

    private static String S_ConnectionUrl = "ldap://localhost:389/dc=domain,dc=us";

    // some useful regex
    private static Pattern commonNamePattern = Pattern.compile("cn=([^,]+)");

    static {
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.SECURITY_PRINCIPAL, S_SecurityPrincipal);
    }

    private String ldapDomain;

    public LdapUtil() {

    }

    public String[] getCNLocation(String cn) {

        logger.debug("getCNLocation: Looking up lat/lon for cn=" + cn);

        final String[] locationArray = new String[] {
            "",
            ""
        };

        try {
            // Create initial context
            final DirContext ctx = new InitialDirContext(env);

            final String searchFilter = "(cn=" + cn + ")";

            final SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            final String[] attrsFilter = {
                "geoLatitude",
                "geoLongitude"
            };
            searchControls.setReturningAttributes(attrsFilter);

            final NamingEnumeration<SearchResult> results = ctx.search("",
                                                                       searchFilter,
                                                                       searchControls);

            SearchResult searchResult;

            String latitude;
            String longitude;

            if (results.hasMoreElements()) {
                searchResult = results.nextElement();
                Attribute members = searchResult.getAttributes().get("geoLatitude");
                if (members != null) {
                    latitude = (String) members.get();
                    locationArray[0] = latitude;
                }
                members = searchResult.getAttributes().get("geoLongitude");
                if (members != null) {
                    longitude = (String) members.get();
                    locationArray[1] = longitude;
                }
            }
            logger.debug("getCNLocation: [lat/lon]: [" + locationArray[0] + "/" + locationArray[1] +
                         "]");
            // Close the context when we're done
            ctx.close();
        } catch (final Exception e) {
            logger.error("getCNLocation: " + e.getMessage());
        }

        return locationArray;
    }

    public ArrayList<String> getGroupMembers(String group) {

        // final String group = getLdapDomain() + "-" + groupName;

        logger.debug("Looking up members of group: " + group);

        final ArrayList<String> membersArray = new ArrayList<String>();

        try {
            // Create initial context
            final DirContext ctx = new InitialDirContext(env);

            final String searchFilter = "(cn=" + group + ")";

            final SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            final String[] attrsFilter = {
                "uniqueMember"
            };
            searchControls.setReturningAttributes(attrsFilter);

            final NamingEnumeration<SearchResult> results = ctx.search("",
                                                                       searchFilter,
                                                                       searchControls);

            SearchResult searchResult;

            Matcher matcher;
            while (results.hasMoreElements()) {
                searchResult = results.nextElement();
                final Attribute members = searchResult.getAttributes().get("uniqueMember");
                for (int i = 0; i < members.size(); i++) {
                    matcher = LdapUtil.commonNamePattern.matcher(members.get(i).toString());
                    if (matcher.find())
                        membersArray.add(matcher.group(1));
                }
            }

            // Close the context when we're done
            ctx.close();
        } catch (final Exception e) {
            logger.error("getGroupMembers: " + e.getMessage());
        }

        return membersArray;

    }

    public ArrayList<String> getGroupMembersForAdmins() {

        return getGroupMembers(GroupName_ADMINS);
    }

    public ArrayList<String> getGroupMembersForUsers() {

        return getGroupMembers(GroupName_USERS);
    }

    public String getLdapDomain() {

        return ldapDomain;
    }

    public Boolean groupContainsMember(String group, String member) {

        logger.debug("Does group " + group + " contain member " + member);

        try {
            // Create initial context
            final DirContext ctx = new InitialDirContext(env);

            final String searchFilter = "(&(uniqueMember=cn=" + member + ",dc=" + getLdapDomain() +
                                        ",dc=us)(objectClass=groupOfUniqueNames))";

            final SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

            final NamingEnumeration<SearchResult> results = ctx.search("",
                                                                       searchFilter,
                                                                       searchControls);

            // Close the context when we're done
            ctx.close();
            // boolean isInGroup = false;
            if (results.hasMoreElements())
                //9/26/2014 Tom and Andrew - add logging and conditional logic to check if results contain group passed in
                while (results.hasMoreElements()) {
                    final SearchResult result = results.nextElement();
                    logger.debug("Name: " + result.getName());
                    logger.debug("Name in namespace " + result.getNameInNamespace());
                    if (result.getName().equals("cn=" + group)) {
                        // isInGroup = true;
                        logger.debug("Found that this member " + member + " is in group " + group);
                        return true;
                    }
                }
            else
                logger.debug("User: " + member + " is not in any groups.");
            logger.debug("User: " + member + " is not in group " + group);

        } catch (final Exception e) {
            logger.error("groupContainsMember: " + e.getMessage());
        }
        return false;
    }

    public List<String> listOfGroup() {

        return listOfObjectByType(false);
    }

    private List<String> listOfObjectByType(boolean isUser) {

        final List<String> members = new ArrayList<String>();
        try {
            // Create initial context
            final DirContext ctx = new InitialDirContext(env);

            final String searchFilter = isUser ? UserObjectName : GroupObjectName;

            final SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

            final NamingEnumeration<SearchResult> results = ctx.search("",
                                                                       searchFilter,
                                                                       searchControls);

            // Close the context when we're done
            ctx.close();

            while (results.hasMoreElements()) {
                final SearchResult result = results.nextElement();

                final Matcher matcher = LdapUtil.commonNamePattern.matcher(result.getName());
                if (matcher.find())
                    members.add(matcher.group(1));
            }

        } catch (final Exception e) {
            logger.error("listOfObjectByType: " + e.getMessage());
            return members;
        }

        return members;
    }

    /*
     * To get a list of all users defined in domain
     */
    public List<String> listOfUsers() {

        return listOfObjectByType(true);
    }

    public void setLdapDomain(String ldapDomain) {

        this.ldapDomain = ldapDomain;
        S_ConnectionUrl = S_ConnectionUrl.replaceAll("domain", ldapDomain);
        env.put(Context.PROVIDER_URL, S_ConnectionUrl);
        logger.debug("setLdapDomain: " + S_ConnectionUrl);
    }

    public void setPassword(String password) {

        LdapUtil.env.put(Context.SECURITY_CREDENTIALS, password);
    }
}
