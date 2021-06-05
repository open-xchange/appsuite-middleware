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

package com.openexchange.chronos.alarm;

import static com.openexchange.osgi.Tools.requireService;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.account.AdministrativeCalendarAccountService;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.database.Databases;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.database.provider.SimpleDBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.ServiceLookup;
import com.openexchange.user.User;
import com.openexchange.user.interceptor.AbstractUserServiceInterceptor;

/**
 * {@link AlarmTriggerServiceInterceptor}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class AlarmTriggerServiceInterceptor extends AbstractUserServiceInterceptor {

    /** A named logger instance */
    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AlarmTriggerServiceInterceptor.class);

    private final ServiceLookup services;

    /**
     * Initializes a new {@link AlarmTriggerServiceInterceptor}.
     *
     * @param services The {@link ServiceLookup}
     */
    public AlarmTriggerServiceInterceptor(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public void afterUpdate(Context context, User user, Contact contactData, Map<String, Object> properties) throws OXException {
        if (context == null || user == null) {
            // ignore
            return;
        }
        List<CalendarAccount> accounts;
        try {
            AdministrativeCalendarAccountService accountService = requireService(AdministrativeCalendarAccountService.class, services);
            accounts = accountService.getAccounts(context.getContextId(), user.getId());
        } catch (OXException e) {
            LoggerFactory.getLogger(AlarmTriggerServiceInterceptor.class).warn(
                "Unable to list calendar accounts for user {} in context {}, skipping re-calculation of floating alarm triggers.", 
                Integer.valueOf(user.getId()), Integer.valueOf(context.getContextId())
            );
            return;
        }
        if (null == accounts || accounts.isEmpty()) {
            return;
        }

        CalendarStorageFactory factory = requireService(CalendarStorageFactory.class, services);
        DBProvider dbProvider = requireService(DBProvider.class, services);

        Connection writeConnection = dbProvider.getWriteConnection(context);
        boolean committed = false;
        int updated = 0;
        try {
            writeConnection.setAutoCommit(false);
            SimpleDBProvider simpleDBProvider = new SimpleDBProvider(null, writeConnection);

            for (CalendarAccount acc : accounts) {
                CalendarStorage storage = factory.create(context, acc.getAccountId(), optEntityResolver(context.getContextId()), simpleDBProvider, DBTransactionPolicy.NO_TRANSACTIONS);
                storage.getAlarmTriggerStorage().recalculateFloatingAlarmTriggers(user.getId());
            }
            CalendarStorage storage = factory.create(context, 0, optEntityResolver(context.getContextId()), simpleDBProvider, DBTransactionPolicy.NO_TRANSACTIONS);
            storage.getAlarmTriggerStorage().recalculateFloatingAlarmTriggers(user.getId());
            writeConnection.commit();
            committed = true;
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            if (null != writeConnection) {
                if (false == committed) {
                    Databases.rollback(writeConnection);
                    Databases.autocommit(writeConnection);
                    dbProvider.releaseWriteConnectionAfterReading(context, writeConnection);
                } else {
                    Databases.autocommit(writeConnection);
                    if (updated != 0) {
                        dbProvider.releaseWriteConnection(context, writeConnection);
                    } else {
                        dbProvider.releaseWriteConnectionAfterReading(context, writeConnection);
                    }
                }
            }
        }
    }

    protected EntityResolver optEntityResolver(int contextId) throws OXException {
        CalendarUtilities calendarUtilities = services.getService(CalendarUtilities.class);
        return null != calendarUtilities ? calendarUtilities.getEntityResolver(contextId) : null;
    }

}
