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

package com.openexchange.caldav.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.caldav.Tools;
import com.openexchange.caldav.mixins.CTag;
import com.openexchange.caldav.mixins.Owner;
import com.openexchange.caldav.mixins.SyncToken;
import com.openexchange.caldav.reports.Syncstatus;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.ldap.User;
import com.openexchange.webdav.acl.mixins.CurrentUserPrivilegeSet;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.WebdavStatusImpl;
import com.openexchange.webdav.protocol.helpers.AbstractResource;

/**
 * {@link CommonFolderCollection} - Abstract base class for WebDAV collections 
 * containing groupware objects.
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class CommonFolderCollection<T extends CommonObject> extends CommonCollection {

    protected WebdavFactory factory;
    protected UserizedFolder folder;
    protected int folderID;
    
    private Date lastModified = null;

    /**
     * Initializes a new {@link CommonFolderCollection}.
     * 
     * @param factory the factory
     * @param url the WebDAV path
     * @throws OXException 
     */
    public CommonFolderCollection(WebdavFactory factory, WebdavPath url, UserizedFolder folder) throws OXException {
        super(factory, url);
        this.factory = factory;
        this.folder = folder;
        this.folderID = Tools.parse(folder.getID());
        includeProperties(new CurrentUserPrivilegeSet(folder.getOwnPermission()), new CTag(this), new SyncToken(this), new Owner(this));
        LOG.debug(getUrl() + ": initialized.");
    }
    
    @Override
    public List<WebdavResource> getChildren() throws WebdavProtocolException {
        try {
            List<WebdavResource> children = new ArrayList<WebdavResource>();
            for (T object : this.getObjects()) {
                children.add(createResource(object, constructPathForChildResource(object)));
            }
            LOG.debug(this.getUrl() + ": added " + children.size() + " child resources.");
            return children;
        } catch (OXException e) {
            throw protocolException(e);
        }
    }

    /**
     * Gets a child resource from this collection by name. If the resource 
     * does not yet exists, a placeholder resource is created.
     * 
     * @param name the name of the resource
     * @return the child resource
     * @throws WebdavProtocolException
     */
    @Override
    public AbstractResource getChild(String name) throws WebdavProtocolException {
        try {
            String resourceName = Tools.extractResourceName(name, getFileExtension());
            T object = this.getObject(resourceName);
            if (null != object) {
                LOG.debug(this.getUrl() + ": found child resource by name '" + name + "'");
                return createResource(object, constructPathForChildResource(object));
            } else {
                LOG.debug(this.getUrl() + ": child resource '" + name + "' not found, creating placeholder resource");
                return createResource(null, constructPathForChildResource(name));
            }
        } catch (OXException e) {
            throw protocolException(e);
        }
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
            try {
                since = Long.parseLong(token);
            } catch (NumberFormatException e) {
                LOG.warn("Invalid sync token: '" + token + "', falling back to '0'.");                              
            }
        }
        try {
            /*
             * get sync-status
             */
            return this.getSyncStatus(new Date(since));
        } catch (OXException e) {
            throw protocolException(e);
        }
    }

    @Override
    public Date getCreationDate() throws WebdavProtocolException {
        return this.folder.getCreationDateUTC();
    }

    @Override
    public String getDisplayName() throws WebdavProtocolException {
        return this.folder.getName();
    }

    @Override
    public Date getLastModified() throws WebdavProtocolException {
        if (null == this.lastModified) {
            lastModified = this.folder.getLastModifiedUTC();
            try {
                /*
                 * new and modified objects
                 */
                Collection<T> modifiedObjects = this.getModifiedObjects(lastModified);
                for (T object : modifiedObjects) {
                    lastModified = Tools.getLatestModified(lastModified, object);
                }
                /*
                 * deleted objects
                 */
                Collection<T> deletedObjects = this.getDeletedObjects(lastModified);
                for (T object : deletedObjects) {
                    lastModified = Tools.getLatestModified(lastModified, object);
                }
            } catch (OXException e) {
                throw protocolException(e);
            }
        }
        return lastModified;
    }
    
    /**
     * Gets the folder of this collection.
     * 
     * @return the folder
     */
    public UserizedFolder getFolder() {
        return folder;
    }
    
    /**
     * Gets all objects that have been created or modified since the 
     * supplied time.
     * 
     * @param since the exclusive minimum modification time to consider  
     * @return the objects
     * @throws OXException
     */
    protected abstract Collection<T> getModifiedObjects(Date since) throws OXException;

    /**
     * Gets all objects that have been deleted since the supplied time.
     * 
     * @param since the exclusive minimum modification time to consider  
     * @return the objects
     * @throws OXException
     */
    protected abstract Collection<T> getDeletedObjects(Date since) throws OXException;

    /**
     * Gets all groupware objects in the collection.
     * 
     * @return the objects
     * @throws OXException
     */
    protected abstract Collection<T> getObjects() throws OXException;

    protected abstract T getObject(String resourceName) throws OXException;

    protected abstract AbstractResource createResource(T object, WebdavPath url) throws OXException;
    
    protected abstract String getFileExtension();
    
    public abstract User getOwner() throws OXException;
    
    /**
     * Constructs a {@link WebdavPath} for a child resource of this
     * collection with the resource name found in the supplied object.
     * 
     * @param object the groupware object represented by the resource
     * @return the path
     */
    protected WebdavPath constructPathForChildResource(T object) {
        String fileName = object.getFilename();
        if (null == fileName || 0 == fileName.length()) {
            fileName = object.getUid();
        }
        String fileExtension = getFileExtension().toLowerCase();
        if (false == fileExtension.startsWith(".")) {
            fileExtension = "." + fileExtension;
        }
        return constructPathForChildResource(fileName + fileExtension);
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
        boolean initialSync = 0 == since.getTime();
		/*
		 * new and modified objects
		 */
		Collection<T> modifiedObjects = this.getModifiedObjects(since);
		for (T object : modifiedObjects) {
			// add resource to multistatus
			WebdavResource resource = createResource(object, constructPathForChildResource(object));
			int status = null != object.getCreationDate() && object.getCreationDate().after(since) ? 
			    HttpServletResponse.SC_CREATED : HttpServletResponse.SC_OK;
			multistatus.addStatus(new WebdavStatusImpl<WebdavResource>(status, resource.getUrl(), resource));
			// remember aggregated last modified for next sync token 
			nextSyncToken = Tools.getLatestModified(nextSyncToken, object);
		}
		/*
		 * deleted objects
		 */
		if (false == initialSync) {
		    Collection<T> deletedObjects = this.getDeletedObjects(since);
    		for (T object : deletedObjects) {
    			// only include objects that are not also modified (due to move operations)
    			if (null != object.getUid() && false == contains(modifiedObjects, object.getUid())) {
    				// add resource to multistatus
    				WebdavResource resource = createResource(object, constructPathForChildResource(object));
    				multistatus.addStatus(new WebdavStatusImpl<WebdavResource>(
    						HttpServletResponse.SC_NOT_FOUND, resource.getUrl(), resource));
    				// remember aggregated last modified for parent folder
    				nextSyncToken = Tools.getLatestModified(nextSyncToken, object);
    			}
    		}
		}
		/*
		 * Return response with new next sync-token in response
		 */
		multistatus.setToken(Long.toString(nextSyncToken.getTime()));
		return multistatus;
	}	
	   
	private static <T extends CommonObject> boolean contains(Collection<T> objects, String uid) {
		for (T object : objects) {
			if (uid.equals(object.getUid())) {
				return true;
			}
		}
		return false;
	}

}
