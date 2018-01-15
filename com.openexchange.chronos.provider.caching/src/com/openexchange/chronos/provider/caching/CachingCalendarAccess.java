/*
 *
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

package com.openexchange.chronos.provider.caching;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccess;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarFolder;
import com.openexchange.chronos.provider.account.AdministrativeCalendarAccountService;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.chronos.provider.caching.internal.CachingCalendarAccessConstants;
import com.openexchange.chronos.provider.caching.internal.Services;
import com.openexchange.chronos.provider.caching.internal.handler.CachingExecutor;
import com.openexchange.chronos.provider.caching.internal.handler.FolderProcessingType;
import com.openexchange.chronos.provider.caching.internal.handler.FolderUpdateState;
import com.openexchange.chronos.provider.caching.internal.handler.utils.HandlerHelper;
import com.openexchange.chronos.provider.caching.internal.response.ChangeExceptionsResponseGenerator;
import com.openexchange.chronos.provider.caching.internal.response.DedicatedEventsResponseGenerator;
import com.openexchange.chronos.provider.caching.internal.response.FolderEventsResponseGenerator;
import com.openexchange.chronos.provider.caching.internal.response.SingleEventResponseGenerator;
import com.openexchange.chronos.provider.extensions.WarningsAware;
import com.openexchange.chronos.provider.folder.FolderCalendarAccess;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.EventID;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * A caching {@link CalendarAccess} implementation that provides generic database caching for external events.<br>
 * <br>
 * Some abstract methods have to be implemented from the underlying implementation to retrieve data and some configurations (for instance the refresh interval to define after which period the external provider should be contacted)
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public abstract class CachingCalendarAccess implements FolderCalendarAccess, WarningsAware {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CachingCalendarAccess.class);

    private final ServerSession session;
    private final CalendarParameters parameters;
    private CalendarAccount account;
    private List<OXException> warnings = new ArrayList<>();

    private JSONObject originInternalConfiguration;
    private JSONObject originUserConfiguration;

    /**
     * Initializes a new {@link CachingCalendarAccess}.
     *
     * @param session The user session
     * @param account The user calendar account
     * @param parameters The calendar parameters (for the given request)
     * @throws OXException
     */
    protected CachingCalendarAccess(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        this.parameters = parameters;
        this.session = ServerSessionAdapter.valueOf(session);
        this.account = account;
        this.originInternalConfiguration = new JSONObject(this.getAccount().getInternalConfiguration());
        this.originUserConfiguration = new JSONObject(this.getAccount().getUserConfiguration());
    }

    /**
     * Defines the refresh interval in minutes that has to be expired to contact the external event provider for the up-to-date calendar. The interval can be defined on a per folder base.<br>
     * <br>
     * If the value is <=0 the default of one day will be used.
     *
     * @return The interval that defines the expire of the caching in {@link TimeUnit#MINUTES}
     * @throws OXException
     */
    protected abstract long getRefreshInterval(String folderId) throws OXException;

    /**
     * Defines how long should be wait for the next request to the external calendar provider in case an error occurred.
     *
     * @return The time in {@link TimeUnit#MINUTES} that should be wait for contacting the external calendar provider for updates.
     */
    public abstract long getRetryAfterErrorInterval();

    /**
     * Returns an {@link ExternalCalendarResult} containing all external {@link Event}s by querying the underlying calendar for the given folder id and additional information.<b>
     * <b>
     * Make sure not to consider client parameters (available via {@link CachingCalendarAccess#getParameters()}) while requesting events!
     *
     * @param folderId The identifier of the folder to get the events from
     * @return {@link ExternalCalendarResult}
     */
    public abstract ExternalCalendarResult getAllEvents(String folderId) throws OXException;

    /**
     * Allows the underlying calendar provider to handle {@link OXException}s that might occur while retrieving data from the external source.
     *
     * @param folderId The identifier of the folder the error occurred for
     * @param e The {@link OXException} occurred
     */
    public abstract void handleExceptions(String folderId, OXException e);

    @Override
    public final Event getEvent(String folderId, String eventId, RecurrenceId recurrenceId) throws OXException {
        Set<FolderUpdateState> executionList = generateExecutionList(folderId);

        new CachingExecutor(this, executionList).cache(this.warnings);
        saveConfig();

        return new SingleEventResponseGenerator(this, folderId, eventId, recurrenceId).generate();
    }

    @Override
    public final List<Event> getEvents(List<EventID> eventIDs) throws OXException {
        Map<String, List<EventID>> sortEventIDsPerFolderId = HandlerHelper.sortEventIDsPerFolderId(eventIDs);

        Set<FolderUpdateState> executionList = generateExecutionList(sortEventIDsPerFolderId.keySet().toArray(new String[sortEventIDsPerFolderId.size()]));
        new CachingExecutor(this, executionList).cache(this.warnings);
        saveConfig();

        return new DedicatedEventsResponseGenerator(this, eventIDs).generate();
    }

    @Override
    public final List<Event> getEventsInFolder(String folderId) throws OXException {
        Set<FolderUpdateState> executionList = generateExecutionList(folderId);

        new CachingExecutor(this, executionList).cache(this.warnings);
        saveConfig();

        return new FolderEventsResponseGenerator(this, folderId).generate();
    }

    @Override
    public final List<Event> getChangeExceptions(String folderId, String seriesId) throws OXException {
        Set<FolderUpdateState> executionList = generateExecutionList(folderId);

        new CachingExecutor(this, executionList).cache(this.warnings);
        saveConfig();

        return new ChangeExceptionsResponseGenerator(this, folderId, seriesId).generate();
    }

    private Set<FolderUpdateState> generateExecutionList(String... folderIds) throws OXException {
        List<CalendarFolder> externalFolders = new ArrayList<>(this.getVisibleFolders()); // retrieves external folders
        List<FolderUpdateState> persistedUpdateStates = getLatestUpdateStates(); // retrieves folder states of last update

        Set<FolderUpdateState> executionList = merge(persistedUpdateStates, externalFolders);
        cleanup(executionList, folderIds);
        return executionList;
    }

    /**
     * Cleans up the execution list to only have the really desired instructions left. This means each {@link FolderProcessingType#DELETE}, {@link FolderProcessingType#READ_DB}, {@link FolderProcessingType#INITIAL_INSERT} instruction and
     * {@link FolderProcessingType#UPDATE} for requested folders will be left over.
     *
     * @param executionList Instructions retrieved from merging current state and external folders
     * @param folderIds The folders that should be updated
     */
    protected void cleanup(Set<FolderUpdateState> executionList, String... folderIds) {
        for (Iterator<FolderUpdateState> iterator = executionList.iterator(); iterator.hasNext();) {
            FolderUpdateState folderUpdateState = iterator.next();
            if (!folderUpdateState.getType().equals(FolderProcessingType.UPDATE)) {
                continue;
            }
            if (!Arrays.asList(folderIds).contains(folderUpdateState.getFolderId())) {
                iterator.remove();
            }
        }
    }

    protected Set<FolderUpdateState> merge(List<FolderUpdateState> persistedUpdateStates, List<CalendarFolder> externalFolders) throws OXException {
        if (persistedUpdateStates == null) {
            persistedUpdateStates = Collections.emptyList();
        }
        if (externalFolders == null) {
            externalFolders = Collections.emptyList();
        }

        Set<FolderUpdateState> executionList = new HashSet<>();

        for (Iterator<CalendarFolder> iterator = externalFolders.iterator(); iterator.hasNext();) {
            CalendarFolder externalFolder = iterator.next();
            String folderId = externalFolder.getId();
            FolderUpdateState found = findExistingFolder(persistedUpdateStates, folderId);
            if (found == null) {
                if (acquireUpdateLock()) {
                    executionList.add(new FolderUpdateState(folderId, 0, 0, FolderProcessingType.INITIAL_INSERT));
                } else {
                    executionList.add(new FolderUpdateState(folderId, 0, 0, FolderProcessingType.READ_DB));
                }
                iterator.remove();
                continue;
            }
            executionList.add(found);
            persistedUpdateStates.remove(found);
        }

        if (!persistedUpdateStates.isEmpty()) {
            for (FolderUpdateState folderUpdateState : persistedUpdateStates) {
                executionList.add(new FolderUpdateState(folderUpdateState.getFolderId(), folderUpdateState.getLastUpdated(), folderUpdateState.getRefreshInterval(), FolderProcessingType.DELETE));
            }
        }

        return executionList;
    }

    protected static FolderUpdateState findExistingFolder(List<FolderUpdateState> updateStates, String folderId) {
        for (FolderUpdateState updateState : updateStates) {
            if (updateState.getFolderId().equals(folderId)) {
                return updateState;
            }
        }
        return null;
    }

    public ServerSession getSession() {
        return session;
    }

    public CalendarAccount getAccount() {
        return account;
    }

    /**
     * Returns the origin request parameters
     *
     * @return {@link CalendarParameters} containing the parameters
     */
    public CalendarParameters getParameters() {
        return parameters;
    }

    /**
     * Returns the last persisted update state for each currently known folder (under consideration of the provided refresh interval).
     *
     * @return {@link ProcessingType} that indicates the following steps of processing
     * @throws OXException
     */
    protected final List<FolderUpdateState> getLatestUpdateStates() throws OXException {
        JSONObject lastUpdates = getAccount().getInternalConfiguration().optJSONObject(CachingCalendarAccessConstants.CACHING);
        if (lastUpdates == null) {
            return Collections.emptyList();
        }
        List<FolderUpdateState> currentStates = new ArrayList<>();

        //TODO: json
        for (Entry<String, Object> folderUpdateState : lastUpdates.asMap().entrySet()) {
            String folderId = folderUpdateState.getKey();
            Map<String, Object> folderConfig = (Map<String, Object>) folderUpdateState.getValue();
            Number lastFolderUpdate = (Number) folderConfig.get(CachingCalendarAccessConstants.LAST_UPDATE);
            long refreshInterval = getCascadedRefreshInterval(folderId);

            if (lastFolderUpdate == null || lastFolderUpdate.longValue() < 0) {
                if (acquireUpdateLock()) {
                    currentStates.add(new FolderUpdateState(folderId, 0, refreshInterval, FolderProcessingType.INITIAL_INSERT));
                } else {
                    currentStates.add(new FolderUpdateState(folderId, lastFolderUpdate.longValue(), refreshInterval, FolderProcessingType.READ_DB));
                }
                continue;
            }
            long currentTimeMillis = System.currentTimeMillis();
            if (TimeUnit.MINUTES.toMillis(refreshInterval) < currentTimeMillis - lastFolderUpdate.longValue()) {
                if (acquireUpdateLock()) {
                    currentStates.add(new FolderUpdateState(folderId, lastFolderUpdate.longValue(), refreshInterval, FolderProcessingType.UPDATE));
                } else {
                    currentStates.add(new FolderUpdateState(folderId, lastFolderUpdate.longValue(), refreshInterval, FolderProcessingType.READ_DB));
                }
                continue;
            }
            currentStates.add(new FolderUpdateState(folderId, lastFolderUpdate.longValue(), refreshInterval, FolderProcessingType.READ_DB));
        }

        return currentStates;
    }

    protected long getCascadedRefreshInterval(String folderId) {
        try {
            long providerRefreshInterval = getRefreshInterval(folderId);
            if (providerRefreshInterval > 0) {
                return providerRefreshInterval;
            }
        } catch (OXException e) {
            LOG.warn("Unable to retrieve refresh interval from implementation. Will use one day as default.", e);
        }
        return TimeUnit.DAYS.toMinutes(1L);
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
            accountService.updateAccount(getSession().getContextId(), getSession().getUserId(), getAccount().getAccountId(), getAccount().getInternalConfiguration(), getAccount().getUserConfiguration(), getAccount().getLastModified().getTime());
        } catch (OXException e) {
            LOG.error("Unable to save configuration: {}", e.getMessage(), e);
        }
    }

    @Override
    public List<OXException> getWarnings() {
        return warnings;
    }

    /**
     * Updates the internal configuration data of a specific calendar account.
     *
     * @param internalConfiguration The internal configuration data.
     * @throws OXException if an error is occurred
     */
    protected void updateInternalConfigurationData(JSONObject internalConfiguration) throws OXException {
        updateConfigurationData(internalConfiguration, null);
    }

    /**
     * Updates the external configuration data of a specific calendar account.
     *
     * @param externalConfiguration The external configuration data.
     * @throws OXException if an error is occurred
     */
    protected void updateExteralConfigurationData(JSONObject externalConfiguration) throws OXException {
        updateConfigurationData(null, externalConfiguration);
    }

    /**
     * Updates the configuration data of a specific calendar account.
     *
     * @param internalConfig The provider-specific <i>internal</i> configuration data for the calendar account, or <code>null</code> to skip
     * @param userConfig The provider-specific <i>user</i> configuration data for the calendar account, or <code>null</code> to skip
     */
    protected void updateConfigurationData(JSONObject internalConfiguration, JSONObject userConfiguration) throws OXException {
        AdministrativeCalendarAccountService service = Services.getService(AdministrativeCalendarAccountService.class);
        account = service.updateAccount(getSession().getContextId(), getAccount().getUserId(), getAccount().getAccountId(), internalConfiguration, userConfiguration, getAccount().getLastModified().getTime());
    }

    /**
     * Locks the calendar account prior updating the cached calendar data by persisting an appropriate marker within the account's
     * internal configuration data. If successful, the update operation should proceed, if not, another update operation for this account
     * is already being executed.
     *
     * @return <code>true</code> if the account was locked for update successfully, <code>false</code>, otherwise
     */
    private boolean acquireUpdateLock() throws OXException {
        long now = System.currentTimeMillis();
        JSONObject internalConfig = account.getInternalConfiguration();
        if (null == internalConfig) {
            internalConfig = new JSONObject();
        }
        /*
         * check if an update is already in progress
         */
        long lockedUntil = internalConfig.optLong("lockedForUpdateUntil", 0L);
        if (lockedUntil > now) {
            LOG.debug("Account {} is already locked until {}, aborting lock acquisition.", I(account.getAccountId()), L(lockedUntil));
            return false;
        }
        /*
         * no running update detected, try entering exclusive update and persist lock for 10 minutes in account config
         */
        lockedUntil = now + TimeUnit.MINUTES.toMillis(10);
        internalConfig.putSafe("lockedForUpdateUntil", L(lockedUntil));
        AdministrativeCalendarAccountService accountService = Services.getService(AdministrativeCalendarAccountService.class);
        try {
            account = accountService.updateAccount(session.getContextId(), session.getUserId(), account.getAccountId(), internalConfig, null, account.getLastModified().getTime());
            LOG.debug("Successfully acquired and persisted lock for account {} until {}.", I(account.getAccountId()), L(lockedUntil));
            return true;
        } catch (OXException e) {
            if (CalendarExceptionCodes.CONCURRENT_MODIFICATION.equals(e)) {
                /*
                 * account updated in the meantime; refresh & don't update now
                 */
                LOG.debug("Concurrent modification while attempting to persist lock for account {}, aborting.", I(account.getAccountId()));
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

        // TODO
        // execution flow is a bit awkward, so that the lock currently cannot be released from the same location where it was previously
        // acquired (within a try/finally block)
        // so at the moment, we're effectively producing stale locks (that at least time out after 10 minutes)
        // this should be adjusted along with the pending changes for dedicated support of BasicCalendarAccess

        JSONObject internalConfig = account.getInternalConfiguration();
        if (null != internalConfig && null != internalConfig.remove("lockedForUpdateUntil")) {
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
                    LOG.debug("Concurrent modification while attempting to release lock for account {}, aborting.", I(account.getAccountId()));
                    account = Services.getService(CalendarAccountService.class).getAccount(session, account.getAccountId(), parameters);
                    return false;
                }
                throw e;
            }
        }
        return false;
    }

}
