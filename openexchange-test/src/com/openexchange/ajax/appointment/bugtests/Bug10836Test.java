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

package com.openexchange.ajax.appointment.bugtests;

import java.util.Date;
import java.util.TimeZone;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.ListRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.ListIDInt;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;

/**
 * Checks if the calendar has a vulnerability in the list request.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug10836Test extends AbstractAJAXSession {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Bug10836Test.class);

	/**
	 * Default constructor.
	 * @param name Name of the test.
	 */
	public Bug10836Test(final String name) {
		super(name);
	}

	/**
	 * Creates a private appointment with user A and tries to read it with user
	 * B through a list request.
	 * @throws Throwable if some exception occurs.
	 */
	public void testVulnerability() throws Throwable {
		final AJAXClient clientA = getClient();
		final AJAXClient clientB = new AJAXClient(User.User2);
		final int folderA = clientA.getValues().getPrivateAppointmentFolder();
		final int folderB = clientB.getValues().getPrivateAppointmentFolder();
		final TimeZone tz = clientA.getValues().getTimeZone();
		final Appointment app = new Appointment();
		app.setParentFolderID(folderA);
		app.setTitle("Bug10836Test");
		app.setStartDate(new Date(TimeTools.getHour(0, tz)));
		app.setEndDate(new Date(TimeTools.getHour(1, tz)));
		app.setIgnoreConflicts(true);
		final CommonInsertResponse insertR = clientA.execute(new InsertRequest(app, tz));
		try {
		    final ListIDs list = new ListIDs();
		    list.add(new ListIDInt(folderB, insertR.getId()));
			final CommonListResponse listR = clientB.execute(new ListRequest(list,
		        new int[] { Appointment.TITLE }, false));

			assertTrue(listR.hasError());
			/*
			for (Object[] obj1 : listR) {
				for (Object obj2 : obj1) {
					assertNull(obj2);
				}
			}
			*/
		} finally {
			clientA.execute(new DeleteRequest(insertR.getId(), folderA,
			    insertR.getTimestamp()));
		}
	}
}
