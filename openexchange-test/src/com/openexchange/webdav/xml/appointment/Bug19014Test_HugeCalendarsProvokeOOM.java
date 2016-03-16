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
package com.openexchange.webdav.xml.appointment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import com.meterware.httpunit.Base64;
import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.AJAXConfig.Property;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;

public class Bug19014Test_HugeCalendarsProvokeOOM extends ManagedAppointmentTest{

	private static final int MAX_NUM_APP = 200; // should be a multiple of the batch size
	private static final int BATCH_SIZE = 100;

	public Bug19014Test_HugeCalendarsProvokeOOM(String name) {
		super(name);
	}

	protected long insertAppointments(int numberOfApps, int batchSize) throws Exception {
		long start = new Date().getTime();
		StringBuilder ical = new StringBuilder();

		for(int i = 1; i < numberOfApps+1; i++){

			ical.append(
				"BEGIN:VEVENT\n" +
				"ORGANIZER:MAILTO:oxpro-a01@qs-c4.de\n" +
				"DTSTART;TZID=W. Europe Standard Time:20110510T170000\n" +
				"DTEND;TZID=W. Europe Standard Time:20110510T180000\n" +
				"DTSTAMP:20110510T113750Z\n" +
				"LAST-MODIFIED:20110510T113750Z\n" +
				"CLASS:PUBLIC\n");
			ical.append("SUMMARY:Bug 19014 Test Appointment #"+i+"/"+numberOfApps+"\n");
			ical.append("TITLE:Bug 19014 Test Appointment #"+i+"/"+numberOfApps+"\n");
			ical.append("END:VEVENT\n");

			if(i % batchSize == 0){
				ical.insert(0,
						"BEGIN:VCALENDAR\n" +
						"METHOD:REQUEST\n" +
						"PRODID:OX Test for Bug 19014\n" +
				"VERSION:2.0\n");
				ical.append("END:VCALENDAR");

				ICalImportResponse importResponse = getClient().execute(new ICalImportRequest(folder.getObjectID(), ical.toString()));
				assertFalse(importResponse.hasConflicts() || importResponse.hasError());
				ical = new StringBuilder();
			}
		}
		return (new Date().getTime() - start)/1000;
	}


	private HttpResponse makeTheCall(int folderId) throws OXException, JSONException, IOException {
		AJAXConfig.init();
		String login = AJAXConfig.getProperty(User.User1.getLogin());
		String context = AJAXConfig.getProperty(Property.CONTEXTNAME);
		String password = AJAXConfig.getProperty(User.User1.getPassword());

		DefaultHttpClient rawClient = getClient().getSession().getHttpClient();

		HttpUriRequest icalRequest = new HttpGet("http://localhost/servlet/webdav.ical?calendarfolder="+folderId);
		icalRequest.addHeader("authorization", "Basic " + Base64.encode(login + "@" + context + ":" + password));

		HttpResponse response = rawClient.execute(icalRequest );
		return response;
	}


	public void testHugeNumberOfAppointments() throws Exception{
		insertAppointments(MAX_NUM_APP,BATCH_SIZE);
		HttpResponse response = makeTheCall(folder.getObjectID());

		Pattern pattern = Pattern.compile("Bug 19014 Test Appointment #(\\d+)/"+MAX_NUM_APP);
		BufferedReader ical = new BufferedReader(new InputStreamReader(response.getEntity().getContent()), 65536);
		String line = null;
		int numTzDefs = 0;
		Set<Integer> number = new HashSet<Integer>();
		while((line = ical.readLine()) != null){
			// System.out.println(line);
			Matcher matcher = pattern.matcher(line);
			if(line.contains("TZID:Europe/Berlin")) {
                numTzDefs++;
            }
			if(!matcher.find()) {
                continue;
            }
			String group = matcher.group(1);
			number.add(Integer.valueOf(group));
		}
		ical.close();

		for(int i = 1; i < MAX_NUM_APP+1; i++){
			assertTrue("Did not find "+i, number.contains(i));
		}

		assertEquals("Timezone Europe/Berlin should be defined only once!", 1, numTzDefs);
	}

	/**
	 * Series with master and exception receive a different treatment than normal appointments,
	 * so they get tested explicitly.
	 *
	 * @throws Exception
	 */
	public void testSeriesExceptions() throws Exception{
		int fid = folder.getObjectID();
		Appointment series = generateDailyAppointment();
		series.setStartDate(D("1/1/2008 11:00",utc)); //note fails with 01:00, because for winter- to summertime, that hour might not exist.
		series.setEndDate(D("1/1/2008 12:00", utc));
		series.setTitle("A daily series");
		series.setParentFolderID(fid);
		calendarManager.insert(series);

		Date lastMod = series.getLastModified();
		int numChanges = BATCH_SIZE+5;
		for(int i = 1; i < numChanges +1; i++){
			Appointment changeEx = new Appointment();
			changeEx.setParentFolderID(series.getParentFolderID());
			changeEx.setObjectID(series.getObjectID());
			changeEx.setLastModified(lastMod);
			changeEx.setRecurrencePosition(i);
			changeEx.setTitle("Element # "+i+" of series that has different name");
			calendarManager.update(changeEx);
			assertNull("Problem with update #"+i, calendarManager.getLastException());
			lastMod = new Date(calendarManager.getLastModification().getTime() +1);
		}

		HttpResponse response = makeTheCall(folder.getObjectID());
		String ical = IOUtils.toString(response.getEntity().getContent());
		// System.out.println(ical);
		assertTrue("The series should be exported:"+System.getProperty("line.separator")+ical, ical.contains("A daily series"));
		for(int i = 1; i < numChanges+1; i++){
			assertTrue("The exception should be exported", ical.contains("Element # "+i+" of series that has different name"));
			if(i < 10) {
				assertTrue("There should be an exception on day #"+i, ical.contains("RECURRENCE-ID:2008010"+i+"T"));
			} else if(i < 32) {
				assertTrue("There should be an exception on day #"+i, ical.contains("RECURRENCE-ID:200801"+i+"T"));
			}
		}

	}


}
