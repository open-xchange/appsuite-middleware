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

package com.openexchange.passwordchange.history.handler.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.passwordchange.exception.PasswordChangeHistoryException;
import com.openexchange.passwordchange.history.groupware.PasswordChangeHistoryProperties;
import com.openexchange.passwordchange.history.handler.PasswordChangeInfo;
import com.openexchange.passwordchange.history.handler.PasswordHistoryHandler;
import com.openexchange.passwordchange.history.handler.SortType;
import com.openexchange.server.ServiceLookup;

/**
 * {@link RdbPasswordHistoryHandler}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class RdbPasswordHistoryHandler implements PasswordHistoryHandler {

    private static final String GET_DATA = "SELECT created, source, ip FROM user_password_history WHERE cid=? AND uid=?;";
    private static final String GET_DATA_ASC = "SELECT created, source, ip FROM user_password_history WHERE cid=? AND uid=? ORDER BY id ASC;";
    private static final String GET_DATA_DESC = "SELECT created, source, ip FROM user_password_history WHERE cid=? AND uid=? ORDER BY id DESC;";
    private static final String GET_HISTORY_ID = "SELECT id FROM user_password_history WHERE cid=? AND uid=?;";

    private static final String CLEAR_FOR_ID = "DELETE FROM user_password_history WHERE cid=? AND id=?;";
    private static final String CLEAR_FOR_USER = "DELETE FROM user_password_history WHERE cid=? AND uid=?;";

    private static final String INSERT_DATA = "INSERT INTO user_password_history (cid, uid, source, ip, created) VALUES (?,?,?,?,?);";

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RdbPasswordHistoryHandler.class);
    private static final String SYMBOLIC_NAME = "DB";

    private final ServiceLookup service;

    public RdbPasswordHistoryHandler(ServiceLookup service) {
        super();
        this.service = service;
    }

    @Override
    public String getSymbolicName() {
        return SYMBOLIC_NAME;
    }

    @Override
    public List<PasswordChangeInfo> listPasswordChanges(int userID, int contextID, SortType type) {
        List<PasswordChangeInfo> retval = new LinkedList<>();
        Connection con = null;
        PreparedStatement stmt = null;
        DatabaseService dbService = null;
        try {
            // Get data
            dbService = getService(DatabaseService.class);
            con = dbService.getReadOnly(contextID);
            stmt = con.prepareStatement(getData(type));
            stmt.setInt(1, contextID);
            stmt.setInt(2, userID);
            stmt.execute();
            ResultSet set = stmt.getResultSet();
            if (null == set) {
                return retval;
            }
            try {
                // Convert data
                while (set.next()) {
                    long created = set.getLong(1);
                    String client = set.getString(2);
                    String ip = set.getString(3);

                    retval.add(new PasswordChangeInfoImpl(created, client, ip));
                }
            } catch (SQLException e) {
                LOG.debug("Error while getting password history from DB.");
            }
        } catch (Exception e) {
            LOG.warn("Could not get password history. Cause: {}\nMessage: {} ", e.getCause(), e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
            if (null != dbService) {
                dbService.backReadOnly(contextID, con);
            }
        }
        return retval;

    }

    /**
     * Converts the SortType into fitting SQL command
     * 
     * @return The SQL command
     */
    private String getData(SortType type) {
        switch (type) {
            case NEWEST:
                return GET_DATA_DESC;
            case OLDEST:
                return GET_DATA_ASC;
            case NONE:
            default:
                return GET_DATA;
        }
    }

    @Override
    public void trackPasswordChange(int userID, int contextID, PasswordChangeInfo info) {
        Connection con = null;
        PreparedStatement stmt = null;
        DatabaseService dbService = null;
        try {
            // Get writable connection
            dbService = getService(DatabaseService.class);
            con = dbService.getWritable(contextID);

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
            stmt.execute();
        } catch (Exception e) {
            LOG.warn("Could not save password history. Error: {}", e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
            if (null != dbService) {
                dbService.backWritable(contextID, con);
            }
        }

        // Clean up data too
        try {
            final ConfigViewFactory casscade = getService(ConfigViewFactory.class);
            if (null == casscade) {
                LOG.warn("Could not get config to delete password history.");
            } else {
                Integer limit = null;
                try {
                    limit = casscade.getView(userID, contextID).get(PasswordChangeHistoryProperties.limit.getFQPropertyName(), Integer.class);
                } catch (Exception e) {
                    // Nothing configured. Go with standard value
                    LOG.debug("Error while getting c.o.passwordchange.history.limit for user {}", userID);
                }
                if (null == limit) {
                    limit = PasswordChangeHistoryProperties.limit.getDefaultValue(Integer.class);
                }

                clear(userID, contextID, limit.intValue());
            }
        } catch (Exception e) {
            LOG.warn("Could not clear password change history for " + userID + " in context " + contextID);
        }
    }

    @Override
    public void clear(int userID, int contextID, int limit) {
        Connection con = null;
        PreparedStatement stmt = null;
        DatabaseService dbService = null;
        try {
            // Get writable connection
            dbService = getService(DatabaseService.class);
            con = dbService.getWritable(contextID);
            if (limit <= 0) {
                stmt = con.prepareStatement(CLEAR_FOR_USER);
                stmt.setInt(1, contextID);
                stmt.setInt(2, userID);
                stmt.execute();
            } else {
                // Get entries
                stmt = con.prepareStatement(GET_HISTORY_ID);
                stmt.setInt(1, contextID);
                stmt.setInt(2, userID);
                stmt.execute();
                ResultSet set = stmt.getResultSet();
                List<Integer> data = new LinkedList<>(); // Oldest values have smaller IDs, so we should be fine only using the IDs
                while (set.next()) {
                    data.add(set.getInt(1));
                }
                Databases.closeSQLStuff(stmt);

                if (data.size() > limit) {
                    // Sort & delete the first until limit triggers
                    Collections.sort(data);

                    final int batchSize = 100;
                    int count = 0;

                    stmt = con.prepareStatement(CLEAR_FOR_ID);
                    for (int i = 0; i < data.size() - limit; i++) {
                        stmt.setInt(1, contextID);
                        stmt.setInt(2, data.get(i));
                        stmt.addBatch();

                        // Just in case we have many entries to delete
                        if (++count % batchSize == 0) {
                            stmt.executeBatch();
                        }
                    }
                    stmt.executeBatch();
                }
            }
        } catch (Exception e) {
            LOG.warn("Could not delete password histroy.");
        } finally {
            Databases.closeSQLStuff(stmt);
            if (null != dbService) {
                dbService.backWritable(contextID, con);
            }
        }
    }

    private <T extends Object> T getService(Class<? extends T> clazz) throws OXException {
        T retval = service.getService(clazz);
        if (null == retval) {
            throw PasswordChangeHistoryException.MISSING_SERVICE.create(clazz.getSimpleName());
        }
        return retval;
    }
}
