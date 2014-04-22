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

package com.openexchange.mail.parser.handlers;

import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.groupware.contexts.SimContext;
import com.openexchange.groupware.ldap.SimUser;
import com.openexchange.html.HtmlService;
import com.openexchange.html.SimHtmlService;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.utils.DisplayMode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.SimServerSession;
import junit.framework.TestCase;


/**
 * {@link JsonMessageHandlerTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class JsonMessageHandlerTest extends TestCase {

    /**
     * Initializes a new {@link JsonMessageHandlerTest}.
     */
    public JsonMessageHandlerTest() {
        super();
    }

    /**
     * Initializes a new {@link JsonMessageHandlerTest}.
     */
    public JsonMessageHandlerTest(String name) {
        super(name);
    }

    /**
     * Ensure that duplicate text/calendar attachment that is part of multipart/alternative gets ignored
     */
    public void testBug32108() {
        try {
            final byte[] bytes = ("Delivered-To: fake@gmail.com\n" +
                "Date: Sat, 19 Apr 2014 12:45:45 +0800 (MYT)\n" +
                "From: Test User Dhamu <testoxuser10@yes1.my>\n" +
                "Reply-To: Test User Dhamu <testoxuser10@yes1.my>\n" +
                "To: \"fake@gmail.com\" <fake@gmail.com>\n" +
                "Message-ID: <9575168.5.1397882746309.open-xchange@SN2YCVX1C1003>\n" +
                "Subject: New appointment: supertest\n" +
                "MIME-Version: 1.0\n" +
                "Content-Type: multipart/mixed; boundary=\"----=_Part_3_1465200755.1397882745831\"\n" +
                "X-Priority: 3 (normal)\n" +
                "X-Mailer: Open-Xchange Mailer v7.4.2-Rev18\n" +
                "X-OX-Marker: cfeefd6b-a2bb-4c1c-b705-22f9f7bf6597\n" +
                "\n" +
                "------=_Part_3_1465200755.1397882745831\n" +
                "Content-Type: multipart/alternative; \n" +
                "    boundary=\"----=_Part_2_507915751.1397882745827\"\n" +
                "\n" +
                "------=_Part_2_507915751.1397882745827\n" +
                "MIME-Version: 1.0\n" +
                "Content-Type: text/plain; charset=UTF-8\n" +
                "Content-Transfer-Encoding: 7bit\n" +
                "\n" +
                "You have been invited to an event by Test User Dhamu:\n" +
                "\n" +
                "\n" +
                "\n" +
                "====[       supertest       ]====\n" +
                "\n" +
                "All times will be shown in the Malaysia Time time zone\n" +
                "\n" +
                "When: Saturday, 19 April 2014 13:00 - 14:00\n" +
                "\n" +
                "\n" +
                "\n" +
                "== Participants: ==\n" +
                "\n" +
                "Test User Dhamu (accepted)\n" +
                "fake@gmail.com (waiting)\n" +
                "\n" +
                "== Resources ==\n" +
                "\n" +
                "\n" +
                "== Details: ==\n" +
                "\n" +
                "Show as: Reserved\n" +
                "Created: Saturday, 19 April 2014 12:45 - Test User Dhamu\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "------=_Part_2_507915751.1397882745827\n" +
                "MIME-Version: 1.0\n" +
                "Content-Type: text/html; charset=UTF-8\n" +
                "Content-Transfer-Encoding: quoted-printable\n" +
                "\n" +
                "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org=\n" +
                "/TR/xhtml1/DTD/xhtml1-strict.dtd\"><html xmlns=3D\"http://www.w3.org/1999/xht=\n" +
                "ml\"><head>\n" +
                "    <meta http-equiv=3D\"Content-Type\" content=3D\"text/html; charset=3DUTF-8=\n" +
                "\" />\n" +
                " =20\n" +
                "  <title></title>=20\n" +
                "  <meta charset=3D\"UTF-8\" />=20\n" +
                "  <style type=3D\"text/css\">\n" +
                "            .content {\n" +
                " white-space: normal;\n" +
                " color: black;\n" +
                " font-family: Arial, Helvetica, sans-serif;\n" +
                " font-size: 12px;\n" +
                " cursor: default;\n" +
                "}\n" +
                "/* shown_as */\n" +
                "\n" +
                "\n" +
                ".shown_as.reserved { background-color: #08c; } /* blue */\n" +
                ".shown_as.temporary { background-color: #fc0; } /* yellow */\n" +
                ".shown_as.absent { background-color: #913F3F; } /* red */\n" +
                ".shown_as.free { background-color: #8EB360; } /* green */\n" +
                "\n" +
                ".shown_as_label.reserved { color: #08c; } /* blue */\n" +
                ".shown_as_label.temporary { color: #fc0; } /* yellow */\n" +
                ".shown_as_label.absent { color: #913F3F; } /* red */\n" +
                ".shown_as_label.free { color: #8EB360; } /* green */\n" +
                "\n" +
                "em {\n" +
                " font-weight: bold;\n" +
                "}\n" +
                "\n" +
                "/* Detail view */\n" +
                "\n" +
                ".timezone {\n" +
                " margin-bottom: 2em;\n" +
                "}\n" +
                "\n" +
                ".justification, .attachmentNote {\n" +
                " margin-top: 2em;\n" +
                " margin-bottom: 2em;\n" +
                "}\n" +
                "\n" +
                ".calendar-detail .action {\n" +
                " float: right;\n" +
                " margin-right: 1em;\n" +
                "}\n" +
                "\n" +
                ".calendar-detail .link {\n" +
                " cursor: pointer;\n" +
                " text-decoration: underline;\n" +
                " color: #00a0cd;\n" +
                "}\n" +
                "\n" +
                ".calendar-detail .calendar-buttons {\n" +
                " height: 2em;\n" +
                " text-align: right;\n" +
                " line-height: 2em;\n" +
                " border-bottom: 1px solid #f0f0f0;\n" +
                "}\n" +
                "\n" +
                ".calendar-detail .date {\n" +
                "    font-size: 11pt;\n" +
                "    color: #ccc;\n" +
                "}\n" +
                "\n" +
                ".calendar-detail .interval {\n" +
                "    color: #555;\n" +
                "    white-space: nowrap;\n" +
                "    float: right;\n" +
                "}\n" +
                "\n" +
                ".calendar-detail .day {\n" +
                "    color: #888;\n" +
                "}\n" +
                "\n" +
                ".calendar-detail .title {\n" +
                "    font-size: 18pt;\n" +
                "    line-height: 22pt;\n" +
                "    margin: 0.25em 0 0.25em 0;\n" +
                "}\n" +
                "\n" +
                ".calendar-detail .location {\n" +
                "    font-size: 11pt;\n" +
                "    color: #888;\n" +
                "    margin-bottom: 1em;\n" +
                "}\n" +
                "\n" +
                ".calendar-detail .label {\n" +
                "    font-size: 9pt;\n" +
                "    color: #888;\n" +
                "    clear: both;\n" +
                "    border-bottom: 1px solid #ccc;\n" +
                "    padding: 1em 0 0.25em 0em;\n" +
                "    margin-bottom: 0.5em;\n" +
                "}\n" +
                "\n" +
                ".calendar-detail .note {\n" +
                "    max-width: 550px;\n" +
                "    margin: 2em 0 1em 0;\n" +
                "    -webkit-user-select: text;\n" +
                "    -moz-user-select: text;\n" +
                "    user-select: text;\n" +
                "    cursor: text;\n" +
                "}\n" +
                "\n" +
                ".calendar-detail .participants {\n" +
                "    min-height: 2em;\n" +
                "}\n" +
                "\n" +
                ".calendar-detail .participants table {\n" +
                "    text-align: left;\n" +
                "    vertical-align: left;\n" +
                "}\n" +
                "\n" +
                ".calendar-detail .participant {\n" +
                "    line-height: 1.2 em;\n" +
                "}\n" +
                "\n" +
                ".calendar-detail .detail-label {\n" +
                "    display: inline-block;\n" +
                "    width: 80px;\n" +
                "    white-space: nowrap;\n" +
                "    color: #666;\n" +
                "}\n" +
                "\n" +
                ".calendar-detail .detail {\n" +
                "    white-space: nowrap;\n" +
                "}\n" +
                "\n" +
                ".calendar-detail .detail.shown_as {\n" +
                "    display: inline-block;\n" +
                "    height: 1em;\n" +
                "    width: 1em;\n" +
                "}\n" +
                "\n" +
                ".calendar-detail .participant .status { font-weight: bold; }\n" +
                ".calendar-detail .participant .status.accepted { color: #8EB360; } /* green=\n" +
                " */\n" +
                ".calendar-detail .participant .status.declined { color: #913F3F; } /* red *=\n" +
                "/\n" +
                ".calendar-detail .participant .status.tentative { color: #c80; } /* orange =\n" +
                "*/\n" +
                "\n" +
                ".calendar-detail .participant .comment {\n" +
                "    color: #888;\n" +
                "    display: block;\n" +
                "    white-space: normal;\n" +
                "    padding-left: 1em;\n" +
                "}\n" +
                "\n" +
                ".calendar-detail .group {\n" +
                "    margin: 0.75em 0 0.25em 0;\n" +
                "    color: #333;\n" +
                "}\n" +
                "\n" +
                ".person, .person-link {\n" +
                " color: #00A0CD;\n" +
                "}\n" +
                "\n" +
                ".clear-title {\n" +
                " font-family: OpenSans, Helvetica, Arial, sans-serif;\n" +
                " font-weight: 200;\n" +
                " font-size: 20pt;\n" +
                " line-height: 1.15em;\n" +
                "}\n" +
                "\n" +
                ".calendar-action {\n" +
                " margin-bottom: 2em;\n" +
                " font-family: OpenSans, Helvetica, Arial, sans-serif;\n" +
                " font-weight: 200;\n" +
                " font-size: 12pt;\n" +
                "}\n" +
                "\n" +
                ".calendar-action .changes{\n" +
                "    margin-top: 2em;\n" +
                " font-size: 11pt;\n" +
                "}\n" +
                "\n" +
                ".calendar-action .changes .original {\n" +
                "    font-weight: bold;\n" +
                "}\n" +
                "\n" +
                ".calendar-action .changes .recurrencePosition {\n" +
                "    font-weight: bold;\n" +
                "}\n" +
                "\n" +
                ".calendar-action .changes .updated {\n" +
                "    color: green;\n" +
                "    font-weight: bold;\n" +
                "}\n" +
                "\n" +
                ".calendar-action .status {  }\n" +
                ".calendar-action  .status.accepted { color: #8EB360; } /* green */\n" +
                ".calendar-action  .status.declined { color: #913F3F; } /* red */\n" +
                ".calendar-action  .status.tentative { color: #c80; } /* orange */\n" +
                "\n" +
                "        </style>=20\n" +
                " =20\n" +
                " </head><body>\n" +
                " =20\n" +
                "  <div class=3D\"content\">=20\n" +
                "   <div class=3D\"timezone\">\n" +
                "     All times will be shown in the=20\n" +
                "    <em>Malaysia Time</em> time zone=20\n" +
                "   </div>=20\n" +
                "   <div class=3D\"calendar-action\">\n" +
                "    You have been invited to an event by=20\n" +
                "    <span class=3D\"person\">Test User Dhamu</span>:\n" +
                "   </div>=20\n" +
                "   <div class=3D\"calendar-detail\">=20\n" +
                "    <div class=3D\"date\">=20\n" +
                "     <div class=3D\"interval\">\n" +
                "       13:00 - 14:00=20\n" +
                "     </div>=20\n" +
                "     <div class=3D\"day\">\n" +
                "       Saturday, 19 April 2014=20\n" +
                "     </div>=20\n" +
                "    </div>=20\n" +
                "    <div class=3D\"title clear-title\">\n" +
                "      supertest=20\n" +
                "    </div>=20\n" +
                "    <div class=3D\"location\">=20\n" +
                "    </div>=20\n" +
                "    <div style=3D\"display:none\" class=3D\"calendar-buttons\"></div>=20\n" +
                "    <div class=3D\"note\">=20\n" +
                "    </div>=20\n" +
                "    <div class=3D\"participants\">=20\n" +
                "     <div class=3D\"label\">\n" +
                "       Participants:=20\n" +
                "     </div>=20\n" +
                "     <div class=3D\"participant-list\">=20\n" +
                "      <div class=3D\"participant\">=20\n" +
                "       <span class=3D\"person\">Test User Dhamu</span>=20\n" +
                "       <span class=3D\"status accepted\">=E2=9C=93</span>=20\n" +
                "       <span class=3D\"comment\"></span>=20\n" +
                "      </div>=20\n" +
                "      <div class=3D\"participant\">=20\n" +
                "       <span class=3D\"person\">fake@gmail.com</span>=20\n" +
                "       <span class=3D\"comment\"></span>=20\n" +
                "      </div>=20\n" +
                "     </div>=20\n" +
                "     <div class=3D\"participants-clear\"></div>=20\n" +
                "    </div>=20\n" +
                "    <div class=3D\"participants\">=20\n" +
                "     <div class=3D\"label\">\n" +
                "       Resources=20\n" +
                "     </div>=20\n" +
                "     <div class=3D\"participant-list\">=20\n" +
                "     </div>=20\n" +
                "     <div class=3D\"participants-clear\"></div>=20\n" +
                "    </div>=20\n" +
                "    <div>=20\n" +
                "     <div class=3D\"label\">\n" +
                "       Details:=20\n" +
                "     </div>\n" +
                "     <span class=3D\"detail-label\">Show as:&#160;</span>\n" +
                "     <span class=3D\"detail\"><span class=3D\"shown_as_label reserved\">Reserve=\n" +
                "d</span></span>\n" +
                "     <br />=20\n" +
                "     <span class=3D\"detail-label\">Created:&#160;</span>\n" +
                "     <span class=3D\"detail\"><span>Saturday, 19 April 2014 12:45</span> <spa=\n" +
                "n>-</span> <span>Test User Dhamu</span></span>=20\n" +
                "    </div>=20\n" +
                "    <div class=3D\"attachmentNote\">=20\n" +
                "    </div>=20\n" +
                "    <div class=3D\"justification\">=20\n" +
                "    </div>=20\n" +
                "   </div>=20\n" +
                "  </div>  =20\n" +
                "=20\n" +
                "</body></html>\n" +
                "------=_Part_2_507915751.1397882745827\n" +
                "Content-Type: text/calendar; charset=UTF-8; method=REQUEST\n" +
                "Content-Transfer-Encoding: 7bit\n" +
                "\n" +
                "BEGIN:VCALENDAR\n" +
                "PRODID:Open-Xchange\n" +
                "VERSION:2.0\n" +
                "CALSCALE:GREGORIAN\n" +
                "METHOD:REQUEST\n" +
                "BEGIN:VTIMEZONE\n" +
                "TZID:Asia/Kuala_Lumpur\n" +
                "TZURL:http://tzurl.org/zoneinfo-outlook/Asia/Kuala_Lumpur\n" +
                "X-LIC-LOCATION:Asia/Kuala_Lumpur\n" +
                "BEGIN:STANDARD\n" +
                "TZOFFSETFROM:+0800\n" +
                "TZOFFSETTO:+0800\n" +
                "TZNAME:MYT\n" +
                "DTSTART:19700101T003000\n" +
                "END:STANDARD\n" +
                "END:VTIMEZONE\n" +
                "BEGIN:VEVENT\n" +
                "DTSTAMP:20140419T044545Z\n" +
                "SUMMARY:supertest\n" +
                "DTSTART;TZID=Asia/Kuala_Lumpur:20140419T130000\n" +
                "DTEND;TZID=Asia/Kuala_Lumpur:20140419T140000\n" +
                "CLASS:PUBLIC\n" +
                "TRANSP:OPAQUE\n" +
                "UID:7213b334-f321-47c3-aacf-9f2d5aa0b2f3\n" +
                "CREATED:20140419T044544Z\n" +
                "LAST-MODIFIED:20140419T044544Z\n" +
                "ORGANIZER:mailto:testoxuser10@yes1.my\n" +
                "SEQUENCE:0\n" +
                "ATTENDEE;ROLE=REQ-PARTICIPANT;CUTYPE=INDIVIDUAL;CN=Test User Dhamu;PARTSTAT=ACCEPTED:mailto:testoxuser10@yes1.my\n" +
                "ATTENDEE;CUTYPE=INDIVIDUAL;PARTSTAT=NEEDS-ACTION;ROLE=REQ-PARTICIPANT;RSVP=TRUE:mailto:fake@gmail.com\n" +
                "END:VEVENT\n" +
                "END:VCALENDAR\n" +
                "\n" +
                "------=_Part_2_507915751.1397882745827--\n" +
                "\n" +
                "------=_Part_3_1465200755.1397882745831\n" +
                "Content-Type: application/ics; name=invite.ics\n" +
                "Content-Transfer-Encoding: base64\n" +
                "Content-Disposition: attachment; filename=invite.ics\n" +
                "\n" +
                "QkVHSU46VkNBTEVOREFSDQpQUk9ESUQ6T3Blbi1YY2hhbmdlDQpWRVJTSU9OOjIuMA0KQ0FMU0NB\n" +
                "TEU6R1JFR09SSUFODQpNRVRIT0Q6UkVRVUVTVA0KQkVHSU46VlRJTUVaT05FDQpUWklEOkFzaWEv\n" +
                "S3VhbGFfTHVtcHVyDQpUWlVSTDpodHRwOi8vdHp1cmwub3JnL3pvbmVpbmZvLW91dGxvb2svQXNp\n" +
                "YS9LdWFsYV9MdW1wdXINClgtTElDLUxPQ0FUSU9OOkFzaWEvS3VhbGFfTHVtcHVyDQpCRUdJTjpT\n" +
                "VEFOREFSRA0KVFpPRkZTRVRGUk9NOiswODAwDQpUWk9GRlNFVFRPOiswODAwDQpUWk5BTUU6TVlU\n" +
                "DQpEVFNUQVJUOjE5NzAwMTAxVDAwMzAwMA0KRU5EOlNUQU5EQVJEDQpFTkQ6VlRJTUVaT05FDQpC\n" +
                "RUdJTjpWRVZFTlQNCkRUU1RBTVA6MjAxNDA0MTlUMDQ0NTQ1Wg0KU1VNTUFSWTpzdXBlcnRlc3QN\n" +
                "CkRUU1RBUlQ7VFpJRD1Bc2lhL0t1YWxhX0x1bXB1cjoyMDE0MDQxOVQxMzAwMDANCkRURU5EO1Ra\n" +
                "SUQ9QXNpYS9LdWFsYV9MdW1wdXI6MjAxNDA0MTlUMTQwMDAwDQpDTEFTUzpQVUJMSUMNClRSQU5T\n" +
                "UDpPUEFRVUUNClVJRDo3MjEzYjMzNC1mMzIxLTQ3YzMtYWFjZi05ZjJkNWFhMGIyZjMNCkNSRUFU\n" +
                "RUQ6MjAxNDA0MTlUMDQ0NTQ0Wg0KTEFTVC1NT0RJRklFRDoyMDE0MDQxOVQwNDQ1NDRaDQpPUkdB\n" +
                "TklaRVI6bWFpbHRvOnRlc3RveHVzZXIxMEB5ZXMxLm15DQpTRVFVRU5DRTowDQpBVFRFTkRFRTtS\n" +
                "T0xFPVJFUS1QQVJUSUNJUEFOVDtDVVRZUEU9SU5ESVZJRFVBTDtDTj1UZXN0IFVzZXIgRGhhbXU7\n" +
                "UEFSVFNUQVQ9QUNDRVBURUQ6bWFpbHRvOnRlc3RveHVzZXIxMEB5ZXMxLm15DQpBVFRFTkRFRTtD\n" +
                "VVRZUEU9SU5ESVZJRFVBTDtQQVJUU1RBVD1ORUVEUy1BQ1RJT047Uk9MRT1SRVEtUEFSVElDSVBB\n" +
                "TlQ7UlNWUD1UUlVFOm1haWx0bzptYWxhc2FAZ21haWwuY29tDQpFTkQ6VkVWRU5UDQpFTkQ6VkNB\n" +
                "TEVOREFSDQo=\n" +
                "------=_Part_3_1465200755.1397882745831--\n" +
                "").getBytes();

            // Ensure that duplicate text/calendar attachment that is part of multipart/alternative gets ignored

            final MailMessage mail = MimeMessageConverter.convertMessage(bytes);

            // Preps

            ServerServiceRegistry.getInstance().addService(HtmlService.class, new SimHtmlService());

            UserSettingMail usm = new UserSettingMail(1, 1);
            usm.parseBits(627479);

            ServerSession session = new SimServerSession(new SimContext(1), new SimUser(1), null);

            JsonMessageHandler handler = new JsonMessageHandler(0, "INBOX/1", DisplayMode.DISPLAY, true, session, usm, false, 0);

            // Test

            MailMessageParser parser = new MailMessageParser();
            parser.parseMailMessage(mail, handler);

            JSONObject jMail = handler.getJSONObject();
            assertNotNull(jMail);

            JSONArray jAttachments = jMail.getJSONArray("attachments");
            assertNotNull(jAttachments);
            assertEquals("Unexpected number of attachments", 2, jAttachments.length());

            final JSONObject jAttachment1 = jAttachments.getJSONObject(0);
            assertNotNull(jAttachment1);
            final JSONObject jAttachment2 = jAttachments.getJSONObject(1);
            assertNotNull(jAttachment2);

            assertTrue("Unexpected content", jAttachment1.getString("content_type").startsWith("text/html"));
            assertTrue("Unexpected content", jAttachment2.getString("content_type").startsWith("application/ics"));

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
