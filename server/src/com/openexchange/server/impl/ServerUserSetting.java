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

import com.openexchange.api2.OXException;
import com.openexchange.database.Database;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contact.ContactException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.tools.oxfolder.OXFolderAccess;

/**
 * Interface for accessing Configuration settings.
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 *
 */
public class ServerUserSetting {
    
    private static final String CONTACT_COLLECT_ENABLED = "contact_collect_enabled";
    private static final String CONTACT_COLLECT_FOLDER = "contact_collect_folder";
    
    private static final Log LOG = LogFactory.getLog(ServerUserSetting.class);
    
    private ServerUserSetting(){}
    
    /**
     * Enables/Disables the collection of Contacts triggered by mails.
     * @param cid context id
     * @param user user id
     */
    public static void setContactColletion(final int cid, final int user, final boolean enabled){
        try {
            if(getAttributeWithoutException(cid, user, CONTACT_COLLECT_FOLDER) == null) {
                final int defaultFolder = new OXFolderAccess(ContextStorage.getStorageContext(cid)).getDefaultFolder(user, FolderObject.CONTACT).getObjectID();
                setAttributeWithoutException(cid, user, CONTACT_COLLECT_FOLDER, defaultFolder);
            }
            setAttributeWithoutException(cid, user, CONTACT_COLLECT_ENABLED, enabled);
        } catch (ContextException e) {
            LOG.info("Error during Context creation.", e);
        } catch (OXException e) {
            LOG.info("Error during folder creation.", e);
        }
    }
    
    /**
     * Gets the flag for Contact collection.
     * @param cid context id
     * @param user user id
     * @return true if mails should be collected, false otherwise
     */
    public static boolean contactCollectionEnabled(final int cid, final int user){
        boolean retval = false;
        final Object temp = getAttributeWithoutException(cid, user, CONTACT_COLLECT_ENABLED);
        if(temp != null) {
            retval = (Boolean)temp;
        }
        return retval;
    }
    
    /**
     * Sets the folder used to store collected Contacts.
     * @param cid context id
     * @param user user id
     * @param folder folder id
     */
    public static void setContactCollectionFolder(final int cid, final int user, final long folder){
        if(getAttributeWithoutException(cid, user, CONTACT_COLLECT_ENABLED) == null) {
            setAttributeWithoutException(cid, user, CONTACT_COLLECT_ENABLED, false);
        }
        setAttributeWithoutException(cid, user, CONTACT_COLLECT_FOLDER, folder);
    }
    
    /**
     * Returns the folder used to store collected Contacts.
     * @param cid context id
     * @param user user id
     * @return folder id
     */
    public static int getContactCollectionFolder(final int cid, final int user){
        final Object retval = getAttributeWithoutException(cid, user, CONTACT_COLLECT_FOLDER);
        if(retval == null) {
			return 0;
		}
        return (int)(long)(Long) retval;
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
            if(hasEntry(cid, user)){
                updateAttribute(cid, user, column, value);
            } else {
                setAttribute(cid, user, column, value);
            }
        } catch (final DBPoolingException e) {
            LOG.info("Can not retrieve Connection", e);
        } catch (final SQLException e) {
            LOG.info("SQL Exception occurred", new ContactException(Category.CODE_ERROR, -1, "SQL Exception occurred", e));
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
            Database.back(cid, false, con);
        }
        
        return retval;
    }
    
    private static void updateAttribute(final int cid, final int user, final String column, final Object value) throws DBPoolingException, SQLException{
        final Connection con = Database.get(cid, true);
        final String update = "UPDATE user_setting_server SET " + column + " = ? WHERE cid = ? AND user = ?";
        
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(update);
            stmt.setObject(1, value);
            stmt.setInt(2, cid);
            stmt.setInt(3, user);
            stmt.execute();
        } finally {
            stmt.close();
            Database.back(cid, true, con);
        }
    }
    
    private static void setAttribute(final int cid, final int user, final String column, final Object value) throws DBPoolingException, SQLException{
        final Connection con = Database.get(cid, true);
        final String insert = "INSERT INTO user_setting_server (cid, user, " + column + ") VALUES (?, ?, ?)";
        
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(insert);
            stmt.setInt(1, cid);
            stmt.setInt(2, user);
            stmt.setObject(3, value);
            stmt.execute();
        } finally {
            stmt.close();
            Database.back(cid, true, con);
        }
    }
    
    private static boolean hasEntry(final int cid, final int user) throws DBPoolingException, SQLException{
        boolean retval = false;
        final Connection con = Database.get(cid, false);
        final String select = "SELECT * FROM user_setting_server WHERE cid = ? AND user = ?";
        
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = con.prepareStatement(select);
            stmt.setInt(1, cid);
            stmt.setInt(2, user);
            rs = stmt.executeQuery();
            retval = rs.next();
        } finally {
            closeSQLStuff(rs, stmt);
            Database.back(cid, false, con);
        }
        
        return retval;
    }

}
