/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
public interface GroupStorage {

    /**
     * Identifies the virtual 0-group containing all users but no guests.
     */
    public static final int GROUP_ZERO_IDENTIFIER = 0;

    /**
     * Identifies the virtual guest group containing all guests.
     */
    public static final int GUEST_GROUP_IDENTIFIER = Integer.MAX_VALUE;

    /**
     * The reserved name for special group containing all users.
     */
    public static final String GROUP_STANDARD_SIMPLE_NAME = "users";

    /**
     * The special (cache) identifier referencing the identifiers of all groups in a context.
     */
    public static final int SPECIAL_FOR_ALL_GROUP_IDS = -1;

    /**
     * The name for the cache region holding group information.
     */
    public static final String CACHE_REGION_NAME = "Group";

    /**
     * This method inserts a group without its members into the storage.
     *
     * @param ctx Context.
     * @param con writable database connection.
     * @param group group to insert.
     * @throws OXException if some problem occurs.
     */
    public default void insertGroup(final Context ctx, final Connection con, final Group group) throws OXException {
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
    public void insertGroup(Context ctx, Connection con, Group group, StorageType type) throws OXException;

    /**
     * This method updates group field in the storage.
     *
     * @param ctx Context.
     * @param con writable database connection.
     * @param group group with fields to update.
     * @param lastRead timestamp when the group has been read last.
     * @throws OXException if updating does not finish successfully.
     */
    public void updateGroup(Context ctx, Connection con, Group group, Date lastRead) throws OXException;

    /**
     * Insert a member into a group
     *
     * @param ctx The context
     * @param con The connection to use
     * @param group The group to change
     * @param members The members to add
     * @throws OXException
     */
    public void insertMember(Context ctx, Connection con, Group group, int[] members) throws OXException;

    /**
     * Removes a member from a group.
     *
     * @param ctx The context
     * @param con The connection to use
     * @param group The group to change
     * @param members The members to remove
     * @throws OXException
     */
    public void deleteMember(Context ctx, Connection con, Group group, int[] members) throws OXException;

    /**
     * This method deletes a group from the database. Before all its members must be removed.
     *
     * @param ctx Context.
     * @param con writable database connection.
     * @param groupId unique identifier of the group to delete.
     * @param lastRead timestamp when the group has been read last.
     * @throws OXException if deleting fails.
     */
    public void deleteGroup(Context ctx, Connection con, int groupId, Date lastRead) throws OXException;

    /**
     * Reads a group from the persistent storage.
     *
     * @param gid Unique identifier of the group.
     * @param The context.
     * @return The group data object.
     * @throws OXException If an error occurs
     */
    public Group getGroup(int gid, Context context) throws OXException;

    /**
     * Reads a group from the persistent storage.
     *
     * @param gid Unique identifier of the group.
     * @param loadMembers - switch whether members should be loaded, too (decreases performance, don't use if not needed)
     * @param The context.
     * @return The group data object.
     * @throws OXException If an error occurs
     */
    public Group getGroup(int gid, boolean loadMembers, Context context) throws OXException;

    /**
     * Reads group ids from the persistent storage.
     *
     * @param gid[] Array of unique identifiers of the group.
     * @param The context.
     * @return The group data object.
     * @throws OXException
     */
    public abstract Group[] getGroup(int[] gid, Context context) throws OXException;

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
    public Group[] searchGroups(String pattern, boolean loadMembers, Context context) throws OXException;

    /**
     * This method returns groups that have been modified since the given timestamp.
     *
     * @param modifiedSince timestamp after that the groups have been modified.
     * @param The context.
     * @return an array of groups.
     * @throws OXException if an error occurs.
     */
    public Group[] listModifiedGroups(Date modifiedSince, Context context) throws OXException;

    /**
     * This metods returns groups that have been deleted since the given timestamp
     *
     * @param modifiedSince timestamp after that the groups have been deleted.
     * @param The context.
     * @return an array of groups.
     * @throws OXException if an error occurs.
     */
    public Group[] listDeletedGroups(Date modifiedSince, Context context) throws OXException;

    /**
     * Returns the data objects of all groups.
     *
     * @param context - the context.
     * @param loadMembers - switch whether members should be loaded, too (decreases performance, don't use if not needed)
     * @return all groups.
     * @throws OXException if an error occurs.
     */
    public Group[] getGroups(boolean loadMembers, Context context) throws OXException;

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
