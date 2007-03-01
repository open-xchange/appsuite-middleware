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

package com.openexchange.admin.auth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.exceptions.PoolException;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.tools.UnixCrypt;

/**
 * Default mysql implementation for admin auth.
 * 
 * @author cutmasta
 */
public class MySQLAuthenticationImpl implements AuthenticationInterface {

    private final static Log log = LogFactory
            .getLog(MySQLAuthenticationImpl.class);

    /** */
    public MySQLAuthenticationImpl() {
    }

    public boolean authenticate(Credentials authdata) throws StorageException {
        return false;
    }

    /**
     * 
     * Authenticates the admin user of the system within context.
     * 
     */
    public boolean authenticate(Credentials authdata, Context ctx)
            throws StorageException {

        if (authdata != null && authdata.getLogin() != null && authdata.getPassword() != null) {
            Connection sql_con = null;
            PreparedStatement prep = null;
            ResultSet rs = null;
            try {

                sql_con = ClientAdminThread.cache
                        .getREADConnectionForContext(ctx.getIdAsInt());
                prep = sql_con
                        .prepareStatement("select u.userPassword,u.passwordMech from user u " +
                                        "JOIN login2user l JOIN user_setting_admin usa ON u.id = l.id " +
                                        "AND u.cid = l.cid AND u.cid = usa.cid AND u.id = usa.user " +
                                        "WHERE u.cid = ? AND l.uid = ?");

                prep.setInt(1, ctx.getIdAsInt());
                prep.setString(2, authdata.getLogin());

                rs = prep.executeQuery();
                if (!rs.next()) {
                    // auth failed , admin user not found in context
                    log.debug("Admin user \""+authdata.getLogin()+"\" not found in context \""+ctx.getIdAsInt()+"\"!");
                    return false;
                }else{
                    // now check via our crypt mech the password
                    if(UnixCrypt.matches(rs.getString("userPassword"), authdata.getPassword())){
                        return true;
                    }else{
                        log.debug("Password for admin user \""+authdata.getLogin()+"\" did not match!");
                        return false;
                    }                    
                }
            } catch (SQLException sql) {
                throw new StorageException(sql);
            } catch (PoolException ex) {
                throw new StorageException(ex);
            } finally {
                
                try{
                    if(rs!=null){
                        rs.close();
                    }
                }catch(Exception ecp){
                    log.error("Error closing resultset");                    
                }           
                
                try{
                    if(prep!=null){
                        prep.close();
                    }
                }catch(Exception ecp){
                    log.error("Error closing statement");                    
                }                
                
                try {
                    ClientAdminThread.cache.pushOXDBRead(ctx.getIdAsInt(),
                            sql_con);
                } catch (PoolException ecp) {
                    log.error("Pool Error", ecp);
                }
            }
        }else{
            return false;
        }
    }
}
