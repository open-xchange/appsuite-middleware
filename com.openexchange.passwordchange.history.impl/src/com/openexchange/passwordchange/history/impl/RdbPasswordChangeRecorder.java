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

package com.openexchange.passwordchange.history.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Lists;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.passwordchange.history.PasswordChangeRecorderException;
import com.openexchange.passwordchange.history.PasswordChangeInfo;
import com.openexchange.passwordchange.history.PasswordChangeRecorder;
import com.openexchange.passwordchange.history.SortField;
import com.openexchange.passwordchange.history.SortOrder;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;

/**
 * {@link RdbPasswordChangeRecorder} - Default {@link PasswordChangeRecorder}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class RdbPasswordChangeRecorder implements PasswordChangeRecorder {

    private static final Logger LOGGER = LoggerFactory.getLogger(RdbPasswordChangeRecorder.class);

    private static final String GET_DATA       = "SELECT created, source, ip FROM user_password_history WHERE cid=? AND uid=? ORDER BY ";
    private static final String GET_HISTORY_ID = "SELECT id FROM user_password_history WHERE cid=? AND uid=?";

    private static final String CLEAR_FOR_ID_IN = "DELETE FROM user_password_history WHERE id IN (";
    private static final String CLEAR_FOR_USER  = "DELETE FROM user_password_history WHERE cid=? AND uid=?";

    private static final String INSERT_DATA = "INSERT INTO user_password_history (cid, uid, source, ip, created) VALUES (?,?,?,?,?)";

    private static final String SYMBOLIC_NAME = "default";

    private final ServiceLookup service;

    public RdbPasswordChangeRecorder(ServiceLookup service) {
        super();
        this.service = service;
    }

    @Override
    public String getSymbolicName() {
        return SYMBOLIC_NAME;
    }

    @Override
    public List<PasswordChangeInfo> listPasswordChanges(int userID, int contextID) throws OXException {
        return listPasswordChanges(userID, contextID, null);
    }

    @Override
    public List<PasswordChangeInfo> listPasswordChanges(int userID, int contextID, Map<SortField, SortOrder> fieldNames) throws OXException {
        DatabaseService dbService = getService(DatabaseService.class);
        Connection con = dbService.getReadOnly(contextID);
        PreparedStatement stmt = null;
        ResultSet set = null;
        try {
            StringBuilder builder = new StringBuilder(GET_DATA);
            if (null == fieldNames || fieldNames.isEmpty()) {
                // Default is ID ascending
                builder.append("created ");
                builder.append(SortOrder.DESC);
            } else {
                // User send fields
                for (SortField field : fieldNames.keySet()) {
                    builder.append(fieldToTable(field));
                    builder.append(' ');
                    builder.append(fieldNames.get(field));
                    builder.append(',');
                }
                // Remove last ','
                builder.deleteCharAt(builder.length() - 1);
            }

            // Get data
            stmt = con.prepareStatement(builder.toString());
            builder = null;
            stmt.setInt(1, contextID);
            stmt.setInt(2, userID);
            set = stmt.executeQuery();
            if (false == set.next()) {
                return Collections.emptyList();
            }

            // Convert data
            List<PasswordChangeInfo> retval = new LinkedList<>();
            do {
                long created = set.getLong(1);
                String client = set.getString(2);
                String ip = set.getString(3);
                retval.add(new PasswordChangeInfoImpl(created, client, ip));
            } while (set.next());
            return retval;
        } catch (SQLException e) {
            throw PasswordChangeRecorderException.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(set, stmt);
            dbService.backReadOnly(contextID, con);
        }

    }

    @Override
    public void trackPasswordChange(int userID, int contextID, PasswordChangeInfo info) throws OXException {
        DatabaseService dbService = getService(DatabaseService.class);
        ConfigViewFactory cascade = getService(ConfigViewFactory.class);

        // Get writable connection
        Connection con = dbService.getWritable(contextID);
        PreparedStatement stmt = null;
        try {

            // Write info
            stmt = con.prepareStatement(INSERT_DATA);
            stmt.setInt(1, contextID);
            stmt.setInt(2, userID);
            stmt.setString(3, info.getClient());
            if (null == info.getIP()) {
                stmt.setNull(4, Types.VARCHAR);
            } else {
                stmt.setString(4, info.getIP());
            }
            stmt.setLong(5, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw PasswordChangeRecorderException.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
            dbService.backWritable(contextID, con);
        }

        // Clean up data, too
        ConfigView view = cascade.getView(userID, contextID);
        ComposedConfigProperty<Integer> property = view.property(PasswordChangeRecorderProperties.LIMIT.getFQPropertyName(), Integer.class);
        Integer limit;
        if (property.isDefined()) {
            limit = property.get();
            if (null == limit) {
                limit = PasswordChangeRecorderProperties.LIMIT.getDefaultValue(Integer.class);
            }
        } else {
            limit = PasswordChangeRecorderProperties.LIMIT.getDefaultValue(Integer.class);
        }

        try {
            clear(userID, contextID, limit.intValue());
        } catch (Exception e) {
            LOGGER.warn("Failed to clear obsolete password change records from storage", e);
        }
    }

    @Override
    public void clear(int userID, int contextID, int limit) throws OXException {
        // Get writable connection
        DatabaseService dbService = getService(DatabaseService.class);
        Connection con = dbService.getWritable(contextID);
        boolean modified = true;

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (limit <= 0) {
                stmt = con.prepareStatement(CLEAR_FOR_USER);
                stmt.setInt(1, contextID);
                stmt.setInt(2, userID);
                stmt.executeUpdate();
            } else {
                // Get entries
                stmt = con.prepareStatement(GET_HISTORY_ID);
                stmt.setInt(1, contextID);
                stmt.setInt(2, userID);
                rs = stmt.executeQuery();
                List<Integer> data = new LinkedList<>(); // Oldest values have smaller IDs, so we should be fine only using the IDs
                while (rs.next()) {
                    data.add(Integer.valueOf(rs.getInt(1)));
                }
                Databases.closeSQLStuff(rs, stmt);

                int len = data.size() - limit;
                if (len > 0) {
                    boolean rollback = false;
                    try {
                        Databases.startTransaction(con);
                        rollback = true;

                        // Sort & delete the first until limit triggers
                        Collections.sort(data);
                        data = data.subList(0, len);

                        if (len <= 100) {
                            stmt = con.prepareStatement(Databases.getIN(CLEAR_FOR_ID_IN, len));
                            int pos = 1;
                            for (Integer id : data) {
                                stmt.setInt(pos++, id.intValue());
                            }
                            stmt.executeUpdate();
                            Databases.closeSQLStuff(stmt);
                        } else {
                            for (List<Integer> chunk : Lists.partition(data, 100)) {
                                stmt = con.prepareStatement(Databases.getIN(CLEAR_FOR_ID_IN, chunk.size()));
                                int pos = 1;
                                for (Integer id : chunk) {
                                    stmt.setInt(pos++, id.intValue());
                                }
                                stmt.executeUpdate();
                                Databases.closeSQLStuff(stmt);
                            }
                        }

                        con.commit();
                        rollback = false;
                    } finally {
                        if (rollback) {
                            Databases.rollback(con);
                        }
                        Databases.autocommit(con);
                    }
                } else {
                    modified = false;
                }
            }
        } catch (SQLException e) {
            throw PasswordChangeRecorderException.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            if (modified) {
                dbService.backWritable(contextID, con);
            } else {
                dbService.backWritableAfterReading(contextID, con);
            }
        }
    }

    /**
     * Get a specific service
     *
     * @param clazz The class of the service
     * @return The service
     * @throws OXException In case service can not be loaded
     */
    private <T extends Object> T getService(Class<? extends T> clazz) throws OXException {
        T retval = service.getService(clazz);
        if (null == retval) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(clazz.getName());
        }
        return retval;
    }

    /**
     * Get the corresponding table name
     *
     * @param field The field name from REST client
     * @return A string representing a table field name
     */
    private String fieldToTable(SortField field) {
        if (field.equals(SortField.CLIENT_ID)) {
            return "source";
        }
        return "created";
    }
}
