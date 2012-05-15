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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.openexchange.contact.ContactFieldOperand;
import com.openexchange.contact.SortOptions;
import com.openexchange.contact.SortOrder;
import com.openexchange.contact.internal.mapping.ContactMapper;
import com.openexchange.contact.storage.ContactStorage;
import com.openexchange.contact.storage.registry.ContactStorageRegistry;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.folder.FolderService;
import com.openexchange.groupware.contact.ContactConfig;
import com.openexchange.groupware.contact.ContactConfig.Property;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.l10n.SuperCollator;
import com.openexchange.log.LogFactory;
import com.openexchange.preferences.ServerUserSetting;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ConstantOperand;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;

/**
 * {@link Tools} - Static utility functions for the contact service.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class Tools {
	
    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(Tools.class));
	
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
	public static ContactStorage getStorage(int contextID, String folderID) throws OXException {
		ContactStorage storage = ContactServiceLookup.getService(ContactStorageRegistry.class, true).getStorage(contextID, folderID);
        if (null == storage) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create("'Contact storage for folder " + folderID + "'");
        }
        return storage;
	}
	
	/**
	 * Gets all contact storages. 
	 * 
	 * @param contextID the current context ID
	 * @return the contact storages
	 * @throws OXException
	 */
	public static List<ContactStorage> getStorages(int contextID) throws OXException {
		return ContactServiceLookup.getService(ContactStorageRegistry.class, true).getStorages(contextID);
	}
	
	/**
	 * Gets the contact storages for the supplied folders, each storage mapped 
	 * to a list of folder IDs the respective storage is responsible for. 
	 * 
	 * @param contextID the current context ID
	 * @param folderIDs the folder IDs to get the storages for
	 * @return the contact storages
	 * @throws OXException
	 */
	public static Map<ContactStorage, List<String>> getStorages(int contextID, Collection<String> folderIDs) throws OXException {
		Map<ContactStorage, List<String>> storages = new HashMap<ContactStorage, List<String>>();
		for (String folderID : folderIDs) {
			ContactStorage storage = getStorage(contextID, folderID);
			if (false == storages.containsKey(storage)) {
				storages.put(storage, new ArrayList<String>());
			}
			storages.get(storage).add(folderID);
		}
		return storages;
	}
	
	/**
	 * Gets a context.
	 * 
	 * @param contextID the context ID
	 * @return the context
	 * @throws OXException
	 */
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
		return ContactServiceLookup.getService(FolderService.class, true).getFolderObject(parse(folderId), contextID);
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
		return ContactServiceLookup.getService(FolderService.class, true).getFolderPermission(parse(folderId), userID, contextID);
	}
	
	public static FolderService getFolderService() throws OXException {
		return ContactServiceLookup.getService(FolderService.class, true); 
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
	
	/**
	 * Constructs a search to search for specific folder IDs. 
	 * 
	 * @param folderIDs the folder IDs
	 * @return the search term
	 */
	public static SearchTerm<?> getFoldersTerm(final List<String> folderIDs) {
		if (null == folderIDs || 0 == folderIDs.size()) {
			return null;			
		} else if (1 == folderIDs.size()) {
			return getFolderTerm(folderIDs.get(0));
		} else {
    		final CompositeSearchTerm orTerm = new CompositeSearchTerm(CompositeOperation.OR);
			for (final String folderID : folderIDs) {
				orTerm.addSearchTerm(getFolderTerm(folderID));
			}
			return orTerm;
		}
	}

	/**
	 * Constructs a search to search for the specific folder ID. 
	 * 
	 * @param folderID the folder ID
	 * @return the search term
	 */
	public static SingleSearchTerm getFolderTerm(final String folderID) {
    	final SingleSearchTerm term = new SingleSearchTerm(SingleOperation.EQUALS);
    	term.addOperand(new ContactFieldOperand(ContactField.FOLDER_ID));
    	term.addOperand(new ConstantOperand<String>(folderID));
    	return term;
    }

	/**
	 * Gets the contact folders that are used for search by default when no 
	 * other folders are specified by the search term. This may either be a 
	 * set of default folders for the user, or all contact folders visible to
	 * the user. 
	 * 
	 * @param contextID the context ID
	 * @param userID the user ID.
	 * @return
	 * @throws OXException
	 */
	public static List<String> getSearchFolders(final int contextID, final int userID) throws OXException {
		if (ContactConfig.getInstance().getBoolean(Property.ALL_FOLDERS_FOR_AUTOCOMPLETE).booleanValue()) {
			/*
			 * use all visible folders for search
			 */
			return getVisibleFolders(contextID, userID);			
		} else {
			/*
			 * use default set of folders for search
			 */
			return getBasicFolders(contextID, userID);
		}
	}
	
	/**
	 * Gets all contact folders where the user at least has permissions to read 
	 * own objects.  
	 * 
	 * @param contextID the context ID
	 * @param userID the user ID
	 * @return the folder IDs
	 * @throws OXException
	 */
	private static List<String> getVisibleFolders(final int contextID, final int userID) throws OXException {
		final List<String> folderIDs = new ArrayList<String>();
        final UserConfiguration userConfig = UserConfigurationStorage.getInstance().getUserConfiguration(
        		userID, Tools.getContext(contextID));
        SearchIterator<FolderObject> searchIterator = null;
        try {
        	searchIterator = OXFolderIteratorSQL.getAllVisibleFoldersIteratorOfModule(userID, userConfig.getGroups(), 
        			userConfig.getAccessibleModules(), FolderObject.CONTACT, Tools.getContext(contextID));
            while (searchIterator.hasNext()) {
                final FolderObject folder = searchIterator.next();
    			if (FolderObject.CONTACT != folder.getModule()) {
    				continue;
    			}
    			final EffectivePermission permission = Tools.getPermission(
    					contextID, Integer.toString(folder.getObjectID()), userID);
    			if (null == permission || false == permission.canReadOwnObjects()) {
    				continue;
    			}
    			folderIDs.add(Integer.toString(folder.getObjectID()));
            }
        } finally {
        	if (null != searchIterator) {
        		searchIterator.close();
        	}
		}
        return folderIDs;
	}
	
	/**
	 * Gets a default set of folders used for searches of an user.
	 * 
	 * @param contextID the context ID
	 * @param userID the user ID
	 * @return the folder IDs
	 * @throws OXException
	 */
	private static List<String> getBasicFolders(final int contextID, final int userID) throws OXException {
		final List<String> folderIDs = new ArrayList<String>();
		folderIDs.add(Integer.toString(
				new OXFolderAccess(Tools.getContext(contextID)).getDefaultFolder(userID, FolderObject.CONTACT).getObjectID()));
		if (Tools.getPermission(contextID, Integer.toString(FolderObject.SYSTEM_LDAP_FOLDER_ID), userID).canReadAllObjects()) {
			folderIDs.add(Integer.toString(FolderObject.SYSTEM_LDAP_FOLDER_ID));
		}
		final Integer collectedContactFolderID = ServerUserSetting.getInstance().getContactCollectionFolder(contextID, userID);
		if (null != collectedContactFolderID) {
			folderIDs.add(Integer.toString(collectedContactFolderID));
		}
		return folderIDs;
	}

	/**
	 * Parses a numerical identifier from a string, wrapping a possible 
	 * NumberFormatException into an OXException.
	 * 
	 * @param id the id string
	 * @return the parsed identifier
	 * @throws OXException
	 */
	public static int parse(final String id) throws OXException {
		try {
			return Integer.parseInt(id);
		} catch (final NumberFormatException e) {
			throw ContactExceptionCodes.ID_PARSING_FAILED.create(e, id); 
		}
	}
	
	/**
	 * Invalidates the address properties of the given contact if one of the 
	 * corresponding parts of the address is set, e.g. sets the "Business
	 * Address" to <code>null</code>, when the "Postal Code Business" is set.
	 * 
	 * Necessary for Outlook, see bug #19827 for details
	 * 
	 * @param contact the contact to invalidate the addresses for
	 */
	public static void invalidateAddressesIfNeeded(final Contact contact) {
		if (false == contact.containsAddressBusiness()) {
			if (contact.containsStreetBusiness() || contact.containsPostalCodeBusiness() || contact.containsCityBusiness() ||
					contact.containsStateBusiness() || contact.containsCountryBusiness()) {
				contact.setAddressBusiness(null);
			}
		}
		if (false == contact.containsAddressHome()) {
			if (contact.containsStreetHome() || contact.containsPostalCodeHome() || contact.containsCityHome() ||
					contact.containsStateHome() || contact.containsCountryHome()) {
				contact.setAddressHome(null);
			}
		}
		if (false == contact.containsAddressOther()) {
			if (contact.containsStreetOther() || contact.containsPostalCodeOther() || contact.containsCityOther() ||
					contact.containsStateOther() || contact.containsCountryOther()) {
				contact.setAddressOther(null);
			}
		}
	}
	
	private Tools() {
		// prevent instantiation
	}

}
