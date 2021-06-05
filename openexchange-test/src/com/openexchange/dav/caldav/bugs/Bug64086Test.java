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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpState;
import org.apache.jackrabbit.webdav.client.methods.ReportMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.junit.Test;
import com.openexchange.dav.Config;
import com.openexchange.dav.PropertyNames;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.Abstract2UserCalDAVTest;
import com.openexchange.dav.caldav.reports.CalendarQueryReportInfo;
import com.openexchange.dav.caldav.reports.CompFilter;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * {@link Bug64086Test} - Calendar: Appointment gets deleted w/o action by organizer / Sync with caldav iOS
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.2
 */
public class Bug64086Test extends Abstract2UserCalDAVTest {

    @Test
    public void testReportWithChangeExceptions() throws Exception {
        /*
         * calendar-query request is returning several etags for the same uri
         */
        String uid = randomUID();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(TimeTools.D("next friday at noon", TimeZone.getTimeZone("Europe/Berlin")));
        Appointment appointment = new Appointment();
        appointment.setUid(uid);
        appointment.setTitle("Bug64086Test");
        appointment.setIgnoreConflicts(true);
        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setInterval(1);
        appointment.setRecurrenceCount(20);
        appointment.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        appointment.setEndDate(calendar.getTime());
        appointment.addParticipant(new UserParticipant(getClient().getValues().getUserId()));
        appointment.addParticipant(new UserParticipant(client2.getValues().getUserId()));
        appointment.setParentFolderID(catm.getPrivateFolder());
        catm.insert(appointment);
        Date clientLastModified = catm.getLastModification();
        /*
         * create change exceptions on server (by changing the user's participation status)
         */
        catm.confirm(appointment.getParentFolderID(), appointment.getObjectID(), clientLastModified, Appointment.TENTATIVE, "abc", 4);
        clientLastModified = catm.getLastModification();
        catm.confirm(appointment.getParentFolderID(), appointment.getObjectID(), clientLastModified, Appointment.TENTATIVE, "abc", 7);
        clientLastModified = catm.getLastModification();
        catm.confirm(appointment.getParentFolderID(), appointment.getObjectID(), clientLastModified, Appointment.TENTATIVE, "abc", 13);
        clientLastModified = catm.getLastModification();
        /*
         * issue calendar-query report
         */
        DavPropertyNameSet propertyNames = new DavPropertyNameSet();
        propertyNames.add(PropertyNames.GETETAG);
        CompFilter filter = new CompFilter("VCALENDAR", Collections.singletonList(new CompFilter("VEVENT", null)));
        CalendarQueryReportInfo reportInfo = new CalendarQueryReportInfo(filter, propertyNames, 1);
        String response = null;
        ReportMethod report = null;
        try {
            report = new ReportMethod(getBaseUri() + Config.getPathPrefix() + "/caldav/" + encodeFolderID(getDefaultFolderID()) + '/', reportInfo) {

                @Override
                protected void processResponseBody(HttpState httpState, HttpConnection httpConnection) {
                    // do nothing to still be able to get the response body as string
                }
            };
            assertEquals("unexpected http status", StatusCodes.SC_MULTISTATUS, getWebDAVClient().executeMethod(report));
            response = report.getResponseBodyAsString();
        } finally {
            release(report);
        }
        assertNotNull("No response", response);
        /*
         * check number of hrefs in response
         */
        int count = 0;
        int index = response.indexOf(uid);
        while (0 <= index) {
            count++;
            index = response.indexOf(uid, 1 + index);
        }
        assertEquals("Unexpected number of hrefs in response " + response, 1, count);
    }

}
