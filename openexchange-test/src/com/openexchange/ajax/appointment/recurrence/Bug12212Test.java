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

package com.openexchange.ajax.appointment.recurrence;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;
import org.json.JSONException;
import org.junit.Test;
import org.xml.sax.SAXException;
import com.openexchange.ajax.appointment.action.AllRequest;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;

public class Bug12212Test extends AbstractAJAXSession {
	final String bugname = "Test for bug 12212";

	public Bug12212Test(final String name) {
		super(name);
	}

	public Appointment createDailyRecurringAppointment(final TimeZone timezone, final int folderId){
		final Calendar calendar = TimeTools.createCalendar(timezone);
		final Appointment series = new Appointment();

		series.setTitle(bugname);
		series.setParentFolderID(folderId);
		series.setIgnoreConflicts(true);
		calendar.set(Calendar.HOUR_OF_DAY, 12);
		series.setStartDate(calendar.getTime());
		calendar.add(Calendar.HOUR, 1);
		series.setEndDate(calendar.getTime());
		series.setRecurrenceType(Appointment.DAILY);
		series.setInterval(1);
		series.setOccurrence(5);
		return series;
	}

	public void shiftAppointmentDateOneHour(final Appointment appointment, final TimeZone tz){
		final Calendar calendar = TimeTools.createCalendar(tz);
		calendar.setTime(appointment.getStartDate());
		calendar.add(Calendar.HOUR, 1);
		appointment.setStartDate(calendar.getTime());

		calendar.setTime(appointment.getEndDate());
		calendar.add(Calendar.HOUR, 1);
		appointment.setEndDate(calendar.getTime());
	}

	@Test public void testMovingExceptionTwiceShouldNeitherCrashNorDuplicate() throws OXException, IOException, SAXException, JSONException, OXException{
		final AJAXClient client = getClient();
	    final int folderId = client.getValues().getPrivateAppointmentFolder();
	    final TimeZone tz = client.getValues().getTimeZone();

	    //create appointment
		final Appointment appointmentSeries = createDailyRecurringAppointment(tz, folderId);

		{//send appointment
			final InsertRequest request = new InsertRequest(appointmentSeries, tz);
        	final CommonInsertResponse response = client.execute(request);
        	appointmentSeries.setObjectID(response.getId());
        	appointmentSeries.setLastModified(response.getTimestamp());
		}
		try {
			//get one occurrence
	        final int recurrence_position = 3;
		    final Appointment occurrence;
			{
				final GetRequest request= new GetRequest(folderId, appointmentSeries.getObjectID(), recurrence_position);
				final GetResponse response = client.execute(request);
				occurrence = response.getAppointment(tz);
			}

			//make an exception out of the occurrence
	        Appointment exception = new Appointment();
	        exception.setObjectID(occurrence.getObjectID());
	        exception.setParentFolderID(folderId);
	        exception.setLastModified(occurrence.getLastModified());
	        exception.setRecurrencePosition(occurrence.getRecurrencePosition());
			exception.setTitle(occurrence.getTitle() + "-changed");
	        exception.setIgnoreConflicts(true);
			exception.setStartDate(occurrence.getStartDate());
			exception.setEndDate(occurrence.getEndDate());
			//move the exception one hour
			shiftAppointmentDateOneHour(exception, tz);
			//send exception
			int exceptionId;
			{
				final UpdateRequest request = new UpdateRequest(exception, tz);
				final UpdateResponse response = client.execute(request);
				exceptionId = response.getId();
				appointmentSeries.setLastModified(response.getTimestamp());
			}
			{//get exception
				final GetRequest request = new GetRequest(folderId, exceptionId);
		    	final GetResponse response = client.execute(request);
	        	exception = response.getAppointment(tz);
			}
			//move it again
			shiftAppointmentDateOneHour(exception, tz);

			{//send exception again
				exception.setIgnoreConflicts(true);
				final UpdateRequest request = new UpdateRequest(exception, tz);
				final UpdateResponse response = client.execute(request);
				exceptionId = response.getId();
				appointmentSeries.setLastModified(response.getTimestamp());
			}

			{//assert no duplicate exists
				final AllRequest request = new AllRequest(folderId, new int[] { Appointment.TITLE , Appointment.START_DATE, Appointment.END_DATE},
						exception.getStartDate(), exception.getEndDate(),
						tz, false);
				final CommonAllResponse response = client.execute(request);
				final Object[][] allAppointmentsWithinTimeframe = response.getArray();
				int countOfPotentialDuplicates = 0;
				for(final Object[] arr: allAppointmentsWithinTimeframe){
					if(null != arr[0] && ((String)arr[0]).startsWith(bugname)){
						countOfPotentialDuplicates++;
					}
				}
				assertEquals("Should be only one occurrence of this appointment", Integer.valueOf(1), Integer.valueOf(countOfPotentialDuplicates));
			}
		} finally {
			//clean up
		    final GetRequest request= new GetRequest(folderId, appointmentSeries.getObjectID(), true);
		    appointmentSeries.setLastModified(client.execute(request).getTimestamp());
			final DeleteRequest deleteRequest = new DeleteRequest(appointmentSeries);
			client.execute(deleteRequest);
		}
	}

}
