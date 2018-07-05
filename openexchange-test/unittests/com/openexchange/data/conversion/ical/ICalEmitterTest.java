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

import static com.openexchange.data.conversion.ical.Assert.assertNoProperty;
import static com.openexchange.data.conversion.ical.Assert.assertProperty;
import static com.openexchange.data.conversion.ical.Assert.assertStandardTaskFields;
import static com.openexchange.groupware.calendar.tools.CommonAppointments.D;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.data.conversion.ical.ical4j.ICal4JEmitter;
import com.openexchange.data.conversion.ical.ical4j.internal.UserResolver;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.MockUserLookup;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.notify.NotificationConfig;
import com.openexchange.groupware.notify.NotificationConfig.NotificationProperty;
import com.openexchange.groupware.tasks.Task;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class ICalEmitterTest {

    private ICal4JEmitter emitter;
    private MockUserLookup users;
    private UserResolver oldUserResolver;

    // --------------------------------- Tasks ---------------------------------

    /**
     * Tests task emitter for title and note.
     */
    @Test
    public void testTaskSimpleFields() throws IOException {
        final Task task = new Task();
        task.setTitle("The Title");
        task.setNote("The Note");
        task.setCategories("cat1, cat2, cat3");
        task.setPercentComplete(23);

        final ICalFile ical = serialize(task);
        assertProperty(ical, "SUMMARY", "The Title");
        assertProperty(ical, "DESCRIPTION", "The Note");
        assertProperty(ical, "CATEGORIES", "cat1, cat2, cat3");
        assertProperty(ical, "PERCENT-COMPLETE", "23");
    }

    @Test
    public void testTaskCompleted() throws IOException {
        final Task task = new Task();
        task.setTitle("The Title");
        task.setDateCompleted(D("24/02/2009 10:00"));
        task.setPercentComplete(100);
        task.setStatus(Task.DONE);

        final ICalFile ical = serialize(task);
        assertProperty(ical, "SUMMARY", "The Title");
        assertProperty(ical, "COMPLETED", "20090224T100000Z");
        assertProperty(ical, "PERCENT-COMPLETE", "100");
    }

    @Test
    public void testTaskCategoriesMayBeNullOrUnset() throws Exception {
        final Task task = new Task();
        ICalFile ical = serialize(task);

        assertNoProperty(ical, "CATEGORIES");

        task.setCategories(null);
        ical = serialize(task);

        assertNoProperty(ical, "CATEGORIES");
    }

    @Test
    public void testTaskCreated() throws IOException {
        final Task task = new Task();
        task.setCreationDate(D("24/02/1981 10:00"));

        final ICalFile ical = serialize(task);

        assertProperty(ical, "CREATED", "19810224T100000Z");
    }

    @Test
    public void testTaskLastModified() throws IOException {
        final Task task = new Task();
        task.setLastModified(D("24/02/1981 10:00"));

        final ICalFile ical = serialize(task);

        assertProperty(ical, "LAST-MODIFIED", "19810224T100000Z");
    }

    @Test
    public void testTaskDateFields() throws IOException {
        final Task task = new Task();
        final Date start = D("13/07/1976 15:00");
        final Date end = D("13/07/1976 17:00");
        task.setStartDate(start);
        task.setEndDate(end);
        final ICalFile ical = serialize(task);
        assertStandardTaskFields(ical, start, end);
    }

    @Test
    public void testTaskUid() throws IOException {
        final Task task = new Task();
        task.setUid("nexn787n478478onzwo87nwiuhi");

        final ICalFile ical = serialize(task);

        assertProperty(ical, "UID", "nexn787n478478onzwo87nwiuhi");
    }

    public void no_testTaskOrganizer() throws IOException {
        final Task task = new Task();
        task.setOrganizer("organizer");

        final ICalFile ical = serialize(task);

        assertProperty(ical, "ORGANIZER", "mailto:organizer");
    }

    // SetUp

    @Before
    public void setUp() throws Exception {
        Init.startServer();
        NotificationConfig.override(NotificationProperty.FROM_SOURCE, "internal");
        users = new MockUserLookup();
        emitter = new ICal4JEmitter();
        oldUserResolver = com.openexchange.data.conversion.ical.ical4j.internal.calendar.Participants.userResolver;
        com.openexchange.data.conversion.ical.ical4j.internal.calendar.Participants.userResolver = new UserResolver() {

            @Override
            public List<User> findUsers(final List<String> mails, final Context ctx) {
                final List<User> found = new LinkedList<User>();
                for (final String mail : mails) {
                    final User user = ICalEmitterTest.this.users.getUserByMail(mail);
                    if (user != null) {
                        found.add(user);
                    }
                }

                return found;
            }

            @Override
            public User loadUser(final int userId, final Context ctx) throws OXException {
                return ICalEmitterTest.this.users.getUser(userId);
            }
        };
    }

    @After
    public void tearDown() {
        com.openexchange.data.conversion.ical.ical4j.internal.calendar.Participants.userResolver = oldUserResolver;
        NotificationConfig.forgetOverrides();
    }

    /**
     * Serializes a task.
     * 
     * @param task task to serialize.
     * @return an iCal file.
     * @throws IOException if serialization fails.
     */
    private ICalFile serialize(final Task task) throws IOException {
        return new ICalFile(new StringReader(emitter.writeTasks(Arrays.asList(task), new ArrayList<ConversionError>(), new ArrayList<ConversionWarning>(), null)));
    }
}
