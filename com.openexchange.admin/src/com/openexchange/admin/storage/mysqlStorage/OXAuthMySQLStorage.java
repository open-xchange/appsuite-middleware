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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.interfaces.OXAuthStorageInterface;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.GenericChecks;

/**
 * Default mysql implementation for admin auth.
 *
 * @author cutmasta
 */
public class OXAuthMySQLStorage extends OXAuthStorageInterface {

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OXAuthMySQLStorage.class);

    private final AdminCache cache;

    /** */
    public OXAuthMySQLStorage() {
        super();
        cache = ClientAdminThread.cache;
    }

    @Override
    public boolean authenticate(final Credentials authdata) {
        return false;
    }

    /**
     *
     * Authenticates the admin user of the system within context.
     *
     */
    @Override
    public boolean authenticate(final Credentials authdata, final Context ctx) throws StorageException {

        if (authdata != null && authdata.getLogin() != null && authdata.getPassword() != null) {

            final Credentials cachedAdminCredentials = cache.getAdminCredentials(ctx);
            //disabling caching for admin-password as fix for bug 15200
            //if(cachedAdminCredentials == null ) {
                final OXToolStorageInterface instance = OXToolStorageInterface.getInstance();
                int uid;
                try {
                    uid = instance.getUserIDByUsername(ctx, authdata.getLogin());
                } catch (NoSuchUserException e) {
                    throw new StorageException(e);
                }
                if (instance.isContextAdmin(ctx, uid)) {
                    Connection sql_con = null;
                    PreparedStatement prep = null;
                    ResultSet rs = null;
                    try {

                        sql_con = cache.getConnectionForContext(ctx.getId());
                        prep = sql_con.prepareStatement("select u.userPassword,u.passwordMech from user u JOIN login2user l JOIN user_setting_admin usa ON u.id = l.id AND u.cid = l.cid AND u.cid = usa.cid AND u.id = usa.user WHERE u.cid = ? AND l.uid = ?");

                        prep.setInt(1, ctx.getId());
                        prep.setString(2, authdata.getLogin());

                        rs = prep.executeQuery();
                        if (!rs.next()) {
                            // auth failed , admin user not found in context
                                log.debug("Admin user \"{}\" not found in context \"{}\"!", authdata.getLogin(), ctx.getId());
                            return false;
                        } else {
                            String pwcrypt = rs.getString("userPassword");
                            String pwmech = rs.getString("passwordMech");
                            if (GenericChecks.authByMech(pwcrypt, authdata.getPassword(), pwmech)) {
                                Credentials cauth = new Credentials(authdata.getLogin(), pwcrypt);
                                cache.setAdminCredentials(ctx, pwmech, cauth);
                                return true;
                            }
                            return false;
                        }
                    } catch (final SQLException sql) {
                        log.error("", sql);
                        throw new StorageException(sql.toString());
                    } catch (final PoolException ex) {
                        log.error("", ex);
                        throw new StorageException(ex);
                    } catch (NoSuchAlgorithmException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    } catch (UnsupportedEncodingException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    } finally {
                        try {
                            if (rs != null) {
                                rs.close();
                            }
                        } catch (final SQLException ecp) {
                            log.error("Error closing resultset", ecp);
                        }

                        try {
                            if (prep != null) {
                                prep.close();
                            }
                        } catch (final SQLException ecp) {
                            log.error("Error closing statement", ecp);
                        }

                        try {
                            cache.pushConnectionForContextAfterReading(ctx.getId(), sql_con);
                        } catch (final PoolException ecp) {
                            log.error("Pool Error", ecp);
                        }
                    }
                } else {
                    return false;
                }
                //disabling caching for admin-password as fix for bug 15200
//            } else {
//                try {
//                    if ( authdata.getLogin().equals(cachedAdminCredentials.getLogin())) {
//                        if ( GenericChecks.authByMech(cachedAdminCredentials.getPassword(),
//                                authdata.getPassword(), ClientAdminThread.cache.getAdminAuthMech(ctx) ) ) {
//                            return true;
//                        } else {
//                            if (log.isDebugEnabled()) {
//                                log.debug("Password for admin user \"{}\" did not match!", authdata.getLogin());
//                            }
//                            return false;
//                        }
//                    } else {
//                        return false;
//                    }
//                } catch (NoSuchAlgorithmException e) {
//                    log.error("", e);
//                    throw new StorageException(e);
//                } catch (UnsupportedEncodingException e) {
//                    log.error("", e);
//                    throw new StorageException(e);
//                }
//
//            }
        } else {
            return false;
        }
    }

    @Override
    public boolean authenticateUser(final Credentials authdata, final Context ctx) throws StorageException {

        if (authdata != null && authdata.getLogin() != null && authdata.getPassword() != null) {
            Connection sql_con = null;
            PreparedStatement prep = null;
            ResultSet rs = null;
            try {

                sql_con = cache.getConnectionForContext(ctx.getId());
                prep = sql_con.prepareStatement("SELECT u.userPassword,u.passwordMech FROM user u JOIN login2user l ON u.id = l.id AND u.cid = l.cid WHERE u.cid = ? AND l.uid = ?");

                prep.setInt(1, ctx.getId());
                prep.setString(2, authdata.getLogin());

                rs = prep.executeQuery();
                if (!rs.next()) {
                    // auth failed , user not found in context
                    log.debug("User \"{}\" not found in context \"{}\"!", authdata.getLogin(), ctx.getId());
                    return false;
                }
                String pwcrypt = rs.getString("userPassword");
                String pwmech  = rs.getString("passwordMech");
                // now check via our crypt mech the password
                if ( GenericChecks.authByMech(pwcrypt, authdata.getPassword(), pwmech) ) {
                    return true;
                }
                log.debug("Password for ser \"{}\" did not match!", authdata.getLogin());
                return false;
            } catch (final SQLException sql) {
                log.error("", sql);
                throw new StorageException(sql.toString());
            } catch (final PoolException ex) {
                log.error("", ex);
                throw new StorageException(ex);
            } catch (NoSuchAlgorithmException e) {
                log.error("", e);
                throw new StorageException(e);
            } catch (UnsupportedEncodingException e) {
                log.error("", e);
                throw new StorageException(e);
            } finally {

                try {
                    if (rs != null) {
                        rs.close();
                    }
                } catch (final SQLException ecp) {
                    log.error("Error closing resultset", ecp);
                }

                try {
                    if (prep != null) {
                        prep.close();
                    }
                } catch (final SQLException ecp) {
                    log.error("Error closing statement", ecp);
                }

                try {
                    cache.pushConnectionForContextAfterReading(ctx.getId(), sql_con);
                } catch (final PoolException ecp) {
                    log.error("Pool Error", ecp);
                }
            }
        } else {
            return false;
        }
    }
}
