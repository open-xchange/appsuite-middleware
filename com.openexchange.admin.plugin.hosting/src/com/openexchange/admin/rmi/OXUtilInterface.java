
package com.openexchange.admin.rmi;

import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.dataobjects.Server;
import java.rmi.Remote;
import java.rmi.RemoteException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 * This class defines the Open-Xchange API Version 2 for creating and
 * manipulating the service system.
 * 
 * @author cutmasta
 */
public interface OXUtilInterface extends Remote {

    /**
     * RMI name to be used in RMI URL
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
     *             When an error in the subsystems occured.
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
     *             When an error in the subsystems occured.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException
     *             If the data sent within the method contained invalid data.
     * @throws RemoteException
     *             General RMI Exception
     */
    public void deleteMaintenanceReason(final MaintenanceReason[] reasons, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

    /**
     * Get all maintenance reasons.
     * 
     * @return MaintenanceReason[] containing MaintenanceReason objects.
     * @param auth
     *            Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException
     *             When an error in the subsystems occured.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws RemoteException
     *             General RMI Exception
     */
    public MaintenanceReason[] listMaintenanceReasons(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException;

    /**
     * Register an OX Server in the system.
     * 
     * @return Contains the new generated server id.
     * @param srv
     *            Server object containing the server name
     * @param auth
     *            Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException
     *             When an error in the subsystems occured.
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
     *             When an error in the subsystems occured.
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
     * HINT: Use unregisterDatabase to remove/unregitser.
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
     * client_db.setUrl(&quot;jdbc:mysql://localhost/?useUnicode=true&amp;characterEncoding=UTF-8&amp;&quot; + &quot;autoReconnect=true&amp;useUnicode=true&amp;useServerPrepStmts=false&amp;useTimezone=true&amp;&quot; + &quot;serverTimezone=UTC&amp;connectTimeout=15000&amp;socketTimeout=15000&quot;);
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
     *             When an error in the subsystems occured.
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
     *            Database containing the infos of the database to edit.
     * @param auth
     *            Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException
     *             When an error in the subsystems occured.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException
     *             If the data sent within the method contained invalid data.
     * @throws RemoteException
     *             General RMI Exception
     */
    public void changeDatabase(final Database db, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

    /**
     * Creates a new database from scratch on the given host with the given
     * informations
     * 
     * <p>
     * <blockquote>
     * 
     * <pre>
     *     Database client_db = new Database();     
     *     client_db.setDriver(&quot;com.mysql.jdbc.Driver&quot;);
     *     client_db.setLogin(&quot;openexchange&quot;);
     *     client_db.setPassword(&quot;xxx&quot;);     
     *     client_db.setScheme(&quot;openexchange&quot;)
     *     client_db.setUrl(&quot;jdbc:mysql://localhost/?useUnicode=true&amp;characterEncoding=UTF-8&amp;&quot; +
     *                &quot;autoReconnect=true&amp;useUnicode=true&amp;useServerPrepStmts=false&amp;useTimezone=true&amp;&quot; +
     *                &quot;serverTimezone=UTC&amp;connectTimeout=15000&amp;socketTimeout=15000&quot;);
     * </pre>
     * 
     * </blockquote>
     * </p>
     * 
     * @param db
     *            The Database object which contains all needed infos to create
     *            a new db.
     * @param auth
     *            Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException
     *             When an error in the subsystems occured.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException
     *             If the data sent within the method contained invalid data.
     * @throws RemoteException
     *             General RMI Exception
     */
    public void createDatabase(final Database db, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

    /**
     * Delete a complete database from the given sql host with the given
     * informations
     * 
     * <p>
     * <blockquote>
     * 
     * <pre>
     *     Database client_db = new Database();     
     *     client_db.setDriver(&quot;com.mysql.jdbc.Driver&quot;);
     *     client_db.setLogin(&quot;openexchange&quot;);
     *     client_db.setPassword(&quot;xxx&quot;);     
     *     client_db.setScheme(&quot;openexchange&quot;)
     *     client_db.setUrl(&quot;jdbc:mysql://localhost/?useUnicode=true&amp;characterEncoding=UTF-8&amp;&quot; +
     *                &quot;autoReconnect=true&amp;useUnicode=true&amp;useServerPrepStmts=false&amp;useTimezone=true&amp;&quot; +
     *                &quot;serverTimezone=UTC&amp;connectTimeout=15000&amp;socketTimeout=15000&quot;);
     * </pre>
     * 
     * </blockquote>
     * </p>
     * 
     * @param db
     *            Database containing the infos(scheme,host,login etc.) of the
     *            database to delete completely from this host.
     * @param auth
     *            Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException
     *             When an error in the subsystems occured.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException
     *             If the data sent within the method contained invalid data.
     * @throws RemoteException
     *             General RMI Exception
     */
    public void deleteDatabase(final Database db, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

    /**
     * Unregister database identified by its ID from configdb.
     * 
     * @param database
     *            Database with id set.
     * @param auth
     *            Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException
     *             When an error in the subsystems occured.
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
     *             When an error in the subsystems occured.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException
     *             If the data sent within the method contained invalid data.
     * @throws RemoteException
     *             General RMI Exception
     */
    public Database[] listDatabases(final String search_pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

    /**
     * Search for server
     * 
     * @return Containing Server Object found by the search.
     * @param search_pattern
     *            Search pattern e.g "*" "*my*".
     * @param auth
     *            Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException
     *             When an error in the subsystems occured.
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
     * Register new filestore to the system.
     * 
     * @param fstore
     *            Filestore to register with the store data.
     * @param auth
     *            Credentials for authenticating against server.
     * @return Contains the new filestore id.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException
     *             When an error in the subsystems occured.
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
     *             When an error in the subsystems occured.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException
     *             If the data sent within the method contained invalid data.
     * @throws RemoteException
     *             General RMI Exception
     */
    public void changeFilestore(final Filestore fstore, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

    /**
     * List filestores.
     * 
     * @return Containing result objects.
     * @param search_pattern
     *            Search pattern e.g "*" "*file://%*"
     * @param auth
     *            Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException
     *             When an error in the subsystems occured.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException
     *             If the data sent within the method contained invalid data.
     * @throws RemoteException
     *             General RMI Exception
     */
    public Filestore[] listFilestores(final String search_pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

    /**
     * Unregister filestore from system identified by its ID
     * 
     * @param store
     *            Filestore to unregister with id set.
     * @param auth
     *            Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException
     *             When an error in the subsystems occured.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException
     *             If the data sent within the method contained invalid data.
     * @throws RemoteException
     *             General RMI Exception
     */
    public void unregisterFilestore(final Filestore store, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

}
