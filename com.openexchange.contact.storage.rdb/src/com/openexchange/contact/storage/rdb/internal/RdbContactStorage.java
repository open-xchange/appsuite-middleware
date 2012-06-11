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

import static com.openexchange.contact.storage.rdb.internal.Tools.parse;

import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.openexchange.contact.SortOptions;
import com.openexchange.contact.storage.DefaultContactStorage;
import com.openexchange.contact.storage.rdb.fields.DistListMemberField;
import com.openexchange.contact.storage.rdb.fields.Fields;
import com.openexchange.contact.storage.rdb.fields.QueryFields;
import com.openexchange.contact.storage.rdb.mapping.Mappers;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.log.LogFactory;
import com.openexchange.search.SearchTerm;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link RdbContactStorage} - Database storage for contacts.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class RdbContactStorage extends DefaultContactStorage {
    
    private static Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(RdbContactStorage.class));
    private static boolean PREFETCH_ATTACHMENT_INFO = true;

    private final Executor executor;
    
    /**
     * Initializes a new {@link RdbContactStorage}.
     */
    public RdbContactStorage() {
    	super();
        this.executor = new Executor();
      	LOG.debug("RdbContactStorage initialized.");
    }
    
    @Override
    public boolean supports(int contextID, String folderId) throws OXException {
        return true;
    }

    @Override
    public Contact get(int contextID, String folderId, String id, ContactField[] fields) throws OXException {
        int objectID = parse(id);
        DatabaseService databaseService = getDatabaseService();
        Connection connection = databaseService.getReadOnly(contextID);
        try {
            /*
             * check fields
             */
            QueryFields queryFields = new QueryFields(fields);
            if (false == queryFields.hasContactData()) {
                return null; // nothing to do
            }
            /*
             * get contact data
             */        
            Contact contact = executor.selectSingle(connection, Table.CONTACTS, contextID, objectID, queryFields.getContactDataFields());
            if (null == contact) {
                throw ContactExceptionCodes.CONTACT_NOT_FOUND.create(Integer.valueOf(objectID), Integer.valueOf(contextID));
            }
            contact.setObjectID(objectID);
            contact.setContextId(contextID);
            /*
             * merge image data if needed
             */
            if (queryFields.hasImageData() && 0 < contact.getNumberOfImages()) {
                Contact imageData = executor.selectSingle(connection, Table.IMAGES, contextID, objectID, queryFields.getImageDataFields());
                if (null != imageData) {
                	Mappers.CONTACT.mergeDifferences(contact, imageData);
                }
            }
            /*
             * merge distribution list data if needed
             */
            if (queryFields.hasDistListData() && 0 < contact.getNumberOfDistributionLists()) {
                contact.setDistributionList(executor.select(connection, Table.DISTLIST, contextID, objectID, 
                		Fields.DISTLIST_DATABASE_ARRAY));
            }
            /*
             * add attachment information in advance if needed
             */
            //TODO: at this stage, we break the storage separation, since we assume that attachments are stored in the same database
            if (PREFETCH_ATTACHMENT_INFO && queryFields.hasAttachmentData() && 0 < contact.getNumberOfAttachments()) {
            	contact.setLastModifiedOfNewestAttachment(executor.selectNewestAttachmentDate(connection, contextID, objectID));            	
            }
            return contact;
        } catch (SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            databaseService.backReadOnly(contextID, connection);
        }
    }
    
    @Override
    public void create(int contextID, String folderId, Contact contact) throws OXException {
    	boolean committed = false;
        DatabaseService databaseService = getDatabaseService();
        Connection connection = databaseService.getWritable(contextID);
        try {
            /*
             * prepare insert
             */
            connection.setAutoCommit(false);
            contact.setObjectID(IDGenerator.getId(contextID, com.openexchange.groupware.Types.CONTACT, connection));
            Date now = new Date();
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
            	DistListMember[] members = DistListMember.create(contact.getDistributionList(), contextID, contact.getObjectID());
                this.executor.insert(connection, Table.DISTLIST, members, Fields.DISTLIST_DATABASE_ARRAY);
            }
            /*
             * commit
             */
            connection.commit();
        	committed = true;
        } catch (DataTruncation e) {
            DBUtils.rollback(connection);
            throw Tools.getTruncationException(connection, e, contact, Table.CONTACTS);
        } catch (SQLException e) {
            DBUtils.rollback(connection);
            throw ContactExceptionCodes.SQL_PROBLEM.create(e);
        } catch (OXException e) {
            DBUtils.rollback(connection);
            throw e;
        } finally {
        	if (false == committed) {
        		DBUtils.rollback(connection);
        	}
            DBUtils.autocommit(connection);
            databaseService.backWritable(contextID, connection);
        }
    }    
        
    @Override
    public void delete(int contextID, int userID, String folderId, String id, Date lastRead) throws OXException {
    	boolean committed = false;
        int objectID = parse(id);
        long minLastModified = lastRead.getTime();
        DatabaseService databaseService = getDatabaseService();
        Connection connection = databaseService.getWritable(contextID);
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
            Contact contact = new Contact();
            contact.setLastModified(new Date());
            contact.setModifiedBy(userID);
            executor.update(connection, Table.DELETED_CONTACTS, contextID, objectID, minLastModified, contact, new ContactField[] { 
            		ContactField.MODIFIED_BY, ContactField.LAST_MODIFIED });
            /*
             * commit
             */
            connection.commit();
        	committed = true;
        } catch (SQLException e) {
            DBUtils.rollback(connection);
            throw ContactExceptionCodes.SQL_PROBLEM.create(e);
        } catch (OXException e) {
            DBUtils.rollback(connection);
            throw e;
        } finally {
        	if (false == committed) {
        		DBUtils.rollback(connection);
        	}
            DBUtils.autocommit(connection);
            databaseService.backWritable(contextID, connection);
        }
    }
    
    @Override
    public void update(int contextID, String folderId, String id, Contact contact, Date lastRead) 
    		throws OXException {
    	boolean committed = false;
        int objectID = parse(id);
        long minLastModified = lastRead.getTime();
        DatabaseService databaseService = getDatabaseService();
        Connection connection = databaseService.getWritable(contextID);
        try {
            /*
             * prepare insert
             */
            connection.setAutoCommit(false);
            Date now = new Date();
            contact.setLastModified(now);
            QueryFields queryFields = new QueryFields(Mappers.CONTACT.getAssignedFields(contact));
            /*
             * insert copied record to 'deleted' table when parent folder changes
             */
            if (contact.containsParentFolderID() && false == Integer.toString(contact.getParentFolderID()).equals(folderId)) {
	            executor.delete(connection, Table.DELETED_CONTACTS, contextID, objectID, minLastModified);
	            if (0 == executor.insertFrom(connection, Table.CONTACTS, Table.DELETED_CONTACTS, contextID, objectID, minLastModified)) {
	                throw ContactExceptionCodes.CONTACT_NOT_FOUND.create(objectID, contextID);
	            }
	            Contact deletedContact = new Contact();
	            deletedContact.setLastModified(now);
	            deletedContact.setModifiedBy(contact.getModifiedBy());
	            executor.update(connection, Table.DELETED_CONTACTS, contextID, objectID, minLastModified, deletedContact, new ContactField[] { 
	            		ContactField.MODIFIED_BY, ContactField.LAST_MODIFIED });
            }
            /*
             * update image data if needed
             */
            if (queryFields.hasImageData()) {
                contact.setImageLastModified(now);
                queryFields.update(Mappers.CONTACT.getAssignedFields(contact));
                if (null == contact.getImage1()) {
                    // delete previous image if exists
                    executor.delete(connection, Table.IMAGES, contextID, objectID, minLastModified);
                } else {
                    if (null != executor.selectSingle(connection, Table.IMAGES, contextID, objectID, new ContactField[] { 
                    		ContactField.OBJECT_ID })) {
                        // update previous image
                        if (0 == executor.update(connection, Table.IMAGES, contextID, objectID, minLastModified, contact, 
                        		queryFields.getImageDataFields(true))) {
                        	throw ContactExceptionCodes.OBJECT_HAS_CHANGED.create(contextID, objectID);
                        }
                    } else {
                        // create new image
                    	Contact imageData = new Contact();
                    	imageData.setObjectID(objectID);
                    	imageData.setContextId(contextID);
                    	imageData.setImage1(contact.getImage1());
                    	imageData.setImageContentType(contact.getImageContentType());
                    	imageData.setImageLastModified(contact.getImageLastModified());                    	
                        this.executor.insert(connection, Table.IMAGES, imageData, Fields.IMAGE_DATABASE_ARRAY);                        
                    }                    
                }
            }
            /*
             * update contact data
             */
            if (0 == executor.update(connection, Table.CONTACTS, contextID, objectID, minLastModified, contact, 
            		queryFields.getContactDataFields())) {
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
                	DistListMember[] members = DistListMember.create(contact.getDistributionList(), contextID, objectID);
                    executor.insert(connection, Table.DISTLIST, members, Fields.DISTLIST_DATABASE_ARRAY);
                }
            }
            /*
             * commit
             */
            connection.commit();
        	committed = true;
        } catch (DataTruncation e) {
            DBUtils.rollback(connection);
            throw Tools.getTruncationException(connection, e, contact, Table.CONTACTS);
        } catch (SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
        	if (false == committed) {
        		DBUtils.rollback(connection);
        	}
            DBUtils.autocommit(connection);
            databaseService.backWritable(contextID, connection);
        }
    }
    
    @Override
    public void updateReferences(int contextID, Contact contact) throws OXException {
    	boolean committed = false;
        DatabaseService databaseService = getDatabaseService();
        Connection connection = databaseService.getWritable(contextID);
        try {
            connection.setAutoCommit(false);
        	/*
        	 * check with existing member references
        	 */
        	List<Integer> affectedDistributionLists = new ArrayList<Integer>();
    		List<DistListMember> referencedMembers = executor.select(connection, Table.DISTLIST, contextID, contact.getObjectID(), 
    				contact.getParentFolderID(), DistListMemberField.values());
    		if (null != referencedMembers && 0 < referencedMembers.size()) {
    			for (DistListMember member : referencedMembers) {
    				if (Tools.updateMember(member, contact)) {
    					/*
    					 * Update member, remember affected parent contact id of the list   
    					 */
    					if (0 < executor.updateMember(connection, Table.DISTLIST, contextID, member, DistListMemberField.values())) {
    						affectedDistributionLists.add(Integer.valueOf(member.getParentContactID()));
    					}
    				}
    			}
    		}
        	/*
        	 * Update affected parent distribution lists' timestamps, too
        	 */
    		if (0 < affectedDistributionLists.size()) {
    			for (Integer distListID : affectedDistributionLists) {
					executor.update(connection, Table.CONTACTS, contextID, distListID.intValue(), Long.MIN_VALUE, contact, 
							new ContactField[] { ContactField.LAST_MODIFIED, ContactField.MODIFIED_BY });
				}    			
    		}
            /*
             * commit
             */
            connection.commit();
        	committed = true;
        } catch (DataTruncation e) {
            DBUtils.rollback(connection);
            throw Tools.getTruncationException(connection, e, contact, Table.CONTACTS);
        } catch (SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
        	if (false == committed) {
        		DBUtils.rollback(connection);
        	}
            DBUtils.autocommit(connection);
            databaseService.backWritable(contextID, connection);
        }
    }
    
    @Override
    public SearchIterator<Contact> deleted(int contextID, String folderId, Date since, ContactField[] fields) throws OXException {
    	return this.getContacts(true, contextID, folderId, null, since, fields, null, null);
    }

    @Override
    public SearchIterator<Contact> deleted(int contextID, String folderId, Date since, ContactField[] fields, SortOptions sortOptions) throws OXException {
    	return this.getContacts(true, contextID, folderId, null, since, fields, null, sortOptions);
    }

    @Override
    public SearchIterator<Contact> modified(int contextID, String folderId, Date since, ContactField[] fields) throws OXException {
    	return this.getContacts(false, contextID, folderId, null, since, fields, null, null);
    }

    @Override
    public SearchIterator<Contact> modified(int contextID, String folderId, Date since, ContactField[] fields, SortOptions sortOptions) throws OXException {
    	return this.getContacts(false, contextID, folderId, null, since, fields, null, sortOptions);
    }

    @Override
    public <O> SearchIterator<Contact> search(int contextID, SearchTerm<O> term, ContactField[] fields, SortOptions sortOptions) throws OXException {
    	return this.getContacts(false, contextID, null, null, null, fields, term, sortOptions);
    }
    
    @Override
    public SearchIterator<Contact> search(int contextID, ContactSearchObject contactSearch, ContactField[] fields, SortOptions sortOptions) throws OXException {
    	return this.getContacts(contextID, contactSearch, fields, sortOptions);
    }
    
    @Override
    public SearchIterator<Contact> all(int contextID, String folderId, ContactField[] fields, SortOptions sortOptions) throws OXException {
    	return this.getContacts(false, contextID, folderId, null, null, fields, null, sortOptions);
    }
    
    @Override
    public SearchIterator<Contact> list(int contextID, String folderId, String[] ids, ContactField[] fields, SortOptions sortOptions) throws OXException {
    	return this.getContacts(false, contextID, folderId, ids, null, fields, null, sortOptions);
    }

    /**
     * Gets contacts from the database.
     * 
     * @param deleted whether to query the tables for deleted objects or not
     * @param contextID the context ID
     * @param folderID the folder ID, or <code>null</code> if not used
     * @param ids the object IDs, or <code>null</code> if not used
     * @param since the exclusive minimum modification time to consider, or <code>null</code> if not used 
     * @param fields the contact fields that should be retrieved
     * @param term a search term to apply, or <code>null</code> if not used
     * @param sortOptions the sort options to use, or <code>null</code> if not used
     * @return the contacts
     * @throws OXException
     */
    private <O> SearchIterator<Contact> getContacts(boolean deleted, int contextID, String folderID, String[] ids, Date since, 
    		ContactField[] fields, SearchTerm<O> term, SortOptions sortOptions) throws OXException {
        /*
         * prepare select
         */
        DatabaseService databaseService = getDatabaseService();
        Connection connection = databaseService.getReadOnly(contextID);
    	long minLastModified = null != since ? since.getTime() : Long.MIN_VALUE;
        int parentFolderID = null != folderID ? parse(folderID) : Integer.MIN_VALUE;
        int[] objectIDs = null != ids ? parse(ids) : null;        
        try {
            /*
             * check fields
             */
            QueryFields queryFields = new QueryFields(fields, ContactField.OBJECT_ID);
            if (false == queryFields.hasContactData()) {
                return null; // nothing to do
            }
            /*
             * get contact data
             */        
            List<Contact> contacts = executor.select(connection, deleted ? Table.DELETED_CONTACTS : Table.CONTACTS, contextID, 
            		parentFolderID, objectIDs, minLastModified, queryFields.getContactDataFields(), term, sortOptions);
            if (null != contacts && 0 < contacts.size()) {
                /*
                 * merge image data if needed
                 */
                if (queryFields.hasImageData()) {
                    contacts = mergeImageData(connection, deleted ? Table.DELETED_IMAGES : Table.IMAGES, contextID, contacts, 
                    		queryFields.getImageDataFields());
                }
                /*
                 * merge distribution list data if needed
                 */
                if (queryFields.hasDistListData()) {
                    contacts = mergeDistListData(connection, deleted ? Table.DELETED_DISTLIST : Table.DISTLIST, contextID, contacts);
                }
                /*
                 * merge attachment information in advance if needed
                 */
                //TODO: at this stage, we break the storage separation, since we assume that attachments are stored in the same database
                if (PREFETCH_ATTACHMENT_INFO && queryFields.hasAttachmentData()) {
                	contacts = mergeAttachmentData(connection, contextID, contacts);            	
                }
            }
            return getSearchIterator(contacts);
        } catch (SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            databaseService.backReadOnly(contextID, connection);
        }
    }

    private <O> SearchIterator<Contact> getContacts(int contextID, ContactSearchObject contactSearch, ContactField[] fields, 
    		SortOptions sortOptions) throws OXException {
        /*
         * prepare select
         */
        DatabaseService databaseService = getDatabaseService();
        Connection connection = databaseService.getReadOnly(contextID);
        try {
            /*
             * check fields
             */
            QueryFields queryFields = new QueryFields(fields, ContactField.OBJECT_ID);
            if (false == queryFields.hasContactData()) {
                return null; // nothing to do
            }
            /*
             * get contact data
             */        
            List<Contact> contacts = executor.select(connection, Table.CONTACTS, contextID, contactSearch, 
            		queryFields.getContactDataFields(), sortOptions);
            if (null != contacts && 0 < contacts.size()) {
                /*
                 * merge image data if needed
                 */
                if (queryFields.hasImageData()) {
                    contacts = mergeImageData(connection, Table.IMAGES, contextID, contacts, 
                    		queryFields.getImageDataFields());
                }
                /*
                 * merge distribution list data if needed
                 */
                if (queryFields.hasDistListData()) {
                    contacts = mergeDistListData(connection, Table.DISTLIST, contextID, contacts);
                }
                /*
                 * merge attachment information in advance if needed
                 */
                //TODO: at this stage, we break the storage separation, since we assume that attachments are stored in the same database
                if (PREFETCH_ATTACHMENT_INFO && queryFields.hasAttachmentData()) {
                	contacts = mergeAttachmentData(connection, contextID, contacts);            	
                }
            }
            return getSearchIterator(contacts);
        } catch (SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            databaseService.backReadOnly(contextID, connection);
        }
    }

    private List<Contact> mergeDistListData(Connection connection, Table table, int contextID, List<Contact> contacts) throws SQLException, OXException {
        int[] objectIDs = getObjectIDsWithDistLists(contacts);
        if (null != objectIDs && 0 < objectIDs.length) {
            Map<Integer, List<DistListMember>> distListData = executor.select(connection, table, contextID, objectIDs, Fields.DISTLIST_DATABASE_ARRAY);
            for (Contact contact : contacts) {
                List<DistListMember> distList = distListData.get(Integer.valueOf(contact.getObjectID()));
                if (null != distList) {
                    contact.setDistributionList(distList.toArray(new DistListMember[distList.size()]));
                }
            }
        }
        return contacts;
    }

    private List<Contact> mergeAttachmentData(Connection connection, int contextID, List<Contact> contacts) throws SQLException, OXException {
        int[] objectIDs = getObjectIDsWithAttachments(contacts);
        if (null != objectIDs && 0 < objectIDs.length) {
            Map<Integer, Date> attachmentData = executor.selectNewestAttachmentDates(connection, contextID, objectIDs);
            for (Contact contact : contacts) {
                Date attachmentLastModified = attachmentData.get(Integer.valueOf(contact.getObjectID()));
                if (null != attachmentLastModified) {
                    contact.setLastModifiedOfNewestAttachment(attachmentLastModified);
                }
            }
        }
        return contacts;
    }

    private List<Contact> mergeImageData(Connection connection, Table table, int contextID, List<Contact> contacts, 
    		ContactField[] fields) throws SQLException, OXException {
        int[] objectIDs = getObjectIDsWithImages(contacts);
        if (null != objectIDs && 0 < objectIDs.length) {
        	List<Contact> imagaDataList = executor.select(connection, table, contextID, Integer.MIN_VALUE, objectIDs, Long.MIN_VALUE, fields, null, null);
            if (null != imagaDataList && 0 < imagaDataList.size()) {
                return mergeByID(contacts, imagaDataList);
            }
        }
        return contacts;
    }

    private static List<Contact> mergeByID(List<Contact> into, List<Contact> from) throws OXException {
        if (null == into) {
            throw new IllegalArgumentException("into");
        } else if (null == from) {
            throw new IllegalArgumentException("from");
        }        
        for (Contact fromData : from) {
            int objectID = fromData.getObjectID();
            for (int i = 0; i < into.size(); i++) {
                Contact intoData = into.get(i);
                if (objectID == intoData.getObjectID()) {
                	Mappers.CONTACT.mergeDifferences(intoData, fromData);
                    break;
                }
            }
        }
        return into;
    }

	private int[] getObjectIDsWithImages(List<Contact> contacts) {
        int i = 0;
        int[] objectIDs = new int[contacts.size()];
        for (Contact contact : contacts) {
            if (0 < contact.getNumberOfImages()) {
                objectIDs[i++] = contact.getObjectID();
            }
        }
        return Arrays.copyOf(objectIDs, i);
    }
    
    private int[] getObjectIDsWithDistLists(List<Contact> contacts) {
        int i = 0;
        int[] objectIDs = new int[contacts.size()];
        for (Contact contact : contacts) {
            if (0 < contact.getNumberOfDistributionLists()) {
                objectIDs[i++] = contact.getObjectID();
            }
        }
        return Arrays.copyOf(objectIDs, i);
    }
    
    private int[] getObjectIDsWithAttachments(List<Contact> contacts) {
        int i = 0;
        int[] objectIDs = new int[contacts.size()];
        for (Contact contact : contacts) {
            if (0 < contact.getNumberOfAttachments()) {
                objectIDs[i++] = contact.getObjectID();
            }
        }
        return Arrays.copyOf(objectIDs, i);
    }
    
    private static DatabaseService getDatabaseService() throws OXException {
        return RdbServiceLookup.getService(DatabaseService.class, true);
    }
    
}

