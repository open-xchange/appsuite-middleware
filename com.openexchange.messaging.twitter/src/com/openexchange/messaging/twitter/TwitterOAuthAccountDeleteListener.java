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

package com.openexchange.messaging.twitter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.twitter.session.TwitterAccessRegistry;
import com.openexchange.oauth.OAuthAccountDeleteListener;
import com.openexchange.oauth.OAuthAccountInvalidationListener;
import com.openexchange.oauth.OAuthExceptionCodes;

/**
 * {@link TwitterOAuthAccountDeleteListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TwitterOAuthAccountDeleteListener implements OAuthAccountDeleteListener, OAuthAccountInvalidationListener {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TwitterOAuthAccountDeleteListener.class);

    /**
     * Initializes a new {@link TwitterOAuthAccountDeleteListener}.
     */
    public TwitterOAuthAccountDeleteListener() {
        super();
    }

    @Override
    public void onBeforeOAuthAccountDeletion(final int id, final Map<String, Object> eventProps, final int user, final int cid, final Connection con) throws OXException {
        // Nope
    }

    @Override
    public void onAfterOAuthAccountDeletion(final int id, final Map<String, Object> eventProps, final int user, final int cid, final Connection con) throws OXException {
        final List<int[]> dataList = listTwitterMessagingAccounts(user, cid, con);
        for (final int[] data : dataList) {
            if (checkData(id, data, con)) {
                dropAccountByData(data, con);
                int accountId = data[0];
                TwitterAccessRegistry.getInstance().purgeUserAccess(cid, user, accountId);
            }
        }
    }

    @Override
    public void onAfterOAuthAccountInvalidation(int id, Map<String, Object> eventProps, int user, int cid, Connection con) throws OXException {
        TwitterAccessRegistry.getInstance().purgeUserAccess(cid, user, id);
    }

    private static List<int[]> listTwitterMessagingAccounts(final int userId, final int contextId, final Connection writeCon) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = writeCon.prepareStatement("SELECT account, confId, user, cid FROM messagingAccount WHERE cid = ? AND user = ? AND serviceId = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setString(3, TwitterMessagingService.getServiceId());
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptyList();
            }
            final List<int[]> dataList = new ArrayList<int[]>(64);
            do {
                final int[] data = new int[4];
                data[0] = rs.getInt(1); // account
                data[1] = rs.getInt(2); // confId
                data[2] = rs.getInt(3); // user
                data[3] = rs.getInt(4); // cid
                dataList.add(data);
            } while (rs.next());
            return dataList;
        } catch (final SQLException e) {
            throw createSQLError(e);
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    private static boolean checkData(final int accountId, final int[] data, final Connection writeCon) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = writeCon.prepareStatement("SELECT value FROM genconf_attributes_strings WHERE cid = ? AND id = ? AND name = ?");
            stmt.setInt(1, data[3]);
            stmt.setInt(2, data[1]);
            stmt.setString(3, TwitterConstants.TWITTER_OAUTH_ACCOUNT);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return false;
            }
            return Integer.parseInt(rs.getString(1)) == accountId;
        } catch (final SQLException e) {
            throw createSQLError(e);
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    private static void dropAccountByData(final int[] data, final Connection writeCon) throws OXException {
        PreparedStatement stmt = null;
        try {
            /*
             * Delete genconf
             */
            stmt = writeCon.prepareStatement("DELETE FROM genconf_attributes_strings WHERE cid = ? AND id = ?");
            stmt.setInt(1, data[3]);
            stmt.setInt(2, data[1]);
            stmt.executeUpdate();
            /*
             * Delete account
             */
            stmt.close();
            stmt = writeCon.prepareStatement("DELETE FROM messagingAccount WHERE cid = ? AND user = ? AND serviceId = ? AND account = ?");
            stmt.setInt(1, data[3]);
            stmt.setInt(2, data[2]);
            stmt.setString(3, TwitterMessagingService.getServiceId());
            stmt.setInt(4, data[0]);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw createSQLError(e);
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private static OXException createSQLError(final SQLException e) {
        return OAuthExceptionCodes.SQL_ERROR.create(e, e.getMessage());
    }

    /**
     * Closes the ResultSet.
     *
     * @param result <code>null</code> or a ResultSet to close.
     */
    private static void closeSQLStuff(final ResultSet result) {
        if (result != null) {
            try {
                result.close();
            } catch (final SQLException e) {
                LOG.error("", e);
            }
        }
    }

    /**
     * Closes the {@link Statement}.
     *
     * @param stmt <code>null</code> or a {@link Statement} to close.
     */
    private static void closeSQLStuff(final Statement stmt) {
        if (null != stmt) {
            try {
                stmt.close();
            } catch (final SQLException e) {
                LOG.error("", e);
            }
        }
    }

    /**
     * Closes the ResultSet and the Statement.
     *
     * @param result <code>null</code> or a ResultSet to close.
     * @param stmt <code>null</code> or a Statement to close.
     */
    private static void closeSQLStuff(final ResultSet result, final Statement stmt) {
        closeSQLStuff(result);
        closeSQLStuff(stmt);
    }

}
