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
package com.openexchange.admin.adminConsole;

import java.io.File;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


import com.openexchange.admin.dataSource.I_OXContext;
import com.openexchange.admin.dataSource.I_OXUtil;
import com.openexchange.admin.dataSource.impl.OXContext;



public class OXUtil implements I_OXUtil {
    

    public static final String SHELL_COMMAND                        = "command";
    public static final String SHELL_COMMAND_REGISTER_SERVER        = "register_server";
    public static final String SHELL_COMMAND_REGISTER_DBMS          = "register_database";
    public static final String SHELL_COMMAND_REGISTER_FILESTORE     = "register_filestore";
    public static final String SHELL_COMMAND_ADD_REASON             = "add_reason";
    public static final String SHELL_COMMAND_LIST_ALL_REASONS       = "list_reasons";
    public static final String SHELL_COMMAND_LIST_DBMS              = "list_dbms";
    public static final String SHELL_COMMAND_LIST_FILESTORE         = "list_filestore";
    public static final String SHELL_COMMAND_LIST_SERVER            = "list_server";
    public static final String SHELL_COMMAND_EDIT_FILESTORE         = "edit_filestore";
    public static final String SHELL_COMMAND_UNREGISTER_FILESTORE   = "unregister_filestore";
    public static final String SHELL_COMMAND_UNREGISTER_SERVER      = "unregister_server";
    public static final String SHELL_COMMAND_UNREGISTER_DBMS        = "unregister_database";
    public static final String SHELL_COMMAND_UPDATE_DBMS            = "update_database";
    
    
    public static final String[] POSSIBLE_SHELL_COMMANDY = {
        SHELL_COMMAND_REGISTER_SERVER,
        SHELL_COMMAND_REGISTER_DBMS,
        SHELL_COMMAND_REGISTER_FILESTORE,
        SHELL_COMMAND_ADD_REASON,
        SHELL_COMMAND_LIST_ALL_REASONS,
        SHELL_COMMAND_LIST_DBMS,
        SHELL_COMMAND_LIST_FILESTORE,
        SHELL_COMMAND_LIST_SERVER,
        SHELL_COMMAND_EDIT_FILESTORE,
        SHELL_COMMAND_UNREGISTER_FILESTORE,
        SHELL_COMMAND_UNREGISTER_SERVER,
        SHELL_COMMAND_UNREGISTER_DBMS,
        SHELL_COMMAND_UPDATE_DBMS
    };
    
    public static final String SHELL_SERVER_NAME        = "server_name";
    
    
    public static final String SHELL_SEARCH_PATTERN     = "pattern";
    
    public static final String SHELL_ID                 = "ID";
    
    public static final String SHELL_DB_WEIGHT          = I_OXUtil.DB_CLUSTER_WEIGHT;
    public static final String SHELL_DB_MAX_USER        = I_OXUtil.DB_MAX_UNITS;
    public static final String SHELL_DB_CUR_USER        = I_OXUtil.DB_CUR_UNITS;
    public static final String SHELL_DB_NAME            = I_OXUtil.DB_DISPLAY_NAME;
    public static final String SHELL_DB_DRIVER          = I_OXUtil.DB_DRIVER;
    public static final String SHELL_DB_USER            = I_OXUtil.DB_AUTHENTICATION_ID;
    public static final String SHELL_DB_PASSWD          = I_OXUtil.DB_AUTHENTICATION_PASSWORD;
    public static final String SHELL_DB_URL             = I_OXUtil.DB_URL;
    public static final String SHELL_DB_HOST            = "OX_DB_HOSTNAME";
    public static final String SHELL_DB_PARAM           = "OX_DB_SQLPARAM";
    public static final String SHELL_DB_IS_MASTER       = I_OXUtil.DB_POOL_IS_MASTER;
    public static final String SHELL_DB_MASTER_ID       = I_OXUtil.DB_POOL_MASTER_ID;
    public static final String SHELL_DB_POOL_HARDLIMIT  = I_OXUtil.DB_POOL_HARDLIMIT;
    public static final String SHELL_DB_POOL_MAX        = I_OXUtil.DB_POOL_MAX;
    public static final String SHELL_DB_POOL_INITIAL    = I_OXUtil.DB_POOL_INIT;
    //public static final String SHELL_STORE_ID           = "filestore_id";
    public static final String SHELL_STORE_PATH         = "OX_STORE_PATH";
    public static final String SHELL_STORE_URI          = I_OXUtil.STORE_URL;
    public static final String SHELL_STORE_SIZE         = I_OXUtil.STORE_SIZE;
    public static final String SHELL_STORE_MAX_CTX      = I_OXUtil.STORE_MAX_CONTEXT;
    public static final String SHELL_STORE_CUR_CTX      = I_OXUtil.STORE_CUR_CONTEXT;
    public static final String SHELL_REASON_TEXT        = "REASON";
    
    
    public static final String SHELL_HOST               = "host";
    
    public static final String SHELL_SSL                = "ssl";
    
    private static Hashtable<String, Comparable>    utilData            = null;
    private static String       command             = "";
    
    private static Vector       xmlrpc_return       = null;
    private static String[]     neededFields        = null;
    private static Vector<String>       missingFields       = null;
    private static I_OXUtil            ox_util             = null;
    private Registry            registry            = null;

