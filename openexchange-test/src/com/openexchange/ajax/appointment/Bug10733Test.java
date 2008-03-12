package com.openexchange.ajax.appointment;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.InsertResponse;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.groupware.container.AppointmentObject;

public class Bug10733Test extends AppointmentTest {
	
	private static final Log LOG = LogFactory.getLog(Bug10733Test.class);
	
	public Bug10733Test(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testDummy() {
		
	}
	
	public void testBug10733() throws Exception {
		final StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("testBug10733");
		stringBuffer.append(" - ");
		stringBuffer.append("012345678901234567890123456789012345678901234567890123456789"); // 60 chars
		stringBuffer.append("012345678901234567890123456789012345678901234567890123456789"); // 60 chars
		stringBuffer.append("012345678901234567890123456789012345678901234567890123456789"); // 60 chars
		stringBuffer.append("012345678901234567890123456789012345678901234567890123456789"); // 60 chars
		stringBuffer.append("012345678901234567890123456789012345678901234567890123456789"); // 60 chars
	
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle(stringBuffer.toString());
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);

		final AJAXSession ajaxSession = new AJAXSession(getWebConversation(), getSessionId());
		final InsertRequest insertRequest = new InsertRequest(appointmentObj, timeZone, false);

		final InsertResponse insertResponse = (InsertResponse) Executor.execute(ajaxSession, insertRequest);
		final boolean hasError = insertResponse.hasError();
		assertTrue("error message expected", hasError);
		
		final JSONObject jsonObj = insertResponse.getResponse().getJSON();
		
		final String errorCode = jsonObj.getString("code");
		
		assertEquals("unexpected error message", "APP-0005", errorCode);
	}
}
