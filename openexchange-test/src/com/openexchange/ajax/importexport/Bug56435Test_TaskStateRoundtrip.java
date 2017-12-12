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
