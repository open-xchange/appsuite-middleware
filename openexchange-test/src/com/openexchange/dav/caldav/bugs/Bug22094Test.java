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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.Date;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.junit.Test;
import com.openexchange.dav.PropertyNames;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * {@link NewTest} - appointments can not be moved between calendars via iCal
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug22094Test extends CalDAVTest {

    @Test
    public void testMoveAppointment() throws Exception {
        /*
         * create target folder for move on server
         */
        FolderObject subfolder = super.createFolder("move test" + System.currentTimeMillis());
        String subfolderID = Integer.toString(subfolder.getObjectID());
        /*
         * create appointment in default folder on client
         */
        String uid = randomUID();
        Date start = TimeTools.D("next thursday at 7:15");
        Date end = TimeTools.D("next thursday at 11:30");
        String title = "move test";
        String iCal = "BEGIN:VCALENDAR" + "\r\n" + "VERSION:2.0" + "\r\n" + "PRODID:-//Apple Inc.//iCal 5.0.2//EN" + "\r\n" + "CALSCALE:GREGORIAN" + "\r\n" + "BEGIN:VTIMEZONE" + "\r\n" + "TZID:Europe/Berlin" + "\r\n" + "BEGIN:DAYLIGHT" + "\r\n" + "TZOFFSETFROM:+0100" + "\r\n" + "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU" + "\r\n" + "DTSTART:19810329T020000" + "\r\n" + "TZNAME:CEST" + "\r\n" + "TZOFFSETTO:+0200" + "\r\n" + "END:DAYLIGHT" + "\r\n" + "BEGIN:STANDARD" + "\r\n" + "TZOFFSETFROM:+0200" + "\r\n" + "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU" + "\r\n" + "DTSTART:19961027T030000" + "\r\n" + "TZNAME:CET" + "\r\n" + "TZOFFSETTO:+0100" + "\r\n" + "END:STANDARD" + "\r\n" + "END:VTIMEZONE" + "\r\n" + "BEGIN:VEVENT" + "\r\n" + "CREATED:" + formatAsUTC(new Date()) + "\r\n" + "UID:" + uid + "\r\n" + "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" + "TRANSP:OPAQUE" + "\r\n" + "CLASS:PUBLIC" + "\r\n" + "SUMMARY:" + title + "\r\n" + "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" + "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" + "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" + "SEQUENCE:1" + "\r\n" + "END:VEVENT" + "\r\n" + "END:VCALENDAR";
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putICal(uid, iCal));
        /*
         * verify appointment on server
         */
        Appointment appointment = super.getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        super.rememberForCleanUp(appointment);
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = super.get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        /*
         * move appointment on client
         */
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.move(iCalResource, subfolderID));
        /*
         * update etag from moved appointment
         */
        DavPropertyNameSet props = new DavPropertyNameSet();
        props.add(PropertyNames.GETETAG);
        PropFindMethod propFind = new PropFindMethod(getWebDAVClient().getBaseURI() + iCalResource.getHref(), DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_0);
        MultiStatusResponse[] responses = getWebDAVClient().doPropFind(propFind);
        assertNotNull("got no response", responses);
        assertTrue("got no responses", 1 == responses.length);
        String eTag = this.extractTextContent(PropertyNames.GETETAG, responses[0]);
        assertNotNull("got no ETag from response", eTag);
        iCalResource.setEtag(eTag);
        /*
         * update resource on target location again
         */
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putICalUpdate(iCalResource));
        /*
         * verify appointment on client
         */
        iCalResource = get(subfolderID, uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        /*
         * verify moved appointment on server
         */
        assertNull("appointment still in source folder on server", super.getAppointment(uid));
        appointment = super.getAppointment(subfolderID, uid);
        super.rememberForCleanUp(appointment);
        assertNotNull("appointment not found in target folder on server", appointment);
        assertEquals("folder ID wrong", subfolder.getObjectID(), appointment.getParentFolderID());
    }

}
