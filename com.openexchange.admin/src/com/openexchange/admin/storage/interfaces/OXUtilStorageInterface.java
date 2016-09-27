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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import com.openexchange.admin.daemons.ClientAdminThreadExtended;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.dataobjects.Server;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.tools.AdminCacheExtended;
import com.openexchange.admin.tools.PropertyHandler;
import com.openexchange.admin.tools.PropertyHandlerExtended;

/**
 * This interface provides an abstraction to the storage of the util information
 *
 * @author d7
 * @author cutmasta
 *
 */
public abstract class OXUtilStorageInterface {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OXUtilStorageInterface.class);

    private static volatile OXUtilStorageInterface instance;

    /**
     * Creates a new instance implementing the group storage interface.
     * @return an instance implementing the group storage interface.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException Storage exception
     */
    public static OXUtilStorageInterface getInstance() throws StorageException {
        OXUtilStorageInterface inst = instance;
        if (null == inst) {
            synchronized (OXUtilStorageInterface.class) {
                inst = instance;
                if (null == inst) {
                    Class<? extends OXUtilStorageInterface> implementingClass;
                    AdminCacheExtended cache = ClientAdminThreadExtended.cache;
                    PropertyHandler prop = cache.getProperties();
                    final String className = prop.getProp(PropertyHandlerExtended.UTIL_STORAGE, null);
                    if (null != className) {
                        try {
                            implementingClass = Class.forName(className).asSubclass(OXUtilStorageInterface.class);
                        } catch (final ClassNotFoundException e) {
                            log.error("", e);
                            throw new StorageException(e);
                        }
                    } else {
                        final StorageException storageException = new StorageException("Property for util_storage not defined");
                        log.error("", storageException);
                        throw storageException;
                    }

                    Constructor<? extends OXUtilStorageInterface> cons;
                    try {
                        cons = implementingClass.getConstructor(new Class[] {});
                        inst = cons.newInstance(new Object[] {});
                        instance = inst;
                    } catch (final SecurityException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    } catch (final NoSuchMethodException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    } catch (final IllegalArgumentException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    } catch (final InstantiationException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    } catch (final IllegalAccessException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    } catch (final InvocationTargetException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    }
                }
            }
        }
        return inst;
    }

    /**
     * Register filestore in configbdb.
     *
     * @param fstore
     *            filestore object
     * @return the id of the created filestore as a long.
     * @throws StorageException
     */
    public abstract int registerFilestore(final Filestore fstore) throws StorageException;

    /**
     * Changes a given filestore
     *
     * @param fstore
     *            filestore object
     * @throws StorageException
     */
    public abstract void changeFilestore(final Filestore fstore) throws StorageException;

    /**
     * @param ctx Context with Filestore data set!
     * @throws StorageException
     */
    public abstract void changeFilestoreDataFor(Context ctx) throws StorageException;

    /**
     * @param ctx Context with Filestore data set!
     * @param configDbCon The connection to use
     * @throws StorageException
     */
    public abstract void changeFilestoreDataFor(Context ctx, Connection configDbCon) throws StorageException;

    /**
     * @param user The associated user
     * @param ctx Context with Filestore data set!
     * @param con The connection to use
     * @throws StorageException
     */
    public abstract void changeFilestoreDataFor(User user, Context ctx) throws StorageException;

    /**
     * @param user The associated user
     * @param ctx Context with Filestore data set!
     * @param con The connection to use
     * @throws StorageException
     */
    public abstract void changeFilestoreDataFor(User user, Context ctx, Connection con) throws StorageException;

    /**
     * Prepares filestore usage for given user
     *
     * @param user The user
     * @param ctx The context
     * @throws StorageException If operation fails
     */
    public abstract void prepareFilestoreUsageFor(User user, Context ctx) throws StorageException;

    /**
     * Prepares filestore usage for given user
     *
     * @param user The user
     * @param ctx The context
     * @param con The connection to use
     * @throws StorageException If operation fails
     */
    public abstract void prepareFilestoreUsageFor(User user, Context ctx, Connection con) throws StorageException;

    /**
     * Cleans filestore usage for given user
     *
     * @param user The user
     * @param ctx The context
     * @throws StorageException If operation fails
     */
    public abstract void cleanseFilestoreUsageFor(User user, Context ctx) throws StorageException;

    /**
     * Cleans filestore usage for given user
     *
     * @param user The user
     * @param ctx The context
     * @param con The connection to use
     * @throws StorageException If operation fails
     */
    public abstract void cleanseFilestoreUsageFor(User user, Context ctx, Connection con) throws StorageException;

    /**
     * Gets the URIs of the file storages that are in use by specified context (either itself or by one if its users).
     *
     * @param contextId The context identifier
     * @return The file storages in use
     * @throws StorageException If file storages cannot be determined
     */
    public abstract List<URI> getUrisforFilestoresUsedBy(int contextId) throws StorageException;

    /**
     * List all registered file stores.
     * @param pattern a pattern to search for
     * @return an array of file store objects
     * @throws StorageException
     */
    public abstract Filestore[] listFilestores(String pattern, boolean omitUsage) throws StorageException;

    /**
     * Loads filestore information, but w/o any usage information. Only basic information.
     *
     * @param id The unique identifier of the filestore.
     * @return Basic filestore information
     * @throws StorageException if loading the filestore information fails.
     */
    public abstract Filestore getFilestoreBasic(int id) throws StorageException;

    /**
     * Gets the filestore associated with given identifier
     *
     * @param id The filestore identifier
     * @return The filestore instance
     * @throws StorageException If filestore instance cannot be returned
     */
    public abstract Filestore getFilestore(final int id) throws StorageException;

    /**
     * Load a filestore. Specify whether the file store usage should be calculated by summing up all filestore usages.
     *
     * @param filestoreId The filestore identifier
     * @param loadUsage Whether the usage must be determined. Note: This is very slow.
     * @return The filestore instance
     * @throws StorageException If filestore instance cannot be returned
     */
    public abstract Filestore getFilestore(int filestoreId, boolean loadUsage) throws StorageException;

    /**
     * Loads the base URI from specified filestore.
     *
     * @param filestoreId The filestore identifier
     * @return The filestore base URI
     * @throws StorageException If filestore base URI cannot be returned
     */
    public abstract java.net.URI getFilestoreURI(int filestoreId) throws StorageException;

    /**
     * Unregister filestore from configbdb
     *
     * @param store_id
     *            the id of the filestore
     * @throws StorageException
     */
    public abstract void unregisterFilestore(final int store_id) throws StorageException;

    /**
     * Iterates across all existing file storages and searches for one having enough space for a context.
     */
    public abstract Filestore findFilestoreForContext() throws StorageException;

    /**
     * Iterates across all existing file storages and searches for one having enough space for a user.
     *
     * @param fileStoreId The optional identifier of the file storage to prefer during auto-selection or <code>-1</code> to ignore
     */
    public abstract Filestore findFilestoreForUser(int fileStoreId) throws StorageException;

    /**
     * Gets the identifier of the file storage currently assigned to given context
     *
     * @param contextId The context identifier
     * @return The identifier of the file storage
     * @throws StorageException If the identifier of the file storage cannot be returned
     */
    public abstract int getFilestoreIdFromContext(int contextId) throws StorageException;

    /**
     * Checks if specified file storage offers enough space for a further context assignment.
     *
     * @param filestore The file storage to which a further context is supposed to be assigned
     * @return <code>true</code> if enough space is available; otherwise <code>false</code>
     * @throws StorageException If check for enough space fails
     */
    public abstract boolean hasSpaceForAnotherContext(Filestore filestore) throws StorageException;

    /**
     * Checks if specified file storage offers enough space for a further user assignment.
     *
     * @param filestore The file storage to which a further user is supposed to be assigned
     * @return <code>true</code> if enough space is available; otherwise <code>false</code>
     * @throws StorageException If check for enough space fails
     */
    public abstract boolean hasSpaceForAnotherUser(Filestore filestore) throws StorageException;

    /**
     * Create a new maintenance reason in configdb.They are needed to disable a
     * context.
     *
     * @param reason
     *            the MaintenanceReason
     * @return the id as a long of the new created reason
     * @throws StorageException
     */
    public abstract int createMaintenanceReason(final MaintenanceReason reason) throws StorageException;

    /**
     * Delete reason from configdb
     *
     * @param reason
     *            the MaintenanceReason
     * @throws StorageException
     */
    public abstract void deleteMaintenanceReason(final int[] reason_ids) throws StorageException;

    /**
     * @param reason_id
     *            the id of a MaintenanceReason
     * @return MaintenanceReason from configdb identified by the reason_id
     * @throws StorageException
     */
    public abstract MaintenanceReason[] getMaintenanceReasons(final int[] reason_id) throws StorageException;

    /**
     * @return an array of all available MaintenanceReasons in configdb.
     * @throws StorageException
     */
    public abstract MaintenanceReason[] getAllMaintenanceReasons() throws StorageException;

    /**
     * @return an array of all available MaintenanceReasons in configdb match the specified pattern
     * @throws StorageException
     */
    public abstract MaintenanceReason[] listMaintenanceReasons(final String search_pattern) throws StorageException;

    /**
     * Register a new Database in configdb
     *
     * @param db
     *            a database object to register
     * @return long with the id of the database
     * @throws StorageException
     */
    public abstract int registerDatabase(final Database db) throws StorageException;

    /**
     * Creates a new database from scratch on the given database host. Is used
     * ONLY internally at the moment.
     *
     * @param db
     *            a database object to create
     * @throws StorageException
     */
    public abstract void createDatabase(final Database db) throws StorageException;

    /**
     * Delete a complete database(scheme) from the given database host. Is used
     * ONYL internally at the moment.
     *
     * @param db
     *            a database object to be deleted
     * @throws StorageException
     */
    public abstract void deleteDatabase(final Database db) throws StorageException;

    // TODO: cutamasta: please fill javadoc comment
    /**
     * @param db
     *            a database object to be changed
     * @throws StorageException
     */
    public abstract void changeDatabase(final Database db) throws StorageException;

    /**
     * Registers a new server in the configdb
     *
     * @param serverName
     *            a server name to be registered
     * @return long with the id of the server
     * @throws StorageException
     */
    public abstract int registerServer(final String serverName) throws StorageException;

    /**
     * Unregister a database from configdb
     *
     * @param db_id
     *            a database id which is unregistered
     * @throws StorageException
     */
    public abstract void unregisterDatabase(final int db_id, final boolean isMaster) throws StorageException;

    /**
     * Unregister a server from configdb
     *
     * @param server_id
     *            a server id which is unregistered
     * @throws StorageException
     */
    public abstract void unregisterServer(final int server_id) throws StorageException;

    /**
     * Searches for databases matching search_pattern
     *
     * @param search_pattern
     *            a pattern to search for
     * @return a database array
     * @throws StorageException
     */
    public abstract Database[] searchForDatabase(final String search_pattern) throws StorageException;

    /**
     * Searchs for server matching given search_pattern
     *
     * @param search_pattern
     *            a pattern to search for
     * @return Server array with found servers
     * @throws StorageException
     */
    public abstract Server[] searchForServer(final String search_pattern) throws StorageException;

    /**
     * Get the write pool identifier for the specified cluster
     *
     * @param clusterId The cluster identifier
     * @return The write pool identifier
     * @throws StorageException
     */
    public abstract int getWritePoolIdForCluster(final int clusterId) throws StorageException;


    /**
     * Creates a new schema in the given database if possible. In case the optDBId is null the best suitable DB is selected automatically.
     *
     * @param optDBId Optional database identifier. If missing the best suitable database is selected automatically.
     * @return The schema name.
     * @throws StorageException
     */
    public abstract Database createScheme(Integer optDBId) throws StorageException;

    /**
     * Determine the next database to use depending on database weight factor. Each database should be equal full according to their weight.
     * Additionally check each master for availability.
     *
     * @param con
     * @return the database
     * @throws SQLException
     * @throws StorageException
     */
    public abstract Database getNextDBHandleByWeight(Connection con) throws SQLException, StorageException;
}
