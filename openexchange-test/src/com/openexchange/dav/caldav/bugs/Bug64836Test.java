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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpState;
import org.apache.jackrabbit.webdav.client.methods.ReportMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.junit.Test;
import com.openexchange.dav.Config;
import com.openexchange.dav.PropertyNames;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.reports.CalendarQueryReportInfo;
import com.openexchange.dav.caldav.reports.CompFilter;
import com.openexchange.dav.caldav.reports.TimeRangeFilter;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * {@link Bug64836Test} - "The requested appointment was not found." when editing a series
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.2
 */
public class Bug64836Test extends CalDAVTest {

    @Test
    public void testTimeRangeWithChangeExceptions() throws Exception {
        /*
         * calendar-query request is returning several etags for the same uri
         */
        String uid = randomUID();
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
        calendar.setTime(TimeTools.D("next friday at noon", TimeZone.getTimeZone("Europe/Berlin")));
        Appointment appointment = new Appointment();
        appointment.setUid(uid);
        appointment.setTitle("Bug64836Test");
        appointment.setIgnoreConflicts(true);
        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setInterval(1);
        appointment.setRecurrenceCount(2);
        appointment.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        appointment.setEndDate(calendar.getTime());
        appointment.setParentFolderID(catm.getPrivateFolder());
        catm.insert(appointment);
        Date clientLastModified = catm.getLastModification();
        /*
         * create change exceptions on server (by changing the user's participation status)
         */
        catm.confirm(appointment.getParentFolderID(), appointment.getObjectID(), clientLastModified, Appointment.TENTATIVE, "abc", 1);
        clientLastModified = catm.getLastModification();
        catm.confirm(appointment.getParentFolderID(), appointment.getObjectID(), clientLastModified, Appointment.TENTATIVE, "abc", 2);
        clientLastModified = catm.getLastModification();
        /*
         * issue calendar-query report, overlapping the first occurrence
         */
        calendar.setTime(appointment.getEndDate());
        String end = formatAsUTC(calendar.getTime());
        calendar.add(Calendar.DATE, -10);
        String start = formatAsUTC(calendar.getTime());
        DavPropertyNameSet propertyNames = new DavPropertyNameSet();
        propertyNames.add(PropertyNames.GETETAG);
        CompFilter filter = new CompFilter("VCALENDAR", Collections.singletonList(new CompFilter("VEVENT", null, new TimeRangeFilter(start, end))));
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
         * check that event resource was found
         */
        assertTrue("Event resource not found in response", response.contains(uid));
    }

    @Test
    public void testTimeRangeWithChangeExceptionsOutsideRange() throws Exception {
        /*
         * calendar-query request is returning several etags for the same uri
         */
        String uid = randomUID();
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
        calendar.setTime(TimeTools.D("next friday at noon", TimeZone.getTimeZone("Europe/Berlin")));
        Appointment appointment = new Appointment();
        appointment.setUid(uid);
        appointment.setTitle("Bug64836Test");
        appointment.setIgnoreConflicts(true);
        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setInterval(1);
        appointment.setRecurrenceCount(2);
        appointment.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        appointment.setEndDate(calendar.getTime());
        appointment.setParentFolderID(catm.getPrivateFolder());
        catm.insert(appointment);
        Date clientLastModified = catm.getLastModification();
        /*
         * create change exceptions on server (by changing the user's participation status)
         */
        catm.confirm(appointment.getParentFolderID(), appointment.getObjectID(), clientLastModified, Appointment.TENTATIVE, "abc", 1);
        clientLastModified = catm.getLastModification();
        catm.confirm(appointment.getParentFolderID(), appointment.getObjectID(), clientLastModified, Appointment.TENTATIVE, "abc", 2);
        clientLastModified = catm.getLastModification();
        /*
         * shift first change exception to a later time
         */
        List<Appointment> changeExceptions = catm.getChangeExceptions(appointment.getParentFolderID(), appointment.getObjectID(), Appointment.ALL_COLUMNS);
        Appointment changeException = new Appointment();
        changeException.setLastModified(clientLastModified);
        changeException.setObjectID(changeExceptions.get(0).getObjectID());
        changeException.setParentFolderID(changeExceptions.get(0).getParentFolderID());
        calendar.setTime(appointment.getStartDate());
        calendar.add(Calendar.HOUR_OF_DAY, 2);
        changeException.setStartDate(calendar.getTime());
        calendar.setTime(appointment.getEndDate());
        calendar.add(Calendar.HOUR_OF_DAY, 2);
        changeException.setEndDate(calendar.getTime());
        catm.update(changeException);
        /*
         * issue calendar-query report, overlapping the first (regular) occurrence
         */
        calendar.setTime(appointment.getEndDate());
        String end = formatAsUTC(calendar.getTime());
        calendar.add(Calendar.DATE, -10);
        String start = formatAsUTC(calendar.getTime());
        DavPropertyNameSet propertyNames = new DavPropertyNameSet();
        propertyNames.add(PropertyNames.GETETAG);
        CompFilter filter = new CompFilter("VCALENDAR", Collections.singletonList(new CompFilter("VEVENT", null, new TimeRangeFilter(start, end))));
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
         * check that no event resource was found
         */
        assertFalse("Event resource not found in response", response.contains(uid));
    }

