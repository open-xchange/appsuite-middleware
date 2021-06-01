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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.dav.Config;
import com.openexchange.dav.PropertyNames;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.caldav.Abstract2UserCalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.reports.CalendarMultiGetReportInfo;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.common.groupware.calendar.TimeTools;
import com.openexchange.test.common.test.TestClassConfig;

/**
 * {@link Bug44309Test}
 *
 * CalDAV-Sync client keeps rerequesting non existing Object
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug44309Test extends Abstract2UserCalDAVTest {

    private CalendarTestManager manager2;
    private AJAXClient client3;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        manager2 = new CalendarTestManager(client2);
        manager2.setFailOnError(true);

        client3 = testContext.acquireUser().getAjaxClient();
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().createApiClient().withUserPerContext(3).useEnhancedApiClients().build();
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
        appointment.addParticipant(new UserParticipant(client3.getValues().getUserId()));
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
        exception.addParticipant(new UserParticipant(client3.getValues().getUserId()));
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
        MultiStatusResponse[] responses = getWebDAVClient().doReport(reportInfo, getBaseUri() + Config.getPathPrefix() + "/caldav/" + encodeFolderID(getDefaultFolderID()) + "/");
        assertNotNull(responses);
        assertEquals(1, responses.length);
        MultiStatusResponse response = responses[0];
        assertEquals(iCalResource.getHref(), response.getHref());
        assertTrue(null != response.getStatus() && 1 == response.getStatus().length && StatusCodes.SC_NOT_FOUND == response.getStatus()[0].getStatusCode());
    }

}
