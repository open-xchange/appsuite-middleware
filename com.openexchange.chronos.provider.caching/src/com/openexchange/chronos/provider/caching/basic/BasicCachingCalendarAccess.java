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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos.provider.caching.basic;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.Check;
import com.openexchange.chronos.common.DataHandlers;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.account.AdministrativeCalendarAccountService;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.chronos.provider.basic.BasicCalendarAccess;
import com.openexchange.chronos.provider.basic.CalendarSettings;
import com.openexchange.chronos.provider.caching.DiffAwareExternalCalendarResult;
import com.openexchange.chronos.provider.caching.ExternalCalendarResult;
import com.openexchange.chronos.provider.caching.basic.handlers.SearchHandler;
import com.openexchange.chronos.provider.caching.basic.handlers.SyncHandler;
import com.openexchange.chronos.provider.caching.internal.CachingCalendarAccessConstants;
import com.openexchange.chronos.provider.caching.internal.Services;
import com.openexchange.chronos.provider.caching.internal.handler.CachingHandler;
import com.openexchange.chronos.provider.caching.internal.handler.ProcessingType;
import com.openexchange.chronos.provider.caching.internal.handler.impl.InitialWriteHandler;
import com.openexchange.chronos.provider.caching.internal.handler.impl.ReadOnlyHandler;
import com.openexchange.chronos.provider.caching.internal.handler.impl.UpdateHandler;
import com.openexchange.chronos.provider.caching.internal.handler.utils.EmptyUidUpdates;
import com.openexchange.chronos.provider.caching.internal.response.AccountResponseGenerator;
import com.openexchange.chronos.provider.caching.internal.response.ChangeExceptionsResponseGenerator;
import com.openexchange.chronos.provider.caching.internal.response.DedicatedEventsResponseGenerator;
import com.openexchange.chronos.provider.caching.internal.response.SingleEventResponseGenerator;
import com.openexchange.chronos.provider.extensions.BasicSearchAware;
import com.openexchange.chronos.provider.extensions.BasicSyncAware;
import com.openexchange.chronos.provider.extensions.CachedAware;
import com.openexchange.chronos.provider.extensions.WarningsAware;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.EventUpdates;
import com.openexchange.chronos.service.SearchFilter;
import com.openexchange.chronos.service.UpdatesResult;
import com.openexchange.conversion.ConversionResult;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataHandler;
import com.openexchange.conversion.SimpleData;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link BasicCachingCalendarAccess}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
public abstract class BasicCachingCalendarAccess implements BasicCalendarAccess, BasicSearchAware, BasicSyncAware, WarningsAware, CachedAware {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(BasicCachingCalendarAccess.class);

    protected CalendarParameters parameters;
    protected CalendarAccount account;
    protected Session session;
    protected ServiceLookup services;

    private final List<OXException> warnings = new ArrayList<>();
    private final JSONObject originInternalConfiguration;
    private final JSONObject originUserConfiguration;

    /**
     * Initializes a new {@link BirthdaysCalendarAccess}.
     *
     * @param services The {@link ServiceLookup} instance
     * @param session The session
     * @param account The underlying calendar account
     * @param parameters Additional calendar parameters
     */
    protected BasicCachingCalendarAccess(ServiceLookup services, Session session, CalendarAccount account, CalendarParameters parameters) {
        this.services = services;
        this.session = session;
        this.account = account;
        this.parameters = parameters;
        this.originInternalConfiguration = new JSONObject(account.getInternalConfiguration());
        this.originUserConfiguration = new JSONObject(account.getUserConfiguration());
    }

    public Session getSession() {
        return session;
    }

    public CalendarAccount getAccount() {
        return account;
    }

    public CalendarParameters getParameters() {
        return parameters;
    }

    /**
     * Tracks one or more warnings in the calendar access.
     *
     * @param warnings The warnings to add, or <code>null</code> to ignore
     */
    public void addWarnings(Collection<OXException> warnings) {
        if (null != warnings && 0 < warnings.size()) {
            warnings.addAll(warnings);
        }
    }

    /**
     * Defines the refresh interval in minutes that has to be expired to contact the external event provider for the up-to-date calendar.<br>
     * <br>
     * If the value is <=0 the default of one day will be used.
     *
     * @return The interval that defines the expire of the caching in {@link TimeUnit#MINUTES}
     */
    protected abstract long getRefreshInterval() throws OXException;

