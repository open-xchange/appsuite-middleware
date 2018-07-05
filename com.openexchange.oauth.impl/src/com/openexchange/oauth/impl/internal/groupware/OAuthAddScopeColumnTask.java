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

import static com.openexchange.database.Databases.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.scope.OXScope;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * {@link OAuthAddScopeColumnTask}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.3
 */
public class OAuthAddScopeColumnTask extends AbstractOAuthUpdateTask {

    public OAuthAddScopeColumnTask() {
        super();
    }

    @Override
    void innerPerform(Connection connection, PerformParameters performParameters) throws OXException, SQLException {

        final List<Column> toCreate = new ArrayList<>();
        if (!Tools.columnExists(connection, CreateOAuthAccountTable.TABLE_NAME, "scope")) {
            toCreate.add(new Column("scope", "varchar(767)"));
        }
        if (!toCreate.isEmpty()) {
            Tools.addColumns(connection, CreateOAuthAccountTable.TABLE_NAME, toCreate.toArray(new Column[toCreate.size()]));
        }

        for (int contextId : performParameters.getContextsInSameSchema()) {
            Set<OAuthAccount> accounts = getAccounts(connection, contextId);
            if (!accounts.isEmpty()) {
                migrate(connection, accounts);
            }
        }
    }

    private final static String SQL_MIGRATE = "UPDATE " + CreateOAuthAccountTable.TABLE_NAME + " SET scope=? WHERE cid=? and id=?";

    /**
     * @param writeCon
     * @param accounts
     * @throws SQLException
     */
    private void migrate(Connection writeCon, Set<OAuthAccount> accounts) throws SQLException {
        for (OAuthAccount acc : accounts) {
            Scope scope = Scope.getScopeByServiceId(acc.getServiceId());
            if (scope == null) {
                continue;
            }

            String scopeValue = scope.getScope();
            try (PreparedStatement stmt = writeCon.prepareStatement(SQL_MIGRATE)) {
                stmt.setString(1, scopeValue);
                stmt.setInt(2, acc.getCid());
                stmt.setInt(3, acc.getId());
                stmt.execute();
            }
        }
    }

    private enum Scope {
        BOXCOM(KnownApi.BOX_COM.getFullName(), OXScope.drive),
        DROPBOX(KnownApi.DROPBOX.getFullName(), OXScope.drive),
        GOOGLE(KnownApi.GOOGLE.getFullName(), OXScope.calendar_ro, OXScope.contacts_ro, OXScope.drive),
        MSLIVE_CONNECT(KnownApi.MS_LIVE_CONNECT.getFullName(), OXScope.calendar_ro, OXScope.contacts_ro, OXScope.drive),
        LINKEDIN(KnownApi.LINKEDIN.getFullName(), OXScope.contacts_ro),
        VKONTAKTE(KnownApi.VKONTAKTE.getFullName(), OXScope.contacts_ro),
        XING(KnownApi.XING.getFullName(), OXScope.contacts_ro),
        YAHOO(KnownApi.YAHOO.getFullName(), OXScope.contacts_ro),
        TWITTER(KnownApi.TWITTER.getFullName(), OXScope.generic),
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
        try {
            stmt = con.prepareStatement("SELECT id, serviceId FROM " + CreateOAuthAccountTable.TABLE_NAME + " WHERE cid=?");
            stmt.setInt(1, ctxId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptySet();
            }

            Set<OAuthAccount> accounts = new LinkedHashSet<>();
            int index;
            do {
                index = 0;
                int id = rs.getInt(++index);
                String serviceId = rs.getString(++index);
                accounts.add(new OAuthAccount(ctxId, id, serviceId));
            } while (rs.next());
            return accounts;
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private static final class OAuthAccount {

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
