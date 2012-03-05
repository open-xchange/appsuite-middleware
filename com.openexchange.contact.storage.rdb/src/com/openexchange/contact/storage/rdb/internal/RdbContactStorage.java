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

package com.openexchange.contact.storage.rdb.internal;

import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.openexchange.contact.storage.DefaultContactStorage;
import com.openexchange.contact.storage.rdb.fields.Fields;
import com.openexchange.contact.storage.rdb.fields.QueryFields;
import com.openexchange.contact.storage.rdb.mapping.Mappers;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.search.SearchTerm;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link RdbContactStorage} - Database storage for contacts.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class RdbContactStorage extends DefaultContactStorage {
    
    private final Executor executor;
    
    /**
     * Initializes a new {@link RdbContactStorage}.
     */
    public RdbContactStorage() {
        this.executor = new Executor();
    }
    
    @Override
    public boolean supports(final int contextID, final String folderId) throws OXException {
        return true;
    }

    @Override
    public Contact get(final int contextID, final String folderId, final String id, final ContactField[] fields) throws OXException {
        final int objectID = parse(id);
        final DatabaseService databaseService = getDatabaseService();
        final Connection connection = databaseService.getReadOnly(contextID);
        try {
            /*
             * check fields
             */
            final QueryFields queryFields = new QueryFields(fields);
            if (false == queryFields.hasContactData()) {
                return null; // nothing to do
            }
            /*
             * get contact data
             */        
            Contact contact = executor.selectSingle(connection, Table.CONTACTS, contextID, objectID, queryFields.getContactDataFields());
            if (null == contact) {
                throw ContactExceptionCodes.CONTACT_NOT_FOUND.create(objectID, contextID);
            }
            contact.setObjectID(objectID);
            contact.setContextId(contextID);
            /*
             * merge image data if needed
             */
            if (queryFields.hasImageData() && 0 < contact.getNumberOfImages()) {
                final Contact imageData = executor.selectSingle(connection, Table.IMAGES, contextID, objectID, 
                    queryFields.getImageDataFields());
                if (null != imageData) {
                    contact = super.merge(contact, imageData);
                }
            }
            /*
             * merge distribution list data if needed
             */
            if (queryFields.hasDistListData() && 0 < contact.getNumberOfDistributionLists()) {
                contact.setDistributionList(executor.select(connection, Table.DISTLIST, contextID, objectID, Fields.DISTLIST_DATABASE_ARRAY));
            }            
            return contact;
        } catch (final SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create();
        } finally {
            databaseService.backReadOnly(contextID, connection);
        }
    }
    
    @Override
    public void create(final int contextID, final String folderId, final Contact contact) throws OXException {
        final DatabaseService databaseService = getDatabaseService();
        final Connection connection = databaseService.getWritable(contextID);
        try {
            /*
             * prepare insert
             */
            connection.setAutoCommit(false);
            contact.setObjectID(IDGenerator.getId(contextID, com.openexchange.groupware.Types.CONTACT, connection));
            final Date now = new Date();
            contact.setLastModified(now);
            contact.setCreationDate(now);
            contact.setParentFolderID(parse(folderId));
            contact.setContextId(contextID);
            /*
             * insert image data if needed
             */
            if (contact.containsImage1() && null != contact.getImage1()) {
                contact.setImageLastModified(now);
                this.executor.insert(connection, Table.IMAGES, contact, Fields.IMAGE_DATABASE_ARRAY);
            } 
            /*
             * insert contact
             */
            this.executor.insert(connection, Table.CONTACTS, contact, Fields.CONTACT_DATABASE_ARRAY);
            /*
             * insert distribution list data if needed
             */
            if (contact.containsDistributionLists()) {
                this.executor.insert(connection, Table.DISTLIST, getMembers(contact.getDistributionList(), contextID, contact.getObjectID()), Fields.DISTLIST_DATABASE_ARRAY);
            }
            /*
             * commit
             */
            connection.commit();
        } catch (final DataTruncation e) {
            DBUtils.rollback(connection);
            throw Tools.truncation(connection, e, contact, Table.CONTACTS);
        } catch (final SQLException e) {
            DBUtils.rollback(connection);
            throw ContactExceptionCodes.SQL_PROBLEM.create(e);
        } catch (final OXException e) {
            DBUtils.rollback(connection);
            throw e;
        } finally {
            DBUtils.autocommit(connection);
            databaseService.backWritable(contextID, connection);
        }
    }
        
    @Override
    public void delete(final int contextID, final String folderId, final String id, final Date lastRead) throws OXException {
        final int objectID = parse(id);
        final long minLastModified = lastRead.getTime();
        final DatabaseService databaseService = getDatabaseService();
        final Connection connection = databaseService.getWritable(contextID);
        try {
            connection.setAutoCommit(false);
            /*
             * ensure there is no previous record in the 'deleted' tables
             */
            executor.delete(connection, Table.DELETED_CONTACTS, contextID, objectID, minLastModified);
            executor.delete(connection, Table.DELETED_IMAGES, contextID, objectID, minLastModified);
            executor.delete(connection, Table.DELETED_DISTLIST, contextID, objectID);
            /*
             * insert copied records to 'deleted' tables 
             */
            if (0 == executor.insertFrom(connection, Table.CONTACTS, Table.DELETED_CONTACTS, contextID, objectID, minLastModified)) {
                throw ContactExceptionCodes.CONTACT_NOT_FOUND.create(objectID, contextID);
            }
            executor.insertFrom(connection, Table.IMAGES, Table.DELETED_IMAGES, contextID, objectID, minLastModified);
            executor.insertFrom(connection, Table.DISTLIST, Table.DELETED_DISTLIST, contextID, objectID);
            /*
             * delete records in original tables
             */
            if (0 == executor.delete(connection, Table.CONTACTS, contextID, objectID, minLastModified)) {
                throw ContactExceptionCodes.CONTACT_NOT_FOUND.create(objectID, contextID);
            }
            executor.delete(connection, Table.IMAGES, contextID, objectID, minLastModified);
            executor.delete(connection, Table.DISTLIST, contextID, objectID);
            /*
             * update meta data
             */
            final Contact contact = new Contact();
            contact.setContextId(contextID);
            contact.setObjectID(objectID);
            contact.setLastModified(new Date());
            contact.setParentFolderID(parse(folderId));
            executor.update(connection, Table.CONTACTS, minLastModified, contact, new ContactField[] 
                { ContactField.MODIFIED_BY, ContactField.LAST_MODIFIED });
            /*
             * commit
             */
            connection.commit();
        } catch (final SQLException e) {
            DBUtils.rollback(connection);
            throw ContactExceptionCodes.SQL_PROBLEM.create(e);
        } catch (final OXException e) {
            DBUtils.rollback(connection);
            throw e;
        } finally {
            DBUtils.autocommit(connection);
            databaseService.backWritable(contextID, connection);
        }
    }
    
    @Override
    public void update(final int contextID, final String folderId, final Contact contact, final Date lastRead) throws OXException {
        final int objectID = contact.getObjectID();
        final long minLastModified = lastRead.getTime();
        final DatabaseService databaseService = getDatabaseService();
        final Connection connection = databaseService.getWritable(contextID);
        try {
            /*
             * prepare insert
             */
            connection.setAutoCommit(false);
            final Date now = new Date();
            contact.setLastModified(now);
            final QueryFields queryFields = new QueryFields(super.getAssignedFields(contact));
            /*
             * update image data if needed
             */
            if (queryFields.hasImageData()) {
                contact.setImageLastModified(now);
                queryFields.update(super.getAssignedFields(contact));
                if (null == contact.getImage1()) {
                    // delete previous image if exists
                    executor.delete(connection, Table.IMAGES, contextID, objectID, now.getTime());
                } else {
                    if (null != executor.selectSingle(connection, Table.IMAGES, contextID, objectID, new ContactField[] { 
                    		ContactField.OBJECT_ID })) {
                        // update previous image
                        if (0 == executor.update(connection, Table.IMAGES, now.getTime(), contact, queryFields.getImageDataFields())) {
                            throw ContactExceptionCodes.OBJECT_HAS_CHANGED.create(contextID, objectID);
                        }
                    } else {
                        // create new image
                        this.executor.insert(connection, Table.CONTACTS, contact, Fields.IMAGE_DATABASE_ARRAY);                        
                    }                    
                }
            }
            /*
             * update contact data
             */
            if (0 == executor.update(connection, Table.CONTACTS, minLastModified, contact, queryFields.getContactDataFields())) {
                //TODO: check imagelastmodified also?
                throw ContactExceptionCodes.OBJECT_HAS_CHANGED.create(contextID, objectID);
            }
            /*
             * update distlist data if needed
             */
            if (queryFields.hasDistListData()) {
                //TODO: this is lazy compared to the old implementation
                // delete any previous entries
                executor.delete(connection, Table.DISTLIST, contextID, objectID);
                if (0 < contact.getNumberOfDistributionLists() && null != contact.getDistributionList()) {
                    // insert distribution list entries
                    executor.insert(connection, Table.DISTLIST, getMembers(contact.getDistributionList(), objectID, contextID), Fields.DISTLIST_DATABASE_ARRAY);
                }
            }
            /*
             * commit
             */
            connection.commit();
        } catch (final SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create();
        } finally {
            DBUtils.autocommit(connection);
            databaseService.backWritable(contextID, connection);
        }
    }
    
    private static DistListMember[] getMembers(final DistributionListEntryObject[] distList, final int contextID, final int parentContactID) throws OXException {
    	if (null != distList) {
    		final DistListMember[] members = new DistListMember[distList.length];
    		for (int i = 0; i < members.length; i++) {
				members[i] = DistListMember.create(distList[i], contextID, parentContactID);
			}
        	return members;
    	}
    	return null;
    }
    
    @Override
    public Collection<Contact> deleted(final int contextID, final String folderId, final Date since, final ContactField[] fields) throws OXException {
        final long minLastModified = since.getTime();
        final int parentFolderID = parse(folderId);
        final DatabaseService databaseService = getDatabaseService();
        final Connection connection = databaseService.getReadOnly(contextID);
        try {
            /*
             * get contact data
             */        
            Collection<Contact> contacts = null;
            final ContactField[] contactDataFields = Mappers.CONTACT.filter(fields, Fields.CONTACT_DATABASE, ContactField.OBJECT_ID).toArray(new ContactField[0]);
            if (null != contactDataFields && 0 < contactDataFields.length) {
                contacts = executor.select(connection, Table.DELETED_CONTACTS, contextID, parentFolderID, minLastModified, contactDataFields);
            }
            /*
             * merge image data if needed
             */
            //TODO necessary in deleted call?
            /*
             * merge distribution list data if needed
             */
            //TODO necessary in deleted call?
            return contacts;
        } catch (final SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create();
        } finally {
            databaseService.backReadOnly(contextID, connection);
        }
    }

    @Override
    public <O> Collection<Contact> search(final int contextID, final SearchTerm<O> term, final ContactField[] fields) throws OXException {
        //TODO move ContactSearchtermSqlConverter to this bundle
        return null;
    }
    
    @Override
    public Collection<Contact> all(final int contextID, final String folderId, final ContactField[] fields) throws OXException {
        final int parentFolderID = parse(folderId);
        final DatabaseService databaseService = getDatabaseService();
        final Connection connection = databaseService.getReadOnly(contextID);
        try {
            /*
             * check fields
             */
            final QueryFields queryFields = new QueryFields(fields);
            if (false == queryFields.hasContactData()) {
                return null; // nothing to do
            }
            /*
             * get contact data
             */        
            List<Contact> contacts = executor.select(connection, Table.CONTACTS, contextID, parentFolderID, queryFields.getContactDataFields());
            if (null != contacts && 0 < contacts.size()) {
                /*
                 * merge image data if needed
                 */
                if (queryFields.hasImageData()) {
                    contacts = mergeImageData(connection, Table.IMAGES, contextID, contacts, queryFields.getImageDataFields());
                }
                /*
                 * merge distribution list data if needed
                 */
                if (queryFields.hasDistListData()) {
                    contacts = mergeDistListData(connection, Table.DISTLIST, contextID, contacts);
                }
            }
            return contacts;
        } catch (final SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create();
        } finally {
            databaseService.backReadOnly(contextID, connection);
        }
    }
    
    @Override
    public Collection<Contact> list(final int contextID, final String folderId, final String[] ids, final ContactField[] fields) throws OXException {
        final DatabaseService databaseService = getDatabaseService();
        final Connection connection = databaseService.getReadOnly(contextID);
        try {
            /*
             * check fields
             */
            final QueryFields queryFields = new QueryFields(fields);
            if (false == queryFields.hasContactData()) {
                return null; // nothing to do
            }
            /*
             * get contact data
             */        
            List<Contact> contacts = executor.select(connection, Table.CONTACTS, contextID, parse(ids), queryFields.getContactDataFields());
            if (null != contacts && 0 < contacts.size()) {
                /*
                 * merge image data if needed
                 */
                if (queryFields.hasImageData()) {
                    contacts = mergeImageData(connection, Table.IMAGES, contextID, contacts, queryFields.getImageDataFields());
                }
                /*
                 * merge distribution list data if needed
                 */
                if (queryFields.hasDistListData()) {
                    contacts = mergeDistListData(connection, Table.DISTLIST, contextID, contacts);
                }
            }
            return contacts;
        } catch (final SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create();
        } finally {
            databaseService.backReadOnly(contextID, connection);
        }
    }

    private List<Contact> mergeDistListData(final Connection connection, final Table table, final int contextID, final List<Contact> contacts) throws SQLException, OXException {
        final int[] objectIDs = getObjectIDsWithDistLists(contacts);
        if (null != objectIDs && 0 < objectIDs.length) {
            final Map<Integer, List<DistListMember>> distListData = executor.select(connection, table, contextID, objectIDs, Fields.DISTLIST_DATABASE_ARRAY);
            for (final Contact contact : contacts) {
                final List<DistListMember> distList = distListData.get(Integer.valueOf(contact.getObjectID()));
                if (null != distList) {
                    contact.setDistributionList(distList.toArray(new DistListMember[distList.size()]));
                }
            }
        }
        return contacts;
    }

    private List<Contact> mergeImageData(final Connection connection, final Table table, final int contextID, final List<Contact> contacts, final ContactField[] fields) throws SQLException, OXException {
        final int[] objectIDs = getObjectIDsWithImages(contacts);
        if (null != objectIDs && 0 < objectIDs.length) {
            final List<Contact> imagaDataList = executor.select(connection, table, contextID, objectIDs, fields);
            if (null != imagaDataList && 0 < imagaDataList.size()) {
                return mergeByID(contacts, imagaDataList);
            }
        }
        return contacts;
    }    

    private int[] getObjectIDsWithImages(final List<Contact> contacts) {
        int i = 0;
        final int[] objectIDs = new int[contacts.size()];
        for (final Contact contact : contacts) {
            if (0 < contact.getNumberOfImages()) {
                objectIDs[i++] = contact.getObjectID();
            }
        }
        return Arrays.copyOf(objectIDs, i);
    }
    
    private int[] getObjectIDsWithDistLists(final List<Contact> contacts) {
        int i = 0;
        final int[] objectIDs = new int[contacts.size()];
        for (final Contact contact : contacts) {
            if (0 < contact.getNumberOfDistributionLists()) {
                objectIDs[i++] = contact.getObjectID();
            }
        }
        return Arrays.copyOf(objectIDs, i);
    }
    
    private static DatabaseService getDatabaseService() throws OXException {
        return RdbServiceLookup.getService(DatabaseService.class, true);
    }
    
    private static int parse(final String id) throws OXException {
        try {
            return Integer.parseInt(id);
        } catch (final NumberFormatException e) {
            throw new OXException(e);
        }
    }
    
    private static int[] parse(final String[] ids) throws OXException {
        try {
            final int[] intIDs = new int[ids.length];
            for (int i = 0; i < intIDs.length; i++) {
                intIDs[i] = Integer.parseInt(ids[i]);
            }
            return intIDs;
        } catch (final NumberFormatException e) {
            throw new OXException(e);
        }
    }
    
}
