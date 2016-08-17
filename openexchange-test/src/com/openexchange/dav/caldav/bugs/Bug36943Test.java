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
import java.io.IOException;
import java.util.Date;
import javax.servlet.http.HttpServletResponse;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.client.methods.PropPatchMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.junit.Test;
import org.w3c.dom.Document;
import com.openexchange.dav.PropertyNames;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.methods.CustomXmlRequestEntity;
import com.openexchange.dav.caldav.properties.SupportedCalendarComponentSetProperty;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.tasks.Task;

/**
 * {@link Bug36943Test}
 *
 * iOS emoticon causes database exception
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug36943Test extends CalDAVTest {

    @Test
    public void testVEventWithAstralSymbols() throws Exception {
        /*
         * create appointment
         */
        String uid = randomUID();
        Date start = TimeTools.D("next friday at 09:00");
        Date end = TimeTools.D("next friday at 10:00");
        String summary = "Pile of \uD83D\uDCA9 poo";
        String iCal = generateICal(start, end, uid, summary, "test");
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(uid, iCal));
        /*
         * verify appointment on server
         */
        String expectedTitle = summary.replaceAll("\uD83D\uDCA9", "");
        Appointment appointment = getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        rememberForCleanUp(appointment);
        assertEquals("Title wrong", expectedTitle, appointment.getTitle());
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No SUMMARY in iCal found", iCalResource.getVEvent().getSummary());
        assertEquals("SUMMARY wrong", expectedTitle, iCalResource.getVEvent().getSummary());
    }

    @Test
    public void testVTodoWithAstralSymbols() throws Exception {
        /*
         * create VTODO
         */
        String folderID = String.valueOf(getClient().getValues().getPrivateTaskFolder());
        String uid = randomUID();
        Date start = TimeTools.D("next friday at 09:00");
        Date end = TimeTools.D("next friday at 10:00");
        String summary = "Pile of \uD83D\uDCA9 poo";
        String iCal = generateVTodo(start, end, uid, summary, "test");
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(folderID, uid, iCal));
        /*
         * verify task on server
         */
        String expectedTitle = summary.replaceAll("\uD83D\uDCA9", "");
        Task task = getTask(folderID, uid);
        assertNotNull("task not found on server", task);
        assertEquals("Title wrong", expectedTitle, task.getTitle());
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = get(folderID, uid, null);
        assertNotNull("No VTODO in iCal found", iCalResource.getVTodo());
        assertEquals("UID wrong", uid, iCalResource.getVTodo().getUID());
        assertNotNull("No SUMMARY in iCal found", iCalResource.getVTodo().getSummary());
        assertEquals("SUMMARY wrong", expectedTitle, iCalResource.getVTodo().getSummary());
    }

    @Test
    public void testCreateCollectionWithAstralSymbols() throws Exception {
        /*
         * perform mkcalendar request
         */
        String uid = randomUID();
        String name = "Pile of \uD83D\uDCA9 poo " + randomUID();
        DavPropertySet setProperties = new DavPropertySet();
        setProperties.add(new SupportedCalendarComponentSetProperty(SupportedCalendarComponentSetProperty.Comp.VEVENT));
        setProperties.add(new DefaultDavProperty<String>(PropertyNames.DISPLAYNAME, name));
        mkCalendar(uid, setProperties);
        /*
         * verify calendar on server
         */
        String expectedName = name.replaceAll("\uD83D\uDCA9", "");
        FolderObject folder = super.getCalendarFolder(expectedName);
        assertNotNull("folder not found on server", folder);
        rememberForCleanUp(folder);
        assertEquals("folder name wrong", expectedName, folder.getFolderName());
        /*
         * verify calendar on client
         */
        DavPropertyNameSet props = new DavPropertyNameSet();
        props.add(PropertyNames.DISPLAYNAME);
        props.add(PropertyNames.RESOURCE_ID);
        props.add(PropertyNames.RESOURCETYPE);
        PropFindMethod propFind = new PropFindMethod(super.getWebDAVClient().getBaseURI() + "/caldav/",
                DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_1);
        MultiStatusResponse[] responses = super.getWebDAVClient().doPropFind(propFind);
        assertNotNull("got no response", responses);
        assertTrue("got no response", 0 < responses.length);
        MultiStatusResponse folderResponse = null;
        for (MultiStatusResponse response : responses) {
            if (response.getPropertyNames(HttpServletResponse.SC_OK).contains(PropertyNames.DISPLAYNAME)) {
                if (expectedName.equals(super.extractTextContent(PropertyNames.DISPLAYNAME, response))) {
                    folderResponse = response;
                    break;
                }

            }
        }
        assertNotNull("no response for new folder", folderResponse);
    }

    @Test
    public void testRenameCollectionWithAstralSymbols() throws Exception {
        /*
         * create calendar folder on server
         */
        String originalName = randomUID();
        FolderObject folder = createFolder(originalName);
        /*
         * verify calendar on client
         */
        DavPropertyNameSet props = new DavPropertyNameSet();
        props.add(PropertyNames.DISPLAYNAME);
        props.add(PropertyNames.RESOURCE_ID);
        props.add(PropertyNames.RESOURCETYPE);
        PropFindMethod propFind = new PropFindMethod(super.getWebDAVClient().getBaseURI() + "/caldav/",
                DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_1);
        MultiStatusResponse[] responses = super.getWebDAVClient().doPropFind(propFind);
        assertNotNull("got no response", responses);
        assertTrue("got no response", 0 < responses.length);
        MultiStatusResponse folderResponse = null;
        for (MultiStatusResponse response : responses) {
            if (response.getPropertyNames(HttpServletResponse.SC_OK).contains(PropertyNames.DISPLAYNAME)) {
                if (originalName.equals(super.extractTextContent(PropertyNames.DISPLAYNAME, response))) {
                    folderResponse = response;
                    break;
                }

            }
        }
        assertNotNull("no response for new folder", folderResponse);
        /*
         * rename folder on client
         */
        String newName = "Pile of \uD83D\uDCA9 poo " + randomUID();
        DavPropertySet setProperties = new DavPropertySet();
        setProperties.add(new DefaultDavProperty<String>(PropertyNames.DISPLAYNAME, newName));
        PropPatchMethod propPatch = new PropPatchMethod(getWebDAVClient().getBaseURI() + folderResponse.getHref(), setProperties, new DavPropertyNameSet()) {

            @Override
            public void setRequestBody(Document requestBody) throws IOException {
                setRequestEntity(new CustomXmlRequestEntity(requestBody, "UTF-16"));
            }
        };
        responses = super.getWebDAVClient().doPropPatch(propPatch, StatusCodes.SC_MULTISTATUS);
        assertNotNull("got no response", responses);
        assertTrue("got no response", 0 < responses.length);
        /*
         * verify calendar folder on server
         */
        String expectedName = newName.replaceAll("\uD83D\uDCA9", "");
        folder = getCalendarFolder(expectedName);
        assertNotNull("folder not found on server", folder);
        rememberForCleanUp(folder);
        assertEquals("folder name wrong", expectedName, folder.getFolderName());
        /*
         * verify calendar on client
         */
        props = new DavPropertyNameSet();
        props.add(PropertyNames.DISPLAYNAME);
        props.add(PropertyNames.RESOURCE_ID);
        props.add(PropertyNames.RESOURCETYPE);
        propFind = new PropFindMethod(getWebDAVClient().getBaseURI() + "/caldav/", DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_1);
        responses = getWebDAVClient().doPropFind(propFind);
        assertNotNull("got no response", responses);
        assertTrue("got no response", 0 < responses.length);
        folderResponse = null;
        for (MultiStatusResponse response : responses) {
            if (response.getPropertyNames(HttpServletResponse.SC_OK).contains(PropertyNames.DISPLAYNAME)) {
                if (expectedName.equals(super.extractTextContent(PropertyNames.DISPLAYNAME, response))) {
                    folderResponse = response;
                    break;
                }

            }
        }
        assertNotNull("no response for renamed folder", folderResponse);
    }

}
