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

package com.openexchange.groupware.infostore;

import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.server.impl.OCLPermission;


/**
 * {@link EffectiveInfostoreFolderPermission}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class EffectiveInfostoreFolderPermission {

    private final EffectivePermission permission;
    private final int folderOwner;

    /**
     * Initializes a new {@link EffectiveInfostoreFolderPermission}.
     *
     * @param permission The permission
     * @param folderOwner The folder owner
     */
    public EffectiveInfostoreFolderPermission(EffectivePermission permission, int folderOwner) {
        super();
        this.permission = permission;
        this.folderOwner = folderOwner;
    }

    /**
     * Gets the permission
     *
     * @return The permission
     */
    public EffectivePermission getPermission() {
        return permission;
    }

    /**
     * Gets the optional folder owner identifier
     *
     * @return The folder owner identifier or <code>-1</code>
     */
    public int getFolderOwner() {
        return folderOwner;
    }

    public boolean hasModuleAccess(int folderModule) {
        return permission.hasModuleAccess(folderModule);
    }

    public boolean isFolderAdmin() {
        return permission.isFolderAdmin();
    }

    public void setFolderAdmin(boolean folderAdmin) {
        permission.setFolderAdmin(folderAdmin);
    }

    public void reset() {
        permission.reset();
    }

    public void setGroupPermission(boolean groupPermission) {
        permission.setGroupPermission(groupPermission);
    }

    public void setSystem(int system) {
        permission.setSystem(system);
    }

    public int getFolderPermission() {
        return permission.getFolderPermission();
    }

    public void setName(String name) {
        permission.setName(name);
    }

    public boolean setFolderPermission(int fp) {
        return permission.setFolderPermission(fp);
    }

    public int getReadPermission() {
        return permission.getReadPermission();
    }

    public boolean setReadObjectPermission(int p) {
        return permission.setReadObjectPermission(p);
    }

    public int getWritePermission() {
        return permission.getWritePermission();
    }

    public boolean setWriteObjectPermission(int p) {
        return permission.setWriteObjectPermission(p);
    }

    public int getDeletePermission() {
        return permission.getDeletePermission();
    }

    public boolean setAllObjectPermission(int pr, int pw, int pd) {
        return permission.setAllObjectPermission(pr, pw, pd);
    }

    public boolean setDeleteObjectPermission(int p) {
        return permission.setDeleteObjectPermission(p);
    }

    public boolean setAllPermission(int fp, int opr, int opw, int opd) {
        return permission.setAllPermission(fp, opr, opw, opd);
    }

    public void setEntity(int entity) {
        permission.setEntity(entity);
    }

    public void setFuid(int fuid) {
        permission.setFuid(fuid);
    }

    public boolean isGroupPermission() {
        return permission.isGroupPermission();
    }

    public boolean isFolderVisible() {
        return permission.isFolderVisible();
    }

    public boolean canCreateObjects() {
        return permission.canCreateObjects();
    }

    public boolean canCreateSubfolders() {
        return permission.canCreateSubfolders();
    }

    public boolean canReadOwnObjects() {
        return permission.canReadOwnObjects();
    }

    public boolean canReadAllObjects() {
        return permission.canReadAllObjects();
    }

    public boolean canWriteOwnObjects() {
        return permission.canWriteOwnObjects();
    }

    public boolean canWriteAllObjects() {
        return permission.canWriteAllObjects();
    }

    public boolean canDeleteOwnObjects() {
        return permission.canDeleteOwnObjects();
    }

    public boolean canDeleteAllObjects() {
        return permission.canDeleteAllObjects();
    }

    /**
     * Gets a value indicating whether the permissions are sufficient to share "own" items in the folder or not.
     *
     * @return <code>true</code> if "own" items can be shared, <code>false</code>, otherwise
     */
    public boolean canShareOwnObjects() {
        return permission.canWriteOwnObjects() && FolderObject.TRASH != permission.getFolderType();
    }

    /**
     * Gets a value indicating whether the permissions are sufficient to share all items in the folder or not.
     *
     * @return <code>true</code> if all items can be shared, <code>false</code>, otherwise
     */
    public boolean canShareAllObjects() {
        return permission.canWriteAllObjects() && FolderObject.TRASH != permission.getFolderType();
    }

    public OCLPermission getUnderlyingPermission() {
        return permission.getUnderlyingPermission();
    }

    @Override
    public String toString() {
        return permission.toString();
    }

    public String getName() {
        return permission.getName();
    }

    public int getEntity() {
        return permission.getEntity();
    }

    public int getFuid() {
        return permission.getFuid();
    }

    public int getSystem() {
        return permission.getSystem();
    }

    public boolean isSystem() {
        return permission.isSystem();
    }

    public boolean equalsPermission(OCLPermission op) {
        return permission.equalsPermission(op);
    }



}
