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

import static com.openexchange.contact.internal.Tools.parse;

import java.util.Date;

import com.openexchange.contact.ContactFieldOperand;
import com.openexchange.contact.internal.mapping.ContactMapper;
import com.openexchange.contact.storage.ContactStorage;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.contact.Search;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.internal.operands.ConstantOperand;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link Check} - Static utility functions for the contact service.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class Check {
	
	public static void argNotNull(Object object, String argumentName) {
		if (null == object) {
			throw new IllegalArgumentException("the passed argument '" + argumentName + "' may not be null");
		}
	}
    
	public static void validateProperties(Contact contact) throws OXException {
		ContactMapper.getInstance().validateAll(contact);
	}
	
	public static void isNotPrivate(Contact contact, int contextID, int userID, String folderID) throws OXException {
		if (contact.containsPrivateFlag()) {
			throw ContactExceptionCodes.NO_ACCESS_PERMISSION.create(parse(folderID), contextID, userID);
		}
	}
	
	public static void canReadOwn(EffectivePermission permission, int contextID, int userID, String folderID) throws OXException {
		if (false == permission.canReadOwnObjects()) {
			throw ContactExceptionCodes.NO_ACCESS_PERMISSION.create(parse(folderID), contextID, userID);
		}
	}
	
	public static void canWriteOwn(EffectivePermission permission, int contextID, int userID, String folderID) throws OXException {
		if (false == permission.canWriteOwnObjects()) {
			throw ContactExceptionCodes.NO_CHANGE_PERMISSION.create(parse(folderID), contextID, userID);
		}
	}
	
	public static void canWriteAll(EffectivePermission permission, int contextID, int userID, String folderID) throws OXException {
		if (false == permission.canWriteAllObjects()) {
			throw ContactExceptionCodes.NO_CHANGE_PERMISSION.create(parse(folderID), contextID, userID);
		}
	}
	
	public static void canReadAll(EffectivePermission permission, int contextID, int userID, String folderID) throws OXException {
		if (false == permission.canReadAllObjects()) {
			throw ContactExceptionCodes.NO_ACCESS_PERMISSION.create(parse(folderID), contextID, userID);
		}
	}
	
	public static void canCreateObjects(EffectivePermission permission, int contextID, int userID, String folderID) throws OXException {
		if (false == permission.canCreateObjects()) {
			throw ContactExceptionCodes.NO_CREATE_PERMISSION.create(parse(folderID), contextID, userID);
		}
	}
	
	public static void canDeleteOwn(EffectivePermission permission, int contextID, int userID, String folderID) throws OXException {
		if (false == permission.canDeleteOwnObjects()) {
			throw ContactExceptionCodes.NO_DELETE_PERMISSION.create(parse(folderID), contextID, userID);
		}
	}
	
	public static void canDeleteAll(EffectivePermission permission, int contextID, int userID, String folderID) throws OXException {
		if (false == permission.canDeleteAllObjects()) {
			throw ContactExceptionCodes.NO_DELETE_PERMISSION.create(parse(folderID), contextID, userID);
		}
	}
	
    public static void isContactFolder(FolderObject folder, int contextID, int userID) throws OXException {
		if (FolderObject.CONTACT != folder.getModule()) {
			throw ContactExceptionCodes.NON_CONTACT_FOLDER.create(folder.getObjectID(), contextID, userID);
		}
    }    
    
    public static void contactNotNull(Contact contact, int contextID, int userID) throws OXException {
		if (null == contact) {
			throw ContactExceptionCodes.CONTACT_NOT_FOUND.create(userID, contextID);
		}		
    }
    
    public static void lastModifiedBefore(Contact contact, Date lastRead) throws OXException {
    	if (lastRead.before(contact.getLastModified())) {
			throw ContactExceptionCodes.OBJECT_HAS_CHANGED.create();
		} 
    }
    
    public static void folderEquals(Contact contact, String folderID, int contextID) throws OXException {
    	if (contact.getParentFolderID() != parse(folderID)) {
			throw ContactExceptionCodes.NOT_IN_FOLDER.create(contact.getObjectID(), parse(folderID), contextID); 
		}		
    }
    
    public static void noPrivateInPublic(FolderObject folder, Contact contact, int contextID, int userID) throws OXException {
    	if (FolderObject.PUBLIC == folder.getType() && contact.getPrivateFlag()) {
            throw ContactExceptionCodes.PFLAG_IN_PUBLIC_FOLDER.create(folder.getObjectID(), contextID, userID);
        }
    }
    
	public static void validateSearch(ContactSearchObject contactSearch) throws OXException {
		Search.checkPatternLength(contactSearch);
		if (0 != contactSearch.getIgnoreOwn() || null != contactSearch.getAnniversaryRange() || 
				null != contactSearch.getBirthdayRange() || null != contactSearch.getBusinessPostalCodeRange() ||
				null != contactSearch.getCreationDateRange() || null != contactSearch.getDynamicSearchField() ||
				null != contactSearch.getDynamicSearchFieldValue() || null != contactSearch.getFrom() ||
				null != contactSearch.getLastModifiedRange() || null != contactSearch.getNumberOfEmployeesRange() ||
				null != contactSearch.getSalesVolumeRange() ||
				null != contactSearch.getOtherPostalCodeRange() || null != contactSearch.getPrivatePostalCodeRange()) {
			throw new UnsupportedOperationException("not implemented");
		}	
	}	

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
	public static void canWriteInGAB(ContactStorage storage, int contextID, int userID, String folderID, Contact update) throws OXException {
		if (FolderObject.SYSTEM_LDAP_FOLDER_ID == parse(folderID)) {
			/*
			 * check display name
			 */
			if (update.containsDisplayName()) {
				if (Tools.isEmpty(update.getDisplayName())) {
					throw ContactExceptionCodes.DISPLAY_NAME_MANDATORY.create();				
				}
				/*
				 * check if display name is already in use
				 */
		    	CompositeSearchTerm andTerm = new CompositeSearchTerm(CompositeOperation.AND);
				SingleSearchTerm folderIDTerm = new SingleSearchTerm(SingleSearchTerm.SingleOperation.EQUALS);
				folderIDTerm.addOperand(new ContactFieldOperand(ContactField.FOLDER_ID)); 
				folderIDTerm.addOperand(new ConstantOperand<String>(folderID));
				andTerm.addSearchTerm(folderIDTerm);
				SingleSearchTerm displayNameTerm = new SingleSearchTerm(SingleSearchTerm.SingleOperation.EQUALS);
				displayNameTerm.addOperand(new ContactFieldOperand(ContactField.DISPLAY_NAME)); 
				displayNameTerm.addOperand(new ConstantOperand<String>(update.getDisplayName()));
				andTerm.addSearchTerm(displayNameTerm);
				SingleSearchTerm objectIDTerm = new SingleSearchTerm(SingleSearchTerm.SingleOperation.NOT_EQUALS);
				objectIDTerm.addOperand(new ContactFieldOperand(ContactField.OBJECT_ID)); 
				objectIDTerm.addOperand(new ConstantOperand<Integer>(update.getObjectID()));
				andTerm.addSearchTerm(objectIDTerm);
				SearchIterator<Contact> searchIterator = null;
				try {
					searchIterator = storage.search(contextID, andTerm, new ContactField[] { ContactField.OBJECT_ID });
					if (searchIterator.hasNext()) {
						throw ContactExceptionCodes.DISPLAY_NAME_IN_USE.create(contextID, update.getObjectID());
					}
				} finally {
					if (null != searchIterator) {
						searchIterator.close();
					}
				}
			}
			/*
			 * further checks for mandatory properties
			 */
	        if (update.containsSurName() && Tools.isEmpty(update.getSurName())) {
	        	throw ContactExceptionCodes.LAST_NAME_MANDATORY.create();
	        } else if (update.containsGivenName() && Tools.isEmpty(update.getGivenName())) {
	        	throw ContactExceptionCodes.FIRST_NAME_MANDATORY.create();
	        } 
	        /*
	         * check primary mail address
	         */
	        if (update.containsEmail1()) {
	        	final Context context = Tools.getContext(contextID);
	        	if (context.getMailadmin() != userID) {
	        		throw ContactExceptionCodes.NO_PRIMARY_EMAIL_EDIT.create(contextID, update.getObjectID(), userID);        		
	        	}
	        }
		}
	}			
	
	private Check() {
		// prevent instantiation
	}

}
