/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