    public static final String SHELL_DEFAULT_DB_PARAM    = "useUnicode=true" +
    "&characterEncoding=UTF-8" +
    "&autoReconnect=true" +
    "&useUnicode=true" +
    "&useServerPrepStmts=false" +
    "&useTimezone=true" +
    "&serverTimezone=UTC" +
    "&connectTimeout=15000" +
    "&socketTimeout=15000";
    public static final String SHELL_DEFAULT_DB_HOST     = "localhost";
    
    
    public static void main( String args[] ) throws Exception {
        utilData = new Hashtable<String, Comparable>();
        missingFields = new Vector<String>();
        
        utilData = AdminConsoleTools.parseInput( args, SHELL_COMMAND );
        if ( utilData != null && utilData.containsKey( SHELL_COMMAND ) ) {
            command = utilData.get( SHELL_COMMAND ).toString();
            utilData.remove( SHELL_COMMAND );
        }
        
        if ( checkNeeded() ) {
            OXUtil ox_util = new OXUtil();
            
            if ( utilData.containsKey( SHELL_DB_WEIGHT ) ) {
                Float fl = new Float( utilData.get( SHELL_DB_WEIGHT ).toString() );
                fl = fl * 100;
                Integer ii = new Integer( fl.intValue() );
                utilData.put( SHELL_DB_WEIGHT, ii );
            }
            
            if ( utilData.containsKey( SHELL_DB_MAX_USER ) ) {
                Integer ii = new Integer( utilData.get( SHELL_DB_MAX_USER ).toString() );
                utilData.put( SHELL_DB_MAX_USER, ii );
            }
            
            if ( utilData.containsKey( SHELL_DB_POOL_HARDLIMIT )) {
                String bo = utilData.get( SHELL_DB_POOL_HARDLIMIT ).toString();
                if ( Boolean.valueOf( bo ) ) {
                    utilData.put( SHELL_DB_POOL_HARDLIMIT, 1 );
                } else {
                    utilData.put( SHELL_DB_POOL_HARDLIMIT, 0 );
                }
            }
            
            if ( utilData.containsKey( SHELL_DB_POOL_MAX ) ) {
                Integer ii = new Integer( utilData.get( SHELL_DB_POOL_MAX ).toString() );
                utilData.put( SHELL_DB_POOL_MAX, ii );
            }
            
            if ( utilData.containsKey( SHELL_DB_POOL_INITIAL ) ) {
                Integer ii = new Integer( utilData.get( SHELL_DB_POOL_INITIAL ).toString() );
                utilData.put( SHELL_DB_POOL_INITIAL, ii );
            }
            
            
            if ( command.equals( SHELL_COMMAND_REGISTER_SERVER ) ) {
                String host = "";
                
                if ( utilData.containsKey( SHELL_HOST ) ) {
                    host = utilData.get( SHELL_HOST ).toString();
                    
                    xmlrpc_return = ox_util.registerServer( host );
                    
                    if ( xmlrpc_return != null && xmlrpc_return.size() >= 1 && xmlrpc_return.get( 0 ).toString().equalsIgnoreCase( "OK" ) ) {
                        String id = "";
                        if ( xmlrpc_return.size() > 1 ) {
                            id = " (ID=" + xmlrpc_return.get( 1 ).toString() + ")";
                        }
                        System.out.println( "New Server " + host + " added" + id + "." );
                    } else {
                        System.err.println( xmlrpc_return );
                        System.exit( 1 );
                    }
                } else {
                    showRegisterServerUsage();
                    System.exit( 1 );
                }
                
            } else if ( command.equals( SHELL_COMMAND_LIST_SERVER ) ) {
                String pattern = "*";
                if ( utilData.containsKey( SHELL_SEARCH_PATTERN ) ) {
                    pattern = utilData.get( SHELL_SEARCH_PATTERN ).toString();
                }
                
                xmlrpc_return = ox_util.searchForServer( pattern );
                
                if ( xmlrpc_return != null && xmlrpc_return.size() > 1 && xmlrpc_return.get( 0 ).toString().equalsIgnoreCase( "OK" ) ) {
                    
                    Hashtable<String, String> dn = new Hashtable<String, String>();
                    dn.put( SERVER_NAME, "Host" );
                    dn.put( SERVER_ID, "ID" );
                    
                    System.out.println( dn.get( SERVER_ID ) + "\t" + dn.get( SERVER_NAME )  );
                    
                    for ( int i = 1; i < xmlrpc_return.size(); i++ ) {
                        Hashtable h = (Hashtable)xmlrpc_return.get( i );
                        
                        Enumeration eh = h.keys();
                        while ( eh.hasMoreElements() ) {
                            Object key = eh.nextElement();
                            Hashtable h2 = (Hashtable)h.get( key );
                            System.out.print( key + "\t" );
                            System.out.print( h2.get( SERVER_NAME ) + "\n");
                        }
                    }
                    
                } else {
                    System.err.println( xmlrpc_return );
                    System.exit( 1 );
                }
                
            } else if ( command.equals( SHELL_COMMAND_LIST_DBMS ) ) {
                String pattern = "*";
                if ( utilData.containsKey( SHELL_SEARCH_PATTERN ) ) {
                    pattern = utilData.get( SHELL_SEARCH_PATTERN ).toString();
                }
                
                xmlrpc_return = ox_util.searchForDatabase( pattern );
                
                if ( xmlrpc_return != null && xmlrpc_return.size() > 1 && xmlrpc_return.get( 0 ).toString().equalsIgnoreCase( "OK" ) ) {
                    
                    String TFORMAT = "%-5s %-20s %-20s %-6s %12s %-6s %-8s %-6s %-6s %-3s %-6s\n";
                    String VFORMAT = "%5s %-20s %-20s %-6s %12s %6s %8s %6s %6s %3s %6s\n";
                    System.out.format(TFORMAT,
                    "ID", "Name", "Hostname", "Master", "Master ID","Weight", "max. Ctx", "is Ctx", "hlimit", "max", "initial");

                    for ( int i = 1; i < xmlrpc_return.size(); i++ ) {
                        Hashtable h = (Hashtable)xmlrpc_return.get( i );
                        
                        Enumeration eh = h.keys();
                        while ( eh.hasMoreElements() ) {
                            Object key = eh.nextElement();
                            Hashtable h2 = (Hashtable)h.get( key );
                            java.net.URI uri = new java.net.URI(((String)h2.get( SHELL_DB_URL )).substring("jdbc:".length()));
                            String hostname = uri.getHost();
                            String hlimit = "true";
                            if ( h2.get( SHELL_DB_POOL_HARDLIMIT ).toString().equalsIgnoreCase( "0" ) ) {
                                hlimit = "false";
                            }
                            
                            System.out.format(VFORMAT,
                            		key,
                            		h2.get( SHELL_DB_NAME ),
                            		hostname,
                            		h2.get( SHELL_DB_IS_MASTER ),
                            		h2.get( SHELL_DB_MASTER_ID ),
                            		h2.get( SHELL_DB_WEIGHT ),
                            		h2.get( SHELL_DB_MAX_USER ),
                            		h2.get( SHELL_DB_CUR_USER ),
                                    hlimit,
                                    h2.get( SHELL_DB_POOL_MAX ),
                                    h2.get( SHELL_DB_POOL_INITIAL ) );
                        }
                    }
                    
                } else {
                    System.err.println( xmlrpc_return );
                    System.exit( 1 );
                }
                
            } else if ( command.equals( SHELL_COMMAND_LIST_ALL_REASONS ) ) {
                xmlrpc_return = ox_util.getAllMaintenanceReasons();
                if ( xmlrpc_return != null && xmlrpc_return.size() > 1 && xmlrpc_return.get( 0 ).toString().equalsIgnoreCase( "OK" ) ) {
                    
                    Vector resons = (Vector)xmlrpc_return.get( 1 );
                    String TFORMAT = "%-5s %-60s\n";
                    String VFORMAT = "%5s %-60s\n";
                    System.out.format( TFORMAT, "ID", "Reason_Text" );
                    
                    for ( int i = 0; i < resons.size(); i++ ) {
                        int key = Integer.parseInt( resons.get( i ).toString() );
                        String res_text = "no text added or error on get it.";
                        
                        xmlrpc_return = ox_util.getMaintenanceReason( key );
                        if ( xmlrpc_return != null && xmlrpc_return.size() > 1 && xmlrpc_return.get( 0 ).toString().equalsIgnoreCase( "OK" ) ) {
                            res_text = xmlrpc_return.get( 1 ).toString();
                        }
                        
                        System.out.format( VFORMAT, key, res_text );
                    }
                        
                    
                } else {
                    System.err.println( xmlrpc_return );
                    System.exit( 1 );
                }
            } else if ( command.equals( SHELL_COMMAND_ADD_REASON ) ) {
                String reason_txt = "*";
                if ( utilData.containsKey( SHELL_REASON_TEXT ) ) {
                    reason_txt = utilData.get( SHELL_REASON_TEXT ).toString();
                } else {
                    missingFields.add( SHELL_REASON_TEXT );
                    showUsage();
                    showMissing();
                    System.exit( 1 );
                }
                
                xmlrpc_return = ox_util.addMaintenanceReason( reason_txt );
                
                if ( xmlrpc_return != null && xmlrpc_return.size() > 1 && xmlrpc_return.get( 0 ).toString().equalsIgnoreCase( "OK" ) ) {
                    String id = "";
                    if ( xmlrpc_return.size() > 1 ) {
                        id = " (ID=" + xmlrpc_return.get( 1 ).toString() + ")";
                    }
                    System.out.println( "New Reason added" + id + "." );
                } else {
                    System.err.println( xmlrpc_return );
                    System.exit( 1 );
                }
            } else if ( command.equals( SHELL_COMMAND_LIST_FILESTORE ) ) {
                String pattern = "*";
                if ( utilData.containsKey( SHELL_SEARCH_PATTERN ) ) {
                    pattern = utilData.get( SHELL_SEARCH_PATTERN ).toString();
                }
                
                xmlrpc_return = ox_util.listFilestores( pattern );
                
                if ( xmlrpc_return != null && xmlrpc_return.size() > 1 && xmlrpc_return.get( 0 ).toString().equalsIgnoreCase( "OK" ) ) {
                    
                    String TFORMAT = "%-5s %-40s %8s %8s %8s %-7s %-7s\n";
                    String VFORMAT = "%5s %-40s %8s %8s %8s %7s %7s\n";
                    System.out.format(TFORMAT,
                    "ID", "Path", "Total", "Reserved", "Used", "max Ctx.", "is Ctx.");

                    
                    for ( int i = 1; i < xmlrpc_return.size(); i++ ) {
                        Hashtable h = (Hashtable)xmlrpc_return.get( i );
                        
                        Enumeration eh = h.keys();
                        while ( eh.hasMoreElements() ) {
                            Object key = eh.nextElement();
                            Hashtable h2 = (Hashtable)h.get( key );
                            long storesize = (Long)h2.get( SHELL_STORE_SIZE );
                            String suri = (String)h2.get( SHELL_STORE_URI );
                            // now summarize used quota of all contexts on this store
                            long quota_used = 0; // in MB
                            long quota_reserved = 0; // in MB
                            com.openexchange.admin.dataSource.impl.OXContext ox_context = new OXContext();
                            // find contexts on this store
                            Vector ret = ox_context.searchContextByFilestore(suri);
                            if ( ret != null && ret.size() > 1 && ret.get( 0 ).toString().equalsIgnoreCase( "OK" ) ) {
                            	Vector<Integer> ids = (Vector)ret.get(1);
                            	Enumeration<Integer> e = ids.elements();
                            	// summarize used quota
                            	while( e.hasMoreElements() ) {
                            		int ctxid = e.nextElement();
                            		Vector ret1 = ox_context.getContextSetup(ctxid);
                                    if ( ret1 != null && ret1.size() > 1 && ret1.get( 0 ).toString().equalsIgnoreCase( "OK" ) ) {
                                    	Hashtable ctxdata = (Hashtable)ret1.get(1);
                                    	//System.out.println(ctxdata);
                                    	if( ctxdata.containsKey(I_OXContext.CONTEXT_FILESTORE_QUOTA_USED) ) {
                                    		quota_used += (Long)ctxdata.get(I_OXContext.CONTEXT_FILESTORE_QUOTA_USED); 
                                    	}
                                    	if( ctxdata.containsKey(I_OXContext.CONTEXT_AVERAGE_QUOTA) ) {
                                    		quota_reserved += (Long)ctxdata.get(I_OXContext.CONTEXT_AVERAGE_QUOTA); 
                                    	}
                                    } else {
                                    	System.out.println(ret1);
                                    }
                            	}
                            }
                            System.out.format(VFORMAT,
                            		key,
                            		new java.net.URI(suri).getPath(),
                            		storesize,
                            		quota_reserved,
                            		quota_used,
                            		h2.get( SHELL_STORE_MAX_CTX ),
                            		h2.get( SHELL_STORE_CUR_CTX ));
                        }
                    }
                    
                } else {
                    System.err.println( xmlrpc_return );
                    System.exit( 1 );
                }
                
            } else if ( command.equals( SHELL_COMMAND_REGISTER_DBMS ) ) {
                boolean isMaster = true;
                if ( utilData.containsKey( SHELL_DB_IS_MASTER ) ) {
                    Boolean b = new Boolean( utilData.get( SHELL_DB_IS_MASTER ).toString() );
                    isMaster = b.booleanValue();
                }
                
                int master_id = -1;
                if ( !isMaster ) {
                    if ( utilData.containsKey( SHELL_DB_MASTER_ID ) ) {
                        master_id = Integer.parseInt( utilData.get( SHELL_DB_MASTER_ID ).toString() );
                    } else {
                        missingFields.add( SHELL_DB_MASTER_ID );
                        showUsage();
                        showMissing();
                        System.exit( 1 );
                    }
                }
                String dbhost = SHELL_DEFAULT_DB_HOST;
                String dbparam  = SHELL_DEFAULT_DB_PARAM;
                if ( utilData.containsKey( SHELL_DB_HOST ) ) {
                	dbhost = (String)utilData.get( SHELL_DB_HOST );
                }
                if ( utilData.containsKey( SHELL_DB_PARAM ) ) {
                	dbparam = (String)utilData.get( SHELL_DB_PARAM );
                }
                String uri = "jdbc:mysql://" + dbhost + "/?" + dbparam; 
                utilData.put(I_OXUtil.DB_URL, uri);

                if ( !utilData.containsKey( I_OXUtil.DB_DRIVER ) ) {
                	utilData.put(I_OXUtil.DB_DRIVER, "com.mysql.jdbc.Driver");
                }
                
                xmlrpc_return = ox_util.registerDatabase( utilData, isMaster, master_id ) ;
                
                if ( xmlrpc_return != null && xmlrpc_return.size() > 1 && xmlrpc_return.get( 0 ).toString().equalsIgnoreCase( "OK" ) ) {
                    String id = "";
                    if ( xmlrpc_return.size() > 1 ) {
                        id = " (ID=" + xmlrpc_return.get( 1 ).toString() + ")";
                    }
                    System.out.println( "New DBMS added" + id + "." );
                } else {
                    System.err.println( xmlrpc_return );
                    System.exit( 1 );
                }
                
            } else if ( command.equals( SHELL_COMMAND_UPDATE_DBMS ) ) {
                int database_id = 0;
                if ( utilData.containsKey( SHELL_ID ) ) {
                    Integer iid = new Integer( utilData.get( SHELL_ID ).toString() );
                    database_id = iid.intValue();
                } else {
                    missingFields.add( SHELL_ID );
                    showUsage();
                    showMissing();
                    System.exit( 1 );
                }
                
                xmlrpc_return = ox_util.changeDatabase( database_id, utilData );
                
                if ( xmlrpc_return != null && xmlrpc_return.size() > 1 && xmlrpc_return.get( 0 ).toString().equalsIgnoreCase( "OK" ) ) {
                    String id = "";
                    if ( xmlrpc_return.size() > 1 ) {
                        id = " (ID=" + xmlrpc_return.get( 1 ).toString() + ")";
                    }
                    System.out.println( "Updated DBMS " + id + "." );
                } else {
                    System.err.println( xmlrpc_return );
                    System.exit( 1 );
                }
                
            } else if ( command.equals( SHELL_COMMAND_REGISTER_FILESTORE ) ) {
                String store_URL = null;
                long store_size = 0;
                int store_maxContexts = 0;
                
                if ( utilData.containsKey( SHELL_STORE_PATH ) ) {
                    store_URL = utilData.get( SHELL_STORE_PATH ).toString();
                    File f_store = new File( store_URL );
                    store_URL = f_store.toURI().toString();
                }
                if ( utilData.containsKey( SHELL_STORE_SIZE ) ) {
                    Long l = new Long( utilData.get( SHELL_STORE_SIZE ).toString() );
                    store_size = l.longValue();
                }
                if ( utilData.containsKey( SHELL_STORE_MAX_CTX ) ) {
                    Integer i = new Integer( utilData.get( SHELL_STORE_MAX_CTX ).toString() );
                    store_maxContexts = i.intValue();
                }
                
                xmlrpc_return = ox_util.registerFilestore( store_URL, store_size, store_maxContexts );
                
                if ( xmlrpc_return != null && xmlrpc_return.size() > 1 && xmlrpc_return.get( 0 ).toString().equalsIgnoreCase( "OK" ) ) {
                    String id = "";
                    if ( xmlrpc_return.size() > 1 ) {
                        id = " (ID=" + xmlrpc_return.get( 1 ).toString() + ")";
                    }
                    System.out.println( "New storage "+store_URL+" added" + id + "." );
                } else {
                    System.err.println( xmlrpc_return );
                    System.exit( 1 );
                }
                
            } else if ( command.equals( SHELL_COMMAND_UNREGISTER_DBMS ) ) {
                int dbms_id = 0;
                if ( utilData.containsKey( SHELL_ID ) ) {
                    Integer i = new Integer( utilData.get( SHELL_ID ).toString() );
                    dbms_id = i.intValue();
                } else {
                    showUnregisterDatabaseUsage();
                    System.exit( 1 );
                }
                
                xmlrpc_return = ox_util.unregisterDatabase( dbms_id );
                
                if ( xmlrpc_return != null && xmlrpc_return.size() >= 1 && xmlrpc_return.get( 0 ).toString().equalsIgnoreCase( "OK" ) ) {
                    System.out.println( "DBMS with ID="+dbms_id+" removed." );
                } else {
                    System.err.println( xmlrpc_return );
                    System.exit( 1 );
                }
                
            } else if ( command.equals( SHELL_COMMAND_UNREGISTER_SERVER ) ) {
                int server_id = 0;
                if ( utilData.containsKey( SHELL_ID ) ) {
                    Integer i = new Integer( utilData.get( SHELL_ID ).toString() );
                    server_id = i.intValue();
                } else {
                    showUnregisterServerUsage();
                    System.exit( 1 );
                }
                
                xmlrpc_return = ox_util.unregisterServer( server_id );
                
                if ( xmlrpc_return != null && xmlrpc_return.size() >= 1 && xmlrpc_return.get( 0 ).toString().equalsIgnoreCase( "OK" ) ) {
                    System.out.println( "Server with ID="+server_id+" removed." );
                } else {
                    System.err.println( xmlrpc_return );
                    System.exit( 1 );
                }
                
            } else if ( command.equals( SHELL_COMMAND_UNREGISTER_FILESTORE ) ) {
                int store_id = 0;
                if ( utilData.containsKey( SHELL_ID ) ) {
                    Integer i = new Integer( utilData.get( SHELL_ID ).toString() );
                    store_id = i.intValue();
                } else {
                    showUnregisterFilestoreUsage();
                    System.exit( 1 );
                }
                
                xmlrpc_return = ox_util.unregisterFilestore( store_id );
                
                if ( xmlrpc_return != null && xmlrpc_return.size() >= 1 && xmlrpc_return.get( 0 ).toString().equalsIgnoreCase( "OK" ) ) {
                    System.out.println( "Storage with ID="+store_id+" removed." );
                } else {
                    System.err.println( xmlrpc_return );
                    System.exit( 1 );
                }
                
            } else if ( command.equals( SHELL_COMMAND_EDIT_FILESTORE ) ) {
                int store_id = 0;
                Hashtable<String, Object> filestoreData = new Hashtable<String, Object>();
                
                if ( utilData.containsKey( SHELL_ID ) ) {
                    Integer i = new Integer( utilData.get( SHELL_ID ).toString() );
                    store_id = i.intValue();
                }
                if ( utilData.containsKey( SHELL_STORE_PATH ) ) {
                    String store_URL = utilData.get( SHELL_STORE_PATH ).toString();
                    File f_store = new File( store_URL );
                    store_URL = f_store.toURI().toString();
                    filestoreData.put( I_OXUtil.STORE_URL, store_URL );
                }
                if ( utilData.containsKey( SHELL_STORE_SIZE ) ) {
                    Long l_storesize = new Long( utilData.get( SHELL_STORE_SIZE ).toString() );
                    filestoreData.put( I_OXUtil.STORE_SIZE, l_storesize );
                }
                if ( utilData.containsKey( SHELL_STORE_MAX_CTX ) ) {
                    Integer i_maxctx = new Integer( utilData.get( SHELL_STORE_MAX_CTX ).toString() );
                    filestoreData.put( I_OXUtil.STORE_MAX_CONTEXT, i_maxctx );
                }
                
                xmlrpc_return = ox_util.changeFilestore( store_id, filestoreData );
                
                if ( xmlrpc_return != null && xmlrpc_return.size() > 1 && xmlrpc_return.get( 0 ).toString().equalsIgnoreCase( "OK" ) ) {
                    System.out.println( "DB_ID = " + xmlrpc_return.get( 1 ) );
                } else {
                    System.err.println( xmlrpc_return );
                    System.exit( 1 );
                }
                
                
            } else {
                showUsage();
                System.exit( 1 );
            }
            
            // Need for debug
            //System.out.println("\n\n\n--> "+xmlrpc_return);
        } else {
            showUsage();
            showMissing();
            System.exit( 1 );
        }
        System.exit( 0 );
    }
    
    
    
