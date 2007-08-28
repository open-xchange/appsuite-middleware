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
package com.openexchange.admin.storage.mysqlStorage;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.configuration.ConversionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.properties.AdminProperties;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.impl.OXUser;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;
import com.openexchange.admin.storage.sqlStorage.OXUserSQLStorage;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.SHACrypt;
import com.openexchange.admin.tools.UnixCrypt;
import com.openexchange.admin.tools.PropertyHandler.PropertyFiles;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.IDGenerator;
import com.openexchange.groupware.RdbUserConfigurationStorage;
import com.openexchange.groupware.UserConfiguration;
import com.openexchange.groupware.contexts.ContextException;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedException;
import com.openexchange.imap.UserSettingMail;
import com.openexchange.server.DBPoolingException;
import com.openexchange.tools.oxfolder.OXFolderAdminHelper;

/**
 * @author cutmasta
 * @author d7
 */
public class OXUserMySQLStorage extends OXUserSQLStorage implements OXMySQLDefaultValues {

    private class MethodAndNames {
        private Method method;

        private String name;

        /**
         * @param method
         * @param name
         */
        public MethodAndNames(final Method method, final String name) {
            super();
            this.method = method;
            this.name = name;
        }

        public Method getMethod() {
            return this.method;
        }

        public void setMethod(final Method method) {
            this.method = method;
        }

        public String getName() {
            return this.name;
        }

        public void setName(final String name) {
            this.name = name;
        }

    }

    private static final Log log = LogFactory.getLog(OXUserMySQLStorage.class);

    // DEFAULTS FOR USER CREATE; SHOULD BE MOVED TO PROPERTIES FILE
    private static final String DEFAULT_TIMEZONE_CREATE = "Europe/Berlin";

    private static final String DEFAULT_SMTP_SERVER_CREATE = "localhost";

    private static final String DEFAULT_IMAP_SERVER_CREATE = "localhost";

    public OXUserMySQLStorage() {
    }

