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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.chronos.impl;

import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.BasicPermission;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;

/**
 * {@link CalendarFolder}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarFolder {

    private final String id;
    private final Permission ownPermission;
    private final Type type;
    private final Permission[] permissions;
    private final int createdBy;
    private final Session session;

    /**
     * Initializes a new {@link CalendarFolder} for the session's user based on the supplied folder object, applying a specific set of
     * <i>own</i> permissions.
     *
     * @param session The session
     * @param folder The underlying folder to use for initialization
     * @param ownPermissionBits The permission bits to take over as <i>own</i> permissions
     * @throws OXException {@link CalendarExceptionCodes#UNSUPPORTED_FOLDER}
     */
    public CalendarFolder(Session session, FolderObject folder, int ownPermissionBits) throws OXException {
        this(session, folder, new BasicPermission(session.getUserId(), false, ownPermissionBits));
    }

    /**
     * Initializes a new {@link CalendarFolder} for the session's user based on the supplied folder object, applying a specific set of
     * <i>own</i> permissions.
     *
     * @param session The session
     * @param folder The underlying folder to use for initialization
     * @param ownPermission The permission take over as <i>own</i> permissions
     * @throws OXException {@link CalendarExceptionCodes#UNSUPPORTED_FOLDER}
     */
    public CalendarFolder(Session session, FolderObject folder, OCLPermission ownPermission) throws OXException {
        this(session, folder, getPermission(ownPermission));
    }

    /**
     * Initializes a new {@link CalendarFolder} for the session's user based on the supplied folder object, applying a specific set of
     * <i>own</i> permissions.
     *
     * @param session The session
     * @param folder The underlying folder to use for initialization
     * @param ownPermission The permission take over as <i>own</i> permissions
     * @throws OXException {@link CalendarExceptionCodes#UNSUPPORTED_FOLDER}
     */
    private CalendarFolder(Session session, FolderObject folder, Permission ownPermission) throws OXException {
        super();
        if (Module.CALENDAR.getFolderConstant() != folder.getModule()) {
            throw CalendarExceptionCodes.UNSUPPORTED_FOLDER.create(String.valueOf(folder.getObjectID()), String.valueOf(folder.getModule()));
        }
        this.id = String.valueOf(folder.getObjectID());
        this.createdBy = folder.getCreatedBy();
        this.type = FolderObject.PUBLIC == folder.getType() ? PublicType.getInstance() : createdBy != session.getUserId() ? SharedType.getInstance() : PrivateType.getInstance();
        this.permissions = getPermissions(folder.getNonSystemPermissionsAsArray());
        this.ownPermission = ownPermission;
        this.session = session;
    }

    /**
     * Gets the identifier of the calendar folder.
     *
     * @return The folder identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the identifier of the actual target calendar user of this folder. This is either the current session's user for
     * "private" or "public" folders, or the folder owner for "shared" calendar folders.
     *
     * @return The identifier of the calendar user
     */
    public int getCalendarUserId() {
        return Utils.getCalendarUserId(this);
    }

    /**
     * Gets the entity of the user that created the folder, who is treated as <i>owner</i> of the folder.
     *
     * @return The identifier of the user that created the folder
     */
    public int getCreatedBy() {
        return createdBy;
    }

    /**
     * Gets the folder type.
     *
     * @return The folder type
     */
    public Type getType() {
        return type;
    }

    /**
     * Gets the folder's permissions of the current session user.
     *
     * @return The current session user's permissions
     */
    public Permission getOwnPermission() {
        return ownPermission;
    }

    /**
     * Gets the folder's permissions.
     *
     * @return The permissions
     */
    public Permission[] getPermissions() {
        return permissions;
    }

    /**
     * Gets the session
     *
     * @return The session
     */
    public Session getSession() {
        return session;
    }

    @Override
    public String toString() {
        return "CalendarFolder [id=" + id + ", type=" + type + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CalendarFolder other = (CalendarFolder) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    private static Permission[] getPermissions(OCLPermission[] oclPermissions) {
        if (null == oclPermissions) {
            return null;
        }
        Permission[] permissions = new Permission[oclPermissions.length];
        for (int i = 0; i < oclPermissions.length; i++) {
            permissions[i] = getPermission(oclPermissions[i]);
        }
        return permissions;
    }

    private static Permission getPermission(OCLPermission oclPermission) {
        int permissionBits = Permissions.createPermissionBits(
            oclPermission.getFolderPermission(),
            oclPermission.getReadPermission(),
            oclPermission.getWritePermission(),
            oclPermission.getDeletePermission(),
            oclPermission.isFolderAdmin()
        );
        return new BasicPermission(oclPermission.getEntity(), oclPermission.isGroupPermission(), permissionBits);
    }

}
