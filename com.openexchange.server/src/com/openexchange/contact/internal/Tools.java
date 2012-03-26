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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.openexchange.contact.ContactFieldOperand;
import com.openexchange.contact.storage.ContactStorage;
import com.openexchange.contact.storage.registry.ContactStorageRegistry;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.folder.FolderService;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.search.Operand;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link Tools} - Static utility functions for the contact service.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class Tools {
	
	/**
	 * Performs validation checks prior performing write operations on the 
	 * global address book, throwing appropriate exceptions if checks fail.
	 * 
	 * @param contextID the context ID
	 * @param userID the user ID
	 * @param folderID the folder ID
	 * @param update the contact to be written
	 * @throws OXException
	 */
	public static void checkWriteInGAB(final ContactStorage storage, final int contextID, final int userID,
			final String folderID, final Contact update) throws OXException {
		/*
		 * check display name
		 */
		if (update.containsDisplayName()) {
			if (isEmpty(update.getDisplayName())) {
				throw ContactExceptionCodes.DISPLAY_NAME_MANDATORY.create();				
			}
			/*
			 * check if display name is already in use
			 */
			final SingleSearchTerm term = new SingleSearchTerm(SingleSearchTerm.SingleOperation.EQUALS);
			term.addOperand(new ContactFieldOperand(ContactField.DISPLAY_NAME));
			term.addOperand(new Operand<String>() {

				@Override
				public com.openexchange.search.Operand.Type getType() {
					return Type.CONSTANT;
				}

				@Override
				public String getValue() {
					return update.getDisplayName();
				}
				
			});
			final SearchIterator<Contact> contacts = storage.search(contextID, folderID, term, new ContactField[] { 
					ContactField.OBJECT_ID });
			if (null != contacts && 0 < contacts.size()) {
				throw ContactExceptionCodes.DISPLAY_NAME_IN_USE.create(contextID, update.getObjectID());
			}
		}
		/*
		 * further checks for mandatory properties
		 */
        if (update.containsSurName() && isEmpty(update.getSurName())) {
        	throw ContactExceptionCodes.LAST_NAME_MANDATORY.create();
        } else if (update.containsGivenName() && isEmpty(update.getGivenName())) {
        	throw ContactExceptionCodes.FIRST_NAME_MANDATORY.create();
        } 
        /*
         * check primary mail address
         */
        if (update.containsEmail1()) {
        	final Context context = getContext(contextID);
        	if (context.getMailadmin() != userID) {
        		throw ContactExceptionCodes.NO_PRIMARY_EMAIL_EDIT.create(contextID, update.getObjectID(), userID);        		
        	}
        }        
	}			
	
	/**
	 * Filters the supplied contact collection in respect to personal object 
	 * permissions of the user. Assumes the contacts in the collection having
	 * the "private flag" and "created by" properties set.
	 * 
	 * @param contacts the contacts to filter
	 * @param userID the user ID
	 * @param permission the effective permissions for the user on the folder
	 */
	public static void filterByObjectPermissions(final Collection<Contact> contacts, final int userID, 
			final EffectivePermission permission) {
		final Iterator<Contact> iterator = contacts.iterator();
		final boolean filterForeignContacts = false == permission.canReadAllObjects();
		while (iterator.hasNext()) {
			final Contact contact = iterator.next();
			if (contact.getCreatedBy() != userID && (filterForeignContacts || contact.getPrivateFlag())) {
				iterator.remove();
			} 
		}
	}	
	
	/**
	 * Gets the contact storage. 
	 * 
	 * @param contextID the current context ID
	 * @param folderId the folder ID
	 * @return the contact storage
	 * @throws OXException
	 */
	public static ContactStorage getStorage(final int contextID, final String folderId) throws OXException {
		final ContactStorage storage = ContactServiceLookup.getService(ContactStorageRegistry.class, true).getStorage(contextID, folderId);
		return storage;
	}
	
	public static Context getContext(final int contextID) throws OXException {
		final Context context = ContactServiceLookup.getService(ContextService.class, true).getContext(contextID);
		return context;
	}

	/**
	 * Gets a folder.
	 *  
	 * @param contextID
	 * @param folderId
	 * @return
	 * @throws OXException
	 */
	public static FolderObject getFolder(final int contextID, final String folderId) throws OXException {
		return ContactServiceLookup.getService(FolderService.class, true).getFolderObject(Integer.parseInt(folderId), contextID);
	}
	
	/**
	 * Gets the permissions of a folder.
	 * 
	 * @param contextID
	 * @param folderId
	 * @param userID
	 * @return
	 * @throws OXException
	 */
	public static EffectivePermission getPermission(final int contextID, final String folderId, final int userID) throws OXException {
		return ContactServiceLookup.getService(FolderService.class, true).getFolderPermission(Integer.parseInt(folderId), userID, contextID);
	}
	
	/**
	 * Prepares a contact field array by ensuring that all necessary fields 
	 * are included.
	 * 
	 * @param fields the contact fields to prepare, or <code>null</code> if 
	 * all fields should be used 
	 * @param necessaryFields the fields to extend the result if necessary 
	 * @return the prepared fields
	 */
	public static ContactField[] prepare(final ContactField[] fields, final ContactField... necessaryFields) {
        final Set<ContactField> extendedFields = new HashSet<ContactField>();
        if (null != fields) {
            for (final ContactField field : fields) {
                extendedFields.add(field);
            }
        } else {
            for (final ContactField field : ContactField.values()) {
          		extendedFields.add(field);
            }
        }
        if (null != necessaryFields) {
            for (final ContactField field : necessaryFields) {
                extendedFields.add(field);
            }
        }
        return extendedFields.toArray(new ContactField[extendedFields.size()]);
    }
	
	/**
	 * Checks whether the supplied string is empty, that is it is either 
	 * <code>null</code>, or consists of whitespace characters exclusively.
	 * 
	 * @param string the string to check
	 * @return <code>true</code>, if the string is 'empty', <code>false</code>,
	 * otherwise
	 */
	public static boolean isEmpty(final String string) {
        if (null != string) {
		    final int length = string.length();
		    for (int i = 0; i < length; i++) {
		    	if (false == Character.isWhitespace(string.charAt(i))) {
		    		return false;
		    	}
		    }
        }
        return true;
    }

	private Tools() {
		// prevent instantiation
	}

}
