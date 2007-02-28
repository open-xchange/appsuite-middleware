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
//import java.util.Vector;
//
//import com.openexchange.admin.dataSource.I_OXContext;
//import com.openexchange.admin.dataSource.I_OXUser;
//import com.openexchange.admin.rmi.OXUserInterface;
//import com.openexchange.admin.rmi.dataobjects.Context;
//import com.openexchange.admin.rmi.dataobjects.Credentials;
//import com.openexchange.admin.rmi.dataobjects.User;
//import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
//import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
//import com.openexchange.admin.rmi.exceptions.InvalidDataException;
//import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
//import com.openexchange.admin.rmi.exceptions.StorageException;
//import com.openexchange.admin.tools.DataConverter;
//
//public class OXUser {
//
//    private static final String SHELL_COMMAND = "command";
//
//    private static final String SHELL_COMMAND_CREATE_USER = "create_user";
//
//    private static final String SHELL_COMMAND_LIST_USER = "list_user";
//
//    private static final String SHELL_COMMAND_REMOVE_USER = "remove_user";
//
//    private static final String SHELL_ADMIN_USER = "adminuser";
//
//    private static final String SHELL_ADMIN_PASSWORD = "adminpassword";
//
//    private static final String SHELL_DISABLE_MODULE = "no_access";
//
//    public static final String[] POSSIBLE_SHELL_COMMANDY = {
//            SHELL_COMMAND_CREATE_USER, SHELL_COMMAND_LIST_USER,
//            SHELL_COMMAND_REMOVE_USER };
//
//    private static Hashtable userData = null;
//
//    private static String command = "";
//
//    private static String[] neededFields = null;
//
//    private static OXUserInterface ox_user = null;
//
//    private Registry registry = null;
//
//    private static Vector<String> missingFields = null;
//
//    public static void main(String args[]) {
//        OXUser u = new OXUser();
//
//        try {
//            u.init();
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        } catch (NotBoundException e) {
//            e.printStackTrace();
//        }
//
//        userData = new Hashtable();
//        missingFields = new Vector<String>();
//
//        userData = AdminConsoleTools.parseInput(args, SHELL_COMMAND);
//        if (userData != null && userData.containsKey(SHELL_COMMAND)) {
//            command = userData.get(SHELL_COMMAND).toString();
//            userData.remove(SHELL_COMMAND);
//        }
//
//        Credentials cred = new Credentials((String) userData
//                .get(SHELL_ADMIN_USER), (String) userData
//                .get(SHELL_ADMIN_PASSWORD));
//
//        int context_id = 0;
//        if (userData.containsKey(I_OXContext.CONTEXT_ID)) {
//            String s_id = userData.get(I_OXContext.CONTEXT_ID).toString();
//            try {
//                Integer i_id = new Integer(s_id);
//                context_id = i_id.intValue();
//            } catch (NumberFormatException nfe) {
//                missingFields.add(I_OXContext.CONTEXT_ID);
//            }
//        } else {
//            missingFields.add(I_OXContext.CONTEXT_ID);
//        }
//        Context ctx = new Context(context_id);
//        UserModuleAccess access = new UserModuleAccess();
//
//        if (checkNeeded()) {
//
//            if (command.equals(SHELL_COMMAND_CREATE_USER)) {
//
//                if (userData.containsKey(SHELL_DISABLE_MODULE)) {
//                    String no_access = userData.get(SHELL_DISABLE_MODULE)
//                            .toString();
//                    if (no_access.equalsIgnoreCase("ALL")) {
//                        access.disableAll();
//                    } else {
//                        for (String a : no_access.split(",")) {
//                            a.trim();
//                            if (a.equals(I_OXUser.ACCESS_WEBMAIL)) {
//                                access.setWebmail(false);
//                            } else if (a.equals(I_OXUser.ACCESS_CALENDAR)) {
//                                access.setCalendar(false);
//                            } else if (a.equals(I_OXUser.ACCESS_CONTACTS)) {
//                                access.setContacts(false);
//                            } else if (a.equals(I_OXUser.ACCESS_TASKS)) {
//                                access.setTasks(false);
//                            } else if (a.equals(I_OXUser.ACCESS_INFOSSTORE)) {
//                                access.setInfostore(false);
//                            } else if (a.equals(I_OXUser.ACCESS_PROJECTS)) {
//                                access.setProjects(false);
//                            } else if (a.equals(I_OXUser.ACCESS_FORUM)) {
//                                access.setForum(false);
//                            } else if (a
//                                    .equals(I_OXUser.ACCESS_PINBOARD_WRITE_ACCESS)) {
//                                access.setPinboardWrite(false);
//                            } else if (a.equals(I_OXUser.ACCESS_WEBDAV_XML)) {
//                                access.setWebdavXml(false);
//                            } else if (a.equals(I_OXUser.ACCESS_WEBDAV)) {
//                                access.setWebdav(false);
//                            } else if (a.equals(I_OXUser.ACCESS_ICAL)) {
//                                access.setIcal(false);
//                            } else if (a.equals(I_OXUser.ACCESS_VCARD)) {
//                                access.setVcard(false);
//                            } else if (a.equals(I_OXUser.ACCESS_RSS_BOOKMARKS)) {
//                                access.setRssBookmarks(false);
//                            } else if (a.equals(I_OXUser.ACCESS_RSS_PORTAL)) {
//                                access.setRssPortal(false);
//                            } else if (a.equals(I_OXUser.ACCESS_SYNCML)) {
//                                access.setSyncml(false);
//                            } else if (a
//                                    .equals(I_OXUser.ACCESS_EDIT_PUBLIC_FOLDERS)) {
//                                access.setEditPublicFolders(false);
//                            } else if (a
//                                    .equals(I_OXUser.ACCESS_READ_CREATE_SHARED_FOLDERS)) {
//                                access.setReadCreateSharedFolders(false);
//                            } else if (a.equals(I_OXUser.ACCESS_DELEGATE_TASKS)) {
//                                access.setDelegateTask(false);
//                            }
//                        }
//                    }
//                }
//
//                if (!userData.containsKey(I_OXUser.EMAIL1)) {
//                    userData.put(I_OXUser.EMAIL1, userData
//                            .get(I_OXUser.PRIMARY_MAIL));
//                }
//                if (userData.containsKey(I_OXUser.ALIAS)) {
//                    String a = (String) userData.get(I_OXUser.ALIAS);
//                    String aliases[] = a.split(",");
//                    userData.remove(I_OXUser.ALIAS);
//                    userData.put(I_OXUser.ALIAS, aliases);
//                }
//
//                User user = DataConverter.userHashtable2UserObject(userData);
//                int id = 0;
//                try {
//                    id = ox_user.create(ctx, user, access, cred);
//                    String id_info = " (ID=" + id + ")";
//                    System.out.println("New user in context ID=" + context_id
//                            + " added" + id_info + ".");
//                } catch (RemoteException e) {
//                    u.doExit(e);
//                } catch (StorageException e) {
//                    u.doExit(e);
//                } catch (InvalidCredentialsException e) {
//                    u.doExit(e);
//                } catch (NoSuchContextException e) {
//                    u.doExit(e);
//                } catch (InvalidDataException e) {
//                    u.doExit(e);
//                }
//
//            } else if (command.equals(SHELL_COMMAND_LIST_USER)) {
//
//                int[] ids = {};
//                try {
//                    ids = ox_user.getAll(ctx, cred);
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                    u.doExit(e);
//                } catch (StorageException e) {
//                    e.printStackTrace();
//                    u.doExit(e);
//                } catch (InvalidCredentialsException e) {
//                    e.printStackTrace();
//                    u.doExit(e);
//                } catch (NoSuchContextException e) {
//                    e.printStackTrace();
//                    u.doExit(e);
//                } catch (InvalidDataException e) {
//                    e.printStackTrace();
//                    u.doExit(e);
//                }
//                System.out.println("User_ID");
//
//                for (int id : ids) {
//                    System.out.println(id);
//                }
//                if (ids.length == 0) {
//                    System.out.println("none");
//                }
//
//            } else if (command.equals(SHELL_COMMAND_REMOVE_USER)) {
//                int user_id = 0;
//                if (userData.containsKey(I_OXUser.UID_NUMBER)) {
//                    String s_uid = userData.get(I_OXUser.UID_NUMBER).toString();
//                    try {
//                        Integer i_id = new Integer(s_uid);
//                        user_id = i_id.intValue();
//                    } catch (NumberFormatException nfe) {
//                        missingFields.add(I_OXUser.UID_NUMBER);
//                        showMissing();
//                    }
//                } else {
//                    missingFields.add(I_OXUser.UID_NUMBER);
//                    showMissing();
//                }
//                int[] ids = { user_id };
//                try {
//                    ox_user.delete(ctx, ids, cred);
//                } catch (RemoteException e) {
//                    u.doExit(e);
//                } catch (StorageException e) {
//                    u.doExit(e);
//                } catch (InvalidCredentialsException e) {
//                    u.doExit(e);
//                } catch (NoSuchContextException e) {
//                    u.doExit(e);
//                } catch (InvalidDataException e) {
//                    u.doExit(e);
//                }
//                System.out.println("User with ID=" + user_id
//                        + " in context ID=" + context_id + " removed.");
//            } else {
//                showUsage();
//            }
//
//            // Need for debug!
//            // System.out.println( xmlrpc_return );
//        } else {
//            showUsage();
//            showMissing();
//        }
//
//    }
//
//    private static void showMissing() {
//        StringBuffer sb = new StringBuffer();
//        sb.append("Missing options: \n");
//        for (int i = 0; i < missingFields.size(); i++) {
//            sb.append("\t --" + missingFields.get(i) + "\n");
//        }
//
//        System.out.println(sb.toString());
//    }
//
//    private static boolean checkNeeded() {
//        boolean allFields = true;
//        neededFields = new String[0];
//        for (String f : new String[] { SHELL_ADMIN_USER, SHELL_ADMIN_PASSWORD }) {
//            if (!userData.containsKey(f)) {
//                missingFields.add(f);
//                allFields = false;
//            }
//        }
//        if (command.length() < 1) {
//            missingFields.add(SHELL_COMMAND);
//            allFields = false;
//        } else {
//
//            if (command.equalsIgnoreCase(SHELL_COMMAND_REMOVE_USER)) {
//                String f[] = new String[2];
//                f[0] = I_OXContext.CONTEXT_ID;
//                f[1] = I_OXUser.UID_NUMBER;
//                neededFields = f;
//            }
//
//            if (command.equalsIgnoreCase(SHELL_COMMAND_LIST_USER)) {
//                String f[] = new String[1];
//                f[0] = I_OXContext.CONTEXT_ID;
//                neededFields = f;
//            }
//
//            if (command.equalsIgnoreCase(SHELL_COMMAND_CREATE_USER)) {
//                String api[] = I_OXUser.REQUIRED_KEYS_CREATE;
//                String f[] = new String[api.length];
//                for (int i = 0; i < api.length; i++) {
//                    f[i] = api[i];
//                }
//                neededFields = f;
//            }
//
//        }
//
//        for (int i = 0; i < neededFields.length; i++) {
//            if (userData.size() <= 0 || !userData.containsKey(neededFields[i])) {
//                if (!missingFields.contains(neededFields[i])) {
//                    missingFields.add(neededFields[i]);
//                }
//                allFields = false;
//            }
//        }
//
//        return allFields;
//    }
//
//    private static void showUsage() {
//        if (command != null && command.length() > 1) {
//            if (command.equals(SHELL_COMMAND_CREATE_USER)) {
//                showCreateUserUsage();
//            } else if (command.equals(SHELL_COMMAND_LIST_USER)) {
//                showListUserUsage();
//            } else if (command.equals(SHELL_COMMAND_REMOVE_USER)) {
//                showRemoveUserUsage();
//            } else {
//                showGeneralUsages();
//            }
//        } else {
//            showGeneralUsages();
//        }
//    }
//
//    private static void showGeneralUsages() {
//        StringBuffer sb = new StringBuffer();
//        sb.append("\n");
//        sb.append("Options: \n");
//        sb.append("\t --" + SHELL_COMMAND + "=[");
//        for (int i = 0; i < POSSIBLE_SHELL_COMMANDY.length; i++) {
//            sb.append(POSSIBLE_SHELL_COMMANDY[i]);
//            if (i != (POSSIBLE_SHELL_COMMANDY.length - 1)) {
//                sb.append("|");
//            }
//        }
//        sb.append("] \n");
//
//        System.out.println(sb.toString());
//    }
//
//    private static void showCreateUserUsage() {
//        StringBuffer sb = new StringBuffer();
//        sb.append("Options " + SHELL_COMMAND + "=" + SHELL_COMMAND_CREATE_USER
//                + ":\n");
//
//        sb.append("\t --" + I_OXContext.CONTEXT_ID + "=112\n");
//        sb.append("\t --" + I_OXUser.UID + "=myuser\n");
//        sb.append("\t --" + SHELL_ADMIN_USER + "=oxadmin\n");
//        sb.append("\t --" + SHELL_ADMIN_PASSWORD + "=secret\n");
//        sb.append("\t --" + I_OXUser.DISPLAY_NAME + "=\"This is an User\"\n");
//        sb.append("\t --" + I_OXUser.PASSWORD + "=secret\n");
//        sb.append("\t --" + I_OXUser.GIVEN_NAME + "=John\n");
//        sb.append("\t --" + I_OXUser.SUR_NAME + "=Doe\n");
//        sb.append("\t --" + I_OXUser.PRIMARY_MAIL + "=johny@example.org\n");
//        sb.append("\n");
//        sb.append("\t --" + SHELL_DISABLE_MODULE + "=");
//        String[] acc = I_OXUser.POSSIBLE_ACCESS_RIGHTS;
//        for (int i = 0; i < acc.length; i++) {
//            sb.append(acc[i]);
//            if (i + 1 != acc.length) {
//                sb.append(",");
//            } else {
//                sb.append("\n");
//            }
//        }
//
//        sb.append("\t --" + SHELL_DISABLE_MODULE
//                + "=all (this will disable all modules!)\n");
//
//        System.out.println(sb.toString());
//    }
//
//    private static void showListUserUsage() {
//        StringBuffer sb = new StringBuffer();
//        sb.append("Options " + SHELL_COMMAND + "=" + SHELL_COMMAND_LIST_USER
//                + ":\n");
//        sb.append("\t --" + SHELL_ADMIN_USER + "=oxadmin\n");
//        sb.append("\t --" + SHELL_ADMIN_PASSWORD + "=secret\n");
//
//        sb.append("\t --" + I_OXContext.CONTEXT_ID + "=112\n");
//
//        System.out.println(sb.toString());
//    }
//
//    private static void showRemoveUserUsage() {
//        StringBuffer sb = new StringBuffer();
//        sb.append("Options " + SHELL_COMMAND + "=" + SHELL_COMMAND_REMOVE_USER
//                + ":\n");
//        sb.append("\t --" + SHELL_ADMIN_USER + "=oxadmin\n");
//        sb.append("\t --" + SHELL_ADMIN_PASSWORD + "=secret\n");
//
//        sb.append("\t --" + I_OXContext.CONTEXT_ID + "=112\n");
//        sb.append("\t --" + I_OXUser.UID_NUMBER + "=1\n");
//
//        System.out.println(sb.toString());
//    }
//
//    private void doExit(Exception e) {
//        System.out.println(e);
//        System.exit(1);
//    }
//
//    private void deleteUser(Context ctx, int id, Credentials cred) {
//        try {
//            int[] ids = new int[1];
//            ids[0] = id;
//            ox_user.delete(ctx, ids, cred);
//        } catch (RemoteException e1) {
//            System.out.println("Error deleting user from database");
//            doExit(e1);
//        } catch (StorageException e1) {
//            System.out.println("Error deleting user from database");
//            doExit(e1);
//        } catch (InvalidCredentialsException e1) {
//            System.out.println("Error deleting user from database");
//            doExit(e1);
//        } catch (NoSuchContextException e1) {
//            System.out.println("Error deleting user from database");
//            doExit(e1);
//        } catch (InvalidDataException e1) {
//            System.out.println("Error deleting user from database");
//            doExit(e1);
//        }
//    }
//
//    private void init() throws RemoteException, NotBoundException {
//        registry = LocateRegistry.getRegistry("localhost");
//        ox_user = (OXUserInterface) registry.lookup(OXUserInterface.RMI_NAME);
//    }
//
//    public OXUser() {
//    }
//}
