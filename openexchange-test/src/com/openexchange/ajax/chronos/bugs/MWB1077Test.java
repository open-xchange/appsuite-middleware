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

package com.openexchange.ajax.chronos.bugs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.junit.Test;
import com.openexchange.ajax.chronos.AbstractICalCalendarProviderTest;
import com.openexchange.exception.OXException;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.EventData;

/**
 * {@link MWB1077Test}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v8.0.0
 */
public class MWB1077Test extends AbstractICalCalendarProviderTest {

    @Test
    public void testMicrosoftException() throws JSONException, ApiException, OXException, IOException {
        /*
         * mock iCalendar feed
         */
        String mockediCalFeed = // @formatter:off
            "BEGIN:VCALENDAR" + "\r\n" +
            "METHOD:PUBLISH" + "\r\n" +
            "PRODID:Microsoft Exchange Server 2010" + "\r\n" +
            "VERSION:2.0" + "\r\n" +
            "X-WR-CALNAME:test55" + "\r\n" +
            "BEGIN:VTIMEZONE" + "\r\n" +
            "TZID:W. Europe Standard Time" + "\r\n" +
            "BEGIN:STANDARD" + "\r\n" +
            "DTSTART:16010101T030000" + "\r\n" +
            "TZOFFSETFROM:+0200" + "\r\n" +
            "TZOFFSETTO:+0100" + "\r\n" +
            "RRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=-1SU;BYMONTH=10" + "\r\n" +
            "END:STANDARD" + "\r\n" +
            "BEGIN:DAYLIGHT" + "\r\n" +
            "DTSTART:16010101T020000" + "\r\n" +
            "TZOFFSETFROM:+0100" + "\r\n" +
            "TZOFFSETTO:+0200" + "\r\n" +
            "RRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=-1SU;BYMONTH=3" + "\r\n" +
            "END:DAYLIGHT" + "\r\n" +
            "END:VTIMEZONE" + "\r\n" +
            "BEGIN:VEVENT" + "\r\n" +
            "DESCRIPTION:\n" + "\r\n" +
            "RRULE:FREQ=DAILY;UNTIL=20210528T170000Z;INTERVAL=1" + "\r\n" +
            "UID:040000008200E00074C5B7101A82E0080000000009525DF5AC45D701000000000000000" + "\r\n" +
            " 0100000004469E9F68693EF47AA1A3332D5EE2887" + "\r\n" +
            "SUMMARY:rec44" + "\r\n" +
            "DTSTART;TZID=W. Europe Standard Time:20210511T190000" + "\r\n" +
            "DTEND;TZID=W. Europe Standard Time:20210511T200000" + "\r\n" +
            "CLASS:PUBLIC" + "\r\n" +
            "PRIORITY:5" + "\r\n" +
            "DTSTAMP:20210510T150336Z" + "\r\n" +
            "TRANSP:OPAQUE" + "\r\n" +
            "STATUS:CONFIRMED" + "\r\n" +
            "SEQUENCE:0" + "\r\n" +
            "LOCATION:" + "\r\n" +
            "X-MICROSOFT-CDO-APPT-SEQUENCE:0" + "\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY" + "\r\n" +
            "X-MICROSOFT-CDO-INTENDEDSTATUS:BUSY" + "\r\n" +
            "X-MICROSOFT-CDO-ALLDAYEVENT:FALSE" + "\r\n" +
            "X-MICROSOFT-CDO-IMPORTANCE:1" + "\r\n" +
            "X-MICROSOFT-CDO-INSTTYPE:1" + "\r\n" +
            "X-MICROSOFT-DONOTFORWARDMEETING:FALSE" + "\r\n" +
            "X-MICROSOFT-DISALLOW-COUNTER:FALSE" + "\r\n" +
            "END:VEVENT" + "\r\n" +
            "BEGIN:VEVENT" + "\r\n" +
            "DESCRIPTION:\n" + "\r\n" +
            "UID:040000008200E00074C5B7101A82E0080000000009525DF5AC45D701000000000000000" + "\r\n" +
            " 0100000004469E9F68693EF47AA1A3332D5EE2887" + "\r\n" +
            "RECURRENCE-ID;TZID=W. Europe Standard Time:20210513T190000" + "\r\n" +
            "SUMMARY:rec44_edit" + "\r\n" +
            "DTSTART;TZID=W. Europe Standard Time:20210513T200000" + "\r\n" +
            "DTEND;TZID=W. Europe Standard Time:20210513T210000" + "\r\n" +
            "CLASS:PUBLIC" + "\r\n" +
            "PRIORITY:5" + "\r\n" +
            "DTSTAMP:20210510T150336Z" + "\r\n" +
            "TRANSP:OPAQUE" + "\r\n" +
            "STATUS:CONFIRMED" + "\r\n" +
            "SEQUENCE:0" + "\r\n" +
            "LOCATION:" + "\r\n" +
            "X-MICROSOFT-CDO-APPT-SEQUENCE:0" + "\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY" + "\r\n" +
            "X-MICROSOFT-CDO-INTENDEDSTATUS:BUSY" + "\r\n" +
            "X-MICROSOFT-CDO-ALLDAYEVENT:FALSE" + "\r\n" +
            "X-MICROSOFT-CDO-IMPORTANCE:1" + "\r\n" +
            "X-MICROSOFT-CDO-INSTTYPE:3" + "\r\n" +
            "X-MICROSOFT-DONOTFORWARDMEETING:FALSE" + "\r\n" +
            "X-MICROSOFT-DISALLOW-COUNTER:FALSE" + "\r\n" +
            "END:VEVENT" + "\r\n" +
            "END:VCALENDAR" + "\r\n"
        ; // @formatter:off
        String mockedFeedUri = "http://example.com/files/" + UUID.randomUUID().toString() + ".ics";
        mock(mockedFeedUri, mockediCalFeed, HttpStatus.SC_OK);
        /*
         * subscribe to mocked feed & get imported event data
         */
        String folderId = createDefaultAccount(mockedFeedUri);
        Date rangeFrom = new Date(dateToMillis("20210501T000000Z"));
        Date rangeUntil = new Date(dateToMillis("20210601T000000Z"));
        List<EventData> events = eventManager.getAllEvents(rangeFrom, rangeUntil, true, folderId);
        /*
         * verify event data
         */
        assertEquals("unexpcted number of events", 18, events.size());
        EventData changeException = null;
        for (EventData event : events) {
            if ("rec44_edit".equals(event.getSummary())) {
                changeException = event;
                break;
            }
        }
        assertNotNull("change exception not imported", changeException);
    }

}
