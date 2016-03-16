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

package com.openexchange.groupware.calendar.calendarsqltests.untiltests;

import com.openexchange.exception.OXException;
import static com.openexchange.groupware.calendar.TimeTools.D;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.calendarsqltests.CalendarSqlTest;
import com.openexchange.groupware.container.Appointment;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public abstract class UntilTest extends CalendarSqlTest {

    protected static final int MONTHLY2 = 5;

    protected static final int YEARLY2 = 6;

    protected static final Date u = D("01.02.2016 00:00");

    protected static final int occ = 10;

    protected CalendarDataObject setUntil, setOccurrences, removeUntil;

    protected static final Map<Integer, Date> generatedUntils = new HashMap<Integer, Date>() {

        {
            put(Appointment.DAILY, D("10.02.2010 00:00"));
            put(Appointment.WEEKLY, D("05.04.2010 00:00"));
            put(Appointment.MONTHLY, D("01.11.2010 00:00"));
            put(Appointment.YEARLY, D("01.02.2019 00:00"));
            put(MONTHLY2, D("01.11.2010 00:00"));
            put(YEARLY2, D("04.02.2019 00:00"));
        }
    };

    @Override
    public void setUp() throws Exception {
        super.setUp();

        setUntil = new CalendarDataObject();
        setUntil.setUntil(u);
        setOccurrences = new CalendarDataObject();
        setOccurrences.setOccurrence(10);
        removeUntil = new CalendarDataObject();
        removeUntil.setUntil(null);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    protected CalendarDataObject createAppointment(int type, boolean occurrences, boolean until, boolean fulltime) throws Exception {
        CalendarDataObject retval = null;

        if (fulltime) {
            retval = getAppointment("01.02.2010 00:00", "02.02.2010 00:00");
            retval.setTitle("Until Test - fulltime");
            retval.setFullTime(true);
        } else {
            retval = getAppointment("01.02.2010 08:00", "01.02.2010 10:00");
            retval.setTitle("Until Test");
        }

        if (until) {
            retval.setUntil(u);
            retval.setTitle(retval.getTitle() + " - until (01.02.2016)");
        }

        if (occurrences) {
            retval.setOccurrence(occ);
            retval.setTitle(retval.getTitle() + " - occurrences (10)");
        }

        retval.setRecurrenceType(type);
        retval.setInterval(1);
        retval.setIgnoreConflicts(true);

        switch (type) {
        case Appointment.DAILY:
            retval.setTitle(retval.getTitle() + " - daily");
            break;
        case Appointment.WEEKLY:
            retval.setDays(Appointment.MONDAY);
            retval.setTitle(retval.getTitle() + " - weekly");
            break;
        case Appointment.MONTHLY:
            retval.setDayInMonth(1);
            retval.setTitle(retval.getTitle() + " - monthly");
            break;
        case MONTHLY2:
            retval.setRecurrenceType(Appointment.MONTHLY);
            retval.setDayInMonth(1);
            retval.setDays(Appointment.MONDAY);
            retval.setTitle(retval.getTitle() + " - monthly2");
            break;
        case Appointment.YEARLY:
            retval.setDayInMonth(1);
            retval.setMonth(Calendar.FEBRUARY);
            retval.setTitle(retval.getTitle() + " - yearly");
            break;
        case YEARLY2:
            retval.setRecurrenceType(Appointment.YEARLY);
            retval.setDayInMonth(1);
            retval.setDays(Appointment.MONDAY);
            retval.setMonth(Calendar.FEBRUARY);
            retval.setTitle(retval.getTitle() + " - yearly2");
            break;
        default:
            throw new Exception("Bad recurrence type.");
        }

        insertAppointment(retval);

        return retval;
    }

    private CalendarDataObject getAppointment(String start, String end) {
        return appointments.buildBasicAppointment(D(start), D(end));
    }

    private void insertAppointment(CalendarDataObject appointment) throws Exception {
        appointments.save(appointment);
        clean.add(appointment);
    }

    protected void updateAppointment(CalendarDataObject old, CalendarDataObject update) throws OXException {
        CalendarDataObject copy = appointments.createIdentifyingCopy(old);
        overrideRecurringInformation(old, copy);
        overrideRecurringInformation(update, copy);
        copy.setIgnoreConflicts(true);

        appointments.save(copy);
    }

    private void overrideRecurringInformation(CalendarDataObject source, CalendarDataObject target) {
        if (source.containsRecurrenceType()) {
            target.setRecurrenceType(source.getRecurrenceType());
        }

        if (source.containsOccurrence()) {
            target.setOccurrence(source.getOccurrence());
        } else {
            target.removeOccurrence();
        }

        if (source.containsUntil()) {
            target.setUntil(source.getUntil());
        } else {
            target.removeUntil();
        }

        List<Integer> fields = new ArrayList<Integer>() {

            {
                add(CalendarDataObject.DAYS);
                add(CalendarDataObject.DAY_IN_MONTH);
                add(CalendarDataObject.MONTH);
                add(CalendarDataObject.INTERVAL);
            }
        };

        for (int field : fields) {
            if (source.contains(field)) {
                target.set(field, source.get(field));
            }
        }
    }

    protected void checkUntilInformation(CalendarDataObject appointment, Date until, Integer occurrences) throws Exception {
        CalendarDataObject loaded = appointments.load(appointment.getObjectID(), appointment.getParentFolderID());

        if (until == null) {
            assertFalse("Unexpected Until value.", loaded.containsUntil());
        } else {
            assertTrue("Until not set.", loaded.containsUntil());
            assertEquals("Wrong Until value.", until, loaded.getUntil());
        }

        if (occurrences == null) {
            assertFalse("Unexpected Occurrences value.", loaded.containsOccurrence());
            assertEquals("Unexpected Occurrences value.", 0, loaded.getOccurrence());
        } else {
            assertTrue("Occurrences not set.", loaded.containsOccurrence());
            assertEquals("Wrong occurrences value.", occurrences.intValue(), loaded.getOccurrence());
        }
    }

    protected void withoutTest(int type, boolean fulltime) throws Exception {
        CalendarDataObject without = createAppointment(type, false, false, fulltime);
        checkUntilInformation(without, null, null);
        updateAppointment(without, setUntil);
        checkUntilInformation(without, u, null);
        updateAppointment(without, setOccurrences);
        checkUntilInformation(without, generatedUntils.get(type), occ);
        updateAppointment(without, removeUntil);
        checkUntilInformation(without, null, null);

        without = createAppointment(type, false, false, fulltime);
        updateAppointment(without, setOccurrences);
        checkUntilInformation(without, generatedUntils.get(type), occ);
        updateAppointment(without, setUntil);
        checkUntilInformation(without, u, null);
        updateAppointment(without, removeUntil);
        checkUntilInformation(without, null, null);
    }

    protected void withOccurrencesTest(int type, boolean fulltime) throws Exception {
        CalendarDataObject withOccurrences = createAppointment(type, true, false, fulltime);
        checkUntilInformation(withOccurrences, generatedUntils.get(type), occ);
        updateAppointment(withOccurrences, removeUntil);
        checkUntilInformation(withOccurrences, null, null);
        updateAppointment(withOccurrences, setUntil);
        checkUntilInformation(withOccurrences, u, null);
        updateAppointment(withOccurrences, setOccurrences);
        checkUntilInformation(withOccurrences, generatedUntils.get(type), occ);

        withOccurrences = createAppointment(type, true, false, fulltime);
        updateAppointment(withOccurrences, setUntil);
        checkUntilInformation(withOccurrences, u, null);
        updateAppointment(withOccurrences, removeUntil);
        checkUntilInformation(withOccurrences, null, null);
        updateAppointment(withOccurrences, setOccurrences);
        checkUntilInformation(withOccurrences, generatedUntils.get(type), occ);
    }

    protected void withUntilTest(int type, boolean fulltime) throws Exception {
        CalendarDataObject withUntil = createAppointment(type, false, true, fulltime);
        checkUntilInformation(withUntil, u, null);
        updateAppointment(withUntil, removeUntil);
        checkUntilInformation(withUntil, null, null);
        updateAppointment(withUntil, setOccurrences);
        checkUntilInformation(withUntil, generatedUntils.get(type), occ);
        updateAppointment(withUntil, setUntil);
        checkUntilInformation(withUntil, u, null);

        withUntil = createAppointment(type, false, true, fulltime);
        updateAppointment(withUntil, setOccurrences);
        checkUntilInformation(withUntil, generatedUntils.get(type), occ);
        updateAppointment(withUntil, removeUntil);
        checkUntilInformation(withUntil, null, null);
        updateAppointment(withUntil, setUntil);
        checkUntilInformation(withUntil, u, null);
    }
}
