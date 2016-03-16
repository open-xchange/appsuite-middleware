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

import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.webdav.xml.AppointmentTest;
import com.openexchange.webdav.xml.TaskTest;

public class ICalExportTest extends AbstractICalTest {

	public ICalExportTest(final String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testExportICalAppointment() throws Exception {
		final String title = "testExportICalAppointment" + System.currentTimeMillis();

		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle(title);
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setShownAs(Appointment.RESERVED);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);

		final int objectId = AppointmentTest.insertAppointment(getWebConversation(), appointmentObj, getHostName(), getLogin(), getPassword(), "");

		final Appointment[] appointmentArray = exportAppointment(getWebConversation(), appointmentFolderId, timeZone, getHostName(), getSessionId(), null);

		boolean found = false;
		for (int a = 0; a < appointmentArray.length; a++) {
			if ((null != appointmentArray[a].getTitle()) && (appointmentArray[a].getTitle().equals(title))) {
				found = true;
				appointmentObj.setUntil(appointmentArray[a].getUntil());
				appointmentArray[a].setParentFolderID(appointmentFolderId);
				AppointmentTest.compareObject(appointmentObj, appointmentArray[a]);
			}
		}

		assertTrue("appointment with title: " + title + " not found", found);

		AppointmentTest.deleteAppointment(getWebConversation(), objectId, appointmentFolderId, getHostName(), getLogin(), getPassword(), "");
	}

	public void testExportICalTask() throws Exception {
		final String title = "testExportICalTask" + System.currentTimeMillis();
		final Task taskObj = new Task();
		taskObj.setTitle(title);
		taskObj.setStartDate(startTime);
		taskObj.setEndDate(endTime);
		taskObj.setParentFolderID(taskFolderId);
		final int objectId = TaskTest.insertTask(getWebConversation(), taskObj, getHostName(), getLogin(), getPassword(), "");
		final Task[] taskArray = exportTask(getWebConversation(), taskFolderId, emailaddress, timeZone, getHostName(), getSessionId(), null);
		boolean found = false;
		for (int a = 0; a < taskArray.length; a++) {
			if (title.equals(taskArray[a].getTitle())) {
				found = true;
				taskObj.setStartDate(taskArray[a].getStartDate());
				taskArray[a].setParentFolderID(taskFolderId);
				TaskTest.compareObject(taskObj, taskArray[a]);
			}
		}
		assertTrue("task with id: " + objectId + " not found", found);
		TaskTest.deleteTask(getWebConversation(), objectId, taskFolderId, getHostName(), getLogin(), getPassword(), "");
	}
}
