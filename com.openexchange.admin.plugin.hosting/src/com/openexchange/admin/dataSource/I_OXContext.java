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
import java.util.Hashtable;
import java.util.Vector;





/**
 * The interface class <code>I_OXContext</code> defines the Open-Xchange
 * API for creating and manipulating OX Contexts.<br>
 * <br>
 * Creating a new context requires to register databases and servers to
 * the system using the <code>I_OXUtil</code> interface.
 * <br>
 * Example code:<br>
 * 
 * <pre>
 * try {
 *     String ox_host = "localhost";
 *     // This will create a rmi object whitch is connected with the server!
 *     I_OXContext rmi = (I_OXContext)Naming.lookup( "rmi://" + ox_host + "/" + I_OXContext.RMI_NAME );
 *     
 *     // Now, you are able to execute the methods on the server
 *     
 *     Vector retVals = new Vector();
 *     
 *     retVals = rmi.createContext( ext_context_id, max_quota, adminUser );
 *     retVals = rmi.deleteContext( ext_context_id );
 *     // and all the other methods
 *     
 * } catch ( Exception exp ) {
 *     exp.printStackTrace();
 * }
 * </pre>
 *
 * @see I_OXUser
 * @see I_OXUtil
 */
public interface I_OXContext extends Remote {
    
    
    
    /**
     * RMI name to be used in RMI URL
     */
    public static final String RMI_NAME = "OX_AdminDaemon_OXContext";
    
    
    
    /**
     * Key representing the context id  , value is <code>Integer</code>
     */
    public final static String CONTEXT_ID = "OX_CONTEXT_ID";
    
    
    
    /**
     * Key representing the context name  , value is <code>String</code>
     */
    public final static String CONTEXT_NAME = "OX_CONTEXT_NAME";
    
    
    
    /**
     * Key representing the context login name  , value is <code>String</code>
     */
    public final static String CONTEXT_LOGIN_NAME = "OX_CONTEXT_LOGIN_NAME";
    
    
    
    /**
     * Key representing the context`s write pool id  , value is <code>Integer</code>
     */
    public final static String CONTEXT_WRITE_POOL_ID = "OX_CONTEXT_WRITE_POOL_ID";
    
    
    
    /**
     * Key representing the context`s read pool id  , value is <code>Integer</code>
     */
    public final static String CONTEXT_READ_POOL_ID = "OX_CONTEXT_READ_POOL_ID";
    
    
    
    /**
     * Key representing the context`s database schema name if different from standard  , value is <code>String</code>
     */
    public final static String CONTEXT_DB_SCHEMA_NAME = "OX_CONTEXT_DB_SCHEMA";
    
    
    
    /**
     * Key representing if context is locked  , value is <code>Boolean</code>
     */
    public final static String CONTEXT_LOCKED = "OX_CONTEXT_LOCKED";
    
    
    
    /**
     * Key representing the maintenance reason id of the locked context  , value is <code>Integer</code>
     */
    public final static String CONTEXT_LOCKED_TXT_ID = "OX_CONTEXT_LOCKED_TXT_ID";
    
    
    
    /**
     * Key representing the context`s filestore id , value is <code>String</code>
     */
    public final static String CONTEXT_FILESTORE_ID = "OX_CONTEXT_FILESTORE_ID";
    
    
    
     /**
     * Key representing the context`s filestore name , value is <code>String</code>
     */
    public final static String CONTEXT_FILESTORE_NAME = "OX_CONTEXT_FILESTORE_NAME";
     
    
    /**
     * Key representing the context`s database handle , value is <code>Hashtable</code>
     */
    public final static String CONTEXT_DATABASE_HANDLE = "OX_CONTEXT_DATABASE_HANDLE";    
    
    
    /**
     * Key representing the context`s filestore username , value is <code>String</code>
     */
    public final static String CONTEXT_FILESTORE_USERNAME = "OX_CONTEXT_FILESTORE_USERNAME";
    
    
    
    /**
     * Key representing the context`s filestore password , value is <code>String</code>
     */
    public final static String CONTEXT_FILESTORE_PASSWORD = "OX_CONTEXT_FILESTORE_PASSWORD";
    
    
    
