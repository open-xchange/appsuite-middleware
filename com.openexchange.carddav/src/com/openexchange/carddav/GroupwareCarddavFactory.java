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

package com.openexchange.carddav;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

import com.openexchange.carddav.resources.RootCollection;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.SortOptions;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.ContactContentType;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.search.Order;
import com.openexchange.log.LogFactory;
import com.openexchange.search.SearchTerm;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.session.SessionHolder;
import com.openexchange.user.UserService;
import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.helpers.AbstractWebdavFactory;

/**
 * {@link GroupwareCarddavFactory}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class GroupwareCarddavFactory extends AbstractWebdavFactory {

	private static final String OVERRIDE_NEXT_SYNC_TOKEN_PROPERTY = "com.openexchange.carddav.overridenextsynctoken";
	private static final String FOLDER_BLACKLIST_PROPERTY = "com.openexchange.carddav.ignoreFolders";
	private static final String FOLDER_TRRE_ID_PROPERTY = "com.openexchange.carddav.tree";
	private static final String COMBINED_REQUEST_TIMEOUT_PROPERTY = "com.openexchange.carddav.combinedRequestTimeout";
	private static final CarddavProtocol PROTOCOL = new CarddavProtocol();
	private static final Log LOG = com.openexchange.log.Log.loggerFor(GroupwareCarddavFactory.class);

	private final FolderService folderService;
	private final SessionHolder sessionHolder;
	private final ThreadLocal<State> stateHolder = new ThreadLocal<State>();
	private final ConfigViewFactory configs;
	private final UserService userService;
	private final ContactService contactService;	
	
	public GroupwareCarddavFactory(FolderService folders, SessionHolder sessionHolder, ConfigViewFactory configs, 
			UserService users, ContactService contactService) {
		super();
		this.folderService = folders;
		this.sessionHolder = sessionHolder;
		this.configs = configs;
		this.userService = users;
		this.contactService = contactService;
	}
	
	@Override
	public void beginRequest() {
		super.beginRequest();
		stateHolder.set(new State(this));
	}

	@Override
	public void endRequest(int status) {
		stateHolder.set(null);
		super.endRequest(status);
	}

	@Override
	public CarddavProtocol getProtocol() {
		return PROTOCOL;
	}

	@Override
	public WebdavCollection resolveCollection(WebdavPath url) throws WebdavProtocolException {
		if (0 == url.size()) {
			/*
			 * this is the root collection
			 */
			return mixin(new RootCollection(this));
		} else if (1 == url.size()) {
			/*
			 * get child collection from root by name 
			 */
			return mixin(new RootCollection(this).getChild(url.name()));
		} else {		
			throw WebdavProtocolException.Code.GENERAL_ERROR.create(url, HttpServletResponse.SC_NOT_FOUND);
		}		
	}

	@Override
	public WebdavResource resolveResource(WebdavPath url) throws WebdavProtocolException {
		if (2 == url.size()) {
			/*
			 * get child resource from parent collection by name 
			 */
			return mixin(new RootCollection(this).getChild(url.parent().name()).getChild(url.name()));
		} else {
			return resolveCollection(url);
		}
	}

    public User resolveUser(int userID) throws OXException {
    	return userService.getUser(userID, getContext());
    }

	public FolderService getFolderService() {
		return folderService;
	}

	public Context getContext() {
		return sessionHolder.getContext();
	}

	public Session getSession() {
		return sessionHolder.getSessionObject();
	}

	public User getUser() {
		return sessionHolder.getUser();
	}

	public ContactService getContactService() throws OXException {
		return this.contactService;
	}

	public State getState() {
		return stateHolder.get();
	}

    public String getConfigValue(String key, String defaultValue) throws OXException {
        ConfigView view = configs.getView(sessionHolder.getUser().getId(), sessionHolder.getContext().getContextId());
        ComposedConfigProperty<String> property = view.property(key, String.class);
        return property.isDefined() ? property.get() : defaultValue;
    }
	
	/**
	 * Sets the next sync token for the current user to <code>"0"</code>, 
	 * enforcing the next sync status report to contain all changes 
	 * independently of the sync token supplied by the client, thus emulating 
	 * some kind of slow-sync this way. 
	 */
	public void overrideNextSyncToken() {
		this.setOverrideNextSyncToken("0");		
	}

	/**
	 * Sets the next sync token for the current user to the supplied value.
	 * 
	 * @param value
	 */
	public void setOverrideNextSyncToken(String value) {
		try {
			this.userService.setUserAttribute(getOverrideNextSyncTokenAttributeName(), value, this.getUser().getId(), this.getContext());
		} catch (OXException e) {
			LOG.error(e.getMessage(), e);
		}
	}
	
	/**
	 * Gets a value indicating the overridden sync token for the current user 
	 * if defined
	 *  
	 * @return the value of the overridden sync-token, or <code>null</code> if 
	 * not set
	 */
	public String getOverrideNextSyncToken() {
		try {
			return this.userService.getUserAttribute(getOverrideNextSyncTokenAttributeName(), this.getUser().getId(), this.getContext());
		} catch (OXException e) {
			LOG.error(e.getMessage(), e);
		}
		return null;
	}
	
	private String getOverrideNextSyncTokenAttributeName() {
		String userAgent = (String)this.getSession().getParameter("user-agent");
		return null != userAgent ? OVERRIDE_NEXT_SYNC_TOKEN_PROPERTY + userAgent.hashCode() : OVERRIDE_NEXT_SYNC_TOKEN_PROPERTY;
	}
	
	public static final class State {

		private static final ContactField[] BASIC_FIELDS = { 
			ContactField.OBJECT_ID, ContactField.LAST_MODIFIED, ContactField.CREATION_DATE, ContactField.MARK_AS_DISTRIBUTIONLIST, 
			ContactField.DISTRIBUTIONLIST, ContactField.UID, ContactField.FILENAME
		};
		
		private final GroupwareCarddavFactory factory;
		private Map<String, Contact> uidCache = null;
		private Map<String, Contact> filenameCache = null;
		private List<UserizedFolder> allFolders = null;		
		private HashSet<String> folderBlacklist = null;		
		private UserizedFolder defaultFolder = null;
		private String treeID = null;
		private Date overallLastModified = null;
		private long combinedRequestTimeout = Long.MIN_VALUE; 
		
		/**
		 * Initializes a new {@link State}.
		 * 
		 * @param factory the CardDAV factory
		 */
		public State(final GroupwareCarddavFactory factory) {
			super();
			this.factory = factory;
		}
		
		public Contact waitFor(String uid, ContactField[] fields) throws OXException, InterruptedException {
			/*
			 * try caches first
			 */
			if (null != uidCache && uidCache.containsKey(uid)) {
				return load(uidCache.get(uid), fields);
			} else if (null != filenameCache && filenameCache.containsKey(uid)) {
				return load(filenameCache.get(uid), fields);
			}
			/*
			 * perform repeated search 
			 */
			SearchTerm<?> searchTerm = Tools.getSearchTerm(uid, getFolderIDs());
			SortOptions sortOptions = new SortOptions(0, 1);
			long timeoutTime = getCombinedRequestTimeout() + System.currentTimeMillis();
			long waitInterval = getCombinedRequestTimeout() / 20;
			do {
				SearchIterator<Contact> iter = null;
				try {
					iter = factory.getContactService().searchContacts(factory.getSession(), searchTerm, fields, sortOptions);
					if (null != iter && iter.hasNext()) {
						return prepare(iter.next());
					}
				} finally {
					close(iter);
				}
				LOG.debug("Waiting for contact '" + uid + "'...");
				Thread.sleep(waitInterval);
			} while (System.currentTimeMillis() < timeoutTime);
			LOG.warn("Contact '" + uid + "' not found after " + getCombinedRequestTimeout() + "ms.");
			return null; // not found
		}
		
		/**
		 * Loads a {@link Contact} containing all data identified by the supplied uid
		 * @param uid
		 * @return
		 * @throws AbstractOXException
		 */
		public Contact load(String uid) throws OXException {
			Contact contact = this.get(uid);
			if (null == contact) {
					LOG.debug("Contact '" + uid + "' not found, unable to load.");
				return null;
			} else {
				return this.load(contact);
			}
		}

		public Contact load(Contact contact) throws OXException {
			return load(contact, null);
		}
		
		/**
		 * Loads a {@link Contact} containing all data identified by object- 
		 * and parent folder id found in the supplied contact.
		 * @param contact
		 * @return
		 * @throws OXException
		 * @throws ContextException
		 */
		public Contact load(Contact contact, ContactField[] fields) throws OXException {
			if (null == contact) {
				throw new IllegalArgumentException("contact is null");
			} else if (false == contact.containsObjectID() || false == contact.containsParentFolderID()) {
				throw new IllegalArgumentException("need at least object- and parent folder id");
			}
			return this.load(contact.getObjectID(), Integer.toString(contact.getParentFolderID()), fields);
		}
		
		/**
		 * Gets all contacts, each containing the basic information as defined by
		 * the <code>FIELDS_FOR_ALL_REQUEST</code> array.
		 * @return
		 * @throws AbstractOXException
		 */
		public Collection<Contact> getContacts() throws OXException {
			return this.getUidCache().values();			
		}
		
		public Collection<Contact> getContacts(String folderID) throws OXException {
			List<Contact> contacts = new ArrayList<Contact>();
			int parsedFolderID = Tools.parse(folderID);
			for (Contact contact : getContacts()) {
				if (contact.getParentFolderID() == parsedFolderID) {
					contacts.add(contact);
				}
			}
			return contacts;			
		}
		
		/**
		 * Gets the default contact folder.
		 * 
		 * @return the default folder.
		 * @throws OXException
		 */
		public UserizedFolder getDefaultFolder() throws OXException {
			if (null == this.defaultFolder) {
				this.defaultFolder = factory.getFolderService().getDefaultFolder(factory.getUser(), getTreeID(), 
						ContactContentType.getInstance(), factory.getSession(), null);
			}
			return this.defaultFolder;
		}
		
		/**
		 * Gets the folder identified by the supplied id.
		 * 
		 * @param id
		 * @return
		 * @throws OXException 
		 */
		public UserizedFolder getFolder(String id) throws OXException {
			final List<UserizedFolder> folders = this.getFolders();
			for (final UserizedFolder folder : folders) {
				if (id.equals(folder.getID())) {
					return folder;
				}
			}
			LOG.warn("Folder '" + id + "' not found.");
			return null;
		}

		/**
		 * Gets a list of all folders.
		 * 
		 * @return
		 * @throws FolderException
		 * @throws ConfigCascadeException
		 * @throws OXException
		 */
		public List<UserizedFolder> getFolders() throws OXException {
			if (null == this.allFolders) {
				this.allFolders = getVisibleFolders();
			}
			return this.allFolders;
		}
		
		/**
		 * Gets a list of all visible folders.
		 * 
		 * @return
		 * @throws FolderException
		 */
	    private List<UserizedFolder> getVisibleFolders() throws OXException {
	    	List<UserizedFolder> folders = new ArrayList<UserizedFolder>();
	    	folders.addAll(this.getVisibleFolders(PrivateType.getInstance()));
	    	folders.addAll(this.getVisibleFolders(PublicType.getInstance()));
	    	folders.addAll(this.getVisibleFolders(SharedType.getInstance()));
	    	return folders;
	    }
		
		/**
		 * Gets a list containing all visible folders of the given {@link Type}.
		 * @param type
		 * @return
		 * @throws FolderException 
		 */
	    private List<UserizedFolder> getVisibleFolders(final Type type) throws OXException {
	    	final List<UserizedFolder> folders = new ArrayList<UserizedFolder>();
			final FolderService folderService = this.factory.getFolderService();
			final FolderResponse<UserizedFolder[]> visibleFoldersResponse = folderService.getVisibleFolders(
					FolderStorage.REAL_TREE_ID, ContactContentType.getInstance(), type, true, 
					this.factory.getSession(), null);
            final UserizedFolder[] response = visibleFoldersResponse.getResponse();
            for (final UserizedFolder folder : response) {
                if (Permission.READ_OWN_OBJECTS < folder.getOwnPermission().getReadPermission() && false == this.isBlacklisted(folder)) {
                	folders.add(folder);                	
                }
            }
            return folders;
	    }		
		
		/**
		 * Gets an aggregated {@link Date} representing the last modification time of all resources. 
		 * @return
		 * @throws AbstractOXException
		 */
		public Date getLastModified() throws OXException {
			if (null == this.overallLastModified) {
				overallLastModified = new Date(0);
				for (UserizedFolder folder : this.getFolders()) {
					overallLastModified = Tools.getLatestModified(overallLastModified, getLastModified(folder, overallLastModified));
				}
			}
			return overallLastModified;
		}	
		
		/**
		 * Gets the last modification time of the supplied folder, including its contents. 
		 * This covers the folder's last modification time itself, and both updated and 
		 * deleted items inside the folder.
		 * 
		 * @param folder the folder to get the last modification time for
		 * @param since the minimum last modification time to consider
		 * @return
		 * @throws AbstractOXException
		 */
		public Date getLastModified(UserizedFolder folder, Date since) throws OXException {
			ContactField[] fields = new ContactField[] { ContactField.LAST_MODIFIED };
			Date lastModified = Tools.getLatestModified(since, folder);
			SearchIterator<Contact> iterator = null;
			SortOptions sortOptions = new SortOptions(ContactField.LAST_MODIFIED, Order.DESCENDING);
			sortOptions.setLimit(1);			
			try {
				iterator = factory.getContactService().getModifiedContacts(factory.getSession(), folder.getID(), lastModified, fields, sortOptions);			
				if (iterator.hasNext()) {
					lastModified = Tools.getLatestModified(lastModified, iterator.next());
				}
			} finally {
				close(iterator);
			}				
			try {
				iterator = factory.getContactService().getDeletedContacts(factory.getSession(), folder.getID(), lastModified, fields, sortOptions);			
				if (iterator.hasNext()) {
					lastModified = Tools.getLatestModified(lastModified, iterator.next());
				}
			} finally {
				close(iterator);
			}				
			return lastModified;
		}	
		
		public Date getLastModified(UserizedFolder folder) throws OXException {
			return getLastModified(folder, new Date(0));
		}
		
		/**
		 * Gets an aggregated list of all modified contacts in all folders since the supplied {@link Date}.
		 * @param since
		 * @return
		 * @throws AbstractOXException
		 */
		public List<Contact> getModifiedContacts(Date since) throws OXException {
			List<Contact> contacts = new ArrayList<Contact>();
			List<UserizedFolder> folders = this.getFolders();
			for (UserizedFolder folder : folders) {
				contacts.addAll(this.getModifiedContacts(since, folder.getID()));
			}
			return contacts;
		}
		
		/**
		 * Gets a list of all modified contacts in a folder since the supplied {@link Date}.
		 * @param since
		 * @param folderID
		 * @return
		 * @throws AbstractOXException
		 */
		public List<Contact> getModifiedContacts(Date since, String folderID) throws OXException  {
			List<Contact> contacts = new ArrayList<Contact>();
			SearchIterator<Contact> iterator = null;
			try {
				iterator = factory.getContactService().getModifiedContacts(factory.getSession(), folderID, since, BASIC_FIELDS);
				while (iterator.hasNext()) {
					contacts.add(iterator.next());						
				}
			} finally {
				close(iterator);
			}
			return contacts;
		}
		
		/**
		 * Gets an aggregated list of all deleted contacts in all folders since the supplied {@link Date}.
		 * @param since
		 * @return
		 * @throws AbstractOXException
		 */
		public List<Contact> getDeletedContacts(Date since) throws OXException  {
			List<Contact> contacts = new ArrayList<Contact>();
			List<UserizedFolder> folders = this.getFolders();
			for (UserizedFolder folder : folders) {
				contacts.addAll(this.getDeletedContacts(since, folder.getID()));
			}
			return contacts;
		}
		
		/**
		 * Gets an aggregated list of all deleted contacts in a folder since the supplied {@link Date}.
		 * @param since
		 * @param folderID
		 * @return
		 * @throws AbstractOXException
		 */
		public List<Contact> getDeletedContacts(Date since, String folderID) throws OXException  {
			List<Contact> contacts = new ArrayList<Contact>();
			SearchIterator<Contact> iterator = null;
			try {
				iterator = factory.getContactService().getDeletedContacts(factory.getSession(), folderID, since, BASIC_FIELDS);
				while (iterator.hasNext()) {
					contacts.add(iterator.next());						
				}
			} finally {
				close(iterator);
			}
			return contacts;
		}

		/**
		 * Determines whether the supplied folder is blacklisted and should be ignored or not.
		 * @param userizedFolder
		 * @return
		 */
		private boolean isBlacklisted(UserizedFolder userizedFolder) {
			if (null == this.folderBlacklist) {
				String ignoreFolders = null;
				try {
					ignoreFolders = factory.getConfigValue(FOLDER_BLACKLIST_PROPERTY, null);
				} catch (OXException e) {
			        LOG.error(e.getMessage(), e);
				}
				if (null == ignoreFolders || 0 >= ignoreFolders.length()) {
					this.folderBlacklist = new HashSet<String>(0);
				} else {
					this.folderBlacklist = new HashSet<String>(Arrays.asList(ignoreFolders.split("\\s*,\\s*")));
				}
			}
			return this.folderBlacklist.contains(userizedFolder.getID());
		}
		
	    /**
	     * Gets the used folder tree identifier for folder operations.
	     */
	    private String getTreeID() {
	    	if (null == this.treeID) {
		        try {
					treeID = factory.getConfigValue(FOLDER_TRRE_ID_PROPERTY, FolderStorage.REAL_TREE_ID);
				} catch (OXException e) {
					LOG.warn("falling back to tree id '" + FolderStorage.REAL_TREE_ID +"'.", e);
					treeID = FolderStorage.REAL_TREE_ID;
				}
	    	}
	    	return this.treeID;
	    }

	    private long getCombinedRequestTimeout() {
	    	if (Long.MIN_VALUE == this.combinedRequestTimeout) {
		        try {
		        	String value = factory.getConfigValue(COMBINED_REQUEST_TIMEOUT_PROPERTY, "10000");
		        	combinedRequestTimeout = Tools.parse(value);
				} catch (OXException e) {
					LOG.warn("falling back to a value of 10000 for the combined request timeout.", e);
					combinedRequestTimeout = 10000;
				}
	    	}
	    	return this.combinedRequestTimeout;
	    }

		/**
		 * Gets a contact object containing the basic information.
		 * 
		 * @param uid
		 * @return
		 * @throws AbstractOXException 
		 */
		public Contact get(String uid) throws OXException {
			Contact contact = this.getUidCache().get(uid);
			if (null == contact) {
				LOG.debug("Contact with UID '" + uid + "' not found, trying to get contact by filename...");
				contact = getFilenameCache().get(uid);
			}
			if (null == contact) {
				LOG.debug("Contact with UID '" + uid + "' not found.");
			}
			return contact;
		}
		
		/**
		 * Loads a contact with the supplied fields.
		 * 
		 * @param objectId the contact's object ID
		 * @param inFolder the contact' parent folder ID
		 * @return the contact
		 * @throws OXException
		 */
		private Contact load(int objectId, String inFolder, ContactField[] fields) throws OXException {
			Contact contact = null;
			if (null != fields) {
				contact = factory.getContactService().getContact(factory.getSession(), inFolder, Integer.toString(objectId), fields);
			} else {
				contact = factory.getContactService().getContact(factory.getSession(), inFolder, Integer.toString(objectId));
			}
			if (null == contact) {
				LOG.warn("Contact '" + objectId + "' in folder '" + inFolder + "' not found.");
			}
			return contact;
		}
		
		private Map<String, Contact> getUidCache() throws OXException {
			if (null == this.uidCache) {
				this.uidCache = generateUidCache();
			}
			return this.uidCache;
		}		
		
		private Map<String, Contact> getFilenameCache() throws OXException {
			if (null == this.filenameCache) {
				this.filenameCache = generateFilenameCache();
			}
			return this.filenameCache;
		}		
		
		private Map<String, Contact> generateUidCache() throws OXException {
			HashMap<String, Contact> cache = new HashMap<String, Contact>();			
			for (UserizedFolder folder : this.getFolders()) {
				int folderID = Integer.parseInt(folder.getID());
				SearchIterator<Contact> iterator = null;
				try {
					iterator = factory.getContactService().getAllContacts(factory.getSession(), folder.getID(), BASIC_FIELDS);
					while (iterator.hasNext()) {
						Contact contact = iterator.next();
						if (false == contact.containsUid() && false == this.tryAddUID(contact, folder)) {
							LOG.warn("No UID found in contact '" + contact.toString() + "', skipping.");
							continue;
						}
						contact.setParentFolderID(folderID);
						cache.put(contact.getUid(), prepare(contact));
					}
				} finally {
					close(iterator);
				}
			}	
			return cache;
		}
		
		private Map<String, Contact> generateFilenameCache() throws OXException {
			HashMap<String, Contact> cache = new HashMap<String, Contact>();
			for (Contact contact : getUidCache().values()) {
				if (null != contact.getFilename()) {
					cache.put(contact.getFilename(), contact);
				}
			}
			return cache;
		}
		
		private Contact prepare(Contact contact) throws OXException {
			if (contact.getMarkAsDistribtuionlist()) {
				filterMembers(contact);
			}
			return contact;
		}
		
		/**
		 * Filters members from distribution lists that are not meant to be 
		 * synchronized via the CardDAV interface, i.e. one-off members that
		 * don't reference an existing contact and also those members that 
		 * reference distribution list members from other folders.		 * 
		 * 
		 * @param distributionList the distribution list
		 * @throws OXException
		 */
		private void filterMembers(Contact distributionList) throws OXException {
			if (null != distributionList && null != distributionList.getDistributionList() && 
					0 < distributionList.getDistributionList().length) {
				List<DistributionListEntryObject> filteredMembers = new ArrayList<DistributionListEntryObject>();
				for (DistributionListEntryObject member : distributionList.getDistributionList()) {
					if (DistributionListEntryObject.INDEPENDENT != member.getEmailfield() && 
							this.hasFolderID(Integer.toString(member.getFolderID()))) {
						filteredMembers.add(member);						
					} else {
						LOG.debug("Excluding distribution list member '" + member.getEmailaddress() + "' from distribution list.");
					}
				}
				distributionList.setDistributionList(filteredMembers.toArray(new DistributionListEntryObject[filteredMembers.size()]));
			}
		}
		
		private boolean hasFolderID(String folderID) throws OXException {
			for (UserizedFolder folder : this.getFolders()) {
				if (folderID.equals(folder.getID())) {
					return true;
				}
			}
			return false;
		}
		
		private List<String> getFolderIDs() throws OXException {
			List<String> folderIDs = new ArrayList<String>();
			for (UserizedFolder folder : this.getFolders()) {
				folderIDs.add(folder.getID());
			}
			return folderIDs;
		}
		
		private boolean tryAddUID(Contact contact, UserizedFolder folder) {
            if (Permission.WRITE_OWN_OBJECTS < folder.getOwnPermission().getWritePermission()) {
            	LOG.debug("Adding uid for contact '" + contact.toString() + "'.");
				try {
					contact.setUid(UUID.randomUUID().toString());			
					factory.getContactService().updateContact(factory.getSession(), folder.getID(), 
							Integer.toString(contact.getObjectID()), contact, contact.getLastModified());
					return true;
				} catch (OXException e) {
					LOG.error(e.getMessage(), e);
				}
            }
			return false;
		}

		private static void close(final SearchIterator<Contact> iterator) {
			if (null != iterator) {
				try { 
					iterator.close();
				} catch (final OXException e) { 
					LOG.error(e.getMessage(), e); 
				}
			}
		}
	}
}