    @Override
    public void change(final Context ctx, final User usrdata) throws StorageException {
        Connection write_ox_con = null;
        PreparedStatement stmt = null;
        PreparedStatement folder_update = null;
        final int context_id = ctx.getId();
        final int user_id = usrdata.getId();
        try {

            // first fill the user_data hash to update user table
            write_ox_con = cache.getWRITEConnectionForContext(context_id);
            write_ox_con.setAutoCommit(false);

            if (usrdata.getPrimaryEmail() != null) {
                stmt = write_ox_con.prepareStatement("UPDATE user SET mail = ? WHERE cid = ? AND id = ?");
                stmt.setString(1, usrdata.getPrimaryEmail());
                stmt.setInt(2, context_id);
                stmt.setInt(3, user_id);
                stmt.executeUpdate();
                stmt.close();
            }

            if (usrdata.getLanguage() != null) {
                stmt = write_ox_con.prepareStatement("UPDATE user SET preferredlanguage = ? WHERE cid = ? AND id = ?");
                stmt.setString(1, usrdata.getLanguage().getLanguage() + "_"
                        + usrdata.getLanguage().getCountry());
                stmt.setInt(2, context_id);
                stmt.setInt(3, user_id);
                stmt.executeUpdate();
                stmt.close();
            }

            if (usrdata.getTimezone() != null) {
                stmt = write_ox_con.prepareStatement("UPDATE user SET timezone = ? WHERE cid = ? AND id = ?");
                stmt.setString(1, usrdata.getTimezone().getID());
                stmt.setInt(2, context_id);
                stmt.setInt(3, user_id);
                stmt.executeUpdate();
                stmt.close();
            }

            if (usrdata.getMailenabled() != null) {
                stmt = write_ox_con.prepareStatement("UPDATE user SET mailEnabled = ? WHERE cid = ? AND id = ?");
                stmt.setBoolean(1, usrdata.getMailenabled().booleanValue());
                stmt.setInt(2, context_id);
                stmt.setInt(3, user_id);
                stmt.executeUpdate();
                stmt.close();
            }

            if (usrdata.getPassword_expired() != null) {
                stmt = write_ox_con.prepareStatement("UPDATE user SET  shadowLastChange = ? WHERE cid = ? AND id = ?");
                stmt.setInt(1, getintfrombool(usrdata.getPassword_expired()));
                stmt.setInt(2, context_id);
                stmt.setInt(3, user_id);
                stmt.executeUpdate();
                stmt.close();
            }

            if (usrdata.getImapServer() == null && usrdata.isImapServerset() ) {
                stmt = write_ox_con.prepareStatement("UPDATE user SET  imapserver = ? WHERE cid = ? AND id = ?");
                stmt.setNull(1, java.sql.Types.VARCHAR);
                stmt.setInt(2, context_id);
                stmt.setInt(3, user_id);
                stmt.executeUpdate();
                stmt.close();
            } else if (usrdata.getImapServer() != null) {
                stmt = write_ox_con.prepareStatement("UPDATE user SET  imapserver = ? WHERE cid = ? AND id = ?");
                stmt.setString(1, usrdata.getImapServer());
                stmt.setInt(2, context_id);
                stmt.setInt(3, user_id);
                stmt.executeUpdate();
                stmt.close();
            }

            if (usrdata.getImapLogin() == null && usrdata.isImapLoginset() ) {
                stmt = write_ox_con.prepareStatement("UPDATE user SET  imapLogin = ? WHERE cid = ? AND id = ?");
                stmt.setNull(1, java.sql.Types.VARCHAR);
                stmt.setInt(2, context_id);
                stmt.setInt(3, user_id);
                stmt.executeUpdate();
                stmt.close();
            } else if(usrdata.getImapLogin() != null){
                stmt = write_ox_con.prepareStatement("UPDATE user SET  imapLogin = ? WHERE cid = ? AND id = ?");
                stmt.setString(1, usrdata.getImapLogin());
                stmt.setInt(2, context_id);
                stmt.setInt(3, user_id);
                stmt.executeUpdate();
                stmt.close();
            }

            if (usrdata.getSmtpServer() == null && usrdata.isSmtpServerset() ) {
                stmt = write_ox_con.prepareStatement("UPDATE user SET  smtpserver = ? WHERE cid = ? AND id = ?");
                stmt.setNull(1, java.sql.Types.VARCHAR);
                stmt.setInt(2, context_id);
                stmt.setInt(3, user_id);
                stmt.executeUpdate();
                stmt.close();
            } else if (usrdata.getSmtpServer() != null) {
                stmt = write_ox_con.prepareStatement("UPDATE user SET  smtpserver = ? WHERE cid = ? AND id = ?");
                stmt.setString(1, usrdata.getSmtpServer());
                stmt.setInt(2, context_id);
                stmt.setInt(3, user_id);
                stmt.executeUpdate();
                stmt.close();
            }

            if (usrdata.getPassword() != null) {
                stmt = write_ox_con.prepareStatement("UPDATE user SET  userPassword = ? WHERE cid = ? AND id = ?");
                stmt.setString(1, password2crypt(usrdata));
                stmt.setInt(2, context_id);
                stmt.setInt(3, user_id);
                stmt.executeUpdate();
                stmt.close();
            }

            if (usrdata.getPasswordMech() != null) {
                stmt = write_ox_con.prepareStatement("UPDATE user SET  passwordMech = ? WHERE cid = ? AND id = ?");
                stmt.setString(1, usrdata.getPasswordMech2String());
                stmt.setInt(2, context_id);
                stmt.setInt(3, user_id);
                stmt.executeUpdate();
                stmt.close();
            }

            // update user aliases
            final HashSet<String> alias = usrdata.getAliases();
            if (null != alias) {
                stmt = write_ox_con.prepareStatement("DELETE FROM user_attribute WHERE cid = ? AND id = ?" + " AND name = \"alias\"");
                stmt.setInt(1, context_id);
                stmt.setInt(2, user_id);
                stmt.executeUpdate();
                stmt.close();
                for (final String elem : alias) {
                    if(elem!=null && elem.trim().length()>0){
                        stmt = write_ox_con.prepareStatement("INSERT INTO user_attribute (cid,id,name,value) VALUES (?,?,?,?)");
                        stmt.setInt(1, context_id);
                        stmt.setInt(2, user_id);
                        stmt.setString(3, "alias");
                        stmt.setString(4, elem);
                        stmt.executeUpdate();
                        stmt.close();
                    }
                }
            } else if (usrdata.isAliasesset()){
                stmt = write_ox_con.prepareStatement("DELETE FROM user_attribute WHERE cid = ? AND id = ?" + " AND name = \"alias\"");
                stmt.setInt(1, context_id);
                stmt.setInt(2, user_id);
                stmt.executeUpdate();
                stmt.close();
            }

            // update prg_contacts ONLY if needed ( see
            // "prg_contacts_update_needed")
            final Class<? extends User> c = usrdata.getClass();
            final Method[] theMethods = c.getMethods();
            final HashSet<String> notallowed = new HashSet<String>(9);
            // Define all those fields which are contained in the user table
            notallowed.add("Id");
            notallowed.add("Password");
            notallowed.add("PrimaryEmail");
            notallowed.add("TimeZone");
            notallowed.add("Enabled");
            notallowed.add("ImapServer");
            notallowed.add("ImapLogin");
            notallowed.add("SmtpServer");
            notallowed.add("Password_expired");
            notallowed.add("Locale");
            notallowed.add("Spam_filter_enabled");

            final ArrayList<MethodAndNames> methodlist = getGetters(theMethods);

            final StringBuilder contact_query = new StringBuilder(
                    "UPDATE prg_contacts SET ");

            final ArrayList<Method> methodlist2 = new ArrayList<Method>();
            final ArrayList<String> returntypes = new ArrayList<String>();

            boolean prg_contacts_update_needed = false;

            for (final MethodAndNames methodandname : methodlist) {
                // First we have to check which return value we have. We have to
                // distinguish four types
                final Method method = methodandname.getMethod();
                final Method methodbool = getMethodforbooleanparameter(method);
                boolean test = (Boolean) methodbool.invoke(usrdata, (Object[]) null);
                final String methodname = methodandname.getName();
                final String returntype = method.getReturnType().getName();
                if (returntype.equalsIgnoreCase("java.lang.String")) {
                    final String result = (java.lang.String) method.invoke(usrdata, (Object[]) null);
                    if (null != result || test) {
                        contact_query.append(Mapper.method2field.get(methodname));
                        contact_query.append(" = ?, ");
                        methodlist2.add(method);
                        returntypes.add(returntype);
                        prg_contacts_update_needed = true;
                    }
                } else if (returntype.equalsIgnoreCase("java.lang.Integer")) {
                    final int result = (Integer) method.invoke(usrdata, (Object[]) null);
                    if (-1 != result || test) {
                        contact_query.append(Mapper.method2field.get(methodname));
                        contact_query.append(" = ?, ");
                        methodlist2.add(method);
                        returntypes.add(returntype);
                        prg_contacts_update_needed = true;
                    }
                } else if (returntype.equalsIgnoreCase("java.lang.Boolean")) {
                    final Boolean result = (Boolean) method.invoke(usrdata, (Object[]) null);
                    if (null != result || test) {
                        contact_query.append(Mapper.method2field.get(methodname));
                        contact_query.append(" = ?, ");
                        methodlist2.add(method);
                        returntypes.add(returntype);
                        prg_contacts_update_needed = true;
                    }
                } else if (returntype.equalsIgnoreCase("java.util.Date")) {
                    final Date result = (Date) method.invoke(usrdata, (Object[]) null);
                    if (null != result || test) {
                        contact_query.append(Mapper.method2field.get(methodname));
                        contact_query.append(" = ?, ");
                        methodlist2.add(method);
                        returntypes.add(returntype);
                        prg_contacts_update_needed = true;
                    }
                }
            }

            // onyl if min. 1 field set , exeute update on contact table
            if (prg_contacts_update_needed) {

                contact_query.delete(contact_query.length() - 2, contact_query.length() - 1);
                contact_query.append(" WHERE cid = ? AND userid = ?");

                stmt = write_ox_con.prepareStatement(contact_query.toString());

                for (int i = 0; i < methodlist2.size(); i++) {
                    final int db = 1 + i;
                    final Method method = methodlist2.get(i);
                    final String returntype = returntypes.get(i);
                    if (returntype.equalsIgnoreCase("java.lang.String")) {
                        final String result = (java.lang.String) method.invoke(usrdata, (Object[]) null);
                        if (null != result) {
                            stmt.setString(db, result);
                        } else {
                            final Method methodbool = getMethodforbooleanparameter(method);
                            final boolean test = (Boolean) methodbool.invoke(usrdata, (Object[]) null);
                            if (test) {
                                stmt.setNull(db, java.sql.Types.VARCHAR);
                            }
                        }
                    } else if (returntype.equalsIgnoreCase("java.lang.Integer")) {
                        final int result = (Integer) method.invoke(usrdata, (Object[]) null);
                        if (-1 != result) {
                            stmt.setInt(db, result);
                        } else {
                            final Method methodbool = getMethodforbooleanparameter(method);
                            final boolean test = (Boolean) methodbool.invoke(usrdata, (Object[]) null);
                            if (test) {
                                stmt.setNull(db, java.sql.Types.INTEGER);
                            }
                        }
                    } else if (returntype.equalsIgnoreCase("java.lang.Boolean")) {
                        final boolean result = (Boolean) method.invoke(usrdata, (Object[]) null);
                        stmt.setBoolean(db, result);
                    } else if (returntype.equalsIgnoreCase("java.util.Date")) {
                        final Date result = (java.util.Date) method.invoke(usrdata, (Object[]) null);
                        if (null != result) {
                            stmt.setDate(db, new java.sql.Date(result.getTime()));
                        } else {
                            final Method methodbool = getMethodforbooleanparameter(method);
                            final boolean test = (Boolean) methodbool.invoke(usrdata, (Object[]) null);
                            if (test) {
                                stmt.setNull(db, java.sql.Types.DATE);
                            }
                        }
                    }
                    // TODO: d7 rewrite log
                    // log.debug("******************* " +
                    // user_data.get(CONTACT_FIELDS[f]).toString() + " / " +
                    // Contacts.mapping[cfield].getDBFieldName() + " / " +
                    // cfield);
                }

                stmt.setInt(methodlist2.size() + 1, context_id);
                stmt.setInt(methodlist2.size() + 2, user_id);
                stmt.executeUpdate();
                stmt.close();

            }

            
            final Boolean spam_filter_enabled = usrdata.getGUI_Spam_filter_capabilities_enabled();
            if(null != spam_filter_enabled ) {
                final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
                if( spam_filter_enabled ) {
                    tool.setUserSettingMailBit(ctx, usrdata, UserSettingMail.INT_SPAM_ENABLED, write_ox_con);
                } else {
                    tool.unsetUserSettingMailBit(ctx, usrdata, UserSettingMail.INT_SPAM_ENABLED, write_ox_con);
                }
            }
            
            // update the mailfolder mapping
            final String send_addr = usrdata.getPrimaryEmail();
            if (null != send_addr) {
                folder_update = write_ox_con
                        .prepareStatement("UPDATE user_setting_mail SET send_addr = ? WHERE cid = ? AND user = ?");
                folder_update.setString(1, send_addr);
                folder_update.setInt(2, context_id);
                folder_update.setInt(3, user_id);
                folder_update.executeUpdate();
                folder_update.close();
            }
            final String mailfolderdrafts = usrdata
                    .getMail_folder_drafts_name();
            if (null != mailfolderdrafts) {
                folder_update = write_ox_con
                        .prepareStatement("UPDATE user_setting_mail SET std_drafts = ? WHERE cid = ? AND user = ?");
                folder_update.setString(1, mailfolderdrafts);
                folder_update.setInt(2, context_id);
                folder_update.setInt(3, user_id);
                folder_update.executeUpdate();
                folder_update.close();
            }
            final String mailfoldersent = usrdata.getMail_folder_sent_name();
            if (null != mailfoldersent) {
                folder_update = write_ox_con
                        .prepareStatement("UPDATE user_setting_mail SET std_sent = ? WHERE cid = ? AND user = ?");
                folder_update.setString(1, mailfoldersent);
                folder_update.setInt(2, context_id);
                folder_update.setInt(3, user_id);
                folder_update.executeUpdate();
                folder_update.close();
            }
            final String mailfolderspam = usrdata.getMail_folder_spam_name();
            if (null != mailfolderspam) {
                folder_update = write_ox_con
                        .prepareStatement("UPDATE user_setting_mail SET std_spam = ? WHERE cid = ? AND user = ?");
                folder_update.setString(1, mailfolderspam);
                folder_update.setInt(2, context_id);
                folder_update.setInt(3, user_id);
                folder_update.executeUpdate();
                folder_update.close();
            }
            final String mailfoldertrash = usrdata.getMail_folder_trash_name();
            if (null != mailfoldertrash) {
                folder_update = write_ox_con
                        .prepareStatement("UPDATE user_setting_mail SET std_trash = ? WHERE cid = ? AND user = ?");
                folder_update.setString(1, mailfoldertrash);
                folder_update.setInt(2, context_id);
                folder_update.setInt(3, user_id);
                folder_update.executeUpdate();
                folder_update.close();
            }
            final String mailfolderconfirmedspam = usrdata.getMail_folder_confirmed_spam_name();
            if (null != mailfolderconfirmedspam) {
                folder_update = write_ox_con
                        .prepareStatement("UPDATE user_setting_mail SET confirmed_spam = ? WHERE cid = ? AND user = ?");
                folder_update.setString(1, mailfolderconfirmedspam);
                folder_update.setInt(2, context_id);
                folder_update.setInt(3, user_id);
                folder_update.executeUpdate();
                folder_update.close();
            }
            final String mailfolderconfirmedham = usrdata.getMail_folder_confirmed_ham_name();
            if (null != mailfolderconfirmedham) {
                folder_update = write_ox_con
                        .prepareStatement("UPDATE user_setting_mail SET confirmed_ham = ? WHERE cid = ? AND user = ?");
                folder_update.setString(1, mailfolderconfirmedham);
                folder_update.setInt(2, context_id);
                folder_update.setInt(3, user_id);
                folder_update.executeUpdate();
                folder_update.close();
            }

            if (folder_update != null) {
                folder_update.close();
            }

            // update last modified column
            changeLastModified(user_id, ctx, write_ox_con);
            
            
            
            // fire up
            write_ox_con.commit();
        }catch (final DataTruncation dt){
            log.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            try {
                write_ox_con.rollback();
            } catch (final SQLException e2) {
                log.error("Error doing rollback", e2);
            }
            throw AdminCache.parseDataTruncation(dt);
        } catch (final SQLException e) {
            log.error("SQL Error", e);
            try {
                write_ox_con.rollback();
            } catch (final SQLException e2) {
                log.error("Error doing rollback", e2);
            }
            throw new StorageException(e);
        } catch (final PoolException e) {
            log.error("Pool Error", e);
            try {
                write_ox_con.rollback();
            } catch (final SQLException e2) {
                log.error("Error doing rollback", e2);
            }
            throw new StorageException(e);
        } catch (final IllegalArgumentException e) {
            log.error("Error", e);
            try {
                write_ox_con.rollback();
            } catch (final SQLException e2) {
                log.error("Error doing rollback", e2);
            }
            throw new StorageException(e);
        } catch (final IllegalAccessException e) {
            log.error("Error", e);
            try {
                write_ox_con.rollback();
            } catch (final SQLException e2) {
                log.error("Error doing rollback", e2);
            }
            throw new StorageException(e);
        } catch (final InvocationTargetException e) {
            log.error("Error", e);
            try {
                write_ox_con.rollback();
            } catch (final SQLException e2) {
                log.error("Error doing rollback", e2);
            }
            throw new StorageException(e);
        } catch (final SecurityException e) {
            log.error("Error", e);
            try {
                write_ox_con.rollback();
            } catch (final SQLException e2) {
                log.error("Error doing rollback", e2);
            }
            throw new StorageException(e);
        } catch (final NoSuchMethodException e) {
            log.error("Error", e);
            try {
                write_ox_con.rollback();
            } catch (final SQLException e2) {
                log.error("Error doing rollback", e2);
            }
            throw new StorageException(e);
        } catch (final NoSuchAlgorithmException e) {
            log.error("Error", e);
            try {
                write_ox_con.rollback();
            } catch (final SQLException e2) {
                log.error("Error doing rollback", e2);
            }
            throw new StorageException(e);
        } catch (UnsupportedEncodingException e) {
            log.error("Error", e);
            try {
                write_ox_con.rollback();
            } catch (final SQLException e2) {
                log.error("Error doing rollback", e2);
            }
            throw new StorageException(e);       
        } finally {
            try {
                if (folder_update != null) {
                    folder_update.close();
                }
            } catch (final SQLException e) {
                log.error("SQL Error closing statement", e);
            }
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (final SQLException e) {
                log.error("SQL Error closing statement", e);
            }

            try {
                if (write_ox_con != null) {
                    cache.pushOXDBWrite(context_id, write_ox_con);
                }
            } catch (final PoolException exp) {
                log.error("Pool Error pushing ox write connection to pool!",
                        exp);
            }
        }
    }