    /**
     * Key representing the context`s filestore max quota (MByte), value is <code>Long</code>
     */
    public final static String CONTEXT_FILESTORE_QUOTA_MAX = "OX_CONTEXT_QUOTA_MAX";
    
    
    
    /**
     * Key representing the context`s filestore used quota (MByte) , value is <code>Long</code>
     */
    public final static String CONTEXT_FILESTORE_QUOTA_USED = "OX_CONTEXT_QUOTA_USED";
    
    /**
     * Key representing the overall daemon wide average quota per context (MByte),
     * value is <code>Long</code>
     */
    public final static String CONTEXT_AVERAGE_QUOTA = "OX_CONTEXT_AVERAGE_QUOTA";
    
    
    /**
     * For creating a new context, you need an admin-user. The admin-user need this values.<br>
     * I_OXUser.UID<br>
     * I_OXUser.PASSWORD<br>
     * I_OXUser.PRIMARY_MAIL<br>
     * <br>
     * Values are <code>String</code>
     * 
     * @see I_OXUser
     * @see I_OXUser#UID
     * @see I_OXUser#PASSWORD
     * @see I_OXUser#PRIMARY_MAIL
     */
    public static final String REQUIRED_KEYS_CREATE[]   = { I_OXUser.UID, I_OXUser.PASSWORD, I_OXUser.PRIMARY_MAIL };
    
    
    
    /**
     * This is the pattern to change the method searchContext from search to list!
     */
    public static final String PATTERN_SEARCH_ALL_CONTEXT   = "*";
    
    
    
    /**
     * Create a new context.
     * 
     * @param context_id numerical context identifier
     * @param quota_max maximum quota value, this context can use in the filestore (in MB) 
     * @param admin_user data of administrative user for this context
     * @return Vector containing return code and/or result objects<br>
     * 
     * <p>
     *     1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *     when method succeeds and <code>"ERROR"</code> in case of a failure.<br>
     *     2nd Object is only avaible if 1st Object is "ERROR". It's of type <code>String</code>
     *     and contains the errorMessage.
     * </p>
     * 
     * Example code:<br>
     * <pre>
     * try {
     *     String ox_host = "localhost";
     *     I_OXContext rmi = (I_OXContext)Naming.lookup( "rmi://" + ox_host + "/" + I_OXContext.RMI_NAME );
     *     
     *     Hashtable adminUser = new Hashtable();
     *     adminUser.put( I_OXUser.UID, "admin" );
     *     adminUser.put( I_OXUser.PASSWORD, "secret" );
     *     adminUser.put( I_OXUser.FIRST_NAME, "firstname" );
     *     adminUser.put( I_OXUser.LAST_NAME, "lastname" );
     *     adminUser.put( I_OXUser.PRIMARY_MAIL, "admin@example.ox" );
     *     adminUser.put( I_OXUser.DISPLAYNAME, "The fine admin-user" );
     *     
     *     
     *     int ext_context_id = 1; // The Context_ID must be unique!
     *     long max_quota = 50000; // Value is MegaByte => 50000 MegaByte == 50 GigaByte
     *     
     *     Vector retVals = rmi.createContext( ext_context_id, max_quota, adminUser );
     *     
     *     String status = (String)retVals.get( 0 );
     *     
     *     if ( status.equalsIgnoreCase( "OK" ) ) {
     *         // no-error
     *     }
     *     
     *     if ( status.equalsIgnoreCase( "ERROR" ) ) {
     *         // error
     *         String errorMessage = (String)retVals.get( 1 );
     *     }
     * } catch ( Exception exp ) {
     *     exp.printStackTrace();
     * }       
     * </pre>
     * 
     * @see #REQUIRED_KEYS_CREATE
     * @see I_OXUser
     * 
     * @throws RemoteException
     */
    public Vector createContext( int context_id, long quota_max, Hashtable admin_user ) throws RemoteException;
    
    
    
