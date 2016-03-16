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

package com.openexchange.freebusy.test;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;
import junit.framework.TestCase;
import com.openexchange.freebusy.BusyStatus;
import com.openexchange.freebusy.FreeBusyData;
import com.openexchange.freebusy.FreeBusyInterval;
import com.openexchange.time.TimeTools;

/**
 * {@link NormalizeTest}
 *
 * Tests normaliziation of free/busy data.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class NormalizeTest extends TestCase {

    private FreeBusyData originalData;
    private FreeBusyData normalizedData;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTime(TimeTools.D("first day last month"));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        Date from = calendar.getTime();
        calendar.setTime(TimeTools.D("first day next month"));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        Date until = calendar.getTime();
        originalData = generateData(from, until, 500, 10, 500);
        normalizedData = new FreeBusyData(originalData.getParticipant(), originalData.getFrom(), originalData.getUntil());
        for (FreeBusyInterval interval : originalData.getIntervals()) {
            normalizedData.add(new FreeBusyInterval(interval.getStartTime(), interval.getEndTime(), interval));
        }
        normalizedData.normalize();
    }

    /**
     * Tests that each interval is within the data's boundaries.
     */
    public void testBoundaries() {
        Date from = normalizedData.getFrom();
        Date until = normalizedData.getUntil();
        for (FreeBusyInterval interval : normalizedData.getIntervals()) {
            assertFalse("Interval start-time outside data range", interval.getStartTime().before(from));
            assertFalse("Interval end-time outside data range", interval.getEndTime().after(until));
        }
    }

    /**
     * Tests that the intervals are sorted correctly.
     */
    public void testSorting() {
        for (int i = 0; i < normalizedData.getIntervals().size() - 1; i++) {
            FreeBusyInterval first = normalizedData.getIntervals().get(i);
            FreeBusyInterval second = normalizedData.getIntervals().get(i + 1);
            assertFalse("Intervals not sorted correctly", first.getStartTime().after(second.getStartTime()));
            if (first.getStartTime().equals(second.getStartTime())) {
                assertFalse("Intervals not sorted correctly", first.getEndTime().after(second.getEndTime()));
                if (first.getEndTime().equals(second.getEndTime())) {
                    assertTrue("Intervals not sorted correctly", first.getStatus().isMoreConflicting(second.getStatus()));
                }
            }
        }
    }

    /**
     * Tests that each original interval is also present in the normalized data with an equal or more conflicting busy status.
     */
    public void testPresence() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        for (FreeBusyInterval originalInterval : originalData.getIntervals()) {
            calendar.setTime(originalInterval.getStartTime().before(normalizedData.getFrom()) ?
                normalizedData.getFrom() : originalInterval.getStartTime());
            calendar.add(Calendar.MINUTE, 1);
            while (calendar.getTime().before(originalInterval.getEndTime()) && calendar.getTime().before(normalizedData.getUntil())) {
                BusyStatus status = getStatusAt(calendar.getTime(), normalizedData);
                assertNotNull("No busy status for original interval", status);
                assertFalse("Busy status is less conflicting than original status",
                    originalInterval.getStatus().isMoreConflicting(status));
                calendar.add(Calendar.MINUTE, 1);
            }
        }
    }

    /**
     * Tests that no interval overlaps another one in the normalized data
     */
    public void testOverlapping() {
        for (int i = 0; i < normalizedData.getIntervals().size() - 1; i++) {
            FreeBusyInterval first = normalizedData.getIntervals().get(i);
            FreeBusyInterval second = normalizedData.getIntervals().get(i + 1);
            assertFalse("Interval overlaps another", first.getEndTime().after(second.getStartTime()));
        }
    }

    private static BusyStatus getStatusAt(Date when, FreeBusyData data) {
        for (FreeBusyInterval interval : data.getIntervals()) {
            if (false == interval.getEndTime().before(when) && false == interval.getStartTime().after(when)) {
                return interval.getStatus();
            }
        }
        return null;
    }

    private static FreeBusyData generateData(Date from, Date until, int intervals, int minIntervalMinutes, int maxIntervalMinutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        FreeBusyData freeBusyData = new FreeBusyData("horst.tester@example.com", from, until);
        Random random = new Random();
        int daysBetween = getDaysBetween(from, until);
        for (int i = 0; i < intervals; i++) {
            BusyStatus status = BusyStatus.valueOf(random.nextInt(5));
            calendar.setTime(from);
            calendar.add(Calendar.DATE, random.nextInt(daysBetween));
            calendar.add(Calendar.HOUR_OF_DAY, random.nextInt(24));
            calendar.add(Calendar.MINUTE, random.nextInt(60));
            Date start = calendar.getTime();
            calendar.add(Calendar.MINUTE, minIntervalMinutes + random.nextInt(maxIntervalMinutes - minIntervalMinutes));
            Date end = calendar.getTime();
            FreeBusyInterval interval = new FreeBusyInterval(start, end, status);
            interval.setObjectID(String.valueOf(random.nextInt()));
            freeBusyData.add(interval);
        }
        return freeBusyData;
    }

    private static int getDaysBetween(Date date1, Date date2) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTime(date1);
        int days = 0;
        while (calendar.getTime().before(date2)) {
            calendar.add(Calendar.DATE, 1);
            days++;
        }
        return days;
    }

}
