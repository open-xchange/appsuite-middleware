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
//import java.rmi.NotBoundException;
//import java.rmi.RemoteException;
//import java.rmi.registry.LocateRegistry;
//import java.rmi.registry.Registry;
//import java.util.Hashtable;
//import java.util.StringTokenizer;
//import java.util.Vector;
//
//import com.openexchange.admin.dataSource.I_OXGroup;
//
//
//
//public class OXGroup implements I_OXGroup {
//    
//    
//    public static final String SHELL_COMMAND                = "command";
//    public static final String SHELL_COMMAND_CREATE         = "create";
//    public static final String SHELL_COMMAND_ADD_MEMBER     = "addmember";
//    public static final String SHELL_COMMAND_REMOVE_MEMBER  = "removemember";
//    public static final String SHELL_COMMAND_LIST           = "list";
//    public static final String SHELL_COMMAND_SHOW           = "show";
//    public static final String SHELL_COMMAND_DELETE         = "delete";
//    public static final String SHELL_COMMAND_EDIT           = "update";
//    
//    public static final String SHELL_CONTEXT_ID         = "context_id";
//    public static final String SHELL_MEMBER_NUMBER      = "member_id";
//    public static final String SHELL_GID_NUMBER         = I_OXGroup.GID_NUMBER;
//    public static final String SHELL_GROUP_NAME         = I_OXGroup.GID;
//    public static final String SHELL_GROUP_DISPLAY_NAME = I_OXGroup.DISPLAYNAME;
//    public static final String SHELL_SEARCH_PATTERN     = "pattern";
//    public static final String SHELL_SSL                = "ssl";
//    
//    private static Hashtable<String, Object>    groupData   = null;
//    private static String       command     = "";
//    
//    private static Vector       xmlrpc_return       = null;
//    private static String[]     neededFields        = null;
//    private static Vector<String>       missingFields       = null;
//    private static I_OXGroup           ox_group            = null;
//    private Registry            registry            = null;
//
//    
//    public static void main( String args[] ) throws Exception {
//        groupData = new Hashtable<String, Object>();
//        missingFields = new Vector<String>();
//        
//        parseInput( args );
//        if ( checkNeeded() ) {
//            OXGroup ox_group = new OXGroup();
//            
//            if ( command.equals( SHELL_COMMAND_CREATE ) ) {
//                ox_group.createGroup();
//            } else if ( command.equals( SHELL_COMMAND_LIST ) ) {
//                ox_group.listGroup();
//            } else if ( command.equals( SHELL_COMMAND_DELETE ) ) {
//                ox_group.deleteGroup();
//            } else if ( command.equals( SHELL_COMMAND_EDIT ) ){
//                ox_group.changeGroup();
//            } else if ( command.equals( SHELL_COMMAND_ADD_MEMBER ) ) {
//                ox_group.addMember();
//            } else if ( command.equals( SHELL_COMMAND_REMOVE_MEMBER ) ) {
//                ox_group.removeMember();
//            } else {
//                showUsage();
//            }
//        } else {
//            showUsage();
//            showMissing();
//        }
//        
//    }
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
//            } else if ( command.equals( SHELL_COMMAND_ADD_MEMBER ) ) {
//                showAddMemberUsage();
//            } else if ( command.equals( SHELL_COMMAND_REMOVE_MEMBER ) ) {
//                showRemoveMemberUsage();
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
//    
//    private static void showGeneralUsages() {
//        //StringBuffer sb = new StringBuffer();
//        //sb.append( "\n" );
//        //sb.append( "Options: \n" );
//        //sb.append( "\t --"+SHELL_COMMAND+"=["+SHELL_COMMAND_CREATE+"|"+SHELL_COMMAND_DELETE+"|"+SHELL_COMMAND_EDIT+"|"+SHELL_COMMAND_ADD_MEMBER+"|"+SHELL_COMMAND_LIST+"|"+SHELL_COMMAND_SHOW+"|"+SHELL_COMMAND_REMOVE_MEMBER+"] \n" );
//        //sb.append( "\t --"+SHELL_SSL+"=true|false\t(defaul=false)\n" );
//        
//        //System.out.println( sb.toString() );
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
//    private static void showCreateUsage() {
//        showGeneralUsages();
//        
//        StringBuffer sb = new StringBuffer();
//        sb.append( "\n" );
//        sb.append( "Options:\n" );
//        
//        sb.append( "\t --"+SHELL_CONTEXT_ID+"=2\n" );
//        sb.append( "\t --"+SHELL_GROUP_NAME+"=devel\n" );
//        sb.append( "\t --"+SHELL_GROUP_DISPLAY_NAME+"=\"Development Team\"\n" );
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
//        sb.append( "\n" );
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
//        sb.append( "\n" );
//        sb.append( "Options:\n" );
//        
//        sb.append( "\t --"+SHELL_CONTEXT_ID+"=2\n" );
//        sb.append( "\t --"+SHELL_GID_NUMBER+"=12\n" );
//        sb.append( "\t --"+SHELL_GROUP_NAME+"=devel\n" );
//        sb.append( "\t --"+SHELL_GROUP_DISPLAY_NAME+"=\"Development Team\"\n" );
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
//    private static void showAddMemberUsage() {
//        showGeneralUsages();
//        
//        StringBuffer sb = new StringBuffer();
//        sb.append( "\n" );
//        sb.append( "Options:\n" );
//        
//        sb.append( "\t --"+SHELL_CONTEXT_ID+"=2\n" );
//        sb.append( "\t --"+SHELL_GID_NUMBER+"=12\n" );
//        sb.append( "\t --"+SHELL_MEMBER_NUMBER+"=48\n" );
//        
//        p2c( sb.toString() );
//    }
//    
//    
//    
//    private static void showRemoveMemberUsage() {
//        showGeneralUsages();
//        
//        StringBuffer sb = new StringBuffer();
//        sb.append( "\n" );
//        sb.append( "Options:\n" );
//        
//        sb.append( "\t --"+SHELL_CONTEXT_ID+"=2\n" );
//        sb.append( "\t --"+SHELL_GID_NUMBER+"=12\n" );
//        sb.append( "\t --"+SHELL_MEMBER_NUMBER+"=48\n" );
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
//        sb.append( "\n" );
//        sb.append( "Options:\n" );
//        
//        sb.append( "\t --"+SHELL_CONTEXT_ID+"=2\n" );
//        sb.append( "\t --"+SHELL_GID_NUMBER+"=12\n" );
//        
//        p2c( sb.toString() );
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
//                            groupData.put( paramNAME, new Boolean( value ) );
//                        } else {
//                            groupData.put( paramNAME, value );
//                        }
//                    }
//                }
//            }
//        }
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
//                String api[] = I_OXGroup.REQUIRED_KEYS_CREATE;
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
//                String api[] = {I_OXGroup.GID_NUMBER};
//                String f[] = new String[api.length + 1];
//                f[0] = neededFields[0];
//                for ( int i = 0; i < api.length; i++ ) {
//                    f[i+1] = api[i];
//                }
//                neededFields = f;
//            }
//            
//            if ( command.equalsIgnoreCase( SHELL_COMMAND_ADD_MEMBER ) ) {
//                String f[] = new String[3];
//                f[0] = neededFields[0];
//                f[1] = SHELL_GID_NUMBER;
//                f[2] = SHELL_MEMBER_NUMBER;
//                neededFields = f;
//            }
//            
//            if ( command.equalsIgnoreCase( SHELL_COMMAND_REMOVE_MEMBER ) ) {
//                String f[] = new String[3];
//                f[0] = neededFields[0];
//                f[1] = SHELL_GID_NUMBER;
//                f[2] = SHELL_MEMBER_NUMBER;
//                neededFields = f;
//            }
//            
//            if ( command.equalsIgnoreCase( SHELL_COMMAND_DELETE ) ) {
//                String f[] = new String[2];
//                f[0] = neededFields[0];
//                f[1] = SHELL_GID_NUMBER;
//                neededFields = f;
//            }
//            
//        }
//        
//        for ( int i = 0; i < neededFields.length; i++ ) {
//            if ( !groupData.containsKey( neededFields[ i ] ) ) {
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
//    private void createGroup() throws Exception {
//        try {
//            
//            xmlrpc_return = ox_group.createOXGroup( Integer.parseInt(groupData.get( SHELL_CONTEXT_ID ).toString()),
//                    groupData );
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
//    }
//    
//    public static void p2c(Object obj){
//        System.out.println(""+obj);
//    }
//    
//    private void listGroup() throws Exception {
//        try {
//            xmlrpc_return = ox_group.listOXGroups( Integer.parseInt(groupData.get( SHELL_CONTEXT_ID ).toString()), groupData.get( SHELL_SEARCH_PATTERN ).toString() );
//            
//            if ( xmlrpc_return.get( 0 ).toString().toLowerCase().equals( "ok" ) ) {
//                if ( xmlrpc_return.size() > 1 ) {
//                    
//                    // show result
//                    Vector alldata = (Vector)xmlrpc_return.get(1);
//                    p2c("Context-id|Group-id|Identifier|Displayname\n");
//                    for(int a = 0;a<alldata.size();a++){
//                        Hashtable dd = (Hashtable)alldata.get(a);
//                        p2c(""+dd.get(I_OXGroup.CID)+"|"+dd.get(I_OXGroup.GID_NUMBER)+"|"+dd.get(I_OXGroup.GID)+"|"+dd.get(I_OXGroup.DISPLAYNAME));
//                    }
//                } else {
//                    p2c( xmlrpc_return.get( 0 ) );
//                }
//            } else {
//                p2c( xmlrpc_return.get( 1 ) );
//            }
//        }catch(java.lang.NumberFormatException nfe){
//            p2c("Invalid id(s)");
//        } catch ( Exception exp ) {
//            exp.printStackTrace();
//        }
//    }
//    
//    
//    
//    private void changeGroup() throws Exception {
//        try {
//            xmlrpc_return = ox_group.changeOXGroup( Integer.parseInt(groupData.get( SHELL_CONTEXT_ID ).toString()),
//                    Integer.parseInt(groupData.get( SHELL_GID_NUMBER ).toString()),
//                    groupData );
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
//    }
//    
//    
//    
//    private void addMember() throws Exception {
//        try {
//            int [] a = new int[1];
//            a[0] = Integer.parseInt(groupData.get( SHELL_MEMBER_NUMBER ).toString());
//            xmlrpc_return = ox_group.addMember( Integer.parseInt(groupData.get( SHELL_CONTEXT_ID ).toString()), Integer.parseInt(groupData.get( SHELL_GID_NUMBER ).toString()), a );
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
//    
//    
//    private void removeMember() throws Exception {
//        try {
//            int [] a = new int[1];
//            a[0] = Integer.parseInt(groupData.get( SHELL_MEMBER_NUMBER ).toString());
//            xmlrpc_return = ox_group.removeMember( Integer.parseInt(groupData.get( SHELL_CONTEXT_ID ).toString()), Integer.parseInt(groupData.get( SHELL_GID_NUMBER ).toString()), a );
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
//    
//    
//    private void deleteGroup() throws Exception {
//        try {
//            xmlrpc_return = ox_group.deleteOXGroup( Integer.parseInt(groupData.get( SHELL_CONTEXT_ID ).toString()), Integer.parseInt(groupData.get( SHELL_GID_NUMBER ).toString()) );
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
//    	if( ox_group != null ) {
//    		ox_group = null;
//    	}
//    	registry = LocateRegistry.getRegistry("localhost");
//	    ox_group = (I_OXGroup)registry.lookup(I_OXGroup.RMI_NAME);
//    }
//
//    
//    public OXGroup() {
//        try {
//            //ox_group = (I_OXGroup)Naming.lookup( I_OXGroup.RMI_NAME );
//        	initRMI();
//        }catch(java.rmi.ConnectException conexp){
//             p2c("Cant connect to RMI Service!\n"+conexp.getMessage());
//             System.exit(1);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        } catch (NotBoundException e) {
//            e.printStackTrace();
//        }
//        xmlrpc_return   = new Vector();
//    }
//    
//    
//    
//    public Vector createOXGroup(int context_ID, Hashtable groupData) throws RemoteException {
//        // TODO Auto-generated method stub
//        return null;
//    }
//    
//    
//    
//    public Vector changeOXGroup(int context_ID, int group_ID, Hashtable groupData) throws RemoteException {
//        // TODO Auto-generated method stub
//        return null;
//    }
//    
//    
//    
//    public Vector deleteOXGroup(int context_ID, int group_ID) throws RemoteException {
//        // TODO Auto-generated method stub
//        return null;
//    }
//    
//    
//    
//    public Vector addMember(int context_ID, int group_ID, int member_ID) throws RemoteException {
//        // TODO Auto-generated method stub
//        return null;
//    }
//    
//    
//    
//    public Vector removeMember(int context_ID, int group_ID, int member_ID) throws RemoteException {
//        // TODO Auto-generated method stub
//        return null;
//    }
//    
//    
//    
//    public Vector getMembers(int context_ID, int group_ID) throws RemoteException {
//        // TODO Auto-generated method stub
//        return null;
//    }
//    
//    
//    
//    public Vector listOXGroups(int context_ID, String pattern) throws RemoteException {
//        // TODO Auto-generated method stub
//        return null;
//    }
//    
//    public Vector getOXGroupData(int context_ID, int group_id) throws RemoteException {
//        // TODO Auto-generated method stub
//        return null;
//    }
//    
//    public Vector addMember(int context_ID, int group_ID, int[] member_ids) throws RemoteException {
//        return null;
//    }
//    
//    public Vector removeMember(int context_ID, int group_ID, int[] member_ids) throws RemoteException {
//        return null;
//    }
//}