    /**
     * Delete a context.<br>
     * Note: Deleting a context will delete all data whitch the context include (all users, groups, appointments, ... )
     * 
     * @param context_id numerical context identifier
     * @return Vector containing return code and/or result objects
     * 
     * <p>
     *     1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *     when method succeeds and <code>"ERROR"</code> in case of a failure.<br>
     *     2nd Object is only avaible if 1st Object is "ERROR". It's of type <code>String</code>
     *     and contains the errorMessage.
     * </p>
     * 
     * Example code:<br>
     * <pre>
     * Vector retVals = rmi.deleteContext( ext_context_id );
     * String status = (String)retVals.get( 0 );
     * if ( status.equalsIgnoreCase( "OK" ) ) {
     *   // no-error
     * }
     * 
     * if ( status.equalsIgnoreCase( "ERROR" ) ) {
     *   // error
     *   String errorMessage = (String)retVals.get( 1 );
     * }
     *         
     * </pre>
     * @throws RemoteException
     */
    public Vector deleteContext( int context_id ) throws RemoteException;
    
    
//  FIXME: Das muss noch besser beschrieben werden!
    /**
     * Change the Filespool data of the given context.
     * 
     * @param context_id numerical context identifier
     * @param new_filestore_handle <code>Hashtable</code> containing the new filestore handle
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.
     *         <p>
     * @throws RemoteException
     * @see #CONTEXT_FILESTORE_ID
     * @see #CONTEXT_FILESTORE_NAME
     * @see #CONTEXT_FILESTORE_USERNAME
     * @see #CONTEXT_FILESTORE_PASSWORD
     * @see #CONTEXT_FILESTORE_QUOTA_MAX
     */
    public Vector changeStorageData(int context_id,Hashtable new_filestore_handle) throws RemoteException;
    
    
    /**
     * Move all data of a context contained on the filestore to another filestore using
     * specified reason to disable context.
     * 
     * @param context_id numerical context identifier
     * @param dstStore_id id of filestore to which the context will be moved
     * @param reason_id id to table containing textual reasons
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.
     *         <p>
     *         This method returns immediately and the data is going to be copied
     *         in the background. To query the progress and the result of the actual
     *         task, the {@link I_AdminJobExecutor} interface must be used. See
     *         <code>com.openexchange.adminConsole.JobController</code> for an 
     *         example usage.
     * @throws RemoteException
     */
    public Vector moveContextFilestore(int context_id, int dstStore_id, int reason_id) throws RemoteException;
    

    /**
     * Move all data of a context contained in a database to another database using
     * specified reason to disable context.
     * 
     * @param context_id numerical context identifier
     * @param dstDatabase_id id of database to which the context will be moved
     * @param reason_id id to table containing textual reasons
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.
     *         <p>
     *         This method returns immediately and the data is going to be copied
     *         in the background. To query the progress and the result of the actual
     *         task, the {@link I_AdminJobExecutor} interface must be used. See
     *         <code>com.openexchange.adminConsole.JobController</code> for an 
     *         example usage.
     * @throws RemoteException
     */
    public Vector moveContextDatabase(int context_id,int dstDatabase_id,int reason_id) throws RemoteException;
    
    
    /**
     * Disable given context.<br>
     * Note: To disable a context you need a reason_id
     * A reason_id can be determined using I_OXUtil.getAllMaintenanceReasons()
     * 
     * @param context_id numerical context identifier
     * @param reason_id id of the maintenance reason
     * @return Vector containing return code and/or result objects<br>
     * <p>
     *     1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *     when method succeeds and <code>"ERROR"</code> in case of a failure.<br>
     *     2nd Object is only avaible if 1st Object is "ERROR". It's of type <code>String</code>
     *     and contains the errorMessage.
     * </p>
     * 
     * Example code:<br>
     * <pre>
     * Vector retVals = rmi.disableContext( ext_context_id, reason_id );
     * String status = (String)retVals.get( 0 );
     * if ( status.equalsIgnoreCase( "OK" ) ) {
     *   // no-error
     * }
     * 
     * if ( status.equalsIgnoreCase( "ERROR" ) ) {
     *   // error
     *   String errorMessage = (String)retVals.get( 1 );
     * }
     *         
     * </pre>
     * @throws RemoteException
     * @see I_OXUtil#getAllMaintenanceReasons()
     */
    public Vector disableContext( int context_id, int reason_id ) throws RemoteException;
    
    
    
