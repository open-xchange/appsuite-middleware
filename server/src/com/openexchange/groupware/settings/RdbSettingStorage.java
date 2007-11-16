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

package com.openexchange.groupware.settings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.settings.SettingException.Code;
import com.openexchange.server.DBPool;
import com.openexchange.server.DBPoolingException;
import com.openexchange.sessiond.Session;

/**
 * This class implements the storage for settings using a relational database.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class RdbSettingStorage extends SettingStorage {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(RdbSettingStorage.class);

    /**
     * SQL statement for loading one specific user setting.
     */
    private static final String SELECT_VALUE = "SELECT value FROM user_setting "
        + "WHERE cid=? AND user_id=? AND path_id=?";

    /**
     * SQL statement for inserting one specific user setting.
     */
    private static final String INSERT_SETTING =
        "INSERT INTO user_setting (value,cid,user_id,path_id) VALUES (?,?,?,?)";

    /**
     * SQL statement for updating one specific user setting.
     */
    private static final String UPDATE_SETTING = "UPDATE user_setting "
        + "SET value=? WHERE cid=? AND user_id=? AND path_id=?";

    /**
     * SQL statement for checking if a setting for a user exists.
     */
    private static final String SETTING_EXISTS = "SELECT COUNT(value) "
        + "FROM user_setting WHERE cid=? AND user_id=? AND path_id=?";

    /**
     * Reference to the context.
     */
    private final transient Session session;

    /**
     * Context.
     */
    private final transient Context ctx;

    /**
     * Default constructor.
     * @param session Session.
     * @throws SettingException if the initialization of the setting tree fails.
     */
    RdbSettingStorage(final Session session) throws SettingException {
        super();
        this.session = session;
        this.ctx = session.getContext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(final Setting setting) throws SettingException {
        if (!setting.isLeaf()) {
            throw new SettingException(Code.NOT_LEAF, setting.getName());
        }
        if (setting.isShared()) {
            final SharedValue value = ConfigTree.getSharedValue(setting);
            if (null != value && value.isWritable()) {
                value.writeValue(session, setting);
            } else {
                throw new SettingException(Code.NO_WRITE, setting.getName());
            }
        } else {
            final int userId = session.getUserId();
            final boolean update = settingExists(userId, setting);
            Connection con;
            try {
                con = DBPool.pickupWriteable(ctx);
            } catch (DBPoolingException e) {
                throw new SettingException(Code.NO_CONNECTION, e);
            }
            PreparedStatement stmt = null;
            try {
                if (update) {
                    stmt = con.prepareStatement(UPDATE_SETTING);
                } else {
                    stmt = con.prepareStatement(INSERT_SETTING);
                }
                int pos = 1;
                stmt.setString(pos++, setting.getSingleValue().toString());
                stmt.setInt(pos++, ctx.getContextId());
                stmt.setInt(pos++, userId);
                stmt.setInt(pos++, setting.getId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new SettingException(Code.SQL_ERROR, e);
            } finally {
                closeSQLStuff(null, stmt);
                DBPool.closeWriterSilent(ctx, con);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void readValues(final Setting setting) throws SettingException {
        if (!setting.isLeaf()) {
            readSubValues(setting);
            return;
        }
        if (setting.isShared()) {
            readSharedValue(setting);
        } else {
            Connection con;
            try {
                con = DBPool.pickup(ctx);
            } catch (DBPoolingException e) {
                throw new SettingException(Code.NO_CONNECTION, e);
            }
            PreparedStatement stmt = null;
            ResultSet result = null;
            try {
                stmt = con.prepareStatement(
                    SELECT_VALUE);
                int pos = 1;
                stmt.setInt(pos++, ctx.getContextId());
                stmt.setInt(pos++, session.getUserId());
                stmt.setInt(pos++, setting.getId());
                result = stmt.executeQuery();
                if (result.next()) {
                    setting.setSingleValue(result.getString(1));
                } else {
                    setting.setSingleValue(null);
                }
            } catch (SQLException e) {
                throw new SettingException(Code.SQL_ERROR, e);
            } finally {
                closeSQLStuff(result, stmt);
                DBPool.closeReaderSilent(ctx, con);
            }
        }
    }

    /**
     * Reads a shared value.
     * @param setting setting Setting.
     */
    private void readSharedValue(final Setting setting) {
        final SharedValue reader = ConfigTree.getSharedValue(setting);
        if (null != reader) {
            if (reader.isAvailable(session)) {
                try {
                    reader.getValue(session, setting);
                } catch (SettingException e) {
                    LOG.error("Problem while reading setting value.", e);
                }
            } else {
                setting.getParent().removeElement(setting);
            }
        }
    }

    /**
     * Checks if a setting is already stored in the database.
     * @param userId unique identifier of the user.
     * @param setting Setting.
     * @return <code>true</code> if a value for the setting exists in the
     * database.
     * @throws SettingException if an error occurs.
     */
    private boolean settingExists(final int userId, final Setting setting)
        throws SettingException {
        Connection con;
        try {
            con = DBPool.pickup(ctx);
        } catch (DBPoolingException e) {
            throw new SettingException(Code.NO_CONNECTION, e);
        }
        boolean exists = false;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SETTING_EXISTS);
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, userId);
            stmt.setInt(pos++, setting.getId());
            result = stmt.executeQuery();
            if (result.next()) {
                exists = result.getInt(1) == 1;
            }
        } catch (SQLException e) {
            throw new SettingException(Code.SQL_ERROR, e);
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(ctx, con);
        }
        return exists;
    }

    /**
     * Reads all sub values of a setting.
     * @param setting setting to read.
     * @throws SettingException if an error occurs while reading the setting.
     */
    private void readSubValues(final Setting setting)
        throws SettingException {
        for (Setting subSetting : setting.getElements()) {
            readValues(subSetting);
        }
        // During reading values all childs may be removed.
        if (setting.isLeaf()) {
            setting.getParent().removeElement(setting);
        }
    }

    /**
     * Closes an open ResultSet and an open Statement.
     * @param result ResultSet.
     * @param stmt Statement.
     */
    private void closeSQLStuff(final ResultSet result, final Statement stmt) {
        if (null != result) {
            try {
                result.close();
            } catch (SQLException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        if (null != stmt) {
            try {
                stmt.close();
            } catch (SQLException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }
}
