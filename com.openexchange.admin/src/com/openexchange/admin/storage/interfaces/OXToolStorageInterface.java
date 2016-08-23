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

package com.openexchange.admin.storage.interfaces;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import com.openexchange.admin.daemons.AdminDaemon;
import com.openexchange.admin.exceptions.OXGenericException;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.dataobjects.Resource;
import com.openexchange.admin.rmi.dataobjects.Server;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.EnforceableDataObjectException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchGroupException;
import com.openexchange.admin.rmi.exceptions.NoSuchObjectException;
import com.openexchange.admin.rmi.exceptions.NoSuchResourceException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.PropertyHandler;

public abstract class OXToolStorageInterface {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OXToolStorageInterface.class);

    /** The admin cache */
    protected static final AdminCache cache;

    /** The property handler */
    protected static final PropertyHandler prop;

    static {
        try {
            cache = AdminDaemon.getCache();
            prop = cache.getProperties();
        } catch (OXGenericException e) {
            log.warn("", e);
            throw new IllegalStateException(e);
        }
    }

    /**
     * Creates a new instance implementing the group storage interface.
     *
     * @return an instance implementing the group storage interface.
     */
    public static OXToolStorageInterface getInstance() {
        return new com.openexchange.admin.storage.mysqlStorage.OXToolMySQLStorage();
    }

    /**
     * Checks if given domain is used by any user,group or resource as mail address in given context.
     *
     * @param ctx The context
     * @param domain The domain
     * @return <code>true</code> if given domain is used by any user,group or resource as mail address; otherwise <code>false</code>
     * @throws StorageException If check fails
     */
    public abstract boolean domainInUse(final Context ctx, final String domain) throws StorageException;

    /**
     * Checks if given domain is used as mail address of any group in given context.
     *
     * @param domain
     * @return Groups which use this domain.null if no group uses this domain.
     * @throws StorageException
     */
    public abstract Group[] domainInUseByGroup(final Context ctx, final String domain) throws StorageException;

    /**
     * Checks if given domain is used as mail address of any resource in given context.
     *
     * @param domain
     * @return Resources which use this domain. null if no resource uses this domain.
     * @throws StorageException
     */
    public abstract Resource[] domainInUseByResource(final Context ctx, final String domain) throws StorageException;

    /**
     * Checks if given domain is used as alias or primary mail address of any user in given context.
     *
     * @param domain
     * @return Users which use this domain. null if no user uses this domain.
     * @throws StorageException
     */
    public abstract User[] domainInUseByUser(final Context ctx, final String domain) throws StorageException;

    /**
     * Although this method get's a context Object it will only look after the cid
     *
     * @param ctx
     * @return
     * @throws StorageException
     */
    public abstract boolean existsContext(final Context ctx) throws StorageException;

    public abstract boolean existsContextLoginMappings(final Context ctx) throws StorageException;

    public abstract boolean existsContextLoginMappings(final Context ctx, final Connection configdb_connection) throws StorageException;

    public abstract boolean existsDatabase(final int db_id) throws StorageException;

    public abstract boolean existsGroup(final Context ctx, final Group[] grps) throws StorageException;

    public abstract boolean existsGroup(final Context ctx, final Group grp) throws StorageException;

    public abstract boolean existsGroup(final Context ctx, final int gid) throws StorageException;

    /**
     * This method can be used to check if some group exists in a context. The connection is given to be able to check for groups that are
     * not committed yet.
     *
     * @param ctx Context.
     * @param con readable database connection.
     * @param id unique identifier of the group.
     * @return <code>true</code> if the group exists, <code>false</code> otherwise.
     * @throws StorageException if some problem occurs executing the SQL statements.
     */
    public abstract boolean existsGroup(final Context ctx, final Connection con, final int id) throws StorageException;

    public abstract boolean existsGroup(final Context ctx, final int[] gid) throws StorageException;

    public abstract boolean existsGroupMember(final Context ctx, final int group_ID, final int member_ID) throws StorageException;

    public abstract boolean existsGroupMember(final Context ctx, final int group_ID, final int[] user_ids) throws StorageException;

    public abstract boolean existsGroupMember(final Context ctx, final int group_ID, final User[] users) throws StorageException;

    public abstract boolean existsReason(final int rid) throws StorageException;

    public abstract boolean existsReason(final String reason) throws StorageException;

    public abstract boolean existsResource(final Context ctx, final int resource_id) throws StorageException;

    public abstract boolean existsResourceAddress(final Context ctx, final String address) throws StorageException;

    public abstract boolean existsResourceAddress(Context ctx, String address, Integer resource_id) throws StorageException;

    public abstract boolean existsServer(final int server_id) throws StorageException;

    public abstract boolean existsServerID(final int check_ID, final String table, final String field) throws StorageException;

    public abstract boolean existsStore(final int store_id) throws StorageException;

    public abstract boolean existsStore(final String url) throws StorageException;

    public abstract boolean existsUser(final Context ctx, final int uid) throws StorageException;

    public abstract boolean existsUser(final Context ctx, final int[] user_ids) throws StorageException;

    public abstract boolean isGuestUser(Context ctx, int uid) throws StorageException;

    /**
     * This method will detect if a specified user exists. It check this through the user id and the
     * user name specified in the user object
     *
     * @param ctx The context
     * @param users
     * @return
     * @throws StorageException If existence check fails
     */
    public abstract boolean existsUser(Context ctx, User[] users) throws StorageException;

    /**
     * A convenience method for a single user object. See {@link #existsUser(Context, User[])}
     *
     * @param ctx The context
     * @param user
     * @return
     * @throws StorageException If existence check fails
     */
    public abstract boolean existsUser(Context ctx, User user) throws StorageException;

    /**
     * Checks via group id and group name if it already exists in this context. Should be used in change method!
     *
     * @param ctx The context
     * @param grp
     * @return
     * @throws StorageException If existence check fails
     */
    public abstract boolean existsGroupName(Context ctx, Group grp) throws StorageException;

    /**
     * Checks if given name is already associated with a group in given context.Should be used in create method!
     *
     * @param ctx The context
     * @param groupName
     * @return
     * @throws StorageException If existence check fails
     */
    public abstract boolean existsGroupName(Context ctx, String groupName) throws StorageException;

    /**
     * Checks via user identifier and user name if it already exists in this context. Should be used in change method!
     *
     * @param ctx The context
     * @param usr The user to check
     * @return <code>true</code> if such a user does exist; otherwise <code>false</code>
     * @throws StorageException If existence check fails
     */
    public abstract boolean existsUserName(Context ctx, User usr) throws StorageException;

    /**
     * Checks if given name is already associated with a user in given context.
     * <p>
     * Should be used in create method!
     *
     * @param ctx The context
     * @param userName The user name
     * @return <code>true</code> if such a user name is associated with a user; otherwise <code>false</code>
     * @throws StorageException If existence check fails
     */
    public abstract boolean existsUserName(Context ctx, String userName) throws StorageException;

    /**
     * Checks if given display name is already associated with a user in given context.
     *
     * @param ctx The context
     * @param displayName The display name to check
     * @return <code>true</code> if such a display name is associated with a user; otherwise <code>false</code>
     * @throws StorageException If existence check fails
     */
    public abstract boolean existsDisplayName(Context ctx, String displayName) throws StorageException;

    /**
     * Checks via server id and server name if it already exists. Should be used in change method!
     *
     * @param srv The server
     * @return <code>true</code> if such a server does exist; otherwise <code>false</code>
     * @throws StorageException If existence check fails
     */
    public abstract boolean existsServerName(Server srv) throws StorageException;

    /**
     * Checks if given name is already used!
     * <p>
     * Should be used in create method!
     *
     * @param serverName The server name
     * @return <code>true</code> if such a server does exist; otherwise <code>false</code>
     * @throws StorageException If existence check fails
     */
    public abstract boolean existsServerName(String serverName) throws StorageException;

    /**
     * Checks via database id and database name if it already exists.
     * <p>
     * Should be used in change method!
     *
     * @param db The database
     * @return <code>true</code> if such a database does exist; otherwise <code>false</code>
     * @throws StorageException If existence check fails
     */
    public abstract boolean existsDatabaseName(Database db) throws StorageException;

    /**
     * Checks if given name is already used!Should be used in create method!
     *
     * @param databaseName The database name
     * @return <code>true</code> if such a database does exist; otherwise <code>false</code>
     * @throws StorageException If existence check fails
     */
    public abstract boolean existsDatabaseName(String databaseName) throws StorageException;

    /**
     * Checks via resource id and resource name if it already exists. Should be used in change method!
     *
     * @param ctx The context
     * @param res
     * @return
     * @throws StorageException
     */
    public abstract boolean existsResourceName(Context ctx, Resource res) throws StorageException;

    /**
     * Checks if given name is already used for resource in given context!Should be used in create method!
     *
     * @param ctx The context
     * @param resourceName
     * @return
     * @throws StorageException If existence check fails
     */
    public abstract boolean existsResourceName(Context ctx, String resourceName) throws StorageException;

    /**
     * Checks via context id and context name if it already exists.
     * <p>
     * Should be used in change method!
     *
     * @param ctx The context
     * @return <code>true</code> if such a context does exist; otherwise <code>false</code>
     * @throws StorageException If existence check fails
     */
    public abstract boolean existsContextName(Context ctx) throws StorageException;

    /**
     * Checks if given context name already exists!
     * <p>
     * Should be used in create method!
     *
     * @param contextName The context name
     * @return <code>true</code> if such a context does exist; otherwise <code>false</code>
     * @throws StorageException If existence check fails
     */
    public abstract boolean existsContextName(final String contextName) throws StorageException;

    public abstract int getAdminForContext(final Context ctx, final Connection con) throws StorageException;

    public abstract int getAdminForContext(final Context ctx) throws StorageException;

    public abstract int getContextIDByContextname(String ctxName) throws StorageException, NoSuchObjectException;

    public abstract int getDatabaseIDByDatabasename(String dbName) throws StorageException, NoSuchObjectException;

    /**
     * Gets the database for specified database schema name
     *
     * @param schemaName The database schema name
     * @return The database
     * @throws StorageException If database cannot be loaded
     * @throws NoSuchObjectException If there is no such database schema
     */
    public abstract int getDatabaseIDByDatabaseSchema(String schemaName) throws StorageException, NoSuchObjectException;

    /**
     * Gets the database schema for specified context.
     *
     * @param contextId The context identifier
     * @return The database schema
     * @throws StorageException If database cannot be loaded
     * @throws NoSuchObjectException If there is no such database schema
     */
    public abstract Database getSchemaByContextId(int contextId) throws StorageException, NoSuchObjectException;

    /**
     * Generates an appropriate {@link DatabaseUpdateException} for specified context.
     *
     * @param contextId The context identifier
     * @return The <code>DatabaseUpdateException</code> instance
     */
    public abstract DatabaseUpdateException generateDatabaseUpdateException(int contextId);

    /**
     * Checks whether all contexts of the given db share the same write pool ID or not.
     *
     * @param schema the schema name
     * @return <code>true</code> if a distinct pool ID is used for all contexts.
     * @throws StorageException If database cannot be loaded
     * @throws NoSuchObjectException If there is no such database schema
     */
    public abstract boolean isDistinctWritePoolIDForSchema(String schema) throws StorageException, NoSuchObjectException;

    /**
     * Load database information with the given identifier.
     *
     * @param id the identifier of the database. It must be the identifier of the master.
     * @return the database information with the given identifier.
     * @throws StorageException if the database with the given identifier does not exist or a problem occurs when loading it.
     */
    public abstract Database loadDatabaseById(int id) throws StorageException;

    public abstract int getDefaultGroupForContext(final Context ctx, final Connection con) throws StorageException;

    public abstract int getDefaultGroupForContextWithOutConnection(final Context ctx) throws StorageException;

    /**
     * Gets the system's GID for specified group.
     *
     * @param ctx The context
     * @param group_id The group ID
     * @param con A connection with at least read-only permission
     * @return The system's GID for specified group or <code>-1</code> if no GID is set.
     * @throws StorageException If a storage error occurs or group does not exist
     */
    public abstract int getGidNumberOfGroup(final Context ctx, final int group_id, final Connection con) throws StorageException;

    public abstract int getGroupIDByGroupname(Context ctx, String groupName) throws StorageException, NoSuchGroupException;

    public abstract String getGroupnameByGroupID(final Context ctx, final int group_id) throws StorageException;

    public abstract int getResourceIDByResourcename(Context ctx, String resourceName) throws StorageException, NoSuchResourceException;

    public abstract String getResourcenameByResourceID(final Context ctx, final int resource_id) throws StorageException;

    public abstract int getServerIDByServername(String serverName) throws StorageException, NoSuchObjectException;

    public abstract int getUserIDByUsername(Context ctx, String userName) throws StorageException, NoSuchUserException;

    public abstract String getUsernameByUserID(final Context ctx, final int user_id) throws StorageException;

    public abstract boolean getIsGuestByUserID(final Context ctx, final int user_id) throws StorageException;

    public abstract boolean isContextAdmin(final Context ctx, final int user_id) throws StorageException;

    /**
     * This method determines if the user is the context admin. Therefore either the user_id or the username
     * have to be filled. Furthermore this methods sets the uid in the given user object to the correct
     * value if the object contains the name only.
     *
     * @param ctx
     * @param user
     * @return
     * @throws StorageException
     */
    public abstract boolean isContextAdmin(final Context ctx, User user) throws StorageException;

    public abstract boolean isContextEnabled(final Context ctx) throws StorageException;

    /**
     * This method checks if the display name of the given user object is already used for a system user. This method doesn't check for
     * display names in the contacts of the users.
     *
     * @param ctx
     * @param usr
     * @param userId optional user identifier parameter to exclude the user to change. If not applicable give zero.
     * @return <code>true</code> if the display name is already in use by some other user.
     * @throws StorageException if a problem occurs on the storage layer.
     */
    public abstract boolean existsDisplayName(final Context ctx, final User usr, final int userId) throws StorageException;

    public abstract boolean isMasterDatabase(final int database_id) throws StorageException;

    public abstract boolean isUserSettingMailBitSet(final Context ctx, final User user, final int bit, final Connection con) throws StorageException;

    public abstract boolean poolInUse(final int pool_id) throws StorageException;

    public abstract void primaryMailExists(Context ctx, String mail) throws StorageException, InvalidDataException;

    public final boolean checkAndUpdateSchemaIfRequired(Context ctx) throws StorageException {
        return checkAndUpdateSchemaIfRequired(ctx.getId().intValue());
    }

    public abstract boolean checkAndUpdateSchemaIfRequired(int contextId) throws StorageException;

    public abstract boolean schemaBeingLockedOrNeedsUpdate(final int writePoolId, final String schema) throws StorageException;

    /**
     * Lists all schemas in databases that are either locked (currently marked as being updated) or needing an update.
     *
     * @return All such schemas as a list with length of 3; first element contains schemas needing an update, the second those schemas currently marked for being updated, and the third providing outdated updating schemas
     * @throws StorageException If such schemas cannot be returned
     */
    public abstract List<List<Database>> listSchemasBeingLockedOrNeedsUpdate() throws StorageException;

    /**
     * Unblocks specified database schema or all schemas associated with specified database inc ase no schema name is given.
     *
     * @return The list of unblocked database schemas
     * @throws StorageException If unblocking fails
     */
    public abstract List<Database> unblockDatabaseSchema(Database db) throws StorageException;

    public abstract boolean serverInUse(final int server_id) throws StorageException;

    public abstract void setUserSettingMailBit(final Context ctx, final User user, final int bit, final Connection con) throws StorageException;

    public abstract boolean storeInUse(final int store_id) throws StorageException;

    public abstract void unsetUserSettingMailBit(final Context ctx, final User user, final int bit, final Connection con) throws StorageException;

    public abstract void checkCreateUserData(Context ctx, User usr) throws InvalidDataException, EnforceableDataObjectException, StorageException;

    public abstract void validateUserName(String name) throws InvalidDataException;

    public abstract void checkValidEmailsInUserObject(User user) throws InvalidDataException;

    /**
     * Changes access rights for all users in the Database.
     *
     * @param filter Only users with this access combination are affected. Set to -1 for no filter.
     * @param addAccess
     * @param removeAccess
     * @throws StorageException
     */
    public abstract void changeAccessCombination(int filter, int addAccess, int removeAccess) throws StorageException;

    /**
     * Verifies whether the specified user is the owner of a master filestore and other users are using this filestore
     *
     * @param context The context
     * @param userId The user identifier
     * @return true if the user is a master filestore owner AND other users are using this filestore; false otherwise
     * @throws StorageException
     */
    public abstract boolean isMasterFilestoreOwner(Context context, int userId) throws StorageException;

    /**
     * Fetches the slave users of the master filestore
     *
     * @param context
     * @param userId
     * @return
     * @throws StorageException
     */
    public abstract Map<Integer, List<Integer>> fetchSlaveUsersOfMasterFilestore(Context context, int userId) throws StorageException;

    /**
     * Determines whether the specified context is the last one on the database schema
     *
     * @param context The context
     * @return true if the specified context is the last one on the database schema; false otherwise
     * @throws StorageException if a problem occurs on the storage layer.
     * @throws InvalidDataException if the specified context does not exist in any known database schema
     */
    public abstract boolean isLastContextInSchema(Context context) throws StorageException, InvalidDataException;

}
