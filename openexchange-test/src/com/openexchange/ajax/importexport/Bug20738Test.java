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

package com.openexchange.ajax.importexport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.importexport.ImportResult;

/**
 * No error message but a nullpointer when importing ical a second time
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class Bug20738Test extends AbstractAJAXSession {

	private final List<DeleteRequest> toDelete = new ArrayList<DeleteRequest>();
	
    /**
     * Default constructor.
     * @param name test name
     */
    public Bug20738Test(final String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
    	// cleanup
    	for (final DeleteRequest deleteRequest : this.toDelete) {
    		super.getClient().execute(deleteRequest);   		
    	}    
        super.tearDown();
    }

    public void testNoRecurrenceMasterFound() throws Throwable {
    	// upload ical once to create appointment series with unique id
        final AJAXClient client = super.getClient();
        final int folderId = client.getValues().getPrivateAppointmentFolder();
        final ICalImportRequest firstRequest = new ICalImportRequest(folderId, Bug20738Test.ICAL, false);
        final ICalImportResponse firstResponse = Executor.execute(client, firstRequest);
        final ImportResult[] firstResults = firstResponse.getImports();
        for (final ImportResult result : firstResults) {
            assertFalse("Initial import already failed", result.hasError());
            this.markForCleanup(result);
        }
    	// upload ical a second time, should fail and give respective warnings
        final ICalImportRequest secondRequest = new ICalImportRequest(folderId, Bug20738Test.ICAL, false);
        final ICalImportResponse secondResponse = Executor.execute(client, secondRequest);  
        final ImportResult[] secondResults = secondResponse.getImports();
        int app0100Count = 0;
        int ica0020Count = 0;
        for (final ImportResult result : secondResults) {
            assertTrue("Second import passed without error", result.hasError());
            OXException exception = result.getException();
            assertNotNull("Got no conversion exception", exception);
        	if ("ICA-0020".equals(exception.getErrorCode())) {
        		ica0020Count++;
        	} else if ("APP-0100".equals(exception.getErrorCode())) {
        		app0100Count++;
        	} else {
        		fail("Unexpected conversion exception: " + exception.getErrorCode());
        	}
        }
        assertEquals("Incorrect number of ICA-0020 warnings", 2, ica0020Count);
        assertEquals("Incorrect number of APP-0100 warnings", 1, app0100Count);
    }
    
    private void markForCleanup(final ImportResult result) {
    	final DeleteRequest deleteRequest = new DeleteRequest(Integer.parseInt(result.getObjectId()), 
        		Integer.parseInt(result.getFolder()), new Date(Long.MAX_VALUE));
    	deleteRequest.setFailOnError(false);
    	this.toDelete.add(deleteRequest);
    }

    /**
     * Slightly modified ics file with a series and two exceptions, taken from
     * original bug report.
     */
    private static final String ICAL =
   		"BEGIN:VCALENDAR\r\n" +
		"PRODID:-//Google Inc//Google Calendar 70.9054//EN\r\n" +
		"VERSION:2.0\r\n" +
		"CALSCALE:GREGORIAN\r\n" +
		"METHOD:PUBLISH\r\n" +
		"X-WR-CALNAME:Privat\r\n" +
		"X-WR-TIMEZONE:Europe/Berlin\r\n" +
		"BEGIN:VTIMEZONE\r\n" +
		"TZID:Europe/Berlin\r\n" +
		"X-LIC-LOCATION:Europe/Berlin\r\n" +
		"BEGIN:DAYLIGHT\r\n" +
		"TZOFFSETFROM:+0100\r\n" +
		"TZOFFSETTO:+0200\r\n" +
		"TZNAME:CEST\r\n" +
		"DTSTART:19700329T020000\r\n" +
		"RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n" +
		"END:DAYLIGHT\r\n" +
		"BEGIN:STANDARD\r\n" +
		"TZOFFSETFROM:+0200\r\n" +
		"TZOFFSETTO:+0100\r\n" +
		"TZNAME:CET\r\n" +
		"DTSTART:19701025T030000\r\n" +
		"RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n" +
		"END:STANDARD\r\n" +
		"END:VTIMEZONE\r\n" +
		"BEGIN:VTIMEZONE\r\n" +
		"TZID:Africa/Ceuta\r\n" +
		"X-LIC-LOCATION:Africa/Ceuta\r\n" +
		"BEGIN:DAYLIGHT\r\n" +
		"TZOFFSETFROM:+0100\r\n" +
		"TZOFFSETTO:+0200\r\n" +
		"TZNAME:CEST\r\n" +
		"DTSTART:19700329T020000\r\n" +
		"RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n" +
		"END:DAYLIGHT\r\n" +
		"BEGIN:STANDARD\r\n" +
		"TZOFFSETFROM:+0200\r\n" +
		"TZOFFSETTO:+0100\r\n" +
		"TZNAME:CET\r\n" +
		"DTSTART:19701025T030000\r\n" +
		"RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n" +
		"END:STANDARD\r\n" +
		"END:VTIMEZONE\r\n" +
		"BEGIN:VEVENT\r\n" +
		"DTSTART;TZID=Europe/Berlin:20110727T170000\r\n" +
		"DTEND;TZID=Europe/Berlin:20110727T180000\r\n" +
		"DTSTAMP:20110717T154458Z\r\n" +
		"UID:63D1882A85A5496795DCE32F04EF5F8E00000000000000000000000000000000\r\n" +
		"ATTENDEE;CUTYPE=INDIVIDUAL;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;X-NUM\r\n" +
		" -GUESTS=0:mailto:otto.horst@erdbeerkaese.invalid\r\n" +
		"RECURRENCE-ID;TZID=Europe/Berlin:20110728T180000\r\n" +
		"CREATED:20110531T052752Z\r\n" +
		"DESCRIPTION:\r\n" +
		"LAST-MODIFIED:20110713T155545Z\r\n" +
		"LOCATION:C\u00e4cilienstr. 44\r\n" +
		"SEQUENCE:2\r\n" +
		"STATUS:CONFIRMED\r\n" +
		"SUMMARY:Frau Otto\r\n" +
		"TRANSP:OPAQUE\r\n" +
		"CATEGORIES:http://schemas.google.com/g/2005#event\r\n" +
		"END:VEVENT\r\n" +
		"BEGIN:VEVENT\r\n" +
		"DTSTART;TZID=Europe/Berlin:20110707T150000\r\n" +
		"DTEND;TZID=Europe/Berlin:20110707T160000\r\n" +
		"DTSTAMP:20110717T154458Z\r\n" +
		"UID:63D1882A85A5496795DCE32F04EF5F8E00000000000000000000000000000000\r\n" +
		"ATTENDEE;CUTYPE=INDIVIDUAL;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;X-NUM\r\n" +
		" -GUESTS=0:mailto:otto.horst@erdbeerkaese.invalid\r\n" +
		"RECURRENCE-ID;TZID=Europe/Berlin:20110707T180000\r\n" +
		"CREATED:20110531T052752Z\r\n" +
		"DESCRIPTION:\r\n" +
		"LAST-MODIFIED:20110713T155545Z\r\n" +
		"LOCATION:C\u00e4cilienstr. 44\r\n" +
		"SEQUENCE:2\r\n" +
		"STATUS:CONFIRMED\r\n" +
		"SUMMARY:Frau Otto\r\n" +
		"TRANSP:OPAQUE\r\n" +
		"CATEGORIES:http://schemas.google.com/g/2005#event\r\n" +
		"END:VEVENT\r\n" +
		"BEGIN:VEVENT\r\n" +
		"DTSTART;TZID=Africa/Ceuta:20110616T180000\r\n" +
		"DTEND;TZID=Africa/Ceuta:20110616T190000\r\n" +
		"RRULE:FREQ=WEEKLY;BYDAY=TH\r\n" +
		"EXDATE;TZID=Africa/Ceuta:20110721T180000\r\n" +
		"EXDATE;TZID=Africa/Ceuta:20110714T180000\r\n" +
		"DTSTAMP:20110717T154458Z\r\n" +
		"UID:63D1882A85A5496795DCE32F04EF5F8E00000000000000000000000000000000\r\n" +
		"ATTENDEE;CUTYPE=INDIVIDUAL;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;X-NUM\r\n" +
		" -GUESTS=0:mailto:otto.horst@erdbeerkaese.invalid\r\n" +
		"CREATED:20110531T052752Z\r\n" +
		"DESCRIPTION:\r\n" +
		"LAST-MODIFIED:20110713T155545Z\r\n" +
		"LOCATION:C\u00e4cilienstr. 44\r\n" +
		"SEQUENCE:1\r\n" +
		"STATUS:CONFIRMED\r\n" +
		"SUMMARY:Frau Otto\r\n" +
		"TRANSP:OPAQUE\r\n" +
		"CATEGORIES:http://schemas.google.com/g/2005#event\r\n" +
		"END:VEVENT\r\n" +
		"END:VCALENDAR\r\n"
	;
}
