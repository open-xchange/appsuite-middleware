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
 * {@link FileStorageObjectPermission}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface FileStorageObjectPermission {

    /**
     * The numerical value indicating no object permissions.
     */
    static final int NONE = 0;

    /**
     * The numerical value indicating read object permissions.
     */
    static final int READ = 1;

    /**
     * The numerical value indicating write object permissions. This implicitly includes the {@link #READ} permission (this is no bitmask).
     */
    static final int WRITE = 2;

    /**
     * The numerical value indicating delete object permissions. This implicitly includes the {@link #READ} and {@link #WRITE} permission
     * (this is no bitmask).
     */
    static final int DELETE = 4;

    /**
     * Gets the entity associated with this permission, i.e. either the user ID in case this permission is mapped to a user, or the group
     * ID if it is mapped to a group.
     *
     * @return The permission entity
     */
    int getEntity();

    /**
     * Gets a value indicating whether this permission entity represents a group or not.
     *
     * @return <code>true</code> if this permission represents a group, <code>false</code>, otherwise
     */
    boolean isGroup();

    /**
     * Gets the numerical permission value (also known as permission bits).
     *
     * @return The permissions, usually one of {@link #NONE}, {@link #READ}, {@link #WRITE} or {@link #DELETE}.
     */
    int getPermissions();

    /**
     * Gets a value indicating whether it's permitted to view and access an item or not.
     *
     * @return <code>true</code> if it's permitted to read, <code>false</code>, otherwise
     */
    boolean canRead();

    /**
     * Gets a value indicating whether it's permitted to update/change an item or not.
     *
     * @return <code>true</code> if it's permitted to write, <code>false</code>, otherwise
     */
    boolean canWrite();

    /**
     * Gets a value indicating whether it's permitted to delete an item or not.
     *
     * @return <code>true</code> if it's permitted to delete, <code>false</code>, otherwise
     */
    boolean canDelete();

}
