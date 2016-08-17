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

package com.openexchange.admin.schemamove.mbean;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;
import com.openexchange.admin.exceptions.TargetDatabaseException;
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
