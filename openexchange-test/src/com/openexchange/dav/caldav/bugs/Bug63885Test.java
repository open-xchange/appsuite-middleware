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
import java.util.Date;
import java.util.TimeZone;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.jackrabbit.webdav.client.methods.PutMethod;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.CalendarTestManager;

/**
 * {@link Bug63885Test} - "New appointment" notification for declined appointment
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.2
 */
public class Bug63885Test extends CalDAVTest {

    private CalendarTestManager catm2;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        catm2 = new CalendarTestManager(getClient2());
        catm2.setFailOnError(true);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        try {
            if (null != catm2) {
                catm2.cleanUp();
            }
        } finally {
            super.tearDown();
        }
    }

    @Test
    public void testAcknowledgeAlarmOfDeletedEvent() throws Exception {
        /*
         * create appointment on server with alarm
         */
        String uid = randomUID();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(TimeTools.D("next week in the morning", TimeZone.getTimeZone("Europe/Zurich")));
        Appointment appointment = new Appointment();
        appointment.setUid(uid);
        appointment.setAlarm(15);
        appointment.setTitle("Bug63885Test");
        appointment.setIgnoreConflicts(true);
        appointment.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        appointment.setEndDate(calendar.getTime());
        appointment.addParticipant(new UserParticipant(catm2.getClient().getValues().getUserId()));
        appointment.addParticipant(new UserParticipant(getClient().getValues().getUserId()));
        appointment.setParentFolderID(catm.getPrivateFolder());
        appointment = catm.insert(appointment);
        /*
         * decline appointment as user b
         */
        catm2.confirm(catm2.getPrivateFolder(), appointment.getObjectID(), catm.getLastModification(), Appointment.DECLINE, "no");
        /*
         * get appointment via caldav as user a
         */
        ICalResource iCalResource = get(appointment.getUid());
        assertNotNull("Event not found via CalDAV", iCalResource);
        assertNotNull("Event alarm not found", iCalResource.getVEvent().getVAlarm());
        assertNotNull("No schedule tag for calendar resource", iCalResource.getScheduleTag());
        /*
         * delete appointment on server
         */
        catm.delete(appointment);
        /*
         * try to update event via caldav (acknowledge alarm)
         */
        iCalResource.getVEvent().getVAlarm().setProperty("ACKNOWLEDGED", formatAsUTC(new Date()));
        int responseCode;
        PutMethod put = null;
        try {
            put = new PutMethod(getBaseUri() + iCalResource.getHref());
            put.addRequestHeader("If-Schedule-Tag-Match", iCalResource.getScheduleTag());
            put.setRequestEntity(new StringRequestEntity(iCalResource.toString(), "text/calendar", null));
            responseCode = getWebDAVClient().executeMethod(put);
        } finally {
            release(put);
        }
        assertEquals(HttpServletResponse.SC_NOT_FOUND, responseCode);
    }

}
