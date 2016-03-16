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

package com.openexchange.push.imapidle.locking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.push.PushExceptionCodes;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;


/**
 * {@link DbImapIdleClusterLock}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DbImapIdleClusterLock extends AbstractImapIdleClusterLock {

    private static final String ATTR_IMAPIDLE_LOCK = "imapidle.lock";

    /**
     * Initializes a new {@link DbImapIdleClusterLock}.
     */
    public DbImapIdleClusterLock(ServiceLookup services) {
        super(services);
    }

    @Override
    public Type getType() {
        return Type.DATABASE;
    }

    @Override
    public boolean acquireLock(SessionInfo sessionInfo) throws OXException {
        DatabaseService databaseService = services.getOptionalService(DatabaseService.class);
        if (null == databaseService) {
            throw ServiceExceptionCode.absentService(DatabaseService.class);
        }

        int contextId = sessionInfo.getContextId();
        Connection con = databaseService.getWritable(contextId);
        try {
            return acquireDbLock(sessionInfo, con);
        } finally {
            databaseService.backWritable(contextId, con);
        }
    }

    private boolean acquireDbLock(SessionInfo sessionInfo, Connection con) throws OXException {
        int contextId = sessionInfo.getContextId();
        int userId = sessionInfo.getUserId();

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("INSERT INTO user_attribute (cid, id, name, value, uuid) SELECT ?, ?, ?, ?, ? FROM dual WHERE NOT EXISTS (SELECT 1 FROM user_attribute WHERE cid=? AND id=? AND name=?)");
            long now = System.nanoTime();
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setString(pos++, ATTR_IMAPIDLE_LOCK);
            stmt.setString(pos++, generateValue(now, sessionInfo));
            stmt.setBytes(pos++, UUIDs.toByteArray(UUID.randomUUID()));
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setString(pos++, ATTR_IMAPIDLE_LOCK);
            if (stmt.executeUpdate() > 0) {
                return true;
            }

            // Check if elapsed
            Databases.closeSQLStuff(stmt);
            stmt = con.prepareStatement("SELECT value FROM user_attribute WHERE cid=? AND id=? AND name=?");
            pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setString(pos++, ATTR_IMAPIDLE_LOCK);
            rs = stmt.executeQuery();
            String previous = rs.getString(pos);
            Databases.closeSQLStuff(rs, stmt);

            // Check if valid
            if (validValue(previous, now, sessionInfo.isTransient(), services.getOptionalService(HazelcastInstance.class))) {
                // Locked
                return false;
            }

            // Invalid entry - try to replace it mutually exclusive
            stmt = con.prepareStatement("UPDATE user_attribute SET value=? WHERE cid=? AND id=? AND name=? AND value=?");
            pos = 1;
            stmt.setString(pos++, generateValue(now, sessionInfo));
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setString(pos++, ATTR_IMAPIDLE_LOCK);
            stmt.setString(pos++, previous);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public void refreshLock(SessionInfo sessionInfo) throws OXException {
        DatabaseService databaseService = services.getOptionalService(DatabaseService.class);
        if (null == databaseService) {
            throw ServiceExceptionCode.absentService(DatabaseService.class);
        }

        int contextId = sessionInfo.getContextId();
        Connection con = databaseService.getWritable(contextId);
        try {
            refreshDbLock(sessionInfo, con);
        } finally {
            databaseService.backWritable(contextId, con);
        }
    }

    private void refreshDbLock(SessionInfo sessionInfo, Connection con) throws OXException {
        int contextId = sessionInfo.getContextId();
        int userId = sessionInfo.getUserId();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE user_attribute SET value=? WHERE cid=? AND id=? AND name=?");
            int pos = 1;
            stmt.setString(pos++, generateValue(System.nanoTime(), sessionInfo));
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setString(pos++, ATTR_IMAPIDLE_LOCK);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    @Override
    public void releaseLock(SessionInfo sessionInfo) throws OXException {
        DatabaseService databaseService = services.getOptionalService(DatabaseService.class);
        if (null == databaseService) {
            throw ServiceExceptionCode.absentService(DatabaseService.class);
        }

        int contextId = sessionInfo.getContextId();
        Connection con = databaseService.getWritable(contextId);
        try {
            releaseDbLock(sessionInfo, con);
        } finally {
            databaseService.backWritable(contextId, con);
        }
    }

    private void releaseDbLock(SessionInfo sessionInfo, Connection con) throws OXException {
        int contextId = sessionInfo.getContextId();
        int userId = sessionInfo.getUserId();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM user_attribute WHERE cid=? AND id=? AND name=?");
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setString(pos++, ATTR_IMAPIDLE_LOCK);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

}
