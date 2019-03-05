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

package com.openexchange.chronos.itip;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.ParticipantRole;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.ResourceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.service.CalendarConfig;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.service.FreeBusyService;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.MockUser;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.java.Strings;
import com.openexchange.resource.Resource;
import com.openexchange.session.Session;

/**
 * {@link ChronosTestTools}
 *
 * TODO move into fitting bundle/package
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public final class ChronosTestTools {

    /**
     * Create an {@link Attendee} with random data.
     *
     * @param contextId The identifier of the context
     * @param attendeeFields <code>null</code> to fill all fields, otherwise the desired {@link AttendeeField}s
     * @return An {@link Attendee}
     */
    public static Attendee createAttendee(int contextId, AttendeeField... attendeeFields) {
        int uid = randomInt();
        String userId = String.valueOf(uid);

        Attendee attendee = new Attendee();

        if (null == attendeeFields || containsField(attendeeFields, AttendeeField.URI)) {
            attendee.setUri(ResourceId.forUser(contextId, uid));
        }
        if (null == attendeeFields || containsField(attendeeFields, AttendeeField.CN)) {
            attendee.setCn("Test, Attendee No." + userId);
        }
        if (null == attendeeFields || containsField(attendeeFields, AttendeeField.ENTITY)) {
            attendee.setEntity(uid);
        }
        if (null == attendeeFields || containsField(attendeeFields, AttendeeField.SENT_BY)) {
            Attendee sendBy = createAttendee(contextId, AttendeeField.ENTITY, AttendeeField.CN, AttendeeField.EMAIL, AttendeeField.CU_TYPE);
            attendee.setSentBy(sendBy);
        }
        if (null == attendeeFields || containsField(attendeeFields, AttendeeField.CU_TYPE)) {
            attendee.setCuType(CalendarUserType.INDIVIDUAL);
        }
        if (null == attendeeFields || containsField(attendeeFields, AttendeeField.ROLE)) {
            attendee.setRole(ParticipantRole.REQ_PARTICIPANT);
        }
        if (null == attendeeFields || containsField(attendeeFields, AttendeeField.PARTSTAT)) {
            attendee.setPartStat(ParticipationStatus.ACCEPTED);
        }
        if (null == attendeeFields || containsField(attendeeFields, AttendeeField.COMMENT)) {
            attendee.setComment("Totaly random comment for attendee NO. " + userId);
        }
        if (null == attendeeFields || containsField(attendeeFields, AttendeeField.RSVP)) {
            attendee.setRsvp(Boolean.TRUE);
        }
        if (null == attendeeFields || containsField(attendeeFields, AttendeeField.FOLDER_ID)) {
            attendee.setFolderId(String.valueOf(uid + 50));
        }
        if (null == attendeeFields || containsField(attendeeFields, AttendeeField.MEMBER)) {
            attendee.setMember(Collections.emptyList());
        }
        if (null == attendeeFields || containsField(attendeeFields, AttendeeField.EMAIL)) {
            attendee.setEMail("testAttendee" + userId + "@test.org");
        }
        return attendee;
    }

    /**
     * Get a {@link List} of random {@link Attendee}s with full set of {@link AttendeeField}s
     *
     * @param contextId The identifier of the context
     * @param size How many attendees should be created
     * @return The attendees
     */
    public static List<Attendee> getAttendees(int contextId, int size) {
        return getAttendees(contextId, size, (AttendeeField[]) null);
    }

    /**
     * Create an {@link Attendee} with random data that is identified as external User.
     *
     * @param contextId The identifier of the context
     * @param attendeeFields <code>null</code> to fill all fields, otherwise the desired {@link AttendeeField}s
     * @return An {@link Attendee}
     */
    public static Attendee createExternalAttendee(int contextId, AttendeeField... attendeeFields) {
        Attendee attendee = createAttendee(contextId, attendeeFields);
        attendee.setEntity(0);
        attendee.setUri(CalendarUtils.getURI(attendee.getEMail()));
        return attendee;
    }

    /**
     * Create an {@link Attendee} with random data that is identified as internal group
     *
     * @param contextId The identifier of the context
     * @param attendeeFields <code>null</code> to fill all fields, otherwise the desired {@link AttendeeField}s
     * @return An {@link Attendee}
     */
    public static Attendee createGroup(int contextId, AttendeeField... attendeeFields) {
        Attendee group = createAttendee(contextId, attendeeFields);
        group.setCuType(CalendarUserType.GROUP);
        group.setEntity(0);
        return group;
    }

    /**
     * Create an {@link Attendee} with random data that is identified as resource
     *
     * @param contextId The identifier of the context
     * @param attendeeFields <code>null</code> to fill all fields, otherwise the desired {@link AttendeeField}s
     * @return An {@link Attendee}
     */
    public static Attendee createResource(int contextId, AttendeeField... attendeeFields) {
        Attendee resource = createAttendee(contextId, attendeeFields);
        resource.setCuType(CalendarUserType.RESOURCE);
        return resource;
    }

    /**
     * Get a {@link List} of random {@link Attendee}s
     *
     * @param contextId The identifier of the context
     * @param size How many attendees should be created
     * @param fields The desired {@link AttendeeField}s to load
     * @return The attendees
     */
    public static List<Attendee> getAttendees(int contextId, int size, AttendeeField... fields) {
        List<Attendee> attendees = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            attendees.add(createAttendee(contextId, fields));
        }
        return attendees;
    }

    public static Event createEvent(int contextId, EventField... eventFields) {
        long currentTimeMillis = System.currentTimeMillis();
        int uid = randomInt();
        String uniqueId = String.valueOf(uid);

        Attendee owner = createAttendee(contextId, (AttendeeField[]) null);
        Event event = new Event();

        if (null == eventFields || containsField(eventFields, EventField.ID)) {
            event.setId(uniqueId);
        }
        if (null == eventFields || containsField(eventFields, EventField.UID)) {
            event.setUid(uniqueId);
        }
        if (null == eventFields || containsField(eventFields, EventField.FOLDER_ID)) {
            event.setFolderId("222");
        }
        if (null == eventFields || containsField(eventFields, EventField.ATTENDEES)) {
            List<Attendee> attendees = getAttendees(contextId, 3);
            attendees.add(owner);
            event.setAttendees(attendees);
        }
        if (null == eventFields || containsField(eventFields, EventField.SUMMARY)) {
            event.setSummary("Random event summury of event NO." + uniqueId);
        }
        if (null == eventFields || containsField(eventFields, EventField.CALENDAR_USER)) {
            event.setCalendarUser(owner);
        }
        if (null == eventFields || containsField(eventFields, EventField.CREATED_BY)) {
            event.setCreatedBy(owner);
        }
        if (null == eventFields || containsField(eventFields, EventField.MODIFIED_BY)) {
            event.setModifiedBy(owner);
        }
        if (null == eventFields || containsField(eventFields, EventField.ORGANIZER)) {
            Organizer organizer = new Organizer();
            organizer.setCn(owner.getCn());
            organizer.setEMail(owner.getEMail());
            organizer.setEntity(owner.getEntity());
            organizer.setUri(owner.getUri());
            event.setOrganizer(organizer);
        }
        if (null == eventFields || containsField(eventFields, EventField.TIMESTAMP)) {
            event.setTimestamp(currentTimeMillis);
        }
        if (null == eventFields || containsField(eventFields, EventField.START_DATE)) {
            event.setStartDate(new DateTime(currentTimeMillis + 100));
        }
        if (null == eventFields || containsField(eventFields, EventField.END_DATE)) {
            event.setEndDate(new DateTime(currentTimeMillis + TimeUnit.HOURS.toMillis(12)));
        }
        // TODO rest of fields
        return event;
    }

    /**
     * Converts {@link Attendee}s to {@link MockUser}s
     *
     * @param attendees The attendees to convert
     * @return A {@link List} of users with id and mail set
     */
    public static List<MockUser> convertToUser(List<Attendee> attendees) {
        List<MockUser> users = new LinkedList<>();
        for (Attendee attendee : attendees) {
            if (CalendarUtils.isInternalUser(attendee)) {
                MockUser user = convertToUser(attendee);
                users.add(user);
            }
        }
        return users;
    }

    /**
     * Converts {@link CalendarUser} to {@link MockUser}
     *
     * @param calendarUser The calendar user to convert
     * @return A user with id and mail set
     */
    public static MockUser convertToUser(CalendarUser calendarUser) {
        MockUser user = new MockUser(calendarUser.getEntity());
        user.setMail(calendarUser.getEMail());
        user.setTimeZone("");
        user.setLocale(Locale.CANADA_FRENCH);
        if (Strings.isNotEmpty(calendarUser.getUri()) && calendarUser.getUri().toLowerCase().startsWith("mailto:")) {
            user.setAliases(new String[] { CalendarUtils.extractEMailAddress(calendarUser.getUri()) });
        }
        return user;
    }

    /**
     * Converts an attendee to a {@link Resource}
     *
     * @param resource The attendee to convert
     * @return a {@link Resource}
     */
    public static Resource convertToResource(Attendee resource) {
        Resource r = new Resource();
        r.setAvailable(true);
        r.setDescription(resource.getComment());
        r.setDisplayName(resource.getCn());
        r.setIdentifier(resource.getEntity());
        r.setLastModified(System.currentTimeMillis());
        r.setMail(resource.getEMail());
        r.setSimpleName("Resource No." + String.valueOf(resource.getEntity()));

        return r;
    }

    public static CalendarSession createSession(final int contextId, final int userId) {
        return new CalendarSession() {

            @Override
            public <T> CalendarParameters set(String parameter, T value) {
                return null;
            }

            @Override
            public <T> T get(String parameter, Class<T> clazz, T defaultValue) {
                return null;
            }

            @Override
            public <T> T get(String parameter, Class<T> clazz) {
                return null;
            }

            @Override
            public Set<Entry<String, Object>> entrySet() {
                return null;
            }

            @Override
            public boolean contains(String parameter) {
                return false;
            }

            @Override
            public List<OXException> getWarnings() {
                return null;
            }

            @Override
            public CalendarUtilities getUtilities() {
                return null;
            }

            @Override
            public int getUserId() {
                return userId;
            }

            @Override
            public Session getSession() {
                return null;
            }

            @Override
            public RecurrenceService getRecurrenceService() {
                return null;
            }

            @Override
            public HostData getHostData() {
                return null;
            }

            @Override
            public FreeBusyService getFreeBusyService() {
                return null;
            }

            @Override
            public EntityResolver getEntityResolver() {
                return null;
            }

            @Override
            public int getContextId() {
                return contextId;
            }

            @Override
            public CalendarConfig getConfig() {
                return null;
            }

            @Override
            public CalendarService getCalendarService() {
                return null;
            }

            @Override
            public void addWarning(OXException warning) {
                // empty
            }
        };
    }

    /**
     * Generates a random integer
     *
     * @return An integer
     */
    private static int randomInt() {
        return 1337 + (int) (Math.random() * 47111337);
    }

    /**
     * Gets a value indicating whether the supplied array contains an element that is "equal to" the supplied one.
     * Copied from com.openexchange.tools.arrays.Arrays
     *
     * @param array The array to check
     * @param t The element to lookup
     * @return <code>true</code> if an equal element was found, <code>false</code>, otherwise
     */
    private static <T> boolean containsField(T[] array, T t) {
        if (null != t) {
            if (null == array) {
                return false;
            }

            for (int i = array.length; i-- > 0;) {
                if (t.equals(array[i])) {
                    return true;
                }
            }
        }
        return false;
    }
}
