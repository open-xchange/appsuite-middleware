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

package com.openexchange.ajax.importexport;

import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.importexport.ImportResult;

public class Bug6825Test_TruncationOfFields extends ManagedAppointmentTest {

	public Bug6825Test_TruncationOfFields(final String name) {
		super(name);
	}
	
	public void testUnexpectedException() throws Exception{
        // setup
        final String testMailAddress = "stephan.martin@open-xchange.com";
        final String ical = 
        	"BEGIN:VCALENDAR\n" +
        	"VERSION:2.0\n" +
        	"PRODID:OPEN-XCHANGE\n" +
        	"BEGIN:VEVENT\n" +
        	"CLASS:PUBLIC\n" +
        	"CREATED:20060519T120300Z\n" +
        	"DTSTART:20060519T110000Z\n" +
        	"DTSTAMP:20070423T063205Z\n" +
        	"SUMMARY:External 1&1 Review call\n" +
        	"DTEND:20060519T120000Z\n" +
        	"ATTENDEE:mailto:" + testMailAddress + "\n" +
        	"END:VEVENT\n" +
        	"END:VCALENDAR";

        final ICalImportResponse importresponse = getClient().execute(new ICalImportRequest(folder.getObjectID(), ical));

        final ImportResult imported = importresponse.getImports()[0];
		final Appointment appointment = calendarManager.get(Integer.parseInt(imported.getFolder()), Integer.parseInt(imported.getObjectId()));

        
		assertTrue("Should have participants", appointment.containsParticipants());

		final Participant[] participants = appointment.getParticipants();
        assertEquals("Should have two participants", 2, participants.length);
        assertTrue(
            "One user is " + testMailAddress + " (external user)",
            testMailAddress.equals(participants[0].getEmailAddress()) || testMailAddress.equals(participants[1].getEmailAddress()));
        assertTrue(
            "One user is the user doing the import",
            participants[0].getIdentifier() == userId || participants[1].getIdentifier() == userId);
    }
	
    public void testTooMuchInformation() throws Exception {
        // setup: building an ICAL file with a summary longer than 255 characters.
        final String testMailAddress = "stephan.martin@open-xchange.com";
        final String stringTooLong = "zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... ";
        final String ical = "BEGIN:VCALENDAR\nVERSION:2.0\nPRODID:OPEN-XCHANGE\nBEGIN:VEVENT\nCLASS:PUBLIC\nCREATED:20060519T120300Z\nDTSTART:20060519T110000Z\nDTSTAMP:20070423T063205Z\nSUMMARY:" + stringTooLong + "\nDTEND:20060519T120000Z\nATTENDEE:mailto:" + testMailAddress + "\nEND:VEVENT\nEND:VCALENDAR";

        final ICalImportResponse importresponse = getClient().execute(new ICalImportRequest(folder.getObjectID(), ical, false));
        assertTrue(importresponse.hasError());
        final JSONObject data = ((JSONArray) importresponse.getData()).getJSONObject(0);
        assertTrue(data.has("truncated"));
    }


}
