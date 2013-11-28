package com.openexchange.ajax.appointment.bugtests;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.apache.commons.logging.LogFactory;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.tools.URLParameter;

public class Bug11250Test extends AppointmentTest {

	private final static int[] _appointmentFields = {
		DataObject.OBJECT_ID,
		CalendarObject.TITLE,
	};

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Bug11250Test.class);

	public Bug11250Test(final String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testBug11250() throws Exception {
		final FolderObject folderObj = com.openexchange.webdav.xml.FolderTest.createFolderObject(userId, "testBug11250" + System.currentTimeMillis(), FolderObject.CALENDAR, false);
		final int targetFolder = com.openexchange.webdav.xml.FolderTest.insertFolder(getWebConversation(), folderObj, getHostName(), getLogin(), getPassword(), "");

		Appointment appointmentObj = createAppointmentObject("testBug11250_1");
		appointmentObj.setIgnoreConflicts(true);
		appointmentObj.setParentFolderID(targetFolder);

		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		simpleDateFormat.setTimeZone(timeZone);

		final int year = Calendar.getInstance(timeZone).get(Calendar.YEAR)+1;

		final Date start = simpleDateFormat.parse(year + "-06-01 00:00:00");
		final Date end = simpleDateFormat.parse(year + "-06-05 00:00:00");

		Date startDate = simpleDateFormat.parse(year + "-06-01 10:00:00");
		Date endDate = simpleDateFormat.parse(year + "-06-01 11:00:00");

		appointmentObj.setStartDate(startDate);
		appointmentObj.setEndDate(endDate);

		final int objectId1 = insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());

		startDate = simpleDateFormat.parse(year + "-06-03 10:00:00");
		endDate = simpleDateFormat.parse(year + "-06-03 11:00:00");

		appointmentObj.setTitle("testBug11250_3");
		appointmentObj.setStartDate(startDate);
		appointmentObj.setEndDate(endDate);

		final int objectId3 = insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());

		appointmentObj = createAppointmentObject("testBug11250_2");
		appointmentObj.setIgnoreConflicts(true);
		appointmentObj.setParentFolderID(targetFolder);

		startDate = simpleDateFormat.parse(year + "-06-02 10:00:00");
		endDate = simpleDateFormat.parse(year + "-06-02 11:00:00");

		appointmentObj.setStartDate(startDate);
		appointmentObj.setEndDate(endDate);

		appointmentObj.setRecurrenceType(Appointment.DAILY);
		appointmentObj.setInterval(2);
		appointmentObj.setRecurrenceCount(2);

		final int objectId2 = insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());

		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, getSessionId());
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_ALL);
		parameter.setParameter(AJAXServlet.PARAMETER_START, start);
		parameter.setParameter(AJAXServlet.PARAMETER_END, end);
		parameter.setParameter(AJAXServlet.PARAMETER_COLUMNS, URLParameter.colsArray2String(_appointmentFields));
		parameter.setParameter(AJAXServlet.PARAMETER_SORT, CalendarObject.START_DATE);
		parameter.setParameter(AJAXServlet.PARAMETER_INFOLDER, targetFolder);

		final Appointment[] appointmentArray = listAppointment(getWebConversation(), _appointmentFields, parameter, timeZone, getHostName(), getSessionId());
		assertEquals("appointment array size not equals", 4, appointmentArray.length);

		assertEquals("appointment id at position 1 not equals", objectId1, appointmentArray[0].getObjectID());
		assertEquals("appointment id at position 2 not equals", objectId2, appointmentArray[1].getObjectID());
		assertEquals("appointment id at position 3 not equals", objectId3, appointmentArray[2].getObjectID());
		assertEquals("appointment id at position 4 not equals", objectId2, appointmentArray[3].getObjectID());

		com.openexchange.webdav.xml.FolderTest.deleteFolder(getWebConversation(), new int[] { targetFolder }, getHostName(), getLogin(), getPassword(), "");
	}
}
