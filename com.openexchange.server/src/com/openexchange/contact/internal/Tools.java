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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.contact.ContactFieldOperand;
import com.openexchange.contact.SortOptions;
import com.openexchange.contact.SortOrder;
import com.openexchange.contact.internal.mapping.ContactMapper;
import com.openexchange.contact.storage.ContactStorage;
import com.openexchange.contact.storage.registry.ContactStorageRegistry;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.folder.FolderService;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.Attachments;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.search.Order;
import com.openexchange.l10n.SuperCollator;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.internal.operands.ConstantOperand;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link Tools} - Static utility functions for the contact service.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class Tools {
	
    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(Tools.class));
	
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
	    	final CompositeSearchTerm andTerm = new CompositeSearchTerm(CompositeOperation.AND);
			final SingleSearchTerm folderIDTerm = new SingleSearchTerm(SingleSearchTerm.SingleOperation.EQUALS);
			folderIDTerm.addOperand(new ContactFieldOperand(ContactField.FOLDER_ID)); 
			folderIDTerm.addOperand(new ConstantOperand<String>(folderID));
			andTerm.addSearchTerm(folderIDTerm);
			final SingleSearchTerm displayNameTerm = new SingleSearchTerm(SingleSearchTerm.SingleOperation.EQUALS);
			displayNameTerm.addOperand(new ContactFieldOperand(ContactField.DISPLAY_NAME)); 
			displayNameTerm.addOperand(new ConstantOperand<String>(update.getDisplayName()));
			andTerm.addSearchTerm(displayNameTerm);
			final SearchIterator<Contact> contacts = storage.search(contextID, andTerm, new ContactField[] { 
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
	 * Gets a comparator for contacts based on the supplied sort options. 
	 * 
	 * @param sortOptions the sort options 
	 * @return the comparator
	 */
	public static Comparator<Contact> getComparator(final SortOptions sortOptions) {
		final Comparator<Object> collationComparator = null == sortOptions.getCollation() ? null :
			Collator.getInstance(SuperCollator.get(sortOptions.getCollation()).getJavaLocale());
		return new Comparator<Contact>() {
			
			@Override
			public int compare(final Contact o1, final Contact o2) {
				for (final SortOrder order : sortOptions.getOrder()) {
					int comparison = 0;
					try {
						comparison = ContactMapper.getInstance().get(order.getBy()).compare(o1, o2, collationComparator);
					} catch (final OXException e) {
						LOG.error("error comparing objects", e);
					}
					if (0 != comparison) {
						return Order.DESCENDING.equals(order.getOrder()) ? -1 * comparison : comparison;							
					}
				}
				return 0;
			}
		};
	}
	
	/**
	 * Gets the contact storage. 
	 * 
	 * @param contextID the current context ID
	 * @param folderId the folder ID
	 * @return the contact storage
	 * @throws OXException
	 */
	public static ContactStorage getStorage(final int contextID, final String folderID) throws OXException {
		final ContactStorage storage = ContactServiceLookup.getService(ContactStorageRegistry.class, true).getStorage(contextID, folderID);
        if (null == storage) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create("No contact storage available for folder '" + folderID + "'");
        }
        return storage;
	}
	
	/**
	 * Gets a list of contact storages for the supplied folders.
	 * 
	 * @param contextID the current context ID
	 * @param folderIDs the folder IDs to get the storages for
	 * @return the contact storages
	 * @throws OXException
	 */
	public static List<ContactStorage> getStorages(final int contextID, final List<String> folderIDs) throws OXException {
		final List<ContactStorage> storages = new ArrayList<ContactStorage>();
		for (final String folderID : folderIDs) {
			final ContactStorage storage = getStorage(contextID, folderID);
			if (false == storages.contains(storage)) {
				storages.add(storage);
			}
		}
		return storages;
	}
	
	public static Context getContext(final int contextID) throws OXException {
		final Context context = ContactServiceLookup.getService(ContextService.class, true).getContext(contextID);
		if (null == context) {
			throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create("Unable to get context '" + contextID + "'.");
		}
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
	
	public static FolderService getFolderService() throws OXException {
		return ContactServiceLookup.getService(FolderService.class, true); 
	}	
	
	/**
	 * Adds the date of the last modification to attachments of the given 
	 * contact when needed. 
	 * 
	 * @param contact the contact to add the attachment information for
	 * @throws OXException
	 */
	public static void addAttachmentInformation(final Contact contact, final int contextID) throws OXException {
		if (false == contact.containsLastModifiedOfNewestAttachment() && 0 < contact.getNumberOfAttachments()) {
			contact.setLastModifiedOfNewestAttachment(Attachments.getInstance().getNewestCreationDate(
					Tools.getContext(contextID), Types.CONTACT, contact.getObjectID()));
		}
	}

	public static boolean needsAttachmentInfo(final ContactField[] fields) {
		if (null != fields) {
			for (final ContactField field : fields) {
				if (ContactField.LAST_MODIFIED_OF_NEWEST_ATTACHMENT.equals(field)) {
					return true;
				}
			}
		}
		return false;
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

	/**
	 * Extracts all folder IDs that are present in the supplied search term.
	 * 
	 * @param term the search term to analyze
	 * @return the folder IDs, or an empty list if none were found
	 */
	public static List<String> extractFolderIDs(final SearchTerm<?> term) {
		final List<String> folders = new SearchTermAnalyzer(term).getFolderIDs();
		return null != folders ? folders : new ArrayList<String>();
	}
	
	private Tools() {
		// prevent instantiation
	}

}
