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
package com.openexchange.eav.storage.cassandra.impl;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import me.prettyprint.cassandra.model.BasicColumnFamilyDefinition;
import me.prettyprint.cassandra.model.ConfigurableConsistencyLevel;
import me.prettyprint.cassandra.serializers.CompositeSerializer;
import me.prettyprint.cassandra.serializers.UUIDSerializer;
import me.prettyprint.cassandra.service.ThriftCfDef;
import me.prettyprint.cassandra.service.ThriftKsDef;
import me.prettyprint.cassandra.service.template.ColumnFamilyResult;
import me.prettyprint.cassandra.service.template.ColumnFamilyTemplate;
import me.prettyprint.cassandra.service.template.ThriftColumnFamilyTemplate;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.HConsistencyLevel;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.Composite;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.exceptions.HectorException;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;

import org.apache.cassandra.db.KeyspaceNotDefinedException;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.ajax.tools.JSONUtil;
import com.openexchange.eav.EAVStorage;
import com.openexchange.exception.OXException;

/**
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class CassandraEAVStorageImpl implements EAVStorage {
	
	private static final Log log = LogFactory.getLog(CassandraEAVStorageImpl.class);
	
	private static Cluster cluster;
	private static volatile Keyspace keyspace;
	private static final String node = "192.168.33.37"; //TODO: fetch dynamic
	private static final String keyspaceName = "OX";
	private static final String CF_XT_PROPS = "ExtendedProperties";
	private static final int replicationFactor = 3;
	
	private final ColumnFamilyTemplate<UUID, Composite> xtPropsTemplate;
	
	private static final UUIDSerializer us = UUIDSerializer.get();
	private static final CompositeSerializer cs	= CompositeSerializer.get();

	private static ConfigurableConsistencyLevel configurableConsistencyLevel;
	
	public CassandraEAVStorageImpl() {
		initKeyspace();
		
		xtPropsTemplate = new ThriftColumnFamilyTemplate<UUID, Composite>(keyspace, CF_XT_PROPS, us, cs);
	}
	
	/**
	 * Create a connection to the local cluster and initialize all resources.
	 * 
	 * @throws
	 * KeyspaceNotDefinedException will be thrown if the keyspace does not exist
	 */
	private final void initKeyspace() {
		
		if (cluster == null) {
            cluster = HFactory.getOrCreateCluster("Local Cluster", node);
        }
		
		KeyspaceDefinition kDef = cluster.describeKeyspace(keyspaceName);
		
		if (kDef == null) {
			log.fatal("Keyspace '" + keyspaceName + "' does not exist. Creating...");
			createSchema();
			log.info("done.");
		}

		defineConsistencyLevels();
		keyspace = HFactory.createKeyspace(keyspaceName, cluster, configurableConsistencyLevel);
	}
	
	/**
	 * Create the schema.
	 */
	private final void createSchema() {
		BasicColumnFamilyDefinition bcfDef = new BasicColumnFamilyDefinition();
		bcfDef.setKeyspaceName(keyspaceName);
		bcfDef.setName(CF_XT_PROPS);
		bcfDef.setKeyValidationClass("UUIDType");
		bcfDef.setComparatorType(ComparatorType.UTF8TYPE);
		bcfDef.setReplicateOnWrite(true);
		bcfDef.setGcGraceSeconds(10);
		
		ColumnFamilyDefinition cfDef = new ThriftCfDef(bcfDef);
		KeyspaceDefinition newKeyspace = HFactory.createKeyspaceDefinition(keyspaceName, ThriftKsDef.DEF_STRATEGY_CLASS, replicationFactor, Arrays.asList(cfDef));
		try {
			cluster.addKeyspace(newKeyspace, false);
		} catch (HectorException h) {
			log.fatal(h.getMessage());
		}
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
		
		readCLMap.put(CF_XT_PROPS, HConsistencyLevel.ONE);
		
		writeCLMap.put(CF_XT_PROPS, HConsistencyLevel.ONE);
		
		configurableConsistencyLevel.setReadCfConsistencyLevels(readCLMap);
		configurableConsistencyLevel.setWriteCfConsistencyLevels(writeCLMap);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.openexchange.eav.EAVStorageAlt#getAttributes(java.util.UUID)
	 */
	@Override
	public Map<String, Object> getAttributes(UUID u) throws OXException {
		Map<String, Object> attr = null;
		try {
			UUID xtPropsKey = u;
			
			if (xtPropsKey == null) {
				//throw new OXException(666, "nothing found for: contextID:" + contextID + ", objectID: " + objectID + ", module: " + module);
			}
			
			ColumnFamilyResult<UUID, Composite> result = xtPropsTemplate.queryColumns(xtPropsKey);
			if (result == null || !result.hasResults()) {
				log.error("No result");
			} else {
				attr = new HashMap<String, Object>();
				
				Iterator<Composite> it = result.getColumnNames().iterator();
				while (it.hasNext()) {
					Composite columnName = (Composite) it.next();
					ByteBuffer value = result.getColumn(columnName).getValue();
					attr.put(ByteBufferUtil.string((ByteBuffer)columnName.get(0)), JSONUtil.toObject((ByteBufferUtil.string(value))));
				}
			}
		} catch (CharacterCodingException e) {
			e.printStackTrace();
		} catch (JSONException j) {
			j.printStackTrace();
		}
		
		return attr;
	}

	/*
	 * (non-Javadoc)
	 * @see com.openexchange.eav.EAVStorageAlt#getAttributes(java.util.UUID, java.lang.String[])
	 */
	@Override
	public Map<String, Object> getAttributes(UUID u, String... attributes) throws OXException {
		Map<String, Object> attr = getAttributes(u);
		Map<String, Object> retAttr = new HashMap<String, Object>(attributes.length);
		
		int i = 0;
		while (i < attributes.length) {
			if (attr.containsKey(attributes[i])) {
				retAttr.put(attributes[i], attr.get(attributes[i]));
				i++;
			}
		}
		
		return retAttr;
	}

	/*
	 * (non-Javadoc)
	 * @see com.openexchange.eav.EAVStorage#getAttributes(java.util.List)
	 */
	@Override
	public Map<UUID, Map<String, Object>> getAttributes(List<UUID> uuids) throws OXException {
		Map<UUID, Map<String, Object>> retAttr = new HashMap<UUID, Map<String, Object>>(uuids.size());
		
		Iterator<UUID> it = uuids.iterator();
		while (it.hasNext()) {
			UUID uuid = (UUID) it.next();
			Map<String, Object> map = getAttributes(uuid);
			retAttr.put(uuid, map);
			
		}
		
		return retAttr;
	}

	/*
	 * (non-Javadoc)
	 * @see com.openexchange.eav.EAVStorage#getAttributes(java.util.List, java.lang.String[])
	 */
	@Override
	public Map<UUID, Map<String, Object>> getAttributes(List<UUID> uuids, String... attributes) throws OXException {
		Map<UUID, Map<String, Object>> retAttr = new HashMap<UUID, Map<String, Object>>(uuids.size());
		
		Iterator<UUID> it = uuids.iterator();
		while (it.hasNext()) {
			UUID uuid = (UUID) it.next();
			Map<String, Object> map = getAttributes(uuid, attributes);
			retAttr.put(uuid, map);
			
		}
		
		return retAttr;
	}

	/*
	 * (non-Javadoc)
	 * @see com.openexchange.eav.EAVStorageAlt#hasAttributes(java.util.UUID)
	 */
	@Override
	public boolean hasAttributes(UUID u) throws OXException {
		UUID xtPropsKey = u;
		ColumnFamilyResult<UUID, Composite> result = xtPropsTemplate.queryColumns(xtPropsKey);
		
		return !(result == null || !result.hasResults());
	}

	/*
	 * (non-Javadoc)
	 * @see com.openexchange.eav.EAVStorageAlt#deleteAttributes(java.util.UUID)
	 */
	@Override
	public void deleteAttributes(UUID u) throws OXException {
		UUID xtPropsKey = u;
		Mutator<UUID> m = HFactory.createMutator(keyspace, us);
		m.addDeletion(xtPropsKey, CF_XT_PROPS);
		m.execute();
	}

	/*
	 * (non-Javadoc)
	 * @see com.openexchange.eav.EAVStorageAlt#setAttributes(java.util.UUID, java.util.Map)
	 */
	@Override
	public void setAttributes(UUID u, Map<String, Object> attributes) throws OXException {
		Mutator<UUID> m = HFactory.createMutator(keyspace, us);
		UUID xtPropsKey = u;
		
		Iterator<String> it = attributes.keySet().iterator();
		while (it.hasNext()) {
			String columnName = (String) it.next();
			Object o = attributes.get(columnName);
			Composite compoColumnName = new Composite(columnName);
			if (o == null) {
				m.addDeletion(xtPropsKey, CF_XT_PROPS, compoColumnName, cs);
			} else {
				
				if (JSONCoercion.needsJSONCoercion(o)) {
					try {
						Object j = JSONCoercion.coerceToJSON(o);
						String json = null;
						
						if (j instanceof JSONObject)
							json = ((JSONObject)JSONCoercion.coerceToJSON(o)).toString();
						else if (j instanceof JSONArray)
							json = ((JSONArray)JSONCoercion.coerceToJSON(o)).toString();
						else
							throw new OXException(666, "Unsupported attribute type. Data: " + j);
						
						m.addInsertion(xtPropsKey, CF_XT_PROPS, HFactory.createColumn(compoColumnName, json));
					
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else {
					if (o instanceof String) {
						m.addInsertion(xtPropsKey, CF_XT_PROPS, HFactory.createColumn(compoColumnName, (String)o));
					} else if (o instanceof Integer) {
						m.addInsertion(xtPropsKey, CF_XT_PROPS, HFactory.createColumn(compoColumnName, String.valueOf((Integer)o)));
					} else if (o instanceof Long)
						m.addInsertion(xtPropsKey, CF_XT_PROPS, HFactory.createColumn(compoColumnName, String.valueOf((Long)o)));
					else if (o instanceof Double)
						m.addInsertion(xtPropsKey, CF_XT_PROPS, HFactory.createColumn(compoColumnName, String.valueOf((Double)o)));
					else if (o instanceof Boolean)
						m.addInsertion(xtPropsKey, CF_XT_PROPS, HFactory.createColumn(compoColumnName, String.valueOf((Boolean)o)));
					else if (o instanceof Float)
						m.addInsertion(xtPropsKey, CF_XT_PROPS, HFactory.createColumn(compoColumnName, String.valueOf((Float)o)));
					else if (o instanceof Date)
						m.addInsertion(xtPropsKey, CF_XT_PROPS, HFactory.createColumn(compoColumnName, String.valueOf(((Date) o).getTime())));
					else
						throw new OXException(666, "Unsupported attribute type. Data: " + o);
				}
			}
		}
		
		try {
			m.execute();
		} catch (HectorException h) {
			h.printStackTrace();
		}
	}

}
