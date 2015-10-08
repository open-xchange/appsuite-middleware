package com.openexchange.webdav;

import java.io.ByteArrayInputStream;
import java.util.Date;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.java.Charsets;

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
		String iCal =
		    "BEGIN:VCALENDAR\r\n" +
		    "VERSION:2.0\r\n" +
		    "PRODID:http://www.example.com/calendarapplication/\r\n" +
		    "METHOD:PUBLISH\r\n" +
		    "BEGIN:VEVENT\r\n" +
		    "UID:461092315540@example.com\r\n" +
		    "ORGANIZER;CN=\"Alice Balder, Example Inc.\":MAILTO:alice@example.com\r\n" +
		    "LOCATION:Somewhere\r\n" +
		    "SUMMARY:Eine Kurzinfo\r\n" +
		    "DESCRIPTION:Beschreibung des Termines\r\n" +
		    "CLASS:PUBLIC\r\n" +
		    "DTSTART:20060910T220000Z\r\n" +
		    "DTEND:20060919T215900Z\r\n" +
		    "DTSTAMP:20060812T125900Z\r\n" +
		    "END:VEVENT\r\n" +
		    "END:VCALENDAR\r\n"
	    ;
		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(iCal.getBytes(Charsets.UTF_8));
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

