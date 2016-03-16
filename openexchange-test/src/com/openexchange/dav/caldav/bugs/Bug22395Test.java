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

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.ical.ICalUtils;
import com.openexchange.dav.caldav.ical.SimpleICal.Component;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link Bug22395Test} - Change exceptions created in iCal client appear one day off
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug22395Test extends CalDAVTest {

	@Test
	public void testDateOfChangeExceptions() throws Exception {
		/*
		 * fetch sync token for later synchronization
		 */
		SyncToken syncToken = new SyncToken(super.fetchSyncToken());
		/*
		 * create appointment series on server
		 */
		List<Appointment> appointments = new ArrayList<Appointment>();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(TimeTools.D("Tomorrow at midnight", TimeZone.getTimeZone("Europe/Berlin")));
		for (int i = 0; i < 24; i++) {
		    Appointment appointment = new Appointment();
		    appointment.setUid(randomUID());
		    appointment.setTitle("Series " + i);
		    appointment.setIgnoreConflicts(true);
		    appointment.setStartDate(calendar.getTime());
		    calendar.add(Calendar.HOUR_OF_DAY, 1);
            appointment.setEndDate(calendar.getTime());
		    appointment.setRecurrenceType(Appointment.DAILY);
		    appointment.setInterval(1);
	        super.create(appointment);
            appointments.add(appointment);
		}
		Date clientLastModified = getManager().getLastModification();
        /*
         * verify appointment series on client
         */
		Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<ICalResource> calendarData = super.calendarMultiget(eTags.keySet());
        for (Appointment appointment : appointments) {
            ICalResource iCalResource = assertContains(appointment.getUid(), calendarData);
            assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
            assertEquals("SUMMARY wrong", appointment.getTitle(), iCalResource.getVEvent().getSummary());
            assertEquals("DTSTART wrong", appointment.getStartDate(), iCalResource.getVEvent().getDTStart());
            assertEquals("DTEND wrong", appointment.getEndDate(), iCalResource.getVEvent().getDTEnd());
        }
        /*
         * create exceptions on client (3rd occurrence in the series)
         */
        for (ICalResource iCalResource : calendarData) {
            Component exception = new Component(ICalResource.VEVENT);
            exception.setProperty("CREATED", formatAsUTC(new Date()));
            exception.setProperty("UID", iCalResource.getVEvent().getUID());
            String tzID = iCalResource.getVEvent().getProperty("DTSTART").getAttribute("TZID");
            calendar.setTime(iCalResource.getVEvent().getDTStart());
            calendar.add(Calendar.DAY_OF_YEAR, 2);
            Date exceptionStart = calendar.getTime();
            Map<String, String> tzIDAttributes = new HashMap<String, String>();
            tzIDAttributes.put("TZID", tzID);
            exception.setProperty("DTSTART", ICalUtils.format(exceptionStart, tzID), tzIDAttributes);
            calendar.setTime(iCalResource.getVEvent().getDTEnd());
            calendar.add(Calendar.DAY_OF_YEAR, 2);
            Date exceptionEnd = calendar.getTime();
            exception.setProperty("DTEND", ICalUtils.format(exceptionEnd, tzID), tzIDAttributes);
            exception.setProperty("TRANSP", "OPAQUE");
            exception.setProperty("SUMMARY", iCalResource.getVEvent().getPropertyValue("SUMMARY") + "_edit");
            exception.setProperty("DTSTAMP", formatAsUTC(new Date()));
            exception.setProperty("SEQUENCE", "3");
            exception.setProperty("RECURRENCE-ID", ICalUtils.format(exceptionStart, tzID), tzIDAttributes);
            iCalResource.addComponent(exception);
            assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putICalUpdate(iCalResource));
        }
        /*
         * verify exceptions on server
         */
        List<Appointment> updates = super.getManager().updates(parse(getDefaultFolderID()), clientLastModified, false);
        assertNotNull("no updates found on server", updates);
        assertTrue("no updated appointments on server", 0 < updates.size());
        for (Appointment appointment : appointments) {
            calendar.setTime(appointment.getStartDate());
            calendar.add(Calendar.DAY_OF_YEAR, 2);
            Date expectedStart = calendar.getTime();
            calendar.setTime(appointment.getEndDate());
            calendar.add(Calendar.DAY_OF_YEAR, 2);
            Date expectedEnd = calendar.getTime();
            Appointment exception = null;
            for (Appointment update : updates) {
                if (appointment.getObjectID() != update.getObjectID() && appointment.getUid().equals(update.getUid())) {
                    exception = update;
                    break;
                }
            }
            assertNotNull("Exception not found", exception);
            assertEquals("Title wrong", appointment.getTitle() + "_edit", exception.getTitle());
            assertEquals("Start date wrong", expectedStart, exception.getStartDate());
            assertEquals("End date wrong", expectedEnd, exception.getEndDate());
            assertEquals("Recurrence date position wrong", 3, exception.getRecurrencePosition());
        }
        /*
         * verify appointment series on client
         */
        eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        calendarData = super.calendarMultiget(eTags.keySet());
        for (Appointment appointment : appointments) {
            calendar.setTime(appointment.getStartDate());
            calendar.add(Calendar.DAY_OF_YEAR, 2);
            Date expectedStart = calendar.getTime();
            calendar.setTime(appointment.getEndDate());
            calendar.add(Calendar.DAY_OF_YEAR, 2);
            Date expectedEnd = calendar.getTime();
            ICalResource iCalResource = assertContains(appointment.getUid(), calendarData);
            assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
            assertEquals("No exception found in iCal", 2, iCalResource.getVEvents().size());
            for (Component vEvent : iCalResource.getVEvents()) {
                Date recurrenceID = vEvent.getRecurrenceID();
                if (null != recurrenceID) {
                    // exception
                    assertEquals("SUMMARY wrong", appointment.getTitle() + "_edit", vEvent.getSummary());
                    assertEquals("DTSTART wrong", expectedStart, vEvent.getDTStart());
                    assertEquals("DTEND wrong", expectedEnd, vEvent.getDTEnd());
                    assertEquals("RECURRENCE-ID wrong", expectedStart, recurrenceID);
                } else {
                    // master
                    assertEquals("SUMMARY wrong", appointment.getTitle(), vEvent.getSummary());
                    assertEquals("DTSTART wrong", appointment.getStartDate(), vEvent.getDTStart());
                    assertEquals("DTEND wrong", appointment.getEndDate(), vEvent.getDTEnd());
                }
            }

        }
	}

}
