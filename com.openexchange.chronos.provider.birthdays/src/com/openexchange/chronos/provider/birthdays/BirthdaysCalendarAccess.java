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

package com.openexchange.chronos.provider.birthdays;

import static com.openexchange.chronos.common.CalendarUtils.optTimeZone;
import static com.openexchange.tools.arrays.Arrays.contains;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.TimeTransparency;
import com.openexchange.chronos.Transp;
import com.openexchange.chronos.common.DefaultCalendarResult;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarFolder;
import com.openexchange.chronos.provider.CalendarPermission;
import com.openexchange.chronos.provider.DefaultCalendarFolder;
import com.openexchange.chronos.provider.DefaultCalendarPermission;
import com.openexchange.chronos.provider.SingleFolderCalendarAccess;
import com.openexchange.chronos.provider.extensions.PersonalAlarmAware;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.chronos.service.UpdateResult;
import com.openexchange.contact.ContactFieldOperand;
import com.openexchange.contact.ContactService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.util.TimeZones;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
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
public class BirthdaysCalendarAccess extends SingleFolderCalendarAccess implements PersonalAlarmAware {

    /** Search term to query for contacts having a birthday */
    private static final SearchTerm<?> HAS_BIRTHDAY_TERM = new CompositeSearchTerm(CompositeOperation.NOT)
        .addSearchTerm(new SingleSearchTerm(SingleOperation.ISNULL).addOperand(new ContactFieldOperand(ContactField.BIRTHDAY)));

    private final ServerSession session;
    private final ServiceLookup services;
    private final EventConverter eventConverter;

    /**
     * Initializes a new {@link BirthdaysCalendarAccess2}.
     *
     * @param services A service lookup reference
     * @param session The session
     * @param account The underlying calendar account
     * @param parameters Additional calendar parameters
     */
    public BirthdaysCalendarAccess(ServiceLookup services, ServerSession session, CalendarAccount account, CalendarParameters parameters) {
        super(account, parameters, prepareFolder(session, account));
        this.services = services;
        this.session = session;
        this.eventConverter = new EventConverter(services, session.getUser().getLocale(), account.getUserId());
    }

    /**
     * (Re-)initializes the birthdays calendar access.
     */
    public void initialize() throws OXException {
        /*
         * clean up any existing alarm remnants & insert new default alarms if configured
         */
        List<Map<String, Object>> defaultAlarms = new ArrayList<Map<String, Object>>();

        Map<String, Object> defaultAlarm = new HashMap<String, Object>();
        defaultAlarm.put("action", "DISPLAY");
        defaultAlarm.put("description", "Reminder");
        Map<String, Object> trigger = new HashMap<String, Object>();
        trigger.put("duration", "PT9H");
        defaultAlarm.put("trigger", trigger);

        Map<String, Object> configuration = account.getConfiguration();
        configuration.put("defaultAlarm", defaultAlarms);

        AlarmHelper alarmHelper = getAlarmHelper();
        alarmHelper.deleteAllAlarms();
        if (alarmHelper.hasDefaultAlarms()) {
            List<Contact> contacts = getBirthdayContacts();
            List<Event> seriesMasters = eventConverter.getSeriesMasters(contacts, null, null, getTimeZone());
            alarmHelper.insertDefaultAlarms(seriesMasters);
        }
    }

    @Override
    public void close() {
        // nothing to do
    }

    @Override
    protected Event getEvent(String eventId, RecurrenceId recurrenceId) throws OXException {
        Contact contact = getBirthdayContact(eventId);
        Event event = null == recurrenceId ? eventConverter.getSeriesMaster(contact) : eventConverter.getOccurrence(contact, recurrenceId);
        return postProcess(event);
    }

    @Override
    protected List<Event> getEvents() throws OXException {
        List<Contact> contacts = getBirthdayContacts();
        if (isExpandOccurrences()) {
            return postProcess(eventConverter.getOccurrences(contacts, getFrom(), getUntil(), getTimeZone()));
        } else {
            return postProcess(eventConverter.getSeriesMasters(contacts, getFrom(), getUntil(), getTimeZone()));
        }
    }

    @Override
    public List<Event> getChangeExceptions(String folderId, String seriesId) throws OXException {
        // no change exceptions possible
        checkFolderId(folderId);
        return Collections.emptyList();
    }

