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
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.dataobjects.Server;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchDatabaseException;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 * This class defines the Open-Xchange API Version 2 for creating and
 * manipulating needed components of the system.<br><br>
 *
 * <b>Example for initializing the system:</b>
 * <pre>
 * <b>// Register database,server and a filestore.</b>
 * final OXUtilInterface iface = (OXUtilInterface)Naming.lookup("rmi:///oxhost/"+OXUtilInterface.RMI_NAME);
 *
 * final Credentials auth = new Credentials();
 * auth.setLogin("masteradmin");
 * auth.setPassword("secret");
 *
 * Database client_db = new Database();
 * client_db.setName(name);
 * client_db.setDriver("com.mysql.jdbc.Driver");
 * client_db.setLogin("openexchange");
 * client_db.setMaster(true);
 * client_db.setMaxUnits(1000);
 * client_db.setPassword("secret");
 * client_db.setPoolHardLimit(20);
 * client_db.setPoolInitial(5);
 * client_db.setPoolMax(100);
 * client_db.setUrl("jdbc:mysql://localhost/?useUnicode=true&characterEncoding=UTF-8&
 * autoReconnect=false&useUnicode=true&useServerPrepStmts=false&useTimezone=true&
 * serverTimezone=UTC&connectTimeout=15000&socketTimeout=15000");
 * client_db.setClusterWeight(100);
 *
 * <b>// Register database</b>
 * iface.registerDatabase(client_db,auth);
 *
 * <b>// Register server</b>
 * Server srv = new Server();
 * srv.setName("local");
 * iface.registerServer(srv,auth);
 *
 * <b>// Register filestore</b>
 * Filestore client_st = new Filestore();
 * client_st.setUrl("file:/var/ox/filestore");
 * client_st.setSize(100L);
 * client_st.setMaxContexts(100);
 * iface.registerFilestore(srv,auth);
 * </pre>
 * The system is now ready to be filled with 100 contexts.<br>
 * See {@link OXContextInterface} for an example of creating a context.
 *
 *
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a>
 * @author <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 */
public interface OXUtilInterface extends Remote {

    public static final int DEFAULT_DB_WEIGHT = 100;
    public static final String DEFAULT_DRIVER = "com.mysql.jdbc.Driver";
    public static final int DEFAULT_MAXUNITS = 1000;
    public static final boolean DEFAULT_POOL_HARD_LIMIT = true;
    public static final int DEFAULT_POOL_INITIAL = 0;
    public static final int DEFAULT_POOL_MAX = 100;
    public static final String DEFAULT_USER = "openexchange";
    public static final String DEFAULT_HOSTNAME = "localhost";
    public static final long DEFAULT_STORE_SIZE = 1000;
    public final static int DEFAULT_STORE_MAX_CTX = 5000;


    /**
     * RMI name to be used in the naming lookup.
     */
    public static final String RMI_NAME = "OXUtil_V2";

    /**
     * Add new maintenance reason.
     *
     * @param reason
     *            MaintenanceReason.
     * @param auth
     *            Credentials for authenticating against server.
     * @return int containing the new id of the added maintenance reason.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException
     *             When an error in the subsystems occurred.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException
     *             If the data sent within the method contained invalid data.
     * @throws RemoteException
     *             General RMI Exception
     */
    public MaintenanceReason createMaintenanceReason(final MaintenanceReason reason, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

    /**
     * Deletes maintenance reason text.
     *
     * @param reasons
     *            Reasons which should be deleted!Currently ID must be set in each object!
     * @param auth
     *            Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException
     *             When an error in the subsystems occurred.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException
     *             If the data sent within the method contained invalid data.
     * @throws RemoteException
     *             General RMI Exception
     */
    public void deleteMaintenanceReason(final MaintenanceReason[] reasons, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

    /**
     * Get all maintenance reasons which match the specified search_pattern
     *
     * @return MaintenanceReason[] containing MaintenanceReason objects.
     * @param search_pattern
     *             A search pattern to list only those reason which match that pattern
     * @param auth
     *             Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException
     *             When an error in the subsystems occurred.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws RemoteException
     *             General RMI Exception
     * @throws InvalidDataException
     */
    public MaintenanceReason[] listMaintenanceReason(final String search_pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

    /**
     * Get all maintenance reasons. Same as calling listMaintenanceReasons with a search_pattern "*"
     *
     * @return MaintenanceReason[] containing MaintenanceReason objects.
     * @param auth
     *             Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException
     *             When an error in the subsystems occurred.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws RemoteException
     *             General RMI Exception
     * @throws InvalidDataException
     */
    public MaintenanceReason[] listAllMaintenanceReason(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

    /**
     * Register an OX Server in the system.
     *
     * @return Contains the new generated server id.
     * @param srv
     *            Server object containing the server name
     * @param auth
     *            Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException
     *             When an error in the subsystems occurred.
     *
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException
     *             If the data sent within the method contained invalid data.
     * @throws RemoteException
     *             General RMI Exception
     */
    public Server registerServer(final Server srv, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

    /**
     * Delete an OX server from the system.
     *
     * @param auth
     *            Credentials for authenticating against server.
     * @param serv
     *            Server with id set.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException
     *             When an error in the subsystems occurred.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException
     *             If the data sent within the method contained invalid data.
     * @throws RemoteException
     *             General RMI Exception
     */
    public void unregisterServer(final Server serv, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

    /**
     * Register a new database to the system.
     * HINT: Use unregisterDatabase to remove/unregister.
     *
     * DONT use deleteDatabase, cause this method performs a real "drop" for its specified Database!
     *
     * <p>
     * <blockquote>
     *
     * <pre>
     * Database client_db = new Database();
     * client_db.setDisplayname(&quot;mydb&quot;);
     * client_db.setDriver(&quot;com.mysql.jdbc.Driver&quot;);
     * client_db.setLogin(&quot;openexchange&quot;);
     * client_db.setMaster(true);
     * client_db.setMaxUnits(1000);
     * client_db.setPassword(&quot;xxx&quot;);
     * client_db.setPoolHardLimit(20);
     * client_db.setPoolInitial(2);
     * client_db.setPoolMax(100);
     * client_db.setUrl(&quot;jdbc:mysql://localhost/?useUnicode=true&amp;characterEncoding=UTF-8&amp;&quot; + &quot;autoReconnect=false&amp;useUnicode=true&amp;useServerPrepStmts=false&amp;useTimezone=true&amp;&quot; + &quot;serverTimezone=UTC&amp;connectTimeout=15000&amp;socketTimeout=15000&quot;);
     * </pre>
     *
     * </blockquote>
     * </p>
     *
     * @return Contains the new database id.
     * @param db
     *            The database to register
     * @param auth
     *            Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException
     *             When an error in the subsystems occurred.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException
     *             If the data sent within the method contained invalid data.
     * @throws RemoteException
     *             General RMI Exception
     */
    public Database registerDatabase(final Database db, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

    /**
     * Change parameters of a database registered in system
     *
     * <p>
     * <blockquote>
     *
     * <pre>
     *     Database client_db = ...load Database from server via
     * <CODE>
     * searchForDatabase
     * </CODE>
     *  to make sure that
     *     the Object contains the correct Database id.
     *
     *     client_db.setDisplayname(client_db.getDisplayname()+&quot;changed&quot;);
     *     client_db.setDriver(client_db.getDriver()+&quot;changed&quot;);
     *     client_db.setLogin(client_db.getLogin()+&quot;changed&quot;);
     *     client_db.setMaxUnits(2000);
     *     client_db.setPassword(client_db.getPassword()+&quot;changed&quot;);
     *     client_db.setPoolHardLimit(40);
     *     client_db.setPoolInitial(4);
     *     client_db.setPoolMax(200);
     *     client_db.setUrl(client_db.getUrl()+&quot;changed&quot;);
     *     ....change Database
     * </pre>
     *
     * </blockquote>
     * </p>
     *
     * @param db
     *            Database containing the information of the database to edit.
     * @param auth
     *            Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException
     *             When an error in the subsystems occurred.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException
     *             If the data sent within the method contained invalid data.
     * @throws RemoteException
     *             General RMI Exception
     */
    public void changeDatabase(final Database db, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

    /**
     * Unregister database identified by its ID from configdb.
     *
     * @param database
     *            Database with id set.
     * @param auth
     *            Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException
     *             When an error in the subsystems occurred.
     *
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException
     *             If the data sent within the method contained invalid data.
     * @throws RemoteException
     *             General RMI Exception
     */
    public void unregisterDatabase(final Database dbhandle, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

    /**
     * Search for databases registered in the system.
     *
     * @return Containing the databases found by the search.
     * @param search_pattern
     *            Search pattern e.g "*" "*my*".
     * @param auth
     *            Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException
     *             When an error in the subsystems occurred.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException
     *             If the data sent within the method contained invalid data.
     * @throws RemoteException
     *             General RMI Exception
     */
    public Database[] listDatabase(final String search_pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

    /**
     * Convenience method for listing all databases registered in the system.
     *
     * @return Containing the databases found by the search.
     * @param auth
     *            Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException
     *             When an error in the subsystems occurred.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException
     *             If the data sent within the method contained invalid data.
     * @throws RemoteException
     *             General RMI Exception
     */
    public Database[] listAllDatabase(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

    /**
     * Checks databases by schema consistency.
     *
     * @return Such databases either needing update, currently updating or outdated updating
     * @param auth
     *            Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException
     *             When an error in the subsystems occurred.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException
     *             If the data sent within the method contained invalid data.
     * @throws RemoteException
     *             General RMI Exception
     */
    public Database[][] checkDatabase(Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

    /**
     * Unblocks specified database schema (in case marked as being updated for too long).
     *
     * @return The list of unblocked database schemas
     * @param database The database schema to unblock
     * @param auth
     *            Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException
     *             When an error in the subsystems occurred.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException
     *             If the data sent within the method contained invalid data.
     * @throws RemoteException
     *             General RMI Exception
     * @throws NoSuchDatabaseException If no such database/schema exists
     */
    public Database[] unblockDatabase(Database database, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, NoSuchDatabaseException;

    /**
     * Search for server
     *
     * @return Containing Server Object found by the search.
     * @param search_pattern
     *            Search pattern e.g "*" "*my*".
     * @param auth
     *            Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException
     *             When an error in the subsystems occurred.
     *
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException
     *             If the data sent within the method contained invalid data.
     * @throws RemoteException
     *             General RMI Exception
     */
    public Server[] listServer(final String search_pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

    /**
     * Convenience method for listing all servers
     *
     * @return Containing Server Object found by the search.
     * @param auth
     *            Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException
     *             When an error in the subsystems occurred.
     *
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException
     *             If the data sent within the method contained invalid data.
     * @throws RemoteException
     *             General RMI Exception
     */
    public Server[] listAllServer(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

    /**
     * Register new filestore to the system.
     *
     * @param fstore
     *            Filestore to register with the store data.
     * @param auth
     *            Credentials for authenticating against server.
     * @return Contains the new filestore id.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException
     *             When an error in the subsystems occurred.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException
     *             If the data sent within the method contained invalid data.
     * @throws RemoteException
     *             General RMI Exception
     */
    public Filestore registerFilestore(final Filestore fstore, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

    /**
     * Change filestore.
     *
     * @param fstore
     *            Contains store to change.
     * @param auth
     *            Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException
     *             When an error in the subsystems occurred.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException
     *             If the data sent within the method contained invalid data.
     * @throws RemoteException
     *             General RMI Exception
     */
    public void changeFilestore(final Filestore fstore, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

    /**
     * A method to list file stores matching some search pattern. Internally calls {@link #listFileStore(String, Credentials, boolean)} with
     * parameter omitUsage set to <code>false</code>.
     *
     * @return Containing result objects.
     * @param search_pattern
     *            Search pattern e.g "*" "*file://%*"
     * @param auth
     *            Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException
     *             When an error in the subsystems occurred.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException
     *             If the data sent within the method contained invalid data.
     * @throws RemoteException
     *             General RMI Exception
     */
    public Filestore[] listFilestore(final String search_pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

    /**
     * A method to list file stores matching some search pattern.
     * @param searchPattern The search pattern the file store should match to. The pattern "*" will list all file stores.
     * @param credentials must be the master administration credentials to be allowed to list file stores.
     * @param omitUsage <code>true</code> to not load the current file store usage from the database, which is an expensive operation
     * because it has to load the usage of every context and summarize them up.
     * @return an array with all configured file stores.
     * @throws RemoteException if a general RMI problem occurs.
     * @throws StorageException if a problem on the storage layer occurs.
     * @throws InvalidCredentialsException if the supplied credentials do not match the master administration credentials.
     * @throws InvalidDataException if the pattern is empty or invalid.
     */
    Filestore[] listFilestore(String searchPattern, Credentials credentials, boolean omitUsage) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

    /**
     * Convenience method for listing all filestores.
     *
     * @return Containing result objects.
     * @param auth
     *            Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException
     *             When an error in the subsystems occurred.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException
     *             If the data sent within the method contained invalid data.
     * @throws RemoteException
     *             General RMI Exception
     */
    public Filestore[] listAllFilestore(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

    /**
     * Unregister filestore from system identified by its ID
     *
     * @param store
     *            Filestore to unregister with id set.
     * @param auth
     *            Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException
     *             When an error in the subsystems occurred.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException
     *             If the data sent within the method contained invalid data.
     * @throws RemoteException
     *             General RMI Exception
     */
    public void unregisterFilestore(final Filestore store, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;


    /**
     * Creates a new schema in the given database if possible. In case the optDBId is null the best suitable DB is selected automatically.
     *
     * @param credentials Credentials for authenticating against server.
     * @param optDBId Optional database identifier. In case the optDBId is null the best suitable database is selected automatically.
     * @return The {@link Database} for the new schema.
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     */
    public Database createSchema(final Credentials credentials, Integer optDBId) throws RemoteException, StorageException, InvalidCredentialsException;
}
