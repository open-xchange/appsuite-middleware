package com.openexchange.webdav;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.TimeZone;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.tools.versit.Versit;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.OXContainerConverter;

public class ICalTest extends AbstractWebdavTest {

	protected Date startTime = null;

	protected Date endTime = null;

	private static final String ICAL_URL = "/servlet/webdav.ical";

	public ICalTest(final String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testUpload() throws Exception {
		final WebRequest initRequest = new GetMethodWebRequest(PROTOCOL + hostName + ICAL_URL);
		initRequest.setHeaderField(AUTHORIZATION, "Basic " + getAuthData(login, password, context));
		final WebResponse initresponse = webCon.getResponse(initRequest);

		assertEquals(200, initresponse.getResponseCode());

		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testUpload");
		appointmentObj.setStartDate(new Date());
		appointmentObj.setEndDate(new Date());

		final Task taskObj = new Task();
		taskObj.setTitle("testUpload");
		taskObj.setStartDate(new Date());
		taskObj.setEndDate(new Date());

		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		final VersitDefinition def = Versit.getDefinition("text/calendar");
		final VersitDefinition.Writer w = def.getWriter(byteArrayOutputStream, "UTF-8");
		final VersitObject ical = OXContainerConverter.newCalendar("2.0");
		def.writeProperties(w, ical);
		final VersitDefinition eventDef = def.getChildDef("VEVENT");
		def.getChildDef("VTODO");

		final OXContainerConverter oxContainerConverter = new OXContainerConverter(TimeZone.getDefault(), "t@t.de");

		final VersitObject versitAppointment = oxContainerConverter.convertAppointment(appointmentObj);

		eventDef.write(w, versitAppointment);

		final VersitObject versitTask = oxContainerConverter.convertTask(taskObj);

		eventDef.write(w, versitTask);

		def.writeEnd(w, ical);

		w.flush();
		byteArrayOutputStream.flush();

		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
		final WebRequest req = new PutMethodWebRequest(PROTOCOL + hostName + ICAL_URL, byteArrayInputStream, "text/calendar");
		req.setHeaderField(AUTHORIZATION, "Basic " + getAuthData(login, password, context));
		final WebResponse resp = webCon.getResponse(req);

		assertEquals(200, resp.getResponseCode());
	}

	public void testDownload() throws Exception {
		final WebRequest req = new GetMethodWebRequest(PROTOCOL + hostName + ICAL_URL);
		req.setHeaderField(AUTHORIZATION, "Basic " + getAuthData(login, password, context));
		final WebResponse resp = webCon.getResponse(req);

		assertEquals(200, resp.getResponseCode());
	}
}

