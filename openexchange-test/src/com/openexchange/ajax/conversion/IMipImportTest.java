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

import java.util.Date;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
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
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailListField;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;


/**
 * {@link IMipImportTest}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class IMipImportTest extends AbstractConversionTest {

    private AJAXClient client1;
    private AJAXClient client2;
    private String uuid;
    private String[] mailFolderAndMailID;
    private String[] mailFolderAndMailID2;
    private String sequenceId;
    private String sequenceId2;
    private int objectId;
    private int folder;

    public IMipImportTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        client1 = getClient();
        client2 = new AJAXClient(User.User2);

        uuid = UUID.randomUUID().toString();

        mailFolderAndMailID = createMail(client1);
        mailFolderAndMailID2 = createMail(client2);

        sequenceId = getSequenceIdForMail(client1, mailFolderAndMailID);
        sequenceId2 = getSequenceIdForMail(client2, mailFolderAndMailID2);
    }

    @Override
    protected void tearDown() throws Exception {
        client.execute(new DeleteRequest(objectId, folder, new Date(Long.MAX_VALUE)));

        super.tearDown();
    }

    public void testIMIP() throws Exception {
        String[][] folderIdFirst = confirm(client1, mailFolderAndMailID, sequenceId, CalendarObject.ACCEPT, "Positive");
        String[][] folderIdSecond = confirm(client2, mailFolderAndMailID2, sequenceId2, CalendarObject.DECLINE, "Negative");

        assertEquals("Wrong amount of appointments", 1, folderIdFirst.length);

        folder = Integer.valueOf(folderIdFirst[0][0]);
        objectId = Integer.valueOf(folderIdFirst[0][1]);

        GetRequest getRequest = new GetRequest(folder, objectId);
        GetResponse getResponse = client.execute(getRequest);
        Appointment appointment = getResponse.getAppointment(client.getValues().getTimeZone());

        boolean foundFirst = false;
        boolean foundSecond = false;

        assertEquals("Wrong amount of participants", 2, appointment.getUsers().length);
        assertEquals("No new objectId for update", 0, folderIdSecond.length);

        for (UserParticipant user : appointment.getUsers()) {
            if (user.getIdentifier() == client1.getValues().getUserId()) {
                foundFirst = true;
                assertEquals("Wrong status", CalendarObject.ACCEPT, user.getConfirm());
                assertEquals("Wrong message", "Positive", user.getConfirmMessage());
            } else if (user.getIdentifier() == client2.getValues().getUserId()) {
                foundSecond = true;
                assertEquals("Wrong status", CalendarObject.DECLINE, user.getConfirm());
                assertEquals("Wrong message", "Negative", user.getConfirmMessage());
            }
        }

        assertTrue("Missing user", foundFirst);
        assertTrue("Missing user", foundSecond);
    }

    protected String[][] confirm(AJAXClient c, String[] folderAndId, String seq, int confirm, String message) throws Exception {
        JSONObject jsonBody = new JSONObject();
        JSONObject jsonSource = new JSONObject().put("identifier", "com.openexchange.mail.ical");
        jsonSource.put("args", new JSONArray().put(
                new JSONObject().put("com.openexchange.mail.conversion.fullname", folderAndId[0])).put(
                new JSONObject().put("com.openexchange.mail.conversion.mailid", folderAndId[1])).put(
                new JSONObject().put("com.openexchange.mail.conversion.sequenceid", seq)));
        jsonBody.put("datasource", jsonSource);
        JSONObject jsonHandler = new JSONObject().put("identifier", "com.openexchange.ical");
        jsonHandler.put("args", new JSONArray().put(
                new JSONObject().put("com.openexchange.groupware.calendar.folder", getPrivateCalendarFolder()))
                .put(new JSONObject().put("com.openexchange.groupware.task.folder", getPrivateTaskFolder()))
                .put(new JSONObject().put("com.openexchange.groupware.calendar.confirmstatus", confirm))
                .put(new JSONObject().put("com.openexchange.groupware.calendar.confirmmessage", message)));
        jsonBody.put("datahandler", jsonHandler);
        ConvertResponse convertResponse = (ConvertResponse) Executor.execute(c.getSession(),
                new ConvertRequest(jsonBody, true));

        return convertResponse.getFoldersAndIDs();
    }

    protected String[] createMail(AJAXClient c) throws Exception {
        byte[] ICAL_BYTES = new StringBuilder()
            .append("BEGIN:VCALENDAR\n")
            .append("VERSION:2.0\n")
            .append("METHOD:REQUEST\n")
            .append("BEGIN:VEVENT\n")
            .append("ORGANIZER:").append(client1.getValues().getSendAddress()).append('\n')
            .append("ATTENDEE;PARTSTAT=ACCEPTED;CN=Da Organiza:Mailto:").append(client1.getValues().getSendAddress()).append('\n')
            //.append("ATTENDEE;RSVP=TRUE;TYPE=INDIVIDUAL;CN=Firs User:Mailto:").append(client1.getValues().getSendAddress()).append("\n")
            .append("ATTENDEE;RSVP=TRUE;TYPE=INDIVIDUAL;CN=Second User:Mailto:").append(client2.getValues().getSendAddress()).append('\n')
            .append("DTSTART;VALUE=DATE:20061221\n")
            .append("DTEND;VALUE=DATE:20070106\n")
            .append("SUMMARY:Weihnachtsferien\n")
            .append("UID:").append(uuid).append('\n')
            .append("SEQUENCE:8\n")
            .append("DTSTAMP:20060520T163834Z\n")
            .append("END:VEVENT\n")
            .append("END:VCALENDAR")
            .toString().getBytes();

        JSONObject mail = new JSONObject();
        mail.put(MailJSONField.FROM.getKey(), c.getValues().getSendAddress());
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

        NetsolSendResponse response = Executor.execute(c.getSession(), new NetsolSendRequest(mail.toString(), in, "text/calendar; charset=US-ASCII", "ical.ics"));
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
