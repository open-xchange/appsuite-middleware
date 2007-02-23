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
package com.openexchange.admin.dataSource;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Vector;
import java.util.Hashtable;


/**
 * The interface class <code>I_OXUtil</code> defines the Open-Xchange
 * API for creating and manipulating the service system.
 *
 */
public interface I_OXUtil extends Remote {
    /**
     * RMI name to be used in RMI URL
     */
    public static final String RMI_NAME         = "OX_AdminDaemon_OXUtil";
    
    /**
     * Key representing the database display name, value is <code>String</code>
     */
    public final static String DB_DISPLAY_NAME = "OX_DB_DISPLAY_NAME";
    
    /**
     * Key representing the database username, value is <code>String</code>
     */
    public final static String DB_AUTHENTICATION_ID = "OX_DB_AUTHENTICATION_USER";
    
    /**
     * Key representing the database password, value is <code>String</code>
     */
    public final static String DB_AUTHENTICATION_PASSWORD = "OX_DB_AUTHENTICATION_PASSWORD";
    
    /**
     * Key representing the database driver name, value is <code>String</code>
     */
    public final static String DB_DRIVER = "OX_DB_DRIVER";
    
    /**
     * Hardlimit of Dbpool activated or deactivated, possible values 1 or 0,
     * value is <code>Integer</code>
     */
    public final static String DB_POOL_HARDLIMIT = "OX_DB_POOL_HARDLIMIT";
    
    /**
     * Key representing the database identifier number, value is <code>Integer</code>
     */
    public final static String DB_POOL_ID = "OX_DB_POOL_ID";
    
    /**
     * Key carrying information on whether pool is master or not,
     * value is <code>String (true/false)</code>
     */
    public final static String DB_POOL_IS_MASTER = "OX_DB_POOL_IS_MASTER";

    /**
     * Key representing the ID of the master, if we are a slave. 0 if not.
     * value is <code>Integer</code>
     */
    public final static String DB_POOL_MASTER_ID = "OX_DB_POOL_MASTER_ID";

    /**
     * Key representing the database pool connection init size , value is <code>Integer</code>
     */
    public final static String DB_POOL_INIT = "OX_DB_POOL_INIT";
    
    /**
     * Key representing the database pool max. connections size, value is <code>Integer</code>
     */
    public final static String DB_POOL_MAX = "OX_DB_POOL_MAX";
    
    /**
     * Key representing the database URL, value is <code>String</code>
     */
    public final static String DB_URL = "OX_DB_URL";
    
    /**
     * Key representing the database schema name, value is <code>String</code>
     */
    public final static String DB_SCHEMA = "OX_DB_SCHEMA";
    
    /**
     * Key representing the store URL, value is <code>String</code>
     */
    public final static String STORE_URL = "OX_STORE_URI";
    
    /**
     * Key representing the store size in MB, value is <code>Long</code>
     */
    public final static String STORE_SIZE = "OX_STORE_SIZE";
    
    /**
     * Key representing the maximum amount of contexts in a filestore,
     * value is <code>Integer</code>, 0: don't add any contexts, -1: unlimited number
     * of contexts
     */
    public final static String STORE_MAX_CONTEXT = "OX_STORE_MAX_CONTEXT";
    
    /**
     * Key representing the current amount of contexts in store, value is <code>Integer</code>
     */
    public final static String STORE_CUR_CONTEXT = "OX_STORE_CUR_CONTEXT";
    
    
    /**
     * The system weight factor of this database in percent, value is <code>Integer</code>
     * <br>
     * This value defines how contexts will be distributed over multiple databases/db pools.
     * Servers with higher values will get more contexts, then servers with lower values
     * with the exception if DB_MAX_UNITS is reached on all other servers. In this case,
     * the database will be filled up to it's own DB_MAX_UNITS.
     * <br>
     * Two servers with the same DB_CLUSTER_WEIGHT will be filled rotational.
     * <br>
     * When an empty server is added with the same weight as an already existing server
     * that contains already contexts, the new server will be prefered up to the moment
     * both servers have a similar number of contexts.
     * <p>
     * Value range: <code>0-100</code>, Default = 100
     */
    public static final String DB_CLUSTER_WEIGHT = "OX_DB_SYS_WEIGHT";
    
    /**
     * The maximum number of contexts to be created on the given database,
     * value is <code>Integer</code>. 0: don't add any contexts, -1: unlimited number
     * of contexts
     */
    public static final String DB_MAX_UNITS = "OX_DB_MAX_CONTEXTS";

