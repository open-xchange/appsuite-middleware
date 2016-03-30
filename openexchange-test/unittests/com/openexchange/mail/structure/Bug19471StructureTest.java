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

package com.openexchange.mail.structure;

import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.mail.AbstractMailTest;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.structure.handler.MIMEStructureHandler;

/**
 * {@link Bug19471StructureTest} - Test for output of structured JSON mail object.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Bug19471StructureTest extends AbstractMailTest {

    /**
     * Initializes a new {@link Bug19471StructureTest}.
     */
    public Bug19471StructureTest() {
        super();
    }

    /**
     * Initializes a new {@link Bug19471StructureTest}.
     *
     * @param name The test name
     */
    public Bug19471StructureTest(final String name) {
        super(name);
    }

    private static final byte[] SIMPLE = ("Date: Fri, 12 Aug 2011 17:31:35 +0200 (CEST)\n" +
    		"From: jane@doe.de\n" +
    		"To: mark@nowhere.com\n" +
    		"Message-ID: <1423407397.2.1313163096085.JavaMail.blubber@bar>\n" +
    		"Subject: New Appointment: Generate ICal\n" +
    		"MIME-Version: 1.0\n" +
    		"Content-Type: multipart/alternative; \n" +
    		"    boundary=\"----=_Part_0_2043410500.1313163095014\"\n" +
    		"\n" +
    		"------=_Part_0_2043410500.1313163095014\n" +
    		"Content-Type: text/plain; charset=UTF-8\n" +
    		"Content-Transfer-Encoding: 7bit\n" +
    		"\n" +
    		"A new appointment was created by Blubber Foo.\n" +
    		"\n" +
    		"Appointment\n" +
    		"===========\n" +
    		"Created by: Blubber Foo\n" +
    		"Created at: Friday, Aug 12, 2011 5:31:00 PM, CEST\n" +
    		"Description: Generate ICal\n" +
    		"\n" +
    		"Start date: Wednesday, Aug 17, 2011 2:00:00 PM, CEST\n" +
    		"End date: Wednesday, Aug 17, 2011 3:00:00 PM, CEST\n" +
    		"\n" +
    		"\n" +
    		"Participants\n" +
    		"============\n" +
    		"Blubber Foo (accepted)\n" +
    		"mark@nowhere.com (external)\n" +
    		"\n" +
    		"Resources\n" +
    		"=========\n" +
    		"No resources have been scheduled.\n" +
    		"\n" +
    		"========================================== \n" +
    		"------=_Part_0_2043410500.1313163095014\n" +
    		"Content-Type: text/calendar; charset=\"utf-8\"\n" +
    		"Content-Transfer-Encoding: 7bit\n" +
    		"\n" +
    		"BEGIN:VCALENDAR\n" +
    		"PRODID:Open-Xchange\n" +
    		"VERSION:2.0\n" +
    		"CALSCALE:GREGORIAN\n" +
    		"METHOD:REQUEST\n" +
    		"BEGIN:VEVENT\n" +
    		"DTSTAMP:20110812T153135Z\n" +
    		"SUMMARY:Generate ICal\n" +
    		"DTSTART;TZID=Europe/Berlin:20110817T140000\n" +
    		"DTEND;TZID=Europe/Berlin:20110817T150000\n" +
    		"CLASS:PUBLIC\n" +
    		"TRANSP:OPAQUE\n" +
    		"UID:cb3f5f83-a2de-4554-b352-78429527d259\n" +
    		"CREATED:20110812T153134Z\n" +
    		"LAST-MODIFIED:20110812T153134Z\n" +
    		"ORGANIZER:mailto:jane@doe.de\n" +
    		"ATTENDEE;ROLE=REQ-PARTICIPANT;CUTYPE=INDIVIDUAL:mailto:thorben@devel-mail.n\n" +
    		" etline.de\n" +
    		"ATTENDEE;CUTYPE=INDIVIDUAL;PARTSTAT=NEEDS-ACTION;ROLE=REQ-PARTICIPANT;RSVP=\n" +
    		" TRUE:mailto:mark@nowhere.com\n" +
    		"END:VEVENT\n" +
    		"BEGIN:VTIMEZONE\n" +
    		"TZID:Europe/Berlin\n" +
    		"TZURL:http://tzurl.org/zoneinfo/Europe/Berlin\n" +
    		"X-LIC-LOCATION:Europe/Berlin\n" +
    		"BEGIN:DAYLIGHT\n" +
    		"TZOFFSETFROM:+0100\n" +
    		"TZOFFSETTO:+0200\n" +
    		"TZNAME:CEST\n" +
    		"DTSTART:19810329T020000\n" +
    		"RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\n" +
    		"END:DAYLIGHT\n" +
    		"BEGIN:STANDARD\n" +
    		"TZOFFSETFROM:+0200\n" +
    		"TZOFFSETTO:+0100\n" +
    		"TZNAME:CET\n" +
    		"DTSTART:19961027T030000\n" +
    		"RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\n" +
    		"END:STANDARD\n" +
    		"BEGIN:STANDARD\n" +
    		"TZOFFSETFROM:+005328\n" +
    		"TZOFFSETTO:+0100\n" +
    		"TZNAME:CET\n" +
    		"DTSTART:18930401T000000\n" +
    		"RDATE:18930401T000000\n" +
    		"END:STANDARD\n" +
    		"BEGIN:DAYLIGHT\n" +
    		"TZOFFSETFROM:+0100\n" +
    		"TZOFFSETTO:+0200\n" +
    		"TZNAME:CEST\n" +
    		"DTSTART:19160430T230000\n" +
    		"RDATE:19160430T230000\n" +
    		"RDATE:19170416T020000\n" +
    		"RDATE:19180415T020000\n" +
    		"RDATE:19400401T020000\n" +
    		"RDATE:19430329T020000\n" +
    		"RDATE:19440403T020000\n" +
    		"RDATE:19450402T020000\n" +
    		"RDATE:19460414T020000\n" +
    		"RDATE:19470406T030000\n" +
    		"RDATE:19480418T020000\n" +
    		"RDATE:19490410T020000\n" +
    		"RDATE:19800406T020000\n" +
    		"END:DAYLIGHT\n" +
    		"BEGIN:STANDARD\n" +
    		"TZOFFSETFROM:+0200\n" +
    		"TZOFFSETTO:+0100\n" +
    		"TZNAME:CET\n" +
    		"DTSTART:19161001T010000\n" +
    		"RDATE:19161001T010000\n" +
    		"RDATE:19170917T030000\n" +
    		"RDATE:19180916T030000\n" +
    		"RDATE:19421102T030000\n" +
    		"RDATE:19431004T030000\n" +
    		"RDATE:19441002T030000\n" +
    		"RDATE:19451118T030000\n" +
    		"RDATE:19461007T030000\n" +
    		"RDATE:19471005T030000\n" +
    		"RDATE:19481003T030000\n" +
    		"RDATE:19491002T030000\n" +
    		"RDATE:19800928T030000\n" +
    		"RDATE:19810927T030000\n" +
    		"RDATE:19820926T030000\n" +
    		"RDATE:19830925T030000\n" +
    		"RDATE:19840930T030000\n" +
    		"RDATE:19850929T030000\n" +
    		"RDATE:19860928T030000\n" +
    		"RDATE:19870927T030000\n" +
    		"RDATE:19880925T030000\n" +
    		"RDATE:19890924T030000\n" +
    		"RDATE:19900930T030000\n" +
    		"RDATE:19910929T030000\n" +
    		"RDATE:19920927T030000\n" +
    		"RDATE:19930926T030000\n" +
    		"RDATE:19940925T030000\n" +
    		"RDATE:19950924T030000\n" +
    		"END:STANDARD\n" +
    		"BEGIN:DAYLIGHT\n" +
    		"TZOFFSETFROM:+0200\n" +
    		"TZOFFSETTO:+0300\n" +
    		"TZNAME:CEMT\n" +
    		"DTSTART:19450524T020000\n" +
    		"RDATE:19450524T020000\n" +
    		"RDATE:19470511T030000\n" +
    		"END:DAYLIGHT\n" +
    		"BEGIN:DAYLIGHT\n" +
    		"TZOFFSETFROM:+0300\n" +
    		"TZOFFSETTO:+0200\n" +
    		"TZNAME:CEST\n" +
    		"DTSTART:19450924T030000\n" +
    		"RDATE:19450924T030000\n" +
    		"RDATE:19470629T030000\n" +
    		"END:DAYLIGHT\n" +
    		"BEGIN:STANDARD\n" +
    		"TZOFFSETFROM:+0100\n" +
    		"TZOFFSETTO:+0100\n" +
    		"TZNAME:CET\n" +
    		"DTSTART:19460101T000000\n" +
    		"RDATE:19460101T000000\n" +
    		"RDATE:19800101T000000\n" +
    		"END:STANDARD\n" +
    		"END:VTIMEZONE\n" +
    		"END:VCALENDAR\n" +
    		"\n" +
    		"------=_Part_0_2043410500.1313163095014--" +
    		"\n").getBytes();

    public void testMIMEStructure() {
        try {
            getSession();

            final MailMessage mail = MimeMessageConverter.convertMessage(SIMPLE);

            final MIMEStructureHandler handler = new MIMEStructureHandler(-1L);
            new StructureMailMessageParser().parseMailMessage(mail, handler);

            final JSONObject jsonMailObject = handler.getJSONMailObject();
            assertNotNull("Structured JSON mail object is null.", jsonMailObject);

            // System.out.println(jsonMailObject.toString(2));

            final JSONArray jsonBodyArray;
            {
                final Object bodyObject = jsonMailObject.opt("body");
                assertNotNull("Missing mail body.", bodyObject);

                // {"data":"This is a text message.\n\n","id":"1"}
                assertTrue("Body object is not a JSON object.", (bodyObject instanceof JSONArray));
                jsonBodyArray = (JSONArray) bodyObject;

            }

            final JSONObject icalObject = jsonBodyArray.getJSONObject(1);
            final JSONObject ct = icalObject.getJSONObject("headers").getJSONObject("content-type");
            final JSONObject param = ct.getJSONObject("params");
            assertTrue(param.hasAndNotNull("method") && "REQUEST".equals(param.getString("method")));
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
