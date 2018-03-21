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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.chronos.recurrence;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import org.dmfs.rfc5545.DateTime;
import org.junit.After;
import org.junit.Before;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.common.DefaultRecurrenceId;
import com.openexchange.chronos.recurrence.service.RecurrenceServiceImpl;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.time.TimeTools;

/**
 * {@link RecurrenceServiceTest}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public abstract class RecurrenceServiceTest {

    protected RecurrenceService service;
    protected String timeZone;

    public RecurrenceServiceTest() {}

    public RecurrenceServiceTest(String timeZone) {
        this.timeZone = timeZone;
    }

    @Before
    public void setUp() {
        service = new RecurrenceServiceImpl(new TestRecurrenceConfig());
    }

    @After
    public void tearDown() {}

    protected void compareInstanceWithMaster(Event master, Event instance, Date start, Date end) {
        assertNotNull("Master must not be null.", master);
        assertNotNull("Instance must not be null", instance);
        Event clone = clone(master);

        instance = clone(instance);
        instance.removeRecurrenceId();
        instance.removeRecurrenceRule();

        clone.removeId();
        clone.removeRecurrenceRule();
        clone.removeDeleteExceptionDates();
        clone.setStartDate(DT(start, clone.getStartDate().getTimeZone(), clone.getStartDate().isAllDay()));
        clone.setEndDate(DT(end, clone.getEndDate().getTimeZone(), clone.getEndDate().isAllDay()));

        boolean equals = equals(clone, instance);
        assertTrue("Not equal.", equals);
    }

    protected void compareChangeExceptionWithMaster(Event master, Event instance, Date recurrenceId, Date start, Date end) {
        assertNotNull("Master must not be null.", master);
        assertNotNull("Instance must not be null", instance);
        Event clone = clone(master);

        clone.removeId();
        clone.removeRecurrenceRule();
        clone.removeDeleteExceptionDates();
        clone.setRecurrenceId(new DefaultRecurrenceId(DT(recurrenceId, master.getStartDate().getTimeZone(), master.getStartDate().isAllDay())));
        clone.setStartDate(DT(start, clone.getStartDate().getTimeZone(), clone.getStartDate().isAllDay()));
        clone.setEndDate(DT(end, clone.getEndDate().getTimeZone(), clone.getEndDate().isAllDay()));

        boolean equals = equals(clone, instance);
        assertTrue("Not equal.", equals);
    }

    protected void compareFullTimeChangeExceptionWithMaster(Event master, Event instance, DateTime recurrenceId, DateTime start, DateTime end) {
        assertNotNull("Master must not be null.", master);
        assertNotNull("Instance must not be null", instance);
        Event clone = clone(master);

        clone.removeId();
        clone.removeRecurrenceRule();
        clone.removeDeleteExceptionDates();
        clone.setRecurrenceId(new DefaultRecurrenceId(recurrenceId));
        clone.setStartDate(start);
        clone.setEndDate(end);

        boolean equals = equals(clone, instance);
        assertTrue("Not equal.", equals);
    }

    protected void compareChangeExceptionWithFullTimeMaster(Event master, Event instance, DateTime recurrenceId, DateTime start, DateTime end) {
        assertNotNull("Master must not be null.", master);
        assertNotNull("Instance must not be null", instance);
        Event clone = clone(master);

        clone.removeId();
        clone.removeRecurrenceRule();
        clone.removeDeleteExceptionDates();
        clone.setRecurrenceId(new DefaultRecurrenceId(recurrenceId));
        clone.setStartDate(start);
        clone.setEndDate(end);

        boolean equals = equals(clone, instance);
        assertTrue("Not equal.", equals);
    }

    protected String getUntilZulu(Calendar c) {
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        String month = Integer.toString(c.get(Calendar.MONTH) + 1);
        String year = Integer.toString(c.get(Calendar.YEAR));
        String dayOfMonth = Integer.toString(c.get(Calendar.DAY_OF_MONTH));
        if (dayOfMonth.length() == 1) {
            dayOfMonth = "0" + dayOfMonth;
        }
        String hourOfDay = Integer.toString(c.get(Calendar.HOUR_OF_DAY));
        if (hourOfDay.length() == 1) {
            hourOfDay = "0" + hourOfDay;
        }
        String minute = Integer.toString(c.get(Calendar.MINUTE));
        if (minute.length() == 1) {
            minute = "0" + minute;
        }
        String second = Integer.toString(c.get(Calendar.SECOND));
        if (second.length() == 1) {
            second = "0" + second;
        }
        return "" + year + month + dayOfMonth + "T" + hourOfDay + minute + second + "Z";
    }

    protected Calendar getCal(String date) {
        Calendar retval = GregorianCalendar.getInstance(TimeZone.getTimeZone(timeZone));
        retval.setTime(TimeTools.D(date, TimeZone.getTimeZone(timeZone)));
        return retval;
    }

    protected Event getInstance(Event master, Date recurrenceId, Date start, Date end) {
        Event instance = clone(master);
        instance.removeId();
        instance.removeRecurrenceRule();
        instance.removeDeleteExceptionDates();
        instance.setRecurrenceId(new DefaultRecurrenceId(DT(recurrenceId, master.getStartDate().getTimeZone(), master.getStartDate().isAllDay())));
        instance.setStartDate(DT(start, instance.getStartDate().getTimeZone(), instance.getStartDate().isAllDay()));
        instance.setEndDate(DT(end, instance.getEndDate().getTimeZone(), instance.getEndDate().isAllDay()));
        return instance;
    }

    protected boolean equals(Event event, Event other) {
        if (event == other)
            return true;
        if (event == null)
            return null == other;
        if (event.getAttachments() == null) {
            if (other.getAttachments() != null)
                return false;
        } else if (!event.getAttachments().equals(other.getAttachments()))
            return false;
        if (event.getAttendees() == null) {
            if (other.getAttendees() != null)
                return false;
        } else if (!event.getAttendees().equals(other.getAttendees()))
            return false;
        if (event.getCategories() == null) {
            if (other.getCategories() != null)
                return false;
        } else if (!event.getCategories().equals(other.getCategories()))
            return false;
        if (event.getClassification() != other.getClassification())
            return false;
        if (event.getColor() == null) {
            if (other.getColor() != null)
                return false;
        } else if (!event.getColor().equals(other.getColor()))
            return false;
        if (event.getCreated() == null) {
            if (other.getCreated() != null)
                return false;
        } else if (!event.getCreated().equals(other.getCreated()))
            return false;
        if (event.getCreatedBy() != other.getCreatedBy())
            return false;
        if (event.getDeleteExceptionDates() == null) {
            if (other.getDeleteExceptionDates() != null)
                return false;
        } else if (!event.getDeleteExceptionDates().equals(other.getDeleteExceptionDates()))
            return false;
        if (event.getDescription() == null) {
            if (other.getDescription() != null)
                return false;
        } else if (!event.getDescription().equals(other.getDescription()))
            return false;
        if (event.getEndDate() == null) {
            if (other.getEndDate() != null)
                return false;
        } else if (!event.getEndDate().equals(other.getEndDate()))
            return false;
        if (event.getFilename() == null) {
            if (other.getFilename() != null)
                return false;
        } else if (!event.getFilename().equals(other.getFilename()))
            return false;
        if (event.getId() != other.getId())
            return false;
        if (event.getLastModified() == null) {
            if (other.getLastModified() != null)
                return false;
        } else if (!event.getLastModified().equals(other.getLastModified()))
            return false;
        if (event.getLocation() == null) {
            if (other.getLocation() != null)
                return false;
        } else if (!event.getLocation().equals(other.getLocation()))
            return false;
        if (event.getModifiedBy() != other.getModifiedBy())
            return false;
        if (event.getOrganizer() == null) {
            if (other.getOrganizer() != null)
                return false;
        } else if (!event.getOrganizer().equals(other.getOrganizer()))
            return false;
        if (event.getFolderId() != other.getFolderId())
            return false;
        if (event.getRecurrenceId() == null) {
            if (other.getRecurrenceId() != null)
                return false;
        } else if (!event.getRecurrenceId().equals(other.getRecurrenceId()))
            return false;
        if (event.getRecurrenceRule() == null) {
            if (other.getRecurrenceRule() != null)
                return false;
        } else if (!event.getRecurrenceRule().equals(other.getRecurrenceRule()))
            return false;
        if (event.getSequence() != other.getSequence())
            return false;
        if (event.getSeriesId() != other.getSeriesId())
            return false;
        if (event.getStartDate() == null) {
            if (other.getStartDate() != null)
                return false;
        } else if (!event.getStartDate().equals(other.getStartDate()))
            return false;
        if (event.getStatus() != other.getStatus())
            return false;
        if (event.getSummary() == null) {
            if (other.getSummary() != null)
                return false;
        } else if (!event.getSummary().equals(other.getSummary()))
            return false;
        if (event.getTransp() == null) {
            if (other.getTransp() != null)
                return false;
        } else if (!event.getTransp().equals(other.getTransp()))
            return false;
        if (event.getUid() == null) {
            if (other.getUid() != null)
                return false;
        } else if (!event.getUid().equals(other.getUid()))
            return false;
        return true;
    }

    protected Event clone(Event event) {
        Event clone = new Event();
        if (event.containsAttachments()) {
            clone.setAttachments(cloneList(event.getAttachments()));
        }
        if (event.containsAttendees()) {
            clone.setAttendees(cloneList(event.getAttendees()));
        }
        if (event.containsAlarms()) {
            clone.setAlarms(cloneList(event.getAlarms()));
        }
        if (event.containsCategories()) {
            clone.setCategories(cloneList(event.getCategories()));
        }
        if (event.containsClassification()) {
            clone.setClassification(event.getClassification());
        }
        if (event.containsColor()) {
            clone.setColor(event.getColor());
        }
        if (event.containsCreated()) {
            clone.setCreated(event.getCreated());
        }
        if (event.containsCreatedBy()) {
            clone.setCreatedBy(event.getCreatedBy());
        }
        if (event.containsDeleteExceptionDates()) {
            clone.setDeleteExceptionDates(cloneSet(event.getDeleteExceptionDates()));
        }
        if (event.containsDescription()) {
            clone.setDescription(event.getDescription());
        }
        if (event.containsEndDate()) {
            clone.setEndDate(event.getEndDate());
        }
        if (event.containsFilename()) {
            clone.setFilename(event.getFilename());
        }
        if (event.containsFolderId()) {
            clone.setFolderId(event.getFolderId());
        }
        if (event.containsId()) {
            clone.setId(event.getId());
        }
        if (event.containsLastModified()) {
            clone.setLastModified(event.getLastModified());
        }
        if (event.containsLocation()) {
            clone.setLocation(event.getLocation());
        }
        if (event.containsModifiedBy()) {
            clone.setModifiedBy(event.getModifiedBy());
        }
        if (event.containsOrganizer()) {
            clone.setOrganizer(event.getOrganizer());
        }
        if (event.containsRecurrenceId()) {
            clone.setRecurrenceId(event.getRecurrenceId());
        }
        if (event.containsRecurrenceRule()) {
            clone.setRecurrenceRule(event.getRecurrenceRule());
        }
        if (event.containsSequence()) {
            clone.setSequence(event.getSequence());
        }
        if (event.containsSeriesId()) {
            clone.setSeriesId(event.getSeriesId());
        }
        if (event.containsStartDate()) {
            clone.setStartDate(event.getStartDate());
        }
        if (event.containsStatus()) {
            clone.setStatus(event.getStatus());
        }
        if (event.containsSummary()) {
            clone.setSummary(event.getSummary());
        }
        if (event.containsTransp()) {
            clone.setTransp(event.getTransp());
        }
        if (event.containsUid()) {
            clone.setUid(event.getUid());
        }
        return clone;
    }

    private <T> List<T> cloneList(List<T> list) {
        if (null == list) {
            return null;
        }
        List<T> retval = new ArrayList<T>();
        retval.addAll(list);
        return retval;
    }

    private <T> SortedSet<T> cloneSet(SortedSet<T> list) {
        if (null == list) {
            return null;
        }
        SortedSet<T> retval = new TreeSet<T>();
        retval.addAll(list);
        return retval;
    }

    protected static DateTime DT(String value, TimeZone timeZone, boolean allDay) {
        return DT(TimeTools.D(value, timeZone), timeZone, allDay);
    }

    protected static DateTime DT(Date date, TimeZone timeZone, boolean allDay) {
        if (allDay) {
            return new DateTime(date.getTime()).toAllDay();
        } else {
            return new DateTime(timeZone, date.getTime());
        }
    }

    protected static void setStartAndEndDates(Event event, Date startDate, Date endDate, boolean allDay, TimeZone timeZone) {
        if (allDay) {
            event.setStartDate(new DateTime(startDate.getTime()).toAllDay());
            event.setEndDate(new DateTime(endDate.getTime()).toAllDay());
        } else {
            event.setStartDate(new DateTime(timeZone, startDate.getTime()));
            event.setEndDate(new DateTime(timeZone, endDate.getTime()));
        }
    }

    protected static void setStartAndEndDates(Event event, String start, String end, boolean allDay, TimeZone timeZone) {
        event.setStartDate(DT(start, timeZone, allDay));
        event.setEndDate(DT(end, timeZone, allDay));
    }

}