    @Override
    public CalendarResult updateAlarms(EventID eventID, List<Alarm> alarms, long clientTimestamp) throws OXException {
        checkFolderId(eventID.getFolderID());
        if (null != eventID.getRecurrenceID()) {
            throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(eventID.getObjectID(), eventID.getRecurrenceID());
        }
        Event originalEvent = eventConverter.getSeriesMaster(getBirthdayContact(eventID.getObjectID()));
        UpdateResult updateResult = getAlarmHelper().updateAlarms(originalEvent, alarms);
        return new DefaultCalendarResult(session, session.getUserId(), FOLDER_ID, null, null == updateResult ? null : Collections.singletonList(updateResult), null);
    }

    private Event postProcess(Event event) throws OXException {
        if (contains(getFields(), EventField.ALARMS)) {
            event = getAlarmHelper().applyAlarms(event);
        }
        return event;
    }

    private List<Event> postProcess(List<Event> events) throws OXException {
        if (contains(getFields(), EventField.ALARMS)) {
            events = getAlarmHelper().applyAlarms(events);
        }
        return sortEvents(events, new SearchOptions(parameters), getTimeZone());
    }

    private List<Event> sortEvents(List<Event> events, SearchOptions searchOptions, TimeZone timeZone) throws OXException {
        if (null == events || 2 > events.size() || null == searchOptions || SearchOptions.EMPTY.equals(searchOptions) ||
            null == searchOptions.getSortOrders() || 0 == searchOptions.getSortOrders().length) {
            return events;
        }
        Collections.sort(events, services.getService(CalendarUtilities.class).getComparator(searchOptions.getSortOrders(), timeZone));
        return events;
    }

    private List<Contact> getBirthdayContacts() throws OXException {
        return searchBirthdayContacts(null);
    }

    private Contact getBirthdayContact(String eventId) throws OXException {
        try {
            int[] decodedId = eventConverter.decodeEventId(eventId);
            Contact contact = services.getService(ContactService.class).getContact(session, String.valueOf(decodedId[0]), String.valueOf(decodedId[1]));
            if (null == contact.getBirthday()) {
                throw OXException.notFound(eventId);
            }
            return contact;
        } catch (IllegalArgumentException | OXException e) {
            throw CalendarExceptionCodes.EVENT_NOT_FOUND.create(e, eventId);
        }
    }

    private List<Contact> searchBirthdayContacts(SearchTerm<?> searchTerm) throws OXException {
        if (null == searchTerm) {
            searchTerm = HAS_BIRTHDAY_TERM;
        } else {
            searchTerm = new CompositeSearchTerm(CompositeOperation.AND).addSearchTerm(HAS_BIRTHDAY_TERM).addSearchTerm(searchTerm);
        }
        SearchIterator<Contact> searchIterator = null;
        try {
            return SearchIterators.asList(searchIterator = services.getService(ContactService.class).searchContacts(session, searchTerm));
        } finally {
            SearchIterators.close(searchIterator);
        }
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

    private TimeZone getTimeZone() throws OXException {
        TimeZone timeZone = null != parameters ? parameters.get(CalendarParameters.PARAMETER_TIMEZONE, TimeZone.class) : null;
        return null != timeZone ? timeZone : optTimeZone(session.getUser().getTimeZone(), TimeZones.UTC);
    }

    private static CalendarFolder prepareFolder(ServerSession session, CalendarAccount account) {
        DefaultCalendarFolder folder = new DefaultCalendarFolder();
        folder.setId(FOLDER_ID);


        folder.setPermissions(Collections.singletonList(DefaultCalendarPermission.readOnlyPermissionsFor(account.getUserId())));


        CalendarPermission permission = new DefaultCalendarPermission(
            account.getUserId(),
            CalendarPermission.READ_FOLDER,
            CalendarPermission.READ_ALL_OBJECTS,
            CalendarPermission.WRITE_ALL_OBJECTS,
            CalendarPermission.NO_PERMISSIONS,
            false,
            false,
            CalendarPermission.NO_PERMISSIONS)
        ;
        folder.setPermissions(Collections.singletonList(permission));


        StringHelper stringHelper = StringHelper.valueOf(session.getUser().getLocale());
        folder.setName(stringHelper.getString(BirthdaysCalendarStrings.CALENDAR_NAME));
        folder.setTransparency(TimeTransparency.TRANSPARENT);
        Map<String, Object> config = account.getConfiguration();
        if (null != config) {
            folder.setColor((String) config.get("color"));
            folder.setTransparency(Transp.TRANSPARENT.equals(config.get("transp")) ? TimeTransparency.TRANSPARENT : TimeTransparency.OPAQUE);
        }
        return folder;
    }

}