    /**
     * @param user
     * @return
     * @throws StorageException
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException 
     */
    private String password2crypt(final User user) throws StorageException,
            NoSuchAlgorithmException, UnsupportedEncodingException {
        String passwd = null;
        if (user.getPasswordMech() == null) {
            // TODO: configurable in AdminDaemon.properties
            user.setPasswordMech(User.PASSWORDMECH.CRYPT);
        }
        if (user.getPasswordMech() == User.PASSWORDMECH.CRYPT) {
            passwd = UnixCrypt.crypt(user.getPassword());
        } else if (user.getPasswordMech() == User.PASSWORDMECH.SHA) {
            passwd = SHACrypt.makeSHAPasswd(user.getPassword());
        } else {
            throw new StorageException("unsupported password mechanism: "
                    + user.getPasswordMech());
        }
        return passwd;
    }

    @Override
    public int create(final Context ctx, final User usrdata, final UserModuleAccess moduleAccess, final Connection write_ox_con, final int internal_user_id, final int contact_id, final int uid_number) throws StorageException {
        PreparedStatement ps = null;
        PreparedStatement return_db_id = null;
        final String LOGINSHELL = "/bin/bash";
        
        try {
            ps = write_ox_con.prepareStatement("SELECT user FROM user_setting_admin WHERE cid=?");
            ps.setInt(1, ctx.getId());
            ResultSet rs = ps.executeQuery();
            int admin_id = 0;
            boolean mustMapAdmin = false;
            if (rs.next()) {
                admin_id = rs.getInt("user");
            } else {
                admin_id = internal_user_id;
                mustMapAdmin = true;
            }
            rs.close();

            final String passwd = password2crypt(usrdata);

            PreparedStatement stmt = null;
            try {
                stmt = write_ox_con.prepareStatement("INSERT INTO user (cid,id,userPassword,passwordMech,shadowLastChange,mail,timeZone,preferredLanguage,mailEnabled,imapserver,smtpserver,contactId,homeDirectory,uidNumber,gidNumber,loginShell,imapLogin) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                stmt.setInt(1, ctx.getId().intValue());
                stmt.setInt(2, internal_user_id);
                stmt.setString(3, passwd);
                stmt.setString(4, usrdata.getPasswordMech2String());

                if (usrdata.getPassword_expired() == null) {
                    usrdata.setPassword_expired(false);
                }
                stmt.setInt(5, getintfrombool(usrdata.getPassword_expired()));

                stmt.setString(6, usrdata.getPrimaryEmail());

                final TimeZone timezone = usrdata.getTimezone();
                if (null != timezone) {
                    stmt.setString(7, timezone.getID());
                } else {
                    stmt.setString(7, DEFAULT_TIMEZONE_CREATE);
                }

                final Locale langus = OXUser.getLanguage(usrdata);
                String lang = "en_US"; // fallback when client sent INVALID locale
                if (langus.getLanguage().indexOf('_') != -1 || langus.getCountry().indexOf('_') != -1) {
                    // ok , wrong language/country set , language/country cannot
                    // contain "_"
                    log.error("Client sent wrong locale data(" + langus + ") in users language!Using fallback en_US");
                } else {
                    lang = langus.getLanguage().toLowerCase() + "_" + langus.getCountry().toUpperCase();
                }
                
                stmt.setString(8, lang);

                // mailenabled
                if (usrdata.getMailenabled() == null) {
                    usrdata.setMailenabled(true);
                }
                stmt.setBoolean(9, usrdata.getMailenabled().booleanValue());

                // imap and smtp server
                if (usrdata.getImapServer() != null) {
                    stmt.setString(10, usrdata.getImapServer());
                } else {
                    stmt.setString(10, DEFAULT_IMAP_SERVER_CREATE);
                }

                if (usrdata.getSmtpServer() != null) {
                    stmt.setString(11, usrdata.getSmtpServer());
                } else {
                    stmt.setString(11, DEFAULT_SMTP_SERVER_CREATE);
                }

                stmt.setInt(12, contact_id);

                String homedir = prop.getString(PropertyFiles.USER, AdminProperties.User.HOME_DIR_ROOT);
                homedir += "/" + usrdata.getName();
                stmt.setString(13, homedir);

                final int uid_number_start;
                uid_number_start = prop.getInt(PropertyFiles.USER, AdminProperties.User.UID_NUMBER_START);
                if(uid_number_start > 0 ) {
                    stmt.setInt(14,uid_number);
                } else {
                    stmt.setInt(14,NOBODY);
                }

                final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();

                int def_group_id = tool.getDefaultGroupForContext(ctx, write_ox_con);
                if (usrdata.getDefault_group() != null) {
                    def_group_id = usrdata.getDefault_group().getId();
                }

                // now check if gidnumber feature is enabled
                // if yes, update user table to correct gidnumber of users
                // default group
                final int gid_number_start;
                try {
                    gid_number_start = prop.getInt(PropertyFiles.GROUP, AdminProperties.Group.GID_NUMBER_START);
                } catch (final ConversionException e) {
                    throw new StorageException("Error getting " + AdminProperties.Group.GID_NUMBER_START + ": " + e.toString());
                }
                if (gid_number_start > 0) {
                    final int gid_number = tool.getGidNumberOfGroup(ctx, def_group_id, write_ox_con);
                    stmt.setInt(15,gid_number);
                } else {
                    stmt.setInt(15,NOGROUP);
                }

                stmt.setString(16,LOGINSHELL);

                if(usrdata.getImapLogin() != null ) {
                    stmt.setString(17,usrdata.getImapLogin());
                } else {
                    stmt.setNull(17, java.sql.Types.VARCHAR);
                }
                
                stmt.executeUpdate();
                stmt.close();

                // fill up statement for prg_contacts update

                final Class<? extends User> c = usrdata.getClass();
                final Method[] theMethods = c.getMethods();
                // final ArrayList<Method> methodlist = new ArrayList<Method>();
                // final ArrayList<String> methodnamelist = new
                // ArrayList<String>();

                final ArrayList<MethodAndNames> methodlist = getGetters(theMethods);

                final StringBuilder contact_query = new StringBuilder(
                        "INSERT INTO prg_contacts (cid, userid, creating_date, created_from, changing_date, fid, ");
                for (int i = 1; i < 9; i++) {
                    contact_query.append("intfield0");
                    contact_query.append(i);
                    contact_query.append(", ");
                }

                final StringBuilder questionmarks = new StringBuilder();

                final ArrayList<Method> methodlist2 = new ArrayList<Method>();

                for (final MethodAndNames methodandname : methodlist) {
                    // First we have to check which return value we have. We
                    // have to
                    // distinguish four types
                    final Method method = methodandname.getMethod();
                    final String methodname = methodandname.getName();
                    final String returntype = method.getReturnType().getName();
                    if (returntype.equalsIgnoreCase("java.lang.String")) {
                        final String result = (java.lang.String) method.invoke(
                                usrdata, (Object[]) null);
                        if (null != result) {
                            contact_query.append(Mapper.method2field
                                    .get(methodname));
                            contact_query.append(", ");
                            questionmarks.append("?, ");
                            methodlist2.add(method);
                        }
                    } else if (returntype.equalsIgnoreCase("java.lang.Integer")) {
                        final int result = (Integer) method.invoke(usrdata,
                                (Object[]) null);
                        if (-1 != result) {
                            contact_query.append(Mapper.method2field
                                    .get(methodname));
                            contact_query.append(", ");
                            questionmarks.append("?, ");
                            methodlist2.add(method);
                        }
                    } else if (returntype.equalsIgnoreCase("java.lang.Boolean")) {
                        contact_query.append(Mapper.method2field
                                .get(methodname));
                        contact_query.append(", ");
                        questionmarks.append("?, ");
                        methodlist2.add(method);
                    } else if (returntype.equalsIgnoreCase("java.util.Date")) {
                        final Date result = (Date) method.invoke(usrdata,
                                (Object[]) null);
                        if (null != result) {
                            contact_query.append(Mapper.method2field
                                    .get(methodname));
                            contact_query.append(", ");
                            questionmarks.append("?, ");
                            methodlist2.add(method);
                        }
                    }
                }
                questionmarks.deleteCharAt(questionmarks.length() - 2);
                contact_query.deleteCharAt(contact_query.length() - 2);
                contact_query.append(") VALUES (?,?,?,?,?,?,");
                for (int i = 1; i < 9; i++) {
                    contact_query.append("?,");
                }
                contact_query.append(questionmarks);
                contact_query.append(")");

                stmt = write_ox_con.prepareStatement(contact_query.toString());

                stmt.setInt(1, ctx.getId());
                stmt.setInt(2, internal_user_id);
                stmt.setLong(3, System.currentTimeMillis());
                stmt.setInt(4, internal_user_id);
                stmt.setLong(5, System.currentTimeMillis());
                stmt.setLong(6, 6); // fid = 6 cause internal user

                stmt.setInt(7, contact_id);
                for (int i = 8; i < 15; i++) {
                    stmt.setNull(i, java.sql.Types.INTEGER);
                }
                for (int i = 0; i < methodlist2.size(); i++) {
                    final int overhead = 15; // How much the index goes ahead
                    final int index = overhead + i;
                    final Method method = methodlist2.get(i);
                    final String returntype = method.getReturnType().getName();
                    if (returntype.equalsIgnoreCase("java.lang.String")) {
                        final String result = (java.lang.String) method.invoke(
                                usrdata, (Object[]) null);
                        if (null != result) {
                            stmt.setString(index, result);
                        } else {
                            stmt.setNull(index, java.sql.Types.VARCHAR);
                        }
                    } else if (returntype.equalsIgnoreCase("java.lang.Integer")) {
                        final int result = (Integer) method.invoke(usrdata,
                                (Object[]) null);
                        if (-1 != result) {
                            stmt.setInt(index, result);
                        } else {
                            stmt.setNull(index, java.sql.Types.INTEGER);
                        }
                    } else if (returntype.equalsIgnoreCase("java.lang.Boolean")) {
                        final boolean result = (Boolean) method.invoke(usrdata,
                                (Object[]) null);
                        stmt.setBoolean(index, result);
                    } else if (returntype.equalsIgnoreCase("java.util.Date")) {
                        final Date result = (java.util.Date) method.invoke(
                                usrdata, (Object[]) null);
                        if (null != result) {
                            stmt.setDate(index, new java.sql.Date(result
                                    .getTime()));
                        } else {
                            stmt.setNull(index, java.sql.Types.VARCHAR);
                        }
                    }

                    // TODO: d7 rewrite log
                    // log.debug("******************* " +
                    // user_data.get(CONTACT_FIELDS[f]).toString() + " / " +
                    // Contacts.mapping[cfield].getDBFieldName() + " / " +
                    // cfield);
                }

                stmt.executeUpdate();
                stmt.close();

                // get mailfolder
                String std_mail_folder_sent = prop.getString(PropertyFiles.USER, AdminProperties.User.SENT_MAILFOLDER_ + lang.toUpperCase(), "Sent");
                if (null != usrdata.getMail_folder_sent_name()) {
                    std_mail_folder_sent = usrdata.getMail_folder_sent_name();
                }

                String std_mail_folder_trash = prop.getString(PropertyFiles.USER, AdminProperties.User.TRASH_MAILFOLDER_ + lang.toUpperCase(), "Trash");
                if (null != usrdata.getMail_folder_trash_name()) {
                    std_mail_folder_trash = usrdata.getMail_folder_trash_name();
                }

                String std_mail_folder_drafts = prop.getString(PropertyFiles.USER, AdminProperties.User.DRAFTS_MAILFOLDER_ + lang.toUpperCase(), "Drafts");
                if (null != usrdata.getMail_folder_drafts_name()) {
                    std_mail_folder_drafts = usrdata
                            .getMail_folder_drafts_name();
                }

                String std_mail_folder_spam = prop.getString(PropertyFiles.USER, AdminProperties.User.SPAM_MAILFOLDER_ + lang.toUpperCase(), "Spam");
                if (null != usrdata.getMail_folder_spam_name()) {
                    std_mail_folder_spam = usrdata.getMail_folder_spam_name();
                }

                String std_mail_folder_confirmed_spam = prop.getString(PropertyFiles.USER, AdminProperties.User.CONFIRMED_SPAM_MAILFOLDER_ + lang.toUpperCase(), "confirmed-spam");
                if (null != usrdata.getMail_folder_confirmed_spam_name()) {
                    std_mail_folder_confirmed_spam = usrdata.getMail_folder_confirmed_spam_name();
                }

                String std_mail_folder_confirmed_ham = prop.getString(PropertyFiles.USER, AdminProperties.User.CONFIRMED_HAM_MAILFOLDER_ + lang.toUpperCase(), "confirmed-ham");
                if (null != usrdata.getMail_folder_confirmed_ham_name()) {
                    std_mail_folder_confirmed_ham = usrdata.getMail_folder_confirmed_ham_name();
                }

                // insert all multi valued attribs to the user_attribute table,
                // here we fill the alias attribute in it
                if (usrdata.getAliases() != null
                        && usrdata.getAliases().size() > 0) {
                    final Iterator<String> itr = usrdata.getAliases().iterator();
                    while (itr.hasNext()) {
                       final String tmp_mail = itr.next().toString().trim();
                       if(tmp_mail.length()>0){
                           stmt = write_ox_con
                                .prepareStatement("INSERT INTO user_attribute (cid,id,name,value) VALUES (?,?,?,?)");
                           stmt.setInt(1, ctx.getId());
                           stmt.setInt(2, internal_user_id);
                           stmt.setString(3, "alias");
                           stmt.setString(4, tmp_mail);
                           stmt.executeUpdate();
                           stmt.close();
                       }
                    }
                }

                // add user to login2user table with the internal id
                stmt = write_ox_con
                        .prepareStatement("INSERT INTO login2user (cid,id,uid) VALUES (?,?,?)");
                stmt.setInt(1, ctx.getId());
                stmt.setInt(2, internal_user_id);
                stmt.setString(3, usrdata.getName());
                stmt.executeUpdate();
                stmt.close();

                stmt = write_ox_con
                        .prepareStatement("INSERT INTO groups_member (cid,id,member) VALUES (?,?,?)");
                stmt.setInt(1, ctx.getId());
                stmt.setInt(2, def_group_id);
                stmt.setInt(3, internal_user_id);
                stmt.executeUpdate();
                stmt.close();

                if (mustMapAdmin) {
                    stmt = write_ox_con
                            .prepareStatement("INSERT INTO user_setting_admin (cid,user) VALUES (?,?)");
                    stmt.setInt(1, ctx.getId());
                    stmt.setInt(2, admin_id);
                    stmt.executeUpdate();
                    stmt.close();
                }

                // add the module access rights to the db
                final int[] all_groups = getGroupsForUser(ctx,
                        internal_user_id, write_ox_con);

                myChangeInsertModuleAccess(ctx, internal_user_id, moduleAccess,
                        true, write_ox_con, write_ox_con, all_groups);

                // add users standard mail settings
                stmt = write_ox_con
                        .prepareStatement("INSERT INTO user_setting_mail (cid,user,std_trash,std_sent,std_drafts,std_spam,send_addr,bits,confirmed_spam,confirmed_ham) VALUES (?,?,?,?,?,?,?,?,?,?)");
                stmt.setInt(1, ctx.getId());
                stmt.setInt(2, internal_user_id);
                stmt.setString(3, std_mail_folder_trash);
                stmt.setString(4, std_mail_folder_sent);
                stmt.setString(5, std_mail_folder_drafts);
                stmt.setString(6, std_mail_folder_spam);
                stmt.setString(7, usrdata.getDefaultSenderAddress());
                // set the flag for "receiving notifications" in the ox, was bug
                // #5336
                // TODO: choeger: Extend API to allow setting of these flags
                int flags = UserSettingMail.INT_NOTIFY_TASKS | UserSettingMail.INT_NOTIFY_APPOINTMENTS;

                if( usrdata.getGUI_Spam_filter_capabilities_enabled() != null && usrdata.getGUI_Spam_filter_capabilities_enabled() ) {
                    flags |= UserSettingMail.INT_SPAM_ENABLED;
                }
                stmt.setInt(8, flags);
                stmt.setString(9, std_mail_folder_confirmed_spam);
                stmt.setString(10, std_mail_folder_confirmed_ham);
                stmt.executeUpdate();
                stmt.close();

                // only when user is NOT the admin user, then invoke the ox api
                // directly, else
                // a context is currently in creation and we would get an error
                // by the ox api
                if (internal_user_id != admin_id) {
                    final OXFolderAdminHelper oxa = new OXFolderAdminHelper();
                    oxa.addUserToOXFolders(internal_user_id, usrdata
                            .getDisplay_name(), lang, ctx.getId(),
                            write_ox_con);
                }
            } finally {
                closePreparedStatement(stmt);
            }

            // return the client the id to work with the user in the system
            return_db_id = write_ox_con
                    .prepareStatement("SELECT id FROM login2user WHERE cid = ? AND uid = ?");
            return_db_id.setInt(1, ctx.getId());
            return_db_id.setString(2, usrdata.getName());
            rs = return_db_id.executeQuery();
            int id_for_client = -1;
            if (rs.next()) {
                id_for_client = rs.getInt("id");
            }
            if (log.isInfoEnabled()) {
            log.info("User " + id_for_client + " created!");
            }
            write_ox_con.commit();
            return id_for_client;

            
        } catch (final DataTruncation dt) {
            log.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            try {
                write_ox_con.rollback();
                if (log.isDebugEnabled()) {
                    log.debug("Rollback successfull for ox db write connection");
                }
            } catch (final SQLException ecp) {
                log.error("Error rollback ox db write connection", ecp);
            }
            throw AdminCache.parseDataTruncation(dt);
        } catch (final SQLException e) {
            log.error("SQL Error", e);
            try {
                write_ox_con.rollback();
                if (log.isDebugEnabled()) {
                    log.debug("Rollback successfull for ox db write connection");
                }
            } catch (final SQLException ecp) {
                log.error("Error rollback ox db write connection", ecp);
            }
            throw new StorageException(e);
        } catch (final OXException e) {
            log.error("OX Error", e);
            try {
                write_ox_con.rollback();
                log.debug("Rollback successfull for ox db write connection");
            } catch (final SQLException ecp) {
                log.error("Error rollback ox db write connection", ecp);
            }
            throw new StorageException(e.toString());
        } catch (final NoSuchAlgorithmException e) {
            // Here we throw without rollback, because at the point this
            // exception is thrown
            // no database activity has happened
            throw new StorageException(e);
        } catch (UnsupportedEncodingException e) {
            // Here we throw without rollback, because at the point this
            // exception is thrown
            // no database activity has happened
            throw new StorageException(e);
        } catch (final IllegalArgumentException e) {
            log.error("IllegalArgument Error", e);
            try {
                write_ox_con.rollback();
                log.debug("Rollback successfull for ox db write connection");
            } catch (final SQLException ecp) {
                log.error("Error rollback ox db write connection", ecp);
            }
            throw new StorageException(e);
        } catch (final IllegalAccessException e) {
            log.error("IllegalAccess Error", e);
            try {
                write_ox_con.rollback();
                log.debug("Rollback successfull for ox db write connection");
            } catch (final SQLException ecp) {
                log.error("Error rollback ox db write connection", ecp);
            }
            throw new StorageException(e);
        } catch (final InvocationTargetException e) {
            log.error("InvocationTarget Error", e);
            try {
                write_ox_con.rollback();
                log.debug("Rollback successfull for ox db write connection");
            } catch (final SQLException ecp) {
                log.error("Error rollback ox db write connection", ecp);
            }
            throw new StorageException(e);
        } catch (final InvalidDataException e) {
            log.error("Invalid data in config file", e);
            try {
                write_ox_con.rollback();
                log.debug("Rollback successfull for ox db write connection");
            } catch (final SQLException ecp) {
                log.error("Error rollback ox db write connection", ecp);
            }
            throw new StorageException(e);
        } finally {
            closePreparedStatement(return_db_id);
            closePreparedStatement(ps);
        }
    }

    @Override
    public int create(final Context ctx, final User usrdata, final UserModuleAccess moduleAccess) throws StorageException {
        final int context_id = ctx.getId();
        Connection write_ox_con = null;
        try {
            write_ox_con = cache.getWRITEConnectionForContext(context_id);
            write_ox_con.setAutoCommit(false);

            final int internal_user_id = IDGenerator.getId(context_id, com.openexchange.groupware.Types.PRINCIPAL, write_ox_con);
            write_ox_con.commit();
            final int contact_id = IDGenerator.getId(context_id, com.openexchange.groupware.Types.CONTACT, write_ox_con);
            write_ox_con.commit();

            int uid_number = -1;
            final int uid_number_start;
            uid_number_start = prop.getInt(PropertyFiles.USER, AdminProperties.User.UID_NUMBER_START);
            if (uid_number_start > 0) {
                uid_number = IDGenerator.getId(context_id, com.openexchange.groupware.Types.UID_NUMBER, write_ox_con);
                write_ox_con.commit();
            }

            return create(ctx, usrdata, moduleAccess, write_ox_con, internal_user_id, contact_id, uid_number);
        } catch (final DataTruncation dt) {
            log.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            try {
                write_ox_con.rollback();
                log.debug("Rollback successfull for ox db write connection");
            } catch (final SQLException ecp) {
                log.error("Error rollback ox db write connection", ecp);
            }
            throw AdminCache.parseDataTruncation(dt);
        } catch (final SQLException sql) {
            log.error("SQL Error", sql);
            // rollback operations on ox db connection
            try {
                write_ox_con.rollback();
                log.debug("Rollback successfull for ox db write connection");
            } catch (final SQLException ecp) {
                log.error("Error rollback ox db write connection", ecp);
            }
            throw new StorageException(sql);
        } catch (final PoolException e) {
            log.error("Pool Error", e);
            // rollback operations on ox db connection
            try {
                write_ox_con.rollback();
                log.debug("Rollback successfull for ox db write connection");
            } catch (final SQLException ecp) {
                log.error("SQL Error rollback ox db write connection", ecp);
            }
            throw new StorageException(e);
        } catch (final InvalidDataException e) {
            log.error("Invalid data in config file", e);
            // rollback operations on ox db connection
            try {
                write_ox_con.rollback();
                log.debug("Rollback successfull for ox db write connection");
            } catch (final SQLException ecp) {
                log.error("SQL Error rollback ox db write connection", ecp);
            }
            throw new StorageException(e);
        } finally {
            try {
                if (write_ox_con != null) {
                    cache.pushOXDBWrite(context_id, write_ox_con);
                }
            } catch (final PoolException ex) {
                log.error("Pool Error pushing ox write connection to pool!", ex);
            }
        }
    }

    @Override
    public int[] getAll(final Context ctx) throws StorageException {
        Connection read_ox_con = null;
        PreparedStatement stmt = null;
        final int context_id = ctx.getId();
        try {
            final ArrayList<Integer> list = new ArrayList<Integer>();
            read_ox_con = cache.getREADConnectionForContext(context_id);
            stmt = read_ox_con
                    .prepareStatement("SELECT con.userid,con.field01,con.field02,con.field03,lu.uid FROM prg_contacts con JOIN login2user lu  ON con.userid = lu.id WHERE con.cid = ? AND con.cid = lu.cid AND (lu.uid LIKE '%' OR con.field01 LIKE '%');");

            stmt.setInt(1, context_id);
            final ResultSet rs3 = stmt.executeQuery();
            while (rs3.next()) {
                final int user_id = rs3.getInt("userid");
                list.add(user_id);
            }
            rs3.close();
            final int[] retval = new int[list.size()];
            for (int i = 0; i < list.size(); i++) {
                retval[i] = list.get(i);
            }

            return retval;
        } catch (final SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e);
        } catch (final PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (final SQLException e) {
                log.error("SQL Error closing statement!", e);
            }
            try {
                if (read_ox_con != null) {
                    cache.pushOXDBRead(context_id, read_ox_con);
                }
            } catch (final PoolException exp) {
                log.error("Pool Error pushing ox read connection to pool!",exp);
            }
        }
    }