    @Test
    public void testTimeRangeWithChangeExceptionsInsideRange() throws Exception {
        /*
         * calendar-query request is returning several etags for the same uri
         */
        String uid = randomUID();
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
        calendar.setTime(TimeTools.D("next friday at noon", TimeZone.getTimeZone("Europe/Berlin")));
        Appointment appointment = new Appointment();
        appointment.setUid(uid);
        appointment.setTitle("Bug64836Test");
        appointment.setIgnoreConflicts(true);
        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setInterval(1);
        appointment.setRecurrenceCount(2);
        appointment.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        appointment.setEndDate(calendar.getTime());
        appointment.setParentFolderID(catm.getPrivateFolder());
        catm.insert(appointment);
        Date clientLastModified = catm.getLastModification();
        /*
         * create change exceptions on server (by changing the user's participation status)
         */
        catm.confirm(appointment.getParentFolderID(), appointment.getObjectID(), clientLastModified, Appointment.TENTATIVE, "abc", 1);
        clientLastModified = catm.getLastModification();
        catm.confirm(appointment.getParentFolderID(), appointment.getObjectID(), clientLastModified, Appointment.TENTATIVE, "abc", 2);
        clientLastModified = catm.getLastModification();
        /*
         * shift first change exception to an earlier time
         */
        List<Appointment> changeExceptions = catm.getChangeExceptions(appointment.getParentFolderID(), appointment.getObjectID(), Appointment.ALL_COLUMNS);
        Appointment changeException = new Appointment();
        changeException.setLastModified(clientLastModified);
        changeException.setObjectID(changeExceptions.get(0).getObjectID());
        changeException.setParentFolderID(changeExceptions.get(0).getParentFolderID());
        calendar.setTime(appointment.getStartDate());
        calendar.add(Calendar.HOUR_OF_DAY, -4);
        changeException.setStartDate(calendar.getTime());
        calendar.setTime(appointment.getEndDate());
        calendar.add(Calendar.HOUR_OF_DAY, -4);
        changeException.setEndDate(calendar.getTime());
        catm.update(changeException);
        /*
         * issue calendar-query report, overlapping the change exception but not the 'regular' occurrences of the series
         */
        calendar.setTime(appointment.getStartDate());
        calendar.add(Calendar.HOUR_OF_DAY, -1);
        String end = formatAsUTC(calendar.getTime());
        calendar.add(Calendar.DATE, -10);
        String start = formatAsUTC(calendar.getTime());
        DavPropertyNameSet propertyNames = new DavPropertyNameSet();
        propertyNames.add(PropertyNames.GETETAG);
        CompFilter filter = new CompFilter("VCALENDAR", Collections.singletonList(new CompFilter("VEVENT", null, new TimeRangeFilter(start, end))));
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
         * check that the event resource was found
         */
        assertTrue("Event resource not found in response", response.contains(uid));
    }

}
