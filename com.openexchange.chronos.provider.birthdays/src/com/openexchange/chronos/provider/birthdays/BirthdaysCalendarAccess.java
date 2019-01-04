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

package com.openexchange.chronos.provider.birthdays;

import static com.openexchange.chronos.common.CalendarUtils.optTimeZone;
import static com.openexchange.chronos.provider.CalendarFolderProperty.COLOR;
import static com.openexchange.chronos.provider.CalendarFolderProperty.DESCRIPTION;
import static com.openexchange.chronos.provider.CalendarFolderProperty.SCHEDULE_TRANSP;
import static com.openexchange.chronos.provider.CalendarFolderProperty.USED_FOR_SYNC;
import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.arrays.Arrays.contains;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmTrigger;
import com.openexchange.chronos.DelegatingEvent;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.TimeTransparency;
import com.openexchange.chronos.common.AlarmPreparator;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultCalendarEvent;
import com.openexchange.chronos.common.DefaultCalendarResult;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.basic.BasicCalendarAccess;
import com.openexchange.chronos.provider.basic.CalendarSettings;
import com.openexchange.chronos.provider.caching.CachingCalendarUtils;
import com.openexchange.chronos.provider.extensions.BasicCTagAware;
import com.openexchange.chronos.provider.extensions.BasicSearchAware;
import com.openexchange.chronos.provider.extensions.PersonalAlarmAware;
import com.openexchange.chronos.provider.extensions.SubscribeAware;
import com.openexchange.chronos.service.CalendarEventNotificationService;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.SearchFilter;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.chronos.service.UpdateResult;
import com.openexchange.contact.ContactFieldOperand;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.SortOptions;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.ContactContentType;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.Order;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Strings;
import com.openexchange.java.util.TimeZones;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ConstantOperand;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link BirthdaysCalendarAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class BirthdaysCalendarAccess implements BasicCalendarAccess, SubscribeAware, PersonalAlarmAware, BasicSearchAware, BasicCTagAware {

    /** Search term to query for contacts having a birthday */
    private static final SearchTerm<?> HAS_BIRTHDAY_TERM = new CompositeSearchTerm(CompositeOperation.NOT)
        .addSearchTerm(new SingleSearchTerm(SingleOperation.ISNULL).addOperand(new ContactFieldOperand(ContactField.BIRTHDAY)));

    /** The fields queried from the contact storage */
    private static final ContactField[] CONTACT_FIELDS = new ContactField[] {
        ContactField.OBJECT_ID, ContactField.FOLDER_ID, ContactField.INTERNAL_USERID, ContactField.UID, ContactField.BIRTHDAY,
        ContactField.LAST_MODIFIED, ContactField.DEPARTMENT, ContactField.SUR_NAME, ContactField.GIVEN_NAME, ContactField.EMAIL1
    };

    private final ServerSession session;
    private final ServiceLookup services;
    private final EventConverter eventConverter;
    private final CalendarAccount account;
    private final CalendarParameters parameters;

    /**
     * Initializes a new {@link BirthdaysCalendarAccess}.
     *
     * @param services A service lookup reference
     * @param session The session
     * @param account The underlying calendar account
     * @param parameters Additional calendar parameters
     */
    public BirthdaysCalendarAccess(ServiceLookup services, ServerSession session, CalendarAccount account, CalendarParameters parameters) {
        super();
        this.account = account;
        this.services = services;
        this.session = session;
        this.parameters = parameters;
        this.eventConverter = new EventConverter(services, session.getUser().getLocale(), account.getUserId());
    }

    /**
     * Callback routine that is invoked after a new account for the calendar provider has been created.
     */
    public void onAccountCreated() throws OXException {
        AlarmHelper alarmHelper = getAlarmHelper();
        if (alarmHelper.hasDefaultAlarms()) {
            List<Contact> contacts = getBirthdayContacts();
            List<Event> seriesMasters = eventConverter.getSeriesMasters(contacts, null, null, getTimeZone());
            alarmHelper.insertDefaultAlarms(seriesMasters);
        }
    }

    /**
     * Callback routine that is invoked after an existing account for the calendar provider has been updated.
     */
    public void onAccountUpdated() throws OXException {
        onAccountDeleted();
        onAccountCreated();
    }

    /**
     * Callback routine that is invoked after an existing account for the calendar provider has been deleted.
     */
    public void onAccountDeleted() throws OXException {
        getAlarmHelper().deleteAllAlarms();
    }

    @Override
    public void close() {
        // nothing to do
    }

    @Override
    public CalendarSettings getSettings() {
        /*
         * init settings & and apply account configuration
         */
        StringHelper stringHelper = StringHelper.valueOf(session.getUser().getLocale());
        CalendarSettings settings = new CalendarSettings();
        settings.setConfig(account.getUserConfiguration());
        settings.setLastModified(account.getLastModified());
        /*
         * take over further properties from internal config
         */
        JSONObject internalConfig = null != account.getInternalConfiguration() ? account.getInternalConfiguration() : new JSONObject();
        String name = internalConfig.optString("name", null);
        if (Strings.isEmpty(name) || BirthdaysCalendarStrings.CALENDAR_NAME.equals(name)) {
            name = stringHelper.getString(BirthdaysCalendarStrings.CALENDAR_NAME);
        }
        settings.setName(name);
        settings.setSubscribed(internalConfig.optBoolean("subscribed", true));
        ExtendedProperties extendedProperties = new ExtendedProperties();
        extendedProperties.add(SCHEDULE_TRANSP(TimeTransparency.TRANSPARENT, true));
        extendedProperties.add(DESCRIPTION(stringHelper.getString(BirthdaysCalendarStrings.CALENDAR_DESCRIPTION), true));
        if (CachingCalendarUtils.canBeUsedForSync(BirthdaysCalendarProvider.PROVIDER_ID, session, true)) {
            extendedProperties.add(USED_FOR_SYNC(B(internalConfig.optBoolean("usedForSync", false)), false));
        } else {
            extendedProperties.add(USED_FOR_SYNC(Boolean.FALSE, true));
        }
        extendedProperties.add(COLOR(internalConfig.optString("color", null), false));
        settings.setExtendedProperties(extendedProperties);
        return settings;
    }

    @Override
    public Event getEvent(String eventId, RecurrenceId recurrenceId) throws OXException {
        Contact contact = getBirthdayContact(eventId);
        Event event = null == recurrenceId ? eventConverter.getSeriesMaster(contact) : eventConverter.getOccurrence(contact, recurrenceId);
        return postProcess(event);
    }

    @Override
    public List<Event> getEvents(List<EventID> eventIDs) throws OXException {
        List<Event> events = new ArrayList<Event>(eventIDs.size());
        Map<String, Contact> contacts = getBirthdayContacts(eventIDs);
        for (EventID eventID : eventIDs) {
            Contact contact = contacts.get(eventID.getObjectID());
            if (null == contact) {
                // log not found event, but include null in resulting list to preserve order
                org.slf4j.LoggerFactory.getLogger(BirthdaysCalendarAccess.class).debug("Requested event \"{}\" not found, skipping.", eventID);
                events.add(null);
            } else if (null != eventID.getRecurrenceID()) {
                events.add(postProcess(eventConverter.getOccurrence(contact, eventID.getRecurrenceID())));
            } else {
                events.add(postProcess(eventConverter.getSeriesMaster(contact)));
            }
        }
        return events;
    }

    @Override
    public List<Event> getEvents() throws OXException {
        List<Contact> contacts = getBirthdayContacts();
        if (isExpandOccurrences()) {
            return postProcess(eventConverter.getOccurrences(contacts, getFrom(), getUntil(), getTimeZone()));
        } else {
            return postProcess(eventConverter.getSeriesMasters(contacts, getFrom(), getUntil(), getTimeZone()));
        }
    }

    @Override
    public List<Event> getChangeExceptions(String seriesId) throws OXException {
        // no change exceptions possible
        return Collections.emptyList();
    }

    @Override
    public List<AlarmTrigger> getAlarmTriggers(Set<String> actions) throws OXException {
        Date until = parameters.get(CalendarParameters.PARAMETER_RANGE_END, Date.class);
        return getAlarmHelper().getAlarmTriggers(until, actions);
    }

    @Override
    public CalendarResult updateAlarms(EventID eventID, List<Alarm> alarms, long clientTimestamp) throws OXException {
        if (null != eventID.getRecurrenceID()) {
            throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(eventID.getObjectID(), eventID.getRecurrenceID());
        }
        Event originalEvent = eventConverter.getSeriesMaster(getBirthdayContact(eventID.getObjectID()));
        AlarmPreparator.getInstance().prepareEMailAlarms(session, services.getOptionalService(CalendarUtilities.class), alarms);
        UpdateResult updateResult = getAlarmHelper().updateAlarms(originalEvent, alarms);
        DefaultCalendarResult result = new DefaultCalendarResult(session, session.getUserId(), FOLDER_ID, null, null == updateResult ? null : Collections.singletonList(updateResult), null);
        return notifyHandlers(result);
    }

    private DefaultCalendarResult notifyHandlers(DefaultCalendarResult result) throws OXException {
        CalendarEventNotificationService notificationService = services.getServiceSafe(CalendarEventNotificationService.class);
        notificationService.notifyHandlers(new DefaultCalendarEvent(    session.getContextId(),
                                                                        account.getAccountId(),
                                                                        session.getUserId(),
                                                                        Collections.singletonMap(session.getUserId(), Collections.singletonList(BasicCalendarAccess.FOLDER_ID)),
                                                                        result.getCreations(),
                                                                        result.getUpdates(),
                                                                        result.getDeletions(),
                                                                        session,
                                                                        null,
                                                                        parameters));
        return result;
    }

    @Override
    public List<Event> searchEvents(List<SearchFilter> filters, List<String> queries) throws OXException {
        List<Contact> contacts = searchBirthdayContacts(SearchAdapter.getContactSearchTerm(filters, queries));
        if (isExpandOccurrences()) {
            return postProcess(eventConverter.getOccurrences(contacts, getFrom(), getUntil(), getTimeZone()));
        } else {
            return postProcess(eventConverter.getSeriesMasters(contacts, getFrom(), getUntil(), getTimeZone()));
        }
    }

    @Override
    public String getCTag() throws OXException {
        return getLastModifiedChecksum();
    }

    private List<Event> postProcess(List<Event> events) throws OXException {
        if (contains(getFields(), EventField.ALARMS) || contains(getFields(), EventField.FLAGS)) {
            events = getAlarmHelper().applyAlarms(events);
        } else if (contains(getFields(), EventField.TIMESTAMP)) {
            applyTimestampFromAlarms(events);
        }
        return CalendarUtils.sortEvents(events, new SearchOptions(parameters).getSortOrders(), getTimeZone());
    }

    private Event postProcess(Event event) throws OXException {
        if (contains(getFields(), EventField.ALARMS) || contains(getFields(), EventField.FLAGS)) {
            event = getAlarmHelper().applyAlarms(event);
        } else if (event.containsTimestamp()) {
            Long timestamp = getAlarmHelper().getLatestTimestamps(Collections.singletonList(event.getId()), account.getUserId()).get(event.getId());
            if (null != timestamp && timestamp.longValue() > event.getTimestamp()) {
                return new DelegatingEvent(event) {

                    @Override
                    public long getTimestamp() {
                        return timestamp;
                    }
                };
            }
        }
        return event;
    }

    private void applyTimestampFromAlarms(List<Event> events) throws OXException {
        List<String> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        Map<String, Long> timestamps = getAlarmHelper().getLatestTimestamps(eventIds, session.getUserId());
        ListIterator<Event> iterator = events.listIterator();
        while (iterator.hasNext()) {
            Event event = iterator.next();
            Long timestamp = timestamps.get(event.getId());
            if (timestamp != null && timestamp.longValue() > event.getTimestamp()) {
                iterator.set(new DelegatingEvent(event) {
                    @Override
                    public long getTimestamp() {
                        return timestamp;
                    }
                    @Override
                    public boolean containsTimestamp() {
                        return true;
                    }
                });
            }
        }
    }

    private List<Contact> getBirthdayContacts() throws OXException {
        return searchBirthdayContacts(null);
    }

    private Contact getBirthdayContact(String eventId) throws OXException {
        try {
            int[] decodedId = eventConverter.decodeEventId(eventId);
            Contact contact = services.getService(ContactService.class).getContact(session, String.valueOf(decodedId[0]), String.valueOf(decodedId[1]), CONTACT_FIELDS);
            if (null == contact.getBirthday()) {
                throw OXException.notFound(eventId);
            }
            return contact;
        } catch (IllegalArgumentException | OXException e) {
            throw CalendarExceptionCodes.EVENT_NOT_FOUND.create(e, eventId);
        }
    }

    private Map<String, Contact> getBirthdayContacts(List<EventID> eventIds) throws OXException {
        /*
         * separate contact ids by parent contact folder
         */
        Map<String, List<String>> idsPerFolderId = new HashMap<String, List<String>>();
        for (EventID eventId : eventIds) {
            try {
                int[] decodedId = eventConverter.decodeEventId(eventId.getObjectID());
                com.openexchange.tools.arrays.Collections.put(idsPerFolderId, String.valueOf(decodedId[0]), String.valueOf(decodedId[1]));
            } catch (IllegalArgumentException e) {
                org.slf4j.LoggerFactory.getLogger(BirthdaysCalendarAccess.class).debug("Skipping invalid event id {}", eventId, e);
            }
        }
        /*
         * get birthday contacts per folder from service
         */
        Map<String, Contact> contactsById = new HashMap<String, Contact>(eventIds.size());
        for (Entry<String, List<String>> entry : idsPerFolderId.entrySet()) {
            SearchIterator<Contact> searchIterator = null;
            try {
                searchIterator = services.getService(ContactService.class).getContacts(
                    session, entry.getKey(), entry.getValue().toArray(new String[entry.getValue().size()]), CONTACT_FIELDS);
                while (searchIterator.hasNext()) {
                    Contact contact = searchIterator.next();
                    if (null == contact.getBirthday()) {
                        org.slf4j.LoggerFactory.getLogger(BirthdaysCalendarAccess.class).debug(
                            "Skipping contact {} due to missing birthday.", I(contact.getObjectID()));
                        continue;
                    }
                    contactsById.put(eventConverter.getEventId(contact), contact);
                }
            } finally {
                SearchIterators.close(searchIterator);
            }
        }
        return contactsById;
    }

    /**
     * Searches for contacts having a known birthday located in one of the configured contact folders.
     *
     * @param searchTerm An additional search term to use, or <code>null</code> to return all contacts
     * @return The found contacts, or an empty list if there are none.
     */
    private List<Contact> searchBirthdayContacts(SearchTerm<?> searchTerm) throws OXException {
        /*
         * prepare base search term
         */
        if (null == searchTerm) {
            searchTerm = HAS_BIRTHDAY_TERM;
        } else {
            searchTerm = new CompositeSearchTerm(CompositeOperation.AND).addSearchTerm(HAS_BIRTHDAY_TERM).addSearchTerm(searchTerm);
        }
        /*
         * add parent folder restrictions
         */
        List<String> folderIds = getContactFolderIds();
        if (null != folderIds && 0 < folderIds.size()) {
            if (1 == folderIds.size()) {
                searchTerm = new CompositeSearchTerm(CompositeOperation.AND)
                    .addSearchTerm(new SingleSearchTerm(SingleOperation.EQUALS)
                        .addOperand(new ContactFieldOperand(ContactField.FOLDER_ID))
                        .addOperand(new ConstantOperand<>(folderIds.get(0))))
                    .addSearchTerm(searchTerm);
            } else {
                CompositeSearchTerm orTerm = new CompositeSearchTerm(CompositeOperation.OR);
                for (String folderId : folderIds) {
                    orTerm.addSearchTerm(new SingleSearchTerm(SingleOperation.EQUALS)
                        .addOperand(new ContactFieldOperand(ContactField.FOLDER_ID))
                        .addOperand(new ConstantOperand<>(folderId)));
                }
                searchTerm = new CompositeSearchTerm(CompositeOperation.AND).addSearchTerm(orTerm).addSearchTerm(searchTerm);
            }
        }
        /*
         * apply offset & limit for search
         */
        SortOptions sortOptions = null;
        if (null != parameters) {
            SearchOptions searchOptions = new SearchOptions(parameters);
            if (0 < searchOptions.getOffset() || 0 < searchOptions.getLimit()) {
                sortOptions = new SortOptions(searchOptions.getOffset(), searchOptions.getLimit());
            }
        }
        /*
         * perform search & collect contacts with birthday
         */
        List<Contact> contacts = new ArrayList<>();
        SearchIterator<Contact> searchIterator = null;
        try {
            searchIterator = services.getService(ContactService.class).searchContacts(session, searchTerm, CONTACT_FIELDS, sortOptions);
            while (searchIterator.hasNext()) {
                Contact contact = searchIterator.next();
                if (null == contact.getBirthday()) {
                    org.slf4j.LoggerFactory.getLogger(BirthdaysCalendarAccess.class).debug(
                        "Skipping contact {} due to missing birthday.", I(contact.getObjectID()));
                    continue;
                }
                contacts.add(contact);
            }
        } finally {
            SearchIterators.close(searchIterator);
        }
        return contacts;
    }

    private AlarmHelper getAlarmHelper() {
        return new AlarmHelper(services, session.getContext(), account);
    }

    private boolean isExpandOccurrences() {
        return parameters.get(CalendarParameters.PARAMETER_EXPAND_OCCURRENCES, Boolean.class, Boolean.FALSE).booleanValue();
    }

    private EventField[] getFields() {
        return parameters.get(CalendarParameters.PARAMETER_FIELDS, EventField[].class, EventField.values());
    }

    private TimeZone getTimeZone() {
        TimeZone timeZone = null != parameters ? parameters.get(CalendarParameters.PARAMETER_TIMEZONE, TimeZone.class) : null;
        return null != timeZone ? timeZone : optTimeZone(session.getUser().getTimeZone(), TimeZones.UTC);
    }

    private List<String> getContactFolderIds() throws OXException {
        List<String> folderIds = new ArrayList<>();
        JSONArray typesJSONArray = account.getUserConfiguration().optJSONArray("folderTypes");
        if (null == typesJSONArray) {
            return null;
        }
        Set<String> types = new HashSet<>(typesJSONArray.length());
        for (int i = 0; i < typesJSONArray.length(); i++) {
            types.add(typesJSONArray.optString(i));
        }
        if (types.contains("public") && types.contains("shared") && types.contains("private")) {
            return null;
        }
        for (String type : types) {
            folderIds.addAll(getContactFolderIds(type));
        }
        return folderIds;
    }

    private List<String> getContactFolderIds(String type) throws OXException {
        switch (type) {
            case "public":
                return getContactFolderIds(PublicType.getInstance());
            case "shared":
                return getContactFolderIds(SharedType.getInstance());
            case "private":
                return getContactFolderIds(PrivateType.getInstance());
            default:
                throw new IllegalArgumentException(type);
        }
    }

    private List<String> getContactFolderIds(Type type) throws OXException {
        List<String> folderIds = new ArrayList<>();
        FolderResponse<UserizedFolder[]> visibleFolders = services.getService(FolderService.class).getVisibleFolders(FolderStorage.REAL_TREE_ID, ContactContentType.getInstance(), type, false, session, null);
        UserizedFolder[] folders = visibleFolders.getResponse();
        if (null != folders && 0 < folders.length) {
            for (UserizedFolder folder : folders) {
                if (folder.getOwnPermission().getReadPermission() >= Permission.READ_OWN_OBJECTS) {
                    folderIds.add(folder.getID());
                }
            }
        }
        return folderIds;
    }

    private List<String> getAllContactFolderIds() throws OXException {
        List<String> folderIds = new ArrayList<>();
        folderIds.addAll(getContactFolderIds(PublicType.getInstance()));
        folderIds.addAll(getContactFolderIds(SharedType.getInstance()));
        folderIds.addAll(getContactFolderIds(PrivateType.getInstance()));
        return folderIds;
    }

    private static final ContactField[] LAST_MODIFIED_FIELDS = new ContactField[] {ContactField.LAST_MODIFIED};

    private String getLastModifiedChecksum() throws OXException {
        Date lastModified = new Date(0);

        List<String> folders = getContactFolderIds();
        if (null == folders) {
            folders = getAllContactFolderIds();
        }

        SortOptions sortOptions = new SortOptions(ContactField.LAST_MODIFIED, Order.DESCENDING);
        sortOptions.setLimit(1);
        ContactService contactService = services.getService(ContactService.class);

        int foldersHash = 1;
        for (String folder : folders) {
            foldersHash = 31 * foldersHash + folder.hashCode(); 
            try (SearchIterator<Contact> searchIterator = contactService.getModifiedContacts(session, folder, lastModified, LAST_MODIFIED_FIELDS, sortOptions)) {
                if (searchIterator.hasNext()) {
                    Contact contact = searchIterator.next();
                    lastModified = lastModified.after(contact.getLastModified()) ? lastModified : contact.getLastModified();
                }
            }
            try (SearchIterator<Contact> searchIterator = contactService.getDeletedContacts(session, folder, lastModified, LAST_MODIFIED_FIELDS, sortOptions)) {
                if (searchIterator.hasNext()) {
                    Contact contact = searchIterator.next();
                    lastModified = lastModified.after(contact.getLastModified()) ? lastModified : contact.getLastModified();
                }
            }
        }
        
        Date latestAlarmLastModified = new Date(getAlarmHelper().getLatestTimestamp(session.getUserId()));
        lastModified = lastModified.after(latestAlarmLastModified) ? lastModified : latestAlarmLastModified;

        return lastModified.getTime() + "-" + foldersHash;
    }

    protected Date getFrom() {
        return parameters.get(CalendarParameters.PARAMETER_RANGE_START, Date.class);
    }

    protected Date getUntil() {
        return parameters.get(CalendarParameters.PARAMETER_RANGE_END, Date.class);
    }

}
