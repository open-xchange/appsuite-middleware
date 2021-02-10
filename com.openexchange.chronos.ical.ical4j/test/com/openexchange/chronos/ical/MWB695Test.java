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

package com.openexchange.chronos.ical;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.dmfs.rfc5545.DateTime;
import org.junit.Test;
import com.openexchange.chronos.Conference;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventStatus;
import com.openexchange.chronos.common.CalendarUtils;

/**
 * {@link MWB695Test}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public class MWB695Test extends ICalTest {

    @Test
    public void testExportImportMultipleValues() throws Exception {
        Event event = new Event();
        event.setSummary("test");
        event.setCategories(Arrays.asList("CONFERENCE"));
        event.setUid("uid1@example.com");
        event.setTimestamp(DateTime.parse("19960704T120000Z").getTimestamp());
        event.setStatus(EventStatus.CONFIRMED);
        List<Conference> conferences = new ArrayList<Conference>();
        Conference conference = new Conference();
        conference.setUri("https://chat.example.com/audio?id=123456");
        conference.setLabel("Attendee dial-in");
        conference.setFeatures(Arrays.asList("AUDIO", "VIDEO"));
        conferences.add(conference);
        event.setConferences(conferences);
        String exportedICal = unfold(exportEvent(event));
        assertTrue(exportedICal.contains("FEATURE=AUDIO,VIDEO"));
        assertFalse(exportedICal.contains("FEATURE=\"AUDIO,VIDEO\""));
        Event importedEvent = importEvent(exportedICal);
        Conference matchingConference = CalendarUtils.find(importedEvent.getConferences(), conference);
        assertNotNull(matchingConference);
        assertEquals(conference.getUri(), matchingConference.getUri());
        assertEquals(conference.getLabel(), matchingConference.getLabel());
        assertTrue(CollectionUtils.isEqualCollection(conference.getFeatures(), matchingConference.getFeatures()));
    }

    @Test
    public void testStillImportQuotedMultiValues() throws Exception {
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" +
            "PRODID:-//xyz Corp//NONSGML PDA Calendar Version 1.0//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VEVENT\r\n" +
            "DTSTAMP:19960704T120000Z\r\n" +
            "UID:uid1@example.com\r\n" +
            "DTSTART:19960918T143000Z\r\n" +
            "DTEND:19960920T220000Z\r\n" +
            "STATUS:CONFIRMED\r\n" +
            "CATEGORIES:CONFERENCE\r\n" +
            "SUMMARY:test\r\n" +
            "CONFERENCE;VALUE=URI;FEATURE=\"AUDIO,VIDEO\";LABEL=Attendee dial-in:https://chat.example.com/audio?id=123456\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        Event event = importEvent(iCal);
        assertEquals("uid1@example.com", event.getUid());
        assertEquals(Collections.singletonList("CONFERENCE"), event.getCategories());
        assertNotNull(event.getConferences());
        assertEquals(1, event.getConferences().size());
        Conference conference = new Conference();
        conference.setUri("https://chat.example.com/audio?id=123456");
        conference.setLabel("Attendee dial-in");
        conference.setFeatures(Arrays.asList("AUDIO", "VIDEO"));
        Conference matchingConference = CalendarUtils.find(event.getConferences(), conference);
        assertNotNull(matchingConference);
        assertEquals(conference.getUri(), matchingConference.getUri());
        assertEquals(conference.getLabel(), matchingConference.getLabel());
        assertTrue(CollectionUtils.isEqualCollection(conference.getFeatures(), matchingConference.getFeatures()));
    }

}
