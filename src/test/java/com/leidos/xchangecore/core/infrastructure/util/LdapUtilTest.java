package com.leidos.xchangecore.core.infrastructure.util;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
    "classpath*:*/applicationContext-infra.xml"
})
public class LdapUtilTest {

    @Autowired
    LdapUtil ldapUtil;

    @Test
    public void testListOfMembers() {

        List<String> members = ldapUtil.listOfUsers();
        for (final String user : members)
            System.out.println("User: " + user);

        members = ldapUtil.listOfGroup();
        for (final String group : members)
            System.out.println("Group: " + group);
    }

}
