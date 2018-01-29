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
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.account.AdministrativeCalendarAccountService;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.chronos.provider.basic.BasicCalendarAccess;
import com.openexchange.chronos.provider.caching.ExternalCalendarResult;
import com.openexchange.chronos.provider.caching.basic.handlers.SearchHandler;
import com.openexchange.chronos.provider.caching.basic.handlers.SyncHandler;
import com.openexchange.chronos.provider.caching.internal.CachingCalendarAccessConstants;
import com.openexchange.chronos.provider.caching.internal.Services;
import com.openexchange.chronos.provider.caching.internal.handler.AccountUpdateState;
import com.openexchange.chronos.provider.caching.internal.handler.CachingExecutor;
import com.openexchange.chronos.provider.caching.internal.handler.ProcessingType;
import com.openexchange.chronos.provider.caching.internal.response.AccountResponseGenerator;
import com.openexchange.chronos.provider.caching.internal.response.ChangeExceptionsResponseGenerator;
import com.openexchange.chronos.provider.caching.internal.response.DedicatedEventsResponseGenerator;
import com.openexchange.chronos.provider.caching.internal.response.SingleEventResponseGenerator;
import com.openexchange.chronos.provider.extensions.BasicSearchAware;
import com.openexchange.chronos.provider.extensions.BasicSyncAware;
import com.openexchange.chronos.provider.extensions.CachedAware;
import com.openexchange.chronos.provider.extensions.WarningsAware;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.SearchFilter;
import com.openexchange.chronos.service.UpdatesResult;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