    private static void showUsage(){
        if ( command != null && command.length() > 1 ) {
            if ( command.equals( SHELL_COMMAND_REGISTER_SERVER ) ) {
                showRegisterServerUsage();
            } else if ( command.equals( SHELL_COMMAND_LIST_DBMS ) ) {
                showListDBMSUsage();
                
            } else if ( command.equals( SHELL_COMMAND_REGISTER_DBMS ) ) {
                showRegisterDatabaeUsage();
                
            } else if ( command.equals( SHELL_COMMAND_REGISTER_FILESTORE ) ) {
                showRegisterFilestoreUsage();
                
            } else if ( command.equals( SHELL_COMMAND_LIST_FILESTORE ) ) {
                showListFilestoreUsage();
                
            } else if ( command.equals( SHELL_COMMAND_EDIT_FILESTORE ) ) {
                showEditFilestoreUsage();
                
            } else if ( command.equals( SHELL_COMMAND_UNREGISTER_FILESTORE ) ) {
                showUnregisterFilestoreUsage();
                
            } else if ( command.equals( SHELL_COMMAND_UPDATE_DBMS ) ) {
                showUpdateDatabaeUsage();
                
            } else if ( command.equals( SHELL_COMMAND_ADD_REASON ) ) {
                showAddReasonUsage();
            
            } else {
                //showGeneralUsages();
                System.err.println( "Unknown used parameter '"+command+"'" );
                System.exit( 1 );
            }
        } else {
            //showGeneralUsages();
            System.err.println( "Unknown used parameter '"+command+"'" );
            System.exit( 1 );
        }
    }
    
    
    
//    private static void showGeneralUsages() {
//        StringBuffer sb = new StringBuffer();
//        sb.append( "\n" );
//        sb.append( "Options: \n" );
//        sb.append( "\t --"+SHELL_COMMAND+"=[" );
//        for ( int i = 0; i < POSSIBLE_SHELL_COMMANDY.length; i++ ) {
//            sb.append( POSSIBLE_SHELL_COMMANDY[i] );
//            if ( i != ( POSSIBLE_SHELL_COMMANDY.length - 1 ) ) {
//                sb.append( "|" );
//            }
//        }
//        sb.append( "] \n" );
//        
//        System.out.println( sb.toString() );
//    }
    
    
    
