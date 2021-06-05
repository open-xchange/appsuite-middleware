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
import java.util.Date;
import org.junit.Test;
import com.google.common.io.BaseEncoding;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.java.Charsets;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * {@link Bug55653Test}
 *
 * Explicit removal of DTSTART not taken over for task updated via CalDAV
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class Bug55653Test extends CalDAVTest {

    @Override
    protected String encodeFolderID(String folderID) {
        return BaseEncoding.base64Url().omitPadding().encode(folderID.getBytes(Charsets.US_ASCII));
    }

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
