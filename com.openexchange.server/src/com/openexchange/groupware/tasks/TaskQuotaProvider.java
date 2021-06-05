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

package com.openexchange.groupware.tasks;

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.config.cascade.ConfigViewFactory;
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
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class TaskQuotaProvider implements QuotaProvider {

    private static final String MODULE_ID = "task";

    private final DatabaseService dbService;

    private final ConfigViewFactory viewFactory;

    public TaskQuotaProvider(DatabaseService dbService, ConfigViewFactory viewFactory) {
        super();
        this.dbService = dbService;
        this.viewFactory = viewFactory;
    }

    @Override
    public String getModuleID() {
        return MODULE_ID;
    }

    @Override
    public String getDisplayName() {
        return "Tasks";
    }

    Quota getAmountQuota(Session session) throws OXException {
        int contextID = session.getContextId();
        Connection connection = dbService.getReadOnly(contextID);
        try {
            long limit = AmountQuotas.getLimit(session, MODULE_ID, viewFactory, connection);
            if (limit <= Quota.UNLIMITED) {
                return Quota.UNLIMITED_AMOUNT;
            }

            int usage = TaskStorage.getInstance().countTasks(contextID, connection);
            return new Quota(QuotaType.AMOUNT, limit, usage);
        } catch (SQLException e) {
            throw TaskExceptionCode.SQL_ERROR.create(e);
        } finally {
            dbService.backReadOnly(contextID, connection);
        }
    }

    @Override
    public AccountQuota getFor(Session session, String accountID) throws OXException {
        if (!"0".equals(accountID)) {
            throw QuotaExceptionCodes.UNKNOWN_ACCOUNT.create(accountID, MODULE_ID);
        }

        return new DefaultAccountQuota(accountID, getDisplayName()).addQuota(getAmountQuota(session));
    }

    @Override
    public AccountQuotas getFor(Session session) throws OXException {
        return new AccountQuotas(getFor(session, "0"));
    }

}