    /**
     * The current existing number of contexts in the given database,
     * value is <code>Integer</code>.
     */
    public static final String DB_CUR_UNITS = "OX_DB_CUR_CONTEXTS";

    
    /**
     * Key representing the server id , value is <code>Integer</code>
     */
    public static final String SERVER_ID = "OX_OXSERVER_ID";
    
    /**
     * Key representing the server`s name , value is <code>String</code>
     */
    public static final String SERVER_NAME = "OX_OXSERVER_NAME";
    
    
    
    
    /**
     * String array containing fields required to create a database
     * @see #DB_AUTHENTICATION_ID
     * @see #DB_AUTHENTICATION_PASSWORD
     * @see #DB_DRIVER
     * @see #DB_URL
     * @see #DB_SCHEMA
     */
    public static final String REQUIRED_KEYS_CREATE_DATABASE[] = { DB_AUTHENTICATION_ID, DB_URL, DB_DRIVER, DB_SCHEMA };
    
    
    /**
     * String array containing fields required to delete a database
     * @see #REQUIRED_KEYS_CREATE_DATABASE
     */
    public static final String REQUIRED_KEYS_DELETE_DATABASE[] = REQUIRED_KEYS_CREATE_DATABASE;
    
    
    
    public static final String REQUIRED_KEYS_REGISTER_DATABASE[] = { DB_MAX_UNITS, DB_CLUSTER_WEIGHT, DB_AUTHENTICATION_ID, DB_AUTHENTICATION_PASSWORD, DB_DISPLAY_NAME, DB_URL, DB_DRIVER };
    
    
    /**
     * String array containing fields required to create a database
     * @see #DB_AUTHENTICATION_ID
     * @see #DB_AUTHENTICATION_PASSWORD
     * @see #DB_DISPLAY_NAME
     * @see #DB_URL
     * @see #DB_DRIVER
     * @see #DB_MAX_UNITS
     */
    public static final String REQUIRED_KEYS_ADD_DATABASE[] = { DB_AUTHENTICATION_ID, DB_AUTHENTICATION_PASSWORD, DB_DISPLAY_NAME, DB_URL, DB_DRIVER, DB_MAX_UNITS };
    
    /**
     * Add new maintenance reason text.
     *
     * @param reason_txt String containing the reason text
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.<br>
     *         2nd Object is of type <code>Integer</code> and returns the reason id.
     *         <p>
     * @throws RemoteException
     * @see #deleteMaintenanceReason(int reason_id)
     */
    public Vector addMaintenanceReason(String reason_txt) throws RemoteException;
    
    /**
     * Deletes maintenance reason text.
     *
     * @param reason_id numerical identifier containing the reason id
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.
     *         <p>
     * @throws RemoteException
     * @see #addMaintenanceReason(String reason_txt)
     */
    public Vector deleteMaintenanceReason(int reason_id) throws RemoteException;
    
    /**
     * Get the text of maintenance reason.
     *
     * @param reason_id numerical identifier containing the reason id
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.<br>
     *         2nd Object is of type <code>String</code> and contains the reason text.
     *         <p>
     * @throws RemoteException
     * @see #addMaintenanceReason(String reason_txt)
     */
    public Vector getMaintenanceReason(int reason_id) throws RemoteException;
    
    
    /**
     * Get all maintenance reason ids.
     *
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.
     *         2nd Object is of type <code>Vector</code> and contains the maintenance reason ids.
     *         <p>
     * @throws RemoteException
     */
    public Vector getAllMaintenanceReasons() throws RemoteException;
    
    /**
     * Register an OX Server in the system.
     *
     * @param serverName the server name
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.
     *         2nd Object is of type <code>Integer</code> and returns the server id.
     *         <p>
     * @throws RemoteException
     * @see #SERVER_NAME
     * @see #SERVER_ID
     * @see #unregisterServer(int server_id)
     */
    public Vector registerServer( String serverName ) throws RemoteException;
    
    /**
     * Delete an OX server from the system.
     *
     * @param server_id numerical server identifier
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.
     *         <p>
     * @throws RemoteException
     * @see #registerServer(String serverName)
     */
    public Vector unregisterServer(int server_id) throws RemoteException;
    
