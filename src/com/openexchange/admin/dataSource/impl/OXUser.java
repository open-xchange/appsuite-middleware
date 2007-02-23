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
package com.openexchange.admin.dataSource.impl;

import com.openexchange.admin.exceptions.OXGenericException;
import com.openexchange.admin.exceptions.PoolException;
import com.openexchange.admin.exceptions.UserException;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.contexts.ContextException;
import com.openexchange.groupware.delete.DeleteFailedException;
import com.openexchange.groupware.ldap.LdapException;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.dataSource.I_OXUser;
import com.openexchange.admin.dataSource.OXUser_MySQL;
import com.openexchange.admin.exceptions.Classes;
import com.openexchange.admin.exceptions.OXContextException;
import com.openexchange.admin.exceptions.OXUserException;
import com.openexchange.admin.exceptions.UserExceptionFactory;
import com.openexchange.admin.properties.AdminProperties;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.AdminDaemonTools;
import com.openexchange.admin.tools.PropertyHandler;
import com.openexchange.admin.tools.monitoring.MonitoringInfos;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.server.DBPoolingException;
import java.sql.SQLException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@OXExceptionSource(classId = Classes.COM_OPENEXCHANGE_ADMIN_DATASOURCE_OXUSER, component = Component.ADMIN_USER)
public class OXUser implements I_OXUser {

    private static final long serialVersionUID = -3220456823559016991L;

    static UserExceptionFactory USER_EXCEPTIONS = new UserExceptionFactory(OXUser.class);

    private AdminCache cache = null;

    private Log log = LogFactory.getLog(this.getClass());

    private PropertyHandler prop = null;

    public OXUser() throws RemoteException {
        super();
        try {
            cache = ClientAdminThread.cache;
            prop = cache.getProperties();
            log.info("Class loaded: " + this.getClass().getName());
        } catch (Exception e) {
            log.error("Error init OXUser", e);
        }
    }

