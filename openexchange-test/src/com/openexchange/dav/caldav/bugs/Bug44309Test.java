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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.dav.PropertyNames;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.reports.CalendarMultiGetReportInfo;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.CalendarTestManager;

/**
 * {@link Bug44309Test}
 *
 * CalDAV-Sync client keeps rerequesting non existing Object
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug44309Test extends CalDAVTest {

    private CalendarTestManager manager2;

    @Before
    public void setUp() throws Exception {
        manager2 = new CalendarTestManager(new AJAXClient(User.User2));
        manager2.setFailOnError(true);
    }

    @After
    public void tearDown() throws Exception {
        if (null != this.manager2) {
            this.manager2.cleanUp();
            if (null != manager2.getClient()) {
                manager2.getClient().logout();
            }
        }
    }

    @Test
	public void testAccessDeletedSoleException() throws Exception {
		/*
		 * fetch sync token for later synchronization
		 */
		SyncToken syncToken = new SyncToken(fetchSyncToken());
		/*
		 * create appointment series on server as user b with user c
		 */
		String uid = randomUID();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(TimeTools.D("last month in the morning", TimeZone.getTimeZone("Europe/Berlin")));
	    Appointment appointment = new Appointment();
	    appointment.setUid(uid);
	    appointment.setTitle("Bug43521Test");
	    appointment.setIgnoreConflicts(true);
        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setInterval(1);
	    appointment.setStartDate(calendar.getTime());
	    calendar.add(Calendar.HOUR_OF_DAY, 1);
        appointment.setEndDate(calendar.getTime());
        appointment.addParticipant(new UserParticipant(manager2.getClient().getValues().getUserId()));
        appointment.addParticipant(new UserParticipant(new AJAXClient(User.User3).getValues().getUserId()));
        appointment.setParentFolderID(manager2.getPrivateFolder());
        manager2.insert(appointment);
		Date clientLastModified = manager2.getLastModification();
        /*
         * create change exception on server as user b, and invite user a there
         */
		Appointment exception = new Appointment();
		exception.setTitle("Bug43521Test_edit");
		exception.setObjectID(appointment.getObjectID());
		exception.setRecurrencePosition(2);
		exception.setLastModified(clientLastModified);
		exception.setParentFolderID(appointment.getParentFolderID());
        exception.addParticipant(new UserParticipant(manager2.getClient().getValues().getUserId()));
        exception.addParticipant(new UserParticipant(new AJAXClient(User.User3).getValues().getUserId()));
        exception.addParticipant(new UserParticipant(getClient().getValues().getUserId()));
        manager2.update(exception);
        clientLastModified = getManager().getLastModification();
        /*
         * verify appointment exception on client as user a
         */
        Map<String, String> eTags = syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<ICalResource> calendarData = calendarMultiget(eTags.keySet());
        ICalResource iCalResource = assertContains(uid, calendarData);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("SUMMARY wrong", exception.getTitle(), iCalResource.getVEvent().getSummary());
        /*
         * delete the change exception as user b
         */
        manager2.delete(exception);
        /*
         * try to access deleted resource as user a via plain GET
         */
        GetMethod get = null;
        try {
            get = new GetMethod(getBaseUri() + iCalResource.getHref());
            Assert.assertEquals("response code wrong", StatusCodes.SC_NOT_FOUND, getWebDAVClient().executeMethod(get));
        } finally {
            release(get);
        }
        /*
         * try to access deleted resource as user a via calendar-multiget
         */
        DavPropertyNameSet props = new DavPropertyNameSet();
        props.add(PropertyNames.GETETAG);
        props.add(PropertyNames.CALENDAR_DATA);
        ReportInfo reportInfo = new CalendarMultiGetReportInfo(new String[] { iCalResource.getHref() }, props);
        MultiStatusResponse[] responses = getWebDAVClient().doReport(reportInfo, getBaseUri() + "/caldav/" + getDefaultFolderID() + "/");
        assertNotNull(responses);
        assertEquals(1, responses.length);
        MultiStatusResponse response = responses[0];
        assertEquals(iCalResource.getHref(), response.getHref());
        assertTrue(null != response.getStatus() && 1 == response.getStatus().length && StatusCodes.SC_NOT_FOUND == response.getStatus()[0].getStatusCode());
	}

}


