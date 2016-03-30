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

package com.openexchange.mailaccount.internal;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.procedure.TIntProcedure;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.tools.net.URIDefaults;
import com.openexchange.tools.net.URIParser;
import com.openexchange.tools.sql.DBUtils;

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
            } catch (final OXException e) {
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
            } catch (final SQLException e) {
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
     * Sanitizes specified user's mail accounts.
     *
     * @param user The user identifier
     * @param contextId The context identifier
     * @param storageService The storage service
     * @throws OXException If sanitizing fails
     */
    protected static void sanitize(final int user, final int contextId, final MailAccountStorageService storageService) throws OXException {
        sanitize(user, contextId, storageService, URIDefaults.IMAP, "imap://localhost:143");
    }

    /**
     * Sanitizes specified user's mail accounts.
     *
     * @param user The user identifier
     * @param contextId The context identifier
     * @param storageService The storage service
     * @throws OXException If sanitizing fails
     */
    protected static void sanitize(final int user, final int contextId, final MailAccountStorageService storageService, final URIDefaults defaults, final String fallbackUri) throws OXException {
        final Connection con;
        try {
            con = Database.get(contextId, true);
            con.setAutoCommit(false);
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        }
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
            stmt = con.prepareStatement("SELECT url, id FROM user_mail_account WHERE cid = ? AND user = ?");
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
                DBUtils.closeSQLStuff(rs, stmt);
                rs = null;
                /*
                 * Batch update broken accounts
                 */
                stmt = con.prepareStatement("UPDATE user_mail_account SET url = ? WHERE cid = ? AND user = ? AND id = ?");
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
            con.commit();
        } catch (final SQLException e) {
            DBUtils.rollback(con);
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final IllegalStateException e) {
            DBUtils.rollback(con);
            final Throwable cause = e.getCause();
            if (null != cause) {
                throw MailAccountExceptionCodes.SQL_ERROR.create(cause, cause.getMessage());
            }
            throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            DBUtils.rollback(con);
            throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            Database.back(contextId, true, con);
        }
    }

}
