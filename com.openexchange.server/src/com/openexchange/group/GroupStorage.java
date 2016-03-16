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

package com.openexchange.group;

import java.sql.Connection;
import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;

/**
 * This class defines the storage API for groups. This is a low level API for reading and writing groups into some storage - normally
 * databases.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class GroupStorage {

    /**
     * Identifies the virtual 0-group containing all users but no guests.
     */
    public static final int GROUP_ZERO_IDENTIFIER = 0;

    /**
     * Identifies the virtual guest group containing all guests.
     */
    public static final int GUEST_GROUP_IDENTIFIER = Integer.MAX_VALUE;

    public static final String GROUP_STANDARD_SIMPLE_NAME = "users";

    private static volatile GroupStorage instance;

    /**
     * Private constructor to prevent instantiation.
     */
    protected GroupStorage() {
        super();
    }

    /**
     * This method inserts a group without its members into the storage.
     *
     * @param ctx Context.
     * @param con writable database connection.
     * @param group group to insert.
     * @throws OXException if some problem occurs.
     */
    public final void insertGroup(final Context ctx, final Connection con, final Group group) throws OXException {
        insertGroup(ctx, con, group, StorageType.ACTIVE);
    }

    /**
     * This method inserts a group without its members into the storage.
     *
     * @param ctx Context.
     * @param con writable database connection.
     * @param group group to insert.
     * @param type defines if group is inserted ACTIVE or DELETED.
     * @throws OXException if some problem occurs.
     */
    public abstract void insertGroup(Context ctx, Connection con, Group group, StorageType type) throws OXException;

    /**
     * This method updates group field in the storage.
     *
     * @param ctx Context.
     * @param con writable database connection.
     * @param group group with fields to update.
     * @param lastRead timestamp when the group has been read last.
     * @throws OXException if updating does not finish successfully.
     */
    public abstract void updateGroup(Context ctx, Connection con, Group group, Date lastRead) throws OXException;

    public abstract void insertMember(Context ctx, Connection con, Group group, int[] members) throws OXException;

    public abstract void deleteMember(Context ctx, Connection con, Group group, int[] members) throws OXException;

    /**
     * This method deletes a group from the database. Before all its members must be removed.
     *
     * @param ctx Context.
     * @param con writable database connection.
     * @param groupId unique identifier of the group to delete.
     * @param lastRead timestamp when the group has been read last.
     * @throws OXException if deleting fails.
     */
    public abstract void deleteGroup(Context ctx, Connection con, int groupId, Date lastRead) throws OXException;

    /**
     * Reads a group from the persistent storage.
     *
     * @param gid Unique identifier of the group.
     * @param The context.
     * @return The group data object.
     * @throws OXException If an error occurs
     */
    public abstract Group getGroup(int gid, Context context) throws OXException;

    /**
     * This method implements a universal search for groups. You have to define additionally to the search pattern the attributes that
     * should be searched in. You can also name the attributes that values should be returned. Please insure that returned attributes are
     * strings and not any other data types. You will get a Set with string arrays. The string arrays contain the values auf the requested
     * attributes in the same order.
     *
     * @param pattern this pattern will be searched in the displayName of the group.
     * @param loadMembers - switch whether members should be loaded, too (decreases performance, don't use if not needed)
     * @param The context.
     * @return an array of groups that match the search pattern.
     * @throws OXException if searching has some storage related problem.
     */
    public abstract Group[] searchGroups(String pattern, boolean loadMembers, Context context) throws OXException;

    /**
     * This method returns groups that have been modified since the given timestamp.
     *
     * @param modifiedSince timestamp after that the groups have been modified.
     * @param The context.
     * @return an array of groups.
     * @throws OXException if an error occurs.
     */
    public abstract Group[] listModifiedGroups(Date modifiedSince, Context context) throws OXException;

    /**
     * This metods returns groups that have been deleted since the given timestamp
     *
     * @param modifiedSince timestamp after that the groups have been deleted.
     * @param The context.
     * @return an array of groups.
     * @throws OXException if an error occurs.
     */
    public abstract Group[] listDeletedGroups(Date modifiedSince, Context context) throws OXException;

    /**
     * Returns the data objects of all groups.
     *
     * @param context - the context.
     * @param loadMembers - switch whether members should be loaded, too (decreases performance, don't use if not needed)
     * @return all groups.
     * @throws OXException if an error occurs.
     */
    public abstract Group[] getGroups(boolean loadMembers, Context context) throws OXException;

    /**
     * Creates a new instance implementing the group storage interface.
     *
     * @return an instance implementing the group storage interface.
     */
    public static GroupStorage getInstance() {
        return instance;
    }

    public static void setInstance(final GroupStorage instance) {
        GroupStorage.instance = instance;
    }

    public static enum StorageType {
        /**
         * Storage type for currently active groups.
         */
        ACTIVE,
        /**
         * Storage type for deleted groups. This must be filled with deleted groups to inform synchronizing clients about not more existing
         * groups.
         */
        DELETED
    }

}
