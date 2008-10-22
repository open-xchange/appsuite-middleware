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

package com.openexchange.server.impl;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.database.Database;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contact.ContactException;

/**
 * Interface for accessing Configuration settings.
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 *
 */
public class ServerUserSetting {
    
    private static final Log LOG = LogFactory.getLog(ServerUserSetting.class);
    
    private ServerUserSetting(){}
    
    /**
     * Enables/Disables the collection of Contacts triggered by mails.
     * @param cid context id
     * @param user user id
     */
    public static void setContactColletion(final int cid, final int user, final boolean enabled){
        setAttributeWithoutException(cid, user, "contact_collect_enabled", enabled);
    }
    
    /**
     * Gets the flag for Contact collection.
     * @param cid context id
     * @param user user id
     * @return true if mails should be collected, false otherwise
     */
    public static boolean contactCollectionEnabled(final int cid, final int user){
        final Object retval = getAttributeWithoutException(cid, user, "contact_collect_enabled");
        if(retval == null) {
			return false;
		}
        return (Boolean) retval;
    }
    
    /**
     * Sets the folder used to store collected Contacts.
     * @param cid context id
     * @param user user id
     * @param folder folder id
     */
    public static void setContactCollectionFolder(final int cid, final int user, final int folder){
        setAttributeWithoutException(cid, user, "contact_collect_folder", folder);
    }
    
    /**
     * Returns the folder used to store collected Contacts.
     * @param cid context id
     * @param user user id
     * @return folder id
     */
    public static int getContactCollectionFolder(final int cid, final int user){
        final Object retval = getAttributeWithoutException(cid, user, "contact_collect_folder");
        if(retval == null) {
			return 0;
		}
        return (Integer) retval;
    }
    
    private static Object getAttributeWithoutException(final int cid, final int user, final String column) {
        try {
            return getAttribute(cid, user, column);
        } catch (final DBPoolingException e) {
            LOG.info("Can not retrieve Connection", e);
        } catch (final SQLException e) {
            LOG.info("SQL Exception occurred", new ContactException(Category.CODE_ERROR, -1, "SQL Exception occurred", e));
        }
        return null;
    }
    
    private static void setAttributeWithoutException(final int cid, final int user, final String column, final Object value) {
        try {
            setAttribute(cid, user, column, value);
        } catch (final DBPoolingException e) {
            LOG.info("Can not retrieve Connection", e);
        } catch (final SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private static Object getAttribute(final int cid, final int user, final String column) throws DBPoolingException, SQLException{
        Object retval = null;
        final Connection con = Database.get(cid, false);
        final String select = "SELECT " + column + " FROM user_setting_server WHERE cid = ? AND user = ?";
        
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = con.prepareStatement(select);
            stmt.setInt(1, cid);
            stmt.setInt(2, user);
            rs = stmt.executeQuery();
            if (rs.next()) {
				retval = rs.getObject(column);
			}
        } finally {
            closeSQLStuff(rs, stmt);
        }
        
        return retval;
    }
    
    private static void setAttribute(final int cid, final int user, final String column, final Object value) throws DBPoolingException, SQLException {
        final Connection con = Database.get(cid, true);
        final String insert = "INSERT INTO user_setting_server (" + column + ") VALUES (?)";
        
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareCall(insert);
            stmt.setObject(1, value);
            stmt.execute();
        } finally {
            stmt.close();
        }
    }

}
