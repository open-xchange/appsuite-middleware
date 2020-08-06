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

import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.rmi.Naming;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.AJAXConfig.Property;
import com.openexchange.dav.Config;
import com.openexchange.dav.PropertyNames;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.java.Strings;

/**
 * {@link Bug67329Test}
 *
 * Caldav sync after time limit in com.openexchange.caldav.interval.end from client to OX possible
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.3
 */
public class Bug67329Test extends CalDAVTest {

    private Date configuredMinDateTime;
    private Date configuredMaxDateTime;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        /*
         * discover min- and max-date-time
         */
        DavPropertyNameSet props = new DavPropertyNameSet();
        props.add(PropertyNames.MIN_DATE_TIME);
        props.add(PropertyNames.MAX_DATE_TIME);
        String uri = getWebDAVClient().getBaseURI() + Config.getPathPrefix() + "/caldav/" + encodeFolderID(getDefaultFolderID());
        PropFindMethod propFind = new PropFindMethod(uri, DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_0);
        MultiStatusResponse[] responses = getWebDAVClient().doPropFind(propFind);
        assertNotNull("got no response", responses);
        MultiStatusResponse response = assertSingleResponse(responses);
        String minDateTime = (String) response.getProperties(StatusCodes.SC_OK).get(PropertyNames.MIN_DATE_TIME).getValue();
        if (Strings.isNotEmpty(minDateTime)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            configuredMinDateTime = dateFormat.parse(minDateTime);
        }
        String maxDateTime = (String) response.getProperties(StatusCodes.SC_OK).get(PropertyNames.MAX_DATE_TIME).getValue();
        if (Strings.isNotEmpty(maxDateTime)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            configuredMaxDateTime = dateFormat.parse(maxDateTime);
        }
        /*
         * ensure timerange checks are in "strict" mode
         */
        setAndCheckUserAttribute(getClient().getValues().getContextId(), getClient().getValues().getUserId(), "config", "com.openexchange.caldav.interval.strict", "true");
    }

    @Override
    public void tearDown() throws Exception {
        /*
         * reset timerange check strictness to defaults
         */
        setAndCheckUserAttribute(getClient().getValues().getContextId(), getClient().getValues().getUserId(), "config", "com.openexchange.caldav.interval.strict", null);
        super.tearDown();
    }

    private boolean setAndCheckUserAttribute(int contextId, int userId, String namespace, String attribute, String value) throws Exception {
        long timeout = System.currentTimeMillis() + 5000L;
        do {
            setUserAttribute(contextId, userId, namespace, attribute, value);
            if (Objects.equals(value, getUserAttribute(contextId, userId, namespace, attribute))) {
                return true;
            }
            Thread.sleep(500);
        } while (System.currentTimeMillis() < timeout);
        return false;
    }

    private String getUserAttribute(int contextId, int userId, String namespace, String attribute) throws Exception {
        com.openexchange.admin.rmi.dataobjects.Context context = new com.openexchange.admin.rmi.dataobjects.Context(I(contextId));
        com.openexchange.admin.rmi.dataobjects.User user = new com.openexchange.admin.rmi.dataobjects.User(userId);
        Credentials credentials = new Credentials(admin.getUser(), admin.getPassword());
        OXUserInterface iface = (OXUserInterface) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMI_HOST) + ":1099/" + OXUserInterface.RMI_NAME);
        User userData = iface.getData(context, user, credentials);
        return userData.getUserAttribute(namespace, attribute);
    }

    private void setUserAttribute(int contextId, int userId, String namespace, String attribute, String value) throws Exception {
        com.openexchange.admin.rmi.dataobjects.Context context = new com.openexchange.admin.rmi.dataobjects.Context(I(contextId));
        com.openexchange.admin.rmi.dataobjects.User user = new com.openexchange.admin.rmi.dataobjects.User(userId);
        Credentials credentials = new Credentials(admin.getUser(), admin.getPassword());
        OXUserInterface iface = (OXUserInterface) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMI_HOST) + ":1099/" + OXUserInterface.RMI_NAME);
        user.setUserAttribute(namespace, attribute, value);
        iface.change(context, user, credentials);
    }

    @Test
    public void testCreateInDistantFuture() throws Exception {
        /*
         * check if max-date-time is configured
         */
        Calendar calendar = Calendar.getInstance();
        if (null != configuredMaxDateTime) {
            calendar.setTime(configuredMaxDateTime);
            calendar.add(Calendar.DATE, 20);
        } else {
            calendar.setTime(TimeTools.D("in five years"));
        }
        /*
         * prepare event
         */
        String uid = randomUID();
        Date start = calendar.getTime();
        calendar.add(Calendar.HOUR, 1);
        Date end = calendar.getTime();
        String iCal = generateICal(start, end, uid, "Bug67329Test", null);
        if (null != configuredMaxDateTime) {
            /*
             * attempt to create event, expecting to fail
             */
            int statusCode = putICal(uid, iCal);
            if (StatusCodes.SC_FORBIDDEN != statusCode) {
                String value = getUserAttribute(getClient().getValues().getContextId(), getClient().getValues().getUserId(), "config", "com.openexchange.caldav.interval.strict");
                System.out.println("Bug67329Test fails with unexpected status code " + statusCode + ", \"com.openexchange.caldav.interval.strict\" is set to \"" + value + "\"");
            }
            assertEquals("response code wrong", StatusCodes.SC_FORBIDDEN, statusCode);
        } else {
            /*
             * attempt to create event
             */
            assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(uid, iCal));
            /*
             * verify appointment on server & client
             */
            Appointment appointment = super.getAppointment(uid);
            assertNotNull("appointment not found on server", appointment);
            rememberForCleanUp(appointment);
            ICalResource iCalResource = get(uid);
            assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
            assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        }
    }

    @Test
    public void testCreateInDistantPast() throws Exception {
        /*
         * check if min-date-time is configured
         */
        Calendar calendar = Calendar.getInstance();
        if (null != configuredMinDateTime) {
            calendar.setTime(configuredMinDateTime);
            calendar.add(Calendar.DATE, -20);
        } else {
            calendar.setTime(TimeTools.D("five years ago"));
        }
        /*
         * prepare event
         */
        String uid = randomUID();
        Date start = calendar.getTime();
        calendar.add(Calendar.HOUR, 1);
        Date end = calendar.getTime();
        String iCal = generateICal(start, end, uid, "Bug67329Test", null);
        if (null != configuredMinDateTime) {
            /*
             * attempt to create event, expecting to fail
             */
            int statusCode = putICal(uid, iCal);
            if (StatusCodes.SC_FORBIDDEN != statusCode) {
                String value = getUserAttribute(getClient().getValues().getContextId(), getClient().getValues().getUserId(), "config", "com.openexchange.caldav.interval.strict");
                System.out.println("Bug67329Test fails with unexpected status code " + statusCode + ", \"com.openexchange.caldav.interval.strict\" is set to \"" + value + "\"");
            }
            assertEquals("response code wrong", StatusCodes.SC_FORBIDDEN, statusCode);
        } else {
            /*
             * attempt to create event
             */
            assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(uid, iCal));
            /*
             * verify appointment on server & client
             */
            Appointment appointment = super.getAppointment(uid);
            assertNotNull("appointment not found on server", appointment);
            rememberForCleanUp(appointment);
            ICalResource iCalResource = get(uid);
            assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
            assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        }
    }

    @Test
    public void testCreateEndlessSeriesInDistantPast() throws Exception {
        /*
         * check if min-date-time is configured
         */
        Calendar calendar = Calendar.getInstance();
        if (null != configuredMinDateTime) {
            calendar.setTime(configuredMinDateTime);
            calendar.add(Calendar.DATE, -20);
        } else {
            calendar.setTime(TimeTools.D("five years ago"));
        }
        /*
         * prepare event
         */
        String uid = randomUID();
        Date start = calendar.getTime();
        calendar.add(Calendar.HOUR, 1);
        Date end = calendar.getTime();
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" +
            "VERSION:2.0\r\n" +
            "PRODID:-//Apple Inc.//Mac OS X 10.8.5//EN\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n" +
            "DTSTART:19810329T020000\r\n" +
            "TZNAME:MESZ\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n" +
            "DTSTART:19961027T030000\r\n" +
            "TZNAME:MEZ\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "RRULE:FREQ=YEARLY\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "CLASS:PUBLIC\r\n" +
            "SUMMARY:Bug67329Test\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        /*
         * attempt to create event
         */
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(uid, iCal));
        /*
         * verify appointment on server & client
         */
        Appointment appointment = super.getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        rememberForCleanUp(appointment);
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
    }

}
