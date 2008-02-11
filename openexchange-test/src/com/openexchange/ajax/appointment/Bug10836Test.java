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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.ajax.appointment;

import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.InsertResponse;
import com.openexchange.ajax.appointment.action.ListRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.api.OXPermissionException;
import com.openexchange.groupware.container.AppointmentObject;

/**
 * Checks if the calendar has a vulnerability in the list request.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug10836Test extends AbstractAJAXSession {

	private static final Log LOG = LogFactory.getLog(Bug10836Test.class);

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
		final AppointmentObject app = new AppointmentObject();
		app.setParentFolderID(folderA);
		app.setTitle("Bug10836Test");
		app.setStartDate(new Date(Tools.getHour(0)));
		app.setEndDate(new Date(Tools.getHour(1)));
		app.setIgnoreConflicts(true);
		final InsertResponse insertR = (InsertResponse) Executor.execute(
				clientA, new InsertRequest(app, tz));
		try {
			final CommonListResponse listR = (CommonListResponse) Executor
					.execute(clientB, new ListRequest(new int[][] { { folderB,
							insertR.getId() } },
							new int[] { AppointmentObject.TITLE }, false));

			assertTrue(listR.hasError());
			/*
			for (Object[] obj1 : listR) {
				for (Object obj2 : obj1) {
					assertNull(obj2);
				}
			}
			*/
		} finally {
			Executor.execute(clientA, new DeleteRequest(folderA, insertR
					.getId(), new Date()));
		}
	}
}
