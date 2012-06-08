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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import java.util.Properties;

import org.apache.cassandra.db.KeyspaceNotDefinedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.loxandra.EAVContactFactoryService;
import com.openexchange.loxandra.EAVContactService;

import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;

/**
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 *
 */
public final class CassandraEAVContactFactoryServiceImpl implements EAVContactFactoryService {
	
	private static final Log log = LogFactory.getLog(CassandraEAVContactFactoryServiceImpl.class);
	
	private final static String DEFAULT_CONFIGURATION = "loxandra.properties";
	
	private static Cluster cluster;
	private static Keyspace keyspace;
	private static String node;
	private static String keyspaceName;
	
	public CassandraEAVContactFactoryServiceImpl() {
		readProperties();
	}
	
	/**
	 * Read the .properties file
	 */
	private void readProperties() {
		Properties prop = new Properties();
		String configUrl = System.getProperty("loxandra.config");
        
		if (configUrl == null)
            configUrl = DEFAULT_CONFIGURATION;
		
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
		if (cluster == null)
			cluster = HFactory.getOrCreateCluster("Local Cluster", node);
		
		KeyspaceDefinition kDef = cluster.describeKeyspace(keyspaceName);
		
		if (kDef == null) {
			log.fatal("Keyspace '" + keyspaceName + "' does not exist. Use the 'schema.cql' file to create a schema.", new KeyspaceNotDefinedException("'" + keyspaceName + "' does not exist." ));
			return null;
		}
		
		keyspace = HFactory.createKeyspace(keyspaceName, cluster);
		
		return keyspace;
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