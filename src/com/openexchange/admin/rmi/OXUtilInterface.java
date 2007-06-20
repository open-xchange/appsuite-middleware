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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
    public int addMaintenanceReason(final MaintenanceReason reason, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

    /**
     * Deletes maintenance reason text.
     * 
     * @param reason_id
     *            numerical identifier containing the reason id
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
    public void deleteMaintenanceReason(final int reason_id[], final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

    /**
     * Get maintenance reasons.
     * 
     * @return MaintenanceReason
     * @param reason_ids
     *            Ids of the maintenance reasons you want to receive from
     *            server.
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
    public MaintenanceReason[] getMaintenanceReasons(final int reason_ids[], final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

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
    public MaintenanceReason[] getAllMaintenanceReasons(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException;

    /**
     * Get all maintenance reason ids.
     * 
     * @return Contains the reason ids to retrieve from server.
     * @param auth
     *            Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException
     *             When an error in the subsystems occured.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws RemoteException
     *             General RMI Exception
     */
    public int[] getAllMaintenanceReasonIds(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException;

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
    public int registerServer(final Server srv, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

    /**
     * Delete an OX server from the system.
     * 
     * @param auth
     *            Credentials for authenticating against server.
     * @param server_id
     *            numerical server identifier
     * @throws com.openexchange.admin.rmi.exceptions.StorageException
     *             When an error in the subsystems occured.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException
     *             If the data sent within the method contained invalid data.
     * @throws RemoteException
     *             General RMI Exception
     */
    public void unregisterServer(final int server_id, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

    /**
     * Register a new database to the system.
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
    public int registerDatabase(final Database db, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

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
     * @param database_id
     *            Database id.
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
    public void unregisterDatabase(final int database_id, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

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
    public Database[] searchForDatabase(final String search_pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

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
    public Server[] searchForServer(final String search_pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

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
    public int registerFilestore(final Filestore fstore, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

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
     * @param store_id
     *            ID of the Filestore to unregister.
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
    public void unregisterFilestore(final int store_id, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

}