    /**
     * Add/Register a new database to configdb
     *
     * <p><blockquote><pre>
     *      Hashtable db = new Hashtable();
     *      db.put(I_OXUtil.DB_AUTHENTICATION_ID, "myuser");
     *      db.put(I_OXUtil.DB_AUTHENTICATION_PASSWORD, "topsecret");
     *      db.put(I_OXUtil.DB_DISPLAY_NAME, "my new db");
     *      db.put(I_OXUtil.DB_DRIVER, "com.mysql.jdbc.Driver");      
     *      db.put(I_OXUtil.DB_URL, "jdbc:mysql://127.0.0.1/openexchange");
     *      db.put(I_OXUtil.DB_POOL_MAX, 20);
     *      db.put(I_OXUtil.DB_POOL_INIT, 10);
     *      db.put(I_OXUtil.DB_CLUSTER_WEIGHT, 100);
     *      db.put(I_OXUtil.DB_MAX_UNITS, 5000);
     *      db.put(I_OXUtil.DB_POOL_HARDLIMIT, 1);
     *      Vector result = rmi_util.registerDatabase(db);
     * </pre></blockquote></p>
     *
     * @param databaseData Hash containing the infos of the new database
     * @param isMaster set to true if database is a master
     * @param master_id if isMaster is false, this must be the id of an existing server
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.<br>
     *         2nd Object is of type <code>Integer</code> and returns the database id.
     *         <p>
     * @throws RemoteException
     * @see #DB_AUTHENTICATION_ID
     * @see #DB_AUTHENTICATION_PASSWORD
     * @see #DB_DISPLAY_NAME
     * @see #DB_DRIVER
     * @see #DB_POOL_HARDLIMIT
     * @see #DB_URL
     * @see #DB_POOL_MAX
     * @see #DB_POOL_INIT
     * @see #DB_CLUSTER_WEIGHT
     * @see #DB_MAX_UNITS
     * @see #unregisterDatabase(int database_id)
     */
    public Vector registerDatabase( Hashtable databaseData, boolean isMaster, int master_id ) throws RemoteException;
    
    /**
     * Change parameters of a database registered in configdb
     *
     * <p><blockquote><pre>
     *      Hashtable db = new Hashtable();
     *      db.put(I_OXUtil.DB_AUTHENTICATION_ID, "myuser");
     *      db.put(I_OXUtil.DB_AUTHENTICATION_PASSWORD, "topsecret");
     *      db.put(I_OXUtil.DB_DISPLAY_NAME, "my new db");
     *      db.put(I_OXUtil.DB_CLUSTER_WEIGHT, 88);
     *      Vector result = rmi_util.changeDatabase(42,db);
     * </pre></blockquote></p>
     *
     * @param databaseData Hash containing the infos of the new database
     * @param database_id id of 
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.<br>
     *         2nd Object is of type <code>Integer</code> and returns the database id.
     *         <p>
     * @throws RemoteException
     * @see #DB_AUTHENTICATION_ID
     * @see #DB_AUTHENTICATION_PASSWORD
     * @see #DB_DISPLAY_NAME
     * @see #DB_DRIVER
     * @see #DB_POOL_HARDLIMIT
     * @see #DB_URL
     * @see #DB_POOL_MAX
     * @see #DB_POOL_INIT
     * @see #DB_CLUSTER_WEIGHT
     * @see #DB_MAX_UNITS
     * @see #registerDatabase(Hashtable, boolean, int)
     */
    public Vector changeDatabase(int database_id, Hashtable databaseData) throws RemoteException;

    /**
     * Creates a new database from scratch on the given host with the given informations
     *
     * <p><blockquote><pre>
     *      Hashtable db = new Hashtable();
     *      db.put(I_OXUtil.DB_AUTHENTICATION_ID, "myuser");
     *      db.put(I_OXUtil.DB_AUTHENTICATION_PASSWORD, "topsecret");
     *      db.put(I_OXUtil.DB_DRIVER, "com.mysql.jdbc.Driver");
     *      db.put(I_OXUtil.DB_URL, "jdbc:mysql://127.0.0.1/"); // DO NOT FORGET TRAILING SLASH!
     *      db.put(I_OXUtil.DB_SCHEMA,"openexchange");
     *      Vector result = rmi_util.createDatabase(db);
     * </pre></blockquote></p>
     *
     * @param databaseData Hash containing the infos of the database to delete
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.
     *         <p>
     * @throws RemoteException
     * @see #DB_AUTHENTICATION_ID
     * @see #DB_AUTHENTICATION_PASSWORD
     * @see #DB_DRIVER
     * @see #DB_URL
     * @see #DB_SCHEMA
     */
    public Vector createDatabase( Hashtable databaseData ) throws RemoteException;
    
    
    /**
     * Delete a complete database from the given sql host with the given informations
     *
     * <p><blockquote><pre>
     *      Hashtable db = new Hashtable();
     *      db.put(I_OXUtil.DB_AUTHENTICATION_ID, "myuser");
     *      db.put(I_OXUtil.DB_AUTHENTICATION_PASSWORD, "topsecret");
     *      db.put(I_OXUtil.DB_DRIVER, "com.mysql.jdbc.Driver");
     *      db.put(I_OXUtil.DB_URL, "jdbc:mysql://127.0.0.1/"); // DO NOT FORGET TRAILING SLASH!
     *      db.put(I_OXUtil.DB_SCHEMA,"openexchange");
     *      Vector result = rmi_util.deleteDatabase(db);
     * </pre></blockquote></p>
     *
     * @param databaseData Hash containing the infos of the new database
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.
     *         <p>
     * @throws RemoteException
     * @see #DB_AUTHENTICATION_ID
     * @see #DB_AUTHENTICATION_PASSWORD
     * @see #DB_DRIVER
     * @see #DB_URL
     * @see #DB_SCHEMA
     */
    public Vector deleteDatabase( Hashtable databaseData ) throws RemoteException;
    
