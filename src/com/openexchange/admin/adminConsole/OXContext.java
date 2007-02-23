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

import com.openexchange.admin.dataSource.I_OXContext;
import com.openexchange.admin.dataSource.I_OXUser;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.tools.DataConverter;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.File;


public class OXContext {
    
    
    private static final String SHELL_COMMAND                   = "command";
    private static final String SHELL_COMMAND_CREATE_CONTEXT    = "create_context";
    private static final String SHELL_COMMAND_LIST_CONTEXT      = "list_context";
    private static final String SHELL_COMMAND_REMOVE_CONTEXT    = "remove_context";
    private static final String SHELL_COMMAND_ENABLE_CONTEXT    = "enable_context";
    
    public static final String SHELL_COMMAND_LIST_CONTEXT_BY_FILESTORE  = "list_context_by_filestore";
    public static final String SHELL_COMMAND_LIST_CONTEXT_BY_DBMS       = "list_context_by_dbms";
    public static final String SHELL_COMMAND_MOVE_FILESTORE         = "move_filestore";
    public static final String SHELL_COMMAND_MOVE_DATABASE          = "move_database";
    
    private static final String SHELL_SEARCH_PATTERN            = "pattern";
    
    
    private static final String SHELL_QUOTA_MAX     = "quotaMax";
    
    public static final String[] POSSIBLE_SHELL_COMMANDY = {
        SHELL_COMMAND_CREATE_CONTEXT,
        SHELL_COMMAND_LIST_CONTEXT,
        SHELL_COMMAND_ENABLE_CONTEXT,
        SHELL_COMMAND_REMOVE_CONTEXT,
        SHELL_COMMAND_MOVE_FILESTORE,
        SHELL_COMMAND_MOVE_DATABASE,
        SHELL_COMMAND_LIST_CONTEXT_BY_FILESTORE,
        SHELL_COMMAND_LIST_CONTEXT_BY_DBMS
    };
    
    public static final String SHELL_ID                 = "ID";
    public static final String SHELL_REASON_ID          = "REASON_ID";
    public static final String SHELL_STORE_ID           = "STORE_ID";
    public static final String SHELL_DATABASE_ID        = "DATABASE_ID";

    private static Hashtable    contextData     = null;
    private static String       command         = "";
    
    private static Vector       xmlrpc_return   = null;
    private static String[]     neededFields    = null;
    
    private static OXContextInterface  ox_context      = null;
    private Registry            registry        = null;

    
    private static Vector<String>       missingFields       = null;
    
    
    
