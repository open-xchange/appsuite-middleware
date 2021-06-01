/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.chronos.itip.bugs;

import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertAttendeePartStat;
import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertSingleChange;
import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertSingleEvent;
import static com.openexchange.ajax.chronos.itip.ITipUtil.constructBody;
import static com.openexchange.ajax.chronos.itip.ITipUtil.receiveIMip;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.io.File;
import java.util.UUID;
import java.util.regex.Pattern;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.junit.Test;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.itip.AbstractITipAnalyzeTest;
import com.openexchange.ajax.chronos.itip.ITipUtil;
import com.openexchange.chronos.scheduling.SchedulingMethod;
import com.openexchange.testing.httpclient.models.AnalysisChangeNewEvent;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.MailData;
import com.openexchange.testing.httpclient.models.MailDestinationData;
import com.openexchange.testing.httpclient.models.MailImportResponse;
import com.openexchange.testing.httpclient.models.MailSourceResponse;
import com.openexchange.testing.httpclient.modules.MailApi;

/**
 * {@link ReplyBugsTest}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class ReplyBugsTest extends AbstractITipAnalyzeTest {

    @Test
    public void testBug59220() throws Exception {

        String summary = "Test for bug 59220 " + UUID.randomUUID().toString();
        EventData eventToCreate = EventFactory.createSingleTwoHourEvent(0, summary);
        Attendee replyingAttendee = prepareCommonAttendees(eventToCreate);
        createdEvent = eventManager.createEvent(eventToCreate);

        /*
         * Receive mail as attendee
         */
        MailData iMip = receiveIMip(apiClientC2, userResponseC1.getData().getEmail1(), summary, 0, SchedulingMethod.REQUEST);
        AnalysisChangeNewEvent newEvent = assertSingleChange(analyze(apiClientC2, iMip)).getNewEvent();
        assertNotNull(newEvent);
        assertEquals(createdEvent.getUid(), newEvent.getUid());
        assertAttendeePartStat(newEvent.getAttendees(), replyingAttendee.getEmail(), PartStat.NEEDS_ACTION.getStatus());

        /*
         * reply with "accepted"
         */
        EventData eventData = assertSingleEvent(accept(apiClientC2, constructBody(iMip), null), createdEvent.getUid());
        assertAttendeePartStat(eventData.getAttendees(), replyingAttendee.getEmail(), PartStat.ACCEPTED.getStatus());

        /*
         * Receive mail as organizer and download
         */
        MailData reply = receiveIMip(apiClient, replyingAttendee.getEmail(), summary, 0, SchedulingMethod.REPLY);
        MailSourceResponse source = new MailApi(apiClient).getMailSource(reply.getFolderId(), reply.getId(), reply.getId(), null, null);
        assertNull(source.getError());
        String mail = source.getData();
        mail.replaceAll(Pattern.quote("{{mailto}}"), "MAILTO");
        MailDestinationData destinationData = createMailInInbox(mail);

        /*
         * Bug happened due a mismatch of saved URI with "mailto:XX" in attendee object
         * VS "MAILTO:XX" in transmitted attendee object. This lead to a false-positive
         * update action
         */
        analyze(destinationData.getId());

        // Accept changes
        update(constructBody(destinationData.getId()));

        /*
         * Check that there is no action after organizer accepted the changes
         */
        analyze(destinationData.getId(), CustomConsumers.EMPTY);
    }

    /**
     * Uploads a mail to the INBOX
     *
     * @param eml The mail to upload
     * @return {@link MailDestinationData} with set mail ID and folder ID
     * @throws Exception In case of error
     */
    protected MailDestinationData createMailInInbox(String eml) throws Exception {
        File tmpFile = File.createTempFile("test", ".eml");
        FileWriterWithEncoding writer = new FileWriterWithEncoding(tmpFile, "ASCII");
        writer.write(eml);
        writer.close();

        MailApi mailApi = new MailApi(getApiClient());
        MailImportResponse importMail = mailApi.importMail(ITipUtil.FOLDER_MACHINE_READABLE, tmpFile, null, Boolean.TRUE);
        return importMail.getData().get(0);
    }
}