    private static void showMissing() {
        StringBuffer sb = new StringBuffer();
        sb.append( "Missing options: \n" );
        for ( int i = 0; i < missingFields.size(); i++ ) {
            sb.append( "\t --" + missingFields.get( i ) + "\n" );
        }
        
        System.out.println( sb.toString() );
    }
    
    
    
    private static void showRegisterServerUsage() {
        StringBuffer sb = new StringBuffer();
        sb.append( "Options "+SHELL_COMMAND+"="+SHELL_COMMAND_REGISTER_SERVER+":\n" );
        
        sb.append( "\t --"+SHELL_HOST+"=ox3.1und1.de\n" );
        
        System.out.println( sb.toString() );
    }
    
    
    
    private static void showAddReasonUsage() {
        StringBuffer sb = new StringBuffer();
        sb.append( "Options "+SHELL_COMMAND+"="+SHELL_COMMAND_ADD_REASON+":\n" );
        
        sb.append( "\t --"+SHELL_REASON_TEXT+"=\"Context is disabled for system update.\"\n" );
        
        System.out.println( sb.toString() );
    }
    
    
    
    private static void showListDBMSUsage() {
        StringBuffer sb = new StringBuffer();
        sb.append( "Options "+SHELL_COMMAND+"="+SHELL_COMMAND_LIST_DBMS+":\n" );
        
        sb.append( "\t --"+SHELL_SEARCH_PATTERN+"=*\t\t(default=*)\n" );
        
        System.out.println( sb.toString() );
    }
    
    
    
