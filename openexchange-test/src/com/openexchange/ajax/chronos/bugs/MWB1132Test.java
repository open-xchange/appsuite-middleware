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
 * {@link MWB1132Test}
 *
 * @author <a href="mailto:philipp.schumacher@open-xchange.com">Philipp Schumacher</a>
 * @since v7.10.6
 */
public class MWB1132Test extends AbstractICalCalendarProviderTest {
    
    @Test
    public void testMWB1132() throws OXException, IOException, JSONException, ApiException {
        String mockediCalFeed = // @formatter:off
            "BEGIN:VCALENDAR" + "\r\n" +
            "VERSION:2.0" + "\r\n" +
            "PRODID:-//Open-Xchange//7.10.6-Rev0//EN" + "\r\n" +
            "METHOD:PUBLISH" + "\r\n" +
            "X-WR-CALNAME:Kalender" + "\r\n" +
            "BEGIN:VTIMEZONE" + "\r\n" +
            "TZID:Europe/Berlin" + "\r\n" +
            "LAST-MODIFIED:20201011T015911Z" + "\r\n" +
            "TZURL:http://tzurl.org/zoneinfo-outlook/Europe/Berlin" + "\r\n" +
            "X-LIC-LOCATION:Europe/Berlin" + "\r\n" +
            "BEGIN:DAYLIGHT" + "\r\n" +
            "TZNAME:CEST" + "\r\n" +
            "TZOFFSETFROM:+0100" + "\r\n" +
            "TZOFFSETTO:+0200" + "\r\n" +
            "DTSTART:19700329T020000" + "\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU" + "\r\n" +
            "END:DAYLIGHT" + "\r\n" +
            "BEGIN:STANDARD" + "\r\n" +
            "TZNAME:CET" + "\r\n" +
            "TZOFFSETFROM:+0200" + "\r\n" +
            "TZOFFSETTO:+0100" + "\r\n" +
            "DTSTART:19701025T030000" + "\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU" + "\r\n" +
            "END:STANDARD" + "\r\n" +
            "END:VTIMEZONE" + "\r\n" +
            "BEGIN:VEVENT" + "\r\n" +
            "DTSTAMP:20210614T110907Z" + "\r\n" +
            "CLASS:PUBLIC" + "\r\n" +
            "CREATED:20210614T110819Z" + "\r\n" +
            "DTEND;VALUE=DATE:20210615" + "\r\n" +
            "DTSTART;VALUE=DATE:20210614" + "\r\n" +
            "LAST-MODIFIED:20210614T110907Z" + "\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=6;BYMONTHDAY=14" + "\r\n" +
            "SEQUENCE:0" + "\r\n" +
            "SUMMARY:Geburtstag" + "\r\n" +
            "TRANSP:OPAQUE" + "\r\n" +
            "UID:89f9eb90-ce98-46f1-95e5-d372acc6c0bd" + "\r\n" +
            "X-MICROSOFT-CDO-ALLDAYEVENT:TRUE" + "\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY" + "\r\n" +
            "END:VEVENT" + "\r\n" +
            "BEGIN:VEVENT" + "\r\n" +
            "DTSTAMP:20210614T110907Z" + "\r\n" +
            "CLASS:PUBLIC" + "\r\n" +
            "CREATED:20210614T110907Z" + "\r\n" +
            "DTEND;TZID=Europe/Berlin:20220614T080000" + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:20220614T060000" + "\r\n" +
            "LAST-MODIFIED:20210614T110907Z" + "\r\n" +
            "RECURRENCE-ID;VALUE=DATE:20220614" + "\r\n" +
            "SEQUENCE:1" + "\r\n" +
            "SUMMARY:Geburtstag" + "\r\n" +
            "TRANSP:OPAQUE" + "\r\n" +
            "UID:89f9eb90-ce98-46f1-95e5-d372acc6c0bd" + "\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY" + "\r\n" +
            "END:VEVENT" + "\r\n" +
            "END:VCALENDAR" + "\r\n"
        ; // @formatter:off
        String mockedFeedUri = "http://example.com/files/" + UUID.randomUUID().toString() + ".ics";
        mock(mockedFeedUri, mockediCalFeed, HttpStatus.SC_OK);
        String folderId = createDefaultAccount(mockedFeedUri);
        Date rangeFrom = new Date(dateToMillis("20200101T000000Z"));
        Date rangeUntil = new Date(dateToMillis("20231231T000000Z"));
        /*
         * verify event data can be fetched and no 'Unexpected error: can not shift the time zone of an all-day' occurs
         */
        List<EventData> events = eventManager.getAllEvents(rangeFrom, rangeUntil, true, folderId);
        assertEquals("unexpcted number of events", 3, events.size());
    }

}