    /**
     * Defines how long should be wait for the next request to the external calendar provider in case an error occurred.
     *
     * @param e The {@link OXException} occurred
     * @return The time in {@link TimeUnit#SECONDS} that should be wait for contacting the external calendar provider for updates.
     * @see {@link BasicCachingCalendarConstants.MINIMUM_DEFAULT_RETRY_AFTER_ERROR_INTERVAL}
     */
    public abstract long getRetryAfterErrorInterval(OXException e);

    /**
     * Returns an {@link ExternalCalendarResult} containing all external {@link Event}s by querying the underlying calendar for the given account and additional information.<b>
     * <b>
     * Make sure not to consider client parameters (available via {@link CachingCalendarAccess#getParameters()}) while requesting events!
     *
     * @return {@link ExternalCalendarResult}
     */
    public abstract ExternalCalendarResult getAllEvents() throws OXException;

    @Override
    public final Event getEvent(String eventId, RecurrenceId recurrenceId) throws OXException {
        cache();
        containsError();
        return new SingleEventResponseGenerator(this, eventId, recurrenceId).generate();
    }

    @Override
    public final List<Event> getEvents(List<EventID> eventIDs) throws OXException {
        cache();
        containsError();
        return new DedicatedEventsResponseGenerator(this, eventIDs).generate();
    }

    @Override
    public List<Event> getEvents() throws OXException {
        cache();
        containsError();
        return new AccountResponseGenerator(this).generate();
    }

    @Override
    public final List<Event> getChangeExceptions(String seriesId) throws OXException {
        cache();
        containsError();
        return new ChangeExceptionsResponseGenerator(this, seriesId).generate();
    }

    /**
     * Throws an {@link OXException} when the account contains an error due to previous execution
     *
     * @throws OXException
     */
    private void containsError() throws OXException {
        OXException accountError = optAccountError();
        if (null != accountError) {
            throw accountError;
        }
    }

    /**
     * Optionally gets a persisted account error that occurred during previous cache update operations from the underlying configuration.
     * <p/>
     * If there is an account error, this error should be added when constructing the account's settings object for
     * {@link BasicCalendarAccess#getSettings()}.
     *
     * @return The account error, or <code>null</code> if there is none
     * @see {@link CalendarSettings#setError(OXException)}
     */
    protected OXException optAccountError() {
        if (null != account.getInternalConfiguration()) {
            JSONObject jsonObject = account.getInternalConfiguration().optJSONObject("lastError");
            if (null != jsonObject) {
                DataHandler dataHandler = Services.getService(ConversionService.class).getDataHandler(DataHandlers.JSON2OXEXCEPTION);
                try {
                    ConversionResult result = dataHandler.processData(new SimpleData<JSONObject>(jsonObject), new DataArguments(), null);
                    if (null != result && null != result.getData() && OXException.class.isInstance(result.getData())) {
                        return (OXException) result.getData();
                    }
                } catch (OXException e) {
                    LOG.error("Unable to process data.", e);
                }
            }
        }
        return null;
    }

    /**
     * Optionally gets the timestamp when the calendar data was last updated.
     *
     * @return The timestamp of the last update, or <code>null</code> if unknown
     */
    protected Long optLastUpdate() {
        JSONObject cachingConfig = account.getInternalConfiguration().optJSONObject(CachingCalendarAccessConstants.CACHING);
        if (null != cachingConfig) {
            long value = cachingConfig.optLong(CachingCalendarAccessConstants.LAST_UPDATE, 0L);
            if (0 < value) {
                return Long.valueOf(value);
            }
        }
        return null;
    }

    private void cache() throws OXException {
        ProcessingType type = getProcessingType();
        if (type != ProcessingType.READ_DB) {
            boolean holdsLock = acquireUpdateLock();
            try {
                if (holdsLock) {
                    executeUpdate(type);
                    saveConfig();
                }
            } finally {
                if (holdsLock) {
                    releaseUpdateLock();
                }
            }
        }
    }