    private static void showListFilestoreUsage() {
        StringBuffer sb = new StringBuffer();
        sb.append( "Options "+SHELL_COMMAND+"="+SHELL_COMMAND_LIST_FILESTORE+":\n" );
        
        sb.append( "\t --"+SHELL_SEARCH_PATTERN+"=*\t\t(default=*)\n" );
        
        System.out.println( sb.toString() );
    }
    
    
    
    private static void showRegisterDatabaeUsage() {
        StringBuffer sb = new StringBuffer();
        sb.append( "Options "+SHELL_COMMAND+"="+SHELL_COMMAND_REGISTER_DBMS+":\n" );
        
        sb.append( "\t --"+SHELL_DB_NAME+"=dbONE\n" );
        sb.append( "\t --"+SHELL_DB_HOST+"=localhost\n" );
        sb.append( "\t --"+SHELL_DB_USER+"=openexchange\n" );
        sb.append( "\t --"+SHELL_DB_PASSWD+"=secret\n" );
        sb.append( "\t --"+SHELL_DB_IS_MASTER+"=true [true|false]\n" );
        sb.append( "\t --"+SHELL_DB_MASTER_ID+"=15 [id from master]\n" );
        sb.append( "\t --"+SHELL_DB_WEIGHT+"=0.75\n" );
        sb.append( "\t --"+SHELL_DB_MAX_USER+"=10000\n" );
        sb.append( "\t --"+SHELL_DB_PARAM + "=" + SHELL_DEFAULT_DB_PARAM);
        sb.append( "\t --"+SHELL_DB_POOL_HARDLIMIT+"=false [true|false]\n" );
        sb.append( "\t --"+SHELL_DB_POOL_INITIAL+"=0\n" );
        sb.append( "\t --"+SHELL_DB_POOL_MAX+"=0\n" );
        
        System.out.println( sb.toString() );
    }
    
    
    
