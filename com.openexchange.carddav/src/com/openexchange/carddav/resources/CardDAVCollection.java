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

package com.openexchange.carddav.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

import com.openexchange.carddav.CarddavProtocol;
import com.openexchange.carddav.GroupwareCarddavFactory;
import com.openexchange.carddav.Tools;
import com.openexchange.carddav.mixins.CTag;
import com.openexchange.carddav.mixins.SupportedReportSet;
import com.openexchange.carddav.mixins.SyncToken;
import com.openexchange.carddav.reports.Syncstatus;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.groupware.container.Contact;
import com.openexchange.log.LogFactory;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.Protocol.Property;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.WebdavStatusImpl;
import com.openexchange.webdav.protocol.helpers.AbstractCollection;

/**
 * {@link CardDAVCollection} - Abstract base class for CardDAV collections.
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class CardDAVCollection extends AbstractCollection {

    private static final Log LOG = LogFactory.getLog(CardDAVCollection.class);
    private static final Pattern LEGACY_FOLDER_NAME = Pattern.compile("f\\d+_(\\d+).vcf");
    private static final long OVERRIDE_LEGACY_FOLDERS = 11;

    protected GroupwareCarddavFactory factory;
    protected WebdavPath url;

    /**
     * Initializes a new {@link CardDAVCollection}.
     * 
     * @param factory the factory
     * @param url the WebDAV path
     */
    public CardDAVCollection(GroupwareCarddavFactory factory, WebdavPath url) {
        super();
        this.factory = factory;
        this.url = url;
        super.includeProperties(new SupportedReportSet(), new CTag(factory, this), new SyncToken(this)); 
        LOG.debug(getUrl() + ": initialized.");
    }
    
    protected WebdavProtocolException protocolException(Throwable t) {
    	return protocolException(t, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    protected WebdavProtocolException protocolException(Throwable t, int statusCode) {
        LOG.error(this.getUrl() + ": " + t.getMessage(), t);
        return WebdavProtocolException.Code.GENERAL_ERROR.create(this.getUrl(), statusCode, t);
    }
    
    /**
     * Gets all contacts that have been created or modified since the 
     * supplied time.
     * 
     * @param since the exclusive minimum modification time to consider  
     * @return the contacts
     * @throws OXException
     */
    protected abstract Collection<Contact> getModifiedContacts(Date since) throws OXException;

    /**
     * Gets all contacts that have been deleted since the supplied time.
     * 
     * @param since the exclusive minimum modification time to consider  
     * @return the contacts
     * @throws OXException
     */
    protected abstract Collection<Contact> getDeletedContacts(Date since) throws OXException;

    /**
     * Gets all contacts in the collection.
     * 
     * @return the contacts
     * @throws OXException
     */
    protected abstract Collection<Contact> getContacts() throws OXException;
    
    /**
     * Gets the ID of the folder that is used to create new contacts for 
     * this collection.
     * 
     * @return the folder ID
     */
    protected abstract String getFolderID() throws OXException;
    
    /**
     * Constructs a {@link WebdavPath} for a vCard child resource of this 
     * collection with the supplied UID.
     * 
     * @param uid the UID of the resource
     * @return the path
     */
    protected WebdavPath constructPathForChildResource(String uid) {
    	return this.getUrl().dup().append(uid + ".vcf");    	
    }

    /**
     * Constructs a {@link WebdavPath} for a vCard child resource of this 
     * collection with UID found in the supplied contact.
     * 
     * @param contact the contact represented by the resource
     * @return the path
     */
    protected WebdavPath constructPathForChildResource(Contact contact) {
		if (null != contact.getUserField19() && false == contact.getUserField19().equals(contact.getUid())) {
			// for MacOS 10.6 and iOS clients
	    	return constructPathForChildResource(contact.getUserField19());
		} else {
			return constructPathForChildResource(contact.getUid());
		}
    }

    /**
     * Extracts the folder ID from the supplied resource name, i.e. the 
     * part of a ox folder resource name representing the folder's id.
     *   
     * @param name the name of the resource
     * @return the folder ID, or <code>null</code> if none was found
     */
    private static String extractLegacyFolderID(String name) {
    	if (null != name && 0 < name.length() && 'f' == name.charAt(0)) {
            Matcher matcher = LEGACY_FOLDER_NAME.matcher(name);
            if (matcher.find()) {
                return matcher.group(1);
            }
    	}
    	return null;
    }

	@Override
	public List<WebdavResource> getChildren() throws WebdavProtocolException {
        try {
            List<WebdavResource> children = new ArrayList<WebdavResource>();
            for (Contact contact : this.getContacts()) {
            	children.add(new ContactResource(contact, this.factory, constructPathForChildResource(contact)));
            }
          	LOG.debug(this.getUrl() + ": added " + children.size() + " contact resources.");
            return children;
		} catch (OXException e) {
            throw protocolException(e);
		}
	}
    
	@Override
	public WebdavPath getUrl() {
		return this.url;
	}
	
	@Override
	protected WebdavFactory getFactory() {
		return this.factory;
	}

	@Override
	protected boolean isset(Property p) {
		int id = p.getId();
		return Protocol.GETCONTENTLANGUAGE != id && Protocol.GETCONTENTLENGTH != id && Protocol.GETETAG != id;
	}

	/**
	 * Gets an updated {@link Syncstatus} based on the supplied sync token for 
	 * this collection.
	 * 
	 * @param syncToken the sync token as supplied by the client
	 * @return the sync status
	 * @throws WebdavProtocolException
	 */
    public Syncstatus<WebdavResource> getSyncStatus(String token) throws WebdavProtocolException {
		long since = 0;
		if (null != token && 0 < token.length()) {
			/*
			 * check for overridden sync-token for this client
			 */
			String overrrideSyncToken = factory.getOverrideNextSyncToken();
			if (null != overrrideSyncToken && 0 < overrrideSyncToken.length()) {
				factory.setOverrideNextSyncToken(null);
				token = overrrideSyncToken;
				LOG.debug("Overriding sync token to '" + token + "' for user '" + this.factory.getUser() + "'.");
			}
			try {
				since = Long.parseLong(token);
			} catch (NumberFormatException e) {
				LOG.warn("Invalid sync token: '" + token + "', falling back to '0'.");								
			}
		}
		Syncstatus<WebdavResource> syncStatus = null;
		try {
			/*
			 * get sync-status
			 */
			syncStatus = this.getSyncStatus(new Date(since));
			if (OVERRIDE_LEGACY_FOLDERS == since) {
				/*
				 * report legacy simulated folder groups as deleted 
				 */
				addLegacyGroupsAsDeleted(syncStatus);
			}
			return syncStatus;
		} catch (OXException e) {
			throw protocolException(e);
		}
    }

	/**
	 * Create a 'sync-status' multistatus report considering all changes since 
	 * the supplied time. 
	 * 
	 * @param since the time
	 * @return the sync status
	 * @throws WebdavProtocolException
	 */
	private Syncstatus<WebdavResource> getSyncStatus(Date since) throws OXException {
		Syncstatus<WebdavResource> multistatus = new Syncstatus<WebdavResource>();
		Date nextSyncToken = new Date(since.getTime());
		/*
		 * new and modified contacts
		 */
		Collection<Contact> modifiedContacts = this.getModifiedContacts(since);
		for (Contact contact : modifiedContacts) {
			// add contact resource to multistatus
			ContactResource resource = new ContactResource(contact, factory, constructPathForChildResource(contact));
			int status = contact.getCreationDate().after(since) ? HttpServletResponse.SC_CREATED : HttpServletResponse.SC_OK;
			multistatus.addStatus(new WebdavStatusImpl<WebdavResource>(status, resource.getUrl(), resource));
			// remember aggregated last modified for next sync token 
			nextSyncToken = Tools.getLatestModified(nextSyncToken, contact.getLastModified());
		}
		/*
		 * deleted contacts
		 */
		Collection<Contact> deletedContacts = this.getDeletedContacts(since);
		for (Contact contact : deletedContacts) {
			// only include deleted contacts that were created before last synchronization,
			// only include contacts that are not also modified (due to move operations)
			if (null != contact.getCreationDate() && 
					(contact.getCreationDate().before(since) || contact.getCreationDate().equals(since)) && 
					false == contains(modifiedContacts, contact.getUid())) {
				// add contact resource to multistatus
				ContactResource resource = new ContactResource(contact, factory, constructPathForChildResource(contact));
				multistatus.addStatus(new WebdavStatusImpl<WebdavResource>(
						HttpServletResponse.SC_NOT_FOUND, resource.getUrl(), resource));
				// remember aggregated last modified for parent folder								
				nextSyncToken = Tools.getLatestModified(nextSyncToken, contact.getLastModified());
			}
		}
		/*
		 * Return response with new next sync-token in response
		 */
		multistatus.setToken(Long.toString(nextSyncToken.getTime()));
		return multistatus;
	}
	
	/**
	 * Adds WebDAV status for the formerly simulated folder groups of the 
	 * default contacts folder and the global addressbook with a status of
	 * "Deleted" to the supplied sync status. 
	 * 
	 * @param syncStatus the sync status to add the legacy groups
	 * @throws OXException 
	 */
	private void addLegacyGroupsAsDeleted(Syncstatus<WebdavResource> syncStatus) throws OXException {
		String name = String.format("f%d_%s", factory.getSession().getContextId(), factory.getState().getDefaultFolder()); 
		ContactResource defaultFolderResource = new ContactResource(factory, constructPathForChildResource(name), null);
		syncStatus.addStatus(new WebdavStatusImpl<WebdavResource>(
				HttpServletResponse.SC_NOT_FOUND, defaultFolderResource.getUrl(), defaultFolderResource));
		name = String.format("f%d_%s", factory.getSession().getContextId(), factory.getFolderService().getFolder(
				FolderStorage.REAL_TREE_ID, FolderStorage.GLOBAL_ADDRESS_BOOK_ID, this.factory.getSession(), null)); 
		ContactResource gabResource = new ContactResource(factory, constructPathForChildResource(name), null);
		syncStatus.addStatus(new WebdavStatusImpl<WebdavResource>(
				HttpServletResponse.SC_NOT_FOUND, gabResource.getUrl(), gabResource));
	}
	
	private static boolean contains(Collection<Contact> contacts, String uid) {
		for (Contact contact : contacts) {
			if (contact.getUid().equals(uid)) {
				return true;
			}
		}
		return false;
	}

    /**
     * Gets a child resource from this collection by name. If the resource 
     * does not yet exists, a placeholder contact resource is created.
     * 
     * @param name the name of the resource
     * @return the child resource
     * @throws WebdavProtocolException
     */
	public CardDAVResource getChild(String name) throws WebdavProtocolException {
		if (null != extractLegacyFolderID(name)) {
			LOG.info(getUrl() + ": client requests legacy simulated group resource '" + name + 
					"', overriding next sync token to '11' for recovery.");
			this.factory.setOverrideNextSyncToken("11");
			throw protocolException(new Throwable("child resource '" + name + "' not found"), HttpServletResponse.SC_NOT_FOUND);
		}
    	try {
        	String uid = Tools.extractUID(name);
    		Contact contact = this.factory.getState().load(uid);
    		if (null != contact) {
              	LOG.debug(this.getUrl() + ": found child resource by name '" + name + "'");
    			return new ContactResource(contact, this.factory, constructPathForChildResource(contact));
    		} else {
              	LOG.debug(this.getUrl() + ": child resource '" + name + "' not found, creating placeholder resource");
    			return new ContactResource(factory, constructPathForChildResource(uid), getFolderID());
    		}
    	} catch (OXException e) {
    		throw protocolException(e);
		}
	}
	
    @Override
    public String getResourceType() throws WebdavProtocolException {
        return super.getResourceType() + CarddavProtocol.ADDRESSBOOK;
    }

	@Override
	public String getSource() throws WebdavProtocolException {
		return null;
	}

	@Override
	public void lock(WebdavLock lock) throws WebdavProtocolException {
	}

	@Override
	public List<WebdavLock> getLocks() throws WebdavProtocolException {
        return Collections.emptyList();
	}

	@Override
	public WebdavLock getLock(String token) throws WebdavProtocolException {
		return null;
	}

	@Override
	public void unlock(String token) throws WebdavProtocolException {
	}

	@Override
	public List<WebdavLock> getOwnLocks() throws WebdavProtocolException {
        return Collections.emptyList();
	}

	@Override
	public WebdavLock getOwnLock(String token) throws WebdavProtocolException {
		return null;
	}

	@Override
	protected void internalDelete() throws WebdavProtocolException {
	}

	@Override
	protected List<WebdavProperty> internalGetAllProps() throws WebdavProtocolException {
		return null;
	}

	@Override
	protected void internalPutProperty(WebdavProperty prop) throws WebdavProtocolException {
	}

	@Override
	protected void internalRemoveProperty(String namespace, String name) throws WebdavProtocolException {
	}

	@Override
	protected WebdavProperty internalGetProperty(String namespace, String name) throws WebdavProtocolException {
		return null;
	}
    
}
