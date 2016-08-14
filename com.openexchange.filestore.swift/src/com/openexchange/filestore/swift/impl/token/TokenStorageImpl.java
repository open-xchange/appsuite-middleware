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

package com.openexchange.filestore.swift.impl.token;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.swift.SwiftExceptionCode;
import com.openexchange.server.ServiceLookup;

/**
 * {@link TokenStorageImpl} - The token storage implementation.
 * <pre>
 * CREATE TABLE swift_token (
 *    id VARCHAR(32) CHARACTER SET ascii COLLATE ascii_general_ci NOT NULL,
 *    token VARCHAR(256) COLLATE utf8_unicode_ci NOT NULL,
 *    expires BIGINT(64) NOT NULL,
 *    PRIMARY KEY (id)
 * ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
 * </pre>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class TokenStorageImpl implements TokenStorage {

    /**
     * The lock timeout in milliseconds.
     * <p>
     * A lock is considered as expired if hold longer than 20 seconds.
     */
    private static final long LOCK_TIMEOUT = TimeUnit.SECONDS.toMillis(20L);

    private final ServiceLookup services;

    /**
     * Initializes a new {@link TokenStorageImpl}.
     */
    public TokenStorageImpl(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public boolean lock(String filestoreId) throws OXException {
        DatabaseService databaseService = services.getService(DatabaseService.class);
        Connection con = databaseService.getWritable();
        boolean locked = false;
        try {
            locked = lock(filestoreId, con);
            return locked;
        } finally {
            if (locked) {
                databaseService.backWritable(con);
            } else {
                databaseService.backWritableAfterReading(con);
            }
        }
    }

    private boolean lock(String filestoreId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO swift_token (id, token, expires) VALUES (?, ?, ?)");
            stmt.setString(1, filestoreId + ".lock");
            stmt.setString(2, "LOCKED");
            stmt.setLong(3, System.currentTimeMillis());
            try {
                stmt.executeUpdate();
                return true;
            } catch (SQLException e) {
                if (Databases.isPrimaryKeyConflictInMySQL(e)) {
                    // Already locked...
                    Databases.closeSQLStuff(stmt);
                    return replaceLockIfExpired(filestoreId, con);
                }
                throw e;
            }
        } catch (SQLException e) {
            throw SwiftExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private boolean replaceLockIfExpired(String filestoreId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT expires FROM swift_token WHERE id=?");
            stmt.setString(1, filestoreId + ".lock");
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return false;
            }

            long currentExpiration = rs.getLong(1);
            if (System.currentTimeMillis() - currentExpiration <= LOCK_TIMEOUT) {
                return false;
            }

            // Expired...
            Databases.closeSQLStuff(rs, stmt);
            stmt = null;
            rs = null;

            stmt = con.prepareStatement("UPDATE swift_token SET expires=? WHERE id=? AND expires=?");
            stmt.setLong(1, System.currentTimeMillis());
            stmt.setString(2, filestoreId + ".lock");
            stmt.setLong(3, currentExpiration);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw SwiftExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public void unlock(String filestoreId) throws OXException {
        DatabaseService databaseService = services.getService(DatabaseService.class);
        Connection con = databaseService.getWritable();
        try {
            unlock(filestoreId, con);
        } finally {
            databaseService.backWritable(con);
        }
    }

    private void unlock(String filestoreId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM swift_token WHERE id=?");
            stmt.setString(1, filestoreId + ".lock");
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw SwiftExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    @Override
    public boolean isLocked(String filestoreId) throws OXException {
        DatabaseService databaseService = services.getService(DatabaseService.class);
        Connection con = databaseService.getWritable();
        try {
            return isLocked(filestoreId, con);
        } finally {
            databaseService.backWritable(con);
        }
    }

    private boolean isLocked(String filestoreId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT expires FROM swift_token WHERE id=?");
            stmt.setString(1, filestoreId + ".lock");
            rs = stmt.executeQuery();
            return rs.next() && ((System.currentTimeMillis() - rs.getLong(1)) <= LOCK_TIMEOUT);
        } catch (SQLException e) {
            throw SwiftExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public void store(Token token, String filestoreId) throws OXException {
        DatabaseService databaseService = services.getService(DatabaseService.class);
        Connection con = databaseService.getWritable();
        try {
            store(token, filestoreId, con);
        } finally {
            databaseService.backWritable(con);
        }
    }

    private void store(Token token, String filestoreId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("REPLACE INTO swift_token (id, token, expires) VALUES (?, ?, ?)");
            stmt.setString(1, filestoreId);
            stmt.setString(2, token.getId());
            stmt.setLong(3, token.getExpires().getTime());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw SwiftExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    @Override
    public Token get(String filestoreId) throws OXException {
        DatabaseService databaseService = services.getService(DatabaseService.class);
        Connection con = databaseService.getReadOnly();
        try {
            return get(filestoreId, con);
        } finally {
            databaseService.backReadOnly(con);
        }
    }

    private Token get(String filestoreId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT token, expires FROM swift_token WHERE id=?");
            stmt.setString(1, filestoreId);
            rs = stmt.executeQuery();
            return rs.next() ? new Token(rs.getString(1), new Date(rs.getLong(2))) : null;
        } catch (SQLException e) {
            throw SwiftExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

}
