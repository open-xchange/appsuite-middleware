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

package com.openexchange.ajax.importexport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import org.json.JSONException;
import org.junit.Test;
import com.openexchange.ajax.importexport.actions.ICalExportRequest;
import com.openexchange.ajax.importexport.actions.ICalExportResponse;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.task.ManagedTaskTest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tasks.Task;

/**
 * {@link Bug56435Test_TaskStateRoundtrip}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10
 */
public class Bug56435Test_TaskStateRoundtrip extends ManagedTaskTest {

	private final String ical =
			  "BEGIN:VCALENDAR\n"
			+ "PRODID:Open-Xchange\n"
			+ "VERSION:2.0\n"
			+ "CALSCALE:GREGORIAN\n"
			+ "METHOD:PUBLISH\n"
			+ "BEGIN:VTODO\n"
			+ "DTSTAMP:20171212T141546Z\n"
			+ "SUMMARY:WAITING testTask1513088134715\n"
			+ "DTSTART:20171212T141542Z\n"
			+ "DUE:20171212T141542Z\n"
			+ "CLASS:PUBLIC\n"
			+ "STATUS;X-RANDOM-STATUS=PARALYZED:CANCELLED\n"
			+ "UID:573f19d6-645d-45f7-9f47-e93dedae57c9\n"
			+ "CREATED:20171212T141545Z\n"
			+ "LAST-MODIFIED:20171212T141545Z\n"
			+ "END:VTODO\n"
			+ "END:VCALENDAR";

	@Test
	public void testTaskWaitingStateRoundtrip() throws OXException, IOException, JSONException {
		final String title = "WAITING testTask" + System.currentTimeMillis();

        final Task taskObj = new Task();
        taskObj.setTitle(title);
        taskObj.setStartDate(new Date());
        taskObj.setEndDate(new Date());
        taskObj.setParentFolderID(folderID);
        taskObj.setStatus(Task.WAITING);

        ttm.insertTaskOnServer(taskObj);

        ICalExportResponse response = getClient().execute(new ICalExportRequest(folderID));
        String iCal = response.getICal();
        assertTrue(iCal.contains(title));
        assertTrue(iCal.contains("STATUS;X-OX-STATUS=WAITING:CANCELLED"));

        Task[] tasks = ttm.getAllTasksOnServer(folderID);
        assertEquals(1, tasks.length);
        Task task = tasks[0];
        task.setParentFolderID(folderID);
        ttm.deleteTaskOnServer(task);

        final ICalImportRequest request = new ICalImportRequest(folderID, new ByteArrayInputStream(iCal.toString().getBytes(com.openexchange.java.Charsets.UTF_8)), false);
        getClient().execute(request);

        tasks = ttm.getAllTasksOnServer(folderID);
        assertEquals(1, tasks.length);
        task = tasks[0];
        assertEquals(4, task.getStatus());
	}

	@Test
	public void testTaskStatusWithUnknownParameters() throws OXException, IOException, JSONException {
		final ICalImportRequest request = new ICalImportRequest(folderID, new ByteArrayInputStream(ical.toString().getBytes(com.openexchange.java.Charsets.UTF_8)), false);
		getClient().execute(request);

		Task[] tasks = ttm.getAllTasksOnServer(folderID);
		assertEquals(1, tasks.length);
        Task task = tasks[0];
        assertEquals(Task.DEFERRED, task.getStatus());
	}

}
