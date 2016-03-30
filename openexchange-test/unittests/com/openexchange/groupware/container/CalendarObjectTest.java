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

package com.openexchange.groupware.container;

import static com.openexchange.groupware.container.CalendarObject.DAYS;
import static com.openexchange.groupware.container.CalendarObject.DAY_IN_MONTH;
import static com.openexchange.groupware.container.CalendarObject.END_DATE;
import static com.openexchange.groupware.container.CalendarObject.INTERVAL;
import static com.openexchange.groupware.container.CalendarObject.MONTH;
import static com.openexchange.groupware.container.CalendarObject.NOTE;
import static com.openexchange.groupware.container.CalendarObject.NOTIFICATION;
import static com.openexchange.groupware.container.CalendarObject.PARTICIPANTS;
import static com.openexchange.groupware.container.CalendarObject.RECURRENCE_CALCULATOR;
import static com.openexchange.groupware.container.CalendarObject.RECURRENCE_COUNT;
import static com.openexchange.groupware.container.CalendarObject.RECURRENCE_DATE_POSITION;
import static com.openexchange.groupware.container.CalendarObject.RECURRENCE_ID;
import static com.openexchange.groupware.container.CalendarObject.RECURRENCE_POSITION;
import static com.openexchange.groupware.container.CalendarObject.RECURRENCE_TYPE;
import static com.openexchange.groupware.container.CalendarObject.START_DATE;
import static com.openexchange.groupware.container.CalendarObject.TITLE;
import static com.openexchange.groupware.container.CalendarObject.UNTIL;
import static com.openexchange.groupware.container.CalendarObject.USERS;
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

    @Override
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

    @Override
    public void testAttrAccessors() {

        CalendarObject object = new TestCalendarObject();

        // RECURRENCE_COUNT
        assertFalse(object.contains(RECURRENCE_COUNT));
        assertFalse(object.containsOccurrence());

        object.setOccurrence(-12);
        assertTrue(object.contains(RECURRENCE_COUNT));
        assertTrue(object.containsOccurrence());
        assertEquals(-12, object.get(RECURRENCE_COUNT));

        object.set(RECURRENCE_COUNT,12);
        assertEquals(12, object.getOccurrence());

        object.remove(RECURRENCE_COUNT);
        assertFalse(object.contains(RECURRENCE_COUNT));
        assertFalse(object.containsOccurrence());



        // UNTIL
        assertFalse(object.contains(UNTIL));
        assertFalse(object.containsUntil());

        object.setUntil(new Date(42));
        assertTrue(object.contains(UNTIL));
        assertTrue(object.containsUntil());
        assertEquals(new Date(42), object.get(UNTIL));

        object.set(UNTIL,new Date(23));
        assertEquals(new Date(23), object.getUntil());

        object.remove(UNTIL);
        assertFalse(object.contains(UNTIL));
        assertFalse(object.containsUntil());


        // USERS
        assertFalse(object.contains(USERS));
        assertFalse(object.containsUserParticipants());

        UserParticipant[] users = new UserParticipant[]{new UserParticipant(1)};
        UserParticipant[] otherUsers = new UserParticipant[]{new UserParticipant(2)};

        object.setUsers(users);
        assertTrue(object.contains(USERS));
        assertTrue(object.containsUserParticipants());
        assertEquals(users, object.get(USERS));

        object.set(USERS, otherUsers);
        assertEquals(otherUsers, object.getUsers());

        object.remove(USERS);
        assertFalse(object.contains(USERS));
        assertFalse(object.containsUserParticipants());



        // NOTE
        assertFalse(object.contains(NOTE));
        assertFalse(object.containsNote());

        object.setNote("Bla");
        assertTrue(object.contains(NOTE));
        assertTrue(object.containsNote());
        assertEquals("Bla", object.get(NOTE));

        object.set(NOTE,"Blupp");
        assertEquals("Blupp", object.getNote());

        object.remove(NOTE);
        assertFalse(object.contains(NOTE));
        assertFalse(object.containsNote());



        // RECURRENCE_DATE_POSITION
        assertFalse(object.contains(RECURRENCE_DATE_POSITION));
        assertFalse(object.containsRecurrenceDatePosition());

        object.setRecurrenceDatePosition(new Date(42));
        assertTrue(object.contains(RECURRENCE_DATE_POSITION));
        assertTrue(object.containsRecurrenceDatePosition());
        assertEquals(new Date(42), object.get(RECURRENCE_DATE_POSITION));

        object.set(RECURRENCE_DATE_POSITION,new Date(23));
        assertEquals(new Date(23), object.getRecurrenceDatePosition());

        object.remove(RECURRENCE_DATE_POSITION);
        assertFalse(object.contains(RECURRENCE_DATE_POSITION));
        assertFalse(object.containsRecurrenceDatePosition());



        // END_DATE
        assertFalse(object.contains(END_DATE));
        assertFalse(object.containsEndDate());

        object.setEndDate(new Date(42));
        assertTrue(object.contains(END_DATE));
        assertTrue(object.containsEndDate());
        assertEquals(new Date(42), object.get(END_DATE));

        object.set(END_DATE,new Date(23));
        assertEquals(new Date(23), object.getEndDate());

        object.remove(END_DATE);
        assertFalse(object.contains(END_DATE));
        assertFalse(object.containsEndDate());



        // RECURRENCE_POSITION
        assertFalse(object.contains(RECURRENCE_POSITION));
        assertFalse(object.containsRecurrencePosition());

        object.setRecurrencePosition(-12);
        assertTrue(object.contains(RECURRENCE_POSITION));
        assertTrue(object.containsRecurrencePosition());
        assertEquals(-12, object.get(RECURRENCE_POSITION));

        object.set(RECURRENCE_POSITION,12);
        assertEquals(12, object.getRecurrencePosition());

        object.remove(RECURRENCE_POSITION);
        assertFalse(object.contains(RECURRENCE_POSITION));
        assertFalse(object.containsRecurrencePosition());



        // RECURRENCE_CALCULATOR
        object.setRecurrenceCalculator(-12);
        assertEquals(-12, object.get(RECURRENCE_CALCULATOR));

        object.set(RECURRENCE_CALCULATOR,12);
        assertEquals(12, object.getRecurrenceCalculator());




        // DAYS
        assertFalse(object.contains(DAYS));
        assertFalse(object.containsDays());

        object.setDays(-12);
        assertTrue(object.contains(DAYS));
        assertTrue(object.containsDays());
        assertEquals(-12, object.get(DAYS));

        object.set(DAYS,12);
        assertEquals(12, object.getDays());

        object.remove(DAYS);
        assertFalse(object.contains(DAYS));
        assertFalse(object.containsDays());



        // NOTIFICATION
        assertFalse(object.contains(NOTIFICATION));
        assertFalse(object.containsNotification());

        object.setNotification(false);
        assertTrue(object.contains(NOTIFICATION));
        assertTrue(object.containsNotification());
        assertEquals(false, object.get(NOTIFICATION));

        object.set(NOTIFICATION,true);
        assertEquals(true, object.getNotification());

        object.remove(NOTIFICATION);
        assertFalse(object.contains(NOTIFICATION));
        assertFalse(object.containsNotification());



        // MONTH
        assertFalse(object.contains(MONTH));
        assertFalse(object.containsMonth());

        object.setMonth(-12);
        assertTrue(object.contains(MONTH));
        assertTrue(object.containsMonth());
        assertEquals(-12, object.get(MONTH));

        object.set(MONTH,12);
        assertEquals(12, object.getMonth());

        object.remove(MONTH);
        assertFalse(object.contains(MONTH));
        assertFalse(object.containsMonth());



        // RECURRENCE_COUNT
        assertFalse(object.contains(RECURRENCE_COUNT));
        assertFalse(object.containsRecurrenceCount());

        object.setRecurrenceCount(-12);
        assertTrue(object.contains(RECURRENCE_COUNT));
        assertTrue(object.containsRecurrenceCount());
        assertEquals(-12, object.get(RECURRENCE_COUNT));

        object.set(RECURRENCE_COUNT,12);
        assertEquals(12, object.getRecurrenceCount());

        object.remove(RECURRENCE_COUNT);
        assertFalse(object.contains(RECURRENCE_COUNT));
        assertFalse(object.containsRecurrenceCount());


        // DAY_IN_MONTH
        assertFalse(object.contains(DAY_IN_MONTH));
        assertFalse(object.containsDayInMonth());

        object.setDayInMonth(-12);
        assertTrue(object.contains(DAY_IN_MONTH));
        assertTrue(object.containsDayInMonth());
        assertEquals(-12, object.get(DAY_IN_MONTH));

        object.set(DAY_IN_MONTH,12);
        assertEquals(12, object.getDayInMonth());

        object.remove(DAY_IN_MONTH);
        assertFalse(object.contains(DAY_IN_MONTH));
        assertFalse(object.containsDayInMonth());



        // RECURRENCE_TYPE
        assertFalse(object.contains(RECURRENCE_TYPE));
        assertFalse(object.containsRecurrenceType());

        object.setRecurrenceType(-12);
        assertTrue(object.contains(RECURRENCE_TYPE));
        assertTrue(object.containsRecurrenceType());
        assertEquals(-12, object.get(RECURRENCE_TYPE));

        object.set(RECURRENCE_TYPE,12);
        assertEquals(12, object.getRecurrenceType());

        object.remove(RECURRENCE_TYPE);
        assertFalse(object.contains(RECURRENCE_TYPE));
        assertFalse(object.containsRecurrenceType());



        // START_DATE
        assertFalse(object.contains(START_DATE));
        assertFalse(object.containsStartDate());

        object.setStartDate(new Date(42));
        assertTrue(object.contains(START_DATE));
        assertTrue(object.containsStartDate());
        assertEquals(new Date(42), object.get(START_DATE));

        object.set(START_DATE,new Date(23));
        assertEquals(new Date(23), object.getStartDate());

        object.remove(START_DATE);
        assertFalse(object.contains(START_DATE));
        assertFalse(object.containsStartDate());



        // INTERVAL
        assertFalse(object.contains(INTERVAL));
        assertFalse(object.containsInterval());

        object.setInterval(-12);
        assertTrue(object.contains(INTERVAL));
        assertTrue(object.containsInterval());
        assertEquals(-12, object.get(INTERVAL));

        object.set(INTERVAL,12);
        assertEquals(12, object.getInterval());

        object.remove(INTERVAL);
        assertFalse(object.contains(INTERVAL));
        assertFalse(object.containsInterval());



        // TITLE
        assertFalse(object.contains(TITLE));
        assertFalse(object.containsTitle());

        object.setTitle("Bla");
        assertTrue(object.contains(TITLE));
        assertTrue(object.containsTitle());
        assertEquals("Bla", object.get(TITLE));

        object.set(TITLE,"Blupp");
        assertEquals("Blupp", object.getTitle());

        object.remove(TITLE);
        assertFalse(object.contains(TITLE));
        assertFalse(object.containsTitle());



        // RECURRENCE_ID
        assertFalse(object.contains(RECURRENCE_ID));
        assertFalse(object.containsRecurrenceID());

        object.setRecurrenceID(-12);
        assertTrue(object.contains(RECURRENCE_ID));
        assertTrue(object.containsRecurrenceID());
        assertEquals(-12, object.get(RECURRENCE_ID));

        object.set(RECURRENCE_ID,12);
        assertEquals(12, object.getRecurrenceID());

        object.remove(RECURRENCE_ID);
        assertFalse(object.contains(RECURRENCE_ID));
        assertFalse(object.containsRecurrenceID());



        // PARTICIPANTS
        assertFalse(object.contains(PARTICIPANTS));
        assertFalse(object.containsParticipants());


        object.setParticipants(users);
        assertTrue(object.contains(PARTICIPANTS));
        assertTrue(object.containsParticipants());
        assertEquals(users, object.get(PARTICIPANTS));

        object.set(PARTICIPANTS, otherUsers);
        assertEquals(otherUsers, object.getParticipants());

        object.remove(PARTICIPANTS);
        assertFalse(object.contains(PARTICIPANTS));
        assertFalse(object.containsParticipants());


    }

    private static class TestCalendarObject extends CalendarObject {

    }
}
