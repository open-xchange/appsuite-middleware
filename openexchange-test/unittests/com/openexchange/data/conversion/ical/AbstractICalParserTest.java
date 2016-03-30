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

package com.openexchange.data.conversion.ical;

import static com.openexchange.groupware.calendar.tools.CommonAppointments.D;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import junit.framework.TestCase;
import com.openexchange.data.conversion.ical.ical4j.ICal4JParser;
import com.openexchange.data.conversion.ical.ical4j.internal.ResourceResolver;
import com.openexchange.data.conversion.ical.ical4j.internal.UserResolver;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Participants;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.ldap.MockUserLookup;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.resource.Resource;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public abstract class AbstractICalParserTest extends TestCase {

    protected ICALFixtures fixtures;
    protected ICalParser parser;
    protected MockUserLookup users;
    protected ResourceResolver oldResourceResolver;
    protected UserResolver oldUserResolver;


    @Override
    protected void setUp() throws Exception {
        fixtures = new ICALFixtures();
        users = new MockUserLookup();
        parser = new ICal4JParser();
        oldUserResolver = Participants.userResolver;
        Participants.userResolver = new UserResolver(){
            @Override
            public List<User> findUsers(final List<String> mails, final Context ctx) {
                final List<User> found = new LinkedList<User>();
                for(final String mail : mails) {
                    final User user = AbstractICalParserTest.this.users.getUserByMail(mail);
                    if(user != null) {
                        found.add( user );
                    }
                }

                return found;
            }

            @Override
            public User loadUser(final int userId, final Context ctx) throws OXException {
                return AbstractICalParserTest.this.users.getUser(userId);
            }
        };
        oldResourceResolver = Participants.resourceResolver;
        Participants.resourceResolver = new ResourceResolver() {
            private final List<Resource> resources = new ArrayList<Resource>() {{
                final Resource toaster = new Resource();
                toaster.setDisplayName("Toaster");
                toaster.setIdentifier(1);
                add(toaster);

                final Resource deflector = new Resource();
                deflector.setDisplayName("Deflector");
                deflector.setIdentifier(2);
                add(deflector);

                final Resource subspaceAnomaly = new Resource();
                subspaceAnomaly.setDisplayName("Subspace Anomaly");
                subspaceAnomaly.setIdentifier(3);
                add(subspaceAnomaly);
            }};

            @Override
            public List<Resource> find(final List<String> names, final Context ctx)
                throws OXException, OXException {
                final List<Resource> retval = new ArrayList<Resource>();
                for(final String name : names) {
                    for(final Resource resource : resources) {
                        if(resource.getDisplayName().equals(name)) {
                            retval.add(resource);
                        }
                    }
                }
                return retval;
            }
            @Override
            public Resource load(final int resourceId, final Context ctx)
                throws OXException, OXException {
                return null;
            }
        };
    }


    protected List<User> U(final int...ids) {
        final List<User> found = new LinkedList<User>();
        for(final int i : ids) {
            try {
                found.add( users.getUser(i) );
            } catch (final OXException e) {
                //IGNORE
            }
        }
        return found;
    }

    @Override
    protected void tearDown() throws Exception {
        Participants.userResolver = oldUserResolver;
        Participants.resourceResolver = oldResourceResolver;
        super.tearDown();
    }

    // single appointment
    protected CalendarDataObject parseAppointment(final String icalText, final TimeZone defaultTZ) throws ConversionError {
    	List<CalendarDataObject> appointments = parseAppointments(icalText, defaultTZ);
    	if(appointments.size() == 0) {
            return null;
        }
    	return appointments.get(0);
    }

    protected CalendarDataObject parseAppointment(final String icalText) throws ConversionError {
        return parseAppointment(icalText, TimeZone.getDefault());
    }

    //multiple appointments
    protected List<CalendarDataObject> parseAppointments(final String icalText) throws ConversionError {
        return parseAppointments(icalText, TimeZone.getDefault());
    }

    protected List<CalendarDataObject> parseAppointments(final String icalText, final TimeZone defaultTZ) throws ConversionError {
        return parser.parseAppointments(icalText, defaultTZ, new ContextImpl(23), new ArrayList<ConversionError>() , new ArrayList<ConversionWarning>() );
    }


    //single task
    protected Task parseTask(final String icalText,  final TimeZone defaultTZ) throws ConversionError {
        return parseTasks(icalText,defaultTZ).get(0);
    }

    protected Task parseTask(final String icalText) throws ConversionError {
        return parseTasks(icalText).get(0);
    }

    //multiple tasks
    protected List<Task> parseTasks(final String icalText,  final TimeZone defaultTZ) throws ConversionError {
        return parser.parseTasks(icalText, defaultTZ, new ContextImpl(23), new ArrayList<ConversionError>() , new ArrayList<ConversionWarning>());
    }

    protected List<Task> parseTasks(final String icalText) throws ConversionError {
        return parseTasks(icalText, TimeZone.getDefault());
    }



    protected Appointment appointmentWithRecurrence(final String recurrence, final Date start, final Date end) throws ConversionError {

        final TimeZone utc = TimeZone.getTimeZone("UTC");

        final String icalText = fixtures.veventWithSimpleProperties(start, end, "RRULE", recurrence);
        final Appointment appointment = parseAppointment(icalText, utc);

        return appointment;
    }

    protected Task taskWithRecurrence(final String recurrence, final Date start, final Date end) throws ConversionError {

        final TimeZone utc = TimeZone.getTimeZone("UTC");

        final String icalText = fixtures.vtodoWithSimpleProperties(start, end, "RRULE", recurrence);
        final Task task = parseTask(icalText, utc);

        return task;
    }

    protected void assertWarningWhenParsingAppointment(final String icalText, final String warning) throws ConversionError {
        final ArrayList<ConversionError> errors = new ArrayList<ConversionError>();
        final ArrayList<ConversionWarning> warnings = new ArrayList<ConversionWarning>();
        final List<CalendarDataObject> result = parser.parseAppointments(icalText, TimeZone.getTimeZone("UTC"), new ContextImpl(23), errors, warnings);

        assertTrue(0 != result.size()); // Warnings don't abort parsing of the object
        assertEquals(1, warnings.size());
        assertEquals(warning, warnings.get(0).getSoleMessage());
    }

    protected void assertErrorWhenParsingAppointment(final String icalText, final String expectedError) throws ConversionError {
        final ArrayList<ConversionError> errors = new ArrayList<ConversionError>();
        final ArrayList<ConversionWarning> warnings = new ArrayList<ConversionWarning>();
        parser.parseAppointments(icalText, TimeZone.getTimeZone("UTC"), new ContextImpl(23), errors, warnings);
        assertEquals(1, errors.size());
        assertEquals(expectedError, errors.get(0).getSoleMessage());
    }

    protected void assertNothingHappensWhenParsingAppointment(final String icalText) throws ConversionError {
        final ArrayList<ConversionError> errors = new ArrayList<ConversionError>();
        final ArrayList<ConversionWarning> warnings = new ArrayList<ConversionWarning>();
        List<CalendarDataObject> parsed = parser.parseAppointments(icalText, TimeZone.getTimeZone("UTC"), new ContextImpl(23), errors, warnings);
        assertTrue(null == parsed || 0 == parsed.size());
        assertEquals(0, errors.size());
        assertEquals(0, warnings.size());
    }


    protected void warningOnAppRecurrence(final String recurrence, final String warning) throws ConversionError {
        final String icalText = fixtures.veventWithSimpleProperties(D("24/02/1981 10:00"), D("24/02/1981 12:00"), "RRULE", recurrence);
        assertWarningWhenParsingAppointment(icalText, warning);

    }
}
