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

import java.util.List;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.SchemaChangeListenerBase;
import com.openexchange.exception.OXException;
import com.openexchange.management.ManagementService;
import com.openexchange.nosql.cassandra.CassandraKeyspaceMBean;
import com.openexchange.nosql.cassandra.CassandraNodeMBean;
import com.openexchange.nosql.cassandra.beans.CassandraKeyspaceMBeanImpl;
import com.openexchange.server.ServiceLookup;

/**
 * {@link MBeanSchemaChangeListener}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class MBeanSchemaChangeListener extends SchemaChangeListenerBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(MBeanSchemaChangeListener.class);

    private ServiceLookup services;

    /**
     * Initialises a new {@link MBeanSchemaChangeListener}.
     */
    public MBeanSchemaChangeListener(ServiceLookup services) {
        super();
        this.services = services;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.datastax.driver.core.SchemaChangeListener#onKeyspaceAdded(com.datastax.driver.core.KeyspaceMetadata)
     */
    @Override
    public void onKeyspaceAdded(KeyspaceMetadata keyspace) {
        registerMBean(keyspace);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.datastax.driver.core.SchemaChangeListener#onKeyspaceRemoved(com.datastax.driver.core.KeyspaceMetadata)
     */
    @Override
    public void onKeyspaceRemoved(KeyspaceMetadata keyspace) {
        unregisterMBean(keyspace);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.datastax.driver.core.SchemaChangeListener#onRegister(com.datastax.driver.core.Cluster)
     */
    @Override
    public void onRegister(Cluster cluster) {
        // Register the keyspaces mbeans
        List<KeyspaceMetadata> keyspaces = cluster.getMetadata().getKeyspaces();
        for (KeyspaceMetadata keyspaceMetadata : keyspaces) {
            registerMBean(keyspaceMetadata);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.datastax.driver.core.SchemaChangeListener#onUnregister(com.datastax.driver.core.Cluster)
     */
    @Override
    public void onUnregister(Cluster cluster) {
        List<KeyspaceMetadata> keyspaces = cluster.getMetadata().getKeyspaces();
        for (KeyspaceMetadata keyspaceMetadata : keyspaces) {
            unregisterMBean(keyspaceMetadata);
        }
    }

    /////////////////////////////////////////// HELEPRS //////////////////////////////////////////

    /**
     * Registers a new {@link CassandraKeyspaceMBean} for the specified Cassandra keyspace
     * 
     * @param keyspaceMetadata The {@link KeyspaceMetadata}
     * @throws MalformedObjectNameException if the {@link ObjectName} does not have the right format.
     * @throws NotCompliantMBeanException If the <code>mbeanInterface</code> does not follow JMX design
     *             patterns for Management Interfaces, or if <code>this</code> does not implement the
     *             specified interface
     * @throws OXException if the {@link ManagementService} is absent
     */
    private void registerMBean(KeyspaceMetadata keyspaceMetadata) {
        try {
            ObjectName objectName = createObjectName(keyspaceMetadata);
            CassandraKeyspaceMBean mbean = new CassandraKeyspaceMBeanImpl(services, keyspaceMetadata.getName());

            ManagementService managementService = services.getService(ManagementService.class);
            managementService.registerMBean(objectName, mbean);
            LOGGER.info("Registered MBean for keyspace '{}'", keyspaceMetadata.getName());
        } catch (NotCompliantMBeanException | MalformedObjectNameException | OXException e) {
            LOGGER.error("Error registering MBean for keyspace '{}'", keyspaceMetadata.getName(), e);
        }
    }

    /**
     * Unregisters the {@link CassandraNodeMBean} for the specified Cassandra {@link Host}
     * 
     * @param host The Cassandra {@link Host}
     * @throws MalformedObjectNameException if the {@link ObjectName} does not have the right format.
     * @throws OXException if the {@link ManagementService} is absent
     */
    private void unregisterMBean(KeyspaceMetadata keyspaceMetadata) {
        try {
            ManagementService managementService = services.getService(ManagementService.class);
            ObjectName objectName = createObjectName(keyspaceMetadata);
            managementService.unregisterMBean(objectName);
            LOGGER.info("Unregistered MBean for keyspace '{}'", keyspaceMetadata.getName());
        } catch (MalformedObjectNameException | OXException e) {
            LOGGER.error("Error unregistering MBean for keyspace '{}'", keyspaceMetadata.getName(), e);
        }
    }

    /**
     * Creates a new {@link ObjectName} for the specified Cassandra keyspace. The created
     * {@link ObjectName} has the format:
     * 
     * <code>com.openexchange.nosql.cassandra:00=Keyspace Monitoring Tool,name=KEYSPACE_NAME</code>
     * 
     * @param keyspaceMetadata The {@link KeyspaceMetadata}
     * @return The new {@link ObjectName}
     * @throws MalformedObjectNameException if the {@link ObjectName} does not have the right format.
     */
    private ObjectName createObjectName(KeyspaceMetadata keyspaceMetadata) throws MalformedObjectNameException {
        StringBuilder sb = new StringBuilder(CassandraKeyspaceMBean.DOMAIN);
        // Append the mbean name
        sb.append(":00=").append(CassandraKeyspaceMBean.NAME);
        // Append the keyspace name
        sb.append(",name=").append(keyspaceMetadata.getName());
        return new ObjectName(sb.toString());
    }
}
