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

package com.openexchange.oauth.impl.internal.groupware;

import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.startTransaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.oauth.API;
import com.openexchange.oauth.scope.OXScope;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * {@link OAuthAddScopeColumnTask}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public class OAuthAddScopeColumnTask extends UpdateTaskAdapter {

    private final DatabaseService dbService;

    public OAuthAddScopeColumnTask(final DatabaseService dbService) {
        super();
        this.dbService = dbService;
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        final int contextId = params.getContextId();
        final Connection writeCon;
        try {
            writeCon = dbService.getForUpdateTask(contextId);
        } catch (final OXException e) {
            throw e;
        }
        try {
            startTransaction(writeCon);
            final List<Column> toCreate = new ArrayList<>();
            if (!Tools.columnExists(writeCon, "oauthAccounts", "scope")) {
                toCreate.add(new Column("scope", "varchar(767)"));
            }
            if (!toCreate.isEmpty()) {
                Tools.addColumns(writeCon, "oauthAccounts", toCreate.toArray(new Column[toCreate.size()]));
            }

            Set<OAuthAccount> accounts = getAccounts(writeCon, contextId);
            if (!accounts.isEmpty()) {
                migrate(writeCon, accounts);
            }
            writeCon.commit();
        } catch (final SQLException e) {
            DBUtils.rollback(writeCon);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            autocommit(writeCon);
            dbService.backForUpdateTask(contextId, writeCon);
        }
    }

    private final static String SQL_MIGRATE = "UPDATE oauthAccounts SET scope=? WHERE cid=? and id=?";

    /**
     * @param writeCon
     * @param accounts
     * @throws SQLException
     */
    private void migrate(Connection writeCon, Set<OAuthAccount> accounts) throws SQLException {
        PreparedStatement stmt = null;
        for (OAuthAccount acc : accounts) {
            Scope scope = Scope.getScopeByServiceId(acc.getServiceId());
            if (scope != null) {
                String scopeValue = scope.getScope();
                try {
                    stmt = writeCon.prepareStatement(SQL_MIGRATE);
                    stmt.setString(1, scopeValue);
                    stmt.setInt(2, acc.getCid());
                    stmt.setInt(3, acc.getId());
                    stmt.execute();
                } finally {
                    closeSQLStuff(stmt);
                }
            }
        }
    }

    private enum Scope {

        BOXCOM(API.BOX_COM.getFullName(), OXScope.drive),
        DROPBOX(API.DROPBOX.getFullName(), OXScope.drive),
        GOOGLE(API.GOOGLE.getFullName(), OXScope.calendar_ro, OXScope.contacts_ro, OXScope.drive, OXScope.offline),
        MSLIVE_CONNECT(API.MS_LIVE_CONNECT.getFullName(), OXScope.calendar_ro, OXScope.contacts_ro, OXScope.drive),
        LINKEDIN(API.LINKEDIN.getFullName(), OXScope.contacts_ro),
        VKONTAKTE(API.VKONTAKTE.getFullName(), OXScope.contacts_ro),
        XING(API.XING.getFullName(), OXScope.contacts_ro),
        YAHOO(API.YAHOO.getFullName(), OXScope.contacts_ro),
        TWITTER(API.TWITTER.getFullName(), OXScope.generic),
        ;

        private String scope;

        private String serviceId;

        Scope(String serviceId, OXScope... modules) {
            this.serviceId = serviceId;
            StringBuilder builder = new StringBuilder();
            for (OXScope m : modules) {
                builder.append(m.name()).append(" ");
            }
            if (builder.length() > 0) {
                builder.setLength(builder.length() - 1);
            }
            scope = builder.toString();
        }

        String getScope() {
            return scope;
        }

        String getServiceId() {
            return serviceId;
        }

        static Scope getScopeByServiceId(String serviceId) {
            for (Scope scope : Scope.values()) {
                if (scope.getServiceId().equals(serviceId)) {
                    return scope;
                }
            }
            return null;
        }

    }

    @Override
    public String[] getDependencies() {
        return new String[] { OAuthCreateTableTask2.class.getName() };
    }

    private Set<OAuthAccount> getAccounts(Connection con, int ctxId) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Set<OAuthAccount> accounts = new LinkedHashSet<>();
        try {
            stmt = con.prepareStatement("SELECT id, serviceId FROM oauthAccounts WHERE cid=?");
            stmt.setInt(1, ctxId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptySet();
            }

            int index;
            do {
                index = 0;
                int id = rs.getInt(++index);
                String serviceId = rs.getString(++index);
                accounts.add(new OAuthAccount(ctxId, id, serviceId));
            } while (rs.next());
        } finally {
            closeSQLStuff(stmt);
        }
        return accounts;
    }

    private class OAuthAccount {

        int cid;
        int id;
        String serviceId;

        /**
         * Initializes a new {@link OAuthAccount}.
         * 
         * @param cid
         * @param id
         * @param serviceId
         */
        public OAuthAccount(int cid, int id, String serviceId) {
            super();
            this.cid = cid;
            this.id = id;
            this.serviceId = serviceId;
        }

        public int getCid() {
            return cid;
        }

        public int getId() {
            return id;
        }

        public String getServiceId() {
            return serviceId;
        }
    }

}