    /**
     * Enable given context.
     * 
     * @param context_id numerical context identifier
     * @return Vector containing return code and/or result objects<br>
     * <p>
     *     1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *     when method succeeds and <code>"ERROR"</code> in case of a failure.<br>
     *     2nd Object is only avaible if 1st Object is "ERROR". It's of type <code>String</code>
     *     and contains the errorMessage.
     * </p>
     * 
     * Example code:<br>
     * <pre>
     * Vector retVals = rmi.enableContext( ext_context_id );
     * String status = (String)retVals.get( 0 );
     * if ( status.equalsIgnoreCase( "OK" ) ) {
     *   // no-error
     * }
     * 
     * if ( status.equalsIgnoreCase( "ERROR" ) ) {
     *   // error
     *   String errorMessage = (String)retVals.get( 1 );
     * }
     *         
     * </pre>
     * @throws RemoteException
     */
    public Vector enableContext( int context_id ) throws RemoteException;
    
    
    /**
     * Search for contexts<br>
     * Use this for search a context or list all contexts.
     * 
     * @param search_pattern String with search pattern e.g "*mycontext*" or {@link #PATTERN_SEARCH_ALL_CONTEXT}
     * @return Vector containing return code and/or result objects<br>
     * 
     * <p>
     *     1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *     when method succeeds and <code>"ERROR"</code> in case of a failure.<br>
     *     2nd Object is of type <code>String</code> and contains the errorMessage, if 1st Object is "ERROR".<br>
     *     <br>
     *     If 1st Object is "OK", 2nd Object is of type <code>Hashtable</code>. This contains keys of type <code>String</code>
     *     they are the context_id's and values a are Objects of type <code>Hashtable</code>. This <code>Hashtable</code>s have
     *     all context data.
     * </p>
     * 
     * Example code:<br>
     * <pre>
     * try {
     *     String ox_host = "localhost";
     *     I_OXContext rmi = (I_OXContext)Naming.lookup( "rmi://" + ox_host + "/" + I_OXContext.RMI_NAME );
     *     
     *     Vector retVals = rmi.searchContext( "*mycontext*" );
     *     // or for list all, use this:
     *     // Vector retVals = rmi.searchContext( I_OXContext.PATTERN_SEARCH_ALL_CONTEXT );
     *     
     *     String status = (String)retVals.get( 0 );
     *     
     *     if ( status.equalsIgnoreCase( "OK" ) ) {
     *         // no-error
     *         Hashtable main_hash = (Hashtable)retVals.get( 1 );
     *         
     *         Enumeration enh = main_hash.keys();
     *         while ( enh.hasMoreElements() ) {
     *             String key_context_id = enh.nextElement().toString();
     *             Hashtable context_data = (Hashtable)main_hash.get( key_context_id );
     *             
     *             // now, you can use the data
     *             System.out.println( context_data.get( I_OXContext.CONTEXT_NAME ) );
     *             System.out.println( context_data.get( I_OXContext.CONTEXT_LOCKED_TXT_ID ) );
     *             System.out.println( context_data.get( I_OXContext.CONTEXT_FILESTORE_NAME ) );
     *             // ... and more
     *         }
     *     }
     *     
     *     if ( status.equalsIgnoreCase( "ERROR" ) ) {
     *         // error
     *         String errorMessage = (String)retVals.get( 1 );
     *     }
     * } catch ( Exception exp ) {
     *     exp.printStackTrace();
     * }       
     * </pre>
     * 
     * @see #PATTERN_SEARCH_ALL_CONTEXT
     * @see #CONTEXT_FILESTORE_ID
     * @see #CONTEXT_FILESTORE_NAME
     * @see #CONTEXT_LOCKED_TXT_ID
     * @see #CONTEXT_NAME
     * @see #CONTEXT_ID
     * @see #CONTEXT_LOCKED
     * @throws RemoteException
     */
    public Vector searchContext( String search_pattern ) throws RemoteException;
    
    
    
