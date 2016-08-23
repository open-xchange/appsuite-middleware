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

package com.openexchange.groupware.settings.impl;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptions;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.IValueHandlerExtended;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.settings.SettingExceptionCodes;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.server.impl.DBPool;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.sql.DBUtils;

/**
 * This class implements the storage for settings using a relational database.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class RdbSettingStorage extends SettingStorage {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RdbSettingStorage.class);

    /**
     * SQL statement for loading one specific user setting.
     */
    private static final String SELECT_VALUE = "SELECT value FROM user_setting WHERE cid=? AND user_id=? AND path_id=?";

    /**
     * SQL statement for inserting one specific user setting.
     */
    private static final String INSERT_SETTING = "INSERT INTO user_setting (value,cid,user_id,path_id) VALUES (?,?,?,?)";

    /**
     * SQL statement for compare-and-set updating one specific user setting.
     */
    private static final String UPDATE_SETTING_CAS = "UPDATE user_setting SET value=? WHERE cid=? AND user_id=? AND path_id=? AND value=?";

    /**
     * Reference to the context.
     */
    private final Session session;

    private final int ctxId;

    private final int userId;

    /**
     * Context.
     */
    private final Context ctx;

    private final User user;

    private final UserConfiguration userConfig;

    /**
     * Default constructor.
     * @param session Session.
     * @throws OXException if the initialization of the setting tree fails.
     */
    RdbSettingStorage(final Session session) throws OXException {
        this(session, session.getContextId(), session.getUserId());
    }

    RdbSettingStorage(final Session session, final int ctxId,
        final int userId) throws OXException {
        super();
        this.session = session;
        this.ctxId = ctxId;
        this.userId = userId;
        if (session instanceof ServerSession) {
            final ServerSession serverSession = (ServerSession) session;
            ctx = serverSession.getContext();
            user = serverSession.getUser();
            userConfig = serverSession.getUserConfiguration();
        } else {
            ctx = Tools.getContext(ctxId);
            user = Tools.getUser(ctx, userId);
            userConfig = Tools.getUserConfiguration(ctx, userId);
        }
    }

    RdbSettingStorage(final Session session, final Context ctx, final User user,
        final UserConfiguration userConfig) {
        super();
        this.session = session;
        this.ctx = ctx;
        this.ctxId = ctx.getContextId();
        this.user = user;
        this.userId = user.getId();
        this.userConfig = userConfig;
    }

    /**
     * Special constructor for admin daemon.
     * @param ctxId
     * @param userId
     */
    RdbSettingStorage(final int ctxId, final int userId) {
        super();
        this.session = null;
        this.ctxId = ctxId;
        this.userId = userId;
        this.ctx = null;
        this.user = null;
        this.userConfig = null;
    }

    @Override
    public void save(final Setting setting) throws OXException {
        save(null, setting);
    }

    @Override
    public void save(final Connection con, final Setting setting) throws
        OXException {
        if (!setting.isLeaf()) {
            throw SettingExceptionCodes.NOT_LEAF.create(setting.getName());
        }
        if (setting.isShared()) {
            final IValueHandler value = getSharedValue(setting);
            if (null != value && value.isWritable()) {
                value.writeValue(session, ctx, user, setting);
            } else {
                // final OXException e = SettingExceptionCodes.NO_WRITE.create(setting.getName());
                LOG.debug("Writing the setting {} is not permitted.", setting.getName());
            }
        } else {
            saveInternal(con, setting);
        }
    }

    /**
     * Internally saves a setting into the database.
     * @param con a writable database connection or <code>null</code>.
     * @param setting setting to store.
     * @throws OXException if storing fails.
     */
    private void saveInternal(final Connection con, final Setting setting)
        throws OXException {
        if (null == con) {
            Connection myCon = null;
            try {
                myCon = DBPool.pickupWriteable(ctx);
                saveInternal2(myCon, setting);
            } finally {
                if (null != myCon) {
                    DBPool.closeWriterSilent(ctx, myCon);
                }
            }
        } else {
            saveInternal2(con, setting);
        }
    }

    private static final int MAX_RETRY = 3;

    /**
     * Internally saves a setting into the database.
     * @param con a writable database connection.
     * @param setting setting to store.
     * @throws OXException if storing fails.
     */
    private void saveInternal2(final Connection con, final Setting setting) throws OXException {
        saveInternalCAS(con, setting, MAX_RETRY);
    }

    /**
     * Internally saves a setting into the database.
     * @param con a writable database connection.
     * @param setting setting to store.
     * @param retryCount The current retry count
     * @throws OXException if storing fails.
     */
    private void saveInternalCAS(final Connection con, final Setting setting, final int retryCount) throws OXException {
        try {
            // Start
            String val = null;
            int retry = 0;
            boolean tryInsert = true;
            while (val == null) {
                // Try to perform a compare-and-set UPDATE
                String cur;
                do {
                    cur = performSelect(setting, con);
                    if (cur == null) {
                        if (tryInsert) {
                            if (performInsert(setting, con)) {
                                return;
                            }
                            tryInsert = false;
                        }
                        cur = performSelect(setting, con);
                    }
                    if (retry++ > retryCount) {
                        throw SettingExceptionCodes.MAX_RETRY.create();
                    }
                } while (!compareAndSet(setting, cur, con));
                val = setting.getSingleValue().toString();
                // Retry...
            }
            // Return value
            return;
        } catch (DataTruncation e) {
            String name = setting.getName();
            if (null == name) {
                name = Integer.toString(setting.getId());
            }
            throw SettingExceptionCodes.DATA_TRUNCATION.create(name, e);
        } catch (SQLException e) {
            throw SettingExceptionCodes.SQL_ERROR.create(e);
        }
    }

    private boolean compareAndSet(final Setting setting, final String expected, final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(UPDATE_SETTING_CAS);
            int pos = 1;
            stmt.setString(pos++, setting.getSingleValue().toString());
            stmt.setInt(pos++, ctxId);
            stmt.setInt(pos++, userId);
            stmt.setInt(pos++, setting.getId());
            stmt.setString(pos, expected);
            final int result = stmt.executeUpdate();
            return (result > 0);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private boolean performInsert(final Setting setting, final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(INSERT_SETTING);
            int pos = 1;
            stmt.setString(pos++, setting.getSingleValue().toString());
            stmt.setInt(pos++, ctxId);
            stmt.setInt(pos++, userId);
            stmt.setInt(pos, setting.getId());
            try {
                final int result = stmt.executeUpdate();
                return (result > 0);
            } catch (final SQLException e) {
                return false;
            }
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private String performSelect(final Setting setting, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SELECT_VALUE + " FOR UPDATE");
            int pos = 1;
            stmt.setInt(pos++, ctxId);
            stmt.setInt(pos++, userId);
            stmt.setInt(pos, setting.getId());
            result = stmt.executeQuery();
            if (result.next()) {
                return result.getString(1);
            }
            return null;
        } catch (final SQLException e) {
            throw SettingExceptionCodes.SQL_ERROR.create(e);
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    @Override
    public void readValues(final Setting setting) throws OXException {
        final Connection con;
        if (!setting.isLeaf()) {
            readSubValues(setting);
            return;
        }
        if (setting.isShared()) {
            readSharedValue(setting);
        } else {
            con = DBPool.pickup(ctx);
            try {
                readValues(con, setting);
            } finally {
                DBPool.closeReaderSilent(ctx, con);
            }
        }
    }

    @Override
    public void readValues(final Connection con, final Setting setting) throws OXException {
        if (!setting.isLeaf()) {
            readSubValues(setting);
            return;
        }
        if (setting.isShared()) {
            readSharedValue(setting);
        } else {
            PreparedStatement stmt = null;
            ResultSet result = null;
            try {
                stmt = con.prepareStatement(SELECT_VALUE);
                int pos = 1;
                stmt.setInt(pos++, ctxId);
                stmt.setInt(pos++, userId);
                stmt.setInt(pos++, setting.getId());
                result = stmt.executeQuery();
                if (result.next()) {
                    setting.setSingleValue(result.getString(1));
                } else {
                    setting.setSingleValue(null);
                }
            } catch (final SQLException e) {
                throw SettingExceptionCodes.SQL_ERROR.create(e);
            } finally {
                closeSQLStuff(result, stmt);
            }
        }
    }

    private void readSharedValue(final Setting setting) throws OXException {
        final IValueHandler reader = getSharedValue(setting);
        if (null != reader) {
            if (reader instanceof IValueHandlerExtended ? ((IValueHandlerExtended) reader).isAvailable(session, userConfig) : reader.isAvailable(userConfig)) {
                try {
                    reader.getValue(session, ctx, user, userConfig, setting);
                    if (setting.getSingleValue() != null && setting.getSingleValue().equals(IValueHandler.UNDEFINED)) {
                        final Setting parent = setting.getParent();
                        if (null != parent) {
                            parent.removeElement(setting);
                        }
                    }
                } catch (final OXException e) {
                    if (OXExceptions.isPermissionDenied(e)) {
                        LOG.debug("Problem while reading setting value.", e);
                    } else {
                        LOG.error("Problem while reading setting value.", e);
                    }
                }
            } else {
                final Setting parent = setting.getParent();
                if (null != parent) {
                    parent.removeElement(setting);
                }
            }
        }
    }

    /**
     * Reads all sub values of a setting.
     * @param setting setting to read.
     * @throws OXException if an error occurs while reading the setting.
     */
    private void readSubValues(final Setting setting)
        throws OXException {
        for (final Setting subSetting : setting.getElements()) {
            readValues(subSetting);
        }
        // During reading values all childs may be removed.
        if (setting.isLeaf()) {
            Setting parent = setting.getParent();
            if (null != parent) {
                parent.removeElement(setting);
            }
        }
    }

    /**
     * Returns the corresponding reader for a setting.
     * @param setting shared setting.
     * @return the reader for the shared setting.
     */
    static IValueHandler getSharedValue(final Setting setting) {
        IValueHandler retval = null;
        if (setting.isLeaf()) {
            retval = setting.getShared();
        }
        return retval;
    }
}
