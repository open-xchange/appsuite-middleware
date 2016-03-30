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

package com.openexchange.groupware.container;

import com.openexchange.groupware.modules.Module;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;


/**
 * An {@link EffectiveObjectPermission} is an {@link ObjectPermission} from an
 * entities point of view, i.e. it denotes the permissions that a certain entity has
 * to access a certain object. Please note that module permissions are taken into
 * account, but folder permissions are not. You always have to check a users access
 * permission for an object like so:<br>
 * <pre>
 * com.openexchange.folderstorage.internal.EffectivePermission    fp = ...;
 * com.openexchange.groupware.container.EffectiveObjectPermission op = ...;
 * if (fp.isVisible() || (op != null && op.canRead())) {
 *     // do privileged operations
 * }
 * </pre>
 * <br>
 * The above example obviously only covers the read-permission case. Of course there are also
 * methods to check for write- and delete-permissions.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class EffectiveObjectPermission {

    private final int module;

    private final int folderId;

    private final int objectId;

    private final ObjectPermission permission;

    private final UserPermissionBits permissionBits;

    /**
     * Initializes a new {@link EffectiveObjectPermission}.
     *
     * @param module The objects module identifier (see {@link Module#getFolderConstant()})
     * @param folderId The objects folder id
     * @param objectId The object id
     * @param permission The object permission
     * @param permissionBits The users permission bits.
     */
    public EffectiveObjectPermission(int module, int folderId, int objectId, ObjectPermission permission, UserPermissionBits permissionBits) {
        super();
        this.module = module;
        this.folderId = folderId;
        this.objectId = objectId;
        this.permission = permission;
        this.permissionBits = permissionBits;
    }

    /**
     * Whether the user may read the affected object.
     *
     * @return If so, <code>true</code> is returned Otherwise <code>false</code>.
     */
    public boolean canRead() {
        return hasModulePermission() && permission.canRead();
    }

    /**
     * Convenient negation of {@link EffectiveObjectPermission#canRead()}.
     */
    public boolean canNotRead() {
        return !canRead();
    }

    /**
     * Whether the user may change the affected object.
     *
     * @return If so, <code>true</code> is returned Otherwise <code>false</code>.
     */
    public boolean canWrite() {
        return hasModulePermission() && permission.canWrite();
    }

    /**
     * Convenient negation of {@link EffectiveObjectPermission#canWrite()}.
     */
    public boolean canNotWrite() {
        return !canWrite();
    }

    /**
     * Whether the user may delete the affected object.
     *
     * @return If so, <code>true</code> is returned Otherwise <code>false</code>.
     */
    public boolean canDelete() {
        return hasModulePermission() && permission.canDelete();
    }

    /**
     * Convenient negation of {@link EffectiveObjectPermission#canDelete()}.
     */
    public boolean canNotDelete() {
        return !canDelete();
    }

    /**
     * Gets the affected objects module identifier.
     * @return The identifier (see {@link Module#getFolderConstant()})
     */
    public int getModule() {
        return module;
    }

    /**
     * Gets the affected objects folder id.
     * @return The folder id
     */
    public int getFolderId() {
        return folderId;
    }

    /**
     * Gets the affected objects id.
     * @return The object id.
     */
    public int getObjectId() {
        return objectId;
    }

    /**
     * Gets the underlying {@link ObjectPermission} instance.
     * @return The object permission
     */
    public ObjectPermission getPermission() {
        return permission;
    }

    /**
     * Whether the user may share the affected object.
     *
     * @return If so, <code>true</code> is returned Otherwise <code>false</code>.
     */
    public boolean canShare() {
        return canWrite() && permissionBits.hasFullSharedFolderAccess();
    }

    private boolean hasModulePermission() {
        return permissionBits.hasModuleAccess(module);
    }

}