    private static void showUpdateDatabaeUsage() {
        StringBuffer sb = new StringBuffer();
        sb.append( "Options "+SHELL_COMMAND+"="+SHELL_COMMAND_UPDATE_DBMS+":\n" );
        sb.append( "\t --"+SHELL_ID+"=4\n" );
        
        sb.append( "\t --"+SHELL_DB_NAME+"=dbONE\n" );
        sb.append( "\t --"+SHELL_DB_HOST+"=localhost\n" );
        sb.append( "\t --"+SHELL_DB_USER+"=openexchange\n" );
        sb.append( "\t --"+SHELL_DB_PASSWD+"=secret\n" );
        sb.append( "\t --"+SHELL_DB_IS_MASTER+"=true [true|false]\n" );
        sb.append( "\t --"+SHELL_DB_MASTER_ID+"=15 [id from master]\n" );
        sb.append( "\t --"+SHELL_DB_WEIGHT+"=0.75\n" );
        sb.append( "\t --"+SHELL_DB_MAX_USER+"=10000\n" );
        sb.append( "\t --"+SHELL_DB_PARAM+"=useUnicode=true" +
                "&characterEncoding=UTF-8" +
                "&autoReconnect=true" +
                "&logger=com.mysql.jdbc.log.CommonsLogger" +
                "&dumpQueriesOnException=true" +
                "&connectTimeout=15" +
                "&socketTimeout=15\n" );
        sb.append( "\t --"+SHELL_DB_POOL_HARDLIMIT+"=false [true|false]\n" );
        sb.append( "\t --"+SHELL_DB_POOL_INITIAL+"=0\n" );
        sb.append( "\t --"+SHELL_DB_POOL_MAX+"=0\n" );
        
        System.out.println( sb.toString() );
    }
    
    
    
