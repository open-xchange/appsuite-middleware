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
import static org.junit.Assert.assertTrue;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import com.openexchange.contact.internal.Tools;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link Bug22338Test} - moving an appointment in iCal will not move the appointment accordingly in the OX GUI
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug22338Test extends CalDAVTest {

	@Test
    public void testLastModified() throws Exception {
        /*
         * create appointment on client
         */
        String uid = randomUID();
        String summary = "bug 22338";
        String location = "test";
        Date start = TimeTools.D("tomorrow at 2pm");
        Date end = TimeTools.D("tomorrow at 8pm");
        String iCal = generateICal(start, end, uid, summary, location);
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putICal(uid, iCal));
        /*
         * verify appointment on server
         */
        Appointment appointment = super.getAppointment(uid);
        super.rememberForCleanUp(appointment);
        assertAppointmentEquals(appointment, start, end, uid, summary, location);
        Date clientLastModified = super.getManager().getLastModification();
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertEquals("SUMMARY wrong", summary, iCalResource.getVEvent().getSummary());
        assertEquals("LOCATION wrong", location, iCalResource.getVEvent().getLocation());
        assertEquals("DTSTART wrong", start, iCalResource.getVEvent().getDTStart());
        assertEquals("DTEND wrong", end, iCalResource.getVEvent().getDTEnd());
        /*
         * change the start- and endtime on the client
         */
        Date updatedStart = TimeTools.D("tomorrow at 4pm");
        Date updatedEnd = TimeTools.D("tomorrow at 10pm");
        iCalResource.getVEvent().setDTStart(updatedStart);
        iCalResource.getVEvent().setDTEnd(updatedEnd);
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putICalUpdate(iCalResource));
        /*
         * verify updated appointments on server
         */
        List<Appointment> updates = super.getManager().updates(Tools.parse(getDefaultFolderID()), clientLastModified, false);
        assertNotNull("appointment not found on server", updates);
        assertTrue("no updated appointments on server", 0 < updates.size());
        Appointment updatedAppointment = null;
        for (Appointment update : updates) {
            if (uid.equals(update.getUid())) {
                updatedAppointment = update;
                break;
            }
        }
        assertNotNull("appointment not listed in updates", updatedAppointment);
        assertAppointmentEquals(updatedAppointment, updatedStart, updatedEnd, uid, summary, location);
        assertTrue("last modified not changed", clientLastModified.before(super.getManager().getLastModification()));
    }

}
