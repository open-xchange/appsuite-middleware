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

package com.openexchange.passwordchange.history.tracker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.context.ContextService;
import com.openexchange.database.Databases;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.passwordchange.history.osgi.Services;
import com.openexchange.server.impl.DBPool;

/**
 * {@link DatabasePasswordChangeTracker}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class DatabasePasswordChangeTracker implements PasswordChangeTracker {

    private static final String GET = "SELECT created, source, ip FROM user_password_change WHERE cid=? AND uid=?;";
    private static final String GET_HISTORY_ID = "SELECT id FROM user_password_change WHERE cid=? AND uid=?;";
    private static final String GET_SEQUENCE_ID = "SELECT id FROM sequence_password_history WHERE cid=?;";
    private static final String UPDATE_SEQUENCE_ID = "UPDATE sequence_password_history SET id=? WHERE cid=?;";
    private static final String INSERT = "INSERT INTO user_password_change (cid, id, uid, source, ip) VALUES (?,?,?,?,?);";
    private static final String CLEAR = "DELETE FROM user_password_change WHERE cid=? AND id=?;";
    private static final String CLEAR_ALL = "DELETE FROM user_password_change WHERE cid=?;";

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DatabasePasswordChangeTracker.class);

    public DatabasePasswordChangeTracker() {
        super();
    }

    @Override
    public List<PasswordChangeInfo> listPasswordChanges(int userID, int contextID) {
        List<PasswordChangeInfo> retval = new LinkedList<>();
        Connection con = null;
        Context context = null;
        PreparedStatement stmt = null;
        try {
            // Get data
            context = Services.getService(ContextService.class).getContext(contextID);
            con = DBPool.pickupWriteable(context);
            stmt = con.prepareStatement(GET);
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
                    final Timestamp created = set.getTimestamp(1);
                    final String source = set.getString(2);
                    final String ip = set.getString(3);

                    retval.add(new PasswordChangeInfo() {

                        @Override
                        public String modifyOrigin() {
                            return ip;
                        }

                        @Override
                        public String modifiedBy() {
                            return source;
                        }

                        @Override
                        public Timestamp lastModified() {
                            return created;
                        }
                    });
                }
            } catch (SQLException e) {
                LOG.debug("Error while getting password history from DB.");
            }
        } catch (Exception e) {
            LOG.warn("Could not get password history.");
        } finally {
            Databases.closeSQLStuff(stmt);
            DBPool.closeWriterSilent(context, con);
        }
        return retval;

    }

    @Override
    public void trackPasswordChange(int userID, int contextID, PasswordChangeInfo info) {
        Connection con = null;
        Context context = null;
        PreparedStatement stmt = null;
        try {
            context = Services.getService(ContextService.class).getContext(contextID);
            con = DBPool.pickupWriteable(context);
            // First get current sequence number
            stmt = con.prepareStatement(GET_SEQUENCE_ID);
            stmt.setInt(1, contextID);
            stmt.execute();
            ResultSet set = stmt.getResultSet();
            set.next();
            int sequence = set.getInt(1);
            Databases.closeSQLStuff(stmt);

            // Increment and update
            sequence++;
            stmt = con.prepareStatement(UPDATE_SEQUENCE_ID);
            stmt.setInt(1, sequence);
            stmt.setInt(2, contextID);
            stmt.execute();
            Databases.closeSQLStuff(stmt);

            // Write info
            stmt = con.prepareStatement(INSERT);
            stmt.setInt(1, contextID);
            stmt.setInt(2, sequence);
            stmt.setInt(3, userID);
            stmt.setString(4, info.modifiedBy());
            if (null == info.modifyOrigin()) {
                stmt.setNull(5, Types.VARCHAR);
            } else {
                stmt.setString(5, info.modifyOrigin());
            }
            stmt.execute();
        } catch (Exception e) {
            LOG.warn("Could not save password history.");
        } finally {
            Databases.closeSQLStuff(stmt);
            DBPool.closeWriterSilent(context, con);
        }
    }

    @Override
    public void clear(int userID, int contextID, int limit) {
        Connection con = null;
        Context context = null;
        PreparedStatement stmt = null;
        try {
            context = Services.getService(ContextService.class).getContext(contextID);
            con = DBPool.pickupWriteable(context);
            if (limit <= 0) {
                stmt = con.prepareStatement(CLEAR_ALL);
                stmt.setInt(1, contextID);
                stmt.execute();
            } else {
                // Get entries, sort after IDs and delete oldest
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
                    stmt = con.prepareStatement(CLEAR);
                    for (int i = 0; i < data.size() - limit; i++) {
                        stmt.setInt(1, contextID);
                        stmt.setInt(2, data.get(i));
                        stmt.addBatch();
                    }
                    
                    stmt.executeBatch();
                    stmt.execute();
                    Databases.closeSQLStuff(stmt);
                }
            }
        } catch (Exception e) {
            LOG.warn("Could not delete password histroy.");
        } finally {
            Databases.closeSQLStuff(stmt);
            DBPool.closeWriterSilent(context, con);
        }
    }
}
