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

package com.openexchange.user.copy.internal.oauth;

import static com.openexchange.java.Autoboxing.i;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.id.IDGeneratorService;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.user.copy.CopyUserTaskService;
import com.openexchange.user.copy.ObjectMapping;
import com.openexchange.user.copy.UserCopyExceptionCodes;
import com.openexchange.user.copy.internal.CopyTools;
import com.openexchange.user.copy.internal.IntegerMapping;
import com.openexchange.user.copy.internal.connection.ConnectionFetcherTask;
import com.openexchange.user.copy.internal.context.ContextLoadTask;
import com.openexchange.user.copy.internal.user.UserCopyTask;


/**
 * {@link OAuthCopyTask}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class OAuthCopyTask implements CopyUserTaskService {

    private static final String SELECT_ACCOUNTS =
        "SELECT " +
            "id, displayName, accessToken, accessSecret, " +
            "serviceId FROM oauthAccounts " +
        "WHERE " +
            "cid = ? " +
        "AND " +
            "user = ?";

    private static final String INSERT_ACCOUNTS =
        "INSERT INTO " +
            "oauthAccounts " +
            "(cid, user, id, displayName, accessToken, accessSecret, serviceId) " +
        "VALUES " +
            "(?, ?, ?, ?, ?, ?, ?)";

    private final IDGeneratorService idService;


    public OAuthCopyTask(final IDGeneratorService idService) {
        super();
        this.idService = idService;
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#getAlreadyCopied()
     */
    @Override
    public String[] getAlreadyCopied() {
        return new String[] {
            UserCopyTask.class.getName(),
            ContextLoadTask.class.getName(),
            ConnectionFetcherTask.class.getName()
        };
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#getObjectName()
     */
    @Override
    public String getObjectName() {
        return OAuthAccount.class.getName();
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#copyUser(java.util.Map)
     */
    @Override
    public IntegerMapping copyUser(final Map<String, ObjectMapping<?>> copied) throws OXException {
        final CopyTools copyTools = new CopyTools(copied);
        final Integer srcCtxId = copyTools.getSourceContextId();
        final Integer dstCtxId = copyTools.getDestinationContextId();
        final Context dstCtx = copyTools.getDestinationContext();
        final Integer srcUsrId = copyTools.getSourceUserId();
        final Integer dstUsrId = copyTools.getDestinationUserId();
        final Connection srcCon = copyTools.getSourceConnection();
        final Connection dstCon = copyTools.getDestinationConnection();

        final IntegerMapping accountMapping = new IntegerMapping();
        try {
            final boolean hasOAuth = DBUtils.tableExists(srcCon, "oauthAccounts");
            if (hasOAuth) {
                final List<OAuthAccount> accounts = loadOAuthAccountsFromDB(srcCon, i(srcUsrId), i(srcCtxId));
                final Map<Integer, Integer> oAuthMapping = exchangeOAuthIds(dstCon, accounts, dstCtx);
                writeOAuthAccountsToDB(accounts, dstCon, i(dstCtxId), i(dstUsrId));

                for (final Integer origin : oAuthMapping.keySet()) {
                    final Integer target = oAuthMapping.get(origin);
                    accountMapping.addMapping(origin, target);
                }
            }
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        }

        return accountMapping;
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#done(java.util.Map, boolean)
     */
    @Override
    public void done(final Map<String, ObjectMapping<?>> copied, final boolean failed) {
    }

    public Map<Integer, Integer> exchangeOAuthIds(final Connection con, final List<OAuthAccount> accounts, final Context ctx) throws OXException {
        final Map<Integer, Integer> mapping = new HashMap<Integer, Integer>();
        for (final OAuthAccount account : accounts) {
            try {
                final int oldId = account.getId();
                final int newId = idService.getId("com.openexchange.oauth.account", ctx.getContextId());
                account.setId(newId);

                mapping.put(oldId, newId);
            } catch (final OXException e) {
                throw UserCopyExceptionCodes.ID_PROBLEM.create(e, "com.openexchange.oauth.account");
            }
        }

        return mapping;
    }

    List<OAuthAccount> loadOAuthAccountsFromDB(final Connection con, final int uid, final int cid) throws OXException {
        final List<OAuthAccount> accounts = new ArrayList<OAuthAccount>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SELECT_ACCOUNTS);
            stmt.setInt(1, cid);
            stmt.setInt(2, uid);

            rs = stmt.executeQuery();
            while (rs.next()) {
                int i = 1;
                final OAuthAccount account = new OAuthAccount();
                account.setId(rs.getInt(i++));
                account.setDisplayName(rs.getString(i++));
                account.setAccessToken(rs.getString(i++));
                account.setAccessSecret(rs.getString(i++));
                account.setServiceId(rs.getString(i++));

                accounts.add(account);
            }
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }

        return accounts;
    }

    void writeOAuthAccountsToDB(final List<OAuthAccount> accounts, final Connection con, final int cid, final int uid) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(INSERT_ACCOUNTS);
            for (final OAuthAccount account : accounts) {
                int i = 1;
                stmt.setInt(i++, cid);
                stmt.setInt(i++, uid);
                stmt.setInt(i++, account.getId());
                stmt.setString(i++, account.getDisplayName());
                stmt.setString(i++, account.getAccessToken());
                stmt.setString(i++, account.getAccessSecret());
                stmt.setString(i++, account.getServiceId());

                stmt.addBatch();
            }

            stmt.executeBatch();
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }
}
