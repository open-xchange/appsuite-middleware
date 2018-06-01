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
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.arrays.Arrays.contains;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmTrigger;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.TimeTransparency;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultCalendarResult;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.basic.BasicCalendarAccess;
import com.openexchange.chronos.provider.basic.CalendarSettings;
import com.openexchange.chronos.provider.extensions.BasicSearchAware;
import com.openexchange.chronos.provider.extensions.PersonalAlarmAware;
import com.openexchange.chronos.provider.extensions.SubscribeAware;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarResult;
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
public class BirthdaysCalendarAccess implements BasicCalendarAccess, SubscribeAware, PersonalAlarmAware, BasicSearchAware {

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
        extendedProperties.add(USED_FOR_SYNC(Boolean.FALSE, true));
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
        UpdateResult updateResult = getAlarmHelper().updateAlarms(originalEvent, alarms);
        return new DefaultCalendarResult(session, session.getUserId(), FOLDER_ID, null, null == updateResult ? null : Collections.singletonList(updateResult), null);
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

    private List<Event> postProcess(List<Event> events) throws OXException {
        if (contains(getFields(), EventField.ALARMS)) {
            events = getAlarmHelper().applyAlarms(events);
        }
        TimeZone timeZone = getTimeZone();
        Date from = getFrom();
        Date until = getUntil();
        for (Iterator<Event> iterator = events.iterator(); iterator.hasNext();) {
            if (false == CalendarUtils.isInRange(iterator.next(), from, until, timeZone)) {
                iterator.remove();
            }
        }
        CalendarUtils.sortEvents(events, new SearchOptions(parameters).getSortOrders(), timeZone);
        return events;
    }

    private Event postProcess(Event event) throws OXException {
        if (contains(getFields(), EventField.ALARMS)) {
            event = getAlarmHelper().applyAlarms(event);
        }
        return event;
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
                        .addOperand(new ConstantOperand<String>(folderIds.get(0))))
                    .addSearchTerm(searchTerm);
            } else {
                CompositeSearchTerm orTerm = new CompositeSearchTerm(CompositeOperation.OR);
                for (String folderId : folderIds) {
                    orTerm.addSearchTerm(new SingleSearchTerm(SingleOperation.EQUALS)
                        .addOperand(new ContactFieldOperand(ContactField.FOLDER_ID))
                        .addOperand(new ConstantOperand<String>(folderId)));
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
        List<Contact> contacts = new ArrayList<Contact>();
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
        return parameters.get(CalendarParameters.PARAMETER_EXPAND_OCCURRENCES, Boolean.class, Boolean.TRUE).booleanValue();
    }

    private EventField[] getFields() {
        return parameters.get(CalendarParameters.PARAMETER_FIELDS, EventField[].class, EventField.values());
    }

    private TimeZone getTimeZone() {
        TimeZone timeZone = null != parameters ? parameters.get(CalendarParameters.PARAMETER_TIMEZONE, TimeZone.class) : null;
        return null != timeZone ? timeZone : optTimeZone(session.getUser().getTimeZone(), TimeZones.UTC);
    }

    private List<String> getContactFolderIds() throws OXException {
        List<String> folderIds = new ArrayList<String>();
        JSONArray typesJSONArray = account.getUserConfiguration().optJSONArray("folderTypes");
        if (null == typesJSONArray) {
            return null;
        }
        Set<String> types = new HashSet<String>(typesJSONArray.length());
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
        List<String> folderIds = new ArrayList<String>();
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

    protected Date getFrom() {
        return parameters.get(CalendarParameters.PARAMETER_RANGE_START, Date.class);
    }

    protected Date getUntil() {
        return parameters.get(CalendarParameters.PARAMETER_RANGE_END, Date.class);
    }

}
