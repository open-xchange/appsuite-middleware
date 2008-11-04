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
import com.openexchange.ajax.contact.action.AllResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.tools.servlet.AjaxException;

public class Bug12212Test extends AbstractAJAXSession {
	final String bugname = "Test for bug 12212";
	
	public Bug12212Test(String name) {
		super(name);
	}
	
	public AppointmentObject createDailyRecurringAppointment(final TimeZone timezone, final int folderId){
		final Calendar calendar = TimeTools.createCalendar(timezone);
		final AppointmentObject series = new AppointmentObject();
		
		series.setTitle(bugname);
		series.setParentFolderID(folderId);
		series.setIgnoreConflicts(true);
		calendar.set(Calendar.HOUR_OF_DAY, 12);
		series.setStartDate(calendar.getTime());
		calendar.add(Calendar.HOUR, 1);
		series.setEndDate(calendar.getTime());
		series.setRecurrenceType(AppointmentObject.DAILY);
		series.setInterval(1);
		series.setOccurrence(5);
		return series;
	}
	
	public void shiftAppointmentDateOneHour(final AppointmentObject appointment, TimeZone tz){
		final Calendar calendar = TimeTools.createCalendar(tz);
		calendar.setTime(appointment.getStartDate());
		calendar.add(Calendar.HOUR, 1);
		appointment.setStartDate(calendar.getTime());
		
		calendar.setTime(appointment.getEndDate());
		calendar.add(Calendar.HOUR, 1);
		appointment.setEndDate(calendar.getTime());
	}

	@Test public void testMovingExceptionTwiceShouldNeitherCrashNorDuplicate() throws AjaxException, IOException, SAXException, JSONException, OXException{
		final AJAXClient client = getClient();
	    final int folderId = client.getValues().getPrivateAppointmentFolder();
	    final TimeZone tz = client.getValues().getTimeZone();
		final Calendar calendar = TimeTools.createCalendar(tz);

	    //create appointment
		final AppointmentObject appointmentSeries = createDailyRecurringAppointment(tz, folderId);
	    
		{//send appointment
			final InsertRequest request = new InsertRequest(appointmentSeries, tz);
        	final CommonInsertResponse response = client.execute(request);
        	appointmentSeries.setObjectID(response.getId());
        	appointmentSeries.setLastModified(response.getTimestamp());
		}
		try {
			//get one occurrence
	        final int recurrence_position = 3;
		    final AppointmentObject occurrence;
			{
				final GetRequest request= new GetRequest(folderId, appointmentSeries.getObjectID(), recurrence_position);
				final GetResponse response = client.execute(request);
				occurrence = response.getAppointment(tz);
			}
	        
			//make an exception out of the occurrence
	        AppointmentObject exception = new AppointmentObject();
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
				final UpdateRequest request = new UpdateRequest(exception, tz);
				final UpdateResponse response = client.execute(request);
				exceptionId = response.getId();
				appointmentSeries.setLastModified(response.getTimestamp());
			}
	
			{//assert no duplicate exists
				AllRequest request = new AllRequest(folderId, new int[]{AppointmentObject.TITLE}, exception.getStartDate(), exception.getEndDate());
				CommonAllResponse response = client.execute(request);
				Object[][] allAppointmentsWithinTimeframe = response.getArray();
				int countOfPotentialDuplicates = 0;
				for(Object[] arr: allAppointmentsWithinTimeframe){
					if(null != arr[0] && ((String)arr[0]).startsWith(bugname)){
						countOfPotentialDuplicates++;
					}
				}
				assertEquals("Should be only one occurrence of this appointment", Integer.valueOf(1), Integer.valueOf(countOfPotentialDuplicates));
			}
		} finally {
			//clean up
			DeleteRequest deleteRequest = new DeleteRequest(appointmentSeries);
			client.execute(deleteRequest);
		}
	} 
}
