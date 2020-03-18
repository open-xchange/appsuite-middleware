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

package com.openexchange.ajax.importexport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;

/**
 * {@link MWB161Test}
 * 
 * import of an ics file results in Error while reading/writing from/to the database
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.4
 */
public class MWB161Test extends ManagedAppointmentTest {

    @Test
    public void testInvalidAlarm() throws Exception {
        String uid = UUIDs.getUnformattedStringFromRandom();
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" +
            "VERSION:2.0\r\n" +
            "PRODID:icalendar-ruby\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            "METHOD:PUBLISH\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Paris\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:20200329T030000\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3\r\n" +
            "TZNAME:CEST\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "DTSTART:20191027T020000\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\r\n" +
            "TZNAME:CET\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "DTSTAMP:20200302T163050Z\r\n" +
            "UID:" + uid + "\r\n" +
            "DTSTART;TZID=Europe/Paris:20200504T083000\r\n" +
            "DTEND;TZID=Europe/Paris:20200504T091500\r\n" +
            "CLASS:PRIVATE\r\n" +
            "DESCRIPTION:Zugangsinformationen:\\n* In Stock 2.OG mit Fahrstuhl\\n* Mit Fah\r\n" +
            " rstuhl\\n* Nicht Barrierefrei\\n\\nUm Ihren Termin zu stornieren oder zu vers\r\n" +
            " chieben\\, klicken Sie auf den folgenden Link :\\nhttp://www.doctolib.de/app\r\n" +
            " ointments/anonymous/...\\n\r\n" +
            "GEO:52.4880182;13.3405172\r\n" +
            "LOCATION:Innsbrucker Straße 58\\, 10825 Berlin\r\n" +
            "PRIORITY:2\r\n" +
            "SUMMARY:Termin bei Gastroenterologie am Bayerischen Platz\r\n" +
            "BEGIN:VALARM\r\n" +
            "TRIGGER:-PT2H\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ;
        /*
         * import iCal & check results
         */
        ICalImportResponse importResponse = getClient().execute(new ICalImportRequest(folder.getObjectID(), iCal, false));
        assertNotNull("Should have processed 1 event", importResponse.getImports());
        assertEquals("Should have processed 1 event", 1, importResponse.getImports().length);
        OXException error = importResponse.getImports()[0].getException();
        assertNotNull("No error for imported event", error);
        assertEquals("Unexpected error code for imported event", "CAL-4005", error.getErrorCode());
    }

}