    public static void main( String args[] ) {
    	OXContext c = new OXContext();
    	try {
			c.init();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
    	
    	contextData = new Hashtable();
        missingFields = new Vector<String>();
        
        contextData = AdminConsoleTools.parseInput( args, SHELL_COMMAND );
        if ( contextData != null && contextData.containsKey( SHELL_COMMAND ) ) {
            command = contextData.get( SHELL_COMMAND ).toString();
            contextData.remove( SHELL_COMMAND );
        }

        //TODO: need super-admin concept for context creation, etc
        Credentials cred = new Credentials("foo","bar");

        int context_id = 0;
        if ( !command.equals( SHELL_COMMAND_LIST_CONTEXT_BY_DBMS ) 
                && !command.equals( SHELL_COMMAND_LIST_CONTEXT_BY_FILESTORE )
                && !command.equals( SHELL_COMMAND_LIST_CONTEXT ) ) {
            // enableAllContexts()
            // disableAllContexts()
            
            if ( contextData.containsKey( I_OXContext.CONTEXT_ID ) ) {
                String s_id = contextData.get( I_OXContext.CONTEXT_ID ).toString();
                try {
                    Integer i_id = new Integer( s_id );
                    context_id = i_id.intValue();
                } catch ( NumberFormatException nfe ) {
                    missingFields.add( I_OXContext.CONTEXT_ID );
                }
            } else {
                missingFields.add( I_OXContext.CONTEXT_ID );
            }
        }
        Context ctx = new Context(context_id);
        
        if ( checkNeeded() ) {
            
            if ( command.equals( SHELL_COMMAND_LIST_CONTEXT_BY_FILESTORE ) ) {
                String path = "";
                if ( contextData.containsKey( OXUtil.SHELL_STORE_PATH ) ) {
                    path = contextData.get( OXUtil.SHELL_STORE_PATH ).toString();
                    File f_path = new File( path );
                    path = f_path.getPath();
                } else {
                    showListContextByFilestoreUsage();
                    System.exit( 1 );
                }
                
                Filestore fstore = new Filestore();
                fstore.setUrl("%"+path+"%");
                Context[] ret = null;
				try {
					ret = ox_context.searchByFilestore(fstore, cred);
				} catch (RemoteException e) {
					c.doExit(e);
				} catch (StorageException e) {
					c.doExit(e);
				} catch (InvalidCredentialsException e) {
					c.doExit(e);
				} catch (InvalidDataException e) {
					c.doExit(e);
				}
                
                System.out.println( "Context_ID" );
                    
                for(Context ci : ret ) {
                	System.out.println(ci.getIdAsString());
                }
                if( ret.length == 0 ) {
                    System.out.println( "none" );
                }
                
            } else if ( command.equals( SHELL_COMMAND_ENABLE_CONTEXT ) ) {
                if ( contextData.containsKey( I_OXContext.CONTEXT_ID ) ) {
                    Integer i = new Integer( contextData.get( I_OXContext.CONTEXT_ID ).toString() );
                    context_id = i.intValue();
                } else {
                    showEnableContextUsage();
                    System.exit( 1 );
                }
                
                try {
					ox_context.enable(ctx, cred);
				} catch (RemoteException e) {
					c.doExit(e);
				} catch (InvalidCredentialsException e) {
					c.doExit(e);
				} catch (NoSuchContextException e) {
					c.doExit(e);
				} catch (StorageException e) {
					c.doExit(e);
				}
                System.out.println( "Context (ID="+context_id+") enabled." );
                
            } else if ( command.equals( SHELL_COMMAND_MOVE_FILESTORE ) ) {
                    int store_id = 0;
                    int reason_id = 0;
                    
                    if ( contextData.containsKey( SHELL_REASON_ID ) ) {
                        Integer i = new Integer( contextData.get( SHELL_REASON_ID ).toString() );
                        reason_id = i.intValue();
                    } else {
                        showMoveFilestoreUsage();
                        System.exit( 1 );
                    }
                    
                    if ( contextData.containsKey( SHELL_STORE_ID ) ) {
                        Integer i = new Integer( contextData.get( SHELL_STORE_ID ).toString() );
                        store_id = i.intValue();
                    } else {
                        showMoveFilestoreUsage();
                        System.exit( 1 );
                    }
                    
                    if ( contextData.containsKey( I_OXContext.CONTEXT_ID ) ) {
                        Integer i = new Integer( contextData.get( I_OXContext.CONTEXT_ID ).toString() );
                        context_id = i.intValue();
                    } else {
                        showMoveFilestoreUsage();
                        System.exit( 1 );
                    }
                    
                    try {
						ox_context.moveContextFilestore(ctx, new Filestore(store_id), new MaintenanceReason(reason_id), cred);
					} catch (RemoteException e) {
						c.doExit(e);
					} catch (InvalidCredentialsException e) {
						c.doExit(e);
					} catch (NoSuchContextException e) {
						c.doExit(e);
					} catch (StorageException e) {
						c.doExit(e);
					} catch (InvalidDataException e) {
						c.doExit(e);
					}
                    System.out.println( "Moving store " + store_id + "." );
                
            } else if ( command.equals( SHELL_COMMAND_MOVE_DATABASE ) ) {
                int target_db_id = 0;
                int reason_id = 0;
                
                if ( contextData.containsKey( SHELL_REASON_ID ) ) {
                    Integer i = new Integer( contextData.get( SHELL_REASON_ID ).toString() );
                    reason_id = i.intValue();
                } else {
                    showMoveDatabaseUsage();
                    System.exit( 1 );
                }
                
                if ( contextData.containsKey( SHELL_DATABASE_ID ) ) {
                    Integer i = new Integer( contextData.get( SHELL_DATABASE_ID ).toString() );
                    target_db_id = i.intValue();
                } else {
                    showMoveDatabaseUsage();
                    System.exit( 1 );
                }
                
                if ( contextData.containsKey( I_OXContext.CONTEXT_ID ) ) {
                    Integer i = new Integer( contextData.get( I_OXContext.CONTEXT_ID ).toString() );
                    context_id = i.intValue();
                } else {
                    showMoveDatabaseUsage();
                    System.exit( 1 );
                }
                
                try {
					ox_context.moveContextDatabase(ctx, new Database(target_db_id), new MaintenanceReason(reason_id), cred);
				} catch (RemoteException e) {
					c.doExit(e);
				} catch (InvalidCredentialsException e) {
					c.doExit(e);
				} catch (NoSuchContextException e) {
					c.doExit(e);
				} catch (StorageException e) {
					c.doExit(e);
				} catch (InvalidDataException e) {
					c.doExit(e);
				}
                System.out.println( "Moving database " + target_db_id + "." );
            
            } else if ( command.equals( SHELL_COMMAND_LIST_CONTEXT_BY_DBMS ) ) {
                String url = "";
                if ( contextData.containsKey( OXUtil.SHELL_DB_HOST ) ) {
                    url = contextData.get( OXUtil.SHELL_DB_HOST ).toString();
                } else {
                    showListContextByDBMSUsage();
                    System.exit( 1 );
                }
                Database db = new Database();
                db.setUrl("%"+url+"%");
                Context[] ret = null;
				try {
					ret = ox_context.searchByDatabase(db, cred);
				} catch (RemoteException e) {
					c.doExit(e);
				} catch (StorageException e) {
					c.doExit(e);
				} catch (InvalidCredentialsException e) {
					c.doExit(e);
				} catch (InvalidDataException e) {
					c.doExit(e);
				}
                
                for( Context cs : ret ) {
                	System.out.println(cs.getIdAsString());
                }
                
                if ( ret.length == 0 ) {
                	System.out.println( "none" );
                }
                    
            } else if ( command.equals( SHELL_COMMAND_LIST_CONTEXT ) ) {
                String pattern = I_OXContext.PATTERN_SEARCH_ALL_CONTEXT;
                if ( contextData.containsKey( SHELL_SEARCH_PATTERN ) ) {
                    pattern = contextData.get( SHELL_SEARCH_PATTERN ).toString();
                }
                
                Context[] ret = null;
				try {
					ret = ox_context.search(pattern, cred);
				} catch (RemoteException e) {
					c.doExit(e);
				} catch (StorageException e) {
					c.doExit(e);
				} catch (InvalidCredentialsException e) {
					c.doExit(e);
				} catch (InvalidDataException e) {
					c.doExit(e);
				}
                System.out.println( "Context_ID" );
                for( Context cs : ret ) {
                	System.out.println(cs.getIdAsString());
                }
            } else if ( command.equals( SHELL_COMMAND_REMOVE_CONTEXT ) ) {
                
            	try {
					ox_context.delete(ctx, cred);
				} catch (RemoteException e) {
					c.doExit(e);
				} catch (InvalidCredentialsException e) {
					c.doExit(e);
				} catch (NoSuchContextException e) {
					c.doExit(e);
				} catch (StorageException e) {
					c.doExit(e);
				}
            	System.out.println( "Context with ID="+context_id+" removed." );

            } else if ( command.equals( SHELL_COMMAND_CREATE_CONTEXT ) ) {
                
                Hashtable<String, Comparable> user_container = new Hashtable<String, Comparable>();
                long quota_max = 0;
                
                if ( contextData.containsKey( SHELL_QUOTA_MAX ) ) {
                    String s_quota = contextData.get( SHELL_QUOTA_MAX ).toString();
                    Long l_quota = new Long( s_quota );
                    quota_max = l_quota.longValue();
                } else {
                    missingFields.add( SHELL_QUOTA_MAX );
                }
                
                if ( contextData.containsKey( I_OXUser.UID ) ) {
                    String s = contextData.get( I_OXUser.UID ).toString();
                    user_container.put( I_OXUser.UID, s );
                } else {
                    missingFields.add( I_OXUser.UID );
                }
                
                if ( contextData.containsKey( I_OXUser.PASSWORD ) ) {
                    String s = contextData.get( I_OXUser.PASSWORD ).toString();
                    user_container.put( I_OXUser.PASSWORD, s );
                } else {
                    missingFields.add( I_OXUser.PASSWORD );
                }
                
                if ( contextData.containsKey( I_OXUser.PRIMARY_MAIL ) ) {
                    String s = contextData.get( I_OXUser.PRIMARY_MAIL ).toString();
                    user_container.put( I_OXUser.PRIMARY_MAIL, s );
                    // Bug  5444
                    user_container.put( I_OXUser.EMAIL1, s );
                } else {
                    missingFields.add( I_OXUser.PRIMARY_MAIL );
                }
                
                
                if ( contextData.containsKey( I_OXUser.GIVEN_NAME ) ) {
                    String s = contextData.get( I_OXUser.GIVEN_NAME ).toString();
                    user_container.put( I_OXUser.GIVEN_NAME, s );
                }
                
                if ( contextData.containsKey( I_OXUser.SUR_NAME ) ) {
                    String s = contextData.get( I_OXUser.SUR_NAME ).toString();
                    user_container.put( I_OXUser.SUR_NAME, s );
                }
                
                if ( contextData.containsKey( I_OXUser.DISPLAY_NAME ) ) {
                    String s = contextData.get( I_OXUser.DISPLAY_NAME ).toString();
                    user_container.put( I_OXUser.DISPLAY_NAME, s );
                }
                
                
                if ( missingFields.size() > 0 ) {
                    showCreateContextUsage();
                    showMissing();
                    System.exit( 1 );
                }
                
                User admin_user = DataConverter.userHashtable2UserObject(user_container);
                try {
					ox_context.create(ctx, admin_user, quota_max, cred);
				} catch (RemoteException e) {
					c.doExit(e);
				} catch (StorageException e) {
					c.doExit(e);
				} catch (InvalidCredentialsException e) {
					c.doExit(e);
				} catch (InvalidDataException e) {
					c.doExit(e);
				}
                System.out.println( "New context "+context_id+" added." );
            } else {
                showUsage();
            }
            
            // Need for debug!
            //System.out.println( xmlrpc_return );
        } else {
            showUsage();
            showMissing();
        }
        
    }
    
    
    
    private static void showMissing() {
        StringBuffer sb = new StringBuffer();
        sb.append( "Missing options: \n" );
        for ( int i = 0; i < missingFields.size(); i++ ) {
            sb.append( "\t --" + missingFields.get( i ) + "\n" );
        }
        
        System.out.println( sb.toString() );
    }
    
    
    
    private static boolean checkNeeded() {
        boolean allFields = true;
        neededFields = new String[0];
        
        if ( command.length() < 1 ) {
            missingFields.add( SHELL_COMMAND );
            allFields = false;
        } else {
            
            if ( command.equalsIgnoreCase( SHELL_COMMAND_LIST_CONTEXT_BY_FILESTORE ) ) {
                String f[] = new String[1];
                f[0] = OXUtil.SHELL_STORE_PATH;
                neededFields = f; 
            }
            
            if ( command.equalsIgnoreCase( SHELL_COMMAND_LIST_CONTEXT_BY_DBMS ) ) {
                String f[] = new String[1];
                f[0] = OXUtil.SHELL_DB_HOST;
                neededFields = f; 
            }
            
            if ( command.equalsIgnoreCase( SHELL_COMMAND_REMOVE_CONTEXT ) ) {
                String f[] = new String[1];
                f[0] = I_OXContext.CONTEXT_ID;
                neededFields = f; 
            }
            
            if ( command.equalsIgnoreCase( SHELL_COMMAND_CREATE_CONTEXT ) ) {
                String api[] = I_OXContext.REQUIRED_KEYS_CREATE;
                String f[] = new String[api.length];
                for ( int i = 0; i < api.length; i++ ) {
                    f[i] = api[i];
                }
                neededFields = f;
            }
            
        }
        
        for ( int i = 0; i < neededFields.length; i++ ) {
            if ( contextData.size() <= 0 || !contextData.containsKey( neededFields[ i ] ) ) {
                if ( !missingFields.contains( neededFields[ i ] )) {
                    missingFields.add( neededFields[ i ] );
                }
                allFields = false;
            }
        }
        
        return allFields;
    }
    
    
    
    private static void showUsage(){
        if ( command != null && command.length() > 1 ) {
            if ( command.equals( SHELL_COMMAND_LIST_CONTEXT_BY_FILESTORE ) ) {
                showListContextByFilestoreUsage();
            } else if ( command.equals( SHELL_COMMAND_LIST_CONTEXT_BY_DBMS ) ) {
                showListContextByDBMSUsage();
            } else if ( command.equals( SHELL_COMMAND_LIST_CONTEXT ) ) {
                showListContextUsage();
            } else if ( command.equals( SHELL_COMMAND_CREATE_CONTEXT ) ) {
                showCreateContextUsage();
            } else if ( command.equals( SHELL_COMMAND_REMOVE_CONTEXT ) ) {
                showRemoveContextUsage();
            } else if ( command.equals( SHELL_COMMAND_MOVE_FILESTORE ) ) {
                showMoveFilestoreUsage();
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
    
    
    
    private static void showCreateContextUsage() {
        StringBuffer sb = new StringBuffer();
        sb.append( "Options "+SHELL_COMMAND+"="+SHELL_COMMAND_CREATE_CONTEXT+":\n" );
        
        sb.append( "\t --"+I_OXContext.CONTEXT_ID+"=112\n" );
        sb.append( "\t --"+OXContext.SHELL_QUOTA_MAX+"=5000\n" );
        sb.append( "\t --"+I_OXUser.UID+"=admin\n" );
        sb.append( "\t --"+I_OXUser.PASSWORD+"=adminPasswd\n" );
        sb.append( "\t --"+I_OXUser.PRIMARY_MAIL+"=admin@local-domain.net\n" );
        sb.append( "\t --"+I_OXUser.GIVEN_NAME+"=John\n" );
        sb.append( "\t --"+I_OXUser.SUR_NAME+"=Doe\n" );
        sb.append( "\t --"+I_OXUser.DISPLAY_NAME+"=\"This bofh is watching you\"\n" );
        
        System.out.println( sb.toString() );
    }
    
    
    
    private static void showMoveFilestoreUsage() {
        StringBuffer sb = new StringBuffer();
        sb.append( "Options "+SHELL_COMMAND+"="+SHELL_COMMAND_MOVE_FILESTORE+":\n" );
        
        sb.append( "\t --"+I_OXContext.CONTEXT_ID+"=12\n" );
        sb.append( "\t --"+SHELL_REASON_ID+"=190\n" );
        sb.append( "\t --"+SHELL_STORE_ID+"=125\n" );
        
        System.out.println( sb.toString() );
    }
    

    private static void showMoveDatabaseUsage() {
        StringBuffer sb = new StringBuffer();
        sb.append( "Options "+SHELL_COMMAND+"="+SHELL_COMMAND_MOVE_DATABASE+":\n" );
        
        sb.append( "\t --"+I_OXContext.CONTEXT_ID+"=12\n" );
        sb.append( "\t --"+SHELL_REASON_ID+"=190\n" );
        sb.append( "\t --"+SHELL_DATABASE_ID+"=125\n" );
        
        System.out.println( sb.toString() );
    }
    
    
    private static void showRemoveContextUsage() {
        StringBuffer sb = new StringBuffer();
        sb.append( "Options "+SHELL_COMMAND+"="+SHELL_COMMAND_REMOVE_CONTEXT+":\n" );
        
        sb.append( "\t --"+I_OXContext.CONTEXT_ID+"=112\n" );
        
        System.out.println( sb.toString() );
    }
    
    
    
    private static void showEnableContextUsage() {
        StringBuffer sb = new StringBuffer();
        sb.append( "Options "+SHELL_COMMAND+"="+SHELL_COMMAND_ENABLE_CONTEXT+":\n" );
        
        sb.append( "\t --"+I_OXContext.CONTEXT_ID+"=112\n" );
        
        System.out.println( sb.toString() );
    }
    
    
    
    private static void showListContextByFilestoreUsage() {
        StringBuffer sb = new StringBuffer();
        sb.append( "Options "+SHELL_COMMAND+"="+SHELL_COMMAND_LIST_CONTEXT_BY_FILESTORE+":\n" );
        
        sb.append( "\t --"+OXUtil.SHELL_STORE_PATH+"=/ox/path\n" );
        
        System.out.println( sb.toString() );
    }
    
    
    
    private static void showListContextByDBMSUsage() {
        StringBuffer sb = new StringBuffer();
        sb.append( "Options "+SHELL_COMMAND+"="+SHELL_COMMAND_LIST_CONTEXT_BY_DBMS+":\n" );
        
        sb.append( "\t --"+OXUtil.SHELL_DB_HOST+"=ox3.1und1.de\n" );
        
        System.out.println( sb.toString() );
    }
    
    
    
    private static void showListContextUsage() {
        StringBuffer sb = new StringBuffer();
        sb.append( "Options "+SHELL_COMMAND+"="+SHELL_COMMAND_LIST_CONTEXT+":\n" );
        
        sb.append( "\t --"+SHELL_SEARCH_PATTERN+"="+I_OXContext.PATTERN_SEARCH_ALL_CONTEXT+"\n" );
        
        System.out.println( sb.toString() );
    }
    
    private void doExit(Exception e) {
    	System.out.println(e);
    	System.exit(1);
    }

    private void init() throws RemoteException, NotBoundException {
    	registry = LocateRegistry.getRegistry("localhost");
	    ox_context = (OXContextInterface)registry.lookup(OXContextInterface.RMI_NAME);
    }
    
    public OXContext() {
    }

}