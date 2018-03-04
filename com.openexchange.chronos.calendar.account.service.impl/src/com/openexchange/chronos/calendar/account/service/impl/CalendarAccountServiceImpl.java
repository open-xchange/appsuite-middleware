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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.osgi.Tools.requireService;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.AutoProvisioningCalendarProvider;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarProvider;
import com.openexchange.chronos.provider.CalendarProviderRegistry;
import com.openexchange.chronos.provider.CalendarProviders;
import com.openexchange.chronos.provider.DefaultCalendarAccount;
import com.openexchange.chronos.provider.account.AdministrativeCalendarAccountService;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.chronos.provider.basic.BasicCalendarProvider;
import com.openexchange.chronos.provider.basic.CalendarSettings;
import com.openexchange.chronos.provider.folder.FolderCalendarProvider;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.storage.CalendarAccountStorage;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.chronos.storage.operation.OSGiCalendarStorageOperation;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViews;
import com.openexchange.context.ContextService;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.database.provider.SimpleDBProvider;
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

    /** Simple comparator for calendar accounts to deliver accounts in a deterministic order */
    private static final Comparator<CalendarAccount> ACCOUNT_COMPARATOR = new Comparator<CalendarAccount>() {

        @Override
        public int compare(CalendarAccount account1, CalendarAccount account2) {
            if (null == account1) {
                return null == account2 ? 0 : 1;
            }
            if (null == account2) {
                return -1;
            }
            return Integer.compare(account1.getAccountId(), account2.getAccountId());
        }
    };

    private final ServiceLookup services;

    /**
     * Initializes a new {@link CalendarAccountServiceImpl}.
     *
     * @param serviceLookup A service lookup reference
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
    public CalendarSettings probeAccountSettings(Session session, String providerId, CalendarSettings settings, CalendarParameters parameters) throws OXException {
        /*
         * get associated calendar provider, check permissions & perform the probe based on the supplied settings
         */
        CalendarProvider calendarProvider = requireCapability(getProvider(providerId), session);
        if (isGuest(session) || false == BasicCalendarProvider.class.isInstance(calendarProvider)) {
            throw CalendarExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(providerId);
        }
        return ((BasicCalendarProvider) calendarProvider).probe(session, settings, parameters);
    }

    @Override
    public CalendarAccount createAccount(Session session, String providerId, CalendarSettings settings, CalendarParameters parameters) throws OXException {
        /*
         * get associated calendar provider, check permissions & initialize account config
         */
        CalendarProvider calendarProvider = requireCapability(getProvider(providerId), session);
        if (isGuest(session) || false == BasicCalendarProvider.class.isInstance(calendarProvider)) {
            throw CalendarExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(providerId);
        }
        JSONObject internalConfig = ((BasicCalendarProvider) calendarProvider).configureAccount(session, settings, parameters);
        /*
         * insert calendar account in storage within transaction
         */
        CalendarAccount account = insertAccount(session.getContextId(), calendarProvider, session.getUserId(), internalConfig, settings.getConfig(), parameters);
        /*
         * let provider perform any additional initialization
         */
        calendarProvider.onAccountCreated(session, account, parameters);
        return account;
    }

    @Override
    public CalendarAccount updateAccount(Session session, int id, CalendarSettings settings, long clientTimestamp, CalendarParameters parameters) throws OXException {
        /*
         * get & check stored calendar account
         */
        CalendarAccount storedAccount = getAccount(session, id, parameters);
        if (null != storedAccount.getLastModified() && storedAccount.getLastModified().getTime() > clientTimestamp) {
            throw CalendarExceptionCodes.CONCURRENT_MODIFICATION.create(String.valueOf(id), clientTimestamp, storedAccount.getLastModified().getTime());
        }
        /*
         * get associated calendar provider & initialize account config
         */
        CalendarProvider calendarProvider = requireCapability(getProvider(storedAccount.getProviderId()), session);
        if (isGuest(session) || false == BasicCalendarProvider.class.isInstance(calendarProvider)) {
            throw CalendarExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(storedAccount.getProviderId());
        }
        JSONObject internalConfig = ((BasicCalendarProvider) calendarProvider).reconfigureAccount(session, storedAccount, settings, parameters);
        /*
         * update calendar account in storage within transaction
         */
        CalendarAccount account = updateAccount(session.getContextId(), session.getUserId(), id, internalConfig, settings.getConfig(), clientTimestamp, parameters);
        /*
         * let provider perform any additional initialization
         */
        calendarProvider.onAccountUpdated(session, account, parameters);
        return account;
    }

    @Override
    public CalendarAccount updateAccount(Session session, int id, JSONObject userConfig, long clientTimestamp, CalendarParameters parameters) throws OXException {
        /*
         * get & check stored calendar account
         */
        CalendarAccount storedAccount = getAccount(session, id, parameters);
        if (null != storedAccount.getLastModified() && storedAccount.getLastModified().getTime() > clientTimestamp) {
            throw CalendarExceptionCodes.CONCURRENT_MODIFICATION.create(String.valueOf(id), clientTimestamp, storedAccount.getLastModified().getTime());
        }
        /*
         * get associated calendar provider & initialize account config
         */
        CalendarProvider calendarProvider = requireCapability(getProvider(storedAccount.getProviderId()), session);
        if (isGuest(session) || false == FolderCalendarProvider.class.isInstance(calendarProvider)) {
            throw CalendarExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(storedAccount.getProviderId());
        }
        JSONObject internalConfig = ((FolderCalendarProvider) calendarProvider).reconfigureAccount(session, storedAccount, userConfig, parameters);
        /*
         * update calendar account in storage within transaction
         */
        CalendarAccount account = updateAccount(session.getContextId(), session.getUserId(), id, internalConfig, userConfig, clientTimestamp, parameters);
        /*
         * let provider perform any additional initialization
         */
        calendarProvider.onAccountUpdated(session, account, parameters);
        return account;
    }

    @Override
    public void deleteAccount(Session session, int id, long clientTimestamp, CalendarParameters parameters) throws OXException {
        /*
         * get & check stored calendar account (directly from storage to circumvent access restrictions and still allow removal)
         */
        CalendarAccount storedAccount = initAccountStorage(session.getContextId(), parameters).loadAccount(session.getUserId(), id);
        if (null == storedAccount) {
            throw CalendarExceptionCodes.ACCOUNT_NOT_FOUND.create(id);
        }
        if (null != storedAccount.getLastModified() && storedAccount.getLastModified().getTime() > clientTimestamp) {
            throw CalendarExceptionCodes.CONCURRENT_MODIFICATION.create(String.valueOf(id), clientTimestamp, storedAccount.getLastModified().getTime());
        }
        CalendarProvider calendarProvider = getProviderRegistry().getCalendarProvider(storedAccount.getProviderId());
        if (null != calendarProvider && AutoProvisioningCalendarProvider.class.isInstance(calendarProvider)) {
            throw CalendarExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(calendarProvider.getId());
        }
        if (isGuest(session)) {
            throw CalendarExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(storedAccount.getProviderId());
        }
        /*
         * delete calendar account in storage within transaction
         */
        new OSGiCalendarStorageOperation<Void>(services, session.getContextId(), -1, parameters) {

            @Override
            protected Void call(CalendarStorage storage) throws OXException {
                CalendarAccount account = storage.getAccountStorage().loadAccount(session.getUserId(), id);
                if (null == account) {
                    throw CalendarExceptionCodes.ACCOUNT_NOT_FOUND.create(id);
                }
                if (null != account.getLastModified() && account.getLastModified().getTime() > clientTimestamp) {
                    throw CalendarExceptionCodes.CONCURRENT_MODIFICATION.create(String.valueOf(id), clientTimestamp, account.getLastModified().getTime());
                }
                storage.getAccountStorage().deleteAccount(session.getUserId(), id, clientTimestamp);
                return null;
            }
        }.executeUpdate();
        invalidateStorage(session.getContextId(), session.getUserId(), id);
        /*
         * finally let provider perform any additional initialization
         */
        if (null == calendarProvider) {
            LoggerFactory.getLogger(CalendarAccountServiceImpl.class).warn("Provider '{}' not available, skipping additional cleanup tasks for deleted account {}.",
                storedAccount.getProviderId(), storedAccount, CalendarExceptionCodes.PROVIDER_NOT_AVAILABLE.create(storedAccount.getProviderId()));
        } else {
            calendarProvider.onAccountDeleted(session, storedAccount, parameters);
        }
    }

    @Override
    public CalendarAccount getAccount(Session session, int id, CalendarParameters parameters) throws OXException {
        return getAccounts(session, new int[] { id }, parameters).get(0);
    }

    @Override
    public List<CalendarAccount> getAccounts(Session session, int[] ids, CalendarParameters parameters) throws OXException {
        CalendarAccount[] storedAccounts = initAccountStorage(session.getContextId(), parameters).loadAccounts(session.getUserId(), ids);
        for (int i = 0; i < storedAccounts.length; i++) {
            if (null == storedAccounts[i] && CalendarAccount.DEFAULT_ACCOUNT.getAccountId() == ids[i]) {
                if (isGuest(session)) {
                    /*
                     * return a virtual default calendar account for guest users
                     */
                    storedAccounts[i] = getVirtualDefaultAccount(session);
                } else {
                    /*
                     * get default account from list to implicitly trigger pending auto-provisioning tasks of the default account
                     */
                    storedAccounts[i] = find(getAccounts(session, parameters), CalendarAccount.DEFAULT_ACCOUNT.getProviderId());
                }
            }
            if (null == storedAccounts[i]) {
                throw CalendarExceptionCodes.ACCOUNT_NOT_FOUND.create(ids[i]);
            }
        }
        return Arrays.asList(storedAccounts);
    }

    @Override
    public List<CalendarAccount> getAccounts(Session session, CalendarParameters parameters) throws OXException {
        /*
         * get accounts from storage
         */
        List<CalendarAccount> accounts = getAccounts(session.getContextId(), session.getUserId());
        /*
         * check for pending provisioning tasks
         */
        if (false == getProvidersRequiringProvisioning(session, accounts).isEmpty() && false == isGuest(session)) {
            accounts = new OSGiCalendarStorageOperation<List<CalendarAccount>>(services, session.getContextId(), -1, parameters) {

                @Override
                protected List<CalendarAccount> call(CalendarStorage storage) throws OXException {
                    /*
                     * re-check account list for pending auto-provisioning within transaction & auto-provision as needed
                     */
                    List<CalendarAccount> accounts = storage.getAccountStorage().loadAccounts(session.getUserId());
                    for (AutoProvisioningCalendarProvider calendarProvider : getProvidersRequiringProvisioning(session, accounts)) {
                        int maxAccounts = getMaxAccounts(calendarProvider, session.getContextId(), session.getUserId());
                        JSONObject userConfig = new JSONObject();
                        JSONObject internalConfig = calendarProvider.autoConfigureAccount(session, userConfig, parameters);
                        CalendarAccount account = insertAccount(storage.getAccountStorage(), calendarProvider.getId(), session.getUserId(), internalConfig, userConfig, maxAccounts);
                        calendarProvider.onAccountCreated(session, account, parameters);
                        accounts.add(account);
                    }
                    return accounts;
                }
            }.executeUpdate();
            /*
             * (re-)invalidate caches outside of transaction
             */
            invalidateStorage(session.getContextId(), session.getUserId());
        }
        if (accounts.isEmpty() && isGuest(session)) {
            /*
             * include a virtual default calendar account for guest users
             */
            return Collections.singletonList(getVirtualDefaultAccount(session));
        }
        return sort(accounts);
    }

    @Override
    public List<CalendarAccount> getAccounts(Session session, String providerId, CalendarParameters parameters) throws OXException {
        return sort(findAll(getAccounts(session, parameters), providerId));
    }

    @Override
    public List<CalendarAccount> getAccounts(int contextId, int userId) throws OXException {
        return sort(initAccountStorage(contextId, null).loadAccounts(userId));
    }

    @Override
    public CalendarAccount getAccount(int contextId, int userId, int id) throws OXException {
        return initAccountStorage(contextId, null).loadAccount(userId, id);
    }

    @Override
    public List<CalendarAccount> getAccounts(int contextId, int[] userIds, String providerId) throws OXException {
        return sort(initAccountStorage(contextId, null).loadAccounts(userIds, providerId));
    }

    @Override
    public CalendarAccount getAccount(int contextId, int userId, String providerId) throws OXException {
        if (CalendarAccount.DEFAULT_ACCOUNT.getProviderId().equals(providerId)) {
            return initAccountStorage(contextId, null).loadAccount(userId, CalendarAccount.DEFAULT_ACCOUNT.getAccountId());
        }
        return initAccountStorage(contextId, null).loadAccount(userId, providerId);
    }
    @Override
    public List<CalendarAccount> getAccounts(int contextId, int userId, String providerId) throws OXException {
        return findAll(initAccountStorage(contextId, null).loadAccounts(userId), providerId);
    }

    @Override
    public CalendarAccount updateAccount(int contextId, int userId, int accountId, JSONObject internalConfig, JSONObject userConfig, long clientTimestamp) throws OXException {
        return updateAccount(contextId, userId, accountId, internalConfig, userConfig, clientTimestamp, null);
    }

    private CalendarAccount updateAccount(int contextId, int userId, int accountId, JSONObject internalConfig, JSONObject userConfig, long clientTimestamp, CalendarParameters parameters) throws OXException {
        CalendarAccount account = new OSGiCalendarStorageOperation<CalendarAccount>(services, contextId, -1, parameters) {

            @Override
            protected CalendarAccount call(CalendarStorage storage) throws OXException {
                CalendarAccount account = storage.getAccountStorage().loadAccount(userId, accountId);
                if (null == account) {
                    throw CalendarExceptionCodes.ACCOUNT_NOT_FOUND.create(accountId);
                }
                if (null != account.getLastModified() && account.getLastModified().getTime() > clientTimestamp) {
                    throw CalendarExceptionCodes.CONCURRENT_MODIFICATION.create(String.valueOf(accountId), clientTimestamp, account.getLastModified().getTime());
                }
                CalendarAccount accountUpdate = new DefaultCalendarAccount(account.getProviderId(), account.getAccountId(), account.getUserId(), internalConfig, userConfig, new Date());
                storage.getAccountStorage().updateAccount(accountUpdate, clientTimestamp);
                return storage.getAccountStorage().loadAccount(userId, accountId);
            }
        }.executeUpdate();
        invalidateStorage(contextId, userId, accountId);
        return account;
    }

    /**
     * Gets a list of auto-provisioning calendar providers where no calendar account is found in the supplied list of accounts, i.e. those
     * providers who where a provisioning task is required.
     *
     * @param session The current session
     * @param existingAccounts The accounts to check against the registered auto-provisioning calendar providers
     * @return The auto-provisioning calendar providers where no calendar account was found
     */
    private List<AutoProvisioningCalendarProvider> getProvidersRequiringProvisioning(Session session, List<CalendarAccount> existingAccounts) throws OXException {
        CalendarProviderRegistry providerRegistry = getProviderRegistry();
        List<AutoProvisioningCalendarProvider> unprovisionedProviders = new ArrayList<AutoProvisioningCalendarProvider>();
        for (AutoProvisioningCalendarProvider calendarProvider : providerRegistry.getAutoProvisioningCalendarProviders()) {
            if (null == find(existingAccounts, calendarProvider.getId()) && hasCapability(calendarProvider, session)) {
                unprovisionedProviders.add(calendarProvider);
            }
        }
        return unprovisionedProviders;
    }

    /**
     * Prepares and stores a new calendar account.
     *
     * @param contextId The context identifier
     * @param calendarProvider The calendar provider
     * @param userId The user identifier
     * @param internalConfig The account's internal / protected configuration data
     * @param userConfig The account's external / user configuration data
     * @return The new calendar account
     */
    private CalendarAccount insertAccount(int contextId, CalendarProvider calendarProvider, int userId, JSONObject internalConfig, JSONObject userConfig, CalendarParameters parameters) throws OXException {
        CalendarAccount account = new OSGiCalendarStorageOperation<CalendarAccount>(services, contextId, -1, parameters) {

            @Override
            protected CalendarAccount call(CalendarStorage storage) throws OXException {
                /*
                 * insert account after checking if maximum number of allowed accounts is reached for this provider
                 */
                int maxAccounts = getMaxAccounts(calendarProvider, contextId, userId);
                checkMaxAccountsNotReached(storage, calendarProvider, userId, maxAccounts);
                return insertAccount(storage.getAccountStorage(), calendarProvider.getId(), userId, internalConfig, userConfig, maxAccounts);
            }
        }.executeUpdate();
        /*
         * (re-)invalidate caches outside of transaction
         */
        invalidateStorage(contextId, userId);
        return account;
    }

    /**
     * Prepares and stores a new calendar account.
     *
     * @param storage The calendar storage
     * @param providerId The provider identifier
     * @param userId The user identifier
     * @param internalConfig The account's internal / protected configuration data
     * @param userConfig The account's external / user configuration data
     * @param maxAccounts The maximum number of accounts allowed for this provider and user
     * @return The new calendar account
     */
    private CalendarAccount insertAccount(CalendarAccountStorage storage, String providerId, int userId, JSONObject internalConfig, JSONObject userConfig, int maxAccounts) throws OXException {
        int accountId;
        if (CalendarAccount.DEFAULT_ACCOUNT.getProviderId().equals(providerId)) {
            accountId = CalendarAccount.DEFAULT_ACCOUNT.getAccountId();
        } else {
            accountId = storage.nextId();
        }
        storage.insertAccount(new DefaultCalendarAccount(providerId, accountId, userId, internalConfig, userConfig, new Date()), maxAccounts);
        return storage.loadAccount(userId, accountId);
    }

    private CalendarProvider getProvider(String providerId) throws OXException {
        CalendarProvider calendarProvider = getProviderRegistry().getCalendarProvider(providerId);
        if (null == calendarProvider) {
            throw CalendarExceptionCodes.PROVIDER_NOT_AVAILABLE.create(providerId);
        }
        return calendarProvider;
    }

    private CalendarProviderRegistry getProviderRegistry() throws OXException {
        CalendarProviderRegistry providerRegistry = services.getOptionalService(CalendarProviderRegistry.class);
        if (null == providerRegistry) {
            throw ServiceExceptionCode.absentService(CalendarProviderRegistry.class);
        }
        return providerRegistry;
    }

    /**
     * Initializes the calendar account storage for a specific context with default settings, i.e. no special transaction policy.
     *
     * @param contextId The context identifier
     * @param parameters The calendar parameters, or <code>null</code> if no available
     * @return The account storage
     */
    private CalendarAccountStorage initAccountStorage(int contextId, CalendarParameters parameters) throws OXException {
        CalendarStorageFactory storageFactory = requireService(CalendarStorageFactory.class, services);
        Context context = requireService(ContextService.class, services).getContext(contextId);
        Connection connection = null == parameters ? null : parameters.get(Connection.class.getName(), Connection.class);
        if (null != connection) {
            SimpleDBProvider dbProvider = new SimpleDBProvider(connection, connection);
            return storageFactory.create(context, -1, null, dbProvider, DBTransactionPolicy.NO_TRANSACTIONS).getAccountStorage();
        }
        return storageFactory.create(context, -1, null).getAccountStorage();
    }

    private static CalendarAccount find(Collection<CalendarAccount> accounts, String providerId) {
        return accounts.stream().filter(account -> providerId.equals(account.getProviderId())).findFirst().orElse(null);
    }

    private static List<CalendarAccount> findAll(Collection<CalendarAccount> accounts, String providerId) {
        return accounts.stream().filter(account -> providerId.equals(account.getProviderId())).collect(Collectors.toList());
    }

    private static boolean isGuest(Session session) {
        return Boolean.TRUE.equals(session.getParameter(Session.PARAM_GUEST));
    }

    private static CalendarAccount getVirtualDefaultAccount(Session session) {
        return new DefaultCalendarAccount(CalendarAccount.DEFAULT_ACCOUNT.getProviderId(), CalendarAccount.DEFAULT_ACCOUNT.getAccountId(), session.getUserId(), new JSONObject(), new JSONObject(), new Date());
    }

    private void invalidateStorage(int contextId, int userId) throws OXException {
        invalidateStorage(contextId, userId, -1);
    }

    private void invalidateStorage(int contextId, int userId, int accountId) throws OXException {
        initAccountStorage(contextId, null).invalidateAccount(userId, accountId);
    }

    private int getMaxAccounts(CalendarProvider provider, int contextId, int userId) throws OXException {
        int defaultValue = provider.getDefaultMaxAccounts();
        ConfigView view = requireService(ConfigViewFactory.class, services).getView(userId, contextId);
        return ConfigViews.getDefinedIntPropertyFrom(CalendarProviders.getMaxAccountsPropertyName(provider), defaultValue, view);
    }

    private void checkMaxAccountsNotReached(CalendarStorage storage, CalendarProvider provider, int userId, int maxAccounts) throws OXException {
        if (0 < maxAccounts) {
            int numAccounts = storage.getAccountStorage().loadAccounts(new int[] { userId }, provider.getId()).size();
            if (maxAccounts <= numAccounts) {
                throw CalendarExceptionCodes.MAX_ACCOUNTS_EXCEEDED.create(provider.getId(), I(maxAccounts), I(numAccounts));
            }
        }
    }

    private CalendarProvider requireCapability(CalendarProvider provider, Session session) throws OXException {
        if (false == hasCapability(provider, session)) {
            throw CalendarExceptionCodes.MISSING_CAPABILITY.create(CalendarProviders.getCapabilityName(provider));
        }
        return provider;
    }

    private boolean hasCapability(CalendarProvider provider, Session session) throws OXException {
        String capabilityName = CalendarProviders.getCapabilityName(provider);
        CapabilitySet capabilities = requireService(CapabilityService.class, services).getCapabilities(session);
        return capabilities.contains(capabilityName);
    }

    private static List<CalendarAccount> sort(List<CalendarAccount> accounts) throws OXException {
        if (null != accounts && 1 < accounts.size()) {
            accounts.sort(ACCOUNT_COMPARATOR);
        }
        return accounts;
    }

    @Override
    public void deleteAccounts(int contextId, int userId, List<CalendarAccount> accounts) throws OXException {
        CalendarAccountStorage accountStorage = initAccountStorage(contextId, null);
        for(CalendarAccount acc: accounts) {
            accountStorage.deleteAccount(userId, acc.getAccountId(), Long.MAX_VALUE);
        }
    }

}
