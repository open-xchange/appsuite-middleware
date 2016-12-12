/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.nosql.cassandra.impl;

import java.util.HashSet;
import java.util.Set;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Host.StateListener;
import com.openexchange.exception.OXException;
import com.openexchange.management.ManagementService;
import com.openexchange.nosql.cassandra.CassandraNodeMBean;
import com.openexchange.nosql.cassandra.beans.CassandraNodeMBeanImpl;
import com.openexchange.server.ServiceLookup;

/**
 * {@link MBeanHostStateListener}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class MBeanHostStateListener implements StateListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(MBeanHostStateListener.class);

    private ServiceLookup services;
    private Set<Host> hosts;

    /**
     * Initialises a new {@link MBeanHostStateListener}.
     */
    public MBeanHostStateListener(ServiceLookup services) {
        super();
        this.services = services;
        hosts = new HashSet<>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.datastax.driver.core.Host.StateListener#onAdd(com.datastax.driver.core.Host)
     */
    @Override
    public void onAdd(Host host) {
        String hostAddress = host.getAddress().getHostAddress();
        if (hosts.contains(host)) {
            LOGGER.debug("The Cassandra node '{}' is already registered with this OX node", hostAddress);
            return;
        }
        try {
            registerMBean(host);
            hosts.add(host);
            LOGGER.info("Registered MBean for Cassandra node '{}", hostAddress);
        } catch (MalformedObjectNameException | NotCompliantMBeanException | OXException e) {
            LOGGER.error("Error registering MBean for Cassandra node '{}'", hostAddress, e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.datastax.driver.core.Host.StateListener#onUp(com.datastax.driver.core.Host)
     */
    @Override
    public void onUp(Host host) {
        //nothing yet
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.datastax.driver.core.Host.StateListener#onDown(com.datastax.driver.core.Host)
     */
    @Override
    public void onDown(Host host) {
        //nothing yet
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.datastax.driver.core.Host.StateListener#onRemove(com.datastax.driver.core.Host)
     */
    @Override
    public void onRemove(Host host) {
        String hostAddress = host.getAddress().getHostAddress();
        if (!hosts.contains(host)) {
            LOGGER.debug("The Cassandra node '{}' was already unregistered from this OX node", hostAddress);
            return;
        }
        try {
            unregisterMBean(host);
            hosts.remove(host);
            LOGGER.info("Unregistered MBean for Cassandra node '{}", hostAddress);
        } catch (MalformedObjectNameException | OXException e) {
            LOGGER.error("Error unregistering MBean for Cassandra node '{}'", hostAddress, e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.datastax.driver.core.Host.StateListener#onRegister(com.datastax.driver.core.Cluster)
     */
    @Override
    public void onRegister(Cluster cluster) {
        //nothing yet
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.datastax.driver.core.Host.StateListener#onUnregister(com.datastax.driver.core.Cluster)
     */
    @Override
    public void onUnregister(Cluster cluster) {
        for (Host host : hosts) {
            String hostAddress = host.getAddress().getHostAddress();
            try {
                unregisterMBean(host);
                LOGGER.info("Unregistered MBean for Cassandra node '{}", hostAddress);
            } catch (MalformedObjectNameException | OXException e) {
                LOGGER.error("Error unregistering MBean for Cassandra node '{}'", hostAddress, e);
            }
        }
        hosts.clear();
    }

    /////////////////////////////////////////// HELEPRS //////////////////////////////////////////

    /**
     * Registers a new {@link CassandraNodeMBean} for the specified Cassandra {@link Host}
     * 
     * @param host The Cassandra {@link Host}
     * @throws MalformedObjectNameException if the {@link ObjectName} does not have the right format.
     * @throws NotCompliantMBeanException If the <code>mbeanInterface</code> does not follow JMX design patterns for Management Interfaces, or if <code>this</code> does not implement the specified interface
     * @throws OXException if the {@link ManagementService} is absent
     */
    private void registerMBean(Host host) throws MalformedObjectNameException, NotCompliantMBeanException, OXException {
        ObjectName objectName = createObjectName(host);
        CassandraNodeMBean mbean = new CassandraNodeMBeanImpl(services, host);

        ManagementService managementService = services.getService(ManagementService.class);
        managementService.registerMBean(objectName, mbean);
    }

    /**
     * Unregisters the {@link CassandraNodeMBean} for the specified Cassandra {@link Host}
     * 
     * @param host The Cassandra {@link Host}
     * @throws MalformedObjectNameException if the {@link ObjectName} does not have the right format.
     * @throws OXException if the {@link ManagementService} is absent
     */
    private void unregisterMBean(Host host) throws MalformedObjectNameException, OXException {
        ManagementService managementService = services.getService(ManagementService.class);
        ObjectName objectName = createObjectName(host);
        managementService.unregisterMBean(objectName);
    }

    /**
     * Creates a new {@link ObjectName} for the specified Cassandra {@link Host}. The created
     * {@link ObjectName} has the format:
     * 
     * <code>com.openexchange.nosql.cassandra:00=Cassandra Node Monitoring Tool,01=DATACENTER,02=RACK,name=HOSTNAME</code>
     * 
     * @param host The {@link Host}
     * @return The new {@link ObjectName}
     * @throws MalformedObjectNameException if the {@link ObjectName} does not have the right format.
     */
    private ObjectName createObjectName(Host host) throws MalformedObjectNameException {
        StringBuilder sb = new StringBuilder(CassandraNodeMBean.DOMAIN);
        sb.append(":00=").append(CassandraNodeMBean.NAME);
        // Append datacenter
        sb.append(",01=").append(host.getDatacenter());
        // Append rack
        sb.append(",02=").append(host.getRack());
        // Append hostname
        sb.append(",name=").append(host.getAddress().getHostAddress());
        return new ObjectName(sb.toString());
    }
}
