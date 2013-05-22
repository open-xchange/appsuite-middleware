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

package com.openexchange.folderstorage.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
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
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SystemType;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.tasks.TaskStorage;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link DatabaseFolder} - A database folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DatabaseFolder extends AbstractFolder {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.loggerFor(DatabaseFolder.class);

    private static final long serialVersionUID = -4035221612481906228L;

    private boolean cacheable;
    private int contextId;
    private int objectId;
    protected boolean global;

    /**
     * Initializes an empty {@link DatabaseFolder}.
     *
     * @param cacheable <code>true</code> if this database folder is cacheable; otherwise <code>false</code>
     */
    public DatabaseFolder(final boolean cacheable) {
        super();
        this.cacheable = cacheable;
        global = true;
        subscribed = true;
    }

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
        objectId = folderObject.getObjectID();
        id = String.valueOf(objectId);
        name = folderObject.getFolderName();
        parent = String.valueOf(folderObject.getParentFolderID());
        type = getType(folderObject.getType());
        contentType = getContentType(folderObject.getModule());
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
    }

    @Override
    public Object clone() {
        final DatabaseFolder clone = (DatabaseFolder) super.clone();
        clone.cacheable = cacheable;
        clone.global = global;
        return clone;
    }

    /**
     * Sets the context identifier
     *
     * @param contextId The context identifier to set
     */
    public <F extends DatabaseFolder> F setContextId(int contextId) {
        this.contextId = contextId;
        return (F) this;
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
        default:
            if (LOG.isWarnEnabled()) {
                LOG.warn("Unknown database folder content type: " + module);
            }
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
        switch (contentType.getModule()) {
        case FolderObject.CALENDAR:
            // TODO: Add calendar invocation here
            return super.getTotal();
        case FolderObject.CONTACT:
            // TODO: Add contact invocation here
            return super.getTotal();
        case FolderObject.TASK:
            try {
                return TaskStorage.getInstance().countTasks(ContextStorage.getStorageContext(contextId), -1, objectId, false, false);
            } catch (final Exception e) {
                // Ignore
                return super.getTotal();
            }
        case FolderObject.INFOSTORE:
            return getTotalInfostore(contextId, objectId, super.getTotal());
        default:
            return super.getTotal();
        }
    }

    private static int getTotalInfostore(int contextId, int folderId, int defaultValue) {
        if (contextId <= 0 || folderId <= 0) {
            return defaultValue;
        }
        final DatabaseService service = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        if (null == service) {
            return defaultValue;
        }
        try {
            Connection con = service.getReadOnly(contextId);
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = con.prepareStatement("SELECT COUNT(id) FROM infostore WHERE cid=? and folder_id=?");
                stmt.setLong(1, contextId);
                stmt.setLong(2, folderId);
                rs = stmt.executeQuery();
                return rs.next() ? rs.getInt(1) : defaultValue;
            } finally {
                Databases.closeSQLStuff(rs, stmt);
                service.backReadOnly(contextId, con);
            }
        } catch (final Exception e) {
            // Ignore
            return defaultValue;
        }
    }

}
