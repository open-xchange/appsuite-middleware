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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.util.Locale;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.SystemType;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.database.contentType.CalendarContentType;
import com.openexchange.folderstorage.database.contentType.ContactContentType;
import com.openexchange.folderstorage.database.contentType.InfostoreContentType;
import com.openexchange.folderstorage.database.contentType.TaskContentType;
import com.openexchange.folderstorage.database.contentType.UnboundContentType;
import com.openexchange.folderstorage.database.type.PrivateType;
import com.openexchange.folderstorage.database.type.PublicType;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link FolderImpl} - A mail folder.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderImpl implements Folder {

    private String treeId;

    private String id;

    private String name;

    private String parent;

    private Permission[] permissions;

    private String[] subfolders;

    private boolean subscribed;

    private ContentType contentType;

    private Type type;

    /**
     * Initializes an empty {@link FolderImpl}.
     */
    public FolderImpl() {
        super();
    }

    /**
     * Initializes a new {@link FolderImpl} from given database folder.
     * <p>
     * Subfolder identifiers and tree identifier are not set within this constructor. Moreover passed database folder is considered to be
     * subscribed.
     * 
     * @param folderObject The underlying database folder
     */
    public FolderImpl(final FolderObject folderObject) {
        super();
        this.id = String.valueOf(folderObject.getObjectID());
        this.name = folderObject.getFolderName();
        this.parent = String.valueOf(folderObject.getParentFolderID());
        this.type = getType(folderObject.getType());
        this.contentType = getContentType(folderObject.getModule());
        final OCLPermission[] oclPermissions = folderObject.getPermissionsAsArray();
        this.permissions = new Permission[oclPermissions.length];
        for (int i = 0; i < oclPermissions.length; i++) {
            this.permissions[i] = new PermissionImpl(oclPermissions[i]);
        }
        this.subscribed = true;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public String getID() {
        return id;
    }

    public String getLocalizedName(final Locale locale) {
        return name;
    }

    public String getName() {
        return name;
    }

    public String getParentID() {
        return parent;
    }

    public Permission[] getPermissions() {
        return permissions;
    }

    public String[] getSubfolderIDs() {
        return subfolders;
    }

    public String getTreeID() {
        return treeId;
    }

    public Type getType() {
        return type;
    }

    public void setContentType(final ContentType contentType) {
        this.contentType = contentType;
    }

    public void setID(final String id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setParentID(final String parentId) {
        this.parent = parentId;
    }

    public void setPermissions(final Permission[] permissions) {
        this.permissions = permissions;
    }

    public void setSubfolderIDs(final String[] subfolderIds) {
        this.subfolders = subfolderIds;
    }

    public void setTreeID(final String id) {
        this.treeId = id;
    }

    public void setType(final Type type) {
        this.type = type;
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    public void setSubscribed(final boolean subscribed) {
        this.subscribed = subscribed;
    }

    private static Type getType(final int type) {
        if (FolderObject.PRIVATE == type) {
            return PrivateType.getInstance();
        }
        if (FolderObject.PUBLIC == type) {
            return PublicType.getInstance();
        }
        if (FolderObject.SYSTEM_TYPE == type) {
            return SystemType.getInstance();
        }
        return null;
    }

    private static ContentType getContentType(final int module) {
        if (FolderObject.CALENDAR == module) {
            return CalendarContentType.getInstance();
        }
        if (FolderObject.CONTACT == module) {
            return ContactContentType.getInstance();
        }
        if (FolderObject.TASK == module) {
            return TaskContentType.getInstance();
        }
        if (FolderObject.INFOSTORE == module) {
            return InfostoreContentType.getInstance();
        }
        if (FolderObject.UNBOUND == module) {
            return UnboundContentType.getInstance();
        }
        return null;
    }

}
