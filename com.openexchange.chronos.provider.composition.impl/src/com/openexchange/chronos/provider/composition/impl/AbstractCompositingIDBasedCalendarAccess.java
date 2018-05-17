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

package com.openexchange.chronos.provider.composition.impl;

import static com.openexchange.chronos.provider.composition.impl.idmangling.IDMangling.getRelativeEventIdsPerAccountId;
import static com.openexchange.chronos.provider.composition.impl.idmangling.IDMangling.getRelativeFolderIdsPerAccountId;
import static com.openexchange.chronos.provider.composition.impl.idmangling.IDMangling.getUniqueFolderId;
import static com.openexchange.chronos.provider.composition.impl.idmangling.IDMangling.getUniqueId;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.I2i;
import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.osgi.Tools.requireService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.json.JSONObject;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.chronos.common.DefaultCalendarResult;
import com.openexchange.chronos.common.DefaultErrorAwareCalendarResult;
import com.openexchange.chronos.common.DefaultEventsResult;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.AutoProvisioningCalendarProvider;
import com.openexchange.chronos.provider.CalendarAccess;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarCapability;
import com.openexchange.chronos.provider.CalendarProvider;
import com.openexchange.chronos.provider.CalendarProviderRegistry;
import com.openexchange.chronos.provider.CalendarProviders;
import com.openexchange.chronos.provider.FreeBusyProvider;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.chronos.provider.composition.impl.idmangling.IDMangling;
import com.openexchange.chronos.provider.extensions.WarningsAware;
import com.openexchange.chronos.provider.groupware.GroupwareCalendarAccess;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.ErrorAwareCalendarResult;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.EventsResult;
import com.openexchange.exception.OXException;
import com.openexchange.java.CallerRunsCompletionService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.threadpool.ThreadPoolCompletionService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tx.TransactionAware;