    /**
     *  Remove/Unregister database from configdb
     *
     * @param database_id numerical database identifier
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.
     *         <p>
     * @throws RemoteException
     * @see #registerDatabase( Hashtable databaseData, boolean isMaster, int master_id )
     */
    public Vector unregisterDatabase(int database_id) throws RemoteException;
    
    /**
     * Search for database
     *
     * @param search_pattern String with search pattern e.g "*" "*my*"
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.
     *         2nd Object is of type <code>Hashtable</code> containing the database infos.
     *         Key of the Hashtable is the database id and value a Hashtable with the
     *         corresponding database infos.
     *         <p>
     * @throws RemoteException
     * @see #DB_DISPLAY_NAME
     * @see #DB_POOL_ID
     * @see #DB_AUTHENTICATION_ID
     * @see #DB_AUTHENTICATION_PASSWORD
     * @see #DB_URL
     * @see #DB_DRIVER
     * @see #DB_POOL_HARDLIMIT
     * @see #DB_POOL_MAX
     * @see #DB_POOL_INIT
     * @see #DB_CLUSTER_WEIGHT
     * @see #DB_MAX_UNITS
     * @see #DB_POOL_IS_MASTER
     * @see #DB_POOL_MASTER_ID
     * @see #DB_CUR_UNITS
     */
    public Vector searchForDatabase(String search_pattern) throws RemoteException;
    
    /**
     * Search for server
     *
     * @param search_pattern String with search pattern e.g "*" "*my*"
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.<br>
     *         2nd Object is of type <code>Hashtable</code> containing the server infos.
     *         Key of the Hashtable is the server id and value a Hashtable with the
     *         corresponding server infos.
     *         <p>
     * @throws RemoteException
     */
    public Vector searchForServer(String search_pattern) throws RemoteException;
    
    
    /**
     * Register new filestore to the system.
     *
     * @param  store_URL String with the store location. E.g. file://mnt/store,http:/host/path
     * @param  store_size long containing the storage size in MB.
     * @param  store_maxContexts int maximum contexts for this store location.
     *         -1: unlimted, 0: don't add new filestores
     *
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.
     *         2nd Object is of type <code>Integer</code> and returns the filestore id.
     *         <p>
     * @throws RemoteException
     * @see #STORE_URL
     * @see #unregisterFilestore(int store_id)
     */
    public Vector registerFilestore(String store_URL, long store_size, int store_maxContexts) throws RemoteException;
    
    /**
     * Change filestore.
     *
     * @param  store_id int representing store to change.
     * @param  filestoreData contains possible values whitch have to been changed!  
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.
     *         <p>
     * @throws RemoteException
     * @see #STORE_URL
     * @see #STORE_SIZE
     * @see #STORE_MAX_CONTEXT
     */
    public Vector changeFilestore( int store_id, Hashtable filestoreData ) throws RemoteException;
    
    
    /**
     * List filestores.
     *
     * @param search_pattern String with search pattern e.g "*" "*file://%*"
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.
     *         2nd Object is of type <code>Hashtable</code> containing the filestore ids as key
     *         and Hashtable as Value with the data of the store.
     *         <p>
     * @throws RemoteException
     * @see #STORE_URL
     * @see #STORE_SIZE
     * @see #STORE_MAX_CONTEXT
     * @see #STORE_CUR_CONTEXT
     */
    public Vector listFilestores(String search_pattern) throws RemoteException;
    
    
    /**
     * Unregister filestore.
     *
     * @param  store_id int representing store to change.
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.
     *         <p>
     * @throws RemoteException
     * @see #registerFilestore(String store_URL, long store_size, int store_maxContexts)
     */
    public Vector unregisterFilestore(int store_id) throws RemoteException;
    
    
}
