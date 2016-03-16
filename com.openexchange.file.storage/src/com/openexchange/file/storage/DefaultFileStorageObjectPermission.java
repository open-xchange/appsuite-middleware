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

package com.openexchange.file.storage;


/**
 * {@link DefaultFileStorageObjectPermission}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DefaultFileStorageObjectPermission implements FileStorageObjectPermission {

    private int entity;
    private boolean group;
    private int permissions;

    /**
     * Initializes a new {@link DefaultFileStorageObjectPermission}.
     */
    public DefaultFileStorageObjectPermission() {
        super();
    }

    /**
     * Initializes a new {@link DefaultFileStorageObjectPermission}.
     *
     * @param entity The entity associated with this permission
     * @param group <code>true</code> if this permission represents a group, <code>false</code>, otherwise
     * @param permissions The numerical permission value (also known as permission bits)
     *
     */
    public DefaultFileStorageObjectPermission(int entity, boolean group, int permissions) {
        super();
        this.entity = entity;
        this.group = group;
        this.permissions = permissions;
    }

    @Override
    public int getEntity() {
        return entity;
    }

    /**
     * Sets the permission entity.
     *
     * @param entity The entity to set, i.e. either the user ID in case this permission is mapped to a user, or the group ID if it is
     *               mapped to a group
     */
    public void setEntity(int entity) {
        this.entity = entity;
    }

    @Override
    public boolean isGroup() {
        return group;
    }

    /**
     * Sets the group permission flag.
     *
     * @param group <code>true</code> to indicate a group permission, <code>false</code>, otherwise
     */
    public void setGroup(boolean group) {
        this.group = group;
    }

    @Override
    public int getPermissions() {
        return permissions;
    }

    /**
     * Sets the numerical permission value (also known as permission bits).
     *
     * @param permissions The permissions, usually one of {@link #NONE}, {@link #READ}, {@link #WRITE} or {@link #DELETE}.
     */
    public void setPermissions(int permissions) {
        this.permissions = permissions;
    }

    @Override
    public boolean canRead() {
        return permissions >= READ;
    }

    @Override
    public boolean canWrite() {
        return permissions >= WRITE;
    }

    @Override
    public boolean canDelete() {
        return permissions >= DELETE;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + entity;
        result = prime * result + (group ? 1231 : 1237);
        result = prime * result + permissions;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DefaultFileStorageObjectPermission)) {
            return false;
        }
        DefaultFileStorageObjectPermission other = (DefaultFileStorageObjectPermission) obj;
        if (entity != other.entity) {
            return false;
        }
        if (group != other.group) {
            return false;
        }
        if (permissions != other.permissions) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DefaultFileStorageObjectPermission [entity=" + entity + ", group=" + group + ", permissions=" + permissions + "]";
    }

}
