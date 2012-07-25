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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import me.prettyprint.cassandra.model.ConfigurableConsistencyLevel;
import me.prettyprint.cassandra.serializers.ByteBufferSerializer;
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
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.RangeSlicesQuery;
import me.prettyprint.hector.api.query.SliceQuery;

import org.apache.cassandra.db.KeyspaceNotDefinedException;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.eav.EAVStorage;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;

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
	
	private static final Map<Integer, String> typesMap;
	
	static {
		typesMap = new HashMap<Integer, String>();
		typesMap.put(Types.CONTACT, "ExtendedProperties");
	}
	
	private final ColumnFamilyTemplate<UUID, String> xtPropsTemplate;
	
	private static final StringSerializer ss = StringSerializer.get();
	private static final UUIDSerializer us = UUIDSerializer.get();
	private static final ByteBufferSerializer bbs = ByteBufferSerializer.get();

	private static ConfigurableConsistencyLevel configurableConsistencyLevel;
	
	public CassandraEAVStorageImpl() {
		initKeyspace();
		
		xtPropsTemplate = new ThriftColumnFamilyTemplate<UUID, String>(keyspace, typesMap.get(Types.CONTACT), us, ss);
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
		
		readCLMap.put("ExtendedProperties", HConsistencyLevel.ONE);
		
		writeCLMap.put("ExtendedProperties", HConsistencyLevel.ONE);
		
		configurableConsistencyLevel.setReadCfConsistencyLevels(readCLMap);
		configurableConsistencyLevel.setWriteCfConsistencyLevels(writeCLMap);
	}
	
	/**
	 * Decode the given uuid. The context ID is stored as the most significant bit.
	 * @param uuid to decode.
	 * @return the Context ID
	 */
	private int getContextIDFromUUID(UUID uuid) {
		return (int)uuid.getMostSignificantBits();
	}
	
	/**
	 * Decode the given uuid. The object ID is stored as the least significant bit.
	 * @param uuid to decode.
	 * @return the object ID
	 */
	private int getObjectIDFromUUID(UUID uuid) {
		return (int)uuid.getLeastSignificantBits();
	}
	
	/**
	 * Encode a UUID from the given contextID and objectID
	 * @param contextID is the most significant bit
	 * @param objectID is the least significant bit
	 * @return the encoded UUID
	 */
	private UUID encodeUUID(int contextID, int objectID) {
		return new UUID(contextID, objectID);
	}
	
	/**
	 * Creates a {@link RangeSlicesQuery},
	 * @param rangeSliceWrapper
	 * @return
	 */
	private RangeSlicesQuery<UUID, String, ByteBuffer> createRangeSlicesQuery(RangeSliceWrapper rangeSliceWrapper) {
		RangeSlicesQuery<UUID, String, ByteBuffer> rangeSlice = HFactory.createRangeSlicesQuery(keyspace, us, ss, bbs);
		
		if (rangeSliceWrapper.hasContextID)
			rangeSlice.addEqualsExpression("contextID", ByteBufferUtil.bytes(rangeSliceWrapper.getContextID()));
		if (rangeSliceWrapper.hasFolderID)
			rangeSlice.addEqualsExpression("folderID", ByteBufferUtil.bytes(rangeSliceWrapper.getFolderID()));
		if (rangeSliceWrapper.hasObjectID)
			rangeSlice.addEqualsExpression("objectID", ByteBufferUtil.bytes(rangeSliceWrapper.getObjectID()));
		if (rangeSliceWrapper.hasModuleID)
			rangeSlice.addEqualsExpression("moduleID", ByteBufferUtil.bytes(rangeSliceWrapper.getModuleID()));

		rangeSlice.setColumnFamily(typesMap.get(rangeSliceWrapper.getModuleID()));
		rangeSlice.setRange(null, null, false, Integer.MAX_VALUE);
		
		return rangeSlice;
	}

	/*
	 * (non-Javadoc)
	 * @see com.openexchange.eav.EAVStorage#getAttributes(int, java.lang.String, int)
	 */
	@Override
	public Map<String, Object> getAttributes(int contextID, String folderID, int objectID, int module) throws OXException {
		Map<String, Object> attr = new HashMap<String, Object>();
		
		RangeSliceWrapper rs = new RangeSliceWrapper(contextID, folderID, objectID, module);
		RangeSlicesQuery<UUID, String, ByteBuffer> slice = createRangeSlicesQuery(rs);
		
		Iterator<Row<UUID, String, ByteBuffer>> it = slice.execute().get().iterator();
		
		while(it.hasNext()) {
			Iterator<HColumn<String, ByteBuffer>> sliceIter = it.next().getColumnSlice().getColumns().iterator();
			
			while (sliceIter.hasNext()) {
				HColumn<String, ByteBuffer> column = sliceIter.next();
				try {
					attr.put(column.getName(), ByteBufferUtil.string(column.getValue()));
				} catch (CharacterCodingException e) {
					e.printStackTrace();
				}
			}
		}
		
		/*ColumnFamilyResult<UUID, String> result = xtPropsTemplate.queryColumns(encodeUUID(contextID, objectID));
		if (result == null || !result.hasResults()) {
			log.error("No result");
		} else {
			attr = new HashMap<String, Object>();
			
			Iterator<String> it = result.getColumnNames().iterator();
			while (it.hasNext()) {
				String columnName = (String) it.next();
				ByteBuffer value = result.getColumn(columnName).getValue();
				
				try {
					attr.put(columnName, ByteBufferUtil.string(value));
				} catch (CharacterCodingException e) {
					e.printStackTrace();
				}
			}
		}*/
		
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
		
		RangeSliceWrapper rs = new RangeSliceWrapper(contextID, folderID, 0, module);
		RangeSlicesQuery<UUID, String, ByteBuffer> slice = createRangeSlicesQuery(rs);
		
		Iterator<Row<UUID, String, ByteBuffer>> it = slice.execute().get().iterator();
		while (it.hasNext()) {
			Row<UUID, String, ByteBuffer> row = (Row<UUID, String, ByteBuffer>) it.next();
			ColumnSlice<String, ByteBuffer> columnSlice = row.getColumnSlice();
			
			Map<String, Object> singleObjectAttr = new HashMap<String, Object>();
			
			int objectID = ByteBufferUtil.toInt(columnSlice.getColumnByName("objectID").getValue());
			singleObjectAttr = getAttributes(contextID, folderID, objectID, module);
			
			attr.put(ByteBufferUtil.toInt(columnSlice.getColumnByName("objectID").getValue()), singleObjectAttr);
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
		RangeSliceWrapper rs = new RangeSliceWrapper(contextID, folderID, objectID, module);
		RangeSlicesQuery<UUID, String, ByteBuffer> slice = createRangeSlicesQuery(rs);

		slice.setRange(null, null, false, 1);
		
		return !(slice.execute().get().peekLast() == null);
	}

	/*
	 * (non-Javadoc)
	 * @see com.openexchange.eav.EAVStorage#deleteAttributes(int, java.lang.String, int, int)
	 */
	@Override
	public void deleteAttributes(int contextID, String folderID, int objectID, int module) throws OXException {
		RangeSliceWrapper rs = new RangeSliceWrapper(contextID, folderID, objectID, module);
		RangeSlicesQuery<UUID, String, ByteBuffer> slice = createRangeSlicesQuery(rs);
		Row<UUID, String, ByteBuffer> r = slice.execute().get().getList().get(0);
		UUID key = r.getKey();
		
		if (key != null) {
			Mutator<UUID> m = HFactory.createMutator(keyspace, us);
			m.addDeletion(key, typesMap.get(module));
			m.execute();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.openexchange.eav.EAVStorage#setAttributes(int, java.lang.String, int, java.util.Map, int)
	 */
	@Override
	public void setAttributes(int contextID, String folderID, int objectID, Map<String, Object> attributes, int module) throws OXException {
		//long startTime = System.currentTimeMillis();
		
		RangeSliceWrapper rs = new RangeSliceWrapper(contextID, folderID, objectID, module);
		RangeSlicesQuery<UUID, String, ByteBuffer> slice = createRangeSlicesQuery(rs);
		
		UUID key;
		if (slice.execute().get().peekLast() == null)
			key = UUID.randomUUID();
		else
			key = slice.execute().get().peekLast().getKey();
		
		ColumnFamilyUpdater<UUID, String> updater = xtPropsTemplate.createUpdater(key);
		
		Iterator<String> it = attributes.keySet().iterator();
		while (it.hasNext()) {
			String columnName = (String) it.next();
			updater.setString(columnName, (String)attributes.get(columnName));
		}
		
		updater.setInteger("contextID", contextID);
		updater.setString("folderID", folderID);
		updater.setInteger("objectID", objectID);
		updater.setInteger("moduleID", module);
		
		xtPropsTemplate.update(updater);
		
		//long endTime = System.currentTimeMillis();
		//System.out.println("runtime: " + (endTime - startTime) + " msec.");
	}
	
	private class RangeSliceWrapper {
		private int contextID;
		private int objectID;
		private int moduleID;
		private String folderID;
		
		private boolean hasContextID;
		private boolean hasObjectID;
		private boolean hasModuleID;
		private boolean hasFolderID;
		
		protected RangeSliceWrapper(int contextID, String folderID, int objectID, int moduleID) {
			if (contextID != 0)
				setContextID(contextID);
			if (folderID != null)
				setFolderID(folderID);
			if (objectID != 0)
				setObjectID(objectID);
			if (moduleID != 0)
				setModuleID(moduleID);
		}
		
		/**
		 * @return the contextID
		 */
		public int getContextID() {
			return contextID;
		}
		/**
		 * @param contextID the contextID to set
		 */
		public void setContextID(int contextID) {
			this.contextID = contextID;
			hasContextID = true;
		}
		/**
		 * @return the objectID
		 */
		public int getObjectID() {
			return objectID;
		}
		/**
		 * @param objectID the objectID to set
		 */
		public void setObjectID(int objectID) {
			this.objectID = objectID;
			hasObjectID = true;
		}
		/**
		 * @return the moduleID
		 */
		public int getModuleID() {
			return moduleID;
		}
		/**
		 * @param moduleID the moduleID to set
		 */
		public void setModuleID(int moduleID) {
			this.moduleID = moduleID;
			hasModuleID = true;
		}
		/**
		 * @return the folderID
		 */
		public String getFolderID() {
			return folderID;
		}
		/**
		 * @param folderID the folderID to set
		 */
		public void setFolderID(String folderID) {
			this.folderID = folderID;
			hasFolderID = true;
		}
	}
}