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

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import me.prettyprint.cassandra.serializers.ByteBufferSerializer;
import me.prettyprint.cassandra.serializers.CompositeSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.serializers.UUIDSerializer;
import me.prettyprint.cassandra.service.ColumnSliceIterator;
import me.prettyprint.cassandra.service.clock.MicrosecondsClockResolution;
import me.prettyprint.cassandra.service.template.ColumnFamilyResult;
import me.prettyprint.cassandra.service.template.ColumnFamilyTemplate;
import me.prettyprint.cassandra.service.template.ColumnFamilyUpdater;
import me.prettyprint.cassandra.service.template.ThriftColumnFamilyTemplate;
import me.prettyprint.cassandra.utils.TimeUUIDUtils;
import me.prettyprint.hector.api.ClockResolution;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.beans.Composite;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.OrderedRows;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.exceptions.HectorException;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.RangeSlicesQuery;
import me.prettyprint.hector.api.query.SliceQuery;
import me.prettyprint.hector.api.query.CounterQuery;

import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.loxandra.EAVContactService;
import com.openexchange.loxandra.dto.EAVContact;

/**
 * Cassandra Contact Data Access Object
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class CassandraEAVContactServiceImpl implements EAVContactService {
	
	/* CF names */
	private static String CF_PERSON = "Person";
	private static String CF_PERSON_FOLDER = "PersonFolder";
	private static String CF_COUNTERS = "Counters";
	
	private static final Log log = LogFactory.getLog(CassandraEAVContactServiceImpl.class);
	
	/* Some Serializers */
	private static final CompositeSerializer cs = CompositeSerializer.get();
	private static final StringSerializer ss = StringSerializer.get();
	private static final ByteBufferSerializer bbs = ByteBufferSerializer.get();
	private static final UUIDSerializer us = UUIDSerializer.get();
	//private static final LongSerializer ls = LongSerializer.get();
	
	/* Templates to access the column families */
	private ColumnFamilyTemplate<UUID, Composite> personTemplate;
	private ColumnFamilyTemplate<UUID, Composite> personFolderTemplate;
	
	//private EAVContactList<EAVContact> sortedContactList;
	
	//private int latestFromIndex, latestToIndex, 
	private int totalRows;
	private Composite startRange, lastEndRange;
	
	private Keyspace keyspace;
	
	/**
	 * Default Constructor. Initializes the CF templates.
	 */
	public CassandraEAVContactServiceImpl() {
		keyspace = CassandraEAVContactFactoryServiceImpl.getKeyspace();
		personTemplate  =  new ThriftColumnFamilyTemplate<UUID, Composite>(keyspace, CF_PERSON, us, cs);
		personFolderTemplate  =  new ThriftColumnFamilyTemplate<UUID, Composite>(keyspace, CF_PERSON_FOLDER, us, cs);
	}
	
	/*
	 * (non-Javadoc)
	 * @see loxandra.dao.ContactDAO#insertContact()
	 */
	@Override
	public void insertContact(EAVContact c) {
		c.setUUID(UUID.randomUUID());
		
		ClockResolution clock = new MicrosecondsClockResolution();
		c.setTimeUUID(TimeUUIDUtils.getTimeUUID(clock.createClock()));
		
		ColumnFamilyUpdater<UUID, Composite> personUpdater = personTemplate.createUpdater(c.getUUID()); //get Key and create updater object
	    populateUpdater(c, personUpdater);
	    
	    ColumnFamilyUpdater<UUID, Composite> personFolderUpdater = personFolderTemplate.createUpdater(c.getFolderUUIDs().get(0)); //get Key and create updater object
	    personFolderUpdater.setUUID(new Composite(c.getDisplayName(), c.getTimeUUID()), c.getUUID());
	    
		try {
	        /* ISSUE: Possible race condition issue. 
	         * If the contact that is inserted into the 'Person' CF gets deleted 
	         * BEFORE inserted into the 'PersonFolder' CF, we end up with stale data in the later CF.
	         * 
	         * A workaround(?) would be to read the 'Person' row right after the write in 'PersonFolder'
	         * in order to ensure consistency. If the row in 'Person' does not exist, then roll back.
	         * Dunno about that though... Looks like another race condition...
	         */
			personTemplate.update(personUpdater); 				// INSERT into Person
	        personFolderTemplate.update(personFolderUpdater);	// INSERT into PersonFolder
	        Mutator<String> m = HFactory.createMutator(keyspace, ss);
	        m.incrementCounter("PersonsInFolder", CF_COUNTERS, c.getFolderUUIDs().get(0), 1L);
	        
	        log.debug("Entity COMPOSITE " + c.getUUID());
	    } catch (HectorException e) {
	        e.printStackTrace();
	    }
	}
	
	/**
	 * Populate the updater for the insert
	 * 
	 * @param c
	 * @param updater
	 */
	private void populateUpdater(EAVContact c, ColumnFamilyUpdater<UUID, Composite> updater) {
		if (c.containsNickname())
			updater.setString(new Composite("named", "nickname"), c.getNickname());
		if (c.containsDisplayName())
			updater.setString(new Composite("named", "displayname"), c.getDisplayName());
		if (c.containsGivenName())
			updater.setString(new Composite("named", "givenname"), c.getGivenName());
		if (c.containsSurName())
			updater.setString(new Composite("named", "surname"), c.getSurName());
		//TODO: complete...
		
		updater.setUUID(new Composite("named", "timeuuid"), c.getTimeUUID());
		
		updater.setUUID(new Composite("folder", c.getFolderUUIDs().get(0)), c.getFolderUUIDs().get(0));
		
		Iterator<String> iterator  = c.getKeysIterator();
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			updater.setValue(new Composite("unnamed", key), c.getUnnamedProperty(key), ss);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see loxandra.dao.ContactDAO#getContact(int, boolean)
	 */
	@Override
	public EAVContact getContact(UUID uuid, boolean limited) {
		EAVContact c = new EAVContact();
		c.setUUID(uuid);
		
		try {
			ColumnFamilyResult<UUID, Composite> res = personTemplate.queryColumns(uuid);
			
			if (null == res || !res.hasResults()) {
				log.error("No result");
			} else {
				if (limited)
					populateDTOLimited(c);
				else
					populateDTO(c);
			}

		} catch (HectorException e) {
			e.printStackTrace();
		} catch (CharacterCodingException e) {
			e.printStackTrace();
		}
		
		return c;
	}
	
	/**
	 * Populate the limited details of a contact, ie. Name, Surname, DisplayName, email, timeUUID, telephone
	 * @param contact
	 * @throws CharacterCodingException 
	 */
	private void populateDTOLimited(EAVContact contact) throws CharacterCodingException {
		// get named props
		Composite start = new Composite();
		start.addComponent(0, "named", Composite.ComponentEquality.EQUAL);
		
		Composite end = new Composite();
		end.addComponent(0, "named", Composite.ComponentEquality.GREATER_THAN_EQUAL);
		
		SliceQuery<UUID, Composite, ByteBuffer> namedSliceQuery = HFactory.createSliceQuery(keyspace, us, cs, bbs);
		namedSliceQuery.setColumnFamily(CF_PERSON).setKey(contact.getUUID());
		ColumnSliceIterator<UUID, Composite, ByteBuffer> namedIterator = new ColumnSliceIterator<UUID, Composite, ByteBuffer>(namedSliceQuery, start, end, false);
		
		while (namedIterator.hasNext()) {
			HColumn<Composite, ByteBuffer> h = namedIterator.next();
			contact.addNamedProperty(ByteBufferUtil.string((ByteBuffer)h.getName().get(1)), h.getValue());
		}
		
		// setters
		if (contact.containsNamedProperty("displayname"))
			contact.setDisplayName(ByteBufferUtil.string(contact.getNamedProperty("displayname")));
		if (contact.containsNamedProperty("givenname"))
			contact.setGivenName(ByteBufferUtil.string(contact.getNamedProperty("givenname")));
		if (contact.containsNamedProperty("surname"))
			contact.setSurName(ByteBufferUtil.string(contact.getNamedProperty("surname")));
		if (contact.containsNamedProperty("email"))
			contact.setEmail1(ByteBufferUtil.string(contact.getNamedProperty("email")));
		
		contact.setTimeUUID(TimeUUIDUtils.uuid(contact.getNamedProperty("timeuuid")));
		
		contact.clearNamedProperties();
	}
	
	/**
	 * Populate the DTO with data from the query result.<br/><br/>
	 * Since the named properties could be Strings, Integers, 
	 * Floats etc., are stored as ByteBuffers, in contrast with the unnamed
	 * which are stores as Strings.
	 * 
	 * @param contact
	 * @param res
	 * @throws CharacterCodingException 
	 */
	private void populateDTO(EAVContact contact) throws CharacterCodingException {
		// get named props
		Composite start = new Composite();
		start.addComponent(0, "named", Composite.ComponentEquality.EQUAL);
		
		Composite end = new Composite();
		end.addComponent(0, "named", Composite.ComponentEquality.GREATER_THAN_EQUAL);
		
		SliceQuery<UUID, Composite, ByteBuffer> namedSliceQuery = HFactory.createSliceQuery(keyspace, us, cs, bbs);
		namedSliceQuery.setColumnFamily(CF_PERSON).setKey(contact.getUUID());
		ColumnSliceIterator<UUID, Composite, ByteBuffer> namedIterator = new ColumnSliceIterator<UUID, Composite, ByteBuffer>(namedSliceQuery, start, end, false);
		
		while (namedIterator.hasNext()) {
			HColumn<Composite, ByteBuffer> h = namedIterator.next();
			contact.addNamedProperty(ByteBufferUtil.string((ByteBuffer)h.getName().get(1)), h.getValue());
		}
		
		// setters
		if (contact.containsNamedProperty("nickname"))
			contact.setNickname(ByteBufferUtil.string(contact.getNamedProperty("nickname")));
		if (contact.containsNamedProperty("displayname"))
			contact.setDisplayName(ByteBufferUtil.string(contact.getNamedProperty("displayname")));
		if (contact.containsNamedProperty("givenname"))
			contact.setGivenName(ByteBufferUtil.string(contact.getNamedProperty("givenname")));
		if (contact.containsNamedProperty("surname"))
			contact.setSurName(ByteBufferUtil.string(contact.getNamedProperty("surname")));
		//TODO: complete...
		
		contact.setTimeUUID(TimeUUIDUtils.uuid(contact.getNamedProperty("timeuuid")));
		
		contact.clearNamedProperties();
		
		// get unnamed props
		start = new Composite();
		start.addComponent(0, "unnamed", Composite.ComponentEquality.EQUAL);
		
		end = new Composite();
		end.addComponent(0, "unnamed", Composite.ComponentEquality.GREATER_THAN_EQUAL);
		
		SliceQuery<UUID, Composite, String> unnamedSliceQuery = HFactory.createSliceQuery(keyspace, us, cs, ss);
		unnamedSliceQuery.setColumnFamily(CF_PERSON).setKey(contact.getUUID());
		ColumnSliceIterator<UUID, Composite, String> unnamedIterator = new ColumnSliceIterator<UUID, Composite, String>(unnamedSliceQuery, start, end, false);
		
		// setters
		while (unnamedIterator.hasNext()) {
			HColumn<Composite, String> h = unnamedIterator.next();
			contact.addUnnamedProperty(ByteBufferUtil.string((ByteBuffer)h.getName().get(1)), h.getValue());
		}
		
		// get folders
		start = new Composite();
		start.addComponent(0, "folder", Composite.ComponentEquality.EQUAL);
		
		end = new Composite();
		end.addComponent(0, "folder", Composite.ComponentEquality.GREATER_THAN_EQUAL);
		
		SliceQuery<UUID, Composite, UUID> folderSliceQuery = HFactory.createSliceQuery(keyspace, us, cs, us);
		folderSliceQuery.setColumnFamily(CF_PERSON).setKey(contact.getUUID());
		ColumnSliceIterator<UUID, Composite, UUID> folderIterator = new ColumnSliceIterator<UUID, Composite, UUID>(folderSliceQuery, start, end, false);
		
		// setters
		while (folderIterator.hasNext()) {
			HColumn<Composite, UUID> h = folderIterator.next();
			contact.addFolderUUID(h.getValue());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see loxandra.dao.ContactDAO#deleteContact(java.lang.String)
	 */
	@Override
	public void deleteContact(UUID uuid) {
		// get folders
		Composite start = new Composite();
		start.addComponent(0, "folder", Composite.ComponentEquality.EQUAL);
		
		Composite end = new Composite();
		end.addComponent(0, "folder", Composite.ComponentEquality.GREATER_THAN_EQUAL);
		
		SliceQuery<UUID, Composite, UUID> folderSliceQuery = HFactory.createSliceQuery(keyspace, us, cs, us);
		folderSliceQuery.setColumnFamily(CF_PERSON).setKey(uuid);
		ColumnSliceIterator<UUID, Composite, UUID> folderIterator = new ColumnSliceIterator<UUID, Composite, UUID>(folderSliceQuery, start, end, false);
		
		// setters
		while (folderIterator.hasNext()) {
			HColumn<Composite, UUID> h = folderIterator.next();
			removeContactFromFolder(getContact(uuid, true), h.getValue());
		}
		
		Mutator<UUID> m = HFactory.createMutator(keyspace, us);
		m.delete(uuid, CF_PERSON, null, ss);
	}

	/*
	 * (non-Javadoc)
	 * @see loxandra.dao.ContactDAO#updateContact(loxandra.dto.Contact)
	 */
	@Override
	public void updateContact(EAVContact c) {
		insertContact(c);
	}

	/*
	 * (non-Javadoc)
	 * @see loxandra.dao.ContactDAO#deleteProperties(java.lang.String, java.lang.String[])
	 */
	@Override
	public void deleteProperties(UUID uuid, String... prop) {
		Mutator<UUID> m = HFactory.createMutator(keyspace, us);
		for (String s : prop) { 
			 m.delete(uuid, CF_PERSON, new Composite("unnamed", s), cs);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see loxandra.dao.ContactDAO#getContactFromFolder(java.util.UUID)
	 */
	@Override
	public List<EAVContact> getContactsFromFolder(UUID folderUUID) {
		// !!!!DOES NOT WORK WITH COMPOSITE COLUMN NAMES!!!!
		List<EAVContact> list = new ArrayList<EAVContact>();
		
		SliceQuery<UUID, String, UUID> query = HFactory.createSliceQuery(keyspace, us, ss, us).setKey(folderUUID).setColumnFamily(CF_PERSON_FOLDER);
		ColumnSliceIterator<UUID, String, UUID> iterator = new ColumnSliceIterator<UUID, String, UUID>(query, null, "\uFFFF", false);
		
		while(iterator.hasNext()) {
			HColumn<String, UUID> h = iterator.next();
			list.add(getContact(h.getValue(), true));
		}
		
		log.info("Sorting list");
		long start = System.currentTimeMillis();
		Collections.sort(list);
		long end = System.currentTimeMillis();
		System.out.println("sorted in : " + (end - start) + " msec");
		
		return list;
	}

	/*
	 * (non-Javadoc)
	 * @see loxandra.dao.ContactDAO#getContactsFromFolder(java.util.UUID, int, int)
	 */
	@Override
	public List<EAVContact> getContactsFromFolder(UUID folderUUID, int from, int to) {
		/* ISSUE: Possible race condition issue.
		 * Priority: Low
		 * If a new contact is inserted after the call of this method, it will not be in the list.
		 */
		if (from == 0) {
			startRange = null;
			totalRows = getNumberOfContactsInFolder(folderUUID);
		} else
			startRange = lastEndRange;
		
		int numOfCols = (to - from) + 1;
		
		List<EAVContact> contactList = new ArrayList<EAVContact>();
		
		RangeSlicesQuery<UUID, Composite, UUID> rangeSlicesQuery = HFactory.createRangeSlicesQuery(keyspace, us, cs, us);
		rangeSlicesQuery.setColumnFamily(CF_PERSON_FOLDER)
						.setKeys(folderUUID, folderUUID)
						.setRange(startRange, null, false, numOfCols)
						.setRowCount(1);
		
		QueryResult<OrderedRows<UUID, Composite, UUID>> result = rangeSlicesQuery.execute();
		OrderedRows<UUID, Composite, UUID> orderedRows = result.get();
		List<Row<UUID, Composite, UUID>> listRows = orderedRows.getList();
		Row<UUID, Composite, UUID> row = listRows.get(0);
		
		ColumnSlice<Composite, UUID> colSlice = row.getColumnSlice();
		List<HColumn<Composite, UUID>> colList = colSlice.getColumns();
		
		for(int i = 0; i < colList.size() - 1; i++) {
			HColumn<Composite, UUID> type = colList.get(i);
			contactList.add(getContact(type.getValue(), true));
		}
		
		lastEndRange = colList.get(colList.size() - 1).getName();
		
		System.out.println("from: " + from + ", to: " + to + ", totalRows: " + totalRows + ", collistsize: " + colList.size() + ", numOfCols: " + numOfCols);
		
		if (from <= totalRows && to > totalRows)
			contactList.add(getContact(colList.get(colList.size() - 1).getValue(), true));
		
		return contactList;
	}

	/*
	 * (non-Javadoc)
	 * @see loxandra.dao.ContactDAO#copyContactToFolder(loxandra.dto.EAVContact, java.util.UUID, java.util.UUID)
	 */
	@Override
	public void copyContactToFolder(EAVContact c, UUID oldFolderUUID, UUID newFolderUUID) {
	 	if (!existsContactInFolder(c, newFolderUUID)) {
	 		ColumnFamilyUpdater<UUID, Composite> personFolderUpdater = personFolderTemplate.createUpdater(newFolderUUID); //get Key and create updater object
		    personFolderUpdater.setUUID(new Composite(c.getDisplayName(), c.getTimeUUID()), c.getUUID());
		    personFolderTemplate.update(personFolderUpdater);
		    
		    Composite compo = new Composite("folder", newFolderUUID);
		    Mutator<UUID> mInsert = HFactory.createMutator(keyspace, us);
		    mInsert.insert(c.getUUID(), CF_PERSON, HFactory.createColumn(compo, newFolderUUID));
		    
	 		Mutator<String> m = HFactory.createMutator(keyspace, ss);
	    	m.incrementCounter("PersonsInFolder", CF_COUNTERS, newFolderUUID, 1L);
	 	}
	}
	
	/*
	 * (non-Javadoc)
	 * @see loxandra.dao.ContactDAO#removeContactFromFolder(loxandra.dto.EAVContact, java.util.UUID)
	 */
	@Override
	public void removeContactFromFolder(EAVContact c, UUID folderUUID) {
		if (existsContactInFolder(c, folderUUID)) {
			Mutator<String> decrementMutator = HFactory.createMutator(keyspace, ss);
			decrementMutator.decrementCounter("PersonsInFolder", CF_COUNTERS, folderUUID, 1L);
			
			// Delete the person's uuid from the PersonFolder CF
			Composite delCompoPersonFolder = new Composite(c.getDisplayName(), c.getTimeUUID());
			Mutator<UUID> m = HFactory.createMutator(keyspace, us);
			m.delete(folderUUID, CF_PERSON_FOLDER, delCompoPersonFolder, cs);
			
			// Delete the uuid of the folder from the Person CF
			Composite delCompoPerson = new Composite("folder", folderUUID);
		    Mutator<UUID> mDelete = HFactory.createMutator(keyspace, us);
		    mDelete.delete(c.getUUID(), CF_PERSON, delCompoPerson, cs);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see loxandra.dao.ContactDAO#moveContactToFolder(loxandra.dto.EAVContact, java.util.UUID, java.util.UUID)
	 */
	@Override
	public void moveContactToFolder(EAVContact c, UUID oldFolderUUID, UUID newFolderUUID) {
		copyContactToFolder(c, oldFolderUUID, newFolderUUID);
		removeContactFromFolder(c, oldFolderUUID);
	}

	/*
	 * (non-Javadoc)
	 * @see loxandra.dao.ContactDAO#getNumberOfContactsInFolder(java.util.UUID)
	 */
	@Override
	public int getNumberOfContactsInFolder(UUID folderUUID) {
		CounterQuery<String, UUID> query = HFactory.createCounterColumnQuery(keyspace, ss, us);
		query.setColumnFamily(CF_COUNTERS).setKey("PersonsInFolder").setName(folderUUID);
		long counter = query.execute().get().getValue();
		
		return (int)counter;
	}
	
	/**
	 * Check whether a contact resides inside a particular folder
	 * 
	 * @param c
	 * @param folderUUID
	 * @return true if contact already exists in the folder
	 */
	private boolean existsContactInFolder(EAVContact c, UUID folderUUID) {
		Composite start = new Composite();
	 	start.addComponent(0, c.getDisplayName(), Composite.ComponentEquality.EQUAL);
	 	start.addComponent(1, c.getTimeUUID(), Composite.ComponentEquality.EQUAL);
	 		
	 	Composite end = new Composite();
	 	end.addComponent(0, c.getDisplayName(), Composite.ComponentEquality.EQUAL);
	 	end.addComponent(1, c.getTimeUUID(), Composite.ComponentEquality.EQUAL);
	 		
	 	SliceQuery<UUID, Composite, ByteBuffer> sliceQuery = HFactory.createSliceQuery(keyspace, us, cs, bbs);
	 	ColumnSlice<Composite, ByteBuffer> slice = sliceQuery.setColumnFamily(CF_PERSON_FOLDER)
	 														.setKey(folderUUID)
	 														.setRange(start, end, false, 1).execute().get();
	    
	    return (slice.getColumns().size() == 0 ? false : true);
	}
}