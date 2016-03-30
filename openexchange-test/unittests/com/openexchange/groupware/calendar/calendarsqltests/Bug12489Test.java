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

package com.openexchange.groupware.calendar.calendarsqltests;

import java.util.Date;
import com.openexchange.groupware.calendar.CalendarDataObject;


public class Bug12489Test extends CalendarSqlTest {
    /**
     * Test for <a href="http://bugs.open-xchange.com/cgi-bin/bugzilla/show_bug.cgi?id=12489">bug #12489</a>:<br>
     * <b>Error message thrown during change of recurring appointment</b>
     */
    public void testTitleUpdateOfRecAppWithException() throws Exception {
        // create monthly recurring appointment
        /*-
         * {"alarm":"-1","days":32,"title":"BlubberFOo","shown_as":1,"end_date":1226048400000,"note":"",
         * "interval":1,"recurrence_type":3,"folder_id":"116","day_in_month":1,"private_flag":false,"occurrences":10,
         * "start_date":1226044800000,"full_time":false}
         */
        final String oldTitle = "testTitleUpdateOfRecAppWithException";
        final CalendarDataObject master = appointments.buildBasicAppointment(new Date(1226044800000L), new Date(1226048400000L));
        master.setTitle(oldTitle);
        master.setRecurrenceType(CalendarDataObject.MONTHLY);
        master.setInterval(1);
        master.setDays(32);
        master.setDayInMonth(1);
        master.setOccurrence(10);
        // Save
        appointments.save(master);
        clean.add(master);
        // Reload master to get real start/end
        final Date masterStart;
        final Date masterEnd;
        {
            final CalendarDataObject tmp = appointments.reload(master);
            masterStart = tmp.getStartDate();
            masterEnd = tmp.getEndDate();
        }
        // Create change exception
        /*-
         * {"alarm":"-1","recurrence_position":1,"categories":"","end_date":1226055600000,"note":null,"recurrence_type":0,
         * "until":null,"folder_id":"116","private_flag":false,"notification":true,"start_date":1226052000000,"location":"",
         * "full_time":false}
         */
        final CalendarDataObject exception = appointments.createIdentifyingCopy(master);
        exception.setRecurrencePosition(1);
        exception.setStartDate(new Date(1226052000000L));
        exception.setEndDate(new Date(1226052000000L));
        exception.setIgnoreConflicts(true);
        // exception.setTimezone("utc");
        appointments.save(exception);
        clean.add(exception);
        // Now try to change master's title only: error-prone GUI request which
        // contains until
        /*-
         * {"alarm":"-1","until":null,"folder_id":"116","private_flag":false,"title":"BlubberFOo Renamed",
         * "notification":true,"categories":"","end_date":1226048400000,"location":"","note":null,
         * "start_date":1226044800000,"full_time":false}
         */
        final String newTitle = "testTitleUpdateOfRecAppWithException RENAMED";
        final CalendarDataObject updateMaster = appointments.createIdentifyingCopy(master);
        updateMaster.setTitle(newTitle);
        appointments.save(updateMaster);
        // Reload and check name for master
        final CalendarDataObject reloadedMaster = appointments.reload(updateMaster);
        assertEquals("Master's start date changed although only its title was updated", masterStart, reloadedMaster.getStartDate());
        assertEquals("Master's end date changed although only its title was updated", masterEnd, reloadedMaster.getEndDate());
        assertEquals("Master's title did not change", newTitle, reloadedMaster.getTitle());
        // Reload and check exception
        final CalendarDataObject reloadedException = appointments.reload(exception);
        assertEquals("Change-exception's title changed", oldTitle, reloadedException.getTitle());
        assertEquals("Change-exception's start changed", new Date(1226052000000L), reloadedException.getStartDate());
        assertEquals("Change-exception's end changed", new Date(1226052000000L), reloadedException.getEndDate());
    }
}
