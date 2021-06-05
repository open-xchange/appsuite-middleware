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

package com.openexchange.mailaccount.internal;

import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.database.Databases;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.tools.net.URIDefaults;
import com.openexchange.tools.net.URIParser;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.procedure.TIntProcedure;

/**
 * {@link Sanitizer} - Sanitizes mail accounts if needed.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class Sanitizer {

    private static final class InvalidateProcedure implements TIntProcedure {

        private final MailAccountStorageService storageService;

        private final int user;

        private final int contextId;

        protected InvalidateProcedure(final MailAccountStorageService storageService, final int user, final int contextId) {
            this.storageService = storageService;
            this.user = user;
            this.contextId = contextId;
        }

        @Override
        public boolean execute(final int accountId) {
            try {
                storageService.invalidateMailAccount(accountId, user, contextId);
            } catch (OXException e) {
                // Swallow
                org.slf4j.LoggerFactory.getLogger(Sanitizer.class).error("", e);
            }
            return true;
        }
    }

    private static final class AddBatchProcedure implements TIntObjectProcedure<String> {

        private final int contextId;

        private final int user;

        private final PreparedStatement stmt;

        protected AddBatchProcedure(final int contextId, final int user, final PreparedStatement stmt) {
            this.contextId = contextId;
            this.user = user;
            this.stmt = stmt;
        }

        @Override
        public boolean execute(final int accountId, final String uri) {
            try {
                stmt.setString(1, uri);
                stmt.setInt(2, contextId);
                stmt.setInt(3, user);
                stmt.setInt(4, accountId);
                stmt.addBatch();
                return true;
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    /**
     * Initializes a new {@link Sanitizer}.
     */
    private Sanitizer() {
        super();
    }

    /**
     * Sanitizes all user mail accounts in the specified context.
     *
     * @param contextId The context identifier
     * @param storageService The storage service
     * @throws OXException If sanitizing fails
     */
    protected static void sanitize(int contextId, MailAccountStorageService storageService) throws OXException {
        Connection connection = Database.get(contextId, true);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.prepareStatement("SELECT id FROM user WHERE cid = ?");
            stmt.setInt(1, contextId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                sanitize(rs.getInt(1), contextId, storageService, URIDefaults.IMAP, "imap://localhost:143", connection);
            }
        } catch (SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            Database.back(contextId, true, connection);
        }
    }

    /**
     * Sanitizes specified user's mail accounts.
     *
     * @param user The user identifier
     * @param contextId The context identifier
     * @param storageService The storage service
     * @throws OXException If sanitizing fails
     */
    protected static void sanitize(final int user, final int contextId, final MailAccountStorageService storageService) throws OXException {
        Connection connection = Database.get(contextId, true);
        try {
            sanitize(user, contextId, storageService, URIDefaults.IMAP, "imap://localhost:143", connection);
        } finally {
            Database.back(contextId, true, connection);
        }
    }

    /**
     * Sanitizes specified user's mail accounts.
     *
     * @param user The user identifier
     * @param contextId The context identifier
     * @param storageService The storage service
     * @param connection The writeable connection
     * @throws OXException If sanitizing fails
     */
    private static void sanitize(final int user, final int contextId, final MailAccountStorageService storageService, final URIDefaults defaults, final String fallbackUri, Connection connection) throws OXException {
        /*
         * A map to store broken accounts
         */
        final TIntObjectMap<String> map = new TIntObjectHashMap<String>(2);
        /*
         * Do SQL...
         */
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            connection.setAutoCommit(false);
            stmt = connection.prepareStatement("SELECT url, id FROM user_mail_account WHERE cid = ? AND user = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, user);
            rs = stmt.executeQuery();
            while (rs.next()) {
                final String url = rs.getString(1);
                /*
                 * Check if current URI is broken
                 */
                if (!URIParser.isValid(url)) {
                    /*
                     * Detected broken URI in mail account
                     */
                    final URI sanitized = URIParser.sanitize(url, defaults);
                    map.put(rs.getInt(2), null == sanitized ? fallbackUri : sanitized.toString());
                }
            }
            if (!map.isEmpty()) {
                Databases.closeSQLStuff(rs, stmt);
                rs = null;
                /*
                 * Batch update broken accounts
                 */
                stmt = connection.prepareStatement("UPDATE user_mail_account SET url = ? WHERE cid = ? AND user = ? AND id = ?");
                map.forEachEntry(new AddBatchProcedure(contextId, user, stmt));
                stmt.executeBatch();
                /*
                 * Invalidate cache
                 */
                map.forEachKey(new InvalidateProcedure(storageService, user, contextId));
            }
            /*
             * Commit possible changes
             */
            connection.commit();
        } catch (SQLException e) {
            Databases.rollback(connection);
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (IllegalStateException e) {
            Databases.rollback(connection);
            final Throwable cause = e.getCause();
            if (null != cause) {
                throw MailAccountExceptionCodes.SQL_ERROR.create(cause, cause.getMessage());
            }
            throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            Databases.rollback(connection);
            throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }
}
