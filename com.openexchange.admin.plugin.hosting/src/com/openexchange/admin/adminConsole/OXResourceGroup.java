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
//import java.net.MalformedURLException;
//import java.rmi.Naming;
//import java.rmi.NotBoundException;
//import java.rmi.RemoteException;
//import java.util.Hashtable;
//import java.util.StringTokenizer;
//import java.util.Vector;
//
//import com.openexchange.admin.dataSource.I_OXResource;
//import com.openexchange.admin.dataSource.I_OXResourceGroup;
//
//
//
//public class OXResourceGroup implements I_OXResourceGroup {
//    
//    public static final String SHELL_COMMAND                = "command";
//    public static final String SHELL_COMMAND_CREATE         = "create";
//    public static final String SHELL_COMMAND_LIST           = "list";
//    public static final String SHELL_COMMAND_SHOW           = "show";
//    public static final String SHELL_COMMAND_DELETE         = "delete";
//    public static final String SHELL_COMMAND_EDIT           = "update";
//    public static final String SHELL_COMMAND_ADD_RESOURCE   = "addresource";
//    public static final String SHELL_COMMAND_DROP_RESOURCE  = "dropresource";
//    
//    public static final String SHELL_CONTEXT_ID                     = "context_id";
//    public static final String SHELL_RID_NUMBER                     = I_OXResource.RID_NUMBER;
//    public static final String SHELL_RESGROUPID_NUMBER              = I_OXResourceGroup.RESGROUPID_NUMBER;
//    public static final String SHELL_RESOURCE_GROUP_NAME            = I_OXResourceGroup.UID;
//    public static final String SHELL_RESOURCE_GROUP_DISPLAY_NAME    = I_OXResourceGroup.DISPLAYNAME;
//    public static final String SHELL_RESOURCE_GROUP_AVAILABLE       = I_OXResourceGroup.AVAILABLE;
//    public static final String SHELL_SEARCH_PATTERN                 = "pattern";
//    
//    private static Hashtable<String, Object>    resGroupData    = null;
//    private static String       command         = "";
//    
//    private static Vector       xmlrpc_return       = null;
//    private static String[]     neededFields        = null;
//    private static Vector<String>       missingFields       = null;
//    private I_OXResourceGroup   ox_resourceGroup    = null;
//    
//    
//    
//    public static void main( String args[] ) throws Exception {
//        resGroupData = new Hashtable<String, Object>();
//        missingFields = new Vector<String>();
//        
//        parseInput( args );
//        if ( checkNeeded() ) {
//            OXResourceGroup ox_resGroup = new OXResourceGroup();
//            int context_ID = Integer.parseInt( resGroupData.get( SHELL_CONTEXT_ID ).toString() );
//            
//            if ( command.equals( SHELL_COMMAND_CREATE ) ) {
//                xmlrpc_return = ox_resGroup.createOXResourceGroup( context_ID, resGroupData );
//            } else if ( command.equals( SHELL_COMMAND_LIST ) ) {
//                xmlrpc_return = ox_resGroup.listOXResourceGroups( context_ID, resGroupData.get( SHELL_SEARCH_PATTERN ).toString() );
//            } else if ( command.equals( SHELL_COMMAND_DELETE ) ) {
//                ox_resGroup.deleteResourceGroup();
//            } else if ( command.equals( SHELL_COMMAND_EDIT ) ){
//                int resourceGroup_ID = Integer.parseInt( resGroupData.get( SHELL_RID_NUMBER ).toString() );
//                xmlrpc_return = ox_resGroup.changeOXResourceGroup( context_ID, resourceGroup_ID, resGroupData );
//            } else if ( command.equals( SHELL_COMMAND_ADD_RESOURCE ) ) {
//                ox_resGroup.addResource();
//            } else if ( command.equals( SHELL_COMMAND_DROP_RESOURCE ) ) {
//                ox_resGroup.dropResource();
//            } else {
//                showUsage();
//            }
//            
//            System.out.println( xmlrpc_return );
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
//            } else if ( command.equals( SHELL_COMMAND_EDIT ) ) {
//                showEditUsage();
//            } else if ( command.equals( SHELL_COMMAND_ADD_RESOURCE ) ) {
//                showAddResourceUsage();
//            } else if ( command.equals( SHELL_COMMAND_DROP_RESOURCE ) ) {
//                showDropResourceUsage();
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
//        sb.append( "\n" );
//        sb.append( "Options: \n" );
//        sb.append( "\t --"+SHELL_COMMAND+"=["+SHELL_COMMAND_CREATE+"|"+SHELL_COMMAND_DELETE+"|"+SHELL_COMMAND_EDIT+"|"+SHELL_COMMAND_LIST+"|"+SHELL_COMMAND_SHOW+"] \n" );
//        
//        System.out.println( sb.toString() );
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
//        System.out.println( sb.toString() );
//    }
//    
//    
//    
//    private static void parseInput( String args[] ) {
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
//                            resGroupData.put( paramNAME, new Boolean( value ) );
//                        } else {
//                            resGroupData.put( paramNAME, value );
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
//        sb.append( "Options "+SHELL_COMMAND+"="+SHELL_COMMAND_CREATE+":\n" );
//        
//        sb.append( "\t --"+SHELL_CONTEXT_ID+"=2\n" );
//        sb.append( "\t --"+SHELL_RESOURCE_GROUP_AVAILABLE+"=true\n" );
//        sb.append( "\t --"+SHELL_RESOURCE_GROUP_NAME+"=garage\n" );
//        sb.append( "\t --"+SHELL_RESOURCE_GROUP_DISPLAY_NAME+"=\"All my cars\"\n" );
//        
//        sb.append( "\t --attribute1=attribute_value1\n" );
//        sb.append( "\t --attribute2=attribute_value2\n" );
//        sb.append( "\t --....\n" );
//        
//        System.out.println( sb.toString() );
//    }
//    
//    
//    
//    private static void showListUsage() {
//        showGeneralUsages();
//        
//        StringBuffer sb = new StringBuffer();
//        sb.append( "Options "+SHELL_COMMAND+"="+SHELL_COMMAND_LIST+":\n" );
//        
//        sb.append( "\t --"+SHELL_CONTEXT_ID+"=2\n" );
//        sb.append( "\t --"+SHELL_SEARCH_PATTERN+"=*\n" );
//        
//        System.out.println( sb.toString() );
//    }
//    
//    
//    
//    private static void showDeleteUsage() {
//        showGeneralUsages();
//        
//        StringBuffer sb = new StringBuffer();
//        sb.append( "Options "+SHELL_COMMAND+"="+SHELL_COMMAND_DELETE+":\n" );
//        
//        sb.append( "\t --"+SHELL_CONTEXT_ID+"=2\n" );
//        sb.append( "\t --"+SHELL_RESGROUPID_NUMBER+"=12\n" );
//        
//        System.out.println( sb.toString() );
//    }
//    
//    
//    
//    private static void showEditUsage() {
//        showGeneralUsages();
//        
//        StringBuffer sb = new StringBuffer();
//        sb.append( "Options "+SHELL_COMMAND+"="+SHELL_COMMAND_EDIT+":\n" );
//        
//        sb.append( "\t --"+SHELL_CONTEXT_ID+"=2\n" );
//        sb.append( "\t --"+SHELL_RESGROUPID_NUMBER+"=12\n" );
//        sb.append( "\t --"+SHELL_RESOURCE_GROUP_AVAILABLE+"=true\n" );
//        sb.append( "\t --"+SHELL_RESOURCE_GROUP_NAME+"=garage\n" );
//        sb.append( "\t --"+SHELL_RESOURCE_GROUP_DISPLAY_NAME+"=\"All my cars\"\n" );
//        
//        
//        sb.append( "\t --attribute1=attribute_value1\n" );
//        sb.append( "\t --attribute2=attribute_value2\n" );
//        sb.append( "\t --....\n" );
//        
//        System.out.println( sb.toString() );
//    }
//    
//    
//    
//    private static void showAddResourceUsage() {
//        showGeneralUsages();
//        
//        StringBuffer sb = new StringBuffer();
//        sb.append( "Options "+SHELL_COMMAND+"="+SHELL_COMMAND_ADD_RESOURCE+":\n" );
//        
//        sb.append( "\t --"+SHELL_CONTEXT_ID+"=2\n" );
//        sb.append( "\t --"+SHELL_RESGROUPID_NUMBER+"=12\n" );
//        sb.append( "\t --"+SHELL_RID_NUMBER+"=48\n" );
//        
//        System.out.println( sb.toString() );
//    }
//    
//    
//    
//    private static void showDropResourceUsage() {
//        showGeneralUsages();
//        
//        StringBuffer sb = new StringBuffer();
//        sb.append( "Options "+SHELL_COMMAND+"="+SHELL_COMMAND_DROP_RESOURCE+":\n" );
//        
//        sb.append( "\t --"+SHELL_CONTEXT_ID+"=2\n" );
//        sb.append( "\t --"+SHELL_RESGROUPID_NUMBER+"=12\n" );
//        sb.append( "\t --"+SHELL_RID_NUMBER+"=48\n" );
//        
//        System.out.println( sb.toString() );
//    }
//    
//    
//    
//    private static boolean checkNeeded() {
//        boolean allFields = true;
//        neededFields = new String[1];
//        neededFields[0] = SHELL_CONTEXT_ID;
//        
//        if ( command.length() < 1 ) {
//            missingFields.add( SHELL_COMMAND );
//            allFields = false;
//        } else {
//            if ( command.equalsIgnoreCase( SHELL_COMMAND_CREATE ) ) {
//                String api[] = I_OXResourceGroup.REQUIRED_KEYS_CREATE;
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
//                String api[] = I_OXResourceGroup.REQUIRED_KEYS_CHANGE;
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
//                f[1] = SHELL_RESGROUPID_NUMBER;
//                neededFields = f; 
//            }
//            
//        }
//        
//        for ( int i = 0; i < neededFields.length; i++ ) {
//            if ( !resGroupData.containsKey( neededFields[ i ] ) ) {
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
//    private void deleteResourceGroup() throws Exception {
//        //xmlrpc_control.put( "METHODNAME", "deleteOXResourceGroup" );
//        
//        try {
//            //xmlrpc_params.add( resGroupData.get( SHELL_CONTEXT_ID ).toString() );
//            //xmlrpc_params.add( resGroupData.get( SHELL_RESGROUPID_NUMBER ).toString() );
//            //xmlrpc_control.put( "USE_SSL", new Boolean ( ssl ) );
//            
//            //if ( ssl ) {
//                //xmlrpc_control.put( "CACERT", "/opt/openexchange/etc/groupware/sslcerts/oxCA/cacert.pem" );
//                //xmlrpc_control.put( "CERT", "/opt/openexchange/etc/groupware/sslcerts/oxCERTS/groupwarecert.pem" );
//                //xmlrpc_control.put( "KEY", "/opt/openexchange/etc/groupware/sslcerts/oxCERTS/groupwarekey.pem" );
//            //}
//            
//            //xmlrpc_return = xmlrpc.execute( xmlrpc_control, xmlrpc_params );
//            xmlrpc_return = ox_resourceGroup.deleteOXResourceGroup( Integer.parseInt(resGroupData.get( SHELL_CONTEXT_ID ).toString()), Integer.parseInt(resGroupData.get( SHELL_RESGROUPID_NUMBER ).toString()) );
//            
//            if ( xmlrpc_return.get( 0 ).toString().toLowerCase().equals( "ok" ) ) {
//                if ( xmlrpc_return.size() > 1 ) {
//                    for ( int i = 1; i < xmlrpc_return.size(); i++ ) {
//                        System.out.println( xmlrpc_return.get( i ) );
//                    }
//                } else {
//                    System.out.println( xmlrpc_return.get( 0 ) );
//                }
//            } else {
//                throw new Exception( xmlrpc_return.get( 1 ).toString() );
//            }
//        } catch ( Exception exp ) {
//            exp.printStackTrace();
//        }
//    }
//    
//    
//    
//    private void addResource() throws Exception {
//        //xmlrpc_control.put( "METHODNAME", "addResource" );
//        
//        try {
//            //xmlrpc_params.add( resGroupData.get( SHELL_CONTEXT_ID ).toString() );
//            //xmlrpc_params.add( resGroupData.get( SHELL_RESGROUPID_NUMBER ).toString() );
//            //xmlrpc_params.add( resGroupData.get( SHELL_RID_NUMBER ).toString() );
//            //xmlrpc_control.put( "USE_SSL", new Boolean ( ssl ) );
//            
//            //if ( ssl ) {
//                //xmlrpc_control.put( "CACERT", "/opt/openexchange/etc/groupware/sslcerts/oxCA/cacert.pem" );
//                //xmlrpc_control.put( "CERT", "/opt/openexchange/etc/groupware/sslcerts/oxCERTS/groupwarecert.pem" );
//                //xmlrpc_control.put( "KEY", "/opt/openexchange/etc/groupware/sslcerts/oxCERTS/groupwarekey.pem" );
//            //}
//            
//            //xmlrpc_return = xmlrpc.execute( xmlrpc_control, xmlrpc_params );
//            xmlrpc_return = ox_resourceGroup.addResource( Integer.parseInt(resGroupData.get( SHELL_CONTEXT_ID ).toString()), Integer.parseInt(resGroupData.get( SHELL_RESGROUPID_NUMBER ).toString()), Integer.parseInt(resGroupData.get( SHELL_RID_NUMBER ).toString()) );
//            
//            if ( xmlrpc_return.get( 0 ).toString().toLowerCase().equals( "ok" ) ) {
//                if ( xmlrpc_return.size() > 1 ) {
//                    for ( int i = 1; i < xmlrpc_return.size(); i++ ) {
//                        System.out.println( xmlrpc_return.get( i ) );
//                    }
//                } else {
//                    System.out.println( xmlrpc_return.get( 0 ) );
//                }
//            } else {
//                throw new Exception( xmlrpc_return.get( 1 ).toString() );
//            }
//        } catch ( Exception exp ) {
//            exp.printStackTrace();
//        }
//    }
//    
//    
//    
//    private void dropResource() throws Exception {
//        //xmlrpc_control.put( "METHODNAME", "dropResource" );
//        
//        try {
//            //xmlrpc_params.add( resGroupData.get( SHELL_CONTEXT_ID ).toString() );
//            //xmlrpc_params.add( resGroupData.get( SHELL_RESGROUPID_NUMBER ).toString() );
//            //xmlrpc_params.add( resGroupData.get( SHELL_RID_NUMBER ).toString() );
//            //xmlrpc_control.put( "USE_SSL", new Boolean ( ssl ) );
//            
//            //if ( ssl ) {
//                //xmlrpc_control.put( "CACERT", "/opt/openexchange/etc/groupware/sslcerts/oxCA/cacert.pem" );
//                //xmlrpc_control.put( "CERT", "/opt/openexchange/etc/groupware/sslcerts/oxCERTS/groupwarecert.pem" );
//                //xmlrpc_control.put( "KEY", "/opt/openexchange/etc/groupware/sslcerts/oxCERTS/groupwarekey.pem" );
//            //}
//            
//            //xmlrpc_return = xmlrpc.execute( xmlrpc_control, xmlrpc_params );
//            xmlrpc_return = ox_resourceGroup.dropResource( Integer.parseInt(resGroupData.get( SHELL_CONTEXT_ID ).toString()), Integer.parseInt(resGroupData.get( SHELL_RESGROUPID_NUMBER ).toString()), Integer.parseInt(resGroupData.get( SHELL_RID_NUMBER ).toString()) );
//            
//            if ( xmlrpc_return.get( 0 ).toString().toLowerCase().equals( "ok" ) ) {
//                if ( xmlrpc_return.size() > 1 ) {
//                    for ( int i = 1; i < xmlrpc_return.size(); i++ ) {
//                        System.out.println( xmlrpc_return.get( i ) );
//                    }
//                } else {
//                    System.out.println( xmlrpc_return.get( 0 ) );
//                }
//            } else {
//                throw new Exception( xmlrpc_return.get( 1 ).toString() );
//            }
//        } catch ( Exception exp ) {
//            exp.printStackTrace();
//        }
//    }
//    
//    
//    
//    public OXResourceGroup() {
//        try {
//            ox_resourceGroup = (I_OXResourceGroup)Naming.lookup( I_OXResourceGroup.RMI_NAME );
//        } catch ( MalformedURLException e ) {
//            e.printStackTrace();
//        } catch ( RemoteException e ) {
//            e.printStackTrace();
//        } catch ( NotBoundException e ) {
//            e.printStackTrace();
//        }
//        xmlrpc_return   = new Vector();
//    }
//    
//    
//    
//    public Vector createOXResourceGroup( int context_ID, Hashtable resourceGroupData ) throws RemoteException {
//        try {
//            xmlrpc_return = ox_resourceGroup.createOXResourceGroup( context_ID, resourceGroupData );
//        } catch ( Exception exp ) {
//            exp.printStackTrace();
//        }
//        
//        return xmlrpc_return;
//    }
//    
//    
//    
//    public Vector listOXResourceGroups( int context_ID, String pattern ) throws RemoteException {
//        try {
//            xmlrpc_return = ox_resourceGroup.listOXResourceGroups( context_ID, pattern );
//        } catch ( Exception exp ) {
//            exp.printStackTrace();
//        }
//        
//        return xmlrpc_return;
//    }
//
//
//
//    public Vector changeOXResourceGroup( int context_ID, int resourceGroup_ID, Hashtable resourceGroupData ) throws RemoteException {
//        try {
//            xmlrpc_return = ox_resourceGroup.changeOXResourceGroup( context_ID, resourceGroup_ID, resourceGroupData );
//        } catch ( Exception exp ) {
//            exp.printStackTrace();
//        }
//        
//        return xmlrpc_return;
//    }
//
//
//
//    public Vector addResource(int context_id, int resourceGroup_ID, int resource_ID) throws RemoteException {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//
//
//    public Vector getResources(int context_id, int resourceGroup_ID) throws RemoteException {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//
//
//    public Vector dropResource(int context_id, int resourceGroup_ID, int resource_ID) throws RemoteException {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//
//
//    public Vector deleteOXResourceGroup(int context_id, int resourceGroup_ID) throws RemoteException {
//        // TODO Auto-generated method stub
//        return null;
//    }
//    
//    
//    
//}
