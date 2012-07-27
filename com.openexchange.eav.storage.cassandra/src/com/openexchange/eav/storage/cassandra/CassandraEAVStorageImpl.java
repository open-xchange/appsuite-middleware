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
package com.openexchange.eav.storage.cassandra;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.MalformedInputException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.prettyprint.cassandra.model.ConfigurableConsistencyLevel;
import me.prettyprint.cassandra.serializers.ByteBufferSerializer;
import me.prettyprint.cassandra.serializers.CompositeSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.serializers.UUIDSerializer;
import me.prettyprint.cassandra.service.template.ColumnFamilyResult;
import me.prettyprint.cassandra.service.template.ColumnFamilyTemplate;
import me.prettyprint.cassandra.service.template.ColumnFamilyUpdater;
import me.prettyprint.cassandra.service.template.ThriftColumnFamilyTemplate;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.HConsistencyLevel;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.beans.Composite;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.exceptions.HectorException;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.SliceQuery;

import org.apache.cassandra.db.KeyspaceNotDefinedException;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.eav.EAVStorage;
import com.openexchange.exception.OXException;

/**
 * Generic {@link EAVStorage} implementation based on Cassandra.
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class CassandraEAVStorageImpl implements EAVStorage {
	
	private static final Log log = LogFactory.getLog(CassandraEAVStorageImpl.class);
	
	private static Cluster cluster;
	private static volatile Keyspace keyspace;
	private static String node = "192.168.33.37";
	private static String keyspaceName = "OX";
	private static final String CF_XT_PROPS = "ExtendedProperties";
	private static final String CF_CONTEXT = "Context";
	
	private final ColumnFamilyTemplate<UUID, String> xtPropsTemplate;
	private final ColumnFamilyTemplate<UUID, Composite> contextTemplate;
	
	private static final StringSerializer ss = StringSerializer.get();
	private static final UUIDSerializer us = UUIDSerializer.get();
	private static final ByteBufferSerializer bbs = ByteBufferSerializer.get();
	private static final CompositeSerializer cs	= CompositeSerializer.get();

	private static ConfigurableConsistencyLevel configurableConsistencyLevel;
	
	public CassandraEAVStorageImpl() {
		initKeyspace();
		
		xtPropsTemplate = new ThriftColumnFamilyTemplate<UUID, String>(keyspace, CF_XT_PROPS, us, ss);
		contextTemplate = new ThriftColumnFamilyTemplate<UUID, Composite>(keyspace, CF_CONTEXT, us, cs);
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
			log.fatal("Keyspace '" + keyspaceName + "' does not exist. Use the 'schema.cql' file to create a schema.", new KeyspaceNotDefinedException("'" + keyspaceName + "' does not exist." ));
		}

		defineConsistencyLevels();
		keyspace = HFactory.createKeyspace(keyspaceName, cluster, configurableConsistencyLevel);
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
		readCLMap.put(CF_CONTEXT, HConsistencyLevel.ONE);
		
		writeCLMap.put(CF_XT_PROPS, HConsistencyLevel.ONE);
		writeCLMap.put(CF_CONTEXT, HConsistencyLevel.ONE);
		
		configurableConsistencyLevel.setReadCfConsistencyLevels(readCLMap);
		configurableConsistencyLevel.setWriteCfConsistencyLevels(writeCLMap);
	}
	
	/**
	 * Encode a UUID from the given contextID
	 * 
	 * @param contextID
	 * @return the encoded UUID
	 */
	private UUID encodeUUID(int contextID) {
        return new UUID(contextID, 0);
	}
	
	private UUID getObjectUUID(int contextID, String folderID, int objectID, int module) {
		UUID contextUUID = encodeUUID(contextID);
		UUID objectUUID = null;
		
		Composite columnName = new Composite(folderID, Integer.toString(module), Integer.toString(objectID));
		
		/*Composite end = new Composite();
		end.addComponent(0, folderID, Composite.ComponentEquality.EQUAL);
		end.addComponent(1, module, Composite.ComponentEquality.EQUAL);
		end.addComponent(2, objectID, Composite.ComponentEquality.GREATER_THAN_EQUAL);*/
		System.out.println(contextUUID + " - " + columnName);
		SliceQuery<UUID, Composite, ByteBuffer> sliceQuery = HFactory.createSliceQuery(keyspace, us, cs, bbs);
		sliceQuery.setColumnFamily(CF_CONTEXT).setKey(contextUUID).setColumnNames(columnName);
		//sliceQuery.setRange(start, end, false, 1);
		ColumnSlice<Composite, ByteBuffer> slice = sliceQuery.execute().get();
		
		try {
			if (slice.getColumns().size() > 0) {
				objectUUID = UUID.fromString(ByteBufferUtil.string(slice.getColumns().get(0).getValue()));
			}
		} catch (CharacterCodingException e) {
			e.printStackTrace();
		}
		
		return objectUUID;
	}
	
	/**
	 * Returns a map with UUID-ObjectIDs for a specific folder
	 * @param contextID
	 * @param folderID
	 * @param module
	 * @return
	 */
	private Map<UUID, Integer> getObjectUUIDs(int contextID, String folderID, int module) {
		UUID contextUUID = encodeUUID(contextID);
		Map<UUID, Integer> map = new HashMap<UUID, Integer>();
		
		Composite start = new Composite();
		start.addComponent(0, folderID, Composite.ComponentEquality.EQUAL);
		start.addComponent(1, Integer.toString(module), Composite.ComponentEquality.EQUAL);
		
		Composite end = new Composite();
		end.addComponent(0, folderID, Composite.ComponentEquality.EQUAL);
		end.addComponent(1, Integer.toString(module), Composite.ComponentEquality.GREATER_THAN_EQUAL);
		
		SliceQuery<UUID, Composite, ByteBuffer> sliceQuery = HFactory.createSliceQuery(keyspace, us, cs, bbs);
		sliceQuery.setColumnFamily(CF_CONTEXT).setKey(contextUUID);
		sliceQuery.setRange(start, end, false, Integer.MAX_VALUE);
		Iterator<HColumn<Composite, ByteBuffer>> it = sliceQuery.execute().get().getColumns().iterator();
		
		while (it.hasNext()) {
			HColumn<Composite, ByteBuffer> hColumn = (HColumn<Composite, ByteBuffer>) it.next();
			try {
				String s = ByteBufferUtil.string(((ByteBuffer) hColumn.getName().get(2)));
				int objid = Integer.parseInt(s);
				UUID uuid = UUID.fromString(ByteBufferUtil.string(hColumn.getValue()));
				map.put(uuid, objid);
			} catch (CharacterCodingException e) {
				e.printStackTrace();
				System.out.println(((ByteBuffer) hColumn.getName().get(2)));
			}
		}
		
		return map;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.openexchange.eav.EAVStorage#getAttributes(int, java.lang.String, int)
	 */
	@Override
	public Map<String, Object> getAttributes(int contextID, String folderID, int objectID, int module) throws OXException {
		Map<String, Object> attr = null;
		try {
			UUID xtPropsKey = getObjectUUID(contextID, folderID, objectID, module);
			
			ColumnFamilyResult<UUID, String> result = xtPropsTemplate.queryColumns(xtPropsKey);
			if (result == null || !result.hasResults()) {
				log.error("No result");
			} else {
				attr = new HashMap<String, Object>();
				
				Iterator<String> it = result.getColumnNames().iterator();
				while (it.hasNext()) {
					String columnName = (String) it.next();
					ByteBuffer value = result.getColumn(columnName).getValue();
					attr.put(columnName, ByteBufferUtil.string(value));
				}
			}
		} catch (CharacterCodingException e) {
			e.printStackTrace();
		}
		
		return attr;
	}

	/*
	 * (non-Javadoc)
	 * @see com.openexchange.eav.EAVStorage#getAttributes(int, java.lang.String, int, java.lang.String[])
	 */
	@Override
	public Map<String, Object> getAttributes(int contextID, String folderID, int objectID, int module, String... attributes) throws OXException {
		Map<String, Object> attr = getAttributes(contextID, folderID, objectID, module);
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
	 * @see com.openexchange.eav.EAVStorage#getAttributes(int, java.lang.String, int)
	 */
	@Override
	public Map<Integer, Map<String, Object>> getAttributes(int contextID, String folderID, int module) throws OXException {
		Map<Integer, Map<String, Object>> attr = new HashMap<Integer, Map<String, Object>>();
		
		Map<UUID, Integer> map = getObjectUUIDs(contextID, folderID, module);
		Iterator<UUID> it = map.keySet().iterator();
		
		while (it.hasNext()) {
			UUID u = (UUID) it.next();
			
			Map<String, Object> singleObjectAttr = new HashMap<String, Object>();
			
			int objectID = map.get(u);
			singleObjectAttr = getAttributes(contextID, folderID, objectID, module);
			
			attr.put(objectID, singleObjectAttr);
		}
		
		return attr;
	}

	/*
	 * (non-Javadoc)
	 * @see com.openexchange.eav.EAVStorage#getAttributes(int, java.lang.String, int, java.lang.String[])
	 */
	@Override
	public Map<Integer, Map<String, Object>> getAttributes(int contextID, String folderID, int module, String... attributes) throws OXException {
		Map<Integer, Map<String, Object>> attr = getAttributes(contextID, folderID, module);
		Map<Integer, Map<String, Object>> retAttr = new HashMap<Integer, Map<String, Object>>(attributes.length);
		
		Iterator<Integer> it = attr.keySet().iterator();
		while (it.hasNext()) {
			Integer objID = (Integer) it.next();
			Map<String, Object> map = getAttributes(contextID, folderID, objID, module, attributes);
			retAttr.put(objID, map);
		}
		
		return retAttr;
	}

	/*
	 * (non-Javadoc)
	 * @see com.openexchange.eav.EAVStorage#getAttributes(int, java.lang.String, int, int[])
	 */
	@Override
	public Map<Integer, Map<String, Object>> getAttributes(int contextID, String folderID, int module, int[] objectIDs) throws OXException {
		Map<Integer, Map<String, Object>> retAttr = new HashMap<Integer, Map<String, Object>>(objectIDs.length);
		
		int i = 0;
		while(i < objectIDs.length) {
			Map<String, Object> map = getAttributes(contextID, folderID, objectIDs[i], module);
			retAttr.put(objectIDs[i], map);
			i++;
		}
		
		return retAttr;
	}

	/*
	 * (non-Javadoc)
	 * @see com.openexchange.eav.EAVStorage#getAttributes(int, java.lang.String, int[], int, java.lang.String[])
	 */
	@Override
	public Map<Integer, Map<String, Object>> getAttributes(int contextID, String folderID, int[] objectIDs, int module, String... attributes) throws OXException {
		Map<Integer, Map<String, Object>> retAttr = new HashMap<Integer, Map<String, Object>>(objectIDs.length);
		
		int i = 0;
		while(i < objectIDs.length) {
			Map<String, Object> map = getAttributes(contextID, folderID, objectIDs[i], module, attributes);
			retAttr.put(objectIDs[i], map);
			i++;
		}
		
		return retAttr;
	}

	/*
	 * (non-Javadoc)
	 * @see com.openexchange.eav.EAVStorage#hasAttributes(int, java.lang.String, int, int)
	 */
	@Override
	public boolean hasAttributes(int contextID, String folderID, int objectID, int module) throws OXException {
		return !(getObjectUUID(contextID, folderID, objectID, module) == null);
	}

	/*
	 * (non-Javadoc)
	 * @see com.openexchange.eav.EAVStorage#deleteAttributes(int, java.lang.String, int, int)
	 */
	@Override
	public void deleteAttributes(int contextID, String folderID, int objectID, int module) throws OXException {
		UUID xtPropsKey = getObjectUUID(contextID, folderID, objectID, module);
		//TODO: delete entry from CF_CONTEXT
		if (xtPropsKey != null) {
			Mutator<UUID> m = HFactory.createMutator(keyspace, us);
			m.addDeletion(xtPropsKey, CF_XT_PROPS);
			m.addDeletion(encodeUUID(contextID), CF_CONTEXT, new Composite(folderID, Integer.toString(module), Integer.toString(objectID)), cs);
			m.execute();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.openexchange.eav.EAVStorage#setAttributes(int, java.lang.String, int, java.util.Map, int)
	 */
	@Override
	public void setAttributes(int contextID, String folderID, int objectID, Map<String, Object> attributes, int module) throws OXException {
		UUID xtPropsKey = getObjectUUID(contextID, folderID, objectID, module);
		boolean exists = true;
		ColumnFamilyUpdater<UUID, Composite> contextUpdater = null;
		
		if (xtPropsKey == null) {
			xtPropsKey = UUID.randomUUID();
			exists = false;
			contextUpdater = contextTemplate.createUpdater(encodeUUID(contextID));
			contextUpdater.setString(new Composite(folderID, Integer.toString(module), Integer.toString(objectID)), xtPropsKey.toString());
		}
		
		ColumnFamilyUpdater<UUID, String> xtPropsUpdater = xtPropsTemplate.createUpdater(xtPropsKey);
		
		Iterator<String> it = attributes.keySet().iterator();
		while (it.hasNext()) {
			String columnName = (String) it.next();
			Object o = attributes.get(columnName);
			
			if (o == null) {
				xtPropsUpdater.deleteColumn(columnName);
			} else {
				
				if (o instanceof String) {
					xtPropsUpdater.setString(columnName, (String)o);
				} else if (o instanceof Integer) {
					xtPropsUpdater.setString(columnName, String.valueOf((Integer)o));
					//updater.setInteger(columnName, (Integer)o);
				} else if (o instanceof Long)
					xtPropsUpdater.setString(columnName, String.valueOf((Long)o));
					//updater.setLong(columnName, (Long)o);
				else if (o instanceof Double)
					xtPropsUpdater.setString(columnName, String.valueOf((Double)o));
					//updater.setDouble(columnName, (Double)o);
				else if (o instanceof Boolean)
					xtPropsUpdater.setString(columnName, String.valueOf((Boolean)o));
				else if (o instanceof Float)
					xtPropsUpdater.setString(columnName, String.valueOf((Float)o));
					//updater.setFloat(columnName, (Float)o);
				else if (o instanceof Date)
					xtPropsUpdater.setString(columnName, String.valueOf(((Date) o).getTime()));
					//updater.setLong(columnName, ((Date) o).getTime());
				else
					throw new OXException(666, "Unsupported attribute type. Data: " + o);
			}
		}
		
		try {
			xtPropsTemplate.update(xtPropsUpdater);
			if (!exists)
				contextTemplate.update(contextUpdater);
		} catch (HectorException h) {
			h.printStackTrace();
		}
	}
}