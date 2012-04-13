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

package com.openexchange.contact.internal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.contact.ContactFieldOperand;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.SortOptions;
import com.openexchange.contact.internal.mapping.ContactMapper;
import com.openexchange.contact.storage.ContactStorage;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.contact.ContactMergerator;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ConstantOperand;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link ContactServiceImpl} - {@link ContactService} implementation.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ContactServiceImpl implements ContactService {
	
    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ContactServiceImpl.class));
    
    public ContactServiceImpl() {
    	super();
    	LOG.debug("initialized.");
    }

	/*
	 * -----------------------------------------------------------------------------------------------------------------------------------
	 */    
    
	@Override
	public Contact getContact(Session session, String folderId, String id) throws OXException {
		return this.getContact(session, folderId, id, null);
	}

	@Override
	public SearchIterator<Contact> getAllContacts(Session session, String folderId) throws OXException {
		return this.getAllContacts(session, folderId, null, null);
	}

	@Override
	public SearchIterator<Contact> getAllContacts(Session session, String folderId, SortOptions sortOptions) throws OXException {
		return this.getAllContacts(session, folderId, null, sortOptions);
	}

	@Override
	public SearchIterator<Contact> getAllContacts(Session session, String folderId, ContactField[] fields) throws OXException {
		return this.getAllContacts(session, folderId, fields, null);
	}

	@Override
	public SearchIterator<Contact> getContacts(Session session, String folderId, String[] ids) throws OXException {
		return this.getContacts(session, folderId, ids, null, null);
	}

	@Override
	public SearchIterator<Contact> getContacts(Session session, String folderId, String[] ids, SortOptions sortOptions) throws OXException {
		return this.getContacts(session, folderId, ids, null, sortOptions);
	}

	@Override
	public SearchIterator<Contact> getContacts(Session session, String folderId, String[] ids, ContactField[] fields) throws OXException {
		return this.getContacts(session, folderId, ids, fields, null);
	}

	@Override
	public SearchIterator<Contact> getDeletedContacts(Session session, String folderId, Date since) throws OXException {
		return this.getDeletedContacts(session, folderId, since, null);
	}

	@Override
	public SearchIterator<Contact> getDeletedContacts(Session session, String folderId, final Date since, final ContactField[] fields) throws OXException {
		return this.getDeletedContacts(session, folderId, since, fields, null);
	}

	@Override
	public SearchIterator<Contact> getModifiedContacts(Session session, String folderId, Date since) throws OXException {
		return this.getModifiedContacts(session, folderId, since, null);
	}

	@Override
	public SearchIterator<Contact> getModifiedContacts(Session session, String folderId, Date since, final ContactField[] fields) throws OXException {
		return this.getModifiedContacts(session, folderId, since, fields, SortOptions.EMPTY);
	}

	@Override
	public <O> SearchIterator<Contact> searchContacts(Session session, SearchTerm<O> term) throws OXException {
		return this.searchContacts(session, term, null, null);
	}

	@Override
	public <O> SearchIterator<Contact> searchContacts(Session session, SearchTerm<O> term, SortOptions sortOptions) throws OXException {
		return this.searchContacts(session, term, null, sortOptions);
	}

	@Override
	public <O> SearchIterator<Contact> searchContacts(Session session, SearchTerm<O> term, ContactField[] fields) throws OXException {
		return this.searchContacts(session, term, fields, null);
	}
	
	@Override
	public Contact getUser(Session session, int userID) throws OXException {
		return getUser(session, userID, null);
	}
	
	@Override
	public SearchIterator<Contact> getUsers(Session session, int[] userIDs) throws OXException {
		return getUsers(session, userIDs, null);
	}
	
	/*
	 * -----------------------------------------------------------------------------------------------------------------------------------
	 */

	@Override
	public <O> SearchIterator<Contact> searchContacts(Session session, SearchTerm<O> term, ContactField[] fields, SortOptions sortOptions) throws OXException {
		checkArgNotNull(session, "session");
		checkArgNotNull(term, "term");
		final int contextID = session.getContextId();
		final int userID = session.getUserId();
		return this.searchContacts(contextID, userID, term, fields, sortOptions);
	}

	@Override
	public SearchIterator<Contact> getAllContacts(Session session, String folderId, ContactField[] fields, SortOptions sortOptions) throws OXException {
		checkArgNotNull(session, "session");
		checkArgNotNull(folderId, "folderId");
		final int contextID = session.getContextId();
		final int userID = session.getUserId();
		return this.getContacts(false, contextID, userID, folderId, null, fields, sortOptions, null);
	}

	@Override
	public SearchIterator<Contact> getContacts(Session session, String folderId, String[] ids, ContactField[] fields, SortOptions sortOptions) throws OXException {
		checkArgNotNull(session, "session");
		checkArgNotNull(folderId, "folderId");
		checkArgNotNull(ids, "ids");
		final int contextID = session.getContextId();
		final int userID = session.getUserId();
		return this.getContacts(false, contextID, userID, folderId, ids, fields, sortOptions, null);
	}

	@Override
	public SearchIterator<Contact> getModifiedContacts(Session session, String folderId, Date since, ContactField[] fields, final SortOptions sortOptions) throws OXException {
		checkArgNotNull(session, "session");
		checkArgNotNull(folderId, "folderId");
		checkArgNotNull(since, "since");
		final int contextID = session.getContextId();
		final int userID = session.getUserId();
		return this.getContacts(false, contextID, userID, folderId, null, fields, sortOptions, since);
	}
	
	@Override
	public SearchIterator<Contact> getDeletedContacts(Session session, String folderId, Date since, ContactField[] fields, SortOptions sortOptions) throws OXException {
		checkArgNotNull(session, "session");
		checkArgNotNull(folderId, "folderId");
		checkArgNotNull(since, "since");
		final int contextID = session.getContextId();
		final int userID = session.getUserId();
		return this.getContacts(true, contextID, userID, folderId, null, fields, sortOptions, since);
	}

	@Override
	public Contact getContact(final Session session, final String folderId, final String id, final ContactField[] fields) throws OXException {
		checkArgNotNull(session, "session");
		checkArgNotNull(folderId, "folderId");
		checkArgNotNull(id, "id");
		final int contextID = session.getContextId();
		final int userID = session.getUserId();
		return this.getContact(contextID, userID, folderId, id, fields);
	}
	
	@Override
	public void createContact(Session session, String folderId, Contact contact) throws OXException {
		checkArgNotNull(session, "session");
		checkArgNotNull(folderId, "folderId");
		checkArgNotNull(contact, "contact");		
		final int contextID = session.getContextId();
		final int userID = session.getUserId();
		this.createContact(contextID, userID, folderId, contact);
	}

	@Override
	public void updateContact(final Session session, final String folderId, final String id, final Contact contact, final Date lastRead) 
			throws OXException {
		checkArgNotNull(session, "session");
		checkArgNotNull(folderId, "folderId");
		checkArgNotNull(id, "id");
		checkArgNotNull(lastRead, "lastRead");
		checkArgNotNull(contact, "contact");
		final int contextID = session.getContextId();
		final int userID = session.getUserId();
		if (contact.containsParentFolderID() && contact.getParentFolderID() != parse(folderId)) {
			this.updateAndMoveContact(contextID, userID, folderId, Integer.toString(contact.getParentFolderID()), id, contact, lastRead);			
		} else {
			this.updateContact(contextID, userID, folderId, id, contact, lastRead);
		}
	}

	@Override
	public void deleteContact(Session session, String folderId, String id, Date lastRead) throws OXException {
		checkArgNotNull(session, "session");
		checkArgNotNull(folderId, "folderId");
		checkArgNotNull(id, "id");
		checkArgNotNull(lastRead, "lastRead");
		final int contextID = session.getContextId();		
		final int userID = session.getUserId();
		this.deleteContact(contextID, userID, folderId, id, lastRead);
	}
	
	@Override
    public Contact getUser(Session session, int userID, ContactField[] fields) throws OXException {
		checkArgNotNull(session, "session");
		final int contextID = session.getContextId();
		final int currentUserID = session.getUserId();
		return this.getUser(contextID, currentUserID, userID, fields);
    }
    
	@Override
    public SearchIterator<Contact> getUsers(Session session, int[] userIDs, ContactField[] fields) throws OXException {
		checkArgNotNull(session, "session");
		checkArgNotNull(userIDs, "userIDs");
		final int contextID = session.getContextId();
		final int currentUserID = session.getUserId();
		return this.getUsers(contextID, currentUserID, userIDs, fields);
    }
    
	@Override
    public String getOrganization(Session session) throws OXException {
		checkArgNotNull(session, "session");
		final int contextID = session.getContextId();
		return this.getOrganization(contextID);
    }

	/*
	 * -----------------------------------------------------------------------------------------------------------------------------------
	 */
	
	protected Contact getContact(final int contextID, final int userID, final String folderID, final String id, final ContactField[] fields) 
			throws OXException {
		final ContactStorage storage = Tools.getStorage(contextID, folderID);
		/*
		 * check folder
		 */
		final FolderObject folder = Tools.getFolder(contextID, folderID);
		if (FolderObject.CONTACT != folder.getModule()) {
			throw ContactExceptionCodes.NON_CONTACT_FOLDER.create(parse(folderID), contextID, userID);
		}
		/*
		 * check general permissions
		 */
		final EffectivePermission permission = Tools.getPermission(contextID, folderID, userID);
		if (false == permission.canReadOwnObjects()) {
			throw ContactExceptionCodes.NO_ACCESS_PERMISSION.create(parse(folderID), contextID, userID);
		}
		/*
		 * prepare fields
		 */
		final QueryFields queryFields = new QueryFields(fields);		
		/*
		 * get contact from storage
		 */		
		final Contact contact = storage.get(contextID, folderID, id, queryFields.getFields());
		if (null == contact) {
			throw ContactExceptionCodes.CONTACT_NOT_FOUND.create(parse(id), contextID);
		}		
		/*
		 * check further permission restrictions
		 */
		if (false == permission.canReadAllObjects() && contact.getCreatedBy() != userID) {
			throw ContactExceptionCodes.NO_ACCESS_PERMISSION.create(parse(folderID), contextID, userID);
		} else if (contact.getPrivateFlag() && contact.getCreatedBy() != userID) {
			throw ContactExceptionCodes.NO_ACCESS_PERMISSION.create(parse(folderID), contextID, userID);
		}
		/*
		 * Add attachment info if needed
		 */
		if (queryFields.needsAttachmentInfo()) {
			Tools.addAttachmentInformation(contact, contextID);
		}
		/*
		 * deliver contact
		 */
		return contact;
	}
	
	protected void createContact(final int contextID, final int userID, final String folderID, final Contact contact) throws OXException {
		final ContactStorage storage = Tools.getStorage(contextID, folderID);
		/*
		 * check supplied contact
		 */
		ContactMapper.getInstance().validateAll(contact);		
		/*
		 * check folder
		 */
		final FolderObject folder = Tools.getFolder(contextID, folderID);
		if (FolderObject.CONTACT != folder.getModule()) {
			throw ContactExceptionCodes.NON_CONTACT_FOLDER.create(parse(folderID), contextID, userID);
		} else if (FolderObject.PUBLIC == folder.getType() && contact.getPrivateFlag()) {
            throw ContactExceptionCodes.PFLAG_IN_PUBLIC_FOLDER.create(parse(folderID), contextID, userID);
        } else if (FolderObject.SYSTEM_LDAP_FOLDER_ID == parse(folderID)) {
        	Tools.checkWriteInGAB(storage, contextID, userID, folderID, contact);
        }
		/*
		 * check general permissions
		 */
		final EffectivePermission permission = Tools.getPermission(contextID, folderID, userID);
		if (false == permission.canCreateObjects()) {
			throw ContactExceptionCodes.NO_ACCESS_PERMISSION.create(parse(folderID), contextID, userID);
		}
		/*
		 * prepare create
		 */
        final Date now = new Date();
		contact.setParentFolderID(parse(folderID));
        contact.setContextId(contextID);
        contact.setLastModified(now);
        contact.setCreationDate(now);
        contact.setCreatedBy(userID);
        contact.setModifiedBy(userID);
        contact.removeObjectID(); // set by storage during create
        contact.setNumberOfAttachments(0);
        contact.setUseCount(0);
        if (contact.containsImage1()) {
        	contact.setImageLastModified(now);
        	if (null != contact.getImage1()) {
            	contact.setNumberOfImages(1);
        	} else {
            	contact.setNumberOfImages(0);
            	contact.setImageContentType(null);        		
        	}
        }        
		if (false == contact.containsUid() || Tools.isEmpty(contact.getUid())) {
			contact.setUid(UUID.randomUUID().toString());
		}
        if (false == contact.containsFileAs() && contact.containsDisplayName()) {
            contact.setFileAs(contact.getDisplayName());
        }
		/*
		 * pass through to storage
		 */
		storage.create(contextID, folderID, contact);
	}
	
	protected void updateAndMoveContact(final int contextID, final int userID, final String sourceFolderId, final String targetFolderId, 
			final String objectID, final Contact contact, final Date lastRead) throws OXException {
		/*
		 * check supplied contact
		 */
		ContactMapper.getInstance().validateAll(contact);
		if (contact.containsObjectID() && false == Integer.toString(contact.getObjectID()).equals(objectID)) {
			throw ContactExceptionCodes.NO_CHANGE_PERMISSION.create(objectID, contextID);
		}
		/*
		 * check source folder
		 */
		final FolderObject sourceFolder = Tools.getFolder(contextID, sourceFolderId);
		if (FolderObject.CONTACT != sourceFolder.getModule()) {
			throw ContactExceptionCodes.NON_CONTACT_FOLDER.create(parse(sourceFolderId), contextID, userID);
		}
		final EffectivePermission sourceFolderPermission = Tools.getPermission(contextID, sourceFolderId, userID);
		if (false == sourceFolderPermission.canDeleteOwnObjects()) {
			throw ContactExceptionCodes.NO_DELETE_PERMISSION.create(parse(sourceFolderId), contextID, userID);
		}
		/*
		 * check destination folder
		 */
		final FolderObject targetFolder = Tools.getFolder(contextID, targetFolderId);
		if (FolderObject.CONTACT != targetFolder.getModule()) {
			throw ContactExceptionCodes.NON_CONTACT_FOLDER.create(parse(targetFolderId), contextID, userID);
		} else if (FolderObject.PUBLIC == targetFolder.getType() && contact.getPrivateFlag()) {
	        throw ContactExceptionCodes.PFLAG_IN_PUBLIC_FOLDER.create(parse(targetFolderId), contextID, userID);
	    }
		final EffectivePermission targetFolderPermission = Tools.getPermission(contextID, targetFolderId, userID);
		if (false == targetFolderPermission.canCreateObjects()) {
			throw ContactExceptionCodes.NO_CREATE_PERMISSION.create(parse(targetFolderId), contextID, userID);
		} 
		/*
		 * check currently stored contact
		 */
		final ContactStorage sourceStorage = Tools.getStorage(contextID, sourceFolderId);
		final Contact storedContact = sourceStorage.get(contextID, sourceFolderId, objectID, ContactField.values());
		if (null == storedContact) {
			throw ContactExceptionCodes.CONTACT_NOT_FOUND.create(parse(objectID), contextID);
		} else if (false == sourceFolderPermission.canDeleteAllObjects() && storedContact.getCreatedBy() != userID) {
			throw ContactExceptionCodes.NO_DELETE_PERMISSION.create(parse(sourceFolderId), contextID, userID);
		} else if (lastRead.before(storedContact.getLastModified())) {
			throw ContactExceptionCodes.OBJECT_HAS_CHANGED.create();
		} else if (storedContact.getParentFolderID() != parse(sourceFolderId)) {
			throw ContactExceptionCodes.NOT_IN_FOLDER.create(parse(objectID), parse(sourceFolderId), contextID); 
		}		
		/*
		 * check for not allowed changes
		 */
		final Contact delta = ContactMapper.getInstance().getDifferences(storedContact, contact);
		if (delta.containsContextId()) {
			throw ContactExceptionCodes.NO_CHANGE_PERMISSION.create(objectID, contextID);
		} else if (delta.containsObjectID()) {
			throw ContactExceptionCodes.NO_CHANGE_PERMISSION.create(objectID, contextID);
		} else if (delta.containsUid() && false == Tools.isEmpty(storedContact.getUid())) {
			throw ContactExceptionCodes.NO_CHANGE_PERMISSION.create(objectID, contextID);
		} else if (delta.containsCreatedBy()) {
			throw ContactExceptionCodes.NO_CHANGE_PERMISSION.create(objectID, contextID);
		} else if (delta.containsCreationDate()) {
			throw ContactExceptionCodes.NO_CHANGE_PERMISSION.create(objectID, contextID);
		} else if (delta.containsPrivateFlag() && delta.getPrivateFlag() && storedContact.getModifiedBy() != userID) {
			throw ContactExceptionCodes.NO_CHANGE_PERMISSION.create(objectID, contextID);
		}
		/*
		 * prepare update
		 */
        final Date now = new Date();
        delta.setLastModified(now);
        delta.setModifiedBy(userID);
        delta.setParentFolderID(parse(targetFolderId));
		if ((false == storedContact.containsUid() || Tools.isEmpty(storedContact.getUid())) && false == delta.containsUid()) {
			delta.setUid(UUID.randomUUID().toString());
		}
        if (delta.containsImage1()) {
        	delta.setImageLastModified(now);
        	if (null != delta.getImage1()) {
        		delta.setNumberOfImages(1);
        	} else {
        		delta.setNumberOfImages(0);
        		delta.setImageContentType(null);        		
        	}
        }
		/*
		 * pass through to storage
		 */
		final ContactStorage targetStorage = Tools.getStorage(contextID, targetFolderId);
		if (sourceStorage.equals(targetStorage)) {
			/*
			 * same storage, send update as delta
			 */
			sourceStorage.update(contextID, sourceFolderId, objectID, delta, lastRead);			
		} else {
			/*
			 * different storage, perform delete & create of complete contact information
			 */
			//TODO: move attachments
			ContactMapper.getInstance().mergeDifferences(storedContact, delta); 
			targetStorage.create(contextID, targetFolderId, storedContact);
			try {
				sourceStorage.delete(contextID, userID, sourceFolderId, objectID, lastRead);
			} catch (final OXException e) {
				LOG.warn("error deleting contact from source folder, rolling back move operation", e);
				// TODO: simple rollback for now
				targetStorage.delete(contextID, userID, targetFolderId, Integer.toString(storedContact.getObjectID()), 
						storedContact.getLastModified());
				throw e;
			}
		}
	}	
	
	protected void updateContact(final int contextID, final int userID, final String folderID, final String objectID, 
			final Contact contact, final Date lastRead) throws OXException {
		final ContactStorage storage = Tools.getStorage(contextID, folderID);
		/*
		 * check supplied contact
		 */
		ContactMapper.getInstance().validateAll(contact);
		if (contact.containsObjectID() && false == Integer.toString(contact.getObjectID()).equals(objectID)) {
			throw ContactExceptionCodes.NO_CHANGE_PERMISSION.create(objectID, contextID);
		}
		/*
		 * check folder
		 */
		final FolderObject folder = Tools.getFolder(contextID, folderID);
		if (FolderObject.CONTACT != folder.getModule()) {
			throw ContactExceptionCodes.NON_CONTACT_FOLDER.create(parse(folderID), contextID, userID);
		} else if (FolderObject.PUBLIC == folder.getType() && contact.getPrivateFlag()) {
	        throw ContactExceptionCodes.PFLAG_IN_PUBLIC_FOLDER.create(parse(folderID), contextID, userID);
	    } else if (FolderObject.SYSTEM_LDAP_FOLDER_ID == parse(folderID)) {
	    	Tools.checkWriteInGAB(storage, contextID, userID, folderID, contact);
        }
		/*
		 * check general permissions
		 */
		final EffectivePermission permission = Tools.getPermission(contextID, folderID, userID);
		if (false == permission.canWriteOwnObjects()) {
			throw ContactExceptionCodes.NO_CHANGE_PERMISSION.create(parse(folderID), contextID, userID);
		}
		/*
		 * check currently stored contact
		 */
		final Contact storedContact = storage.get(contextID, folderID, objectID, ContactField.values());
		if (null == storedContact) {
			throw ContactExceptionCodes.CONTACT_NOT_FOUND.create(parse(objectID), contextID);
		} else if (false == permission.canWriteAllObjects() && storedContact.getCreatedBy() != userID) {
			throw ContactExceptionCodes.NO_CHANGE_PERMISSION.create(parse(folderID), contextID, userID);
		} else if (lastRead.before(storedContact.getLastModified())) {
			throw ContactExceptionCodes.OBJECT_HAS_CHANGED.create();
		} else if (storedContact.getParentFolderID() != parse(folderID)) {
			throw ContactExceptionCodes.NOT_IN_FOLDER.create(parse(objectID), parse(folderID), contextID); 
		}
		/*
		 * check for not allowed changes
		 */
		final Contact delta = ContactMapper.getInstance().getDifferences(storedContact, contact);
		if (delta.containsContextId()) {
			throw ContactExceptionCodes.NO_CHANGE_PERMISSION.create(objectID, contextID);
		} else if (delta.containsObjectID()) {
			throw ContactExceptionCodes.NO_CHANGE_PERMISSION.create(objectID, contextID);
		} else if (delta.containsUid() && false == Tools.isEmpty(storedContact.getUid())) {
			throw ContactExceptionCodes.NO_CHANGE_PERMISSION.create(objectID, contextID);
		} else if (delta.containsCreatedBy()) {
			throw ContactExceptionCodes.NO_CHANGE_PERMISSION.create(objectID, contextID);
		} else if (delta.containsCreationDate()) {
			throw ContactExceptionCodes.NO_CHANGE_PERMISSION.create(objectID, contextID);
		} else if (delta.containsParentFolderID()) {
			throw ContactExceptionCodes.NO_CHANGE_PERMISSION.create(objectID, contextID);
		} else if (delta.containsPrivateFlag() && delta.getPrivateFlag() && storedContact.getModifiedBy() != userID) {
			throw ContactExceptionCodes.NO_CHANGE_PERMISSION.create(objectID, contextID);
		}
		/*
		 * prepare update
		 */
        final Date now = new Date();
        delta.setLastModified(now);
        delta.setModifiedBy(userID);
		if ((false == storedContact.containsUid() || Tools.isEmpty(storedContact.getUid())) && false == delta.containsUid()) {
			delta.setUid(UUID.randomUUID().toString());
		}
        if (delta.containsImage1()) {
        	delta.setImageLastModified(now);
        	if (null != delta.getImage1()) {
        		delta.setNumberOfImages(1);
        	} else {
        		delta.setNumberOfImages(0);
        		delta.setImageContentType(null);        		
        	}
        }
		/*
		 * pass through to storage
		 */
		storage.update(contextID, folderID, objectID, delta, lastRead);
	}
	
	protected void deleteContact(final int contextID, final int userID, final String folderID, final String objectID, final Date lastRead) 
			throws OXException {
		final ContactStorage storage = Tools.getStorage(contextID, folderID);
		/*
		 * check folder
		 */
		final FolderObject folder = Tools.getFolder(contextID, folderID);
		if (FolderObject.CONTACT != folder.getModule()) {
			throw ContactExceptionCodes.NON_CONTACT_FOLDER.create(parse(folderID), contextID, userID);
		}
		/*
		 * check general permissions
		 */
		final EffectivePermission permission = Tools.getPermission(contextID, folderID, userID);
		if (false == permission.canDeleteOwnObjects()) {
			throw ContactExceptionCodes.NO_DELETE_PERMISSION.create(parse(folderID), contextID, userID);
		}
		/*
		 * check currently stored contact
		 */
		final Contact storedContact = storage.get(contextID, folderID, objectID, new ContactField[] { ContactField.CREATED_BY, 
				ContactField.LAST_MODIFIED });
		if (null == storedContact) {
			throw ContactExceptionCodes.CONTACT_NOT_FOUND.create(parse(objectID), contextID);
		} else if (false == permission.canDeleteAllObjects() && storedContact.getCreatedBy() != userID) {
			throw ContactExceptionCodes.NO_DELETE_PERMISSION.create(parse(folderID), contextID, userID);
		} else if (lastRead.before(storedContact.getLastModified())) {
			throw ContactExceptionCodes.OBJECT_HAS_CHANGED.create();
		} 
		/*
		 * delete contact from storage
		 */
		storage.delete(contextID, userID, folderID, objectID, lastRead);
	}
	
	/**
	 * Gets contacts from the storage, performing the necessary permission 
	 * checks and other validations. Depending on the supplied parameters, the  
	 * call is delegated to different methods in the storage layer implicitly.
	 * 
	 * @param deleted whether to get 'deleted' contacts or not
	 * @param contextID the context ID
	 * @param userID the user ID
	 * @param folderID the folder ID
	 * @param ids the object IDs
	 * @param term the search term
	 * @param fields the contact fields to retrieve
	 * @param sortOptions the sort options to apply
	 * @param since the date of the last modification
	 * @return the contacts
	 * @throws OXException
	 */
	protected <O> SearchIterator<Contact> getContacts(boolean deleted, final int contextID, final int userID, final String folderID, 
			final String[] ids, final ContactField[] fields, final SortOptions sortOptions, 
			final Date since) throws OXException {
		/*
		 * check folder
		 */
		final FolderObject folder = Tools.getFolder(contextID, folderID);
		if (FolderObject.CONTACT != folder.getModule()) {
			throw ContactExceptionCodes.NON_CONTACT_FOLDER.create(parse(folderID), contextID, userID);
		}
		/*
		 * check general permissions
		 */
		final EffectivePermission permission = Tools.getPermission(contextID, folderID, userID);
		if (false == permission.canReadOwnObjects()) {
			throw ContactExceptionCodes.NO_ACCESS_PERMISSION.create(parse(folderID), contextID, userID);
		}
		/*
		 * prepare fields
		 */
		final QueryFields queryFields = new QueryFields(fields);		
		/*
		 * get contacts from storage
		 */		
		final ContactStorage storage = Tools.getStorage(contextID, folderID);
		SearchIterator<Contact> contacts = null;
		if (null != since) {
			contacts = deleted ? storage.deleted(contextID, folderID, since, queryFields.getFields()) : 
				storage.modified(contextID, folderID, since, queryFields.getFields(), sortOptions);
		} else if (null != ids) {
			contacts = storage.list(contextID, folderID, ids, queryFields.getFields(), sortOptions);
		} else {
			contacts = storage.all(contextID, folderID, queryFields.getFields(), sortOptions);
		} 
		if (null == contacts) {
			throw ContactExceptionCodes.UNEXPECTED_ERROR.create("got no results from storage");
		}
		/*
		 * filter results respecting object permission restrictions, adding attachment info as needed
		 */
		return new ResultIterator(contacts, queryFields.needsAttachmentInfo(), contextID, userID, permission.canReadAllObjects());	
	}
	
	protected <O> SearchIterator<Contact> searchContacts(final int contextID, final int userID, final SearchTerm<O> term, 
			final ContactField[] fields, final SortOptions sortOptions) throws OXException {
		/*
		 * analyze term
		 */
		final SearchTermAnalyzer termAnanlyzer = new SearchTermAnalyzer(term);
		/*
		 * determine queried storages according to searched folders
		 */
		final Map<ContactStorage, List<String>> queriedStorages = Tools.getStorages(contextID, 
				termAnanlyzer.hasFolderIDs() ? termAnanlyzer.getFolderIDs() : Tools.getSearchFolders(contextID, userID));
		if (null == queriedStorages || 0 == queriedStorages.size()) {
			throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create("No contact storage found for queried folder IDs");
		}
		/*
		 * prepare fields
		 */
		final QueryFields queryFields = new QueryFields(fields);
		/*
		 * perform searches
		 */
		final List<SearchIterator<Contact>> searchIterators = new ArrayList<SearchIterator<Contact>>();		
		for (final Entry<ContactStorage, List<String>> queriedStorage : queriedStorages.entrySet()) {
			final SearchTerm<?> searchTerm;			
			if (termAnanlyzer.hasFolderIDs()) {
				/*
				 * leave term as is
				 */
				searchTerm = term;
			} else {
				/*
				 * combine term with extracted folder information for that storage
				 */
				final CompositeSearchTerm combinedTerm = new CompositeSearchTerm(CompositeOperation.AND);
				combinedTerm.addSearchTerm(Tools.getFoldersTerm(queriedStorage.getValue()));
				combinedTerm.addSearchTerm(term);
				searchTerm = combinedTerm;
			}			
			/*
			 * get results, filtered respecting object permission restrictions, adding attachment info as needed
			 */
			final SearchIterator<Contact> searchIterator = queriedStorage.getKey().search(
					contextID, searchTerm, queryFields.getFields(), sortOptions);
			searchIterators.add(new ResultIterator(searchIterator, queryFields.needsAttachmentInfo(), contextID, userID));
		}
		return 2 > searchIterators.size() ? searchIterators.get(0) : 
			new ContactMergerator(Tools.getComparator(sortOptions), searchIterators);				
	}
	
	protected String getOrganization(final int contextID) throws OXException {
		final String folderID = Integer.toString(FolderObject.SYSTEM_LDAP_FOLDER_ID);
		final int userID = Tools.getContext(contextID).getMailadmin();
		final ContactStorage storage = Tools.getStorage(contextID, folderID);
		final Contact contact = storage.get(contextID, folderID, Integer.toString(userID), new ContactField[] { ContactField.COMPANY } );
		if (null == contact) {
			throw ContactExceptionCodes.CONTACT_NOT_FOUND.create(userID, contextID);
		} else {
			return contact.getCompany();
		}
	}
	
	protected Contact getUser(final int contextID, final int currentUserID, final int userID, final ContactField[] fields) 
			throws OXException {
		/*
		 * get user contact from storage
		 */
		Contact contact = null;
		SearchIterator<Contact> searchIterator = null;
		try {
			searchIterator = this.getUsers(contextID, currentUserID, new int[] { userID }, fields); 
			contact = searchIterator.next();
		} finally {
			if (null != searchIterator) {
				searchIterator.close();
			}
		}
		if (null == contact) {
			throw ContactExceptionCodes.CONTACT_NOT_FOUND.create(userID, contextID);
		}		
		/*
		 * deliver user contact
		 */
		return contact;
	}
	
	protected SearchIterator<Contact> getUsers(final int contextID, final int currentUserID, final int[] userIDs, 
			final ContactField[] fields) throws OXException {
		final String folderID = Integer.toString(FolderObject.SYSTEM_LDAP_FOLDER_ID);
		final ContactStorage storage = Tools.getStorage(contextID, folderID);
		/*
		 * limit queried fields when necessary due to permissions
		 */
		final EffectivePermission permission = Tools.getPermission(contextID, folderID, currentUserID);
		final QueryFields queryFields;
		
		if (permission.canReadAllObjects() || permission.canReadOwnObjects() && 1 == userIDs.length && currentUserID == userIDs[0]) {
			// no limitation
			queryFields = new QueryFields(fields);		
		} else {
			// restrict queried fields
			queryFields = new QueryFields(fields, LIMITED_USER_FIELDS);
		}
		/*
		 * prepare search term for users
		 */
		final SingleSearchTerm folderIDTerm = new SingleSearchTerm(SingleOperation.EQUALS);
		folderIDTerm.addOperand(new ContactFieldOperand(ContactField.FOLDER_ID));
		folderIDTerm.addOperand(new ConstantOperand<String>(folderID));
		final SearchTerm<?> userIDsTerm;
		if (1 == userIDs.length) {
			final SingleSearchTerm userIDTerm = new SingleSearchTerm(SingleOperation.EQUALS);
			userIDTerm.addOperand(new ContactFieldOperand(ContactField.INTERNAL_USERID));
			userIDTerm.addOperand(new ConstantOperand<Integer>(userIDs[0]));
			userIDsTerm = userIDTerm;
		} else {
			final CompositeSearchTerm orTerm = new CompositeSearchTerm(CompositeOperation.OR);
			for (final int userID : userIDs) {
				final SingleSearchTerm userIDTerm = new SingleSearchTerm(SingleOperation.EQUALS);
				userIDTerm.addOperand(new ContactFieldOperand(ContactField.INTERNAL_USERID));
				userIDTerm.addOperand(new ConstantOperand<Integer>(userID));
				orTerm.addSearchTerm(userIDTerm);
			}
			userIDsTerm = orTerm;
		}
		final CompositeSearchTerm term = new CompositeSearchTerm(CompositeOperation.AND);
		term.addSearchTerm(folderIDTerm);
		term.addSearchTerm(userIDsTerm);
		/*
		 * get user contacts from storage
		 */
		return new ResultIterator(storage.search(contextID, term, queryFields.getFields()), queryFields.needsAttachmentInfo(), 
				contextID, currentUserID, true);
	}
	
	/*
	 * -----------------------------------------------------------------------------------------------------------------------------------
	 */
	
	private static void checkArgNotNull(final Object object, final String argumentName) {
		if (null == object) {
			throw new IllegalArgumentException("the passed argument '" + argumentName + "' may not be null");
		}
	}
	
	private static int parse(final String id) {
		try {
			return Integer.parseInt(id);
		} catch (final NumberFormatException e) {
			throw new IllegalArgumentException(e);
		}
	}

}
