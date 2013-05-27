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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.dav.caldav;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.MoveMethod;
import org.apache.jackrabbit.webdav.client.methods.PutMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.json.JSONException;
import com.openexchange.dav.Headers;
import com.openexchange.dav.PropertyNames;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.WebDAVTest;
import com.openexchange.dav.caldav.ical.SimpleICal.SimpleICalException;
import com.openexchange.dav.caldav.methods.MkCalendarMethod;
import com.openexchange.dav.caldav.reports.CalendarMultiGetReportInfo;
import com.openexchange.dav.reports.SyncCollectionResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.Charsets;
import com.openexchange.test.CalendarTestManager;

/**
 * {@link CalDAVTest} - Common base class for CalDAV tests
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class CalDAVTest extends WebDAVTest {

	protected static final int TIMEOUT = 10000;

	private CalendarTestManager testManager = null;
	private int folderId;

	public CalDAVTest(final String name) {
		super(name);
	}

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.folderId = this.getAJAXClient().getValues().getPrivateAppointmentFolder();
        this.testManager = new CalendarTestManager(this.getAJAXClient());
    	this.testManager.setFailOnError(true);
    }

    @Override
    protected void tearDown() throws Exception {
    	if (null != this.getManager()) {
    		this.getManager().cleanUp();
    	}
        super.tearDown();
    }

    /**
     * Gets the personal calendar folder id
     *
     * @return
     */
    protected String getDefaultFolderID() {
    	return Integer.toString(this.folderId);
    }

    protected FolderObject createFolder(String folderName) throws OXException, IOException, JSONException {
    	return createFolder(getFolder(this.folderId), folderName);
    }

    /**
     * Gets the underlying {@link CalendarTestManager} instance.
     *
     * @return
     */
    protected CalendarTestManager getManager() {
    	return this.testManager;
    }

    @Override
    protected String getDefaultUserAgent() {
    	return UserAgents.MACOS_10_7_3;
    }

    protected void delete(Appointment appointment) {
    	getManager().delete(appointment);
    }

	@Override
    protected String fetchSyncToken(String folderID) throws OXException, IOException, DavException {
		return super.fetchSyncToken("/caldav/" + folderID);
	}

	protected String fetchSyncToken() throws OXException, IOException, DavException {
		return fetchSyncToken(getDefaultFolderID());
	}

	@Override
    protected SyncCollectionResponse syncCollection(SyncToken syncToken, String folderID) throws OXException, IOException, DavException {
		return super.syncCollection(syncToken, "/caldav/" + folderID);
	}

	protected SyncCollectionResponse syncCollection(SyncToken syncToken) throws OXException, IOException, DavException {
		return this.syncCollection(syncToken, getDefaultFolderID());
	}

	protected List<ICalResource> calendarMultiget(Collection<String> hrefs) throws OXException, IOException, DavException, SimpleICalException {
		return calendarMultiget(getDefaultFolderID(), hrefs);
	}

	protected List<ICalResource> calendarMultiget(String folderID, Collection<String> hrefs) throws OXException, IOException, DavException, SimpleICalException {
		List<ICalResource> calendarData = new ArrayList<ICalResource>();
    	DavPropertyNameSet props = new DavPropertyNameSet();
    	props.add(PropertyNames.GETETAG);
    	props.add(PropertyNames.CALENDAR_DATA);
    	ReportInfo reportInfo = new CalendarMultiGetReportInfo(hrefs.toArray(new String[hrefs.size()]), props);
    	MultiStatusResponse[] responses = this.getWebDAVClient().doReport(reportInfo, getBaseUri() + "/caldav/" + folderID + "/");
        for (MultiStatusResponse response : responses) {
        	if (response.getProperties(StatusCodes.SC_OK).contains(PropertyNames.GETETAG)) {
	        	String href = response.getHref();
	        	assertNotNull("got no href from response", href);
	        	String data = this.extractTextContent(PropertyNames.CALENDAR_DATA, response);
	        	assertNotNull("got no address data from response", data);
	        	String eTag = this.extractTextContent(PropertyNames.GETETAG, response);
	        	assertNotNull("got no etag data from response", eTag);
	        	calendarData.add(new ICalResource(data, href, eTag));
        	}
		}
		return calendarData;
	}

	protected int putICal(String resourceName, String iCal) throws HttpException, IOException, OXException, URISyntaxException {
		return putICal(getDefaultFolderID(), resourceName, iCal);
	}

	protected int putICal(String folderID, String resourceName, String iCal) throws HttpException, IOException, OXException, URISyntaxException {
        PutMethod put = null;
        try {
            String href = "/caldav/" + folderID + "/" + urlEncode(resourceName) + ".ics";
            put = new PutMethod(getBaseUri() + href);
            put.addRequestHeader(Headers.IF_NONE_MATCH, "*");
            put.setRequestEntity(new StringRequestEntity(iCal, "text/calendar", null));
            return getWebDAVClient().executeMethod(put);
        } finally {
            release(put);
        }
	}

    protected int move(ICalResource iCalResource, String targetFolderID) throws OXException, HttpException, IOException {
        MoveMethod move = null;
        try {
            String targetHref = "/caldav/" + targetFolderID + "/" +
                    iCalResource.getHref().substring(1 + iCalResource.getHref().lastIndexOf('/'));
            move = new MoveMethod(getBaseUri() + iCalResource.getHref(), getBaseUri() + targetHref, false);
            if (null != iCalResource.getETag()) {
                move.addRequestHeader(Headers.IF_MATCH, iCalResource.getETag());
            }
            int status = getWebDAVClient().executeMethod(move);
            if (StatusCodes.SC_CREATED == status) {
                iCalResource.setHref(targetHref);
            }
            return status;
        } finally {
            release(move);
        }
    }

    protected void mkCalendar(String targetResourceName, DavPropertySet setProperties) throws OXException, HttpException, IOException {
        MkCalendarMethod mkCalendar = null;
        try {
            String targetHref = "/caldav/" + targetResourceName + '/';
            mkCalendar = new MkCalendarMethod(getBaseUri() + targetHref, setProperties);
            assertEquals("response code wrong", StatusCodes.SC_CREATED, getWebDAVClient().executeMethod(mkCalendar));
        } finally {
            release(mkCalendar);
        }
    }

	protected ICalResource get(String resourceName, String ifNoneMatchEtag) throws HttpException, IOException, OXException, URISyntaxException, SimpleICalException {
		return get(getDefaultFolderID(), resourceName, ifNoneMatchEtag);
	}

	protected ICalResource get(String folderID, String resourceName, String ifNoneMatchEtag) throws HttpException, IOException, OXException, URISyntaxException, SimpleICalException {
		GetMethod get = null;
        try {
            String href = "/caldav/" + folderID + "/" + urlEncode(resourceName) + ".ics";
            get = new GetMethod(getBaseUri() + href);
            get.addRequestHeader(Headers.IF_NONE_MATCH, null != ifNoneMatchEtag ? ifNoneMatchEtag : "*");
            assertEquals("response code wrong", StatusCodes.SC_OK, getWebDAVClient().executeMethod(get));
            byte[] responseBody = get.getResponseBody();
            assertNotNull("got no response body", responseBody);
            return new ICalResource(new String(responseBody, Charsets.UTF_8), href, get.getResponseHeader("ETag").getValue());
        } finally {
            release(get);
        }
	}

	private static String urlEncode(String name) throws URISyntaxException {
		return new URI(null, name, null).toString();
	}

	protected int putICalUpdate(String resourceName, String iCal, String ifMatchEtag) throws HttpException, IOException, OXException, URISyntaxException {
		return this.putICalUpdate(getDefaultFolderID(), resourceName, iCal, ifMatchEtag);
	}

	protected int putICalUpdate(String folderID, String resourceName, String iCal, String ifMatchEtag) throws HttpException, IOException, OXException, URISyntaxException {
        PutMethod put = null;
        try {
            String href = "/caldav/" + folderID + "/" + urlEncode(resourceName) + ".ics";
            put = new PutMethod(getBaseUri() + href);
            if (null != ifMatchEtag) {
                put.addRequestHeader(Headers.IF_MATCH, ifMatchEtag);
            }
            put.setRequestEntity(new StringRequestEntity(iCal, "text/calendar", null));
            return getWebDAVClient().executeMethod(put);
        } finally {
            release(put);
        }
	}

	protected int putICalUpdate(ICalResource iCalResource) throws HttpException, IOException, OXException {
        PutMethod put = null;
        try {
            put = new PutMethod(getBaseUri() + iCalResource.getHref());
            if (null != iCalResource.getETag()) {
                put.addRequestHeader(Headers.IF_MATCH, iCalResource.getETag());
            }
            put.setRequestEntity(new StringRequestEntity(iCalResource.toString(), "text/calendar", null));
            return getWebDAVClient().executeMethod(put);
        } finally {
            release(put);
        }
	}

	protected Appointment getAppointment(String folderID, String uid) throws OXException {
		Appointment[] appointments = this.testManager.all(parse(folderID), new Date(0), new Date(100000000000000L),
				new int[] { Appointment.OBJECT_ID, Appointment.FOLDER_ID, Appointment.UID });
		for (Appointment appointment : appointments) {
			if (uid.equals(appointment.getUid())) {
				return testManager.get(appointment);
			}
		}
		return null;
	}

    /**
     * Remembers the supplied appointment for deletion after the test is
     * finished in the <code>tearDown()</code> method.
     *
     * @param appointment
     */
    protected void rememberForCleanUp(Appointment appointment) {
    	if (null != appointment) {
    		this.getManager().getCreatedEntities().add(appointment);
    	}
    }

	protected Appointment getAppointment(String uid) throws OXException {
		return getAppointment(getDefaultFolderID(), uid);
	}

	protected static int parse(String id) {
		return Integer.parseInt(id);
	}

    protected static String format(Date date, TimeZone timeZone) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmm'00'");
		dateFormat.setTimeZone(timeZone);
		return dateFormat.format(date);
    }

    protected static String format(Date date, String timeZoneID) {
    	return format(date, TimeZone.getTimeZone(timeZoneID));
    }

    protected static Appointment generateAppointment(Date start, Date end, String uid, String summary, String location) {
		Appointment appointment = new Appointment();
		appointment.setTitle(summary);
		appointment.setLocation(location);
		appointment.setStartDate(start);
		appointment.setEndDate(end);
		appointment.setUid(uid);
		return appointment;
    }

	protected static String generateICal(Date start, Date end, String uid, String summary, String location) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder
			.append("BEGIN:VCALENDAR").append("\r\n")
			.append("VERSION:2.0").append("\r\n")
			.append("PRODID:-//Apple Inc.//iCal 5.0.2//EN").append("\r\n")
			.append("CALSCALE:GREGORIAN").append("\r\n")
			.append("BEGIN:VTIMEZONE").append("\r\n")
			.append("TZID:Europe/Amsterdam").append("\r\n")
			.append("BEGIN:DAYLIGHT").append("\r\n")
			.append("TZOFFSETFROM:+0100").append("\r\n")
			.append("RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU").append("\r\n")
			.append("DTSTART:19810329T020000").append("\r\n")
			.append("TZNAME:CEST").append("\r\n")
			.append("TZOFFSETTO:+0200").append("\r\n")
			.append("END:DAYLIGHT").append("\r\n")
			.append("BEGIN:STANDARD").append("\r\n")
			.append("TZOFFSETFROM:+0200").append("\r\n")
			.append("RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU").append("\r\n")
			.append("DTSTART:19961027T030000").append("\r\n")
			.append("TZNAME:CET").append("\r\n")
			.append("TZOFFSETTO:+0100").append("\r\n")
			.append("END:STANDARD").append("\r\n")
			.append("END:VTIMEZONE").append("\r\n")
			.append("BEGIN:VEVENT").append("\r\n")
			.append("CREATED:").append(formatAsUTC(new Date())).append("\r\n")
		;
		if (null != uid) {
			stringBuilder.append("UID:").append(uid).append("\r\n");
		}
		if (null != end) {
			stringBuilder.append("DTEND;TZID=Europe/Amsterdam:").append(format(end, "Europe/Amsterdam")).append("\r\n");
		}
		stringBuilder.append("TRANSP:OPAQUE").append("\r\n");
		if (null != summary) {
			stringBuilder.append("SUMMARY:").append(summary).append("\r\n");
		}
		if (null != location) {
			stringBuilder.append("LOCATION:").append(location).append("\r\n");
		}
		if (null != start) {
			stringBuilder.append("DTSTART;TZID=Europe/Amsterdam:").append(format(start, "Europe/Amsterdam")).append("\r\n");
		}
		stringBuilder
			.append("DTSTAMP:").append(formatAsUTC(new Date())).append("\r\n")
			.append("SEQUENCE:0").append("\r\n")
			.append("END:VEVENT").append("\r\n")
			.append("END:VCALENDAR").append("\r\n")
		;

		return stringBuilder.toString();
	}

	public static void assertEquals(Appointment appointment, Date expectedStart, Date expectedEnd, String expectedUid,
			String expectedTitle, String expectedLocation) {
		assertNotNull("appointment is null", appointment);
		assertEquals("start date wrong", expectedStart, appointment.getStartDate());
		assertEquals("end date wrong", expectedEnd, appointment.getEndDate());
		assertEquals("uid wrong", expectedUid, appointment.getUid());
		assertEquals("title wrong", expectedTitle, appointment.getTitle());
		assertEquals("location wrong", expectedLocation, appointment.getLocation());
	}

    public static ICalResource assertContains(String uid, Collection<ICalResource> iCalResources) {
    	ICalResource match = null;
    	for (ICalResource iCalResource : iCalResources) {
    		if (uid.equals(iCalResource.getVEvent().getUID())) {
    			assertNull("duplicate match for UID '" + uid + "'", match);
    			match = iCalResource;
    		}
		}
    	assertNotNull("no iCal resource with UID '" + uid + "' found", match);
    	return match;
    }

	protected Appointment create(Appointment appointment) {
		return create(getDefaultFolderID(), appointment);
	}

    protected Appointment create(String folderID, Appointment appointment) {
		appointment.setParentFolderID(parse(folderID));
		appointment.setIgnoreConflicts(true);
		return getManager().insert(appointment);

	}


}
