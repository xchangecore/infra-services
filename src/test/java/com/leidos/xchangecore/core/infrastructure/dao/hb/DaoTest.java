package com.leidos.xchangecore.core.infrastructure.dao.hb;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
    "classpath*:*/applicationContext-dataSrc.xml",
    "file:src/test/resources/contexts/test-WorkProductDAOContext.xml"
})
public class DaoTest {

    @Test
    public void testDao() {

    }

}
