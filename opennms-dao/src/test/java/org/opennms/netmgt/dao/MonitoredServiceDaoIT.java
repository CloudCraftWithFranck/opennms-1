/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;


@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class MonitoredServiceDaoIT implements InitializingBean {

    @Autowired
    private MonitoredServiceDao m_monitoredServiceDao;

    @Autowired
    private DatabasePopulator m_databasePopulator;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() {
        m_databasePopulator.populateDatabase();
    }

    @Test
    @Transactional
    public void testLazy() {
        final List<OnmsMonitoredService> allSvcs = m_monitoredServiceDao.findAll();
        assertTrue(allSvcs.size() > 1);

        final OnmsMonitoredService svc = allSvcs.iterator().next();
        assertEquals(addr("192.168.1.1"), svc.getIpAddress());
        assertEquals(1, svc.getIfIndex().intValue());
        assertEquals(m_databasePopulator.getNode1().getId(), svc.getIpInterface().getNode().getId());
        assertEquals("M", svc.getIpInterface().getIsManaged());
    }

    @Test
    @Transactional
    public void testFindAllServices() {
        final List<OnmsMonitoredService> allSvcs = m_monitoredServiceDao.findAllServices();
        assertTrue(allSvcs.size() > 1);
        for (OnmsMonitoredService ifservice: allSvcs) {
            assertNotNull(ifservice.getIpInterface());
            assertNotNull(ifservice.getIpInterface().getNode());
            assertNotNull(ifservice.getIpAddress());
            assertNotNull(ifservice.getNodeId());
            assertNotNull(ifservice.getServiceType());
        }
        
    }

    @Test
    @Transactional
    public void testGetByCompositeId() {
        final OnmsMonitoredService monSvc = m_monitoredServiceDao.get(m_databasePopulator.getNode1().getId(), addr("192.168.1.1"), "SNMP");
        assertNotNull(monSvc);

        final OnmsMonitoredService monSvc2 = m_monitoredServiceDao.get(m_databasePopulator.getNode1().getId(), addr("192.168.1.1"), monSvc.getIfIndex(), monSvc.getServiceId());
        assertNotNull(monSvc2);

    }

    /**
     * This test exposes a bug in Hibernate: it is not applying join conditions
     * correctly to the many-to-many service-to-application relationship.
     * 
     * This issue is documented in NMS-9470. If we upgrade Hibernate, we should
     * recheck this issue to see if it is fixed.
     * 
     * @see https://issues.opennms.org/browse/NMS-9470
     */
    @Test
    @Transactional
    @Ignore("Ignore until Hibernate can be upgraded and this can be rechecked")
    public void testCriteriaBuilderWithApplicationAlias() {
        CriteriaBuilder cb = new CriteriaBuilder(OnmsMonitoredService.class);
        cb.alias("applications", "application", JoinType.LEFT_JOIN, Restrictions.eq("application.name", "HelloWorld"));
        m_monitoredServiceDao.findMatching(cb.toCriteria());
    }

}
