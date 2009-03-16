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

package com.openexchange.group;

import java.sql.Connection;
import java.util.Date;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.LdapException;

/**
 * This class defines the storage API for groups. This is a low level API for
 * reading and writing groups into some storage - normally databases.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class GroupStorage {

    private static GroupStorage instance;

    private static GroupStorage instanceWithZero;

    /**
     * Private constructor to prevent instantiation.
     */
    protected GroupStorage() {
        super();
    }

    /**
     * This method inserts a group without its members into the storage.
     * @param ctx Context.
     * @param con writable database connection.
     * @param group group to insert.
     * @throws GroupException if some problem occurs.
     */
    public final void insertGroup(final Context ctx, final Connection con,
        final Group group) throws GroupException {
        insertGroup(ctx, con, group, StorageType.ACTIVE);
    }

    /**
     * This method inserts a group without its members into the storage.
     * @param ctx Context.
     * @param con writable database connection.
     * @param group group to insert.
     * @param type defines if group is inserted ACTIVE or DELETED.
     * @throws GroupException if some problem occurs.
     */
    public abstract void insertGroup(Context ctx, Connection con, Group group,
        StorageType type) throws GroupException;

    /**
     * This method updates group field in the storage.
     * @param ctx Context.
     * @param con writable database connection.
     * @param group group with fields to update.
     * @param lastRead timestamp when the group has been read last.
     * @throws GroupException if updating does not finish successfully.
     */
    public abstract void updateGroup(Context ctx, Connection con, Group group,
        Date lastRead) throws GroupException;

    public abstract void insertMember(Context ctx, Connection con, Group group,
        int[] members) throws GroupException;

    public abstract void deleteMember(Context ctx, Connection con, Group group,
        int[] members) throws GroupException;

    /**
     * This method deletes a group from the database. Before all its members
     * must be removed.
     * @param ctx Context.
     * @param con writable database connection.
     * @param groupId unique identifier of the group to delete.
     * @param lastRead timestamp when the group has been read last.
     * @throws GroupException if deleting fails.
     */
    public abstract void deleteGroup(Context ctx, Connection con, int groupId,
        Date lastRead) throws GroupException;

    /**
     * Reads a group from the persistent storage.
     * @param gid Unique identifier of the group.
     * @param The context.
     * @return The group data object.
     * @throws LdapException if an error occurs.
     */
    public abstract Group getGroup(int gid, Context context) throws LdapException;

    /**
     * This method implements a universal search for groups. You have to define
     * additionally to the search pattern the attributes that should be searched
     * in. You can also name the attributes that values should be returned.
     * Please insure that returned attributes are strings and not any other data
     * types. You will get a Set with string arrays. The string arrays contain
     * the values auf the requested attributes in the same order.
     * @param pattern this pattern will be searched in the displayName of the
     * group.
     * @param The context.
     * @return an array of groups that match the search pattern.
     * @throws GroupException if searching has some storage related problem.
     */
    public abstract Group[] searchGroups(String pattern, Context context) throws GroupException;

    /**
     * This method returns groups that have been modified since the given
     * timestamp.
     * @param modifiedSince timestamp after that the groups have been modified.
     * @param The context.
     * @return an array of groups.
     * @throws LdapException if an error occurs.
     */
    public abstract Group[] listModifiedGroups(Date modifiedSince, Context context)
        throws LdapException;

    /**
     * Returns the data objects of all groups.
     * @param The context.
     * @return all groups.
     * @throws LdapException if an error occurs.
     */
    public abstract Group[] getGroups(Context context) throws LdapException;

    /**
     * Creates a new instance implementing the group storage interface.
     * @return an instance implementing the group storage interface.
     */
    public static GroupStorage getInstance() {
        return getInstance(false);
    }

    /**
     * Creates a new instance implementing the group storage interface. The
     * returned instance can also handle the group with identifier 0.
     * @param group0 <code>true</code> if group with identifier 0 should be
     * handled.
     * @return an instance implementing the group storage interface.
     */
    public static GroupStorage getInstance(final boolean group0) {
        return group0 ? getInstanceWithZero() : instance;
    }

    public static void setInstance(final GroupStorage instance) {
        GroupStorage.instance = instance;
    }

    public static void setInstanceWithZero(final GroupStorage instanceWithZero) {
        GroupStorage.instanceWithZero = instanceWithZero;
    }

    public static GroupStorage getInstanceWithZero() {
        return instanceWithZero;
    }

    public static enum StorageType {
        /**
         * Storage type for currently active groups.
         */
        ACTIVE,
        /**
         * Storage type for deleted groups. This must be filled with deleted
         * groups to inform synchronizing clients about not more existing groups.
         */
        DELETED
    }
}