    private void executeUpdate(ProcessingType type) throws OXException {
        CachingHandler cachingHandler = get(type);
        try {
            ExternalCalendarResult externalCalendarResult = cachingHandler.getExternalEvents();
            if (externalCalendarResult.isUpdated()) {
                List<Event> existingEvents = cachingHandler.getExistingEvents();
                EventUpdates diff = null;

                if (externalCalendarResult instanceof DiffAwareExternalCalendarResult) {
                    diff = ((DiffAwareExternalCalendarResult) externalCalendarResult).calculateDiff(existingEvents);
                } else {
                    List<Event> externalEvents = externalCalendarResult.getEvents();
                    cleanupEvents(externalEvents);

                    boolean containsUID = containsUid(externalEvents);
                    if (containsUID) {
                        diff = generateEventDiff(existingEvents, externalEvents);
                    } else {
                        //FIXME generate reproducible UID for upcoming refreshes
                        diff = new EmptyUidUpdates(existingEvents, externalEvents);
                    }
                }

                if (!diff.isEmpty()) {
                    cachingHandler.persist(diff);
                }
            }
            cachingHandler.updateLastUpdated(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1));
            success();
        } catch (OXException e) {
            LOG.info("Unable to update cache for account {}: {}", account.getAccountId(), e.getMessage(), e);
            warnings.add(e);

            handleInternally(cachingHandler, e);
            throw e;
        }
    }

    private void success() {
        account.getInternalConfiguration().remove("lastError");
    }

    /**
     * Saves the current configuration for the account if it has been changed while processing
     */
    protected void saveConfig() {
        if (Objects.equals(originInternalConfiguration, account.getInternalConfiguration()) && Objects.equals(originUserConfiguration, account.getUserConfiguration())) {
            return;
        }
        try {
            AdministrativeCalendarAccountService accountService = Services.getService(AdministrativeCalendarAccountService.class);
            account = accountService.updateAccount(getSession().getContextId(), getSession().getUserId(), account.getAccountId(), account.getInternalConfiguration(), account.getUserConfiguration(), account.getLastModified().getTime());
        } catch (OXException e) {
            LOG.error("Unable to save configuration: {}", e.getMessage(), e);
        }
    }

    public CachingHandler get(ProcessingType type) {
        switch (type) {
            case INITIAL_INSERT:
                return new InitialWriteHandler(this);
            case UPDATE:
                return new UpdateHandler(this);
            case READ_DB:
            default:
                return new ReadOnlyHandler(this);
        }
    }

    /**
     * Returns the last persisted update state for the account (under consideration of the provided refresh interval).
     *
     * @return {@link ProcessingType} that indicates the following steps of processing
     * @throws OXException
     */
    protected final ProcessingType getProcessingType() throws OXException {
        JSONObject caching = account.getInternalConfiguration().optJSONObject(CachingCalendarAccessConstants.CACHING);
        long refreshInterval = getCascadedRefreshInterval();
        if (caching == null) {
            return ProcessingType.INITIAL_INSERT;
        }

        Number lastUpdate = (Number) caching.opt(CachingCalendarAccessConstants.LAST_UPDATE);
        if (lastUpdate == null || lastUpdate.longValue() < 0) {
            return ProcessingType.INITIAL_INSERT;
        }
        long currentTimeMillis = System.currentTimeMillis();
        if (TimeUnit.MINUTES.toMillis(refreshInterval) < currentTimeMillis - lastUpdate.longValue()) {
            return ProcessingType.UPDATE;
        }
        if (currentTimeMillis < lastUpdate.longValue()) {
            return ProcessingType.READ_DB;
        }
        if (this.parameters.contains(CalendarParameters.PARAMETER_UPDATE_CACHE) && this.parameters.get(CalendarParameters.PARAMETER_UPDATE_CACHE, Boolean.class, Boolean.FALSE).booleanValue()) {
            return ProcessingType.UPDATE;
        }
        return ProcessingType.READ_DB;
    }

    protected long getCascadedRefreshInterval() {
        try {
            long providerRefreshInterval = getRefreshInterval();
            if (providerRefreshInterval >= 0) {
                return providerRefreshInterval;
            }
        } catch (OXException e) {
            LOG.warn("Unable to retrieve refresh interval from implementation. Will use one day as default.", e);
        }
        return TimeUnit.DAYS.toMinutes(1L);
    }

    /**
     * Locks the calendar account prior updating the cached calendar data by persisting an appropriate marker within the account's
     * internal configuration data. If successful, the update operation should proceed, if not, another update operation for this account
     * is already being executed.
     *
     * @return <code>true</code> if the account was locked for update successfully, <code>false</code>, otherwise
     */
    protected boolean acquireUpdateLock() throws OXException {
        long now = System.currentTimeMillis();

        JSONObject internalConfig = account.getInternalConfiguration();
        if (null == internalConfig) {
            internalConfig = new JSONObject();
        }
        JSONObject caching = internalConfig.optJSONObject(CachingCalendarAccessConstants.CACHING);
        if (null == caching) {
            caching = new JSONObject();
            internalConfig.putSafe(CachingCalendarAccessConstants.CACHING, caching);
        }
        /*
         * check if an update is already in progress
         */
        long lockedUntil = caching.optLong(CachingCalendarAccessConstants.LOCKED_FOR_UPDATE_UNTIL, 0L);
        String lockedBy = caching.optString(CachingCalendarAccessConstants.LOCKED_FOR_UPDATE_BY, null);
        if (lockedUntil > now) {
            LOG.debug("Account {} is already locked until {} by {}, aborting lock acquisition.", I(account.getAccountId()), L(lockedUntil), lockedBy);
            return false;
        }
        /*
         * no running update detected, try entering exclusive update and persist lock for 10 minutes in account config
         */
        lockedBy = Thread.currentThread().getName();
        lockedUntil = now + TimeUnit.MINUTES.toMillis(10);
        caching.putSafe(CachingCalendarAccessConstants.LOCKED_FOR_UPDATE_UNTIL, L(lockedUntil));
        caching.putSafe(CachingCalendarAccessConstants.LOCKED_FOR_UPDATE_BY, lockedBy);
        AdministrativeCalendarAccountService accountService = Services.getService(AdministrativeCalendarAccountService.class);
        try {
            accountService.updateAccount(session.getContextId(), session.getUserId(), account.getAccountId(), internalConfig, null, account.getLastModified().getTime());
            {
                int accountId = account.getAccountId();
                account = accountService.getAccount(session.getContextId(), session.getUserId(), accountId);
                if (null == account) {
                    throw CalendarExceptionCodes.ACCOUNT_NOT_FOUND.create(accountId);
                }
            }
            caching = account.getInternalConfiguration().optJSONObject(CachingCalendarAccessConstants.CACHING);
            if (null == caching) {
                return false;
            }
            String actualLockedBy = caching.optString(CachingCalendarAccessConstants.LOCKED_FOR_UPDATE_BY, null);
            long actualLockedUntil = caching.optLong(CachingCalendarAccessConstants.LOCKED_FOR_UPDATE_UNTIL, 0L);
            if (lockedBy.equals(actualLockedBy) && actualLockedUntil == lockedUntil) {
                LOG.debug("Successfully acquired and persisted lock for account {} until {} by {}.", I(account.getAccountId()), L(lockedUntil), lockedBy);
                return true;
            }
            LOG.debug("Account {} is already locked until {} by {}, aborting lock acquisition.", I(account.getAccountId()), L(actualLockedUntil), actualLockedBy);
            return false;
        } catch (OXException e) {
            if (CalendarExceptionCodes.CONCURRENT_MODIFICATION.equals(e)) {
                /*
                 * account updated in the meantime; keep old config to not have "lockedForUpdateUntil" set and reuse c
                 */
                String actualLockedBy = caching.optString(CachingCalendarAccessConstants.LOCKED_FOR_UPDATE_BY, null);
                LOG.debug("Concurrent modification while attempting to persist lock for account {}, aborting. Account is already locked until {} by {}", I(account.getAccountId()), L(lockedUntil), actualLockedBy, e);
                account = Services.getService(CalendarAccountService.class).getAccount(session, account.getAccountId(), parameters);
                return false;
            }
            throw e;
        }
    }

    /**
     * Releases a previously acquired lock for updating the account's cached calendar data.
     *
     * @return <code>true</code> if a lock was removed successfully, <code>false</code>, otherwise
     */
    private boolean releaseUpdateLock() throws OXException {
        JSONObject internalConfig = account.getInternalConfiguration();

        if (internalConfig != null) {
            JSONObject caching = internalConfig.optJSONObject(CachingCalendarAccessConstants.CACHING);
            if (caching != null && caching.remove(CachingCalendarAccessConstants.LOCKED_FOR_UPDATE_UNTIL) != null) {
                caching.remove(CachingCalendarAccessConstants.LOCKED_FOR_UPDATE_BY);
                /*
                 * update lock removed from config, update account config in storage
                 */
                AdministrativeCalendarAccountService accountService = Services.getService(AdministrativeCalendarAccountService.class);
                try {
                    account = accountService.updateAccount(session.getContextId(), session.getUserId(), account.getAccountId(), internalConfig, null, account.getLastModified().getTime());
                    LOG.debug("Successfully released lock for account {}.", I(account.getAccountId()));
                    return true;
                } catch (OXException e) {
                    if (CalendarExceptionCodes.CONCURRENT_MODIFICATION.equals(e)) {
                        /*
                         * account updated in the meantime; refresh & don't update now
                         */
                        LOG.debug("Concurrent modification while attempting to release lock for account {}, aborting.", I(account.getAccountId()), e);
                        account = Services.getService(CalendarAccountService.class).getAccount(session, account.getAccountId(), parameters);
                        return false;
                    }
                    throw e;
                }
            }
        }
        return false;
    }

    private void cleanupEvents(List<Event> externalEvents) {
        List<Event> addedItems = new ArrayList<Event>(externalEvents);
        for (Event event : addedItems) {
            try {
                Check.mandatoryFields(event, EventField.START_DATE, EventField.TIMESTAMP);
            } catch (OXException e) {
                LOG.debug("Removed event with uid {} from list to add because of the following corrupt data: {}", event.getUid(), e.getMessage());
                externalEvents.remove(event);
            }
        }
    }

    private void handleInternally(CachingHandler cachingHandler, OXException e) {
        OXException copy = new OXException(e);
        long retryAfterErrorInterval = getRetryAfterErrorInterval(copy);
        if (retryAfterErrorInterval < BasicCachingCalendarConstants.MINIMUM_DEFAULT_RETRY_AFTER_ERROR_INTERVAL) { // prevent wrong configuration that will allow ongoing external requests
            retryAfterErrorInterval = BasicCachingCalendarConstants.MINIMUM_DEFAULT_RETRY_AFTER_ERROR_INTERVAL;
        }
        long timeoutInMillis = TimeUnit.SECONDS.toMillis(retryAfterErrorInterval);
        long nextProcessingAfter = System.currentTimeMillis() + timeoutInMillis;
        cachingHandler.updateLastUpdated(nextProcessingAfter);

        rememberOXException(copy);
    }

    /**
     * @param exception - The {@link OXException} to remember
     */
    private void rememberOXException(OXException exception) {
        JSONObject internalConfig = account.getInternalConfiguration();

        DataHandler dataHandler = Services.getService(ConversionService.class).getDataHandler(DataHandlers.OXEXCEPTION2JSON);
        try {
            ConversionResult result = dataHandler.processData(new SimpleData<OXException>(exception), new DataArguments(), null);
            if (null != result && null != result.getData() && JSONObject.class.isInstance(result.getData())) {
                JSONObject errorJson = (JSONObject) result.getData();
                errorJson.remove("error_stack");
                internalConfig.putSafe("lastError", errorJson);
            }
        } catch (OXException e1) {
            LOG.error("Unable to process data.", e1);
        }
    }

    /**
     * Returns if all provided {@link Event}s do contain a UID
     *
     * @param events A list of {@link Event}s to check for the UID
     * @return <code>true</code> if all {@link Event}s do have a UID; <code>false</code> if at least one {@link Event} is missing the UID field
     */
    private boolean containsUid(List<Event> events) {
        for (Event event : events) {
            if (!event.containsUid()) {
                return false;
            }
        }
        return true;
    }

    private static final EventField[] FIELDS_TO_IGNORE = new EventField[] { EventField.CREATED_BY, EventField.FOLDER_ID, EventField.ID, EventField.CALENDAR_USER, EventField.CREATED, EventField.MODIFIED_BY, EventField.EXTENDED_PROPERTIES, EventField.TIMESTAMP };
    private static final EventField[] EQUALS_IDENTIFIER = new EventField[] { EventField.UID, EventField.RECURRENCE_ID };

    private EventUpdates generateEventDiff(List<Event> persistedEvents, List<Event> updatedEvents) throws OXException {
        return CalendarUtils.getEventUpdates(persistedEvents, updatedEvents, true, FIELDS_TO_IGNORE, EQUALS_IDENTIFIER);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.provider.extensions.BasicSearchAware#searchEvents(java.util.List, java.util.List)
     */
    @Override
    public List<Event> searchEvents(List<SearchFilter> filters, List<String> queries) throws OXException {
        if ((null == filters || filters.isEmpty()) && (null == queries || queries.isEmpty())) {
            return getEvents();
        }
        cache();
        return new SearchHandler(session, account, parameters).searchEvents(filters, queries);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.provider.extensions.BasicSyncAware#getUpdatedEvents(long)
     */
    @Override
    public UpdatesResult getUpdatedEvents(long updatedSince) throws OXException {
        cache();
        return new SyncHandler(session, account, parameters).getUpdatedEvents(updatedSince);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.provider.extensions.BasicSyncAware#getSequenceNumber()
     */
    @Override
    public long getSequenceNumber() throws OXException {
        cache();
        return new SyncHandler(session, account, parameters).getSequenceNumber();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.provider.extensions.BasicSyncAware#resolveResource(java.lang.String)
     */
    @Override
    public List<Event> resolveResource(String resourceName) throws OXException {
        cache();
        return new SyncHandler(session, account, parameters).resolveResource(resourceName);
    }
}
