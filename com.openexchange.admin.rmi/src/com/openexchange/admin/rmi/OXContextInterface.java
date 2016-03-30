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

package com.openexchange.admin.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.Quota;
import com.openexchange.admin.rmi.dataobjects.SchemaSelectStrategy;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.ContextExistsException;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchDatabaseException;
import com.openexchange.admin.rmi.exceptions.NoSuchFilestoreException;
import com.openexchange.admin.rmi.exceptions.NoSuchReasonException;
import com.openexchange.admin.rmi.exceptions.OXContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;


/**
 * This class defines the Open-Xchange API Version 2 for creating and manipulating OX Contexts.<br><br>
 *
 * To create a new context, you must first register a database,server and filestore to the system!<br>
 * For details about registering a filestore,server or databases have a look in the {@link OXUtilInterface}.<br><br>
 *
 * <b>Example:</b>
 * <pre>
 * final OXContextInterface iface = (OXContextInterface)Naming.lookup("rmi:///oxhost/"+OXContextInterface.RMI_NAME);
 *
 * final Context ctx = new Context(1337);
 *
 * User usr = new User();
 * usr.setDisplay_name("admin display name");
 * usr.setName("admin");
 * usr.setPassword("secret");
 * usr.setMailenabled(true);
 * usr.setPrimaryEmail("admin@example.org");
 * usr.setEmail1("admin@example.org");
 * usr.setGiven_name("my");
 * usr.setSur_name("admin");
 *
 *
 * final Credentials auth = new Credentials();
 * auth.setLogin("admin");
 * auth.setPassword("secret");
 *
 * iface.create(ctx,usr,access,auth);
 *
 * </pre>
 *
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a>
 * @author <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public interface OXContextInterface extends Remote {

    /**
     * RMI name to be used in the naming lookup.
     */
    public static final String RMI_NAME = "OXContext_V2";

    /**
     * Create a new context.
     *
     * If setFilestoreId() or setWriteDatabase() has been used in the given context object, the context will be created
     * in the corresponding database or filestore.
     * The assigned limits to the database/filestore are ignored, though.
     *
     * @param ctx A new Context object, this should not have been used before or a one returned from a previous call to this API.
     * @param admin_user User data of administrative user account for this context
     * @param auth Credentials for authenticating against server.
     *
     * @return Context object.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     * @throws StorageException When an error in the subsystems occurred.
     * @throws ContextExistsException
     */
    public Context create(final Context ctx, final User admin_user, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException,InvalidDataException, ContextExistsException;

    /**
     * Create a new context! Given access combination name will be used for admin module access rights!
     * @param ctx Context object
     * @param admin_user User data of administrative user account for this context
     * @param access_combination_name String Access combination name!
     * @param auth Credentials for authenticating against server.
     *
     * @return A new Context object, this should not have been used before or a one returned from a previous call to this API.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     * @throws StorageException When an error in the subsystems occurred.
     * @throws ContextExistsException
     */
    public Context create(final Context ctx, final User admin_user, String access_combination_name,final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException,InvalidDataException, ContextExistsException;

    /**
     * Create a new context! Given access rights be used for admin!
     * @param ctx Context object
     * @param admin_user User data of administrative user account for this context
     * @param access UserModuleAccess Access rights!
     * @param auth Credentials for authenticating against server.
     *
     * @return A new Context object, this should not have been used before or a one returned from a previous call to this API.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     * @throws StorageException When an error in the subsystems occurred.
     * @throws ContextExistsException
     */
    public Context create(final Context ctx, final User admin_user, UserModuleAccess access,final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException,InvalidDataException, ContextExistsException;

    /**
     * Create a new context.
     *
     * If setFilestoreId() or setWriteDatabase() has been used in the given context object, the context will be created
     * in the corresponding database or filestore.
     * The assigned limits to the database/filestore are ignored, though.
     *
     * @param ctx A new Context object, this should not have been used before or a one returned from a previous call to this API.
     * @param admin_user User data of administrative user account for this context
     * @param auth Credentials for authenticating against server.
     * @param schemaSelectStrategy SchemaSelectStrategy to define how to select the schema where the context will be created.
     *
     * @return Context object.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     * @throws StorageException When an error in the subsystems occurred.
     * @throws ContextExistsException
     */
    public Context create(final Context ctx, final User admin_user, final Credentials auth, SchemaSelectStrategy schemaSelectStrategy) throws RemoteException, StorageException, InvalidCredentialsException,InvalidDataException, ContextExistsException;

    /**
     * Create a new context! Given access combination name will be used for admin module access rights!
     * @param ctx Context object
     * @param admin_user User data of administrative user account for this context
     * @param access_combination_name String Access combination name!
     * @param auth Credentials for authenticating against server.
     * @param schemaSelectStrategy SchemaSelectStrategy to define how to select the schema where the context will be created.
     *
     * @return A new Context object, this should not have been used before or a one returned from a previous call to this API.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     * @throws StorageException When an error in the subsystems occurred.
     * @throws ContextExistsException
     */
    public Context create(final Context ctx, final User admin_user, String access_combination_name,final Credentials auth, SchemaSelectStrategy schemaSelectStrategy) throws RemoteException, StorageException, InvalidCredentialsException,InvalidDataException, ContextExistsException;

    /**
     * Create a new context! Given access rights be used for admin!
     * @param ctx Context object
     * @param admin_user User data of administrative user account for this context
     * @param access UserModuleAccess Access rights!
     * @param auth Credentials for authenticating against server.
     * @param schemaSelectStrategy SchemaSelectStrategy to define how to select the schema where the context will be created.
     *
     * @return A new Context object, this should not have been used before or a one returned from a previous call to this API.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     * @throws StorageException When an error in the subsystems occurred.
     * @throws ContextExistsException
     */
    public Context create(final Context ctx, final User admin_user, UserModuleAccess access,final Credentials auth, SchemaSelectStrategy schemaSelectStrategy) throws RemoteException, StorageException, InvalidCredentialsException,InvalidDataException, ContextExistsException;


    /**
     * Delete a context.<br>
     * Note: Deleting a context will delete all data which the context include (all users, groups, appointments, ... )
     *
     * @param auth Credentials for authenticating against server.
     * @param ctx A new Context object, this should not have been used before or a one returned from a previous call to this API.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.NoSuchContextException If the context does not exist in the system.
     *
     * @throws RemoteException General RMI Exception
     * @throws StorageException When an error in the subsystems occurred.
     * @throws DatabaseUpdateException
     * @throws InvalidDataException
     */
    public void delete(final Context ctx, final Credentials auth) throws RemoteException,InvalidCredentialsException,NoSuchContextException,StorageException, DatabaseUpdateException, InvalidDataException;

    /**
     * If context was changed, call this method to flush data
     * which is no longer needed due to access permission changes!
     *
     * @param ctx A new Context object, this should not have been used before or a one returned from a previous call to this API.
     * @param auth Credentials for authenticating against server.
     * @throws RemoteException General RMI Exception
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException When an error in the subsystems occurred.
     * @throws DatabaseUpdateException
     * @throws InvalidDataException
     */
    public void downgrade(final Context ctx, final Credentials auth) throws RemoteException,InvalidCredentialsException,NoSuchContextException,StorageException, DatabaseUpdateException, InvalidDataException;


    /**
     * Move all data of a context contained on the filestore to another filestore
     *         <p>
     *         This method returns immediately and the data is going to be copied
     *         in the background. To query the progress and the result of the actual
     *         task, the AdminJobExecutor interface must be used.
     *
     * @param ctx A new Context object, this should not have been used before or a one returned from a previous call to this API.
     * @param dst_filestore_id Id of the Filestore to move the context in.
     * @param auth Credentials for authenticating against server.
     * @return Job id which can be used for retrieving progress information.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.NoSuchContextException If the context does not exist in the system.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     * @throws StorageException When an error in the subsystems occured.
     * @throws NoSuchFilestoreException
     * @throws NoSuchReasonException
     * @throws OXContextException
     */
    //public String moveContextFilestore(Context ctx, Filestore dst_filestore_id, MaintenanceReason reason, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, NoSuchFilestoreException, NoSuchReasonException, OXContextException;
    //* @param reason ID of the maintenance reason for disabling the context while the move is in progress.
    public int moveContextFilestore(final Context ctx, final Filestore dst_filestore_id, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, NoSuchFilestoreException, NoSuchReasonException, OXContextException;

    /**
     * Move all data of a context contained in a database to another database
     *
     * @param ctx A new Context object, this should not have been used before or a one returned from a previous call to this API.
     * @param dst_database_id ID of a registered Database to move all data of this context in.
     * @param auth Credentials for authenticating against server.
     * @return String containing return queue id to query status of job.
     *         <p>
     *         This method returns immediately and the data is going to be copied
     *         in the background. To query the progress and the result of the actual
     *         task, the AdminJobExecutor interface must be used.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.NoSuchContextException If the context does not exist in the system.
     *
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     *
     * @throws StorageException When an error in the subsystems occured.
     * @throws DatabaseUpdateException
     * @throws OXContextException
     */
    //    public int moveContextDatabase(Context ctx, Database dst_database_id, MaintenanceReason reason, Credentials auth) throws RemoteException,InvalidCredentialsException,NoSuchContextException,StorageException,InvalidDataException, DatabaseUpdateException, OXContextException;
    //    * @param reason ID of the maintenance reason for disabling the context while the move is in progress.
    public int moveContextDatabase(final Context ctx, final Database dst_database_id, final Credentials auth) throws RemoteException,InvalidCredentialsException,NoSuchContextException,StorageException,InvalidDataException, DatabaseUpdateException, OXContextException;

    /**
     * Disable given context.<br>
     *
     * @param ctx A new Context object, this should not have been used before or a one returned from a previous call to this API.
     * @param auth Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.NoSuchContextException If the context does not exist in the system.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     * @throws StorageException When an error in the subsystems occurred.
     * @throws NoSuchReasonException
     * @throws OXContextException
     */
    //public void disable(Context ctx, MaintenanceReason reason, Credentials auth) throws RemoteException,InvalidCredentialsException,NoSuchContextException,StorageException,InvalidDataException, NoSuchReasonException, OXContextException;
    //* @param reason MaintenanceReason
    public void disable(final Context ctx, final Credentials auth) throws RemoteException,InvalidCredentialsException,NoSuchContextException,StorageException,InvalidDataException, NoSuchReasonException, OXContextException;

    /**
     * Enable given context.
     *
     * @param auth Credentials for authenticating against server.
     * @param ctx A new Context object, this should not have been used before or a one returned from a previous call to this API.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.NoSuchContextException If the context does not exist in the system.
     * @throws RemoteException General RMI Exception
     * @throws StorageException When an error in the subsystems occurred.
     * @throws InvalidDataException
     */
    public void enable(final Context ctx, final Credentials auth) throws RemoteException,InvalidCredentialsException,NoSuchContextException,StorageException, InvalidDataException;

    /**
     * Search for contexts<br>
     * Returns all contexts matching the provided search_pattern.
     * The search pattern is directly transformed into a SQL LIKE string comparison, where<br>
     * a * is transformed into a %<br>
     * a % and a _ must be escaped by a \ (e.g. if you want to search for _doe, use the pattern \_doe
     *
     * @param auth Credentials for authenticating against server.
     * @param search_pattern Search pattern e.g "*mycontext*".
     * @return Contexts.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     * @throws StorageException When an error in the subsystems occurred.
     */
    public Context[] list(final String search_pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException,InvalidDataException;

    /**
     * Convenience method for listing all contexts
     * Use this for search a context or list all contexts.
     *
     * @param auth Credentials for authenticating against server.
     * @return Contexts.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     * @throws StorageException When an error in the subsystems occurred.
     */
    public Context[] listAll(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

    /**
     * Disable all contexts.<br>
     *
     * @param auth Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     *
     * @throws StorageException When an error in the subsystems occurred.
     * @throws NoSuchReasonException
     */
    //public void disableAll(MaintenanceReason reason, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException,InvalidDataException, NoSuchReasonException;
    //* @param reason MaintenanceReason
    public void disableAll(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException,InvalidDataException, NoSuchReasonException;

    /**
     * Enable all contexts.
     *
     * @param auth Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws RemoteException General RMI Exception
     *
     * @throws StorageException When an error in the subsystems occurred.
     */
    public void enableAll(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException;

    /**
     * Get specified context details
     *
     * @param ctx A new Context object, this should not have been used before or a one returned from a previous call to this API. The context IDs of these objects should be set.
     * @param auth The context-specific credentials
     * @return
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     */
    public Context getOwnData(Context ctx, Credentials auth) throws RemoteException,InvalidCredentialsException,NoSuchContextException,StorageException, InvalidDataException;

    /**
     * Get specified context details
     *
     * @param ctx A new Context object, this should not have been used before or a one returned from a previous call to this API. The context IDs of these objects should be set.
     * @param auth
     * @return
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     */
    public Context[] getData(final Context[] ctx, final Credentials auth) throws RemoteException,InvalidCredentialsException,NoSuchContextException,StorageException, InvalidDataException;

    /**
     * Get specified context details
     *
     * @param ctx A new Context object, this should not have been used before or a one returned from a previous call to this API. The context ID of this object should be set.
     * @param auth Credentials for authenticating against server.
     * @return Data for the requested context.
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     */
    public Context getData(final Context ctx, final Credentials auth) throws RemoteException,InvalidCredentialsException,NoSuchContextException,StorageException, InvalidDataException;

    /**
     * Change specified context!
     *
     * This method currently modifies following data:
     *
     * Login mappings - You can then login via usernam@loginmapping instead of username@contextID
     *
     * Context name in configdb - This is for better organization of contexts in your whole system.
     *
     * Change filestore quota size - Change how much quota the context is allowed to use!
     *
     * Change storage data informations - Change filestore infos for context. Normally NO need to change!
     *
     * @param ctx A new Context object, this should not have been used before or a one returned from a previous call to this API. Beside the context ID
     * or name for identifying the context itself the object should only contain those field which need to be changed.
     * @param auth
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     */
    public void change(final Context ctx, final Credentials auth) throws RemoteException,InvalidCredentialsException,NoSuchContextException,StorageException, InvalidDataException;

    /**
     * Gets specified context's capabilities.
     *
     * @param ctx A new Context object, this should not have been used before or a one returned from a previous call to this API.
     * @param auth The credentials
     * @return The capabilities
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     */
    public Set<String> getCapabilities(Context ctx, Credentials auth) throws RemoteException,InvalidCredentialsException,NoSuchContextException,StorageException, InvalidDataException;

    /**
     * Changes specified context's capabilities.
     *
     * @param ctx A new Context object, this should not have been used before or a one returned from a previous call to this API.
     * @param capsToAdd The capabilities to add
     * @param capsToRemove The capabilities to remove
     * @param capsToDrop The capabilities to drop; e.g. clean from storage
     * @param auth The credentials
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     */
    public void changeCapabilities(Context ctx, Set<String> capsToAdd, Set<String> capsToRemove, Set<String> capsToDrop, Credentials auth) throws RemoteException,InvalidCredentialsException,NoSuchContextException,StorageException, InvalidDataException;

    /**
     * Gets the configured quotas in given context.
     *
     * @param ctx The context
     * @param auth The credentials
     * @return The configured quota
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     */
    public Quota[] listQuotas(Context ctx, Credentials auth) throws RemoteException,InvalidCredentialsException,NoSuchContextException,StorageException, InvalidDataException;

    /**
     * Changes specified context's quota for a certain module.
     *
     * @param ctx A new Context object, this should not have been used before or a one returned from a previous call to this API.
     * @param module The module to apply quota to
     * @param quotaValue The quota value to set
     * @param auth The credentials
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     */
    public void changeQuota(Context ctx, String module, long quotaValue, Credentials auth) throws RemoteException,InvalidCredentialsException,NoSuchContextException,StorageException, InvalidDataException;

    /**
     * Change module access rights for ALL users in the specified context.<br>
     * IF you want to change data of a context like quota etc.<br>
     * use Method change(final Context ctx, final Credentials auth)
     *
     * This method modifies ONLY the access rights of the context!
     *
     *
     * @param ctx A new Context object, this should not have been used before or a one returned from a previous call to this API.
     * @param access
     * @param auth
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     */
    public void changeModuleAccess(final Context ctx,final UserModuleAccess access, final Credentials auth) throws RemoteException,InvalidCredentialsException,NoSuchContextException,StorageException, InvalidDataException;


    /**
     * Change module access rights by "access combination name" for ALL users in the specified context.<br>
     * IF you want to change data of a context like quota etc.<br>
     * use Method change(Context ctx, Credentials auth)
     *
     * This method modifies ONLY the access rights of the context!
     *
     * @param ctx A new Context object, this should not have been used before or a one returned from a previous call to this API.
     * @param access
     * @param auth
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     */
    public void changeModuleAccess(final Context ctx,final String access_combination_name, final Credentials auth) throws RemoteException,InvalidCredentialsException,NoSuchContextException,StorageException, InvalidDataException;

    /**
     * Get current module access rights of the context based on the rights of the admin user!
     *
     * @param ctx A new Context object, this should not have been used before or a one returned from a previous call to this API.
     * @param auth
     * @return Current module access rights!
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     */
    public UserModuleAccess getModuleAccess(final Context ctx,final Credentials auth) throws RemoteException,InvalidCredentialsException,NoSuchContextException,StorageException, InvalidDataException;

    /**
     * Get current access combination name of the context based on the rights of the admin user!
     *
     *
     * @param ctx A new Context object, this should not have been used before or a one returned from a previous call to this API.
     * @param auth
     * @return Access combination name or null if current access rights cannot be mapped to an access combination name.
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     */
    public String getAccessCombinationName(final Context ctx,final Credentials auth) throws RemoteException,InvalidCredentialsException,NoSuchContextException,StorageException, InvalidDataException;


    /**
     * Search for context on specified db.
     *
     * @param db Database on which to search for contexts.
     * @param auth Credentials for authenticating against server.
     * @return Found contexts on the specified database.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     *
     * @throws StorageException When an error in the subsystems occurred.
     * @throws NoSuchDatabaseException
     */
    public Context[] listByDatabase(final Database db, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException,InvalidDataException, NoSuchDatabaseException;

    /**
     * Search for context which store data on specified filestore
     *
     * @param fs Filestore
     * @param auth Credentials for authenticating against server.
     * @return Contexts found on this filestore.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     *
     * @throws StorageException When an error in the subsystems occurred.
     * @throws NoSuchFilestoreException
     */
    public Context[] listByFilestore(final Filestore fs, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException,InvalidDataException, NoSuchFilestoreException;

    /**
     * Determines the user ID of the admin user for a given context
     *
     * @param ctx A new Context object, this should not have been used before or a one returned from a previous call to this API. This context will be used for determining the userId of the admin.
     * @param auth Credentials for authenticating against the server.
     * @return The userId of the admin user
     * @throws RemoteException General RMI Exception
     * @throws InvalidCredentialsException Thrown when the login fails
     * @throws StorageException Thrown when an error in a subsystem occurred.
     * @throws NoSuchContextException
     */
    public int getAdminId(Context ctx, Credentials auth) throws RemoteException, InvalidCredentialsException, StorageException, NoSuchContextException ;

    /**
     * Determines whether a context already exists.
     *
     * @param ctx A new Context object, this should not have been used before or a one returned from a previous call to this API.
     * @param auth Credentials for authenticating against the server.
     * @return Whether the given context exists or not
     */
    public boolean exists(Context ctx, Credentials auth) throws RemoteException, InvalidDataException, StorageException, InvalidCredentialsException;

    /**
     * Determines whether a context already exists.
     *
     * @param ctx A new Context object, this should not have been used before or a one returned from a previous call to this API.
     * @param auth Credentials for authenticating against the server.
     * @return Whether the given context exists or not
     * @deprecated
     */
    @Deprecated
    public boolean checkExists(Context ctx, Credentials auth) throws RemoteException, InvalidDataException, StorageException, InvalidCredentialsException;
}
