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

package com.openexchange.chronos;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.groupware.ldap.MockUser;
import edu.emory.mathcs.backport.java.util.Arrays;

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
        List<Enum<?>> fields = null == attendeeFields ? null : Arrays.asList(attendeeFields);

        int uid = randomInt();
        String userId = String.valueOf(uid);

        Attendee attendee = new Attendee();

        if (null == fields || containsField(fields, AttendeeField.URI)) {
            attendee.setUri(ResourceId.forUser(contextId, uid));
        }
        if (null == fields || containsField(fields, AttendeeField.CN)) {
            attendee.setCn("Test, Attendee No." + userId);
        }
        if (null == fields || containsField(fields, AttendeeField.ENTITY)) {
            attendee.setEntity(uid);
        }
        if (null == fields || containsField(fields, AttendeeField.SENT_BY)) {
            Attendee sendBy = createAttendee(contextId, AttendeeField.ENTITY, AttendeeField.CN, AttendeeField.EMAIL, AttendeeField.CU_TYPE);
            attendee.setSentBy(sendBy);
        }
        if (null == fields || containsField(fields, AttendeeField.CU_TYPE)) {
            attendee.setCuType(CalendarUserType.INDIVIDUAL);
        }
        if (null == fields || containsField(fields, AttendeeField.ROLE)) {
            attendee.setRole(ParticipantRole.REQ_PARTICIPANT);
        }
        if (null == fields || containsField(fields, AttendeeField.PARTSTAT)) {
            attendee.setPartStat(ParticipationStatus.ACCEPTED);
        }
        if (null == fields || containsField(fields, AttendeeField.COMMENT)) {
            attendee.setComment("Totaly random comment for attendee NO. " + userId);
        }
        if (null == fields || containsField(fields, AttendeeField.RSVP)) {
            attendee.setRsvp(Boolean.TRUE);
        }
        if (null == fields || containsField(fields, AttendeeField.FOLDER_ID)) {
            attendee.setFolderId(String.valueOf(uid + 50));
        }
        if (null == fields || containsField(fields, AttendeeField.MEMBER)) {
            attendee.setMember(Collections.emptyList());
        }
        if (null == fields || containsField(fields, AttendeeField.EMAIL)) {
            attendee.setEMail("testAttendee" + userId + "@test.org");
        }
        if (null == fields || containsField(fields, AttendeeField.EXTENDED_PROPERTIES)) {
            ExtendedProperties props = new ExtendedProperties();
            props.add(new ExtendedProperty(ExtendedProperty.CN, "Diffrent, CN"));
            props.add(new ExtendedProperty(ExtendedProperty.EMAIL, "alias" + userId + "@test.org"));
            props.add(new ExtendedProperty(ExtendedProperty.URI, "mailto:alias" + userId + "@test.org"));
            attendee.setExtendedProperties(props);
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
        return getAttendees(contextId, size, null);
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
        attendee.setEntity(-1 * attendee.getEntity());
        return attendee;
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
        List<Enum<?>> fields = null == eventFields ? null : Arrays.asList(eventFields);

        long currentTimeMillis = System.currentTimeMillis();
        int uid = randomInt();
        String uniqueId = String.valueOf(uid);

        Attendee owner = createAttendee(contextId, null);
        Event event = new Event();

        if (null == fields || containsField(fields, EventField.ID)) {
            event.setId(uniqueId);
        }
        if (null == fields || containsField(fields, EventField.UID)) {
            event.setUid(uniqueId);
        }
        if (null == fields || containsField(fields, EventField.FOLDER_ID)) {
            event.setFolderId("222");
        }
        if (null == fields || containsField(fields, EventField.ATTENDEES)) {
            List<Attendee> attendees = getAttendees(contextId, 3);
            attendees.add(owner);
            event.setAttendees(attendees);
        }
        if (null == fields || containsField(fields, EventField.SUMMARY)) {
            event.setSummary("Random event summury of event NO." + uniqueId);
        }
        if (null == fields || containsField(fields, EventField.CALENDAR_USER)) {
            event.setCalendarUser(owner);
        }
        if (null == fields || containsField(fields, EventField.CREATED_BY)) {
            event.setCreatedBy(owner);
        }
        if (null == fields || containsField(fields, EventField.MODIFIED_BY)) {
            event.setModifiedBy(owner);
        }
        if (null == fields || containsField(fields, EventField.ORGANIZER)) {
            Organizer organizer = new Organizer();
            organizer.setCn(owner.getCn());
            organizer.setEMail(owner.getEMail());
            organizer.setEntity(owner.getEntity());
            organizer.setUri(owner.getUri());
            event.setOrganizer(organizer);
        }
        if (null == fields || containsField(fields, EventField.TIMESTAMP)) {
            event.setTimestamp(currentTimeMillis);
        }
        if (null == fields || containsField(fields, EventField.START_DATE)) {
            event.setStartDate(new DateTime(currentTimeMillis + 100));
        }
        if (null == fields || containsField(fields, EventField.END_DATE)) {
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
            MockUser user = convertToUser(attendee);
            users.add(user);
        }
        return users;
    }

    /**
     * Converts {@link Attendee} to {@link MockUser}
     * 
     * @param attendee The attendee to convert
     * @return A user with id and mail set
     */
    public static MockUser convertToUser(Attendee attendee) {
        MockUser user = new MockUser(attendee.getEntity());
        user.setMail(attendee.getEMail());
        if (attendee.containsExtendedProperties()) {
            ExtendedProperties extendedProperties = attendee.getExtendedProperties();
            ExtendedProperty property = extendedProperties.get(ExtendedProperty.EMAIL);
            if (null != property) {
                String[] aliases = new String[] { property.getValue() };
                user.setAliases(aliases);
            }
            user.setTimeZone(new String());
        }
        return user;
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
        return user;
    }

    private static int randomInt() {
        return 1337 + (int) (Math.random() * 47111337);
    }

    private static boolean containsField(List<Enum<?>> fields, Enum<?> field) {
        return fields.stream().filter(f -> f.equals(field)).findFirst().isPresent();
    }
}