    private static void showRegisterFilestoreUsage() {
        StringBuffer sb = new StringBuffer();
        sb.append( "Options "+SHELL_COMMAND+"="+SHELL_COMMAND_REGISTER_FILESTORE+":\n" );
        
        sb.append( "\t --"+SHELL_STORE_PATH+"=/mnt/filestore\n" );
        sb.append( "\t --"+SHELL_STORE_SIZE+"=100\t\t(Megabyte)\n" );
        sb.append( "\t --"+SHELL_STORE_MAX_CTX+"=5000\n" );
        
        System.out.println( sb.toString() );
    }
    
    
    
    private static void showEditFilestoreUsage() {
        StringBuffer sb = new StringBuffer();
        sb.append( "Options "+SHELL_COMMAND+"="+SHELL_COMMAND_EDIT_FILESTORE+":\n" );
        
        sb.append( "\t --"+SHELL_ID+"=125\n" );
        sb.append( "\t --"+SHELL_STORE_PATH+"=/tmp/ox/newpath\n" );
        sb.append( "\t --"+SHELL_STORE_SIZE+"=150\t\t(Megabyte)\n" );
        sb.append( "\t --"+SHELL_STORE_MAX_CTX+"=6000\n" );
        
        System.out.println( sb.toString() );
    }
    
    
    
    private static void showUnregisterFilestoreUsage() {
        StringBuffer sb = new StringBuffer();
        sb.append( "Options "+SHELL_COMMAND+"="+SHELL_COMMAND_UNREGISTER_FILESTORE+":\n" );
        
        sb.append( "\t --"+SHELL_ID+"=125\n" );
        
        System.out.println( sb.toString() );
    }
    
    
    
    private static void showUnregisterServerUsage() {
        StringBuffer sb = new StringBuffer();
        sb.append( "Options "+SHELL_COMMAND+"="+SHELL_COMMAND_UNREGISTER_SERVER+":\n" );
        
        sb.append( "\t --"+SHELL_ID+"=3\n" );
        
        System.out.println( sb.toString() );
    }
    
    
    private static void showUnregisterDatabaseUsage() {
        StringBuffer sb = new StringBuffer();
        sb.append( "Options "+SHELL_COMMAND+"="+SHELL_COMMAND_UNREGISTER_DBMS+":\n" );
        
        sb.append( "\t --"+SHELL_ID+"=3\n" );
        
        System.out.println( sb.toString() );
    }
    
    
    
    private static boolean checkNeeded() {
        boolean allFields = true;
        neededFields = new String[0];
        
        if ( command.length() < 1 ) {
            missingFields.add( SHELL_COMMAND );
            allFields = false;
        } else {
            if ( command.equalsIgnoreCase( SHELL_COMMAND_REGISTER_SERVER ) ) {
                String f[] = new String[1];
                f[0] = SHELL_HOST;
                neededFields = f; 
            }
            
            if ( command.equalsIgnoreCase( SHELL_COMMAND_REGISTER_DBMS ) ) {
                String api[] = { DB_MAX_UNITS, DB_CLUSTER_WEIGHT, DB_AUTHENTICATION_ID, DB_AUTHENTICATION_PASSWORD, DB_DISPLAY_NAME};
                String f[] = new String[api.length + 1];
                for ( int i = 0; i < api.length; i++ ) {
                    f[i] = api[i];
                }
                f[ api.length ] = SHELL_DB_IS_MASTER;
                neededFields = f;
            }
            
            if ( command.equalsIgnoreCase( SHELL_COMMAND_REGISTER_FILESTORE ) ) {
                String api[] = { SHELL_STORE_MAX_CTX, SHELL_STORE_PATH, SHELL_STORE_SIZE };
                String f[] = new String[api.length];
                for ( int i = 0; i < api.length; i++ ) {
                    f[i] = api[i];
                }
                neededFields = f;
            }
            
            if ( command.equalsIgnoreCase( SHELL_COMMAND_EDIT_FILESTORE ) ) {
                String api[] = { SHELL_ID };
                String f[] = new String[api.length];
                for ( int i = 0; i < api.length; i++ ) {
                    f[i] = api[i];
                }
                neededFields = f;
            }
            
            if ( command.equalsIgnoreCase( SHELL_COMMAND_UNREGISTER_FILESTORE ) ) {
                String api[] = { SHELL_ID };
                String f[] = new String[api.length];
                for ( int i = 0; i < api.length; i++ ) {
                    f[i] = api[i];
                }
                neededFields = f;
            }
            
        }
        
        for ( int i = 0; i < neededFields.length; i++ ) {
            if ( utilData.size() <= 0 || !utilData.containsKey( neededFields[ i ] ) ) {
                missingFields.add( neededFields[ i ] );
                allFields = false;
            }
        }
        
        return allFields;
    }
    
    
    private void initRMI() throws RemoteException, NotBoundException {
    	if( registry != null ) {
    		registry = null;
    	}
    	if( ox_util != null ) {
    		ox_util = null;
    	}
    	registry = LocateRegistry.getRegistry("localhost");
	    ox_util = (I_OXUtil)registry.lookup(I_OXUtil.RMI_NAME);
    }
    
