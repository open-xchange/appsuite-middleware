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

package com.openexchange.folderstorage.database;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccounts;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.folderstorage.AbstractFolder;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.SystemContentType;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.database.contentType.CalendarContentType;
import com.openexchange.folderstorage.database.contentType.ContactContentType;
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
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.groupware.userconfiguration.UserPermissionBitsStorage;
import com.openexchange.java.Strings;
import com.openexchange.log.LogProperties;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.impl.ThreadLocalSessionHolder;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;

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
            return ContactContentType.getInstance();
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
            LOG.warn("Unknown database folder content type: {}", module);
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
        final Session session = getSession();
        if (null != session) {
            try {
                final FolderObject folderObject = this.folderObject;
                if (session instanceof ServerSession) {
                    final ServerSession serverSession = (ServerSession) session;
                    final EffectivePermission permission = folderObject.getEffectiveUserPermission(session.getUserId(), serverSession.getUserConfiguration());
                    if (permission.getFolderPermission() <= 0 || permission.getReadPermission() <= 0) {
                        return 0;
                    }
                    final Context ctx = serverSession.getContext();
                    final int count = (int) new OXFolderAccess(ctx).getItemCount(folderObject, session, ctx);
                    return count < 0 ? super.getTotal() : count;
                }
                final Context ctx = ContextStorage.getStorageContext(session.getContextId());
                final int userId = session.getUserId();
                final User user = UserStorage.getInstance().getUser(userId, ctx);
                final UserPermissionBits userPerm = UserPermissionBitsStorage.getInstance().getUserPermissionBits(userId, ctx);
                userPerm.setGroups(user.getGroups());
                final EffectivePermission permission = folderObject.getEffectiveUserPermission(userId, userPerm);
                if (permission.getFolderPermission() <= 0 || permission.getReadPermission() <= 0) {
                    return 0;
                }
                final int count = (int) new OXFolderAccess(ctx).getItemCount(folderObject, session, ctx);
                return count < 0 ? super.getTotal() : count;
            } catch (final OXException e) {
                // Ignore
                LOG.debug("", e);
            }
        }
        return super.getTotal();
    }

    private Session getSession() {
        ServerSession session = ThreadLocalSessionHolder.getInstance().getSessionObject();
        if (null != session) {
            return session;
        }
        final String sessionId = LogProperties.getLogProperty(LogProperties.Name.SESSION_SESSION_ID);
        return null == sessionId ? null : SessiondService.SERVICE_REFERENCE.get().getSession(sessionId);
    }

}
