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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.SerializationUtils;
import org.joda.time.Hours;
import org.joda.time.Weeks;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.provider.CalendarAccess;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarFolder;
import com.openexchange.chronos.provider.caching.internal.Services;
import com.openexchange.chronos.provider.caching.internal.handler.CachingExecutor;
import com.openexchange.chronos.provider.caching.internal.handler.FolderProcessingType;
import com.openexchange.chronos.provider.caching.internal.handler.FolderUpdateState;
import com.openexchange.chronos.provider.caching.internal.handler.utils.HandlerHelper;
import com.openexchange.chronos.provider.caching.internal.handler.utils.ResultCollector;
import com.openexchange.chronos.provider.extensions.WarningsAware;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.storage.CalendarAccountStorage;
import com.openexchange.chronos.storage.CalendarAccountStorageFactory;
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
public abstract class CachingCalendarAccess implements WarningsAware {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CachingCalendarAccess.class);

    /**
     * The general key for persisting the caching information
     */
    public static final String CACHING = "folderCaching";

    /**
     * The key for persisting the folders last update information
     */
    public static final String LAST_UPDATE = "lastUpdate";

    /**
     * The key for persisting the used refresh interval (if provided by the external folder). The interval should be persisted in minutes
     */
    public static final String REFRESH_INTERVAL = "refreshInterval";

    private final ServerSession session;
    private final CalendarAccount account;
    private final CalendarParameters parameters;
    private List<OXException> warnings = new ArrayList<>();

    private Map<String, Object> originConfiguration;

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
        this.originConfiguration = SerializationUtils.clone((HashMap<String, Object>) this.getAccount().getConfiguration());
    }

    /**
     * Defines the refresh interval in minutes that has to be expired to contact the external event provider for the up-to-date calendar.<br>
     * <br>
     * If the value is <=0 the default of one day will be used.
     * 
     * @return The interval that defines the expire of the caching in {@link TimeUnit#MINUTES}
     */
    protected abstract long getRefreshInterval();

    /**
     * Defines how long should be wait for the next request to the external calendar provider in case an error occurred.
     * 
     * @return The time in {@link TimeUnit#MINUTES} that should be wait for contacting the external calendar provider for updates.
     */
    public abstract long getExternalRequestTimeout();

    /**
     * Returns a list of {@link Event}s by querying the underlying calendar for the given folder id.
     * 
     * @param folderId The identifier of the folder to get the events from
     * @return The events
     */
    public abstract List<Event> getEvents(String folderId) throws OXException;

    /**
     * Allows the underlying calendar provider to handle {@link OXException}s that might occur while retrieving data from the external source.
     * 
     * @param folderId The identifier of the folder the error occurred for
     * @param e The {@link OXException} occurred
     */
    public abstract void handleExceptions(String calendarFolderId, OXException e);

    @Override
    public final Event getEvent(String folderId, String eventId, RecurrenceId recurrenceId) throws OXException {
        Set<FolderUpdateState> executionList = generateExecutionList(folderId);

        ResultCollector resultCollector = new CachingExecutor(this, executionList).cache(this.warnings);
        saveConfig();

        return resultCollector.get(folderId, eventId, recurrenceId);
    }

    @Override
    public final List<Event> getEvents(List<EventID> eventIDs) throws OXException {
        Map<String, List<EventID>> sortEventIDsPerFolderId = HandlerHelper.sortEventIDsPerFolderId(eventIDs);

        Set<FolderUpdateState> executionList = generateExecutionList(sortEventIDsPerFolderId.keySet().toArray(new String[sortEventIDsPerFolderId.size()]));
        ResultCollector resultCollector = new CachingExecutor(this, executionList).cache(this.warnings);
        saveConfig();

        return resultCollector.get(eventIDs);
    }

    @Override
    public final List<Event> getEventsInFolder(String folderId) throws OXException {
        Set<FolderUpdateState> executionList = generateExecutionList(folderId);

        ResultCollector resultCollector = new CachingExecutor(this, executionList).cache(this.warnings);
        saveConfig();

        return resultCollector.get(folderId);
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

    protected Set<FolderUpdateState> merge(List<FolderUpdateState> persistedUpdateStates, List<CalendarFolder> externalFolders) {
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
                executionList.add(new FolderUpdateState(folderId, 0, 0, FolderProcessingType.INITIAL_INSERT));
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
        Map<String, Map<String, Object>> lastUpdates = (Map<String, Map<String, Object>>) getAccount().getConfiguration().get(CACHING);

        if (lastUpdates == null) {
            return Collections.emptyList();
        }
        List<FolderUpdateState> currentStates = new ArrayList<>();

        for (Entry<String, Map<String, Object>> folderUpdateState : lastUpdates.entrySet()) {
            String folderId = folderUpdateState.getKey();
            Map<String, Object> folderConfig = folderUpdateState.getValue();
            Long lastFolderUpdate = (Long) folderConfig.get(LAST_UPDATE);
            long refreshInterval = getCascadedRefreshInterval(folderConfig);

            if (lastFolderUpdate == null || lastFolderUpdate.longValue() <= 0) {
                currentStates.add(new FolderUpdateState(folderId, 0, refreshInterval, FolderProcessingType.INITIAL_INSERT));
                continue;
            }
            long currentTimeMillis = System.currentTimeMillis();
            if (TimeUnit.MINUTES.toMillis(refreshInterval) < currentTimeMillis - lastFolderUpdate.longValue()) {
                currentStates.add(new FolderUpdateState(folderId, lastFolderUpdate.longValue(), refreshInterval, FolderProcessingType.UPDATE));
                continue;
            }
            currentStates.add(new FolderUpdateState(folderId, lastFolderUpdate.longValue(), refreshInterval, FolderProcessingType.READ_DB));
        }

        return currentStates;
    }

    protected long getCascadedRefreshInterval(Map<String, Object> folderConfig) {
        if (folderConfig != null && !folderConfig.isEmpty()) {
            Long calendarProviderInterval = (Long) folderConfig.get(REFRESH_INTERVAL);
            if (calendarProviderInterval != null) {
                return calendarProviderInterval.longValue();
            }
        }
        long providerRefreshInterval = getRefreshInterval();
        if (providerRefreshInterval > Hours.ONE.toStandardMinutes().getMinutes()) {
            return providerRefreshInterval;
        }
        return Weeks.ONE.toStandardMinutes().getMinutes();
    }

    /**
     * Saves the current configuration for the account if it has been changed while processing
     */
    protected void saveConfig() {
        if (this.originConfiguration.equals(this.getAccount().getConfiguration())) {
            return;
        }
        try {
            CalendarAccountStorage accountStorage = Services.getService(CalendarAccountStorageFactory.class).create(this.getSession().getContext());
            accountStorage.updateAccount(this.account.getAccountId(), this.getAccount().getConfiguration(), this.getAccount().getLastModified().getTime());
        } catch (OXException e) {
            LOG.error("Unable to save configuration: {}", e.getMessage(), e);
        }
    }

    @Override
    public List<OXException> getWarnings() {
        return warnings;
    }

    public Map<String, Object> getFolderConfiguration(String folderId) {
        Map<String, Object> configuration = getAccount().getConfiguration();
        Map<String, Map<String, Object>> caching = (Map<String, Map<String, Object>>) configuration.get(CACHING);
        if (caching == null) {
            caching = new HashMap<>();
            configuration.put(CACHING, caching);
        }
        Map<String, Object> folderConfig = caching.get(folderId);
        if (folderConfig == null) {
            folderConfig = new HashMap<>();
            caching.put(folderId, folderConfig);
        }
        return folderConfig;
    }
}