    public OXUtil() {
        try {
            //ox_util = (I_OXUtil)Naming.lookup( I_OXUtil.RMI_NAME );
        	initRMI();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
        xmlrpc_return   = new Vector();
    }



    public Vector addMaintenanceReason( String reason_txt ) throws RemoteException {
        try {
            xmlrpc_return = ox_util.addMaintenanceReason( reason_txt );
        } catch ( Exception exp ) {
            exp.printStackTrace();
        }
        return xmlrpc_return;
    }



    public Vector changeFilestore( int store_id, Hashtable filestoreData ) throws RemoteException {
        try {
            xmlrpc_return = ox_util.changeFilestore( store_id, filestoreData );
        } catch ( Exception exp ) {
            exp.printStackTrace();
        }
        return xmlrpc_return;
    }



    public Vector createDatabase( Hashtable databaseData ) throws RemoteException {
        try {
            xmlrpc_return = ox_util.createDatabase( databaseData );
        } catch ( Exception exp ) {
            exp.printStackTrace();
        }
        return xmlrpc_return;
    }



    public Vector deleteDatabase( Hashtable databaseData ) throws RemoteException {
        try {
            xmlrpc_return = ox_util.deleteDatabase( databaseData );
        } catch ( Exception exp ) {
            exp.printStackTrace();
        }
        return xmlrpc_return;
    }



    public Vector deleteMaintenanceReason( int reason_id ) throws RemoteException {
        try {
            xmlrpc_return = ox_util.deleteMaintenanceReason( reason_id );
        } catch ( Exception exp ) {
            exp.printStackTrace();
        }
        return xmlrpc_return;
    }



    public Vector getAllMaintenanceReasons() throws RemoteException {
        try {
            xmlrpc_return = ox_util.getAllMaintenanceReasons();
        } catch ( Exception exp ) {
            exp.printStackTrace();
        }
        return xmlrpc_return;
    }



    public Vector getMaintenanceReason( int reason_id ) throws RemoteException {
        try {
            xmlrpc_return = ox_util.getMaintenanceReason( reason_id );
        } catch ( Exception exp ) {
            exp.printStackTrace();
        }
        return xmlrpc_return;
    }



    public Vector listFilestores( String search_pattern ) throws RemoteException {
        try {
            xmlrpc_return = ox_util.listFilestores( search_pattern );
        } catch ( Exception exp ) {
            exp.printStackTrace();
        }
        return xmlrpc_return;
    }



    public Vector registerDatabase( Hashtable databaseData, boolean isMaster, int master_id ) throws RemoteException {
        try {
            xmlrpc_return = ox_util.registerDatabase( databaseData, isMaster, master_id );
        } catch ( Exception exp ) {
            exp.printStackTrace();
        }
        return xmlrpc_return;
    }



    public Vector registerFilestore( String store_URL, long store_size, int store_maxContexts ) throws RemoteException {
        try {
            xmlrpc_return = ox_util.registerFilestore( store_URL, store_size, store_maxContexts );
        } catch ( Exception exp ) {
            exp.printStackTrace();
        }
        return xmlrpc_return;
    }



    public Vector registerServer( String serverName ) throws RemoteException {
        try {
            xmlrpc_return = ox_util.registerServer( serverName );
        } catch ( Exception exp ) {
            exp.printStackTrace();
        }
        return xmlrpc_return;
    }



    public Vector searchForDatabase( String search_pattern ) throws RemoteException {
        try {
            xmlrpc_return = ox_util.searchForDatabase( search_pattern );
        } catch ( Exception exp ) {
            exp.printStackTrace();
        }
        return xmlrpc_return;
    }



    public Vector searchForServer( String search_pattern ) throws RemoteException {
        try {
            xmlrpc_return = ox_util.searchForServer( search_pattern );
        } catch ( Exception exp ) {
            exp.printStackTrace();
        }
        return xmlrpc_return;
    }



    public Vector unregisterDatabase( int database_id ) throws RemoteException {
        try {
            xmlrpc_return = ox_util.unregisterDatabase( database_id );
        } catch ( Exception exp ) {
            exp.printStackTrace();
        }
        return xmlrpc_return;
    }



    public Vector unregisterFilestore( int store_id ) throws RemoteException {
        try {
            xmlrpc_return = ox_util.unregisterFilestore( store_id );
        } catch ( Exception exp ) {
            exp.printStackTrace();
        }
        return xmlrpc_return;
    }



    public Vector unregisterServer( int server_id ) throws RemoteException {
        try {
            xmlrpc_return = ox_util.unregisterServer( server_id );
        } catch ( Exception exp ) {
            exp.printStackTrace();
        }
        return xmlrpc_return;
    }



	public Vector changeDatabase(int database_id, Hashtable databaseData) throws RemoteException {
        try {
            xmlrpc_return = ox_util.changeDatabase(database_id, databaseData);
        } catch ( Exception exp ) {
            exp.printStackTrace();
        }
        return xmlrpc_return;
	}



}
