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
import static org.junit.Assert.assertNull;
import java.util.Date;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.tasks.Task;

/**
 * {@link Bug55653Test}
 *
 * Explicit removal of DTSTART not taken over for task updated via CalDAV
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class Bug55653Test extends CalDAVTest {

    @Test
    public void testCreateVTodoWithoutStart() throws Exception {
        /*
         * create VTODO
         */
        String folderID = String.valueOf(getClient().getValues().getPrivateTaskFolder());
        String uid = randomUID();
        Date end = TimeTools.D("next friday at 10:00");
        String iCal = generateVTodo(null, end, uid, "test", "test");
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(folderID, uid, iCal));
        /*
         * verify task on server
         */
        Task task = getTask(folderID, uid);
        assertNotNull("task not found on server", task);
        assertNull(task.getStartDate());
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = get(folderID, uid, null);
        assertNotNull("No VTODO in iCal found", iCalResource.getVTodo());
        assertEquals("UID wrong", uid, iCalResource.getVTodo().getUID());
        assertNull("DTSTART found", iCalResource.getVTodo().getDTStart());
    }

    @Test
    public void testRemoveStartFromVTodo() throws Exception {
        /*
         * create VTODO
         */
        String folderID = String.valueOf(getClient().getValues().getPrivateTaskFolder());
        String uid = randomUID();
        Date start = TimeTools.D("next friday at 09:00");
        Date end = TimeTools.D("next friday at 10:00");
        String iCal = generateVTodo(start, end, uid, "test", "test");
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(folderID, uid, iCal));
        /*
         * verify task on server
         */
        Task task = getTask(folderID, uid);
        assertNotNull("task not found on server", task);
        assertNotNull(task.getStartDate());
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = get(folderID, uid, null);
        assertNotNull("No VTODO in iCal found", iCalResource.getVTodo());
        assertEquals("UID wrong", uid, iCalResource.getVTodo().getUID());
        assertNotNull("No DTSTART found", iCalResource.getVTodo().getDTStart());
        /*
         * remove DTSTART on client
         */
        iCalResource.getVTodo().removeProperties("DTSTART");
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(iCalResource));
        /*
         * verify task on server
         */
        task = getTask(folderID, uid);
        assertNotNull("task not found on server", task);
        assertNull(task.getStartDate());
        /*
         * verify appointment on client
         */
        iCalResource = get(folderID, uid, null);
        assertNotNull("No VTODO in iCal found", iCalResource.getVTodo());
        assertEquals("UID wrong", uid, iCalResource.getVTodo().getUID());
        assertNull("DTSTART found", iCalResource.getVTodo().getDTStart());
    }

}