/**
 * {@link AbstractCompositingIDBasedCalendarAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class AbstractCompositingIDBasedCalendarAccess implements TransactionAware, CalendarParameters {

    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractCompositingIDBasedCalendarAccess.class);

    protected final ServiceLookup services;
    protected final ServerSession session;
    protected final List<OXException> warnings;

    private final CalendarProviderRegistry providerRegistry;
    private final Map<String, Object> parameters;
    private final ConcurrentMap<Integer, CalendarAccess> connectedAccesses;

    /**
     * Initializes a new {@link AbstractCompositingIDBasedCalendarAccess}.
     *
     * @param session The session to create the ID-based access for
     * @param providerRegistry A reference to the calendar provider registry
     * @param services A service lookup reference
     */
    protected AbstractCompositingIDBasedCalendarAccess(Session session, CalendarProviderRegistry providerRegistry, ServiceLookup services) throws OXException {
        super();
        this.services = services;
        this.providerRegistry = providerRegistry;
        this.session = ServerSessionAdapter.valueOf(session);
        this.parameters = new HashMap<String, Object>();
        this.connectedAccesses = new ConcurrentHashMap<Integer, CalendarAccess>();
        this.warnings = new ArrayList<OXException>();
    }

    @Override
    public void startTransaction() throws OXException {
        ConcurrentMap<Integer, CalendarAccess> connectedAccesses = this.connectedAccesses;
        if (false == connectedAccesses.isEmpty()) {
            for (CalendarAccess access : connectedAccesses.values()) {
                LOG.warn("Access already connected: {}", access);
            }
        }
        connectedAccesses.clear();
        warnings.clear();
    }

    @Override
    public void finish() throws OXException {
        /*
         * close any connected calendar accesses
         */
        ConcurrentMap<Integer, CalendarAccess> connectedAccesses = this.connectedAccesses;
        for (Iterator<Entry<Integer, CalendarAccess>> iterator = connectedAccesses.entrySet().iterator(); iterator.hasNext();) {
            Entry<Integer, CalendarAccess> entry = iterator.next();
            CalendarAccess access = entry.getValue();
            LOG.debug("Closing calendar access {} for account {}.", access, entry.getKey());
            if (WarningsAware.class.isInstance(access)) {
                List<OXException> warnings = ((WarningsAware) access).getWarnings();
                if (null != warnings) {
                    this.warnings.addAll(warnings);
                }
            }
            access.close();
            iterator.remove();
        }
    }

    @Override
    public void commit() throws OXException {
        //
    }

    @Override
    public void rollback() throws OXException {
        //
    }

    @Override
    public void setTransactional(boolean transactional) {
        //
    }

    @Override
    public void setRequestTransactional(boolean transactional) {
        //
    }

    @Override
    public void setCommitsTransaction(boolean commits) {
        //
    }

    @Override
    public <T> CalendarParameters set(String parameter, T value) {
        parameters.put(parameter, value);
        return this;
    }

    @Override
    public <T> T get(String parameter, Class<T> clazz) {
        return get(parameter, clazz, null);
    }

    @Override
    public <T> T get(String parameter, Class<T> clazz, T defaultValue) {
        Object value = parameters.get(parameter);
        return null == value ? defaultValue : clazz.cast(value);
    }

    @Override
    public boolean contains(String parameter) {
        return parameters.containsKey(parameter);
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return Collections.unmodifiableSet(parameters.entrySet());
    }

    /**
     * Gets the calendar access implementing a specific extension for a specific account. The account is connected implicitly and
     * remembered to be closed during {@link #finish()} implicitly, if not already done.
     * <p/>
     * If the extension is not provided by the account's calendar access, an appropriate exception is thrown.
     *
     * @param accountId The identifier to get the calendar access for
     * @return The calendar access for the specified account
     */
    protected <T extends CalendarAccess> T getAccess(int accountId, Class<T> extensionClass) throws OXException {
        CalendarAccess access = getAccess(accountId);
        try {
            return extensionClass.cast(access);
        } catch (ClassCastException e) {
            throw CalendarExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(e, getProviderName(getAccount(accountId)));
        }
    }

    /**
     * Gets the groupware calendar access for a specific account. The account is connected implicitly and remembered to be closed during
     * {@link #finish()} implicitly, if not already done.
     *
     * @param accountId The identifier to get the calendar access for
     * @return The groupware calendar access for the specified account
     */
    protected GroupwareCalendarAccess getGroupwareAccess(int accountId) throws OXException {
        return getAccess(accountId, GroupwareCalendarAccess.class);
    }

    /**
     * Gets the calendar access for a specific account. The account is connected implicitly and remembered to be closed during
     * {@link #finish()} implicitly, if not already done.
     *
     * @param accountId The identifier to get the calendar access for
     * @return The calendar access for the specified account
     */
    protected CalendarAccess getAccess(int accountId) throws OXException {
        CalendarAccess access = connectedAccesses.get(I(accountId));
        return null != access ? access : getAccess(getAccount(accountId));
    }

    /**
     * Gets the calendar access implementing a specific extension for a specific account. The account is connected implicitly and
     * remembered to be closed during {@link #finish()} implicitly, if not already done.
     * <p/>
     * If the extension is not provided by the account's calendar access, an appropriate exception is thrown.
     *
     * @param account The account to get the calendar access for
     * @param extensionClass The targeted extension class
     * @return The calendar access for the specified account
     */
    protected <T extends CalendarAccess> T getAccess(CalendarAccount account, Class<T> extensionClass) throws OXException {
        CalendarAccess access = getAccess(account);
        try {
            return extensionClass.cast(access);
        } catch (ClassCastException e) {
            throw CalendarExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(e, getProviderName(account));
        }
    }

    /**
     * Gets a value indicating whether a specific calendar account is auto-provisioned, i.e. it was created automatically and the user
     * cannot delete it.
     *
     * @param account The account to check
     * @return <code>true</code> if the account is auto-provisioned, <code>false</code>, otherwise
     */
    protected boolean isAutoProvisioned(CalendarAccount account) {
        CalendarProvider provider = providerRegistry.getCalendarProvider(account.getProviderId());
        return null != provider && AutoProvisioningCalendarProvider.class.isInstance(provider);
    }

    /**
     * Gets the groupware calendar access for a specific account. The account is connected implicitly and remembered to be closed during
     * {@link #finish()} implicitly, if not already done.
     *
     * @param account The account to get the calendar access for
     * @return The groupware calendar access for the specified account
     */
    protected GroupwareCalendarAccess getGroupwareAccess(CalendarAccount account) throws OXException {
        return getAccess(account, GroupwareCalendarAccess.class);
    }

    /**
     * Gets the calendar access for a specific account. The account is connected implicitly and remembered to be closed during
     * {@link #finish()} implicitly, if not already done.
     *
     * @param account The account to get the calendar access for
     * @return The calendar access for the specified account
     */
    protected CalendarAccess getAccess(CalendarAccount account) throws OXException {
        ConcurrentMap<Integer, CalendarAccess> connectedAccesses = this.connectedAccesses;
        CalendarAccess access = connectedAccesses.get(I(account.getAccountId()));
        if (null == access) {
            CalendarProvider provider = providerRegistry.getCalendarProvider(account.getProviderId());
            if (null == provider) {
                throw CalendarExceptionCodes.PROVIDER_NOT_AVAILABLE.create(account.getProviderId());
            }
            access = provider.connect(session, account, this);
            CalendarAccess existingAccess = connectedAccesses.put(I(account.getAccountId()), access);
            if (null != existingAccess) {
                access.close();
                access = existingAccess;
            }
        }
        return access;
    }

    /**
     * Gets all <i>enabled</i> calendar accounts of the current session's user.
     *
     * @return The calendar accounts, or an empty list if there are none
     */
    protected List<CalendarAccount> getAccounts() throws OXException {
        return requireService(CalendarAccountService.class, services).getAccounts(session, this);
    }

    /**
     * Gets all <i>enabled</i> calendar accounts of the current session's user supporting a specific calendar capability..
     *
     * @param capability The targeted capability
     * @return The calendar accounts supporting the capability, or an empty list if there are none
     */
    protected List<CalendarAccount> getAccounts(CalendarCapability capability) throws OXException {
        List<CalendarAccount> accounts = new ArrayList<CalendarAccount>();
        for (CalendarAccount account : getAccounts()) {
            if (supports(account, capability)) {
                accounts.add(account);
            }
        }
        return accounts;
    }

    /**
     * Gets all <i>enabled</i> calendar accounts of the current session's user implementing a specific extension.
     *
     * @param extensionClass The targeted extension class
     * @return The calendar accounts supporting the extension, or an empty list if there are none
     */
    protected <T extends CalendarAccess> List<CalendarAccount> getAccounts(Class<T> extensionClass) throws OXException {
        List<CalendarAccount> accounts = new ArrayList<CalendarAccount>();
        for (CalendarAccount account : getAccounts()) {
            if (supports(account, extensionClass)) {
                accounts.add(account);
            }
        }
        return accounts;
    }

    /**
     * Gets the relative representation of a list of unique composite folder identifiers, mapped to their associated calendar account.
     * <p/>
     * {@link IDMangling#ROOT_FOLDER_IDS} are passed as-is implicitly, mapped to the default account.
     *
     * @param uniqueFolderIds The unique composite folder identifiers, e.g. <code>cal://4/35</code>
     * @return The relative folder identifiers, mapped to their associated calendar account
     * @throws OXException {@link CalendarExceptionCodes#UNSUPPORTED_FOLDER} if the account identifier can't be extracted from a passed composite identifier
     */
    protected Map<CalendarAccount, List<String>> getRelativeFolderIdsPerAccount(List<String> folderIds) throws OXException {
        if (null == folderIds) {
            return null;
        }
        Map<Integer, List<String>> folderIdsPerAccountId = getRelativeFolderIdsPerAccountId(folderIds);
        List<CalendarAccount> accounts = getAccounts(I2i(folderIdsPerAccountId.keySet()));
        Map<CalendarAccount, List<String>> folderIdsPerAccount = new HashMap<CalendarAccount, List<String>>(folderIdsPerAccountId.size());
        for (Entry<Integer, List<String>> entry : folderIdsPerAccountId.entrySet()) {
            CalendarAccount account = accounts.stream().filter(a -> i(entry.getKey()) == a.getAccountId()).findFirst().orElse(null);
            folderIdsPerAccount.put(account, entry.getValue());
        }
        return folderIdsPerAccount;
    }

    /**
     * Gets the relative representation of a list of unique composite folder identifiers, mapped to their associated calendar account.
     * <p/>
     * {@link IDMangling#ROOT_FOLDER_IDS} are passed as-is implicitly, mapped to the default account.
     *
     * @param folderIds The unique composite folder identifiers, e.g. <code>cal://4/35</code>
     * @param errorsPerFolderId A map to track possible errors that occurred while retrieving the accounts
     * @return The relative folder identifiers, mapped to their associated calendar account
     */
    protected Map<CalendarAccount, List<String>> getRelativeFolderIdsPerAccount(List<String> folderIds, Map<String, OXException> errorsPerFolderId) {
        Map<Integer, List<String>> folderIdsPerAccountId = getRelativeFolderIdsPerAccountId(folderIds, errorsPerFolderId);
        if (null == folderIdsPerAccountId || folderIdsPerAccountId.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<CalendarAccount, List<String>> foldersIdsPerAccount = new HashMap<CalendarAccount, List<String>>(folderIdsPerAccountId.size());
        try {
            /*
             * attempt to batch-load all referenced accounts
             */
            List<CalendarAccount> accounts = getAccounts(I2i(folderIdsPerAccountId.keySet()));
            for (Entry<Integer, List<String>> entry : folderIdsPerAccountId.entrySet()) {
                CalendarAccount account = accounts.stream().filter(a -> i(entry.getKey()) == a.getAccountId()).findFirst().orElse(null);
                foldersIdsPerAccount.put(account, entry.getValue());
            }
        } catch (OXException e) {
            /*
             * load each account separately as fallback & track errors
             */
            for (Entry<Integer, List<String>> entry : folderIdsPerAccountId.entrySet()) {
                try {
                    foldersIdsPerAccount.put(getAccount(i(entry.getKey())), entry.getValue());
                } catch (OXException x) {
                    for (String folderId : entry.getValue()) {
                        errorsPerFolderId.put(getUniqueFolderId(i(entry.getKey()), folderId), x);
                    }
                }
            }
        }
        return foldersIdsPerAccount;
    }

    /**
     * Gets the relative representation of a list of unique composite event identifiers, mapped to their associated calendar account.
     * <p/>
     * Event IDs whose folder denotes one of the {@link IDMangling#ROOT_FOLDER_IDS} are passed as-is implicitly, mapped to the default account.
     *
     * @param eventIds The event ids with unique composite folder identifiers, e.g. <code>cal://4/35</code>
     * @param errorsPerEventId A map to track possible errors that occurred while retrieving the accounts
     * @return Event identifiers with relative folder identifiers, mapped to their associated calendar account
     */
    protected Map<CalendarAccount, List<EventID>> getRelativeEventIdsPerAccount(List<EventID> eventIds, Map<EventID, OXException> errorsPerEventId) {
        Map<Integer, List<EventID>> eventIdsPerAccountId = getRelativeEventIdsPerAccountId(eventIds, errorsPerEventId);
        if (null == eventIdsPerAccountId || eventIdsPerAccountId.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<CalendarAccount, List<EventID>> eventIdsPerAccount = new HashMap<CalendarAccount, List<EventID>>(eventIdsPerAccountId.size());
        try {
            /*
             * attempt to batch-load all referenced accounts
             */
            List<CalendarAccount> accounts = getAccounts(I2i(eventIdsPerAccountId.keySet()));
            for (Entry<Integer, List<EventID>> entry : eventIdsPerAccountId.entrySet()) {
                CalendarAccount account = accounts.stream().filter(a -> i(entry.getKey()) == a.getAccountId()).findFirst().orElse(null);
                eventIdsPerAccount.put(account, entry.getValue());
            }
        } catch (OXException e) {
            /*
             * load each account separately as fallback & track errors
             */
            for (Entry<Integer, List<EventID>> entry : eventIdsPerAccountId.entrySet()) {
                try {
                    eventIdsPerAccount.put(getAccount(i(entry.getKey())), entry.getValue());
                } catch (OXException x) {
                    for (EventID eventId : entry.getValue()) {
                        errorsPerEventId.put(getUniqueId(i(entry.getKey()), eventId), x);
                    }
                }
            }
        }
        return eventIdsPerAccount;
    }

    /**
     * Builds a map of error events results for the supplied collection of exceptions, mapped to the corresponding folder identifier.
     *
     * @param errorsPerFolderId A map of errors associated with the corresponding folder identifier
     * @return A corresponding map of error event results, or an empty map if there were no errors
     */
    protected static Map<String, EventsResult> getErrorResults(Map<String, OXException> errorsPerFolderId) {
        if (null != errorsPerFolderId && 0 < errorsPerFolderId.size()) {
            Map<String, EventsResult> eventsResults = new HashMap<String, EventsResult>(errorsPerFolderId.size());
            for (Entry<String, OXException> entry : errorsPerFolderId.entrySet()) {
                eventsResults.put(entry.getKey(), new DefaultEventsResult(entry.getValue()));
            }
            return eventsResults;
        }
        return Collections.emptyMap();
    }

    /**
     * Builds a map of error results for the supplied collection of exceptions, mapped to the corresponding event identifier.
     *
     * @param errorsPerEventId A map of errors associated with the corresponding event identifier
     * @return A corresponding map of error results, or an empty map if there were no errors
     */
    protected Map<EventID, ErrorAwareCalendarResult> getErrorCalendarResults(Map<EventID, OXException> errorsPerEventId) {
        if (null != errorsPerEventId && 0 < errorsPerEventId.size()) {
            Map<EventID, ErrorAwareCalendarResult> results = new HashMap<EventID, ErrorAwareCalendarResult>(errorsPerEventId.size());
            for (Entry<EventID, OXException> entry : errorsPerEventId.entrySet()) {
                EventID eventID = entry.getKey();
                DefaultCalendarResult calendarResult = new DefaultCalendarResult(session, session.getUserId(), eventID.getFolderID(), null, null, null);
                results.put(eventID, new DefaultErrorAwareCalendarResult(calendarResult, Collections.emptyList(), entry.getValue()));
            }
            return results;
        }
        return Collections.emptyMap();
    }

    /**
     * Gets a specific calendar account.
     *
     * @param accountId The identifier of the account to get
     * @return The calendar account
     */
    protected CalendarAccount getAccount(int accountId) throws OXException {
        return getAccount(accountId, false);
    }

    /**
     * Gets a specific calendar account.
     *
     * @param accountId The identifier of the account to get
     * @param checkCapbilities <code>true</code> to check the current session user's capabilities for the underlying calendar provider,
     *            <code>false</code>, otherwise
     * @return The calendar account
     */
    protected CalendarAccount getAccount(int accountId, boolean checkCapbilities) throws OXException {
        CalendarAccount account = optAccount(accountId);
        if (null == account) {
            throw CalendarExceptionCodes.ACCOUNT_NOT_FOUND.create(I(accountId));
        }
        if (checkCapbilities) {
            requireCapability(account.getProviderId());
        }
        return account;
    }

    /**
     * Gets specific calendar accounts.
     *
     * @param accountIds The identifiers of the account to get
     * @return The calendar accounts
     */
    protected List<CalendarAccount> getAccounts(int[] accountIds) throws OXException {
        return requireService(CalendarAccountService.class, services).getAccounts(session, accountIds, this);
    }

    /**
     * Optionally gets a specific calendar account.
     *
     * @param accountId The identifier of the account to get
     * @return The calendar account, or <code>null</code> if not found
     */
    protected CalendarAccount optAccount(int accountId) throws OXException {
        return requireService(CalendarAccountService.class, services).getAccount(session, accountId, this);
    }

    /**
     * Checks that the current session's user has the required capability for a specific calendar provider.
     *
     * @param providerId The identifier of the calendar provider to check the capabilities for
     * @throws OXException {@link CalendarExceptionCodes#MISSING_CAPABILITY}
     */
    protected void requireCapability(String providerId) throws OXException {
        if (false == hasCapability(providerId)) {
            throw CalendarExceptionCodes.MISSING_CAPABILITY.create(CalendarProviders.getCapabilityName(providerId));
        }
    }

    /**
     * Gets a value indicating whether the current session's user has the required capability for a specific calendar provider or not.
     *
     * @param providerId The identifier of the calendar provider to check the capabilities for
     * @return <code>true</code> if the user has the required capability, <code>false</code>, otherwise
     */
    protected boolean hasCapability(String providerId) throws OXException {
        String capabilityName = CalendarProviders.getCapabilityName(providerId);
        CapabilitySet capabilities = requireService(CapabilityService.class, services).getCapabilities(session);
        return capabilities.contains(capabilityName);
    }

    /**
     * Gets the account's calendar provider's display name, localized in the current session user's language.
     *
     * @param account The account to get the provider name for
     * @return The provider name
     */
    protected String getProviderName(CalendarAccount account) {
        try {
            CalendarProvider provider = providerRegistry.getCalendarProvider(account.getProviderId());
            if (null != provider) {
                return provider.getDisplayName(ServerSessionAdapter.valueOf(session).getUser().getLocale());
            }
        } catch (Exception e) {
            LOG.debug("Error getting display name for calendar provider \"{}\": {}", account.getProviderId(), e.getMessage());
        }
        return account.getProviderId();
    }

    /**
     * Gets a (fallback) for the account's display name, in case the corresponding settings are not available.
     *
     * @param account The account to get the name for
     * @return The account name
     */
    protected String getAccountName(CalendarAccount account) {
        String fallbackName = "Account " + account.getAccountId();
        try {
            JSONObject internalConfig = account.getInternalConfiguration();
            if (null != internalConfig) {
                return internalConfig.optString("name", fallbackName);
            }
        } catch (Exception e) {
            LOG.debug("Error getting display name for calendar account \"{}\": {}", account.getProviderId(), e.getMessage());
        }
        return fallbackName;
    }

    /**
     * Gets all registered free/busy providers.
     *
     * @return A list of all registered free/busy providers
     */
    protected List<FreeBusyProvider> getFreeBusyProviders() {
        return providerRegistry.getFreeBusyProviders();
    }

    /**
     * Gets the completion service to manage asynchronous operations on multiple calendar accounts.
     *
     * @return The completion service
     */
    protected static <V> CompletionService<V> getCompletionService() {
        ThreadPoolService threadPool = ThreadPools.getThreadPool();
        if (null == threadPool) {
            return new CallerRunsCompletionService<V>();
        }
        return new ThreadPoolCompletionService<V>(threadPool);
    }

    protected <T extends CalendarAccess> boolean supports(CalendarAccount account, Class<T> extensionClass) {
        CalendarProvider provider = providerRegistry.getCalendarProvider(account.getProviderId());
        if (null == provider) {
            LOG.warn("Calendar provider \"{}\" for account {} not found; skipping.", account.getProviderId(), account.getAccountId());
            return false;
        }
        for (CalendarCapability capability : provider.getCapabilities()) {
            if (capability.getAccessInterface().isAssignableFrom(extensionClass)) {
                return true;
            }
        }
        return false;
    }

    protected <T extends CalendarAccess> boolean supports(CalendarAccount account, CalendarCapability capability) {
        CalendarProvider provider = providerRegistry.getCalendarProvider(account.getProviderId());
        if (null == provider) {
            LOG.warn("Calendar provider \"{}\" for account {} not found; skipping.", account.getProviderId(), account.getAccountId());
            return false;
        }
        return provider.getCapabilities().contains(capability);
    }

    @Override
    public String toString() {
        return new StringBuilder("IDBasedCalendarAccess ").append("[user=").append(session.getUserId()).append(", context=").append(session.getContextId()).append(", connectedAccesses=").append(connectedAccesses.keySet()).append(']').toString();
    }

}
