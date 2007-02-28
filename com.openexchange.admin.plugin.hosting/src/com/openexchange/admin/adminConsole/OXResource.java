///*
// *
// *    OPEN-XCHANGE legal information
// *
// *    All intellectual property rights in the Software are protected by
// *    international copyright laws.
// *
// *
// *    In some countries OX, OX Open-Xchange, open xchange and OXtender
// *    as well as the corresponding Logos OX Open-Xchange and OX are registered
// *    trademarks of the Open-Xchange, Inc. group of companies.
// *    The use of the Logos is not covered by the GNU General Public License.
// *    Instead, you are allowed to use these Logos according to the terms and
// *    conditions of the Creative Commons License, Version 2.5, Attribution,
// *    Non-commercial, ShareAlike, and the interpretation of the term
// *    Non-commercial applicable to the aforementioned license is published
// *    on the web site http://www.open-xchange.com/EN/legal/index.html.
// *
// *    Please make sure that third-party modules and libraries are used
// *    according to their respective licenses.
// *
// *    Any modifications to this package must retain all copyright notices
// *    of the original copyright holder(s) for the original code used.
// *
// *    After any such modifications, the original and derivative code shall remain
// *    under the copyright of the copyright holder(s) and/or original author(s)per
// *    the Attribution and Assignment Agreement that can be located at
// *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
// *    given Attribution for the derivative code and a license granting use.
// *
// *     Copyright (C) 2004-2006 Open-Xchange, Inc.
// *     Mail: info@open-xchange.com
// *
// *
// *     This program is free software; you can redistribute it and/or modify it
// *     under the terms of the GNU General Public License, Version 2 as published
// *     by the Free Software Foundation.
// *
// *     This program is distributed in the hope that it will be useful, but
// *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
// *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
// *     for more details.
// *
// *     You should have received a copy of the GNU General Public License along
// *     with this program; if not, write to the Free Software Foundation, Inc., 59
// *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
// *
// */
//package com.openexchange.admin.adminConsole;
//
//
//import java.rmi.NotBoundException;
//import java.rmi.RemoteException;
//import java.rmi.registry.LocateRegistry;
//import java.rmi.registry.Registry;
//import java.util.Hashtable;
//import java.util.StringTokenizer;
//import java.util.Vector;
//
//import com.openexchange.admin.dataSource.I_OXContext;
//import com.openexchange.admin.dataSource.I_OXResource;
//
//
//public class OXResource implements I_OXResource {
//    
//    public static final String SHELL_COMMAND            = "command";
//    public static final String SHELL_COMMAND_CREATE     = "create";
//    public static final String SHELL_COMMAND_LIST       = "list";
//    public static final String SHELL_COMMAND_SHOW       = "show";
//    public static final String SHELL_COMMAND_DELETE     = "delete";
//    public static final String SHELL_COMMAND_EDIT       = "update";
//    
//    public static final String SHELL_CONTEXT_ID             = "context_id";
//    public static final String SHELL_RID_NUMBER             = I_OXResource.RID_NUMBER;
//    public static final String SHELL_RESOURCE_NAME          = I_OXResource.RID;
//    public static final String SHELL_RESOURCE_DISPLAY_NAME  = I_OXResource.DISPLAYNAME;
//    public static final String SHELL_RESOURCE_AVAILABLE     = I_OXResource.AVAILABLE;
//    public static final String SHELL_SEARCH_PATTERN         = "pattern";
//    public static final String SHELL_SSL                    = "ssl";
//    
//    private static Hashtable<String, Object>    resData     = null;
//    private static String       command     = "";
//    
//    private static Vector       xmlrpc_return       = null;
//    private static String[]     neededFields        = null;
//    private static Vector<String>       missingFields       = null;
//    private static I_OXResource        ox_resource         = null;
//    private Registry            registry            = null;
//    
//    
//    
//    public static void main( String args[] ) throws Exception {
//        resData = new Hashtable<String, Object>();
//        missingFields = new Vector<String>();
//        
//        parseInput( args );
//        if ( checkNeeded() ) {
//            OXResource ox_res = new OXResource();
//            
//            if ( command.equals( SHELL_COMMAND_CREATE ) ) {
//                ox_res.createOXResource(resData );
//            } else if ( command.equals( SHELL_COMMAND_LIST ) ) {
//                ox_res.listResources();
//            } else if ( command.equals( SHELL_COMMAND_DELETE ) ) {
//                ox_res.deleteResource();
//            } else if ( command.equals( SHELL_COMMAND_EDIT ) ){
//                ox_res.changeResource();
//            } else {
//                showUsage();
//            }
//                        
//        } else {
//            showUsage();
//            showMissing();
//        }
//    }
//    
//    
//    
//    private static void showUsage(){
//        if ( command != null && command.length() > 1 ) {
//            if ( command.equals( SHELL_COMMAND_CREATE ) ) {
//                showCreateUsage();
//            } else if ( command.equals( SHELL_COMMAND_LIST ) ) {
//                showListUsage();
//            } else if ( command.equals( SHELL_COMMAND_DELETE ) ) {
//                showDeleteUsage();
//            } else if( command.equals( SHELL_COMMAND_EDIT ) ) {
//                showEditUsage();
//            } else {
//                showGeneralUsages();
//            }
//        } else {
//            showGeneralUsages();
//        }
//    }
//    
//    
//    
//    private static void showGeneralUsages() {
//        StringBuffer sb = new StringBuffer();
////        sb.append( "\n" );
////        sb.append( "Options: \n" );
////        sb.append( "\t --"+SHELL_COMMAND+"=["+SHELL_COMMAND_CREATE+"|"+SHELL_COMMAND_DELETE+"|"+SHELL_COMMAND_EDIT+"|"+SHELL_COMMAND_LIST+"|"+SHELL_COMMAND_SHOW+"] \n" );
////        sb.append( "\t --"+SHELL_SSL+"=true|false\t(defaul=false)\n" );
//        
//        p2c( sb.toString() );
//    }
//    
//    
//    
//    private static void showMissing() {
//        StringBuffer sb = new StringBuffer();
//        sb.append( "Missing options: \n" );
//        for ( int i = 0; i < missingFields.size(); i++ ) {
//            sb.append( "\t --" + missingFields.get( i ) + "\n" );
//        }
//        
//        p2c( sb.toString() );
//    }
//    
//    
//    
//	private static void parseInput( String args[] ) {
//        String param = null;
//        
//        for ( int a = 0; a < args.length; a++ ) {
//            param = args[a];
//            try {
//                for ( int i = 1; i < args.length; i++ ) {
//                    if ( !args[a+i].startsWith( "--" ) ) {
//                        param += " " + args[a+i];
//                    } else {
//                        break;
//                    }
//                }
//            } catch ( Exception e ) {
//                //nothing
//            }
//            if ( param.startsWith( "--"+SHELL_COMMAND+"=" ) ) {
//                StringTokenizer st = new StringTokenizer( param, "=" );
//                if ( st.countTokens() == 2 ) {
//                    st.nextToken();
//                    command = st.nextToken().toLowerCase();
//                }
//            } else {
//                if ( param.startsWith( "--" ) && param.indexOf( "=" ) != -1 ) {
//                    int pos = param.indexOf( "=" );
//                    String paramNAME = param.substring( 2, pos );
//                    StringTokenizer st = new StringTokenizer( param, "=" );
//                    if ( st.countTokens() == 2 ) {
//                        st.nextToken();
//                        String value = st.nextToken();
//                        if ( value.equalsIgnoreCase( "true" ) || value.equalsIgnoreCase( "false" ) ) {
//                            resData.put( paramNAME, new Boolean( value ) );
//                        } else {
//                            resData.put( paramNAME, value );
//                        }
//                    }
//                }
//            }
//        }
//    }
//    
//    
//    
//    private static void showCreateUsage() {
//        showGeneralUsages();
//        
//        StringBuffer sb = new StringBuffer();
//        sb.append( "Options:\n" );
//        
//        sb.append( "\t --"+SHELL_CONTEXT_ID+"=2\n" );
//        sb.append( "\t --"+SHELL_RESOURCE_AVAILABLE+"=true\n" );
//        sb.append( "\t --"+SHELL_RESOURCE_NAME+"=porsch911red\n" );
//        sb.append( "\t --"+SHELL_RESOURCE_DISPLAY_NAME+"=\"Porsche 911 (color: Red)\"\n" );
//        sb.append( "\t --"+PRIMARY_MAIL+"=tt@aa.de\n" );
//        
////        sb.append( "\t --attribute1=attribute_value1\n" );
////        sb.append( "\t --attribute2=attribute_value2\n" );
////        sb.append( "\t --....\n" );
//        
//        p2c( sb.toString() );
//    }
//    
//    
//    
//    private static void showListUsage() {
//        showGeneralUsages();
//        
//        StringBuffer sb = new StringBuffer();
//        sb.append( "Options:\n" );
//        
//        sb.append( "\t --"+SHELL_CONTEXT_ID+"=2\n" );
//        sb.append( "\t --"+SHELL_SEARCH_PATTERN+"=*\n" );
//        
//        p2c( sb.toString() );
//    }
//    
//    
//    
//    private static void showEditUsage() {
//        showGeneralUsages();
//        
//        StringBuffer sb = new StringBuffer();
//        sb.append( "Options:\n" );
//        
//        sb.append( "\t --"+SHELL_CONTEXT_ID+"=2\n" );
//        sb.append( "\t --"+SHELL_RID_NUMBER+"=12\n" );
//        sb.append( "\t --"+SHELL_RESOURCE_NAME+"=devel\n" );
//        sb.append( "\t --"+SHELL_RESOURCE_DISPLAY_NAME+"=\"Development Team\"\n" );
//        sb.append( "\t --"+SHELL_RESOURCE_AVAILABLE+"=true\n" );
//        sb.append( "\t --"+PRIMARY_MAIL+"=tt@aa.de\n" );
////        sb.append( "\t --attribute1=attribute_value1\n" );
////        sb.append( "\t --attribute2=attribute_value2\n" );
////        sb.append( "\t --....\n" );
//        
//        p2c( sb.toString() );
//    }
//    
//    
//    
//    private static void showDeleteUsage() {
//        showGeneralUsages();
//        
//        StringBuffer sb = new StringBuffer();
//        sb.append( "Options:\n" );
//        
//        sb.append( "\t --"+SHELL_CONTEXT_ID+"=2\n" );
//        sb.append( "\t --"+SHELL_RID_NUMBER+"=12\n" );
//        
//        p2c( sb.toString() );
//    }
//    
//    
//    
//	private static boolean checkNeeded() {
//        boolean allFields = true;
//        neededFields = new String[1];
//        neededFields[0] = SHELL_CONTEXT_ID;
//        
//        if ( command.length() < 1 ) {
//            missingFields.add( SHELL_COMMAND );
//            allFields = false;
//        } else {
//            if ( command.equalsIgnoreCase( SHELL_COMMAND_CREATE ) ) {
//                String api[] = I_OXResource.REQUIRED_KEYS_CREATE;
//                String f[] = new String[api.length + 1];
//                f[0] = neededFields[0];
//                for ( int i = 0; i < api.length; i++ ) {
//                    f[i+1] = api[i];
//                }
//                neededFields = f;
//            }
//            
//            if ( command.equalsIgnoreCase( SHELL_COMMAND_LIST ) ) {
//                String f[] = new String[2];
//                f[0] = neededFields[0];
//                f[1] = SHELL_SEARCH_PATTERN;
//                neededFields = f; 
//            }
//            
//            if ( command.equalsIgnoreCase( SHELL_COMMAND_EDIT ) ) {
//                String api[] = {I_OXResource.RID_NUMBER};
//                String f[] = new String[api.length + 1];
//                f[0] = neededFields[0];
//                for ( int i = 0; i < api.length; i++ ) {
//                    f[i+1] = api[i];
//                }
//                neededFields = f;
//            }
//            
//            if ( command.equalsIgnoreCase( SHELL_COMMAND_DELETE ) ) {
//                String f[] = new String[2];
//                f[0] = neededFields[0];
//                f[1] = SHELL_RID_NUMBER;
//                neededFields = f; 
//            }
//            
//        }
//        
//        for ( int i = 0; i < neededFields.length; i++ ) {
//            if ( !resData.containsKey( neededFields[ i ] ) ) {
//                missingFields.add( neededFields[ i ] );
//                allFields = false;
//            }
//        }
//        
//        return allFields;
//    }
//    
//    
//    
//    
//    private void listResources() throws Exception {
//        try {
//            xmlrpc_return = ox_resource.listOXResources( Integer.parseInt(resData.get( SHELL_CONTEXT_ID ).toString()), resData.get( SHELL_SEARCH_PATTERN ).toString() );
//            
//            if ( xmlrpc_return.get( 0 ).toString().toLowerCase().equals( "ok" ) ) {
//                if ( xmlrpc_return.size() > 1 ) {
//                    // show result
//                    
//                    Vector alldata = (Vector)xmlrpc_return.get(1);
//                    
//                    p2c("Context-id|Resource-id|Identifier|Displayname|Mail|Description\n");
//                    for(int a = 0;a<alldata.size();a++){
//                        Hashtable dd = (Hashtable)alldata.get(a);
//                        String mail = "";
//                        if(dd.get(I_OXResource.PRIMARY_MAIL)!=null){
//                            mail = ""+dd.get(I_OXResource.PRIMARY_MAIL);
//                        }
//                        String desc = "";
//                        if(dd.get(I_OXResource.DESCRIPTION)!=null){
//                            desc = ""+dd.get(I_OXResource.DESCRIPTION);
//                        }
//                        String disp = "";
//                        if(dd.get(I_OXResource.DISPLAYNAME)!=null){
//                            disp = ""+dd.get(I_OXResource.DISPLAYNAME);
//                        }
//                        p2c(""+dd.get(I_OXContext.CONTEXT_ID)+"|"+dd.get(I_OXResource.RID_NUMBER)+"|"+dd.get(I_OXResource.RID)+"|"+disp+"|"+mail+"|"+desc);
//                    }
//                } else {
//                    p2c( xmlrpc_return.get( 0 ) );
//                }
//            } else {
//                p2c(xmlrpc_return.get(1).toString());
//            }
//            
//            
//        }catch(java.lang.NumberFormatException nfe){
//            p2c("Invalid id(s)");
//        } catch ( Exception exp ) {
//            exp.printStackTrace();
//        }
//    }
//    
//    
//    
//    private void changeResource() throws Exception {
//        try {
//            int ctx_id = Integer.parseInt(resData.get( SHELL_CONTEXT_ID ).toString());
//            resData.remove(SHELL_CONTEXT_ID);
//            xmlrpc_return = ox_resource.changeOXResource(ctx_id ,Integer.parseInt(resData.get( SHELL_RID_NUMBER ).toString()),resData );
//            
//            if ( xmlrpc_return.get( 0 ).toString().toLowerCase().equals( "ok" ) ) {
//                if ( xmlrpc_return.size() > 1 ) {
//                    for ( int i = 1; i < xmlrpc_return.size(); i++ ) {
//                        p2c( xmlrpc_return.get( i ) );
//                    }
//                } else {
//                    p2c( xmlrpc_return.get( 0 ) );
//                }
//            } else {
//                p2c(xmlrpc_return.get( 1 ).toString());
//            }
//            
//            
//        }catch(java.lang.NumberFormatException nfe){
//            p2c("Invalid id(s)");
//        } catch ( Exception exp ) {
//            exp.printStackTrace();
//        }
//    }
//    
//    
//    
//    private void deleteResource() throws Exception {
//        try {
//            xmlrpc_return = ox_resource.deleteOXResource( Integer.parseInt(resData.get( SHELL_CONTEXT_ID ).toString()), Integer.parseInt(resData.get( SHELL_RID_NUMBER ).toString()) );
//            
//            if ( xmlrpc_return.get( 0 ).toString().toLowerCase().equals( "ok" ) ) {
//                if ( xmlrpc_return.size() > 1 ) {
//                    for ( int i = 1; i < xmlrpc_return.size(); i++ ) {
//                        p2c( xmlrpc_return.get( i ) );
//                    }
//                } else {
//                    p2c( xmlrpc_return.get( 0 ) );
//                }
//            } else {
//                p2c(xmlrpc_return.get( 1 ).toString());
//            }
//            
//        }catch(java.lang.NumberFormatException nfe){
//            p2c("Invalid id(s)");
//        } catch ( Exception exp ) {
//            exp.printStackTrace();
//        }
//    }
//    
//    private void initRMI() throws RemoteException, NotBoundException {
//    	if( registry != null ) {
//    		registry = null;
//    	}
//    	if( ox_resource != null ) {
//    		ox_resource = null;
//    	}
//    	registry = LocateRegistry.getRegistry("localhost");
//	    ox_resource = (I_OXResource)registry.lookup(I_OXResource.RMI_NAME);
//    }
//
//    
//    public OXResource() {
//        try {
//            //ox_resource = (I_OXResource)Naming.lookup( I_OXResource.RMI_NAME );
//        	initRMI();
//        }catch(java.rmi.ConnectException conexp){
//            p2c("Cant connect to RMI Service!\n"+conexp.getMessage());
//            System.exit(1);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        } catch (NotBoundException e) {
//            e.printStackTrace();
//        }
//        xmlrpc_return   = new Vector();
//    }
//
//    
//    public static void p2c(Object obj){
//        System.out.println(""+obj);
//    }
//
//
//    public Vector createOXResource(Hashtable resData) throws RemoteException {
//        try {
//            
//            xmlrpc_return = ox_resource.createOXResource( Integer.parseInt(resData.get( SHELL_CONTEXT_ID ).toString()), resData );
//            
//            if ( xmlrpc_return.get( 0 ).toString().toLowerCase().equals( "ok" ) ) {
//                if ( xmlrpc_return.size() > 1 ) {
//                    for ( int i = 1; i < xmlrpc_return.size(); i++ ) {
//                        p2c( xmlrpc_return.get( i ) );
//                    }
//                } else {
//                    p2c( xmlrpc_return.get( 0 ) );
//                }
//            } else {
//                p2c(xmlrpc_return.get( 1 ) );
//            }
//        }catch(java.lang.NumberFormatException nfe){
//            p2c("Invalid id(s)");
//        } catch ( Exception exp ) {
//            exp.printStackTrace();
//        }
//        
//        return xmlrpc_return;
//    }
//
//
//
//    public Vector changeOXResource(int context_ID, int resource_ID, Hashtable resData) throws RemoteException {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    public Vector createOXResource(int context_ID, Hashtable resData) throws RemoteException {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    public Vector deleteOXResource(int context_ID, int resource_ID) throws RemoteException {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//
//
//    public Vector listOXResources(int context_ID, String pattern) throws RemoteException {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//
//
//	public Vector getOXResourceData(int context_ID, int resource_ID) throws RemoteException {
//		// TODO Auto-generated method stub
//		return null;
//	}
//    
//}
