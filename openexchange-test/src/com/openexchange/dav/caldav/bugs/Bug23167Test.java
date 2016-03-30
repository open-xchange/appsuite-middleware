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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.ical.SimpleICal.Component;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link Bug23167Test}
 *
 * Unable to calculate given position. Seems to be a delete exception or outside range
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug23167Test extends CalDAVTest {

	@Test
	public void testUpdateOldChangeException() throws Exception {
		/*
		 * fetch sync token for later synchronization
		 */
		SyncToken syncToken = new SyncToken(super.fetchSyncToken());
		/*
		 * create appointment series on server
		 */
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(TimeTools.D("One year ago in the morning", TimeZone.getTimeZone("Europe/Berlin")));
	    Appointment appointment = new Appointment();
	    appointment.setUid(randomUID());
	    appointment.setTitle("Bug23167Test");
	    appointment.setIgnoreConflicts(true);
        appointment.setRecurrenceType(Appointment.WEEKLY);
        appointment.setDays(1 << (calendar.get(Calendar.DAY_OF_WEEK) - 1));
        appointment.setInterval(1);
	    appointment.setStartDate(calendar.getTime());
	    calendar.add(Calendar.HOUR_OF_DAY, 2);
        appointment.setEndDate(calendar.getTime());
        super.create(appointment);
		Date clientLastModified = getManager().getLastModification();
        /*
         * create appointment exception on server
         */
		Appointment exception = new Appointment();
		exception.setTitle("Bug23167Test_edit");
		exception.setObjectID(appointment.getObjectID());
		exception.setRecurrencePosition(2);
		exception.setLastModified(clientLastModified);
		exception.setParentFolderID(appointment.getParentFolderID());
        super.getManager().update(exception);
        clientLastModified = getManager().getLastModification();
        /*
         * verify appointment series on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<ICalResource> calendarData = super.calendarMultiget(eTags.keySet());
        ICalResource iCalResource = assertContains(appointment.getUid(), calendarData);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("No exception found in iCal", 2, iCalResource.getVEvents().size());
        Component vEventException = null;
        for (Component vEvent : iCalResource.getVEvents()) {
            Date recurrenceID = vEvent.getRecurrenceID();
            if (null != recurrenceID) {
                // exception
                assertEquals("SUMMARY wrong", exception.getTitle(), vEvent.getSummary());
                vEventException = vEvent;
            } else {
                // master
                assertEquals("SUMMARY wrong", appointment.getTitle(), vEvent.getSummary());
            }
        }
        /*
         * update exception on client
         */
        vEventException.setProperty("SUMMARY", vEventException.getPropertyValue("SUMMARY") + "_edit2");
        vEventException.setProperty("DTSTAMP", formatAsUTC(new Date()));
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putICalUpdate(iCalResource));
        /*
         * verify exception on server
         */
        List<Appointment> updates = super.getManager().updates(parse(getDefaultFolderID()), clientLastModified, false);
        assertNotNull("no updates found on server", updates);
        assertTrue("no updated appointments on server", 0 < updates.size());
        exception = null;
        for (Appointment update : updates) {
            if (appointment.getObjectID() != update.getObjectID() && appointment.getUid().equals(update.getUid())) {
                exception = update;
                break;
            }
        }
        assertNotNull("Exception not found", exception);
        assertEquals("Title wrong", vEventException.getSummary(), exception.getTitle());
	}

}


