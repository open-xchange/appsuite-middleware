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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.chronos.ical;

import static org.junit.Assert.assertTrue;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import org.dmfs.rfc5545.DateTime;
import org.junit.Test;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultRecurrenceId;
import com.openexchange.chronos.service.SimpleCollectionUpdate;

/**
 * {@link Bug64119Test}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class Bug64119Test extends ICalTest {

    @Test
    public void testEXDates() throws Exception {
        /*
         * prepare original event
         */
        Event storedEvent = new Event();
        storedEvent.setStartDate(new DateTime(TimeZone.getTimeZone("Europe/Berlin"), 1458745200000L));
        storedEvent.setEndDate(new DateTime(TimeZone.getTimeZone("Europe/Berlin"), 1458747000000L));
        storedEvent.setRecurrenceRule("FREQ=WEEKLY;INTERVAL=2;BYDAY=WE");
        SortedSet<RecurrenceId> deleteExceptionDates = new TreeSet<RecurrenceId>();
        deleteExceptionDates.add(new DefaultRecurrenceId("20170405T150000Z"));
        deleteExceptionDates.add(new DefaultRecurrenceId("20170503T150000Z"));
        deleteExceptionDates.add(new DefaultRecurrenceId("20170628T150000Z"));
        deleteExceptionDates.add(new DefaultRecurrenceId("20170809T150000Z"));
        deleteExceptionDates.add(new DefaultRecurrenceId("20171227T160000Z"));
        deleteExceptionDates.add(new DefaultRecurrenceId("20180418T150000Z"));
        storedEvent.setDeleteExceptionDates(deleteExceptionDates);
        /*
         * parse updated event
         */
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" + 
            "METHOD:REQUEST\r\n" + 
            "PRODID:Microsoft Exchange Server 2010\r\n" + 
            "VERSION:2.0\r\n" + 
            "BEGIN:VTIMEZONE\r\n" + 
            "TZID:Pacific Standard Time\r\n" + 
            "BEGIN:STANDARD\r\n" + 
            "DTSTART:16010101T020000\r\n" + 
            "TZOFFSETFROM:-0700\r\n" + 
            "TZOFFSETTO:-0800\r\n" + 
            "RRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=1SU;BYMONTH=11\r\n" + 
            "END:STANDARD\r\n" + 
            "BEGIN:DAYLIGHT\r\n" + 
            "DTSTART:16010101T020000\r\n" + 
            "TZOFFSETFROM:-0800\r\n" + 
            "TZOFFSETTO:-0700\r\n" + 
            "RRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=2SU;BYMONTH=3\r\n" + 
            "END:DAYLIGHT\r\n" + 
            "END:VTIMEZONE\r\n" + 
            "BEGIN:VEVENT\r\n" + 
            "RRULE:FREQ=WEEKLY;INTERVAL=2;BYDAY=WE;WKST=SU\r\n" + 
            "EXDATE;TZID=Pacific Standard Time:20160713T080000,20161102T080000,20161228T\r\n" + 
            " 080000,20170405T080000,20170503T080000,20170628T080000,20170809T080000,201\r\n" + 
            " 71227T080000,20180418T080000,20180627T080000,20181003T080000,20181226T0800\r\n" + 
            " 00\r\n" + 
            "UID:040000008200E00074C5B7101A82E00800000000C03062231C81D101000000000000000\r\n" + 
            " 010000000494852F6A4D8F04B8C791BBA23A12474\r\n" + 
            "SUMMARY;LANGUAGE=en-US:test\r\n" + 
            "DTSTART;TZID=Pacific Standard Time:20160323T080000\r\n" + 
            "DTEND;TZID=Pacific Standard Time:20160323T083000\r\n" + 
            "CLASS:PUBLIC\r\n" + 
            "PRIORITY:5\r\n" + 
            "DTSTAMP:20190201T224614Z\r\n" + 
            "TRANSP:OPAQUE\r\n" + 
            "STATUS:CONFIRMED\r\n" + 
            "SEQUENCE:22\r\n" + 
            "END:VEVENT\r\n" + 
            "END:VCALENDAR\r\n" 
        ; // @formatter:on
        Event eventUpdate = importEvent(iCal);
        /*
         * check that all original deletes exception dates are matched
         */
        SimpleCollectionUpdate<RecurrenceId> exceptionDateUpdates = CalendarUtils.getExceptionDateUpdates(
            storedEvent.getDeleteExceptionDates(), eventUpdate.getDeleteExceptionDates());
        assertTrue("Unexpected removal of exception date", exceptionDateUpdates.getRemovedItems().isEmpty());
    }

}
