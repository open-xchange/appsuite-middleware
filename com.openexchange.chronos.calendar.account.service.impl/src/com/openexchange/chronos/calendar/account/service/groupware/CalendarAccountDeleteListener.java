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

package com.openexchange.chronos.calendar.account.service.groupware;

import static com.openexchange.osgi.Tools.requireService;
import java.sql.Connection;
import java.util.List;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.calendar.account.service.impl.CalendarAccountServiceImpl;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultCalendarParameters;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarProvider;
import com.openexchange.chronos.provider.CalendarProviderRegistry;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.database.provider.SimpleDBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.server.ServiceLookup;

/**
 * {@link CalendarAccountDeleteListener}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarAccountDeleteListener implements DeleteListener {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link CalendarAccountDeleteListener}.
     *
     * @param services A service lookup reference
     */
    public CalendarAccountDeleteListener(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public void deletePerformed(DeleteEvent event, Connection readCon, Connection writeCon) throws OXException {
        /*
         * check if applicable
         */
        if (DeleteEvent.TYPE_USER != event.getType() || DeleteEvent.SUBTYPE_ANONYMOUS_GUEST == event.getSubType() || DeleteEvent.SUBTYPE_INVITED_GUEST == event.getSubType()) {
            return;
        }
        /*
         * initialize calendar storage & delete accounts
         */
        SimpleDBProvider dbProvider = new SimpleDBProvider(readCon, writeCon);
        DefaultCalendarParameters parameters = new DefaultCalendarParameters();
        parameters.set(CalendarParameters.PARAMETER_CONNECTION(), writeCon);
        CalendarStorage calendarStorage = requireService(CalendarStorageFactory.class, services).create(event.getContext(), -1, null, dbProvider, DBTransactionPolicy.NO_TRANSACTIONS);
        List<CalendarAccount> storedAccounts = calendarStorage.getAccountStorage().loadAccounts(event.getId());
        if (null == storedAccounts || storedAccounts.isEmpty()) {
            return;
        }
        CalendarProviderRegistry providerRegistry = requireService(CalendarProviderRegistry.class, services);
        for (CalendarAccount storedAccount : storedAccounts) {
            /*
             * delete account data from storage, then let provider perform any additional cleanup tasks
             */
            calendarStorage.getAccountStorage().deleteAccount(storedAccount.getUserId(), storedAccount.getAccountId(), CalendarUtils.DISTANT_FUTURE);
            CalendarProvider calendarProvider = providerRegistry.getCalendarProvider(storedAccount.getProviderId());
            if (null == calendarProvider) {
                LoggerFactory.getLogger(CalendarAccountServiceImpl.class).warn("Provider '{}' not available, skipping additional cleanup tasks for deleted account {}.",
                    storedAccount.getProviderId(), storedAccount, CalendarExceptionCodes.PROVIDER_NOT_AVAILABLE.create(storedAccount.getProviderId()));
            } else {
                calendarProvider.onAccountDeleted(event.getContext(), storedAccount, parameters);
            }
        }
    }

}
