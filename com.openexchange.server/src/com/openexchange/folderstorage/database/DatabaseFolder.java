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

package com.openexchange.folderstorage.database;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.b;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccounts;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.folderstorage.AbstractFolder;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.FolderPath;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.SystemContentType;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.UsedForSync;
import com.openexchange.folderstorage.database.contentType.CalendarContentType;
import com.openexchange.folderstorage.database.contentType.ContactsContentType;
import com.openexchange.folderstorage.database.contentType.InfostoreContentType;
import com.openexchange.folderstorage.database.contentType.TaskContentType;
import com.openexchange.folderstorage.database.contentType.UnboundContentType;
import com.openexchange.folderstorage.filestorage.contentType.DocumentsContentType;
import com.openexchange.folderstorage.filestorage.contentType.MusicContentType;
import com.openexchange.folderstorage.filestorage.contentType.PicturesContentType;
import com.openexchange.folderstorage.filestorage.contentType.TemplatesContentType;
import com.openexchange.folderstorage.filestorage.contentType.VideosContentType;
import com.openexchange.folderstorage.type.DocumentsType;
import com.openexchange.folderstorage.type.MusicType;
import com.openexchange.folderstorage.type.PicturesType;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SystemType;
import com.openexchange.folderstorage.type.TemplatesType;
import com.openexchange.folderstorage.type.TrashType;
import com.openexchange.folderstorage.type.VideosType;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.FolderPathObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.session.Sessions;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.property.FolderSubscriptionHelper;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

