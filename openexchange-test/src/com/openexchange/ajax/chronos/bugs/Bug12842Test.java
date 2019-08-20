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

package com.openexchange.ajax.chronos.bugs;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import org.junit.Test;
import com.openexchange.ajax.chronos.AbstractChronosTest;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.factory.EventFactory.RecurringFrequency;
import com.openexchange.ajax.chronos.factory.RRuleFactory;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.testing.httpclient.models.ChronosCalendarResultResponse;
import com.openexchange.testing.httpclient.models.DateTimeData;
import com.openexchange.testing.httpclient.models.EventData;

/**
 * 
 * {@link Bug12842Test}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.3
 */
public class Bug12842Test extends AbstractChronosTest {

    public Bug12842Test() {
        super();
    }

    /**
     * Tests if an event conflicts, if the new event is between the start and end date of an occurrence.
     *
     * @throws Throwable
     */
    @Test
    public void testConflictBetween() throws Throwable {
        /*-
         * Occurrence:     [--------]
         * Event:      [----]
         */
        rangeTest(8, 12, 9, 11, RecurringFrequency.DAILY, true);
        rangeTest(8, 12, 9, 11, RecurringFrequency.WEEKLY, true);
        rangeTest(8, 12, 9, 11, RecurringFrequency.MONTHLY, true);
        rangeTest(8, 12, 9, 11, RecurringFrequency.YEARLY, true);
    }

    /**
     * Tests, if an event conflicts, if the new event overlaps the start date of an occurrence, but not the end date.
     *
     * @throws Throwable
     */
    @Test
    public void testConflictOverlappingStartDate() throws Throwable {
        /*-
         * Occurrence:     [--------]
         * Event:  [----]
         */
        rangeTest(8, 12, 7, 9, RecurringFrequency.DAILY, true);
        rangeTest(8, 12, 7, 9, RecurringFrequency.WEEKLY, true);
        rangeTest(8, 12, 7, 9, RecurringFrequency.MONTHLY, true);
        rangeTest(8, 12, 7, 9, RecurringFrequency.YEARLY, true);
    }

    /**
     * Tests, if an event conflicts, if the new event overlaps the end date of an occurrence, but not the start date.
     *
     * @throws Throwable
     */
    @Test
    public void testConflictOverlappingEndDate() throws Throwable {
        /*-
         * Occurrence:     [--------]
         * Event:          [----]
         */
        rangeTest(8, 12, 11, 13, RecurringFrequency.DAILY, true);
        rangeTest(8, 12, 11, 13, RecurringFrequency.WEEKLY, true);
        rangeTest(8, 12, 11, 13, RecurringFrequency.MONTHLY, true);
        rangeTest(8, 12, 11, 13, RecurringFrequency.YEARLY, true);
    }

    /**
     * Tests, if an event conflicts, if the the new event overlaps the start and end date of an occurrence.
     *
     * @throws Throwable
     */
    @Test
    public void testConflictOverlapping() throws Throwable {
        /*-
         * Occurrence:     [--------]
         * Event:  [------------]
         */
        rangeTest(8, 12, 7, 13, RecurringFrequency.DAILY, true);
        rangeTest(8, 12, 7, 13, RecurringFrequency.WEEKLY, true);
        rangeTest(8, 12, 7, 13, RecurringFrequency.MONTHLY, true);
        rangeTest(8, 12, 7, 13, RecurringFrequency.YEARLY, true);
    }

    /**
     * Tests, if an event conflicts, if the the new event touches the start date of an occurrence.
     *
     * @throws Throwable
     */
    @Test
    public void testBoundaryStart() throws Throwable {
        /*-
         * Occurrence:     [--------]
         * Event: [--]
         */
        rangeTest(8, 12, 6, 8, RecurringFrequency.DAILY, false);
        rangeTest(8, 12, 6, 8, RecurringFrequency.WEEKLY, false);
        rangeTest(8, 12, 6, 8, RecurringFrequency.MONTHLY, false);
        rangeTest(8, 12, 6, 8, RecurringFrequency.YEARLY, false);
    }

    /**
     * Tests, if an event conflicts, if the the new event touches the end date of an occurrence.
     *
     * @throws Throwable
     */
    @Test
    public void testBoundaryEnd() throws Throwable {
        /*-
         * Occurrence:     [--------]
         * Event:             [--]
         */
        rangeTest(8, 12, 12, 14, RecurringFrequency.DAILY, false);
        rangeTest(8, 12, 12, 14, RecurringFrequency.WEEKLY, false);
        rangeTest(8, 12, 12, 14, RecurringFrequency.MONTHLY, false);
        rangeTest(8, 12, 12, 14, RecurringFrequency.YEARLY, false);
    }

