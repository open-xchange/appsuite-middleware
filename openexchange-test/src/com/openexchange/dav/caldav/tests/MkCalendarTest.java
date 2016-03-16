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

package com.openexchange.dav.caldav.tests;

import static org.junit.Assert.*;
import javax.servlet.http.HttpServletResponse;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.OptionsMethod;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.junit.Test;
import com.openexchange.dav.PropertyNames;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.properties.SupportedCalendarComponentSetProperty;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link MkCalendarTest}
 *
 * Tests the MKCALENDAR command via the CalDAV interface
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class MkCalendarTest extends CalDAVTest {

    /**
     * Tests if the necessary MKCALENDAR method is listed in the 'Allow' response header.
     *
     * @throws Exception
     */
	@Test
    public void testOptions() throws Exception {
        OptionsMethod options = null;
        try {
            options = new OptionsMethod(getBaseUri());
            assertEquals("unexpected http status", StatusCodes.SC_OK, super.getWebDAVClient().executeMethod(options));
            assertResponseHeaders(new String[] { "MKCALENDAR" }, "Allow", options);
        } finally {
            release(options);
        }
    }

	@Test
    public void testCreateCalendar() throws Exception {
        /*
         * perform mkcalendar request
         */
        String uid = randomUID();
        String name = randomUID();
        DavPropertySet setProperties = new DavPropertySet();
        setProperties.add(new DefaultDavProperty<String>(PropertyNames.CALENDAR_COLOR, "#0E61B9FF"));
        setProperties.add(new SupportedCalendarComponentSetProperty(SupportedCalendarComponentSetProperty.Comp.VEVENT));
        setProperties.add(new DefaultDavProperty<String>(PropertyNames.DISPLAYNAME, name));
        setProperties.add(new DefaultDavProperty<Integer>(PropertyNames.CALENDAR_ORDER, 35));
        setProperties.add(new DefaultDavProperty<String>(PropertyNames.CALENDAR_TIMEZONE,
            "BEGIN:VCALENDAR\r\n" +
            "VERSION:2.0\r\n" +
            "PRODID:-//Apple Inc.//Mac OS X 10.8.2//EN\r\n" +
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
            "END:VCALENDAR\r\n"
        ));
        super.mkCalendar(uid, setProperties);
        /*
         * verify calendar on server
         */
        FolderObject folder = super.getCalendarFolder(name);
        assertNotNull("folder not found on server", folder);
        rememberForCleanUp(folder);
        assertEquals("folder name wrong", name, folder.getFolderName());
        /*
         * verify calendar on client
         */
        DavPropertyNameSet props = new DavPropertyNameSet();
        props.add(PropertyNames.ADD_MEMBER);
        props.add(PropertyNames.ALLOWED_SHARING_MODES);
        props.add(PropertyNames.BULK_REQUESTS);
        props.add(PropertyNames.CALENDAR_COLOR);
        props.add(PropertyNames.CALENDAR_DESCRIPTION);
        props.add(PropertyNames.CALENDAR_FREE_BUSY_SET);
        props.add(PropertyNames.CALENDAR_ORDER);
        props.add(PropertyNames.CALENDAR_TIMEZONE);
        props.add(PropertyNames.CURRENT_USER_PRIVILEGE_SET);
        props.add(PropertyNames.DEFAULT_ALARM_VEVENT_DATE);
        props.add(PropertyNames.DEFAULT_ALARM_VEVENT_DATETIME);
        props.add(PropertyNames.DISPLAYNAME);
        props.add(PropertyNames.GETCTAG);
        props.add(PropertyNames.OWNER);
        props.add(PropertyNames.PRE_PUBLISH_URL);
        props.add(PropertyNames.PUBLISH_URL);
        props.add(PropertyNames.PUSH_TRANSPORTS);
        props.add(PropertyNames.PUSHKEY);
        props.add(PropertyNames.QUOTA_AVAILABLE_BYTES);
        props.add(PropertyNames.QUOTA_USED_BYTES);
        props.add(PropertyNames.REFRESHRATE);
        props.add(PropertyNames.RESOURCE_ID);
        props.add(PropertyNames.RESOURCETYPE);
        props.add(PropertyNames.SCHEDULE_CALENDAR_TRANSP);
        props.add(PropertyNames.SCHEDULE_DEFAULT_CALENDAR_URL);
        props.add(PropertyNames.SOURCE);
        props.add(PropertyNames.SUBSCRIBED_STRIP_ALARMS);
        props.add(PropertyNames.SUBSCRIBED_STRIP_ATTACHMENTS);
        props.add(PropertyNames.SUBSCRIBED_STRIP_TODOS);
        props.add(PropertyNames.SUPPORTED_CALENDAR_COMPONENT_SET);
        props.add(PropertyNames.SUPPORTED_CALENDAR_COMPONENT_SETS);
        props.add(PropertyNames.SUPPORTED_REPORT_SET);
        props.add(PropertyNames.SYNC_TOKEN);
        props.add(PropertyNames.XMPP_SERVER);
        props.add(PropertyNames.XMPP_URI);
        PropFindMethod propFind = new PropFindMethod(super.getWebDAVClient().getBaseURI() + "/caldav/",
                DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_1);
        MultiStatusResponse[] responses = super.getWebDAVClient().doPropFind(propFind);
        assertNotNull("got no response", responses);
        assertTrue("got no response", 0 < responses.length);
        MultiStatusResponse folderResponse = null;
        for (MultiStatusResponse response : responses) {
            if (response.getPropertyNames(HttpServletResponse.SC_OK).contains(PropertyNames.DISPLAYNAME)) {
                if (name.equals(super.extractTextContent(PropertyNames.DISPLAYNAME, response))) {
                    folderResponse = response;
                    break;
                }

            }
        }
        assertNotNull("no response for new folder", folderResponse);
    }

}
