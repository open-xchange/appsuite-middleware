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

package com.openexchange.contact.storage.rdb.internal;

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.storage.rdb.sql.Executor;
import com.openexchange.contact.storage.rdb.sql.Table;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.quota.AccountQuota;
import com.openexchange.quota.AccountQuotas;
import com.openexchange.quota.DefaultAccountQuota;
import com.openexchange.quota.Quota;
import com.openexchange.quota.QuotaExceptionCodes;
import com.openexchange.quota.QuotaProvider;
import com.openexchange.quota.QuotaType;
import com.openexchange.quota.groupware.AmountQuotas;
import com.openexchange.session.Session;

/**
 * {@link RdbContactQuotaProvider}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class RdbContactQuotaProvider implements QuotaProvider {

    private static final String MODULE_ID = "contact";

    public RdbContactQuotaProvider() {
        super();
    }

    @Override
    public String getModuleID() {
        return "contact";
    }

    @Override
    public String getDisplayName() {
        return "Contacts";
    }

    static Quota getAmountQuota(Session session, Executor executor, Connection connection) throws SQLException, OXException {
        long limit = AmountQuotas.getLimit(session, MODULE_ID,
            RdbServiceLookup.getService(ConfigViewFactory.class), connection);
        if (limit <= Quota.UNLIMITED) {
            return Quota.UNLIMITED_AMOUNT;
        }
        long usage = executor.count(connection, Table.CONTACTS, session.getContextId());
        return new Quota(QuotaType.AMOUNT, limit, usage);
    }

    @Override
    public AccountQuota getFor(Session session, String accountID) throws OXException {
        if (!"0".equals(accountID)) {
            throw QuotaExceptionCodes.UNKNOWN_ACCOUNT.create(accountID, MODULE_ID);
        }

        DatabaseService dbService = RdbServiceLookup.getService(DatabaseService.class);
        Connection connection = null;
        try {
            connection = dbService.getReadOnly(session.getContextId());
            return new DefaultAccountQuota(accountID, getDisplayName()).addQuota(getAmountQuota(session, new Executor(), connection));
        } catch (SQLException e) {
            throw QuotaExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (null != connection) {
                dbService.backReadOnly(session.getContextId(), connection);
            }
        }
    }

    @Override
    public AccountQuotas getFor(Session session) throws OXException {
        return new AccountQuotas(getFor(session, "0"));
    }

}
