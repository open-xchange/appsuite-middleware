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

package com.openexchange.carddav.resources;

import static com.openexchange.dav.DAVProtocol.protocolException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.carddav.GroupwareCarddavFactory;
import com.openexchange.dav.mixins.SyncToken;
import com.openexchange.dav.resources.DAVCollection;
import com.openexchange.dav.resources.DAVRootCollection;
import com.openexchange.dav.resources.PlaceholderCollection;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.ContactContentType;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

/**
 * {@link RootCollection} - Top-level collection for CardDAV.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class RootCollection extends DAVRootCollection {

    private static final String EXPOSED_COLLECTIONS_PROPERTY = "com.openexchange.carddav.exposedCollections";
    private static final String REDUCED_AGGREGATED_COLLECTION_PROPERTY = "com.openexchange.carddav.reducedAggregatedCollection";
    private static final String USER_AGENT_FOR_AGGREGATED_COLLECTION_PROPERTY = "com.openexchange.carddav.userAgentForAggregatedCollection";
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RootCollection.class);
    private static final String AGGREGATED_FOLDER_ID = "Contacts"; // folder ID needs to be exactly "Contacts" for backwards compatibility
    private static final String AGGREGATED_DISPLAY_NAME = "All Contacts";

    private final GroupwareCarddavFactory factory;
    private String exposedCollections = null;
    private Pattern userAgentForAggregatedCollection = null;
    private Boolean reducedAggregatedCollection = null;

    /**
     * Initializes a new {@link RootCollection}.
     *
     * @param factory the factory
     */
    public RootCollection(GroupwareCarddavFactory factory) {
    	super(factory, "Addressbooks");
    	this.factory = factory;
    	includeProperties(new SyncToken(this));
    }

	@Override
	public List<WebdavResource> getChildren() throws WebdavProtocolException {
		List<WebdavResource> children = new ArrayList<WebdavResource>();
		if (isUseAggregatedCollection()) {
			/*
			 * add the aggregated collection as child resource
			 */
		    if (isReducedAggregatedCollection()) {
		        children.add(createReducedAggregatedCollection());
            } else {
                children.add(createAggregatedCollection());
            }
			LOG.debug("{}: adding aggregated collection as child resource.", getUrl());
		}
		if (isUseFolderCollections()) {
			/*
			 * add one child resource per contact folder
			 */
			try {
				for (UserizedFolder folder : factory.getState().getFolders()) {
					children.add(createFolderCollection(folder));
					LOG.debug("{}: adding folder collection for folder '{}' as child resource.", getUrl(), folder.getName());
				}
			} catch (OXException e) {
				throw protocolException(getUrl(), e);
			}
		}
		LOG.debug("{}: got {} child resources.", getUrl(), children.size());
		return children;
	}

    /**
     * Gets a child collection from this collection by name. If the resource
     * does not yet exists, a placeholder contact resource is created.
     *
     * @param name the name of the resource
     * @return the child collection
     * @throws WebdavProtocolException
     */
	@Override
    public DAVCollection getChild(String name) throws WebdavProtocolException {
		if (isUseAggregatedCollection() && (AGGREGATED_FOLDER_ID.equals(name) || AGGREGATED_DISPLAY_NAME.equals(name))) {
			/*
			 * this is the aggregated collection
			 */
		    if (isReducedAggregatedCollection()) {
                return createReducedAggregatedCollection();
		    } else {
	            return createAggregatedCollection();
		    }
		}
		if (isUseFolderCollections()) {
	        try {
	            for (UserizedFolder folder : factory.getState().getFolders()) {
	                if (name.equals(folder.getID())) {
	                    LOG.debug("{}: found child collection by name '{}'", getUrl(), name);
	                    return createFolderCollection(folder);
	                }
	                if (null != folder.getMeta() && folder.getMeta().containsKey("resourceName") && name.equals(folder.getMeta().get("resourceName"))) {
	                    LOG.debug("{}: found child collection by resource name '{}'", getUrl(), name);
	                    return createFolderCollection(folder);
	                }
	            }
	            LOG.debug("{}: child collection '{}' not found, creating placeholder collection", getUrl(), name);
	            return new PlaceholderCollection<CommonObject>(factory, constructPathForChildResource(name), ContactContentType.getInstance(), FolderStorage.REAL_TREE_ID);
	        } catch (OXException e) {
	            throw protocolException(getUrl(), e);
	        }
		}
		throw protocolException(getUrl(), new Exception("child resource '" + name + "' not found"), HttpServletResponse.SC_NOT_FOUND);
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

    private Boolean isReducedAggregatedCollection() {
        if (null == this.reducedAggregatedCollection) {
            reducedAggregatedCollection = Boolean.TRUE;
            try {
                reducedAggregatedCollection = Boolean.parseBoolean(factory.getConfigValue(REDUCED_AGGREGATED_COLLECTION_PROPERTY, "true"));
            } catch (OXException e) {
                LOG.error("error getting reduced aggregated collection property from config, falling back to 'true'", e);
            }
        }
        return this.reducedAggregatedCollection;
    }

    private Pattern getUserAgentForAggregatedCollection() {
        if (null == this.userAgentForAggregatedCollection) {
            String regex = ".*CFNetwork.*Darwin.*|.*AddressBook.*CardDAVPlugin.*Mac_OS_X.*|.*Mac OS X.*AddressBook.*";
            try {
                regex = factory.getConfigValue(USER_AGENT_FOR_AGGREGATED_COLLECTION_PROPERTY, regex);
            } catch (OXException e) {
                LOG.error("error getting exposed collections from config, falling back to '{}'", regex, e);
            }
            userAgentForAggregatedCollection = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        }
        return userAgentForAggregatedCollection;
    }

	private boolean isUseAggregatedCollection() {
		return "2".equals(getExposedCollections()) || "0".equals(getExposedCollections()) && isAddressbookClient();
	}

	private boolean isUseFolderCollections() {
		return "1".equals(getExposedCollections()) || "0".equals(getExposedCollections()) && false == isAddressbookClient();
	}

	private boolean isAddressbookClient() {
		String userAgent = (String)factory.getSession().getParameter("user-agent");
		if (null != userAgent && 0 < userAgent.length() && null != getUserAgentForAggregatedCollection()) {
		    return getUserAgentForAggregatedCollection().matcher(userAgent).find();
		}
		return false;
	}

    private CardDAVCollection createFolderCollection(UserizedFolder folder) throws WebdavProtocolException {
        try {
            return factory.mixin(new CardDAVCollection(factory, constructPathForChildResource(folder.getID()), folder));
        } catch (OXException e) {
            throw protocolException(getUrl(), e);
        }
    }

    private CardDAVCollection createAggregatedCollection() throws WebdavProtocolException {
        try {
            return factory.mixin(new AggregatedCollection(factory, constructPathForChildResource(AGGREGATED_FOLDER_ID), AGGREGATED_DISPLAY_NAME));
        } catch (OXException e) {
            throw protocolException(getUrl(), e);
        }
    }

    private CardDAVCollection createReducedAggregatedCollection() throws WebdavProtocolException {
        try {
            return factory.mixin(new ReducedAggregatedCollection(factory, constructPathForChildResource(AGGREGATED_FOLDER_ID), AGGREGATED_DISPLAY_NAME));
        } catch (OXException e) {
            throw protocolException(getUrl(), e);
        }
    }

}
