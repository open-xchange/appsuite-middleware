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

package com.openexchange.chronos.calendar.account.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarProvider;
import com.openexchange.chronos.provider.CalendarProviderRegistry;
import com.openexchange.chronos.provider.account.AdministrativeCalendarAccountService;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.storage.CalendarAccountStorage;
import com.openexchange.chronos.storage.CalendarAccountStorageFactory;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link CalendarAccountServiceImpl}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.0
 */
public class CalendarAccountServiceImpl implements CalendarAccountService, AdministrativeCalendarAccountService {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link CalendarAccountServiceImpl}.
     */
    public CalendarAccountServiceImpl(ServiceLookup serviceLookup) {
        super();
        this.services = serviceLookup;
    }

    @Override
    public List<CalendarProvider> getProviders() throws OXException {
        return getProviderRegistry().getCalendarProviders();
    }

    @Override
    public CalendarAccount createAccount(Session session, String providerId, JSONObject userConfig, CalendarParameters parameters) throws OXException {
        /*
         * get associated calendar provider & initialize account config
         */
        CalendarProvider calendarProvider = getProviderRegistry().getCalendarProvider(providerId);
        if (null == calendarProvider) {
            throw CalendarExceptionCodes.PROVIDER_NOT_AVAILABLE.create(providerId);
        }
        JSONObject internalConfig = calendarProvider.configureAccount(session, userConfig, parameters);
        /*
         * insert calendar account in storage
         */
        CalendarAccountStorage accountStorage = getAccountStorage(session);
        int accountId = accountStorage.createAccount(providerId, session.getUserId(), internalConfig, userConfig);
        /*
         * reload account & let provider perform any additional initialization
         */
        CalendarAccount calendarAccount = accountStorage.getAccount(session.getUserId(), accountId);
        calendarProvider.onAccountCreated(session, calendarAccount, parameters);
        return calendarAccount;
    }

    @Override
    public CalendarAccount updateAccount(Session session, int id, JSONObject userConfig, long timestamp, CalendarParameters parameters) throws OXException {
        /*
         * get stored calendar account
         */
        CalendarAccountStorage accountStorage = getAccountStorage(session);
        CalendarAccount storedAccount = verifyAccountAction(session, accountStorage.getAccount(session.getUserId(), id), timestamp, false);
        /*
         * get associated calendar provider & re-initialize account config
         */
        CalendarProvider calendarProvider = getProviderRegistry().getCalendarProvider(storedAccount.getProviderId());
        if (null == calendarProvider) {
            throw CalendarExceptionCodes.PROVIDER_NOT_AVAILABLE.create(storedAccount.getProviderId());
        }
        JSONObject internalConfig = calendarProvider.reconfigureAccount(session, storedAccount.getInternalConfiguration(), userConfig, parameters);
        /*
         * update calendar account in storage
         */
        accountStorage.updateAccount(session.getUserId(), id, internalConfig, userConfig, timestamp);
        /*
         * reload account & let provider perform any additional initialization
         */
        CalendarAccount calendarAccount = accountStorage.getAccount(session.getUserId(), id);
        calendarProvider.onAccountUpdated(session, calendarAccount, parameters);
        return calendarAccount;
    }

    @Override
    public void deleteAccount(Session session, int id, long timestamp, CalendarParameters parameters) throws OXException {
        /*
         * get stored calendar account & delete it
         */
        CalendarAccountStorage accountStorage = getAccountStorage(session);
        CalendarAccount storedAccount = verifyAccountAction(session, accountStorage.getAccount(session.getUserId(), id), timestamp, false);
        accountStorage.deleteAccount(session.getUserId(), id);
        /*
         * finally let provider perform any additional initialization
         */
        CalendarProvider calendarProvider = getProviderRegistry().getCalendarProvider(storedAccount.getProviderId());
        if (null == calendarProvider) {
            LoggerFactory.getLogger(CalendarAccountServiceImpl.class).warn("Provider '{}' not available, skipping additional cleanup tasks for deleted account {}.",
                storedAccount.getProviderId(), storedAccount, CalendarExceptionCodes.PROVIDER_NOT_AVAILABLE.create(storedAccount.getProviderId()));
        } else {
            calendarProvider.onAccountDeleted(session, storedAccount, parameters);
        }
    }

    @Override
    public CalendarAccount getAccount(Session session, int id) throws OXException {
        if (CalendarAccount.DEFAULT_ACCOUNT.getAccountId() == id) {
            return CalendarAccount.DEFAULT_ACCOUNT;
        }
        return verifyAccountAction(session, loadCalendarAccount(id, session), true);
    }

    @Override
    public List<CalendarAccount> getAccounts(Session session) throws OXException {
        List<CalendarAccount> accounts = new ArrayList<CalendarAccount>();
        accounts.addAll(getAccountStorage(session).getAccounts(session.getUserId()));
        accounts.add(CalendarAccount.DEFAULT_ACCOUNT);

        {
            //            createAccount(session, "birthdays", null);
        }

        return accounts;
    }

    @Override
    public List<CalendarAccount> getAccounts(Session session, String providerId) throws OXException {
        List<CalendarAccount> accounts = new ArrayList<CalendarAccount>();
        if (CalendarAccount.DEFAULT_ACCOUNT.getProviderId().equals(providerId)) {
            accounts.add(CalendarAccount.DEFAULT_ACCOUNT);
        } else {
            accounts.addAll(getAccountStorage(session).getAccounts(providerId, new int[] { session.getUserId() }));
        }
        return accounts;
    }

    @Override
    public List<CalendarAccount> getAccounts(int contextId, int[] userIds, String providerId) throws OXException {
        return getAccountStorage(contextId).getAccounts(providerId, userIds);
    }

    @Override
    public CalendarAccount updateAccount(int contextId, int userId, int id, JSONObject internalConfig, JSONObject userConfig, long timestamp) throws OXException {
        CalendarAccountStorage accountStorage = getAccountStorage(contextId);
        accountStorage.updateAccount(userId, id, internalConfig, userConfig, timestamp);
        return accountStorage.getAccount(userId, id);
    }

    private CalendarAccount verifyAccountAction(Session session, CalendarAccount account, boolean hasDefaultAccountRights) throws OXException {
        return verifyAccountAction(session, account, null, hasDefaultAccountRights);
    }

    private CalendarAccount verifyAccountAction(Session session, CalendarAccount account, Long timestamp, boolean hasDefaultAccountRights) throws OXException {
        if (null == account || session.getUserId() != account.getUserId()) {
            throw CalendarExceptionCodes.ACCOUNT_NOT_FOUND.create(Integer.valueOf(null != account ? account.getAccountId() : -1));
        } else if (CalendarAccount.DEFAULT_ACCOUNT.getAccountId() == account.getAccountId() && !hasDefaultAccountRights) {
            throw CalendarExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(account.getProviderId());
        } else if (null != account.getLastModified() && null != timestamp && account.getLastModified().getTime() > timestamp) {
            throw CalendarExceptionCodes.CONCURRENT_MODIFICATION.create(String.valueOf(account.getAccountId()), timestamp, account.getLastModified().getTime());
        } else {
            return account;
        }
    }

    private CalendarAccount loadCalendarAccount(int id, Session session) throws OXException {
        return getAccountStorage(session).getAccount(session.getUserId(), id);
    }

    private CalendarAccountStorage getAccountStorage(Session session) throws OXException {
        return getAccountStorage(session.getContextId());
    }

    private CalendarAccountStorage getAccountStorage(int contextId) throws OXException {
        CalendarAccountStorageFactory storageFactory = services.getOptionalService(CalendarAccountStorageFactory.class);
        if (null == storageFactory) {
            throw ServiceExceptionCode.absentService(CalendarAccountStorageFactory.class);
        }
        return storageFactory.create(getContext(contextId));
    }

    private CalendarProviderRegistry getProviderRegistry() throws OXException {
        CalendarProviderRegistry providerRegistry = services.getOptionalService(CalendarProviderRegistry.class);
        if (null == providerRegistry) {
            throw ServiceExceptionCode.absentService(CalendarProviderRegistry.class);
        }
        return providerRegistry;
    }

    private Context getContext(int contextId) throws OXException {
        return services.getService(ContextService.class).getContext(contextId);
    }

}
