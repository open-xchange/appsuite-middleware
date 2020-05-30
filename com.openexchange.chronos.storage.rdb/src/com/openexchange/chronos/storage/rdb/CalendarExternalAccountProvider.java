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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.chronos.storage.rdb;

import static org.slf4j.LoggerFactory.getLogger;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.annotation.NonNull;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultCalendarParameters;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.AutoProvisioningCalendarProvider;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarProvider;
import com.openexchange.chronos.provider.CalendarProviderRegistry;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.storage.CalendarAccountStorage;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.database.provider.DatabaseServiceDBProvider;
import com.openexchange.database.provider.SimpleDBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.external.account.DefaultExternalAccount;
import com.openexchange.external.account.ExternalAccount;
import com.openexchange.external.account.ExternalAccountModule;
import com.openexchange.external.account.ExternalAccountProvider;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.ServiceLookup;

/**
 * {@link CalendarExternalAccountProvider}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.4
 */
public class CalendarExternalAccountProvider implements ExternalAccountProvider {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link CalendarExternalAccountProvider}.
     *
     * @param services The {@link ServiceLookup}
     */
    public CalendarExternalAccountProvider(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public @NonNull ExternalAccountModule getModule() {
        return ExternalAccountModule.CALENDAR;
    }

    @Override
    public List<ExternalAccount> list(int contextId) throws OXException {
        return parseCalendarAccounts(contextId, initStorage(contextId).loadAccounts(getProviderIds(false)));
    }

    @Override
    public List<ExternalAccount> list(int contextId, int userId) throws OXException {
        return parseCalendarAccounts(contextId, initStorage(contextId).loadAccounts(userId, getProviderIds(false)));
    }

    @Override
    public List<ExternalAccount> list(int contextId, int userId, String providerId) throws OXException {
        return parseCalendarAccounts(contextId, initStorage(contextId).loadAccounts(userId, providerId));
    }

    @Override
    public List<ExternalAccount> list(int contextId, String providerId) throws OXException {
        return parseCalendarAccounts(contextId, initStorage(contextId).loadAccounts(providerId));
    }

    @Override
    public boolean delete(int id, int contextId, int userId) throws OXException {
        return delete(id, contextId, userId, null);
    }

    @Override
    public boolean delete(int id, int contextId, int userId, Connection connection) throws OXException {
        CalendarAccountStorage storage = initStorage(contextId, connection);
        /*
         * load existing account & check corresponding provider
         */
        CalendarAccount storedAccount = storage.loadAccount(userId, id);
        if (null == storedAccount) {
            return false;
        }
        CalendarProvider calendarProvider = services.getServiceSafe(CalendarProviderRegistry.class).getCalendarProvider(storedAccount.getProviderId());
        if (AutoProvisioningCalendarProvider.class.isInstance(calendarProvider)) {
            if (null != calendarProvider && AutoProvisioningCalendarProvider.class.isInstance(calendarProvider)) {
                getLogger(CalendarExternalAccountProvider.class).warn("Unable to delete auto-provisioned calendar account {} from provider '{}'.",
                    storedAccount, calendarProvider.getId(), CalendarExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(calendarProvider.getId()));
                return false;
            }
        }
        /*
         * delete account data from storage, then let provider perform any additional cleanup tasks
         */
        storage.deleteAccount(userId, id, CalendarUtils.DISTANT_FUTURE);
        if (null == calendarProvider) {
            getLogger(CalendarExternalAccountProvider.class).warn("Provider '{}' not available, skipping additional cleanup tasks for deleted account {}.",
                storedAccount.getProviderId(), storedAccount, CalendarExceptionCodes.PROVIDER_NOT_AVAILABLE.create(storedAccount.getProviderId()));
        } else {
            DefaultCalendarParameters parameters = new DefaultCalendarParameters();
            if (null != connection) {
                parameters.set(CalendarParameters.PARAMETER_CONNECTION(), connection);
            }
            Context context = services.getServiceSafe(ContextService.class).getContext(contextId);
            calendarProvider.onAccountDeleted(context, storedAccount, parameters);
        }
        return true;
    }

    //////////////////////////////////// HELPERS //////////////////////////////////

    /**
     * Parses the specified {@link CalendarAccount}s to {@link ExternalAccount}s
     *
     * @param contextId The context identifier
     * @param calendarAccounts The {@link CalendarAccount}s to parse
     * @return A {@link List} with the parsed {@link ExternalAccount}s
     */
    private List<ExternalAccount> parseCalendarAccounts(int contextId, List<CalendarAccount> calendarAccounts) {
        List<ExternalAccount> accounts = new LinkedList<>();
        for (CalendarAccount account : calendarAccounts) {
            accounts.add(new DefaultExternalAccount(account.getAccountId(), contextId, account.getUserId(), account.getProviderId(), getModule()));
        }
        return accounts;
    }

    private String[] getProviderIds(boolean includeAutoProvisioned) {
        CalendarProviderRegistry providerRegistry = services.getService(CalendarProviderRegistry.class);
        List<CalendarProvider> calendarProviders = providerRegistry.getCalendarProviders();
        List<String> providerIds = new ArrayList<String>(calendarProviders.size());
        for (CalendarProvider calendarProvider : calendarProviders) {
            if (includeAutoProvisioned || false == AutoProvisioningCalendarProvider.class.isInstance(calendarProvider)) {
                providerIds.add(calendarProvider.getId());
            }
        }
        return providerIds.toArray(new String[providerIds.size()]);
    }

    private CalendarAccountStorage initStorage(int contextId) throws OXException {
        return initStorage(contextId, null);
    }

    private CalendarAccountStorage initStorage(int contextId, Connection connection) throws OXException {
        Context context = services.getServiceSafe(ContextService.class).getContext(contextId);
        if (null == connection) {
            DBProvider dbProvider = new DatabaseServiceDBProvider(services.getServiceSafe(DatabaseService.class));
            return RdbCalendarAccountStorage.init(context, dbProvider, DBTransactionPolicy.NORMAL_TRANSACTIONS);
        }
        DBProvider dbProvider = new SimpleDBProvider(connection, connection);
        return RdbCalendarAccountStorage.init(context, dbProvider, DBTransactionPolicy.NO_TRANSACTIONS);
    }

}
