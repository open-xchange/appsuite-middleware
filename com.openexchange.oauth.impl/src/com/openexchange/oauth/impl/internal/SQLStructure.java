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

package com.openexchange.oauth.impl.internal;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
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
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
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
        SCOPE("scope"),
        IDENTITY("identity"),
        EXPIRY_DATE("expiryDate");

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
                    String[] scopes = new String[enabledScopes.size()];
                    int index = 0;
                    for (OAuthScope s : enabledScopes){
                        scopes[index++] = s.getOXScope().name();
                    }
                    return Strings.concat(" ", scopes);
                case IDENTITY:
                    return account.getUserIdentity();
                case EXPIRY_DATE:
                    return L(account.getExpiration());
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
     * Performs an UPDATE for the specified account.
     *
     * @param account The account
     * @param contextId The context identifier
     * @param user The user identifier
     * @param values The added values in insertion order
     * @return The UPDATE command
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
        update.WHERE(new EQUALS(OAUTH_COLUMN.CID.getColumn(), I(contextId)).AND(new EQUALS(OAUTH_COLUMN.ID.getColumn(), I(account.getId()))));
        return update;
    }
}
