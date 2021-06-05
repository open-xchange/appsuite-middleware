/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.nosql.cassandra.mbean.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.management.NotCompliantMBeanException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.FunctionMetadata;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.UserType;
import com.openexchange.exception.OXException;
import com.openexchange.management.AnnotatedDynamicStandardMBean;
import com.openexchange.nosql.cassandra.CassandraService;
import com.openexchange.nosql.cassandra.mbean.CassandraKeyspaceMBean;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;

/**
 * {@link CassandraKeyspaceMBeanImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class CassandraKeyspaceMBeanImpl extends AnnotatedDynamicStandardMBean implements CassandraKeyspaceMBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraKeyspaceMBeanImpl.class);

    private KeyspaceMetadata keyspaceMetadata;
    private final String keyspaceName;

    /**
     * Initialises a new {@link CassandraKeyspaceMBeanImpl}.
     *
     * @param services
     * @param description
     * @param mbeanInterface
     * @throws NotCompliantMBeanException
     */
    public CassandraKeyspaceMBeanImpl(ServiceLookup services, String keyspaceName) throws NotCompliantMBeanException {
        super(services, CassandraKeyspaceMBean.NAME, CassandraKeyspaceMBean.class);
        this.keyspaceName = keyspaceName;
    }

    @Override
    protected void refresh() {
        try {
            CassandraService cassandraService = getService(CassandraService.class);
            if (cassandraService == null) {
                throw ServiceExceptionCode.absentService(CassandraService.class);
            }
            Cluster cluster = cassandraService.getCluster();
            keyspaceMetadata = cluster.getMetadata().getKeyspace(keyspaceName);
        } catch (OXException e) {
            LOGGER.error("Could not refresh the metadata for the keyspace '{}'.", keyspaceName, e);
        }
    }

    @Override
    public Set<String> getTables() {
        Collection<TableMetadata> tables = keyspaceMetadata.getTables();
        Set<String> tableNames = new HashSet<>();
        for (TableMetadata table : tables) {
            tableNames.add(table.getName());
        }
        return tableNames;
    }

    @Override
    public Map<String, String> getReplicationOptions() {
        return keyspaceMetadata.getReplication();
    }

    @Override
    public Set<String> getUserTypes() {
        Set<String> userTypes = new HashSet<>();
        for (UserType userType : keyspaceMetadata.getUserTypes()) {
            userTypes.add(userType.getTypeName());
        }
        return userTypes;
    }

    @Override
    public Set<String> getFunctions() {
        Set<String> functions = new HashSet<>();
        StringBuilder sb = new StringBuilder();
        for (FunctionMetadata function : keyspaceMetadata.getFunctions()) {
            sb.append(function.getReturnType().getName());
            sb.append(" ");
            sb.append(function.getSignature());
            functions.add(sb.toString());
            sb.setLength(0);
        }
        return functions;
    }
}
