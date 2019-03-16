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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link Bug63867Test}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.2
 */
public class Bug63867Test extends ManagedAppointmentTest {

    @Test
    public void testStrippedVTimeZone() throws Exception {
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" + 
            "PRODID;X-RICAL-TZSOURCE=TZINFO:-//com.denhaven2/NONSGML ri_cal gem//EN\r\n" + 
            "CALSCALE:GREGORIAN\r\n" + 
            "VERSION:2.0\r\n" + 
            "BEGIN:VTIMEZONE\r\n" + 
            "TZID;X-RICAL-TZSOURCE=TZINFO:Europe/Berlin\r\n" + 
            "BEGIN:DAYLIGHT\r\n" + 
            "DTSTART:20190331T020000\r\n" + 
            "RDATE:20190331T020000\r\n" + 
            "TZOFFSETFROM:+0100\r\n" + 
            "TZOFFSETTO:+0200\r\n" + 
            "TZNAME:CEST\r\n" + 
            "END:DAYLIGHT\r\n" + 
            "END:VTIMEZONE\r\n" + 
            "BEGIN:VEVENT\r\n" + 
            "DTEND;TZID=Europe/Berlin;VALUE=DATE-TIME:20190406T110000\r\n" + 
            "DTSTART;TZID=Europe/Berlin;VALUE=DATE-TIME:20190406T101500\r\n" + 
            "DTSTAMP;VALUE=DATE-TIME:20190225T110357Z\r\n" + 
            "UID:test\r\n" + 
            "SUMMARY:test\r\n" + 
            "END:VEVENT\r\n" + 
            "END:VCALENDAR\r\n" 
            ; // @formatter:on
        /*
         * import iCal
         */
        ICalImportResponse icalResponse = getClient().execute(new ICalImportRequest(folder.getObjectID(), iCal, false));
        assertFalse("Initial import failed", icalResponse.hasError());
        assertNotNull("Should have processed 1 appointment", icalResponse.getImports());
        assertEquals("Should have processed 1 appointments", 1, icalResponse.getImports().length);
        /*
         * check startdate of imported appointment
         */
        int objectID = Integer.parseInt(icalResponse.getImports()[0].getObjectId());
        Appointment appointment = catm.get(folder.getObjectID(), objectID);
        assertEquals(1554538500000l, appointment.getStartDate().getTime());
        assertEquals("Europe/Berlin", appointment.getTimezone());
    }

}
