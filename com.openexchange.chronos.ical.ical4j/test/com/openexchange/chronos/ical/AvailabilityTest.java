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

package com.openexchange.chronos.ical;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import com.openexchange.chronos.CalendarAvailability;

/**
 * {@link AvailabilityTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class AvailabilityTest extends ICalTest {

    @Test
    public void testImportSingleVAvailability() throws Exception {
        // @formatter:off
        String iCal = "BEGIN:VCALENDAR\n" +
            "BEGIN:VAVAILABILITY\n" + 
            "ORGANIZER:mailto:bernard@example.com\n" + 
            "UID:0428C7D2-688E-4D2E-AC52-CD112E2469DF\n" + 
            "DTSTAMP:20111005T133225Z\n" + 
            "BEGIN:AVAILABLE\n" + 
            "UID:34EDA59B-6BB1-4E94-A66C-64999089C0AF\n" + 
            "SUMMARY:Monday to Friday from 9:00 to 17:00\n" + 
            "DTSTART;TZID=America/Montreal:20111002T090000\n" + 
            "DTEND;TZID=America/Montreal:20111002T170000\n" + 
            "RRULE:FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR\n" + 
            "END:AVAILABLE\n" + 
            "END:VAVAILABILITY\n" + 
            "END:VCALENDAR\n";
        // @formatter:on        

        ImportedCalendar importICal = importICal(iCal);
        assertNotNull("No availability components found", importICal.getAvailabilities());
        assertEquals("More than one availability components found", 1, importICal.getAvailabilities().size());

        CalendarAvailability availability = importICal.getAvailabilities().get(0);
        assertEquals("The organizer uri does not match", "mailto:bernard@example.com", availability.getOrganizer().getUri());
        assertEquals("The uiddoes not match", "0428C7D2-688E-4D2E-AC52-CD112E2469DF", availability.getUid());
        assertEquals("The dtStamp does not match", D("2011-10-05 13:32:25"), availability.getCreationTimestamp());
    }
}
