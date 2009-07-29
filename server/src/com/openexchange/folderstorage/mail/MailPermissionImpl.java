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

package com.openexchange.folderstorage.mail;

import com.openexchange.folderstorage.Permission;
import com.openexchange.mail.permission.MailPermission;

/**
 * {@link MailPermissionImpl} - A mail folder permission.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailPermissionImpl implements Permission {

    private int system;

    private int deletePermission;

    private int folderPermission;

    private int readPermission;

    private int writePermission;

    private boolean admin;

    private int entity;

    private boolean group;

    /**
     * Initializes an empty {@link MailPermissionImpl}.
     */
    public MailPermissionImpl() {
        super();
    }

    /**
     * Initializes an empty {@link MailPermissionImpl}.
     */
    public MailPermissionImpl(final MailPermission mailPermission) {
        super();
        this.admin = mailPermission.isFolderAdmin();
        this.deletePermission = mailPermission.getDeletePermission();
        this.entity = mailPermission.getEntity();
        this.folderPermission = mailPermission.getFolderPermission();
        this.group = mailPermission.isGroupPermission();
        this.readPermission = mailPermission.getReadPermission();
        this.system = mailPermission.getSystem();
        this.writePermission = mailPermission.getWritePermission();
    }

    public int getDeletePermission() {
        return deletePermission;
    }

    public int getEntity() {
        return entity;
    }

    public int getFolderPermission() {
        return folderPermission;
    }

    public int getReadPermission() {
        return readPermission;
    }

    public int getSystem() {
        return system;
    }

    public int getWritePermission() {
        return writePermission;
    }

    public boolean isAdmin() {
        return admin;
    }

    public boolean isGroup() {
        return group;
    }

    public void setAdmin(final boolean admin) {
        this.admin = admin;
    }

    public void setAllPermissions(final int folderPermission, final int readPermission, final int writePermission, final int deletePermission) {
        this.folderPermission = folderPermission;
        this.readPermission = readPermission;
        this.deletePermission = deletePermission;
        this.writePermission = writePermission;
    }

    public void setDeletePermission(final int permission) {
        this.deletePermission = permission;
    }

    public void setEntity(final int entity) {
        this.entity = entity;
    }

    public void setFolderPermission(final int permission) {
        this.folderPermission = permission;
    }

    public void setGroup(final boolean group) {
        this.group = group;
    }

    public void setMaxPermissions() {
        this.folderPermission = Permission.MAX_PERMISSION;
        this.readPermission = Permission.MAX_PERMISSION;
        this.deletePermission = Permission.MAX_PERMISSION;
        this.writePermission = Permission.MAX_PERMISSION;
        this.admin = true;
    }

    public void setNoPermissions() {
        this.folderPermission = Permission.MAX_PERMISSION;
        this.readPermission = Permission.MAX_PERMISSION;
        this.deletePermission = Permission.MAX_PERMISSION;
        this.writePermission = Permission.MAX_PERMISSION;
        this.admin = false;
    }

    public void setReadPermission(final int permission) {
        this.readPermission = permission;
    }

    public void setSystem(final int system) {
        this.system = system;
    }

    public void setWritePermission(final int permission) {
        this.writePermission = permission;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new InternalError(e.getMessage());
        }
    }

}
