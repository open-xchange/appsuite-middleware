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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.groupware.container;

import junit.framework.TestCase;
import java.util.Date;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class CalendarObjectTest extends CommonObjectTest {

    private CalendarObject seriesMaster = null;

    private CalendarObject ocurrence1 = null;

    private CalendarObject ocurrence2 = null;

    private CalendarObject exception = null;

    private CalendarObject single = null;

    public void testIsPartOfSeries() {
        assertTrue(seriesMaster.isPartOfSeries());
        assertTrue(ocurrence1.isPartOfSeries());
        assertTrue(ocurrence2.isPartOfSeries());
        assertTrue(exception.isPartOfSeries());

        assertFalse(single.isPartOfSeries());

    }

    public void testIsSpecificOcurrence() {
        assertTrue(ocurrence1.isSpecificOcurrence());
        assertTrue(ocurrence2.isSpecificOcurrence());
        assertTrue(exception.isSpecificOcurrence());

        assertFalse(seriesMaster.isSpecificOcurrence());
        assertFalse(single.isSpecificOcurrence());
    }

    public void testIsException() {
        assertTrue(exception.isException());
        assertTrue(ocurrence1.isException());
        assertTrue(ocurrence2.isException());

        assertFalse(seriesMaster.isException());
        assertFalse(single.isException());
    }

    public void testIsMaster() {
        assertTrue(seriesMaster.isMaster());

        assertFalse(ocurrence1.isMaster());
        assertFalse(ocurrence2.isMaster());
        assertFalse(exception.isMaster());
        assertFalse(single.isMaster());

    }

    public void testIsSingle() {
        assertTrue(single.isSingle());

        assertFalse(ocurrence1.isSingle());
        assertFalse(ocurrence2.isSingle());
        assertFalse(exception.isSingle());
        assertFalse(seriesMaster.isSingle());

    }

    public void setUp() {
        int masterId = 12;
        int singleId = 14;

        seriesMaster = new TestCalendarObject();
        seriesMaster.setObjectID(masterId);
        seriesMaster.setRecurrenceID(masterId);
        seriesMaster.setRecurrenceType(CalendarObject.DAILY);
        seriesMaster.setInterval(1);
        seriesMaster.setRecurrencePosition(0);

        ocurrence1 = new TestCalendarObject();
        ocurrence1.setObjectID(masterId);
        ocurrence1.setRecurrenceID(masterId);
        ocurrence1.setRecurrencePosition(2);

        ocurrence2 = new TestCalendarObject();
        ocurrence2.setObjectID(masterId);
        ocurrence2.setRecurrenceID(masterId);
        ocurrence2.setRecurrenceDatePosition(new Date());

        exception = new TestCalendarObject();
        exception.setRecurrenceID(masterId);
        exception.setRecurrenceType(0);
        exception.setRecurrencePosition(3);
        exception.setInterval(1);

        single = new TestCalendarObject();
        single.setObjectID(singleId);
        single.setRecurrenceID(0);
        single.setRecurrenceType(0);
    }

    public void testFindDifferingFields() {

        CalendarObject dataObject = getCalendarObject();
        CalendarObject otherDataObject = getCalendarObject();


        otherDataObject.setChangeExceptions(new Date[]{new Date(-2), new Date(-3)});
        assertDifferences(dataObject, otherDataObject, CalendarObject.CHANGE_EXCEPTIONS);

        otherDataObject.setDayInMonth(-2);
        assertDifferences(
            dataObject,
            otherDataObject,
            CalendarObject.CHANGE_EXCEPTIONS,
            CalendarObject.DAY_IN_MONTH);

        otherDataObject.setDays(-2);
        assertDifferences(
            dataObject,
            otherDataObject,
            CalendarObject.CHANGE_EXCEPTIONS,
            CalendarObject.DAY_IN_MONTH,
            CalendarObject.DAYS);

        otherDataObject.setDeleteExceptions(new Date[]{new Date(-2), new Date(-3)});
        assertDifferences(
            dataObject,
            otherDataObject,
            CalendarObject.CHANGE_EXCEPTIONS,
            CalendarObject.DAY_IN_MONTH,
            CalendarObject.DAYS,
            CalendarObject.DELETE_EXCEPTIONS);

        otherDataObject.setEndDate(new Date(-2));
        assertDifferences(
            dataObject,
            otherDataObject,
            CalendarObject.CHANGE_EXCEPTIONS,
            CalendarObject.DAY_IN_MONTH,
            CalendarObject.DAYS,
            CalendarObject.DELETE_EXCEPTIONS,
            CalendarObject.END_DATE);

        otherDataObject.setInterval(-1);
        assertDifferences(
            dataObject,
            otherDataObject,
            CalendarObject.CHANGE_EXCEPTIONS,
            CalendarObject.DAY_IN_MONTH,
            CalendarObject.DAYS,
            CalendarObject.DELETE_EXCEPTIONS,
            CalendarObject.END_DATE,
            CalendarObject.INTERVAL);

        otherDataObject.setMonth(-3);
        assertDifferences(
            dataObject,
            otherDataObject,
            CalendarObject.CHANGE_EXCEPTIONS,
            CalendarObject.DAY_IN_MONTH,
            CalendarObject.DAYS,
            CalendarObject.DELETE_EXCEPTIONS,
            CalendarObject.END_DATE,
            CalendarObject.INTERVAL,
            CalendarObject.MONTH);

        otherDataObject.setNote("Bluppikowski");
        assertDifferences(
            dataObject,
            otherDataObject,
            CalendarObject.CHANGE_EXCEPTIONS,
            CalendarObject.DAY_IN_MONTH,
            CalendarObject.DAYS,
            CalendarObject.DELETE_EXCEPTIONS,
            CalendarObject.END_DATE,
            CalendarObject.INTERVAL,
            CalendarObject.MONTH,
            CalendarObject.NOTE);

        otherDataObject.setNotification(false);
        assertDifferences(
            dataObject,
            otherDataObject,
            CalendarObject.CHANGE_EXCEPTIONS,
            CalendarObject.DAY_IN_MONTH,
            CalendarObject.DAYS,
            CalendarObject.DELETE_EXCEPTIONS,
            CalendarObject.END_DATE,
            CalendarObject.INTERVAL,
            CalendarObject.MONTH,
            CalendarObject.NOTE,
            CalendarObject.NOTIFICATION);

        otherDataObject.setOccurrence(-23);
        assertDifferences(
            dataObject,
            otherDataObject,
            CalendarObject.CHANGE_EXCEPTIONS,
            CalendarObject.DAY_IN_MONTH,
            CalendarObject.DAYS,
            CalendarObject.DELETE_EXCEPTIONS,
            CalendarObject.END_DATE,
            CalendarObject.INTERVAL,
            CalendarObject.MONTH,
            CalendarObject.NOTE,
            CalendarObject.NOTIFICATION,
            CalendarObject.RECURRENCE_COUNT);

        otherDataObject.setParticipants(new Participant[]{new UserParticipant(12)});
        assertDifferences(
            dataObject,
            otherDataObject,
            CalendarObject.CHANGE_EXCEPTIONS,
            CalendarObject.DAY_IN_MONTH,
            CalendarObject.DAYS,
            CalendarObject.DELETE_EXCEPTIONS,
            CalendarObject.END_DATE,
            CalendarObject.INTERVAL,
            CalendarObject.MONTH,
            CalendarObject.NOTE,
            CalendarObject.NOTIFICATION,
            CalendarObject.RECURRENCE_COUNT,
            CalendarObject.PARTICIPANTS);

        otherDataObject.setRecurrenceCalculator(-3);
        assertDifferences(
            dataObject,
            otherDataObject,
            CalendarObject.CHANGE_EXCEPTIONS,
            CalendarObject.DAY_IN_MONTH,
            CalendarObject.DAYS,
            CalendarObject.DELETE_EXCEPTIONS,
            CalendarObject.END_DATE,
            CalendarObject.INTERVAL,
            CalendarObject.MONTH,
            CalendarObject.NOTE,
            CalendarObject.NOTIFICATION,
            CalendarObject.RECURRENCE_COUNT,
            CalendarObject.PARTICIPANTS,
            CalendarObject.RECURRENCE_CALCULATOR);

        otherDataObject.setRecurrenceCount(-23);
        assertDifferences(
            dataObject,
            otherDataObject,
            CalendarObject.CHANGE_EXCEPTIONS,
            CalendarObject.DAY_IN_MONTH,
            CalendarObject.DAYS,
            CalendarObject.DELETE_EXCEPTIONS,
            CalendarObject.END_DATE,
            CalendarObject.INTERVAL,
            CalendarObject.MONTH,
            CalendarObject.NOTE,
            CalendarObject.NOTIFICATION,
            CalendarObject.PARTICIPANTS,
            CalendarObject.RECURRENCE_CALCULATOR,
            CalendarObject.RECURRENCE_COUNT);

        otherDataObject.setRecurrenceDatePosition(new Date(-2));
        assertDifferences(
            dataObject,
            otherDataObject,
            CalendarObject.CHANGE_EXCEPTIONS,
            CalendarObject.DAY_IN_MONTH,
            CalendarObject.DAYS,
            CalendarObject.DELETE_EXCEPTIONS,
            CalendarObject.END_DATE,
            CalendarObject.INTERVAL,
            CalendarObject.MONTH,
            CalendarObject.NOTE,
            CalendarObject.NOTIFICATION,
            CalendarObject.PARTICIPANTS,
            CalendarObject.RECURRENCE_CALCULATOR,
            CalendarObject.RECURRENCE_COUNT,
            CalendarObject.RECURRENCE_DATE_POSITION);

        otherDataObject.setRecurrenceID(-12);
        assertDifferences(
            dataObject,
            otherDataObject,
            CalendarObject.CHANGE_EXCEPTIONS,
            CalendarObject.DAY_IN_MONTH,
            CalendarObject.DAYS,
            CalendarObject.DELETE_EXCEPTIONS,
            CalendarObject.END_DATE,
            CalendarObject.INTERVAL,
            CalendarObject.MONTH,
            CalendarObject.NOTE,
            CalendarObject.NOTIFICATION,
            CalendarObject.PARTICIPANTS,
            CalendarObject.RECURRENCE_CALCULATOR,
            CalendarObject.RECURRENCE_COUNT,
            CalendarObject.RECURRENCE_DATE_POSITION,
            CalendarObject.RECURRENCE_ID);

        otherDataObject.setRecurrencePosition(-3);
        assertDifferences(
            dataObject,
            otherDataObject,
            CalendarObject.CHANGE_EXCEPTIONS,
            CalendarObject.DAY_IN_MONTH,
            CalendarObject.DAYS,
            CalendarObject.DELETE_EXCEPTIONS,
            CalendarObject.END_DATE,
            CalendarObject.INTERVAL,
            CalendarObject.MONTH,
            CalendarObject.NOTE,
            CalendarObject.NOTIFICATION,
            CalendarObject.PARTICIPANTS,
            CalendarObject.RECURRENCE_CALCULATOR,
            CalendarObject.RECURRENCE_COUNT,
            CalendarObject.RECURRENCE_DATE_POSITION,
            CalendarObject.RECURRENCE_ID,
            CalendarObject.RECURRENCE_POSITION);

        otherDataObject.setRecurrenceType(-2);
        assertDifferences(
            dataObject,
            otherDataObject,
            CalendarObject.CHANGE_EXCEPTIONS,
            CalendarObject.DAY_IN_MONTH,
            CalendarObject.DAYS,
            CalendarObject.DELETE_EXCEPTIONS,
            CalendarObject.END_DATE,
            CalendarObject.INTERVAL,
            CalendarObject.MONTH,
            CalendarObject.NOTE,
            CalendarObject.NOTIFICATION,
            CalendarObject.PARTICIPANTS,
            CalendarObject.RECURRENCE_CALCULATOR,
            CalendarObject.RECURRENCE_COUNT,
            CalendarObject.RECURRENCE_DATE_POSITION,
            CalendarObject.RECURRENCE_ID,
            CalendarObject.RECURRENCE_POSITION,
            CalendarObject.RECURRENCE_TYPE);

        otherDataObject.setStartDate(new Date(-3));
        assertDifferences(
            dataObject,
            otherDataObject,
            CalendarObject.CHANGE_EXCEPTIONS,
            CalendarObject.DAY_IN_MONTH,
            CalendarObject.DAYS,
            CalendarObject.DELETE_EXCEPTIONS,
            CalendarObject.END_DATE,
            CalendarObject.INTERVAL,
            CalendarObject.MONTH,
            CalendarObject.NOTE,
            CalendarObject.NOTIFICATION,
            CalendarObject.PARTICIPANTS,
            CalendarObject.RECURRENCE_CALCULATOR,
            CalendarObject.RECURRENCE_COUNT,
            CalendarObject.RECURRENCE_DATE_POSITION,
            CalendarObject.RECURRENCE_ID,
            CalendarObject.RECURRENCE_POSITION,
            CalendarObject.RECURRENCE_TYPE,
            CalendarObject.START_DATE);

        otherDataObject.setTitle("Bluppikowski");
        assertDifferences(
            dataObject,
            otherDataObject,
            CalendarObject.CHANGE_EXCEPTIONS,
            CalendarObject.DAY_IN_MONTH,
            CalendarObject.DAYS,
            CalendarObject.DELETE_EXCEPTIONS,
            CalendarObject.END_DATE,
            CalendarObject.INTERVAL,
            CalendarObject.MONTH,
            CalendarObject.NOTE,
            CalendarObject.NOTIFICATION,
            CalendarObject.PARTICIPANTS,
            CalendarObject.RECURRENCE_CALCULATOR,
            CalendarObject.RECURRENCE_COUNT,
            CalendarObject.RECURRENCE_DATE_POSITION,
            CalendarObject.RECURRENCE_ID,
            CalendarObject.RECURRENCE_POSITION,
            CalendarObject.RECURRENCE_TYPE,
            CalendarObject.START_DATE,
            CalendarObject.TITLE);

        otherDataObject.setUntil(new Date(-3));
        assertDifferences(
            dataObject,
            otherDataObject,
            CalendarObject.CHANGE_EXCEPTIONS,
            CalendarObject.DAY_IN_MONTH,
            CalendarObject.DAYS,
            CalendarObject.DELETE_EXCEPTIONS,
            CalendarObject.END_DATE,
            CalendarObject.INTERVAL,
            CalendarObject.MONTH,
            CalendarObject.NOTE,
            CalendarObject.NOTIFICATION,
            CalendarObject.PARTICIPANTS,
            CalendarObject.RECURRENCE_CALCULATOR,
            CalendarObject.RECURRENCE_COUNT,
            CalendarObject.RECURRENCE_DATE_POSITION,
            CalendarObject.RECURRENCE_ID,
            CalendarObject.RECURRENCE_POSITION,
            CalendarObject.RECURRENCE_TYPE,
            CalendarObject.START_DATE,
            CalendarObject.TITLE,
            CalendarObject.UNTIL);

        otherDataObject.setUsers(new UserParticipant[]{new UserParticipant(12)});
        assertDifferences(
            dataObject,
            otherDataObject,
            CalendarObject.CHANGE_EXCEPTIONS,
            CalendarObject.DAY_IN_MONTH,
            CalendarObject.DAYS,
            CalendarObject.DELETE_EXCEPTIONS,
            CalendarObject.END_DATE,
            CalendarObject.INTERVAL,
            CalendarObject.MONTH,
            CalendarObject.NOTE,
            CalendarObject.NOTIFICATION,
            CalendarObject.PARTICIPANTS,
            CalendarObject.RECURRENCE_CALCULATOR,
            CalendarObject.RECURRENCE_COUNT,
            CalendarObject.RECURRENCE_DATE_POSITION,
            CalendarObject.RECURRENCE_ID,
            CalendarObject.RECURRENCE_POSITION,
            CalendarObject.RECURRENCE_TYPE,
            CalendarObject.START_DATE,
            CalendarObject.TITLE,
            CalendarObject.UNTIL,
            CalendarObject.USERS);
    }

    private CalendarObject getCalendarObject() {
        CalendarObject co = new TestCalendarObject();
        fillCalendarObject(co);
        return co;
    }

    public void fillCalendarObject(CalendarObject co) {
        super.fillCommonObject(co);

        co.setAlarmFlag(true);
        co.setChangeExceptions(new Date[] { new Date(0), new Date(2) });
        co.setConfirm(3);
        co.setConfirmMessage("bier");
        co.setDayInMonth(3);
        co.setDays(2);
        co.setDeleteExceptions(new Date[] { new Date(0), new Date(2) });
        co.setEndDate(new Date(3));
        co.setInterval(2);
        co.setMonth(2);
        co.setNote("Blupp");
        co.setNotification(true);
        co.setOccurrence(23);
        co.setParticipants(new Participant[0]);
        co.setRecurrenceCalculator(2);
        co.setRecurrenceCount(23);
        co.setRecurrenceDatePosition(new Date(23));
        co.setRecurrenceID(2);
        co.setRecurrencePosition(3);
        co.setRecurrenceType(3);
        co.setStartDate(new Date(2));
        co.setTitle("Bla");
        co.setUntil(new Date(2));
        co.setUsers(new UserParticipant[0]);

    }

    private static class TestCalendarObject extends CalendarObject {

    }
}
