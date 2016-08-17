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

package com.openexchange.mail.parser.handlers;

import java.io.FileInputStream;
import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.groupware.contexts.SimContext;
import com.openexchange.groupware.ldap.SimUser;
import com.openexchange.html.HtmlService;
import com.openexchange.html.SimHtmlService;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.MimeSmilFixer;
import com.openexchange.mail.mime.MimeType2ExtMap;
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

    @Override
    public void setUp() throws Exception {
        super.setUp();
        MimeType2ExtMap.addMimeType("application/pdf", "pdf");
    }

    /**
     * Ensure that alternative text/xml attachment that is part of multipart/alternative gets ignored
     */
    public void testBug32692() {
        try {
            final byte[] bytes = ("Content-Type: multipart/alternative; boundary=\"----=_Part_8_228463983.1398448328908\"\n" +
                "Date: Fri, 15 Nov 2013 04:55:25 -0800 (PST)\n" +
                "From: \"email1@mytrial.co.uk\" <email1@mytrial.co.uk>\n" +
                "To: ho hum <email53@mytrial.co.uk>\n" +
                "Message-ID: <11574183.303149.1384520125498.chat@gmail.com>\n" +
                "Subject: Chat with email1@mytrial.co.uk\n" +
                "MIME-Version: 1.0\n" +
                "\n" +
                "------=_Part_8_228463983.1398448328908\n" +
                "Content-Type: text/xml; charset=utf-8\n" +
                "Content-Transfer-Encoding: 7bit\n" +
                "\n" +
                "<con:conversation xmlns:con=\"google:archive:conversation\">\n" +
                "<cli:message to=\"email53@mytrial.co.uk\" iconset=\"classic\" from=\"email1@mytrial.co.uk\" int:cid=\"278391101870075059\" int:sequence-no=\"1\" int:time-stamp=\"1384520125480\" xmlns:cli=\"jabber:client\" xmlns:int=\"google:internal\">\n" +
                "<cli:body>hi</cli:body><met:google-mail-signature xmlns:met=\"google:metadata\">2jLeBillxquvrnTPZKm8uZNifgY</met:google-mail-signature>\n" +
                "<x stamp=\"20131115T12:55:25\" xmlns=\"jabber:x:delay\"/><time ms=\"1384520125498\" xmlns=\"google:timestamp\"/>\n" +
                "</cli:message></con:conversation>\n" +
                "\n" +
                "------=_Part_8_228463983.1398448328908\n" +
                "Content-Type: text/html; charset=utf-8\n" +
                "Content-Transfer-Encoding: 7bit\n" +
                "\n" +
                "hi\n" +
                "\n" +
                "------=_Part_8_228463983.1398448328908--").getBytes();

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
            assertEquals("Unexpected number of attachments", 1, jAttachments.length());

            final JSONObject jAttachment1 = jAttachments.getJSONObject(0);
            assertNotNull(jAttachment1);

            assertTrue("Unexpected content", jAttachment1.getString("content_type").startsWith("text/html"));

            // System.out.println(jAttachment1.toString(2));

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
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
            MimeType2ExtMap.addMimeType("application/pdf", "pdf");
            MimeType2ExtMap.addMimeType("application/rtf", "rtf");
            MimeType2ExtMap.addMimeType("application/ics", "ics");

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
            assertEquals("Unexpected number of attachments", 3, jAttachments.length());

            final JSONObject jAttachment1 = jAttachments.getJSONObject(0);
            assertNotNull(jAttachment1);
            final JSONObject jAttachment2 = jAttachments.getJSONObject(1);
            assertNotNull(jAttachment2);
            final JSONObject jAttachment3 = jAttachments.getJSONObject(2);
            assertNotNull(jAttachment3);

            assertTrue("Unexpected content", jAttachment1.getString("content_type").startsWith("text/html"));
            assertTrue("Unexpected content", jAttachment2.getString("content_type").startsWith("text/calendar"));
            assertTrue("Unexpected content", jAttachment3.getString("content_type").startsWith("application/ics"));

            assertTrue("Unexpected Content-Dispostion for " + jAttachment2.getString("id"), jAttachment2.getString("disp").startsWith("attachment"));

            // System.out.println(jAttachment2.toString(2));

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    /**
     * Ensure that proper TNEF parsing
     */
    public void testBug35899() {
        try {

            final MailMessage mail = MimeMessageConverter.convertMessage(new FileInputStream("./test/com/openexchange/mail/parser/handlers/tnef_oloxproblemmail.eml"));

            // Preps
            MimeType2ExtMap.addMimeType("application/pdf", "pdf");
            MimeType2ExtMap.addMimeType("application/rtf", "rtf");
            MimeType2ExtMap.addMimeType("image/png", "png");

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
            assertEquals("Unexpected number of attachments", 4, jAttachments.length());

            final JSONObject jAttachment1 = jAttachments.getJSONObject(0);
            assertNotNull(jAttachment1);
            final JSONObject jAttachment2 = jAttachments.getJSONObject(1);
            assertNotNull(jAttachment2);
            final JSONObject jAttachment3 = jAttachments.getJSONObject(2);
            assertNotNull(jAttachment3);
            final JSONObject jAttachment4 = jAttachments.getJSONObject(3);
            assertNotNull(jAttachment4);

            System.out.println("------- Debug Output ------");
            System.out.println(jMail.toString(2));
            assertTrue("Unexpected content", jAttachment1.getString("content_type").startsWith("text/plain"));
            assertTrue("Unexpected content", jAttachment2.getString("content_type").startsWith("application/rtf"));
            assertTrue("Unexpected content", jAttachment3.getString("content_type").startsWith("application/pdf"));
            assertTrue("Unexpected content", jAttachment4.getString("content_type").startsWith("image/png"));

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * Ensure that proper TNEF parsing
     */
    public void testBug37141() {
        try {

            final MailMessage mail = MimeMessageConverter.convertMessage(new FileInputStream("./test/com/openexchange/mail/parser/handlers/testproblem.eml"));

            // Preps
            MimeType2ExtMap.addMimeType("application/pdf", "pdf");
            MimeType2ExtMap.addMimeType("application/rtf", "rtf");
            MimeType2ExtMap.addMimeType("image/png", "png");

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

            System.out.println("------- Debug Output ------");
            System.out.println(jMail.toString(2));
            assertTrue("Unexpected content", jAttachment1.getString("content_type").startsWith("text/plain"));
            assertTrue("Unexpected content", jAttachment2.getString("content_type").startsWith("application/pdf"));

            assertTrue("Unexpected message body", jAttachment1.getString("content").indexOf("da muss ein vermutlich") > 0);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * Ensure PGP signature is not handled in S/MIME validation
     */
    public void testBug40645() {
        try {
            final MailMessage mail = MimeMessageConverter.convertMessage(new FileInputStream("./test/com/openexchange/mail/parser/handlers/Test_PGP.eml"));

            // Preps
            MimeType2ExtMap.addMimeType("application/pdf", "pdf");
            MimeType2ExtMap.addMimeType("application/rtf", "rtf");
            MimeType2ExtMap.addMimeType("image/png", "png");

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

            System.out.println("------- Debug Output ------");
            System.out.println(jMail.toString(2));
            assertTrue("Unexpected content", jAttachment1.getString("content_type").startsWith("text/plain"));
            assertTrue("Unexpected content", jAttachment2.getString("content_type").startsWith("application/pgp-signature"));

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testCorrectNestedMessageIdsTillLevel2() {
        try {
            final MailMessage mail = MimeMessageConverter.convertMessage(new FileInputStream("./test/com/openexchange/mail/parser/handlers/test_mail_46443.eml"));
            String folder = "default0/INBOX";
            // Preperations
            ServerServiceRegistry.getInstance().addService(HtmlService.class, new SimHtmlService());

            UserSettingMail usm = new UserSettingMail(1, 1);
            usm.parseBits(627479);

            ServerSession session = new SimServerSession(new SimContext(1), new SimUser(1), null);
            JsonMessageHandler handler = new JsonMessageHandler(0, folder, DisplayMode.DISPLAY, true, session, usm, false, 0);

            MailMessageParser parser = new MailMessageParser();
            parser.parseMailMessage(mail, handler);

            JSONObject jMail = handler.getJSONObject();
            assertNotNull("Mail was not parsed.", jMail);

            JSONArray nestedMessages = jMail.getJSONArray("nested_msgs");
            assertNotNull("No nested messages were parsed", nestedMessages);
            assertEquals("Unexpected number of nested messages", 2, nestedMessages.length());
            
            // First and second level nested message-id calculation correct?
            final JSONObject nestedMessage2 = nestedMessages.getJSONObject(1);
            assertNotNull(nestedMessage2);
            assertEquals("False calculation of first nested message-id", "3", nestedMessage2.getString("id"));
            assertEquals("False calculation of the second nested message-id ", "3.2", nestedMessage2.getJSONArray("nested_msgs").getJSONObject(0).get("id"));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    public void testCorrectNestedMessageIdsOnLevel3To4() {
        try {
            final MailMessage mail = MimeMessageConverter.convertMessage(new FileInputStream("./test/com/openexchange/mail/parser/handlers/test_mail_46443.eml"));
            String folder = "default0/INBOX";
            // Preperations
            ServerServiceRegistry.getInstance().addService(HtmlService.class, new SimHtmlService());

            UserSettingMail usm = new UserSettingMail(1, 1);
            usm.parseBits(627479);

            ServerSession session = new SimServerSession(new SimContext(1), new SimUser(1), null);
            JsonMessageHandler handler = new JsonMessageHandler(0, folder, DisplayMode.DISPLAY, true, session, usm, false, 0);
            handler.setInitialiserSequenceId("2.2");
            MailMessageParser parser = new MailMessageParser();
            
            parser.parseMailMessage(mail, handler);

            JSONObject jMail = handler.getJSONObject();
            assertNotNull("Mail was not parsed.", jMail);

            JSONArray nestedMessages = jMail.getJSONArray("nested_msgs");
            JSONArray attachments = jMail.getJSONArray("attachments");
            
            assertNotNull("No nested messages were parsed", nestedMessages);
            assertEquals("Unexpected number of nested messages", 2, nestedMessages.length());
            
            assertNotNull("No attachments were parsed", attachments);
            assertEquals("Unexpected number of attachments", 1, attachments.length());
            
            // third and fourth level nested message-id calculation correct?
            final JSONObject nestedMessage2 = nestedMessages.getJSONObject(1);
            assertNotNull(nestedMessage2);
            assertEquals("False calculation of first nested message-id", "2.2.3", nestedMessage2.getString("id"));
            assertEquals("False calculation of the second nested message-id ", "2.2.3.2", nestedMessage2.getJSONArray("nested_msgs").getJSONObject(0).get("id"));
            // third and fourth level attachment-id calculation correct?
            final JSONObject attachment1 = attachments.getJSONObject(0);
            assertNotNull(attachment1);
            assertEquals("False calculation of first attachment-id", "2.2.1", attachment1.getString("id"));
            final JSONObject attachment2 = nestedMessage2.getJSONArray("attachments").getJSONObject(0);
            assertNotNull(attachment2);
            assertEquals("False calculation of second and nested attachment-id", "2.2.3.1", attachment2.getString("id"));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
