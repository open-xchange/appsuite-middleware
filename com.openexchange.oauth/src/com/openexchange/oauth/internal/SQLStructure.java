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

package com.openexchange.oauth.internal;

import static com.openexchange.sql.grammar.Constant.PLACEHOLDER;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import com.openexchange.java.Strings;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.scope.OAuthScope;
import com.openexchange.sql.grammar.Column;
import com.openexchange.sql.grammar.EQUALS;
import com.openexchange.sql.grammar.INSERT;
import com.openexchange.sql.grammar.Table;
import com.openexchange.sql.grammar.UPDATE;

/**
 * {@link SQLStructure}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SQLStructure {

    /**
     * The "oauthAccounts" table.
     */
    public static final Table OAUTH_ACCOUNTS = new Table("oauthAccounts");

    /**
     * The columns of "oauthAccounts" table.
     */
    public enum OAUTH_COLUMN {
        CID("cid"),
        USER("user"),
        ID("id"),
        DISPLAY_NAME("displayName"),
        ACCESS_TOKEN("accessToken"),
        ACCESS_SECRET("accessSecret"),
        SERVICE_ID("serviceId"),
        SCOPE("scope");

        public static Set<OAUTH_COLUMN> updateableColumns = EnumSet.complementOf(EnumSet.of(CID, USER, ID, SERVICE_ID));

        private final Column column;

        private OAUTH_COLUMN(final String colName) {
            this.column = new Column(colName);
        }

        /**
         * Gets the associated column
         *
         * @return The associated column
         */
        public Column getColumn() {
            return column;
        }

        /**
         * Gets this column's associated value from given OAuth account
         *
         * @param account The account
         * @param cid The context identifier
         * @param userId The user identifier
         * @return The associated value or <code>null</code> if unknown
         */
        public Object get(final OAuthAccount account, final int cid, final int userId) {
            switch (this) {
                case CID:
                    return Integer.valueOf(cid);
                case USER:
                    return Integer.valueOf(userId);
                case ID:
                    return Integer.valueOf(account.getId());
                case DISPLAY_NAME:
                    return account.getDisplayName();
                case ACCESS_TOKEN:
                    return account.getToken();
                case ACCESS_SECRET:
                    return account.getSecret();
                case SERVICE_ID:
                    return account.getMetaData().getId();
                case SCOPE:
                    Set<OAuthScope> enabledScopes = account.getEnabledScopes();
                    return Strings.concat(",", enabledScopes.toArray(new Object[enabledScopes.size()]));
                default:
                    break;
            }
            return null;
        }

    }

    /**
     * Performs an INSERT for specified account.
     *
     * @param account The account
     * @param contextId The context identifier
     * @param user The user identifier
     * @param values The added values in insertion order
     * @return The INSERT command
     */
    public static INSERT insertAccount(final OAuthAccount account, final int contextId, final int user, final List<Object> values) {
        final INSERT insert = new INSERT().INTO(OAUTH_ACCOUNTS);
        for (final OAUTH_COLUMN column : OAUTH_COLUMN.values()) {
            final Object o = column.get(account, contextId, user);
            if (o != null) {
                insert.SET(column.getColumn(), PLACEHOLDER);
                values.add(o);
            }
        }

        return insert;
    }

    /**
     * @param account
     * @param contextId
     * @param user
     * @param values
     * @return
     */
    public static UPDATE updateAccount(final OAuthAccount account, int contextId, int user, ArrayList<Object> values) {
        final UPDATE update = new UPDATE(OAUTH_ACCOUNTS);
        for (final OAUTH_COLUMN column : OAUTH_COLUMN.updateableColumns) {
            final Object o = column.get(account, contextId, user);
            if (o != null) {
                update.SET(column.getColumn(), PLACEHOLDER);
                values.add(o);
            }
        }
        update.WHERE(new EQUALS(OAUTH_COLUMN.CID.getColumn(), contextId).AND(new EQUALS(OAUTH_COLUMN.ID.getColumn(), account.getId())));
        return update;
    }

}
