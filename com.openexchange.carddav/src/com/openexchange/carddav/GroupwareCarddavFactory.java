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

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

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
import org.apache.commons.logging.LogFactory;

import com.openexchange.carddav.reports.Syncstatus;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.ContactService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
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
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.session.SessionHolder;
import com.openexchange.user.UserService;
import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.WebdavStatusImpl;
import com.openexchange.webdav.protocol.helpers.AbstractWebdavFactory;

/**
 * {@link GroupwareCarddavFactory}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class GroupwareCarddavFactory extends AbstractWebdavFactory {

	public static final CarddavProtocol PROTOCOL = new CarddavProtocol();

	private static final String OVERRIDE_NEXT_SYNC_TOKEN_PROPERTY = "com.openexchange.carddav.overridenextsynctoken";
	private static final Log LOG = LogFactory.getLog(GroupwareCarddavFactory.class);
	private static final int SC_DELETED = 404;

	private final FolderService folders;
	private final SessionHolder sessionHolder;
	private final ThreadLocal<State> stateHolder = new ThreadLocal<State>();
	private final ConfigViewFactory configs;
	private final UserService users;
	private final ContactService contactService;	
	
	public GroupwareCarddavFactory(final FolderService folders, final SessionHolder sessionHolder, 
			final ConfigViewFactory configs, final UserService users, final ContactService contactService) {
		super();
		this.folders = folders;
		this.sessionHolder = sessionHolder;
		this.configs = configs;
		this.users = users;
		this.contactService = contactService;
	}
	
	@Override
	public void beginRequest() {
		super.beginRequest();
		stateHolder.set(new State(this));
	}

	@Override
	public void endRequest(final int status) {
		stateHolder.set(null);
		super.endRequest(status);
	}

	@Override
	public CarddavProtocol getProtocol() {
		return PROTOCOL;
	}

	@Override
	public WebdavCollection resolveCollection(final WebdavPath url) throws WebdavProtocolException {
		if (url.size() > 1) {
			throw WebdavProtocolException.Code.GENERAL_ERROR.create(url, SC_DELETED);
		}
		if (isRoot(url)) {
			return mixin(new RootCollection(this));
		}
		return mixin(new AggregatedCollection(url, this));
	}

	// TODO: i18n

	public boolean isRoot(final WebdavPath url) {
		return url.size() == 0;
	}

	@Override
	public WebdavResource resolveResource(final WebdavPath url) throws WebdavProtocolException {
		if (url.size() == 2) {
			return mixin(((AggregatedCollection) resolveCollection(url.parent())).getChild(url.name()));
		}
		return resolveCollection(url);
	}

	public FolderService getFolderService() {
		return folders;
	}

	public OXFolderManager getOXFolderManager() throws OXException {
		return OXFolderManager.getInstance(getSession());
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

	public OXFolderAccess getOXFolderAccess() {
		return new OXFolderAccess(getContext());
	}

	public ConfigView getConfigView() throws OXException {
		return configs.getView(sessionHolder.getSessionObject().getUserId(), sessionHolder.getSessionObject().getContextId());
	}

	public User resolveUser(final int uid) throws WebdavProtocolException {
		try {
			return users.getUser(uid, getContext());
		} catch (final OXException e) {
			LOG.error(e.getMessage(), e);
			throw WebdavProtocolException.Code.GENERAL_ERROR.create(new WebdavPath(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	public Syncstatus<WebdavResource> getSyncStatus(final Date since) throws WebdavProtocolException {
		final Syncstatus<WebdavResource> multistatus = new Syncstatus<WebdavResource>();
		final TIntObjectMap<Date> contentsLastModified = new TIntObjectHashMap<Date>();
		Date nextSyncToken = new Date(since.getTime());
		final AggregatedCollection collection = new AggregatedCollection(new WebdavPath().append("Contacts"), this);
		try {
			/*
			 * new and modified contacts
			 */
			final List<Contact> modifiedContacts = this.getState().getModifiedContacts(since);
			for (final Contact contact : modifiedContacts) {
				// add contact resource to multistatus
				final ContactResource resource = new ContactResource(collection, this, contact);
				final int status = contact.getCreationDate().after(since) ? HttpServletResponse.SC_CREATED : HttpServletResponse.SC_OK;
				multistatus.addStatus(new WebdavStatusImpl<WebdavResource>(status, resource.getUrl(), resource));
				// remember aggregated last modified for parent folder								
				final Date contactLastModified = contact.getLastModified();
				final int folderID = contact.getParentFolderID();
				if (false == contentsLastModified.containsKey(folderID) || contactLastModified.after(contentsLastModified.get(folderID))) {
					contentsLastModified.put(folderID, contactLastModified);
				}
				// remember aggregated last modified for next sync token 
				nextSyncToken = Tools.getLatestModified(nextSyncToken, contactLastModified);
			}
			/*
			 * deleted contacts
			 */
			final List<Contact> deletedContacts = this.getState().getDeletedContacts(since);
			for (final Contact contact : deletedContacts) {
				// add contact resource to multistatus
				final ContactResource resource = new ContactResource(collection, this, contact);
				multistatus.addStatus(new WebdavStatusImpl<WebdavResource>(SC_DELETED, resource.getUrl(), resource));
				// remember aggregated last modified for parent folder								
				final Date contactLastModified = contact.getLastModified();
				final int folderID = contact.getParentFolderID();
				if (false == contentsLastModified.containsKey(folderID) || contactLastModified.after(contentsLastModified.get(folderID))) {
					contentsLastModified.put(folderID, contactLastModified);
				}
				// remember aggregated last modified for next sync token 								
				nextSyncToken = Tools.getLatestModified(nextSyncToken, contactLastModified);
			}
			/*
			 * folders
			 */			
			final List<UserizedFolder> folders = this.getState().getFolders();
			for (final UserizedFolder folder : folders) {
				// determine effective last modified of folder and its content
				Date folderLastModified = folder.getLastModifiedUTC();
				final Date folderContentsLastModified = contentsLastModified.get(Integer.parseInt(folder.getID()));
				if (null != folderContentsLastModified && folderContentsLastModified.after(folderLastModified)) {
					folderLastModified = folderContentsLastModified;
				}
				if (folderLastModified.after(since)) {
					// add folder resource to multistatus
					final FolderGroupResource resource = new FolderGroupResource(collection, this, folder);
					resource.overrrideLastModified(folderLastModified);
					final int status = folder.getCreationDate().after(since) ? HttpServletResponse.SC_CREATED : HttpServletResponse.SC_OK;
					multistatus.addStatus(new WebdavStatusImpl<WebdavResource>(status, resource.getUrl(), resource));
					// remember aggregated last modified for next sync token
					nextSyncToken = Tools.getLatestModified(nextSyncToken, folderLastModified);
				}				
			}
			multistatus.setToken(Long.toString(nextSyncToken.getTime()));
			// TODO: Deleted Folders
			return multistatus;
		} catch (final OXException e) {
			LOG.error(e.getMessage(), e);
			throw WebdavProtocolException.Code.GENERAL_ERROR.create(new WebdavPath(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
		}
	}
	
	/**
	 * Sets the next sync token for the current user to <code>"0"</code>, enforcing the next sync status report to contain all changes 
	 * independently of the sync token supplied by the client, thus emulating some kind of slow-sync this way. 
	 */
	public void overrideNextSyncToken() {
		this.setOverrideNextSyncToken("0");		
	}

	/**
	 * Sets the next sync token for the current user to the supplied value.
	 * @param value
	 */
	public void setOverrideNextSyncToken(final String value) {
		try {
			this.users.setUserAttribute(OVERRIDE_NEXT_SYNC_TOKEN_PROPERTY, value, this.getUser().getId(), this.getContext());
		} catch (final OXException e) {
			LOG.error(e.getMessage(), e);
		}
	}
	
	/**
	 * Gets a value indicating the overridden sync token for the current user if defined 
	 * @return
	 */
	public String getOverrideNextSyncToken() {
		try {
			return this.users.getUserAttribute(OVERRIDE_NEXT_SYNC_TOKEN_PROPERTY, this.getUser().getId(), this.getContext());
		} catch (final OXException e) {
			LOG.error(e.getMessage(), e);
		}
		return null;
	}
	
	/**
	 * 
	 * @param token
	 * @return
	 * @throws WebdavProtocolException
	 */
	public Syncstatus<WebdavResource> getSyncStatusSince(String token) throws WebdavProtocolException {
		long since = 0;
		if (null != token && 0 < token.length()) {
			final String overrrideSyncToken = this.getOverrideNextSyncToken();
			if (null != overrrideSyncToken && 0 < overrrideSyncToken.length()) {
				this.setOverrideNextSyncToken(null);
				token = overrrideSyncToken;				
				LOG.debug("Overriding sync token to '" + token + "' for user '" + this.getUser() + "'.");
			}
			try {
				since = Long.parseLong(token);
			} catch (final NumberFormatException e) {
				LOG.warn("Invalid sync token: '" + token + "', falling back to '0'.");								
			}
		}		
		return this.getSyncStatus(new Date(since));				
	}	
		
	public static final class State {

		private static final ContactField[] BASIC_FIELDS = { 
			ContactField.OBJECT_ID, ContactField.LAST_MODIFIED, ContactField.CREATION_DATE, ContactField.MARK_AS_DISTRIBUTIONLIST, 
			ContactField.UID 
		};
		
		private final GroupwareCarddavFactory factory;
		private Map<String, Contact> uidCache = null;
		private List<UserizedFolder> allFolders = null;		
		private HashSet<String> folderBlacklist = null;		
		private int defaultFolderId = Integer.MIN_VALUE;
		private ContactService contactService = null;
		
		/**
		 * Initializes a new {@link State}
		 * @param factory
		 */
		public State(final GroupwareCarddavFactory factory) {
			super();
			this.factory = factory;
		}
		
		private ContactService getContactService() throws OXException {
			if (null == this.contactService) {
				contactService = this.factory.getContactService();
			}
			return contactService;
		}

		/**
		 * Loads a {@link Contact} containing all data identified by the supplied uid
		 * @param uid
		 * @return
		 * @throws AbstractOXException
		 */
		public Contact load(final String uid) throws OXException {
			final Contact contact = this.get(uid);
			if (null == contact) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Contact '" + uid + "' not found, unable to load.");
				}
				return null;
			} else {
				return this.load(contact);
			}
		}

		/**
		 * Loads a {@link Contact} containing all data identified by object- 
		 * and parent folder id found in the supplied contact.
		 * @param contact
		 * @return
		 * @throws OXException
		 * @throws ContextException
		 */
		public Contact load(final Contact contact) throws OXException {
			if (null == contact) {
				throw new IllegalArgumentException("contact is null");
			} else if (false == contact.containsObjectID() || false == contact.containsParentFolderID()) {
				throw new IllegalArgumentException("need at least object- and parent folder id");
			}
			return this.load(contact.getObjectID(), contact.getParentFolderID());
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
		
		/**
		 * Gets all contacts from the folder with the supplied id, each 
		 * containing the basic information as defined by the 
		 * <code>FIELDS_FOR_ALL_REQUEST</code> array.
		 * @return
		 * @throws AbstractOXException
		 */
		public List<Contact> getContacts(final int folderId) throws OXException {
			final List<Contact> contacts = new ArrayList<Contact>();
			final Collection<Contact> allContacts = this.getContacts();
			for (final Contact contact : allContacts) {
				if (folderId == contact.getParentFolderID()) {
					contacts.add(contact);
				}
			}			
			return contacts;
		}
		
		/**
		 * Gets the id of the default contact folder.
		 * @return
		 * @throws OXException
		 */
		public int getDefaultFolderId() throws OXException {
			if (Integer.MIN_VALUE == this.defaultFolderId) {
				this.defaultFolderId = this.factory.getOXFolderAccess().getDefaultFolder(
						this.factory.getUser().getId(), FolderObject.CONTACT).getObjectID();
			}
			return this.defaultFolderId;
		}
		
		/**
		 * Gets the folder identified with the supplied id.
		 * @param id
		 * @return
		 * @throws OXException 
		 * @throws ConfigCascadeException 
		 * @throws FolderException 
		 */
		public UserizedFolder getFolder(final String id) throws OXException {
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
		public synchronized List<UserizedFolder> getFolders() throws OXException {
			if (null == this.allFolders) {
				if (CarddavProtocol.REDUCED_FOLDER_SET) {
					this.allFolders = getReducedFolders();
				} else {
					this.allFolders = getVisibleFolders();
				}
			}
			return this.allFolders;
		}
		
	    private List<UserizedFolder> getReducedFolders() throws OXException {
	    	final List<UserizedFolder> folders = new ArrayList<UserizedFolder>();
			final FolderService folderService = this.factory.getFolderService();
			final UserizedFolder globalAddressBookFolder = folderService.getFolder(
					FolderStorage.REAL_TREE_ID, FolderStorage.GLOBAL_ADDRESS_BOOK_ID, this.factory.getSession(), null);
			if (false == this.blacklisted(globalAddressBookFolder)) {
				folders.add(globalAddressBookFolder);
			}
			final UserizedFolder defaultContactsFolder = folderService.getFolder(
					FolderStorage.REAL_TREE_ID, Integer.toString(this.getDefaultFolderId()), this.factory.getSession(), null);
			if (false == this.blacklisted(defaultContactsFolder)) {
				folders.add(defaultContactsFolder);
			}
			return folders;
	    }
		
		/**
		 * Gets a list of all visible folders.
		 * @return
		 * @throws FolderException
		 */
	    private List<UserizedFolder> getVisibleFolders() throws OXException {
	    	final List<UserizedFolder> folders = new ArrayList<UserizedFolder>();
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
                if (Permission.READ_OWN_OBJECTS < folder.getOwnPermission().getReadPermission() && false == this.blacklisted(folder)) {
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
			Date lastModified = new Date(0);
			final List<UserizedFolder> folders = this.getFolders();
			for (final UserizedFolder folder : folders) {
				lastModified = Tools.getLatestModified(lastModified, this.getLastModified(folder));
			}
			return lastModified;
		}	
		
		/**
		 * Gets the last modification time of the supplied folder, including its contents. 
		 * This covers the folder's last modification time itself, and both updated and 
		 * deleted items inside the folder.
		 * @param folderId
		 * @return
		 * @throws AbstractOXException
		 */
		public Date getLastModified(final UserizedFolder folder) throws OXException {
			final ContactField[] fields = new ContactField[] { ContactField.LAST_MODIFIED };
			Date lastModified = folder.getLastModifiedUTC();
			SearchIterator<Contact> iterator = null;
			try {
				iterator = getContactService().getModifiedContacts(factory.getSession(), folder.getID(), lastModified, fields);			
				while (iterator.hasNext()) {
					lastModified = Tools.getLatestModified(lastModified, iterator.next());
				}
			} finally {
				close(iterator);
			}				
			try {
				iterator = getContactService().getDeletedContacts(factory.getSession(), folder.getID(), lastModified, fields);			
				while (iterator.hasNext()) {
					lastModified = Tools.getLatestModified(lastModified, iterator.next());
				}
			} finally {
				close(iterator);
			}				
			return lastModified;
		}
		
		/**
		 * Gets the last modification time of the supplied folder, including its contents. This covers both updated and deleted items.
		 * @param folderId
		 * @return
		 * @throws AbstractOXException
		 */
		public Date getLastModified(final int folderId) throws OXException {
			final List<UserizedFolder> folders = this.getFolders();
			for (final UserizedFolder folder : folders) {
				final int id = Integer.parseInt(folder.getID());
				if (id == folderId) {
					return this.getLastModified(folder);
				}
			}
			throw FolderExceptionErrorMessage.INVALID_FOLDER_ID.create(folderId);
		}	
		
		/**
		 * Gets an aggregated list of all modified contacts in all folders since the supplied {@link Date}.
		 * @param since
		 * @return
		 * @throws AbstractOXException
		 */
		public List<Contact> getModifiedContacts(final Date since) throws OXException  {
			final List<Contact> contacts = new ArrayList<Contact>();
			final List<UserizedFolder> folders = this.getFolders();
			for (final UserizedFolder folder : folders) {
				final int folderId = Integer.parseInt(folder.getID());
				contacts.addAll(this.getModifiedContacts(since, folderId));
			}
			return contacts;
		}
		
		/**
		 * Gets a list of all modified contacts in a folder since the supplied {@link Date}.
		 * @param since
		 * @param folderId
		 * @return
		 * @throws AbstractOXException
		 */
		public List<Contact> getModifiedContacts(final Date since, final int folderId) throws OXException  {
			final List<Contact> contacts = new ArrayList<Contact>();
			SearchIterator<Contact> iterator = null;
			try {
				iterator = getContactService().getModifiedContacts(factory.getSession(), Integer.toString(folderId), since, BASIC_FIELDS);
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
		public List<Contact> getDeletedContacts(final Date since) throws OXException  {
			final List<Contact> contacts = new ArrayList<Contact>();
			final List<UserizedFolder> folders = this.getFolders();
			for (final UserizedFolder folder : folders) {
				final int folderId = Integer.parseInt(folder.getID());
				contacts.addAll(this.getDeletedContacts(since, folderId));
			}
			return contacts;
		}
		
		/**
		 * Gets an aggregated list of all deleted contacts in a folder since the supplied {@link Date}.
		 * @param since
		 * @param folderId
		 * @return
		 * @throws AbstractOXException
		 */
		public List<Contact> getDeletedContacts(final Date since, final int folderId) throws OXException  {
			final List<Contact> contacts = new ArrayList<Contact>();
			SearchIterator<Contact> iterator = null;
			try {
				iterator = getContactService().getDeletedContacts(factory.getSession(), Integer.toString(folderId), since, BASIC_FIELDS);
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
		private boolean blacklisted(final UserizedFolder userizedFolder) {
			if (null == this.folderBlacklist) {
				String ignoreFolders = null;
				try {
					ignoreFolders = factory.getConfigView().get("com.openexchange.carddav.ignoreFolders", String.class);
				} catch (final OXException e) {
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
		 * Gets a contact object containing the basic information as defined by
		 * the <code>FIELDS_FOR_ALL_REQUEST</code> array.
		 * @param uid
		 * @return
		 * @throws AbstractOXException 
		 */
		public Contact get(final String uid) throws OXException {			
			return this.getUidCache().get(uid);
		}
		
		/**
		 * 
		 * @param objectId
		 * @param inFolder
		 * @return
		 * @throws OXException
		 * @throws ContextException
		 */
		private Contact load(final int objectId, final int inFolder) throws OXException {
			final Contact contact = getContactService().getContact(factory.getSession(), Integer.toString(inFolder), 
					Integer.toString(objectId));
			if (null == contact) {
				LOG.warn("Contact '" + objectId + "' in folder '" + inFolder + "' not found.");
			}
			return contact;
		}
		
		private synchronized Map<String, Contact> getUidCache() throws OXException {
			if (null == this.uidCache) {
				this.uidCache = generateUidCache();
			}
			return this.uidCache;
		}		
		
		private Map<String, Contact> generateUidCache() throws OXException {
			final HashMap<String, Contact> cache = new HashMap<String, Contact>();			
			final List<UserizedFolder> folders = this.getFolders(); 
			for (final UserizedFolder folder : folders) {
				final int folderId = Integer.parseInt(folder.getID());
				SearchIterator<Contact> iterator = null;
				try {
					iterator = getContactService().getAllContacts(factory.getSession(), folder.getID(), BASIC_FIELDS);
					while (iterator.hasNext()) {
						final Contact contact = iterator.next();
						if (contact.getMarkAsDistribtuionlist()) {
							continue;
						} 
						contact.setParentFolderID(folderId);
						if (false == contact.containsUid() && false == this.tryAddUID(contact, folder)) {
							LOG.warn("No UID found in contact '" + contact.toString() + "', skipping.");
							continue;
						}
						cache.put(contact.getUid(), contact);
					}
				} finally {
					close(iterator);
				}
			}	
			return cache;
		}
		
		
		private boolean tryAddUID(final Contact contact, final UserizedFolder folder) {
            if (Permission.WRITE_OWN_OBJECTS < folder.getOwnPermission().getWritePermission()) {
            	LOG.debug("Adding uid for contact '" + contact.toString() + "'.");
				try {
					contact.setUid(UUID.randomUUID().toString());			
					getContactService().updateContact(factory.getSession(), folder.getID(), Integer.toString(contact.getObjectID()), 
							contact, contact.getLastModified());
					return true;
				} catch (final OXException e) {
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
