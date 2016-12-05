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

package com.openexchange.carddav;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.carddav.resources.RootCollection;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.vcard.VCardService;
import com.openexchange.contact.vcard.storage.VCardStorageFactory;
import com.openexchange.contact.vcard.storage.VCardStorageService;
import com.openexchange.dav.DAVFactory;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.ContentType;
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
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tools.session.SessionHolder;
import com.openexchange.user.UserService;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

/**
 * {@link GroupwareCarddavFactory}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class GroupwareCarddavFactory extends DAVFactory {

    private static final String OVERRIDE_NEXT_SYNC_TOKEN_PROPERTY = "com.openexchange.carddav.overridenextsynctoken";
    private static final String FOLDER_BLACKLIST_PROPERTY = "com.openexchange.carddav.ignoreFolders";
    private static final String FOLDER_TRRE_ID_PROPERTY = "com.openexchange.carddav.tree";
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GroupwareCarddavFactory.class);

    private final ThreadLocal<State> stateHolder;

    /**
     * Initializes a new {@link GroupwareCarddavFactory}.
     *
     * @param protocol The protocol
     * @param services A service lookup reference
     * @param sessionHolder The session holder to use
     */
    public GroupwareCarddavFactory(Protocol protocol, ServiceLookup services, SessionHolder sessionHolder) {
        super(protocol, services, sessionHolder);
        this.stateHolder = new ThreadLocal<State>();
    }

    @Override
    public String getURLPrefix() {
        return "/carddav/";
    }

    @Override
    public void beginRequest() {
        stateHolder.set(new State(this));
        super.beginRequest();
    }

    @Override
    public void endRequest(int status) {
        stateHolder.set(null);
        super.endRequest(status);
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
        return getService(UserService.class).getUser(userID, getContext());
    }

    public FolderService getFolderService() {
        return getService(FolderService.class);
    }

    public ContactService getContactService() throws OXException {
        return getService(ContactService.class);
    }

    public State getState() {
        return stateHolder.get();
    }

    public VCardService getVCardService() {
        return getService(VCardService.class);
    }

    public VCardStorageService getVCardStorageService(int contextId) {
        VCardStorageFactory vCardStorageFactory = getOptionalService(VCardStorageFactory.class);
        if (vCardStorageFactory != null) {
            return vCardStorageFactory.getVCardStorageService(getService(ConfigViewFactory.class), contextId);
        }
        return null;
    }

    public String getConfigValue(String key, String defaultValue) throws OXException {
        ConfigView view = getService(ConfigViewFactory.class).getView(getUser().getId(), getContext().getContextId());
        ComposedConfigProperty<String> property = view.property(key, String.class);
        return property.isDefined() ? property.get() : defaultValue;
    }

    /**
     * (Optionally) Gets coerced property value from configuration.
     *
     * @param property The property name
     * @param coerceTo The type to coerce to
     * @param defaultValue The default value
     * @return The coerced value or <code>defaultValue</code> if absent
     */
    public <T> T optConfigValue(String property, Class<T> coerceTo, T defaultValue) throws OXException {
        ConfigView view = getService(ConfigViewFactory.class).getView(getUser().getId(), getContext().getContextId());
        return view.opt(property, coerceTo, defaultValue);
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
     * @param value The overridden value
     */
    public void setOverrideNextSyncToken(String value) {
        String attributeName = getOverrideNextSyncTokenAttributeName();
        try {
            getService(UserService.class).setAttribute(attributeName, value, getUser().getId(), getContext());
        } catch (OXException e) {
            LOG.error("", e);
        }
    }

    /**
     * Gets a value indicating the overridden sync token for the current user if defined
     *
     * @return The value of the overridden sync-token, or <code>null</code> if not set
     */
    public String getOverrideNextSyncToken() {
        User user = getUser();
        String attributeName = getOverrideNextSyncTokenAttributeName();
        Map<String, String> attributes = user.getAttributes();
        if (null != attributes) {
            /*
             * pick up & correct any legacy user attribute first
             */
            String matchingAttribute = attributes.get("attr_" + attributeName);
            if (null != matchingAttribute) {
                try {
                    UserService userService = getService(UserService.class);
                    userService.setAttribute(attributeName, matchingAttribute, user.getId(), getContext());
                    userService.setAttribute("attr_" + attributeName, null, user.getId(), getContext());
                } catch (OXException e) {
                    LOG.error("", e);
                }
                return matchingAttribute;
            }
            /*
             * get matching attribute value
             */
            matchingAttribute = attributes.get(attributeName);
            if (null != matchingAttribute) {
                return matchingAttribute;
            }
        }
        return null;
    }

    private String getOverrideNextSyncTokenAttributeName() {
        String userAgent = (String) getSession().getParameter("user-agent");
        return null != userAgent ? OVERRIDE_NEXT_SYNC_TOKEN_PROPERTY + userAgent.hashCode() : OVERRIDE_NEXT_SYNC_TOKEN_PROPERTY;
    }

    public static final class State {

        private final GroupwareCarddavFactory factory;
        private List<UserizedFolder> allFolders = null;
        private List<UserizedFolder> reducedFolders = null;
        private HashSet<String> folderBlacklist = null;
        private UserizedFolder defaultFolder = null;
        private String treeID = null;
        private Long maxVCardSize = null;
        private Long maxUploadSize = null;

        /**
         * Initializes a new {@link State}.
         *
         * @param factory the CardDAV factory
         */
        public State(final GroupwareCarddavFactory factory) {
            super();
            this.factory = factory;
        }

        /**
         * Gets the user's default contact folder.
         *
         * @return The default folder
         */
        public UserizedFolder getDefaultFolder() throws OXException {
            if (null == defaultFolder) {
                defaultFolder = factory.getFolderService().getDefaultFolder(
                    factory.getUser(), getTreeID(), ContactContentType.getInstance(), factory.getSession(), null);
            }
            return defaultFolder;
        }

        /**
         * Gets a list of all synchronized contact folders.
         *
         * @return The contact folders
         */
        public List<UserizedFolder> getFolders() throws OXException {
            if (null == allFolders) {
                allFolders = getVisibleFolders();
            }
            return allFolders;
        }

        /**
         * Gets a reduced set of all synchronized contact folder.
         *
         * @return The contact folders
         */
        public List<UserizedFolder> getReducedFolders() throws OXException {
            if (null == reducedFolders) {
                reducedFolders = new ArrayList<UserizedFolder>();
                UserizedFolder defaultContactsFolder = getDefaultFolder();
                if (false == isBlacklisted(defaultContactsFolder)) {
                    reducedFolders.add(defaultContactsFolder);
                }
                try {
                    UserizedFolder globalAddressBookFolder = factory.getFolderService().getFolder(FolderStorage.REAL_TREE_ID, FolderStorage.GLOBAL_ADDRESS_BOOK_ID, factory.getSession(), null);
                    if (false == isBlacklisted(globalAddressBookFolder)) {
                        reducedFolders.add(globalAddressBookFolder);
                    }
                } catch (OXException e) {
                    if (Category.CATEGORY_PERMISSION_DENIED.equals(e.getCategory())) {
                        LOG.debug("No permission for global addressbook, skipping.", e);
                    } else {
                        throw e;
                    }
                }
            }
            return reducedFolders;
        }

        /**
         * Gets a list of all visible contact folders.
         *
         * @return The visible contact folders
         */
        private List<UserizedFolder> getVisibleFolders() throws OXException {
            UserPermissionBits permissionBits = ServerSessionAdapter.valueOf(factory.getSession()).getUserPermissionBits();
            List<UserizedFolder> folders = new ArrayList<UserizedFolder>();
            folders.addAll(getVisibleFolders(PrivateType.getInstance()));
            if (permissionBits.hasFullPublicFolderAccess()) {
                folders.addAll(getVisibleFolders(PublicType.getInstance()));
            }
            if (permissionBits.hasFullSharedFolderAccess()) {
                folders.addAll(getVisibleFolders(SharedType.getInstance()));
            }
            return folders;
        }

        /**
         * Gets a list containing all visible contact folders of the given {@link Type}.
         *
         * @param type The folder type
         * @return The visible contact folders
         */
        private List<UserizedFolder> getVisibleFolders(Type type) throws OXException {
            List<UserizedFolder> folders = new ArrayList<UserizedFolder>();
            FolderService folderService = factory.getFolderService();
            FolderResponse<UserizedFolder[]> visibleFoldersResponse = folderService.getVisibleFolders(FolderStorage.REAL_TREE_ID, ContactContentType.getInstance(), type, true, this.factory.getSession(), null);
            UserizedFolder[] response = visibleFoldersResponse.getResponse();
            for (UserizedFolder folder : response) {
                if (Permission.READ_OWN_OBJECTS < folder.getOwnPermission().getReadPermission() && false == isBlacklisted(folder)) {
                    folders.add(folder);
                }
            }
            return folders;
        }

        /**
         * Gets a list of folders that were deleted after a specific date.
         *
         * @param since The deletion date to consider
         * @return The folders, or an empty list if there are none
         */
        public List<UserizedFolder> getDeletedFolders(Date since) throws OXException {
            List<UserizedFolder> folders = new ArrayList<UserizedFolder>();
            FolderService folderService = factory.getFolderService();
            FolderResponse<UserizedFolder[][]> updatedFoldersResponse = folderService.getUpdates(FolderStorage.REAL_TREE_ID, since, false, new ContentType[] { ContactContentType.getInstance() }, this.factory.getSession(), null);
            UserizedFolder[][] response = updatedFoldersResponse.getResponse();
            if (2 <= response.length && null != response[1] && 0 < response[1].length) {
                for (UserizedFolder folder : response[1]) {
                    if (Permission.READ_OWN_OBJECTS < folder.getOwnPermission().getReadPermission() && false == this.isBlacklisted(folder) && ContactContentType.getInstance().equals(folder.getContentType())) {
                        folders.add(folder);
                    }
                }
            }
            return folders;
        }

        /**
         * Determines whether the supplied folder is blacklisted and should be ignored or not.
         *
         * @param userizedFolder
         * @return
         */
        private boolean isBlacklisted(UserizedFolder userizedFolder) {
            if (null == this.folderBlacklist) {
                String ignoreFolders = null;
                try {
                    ignoreFolders = factory.getConfigValue(FOLDER_BLACKLIST_PROPERTY, null);
                } catch (OXException e) {
                    LOG.error("", e);
                }
                if (null == ignoreFolders || 0 >= ignoreFolders.length()) {
                    this.folderBlacklist = new HashSet<String>(0);
                } else {
                    this.folderBlacklist = new HashSet<String>(Arrays.asList(Strings.splitByComma(ignoreFolders)));
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
                    LOG.warn("falling back to tree id ''{}''.", FolderStorage.REAL_TREE_ID, e);
                    treeID = FolderStorage.REAL_TREE_ID;
                }
            }
            return this.treeID;
        }

        /**
         * Gets the maximum size allowed for contact vCards.
         *
         * @return The maximum size, or <code>0</code> if not restricted
         */
        public long getMaxVCardSize() {
            if (null == maxVCardSize) {
                Long defaultValue = Long.valueOf(4194304);
                try {
                    maxVCardSize = this.factory.optConfigValue("com.openexchange.contact.maxVCardSize", Long.class, defaultValue);
                } catch (OXException e) {
                    LOG.warn("error reading value for \"com.openexchange.contact.maxVCardSize\", falling back to {}.", defaultValue, e);
                    maxVCardSize = defaultValue;
                }
            }
            return maxVCardSize.longValue();
        }

        /**
         * Gets the maximum (overall) upload size per request.
         *
         * @return The maximum upload size, or <code>0</code> if not restricted
         */
        public long getMaxUploadSize() {
            if (null == maxUploadSize) {
                Long defaultValue = Long.valueOf(104857600);
                try {
                    maxUploadSize = factory.optConfigValue("MAX_UPLOAD_SIZE", Long.class, defaultValue);
                } catch (OXException e) {
                    LOG.warn("error reading value for \"MAX_UPLOAD_SIZE\", falling back to {}.", defaultValue, e);
                    maxUploadSize = defaultValue;
                }
            }
            return maxUploadSize.longValue();
        }

        /**
         * Gets the maximum number of contacts to fetch from the storage, based on the configuration value for
         * <code>com.openexchange.webdav.recursiveMarshallingLimit</code>.
         *
         * @return The contact limit, or <code>0</code> for no limitations
         */
        public int getContactLimit() {
            int limit = 25000;
            try {
                limit = Integer.valueOf(factory.getConfigValue("com.openexchange.webdav.recursiveMarshallingLimit", String.valueOf(limit)));
            } catch (OXException e) {
                LOG.warn("error getting \"com.openexchange.webdav.recursiveMarshallingLimit\", falling back to \"{}\".", limit, e);
            }
            return 0 >= limit ? 0 : 1 + limit;
        }

    }

}
