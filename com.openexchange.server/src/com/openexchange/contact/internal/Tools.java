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

import java.sql.SQLException;
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
import com.openexchange.folder.FolderService.Storage;
import com.openexchange.groupware.contact.ContactConfig;
import com.openexchange.groupware.contact.ContactConfig.Property;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.search.ContactSearchObject;
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
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;
import com.openexchange.userconf.UserConfigurationService;

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
	    if (null == sortOptions || SortOptions.EMPTY.equals(sortOptions) || 
	        null == sortOptions.getOrder() || 0 == sortOptions.getOrder().length) {
	        /*
	         * nothing to sort
	         */
	        return new Comparator<Contact>() {	            
	            @Override
	            public int compare(Contact o1, Contact o2) {
	                return 0;
	            }
            };
	    } else {
	        /*
	         * sort using the mapping's comparator with collation
	         */
	        final Comparator<Object> collationComparator = null == sortOptions.getCollation() ? null :
	            Collator.getInstance(SuperCollator.get(sortOptions.getCollation()).getJavaLocale());
	        return new Comparator<Contact>() {	            
	            @Override
	            public int compare(Contact o1, Contact o2) {	                
	                for (SortOrder order : sortOptions.getOrder()) {
	                    int comparison = 0;
	                    try {
	                        comparison = ContactMapper.getInstance().get(order.getBy()).compare(o1, o2, collationComparator);
	                    } catch (OXException e) {
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
	}
	
	/**
	 * Gets the contact storage. 
	 * 
	 * @param session the session
	 * @param folderId the folder ID
	 * @return the contact storage
	 * @throws OXException
	 */
	public static ContactStorage getStorage(Session session, String folderID) throws OXException {
		ContactStorage storage = ContactServiceLookup.getService(ContactStorageRegistry.class, true).getStorage(session, folderID);
        if (null == storage) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create("'Contact storage for folder " + folderID + "'");
        }
        return storage;
	}
	
	/**
	 * Gets all contact storages. 
	 * 
     * @param session the session
	 * @return the contact storages
	 * @throws OXException
	 */
	public static List<ContactStorage> getStorages(Session session) throws OXException {
		return ContactServiceLookup.getService(ContactStorageRegistry.class, true).getStorages(session);
	}
	
	/**
	 * Gets the contact storages for the supplied folders, each storage mapped 
	 * to a list of folder IDs the respective storage is responsible for. 
	 * 
     * @param session the session
	 * @param folderIDs the folder IDs to get the storages for
	 * @return the contact storages
	 * @throws OXException
	 */
	public static Map<ContactStorage, List<String>> getStorages(Session session, Collection<String> folderIDs) throws OXException {
		Map<ContactStorage, List<String>> storages = new HashMap<ContactStorage, List<String>>();
		for (String folderID : folderIDs) {
			ContactStorage storage = getStorage(session, folderID);
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
    public static Context getContext(int contextID) throws OXException {
        final Context context = ContactServiceLookup.getService(ContextService.class, true).getContext(contextID);
        if (null == context) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create("Unable to get context '" + contextID + "'.");
        }
        return context;
    }
    
    /**
     * Gets the user configuration.
     * 
     * @param session
     * @return
     * @throws OXException
     */
    public static UserConfiguration getUserConfig(Session session) throws OXException {
        return ContactServiceLookup.getService(UserConfigurationService.class, true).getUserConfiguration(
            session.getUserId(), getContext(session));
    }

    /**
     * Gets a context.
     * 
     * @param session the session
     * @return the context
     * @throws OXException
     */
    public static Context getContext(Session session) throws OXException {
        return getContext(session.getContextId());
    }

	/**
	 * Gets a folder.
	 *  
     * @param contextID the context ID
     * @param folderId the folder ID
	 * @return
	 * @throws OXException
	 */
    public static FolderObject getFolder(int contextID, String folderId) throws OXException {
        return getFolder(contextID, folderId, false);
    }
    
    /**
     * Gets a folder.
     * 
     * @param contextID the context ID
     * @param folderId the folder ID
     * @param deleted <code>true</code> to also query the backup folder table, <code>false</code>, otherwise
     * @return
     * @throws OXException
     */
    public static FolderObject getFolder(int contextID, String folderId, boolean deleted) throws OXException {
        FolderService folderService = ContactServiceLookup.getService(FolderService.class, true);
        try {
            return folderService.getFolderObject(parse(folderId), contextID, Storage.LIVE_WORKING);
        } catch (OXException e) {
            if (deleted && "FLD-0008".equals(e.getErrorCode())) {
                // not found, also try backup folder tree
                return folderService.getFolderObject(parse(folderId), contextID, Storage.LIVE_BACKUP);
            }
            throw e;
        }
    }
    
    /**
     * Gets the permissions of a folder.
     * 
     * @param session
     * @param folder
     * @return
     * @throws OXException
     */
    public static EffectivePermission getPermission(Session session, FolderObject folder) throws OXException {
        try {
            return folder.getEffectiveUserPermission(session.getUserId(), getUserConfig(session));
        } catch (SQLException e) {
            throw ContactExceptionCodes.UNEXPECTED_ERROR.create(e);
        }
    }

    /**
     * Gets the permissions of a folder.
     * 
     * @param contextID the context ID
     * @param folderId the folder ID
     * @param userID the user ID
     * @return
     * @throws OXException
     */
    public static EffectivePermission getPermission(int contextID, String folderId, int userID) throws OXException {
        return ContactServiceLookup.getService(FolderService.class, true).getFolderPermission(parse(folderId), userID, contextID);
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
	 * Parses a list of numerical identifiers from strings, wrapping possible 
	 * NumberFormatExceptions into an OXException.
	 * 
	 * @param ids the list of string IDs
	 * @return a list of numerical IDs
	 * @throws OXException
	 */
	public static List<Integer> parse(List<String> ids) throws OXException {
		List<Integer> intIDs = new ArrayList<Integer>();
		for (String id : ids) {
			intIDs.add(parse(id));
		}
		return intIDs;
	}
	
	/**
	 * Converts an array of numeric IDs to a list of String identifiers.
	 * 
	 * @param ids the numeric identifiers
	 * @return the string identifiers
	 */
	public static List<String> toStringList(int[] ids) {
		List<String> stringList = new ArrayList<String>();
		for (int id : ids) {
			stringList.add(Integer.toString(id));
		}
		return stringList;
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

	/**
     * Sets the 'file as' attribute to the contact's display name if needed, i.e. the 'display name'-property is set, while the
     * 'file as'-property isn't.
     *
     * @param contact The contact to set the 'file as' for
     */
    public static void setFileAsIfNeeded(final Contact contact) {
        if (contact.containsDisplayName() && (false == contact.containsFileAs() || Tools.isEmpty(contact.getFileAs()))) {
            contact.setFileAs(contact.getDisplayName());
        }
    }

	/**
	 * Creates a new {@link ContactSearchObject} containing only the relevant 
	 * parts of the supplied search object, extending the contained patterns 
	 * with implicit wildcards as needed. 
	 * 
	 * @param contactSearch
	 * @return
	 */
	public static ContactSearchObject prepareContactSearch(ContactSearchObject contactSearch) {
        if (null != contactSearch.getPattern() && 0 < contactSearch.getPattern().length()) {
            /*
             * "Search contacts"
             */
            return prepareSearchContacts(contactSearch);
        } else {
            /*
             * "Search contacts alternative"
             */
            return prepareSearchContactsAlternative(contactSearch);
        }
    }
	    
    private static ContactSearchObject prepareSearchContacts(ContactSearchObject contactSearch) {
        ContactSearchObject preparedSearchObject = new ContactSearchObject();
        preparedSearchObject.setFolders(contactSearch.getFolders());
        if (contactSearch.isStartLetter()) {
            preparedSearchObject.setStartLetter(true);
            preparedSearchObject.setPattern(addWildcards(contactSearch.getPattern(), false, true));
        } else {
            preparedSearchObject.setStartLetter(false);
            preparedSearchObject.setPattern(addWildcards(contactSearch.getPattern(), true, true));
        }
        return preparedSearchObject;  
    }
    
    private static ContactSearchObject prepareSearchContactsAlternative(ContactSearchObject contactSearch) {
        ContactSearchObject preparedSearchObject = new ContactSearchObject();
        boolean prependWildcard = false == contactSearch.isOrSearch() && false == contactSearch.isEmailAutoComplete();
        preparedSearchObject.setOrSearch(contactSearch.isOrSearch());
        preparedSearchObject.setEmailAutoComplete(contactSearch.isEmailAutoComplete());
        preparedSearchObject.setFolders(contactSearch.getFolders());
        preparedSearchObject.setEmailAutoComplete(contactSearch.isEmailAutoComplete());
        preparedSearchObject.setCatgories(addWildcards(contactSearch.getCatgories(), true, true));
        preparedSearchObject.setCityBusiness(addWildcards(contactSearch.getCityBusiness(), prependWildcard, true));
        preparedSearchObject.setCompany(addWildcards(contactSearch.getCompany(), prependWildcard, true));
        preparedSearchObject.setDepartment(addWildcards(contactSearch.getDepartment(), prependWildcard, true));
        preparedSearchObject.setDisplayName(addWildcards(contactSearch.getDisplayName(), prependWildcard, true));
        preparedSearchObject.setEmail1(addWildcards(contactSearch.getEmail1(), prependWildcard, true));
        preparedSearchObject.setEmail2(addWildcards(contactSearch.getEmail2(), prependWildcard, true));
        preparedSearchObject.setEmail3(addWildcards(contactSearch.getEmail3(), prependWildcard, true));
        preparedSearchObject.setGivenName(addWildcards(contactSearch.getGivenName(), prependWildcard, true));
        preparedSearchObject.setStreetBusiness(addWildcards(contactSearch.getStreetBusiness(), prependWildcard, true));
        preparedSearchObject.setSurname(addWildcards(contactSearch.getSurname(), prependWildcard, true));
        preparedSearchObject.setYomiCompany(addWildcards(contactSearch.getYomiCompany(), prependWildcard, true));
        preparedSearchObject.setYomiFirstname(addWildcards(contactSearch.getYomiFirstName(), prependWildcard, true));
        preparedSearchObject.setYomiLastName(addWildcards(contactSearch.getYomiLastName(), prependWildcard, true));
        return preparedSearchObject;  
    }
    
	private static String addWildcards(String pattern, boolean prepend, boolean append) {
	    if (null != pattern && 0 < pattern.length() && false == "*".equals(pattern)) {
            if (prepend && '*' != pattern.charAt(0)) {
                pattern = "*" + pattern;
            }
            if (append && '*' != pattern.charAt(pattern.length() - 1)) {
                pattern = pattern + "*";
            }
	    }
	    return pattern;
	}
	
	private Tools() {
		// prevent instantiation
	}

}
