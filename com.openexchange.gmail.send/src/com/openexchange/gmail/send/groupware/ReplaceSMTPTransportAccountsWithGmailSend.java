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

package com.openexchange.gmail.send.groupware;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateConcurrency;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.java.Sets;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.UserAndContext;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

/**
 * {@link ReplaceSMTPTransportAccountsWithGmailSend}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class ReplaceSMTPTransportAccountsWithGmailSend extends UpdateTaskAdapter {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link ReplaceSMTPTransportAccountsWithGmailSend}.
     */
    public ReplaceSMTPTransportAccountsWithGmailSend(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            Map<UserAndContext, TIntList> smtpTransports = determineGmailSMTPTransports(con);
            if (smtpTransports.isEmpty()) {
                return;
            }

            con.setAutoCommit(false);
            rollback = 1;

            updateGmailSMTPTransports(smtpTransports, con);

            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
        }
    }

    private Map<UserAndContext, TIntList> determineGmailSMTPTransports(Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT t.cid, t.user, t.id FROM user_transport_account AS t LEFT JOIN user_mail_account AS m "
                + "ON t.cid=m.cid AND t.user=m.user AND t.id=m.id "
                + "WHERE t.url LIKE '%smtp.gmail.com%' OR t.url LIKE '%smtp.googlemail.com%' "
                + "AND ((t.oauth IS NOT NULL AND t.oauth > 0) OR (m.oauth IS NOT NULL AND m.oauth > 0));");
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptyMap();
            }

            Map<UserAndContext, TIntList> map = new LinkedHashMap<>();
            do {
                UserAndContext key = UserAndContext.newInstance(rs.getInt(2), rs.getInt(1));
                TIntList accounts = map.get(key);
                if (accounts == null) {
                    accounts = new TIntArrayList(2);
                    map.put(key, accounts);
                }
                accounts.add(rs.getInt(3));
            } while (rs.next());

            return map;
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private void updateGmailSMTPTransports(Map<UserAndContext, TIntList> smtpTransports, Connection con) throws SQLException {
        for (Set<Map.Entry<UserAndContext, TIntList>> partition : Sets.partition(smtpTransports.entrySet(), 1000)) {
            updateGmailSMTPTransportsBatch(partition, con);
        }
    }

    private void updateGmailSMTPTransportsBatch(Set<Map.Entry<UserAndContext, TIntList>> smtpTransports, Connection con) throws SQLException {
        if (smtpTransports.isEmpty()) {
            return;
        }

        MailAccountStorageService mass = services.getOptionalService(MailAccountStorageService.class);

        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE user_transport_account SET url='gmailsend://www.googleapis.com', starttls=0 WHERE cid=? AND user=? AND id=?");
            for (Map.Entry<UserAndContext, TIntList> userAndAccounts : smtpTransports) {
                int contextId = userAndAccounts.getKey().getContextId();
                int userId = userAndAccounts.getKey().getUserId();
                TIntList accountIds = userAndAccounts.getValue();
                TIntIterator it = accountIds.iterator();
                for (int i = accountIds.size(); i-- > 0;) {
                    int accountId = it.next();
                    stmt.setInt(1, contextId);
                    stmt.setInt(2, userId);
                    stmt.setInt(3, accountId);
                    stmt.addBatch();
                    if (mass != null) {
                        try {
                            mass.invalidateMailAccount(accountId, userId, contextId);
                        } catch (Exception e) {
                            // Ignore...
                        }
                    }
                }
            }
            stmt.executeBatch();
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    @Override
    public String[] getDependencies() {
        return new String[] { "com.openexchange.groupware.update.tasks.AddFailedAuthColumnsToMailAccountTablesTask" };
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(UpdateConcurrency.BACKGROUND);
    }

}
