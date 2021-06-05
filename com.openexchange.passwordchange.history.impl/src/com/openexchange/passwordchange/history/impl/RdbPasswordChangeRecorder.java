/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Lists;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.passwordchange.history.PasswordChangeInfo;
import com.openexchange.passwordchange.history.PasswordChangeRecorder;
import com.openexchange.passwordchange.history.PasswordChangeRecorderException;
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

    private static final String GET_DATA = "SELECT created, source, ip FROM user_password_history WHERE cid=? AND uid=? ORDER BY ";
    private static final String GET_HISTORY_ID = "SELECT id FROM user_password_history WHERE cid=? AND uid=?";

    private static final String CLEAR_FOR_ID_IN = "DELETE FROM user_password_history WHERE id IN (";
    private static final String CLEAR_FOR_USER = "DELETE FROM user_password_history WHERE cid=? AND uid=?";

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
                for (Entry<SortField, SortOrder> field : fieldNames.entrySet()) {
                    builder.append(fieldToTable(field.getKey()));
                    builder.append(' ');
                    builder.append(field.getValue());
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

        // Get limit for the user and check if the password change needs to be saved
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

        if (0 < limit.intValue()) {
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
        }
        // Clean up data, too
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
                    int rollback = 0;
                    try {
                        Databases.startTransaction(con);
                        rollback = 1;

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
                        rollback = 2;
                    } finally {
                        if (rollback > 0) {
                            if (rollback == 1) {
                                Databases.rollback(con);
                            }
                            Databases.autocommit(con);
                        }
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
