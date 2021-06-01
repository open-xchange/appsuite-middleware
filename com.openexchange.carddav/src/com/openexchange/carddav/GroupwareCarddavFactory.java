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

package com.openexchange.carddav;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.java.Autoboxing.l;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.carddav.resources.RootCollection;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.lean.DefaultProperty;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.config.lean.Property;
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
import com.openexchange.folderstorage.database.contentType.ContactsContentType;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.SessionHolder;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.User;
import com.openexchange.user.UserService;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.helpers.AbstractResource;

/**
 * {@link GroupwareCarddavFactory}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class GroupwareCarddavFactory extends DAVFactory {

    private static final String OVERRIDE_NEXT_SYNC_TOKEN_PROPERTY = "carddav:overridenextsynctoken";
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GroupwareCarddavFactory.class);

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
        return getURLPrefix("/carddav/");
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
        WebdavPath path  = sanitize(url);
        if (0 == path.size()) {
            /*
             * this is the root collection
             */
            return mixin(new RootCollection(this));
        } else if (1 == path.size()) {
            /*
             * get child collection from root by name
             */
            return mixin(new RootCollection(this).getChild(path.name()));
        } else {
            throw WebdavProtocolException.Code.GENERAL_ERROR.create(url, HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    public WebdavResource resolveResource(WebdavPath url) throws WebdavProtocolException {
        WebdavPath path = sanitize(url);
        if (2 == path.size()) {
            /*
             * get child resource from parent collection by name
             */
            AbstractResource child = new RootCollection(this).getChild(path.parent().name()).getChild(path.name());
            if (child == null) {
                throw WebdavProtocolException.Code.GENERAL_ERROR.create(url, HttpServletResponse.SC_NOT_FOUND);
            }
            return mixin(child);
        }
        return resolveCollection(url);
    }

    public User resolveUser(int userID) throws OXException {
        return getService(UserService.class).getUser(userID, getContext());
    }

    public FolderService getFolderService() {
        return getService(FolderService.class);
    }

    public ContactService getContactService() {
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

    /**
     * Sets the next sync token for the current user in a certain collection to the supplied value.
     *
     * @param folderId The identifier of the folder to override the sync-token in, or <code>null</code> to override it generally
     * @param value The overridden value, or <code>null</code> to remove a previously overridden value
     */
    public void setOverrideNextSyncToken(String folderId, String value) {
        String attributeName = getOverrideNextSyncTokenAttributeName(folderId);
        try {
            getService(UserService.class).setAttribute(attributeName, value, getUser().getId(), getContext());
        } catch (OXException e) {
            LOG.error("", e);
        }
    }

    /**
     * Gets a value indicating the overridden sync token for the current user if defined
     *
     * @param folderId The identifier of the targeted folder, or <code>null</code> to check for a globally overridden token
     * @return The value of the overridden sync-token, or <code>null</code> if not set
     */
    public String getOverrideNextSyncToken(String folderId) {
        User user = getUser();
        String attributeName = getOverrideNextSyncTokenAttributeName(folderId);
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

    private String getOverrideNextSyncTokenAttributeName(String folderId) {
        StringBuilder stringBuilder = new StringBuilder(OVERRIDE_NEXT_SYNC_TOKEN_PROPERTY);
        String userAgent = (String) getSession().getParameter("user-agent");
        if (null != userAgent) {
            stringBuilder.append('.').append(userAgent.hashCode());
        }
        if (null != folderId) {
            stringBuilder.append('.').append(folderId);
        }
        return stringBuilder.toString();
    }

    public static final class State {

        private final GroupwareCarddavFactory factory;
        private List<UserizedFolder> allFolders = null;
        private List<UserizedFolder> reducedFolders = null;
        private Set<String> folderBlacklist = null;
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
                    factory.getUser(), getTreeID(), ContactsContentType.getInstance(), factory.getSession(), null);
            }
            return defaultFolder;
        }

        public List<UserizedFolder> getVisibleFolders(boolean usedForSyncOnly) throws OXException {
            List<UserizedFolder> allFolders = getAllFolders();
            return usedForSyncOnly ? filterNotUsedForSync(allFolders) : allFolders;
        }

        public List<UserizedFolder> getReducedVisibleFolders(boolean usedForSyncOnly) throws OXException {
            List<UserizedFolder> reducedFolders = getReducedFolders();
            return usedForSyncOnly ? filterNotUsedForSync(reducedFolders) : reducedFolders;
        }

        private static List<UserizedFolder> filterNotUsedForSync(List<UserizedFolder> folders) {
            if (null == folders || folders.isEmpty()) {
                return folders;
            }
            List<UserizedFolder> filteredFolders = new ArrayList<UserizedFolder>(folders.size());
            for (UserizedFolder folder : folders) {
                if (folder.isSubscribed() && (null == folder.getUsedForSync() || folder.getUsedForSync().isUsedForSync())) {
                    filteredFolders.add(folder);
                }
            }
            return filteredFolders;
        }

        private List<UserizedFolder> getAllFolders() throws OXException {
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
        private List<UserizedFolder> getReducedFolders() throws OXException {
            if (null == reducedFolders) {
                reducedFolders = new ArrayList<UserizedFolder>();
                UserizedFolder defaultContactsFolder = getDefaultFolder();
                if (false == isBlacklisted(defaultContactsFolder)) {
                    reducedFolders.add(defaultContactsFolder);
                }
                try {
                    UserizedFolder globalAddressBookFolder = factory.getFolderService().getFolder(FolderStorage.REAL_TREE_ID, FolderStorage.GLOBAL_ADDRESS_BOOK_ID, factory.getSession(), null);
                    if (false == isBlacklisted(globalAddressBookFolder) && globalAddressBookFolder.getUsedForSync().isUsedForSync()) {
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
            FolderResponse<UserizedFolder[]> visibleFoldersResponse = folderService.getVisibleFolders(FolderStorage.REAL_TREE_ID, ContactsContentType.getInstance(), type, true, this.factory.getSession(), null);
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
            FolderResponse<UserizedFolder[][]> updatedFoldersResponse = folderService.getUpdates(FolderStorage.REAL_TREE_ID, since, false, new ContentType[] { ContactsContentType.getInstance() }, this.factory.getSession(), null);
            UserizedFolder[][] response = updatedFoldersResponse.getResponse();
            if (2 <= response.length && null != response[1] && 0 < response[1].length) {
                for (UserizedFolder folder : response[1]) {
                    if (Permission.READ_OWN_OBJECTS < folder.getOwnPermission().getReadPermission() && false == isBlacklisted(folder) && ContactsContentType.getInstance().equals(folder.getContentType())) {
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
            if (null == folderBlacklist) {
                String ignoreFolders;
                try {
                    ignoreFolders = factory.getServiceSafe(LeanConfigurationService.class).getProperty(CardDAVProperty.IGNORE_FOLDERS);
                } catch (OXException e) {
                    LOG.error("", e);
                    ignoreFolders = CardDAVProperty.IGNORE_FOLDERS.getDefaultValue(String.class);
                }
                if (Strings.isEmpty(ignoreFolders)) {
                    folderBlacklist = Collections.emptySet();
                } else {
                    folderBlacklist = new HashSet<String>(Arrays.asList(Strings.splitByComma(ignoreFolders)));
                }
            }
            return folderBlacklist.contains(userizedFolder.getID());
        }

        /**
         * Gets the used folder tree identifier for folder operations.
         */
        private String getTreeID() {
            if (null == treeID) {
                try {
                    treeID = factory.getServiceSafe(LeanConfigurationService.class).getProperty(CardDAVProperty.TREE);
                } catch (OXException e) {
                    treeID = CardDAVProperty.TREE.getDefaultValue(String.class);
                    LOG.warn("falling back to tree id ''{}''.", treeID, e);
                }
            }
            return treeID;
        }

        /**
         * Gets the maximum size allowed for contact vCards.
         *
         * @return The maximum size, or <code>0</code> if not restricted
         */
        public long getMaxVCardSize() {
            if (null == maxVCardSize) {
                Property property = DefaultProperty.valueOf("com.openexchange.contact.maxVCardSize", L(4194304));
                try {
                    maxVCardSize = L(factory.getServiceSafe(LeanConfigurationService.class).getLongProperty(property));
                } catch (OXException e) {
                    maxVCardSize = property.getDefaultValue(Long.class);
                    LOG.warn("error reading value for \"{}\", falling back to {}.", property, maxVCardSize, e);
                }
            }
            return l(maxVCardSize);
        }

        /**
         * Gets the maximum (overall) upload size per request.
         *
         * @return The maximum upload size, or <code>0</code> if not restricted
         */
        public long getMaxUploadSize() {
            if (null == maxUploadSize) {
                Property property = DefaultProperty.valueOf("MAX_UPLOAD_SIZE", L(104857600));
                try {
                    maxUploadSize = L(factory.getServiceSafe(LeanConfigurationService.class).getLongProperty(property));
                } catch (OXException e) {
                    maxUploadSize = property.getDefaultValue(Long.class);
                    LOG.warn("error reading value for \"{}\", falling back to {}.", property, maxVCardSize, e);
                }
            }
            return l(maxUploadSize);
        }

        /**
         * Gets the maximum number of contacts to fetch from the storage, based on the configuration value for
         * <code>com.openexchange.webdav.recursiveMarshallingLimit</code>.
         *
         * @return The contact limit, or <code>0</code> for no limitations
         */
        public int getContactLimit() {
            Property property = DefaultProperty.valueOf("com.openexchange.webdav.recursiveMarshallingLimit", I(25000));
            int limit;
            try {
                limit = factory.getServiceSafe(LeanConfigurationService.class).getIntProperty(property);
            } catch (OXException e) {
                limit = i(property.getDefaultValue(Integer.class));
                LOG.warn("error getting \"{}\", falling back to \"{}\".", property, I(limit), e);
            }
            return 0 >= limit ? 0 : 1 + limit;
        }

    }

}