/**
 * {@link DatabaseFolder} - A database folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DatabaseFolder extends AbstractFolder {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DatabaseFolder.class);

    private static final long serialVersionUID = -4035221612481906228L;

    private static final String CAPABILITY_ZIPPABLE_FOLDER = Strings.asciiLowerCase(FileStorageCapability.ZIPPABLE_FOLDER.name());
    private static final String CAPABILITY_FILE_VERSIONS = Strings.asciiLowerCase(FileStorageCapability.FILE_VERSIONS.name());
    private static final String CAPABILITY_EXTENDED_METADATA = Strings.asciiLowerCase(FileStorageCapability.EXTENDED_METADATA.name());
    private static final String CAPABILITY_LOCKS = Strings.asciiLowerCase(FileStorageCapability.LOCKS.name());
    private static final String CAPABILITY_COUNT_TOTAL = Strings.asciiLowerCase(FileStorageCapability.COUNT_TOTAL.name());
    private static final String CAPABILITY_CASE_INSENSITIVE = Strings.asciiLowerCase(FileStorageCapability.CASE_INSENSITIVE.name());
    private static final String CAPABILITY_RESTORE = Strings.asciiLowerCase(FileStorageCapability.RESTORE.name());

    private static final TIntSet COUNTABLE_MODULES = new TIntHashSet(new int[] { FolderObject.CALENDAR, FolderObject.CONTACT, FolderObject.TASK, FolderObject.INFOSTORE });

    private boolean cacheable;
    private final int objectId;
    private final FolderObject folderObject;
    protected boolean global;

    /**
     * Initializes a new cacheable {@link DatabaseFolder} from given database folder.
     * <p>
     * Subfolder identifiers and tree identifier are not set within this constructor. Moreover passed database folder is considered to be
     * subscribed.
     *
     * @param folderObject The underlying database folder
     */
    public DatabaseFolder(final FolderObject folderObject) {
        this(folderObject, true);
    }

    /**
     * Initializes a new {@link DatabaseFolder} from given database folder.
     * <p>
     * Subfolder identifiers and tree identifier are not set within this constructor. Moreover passed database folder is considered to be
     * subscribed.
     *
     * @param folderObject The underlying database folder
     * @param cacheable <code>true</code> if this database folder is cacheable; otherwise <code>false</code>
     */
    public DatabaseFolder(final FolderObject folderObject, final boolean cacheable) {
        super();
        this.cacheable = cacheable;
        global = true;
        this.folderObject = folderObject;
        objectId = folderObject.getObjectID();
        id = String.valueOf(objectId);
        name = folderObject.getFolderName();
        parent = String.valueOf(folderObject.getParentFolderID());
        type = getType(folderObject.getType());
        contentType = getContentType(folderObject.getModule());
        if (contentType.getModule() == InfostoreContentType.getInstance().getModule()) {
            accountId = FileStorageAccounts.getQualifiedID(FileID.INFOSTORE_SERVICE_ID, FileID.INFOSTORE_ACCOUNT_ID);
            addSupportedCapabilities(CAPABILITY_ZIPPABLE_FOLDER);
            addSupportedCapabilities(CAPABILITY_FILE_VERSIONS);
            addSupportedCapabilities(CAPABILITY_EXTENDED_METADATA);
            addSupportedCapabilities(CAPABILITY_LOCKS);
            addSupportedCapabilities(CAPABILITY_CASE_INSENSITIVE);
            addSupportedCapabilities(CAPABILITY_RESTORE);
        }
        int module = contentType.getModule();
        if (COUNTABLE_MODULES.contains(module)) {
            addSupportedCapabilities(CAPABILITY_COUNT_TOTAL);
        }
        final OCLPermission[] oclPermissions = folderObject.getPermissionsAsArray();
        permissions = new Permission[oclPermissions.length];
        for (int i = 0; i < oclPermissions.length; i++) {
            permissions[i] = new DatabasePermission(oclPermissions[i]);
        }
        createdBy = folderObject.getCreatedBy();
        modifiedBy = folderObject.getModifiedBy();
        {
            final Date d = folderObject.getCreationDate();
            creationDate = null == d ? null : new Date(d.getTime());
        }
        {
            final Date d = folderObject.getLastModified();
            lastModified = null == d ? null : new Date(d.getTime());
        }
        subscribed = true;
        deefault = folderObject.isDefaultFolder();
        defaultType = deefault ? contentType.getModule() : 0;
        meta = folderObject.getMeta();
        FolderPathObject folderPathObject = folderObject.getOriginPath();
        originPath = null == folderPathObject ? null : FolderPath.copyOf(folderPathObject);
    }

    @Override
    public Object clone() {
        final DatabaseFolder clone = (DatabaseFolder) super.clone();
        clone.cacheable = cacheable;
        clone.global = global;
        return clone;
    }

    /**
     * Sets the cachable flag.
     *
     * @param cacheable The cachable flag.
     */
    public void setCacheable(final boolean cacheable) {
        this.cacheable = cacheable;
    }

    @Override
    public boolean isCacheable() {
        return cacheable;
    }

    private static Type getType(final int type) {
        switch (type) {
            case FolderObject.SYSTEM_TYPE:
                return SystemType.getInstance();
            case FolderObject.PRIVATE:
                return PrivateType.getInstance();
            case FolderObject.PUBLIC:
                return PublicType.getInstance();
            case FolderObject.TRASH:
                return TrashType.getInstance();
            case FolderObject.PICTURES:
                return PicturesType.getInstance();
            case FolderObject.DOCUMENTS:
                return DocumentsType.getInstance();
            case FolderObject.MUSIC:
                return MusicType.getInstance();
            case FolderObject.VIDEOS:
                return VideosType.getInstance();
            case FolderObject.TEMPLATES:
                return TemplatesType.getInstance();
            default:
                return null;
        }
    }

    private static ContentType getContentType(final int module) {
        switch (module) {
            case FolderObject.SYSTEM_MODULE:
                return SystemContentType.getInstance();
            case FolderObject.CALENDAR:
                return CalendarContentType.getInstance();
            case FolderObject.CONTACT:
                return ContactsContentType.getInstance();
            case FolderObject.TASK:
                return TaskContentType.getInstance();
            case FolderObject.INFOSTORE:
                return InfostoreContentType.getInstance();
            case FolderObject.UNBOUND:
                return UnboundContentType.getInstance();
            case FolderObject.PICTURES:
                return PicturesContentType.getInstance();
            case FolderObject.DOCUMENTS:
                return DocumentsContentType.getInstance();
            case FolderObject.MUSIC:
                return MusicContentType.getInstance();
            case FolderObject.VIDEOS:
                return VideosContentType.getInstance();
            case FolderObject.TEMPLATES:
                return TemplatesContentType.getInstance();
            default:
                LOG.warn("Unknown database folder content type: {}", I(module));
                return SystemContentType.getInstance();
        }
    }

    @Override
    public boolean isGlobalID() {
        return global;
    }

    /**
     * Sets whether this database folder is globally valid or per-user valid.
     *
     * @param global <code>true</code> if this database folder is globally valid; otherwise <code>false</code> if per-user valid
     */
    public void setGlobal(final boolean global) {
        this.global = global;
    }

    @Override
    public int getTotal() {
        final int module = contentType.getModule();
        if (COUNTABLE_MODULES.contains(module)) {
            return itemCount();
        }
        return super.getTotal();
    }

    private int itemCount() {
        try {
            ServerSession session = getSession();
            if (null != session) {
                FolderObject folderObject = this.folderObject;
                EffectivePermission permission = folderObject.getEffectiveUserPermission(session.getUserId(), session.getUserConfiguration());
                if (permission.getFolderPermission() <= 0 || permission.getReadPermission() <= 0) {
                    return 0;
                }
                Context ctx = session.getContext();
                int count = (int) new OXFolderAccess(ctx).getItemCount(folderObject, session, ctx);
                return count < 0 ? super.getTotal() : count;
            }
        } catch (OXException e) {
            // Ignore
            LOG.debug("", e);
        }
        return super.getTotal();
    }

    private ServerSession getSession() throws OXException {
        Optional<Session> optionalSession = Sessions.getSessionForCurrentThread();
        if (!optionalSession.isPresent()) {
            return null;
        }

        return ServerSessionAdapter.valueOf(optionalSession.get());
    }

    /**
     * Determine if the current user can see this folder because of his non system permissions. Group
     * permissions are also considered.
     *
     * @return true, if this folder is hidden, false if the user has the permission to see this folder
     */
    public boolean isHidden() {
        final Type type = this.getType();
        if (SystemType.getInstance().equals(type) || (PublicType.getInstance().equals(type) && this.isDefault())) {
            return false;
        }
        try {
            ServerSession session = getSession();
            if (null != session) {
                if (folderObject.isNonSystemVisible(session.getUserId())) {
                    return false;
                }
                for (int entity : session.getUser().getGroups()) {
                    if (folderObject.isNonSystemVisible(entity)) {
                        return false;
                    }
                }
            }
        } catch (OXException e) {
            // Ignore
            LOG.debug("Error checking hidden state of folder {}, assuming 'true'.", folderObject, e);
        }
        return true;
    }

    /**
     * Checks if the current user has visibility permissions granted through at least one system
     * permission for this folder. Group permissions are considered also.
     *
     * @return true, if at least one permission has the system flag and is visible. false otherwise
     */
    public boolean isVisibleThroughSystemPermissions() {
        try {
            ServerSession session = getSession();
            if (null != session) {
                List<Integer> entities = Arrays.stream(session.getUser().getGroups()).boxed().collect(Collectors.toList());
                entities.add(I(session.getUserId()));
                for (OCLPermission permission : folderObject.getPermissions()) {
                    if (entities.contains(Integer.valueOf(permission.getEntity())) && permission.isFolderVisible() && permission.isSystem()) {
                        return true;
                    }
                }
            }
        } catch (OXException e) {
            // Ignore
            LOG.debug("Error checking visibility state of folder {}, assuming 'false'.", folderObject, e);
        }
        return false;
    }

    @Override
    public UsedForSync getUsedForSync() {
        try {
            FolderSubscriptionHelper subscriptionHelper = ServerServiceRegistry.getInstance().getService(FolderSubscriptionHelper.class);
            if (subscriptionHelper == null) {
                throw ServiceExceptionCode.absentService(FolderSubscriptionHelper.class);
            }

            if (false == subscriptionHelper.isSubscribableModule(folderObject.getModule())) {
                return UsedForSync.DEACTIVATED;
            }

            ServerSession session = getSession();
            if (null != session) {
                if (folderObject.isDefaultFolder() && FolderObject.PRIVATE == folderObject.getType(session.getUserId()) ) {
                    return UsedForSync.FORCED_ACTIVE;
                }
                Boolean usedForSync = subscriptionHelper.isUsedForSync(
                    Optional.empty(), session.getContextId(), session.getUserId(), folderObject.getObjectID(), folderObject.getModule()).orElse(Boolean.TRUE);
                return UsedForSync.of(usedForSync.booleanValue());
            }
        } catch (OXException e) {
            // Ignore
            LOG.debug("Error checking used-for-sync state of folder {}, assuming 'deactivated'.", folderObject, e);
        }
        return UsedForSync.DEACTIVATED;
    }

    @Override
    public void setUsedForSync(UsedForSync usedForSync) {
        // ignore
    }

    @Override
    public boolean isSubscribed() {
        try {
            FolderSubscriptionHelper subscriptionHelper = ServerServiceRegistry.getInstance().getService(FolderSubscriptionHelper.class);
            if (subscriptionHelper == null) {
                throw ServiceExceptionCode.absentService(FolderSubscriptionHelper.class);
            }

            if (false == subscriptionHelper.isSubscribableModule(folderObject.getModule())) {
                return true;
            }

            ServerSession session = getSession();
            if (null != session) {
                if (folderObject.isDefaultFolder() && FolderObject.PRIVATE == folderObject.getType(session.getUserId()) ) {
                    return true;
                }
                return b(subscriptionHelper.isSubscribed(
                    Optional.empty(), session.getContextId(), session.getUserId(), folderObject.getObjectID(), folderObject.getModule()).orElse(Boolean.TRUE));
            }
        } catch (OXException e) {
            // Ignore
            LOG.debug("Error checking subscribed state of folder {}, assuming 'true'.", folderObject, e);
        }
        return true;
    }

    @Override
    public void setSubscribed(boolean subscribed) {
        // ignore
    }

    @Override
    public boolean hasSubscribedSubfolders() {
        /*
         * only pass parent subscribed subfolders flag if subscribed to mimic recursive character of subscribed flag in database folders
         */
        return isSubscribed() && super.hasSubscribedSubfolders();
    }

    @Override
    public String[] getSubfolderIDs() {
        /*
         * indicate empty subfolders array to mimic recursive character of subscribed flag in database folders
         */
        return isSubscribed() ? super.getSubfolderIDs() : new String[0];
    }

}