    @Override
    public User[] list(final Context ctx, final String search_pattern) throws StorageException {
        Connection read_ox_con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String new_search_pattern = null;
        if (null != search_pattern) {
            new_search_pattern = search_pattern.replace('*', '%');
        }
        final int context_id = ctx.getId();
        try {
            final ArrayList<User> retval = new ArrayList<User>();
            read_ox_con = cache.getREADConnectionForContext(context_id);
            stmt = read_ox_con.prepareStatement("SELECT con.userid FROM prg_contacts con JOIN login2user lu ON con.userid = lu.id AND con.cid = lu.cid WHERE con.cid = ? AND (lu.uid LIKE ? OR con.field01 LIKE ?);");

            stmt.setInt(1, context_id);
            stmt.setString(2, new_search_pattern);
            stmt.setString(3, new_search_pattern);
            rs = stmt.executeQuery();
            while (rs.next()) {
                final int user_id = rs.getInt("userid");
                retval.add(new User(user_id));
            }

            return retval.toArray(new User[retval.size()]);
        } catch (final SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e);
        } catch (final PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } finally {
            try {
                if (null != rs) {
                    rs.close();
                }
            } catch (final SQLException e) {
                log.error("SQL Error closing resultset!", e);
            }
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (final SQLException e) {
                log.error("SQL Error closing statement!", e);
            }
            try {
                if (read_ox_con != null) {
                    cache.pushOXDBRead(context_id, read_ox_con);
                }
            } catch (final PoolException exp) {
                log.error("Pool Error pushing ox read connection to pool!",exp);
            }
        }
    }

    @Override
    public User[] getData(final Context ctx, final User[] users)
            throws StorageException {
        final int context_id = ctx.getId();
        final Class<User> c = User.class;
        final Method[] theMethods = c.getMethods();
        final ArrayList<Method> list = new ArrayList<Method>();
        final HashSet<String> notallowed = new HashSet<String>(9);

        // Define all those fields which are contained in the user table
        notallowed.add("setMailFolderDrafts");
        notallowed.add("setMailFolderSent");
        notallowed.add("setMailFolderSpam");
        notallowed.add("setMailFolderTrash");
        notallowed.add("setMailFolderConfirmedSpam");
        notallowed.add("setMailFolderConfirmedHam");

        final StringBuilder query = new StringBuilder("SELECT ");

        for (final Method method : theMethods) {
            final String methodname = method.getName();

            if (methodname.startsWith("set")) {
                if (!notallowed.contains(methodname)) {
                    final String fieldname = Mapper.method2field.get(methodname.substring(3));
                    if (null != fieldname) {
                        list.add(method);
                        query.append(fieldname);
                        query.append(", ");
                    }
                }
            }
        }
        // query.deleteCharAt(query.length() - 1);
        query.delete(query.length() - 2, query.length() - 1);

        query.append(" FROM user JOIN login2user USING (cid,id) JOIN prg_contacts ON (user.cid=prg_contacts.cid AND user.id=prg_contacts.userid) ");
        query.append("WHERE user.id = ? ");
        query.append("AND user.cid = ? ");

        Connection read_ox_con = null;
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;
        PreparedStatement stmtusername = null;
        PreparedStatement stmtalias = null;
        PreparedStatement stmtstd = null;
        final ArrayList<User> userlist = new ArrayList<User>();
        try {
            read_ox_con = cache.getREADConnectionForContext(context_id);
            final OXToolStorageInterface oxtool = OXToolMySQLStorage.getInstance();
            final int adminForContext = oxtool.getAdminForContext(ctx, read_ox_con);

            stmt = read_ox_con.prepareStatement("SELECT uid FROM login2user WHERE cid = ? AND id = ?");
            stmt.setInt(1, context_id);
            stmt2 = read_ox_con.prepareStatement(query.toString());
            stmtusername = read_ox_con.prepareStatement("SELECT id FROM login2user WHERE cid = ? AND uid = ?");
            stmtusername.setInt(1, context_id);
            stmtalias = read_ox_con.prepareStatement("SELECT value FROM user_attribute WHERE cid = ? and id = ? AND name = \"alias\"");
            stmtalias.setInt(1, context_id);
            stmtstd = read_ox_con.prepareStatement("SELECT std_trash,std_sent,std_drafts,std_spam,confirmed_spam,confirmed_ham,bits,send_addr FROM user_setting_mail WHERE cid = ? and user = ?");
            stmtstd.setInt(1, context_id);
            ResultSet rs = null;
            for (final User user : users) {
                int user_id = user.getId();
                final User newuser = (User) user.clone();
                String username = user.getName();
                if (-1 != user_id) {
                    // TODO: Why do we make this clause?
                    if (null == username) {
                        stmt.setInt(2, user_id);
                        rs = stmt.executeQuery();
                        if (rs.next()) {
                            username = rs.getString("uid");
                            user.setName(username);
                        }
                        rs.close();
                    }
                    stmt2.setInt(1, user_id);
                } else if (null != user.getName()) {
                    stmtusername.setString(2, user.getName());
                    rs = stmtusername.executeQuery();
                    if (rs.next()) {
                        user_id = rs.getInt("id");
                    }
                    rs.close();

                    stmt2.setInt(1, user_id);
                } else {
                    throw new StorageException("No user name oder user id given");
                }
                newuser.setName(username);

                stmt2.setInt(2, context_id);
                rs = stmt2.executeQuery();
                if (rs.next()) {
                    for (final Method method : list) {
                        final String methodnamewithoutset = method.getName().substring(3);
                        final String fieldname = Mapper.method2field.get(methodnamewithoutset);
                        final String paramtype = method.getParameterTypes()[0].getName();
                        if (paramtype.equalsIgnoreCase("java.lang.String")) {
                            final String fieldvalue = rs.getString(fieldname);
                            method.invoke(newuser, fieldvalue);
                        } else if (paramtype.equalsIgnoreCase("java.lang.Integer")) {
                            method.invoke(newuser, rs.getInt(fieldname));
                        } else if (paramtype.equalsIgnoreCase("java.lang.Boolean")) {
                            if (methodnamewithoutset.equals(Mapper.PASSWORD_EXPIRED)) {
                                method.invoke(newuser, getboolfromint(rs.getInt(fieldname)));
                            } else {
                                method.invoke(newuser, rs.getBoolean(fieldname));
                            }

                        } else if (paramtype.equalsIgnoreCase("java.util.Date")) {
                            final Date fieldvalue = rs.getDate(fieldname);
                            method.invoke(newuser, fieldvalue);
                        } else if (paramtype.equalsIgnoreCase("java.util.Locale")) {
                            final String locale = rs.getString(fieldname);
                            final Locale loc = new Locale(locale.substring(0, 2), locale.substring(3, 5));
                            method.invoke(newuser, loc);
                        } else if (paramtype.equalsIgnoreCase("java.util.TimeZone")) {
                            final String fieldvalue = rs.getString(fieldname);
                            method.invoke(newuser, TimeZone.getTimeZone(fieldvalue));
                        }
                    }
                }
                rs.close();

                stmtalias.setInt(2, user_id);
                rs = stmtalias.executeQuery();
                while (rs.next()) {
                    newuser.addAlias(rs.getString("value"));
                }
                rs.close();

                stmtstd.setInt(2, user_id);
                rs = stmtstd.executeQuery();
                if (rs.next()) {
                    newuser.setMail_folder_drafts_name(rs.getString("std_drafts"));
                    newuser.setMail_folder_sent_name(rs.getString("std_sent"));
                    newuser.setMail_folder_spam_name(rs.getString("std_spam"));
                    newuser.setMail_folder_trash_name(rs.getString("std_trash"));
                    newuser.setMail_folder_confirmed_ham_name(rs.getString("confirmed_ham"));
                    newuser.setMail_folder_confirmed_spam_name(rs.getString("confirmed_spam"));
                    final int bits = rs.getInt("bits");
                    if( (bits & UserSettingMail.INT_SPAM_ENABLED) == UserSettingMail.INT_SPAM_ENABLED ) {
                        newuser.setGUI_Spam_filter_capabilities_enabled(true);
                    } else {
                        newuser.setGUI_Spam_filter_capabilities_enabled(false);
                    }
                    newuser.setDefaultSenderAddress(rs.getString("send_addr"));
                }
                rs.close();

                newuser.setContextadmin(newuser.getId().equals(adminForContext));
                userlist.add(newuser);
            }

            return userlist.toArray(new User[userlist.size()]);
        } catch (final PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (final SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e);
        } catch (final IllegalArgumentException e) {
            log.error("Error", e);
            throw new StorageException(e);
        } catch (final IllegalAccessException e) {
            log.error("Error", e);
            throw new StorageException(e);
        } catch (final InvocationTargetException e) {
            log.error("Error", e);
            throw new StorageException(e);
        } catch (final CloneNotSupportedException e) {
            log.error("Error", e);
            throw new StorageException(e);
        } finally {
            closePreparedStatement(stmt);
            closePreparedStatement(stmt2);
            closePreparedStatement(stmtusername);
            closePreparedStatement(stmtalias);
            closePreparedStatement(stmtstd);
            try {
                if (read_ox_con != null) {
                    cache.pushOXDBRead(context_id, read_ox_con);
                }
            } catch (final PoolException exp) {
                log.error("Pool Error pushing ox read connection to pool!", exp);
            }
        }
    }

    private void closePreparedStatement(PreparedStatement stmt) {
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (final SQLException e) {
            log.error("Error closing statement", e);
        }
    }

    @Override
    public void delete(final Context ctx, final int[] user_ids,
            final Connection write_ox_con) throws StorageException {
        PreparedStatement stmt = null;
        try {
            // delete all users
            for (final int user_id : user_ids) {
                if (log.isDebugEnabled()) {
                    log.debug("Start delete user " + user_id + " in context "+ ctx.getId());
                    log.debug("Delete user " + user_id + "(" + ctx.getId()+ ") via OX API...");
                }
                final DeleteEvent delev = new DeleteEvent(this, user_id,
                        DeleteEvent.TYPE_USER, ctx.getId());
                AdminCache.delreg.fireDeleteEvent(delev, write_ox_con,
                        write_ox_con);
                if (log.isDebugEnabled()) {
                    log.debug("Delete user " + user_id + "(" + ctx.getId()+ ") from login2user...");
                }
                stmt = write_ox_con
                        .prepareStatement("DELETE FROM login2user WHERE cid = ? AND id = ?");
                stmt.setInt(1, ctx.getId());
                stmt.setInt(2, user_id);
                stmt.executeUpdate();
                stmt.close();
                if (log.isDebugEnabled()) {
                    log.debug("Delete user " + user_id + "(" + ctx.getId()+ ") from groups member...");
                }
                stmt = write_ox_con
                        .prepareStatement("DELETE FROM groups_member WHERE cid = ? AND member = ?");
                stmt.setInt(1, ctx.getId());
                stmt.setInt(2, user_id);
                stmt.executeUpdate();
                stmt.close();
                if (log.isDebugEnabled()) {
                    log.debug("Delete user " + user_id + "(" + ctx.getId()+ ") from user attribute ...");
                }
                stmt = write_ox_con
                        .prepareStatement("DELETE FROM user_attribute WHERE cid = ? AND id = ?");
                stmt.setInt(1, ctx.getId());
                stmt.setInt(2, user_id);
                stmt.executeUpdate();
                stmt.close();
                if (log.isDebugEnabled()) {
                    log.debug("Delete user " + user_id + "(" + ctx.getId() + ") from user mail setting...");
                }
                stmt = write_ox_con
                        .prepareStatement("DELETE FROM user_setting_mail WHERE cid = ? AND user = ?");
                stmt.setInt(1, ctx.getId());
                stmt.setInt(2, user_id);
                stmt.executeUpdate();
                stmt.close();

                // delete from user_setting_admin if user is mailadmin
                final OXToolStorageInterface tools = OXToolStorageInterface
                        .getInstance();
                if (user_id == tools.getAdminForContext(ctx, write_ox_con)) {
                    stmt = write_ox_con
                            .prepareStatement("DELETE FROM user_setting_admin WHERE cid = ? AND user = ?");
                    stmt.setInt(1, ctx.getId());
                    stmt.setInt(2, user_id);
                    stmt.executeUpdate();
                    stmt.close();
                }
                
                // when table ready, enable this
                createRecoveryData(ctx, user_id, write_ox_con);
                if (log.isDebugEnabled()) {
                    log.debug("Delete user " + user_id + "(" + ctx.getId() + ") from user ...");
                }
                stmt = write_ox_con
                        .prepareStatement("DELETE FROM user WHERE cid = ? AND id = ?");
                stmt.setInt(1, ctx.getId());
                stmt.setInt(2, user_id);
                stmt.executeUpdate();
                stmt.close();
                if (log.isDebugEnabled()) {
                    log.debug("Delete user " + user_id + "(" + ctx.getId() + ") from contacts ...");
                }
                stmt = write_ox_con
                        .prepareStatement("DELETE FROM prg_contacts WHERE cid = ? AND userid = ?");
                stmt.setInt(1, ctx.getId());
                stmt.setInt(2, user_id);
                stmt.executeUpdate();
                stmt.close();

            }
        } catch (final DeleteFailedException dex) {
            log.error("Delete Error", dex);
            throw new StorageException(dex.toString());
        } catch (final ContextException cte) {
            log.error("Context Error", cte);
            throw new StorageException(cte.toString());
        } catch (final SQLException sqle) {
            log.error("SQL Error", sqle);
            throw new StorageException(sqle);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (final SQLException e) {
                log.error("SQL Error closing statement on ox write connection!", e);
            }
        }
    }

    @Override
    public void delete(final Context ctx, final User[] users, final Connection write_ox_con) throws StorageException {
        PreparedStatement stmt = null;
        try {
            // delete all users
            for (final User user : users) {
                final int user_id = user.getId();
                if (log.isDebugEnabled()) {
                    log.debug("Start delete user " + user_id + " in context "+ ctx.getId());
                    log.debug("Delete user " + user_id + "(" + ctx.getId()+ ") via OX API...");
                }
                final DeleteEvent delev = new DeleteEvent(this, user_id, DeleteEvent.TYPE_USER, ctx.getId());
                AdminCache.delreg.fireDeleteEvent(delev, write_ox_con, write_ox_con);
                if (log.isDebugEnabled()) {
                    log.debug("Delete user " + user_id + "(" + ctx.getId()+ ") from login2user...");
                }
                stmt = write_ox_con.prepareStatement("DELETE FROM login2user WHERE cid = ? AND id = ?");
                stmt.setInt(1, ctx.getId());
                stmt.setInt(2, user_id);
                stmt.executeUpdate();
                stmt.close();
                if (log.isDebugEnabled()) {
                    log.debug("Delete user " + user_id + "(" + ctx.getId()+ ") from groups member...");
                }
                stmt = write_ox_con.prepareStatement("DELETE FROM groups_member WHERE cid = ? AND member = ?");
                stmt.setInt(1, ctx.getId());
                stmt.setInt(2, user_id);
                stmt.executeUpdate();
                stmt.close();
                if (log.isDebugEnabled()) {
                    log.debug("Delete user " + user_id + "(" + ctx.getId()+ ") from user attribute ...");
                }
                stmt = write_ox_con.prepareStatement("DELETE FROM user_attribute WHERE cid = ? AND id = ?");
                stmt.setInt(1, ctx.getId());
                stmt.setInt(2, user_id);
                stmt.executeUpdate();
                stmt.close();
                if (log.isDebugEnabled()) {
                    log.debug("Delete user " + user_id + "(" + ctx.getId() + ") from user mail setting...");
                }
                stmt = write_ox_con.prepareStatement("DELETE FROM user_setting_mail WHERE cid = ? AND user = ?");
                stmt.setInt(1, ctx.getId());
                stmt.setInt(2, user_id);
                stmt.executeUpdate();
                stmt.close();
                
                // delete from user_setting_admin if user is mailadmin
                final OXToolStorageInterface tools = OXToolStorageInterface.getInstance();
                if (user_id == tools.getAdminForContext(ctx, write_ox_con)) {
                    stmt = write_ox_con.prepareStatement("DELETE FROM user_setting_admin WHERE cid = ? AND user = ?");
                    stmt.setInt(1, ctx.getId());
                    stmt.setInt(2, user_id);
                    stmt.executeUpdate();
                    stmt.close();
                }
                
                // when table ready, enable this
                createRecoveryData(ctx, user_id, write_ox_con);
                if (log.isDebugEnabled()) {
                    log.debug("Delete user " + user_id + "(" + ctx.getId() + ") from user ...");
                }
                stmt = write_ox_con.prepareStatement("DELETE FROM user WHERE cid = ? AND id = ?");
                stmt.setInt(1, ctx.getId());
                stmt.setInt(2, user_id);
                stmt.executeUpdate();
                stmt.close();
                if (log.isDebugEnabled()) {
                    log.debug("Delete user " + user_id + "(" + ctx.getId() + ") from contacts ...");
                }
                stmt = write_ox_con.prepareStatement("DELETE FROM prg_contacts WHERE cid = ? AND userid = ?");
                stmt.setInt(1, ctx.getId());
                stmt.setInt(2, user_id);
                stmt.executeUpdate();
                stmt.close();
            }
        } catch (final DeleteFailedException dex) {
            log.error("Delete Error", dex);
            throw new StorageException(dex.toString());
        } catch (final ContextException cte) {
            log.error("Context Error", cte);
            throw new StorageException(cte.toString());
        } catch (final SQLException sqle) {
            log.error("SQL Error", sqle);
            throw new StorageException(sqle);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (final SQLException e) {
                log.error("SQL Error closing statement on ox write connection!", e);
            }
        }
    }

    @Override
    public void delete(final Context ctx, final int[] user_ids)
            throws StorageException {
        Connection write_ox_con = null;

        try {

            write_ox_con = cache.getWRITEConnectionForContext(ctx.getId());
            write_ox_con.setAutoCommit(false);

            delete(ctx, user_ids, write_ox_con);

            write_ox_con.commit();
        } catch (final StorageException st) {
            log.error("Storage Error", st);
            try {
                write_ox_con.rollback();
            } catch (final SQLException ex) {
                log.error("Error rollback ox db write connection", ex);
            }
            throw st;
        } catch (final PoolException pep) {
            log.error("Pool Error", pep);
            try {
                write_ox_con.rollback();
            } catch (final SQLException ex) {
                log.error("Error rollback ox db write connection", ex);
            }
            throw new StorageException(pep);
        } catch (final SQLException sql) {
            log.error("SQL Error", sql);
            try {
                write_ox_con.rollback();
            } catch (final SQLException ex) {
                log.error("Error rollback ox db write connection", ex);
            }
            throw new StorageException(sql);
        } finally {
            try {
                if (write_ox_con != null) {
                    cache.pushOXDBWrite(ctx.getId(), write_ox_con);
                }
            } catch (final PoolException aexp) {
                log.error("Pool Error pushing ox write connection to pool!", aexp);
            }
        }

    }

    @Override
    public void delete(final Context ctx, final User[] users) throws StorageException {
        Connection write_ox_con = null;
        
        try {
            write_ox_con = cache.getWRITEConnectionForContext(ctx.getId());
            write_ox_con.setAutoCommit(false);
            
            delete(ctx, users, write_ox_con);
            
            write_ox_con.commit();
        } catch (final StorageException st) {
            log.error("Storage Error", st);
            try {
                write_ox_con.rollback();
            } catch (final SQLException ex) {
                log.error("Error rollback ox db write connection", ex);
            }
            throw st;
        } catch (final PoolException pep) {
            log.error("Pool Error", pep);
            try {
                write_ox_con.rollback();
            } catch (final SQLException ex) {
                log.error("Error rollback ox db write connection", ex);
            }
            throw new StorageException(pep);
        } catch (final SQLException sql) {
            log.error("SQL Error", sql);
            try {
                write_ox_con.rollback();
            } catch (final SQLException ex) {
                log.error("Error rollback ox db write connection", ex);
            }
            throw new StorageException(sql);
        } finally {
            try {
                if (write_ox_con != null) {
                    cache.pushOXDBWrite(ctx.getId(), write_ox_con);
                }
            } catch (final PoolException aexp) {
                log.error("Pool Error pushing ox write connection to pool!", aexp);
            }
        }
        
    }

    @Override
    public void delete(final Context ctx, final User user) throws StorageException {
        delete(ctx, new User[] {user});
    }

    @Override
    public void changeModuleAccess(final Context ctx, final int user_id,
            final UserModuleAccess moduleAccess) throws StorageException {
        Connection read_ox_con = null;
        Connection write_ox_con = null;

        try {
            read_ox_con = cache.getREADConnectionForContext(ctx.getId());
            write_ox_con = cache.getWRITEConnectionForContext(ctx.getId());

            // first get all groups the user is in
            final int[] all_groups = getGroupsForUser(ctx, user_id, read_ox_con);
            // update last modified column
            changeLastModified(user_id, ctx, write_ox_con);
            myChangeInsertModuleAccess(ctx, user_id, moduleAccess, false,
                    read_ox_con, write_ox_con, all_groups);
        } catch (final SQLException sqle) {
            log.error("SQL Error", sqle);
            throw new StorageException(sqle);
        } catch (final PoolException pole) {
            log.error("Pool Error", pole);
            throw new StorageException(pole);
        } finally {
            try {
                if (read_ox_con != null) {
                    cache.pushOXDBRead(ctx.getId(), read_ox_con);
                }
            } catch (final PoolException exp) {
                log.error("Pool Error pushing ox read connection to pool!", exp);
            }
            try {
                if (write_ox_con != null) {
                    cache.pushOXDBWrite(ctx.getId(), write_ox_con);
                }
            } catch (final PoolException exp) {
                log.error("Pool Error pushing ox write connection to pool!", exp);
            }
        }
    }

    @Override
    public UserModuleAccess getModuleAccess(final Context ctx, final int user_id)
            throws StorageException {
        Connection read_ox_con = null;
        try {
            read_ox_con = cache.getREADConnectionForContext(ctx.getId());
            final int[] all_groups_of_user = getGroupsForUser(ctx, user_id,
                    read_ox_con);
            final UserConfiguration user = RdbUserConfigurationStorage
                    .adminLoadUserConfiguration(user_id, all_groups_of_user, ctx.getId(), read_ox_con);

            final UserModuleAccess acc = new UserModuleAccess();

            acc.setCalendar(user.hasCalendar());
            acc.setContacts(user.hasContact());
            acc.setForum(user.hasForum());
            acc.setEditPublicFolders(user.hasFullPublicFolderAccess());
            acc.setReadCreateSharedFolders(user.hasFullSharedFolderAccess());
            acc.setIcal(user.hasICal());
            acc.setInfostore(user.hasInfostore());
            acc.setPinboardWrite(user.hasPinboardWriteAccess());
            acc.setProjects(user.hasProject());
            acc.setRssBookmarks(user.hasRSSBookmarks());
            acc.setRssPortal(user.hasRSSPortal());
            acc.setSyncml(user.hasSyncML());
            acc.setTasks(user.hasTask());
            acc.setVcard(user.hasVCard());
            acc.setWebdav(user.hasWebDAV());
            acc.setWebdavXml(user.hasWebDAVXML());
            acc.setWebmail(user.hasWebMail());
            acc.setDelegateTask(user.canDelegateTasks());

            return acc;
        } catch (final DBPoolingException dbpol) {
            log.error("DBPooling error", dbpol);
            throw new StorageException(dbpol.toString());
        } catch (final PoolException polex) {
            log.error("Pool error", polex);
            throw new StorageException(polex);
        } catch (final SQLException sqle) {
            log.error("SQL Error ", sqle);
            throw new StorageException(sqle);
        } finally {
            try {
                if (read_ox_con != null) {
                    cache.pushOXDBRead(ctx.getId(), read_ox_con);
                }
            } catch (final PoolException exp) {
                log.error("Pool Error pushing ox read connection to pool!", exp);
            }
        }

    }

    public void changeLastModified(final int user_id, final Context ctx,
            final Connection write_ox_con) throws StorageException {
        PreparedStatement prep_edit_user = null;
        try {
            prep_edit_user = write_ox_con
                    .prepareStatement("UPDATE prg_contacts SET changing_date=? WHERE cid=? AND userid=?;");
            prep_edit_user.setLong(1, System.currentTimeMillis());
            prep_edit_user.setInt(2, ctx.getId());
            prep_edit_user.setInt(3, user_id);
            prep_edit_user.executeUpdate();
            
        } catch (final SQLException sqle) {
            log.error("SQL Error ", sqle);
            throw new StorageException(sqle);
        } finally {
            try {
                if (prep_edit_user != null) {
                    prep_edit_user.close();
                }
            } catch (final SQLException ex) {
                log.error("Error closing statement!", ex);
            }
        }
    }

    public void createRecoveryData(final Context ctx, final int user_id,
            final Connection write_ox_con) throws StorageException {
        // move user to del_user table if table is ready
        PreparedStatement del_st = null;
        ResultSet rs = null;
        try {
            del_st = write_ox_con
                    .prepareStatement("SELECT imapServer,smtpServer,imapLogin,mail,mailDomain,mailEnabled,"+
                            "preferredLanguage,shadowLastChange,timeZone,contactId,userPassword,"+
                            "passwordMech,uidNumber,gidNumber,homeDirectory,loginShell FROM user WHERE id = ? AND cid = ?");
            del_st.setInt(1, user_id);
            del_st.setInt(2, ctx.getId());
            rs = del_st.executeQuery();

            String iserver = null;
            String sserver = null;
            String ilogin = null;
            String mail = null;
            String maildomain = null;
            int menabled = -1;
            String preflang = null;
            int shadowlastschange = -1;
            String tzone = null;
            int contactid = -1;
            String passwd = null;
            String pwmech = null;
            int uidnumber = -1;
            int gidnumber = -1;
            String homedir = null;
            String shell = null;
            
            
            if (rs.next()) {
                iserver = rs.getString("imapServer");
                sserver = rs.getString("smtpServer");
                ilogin = rs.getString("imapLogin");
                mail = rs.getString("mail");
                maildomain = rs.getString("maildomain");
                menabled = rs.getInt("mailEnabled");
                preflang = rs.getString("preferredLanguage");
                shadowlastschange = rs.getInt("shadowLastChange");
                tzone = rs.getString("timeZone");
                contactid = rs.getInt("contactId");
                passwd = rs.getString("userPassword");
                pwmech = rs.getString("passwordMech");
                uidnumber = rs.getInt("uidNumber");
                gidnumber = rs.getInt("gidNumber");
                homedir = rs.getString("homeDirectory");
                shell = rs.getString("loginShell");
            }
            del_st.close();
            rs.close();

            del_st = write_ox_con
                    .prepareStatement("INSERT into del_user (id,cid,imapServer,smtpServer,imapLogin,mail,maildomain,mailEnabled,preferredLanguage,shadowLastChange,timeZone,contactId,userPassword,"+
                            "passwordMech,uidNumber,gidNumber,homeDirectory,loginShell) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            del_st.setInt(1, user_id);
            del_st.setInt(2, ctx.getId());
            if (iserver != null) {
                del_st.setString(3, iserver);
            } else {
                del_st.setNull(3, Types.VARCHAR);
            }
            if (sserver != null) {
                del_st.setString(4, sserver);
            } else {
                del_st.setNull(4, Types.VARCHAR);
            }
            if (ilogin != null) {
                del_st.setString(5, ilogin);
            } else {
                del_st.setNull(5, Types.VARCHAR);
            }
            if (mail != null) {
                del_st.setString(6, mail);
            } else {
                del_st.setNull(6, Types.VARCHAR);
            }
            if (maildomain != null) {
                del_st.setString(7, maildomain);
            } else {
                del_st.setNull(7, Types.VARCHAR);
            }
            if (menabled != -1) {
                del_st.setInt(8, menabled);
            } else {
                del_st.setNull(8, Types.INTEGER);
            }
            if (preflang != null) {
                del_st.setString(9, preflang);
            } else {
                del_st.setNull(9, Types.VARCHAR);
            }

            del_st.setInt(10, shadowlastschange);

            if (tzone != null) {
                del_st.setString(11, tzone);
            } else {
                del_st.setNull(11, Types.VARCHAR);
            }
            if (contactid != -1) {
                del_st.setInt(12, contactid);
            } else {
                del_st.setNull(12, Types.INTEGER);
            }
            if (passwd != null) {
                del_st.setString(13, passwd);
            } else {
                del_st.setNull(13, Types.VARCHAR);
            }
            if (pwmech != null) {
                del_st.setString(14, pwmech);
            } else {
                del_st.setNull(14, Types.VARCHAR);
            }
            if (uidnumber != -1) {
                del_st.setInt(15, uidnumber);
            } else {
                del_st.setNull(15, Types.INTEGER);
            }
            if (gidnumber != -1) {
                del_st.setInt(16, gidnumber);
            } else {
                del_st.setNull(16, Types.INTEGER);
            }
            if (homedir != null) {
                del_st.setString(17, homedir);
            } else {
                del_st.setNull(17, Types.VARCHAR);
            }
            if (shell != null) {
                del_st.setString(18, shell);
            } else {
                del_st.setNull(18, Types.VARCHAR);
            }

            del_st.executeUpdate();
        }catch (final DataTruncation dt){
            log.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            throw AdminCache.parseDataTruncation(dt);
        } catch (final SQLException sqle) {
            log.error("SQL Error ", sqle);
            throw new StorageException(sqle);
        } finally {
            try {
                if (del_st != null) {
                    del_st.close();
                }
            } catch (final SQLException e) {
                log.error("Error closing prepared statement!", e);
            }
        }
    }

    public void deleteAllRecoveryData(final Context ctx, final Connection con)
            throws StorageException {
        // delete from del_user table
        PreparedStatement del_st = null;
        try {
            del_st = con.prepareStatement("DELETE from del_user WHERE cid = ?");
            del_st.setInt(1, ctx.getId());
            del_st.executeUpdate();
        } catch (final SQLException sqle) {
            log.error("SQL Error ", sqle);
            throw new StorageException(sqle);
        } finally {
            try {
                if (del_st != null) {
                    del_st.close();
                }
            } catch (final SQLException e) {
                log.error("Error closing prepared statement!", e);
            }
        }
    }

    public void deleteRecoveryData(final Context ctx, final int user_id,
            final Connection con) throws StorageException {
        // delete from del_user table
        PreparedStatement del_st = null;
        try {
            del_st = con
                    .prepareStatement("DELETE from del_user WHERE id = ? AND cid = ?");
            del_st.setInt(1, user_id);
            del_st.setInt(2, ctx.getId());
            del_st.executeUpdate();
        } catch (final SQLException sqle) {
            log.error("SQL Error ", sqle);
            throw new StorageException(sqle);
        } finally {
            try {
                if (del_st != null) {
                    del_st.close();
                }
            } catch (final SQLException e) {
                log.error("Error closing prepared statement!", e);
            }
        }
    }

    public static int getintfrombool(final boolean bool) {
        if (bool) {
            return 0;
        } else {
            return -1;
        }
    }

    public static boolean getboolfromint(final int number) {
        if (0 == number) {
            return true;
        } else {
            return false;
        }
    }

    private int[] getGroupsForUser(final Context ctx, final int user_id,
            final Connection read_ox_con) throws SQLException {

        PreparedStatement prep = null;
        try {

            prep = read_ox_con
                    .prepareStatement("SELECT id FROM groups_member WHERE cid = ? AND member = ?");
            prep.setInt(1, ctx.getId());
            prep.setInt(2, user_id);

            final ResultSet rs = prep.executeQuery();

            final ArrayList<Integer> tmp = new ArrayList<Integer>();

            // add colubrids ALL_GROUPS_AND_USERS group to the group
            tmp.add(0);
            while (rs.next()) {
                tmp.add(rs.getInt(1));
            }
            rs.close();

            final int[] ret = new int[tmp.size()];
            for (int a = 0; a < tmp.size(); a++) {
                ret[a] = tmp.get(a);
            }
            return ret;
        } finally {
            closePreparedStatement(prep);
        }
    }

    private void myChangeInsertModuleAccess(final Context ctx,
            final int user_id, final UserModuleAccess access,
            final boolean insert_or_update, final Connection read_ox_con,
            final Connection write_ox_con, final int[] groups)
            throws StorageException {

        try {
            final UserConfiguration user = RdbUserConfigurationStorage
                    .adminLoadUserConfiguration(user_id, groups, ctx.getId(),
                            read_ox_con);

            user.setCalendar(access.getCalendar());
            user.setContact(access.getContacts());
            user.setForum(access.getForum());
            user.setFullPublicFolderAccess(access.getEditPublicFolders());
            user.setFullSharedFolderAccess(access.getReadCreateSharedFolders());
            user.setICal(access.getIcal());
            user.setInfostore(access.getInfostore());
            user.setPinboardWriteAccess(access.getPinboardWrite());
            user.setProject(access.getProjects());
            user.setRSSBookmarks(access.getRssBookmarks());
            user.setRSSPortal(access.getRssPortal());
            user.setSyncML(access.getSyncml());
            user.setTask(access.getTasks());
            user.setVCard(access.getVcard());
            user.setWebDAV(access.getWebdav());
            user.setWebDAVXML(access.getWebdavXml());
            user.setWebMail(access.getWebmail());
            user.setDelegateTasks(access.getDelegateTask());

            RdbUserConfigurationStorage.saveUserConfiguration(user, insert_or_update,
                    write_ox_con);
        } catch (final DBPoolingException pole) {
            log.error("DBPooling Error", pole);
            throw new StorageException(pole.toString());
        } catch (final SQLException sqle) {
            log.error("SQL Error", sqle);
            throw new StorageException(sqle);
        }
    }

    private ArrayList<MethodAndNames> getGetters(final Method[] theMethods) {
        final ArrayList<MethodAndNames> retlist = new ArrayList<MethodAndNames>();

        // Define the returntypes we search for
        final HashSet<String> returntypes = new HashSet<String>(4);
        returntypes.add("java.lang.String");
        returntypes.add("java.lang.Integer");
        returntypes.add("java.lang.Boolean");
        returntypes.add("java.util.Date");

        // First we get all the getters of the user data class
        for (final Method method : theMethods) {
            final String methodname = method.getName();

            if (methodname.startsWith("get")) {
                final String methodnamewithoutprefix = methodname.substring(3);
                if (!Mapper.notallowed.contains(methodnamewithoutprefix)) {
                    if (null != Mapper.method2field
                            .get(methodnamewithoutprefix)) {
                        final String returntype = method.getReturnType()
                                .getName();
                        if (returntypes.contains(returntype)) {
                            retlist.add(new MethodAndNames(method,
                                    methodnamewithoutprefix));
                        }
                    }
                }
            } else if (methodname.startsWith("is")) {
                final String methodnamewithoutprefix = methodname.substring(2);
                if (!Mapper.notallowed.contains(methodnamewithoutprefix)) {
                    if (null != Mapper.method2field
                            .get(methodnamewithoutprefix)) {
                        final String returntype = method.getReturnType()
                                .getName();
                        if (returntypes.contains(returntype)) {
                            retlist.add(new MethodAndNames(method,
                                    methodnamewithoutprefix));
                        }
                    }
                }
            }
        }
        return retlist;
    }

    private Method getMethodforbooleanparameter(final Method method)
            throws SecurityException, NoSuchMethodException {
        final String methodname = method.getName();
        final String boolmethodname = "is" + methodname.substring(3) + "set";
        final Method retval = User.class.getMethod(boolmethodname);
        return retval;
    }

}