    @OXThrows(category = Category.USER_INPUT, desc = "The data sent by the client is invalid", exceptionId = 0, msg = "Invalid data sent-%s")
    public static void checkCreateUserData(int context_id, Hashtable<String, Object> userData, PropertyHandler prop) throws UserException {
        try {

            AdminDaemonTools.checkNeeded(userData, I_OXUser.REQUIRED_KEYS_CREATE);
            AdminDaemonTools.checkEmpty(userData, I_OXUser.REQUIRED_KEYS_CREATE);
        } catch (OXGenericException ex) {
            throw USER_EXCEPTIONS.create(0, ex.getMessage());
        }

        if (userData.containsKey(I_OXUser.ALIAS) && !(userData.get(I_OXUser.ALIAS) instanceof String[])) {
            throw USER_EXCEPTIONS.create(0, "ALIAS parameter must be of type java.lang.String[]");
        }
        boolean aliases_set = false;
        if (userData.containsKey(I_OXUser.ALIAS) && !(userData.get(I_OXUser.ALIAS) instanceof String[])) {
            throw USER_EXCEPTIONS.create(0, "ALIAS parameter must be of type java.lang.String[]");
        } else {
            if (userData.get(I_OXUser.ALIAS) != null) {
                aliases_set = true;
            }
        }
        if (userData.containsKey(I_OXUser.BIRTHDAY) && !(userData.get(I_OXUser.BIRTHDAY) instanceof java.util.Date)) {
            throw USER_EXCEPTIONS.create(0, "BIRTHDAY parameter must be of type java.util.Date");
        }
        if (userData.containsKey(I_OXUser.ANNIVERSARY) && !(userData.get(I_OXUser.ANNIVERSARY) instanceof java.util.Date)) {
            throw USER_EXCEPTIONS.create(0, "ANNIVERSARY parameter must be of type java.util.Date");
        }

        if (prop.getUserProp(AdminProperties.User.CHECK_NOT_ALLOWED_CHARS, true)) {
            try {
                validateUserName(userData.get(I_OXUser.UID).toString());
            } catch (OXUserException oxu) {
                throw USER_EXCEPTIONS.create(0, oxu.getMessage());
            }
        }

        String uid = (String) userData.get(I_OXUser.UID);
        if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, true)) {
            uid = uid.toLowerCase();
            userData.put(I_OXUser.UID, uid);
        }

        // ### Do some mail attribute checks cause of bug 5444
        // check if primaryemail address is also set in I_OXUser.EMAIL1,
        String[] check_this = { I_OXUser.EMAIL1 };
        String prim_mail = (String) userData.get(I_OXUser.PRIMARY_MAIL);
        int mail_check_counter = 0;
        for (int a = 0; a < check_this.length; a++) {
            if (userData.containsKey(check_this[a])) {
                String tmp_mail_addy = (String) userData.get(check_this[a]);
                if (tmp_mail_addy.equals(prim_mail)) {
                    mail_check_counter++;
                }
            }
        }
        if (mail_check_counter == 0) {
            throw USER_EXCEPTIONS.create(0, "PRIMARY_MAIL value must also be set in I_OXUser.EMAIL1!");
        }
        // ########################################################################################################

        // check if I_OXUser.EMAIL1 is set and if yes, check if value is set in
        // aliases[]
        HashSet tmp_aliases = null;
        if (aliases_set) {
            String[] aliases = (String[]) userData.get(I_OXUser.ALIAS);
            tmp_aliases = new HashSet();
            for (int a = 0; a < aliases.length; a++) {
                tmp_aliases.add(aliases[a]);
            }
        }

        // put primary mail in the aliases[], if aliases not sent, init first
        if (tmp_aliases == null) {
            tmp_aliases = new HashSet();
        }
        tmp_aliases.add(prim_mail);
        String[] checked_aliases = (String[]) tmp_aliases.toArray(new String[tmp_aliases.size()]);
        userData.put(I_OXUser.ALIAS, checked_aliases);

        for (int a = 0; a < check_this.length; a++) {
            if (userData.containsKey(check_this[a]) && ((String) userData.get(check_this[a])).length() > 0) {
                String tmp_mail = (String) userData.get(check_this[a]);
                if (tmp_aliases != null) {
                    if (!tmp_aliases.contains(tmp_mail)) {
                        throw USER_EXCEPTIONS.create(0, check_this[a] + " sent but value does not exists in ALIAS");
                    }
                }
            }
        }

        // ############################################
    }

    @OXThrowsMultiple(category = { Category.USER_INPUT, Category.USER_INPUT, Category.PROGRAMMING_ERROR, Category.PROGRAMMING_ERROR }, desc = { " ", "The user id sent by the client already exists in the system", "SQL query failed to check if the context/user exists", "Error generating password for a new user" }, exceptionId = { 1, 2, 3, 4 }, msg = { OXContextException.NO_SUCH_CONTEXT, OXUserException.USER_EXISTS, OXContext.MSG_SQL_OPERATION_ERROR, "Error generating password" })
    public Vector createUser(int context_id, Hashtable userData, Hashtable moduleAccess) throws RemoteException {
        Vector<Object> retValue = new Vector<Object>();
        log.debug(context_id + " - " + userData + " - " + moduleAccess);
        try {

            if (!AdminDaemonTools.existsContext(context_id)) {
                throw USER_EXCEPTIONS.create(1);
            }

            checkCreateUserData(context_id, userData, prop);

            if (AdminDaemonTools.existsUser(context_id, (String) userData.get(I_OXUser.UID))) {
                throw USER_EXCEPTIONS.create(2);
            }

            // validate email adresss
            AdminDaemonTools.checkPrimaryMail(context_id, userData.get(I_OXUser.PRIMARY_MAIL).toString());

            OXUser_MySQL oxUser = new OXUser_MySQL();
            retValue = oxUser.createUser(context_id, userData, moduleAccess);
            MonitoringInfos.incrementNumberOfCreateUserCalled();
        } catch (SQLException sql) {
            log.error(OXContext.MSG_SQL_OPERATION_ERROR, sql);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add("" + USER_EXCEPTIONS.create(3).getMessage());
        } catch (UserException exp) {
            log.debug(OXContext.LOG_CLIENT_ERROR, exp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add("" + exp.getMessage());
        } catch (PoolException pexp) {
            log.error(OXContext.LOG_PROBLEM_WITH_DB_POOL, pexp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add("" + pexp.getMessage());
        } catch (DBPoolingException pexp) {
            log.error(OXContext.LOG_PROBLEM_WITH_DB_POOL, pexp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add("" + pexp.getMessage());
        } catch (OXException oxexp) {
            log.error("Problem adding rights to ox folders for user", oxexp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add("" + oxexp.getMessage());
        } catch (NoSuchAlgorithmException genexp) {
            log.error("Problem while generating user password", genexp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add("" + USER_EXCEPTIONS.create(4).getMessage());
        }

        log.debug(OXContext.LOG_RESPONSE + retValue);
        return retValue;

    }

    @OXThrowsMultiple(category = { Category.USER_INPUT, Category.USER_INPUT, Category.PROGRAMMING_ERROR }, desc = { " ", "The user id sent by the client does not exist in the system", " " }, exceptionId = { 6, 7, 8 }, msg = { OXContextException.NO_SUCH_CONTEXT + " %s", OXContext.MSG_NO_SUCH_USER_IN_CONTEXT, OXContext.MSG_SQL_OPERATION_ERROR })
    public Vector getUserModuleAccess(int context_id, int user_id) throws RemoteException {
        Vector<Object> retValue = new Vector<Object>();
        log.debug(context_id + " - " + user_id);
        try {

            if (!AdminDaemonTools.existsContext(context_id)) {
                throw USER_EXCEPTIONS.create(6, context_id);
            }

            if (!AdminDaemonTools.existsUser(context_id, user_id)) {
                throw USER_EXCEPTIONS.create(7, user_id, context_id);
            }

            OXUser_MySQL oxUser = new OXUser_MySQL();
            retValue = oxUser.getUserModuleAccess(context_id, user_id);

        } catch (SQLException sql) {
            log.error(OXContext.MSG_SQL_OPERATION_ERROR, sql);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add("" + USER_EXCEPTIONS.create(8).getMessage());
        } catch (PoolException pexp) {
            log.error(OXContext.LOG_PROBLEM_WITH_DB_POOL, pexp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add("" + pexp.getMessage());
        } catch (DBPoolingException pexp) {
            log.error(OXContext.LOG_PROBLEM_WITH_DB_POOL, pexp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add("" + pexp.getMessage());
        } catch (UserException exp) {
            log.debug(OXContext.LOG_CLIENT_ERROR, exp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add("" + exp.getMessage());
        }
        log.debug(OXContext.LOG_RESPONSE + retValue);
        return retValue;
    }

    @OXThrowsMultiple(category = { Category.USER_INPUT, Category.USER_INPUT, Category.PROGRAMMING_ERROR }, desc = { " ", " ", " " }, exceptionId = { 9, 10, 11 }, msg = { OXContextException.NO_SUCH_CONTEXT + " %s", OXContext.MSG_NO_SUCH_USER_IN_CONTEXT, OXContext.MSG_SQL_OPERATION_ERROR })
    public Vector changeUserModuleAccess(int context_id, int user_id, Hashtable moduleAccess) throws RemoteException {
        Vector<String> retValue = new Vector<String>();
        log.debug(context_id + " - " + user_id + " - " + moduleAccess);
        try {

            if (!AdminDaemonTools.existsContext(context_id)) {
                throw USER_EXCEPTIONS.create(9, context_id);
            }

            if (!AdminDaemonTools.existsUser(context_id, user_id)) {
                throw USER_EXCEPTIONS.create(10, user_id, context_id);
            }

            OXUser_MySQL oxUser = new OXUser_MySQL();
            retValue = oxUser.changeUserModuleAccess(context_id, user_id, moduleAccess);
        } catch (SQLException sql) {
            log.error(OXContext.MSG_SQL_OPERATION_ERROR, sql);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add("" + USER_EXCEPTIONS.create(11).getMessage());
        } catch (UserException exp) {
            log.debug(OXContext.LOG_CLIENT_ERROR, exp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add("" + exp.getMessage());
        } catch (PoolException pexp) {
            log.error(OXContext.LOG_PROBLEM_WITH_DB_POOL, pexp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add("" + pexp.getMessage());
        } catch (DBPoolingException pexp) {
            log.error(OXContext.LOG_PROBLEM_WITH_DB_POOL, pexp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add("" + pexp.getMessage());
        }
        log.debug(OXContext.LOG_RESPONSE + retValue);
        return retValue;
    }

    @OXThrowsMultiple(category = { Category.USER_INPUT, Category.PROGRAMMING_ERROR }, desc = { " ", "SQL query failed to get all users for a context!" }, exceptionId = { 12, 13 }, msg = { OXContextException.NO_SUCH_CONTEXT + " %s", OXContext.MSG_SQL_OPERATION_ERROR })
    public Vector getAllUsers(int context_id) throws RemoteException {
        Vector<Object> retValue = new Vector<Object>();
        log.debug(context_id);
        try {

            if (!AdminDaemonTools.existsContext(context_id)) {
                throw USER_EXCEPTIONS.create(12, context_id);
            }

            OXUser_MySQL oxUser = new OXUser_MySQL();
            retValue = oxUser.getAllUsers(context_id);
        } catch (SQLException sql) {
            log.error(OXContext.MSG_SQL_OPERATION_ERROR, sql);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add("" + USER_EXCEPTIONS.create(13).getMessage());
        } catch (UserException exp) {
            log.debug(OXContext.LOG_CLIENT_ERROR, exp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add("" + exp.getMessage());
        } catch (PoolException pexp) {
            log.error(OXContext.LOG_PROBLEM_WITH_DB_POOL, pexp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add("" + pexp.getMessage());
        }

        log.debug(OXContext.LOG_RESPONSE + retValue);
        return retValue;
    }

    @OXThrowsMultiple(category = { Category.USER_INPUT, Category.USER_INPUT, Category.PROGRAMMING_ERROR }, desc = { " ", " ", " " }, exceptionId = { 14, 15, 16 }, msg = { OXContextException.NO_SUCH_CONTEXT + " %s", OXContext.MSG_NO_SUCH_USER_IN_CONTEXT, OXContext.MSG_SQL_OPERATION_ERROR })
    public Vector getUserData(int context_id, String uid) throws RemoteException {
        Vector<Object> retValue = new Vector<Object>();
        log.debug(context_id + " - " + uid);
        try {

            if (!AdminDaemonTools.existsContext(context_id)) {
                throw USER_EXCEPTIONS.create(14, context_id);
            }

            if (!AdminDaemonTools.existsUser(context_id, uid)) {
                throw USER_EXCEPTIONS.create(15, uid, context_id);
            }

            OXUser_MySQL oxUser = new OXUser_MySQL();
            retValue = oxUser.getUserData(context_id, uid);
        } catch (SQLException sql) {
            log.error(OXContext.MSG_SQL_OPERATION_ERROR, sql);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add("" + USER_EXCEPTIONS.create(16).getMessage());
        } catch (UserException exp) {
            log.debug(OXContext.LOG_CLIENT_ERROR, exp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add("" + exp.getMessage());
        } catch (PoolException pexp) {
            log.error(OXContext.LOG_PROBLEM_WITH_DB_POOL, pexp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add("" + pexp.getMessage());
        }

        log.debug(OXContext.LOG_RESPONSE + retValue);
        return retValue;
    }

    @OXThrowsMultiple(category = { Category.USER_INPUT, Category.USER_INPUT, Category.PROGRAMMING_ERROR }, desc = { " ", " ", " " }, exceptionId = { 17, 18, 19 }, msg = { OXContextException.NO_SUCH_CONTEXT + " %s", OXContext.MSG_NO_SUCH_USER_IN_CONTEXT, OXContext.MSG_SQL_OPERATION_ERROR })
    public Vector getUserData(int context_id, int user_id) throws RemoteException {
        Vector<Object> retValue = new Vector<Object>();
        log.debug(context_id + " - " + user_id);
        try {

            if (!AdminDaemonTools.existsContext(context_id)) {
                throw USER_EXCEPTIONS.create(17, context_id);
            }

            if (!AdminDaemonTools.existsUser(context_id, user_id)) {
                throw USER_EXCEPTIONS.create(18, user_id, context_id);
            }

            OXUser_MySQL oxUser = new OXUser_MySQL();
            retValue = oxUser.getUserData(context_id, user_id);
        } catch (UserException exp) {
            log.debug(OXContext.LOG_CLIENT_ERROR, exp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add("" + exp.getMessage());
        } catch (SQLException sql) {
            log.error(OXContext.MSG_SQL_OPERATION_ERROR, sql);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add("" + USER_EXCEPTIONS.create(19).getMessage());
        } catch (PoolException pexp) {
            log.error(OXContext.LOG_PROBLEM_WITH_DB_POOL, pexp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add("" + pexp.getMessage());
        }

        log.debug(OXContext.LOG_RESPONSE + retValue);
        return retValue;
    }

    @OXThrowsMultiple(category = { Category.USER_INPUT, Category.USER_INPUT, Category.PROGRAMMING_ERROR }, desc = { " ", " ", " " }, exceptionId = { 20, 21, 22 }, msg = { OXContextException.NO_SUCH_CONTEXT + " %s", OXContext.MSG_NO_SUCH_USER_IN_CONTEXT, OXContext.MSG_SQL_OPERATION_ERROR })
    public Vector changeUserData(int context_id, int user_id, Hashtable userData) throws RemoteException {
        Vector<String> retValue = new Vector<String>();
        log.debug(context_id + " - " + user_id + " - " + userData);
        try {

            if (!AdminDaemonTools.existsContext(context_id)) {
                throw USER_EXCEPTIONS.create(20, context_id);
            }

            if (!AdminDaemonTools.existsUser(context_id, user_id)) {
                throw USER_EXCEPTIONS.create(21, user_id, context_id);
            }

            if (userData.containsKey(I_OXUser.UID) && prop.getGroupProp(AdminProperties.User.AUTO_LOWERCASE, true)) {
                String uid = userData.get(I_OXUser.UID).toString().toLowerCase();
                userData.put(I_OXUser.UID, uid);
            }

            checkChangeUserData(context_id, userData, prop);

            OXUser_MySQL oxUser = new OXUser_MySQL();
            retValue = oxUser.changeUserData(context_id, user_id, userData);
        } catch (UserException exp) {
            log.debug(OXContext.LOG_CLIENT_ERROR, exp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add("" + exp.getMessage());
        } catch (SQLException sql) {
            log.error(OXContext.MSG_SQL_OPERATION_ERROR, sql);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add("" + USER_EXCEPTIONS.create(22).getMessage());
        } catch (PoolException pexp) {
            log.error(OXContext.LOG_PROBLEM_WITH_DB_POOL, pexp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add("" + pexp.getMessage());
        }

        log.debug(OXContext.LOG_RESPONSE + retValue);
        return retValue;
    }

    @OXThrows(category = Category.USER_INPUT, desc = "The data sent by the client is invalid", exceptionId = 23, msg = "Invalid data sent-%s")
    public static void checkChangeUserData(int context_id, Hashtable userData, PropertyHandler prop) throws UserException {

        // MAIL ATTRIBUTE CHANGE NOT SUPPORTTED FOR 1und1 - DISABLED CAUSE OF
        // MEETING 13.12.2006
        // if( userData.containsKey(I_OXUser.PRIMARY_MAIL)) {
        // throw USER_EXCEPTIONS.create(23,"Changing mail attribute not
        // allowed");
        // }

        if (userData.containsKey(I_OXUser.ALIAS) && !(userData.get(I_OXUser.ALIAS) instanceof String[])) {
            throw USER_EXCEPTIONS.create(23, "ALIAS parameter must be of type java.lang.String[]");
        }
        if (userData.containsKey(I_OXUser.BIRTHDAY) && !(userData.get(I_OXUser.BIRTHDAY) instanceof java.util.Date)) {
            throw USER_EXCEPTIONS.create(23, "BIRTHDAY parameter must be of type java.util.Date");
        }
        if (userData.containsKey(I_OXUser.ANNIVERSARY) && !(userData.get(I_OXUser.ANNIVERSARY) instanceof java.util.Date)) {
            throw USER_EXCEPTIONS.create(23, "ANNIVERSARY parameter must be of type java.util.Date");
        }
        // till we do not update the uid in login2user we disable this check
        // if ( prop.getUserProp( PropertiesUser.CHECK_NOT_ALLOWED_CHARS, true )
        // ) {
        // try{
        // AdminDaemonTools.validateUserName( userData.get( I_OXUser.UID
        // ).toString() );
        // }catch(OXUserException oxu){
        // throw USER_EXCEPTIONS.create(23,oxu.getMessage());
        // }
        // }

    }

    @OXThrowsMultiple(category = { Category.USER_INPUT, Category.USER_INPUT, Category.PROGRAMMING_ERROR, Category.USER_INPUT }, desc = { " ", " ", " ", "" }, exceptionId = { 24, 25, 26, 27 }, msg = { OXContextException.NO_SUCH_CONTEXT + " %s", OXContext.MSG_NO_SUCH_USER_IN_CONTEXT, OXContext.MSG_SQL_OPERATION_ERROR, "%s" })
    public Vector deleteUser(int context_id, int user_id) throws RemoteException {
        Vector<Object> retValue = new Vector<Object>();
        log.debug(context_id + " - " + user_id);
        try {

            if (!AdminDaemonTools.existsContext(context_id)) {
                throw USER_EXCEPTIONS.create(24, context_id);
            }

            if (!AdminDaemonTools.existsUser(context_id, user_id)) {
                throw USER_EXCEPTIONS.create(25, user_id, context_id);
            }

            if (AdminDaemonTools.isContextAdmin(context_id, user_id)) {
                throw USER_EXCEPTIONS.create(27, OXUserException.ADMIN_DELETE_NOT_SUPPORTED);
            }

            OXUser_MySQL oxUser = new OXUser_MySQL();
            retValue = oxUser.deleteUser(context_id, user_id);
        } catch (UserException exp) {
            log.debug(OXContext.LOG_CLIENT_ERROR, exp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add("" + exp.getMessage());
        } catch (SQLException sql) {
            log.error(OXContext.MSG_SQL_OPERATION_ERROR, sql);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add("" + USER_EXCEPTIONS.create(26).getMessage());
        } catch (PoolException pexp) {
            log.error(OXContext.LOG_PROBLEM_WITH_DB_POOL, pexp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add("" + pexp.getMessage());
        } catch (DBPoolingException pexp2) {
            log.error(OXContext.LOG_PROBLEM_WITH_DB_POOL, pexp2);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add("" + pexp2.getMessage());
        } catch (ContextException pexp3) {
            log.error("Context error in OX delete API ", pexp3);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add("" + pexp3.getMessage());
        } catch (DeleteFailedException pexp4) {
            log.error("Delete error in OX delete API ", pexp4);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add("" + pexp4.getMessage());
        } catch (LdapException pexp5) {
            log.error("Delete error in OX delete API ", pexp5);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add("" + pexp5.getMessage());
        }

        log.debug(OXContext.LOG_RESPONSE + retValue);
        return retValue;
    }

    public static String getLanguage(Hashtable userData) {
        String lang = "en_US";
        if (userData.containsKey(I_OXUser.LANGUAGE)) {
            lang = (String) userData.get(I_OXUser.LANGUAGE);

        }
        return lang;
    }

    public static String getDisplayName(Hashtable<String, String> userData) {
        String display = (String) userData.get(I_OXUser.UID);
        if (userData.containsKey(I_OXUser.DISPLAY_NAME)) {
            display = (String) userData.get(I_OXUser.DISPLAY_NAME);
        } else {
            if (userData.containsKey(I_OXUser.GIVEN_NAME)) {
                // SET THE DISPLAYNAME AS NEEDED BY CUSTOMER, SHOULD BE DEFINED
                // ON SERVER SIDE
                display = (String) userData.get(I_OXUser.GIVEN_NAME) + " " + (String) userData.get(I_OXUser.SUR_NAME);

            } else {
                display = (String) userData.get(I_OXUser.SUR_NAME);
            }
            userData.remove(I_OXUser.DISPLAY_NAME);
            userData.put(I_OXUser.DISPLAY_NAME, display);
        }
        return display;
    }

    private static void validateUserName(String userName) throws OXUserException {
        // Check for allowed chars:
        // abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-+.%$@
        String illegal = userName.replaceAll("[$@%\\.+a-zA-Z0-9_-]", "");
        if (illegal.length() > 0) {
            throw new OXUserException(OXUserException.ILLEGAL_CHARS + ": \"" + illegal + "\"");
        }
    }
}
