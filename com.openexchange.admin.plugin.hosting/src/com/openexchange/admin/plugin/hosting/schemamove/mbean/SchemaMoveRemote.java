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

package com.openexchange.admin.plugin.hosting.schemamove.mbean;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;
import com.openexchange.admin.plugin.hosting.exceptions.TargetDatabaseException;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.MissingServiceException;
import com.openexchange.admin.rmi.exceptions.NoSuchObjectException;
import com.openexchange.admin.rmi.exceptions.StorageException;


public interface SchemaMoveRemote extends Remote {

    public static final String RMI_NAME = SchemaMoveRemote.class.getName();

    /**
     * Disables the denoted schema.
     * <ul>
     * <li>Checks required preconditions</li>
     * <li>Determines affected contexts</li>
     * <li>Disables active contexts and decorates them with a certain reason identifier</li>
     * <li>Distribute changes contexts in cluster</li>
     * <li>Terminate active sessions in cluster</li>
     * </ul>
     *
     * @param auth The credentials
     * @param schemaName The schema name
     * @throws TargetDatabaseException
     * @throws NoSuchObjectException
     * @throws StorageException
     * @throws MissingServiceException
     * @throws InvalidDataException
     */
    void disableSchema(final Credentials auth, String schemaName) throws TargetDatabaseException, StorageException, NoSuchObjectException, MissingServiceException, RemoteException, InvalidCredentialsException, InvalidDataException;

    /**
     * Returns the database access information that are necessary to establish a connection to given schema's database.
     * <p>
     * The returned map contains may contain:
     * <ul>
     * <li><code>"url"</code></li>
     * <li><code>"driver"</code></li>
     * <li><code>"login"</code></li>
     * <li><code>"name"</code></li>
     * <li><code>"password"</code></li>
     * </ul>
     * </p>
     *
     * @param auth The credentials
     * @param schemaName The schema name
     * @return The database access information
     * @throws StorageException If database cannot be loaded
     * @throws NoSuchObjectException If the specified schema does not exist
     * @throws InvalidDataException
     */
    Map<String, String> getDbAccessInfoForSchema(final Credentials auth, String schemaName) throws StorageException, NoSuchObjectException, RemoteException, InvalidCredentialsException, InvalidDataException;

    /**
     * Returns the database access information that is necessary to establish connection to a given schema on the specified cluster
     * <p>
     * The returned map contains may contain:
     * <ul>
     * <li><code>"url"</code></li>
     * <li><code>"driver"</code></li>
     * <li><code>"login"</code></li>
     * <li><code>"name"</code></li>
     * <li><code>"password"</code></li>
     * </ul>
     * </p>
     *
     * @param auth The credentials
     * @param clusterId The cluster identifier
     * @return The database access information
     * @throws StorageException
     * @throws NoSuchObjectException
     * @throws InvalidDataException
     */
    Map<String, String> getDbAccessInfoForCluster(final Credentials auth, int clusterId) throws StorageException, NoSuchObjectException, RemoteException, InvalidCredentialsException, InvalidDataException;

    /**
     * Disables the denoted schema, resp. all contexts in that schema.
     * <ul>
     * <li>Enables all contexts that have been disabled via {@link #disableSchema(String)}</li>
     * <li>Distribute changes contexts in cluster</li>
     * </ul>
     *
     * @param auth The credentials
     * @param schemaName The schema name
     * @throws StorageException
     * @throws NoSuchObjectException
     * @throws MissingServiceException
     * @throws InvalidDataException
     */
    void enableSchema(final Credentials auth, String schemaName) throws StorageException, NoSuchObjectException, MissingServiceException, RemoteException, InvalidCredentialsException, InvalidDataException;

    /**
     * Restore the database pool references after a replay
     *
     * @param auth The credentials
     * @param sourceSchema The source schema
     * @param targetSchema The target schema
     * @param targetClusterId The target cluster identifier
     * @throws StorageException
     * @throws InvalidDataException
     */
    void restorePoolReferences(final Credentials auth, String sourceSchema, String targetSchema, int targetClusterId) throws StorageException, RemoteException, InvalidCredentialsException, InvalidDataException;

    /**
     * Create a new database schema
     *
     * @param auth The credentials
     * @param targetClusterId The target cluster identifier
     * @return The name of the new database schema
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws InvalidDataException
     */
    String createSchema(final Credentials auth, int targetClusterId) throws StorageException, RemoteException, InvalidCredentialsException, InvalidDataException;

    /**
     *
     * @param auth
     * @param schemaName
     * @param invalidateSession
     * @return
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws InvalidDataException
     * @throws MissingServiceException
     */
    void invalidateContexts(Credentials auth, String schemaName, boolean invalidateSession) throws StorageException, InvalidCredentialsException, InvalidDataException, MissingServiceException, RemoteException;

}
