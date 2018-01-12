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

package com.openexchange.ajax.chronos.util;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import org.junit.Assert;
import com.openexchange.testing.httpclient.models.EventData;

/**
 * {@link AssertUtil}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class AssertUtil extends Assert {

    /**
     * Asserts that the expected {@link EventData} is equal the actual {@link EventData}
     *
     * @param expected The expected {@link EventData}
     * @param actual The actual {@link EventData}
     */
    public static void assertEventsEqual(EventData expected, EventData actual) {
        assertEquals("The start date does not match", expected.getStartDate(), actual.getStartDate());
        assertEquals("The end date does not match", expected.getEndDate(), actual.getEndDate());
        assertEquals("The created date does not match", expected.getCreated(), actual.getCreated());
        assertEquals("The uid does not match", expected.getUid(), actual.getUid());
        assertEquals("The description does not match", expected.getDescription(), actual.getDescription());
        assertEquals("The last modified date does not match", expected.getLastModified(), actual.getLastModified());
        assertEquals("The summary does not match", expected.getSummary(), actual.getSummary());
        assertEquals("The sequence number does not match", expected.getSequence(), actual.getSequence());
        assertEquals("The identifier does not match", expected.getId(), actual.getId());
        assertEquals("The property class does not match", expected.getPropertyClass(), actual.getPropertyClass());
        assertEquals("The transparency does not match", expected.getTransp(), actual.getTransp());
        assertEquals("The color does not match", expected.getColor(), actual.getColor());
        assertEquals("The folder does not match", expected.getFolder(), actual.getFolder());
        assertEquals("The created by identifier does not match", expected.getCreatedBy(), actual.getCreatedBy());
        assertEquals("The recurrence identifier does not match", expected.getRecurrenceId(), actual.getRecurrenceId());
        assertEquals("The calendar user identifier does not match", expected.getCalendarUser(), actual.getCalendarUser());
        assertEquals("The recurrence rule does not match", expected.getRrule(), actual.getRrule());
        assertEquals("The url does not match", expected.getUrl(), actual.getUrl());
        assertThat("The attendees list does not match", actual.getAttendees(), is(expected.getAttendees()));
        assertThat("The attachments list does not match", actual.getAttachments(), is(expected.getAttachments()));
        assertThat("The alarms list does not match", actual.getAlarms(), is(expected.getAlarms()));
        assertThat("The organizer does not match", actual.getOrganizer(), is(expected.getOrganizer()));
        assertThat("The delete exception dates do not match", actual.getDeleteExceptionDates(), is(expected.getDeleteExceptionDates()));
        assertThat("The extended properties dates do not match", actual.getExtendedProperties(), is(expected.getExtendedProperties()));
        assertThat("The geo data does not match", actual.getGeo(), is(expected.getGeo()));
    }

    /**
     * Asserts that the expected {@link EventData} is not equal the actual {@link EventData}
     *
     * @param expected The expected {@link EventData}
     * @param actual The actual {@link EventData}
     */
    public static void assertEventsNotEqual(EventData expected, EventData actual) {
        assertNotEquals("The start date does match", expected.getStartDate(), actual.getStartDate());
        assertNotEquals("The end date does match", expected.getEndDate(), actual.getEndDate());
        assertNotEquals("The created date does match", expected.getCreated(), actual.getCreated());
        assertNotEquals("The uid does match", expected.getUid(), actual.getUid());
        assertNotEquals("The description does match", expected.getDescription(), actual.getDescription());
        assertNotEquals("The last modified date does match", expected.getLastModified(), actual.getLastModified());
        assertNotEquals("The summary does match", expected.getSummary(), actual.getSummary());
        assertNotEquals("The sequence number does match", expected.getSequence(), actual.getSequence());
        assertNotEquals("The identifier does match", expected.getId(), actual.getId());
        assertNotEquals("The property class does match", expected.getPropertyClass(), actual.getPropertyClass());
        assertNotEquals("The transparency does match", expected.getTransp(), actual.getTransp());
        assertNotEquals("The color does match", expected.getColor(), actual.getColor());
        assertNotEquals("The folder does match", expected.getFolder(), actual.getFolder());
        assertNotEquals("The created by identifier does match", expected.getCreatedBy(), actual.getCreatedBy());
        assertNotEquals("The recurrence identifier does match", expected.getRecurrenceId(), actual.getRecurrenceId());
        assertNotEquals("The calendar user identifier does match", expected.getCalendarUser(), actual.getCalendarUser());
        assertNotEquals("The recurrence rule does match", expected.getRrule(), actual.getRrule());
        assertNotEquals("The url does match", expected.getUrl(), actual.getUrl());
        assertThat("The attendees list does match", actual.getAttendees(), is(not(expected.getAttendees())));
        assertThat("The attachments list does match", actual.getAttachments(), is(not(expected.getAttachments())));
        assertThat("The alarms list does match", actual.getAlarms(), is(not(expected.getAlarms())));
        assertThat("The organizer does match", actual.getOrganizer(), is(not(expected.getOrganizer())));
        assertThat("The delete exception dates do match", actual.getDeleteExceptionDates(), is(not(expected.getDeleteExceptionDates())));
        assertThat("The extended properties dates do match", actual.getExtendedProperties(), is(not(expected.getExtendedProperties())));
        assertThat("The geo data does match", actual.getGeo(), is(not(expected.getGeo())));
    }
}
