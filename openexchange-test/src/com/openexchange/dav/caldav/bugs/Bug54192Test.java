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
import static org.junit.Assert.fail;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.UserAgents;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link Bug54192Test}
 *
 * Sync Conflict with emclient when dismissing appointment notification
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug54192Test extends CalDAVTest {

    @Override
    protected String getDefaultUserAgent() {
        return UserAgents.EM_CLIENT_6_0;
    }

    @Test
    public void testUpdateConcurrently() throws Exception {
        /*
         * fetch sync token for later synchronization
         */
        SyncToken syncToken = new SyncToken(fetchSyncToken());
        /*
         * create appointment on server
         */
        String uid = randomUID();
        Date start = TimeTools.D("next sunday at 12:30");
        Date end = TimeTools.D("next sunday at 14:30");
        Appointment appointment = create(generateAppointment(start, end, uid, "test", "location"));
        /*
         * verify appointment on client
         */
        Map<String, String> eTags = syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<ICalResource> calendarData = calendarMultiget(eTags.keySet());
        ICalResource iCalResource = assertContains(uid, calendarData);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("SUMMARY wrong", appointment.getTitle(), iCalResource.getVEvent().getSummary());
        /*
         * update appointment on client concurrently & verify response codes
         */
        iCalResource.getVEvent().setSummary("test_edit");
        int[] responses = updateEventConcurrently(iCalResource, 10);
        for (int response : responses) {
            assertTrue("Unexpected response: HTTP " + response,
                HttpServletResponse.SC_CREATED == response || HttpServletResponse.SC_PRECONDITION_FAILED == response);
        }
        /*
         * verify appointment on server
         */
        appointment = getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        assertEquals("Title wrong", "test_edit", appointment.getTitle());
        /*
         * verify appointment on client
         */
        iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertEquals("SUMMARY wrong", "test_edit", iCalResource.getVEvent().getSummary());
    }

    private int[] updateEventConcurrently(final ICalResource iCalResource, int numThreads) throws Exception {
        Thread[] threads = new Thread[numThreads];
        final int[] responses = new int[threads.length];
        for (int i = 0; i < threads.length; i++) {
            final int index = i;
            threads[i] = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        responses[index] = putICalUpdate(iCalResource);
                    } catch (Exception e) {
                        fail(e.getMessage());
                    }
                }
            });
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }
        return responses;
    }

}
