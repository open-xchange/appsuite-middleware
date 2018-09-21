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

package com.openexchange.ajax.chronos;

import static com.openexchange.java.Autoboxing.I;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.ajax.chronos.factory.AttendeeFactory;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.configuration.asset.Asset;
import com.openexchange.configuration.asset.AssetType;
import com.openexchange.exception.OXException;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.Attendee.CuTypeEnum;
import com.openexchange.testing.httpclient.models.CalendarUser;
import com.openexchange.testing.httpclient.models.ChronosAttachment;
import com.openexchange.testing.httpclient.models.ChronosCalendarResultResponse;
import com.openexchange.testing.httpclient.models.ChronosMultipleCalendarResultResponse;
import com.openexchange.testing.httpclient.models.DeleteBody;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventId;
import com.openexchange.testing.httpclient.models.MailAttachment;
import com.openexchange.testing.httpclient.models.MailData;
import com.openexchange.testing.httpclient.models.MailResponse;
import com.openexchange.testing.httpclient.models.MailsResponse;
import com.openexchange.testing.httpclient.models.UpdateBody;
import com.openexchange.testing.httpclient.models.UserData;
import com.openexchange.testing.httpclient.models.UserResponse;
import com.openexchange.testing.httpclient.modules.MailApi;
import com.openexchange.testing.httpclient.modules.UserApi;

