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

package com.openexchange.dav.caldav.bugs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.Date;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link Bug39819Test}
 *
 * Deleted Umlaut \u00fc from appointment description
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug39819Test extends CalDAVTest {

    @Test
    public void testSpecialCharacters() throws Exception {
        /*
         * create appointment on client
         */
        String uid = randomUID();
        Date start = TimeTools.D("next tuesday at 06:00");
        Date end = TimeTools.D("next tuesday at 07:00");
        String summary = "Test \u00fc_\u00f6_\u00e4_\u00fa_\u00ec_\u00f4_\u20ac_\u0160_\u0161_\u017d_\u017e_\u0152_\u0153_\u0178_" +
            "\u00a4_\u00a6_\u00a8_\u00b4_\u00b8_\u00bc_\u00bd_\u00be";
        String iCal = generateICal(start, end, uid, summary, "test");
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(uid, iCal));
        /*
         * verify appointment on server
         */
        Appointment appointment = getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        rememberForCleanUp(appointment);
        assertEquals("Title wrong", summary, appointment.getTitle());
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No SUMMARY in iCal found", iCalResource.getVEvent().getSummary());
        assertEquals("SUMMARY wrong", summary, iCalResource.getVEvent().getSummary());
        /*
         * update appointment on client
         */
        iCalResource.getVEvent().setLocation("new location");
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(iCalResource));
        /*
         * verify updated appointment on server
         */
        appointment = getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        assertEquals("Title wrong", summary, appointment.getTitle());
        assertEquals("Location wrong", "new location", appointment.getLocation());
        /*
         * verify updated appointment on client
         */
        iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No SUMMARY in iCal found", iCalResource.getVEvent().getSummary());
        assertEquals("SUMMARY wrong", summary, iCalResource.getVEvent().getSummary());
        assertNotNull("No LOCATION in iCal found", iCalResource.getVEvent().getLocation());
        assertEquals("LOCATION wrong", "new location", iCalResource.getVEvent().getLocation());
    }

}
