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
package com.openexchange.loxandra.impl.cassandra;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import me.prettyprint.cassandra.model.ConfigurableConsistencyLevel;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.HConsistencyLevel;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;
import org.apache.cassandra.db.KeyspaceNotDefinedException;
import com.openexchange.loxandra.EAVContactFactoryService;
import com.openexchange.loxandra.EAVContactService;

/**
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 *
 */
public final class CassandraEAVContactFactoryServiceImpl implements EAVContactFactoryService {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CassandraEAVContactFactoryServiceImpl.class);

	private final static String DEFAULT_CONFIGURATION = "loxandra.properties";

	private static Cluster cluster;
	private static volatile Keyspace keyspace;
	private static String node;
	private static String keyspaceName;

	private static volatile ConfigurableConsistencyLevel configurableConsistencyLevel;

	public CassandraEAVContactFactoryServiceImpl() {
		readProperties();
	}

	/**
	 * Read the .properties file
	 */
	private void readProperties() {
		Properties prop = new Properties();
		String configUrl = System.getProperty("loxandra.config");

		if (configUrl == null) {
            configUrl = DEFAULT_CONFIGURATION;
        }

        try {
			prop.load(new FileInputStream(configUrl));
			node = prop.getProperty("node");
			keyspaceName = prop.getProperty("keyspace");
		} catch (IOException e) {
			e.printStackTrace();
			log.error("Properties file does not exist.");
		}
	}

	/**
	 * Create a connection to the local cluster and initialize all resources.
	 *
	 * @return keyspace
	 * @throws
	 * KeyspaceNotDefinedException will be thrown if the keyspace does not exist
	 */
	public final static Keyspace getKeyspace() {

		if (cluster == null) {
            cluster = HFactory.getOrCreateCluster("Local Cluster", node);
        }

		KeyspaceDefinition kDef = cluster.describeKeyspace(keyspaceName);

		if (kDef == null) {
			log.error("Keyspace ''{}'' does not exist. Use the ''schema.cql'' file to create a schema.", keyspaceName, new KeyspaceNotDefinedException("'" + keyspaceName + "' does not exist." ));
			return null;
		}

		defineConsistencyLevels();
		keyspace = HFactory.createKeyspace(keyspaceName, cluster, configurableConsistencyLevel);

		return keyspace;
	}

	/**
	 * Define consistency levels for each column family.
	 *
	 * <li><b>ANY</b>: Wait until some replica has responded.</li>
	 * <li><b>ONE</b>: Wait until one replica has responded.
	 * <li><b>TWO</b>: Wait until two replicas have responded.
	 * <li><b>THREE</b>: Wait until three replicas have responded.
	 * <li><b>LOCAL_QUORUM</b>: Wait for quorum on the datacenter the connection was stablished.
	 * <li><b>EACH_QUORUM</b>: Wait for quorum on each datacenter.
	 * <li><b>QUORUM</b>: Wait for a quorum of replicas (no matter which datacenter).
	 * <li><b>ALL</b>: Blocks for all the replicas before returning to the client.
	 */
	private final static void defineConsistencyLevels() {
		configurableConsistencyLevel = new ConfigurableConsistencyLevel();

		Map<String, HConsistencyLevel> readCLMap = new HashMap<String, HConsistencyLevel>();
		Map<String, HConsistencyLevel> writeCLMap = new HashMap<String, HConsistencyLevel>();

		readCLMap.put("Person", HConsistencyLevel.ONE);
		readCLMap.put("PersonFolder", HConsistencyLevel.ONE);
		readCLMap.put("Counters", HConsistencyLevel.ONE);
		readCLMap.put("TransactionLog", HConsistencyLevel.QUORUM);

		writeCLMap.put("Person", HConsistencyLevel.ONE);
		writeCLMap.put("PersonFolder", HConsistencyLevel.ONE);
		writeCLMap.put("Counters", HConsistencyLevel.ONE);
		writeCLMap.put("TransactionLog", HConsistencyLevel.QUORUM);

		configurableConsistencyLevel.setReadCfConsistencyLevels(readCLMap);
		configurableConsistencyLevel.setWriteCfConsistencyLevels(writeCLMap);
	}

	/*
	 * (non-Javadoc)
	 * @see loxandra.dao.DAOFactory#getContactDAO()
	 */
	@Override
	public final EAVContactService getEAVContactService() {
		return new CassandraEAVContactServiceImpl();
	}
}