    /**
     * - Tests, if an event conflicts, if the the new event is before an occurrence.
     *
     * @throws Throwable
     */
    @Test
    public void testBeforeStart() throws Throwable {
        /*-
         * Occurrence:      [--------]
         * Event:[--]
         */
        rangeTest(8, 12, 4, 6, RecurringFrequency.DAILY, false);
        rangeTest(8, 12, 4, 6, RecurringFrequency.WEEKLY, false);
        rangeTest(8, 12, 4, 6, RecurringFrequency.MONTHLY, false);
        rangeTest(8, 12, 4, 6, RecurringFrequency.YEARLY, false);
    }

    /**
     * Tests, if an event conflicts, if the the new event is after an occurrence.
     *
     * @throws Throwable
     */
    @Test
    public void testAfterEnd() throws Throwable {
        /*-
         * Occurrence:     [--------]
         * Event:               [--]
         */
        rangeTest(8, 12, 14, 16, RecurringFrequency.DAILY, false);
        rangeTest(8, 12, 14, 16, RecurringFrequency.WEEKLY, false);
        rangeTest(8, 12, 14, 16, RecurringFrequency.MONTHLY, false);
        rangeTest(8, 12, 14, 16, RecurringFrequency.YEARLY, false);
    }

    /**
     * Each test-method does nearly the same, there is only a small variance in the timeframe of the conflicting event. This Method
     * does the main work.
     *
     * @param start start hour of the series event
     * @param end end hour of the series event
     * @param conflictStart start hour of the conflicting event
     * @param conflictEnd end hour of the conflicting event
     * @param freq The {@link RecurringFrequency} for the event series
     * @param shouldConflict
     * @throws Throwable
     */
    private void rangeTest(final int start, final int end, final int conflictStart, final int conflictEnd, RecurringFrequency freq, final boolean shouldConflict) throws Throwable {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 28); // Use the last possible day, which occurs in every month.
        calendar.set(Calendar.HOUR_OF_DAY, start);
        DateTimeData startDate = DateTimeUtil.getDateTime(calendar);
        calendar.set(Calendar.HOUR_OF_DAY, end);
        DateTimeData endDate = DateTimeUtil.getDateTime(calendar);
        EventData event = EventFactory.createSingleEvent(getCalendaruser(), "Bug12842Test", startDate, endDate, folderId);
        event.setRrule(RRuleFactory.getFrequencyWithoutLimit(freq));
        eventManager.createEvent(event, true);
        
        // create second event
        EventData conflictingEvent = event;
        conflictingEvent.setSummary("ConflictingEvent");
        
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(calendar.getTime());
        cal2.set(Calendar.DAY_OF_MONTH, 28); // Use the last possible day, which occurs in every month.
        cal2.set(Calendar.HOUR_OF_DAY, conflictStart);
        switch (freq) {
            case DAILY:
                cal2.add(Calendar.DAY_OF_MONTH, 8);
                break;
            case MONTHLY:
                cal2.add(Calendar.MONTH, 1);
                break;
            case SECONDLY:
                break;
            case WEEKLY:
                // Adding 2 weeks to get into the future. Events in the past do not conflict.
                cal2.add(Calendar.WEEK_OF_YEAR, 2);
                break;
            case YEARLY:
                cal2.add(Calendar.YEAR, 1);
                break;
            case HOURLY:
            case MINUTELY:
            default:
                break;
        }

        cal2.set(Calendar.HOUR_OF_DAY, conflictStart);
        conflictingEvent.setStartDate(DateTimeUtil.getDateTime(cal2));
        cal2.set(Calendar.HOUR_OF_DAY, conflictEnd);
        conflictingEvent.setEndDate(DateTimeUtil.getDateTime(cal2));
        
        ChronosCalendarResultResponse response = defaultUserApi.getChronosApi().createEvent(getSessionId(), folderId, conflictingEvent, Boolean.TRUE, null, Boolean.FALSE, null, null, null, Boolean.FALSE, null);
        assertNull(response.getErrorDesc(), response.getError());
        if (shouldConflict) {
            assertFalse(response.getData().getConflicts().isEmpty());
        } else {
            assertTrue(response.getData().getConflicts().isEmpty());
            eventManager.handleCreation(response);
        }
        
        eventManager.cleanUp();
    }

}
