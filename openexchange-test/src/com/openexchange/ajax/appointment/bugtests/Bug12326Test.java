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

import java.util.Calendar;
import java.util.TimeZone;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.ListRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;

/**
 * Checks if the data of recurring appointment exceptions is correctly stored
 * and given to the GUI.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug12326Test extends AbstractAJAXSession {

    private static final int[] columns = new int[] {
        Appointment.OBJECT_ID,
        Appointment.FOLDER_ID,
        Appointment.RECURRENCE_TYPE,
        Appointment.RECURRENCE_POSITION,
        Appointment.RECURRENCE_ID
    };

	/**
	 * Default constructor.
	 * @param name test name.
	 */
	public Bug12326Test(final String name) {
		super(name);
	}

	/**
	 * Creates an appointment series and modifies one appointment of the series
	 * to be an exception.
	 */
	public void testAppointmentException() throws Throwable {
        final AJAXClient client = getClient();
        final int folderId = client.getValues().getPrivateAppointmentFolder();
        final TimeZone tz = client.getValues().getTimeZone();
		final Appointment series = new Appointment();
        final Calendar calendar = TimeTools.createCalendar(tz);
		{
			series.setTitle("Test for bug 12326");
            series.setParentFolderID(folderId);
            series.setIgnoreConflicts(true);
            // Start and end date.
            calendar.set(Calendar.HOUR_OF_DAY, 12);
            series.setStartDate(calendar.getTime());
            calendar.add(Calendar.HOUR, 1);
            series.setEndDate(calendar.getTime());
            // Configure daily series with 5 occurences
            series.setRecurrenceType(Appointment.DAILY);
            series.setInterval(1);
            series.setOccurrence(5);
		}
		{
            final InsertRequest request = new InsertRequest(series, tz);
            final CommonInsertResponse response = client.execute(request);
            series.setObjectID(response.getId());
            series.setLastModified(response.getTimestamp());
		}
		try {
		    final int recurrence_position = 3;
			// Load third occurence
		    final Appointment occurence;
			{
				final GetRequest request= new GetRequest(folderId, series.getObjectID(), recurrence_position);
				final GetResponse response = client.execute(request);
				occurence = response.getAppointment(tz);
				assertEquals("Occurence must have a recurrence position.", recurrence_position, occurence.getRecurrencePosition());
			}
			// Create exception
			final int exceptionId;
			{
				final Appointment exception = new Appointment();
                exception.setObjectID(occurence.getObjectID());
                exception.setParentFolderID(folderId);
                exception.setLastModified(occurence.getLastModified());
                exception.setRecurrencePosition(occurence.getRecurrencePosition());
				exception.setTitle(occurence.getTitle() + "-changed");
	            exception.setIgnoreConflicts(true);
				calendar.setTime(occurence.getEndDate());
				exception.setStartDate(calendar.getTime());
				calendar.add(Calendar.HOUR, 1);
				exception.setEndDate(calendar.getTime());
				final UpdateRequest request = new UpdateRequest(exception, tz);
				final UpdateResponse response = client.execute(request);
				exceptionId = response.getId();
				series.setLastModified(response.getTimestamp());
			}
			// Check exception in get response
			{
			    final GetRequest request = new GetRequest(folderId, exceptionId);
			    final GetResponse response = client.execute(request);
	            final Appointment exception = response.getAppointment(tz);
	            series.setLastModified(exception.getLastModified());
	            // Check exception
	            assertEquals("Exception is still a series.", Appointment.NO_RECURRENCE, exception.getRecurrenceType());
	            assertEquals("Exception must have a recurrence position.", occurence.getRecurrencePosition(), exception.getRecurrencePosition());
	            assertEquals("Exception is missing reference to series.", series.getObjectID(), exception.getRecurrenceID());
			}
			// Check exception in list response
			{
			    final ListIDs ids = ListIDs.l(new int[] {folderId, exceptionId});
			    final ListRequest request = new ListRequest(ids, columns);
			    final CommonListResponse response = client.execute(request);
			    final Object[] data = response.getArray()[0];
	            assertEquals("Exception is still a series.", Integer.valueOf(Appointment.NO_RECURRENCE), data[2]);
	            assertEquals("Exception must have a recurrence position.", Integer.valueOf(occurence.getRecurrencePosition()), data[3]);
	            assertEquals("Exception is missing reference to series.", Integer.valueOf(series.getObjectID()), data[4]);
	            series.setLastModified(response.getTimestamp());
			}
		} finally {
            client.execute(new DeleteRequest(series.getObjectID(), folderId,
                series.getLastModified()));
		}
	}
}
