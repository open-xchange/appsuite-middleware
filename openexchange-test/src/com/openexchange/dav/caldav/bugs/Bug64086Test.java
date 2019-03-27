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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpState;
import org.apache.jackrabbit.webdav.client.methods.ReportMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.junit.Test;
import com.openexchange.dav.PropertyNames;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.reports.CalendarQueryReportInfo;
import com.openexchange.dav.caldav.reports.CompFilter;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.UserParticipant;

/**
 * {@link Bug64086Test} - Calendar: Appointment gets deleted w/o action by organizer / Sync with caldav iOS
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.2
 */
public class Bug64086Test extends CalDAVTest {

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
        appointment.addParticipant(new UserParticipant(getClient2().getValues().getUserId()));
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
            report = new ReportMethod(getBaseUri() + "/caldav/" + encodeFolderID(getDefaultFolderID()) + '/', reportInfo) {

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