/**
 * {@link BasicCachingCalendarAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
public abstract class BasicCachingCalendarAccess implements BasicCalendarAccess, BasicSearchAware, BasicSyncAware, WarningsAware, CachedAware {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(BasicCachingCalendarAccess.class);

    protected CalendarParameters parameters;
    protected CalendarAccount account;
    protected CalendarSession calendarSession;
    protected ServiceLookup services;

    private List<OXException> warnings = new ArrayList<>();
    private JSONObject originInternalConfiguration;
    private JSONObject originUserConfiguration;

    /**
     * Initializes a new {@link BirthdaysCalendarAccess}.
     *
     * @param services The {@link ServiceLookup} instance
     * @param calendarSession The session
     * @param account The underlying calendar account
     * @param parameters Additional calendar parameters
     */
    protected BasicCachingCalendarAccess(ServiceLookup services, CalendarSession calendarSession, CalendarAccount account, CalendarParameters parameters) {
        this.services = services;
        this.calendarSession = calendarSession;
        this.account = account;
        this.parameters = parameters;
        this.originInternalConfiguration = new JSONObject(account.getInternalConfiguration());
        this.originUserConfiguration = new JSONObject(account.getUserConfiguration());
    }

    public CalendarSession getCalendarSession() {
        return calendarSession;
    }

    public CalendarAccount getAccount() {
        return account;
    }

    public CalendarParameters getParameters() {
        return parameters;
    }

    /**
     * Allows the underlying calendar provider to handle {@link OXException}s that might occur while retrieving data from the external source.
     *
     * @param e The {@link OXException} occurred
     */
    public abstract void handleExceptions(OXException e);

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
     * @return The time in {@link TimeUnit#MINUTES} that should be wait for contacting the external calendar provider for updates.
     */
    public abstract long getRetryAfterErrorInterval();

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
        AccountUpdateState executionList = getLatestUpdateState();

        new CachingExecutor(this, executionList).cache(this.warnings);
        saveConfig();

        return new SingleEventResponseGenerator(this, eventId, recurrenceId).generate();
    }

    @Override
    public final List<Event> getEvents(List<EventID> eventIDs) throws OXException {
        AccountUpdateState executionList = getLatestUpdateState();
        new CachingExecutor(this, executionList).cache(this.warnings);
        saveConfig();

        return new DedicatedEventsResponseGenerator(this, eventIDs).generate();
    }

    @Override
    public List<Event> getEvents() throws OXException {
        AccountUpdateState executionList = getLatestUpdateState();

        new CachingExecutor(this, executionList).cache(this.warnings);
        saveConfig();

        return new AccountResponseGenerator(this).generate();
    }

    @Override
    public final List<Event> getChangeExceptions(String seriesId) throws OXException {
        AccountUpdateState executionList = getLatestUpdateState();

        new CachingExecutor(this, executionList).cache(this.warnings);
        saveConfig();

        return new ChangeExceptionsResponseGenerator(this, seriesId).generate();
    }

    /**
     * Saves the current configuration for the account if it has been changed while processing
     */
    protected void saveConfig() {
        if (Objects.equals(originInternalConfiguration, getAccount().getInternalConfiguration()) && Objects.equals(originUserConfiguration, getAccount().getUserConfiguration())) {
            return;
        }
        try {
            AdministrativeCalendarAccountService accountService = Services.getService(AdministrativeCalendarAccountService.class);
            accountService.updateAccount(getCalendarSession().getContextId(), getCalendarSession().getUserId(), getAccount().getAccountId(), getAccount().getInternalConfiguration(), getAccount().getUserConfiguration(), getAccount().getLastModified().getTime());
        } catch (OXException e) {
            LOG.error("Unable to save configuration: {}", e.getMessage(), e);
        }
    }

    /**
     * Returns the last persisted update state for the account (under consideration of the provided refresh interval).
     *
     * @return {@link ProcessingType} that indicates the following steps of processing
     * @throws OXException
     */
    protected final AccountUpdateState getLatestUpdateState() throws OXException {
        JSONObject caching = getAccount().getInternalConfiguration().optJSONObject(CachingCalendarAccessConstants.CACHING);
        long refreshInterval = getCascadedRefreshInterval();
        if (caching == null) {
            if (acquireUpdateLock()) {
                return new AccountUpdateState(0, refreshInterval, ProcessingType.INITIAL_INSERT);
            }
            return new AccountUpdateState(0, refreshInterval, ProcessingType.READ_DB);
        }

        Number lastUpdate = (Number) caching.opt(CachingCalendarAccessConstants.LAST_UPDATE);
        if (lastUpdate == null || lastUpdate.longValue() < 0) {
            if (acquireUpdateLock()) {
                return new AccountUpdateState(0, refreshInterval, ProcessingType.INITIAL_INSERT);
            }
            return new AccountUpdateState(0, refreshInterval, ProcessingType.READ_DB);
        }
        long currentTimeMillis = System.currentTimeMillis();
        if (TimeUnit.MINUTES.toMillis(refreshInterval) < currentTimeMillis - lastUpdate.longValue()) {
            if (acquireUpdateLock()) {
                return new AccountUpdateState(lastUpdate.longValue(), refreshInterval, ProcessingType.UPDATE);
            }
            return new AccountUpdateState(lastUpdate.longValue(), refreshInterval, ProcessingType.READ_DB);
        }
        return new AccountUpdateState(lastUpdate.longValue(), refreshInterval, ProcessingType.READ_DB);
    }

    protected long getCascadedRefreshInterval() {
        try {
            long providerRefreshInterval = getRefreshInterval();
            if (providerRefreshInterval > 0) {
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
        if (lockedUntil > now) {
            LOG.debug("Account {} is already locked until {}, aborting lock acquisition.", I(account.getAccountId()), L(lockedUntil));
            return false;
        }
        /*
         * no running update detected, try entering exclusive update and persist lock for 10 minutes in account config
         */
        lockedUntil = now + TimeUnit.MINUTES.toMillis(10);
        caching.putSafe(CachingCalendarAccessConstants.LOCKED_FOR_UPDATE_UNTIL, L(lockedUntil));
        AdministrativeCalendarAccountService accountService = Services.getService(AdministrativeCalendarAccountService.class);
        try {
            account = accountService.updateAccount(calendarSession.getContextId(), calendarSession.getUserId(), account.getAccountId(), internalConfig, null, account.getLastModified().getTime());
            LOG.debug("Successfully acquired and persisted lock for account {} until {}.", I(account.getAccountId()), L(lockedUntil));
            return true;
        } catch (OXException e) {
            if (CalendarExceptionCodes.CONCURRENT_MODIFICATION.equals(e)) {
                /*
                 * account updated in the meantime; refresh & don't update now
                 */
                LOG.debug("Concurrent modification while attempting to persist lock for account {}, aborting.", I(account.getAccountId()));
                account = Services.getService(CalendarAccountService.class).getAccount(calendarSession.getSession(), account.getAccountId(), parameters);
                return false;
            }
            throw e;
        }
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
        getEvents();
        return new SearchHandler(calendarSession, account, parameters).searchEvents(filters, queries);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.provider.extensions.BasicSyncAware#getUpdatedEvents(long)
     */
    @Override
    public UpdatesResult getUpdatedEvents(long updatedSince) throws OXException {
        getEvents();
        return new SyncHandler(calendarSession, account, parameters).getUpdatedEvents(updatedSince);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.provider.extensions.BasicSyncAware#getSequenceNumber()
     */
    @Override
    public long getSequenceNumber() throws OXException {
        getEvents();
        return new SyncHandler(calendarSession, account, parameters).getSequenceNumber();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.provider.extensions.BasicSyncAware#resolveResource(java.lang.String)
     */
    @Override
    public List<Event> resolveResource(String resourceName) throws OXException {
        getEvents();
        return new SyncHandler(calendarSession, account, parameters).resolveResource(resourceName);
    }
}
