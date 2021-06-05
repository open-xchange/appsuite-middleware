/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.carddav.resources;

import static com.openexchange.carddav.Tools.getFoldersHash;
import static com.openexchange.dav.DAVProtocol.protocolException;
import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.carddav.AggregatedCollectionMode;
import com.openexchange.carddav.CardDAVProperty;
import com.openexchange.carddav.GroupwareCarddavFactory;
import com.openexchange.config.lean.DefaultProperty;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.dav.mixins.CurrentUserPrivilegeSet;
import com.openexchange.dav.mixins.SyncToken;
import com.openexchange.dav.resources.DAVCollection;
import com.openexchange.dav.resources.DAVRootCollection;
import com.openexchange.dav.resources.PlaceholderCollection;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.ContactsContentType;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.java.Enums;
import com.openexchange.java.Strings;
import com.openexchange.login.Interface;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

/**
 * {@link RootCollection} - Top-level collection for CardDAV.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class RootCollection extends DAVRootCollection {

    private final GroupwareCarddavFactory factory;

    private String exposedCollections;
    private Pattern userAgentForAggregatedCollection;
    private AggregatedCollectionMode aggregatedCollectionMode;

    /**
     * Initializes a new {@link RootCollection}.
     *
     * @param factory the factory
     */
    public RootCollection(GroupwareCarddavFactory factory) throws WebdavProtocolException {
    	super(factory, "Addressbooks");
    	this.factory = factory;
    	includeProperties(new SyncToken(this));
    	if (isUseAggregatedCollection()) {
            /*
             * indicate permissions from default folder also for root collection for macOS client
             */
            try {
                includeProperties(new CurrentUserPrivilegeSet(factory.getState().getDefaultFolder().getOwnPermission()));
            } catch (OXException e) {
                throw protocolException(getUrl(), e);
            }
    	}
    }

    @Override
    public String getPushTopic() {
        return isUseAggregatedCollection() ? null : "ox:" + Interface.CARDDAV.toString().toLowerCase();
    }

    @Override
	public List<WebdavResource> getChildren() throws WebdavProtocolException {
		List<WebdavResource> children = new ArrayList<WebdavResource>();
		if (isUseAggregatedCollection()) {
			/*
			 * add the aggregated collection as child resource
			 */
            try {
                children.add(createAggregatedCollection(getAggregatedFolders()));
            } catch (OXException e) {
                throw protocolException(getUrl(), e);
            }
			LOG.debug("{}: adding aggregated collection as child resource.", getUrl());
		}
		if (isUseFolderCollections()) {
			/*
             * add one child resource per synchronized contact folder
             */
			try {
                for (UserizedFolder folder : factory.getState().getVisibleFolders(true)) {
					children.add(createFolderCollection(folder));
					LOG.debug("{}: adding folder collection for folder '{}' as child resource.", getUrl(), folder.getName());
				}
			} catch (OXException e) {
				throw protocolException(getUrl(), e);
			}
		}
		LOG.debug("{}: got {} child resources.", getUrl(), I(children.size()));
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
        try {
            if (isUseAggregatedCollection()) {
                List<UserizedFolder> aggregatedFolders = getAggregatedFolders();
                String currentName = getAggregatedCollectionId(aggregatedFolders);
                if (currentName.equals(name)) {
                    /*
                     * this is the aggregated collection
                     */
                    return createAggregatedCollection(aggregatedFolders);
                }
                LOG.debug("{}: aggregated collection in use, current name is {}, but requested child collection '{}' not found.", getUrl(), currentName, name);
            }
            if (isUseFolderCollections()) {
                for (UserizedFolder folder : factory.getState().getVisibleFolders(true)) {
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
                return new PlaceholderCollection<CommonObject>(factory, constructPathForChildResource(name), ContactsContentType.getInstance(), FolderStorage.REAL_TREE_ID);
            }
        } catch (OXException e) {
            throw protocolException(getUrl(), e);
        }
		throw protocolException(getUrl(), new Exception("child resource '" + name + "' not found"), HttpServletResponse.SC_NOT_FOUND);
	}

    private String getExposedCollections() {
        if (null == exposedCollections) {
            try {
                exposedCollections = factory.getServiceSafe(LeanConfigurationService.class).getProperty(CardDAVProperty.EXPOSED_COLLECTIONS);
            } catch (OXException e) {
                exposedCollections = CardDAVProperty.EXPOSED_COLLECTIONS.getDefaultValue(String.class);
                LOG.error("error getting exposed collections from config, falling back to '{}'", exposedCollections, e);
            }
        }
        return exposedCollections;
    }

    private Pattern getUserAgentForAggregatedCollection() {
        if (null == userAgentForAggregatedCollection) {
            String regex;
            try {
                regex = factory.getServiceSafe(LeanConfigurationService.class).getProperty(CardDAVProperty.USERAGENT_FOR_AGGREGATED_COLLECTION);
            } catch (OXException e) {
                regex = CardDAVProperty.USERAGENT_FOR_AGGREGATED_COLLECTION.getDefaultValue(String.class);
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

    private CardDAVCollection createAggregatedCollection(List<UserizedFolder> aggregatedFolders) throws WebdavProtocolException {
        try {
            return factory.mixin(new AggregatedCollection(factory, constructPathForChildResource(getAggregatedCollectionId(aggregatedFolders)), aggregatedFolders));
        } catch (OXException e) {
            throw protocolException(getUrl(), e);
        }
    }

    private List<UserizedFolder> getAggregatedFolders() throws OXException {
        switch (getAggregatedCollectionMode()) {
            case ALL:
                return factory.getState().getVisibleFolders(false);
            case DEFAULT_ONLY:
                return Collections.singletonList(factory.getState().getDefaultFolder());
            case REDUCED:
                return factory.getState().getReducedVisibleFolders(false);
            case REDUCED_SYNCED:
                return factory.getState().getReducedVisibleFolders(true);
            case ALL_SYNCED:
            default:
                return factory.getState().getVisibleFolders(true);
        }
    }

    private AggregatedCollectionMode getAggregatedCollectionMode() {
        if (null == aggregatedCollectionMode) {
            try {
                /*
                 * check if defined, first
                 */
                LeanConfigurationService configService = factory.getServiceSafe(LeanConfigurationService.class);
                String value = configService.getProperty(DefaultProperty.valueOf(CardDAVProperty.AGGREGATED_COLLECTION_FOLDERS.getFQPropertyName(), null));
                if (Strings.isEmpty(value)) {
                    /*
                     * not configured, probe legacy property as fallback
                     */
                    String legacyPropertyName = CardDAVProperty.REDUCED_AGGREGATED_COLLECTION.getFQPropertyName();
                    String legacyValue = configService.getProperty(DefaultProperty.valueOf(legacyPropertyName, null));
                    if (Strings.isNotEmpty(legacyValue) && Boolean.parseBoolean(legacyValue)) {
                        value = AggregatedCollectionMode.REDUCED_SYNCED.name();
                        LOG.debug("{}: using mode {} for aggregated collection based on defined legacy property {}.", getUrl(), value, legacyPropertyName);
                    }
                }
                aggregatedCollectionMode = Enums.parse(AggregatedCollectionMode.class, value, AggregatedCollectionMode.ALL_SYNCED);
            } catch (OXException e) {
                LOG.error("Error getting aggregated collection folders mode from config, falling back to '{}'", AggregatedCollectionMode.ALL_SYNCED, e);
                aggregatedCollectionMode = AggregatedCollectionMode.ALL_SYNCED;
            }
        }
        return aggregatedCollectionMode;
    }

    private static String getAggregatedCollectionId(List<UserizedFolder> aggregatedFolders) throws OXException {
        return "Contacts." + getFoldersHash(aggregatedFolders);
    }

}
