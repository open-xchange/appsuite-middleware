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
import java.util.Date;
import org.junit.Test;
import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.importexport.ImportResult;

/**
 * @author tobiasp
 */
public class ICalSeriesTests extends ManagedAppointmentTest {

    @Test
    public void testDeleteException() throws Exception {
        String ical =
            "BEGIN:VCALENDAR\n" +
            "VERSION:2.0\n" +
            "BEGIN:VEVENT\n" +
            "DTSTART;TZID=Europe/Rome:20100202T103000\n" +
            "DTEND;TZID=Europe/Rome:20100202T120000\n" +
            "RRULE:FREQ=DAILY;UNTIL=20100204T215959Z\n" +
            "EXDATE:20100203T103000\n" +
            "DTSTAMP:20110105T174810Z\n" +
            "SUMMARY:Exceptional Meeting #1\n" +
            "END:VEVENT\n"
        ;
        testChangeException(ical, "Exceptional Meeting #1", 1);
    }

    @Test
    public void testChangeExceptionWithMasterFirst() throws Exception {
        String uid = "change-exception-" + new Date().getTime();

        String title = "Change to exceptional meeting #2: Five hours later";
        String ical =
            "BEGIN:VCALENDAR\n" +
            "VERSION:2.0\n" +
            "BEGIN:VEVENT\n" +
            "DTSTART;TZID=Europe/Rome:20100202T110000\n" +
            "DTEND;TZID=Europe/Rome:20100202T120000\n" +
            "RRULE:FREQ=DAILY;UNTIL=20100228T215959Z\n" +
            "DTSTAMP:20110105T174810Z\n" +
            "SUMMARY:Exceptional meeting #2\n" +
            "UID:" + uid + "\n" +
            "END:VEVENT\n" +
            "BEGIN:VEVENT\n" +
            "DTSTART;TZID=Europe/Rome:20100204T160000\n" +
            "DTEND;TZID=Europe/Rome:20100204T170000\n" +
            "DTSTAMP:20110105T174810Z\n" +
            "SUMMARY:" + title + "\n" +
            "RECURRENCE-ID:20100204T100000Z\n" +
            "UID:" + uid + "\n" +
            "END:VEVENT\n"
        ;
        testChangeException(ical, title, 2);
    }

    protected void testChangeException(String ical, String expectedTitle, int expectedLength) throws Exception {
        AJAXClient client = getClient();
        int fid = folder.getObjectID();

        ICalImportRequest request = new ICalImportRequest(fid, ical);
        ICalImportResponse response = client.execute(request);

        ImportResult[] imports = response.getImports();
        assertNotNull(imports);
        assertEquals(expectedLength, imports.length);

        Appointment matchingAppointment = null;
        for (ImportResult result : imports) {
            Appointment appointment = catm.get(fid, Integer.parseInt(result.getObjectId()));
            assertNotNull(appointment);
            if (expectedTitle.equals(appointment.getTitle())) {
                matchingAppointment = appointment;
                break;
            }
        }
        assertNotNull("No appointment with title " + expectedTitle + " found", matchingAppointment);
    }

}
