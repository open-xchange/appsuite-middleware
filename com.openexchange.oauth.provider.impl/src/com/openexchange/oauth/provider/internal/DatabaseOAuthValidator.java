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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.oauth.provider.internal;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import net.oauth.OAuth;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.OAuthValidator;
import net.oauth.SimpleOAuthValidator;
import org.apache.commons.logging.Log;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.OAuthProviderExceptionCodes;

/**
 * {@link DatabaseOAuthValidator} - A simple {@link OAuthValidator}, which checks the version, whether the time stamp is close to now, the
 * nonce hasn't been used before and the signature is valid. Each check may be overridden.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DatabaseOAuthValidator extends SimpleOAuthValidator {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(DatabaseOAuthValidator.class);

    /**
     * Initializes a new {@link DatabaseOAuthValidator} that rejects messages more than five minutes old or with a OAuth version other than
     * <code>1.0</code>.
     */
    public DatabaseOAuthValidator() {
        super();
    }

    /**
     * Initializes a new {@link DatabaseOAuthValidator}.
     * 
     * @param maxTimestampAgeMsec The range of valid time stamps, in milliseconds into the past or future. So the total range of valid time
     *            stamps is twice this value, rounded to the nearest second.
     * @param maxVersion The maximum valid oauth_version
     */
    public DatabaseOAuthValidator(final long maxTimestampAgeMsec, final double maxVersion) {
        super(maxTimestampAgeMsec, maxVersion);
    }

    @Override
    protected Date validateNonce(final OAuthMessage message, final long timestamp, final long currentTimeMsec) throws IOException, OAuthProblemException {
        try {
            final UsedNonce nonce =
                new UsedNonce(timestamp, message.getParameter(OAuth.OAUTH_NONCE), message.getConsumerKey(), message.getToken());
            /*
             * The OAuth standard requires the token to be omitted from the stored nonce. But I include it, to harmonize with a Consumer
             * that generates nonces using several independent computers, each with its own token.
             */
            final DatabaseService databaseService = OAuthProviderServiceLookup.getService(DatabaseService.class);
            final boolean valid = insertIfAbsent(nonce, databaseService);
            if (!valid) {
                throw new OAuthProblemException(OAuth.Problems.NONCE_USED);
            }
            return removeOldNonces(currentTimeMsec, databaseService);
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
            final String logMessage = e.getPlainLogMessage();
            final Object[] args = e.getLogArgs();
            throw new IOException(String.format(Locale.US, null == logMessage ? "I/O error." : logMessage, null == args ? new Object[0] : args), e);
        }
    }

    /**
     * Remove usedNonces with time stamps that are too old to be valid.
     */
    private Date removeOldNonces(final long currentTimeMsec, final DatabaseService databaseService) throws OXException {
        final Set<String> remove = new HashSet<String>();
        String nextSortKey = null;
        {
            final Connection con = databaseService.getReadOnly();
            final String minSortKey = new UsedNonce((currentTimeMsec - maxTimestampAgeMsec + 500) / 1000L).toString();
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = con.prepareStatement("SELECT nonce FROM oauthNone ORDER BY nonce ASC");
                rs = stmt.executeQuery();
                while (rs.next()) {
                    final String sortKey = rs.getString(1);
                    if (minSortKey.compareTo(sortKey) <= 0) {
                        nextSortKey = minSortKey;
                        break; // Rest is OK
                    }
                    remove.add(sortKey);
                }
            } catch (final SQLException e) {
                throw OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            } finally {
                DBUtils.closeSQLStuff(rs, stmt);
                databaseService.backReadOnly(con);
            }
        }
        // Delete elapsed ones
        if (!remove.isEmpty()) {
            final Connection wcon = databaseService.getWritable();
            PreparedStatement stmt = null;
            try {
                stmt = wcon.prepareStatement("DELETE FROM oauthNone WHERE nonce = ?");
                for (final String nonce : remove) {
                    stmt.setString(1, nonce);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            } catch (final SQLException e) {
                throw OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            } finally {
                databaseService.backWritable(wcon);
            }
        }
        if (nextSortKey == null) {
            return null;
        }
        return new Date((getTimestamp(nextSortKey) * 1000L) + maxTimestampAgeMsec + 500);
    }

    private static long getTimestamp(final String sortKey) {
        final int end = sortKey.indexOf("&");
        if (end < 0) {
            return Long.parseLong(sortKey.trim());
        }
        return Long.parseLong(sortKey.substring(0, end).trim());
    }

    private static boolean insertIfAbsent(final UsedNonce nonce, final DatabaseService databaseService) throws OXException {
        final Connection con = databaseService.getWritable();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO oauthNone (nonce) VALUES (?)");
            stmt.setString(1, nonce.toString());
            try {
                final int result = stmt.executeUpdate();
                return (result > 0);
            } catch (final SQLException e) {
                return false;
            }
        } catch (final SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw OAuthProviderExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
            databaseService.backWritable(con);
        }
    }

}
