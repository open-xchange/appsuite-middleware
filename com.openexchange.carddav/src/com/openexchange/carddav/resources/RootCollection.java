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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

import com.openexchange.carddav.GroupwareCarddavFactory;
import com.openexchange.carddav.mixins.DummySyncToken;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.log.LogFactory;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.Protocol.Property;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.helpers.AbstractCollection;

/**
 * {@link RootCollection} - Top-level collection for CardDAV.
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class RootCollection extends AbstractCollection {

	private static final String EXPOSED_COLLECTIONS_PROPERTY = "com.openexchange.carddav.exposedCollections";
    private static final Log LOG = LogFactory.getLog(RootCollection.class);
    private static final String DISPLAY_NAME = "Addressbooks";
    private static final String AGGREGATED_FOLDER_ID = "Contacts"; // folder ID needs to be exactly "Contacts" for backwards compatibility
    private static final String AGGREGATED_DISPLAY_NAME = "All Contacts";
    
    private final GroupwareCarddavFactory factory;
    private final WebdavPath url;
    private String exposedCollections = null;

    /**
     * Initializes a new {@link RootCollection}.
     * 
     * @param factory the factory
     */
    public RootCollection(GroupwareCarddavFactory factory) {
    	super();
    	this.factory = factory;
    	this.url = new WebdavPath();
        includeProperties(new DummySyncToken());
        LOG.debug(getUrl() + ": initialized.");
    }
    
    protected WebdavProtocolException protocolException(Throwable t) {
    	return protocolException(t, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    protected WebdavProtocolException protocolException(Throwable t, int statusCode) {
        LOG.error(t.getMessage(), t);
        return WebdavProtocolException.Code.GENERAL_ERROR.create(this.getUrl(), statusCode, t);
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

	@Override
	public List<WebdavResource> getChildren() throws WebdavProtocolException {
		List<WebdavResource> children = new ArrayList<WebdavResource>();
		if (isUseAggregatedCollection()) {
			/*
			 * add the aggregated collection as child resource
			 */
			children.add(new AggregatedCollection(factory, constructPathForChildResource(AGGREGATED_FOLDER_ID), AGGREGATED_DISPLAY_NAME));
			LOG.debug(getUrl() + ": adding aggregated collection as child resource.");
		} 
		if (isUseFolderCollections()) {
			/*
			 * add one child resource per contact folder
			 */
			try {
				for (UserizedFolder folder : factory.getState().getFolders()) {
					children.add(new FolderCollection(factory, constructPathForChildResource(folder), folder));
					LOG.debug(getUrl() + ": adding folder collection for folder '" + folder.getName() + "' as child resource.");
				}
			} catch (OXException e) {
				throw protocolException(e);
			}
		}
		LOG.debug(getUrl() + ": got " + children.size() + " child resources.");
		return children;
	}

	@Override
	public String getDisplayName() throws WebdavProtocolException {
		return DISPLAY_NAME;
	}

    /**
     * Constructs a string representing the WebDAV name for a folder resource.
     * 
     * @param folder the folder to construct the name for
     * @return the name
     */
	private String constructNameForChildResource(UserizedFolder folder) {
		return folder.getID();
    }

    private WebdavPath constructPathForChildResource(UserizedFolder folder) {
    	return constructPathForChildResource(constructNameForChildResource(folder));
    }

    private WebdavPath constructPathForChildResource(String id) {
    	return this.getUrl().dup().append(id);    	
    }

    /**
     * Gets a child collection from this collection by name. If the resource 
     * does not yet exists, a placeholder contact resource is created.
     * 
     * @param name the name of the resource
     * @return the child collection
     * @throws WebdavProtocolException
     */
	public CardDAVCollection getChild(String name) throws WebdavProtocolException {
		if (isUseAggregatedCollection() && (AGGREGATED_FOLDER_ID.equals(name) || AGGREGATED_DISPLAY_NAME.equals(name))) {
			/*
			 * this is the aggregated collection
			 */
			return new AggregatedCollection(factory,  constructPathForChildResource(AGGREGATED_FOLDER_ID), AGGREGATED_DISPLAY_NAME);
		} 
		if (isUseFolderCollections()) {
			/*
			 * search available folders
			 */
			try {
				List<UserizedFolder> folders = factory.getState().getFolders();
				// try folder ID first
				for (UserizedFolder folder : folders) {
					if (name.equals(constructNameForChildResource(folder))) {
						return new FolderCollection(factory, constructPathForChildResource(folder), folder);
					}
				}
				// try folder name
				for (UserizedFolder folder : folders) {
					if (folder.getName().equals(name)) {
						return new FolderCollection(factory, constructPathForChildResource(folder), folder);
					}
				}
				// try localized folder name
				Locale locale = factory.getUser().getLocale();
				for (UserizedFolder folder : folders) {
					if (folder.getLocalizedName(locale).equals(name)) {
						return new FolderCollection(factory, constructPathForChildResource(folder), folder);
					}
				}
			} catch (OXException e) {
				throw protocolException(e);
			}
		}
		throw protocolException(new Throwable("child resource '" + name + "' not found"), HttpServletResponse.SC_NOT_FOUND);
	}
	
	private String getExposedCollections() {
		if (null == this.exposedCollections) {
			exposedCollections = "0";
			try {
				exposedCollections = factory.getConfigValue(EXPOSED_COLLECTIONS_PROPERTY, "0");
			} catch (OXException e) {
				LOG.error("error getting exposed collections from config, falling back to '0'", e);
			}
		}
		return this.exposedCollections;
	}

	private boolean isUseAggregatedCollection() {
		return "2".equals(getExposedCollections()) || "0".equals(getExposedCollections()) && isAddressbookClient(); 
	}

	private boolean isUseFolderCollections() {
		return "1".equals(getExposedCollections()) || "0".equals(getExposedCollections()) && false == isAddressbookClient(); 
	}
	
	private boolean isAddressbookClient() {
		String userAgent = (String)factory.getSession().getParameter("user-agent");
		return null != userAgent && ( 
				(userAgent.contains("CFNetwork") && userAgent.contains("Darwin")) || 
				(userAgent.contains("AddressBook") && userAgent.contains("CardDAVPlugin") && userAgent.contains("Mac_OS_X")) && 
				(false == (userAgent.contains("iOS") && (userAgent.contains("dataaccessd") || userAgent.contains("Preferences")))) 
			);
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

	@Override
	public void create() throws WebdavProtocolException {
	}

	@Override
	public boolean exists() throws WebdavProtocolException {
		return true;
	}

	@Override
	public void save() throws WebdavProtocolException {
	}

	@Override
	public Date getCreationDate() throws WebdavProtocolException {
		return new Date(0);
	}

	@Override
	public Date getLastModified() throws WebdavProtocolException {
		return new Date(0);
	}

	@Override
	public void setDisplayName(String displayName) throws WebdavProtocolException {
	}

	@Override
	public void setCreationDate(Date date) throws WebdavProtocolException {
	}

}
