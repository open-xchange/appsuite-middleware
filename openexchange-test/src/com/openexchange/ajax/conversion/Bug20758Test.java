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

package com.openexchange.ajax.conversion;

import java.util.TimeZone;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.ajax.conversion.actions.ConvertRequest;
import com.openexchange.ajax.conversion.actions.ConvertResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.mail.FolderAndID;
import com.openexchange.ajax.mail.contenttypes.MailContentType;
import com.openexchange.ajax.mail.netsol.NetsolTestConstants;
import com.openexchange.ajax.mail.netsol.actions.NetsolGetRequest;
import com.openexchange.ajax.mail.netsol.actions.NetsolGetResponse;
import com.openexchange.ajax.mail.netsol.actions.NetsolSendRequest;
import com.openexchange.ajax.mail.netsol.actions.NetsolSendResponse;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailListField;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;

/**
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug20758Test extends AbstractConversionTest {

    private String uuid;
    private String[] mailFolderAndMailID1;
    private String[] mailFolderAndMailID2;
    private AJAXClient client1;
    private AJAXClient client2;
    private Object sequenceId1;
    private Object sequenceId2;
    private String ical1;
    private String ical2;

    public Bug20758Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        client1 = getClient();
        client2 = new AJAXClient(User.User1);

        client1.getValues().setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));

        uuid = UUID.randomUUID().toString();

        ical1 = new StringBuilder()
        .append("BEGIN:VCALENDAR\n")
        .append("VERSION:2.0\n")
        .append("METHOD:REQUEST\n")
        .append("BEGIN:VEVENT\n")
        .append("ORGANIZER:").append(client1.getValues().getSendAddress()).append('\n')
        .append("ATTENDEE;PARTSTAT=ACCEPTED;CN=Da Organiza:Mailto:").append(client1.getValues().getSendAddress()).append('\n')
        .append("ATTENDEE;RSVP=TRUE;TYPE=INDIVIDUAL;CN=First User:Mailto:").append(client1.getValues().getSendAddress()).append('\n')
        .append("ATTENDEE;RSVP=TRUE;TYPE=INDIVIDUAL;CN=Second User:Mailto:").append(client2.getValues().getSendAddress()).append('\n')
        .append("DTSTART:20111115T133000Z\n")
        .append("DTEND:20111115T143000Z\n")
        .append("SUMMARY:Weihnachtsferien\n")
        .append("UID:ls0h48paommdkntd8ekdfquqbs@google.com\n")
        .append("SEQUENCE:0\n")
        .append("DTSTAMP:20111115T110530Z\n")
        .append("END:VEVENT\n")
        .append("END:VCALENDAR")
        .toString();

        ical2 = "BEGIN:VCALENDAR\n" +
        "PRODID:Open-Xchange\n" +
        "VERSION:2.0\n" +
        "CALSCALE:GREGORIAN\n" +
        "METHOD:REQUEST\n" +
        "BEGIN:VEVENT\n" +
        "DTSTAMP:20111201T082524Z\n" +
        "SUMMARY:Test 20829\n" +
        "DTSTART;TZID=Europe/Berlin:20111202T123000\n" +
        "DTEND;TZID=Europe/Berlin:20111202T133000\n" +
        "CLASS:PUBLIC\n" +
        "LOCATION:12:30\n" +
        "TRANSP:OPAQUE\n" +
        "UID:6f7d7ef0-8def-4997-8427-2612b7095865\n" +
        "CREATED:20111201T082524Z\n" +
        "LAST-MODIFIED:20111201T082524Z\n" +
        "ORGANIZER:" + client1.getValues().getSendAddress() + "\n" +
        "ATTENDEE;ROLE=REQ-PARTICIPANT;CUTYPE=INDIVIDUAL:mailto:" + client1.getValues().getSendAddress() + "\n" +
        "ATTENDEE;CUTYPE=INDIVIDUAL;PARTSTAT=NEEDS-ACTION;ROLE=REQ-PARTICIPANT;RSVP=TRUE:mailto:" + client2.getValues().getSendAddress() + "\n" +
        "END:VEVENT\n" +
        "BEGIN:VTIMEZONE\n" +
        "TZID:Europe/Berlin\n" +
        "TZURL:http://tzurl.org/zoneinfo-outlook/Europe/Berlin\n" +
        "X-LIC-LOCATION:Europe/Berlin\n" +
        "BEGIN:DAYLIGHT\n" +
        "TZOFFSETFROM:+0100\n" +
        "TZOFFSETTO:+0200\n" +
        "TZNAME:CEST\n" +
        "DTSTART:19700329T020000\n" +
        "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\n" +
        "END:DAYLIGHT\n" +
        "BEGIN:STANDARD\n" +
        "TZOFFSETFROM:+0200\n" +
        "TZOFFSETTO:+0100\n" +
        "TZNAME:CET\n" +
        "DTSTART:19701025T030000\n" +
        "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\n" +
        "END:STANDARD\n" +
        "END:VTIMEZONE\n" +
        "END:VCALENDAR";

        mailFolderAndMailID1 = createMail(ical1);
        sequenceId1 = getSequenceIdForMail(client1, mailFolderAndMailID1);

        mailFolderAndMailID2 = createMail(ical2);
        sequenceId2 = getSequenceIdForMail(client1, mailFolderAndMailID2);
    }

    public void testWithoutTimeZone() throws Exception {
        JSONObject jsonObject = internal(mailFolderAndMailID1, sequenceId1, new JSONArray());
        assertEquals("Wrong timezone", "Europe/Berlin", jsonObject.get("timezone"));
        assertEquals("Wrong start date", 1321367400000L, jsonObject.get("start_date")); //15.11.2011 14:30

        jsonObject = internal(mailFolderAndMailID2, sequenceId2, new JSONArray());
        assertEquals("Wrong timezone", "Europe/Berlin", jsonObject.get("timezone"));
        assertEquals("Wrong start date", 1322829000000L, jsonObject.get("start_date")); //02.12.2011 12:30
    }

    public void testWithTimeZone() throws Exception {
        JSONObject jsonObject = internal(mailFolderAndMailID2, sequenceId2, new JSONArray().put(new JSONObject().put("com.openexchange.groupware.calendar.timezone", "UTC")));
        assertEquals("Wrong timezone", "America/New_York", jsonObject.get("timezone"));
        assertEquals("Wrong start date", 1321345800000L, jsonObject.get("start_date")); //15.11.2011 08:30

        jsonObject = internal(mailFolderAndMailID2, sequenceId2, new JSONArray());
        assertEquals("Wrong timezone", "Europe/Berlin", jsonObject.get("timezone"));
        assertEquals("Wrong start date", 1322807400000L, jsonObject.get("start_date")); //02.12.2011 06:30
    }

    private JSONObject internal(String[] mail, Object sequenceId, JSONArray args) throws Exception {
        JSONObject jsonBody = new JSONObject();
        JSONObject jsonSource = new JSONObject().put("identifier", "com.openexchange.mail.ical");
        jsonSource.put("args", new JSONArray().put(
                new JSONObject().put("com.openexchange.mail.conversion.fullname", mail[0])).put(
                new JSONObject().put("com.openexchange.mail.conversion.mailid", mail[1])).put(
                new JSONObject().put("com.openexchange.mail.conversion.sequenceid", sequenceId)));
        jsonBody.put("datasource", jsonSource);
        JSONObject jsonHandler = new JSONObject().put("identifier", "com.openexchange.ical.json");
        jsonHandler.put("args", args);
        jsonBody.put("datahandler", jsonHandler);
        ConvertResponse convertResponse = (ConvertResponse) Executor.execute(client1.getSession(), new ConvertRequest(jsonBody, true));
        return ((JSONArray) convertResponse.getData()).getJSONObject(0);
    }

    protected String[] createMail(String ical) throws Exception {
        byte[] ICAL_BYTES = ical.getBytes();

        JSONObject mail = new JSONObject();
        mail.put(MailJSONField.FROM.getKey(), client1.getValues().getSendAddress());
        mail.put(MailJSONField.RECIPIENT_TO.getKey(), client2.getValues().getSendAddress() + "," + client2.getValues().getSendAddress());
        mail.put(MailJSONField.RECIPIENT_CC.getKey(), "");
        mail.put(MailJSONField.RECIPIENT_BCC.getKey(), "");
        mail.put(MailJSONField.SUBJECT.getKey(), "New Event");
        mail.put(MailJSONField.PRIORITY.getKey(), "3");

        JSONObject bodyObject = new JSONObject();
        bodyObject.put(MailJSONField.CONTENT_TYPE.getKey(), MailContentType.ALTERNATIVE.toString());
        bodyObject.put(MailJSONField.CONTENT.getKey(), NetsolTestConstants.MAIL_TEXT_BODY);

        JSONArray attachments = new JSONArray();
        attachments.put(bodyObject);

        mail.put(MailJSONField.ATTACHMENTS.getKey(), attachments);

        UnsynchronizedByteArrayInputStream in = new UnsynchronizedByteArrayInputStream(ICAL_BYTES);

        NetsolSendResponse response = Executor.execute(client1.getSession(), new NetsolSendRequest(mail.toString(), in, "text/calendar; charset=US-ASCII", "ical.ics"));
        assertTrue("Send failed", response.getFolderAndID() != null);
        assertTrue("Duration corrupt", response.getRequestDuration() > 0);
        String[] mailFolderAndMailID = response.getFolderAndID();

        mailFolderAndMailID[1] = parseMailId(mailFolderAndMailID[1]);

        return mailFolderAndMailID;
    }

    protected String getSequenceIdForMail(AJAXClient c, String[] mailFolderAndMailID) throws Exception {
        FolderAndID fai = new FolderAndID(mailFolderAndMailID[0], mailFolderAndMailID[1]);
        NetsolGetResponse resp = Executor.execute(c.getSession(), new NetsolGetRequest(fai, true));
        JSONObject mailObject = (JSONObject) resp.getData();
        JSONArray att = mailObject.getJSONArray(MailJSONField.ATTACHMENTS.getKey());
        int len = att.length();
        String sequenceId = null;
        for (int i = 0; i < len && sequenceId == null; i++) {
            final JSONObject attachObj = att.getJSONObject(i);
            if (attachObj.getString(MailJSONField.CONTENT_TYPE.getKey()).startsWith("text/calendar")) {
                sequenceId = attachObj.getString(MailListField.ID.getKey());
            }
        }

        return sequenceId;
    }

}