/**
 * {@link BasicCommentTest} - Tests for <a href="https://jira.open-xchange.com/browse/MW-989">MW-989</a>
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public class BasicCommentTest extends AbstractChronosTest {

    /**
     * Columns: received_date, id, folder_id, subject
     */
    private static final String COLUMNS = "610,600,601,607";

    private static final String INBOX = "default0%2FINBOX";

    private final static String UPDATE = "Important update message!!";

    private final static String DELETE = "It's cloudy outside. Lets shift the event";

    private EventData eventData;

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        if (null != eventData) {
            eventManager.deleteEvent(getEventId());
        }
        super.tearDown();
    }

    @Test
    public void testUpdate() throws Exception {
        String summary = "Test comment function on update";
        eventData = EventFactory.createSingleTwoHourEvent(apiClient.getUserId().intValue(), summary);

        setAttendees();

        eventData = eventManager.createEvent(eventData);
        UpdateBody body = new UpdateBody();
        body.setEvent(eventData);
        body.setComment(UPDATE);
        eventData.setDescription("Description got updated.");
        ChronosCalendarResultResponse response = chronosApi.updateEvent(apiClient.getSession(), getFolderId(), eventData.getId(), body, Long.valueOf(System.currentTimeMillis()), null, null, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, null, null, Boolean.FALSE);
        Assert.assertThat(response.getErrorDesc(), response.getError(), nullValue());

        validateMailInSecondUsersInbox("Appointment changed: " + summary, UPDATE);
    }

    @Test
    public void testUpdateWithAttachment() throws Exception {
        String summary = "Test comment function on update with attachments";
        eventData = EventFactory.createSingleTwoHourEvent(apiClient.getUserId().intValue(), summary);

        setAttendees();

        eventData = eventManager.createEvent(eventData);

        Asset asset = assetManager.getRandomAsset(AssetType.pdf);
        File file = new File(asset.getAbsolutePath());
        Assert.assertTrue(file.exists());
        ChronosAttachment attachment = new ChronosAttachment();
        attachment.setFilename(file.getName());
        attachment.setUri("cid:file_0");
        attachment.setFmtType("application/pdf");
        eventData.setAttachments(Collections.singletonList(attachment));

        String html = chronosApi.updateEventWithAttachments(apiClient.getSession(), getFolderId(), eventData.getId(), Long.valueOf(System.currentTimeMillis()), buildJSON(), file, null, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE);
        Assert.assertFalse("Should not contain errors", html.contains("error"));

        validateMailInSecondUsersInbox("Appointment changed: " + summary, UPDATE);
    }

    @Test
    public void testDelete() throws Exception {
        String summary = "Test comment function on delete";
        eventData = EventFactory.createSingleTwoHourEvent(apiClient.getUserId().intValue(), summary);

        setAttendees();

        eventData = eventManager.createEvent(eventData);

        DeleteBody body = new DeleteBody();
        body.setComment(DELETE);
        body.setEvents(Collections.singletonList(getEventId()));

        ChronosMultipleCalendarResultResponse response = chronosApi.deleteEvent(apiClient.getSession(), Long.valueOf(System.currentTimeMillis()), body, null, null, Boolean.FALSE, Boolean.FALSE);
        Assert.assertThat(response.getErrorDesc(), response.getError(), nullValue());
        eventData = null;

        validateMailInSecondUsersInbox("Appointment canceled: " + summary, DELETE);
    }

    private void validateMailInSecondUsersInbox(String mailSubject, String comment) throws OXException, ApiException, Exception {
        Thread.sleep(3000);

        ApiClient apiClient2 = generateApiClient(testUser2);
        rememberClient(apiClient2);
        MailApi mailApi = new MailApi(apiClient2);

        MailResponse response = mailApi.getMail(apiClient2.getSession(), INBOX, getMailId(apiClient2, mailApi, mailSubject), null, null, null, null, null, null, null, null, null, null);
        MailData mailData = response.getData();
        Assert.assertThat("No mail data", mailData, notNullValue());
        MailAttachment mailAttachment = mailData.getAttachments().get(0);
        Assert.assertTrue("Should contain comment", mailAttachment.getContent().contains("<i>" + comment + "</i>"));
    }

    private String getMailId(ApiClient apiClient2, MailApi mailApi, String summary) throws Exception {
        MailsResponse mailsResponse = mailApi.getAllMails(apiClient2.getSession(), INBOX, COLUMNS, null, Boolean.FALSE, Boolean.FALSE, "600", "desc", null, null, I(10), null);
        List<List<String>> data = mailsResponse.getData();
        Assert.assertThat("No mails found", I(data.size()), not(I(0)));
        String mailId = null;
        for (List<String> singleMailData : data) {
            // Indices based on COLUMNS
            if (summary.equals(singleMailData.get(3))) {
                mailId = singleMailData.get(1);
                break;
            }
        }
        Assert.assertThat("No update/cancel mail found", mailId, notNullValue());
        return mailId;
    }

    private String buildJSON() {
        StringBuilder sb = new StringBuilder("{\"events\":");
        sb.append(eventData.toJson());
        sb.append(", ");
        sb.append("\"comment\":");
        sb.append("\"").append(UPDATE).append("\"");
        sb.append("}");
        return sb.toString();
    }

    private EventId getEventId() {
        EventId id = new EventId();
        id.setId(eventData.getId());
        id.setFolder(getFolderId());
        return id;
    }

    private String getFolderId() {
        return null != eventData.getFolder() ? eventData.getFolder() : defaultFolderId;
    }

    private void setAttendees() throws ApiException, OXException, IOException, JSONException {
        Attendee organizer = createAttendee(getClient().getValues().getUserId());
        Attendee attendee = createAttendee(getClient2().getValues().getUserId());
        LinkedList<Attendee> attendees = new LinkedList<>();
        attendees.add(organizer);
        attendees.add(attendee);
        eventData.setAttendees(attendees);
        setOrganizer(organizer);
    }

    protected Attendee createAttendee(int userId) throws ApiException {
        Attendee attendee = AttendeeFactory.createAttendee(userId, CuTypeEnum.INDIVIDUAL);

        UserData userData = getUserInformation(userId);

        attendee.cn(userData.getDisplayName());
        attendee.comment("Comment for user " + userData.getDisplayName());
        attendee.email(userData.getEmail1());
        attendee.setUri("mailto:" + userData.getEmail1());
        return attendee;
    }

    protected void setOrganizer(Attendee organizer) {
        CalendarUser c = new CalendarUser();
        c.cn(organizer.getCn());
        c.email(organizer.getEmail());
        c.entity(organizer.getEntity());
        eventData.setOrganizer(c);
        eventData.setCalendarUser(c);
    }

    private UserData getUserInformation(int userId) throws ApiException {
        UserApi api = new UserApi(getApiClient());
        UserResponse userResponse = api.getUser(getApiClient().getSession(), String.valueOf(userId));
        return userResponse.getData();
    }

}
