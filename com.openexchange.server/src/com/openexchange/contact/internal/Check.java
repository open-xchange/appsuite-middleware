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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
import java.util.List;
import java.util.Map;
import com.openexchange.contact.SortOptions;
import com.openexchange.contact.internal.mapping.ContactMapper;
import com.openexchange.contact.storage.ContactStorage;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.contact.Search;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.java.Strings;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.OXFolderProperties;

/**
 * {@link Check} - Static utility functions for the contact service.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class Check {

	public static void argNotNull(final Object object, final String argumentName) {
		if (null == object) {
			throw new IllegalArgumentException("the passed argument '" + argumentName + "' may not be null");
		}
	}

	public static void hasStorages(final Map<ContactStorage, List<String>> storages) throws OXException {
	    if (null == storages || 0 == storages.size()) {
	        throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ContactStorage.class.getName());
        }
	}

	public static void validateProperties(final Contact contact) throws OXException {
		ContactMapper.getInstance().validateAll(contact);
	}

	public static void isNotPrivate(final Contact contact, final Session session, final String folderID) throws OXException {
		if (contact.containsPrivateFlag()) {
			throw ContactExceptionCodes.NO_ACCESS_PERMISSION.create(parse(folderID), session.getContextId(), session.getUserId());
		}
	}

	public static void canReadOwn(final EffectivePermission permission, final Session session, final String folderID) throws OXException {
		if (false == permission.canReadOwnObjects()) {
			throw ContactExceptionCodes.NO_ACCESS_PERMISSION.create(parse(folderID), session.getContextId(), session.getUserId());
		}
	}

	public static void canWriteOwn(final EffectivePermission permission, final Session session) throws OXException {
		if (false == permission.canWriteOwnObjects()) {
			throw ContactExceptionCodes.NO_CHANGE_PERMISSION.create(session.getUserId(), session.getContextId());
		}
	}

	public static void canWriteAll(final EffectivePermission permission, final Session session) throws OXException {
		if (false == permission.canWriteAllObjects()) {
			throw ContactExceptionCodes.NO_CHANGE_PERMISSION.create(session.getUserId(), session.getContextId());
		}
	}

	public static void canReadAll(final EffectivePermission permission, final Session session, final String folderID) throws OXException {
		if (false == permission.canReadAllObjects()) {
			throw ContactExceptionCodes.NO_ACCESS_PERMISSION.create(parse(folderID), session.getContextId(), session.getUserId());
		}
	}

	public static void canCreateObjects(final EffectivePermission permission, final Session session, final String folderID) throws OXException {
		if (false == permission.canCreateObjects()) {
			throw ContactExceptionCodes.NO_CREATE_PERMISSION.create(parse(folderID), session.getContextId(), session.getUserId());
		}
	}

	public static void canDeleteOwn(final EffectivePermission permission, final Session session, final String folderID) throws OXException {
		if (false == permission.canDeleteOwnObjects()) {
			throw ContactExceptionCodes.NO_DELETE_PERMISSION.create(parse(folderID), session.getContextId(), session.getUserId());
		}
	}

	public static void canDeleteAll(final EffectivePermission permission, final Session session, final String folderID) throws OXException {
		if (false == permission.canDeleteAllObjects()) {
			throw ContactExceptionCodes.NO_DELETE_PERMISSION.create(parse(folderID), session.getContextId(), session.getUserId());
		}
	}

    public static void isContactFolder(final FolderObject folder, final Session session) throws OXException {
		if (FolderObject.CONTACT != folder.getModule()) {
			throw ContactExceptionCodes.NON_CONTACT_FOLDER.create(folder.getObjectID(), session.getContextId(), session.getUserId());
		}
    }

    public static void contactNotNull(final Contact contact, final int contextID, final int id) throws OXException {
        if (null == contact) {
            throw ContactExceptionCodes.CONTACT_NOT_FOUND.create(id, contextID);
        }
    }

    public static void lastModifiedBefore(final Contact contact, final Date lastRead) throws OXException {
    	if (lastRead.before(contact.getLastModified())) {
			throw ContactExceptionCodes.OBJECT_HAS_CHANGED.create();
		}
    }

    public static void folderEquals(final Contact contact, final String folderID, final int contextID) throws OXException {
    	if (contact.getParentFolderID() != parse(folderID)) {
			throw ContactExceptionCodes.NOT_IN_FOLDER.create(contact.getObjectID(), parse(folderID), contextID);
		}
    }

    public static void noPrivateInPublic(final FolderObject folder, final Contact contact, final Session session) throws OXException {
    	if (FolderObject.PUBLIC == folder.getType() && contact.getPrivateFlag()) {
            throw ContactExceptionCodes.PFLAG_IN_PUBLIC_FOLDER.create(folder.getObjectID(), session.getContextId(), session.getUserId());
        }
    }

	public static void validateSearch(final ContactSearchObject contactSearch) throws OXException {
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
	 * Performs validation checks prior performing write operations on the global address book, throwing appropriate exceptions if
	 * checks fail.
	 *
	 * @param storage The queried storage
	 * @param session The session
	 * @param folderID The folder ID
	 * @param update The contact to be written
	 * @throws OXException
	 */
	public static void canWriteInGAB(ContactStorage storage, Session session, String folderID, Contact update) throws OXException {
		if (FolderObject.SYSTEM_LDAP_FOLDER_ID == parse(folderID)) {
		    /*
		     * check legacy edit flag
		     */
	        if (false == OXFolderProperties.isEnableInternalUsersEdit()) {
	            throw ContactExceptionCodes.NO_CHANGE_PERMISSION.create(update.getObjectID(), session.getContextId());
	        }
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
		    	CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.AND);
				searchTerm.addSearchTerm(Tools.createContactFieldTerm(ContactField.FOLDER_ID, SingleOperation.EQUALS, folderID));
				searchTerm.addSearchTerm(Tools.createContactFieldTerm(
				    ContactField.DISPLAY_NAME, SingleOperation.EQUALS, update.getDisplayName()));
				searchTerm.addSearchTerm(Tools.createContactFieldTerm(
				    ContactField.OBJECT_ID, SingleOperation.NOT_EQUALS, Integer.valueOf(update.getObjectID())));
				SearchIterator<Contact> searchIterator = null;
				try {
					searchIterator = storage.search(
					    session, searchTerm, new ContactField[] { ContactField.OBJECT_ID }, new SortOptions(0, 1));
					if (searchIterator.hasNext()) {
						throw ContactExceptionCodes.DISPLAY_NAME_IN_USE.create(session.getContextId(), update.getObjectID());
					}
				} finally {
				    Tools.close(searchIterator);
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
	        	if (Tools.getContext(session).getMailadmin() != session.getUserId()) {
	        		throw ContactExceptionCodes.NO_PRIMARY_EMAIL_EDIT.create(
	        		    session.getContextId(), update.getObjectID(), session.getUserId());
	        	}
	        }
		}
	}

	/**
	 * Checks the supplied delta contact for possible changes to read-only fields. If read-only are about to be modified to a value
	 * different from the currently stored value, an appropriate exception is thrown. If they're going to be set to the property's
	 * default value, the properties are removed from the delta reference.
	 *
	 * @param userID The ID of the user performing the update
	 * @param storedContact The stored contact
	 * @param delta The delta holding the changes to be applied
	 * @throws OXException
	 */
	public static void readOnlyFields(int userID, Contact storedContact, Contact delta) throws OXException {
        if (delta.containsContextId()) {
            if (0 == delta.getContextId() || delta.getContextId() == storedContact.getContextId()) {
                delta.removeContextID();
            } else {
                throw ContactExceptionCodes.NO_CHANGE_PERMISSION.create(
                    Integer.valueOf(storedContact.getObjectID()), Integer.valueOf(storedContact.getContextId()));
            }
        }
        if (delta.containsObjectID()) {
            if (0 == delta.getObjectID() || delta.getObjectID() == storedContact.getObjectID()) {
                delta.removeObjectID();
            } else {
                throw ContactExceptionCodes.NO_CHANGE_PERMISSION.create(
                    Integer.valueOf(storedContact.getObjectID()), Integer.valueOf(storedContact.getContextId()));
            }
        }
        if (delta.containsInternalUserId()) {
            if (0 == delta.getInternalUserId() || delta.getInternalUserId() == storedContact.getInternalUserId()) {
                delta.removeInternalUserId();
            } else {
                throw ContactExceptionCodes.NO_CHANGE_PERMISSION.create(
                    Integer.valueOf(storedContact.getObjectID()), Integer.valueOf(storedContact.getContextId()));
            }
        }
        if (delta.containsUid() && false == Strings.isEmpty(storedContact.getUid())) {
            if (Strings.isEmpty(delta.getUid()) || delta.getUid().equals(storedContact.getUid())) {
                delta.removeUid();
            } else {
                throw ContactExceptionCodes.NO_CHANGE_PERMISSION.create(
                    Integer.valueOf(storedContact.getObjectID()), Integer.valueOf(storedContact.getContextId()));
            }
        }
        if (delta.containsCreatedBy()) {
            if (0 == delta.getCreatedBy() || delta.getCreatedBy() == storedContact.getCreatedBy()) {
                delta.removeCreatedBy();
            } else {
                throw ContactExceptionCodes.NO_CHANGE_PERMISSION.create(
                    Integer.valueOf(storedContact.getObjectID()), Integer.valueOf(storedContact.getContextId()));
            }
        }
        if (delta.containsCreationDate()) {
            if (null == delta.getCreationDate() || delta.getCreationDate().equals(storedContact.getCreationDate())
                || 0 == delta.getCreationDate().getTime()) {
                delta.removeCreationDate();
            } else {
                throw ContactExceptionCodes.NO_CHANGE_PERMISSION.create(
                    Integer.valueOf(storedContact.getObjectID()), Integer.valueOf(storedContact.getContextId()));
            }
        }
        if (delta.containsPrivateFlag() && delta.getPrivateFlag() && storedContact.getCreatedBy() != userID) {
            throw ContactExceptionCodes.NO_CHANGE_PERMISSION.create(
                Integer.valueOf(storedContact.getObjectID()), Integer.valueOf(storedContact.getContextId()));
        }
	}

	private Check() {
		// prevent instantiation
	}

}
