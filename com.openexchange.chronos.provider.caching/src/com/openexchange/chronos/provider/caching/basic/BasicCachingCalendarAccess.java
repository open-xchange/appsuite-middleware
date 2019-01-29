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

package com.openexchange.chronos.provider.caching.basic;

import static com.openexchange.chronos.common.CalendarUtils.getEventsByUID;
import static com.openexchange.chronos.common.CalendarUtils.getRecurrenceIds;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.common.CalendarUtils.sortSeriesMasterFirst;
import static com.openexchange.chronos.provider.CalendarFolderProperty.COLOR;
import static com.openexchange.chronos.provider.CalendarFolderProperty.DESCRIPTION;
import static com.openexchange.chronos.provider.CalendarFolderProperty.LAST_UPDATE;
import static com.openexchange.chronos.provider.CalendarFolderProperty.SCHEDULE_TRANSP;
import static com.openexchange.chronos.provider.CalendarFolderProperty.USED_FOR_SYNC;
import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONObject;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmField;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.ResourceId;
import com.openexchange.chronos.TimeTransparency;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.Check;
import com.openexchange.chronos.common.DataHandlers;
import com.openexchange.chronos.common.DefaultCalendarParameters;
import com.openexchange.chronos.common.DeltaEvent;
import com.openexchange.chronos.common.mapping.AttendeeMapper;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.account.AdministrativeCalendarAccountService;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.chronos.provider.basic.BasicCalendarAccess;
import com.openexchange.chronos.provider.basic.CalendarSettings;
import com.openexchange.chronos.provider.basic.CommonCalendarConfigurationFields;
import com.openexchange.chronos.provider.caching.CachingCalendarUtils;
import com.openexchange.chronos.provider.caching.DiffAwareExternalCalendarResult;
import com.openexchange.chronos.provider.caching.ExternalCalendarResult;
import com.openexchange.chronos.provider.caching.basic.exception.BasicCachingCalendarExceptionCodes;
import com.openexchange.chronos.provider.caching.basic.handlers.SearchHandler;
import com.openexchange.chronos.provider.caching.basic.handlers.SyncHandler;
import com.openexchange.chronos.provider.caching.internal.CachingCalendarAccessConstants;
import com.openexchange.chronos.provider.caching.internal.Services;
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
import com.openexchange.chronos.service.CollectionUpdate;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.EventUpdate;
import com.openexchange.chronos.service.EventUpdates;
import com.openexchange.chronos.service.EventsResult;
import com.openexchange.chronos.service.ItemUpdate;
import com.openexchange.chronos.service.SearchFilter;
import com.openexchange.chronos.service.UpdatesResult;
import com.openexchange.chronos.storage.AlarmStorage;
import com.openexchange.chronos.storage.AttendeeStorage;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.operation.OSGiCalendarStorageOperation;
import com.openexchange.conversion.ConversionResult;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataHandler;
import com.openexchange.conversion.SimpleData;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ColumnFieldOperand;
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

    /**
     * The default calendar name if none supplied by the user
     */
    private static final String DEFAULT_CALENDAR_NAME = "calendar";

    protected CalendarParameters parameters;
    protected CalendarAccount account;
    protected Session session;

    private final List<OXException> warnings = new ArrayList<>();

    /**
     * Initializes a new {@link BirthdaysCalendarAccess}.
     *
     * @param session The session
     * @param account The underlying calendar account
     * @param parameters Additional calendar parameters
     */
    protected BasicCachingCalendarAccess(Session session, CalendarAccount account, CalendarParameters parameters) {
        this.session = session;
        this.account = account;
        this.parameters = parameters;
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
        updateCacheIfNeeded();
        containsError();
        return new SingleEventResponseGenerator(this, eventId, recurrenceId).generate();
    }

    @Override
    public final List<Event> getEvents(List<EventID> eventIDs) throws OXException {
        updateCacheIfNeeded();
        containsError();
        return new DedicatedEventsResponseGenerator(this, eventIDs).generate();
    }

    @Override
    public List<Event> getEvents() throws OXException {
        updateCacheIfNeeded();
        containsError();
        return new AccountResponseGenerator(this).generate();
    }

    @Override
    public final List<Event> getChangeExceptions(String seriesId) throws OXException {
        updateCacheIfNeeded();
        containsError();
        return new ChangeExceptionsResponseGenerator(this, seriesId).generate();
    }

    @Override
    public CalendarSettings getSettings() {
        /*
         * generate extended properties from account configuration
         */
        JSONObject internalConfig = account.getInternalConfiguration();
        ExtendedProperties extendedProperties = new ExtendedProperties();
        extendedProperties.add(SCHEDULE_TRANSP(TimeTransparency.TRANSPARENT, true));
        extendedProperties.add(DESCRIPTION(internalConfig.optString(CommonCalendarConfigurationFields.DESCRIPTION, null)));
        if (CachingCalendarUtils.canBeUsedForSync(account.getProviderId(), session)) {
            extendedProperties.add(USED_FOR_SYNC(B(internalConfig.optBoolean(CommonCalendarConfigurationFields.USED_FOR_SYNC, false)), false));
        } else {
            extendedProperties.add(USED_FOR_SYNC(Boolean.FALSE, true));
        }
        extendedProperties.add(COLOR(internalConfig.optString(CommonCalendarConfigurationFields.COLOR, null), false));
        extendedProperties.add(LAST_UPDATE(optLastUpdate()));
        /*
         * build calendar settings
         */
        CalendarSettings settings = new CalendarSettings();
        settings.setSubscribed(true);
        settings.setName(internalConfig.optString(CommonCalendarConfigurationFields.NAME, DEFAULT_CALENDAR_NAME));
        settings.setLastModified(account.getLastModified());
        settings.setConfig(account.getUserConfiguration());
        settings.setExtendedProperties(extendedProperties);
        settings.setError(optAccountError());
        return settings;
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

    protected void updateCacheIfNeeded() throws OXException {
        JSONObject internalConfiguration = account.getInternalConfiguration();
        if (internalConfiguration == null || internalConfiguration.optJSONObject(CachingCalendarAccessConstants.CACHING) == null) {
            update();
            return;
        }
        final JSONObject caching = internalConfiguration.optJSONObject(CachingCalendarAccessConstants.CACHING);
        Number lastUpdate = caching.optNumber(CachingCalendarAccessConstants.LAST_UPDATE);
        long currentTimeMillis = System.currentTimeMillis();
        long cascadedRefreshInterval = getCascadedRefreshInterval();
        if (lastUpdate == null || lastUpdate.longValue() <= 0 || (TimeUnit.MINUTES.toMillis(cascadedRefreshInterval) < currentTimeMillis - lastUpdate.longValue() + TimeUnit.MINUTES.toMillis(1)) || this.parameters.contains(CalendarParameters.PARAMETER_UPDATE_CACHE) && this.parameters.get(CalendarParameters.PARAMETER_UPDATE_CACHE, Boolean.class, Boolean.FALSE).booleanValue()) {
            if (lastUpdate != null && lastUpdate.longValue() > 0 && lastUpdate.longValue() + TimeUnit.MINUTES.toMillis(1) > currentTimeMillis) {
                throw BasicCachingCalendarExceptionCodes.ALREADY_UP_TO_DATE.create(I(account.getAccountId()), I(session.getUserId()), I(session.getContextId()));
            }
            LOG.debug("Try to update cache for account {} with refresh interval {} (used server time: {}, last update: {}) and current cache configuration '{}'", I(account.getAccountId()), L(cascadedRefreshInterval), L(currentTimeMillis), lastUpdate != null ? L(lastUpdate.longValue()) : "never", caching.toString());
            update();
        }
    }

    protected void update() throws OXException {
        boolean holdsLock = acquireUpdateLock();
        try {
            if (holdsLock) {
                executeUpdate();
            }
        } finally {
            if (holdsLock) {
                releaseUpdateLock();
                saveConfig();
            }
        }
    }

    private void executeUpdate() throws OXException {
        try {
            ExternalCalendarResult externalCalendarResult = this.getAllEvents();
            if (externalCalendarResult.isUpdated()) {
                CalendarParameters lParameters = new DefaultCalendarParameters(getParameters()).set(CalendarParameters.PARAMETER_IGNORE_STORAGE_WARNINGS, Boolean.TRUE);
                new OSGiCalendarStorageOperation<Void>(Services.getServiceLookup(), session.getContextId(), account.getAccountId(), lParameters) {

                    @Override
                    protected Void call(CalendarStorage storage) throws OXException {
                        updateCache(storage, externalCalendarResult);
                        addWarnings(collectWarnings(storage));
                        return null;
                    }
                }.executeUpdate();
            }
            this.updateLastUpdated(System.currentTimeMillis());
            account.getInternalConfiguration().remove("lastError");

            LOG.debug("Updated cache for calendar account {} of user {} in context {}.", account.getAccountId(), session.getUserId(), session.getContextId());
        } catch (OXException e) {
            LOG.info("Unable to update cache for account {}: {}", account.getAccountId(), e.getMessage(), e);
            warnings.add(e);

            handleInternally(e);
            throw e;
        }
    }

    /**
     * Tracks one or more warnings in the calendar access.
     *
     * @param warnings The warnings to add, or <code>null</code> to ignore
     */
    protected void addWarnings(Collection<OXException> warnings) {
        if (null != warnings && 0 < warnings.size()) {
            warnings.addAll(warnings);
        }
    }

    private void updateLastUpdated(long timestamp) {
        JSONObject cachingConfig = getCachingConfiguration();
        cachingConfig.putSafe(CachingCalendarAccessConstants.LAST_UPDATE, Long.valueOf(timestamp));
    }

    protected JSONObject getCachingConfiguration() {
        JSONObject internalConfig = this.getAccount().getInternalConfiguration();
        JSONObject caching = internalConfig.optJSONObject(CachingCalendarAccessConstants.CACHING);
        if (caching == null) {
            caching = new JSONObject();
            internalConfig.putSafe(CachingCalendarAccessConstants.CACHING, caching);
        }
        return caching;
    }

    private void releaseUpdateLock() {
        JSONObject caching = getCachingConfiguration();
        if (caching != null && caching.remove(CachingCalendarAccessConstants.LOCKED_FOR_UPDATE_UNTIL) != null) {
            caching.remove(CachingCalendarAccessConstants.LOCKED_FOR_UPDATE_BY);
        }
    }

    /**
     * Updates the cached calendar data in the storage based on the supplied external calendar result.
     *
     * @param storage The initialized calendar storage to use
     * @param externalCalendarResult The external calendar result
     */
    protected void updateCache(CalendarStorage storage, ExternalCalendarResult externalCalendarResult) throws OXException {
        List<Event> existingEvents = this.getExistingEvents();
        EventUpdates diff = null;

        if (externalCalendarResult instanceof DiffAwareExternalCalendarResult) {
            diff = ((DiffAwareExternalCalendarResult) externalCalendarResult).calculateDiff(existingEvents);
        } else {
            Map<String, List<Event>> externalEvents = prepareExternalEvents(externalCalendarResult.getEvents());
            List<Event> updatedEvents = externalEvents.values().stream().flatMap(List::stream).collect(Collectors.toList());
            if (externalEvents.containsKey(null)) {
                /*
                 * event source contains events without UID, use a replacing event update as fallback
                 */
                diff = new EmptyUidUpdates(existingEvents, updatedEvents);
            } else {
                diff = generateEventDiff(existingEvents, updatedEvents);
            }
        }

        if (diff.isEmpty()) {
            /*
             * no data modified, indicate via exception to "back writable after reading"
             */
            throw CalendarExceptionCodes.DB_NOT_MODIFIED.create();
        }
        delete(storage, diff);
        create(storage, diff, existingEvents);
        update(storage, diff);
    }

    private List<Event> getExistingEvents() throws OXException {
        return new OSGiCalendarStorageOperation<List<Event>>(Services.getServiceLookup(), getSession().getContextId(), getAccount().getAccountId(), getParameters()) {

            @Override
            protected List<Event> call(CalendarStorage storage) throws OXException {
                EventField[] fields = getFields();
                List<Event> events = storage.getEventStorage().searchEvents(null, null, fields);
                return storage.getUtilities().loadAdditionalEventData(getAccount().getUserId(), events, fields);
            }

        }.executeQuery();
    }

    private static final EventField[] FIELDS_TO_IGNORE = new EventField[] { EventField.CREATED_BY, EventField.FOLDER_ID, EventField.ID, EventField.CALENDAR_USER, EventField.CREATED, EventField.MODIFIED_BY, EventField.EXTENDED_PROPERTIES, EventField.TIMESTAMP };
    private static final EventField[] EQUALS_IDENTIFIER = new EventField[] { EventField.UID, EventField.RECURRENCE_ID };

    private EventUpdates generateEventDiff(List<Event> persistedEvents, List<Event> updatedEvents) throws OXException {
        return CalendarUtils.getEventUpdates(persistedEvents, updatedEvents, true, FIELDS_TO_IGNORE, EQUALS_IDENTIFIER);
    }

    private void delete(CalendarStorage calendarStorage, EventUpdates diff) throws OXException {
        if (diff.getRemovedItems().isEmpty()) {
            return;
        }

        for (Event event : diff.getRemovedItems()) {
            delete(calendarStorage, event);
        }
    }

    protected void delete(CalendarStorage calendarStorage, Event originalEvent) throws OXException {
        if (isSeriesMaster(originalEvent)) {
            deleteExceptions(calendarStorage, originalEvent.getSeriesId(), getChangeExceptionDates(calendarStorage, originalEvent.getSeriesId()));
        }
        /*
         * delete event data from storage
         */
        String id = originalEvent.getId();
        calendarStorage.getEventStorage().insertEventTombstone(calendarStorage.getUtilities().getTombstone(originalEvent, new Date(), getCalendarUser()));
        calendarStorage.getAttendeeStorage().insertAttendeeTombstones(id, calendarStorage.getUtilities().getTombstones(originalEvent.getAttendees()));
        calendarStorage.getAlarmStorage().deleteAlarms(id);
        calendarStorage.getEventStorage().deleteEvent(id);
        calendarStorage.getAttendeeStorage().deleteAttendees(id, originalEvent.getAttendees());
    }

    protected SortedSet<RecurrenceId> getChangeExceptionDates(CalendarStorage calendarStorage, String seriesId) throws OXException {
        CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.AND).addSearchTerm(CalendarUtils.getSearchTerm(EventField.SERIES_ID, SingleOperation.EQUALS, seriesId)).addSearchTerm(CalendarUtils.getSearchTerm(EventField.ID, SingleOperation.NOT_EQUALS, new ColumnFieldOperand<EventField>(EventField.SERIES_ID)));
        List<Event> changeExceptions = calendarStorage.getEventStorage().searchEvents(searchTerm, null, new EventField[] { EventField.RECURRENCE_ID });
        return CalendarUtils.getRecurrenceIds(changeExceptions);
    }

    protected void deleteExceptions(CalendarStorage calendarStorage, String seriesID, Collection<RecurrenceId> exceptionDates) throws OXException {
        for (Event originalExceptionEvent : loadExceptionData(calendarStorage, seriesID, exceptionDates)) {
            delete(calendarStorage, originalExceptionEvent);
        }
    }

    protected List<Event> loadExceptionData(CalendarStorage calendarStorage, String seriesID, Collection<RecurrenceId> recurrenceIDs) throws OXException {
        List<Event> exceptions = new ArrayList<Event>();
        if (null != recurrenceIDs && 0 < recurrenceIDs.size()) {
            for (RecurrenceId recurrenceID : recurrenceIDs) {
                Event exception = calendarStorage.getEventStorage().loadException(seriesID, recurrenceID, null);
                if (null == exception) {
                    throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(seriesID, String.valueOf(recurrenceID));
                }
                exceptions.add(exception);
            }
        }
        return calendarStorage.getUtilities().loadAdditionalEventData(session.getUserId(), exceptions, getFields());
    }

    private void create(CalendarStorage calendarStorage, EventUpdates diff, List<Event> originalEvents) throws OXException {
        if (diff.getAddedItems().isEmpty()) {
            return;
        }
        create(calendarStorage, diff.getAddedItems(), originalEvents);
    }

    private void update(CalendarStorage calendarStorage, EventUpdates diff) throws OXException {
        if (diff.getUpdatedItems().isEmpty()) {
            return;
        }

        for (EventUpdate eventUpdate : diff.getUpdatedItems()) {
            Event persistedEvent = eventUpdate.getOriginal();
            Event updatedEvent = eventUpdate.getUpdate();
            /*
             * update via special 'delta' event so that identifying properties are still available for the storage
             */
            Set<EventField> updatedFields = eventUpdate.getUpdatedFields();
            Event deltaEvent = EventMapper.getInstance().copy(persistedEvent, null, (EventField[]) null);
            deltaEvent = EventMapper.getInstance().copy(updatedEvent, deltaEvent, updatedFields.toArray(new EventField[updatedFields.size()]));
            deltaEvent = new DeltaEvent(deltaEvent, updatedFields);
            calendarStorage.getEventStorage().updateEvent(deltaEvent);

            CollectionUpdate<Attendee, AttendeeField> attendeeUpdates = eventUpdate.getAttendeeUpdates();
            if (!attendeeUpdates.isEmpty()) {
                updateAttendees(calendarStorage, deltaEvent.getId(), attendeeUpdates);
            }

            CollectionUpdate<Alarm, AlarmField> alarmUpdates = eventUpdate.getAlarmUpdates();
            if (!alarmUpdates.isEmpty()) {
                updateAlarms(calendarStorage, deltaEvent, alarmUpdates);
            }
        }
    }

    private void updateAlarms(CalendarStorage calendarStorage, Event event, CollectionUpdate<Alarm, AlarmField> alarmUpdates) throws OXException {
        if (!alarmUpdates.isEmpty()) {
            int userId = getSession().getUserId();
            AlarmStorage alarmStorage = calendarStorage.getAlarmStorage();
            if (!alarmUpdates.getAddedItems().isEmpty()) {
                for (Alarm alarm : alarmUpdates.getAddedItems()) {
                    alarm.setId(alarmStorage.nextId());
                    alarm.setTimestamp(System.currentTimeMillis());
                }
                alarmStorage.insertAlarms(event, userId, alarmUpdates.getAddedItems());
            }
            if (!alarmUpdates.getRemovedItems().isEmpty()) {
                List<Integer> removedAlarms = new ArrayList<>(alarmUpdates.getRemovedItems().size());
                for (Alarm alarm : alarmUpdates.getRemovedItems()) {
                    removedAlarms.add(I(alarm.getId()));
                }
                alarmStorage.deleteAlarms(event.getId(), userId, ArrayUtils.toPrimitive(removedAlarms.toArray(new Integer[removedAlarms.size()])));
            }
            List<? extends ItemUpdate<Alarm, AlarmField>> updatedItems = alarmUpdates.getUpdatedItems();
            if (!updatedItems.isEmpty()) {
                List<Alarm> alarms = new ArrayList<Alarm>(updatedItems.size());
                for (ItemUpdate<Alarm, AlarmField> itemUpdate : updatedItems) {
                    Alarm update = itemUpdate.getUpdate();
                    update.setId(itemUpdate.getOriginal().getId());
                    update.setUid(itemUpdate.getOriginal().getUid());
                    update.setTimestamp(System.currentTimeMillis());
                    alarms.add(update);
                }
                alarmStorage.updateAlarms(event, userId, alarms);
            }
        }
    }

    private void updateAttendees(CalendarStorage calendarStorage, String eventId, CollectionUpdate<Attendee, AttendeeField> attendeeUpdates) throws OXException {
        if (!attendeeUpdates.isEmpty()) {
            AttendeeStorage attendeeStorage = calendarStorage.getAttendeeStorage();
            if (!attendeeUpdates.getAddedItems().isEmpty()) {
                calendarStorage.getAttendeeStorage().insertAttendees(eventId, attendeeUpdates.getAddedItems());
            }
            if (!attendeeUpdates.getRemovedItems().isEmpty()) {
                attendeeStorage.deleteAttendees(eventId, attendeeUpdates.getRemovedItems());
            }
            if (!attendeeUpdates.getUpdatedItems().isEmpty()) {
                List<Attendee> updatedAttendees = new ArrayList<>();
                for (ItemUpdate<Attendee, AttendeeField> attendeeUpdate : attendeeUpdates.getUpdatedItems()) {
                    Attendee updated = attendeeUpdate.getUpdate();
                    Attendee original = attendeeUpdate.getOriginal();
                    Attendee newUpdatedAttendee = AttendeeMapper.getInstance().copy(original, updated, AttendeeField.URI);
                    updatedAttendees.add(newUpdatedAttendee);
                }
                calendarStorage.getAttendeeStorage().updateAttendees(eventId, updatedAttendees);
            }
        }
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
        long now = System.currentTimeMillis();
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
                LOG.debug("Concurrent modification while attempting to persist lock for account {}, aborting. Account is already locked until {} by {}", I(account.getAccountId()), L(lockedUntil), null == actualLockedBy ? "" : actualLockedBy, e);
                account = Services.getService(CalendarAccountService.class).getAccount(session, account.getAccountId(), parameters);
                return false;
            }
            throw e;
        }
    }

    /**
     * Persists changes made for the account and releases a previously acquired lock for updating the account's cached calendar data.
     */
    private void saveConfig() throws OXException {
        AdministrativeCalendarAccountService accountService = Services.getService(AdministrativeCalendarAccountService.class);
        try {
            account = accountService.updateAccount(session.getContextId(), session.getUserId(), account.getAccountId(), account.getInternalConfiguration(), account.getUserConfiguration(), account.getLastModified().getTime());
            LOG.debug("Successfully released lock for account {}.", I(account.getAccountId()));
            return;
        } catch (OXException e) {
            if (CalendarExceptionCodes.CONCURRENT_MODIFICATION.equals(e)) {
                /*
                 * account updated in the meantime; refresh & don't update now
                 */
                LOG.debug("Concurrent modification while attempting to release lock for account {}, aborting.", I(account.getAccountId()), e);
                account = Services.getService(CalendarAccountService.class).getAccount(session, account.getAccountId(), parameters);
                return;
            }
            throw e;
        }
    }

    private void handleInternally(OXException e) {
        OXException copy = new OXException(e);
        long retryAfterErrorInterval = getRetryAfterErrorInterval(copy);
        if (retryAfterErrorInterval < BasicCachingCalendarConstants.MINIMUM_DEFAULT_RETRY_AFTER_ERROR_INTERVAL) { // prevent wrong configuration that will allow ongoing external requests
            retryAfterErrorInterval = BasicCachingCalendarConstants.MINIMUM_DEFAULT_RETRY_AFTER_ERROR_INTERVAL;
        }
        long timeoutInMillis = TimeUnit.SECONDS.toMillis(retryAfterErrorInterval);
        long nextProcessingAfter = System.currentTimeMillis() + timeoutInMillis;
        updateLastUpdated(nextProcessingAfter);

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
        updateCacheIfNeeded();
        return new SearchHandler(session, account, parameters).searchEvents(filters, queries);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.provider.extensions.BasicSyncAware#getUpdatedEvents(long)
     */
    @Override
    public UpdatesResult getUpdatedEvents(long updatedSince) throws OXException {
        updateCacheIfNeeded();
        return new SyncHandler(session, account, parameters).getUpdatedEvents(updatedSince);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.provider.extensions.BasicSyncAware#getSequenceNumber()
     */
    @Override
    public long getSequenceNumber() throws OXException {
        updateCacheIfNeeded();
        return new SyncHandler(session, account, parameters).getSequenceNumber();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.provider.extensions.BasicSyncAware#resolveResource(java.lang.String)
     */
    @Override
    public List<Event> resolveResource(String resourceName) throws OXException {
        updateCacheIfNeeded();
        return new SyncHandler(session, account, parameters).resolveResource(resourceName);
    }

    @Override
    public Map<String, EventsResult> resolveResources(List<String> resourceNames) throws OXException {
        updateCacheIfNeeded();
        return new SyncHandler(session, account, parameters).resolveResources(resourceNames);
    }

    protected void create(CalendarStorage calendarStorage, List<Event> externalEvents, List<Event> originalEvents) throws OXException {
        if (!externalEvents.isEmpty()) {
            Map<String, List<Event>> originalEventsByUID = CalendarUtils.getEventsByUID(originalEvents, false);
            Date now = new Date();
            Map<String, List<Event>> extEventsByUID = getEventsByUID(externalEvents, true);
            for (Entry<String, List<Event>> entry : extEventsByUID.entrySet()) {
                List<Event> originalEventGroup = sortSeriesMasterFirst(originalEventsByUID.get(entry.getKey()));
                if (null != originalEventGroup && 0 < originalEventGroup.size() && isSeriesMaster(originalEventGroup.get(0))) {
                    insertEvents(calendarStorage, now, sortSeriesMasterFirst(entry.getValue()), originalEventGroup.get(0));
                } else {
                    insertEvents(calendarStorage, now, sortSeriesMasterFirst(entry.getValue()), null);
                }
            }
        }
    }

    protected void insertEvents(CalendarStorage calendarStorage, Date now, List<Event> events, Event originalSeriesMaster) throws OXException {
        if (null == events || 0 == events.size()) {
            return;
        }

        String id = calendarStorage.getEventStorage().nextId();
        Event importedEvent = applyDefaults(events.get(0), now);
        importedEvent.setId(id);
        importedEvent.setCalendarUser(getCalendarUser());
        String seriesId = Strings.isNotEmpty(importedEvent.getRecurrenceRule()) ? id : null != originalSeriesMaster ? originalSeriesMaster.getSeriesId() : null;
        importedEvent.setSeriesId(seriesId);
        calendarStorage.getEventStorage().insertEvent(importedEvent);
        List<Attendee> attendees = importedEvent.getAttendees();

        if (null != attendees && !attendees.isEmpty()) {
            calendarStorage.getAttendeeStorage().insertAttendees(id, attendees);
        }

        if (null != importedEvent.getAlarms() && !importedEvent.getAlarms().isEmpty()) {
            for (Alarm alarm : importedEvent.getAlarms()) {
                alarm.setId(calendarStorage.getAlarmStorage().nextId());
                alarm.setTimestamp(System.currentTimeMillis());
            }
            calendarStorage.getAlarmStorage().insertAlarms(importedEvent, session.getUserId(), importedEvent.getAlarms());
        }

        for (int i = 1; i < events.size(); i++) {
            Event importedChangeException = applyDefaults(events.get(i), now);
            importedChangeException.setSeriesId(seriesId);
            importedChangeException.setId(calendarStorage.getEventStorage().nextId());
            calendarStorage.getEventStorage().insertEvent(importedChangeException);
            List<Attendee> changeExceptionAttendees = importedChangeException.getAttendees();
            if (null != changeExceptionAttendees && !changeExceptionAttendees.isEmpty()) {
                calendarStorage.getAttendeeStorage().insertAttendees(importedChangeException.getId(), changeExceptionAttendees);
            }
            if (null != importedChangeException.getAlarms() && !importedChangeException.getAlarms().isEmpty()) {
                for (Alarm alarm : importedChangeException.getAlarms()) {
                    alarm.setId(calendarStorage.getAlarmStorage().nextId());
                    alarm.setTimestamp(System.currentTimeMillis());
                }
                calendarStorage.getAlarmStorage().insertAlarms(importedChangeException, session.getUserId(), importedChangeException.getAlarms());
            }
        }
    }

    private Event applyDefaults(Event importedEvent, Date now) {
        importedEvent.setCalendarUser(getCalendarUser());
        importedEvent.setTimestamp(now.getTime());
        if (null != importedEvent.getRecurrenceId()) {
            importedEvent.setChangeExceptionDates(new TreeSet<RecurrenceId>(Collections.singleton(importedEvent.getRecurrenceId())));
        }
        return importedEvent;
    }

    /**
     * Gets the calendar user representing the internal user associated with the underlying calendar account.
     *
     * @return The calendar user
     */
    private CalendarUser getCalendarUser() {
        CalendarUser calendarUser = new CalendarUser();
        calendarUser.setEntity(session.getUserId());
        calendarUser.setUri(ResourceId.forUser(session.getContextId(), session.getUserId()));
        return calendarUser;
    }

    private static final List<EventField> IGNORED_FIELDS = Arrays.asList(EventField.ATTACHMENTS);

    protected EventField[] getFields() {
        EventField[] all = EventField.values();

        Set<EventField> fields = new HashSet<EventField>();
        fields.addAll(Arrays.asList(all));
        fields.removeAll(IGNORED_FIELDS);
        return fields.toArray(new EventField[fields.size()]);
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

    /**
     * Prepares the list of events from the external calendar source for further processing. This includes:
     * <ul>
     * <li>remove events that cannot be stored due to missing mandatory fields</li>
     * <li>map events by their UID property (events without UID are mapped to <code>null</code>)</li>
     * <li>event lists are sorted so that the series master event will be the first element</li>
     * <li>the change exception field of series master events will be set based on the actual overridden instances</li>
     * </ul>
     *
     * @param events The events to prepare
     * @return The prepared events, mapped by their unique identifier (events without UID are mapped to <code>null</code>)
     */
    private static Map<String, List<Event>> prepareExternalEvents(List<Event> events) {
        if (null == events) {
            return Collections.emptyMap();
        }
        Map<String, List<Event>> eventsByUID = new LinkedHashMap<String, List<Event>>();
        for (Event event : events) {
            /*
             * ignore events lacking mandatory fields
             */
            try {
                Check.mandatoryFields(event, EventField.START_DATE);
            } catch (OXException e) {
                LOG.debug("Removed event with uid {} from list to add because of the following corrupt data: {}", event.getUid(), e.getMessage());
                continue;
            }
            /*
             * map events by UID
             */
            com.openexchange.tools.arrays.Collections.put(eventsByUID, event.getUid(), event);
        }
        for (List<Event> eventGroup : eventsByUID.values()) {
            if (1 >= eventGroup.size()) {
                continue;
            }
            /*
             * sort series master first, then assign change exception dates
             */
            eventGroup = sortSeriesMasterFirst(eventGroup);
            if (null != eventGroup.get(0).getRecurrenceRule()) {
                eventGroup.get(0).setChangeExceptionDates(getRecurrenceIds(eventGroup.subList(1, eventGroup.size())));
            }
        }
        return eventsByUID;
    }

}
