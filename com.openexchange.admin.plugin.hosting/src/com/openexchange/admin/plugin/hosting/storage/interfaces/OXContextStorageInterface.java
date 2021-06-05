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

package com.openexchange.admin.plugin.hosting.storage.interfaces;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;
import com.openexchange.admin.daemons.ClientAdminThreadExtended;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.dataobjects.Quota;
import com.openexchange.admin.rmi.dataobjects.SchemaSelectStrategy;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.ContextExistsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.interfaces.OXUtilStorageInterface;
import com.openexchange.admin.tools.AdminCacheExtended;
import com.openexchange.admin.tools.PropertyHandlerExtended;
import com.openexchange.log.LogProperties;
import com.openexchange.tools.pipesnfilters.Filter;

/**
 * This interface provides an abstraction to the storage of the context information
 *
 * @author d7
 */
public abstract class OXContextStorageInterface {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OXContextStorageInterface.class);

    protected static final AdminCacheExtended cache = ClientAdminThreadExtended.cache;

    private static volatile OXContextStorageInterface instance;

    /**
     * Creates a new instance implementing the context storage interface.
     *
     * @return an instance implementing the group storage interface.
     * @throws StorageException Storage exception
     */
    public static OXContextStorageInterface getInstance() throws StorageException {
        OXContextStorageInterface inst = instance;
        if (null == inst) {
            synchronized (OXContextStorageInterface.class) {
                inst = instance;
                if (null == inst) {
                    Class<? extends OXContextStorageInterface> implementingClass;
                    PropertyHandlerExtended prop = cache.getProperties();
                    final String className = prop.getProp(PropertyHandlerExtended.CONTEXT_STORAGE, null);
                    if (null != className) {
                        try {
                            implementingClass = Class.forName(className).asSubclass(OXContextStorageInterface.class);
                        } catch (ClassNotFoundException e) {
                            log.error("", e);
                            throw new StorageException(e);
                        }
                    } else {
                        final StorageException storageException = new StorageException("Property for context_storage not defined");
                        log.error("", storageException);
                        throw storageException;
                    }

                    Constructor<? extends OXContextStorageInterface> cons;
                    try {
                        cons = implementingClass.getConstructor(new Class[] {});
                        inst = cons.newInstance(new Object[] {});
                        instance = inst;
                    } catch (SecurityException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    } catch (NoSuchMethodException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    } catch (IllegalArgumentException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    } catch (InstantiationException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    } catch (IllegalAccessException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    } catch (InvocationTargetException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    }
                }
            }
        }
        return inst;
    }

    /**
     * Move data of context to target database
     *
     * @param ctx
     * @param target_database_id
     * @param reason
     * @throws StorageException
     */
    public abstract void moveDatabaseContext(final Context ctx, final Database target_database_id, final MaintenanceReason reason) throws StorageException;

    /**
     * @param ctx
     * @param dst_filestore_id
     * @param reason
     * @return
     * @throws StorageException
     */
    public abstract String moveContextFilestore(final Context ctx, final Filestore dst_filestore_id, final MaintenanceReason reason) throws StorageException;

    /**
     * @param ctx Context with Filestore data set!
     * @throws StorageException
     * @deprecated Use {@link OXUtilStorageInterface#changeFilestoreDataFor(Context)}
     */
    @Deprecated
    public abstract void changeStorageData(final Context ctx) throws StorageException;

    /**
     * Gets the login mappings for specified context.
     *
     * @param ctx The context for which to load the mappings
     * @return The available login mappings
     * @throws StorageException If login mappings cannot be returned
     */
    public abstract Set<String> getLoginMappings(Context ctx) throws StorageException;

    /**
     * @param ctx
     * @return a context object
     * @throws StorageException
     */
    public abstract Context getData(final Context ctx) throws StorageException;

    /**
     * @param ctx
     * @return a context object
     * @throws StorageException
     */
    public abstract Context[] getData(final Context[] ctx) throws StorageException;

    /**
     * @param ctx
     * @throws StorageException
     */
    public abstract void change(final Context ctx) throws StorageException;

    /**
     * Gets the configured quotas in given context.
     *
     * @param ctx The context
     * @return The configured quota
     * @throws StorageException If quotas cannot be returned
     */
    public abstract Quota[] listQuotas(Context ctx) throws StorageException;

    /**
     * Changes specified context's quota.
     *
     * @param ctx The context
     * @param modules The modules
     * @param quota The quota to set
     * @param auth The credentials
     * @throws StorageException
     */
    public abstract void changeQuota(Context ctx, List<String> modules, long quota, Credentials auth) throws StorageException;

    /**
     * Gets the current capabilities for denoted context.
     *
     * @param ctx The context
     * @return The current capabilities
     * @throws StorageException If retrieving capabilities fails
     */
    public abstract Set<String> getCapabilities(Context ctx) throws StorageException;

    /**
     * Changes specified context's capabilities.
     *
     * @param ctx The context
     * @param capsToAdd The capabilities to add
     * @param capsToRemove The capabilities to remove
     * @param capsToDrop The capabilities to drop; e.g. clean from storage
     * @param auth The credentials
     * @throws StorageException
     */
    public abstract void changeCapabilities(Context ctx, Set<String> capsToAdd, Set<String> capsToRemove, Set<String> capsToDrop, Credentials auth) throws StorageException;

    /**
     * @param ctx
     * @param admin_user
     * @param access
     * @param schemaSelectStrategy
     * @throws StorageException If a general storage error occurs
     * @throws InvalidDataException If there is already a context with the same name
     * @throws ContextExistsException If there is already a context with the same context identifier
     */
    public abstract Context create(final Context ctx, final User admin_user, final UserModuleAccess access, SchemaSelectStrategy schemaSelectStrategy) throws StorageException, InvalidDataException, ContextExistsException;

    /**
     * @param ctx
     * @throws StorageException
     */
    public abstract void delete(final Context ctx) throws StorageException;

    /**
     * @param search_pattern
     * @param filters
     * @param loaders
     * @param offset The start offset
     * @param length The max. number of contexts to return
     * @return
     * @throws StorageException
     */
    public abstract Context[] listContext(final String search_pattern, List<Filter<Integer, Integer>> filters, List<Filter<Context, Context>> loaders, int offset, int length) throws StorageException;

    /**
     * @param ctx
     * @param reason
     * @throws StorageException
     */
    public abstract void disable(final Context ctx, final MaintenanceReason reason) throws StorageException;

    /**
     * Disables all contexts in a schema and sets the given maintenance reason. Contexts that are already disabled will be ignored and keep
     * their original reason.
     *
     * @param schema
     * @param reason
     * @throws StorageException
     */
    public abstract void disable(final String schema, final MaintenanceReason reason) throws StorageException;

    /**
     * @param ctx
     * @throws StorageException
     */
    public abstract void enable(final Context ctx) throws StorageException;

    /**
     * Enables all contexts in a schema which are currently disabled with to the given maintenance reason. Contexts that are disabled with
     * any other maintenance reason will not be enabled. If no reason is given, all disabled contexts are enabled. For every context that is
     * enabled, the reason ID is set to NULL.
     *
     * @param schema
     * @param reason The reason or <code>null</code>
     * @throws StorageException
     */
    public abstract void enable(final String schema, final MaintenanceReason reason) throws StorageException;

    /**
     * @param reason
     * @throws StorageException
     */
    public abstract void disableAll(final MaintenanceReason reason) throws StorageException;

    /**
     * @throws StorageException
     */
    public abstract void enableAll() throws StorageException;

    /**
     * @param db_host The database host to filter by
     * @param offset The start offset in returned chunk
     * @param length The max. number of contexts to return (starting from offset)
     * @return
     * @throws StorageException
     */
    public abstract Context[] searchContextByDatabase(Database db_host, int offset, int length) throws StorageException;

    /**
     * @param filestore The file storage to filter by
     * @param offset The start offset in returned chunk
     * @param length The max. number of contexts to return (starting from offset)
     * @return
     * @throws StorageException
     */
    public abstract Context[] searchContextByFilestore(Filestore filestore, int offset, int length) throws StorageException;

    /**
     * Gets all contexts that belong to the given schema.
     * <p>
     * <div style="background-color:#FFDDDD; padding:6px; margin:0px;">{@link LogProperties.Name#DATABASE_POOL_ID} is set by this method.</div>
     *
     * @param schema The schema name
     * @return The identifiers of all contexts that belong to the schema
     * @throws StorageException If context identifiers cannot be returned
     */
    public abstract List<Integer> getContextIdsBySchema(final String schema) throws StorageException;

    /**
     * This method deletes all inaccessible data in a context.
     *
     * @param ctx Context.
     * @throws StorageException if some problem occurs.
     */
    public abstract void downgrade(final Context ctx) throws StorageException;

    /**
     * @param reason
     * @param additionaltable
     * @param sqlconjunction
     * @throws StorageException
     */
    public abstract void disableAll(final MaintenanceReason reason, final String additionaltable, String sqlconjunction) throws StorageException;

    /**
     * @param additionaltable
     * @param sqlconjunction
     * @throws StorageException
     */
    public abstract void enableAll(final String additionaltable, final String sqlconjunction) throws StorageException;

    /**
     * Updates the context references after a replay
     *
     * @param sourceSchema The source schema
     * @param targetSchema The target schema
     * @param targetClusterId The target cluster identifier
     * @throws StorageException
     */
    public abstract void updateContextReferences(final String sourceSchema, final String targetSchema, final int targetClusterId) throws StorageException;

    /**
     * Create a new database schema
     *
     * @param targetClusterId The identifier of the target cluster
     * @return The name of the new database schema
     * @throws StorageException
     */
    public abstract String createSchema(int targetClusterId) throws StorageException;
}