    /**
     * Disable all contexts.<br>
     * Note: To disable all contexts, you need a reason_id.
     * A reason_id can be determined using I_OXUtil.getAllMaintenanceReasons()
     * @param reason_id id of maintenance reason
     * @return Vector containing return code and/or result objects
     * <p>
     *     1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *     when method succeeds and <code>"ERROR"</code> in case of a failure.<br>
     *     2nd Object is only avaible if 1st Object is "ERROR". It's of type <code>String</code>
     *     and contains the errorMessage.
     * </p>
     * 
     * Example code:<br>
     * <pre>
     * Vector retVals = rmi.disableAllContexts(reason_id);
     * String status = (String)retVals.get( 0 );
     * if ( status.equalsIgnoreCase( "OK" ) ) {
     *   // no-error
     * }
     * 
     * if ( status.equalsIgnoreCase( "ERROR" ) ) {
     *   // error
     *   String errorMessage = (String)retVals.get( 1 );
     * }
     *         
     * </pre>
     * @throws RemoteException
     * @see I_OXUtil#getAllMaintenanceReasons()
     */
    public Vector disableAllContexts( int reason_id ) throws RemoteException;
    
    
    
    /**
     * Enable all contexts.
     * 
     * @return Vector containing return code and/or result objects
     *         <p>
     *     1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *     when method succeeds and <code>"ERROR"</code> in case of a failure.<br>
     *     2nd Object is only avaible if 1st Object is "ERROR". It's of type <code>String</code>
     *     and contains the errorMessage.
     * </p>
     * 
     * Example code:<br>
     * <pre>
     * Vector retVals = rmi.enableAllContexts();
     * String status = (String)retVals.get( 0 );
     * if ( status.equalsIgnoreCase( "OK" ) ) {
     *   // no-error
     * }
     * 
     * if ( status.equalsIgnoreCase( "ERROR" ) ) {
     *   // error
     *   String errorMessage = (String)retVals.get( 1 );
     * }
     *         
     * </pre>
     * @throws RemoteException
     */
    public Vector enableAllContexts() throws RemoteException;
    
    
    
//  FIXME: Das muss noch besser beschrieben werden!
    /**
     * Retrieves the context setup data all contexts.
     *
     * @param context_id numerical server identifier 
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.
     *         2nd Object is of type <code>Hashtable</code> containing the webdav handle,
     *         database handle, context state (maintenance reason if context is locked), 
     *         quota size and currently used quota in MB.
     *         <p>
     * @throws RemoteException
     */
    public Vector getContextSetup(int context_id) throws RemoteException;
    
    
//  FIXME: Das muss noch besser beschrieben werden!
    /**
     * Change the database handle of the given context.
     * 
     * @param context_id numerical context identifier
     * @param new_database_handle <code>Hashtable</code> containing the new database handle
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.
     *         <p>
     * @throws RemoteException
     */
    public Vector changeDatabaseContext(int context_id,Hashtable new_database_handle ) throws RemoteException;
    
    
//  FIXME: Das muss noch besser beschrieben werden!
    /**
     * Change the quota size of the given context.
     * 
     * @param context_id numerical context identifier
     * @param quota_max new maximum quota value for this context
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.
     *         <p>
     * @throws RemoteException
     */
    public Vector changeQuota(int context_id, long quota_max ) throws RemoteException;
    
    
//  FIXME: Das muss noch besser beschrieben werden!
    /**
     * Search for context on specified db.
     *
     * @param db_host_url String containing the db host url to search on
     * @return Vector containing return code and/or result objects
     *         <p>
     *     1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *     when method succeeds and <code>"ERROR"</code> in case of a failure.<br>
     *     2nd Object is of type <code>String</code> and contains the errorMessage, if 1st Object is "ERROR".<br>
     *     <br>
     *     If 1st Object is "OK", 2nd Object is of type <code>Hashtable</code>. This contains keys of type <code>String</code>
     *     they are the context_id's and values a are Objects of type <code>Hashtable</code>. This <code>Hashtable</code>s have
     *     all context data.
     * </p>
     * 
     * Example code:<br>
     * <pre>
     * try {
     *     String ox_host = "localhost";
     *     I_OXContext rmi = (I_OXContext)Naming.lookup( "rmi://" + ox_host + "/" + I_OXContext.RMI_NAME );
     *     
     *     Vector retVals = rmi.searchContext( "*mycontext*" );
     *     // or for list all, use this:
     *     // Vector retVals = rmi.searchContext( I_OXContext.PATTERN_SEARCH_ALL_CONTEXT );
     *     
     *     String status = (String)retVals.get( 0 );
     *     
     *     if ( status.equalsIgnoreCase( "OK" ) ) {
     *         // no-error
     *         Hashtable main_hash = (Hashtable)retVals.get( 1 );
     *         
     *         Enumeration enh = main_hash.keys();
     *         while ( enh.hasMoreElements() ) {
     *             String key_context_id = enh.nextElement().toString();
     *             Hashtable context_data = (Hashtable)main_hash.get( key_context_id );
     *             
     *             // now, you can use the data
     *             System.out.println( context_data.get( I_OXContext.CONTEXT_NAME ) );
     *             System.out.println( context_data.get( I_OXContext.CONTEXT_LOCKED_TXT_ID ) );
     *             System.out.println( context_data.get( I_OXContext.CONTEXT_FILESTORE_NAME ) );
     *             // ... and more
     *         }
     *     }
     *     
     *     if ( status.equalsIgnoreCase( "ERROR" ) ) {
     *         // error
     *         String errorMessage = (String)retVals.get( 1 );
     *     }
     * } catch ( Exception exp ) {
     *     exp.printStackTrace();
     * }       
     * </pre>
     * 
     * @see #PATTERN_SEARCH_ALL_CONTEXT
     * @see #CONTEXT_FILESTORE_ID
     * @see #CONTEXT_FILESTORE_NAME
     * @see #CONTEXT_LOCKED_TXT_ID
     * @see #CONTEXT_NAME
     * @see #CONTEXT_ID
     * @see #CONTEXT_LOCKED
     * @throws RemoteException
     */
    public Vector searchContextByDatabase(String db_host_url) throws RemoteException;
    
    
//  FIXME: Das muss noch besser beschrieben werden!
    /**
     * Search for context which store data on specified filestore
     *
     * @param filestore_url String containing the filestore url.
     * @return Vector containing return code and/or result objects
     *         <p>
     *     1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *     when method succeeds and <code>"ERROR"</code> in case of a failure.<br>
     *     2nd Object is of type <code>String</code> and contains the errorMessage, if 1st Object is "ERROR".<br>
     *     <br>
     *     If 1st Object is "OK", 2nd Object is of type <code>Hashtable</code>. This contains keys of type <code>String</code>
     *     they are the context_id's and values a are Objects of type <code>Hashtable</code>. This <code>Hashtable</code>s have
     *     all context data.
     * </p>
     * 
     * Example code:<br>
     * <pre>
     * try {
     *     String ox_host = "localhost";
     *     I_OXContext rmi = (I_OXContext)Naming.lookup( "rmi://" + ox_host + "/" + I_OXContext.RMI_NAME );
     *     
     *     Vector retVals = rmi.searchContextByFilestore( "*mycontext*" );
     *     // or for list all, use this:
     *     
     *     String status = (String)retVals.get( 0 );
     *     
     *     if ( status.equalsIgnoreCase( "OK" ) ) {
     *         // no-error
     *         Hashtable main_hash = (Hashtable)retVals.get( 1 );
     *         
     *         Enumeration enh = main_hash.keys();
     *         while ( enh.hasMoreElements() ) {
     *             String key_context_id = enh.nextElement().toString();
     *             Hashtable context_data = (Hashtable)main_hash.get( key_context_id );
     *             
     *             // now, you can use the data
     *             System.out.println( context_data.get( I_OXContext.CONTEXT_NAME ) );
     *             System.out.println( context_data.get( I_OXContext.CONTEXT_LOCKED_TXT_ID ) );
     *             System.out.println( context_data.get( I_OXContext.CONTEXT_FILESTORE_NAME ) );
     *             // ... and more
     *         }
     *     }
     *     
     *     if ( status.equalsIgnoreCase( "ERROR" ) ) {
     *         // error
     *         String errorMessage = (String)retVals.get( 1 );
     *     }
     * } catch ( Exception exp ) {
     *     exp.printStackTrace();
     * }       
     * </pre>
     * 
     * @see I_OXUtil#listFilestores(String)
     * @see #CONTEXT_FILESTORE_ID
     * @see #CONTEXT_FILESTORE_NAME
     * @see #CONTEXT_LOCKED_TXT_ID
     * @see #CONTEXT_NAME
     * @see #CONTEXT_ID
     * @see #CONTEXT_LOCKED
     * @throws RemoteException
     */
    public Vector searchContextByFilestore( String filestore_url ) throws RemoteException;
}
