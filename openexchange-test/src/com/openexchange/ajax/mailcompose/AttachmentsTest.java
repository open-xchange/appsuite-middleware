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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.ajax.mailcompose;

import static com.openexchange.java.Autoboxing.l;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Collections;
import org.junit.Test;
import com.openexchange.testing.httpclient.models.ComposeBody;
import com.openexchange.testing.httpclient.models.MailComposeAttachmentResponse;
import com.openexchange.testing.httpclient.models.MailComposeMessageModel;
import com.openexchange.testing.httpclient.models.MailComposeResponse;

/**
 * {@link AttachmentsTest}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.2
 */
public class AttachmentsTest extends AbstractMailComposeTest {

    @Test
    public void testAttachment() throws Exception {
        MailComposeMessageModel model = createNewCompositionSpace();
        check(api.postAttachments(getSessionId(), model.getId(), attachment));
        MailComposeResponse response = api.getMailComposeById(getSessionId(), model.getId());
        assertNotNull("Expected attachments.", response.getData().getAttachments());
        check(response);
        assertEquals("Expected one attachment.", 1, response.getData().getAttachments().size());
        assertEquals("Wrong attachment name.", attachment.getName(), response.getData().getAttachments().get(0).getName());
        assertTrue("Empty file.", l(response.getData().getAttachments().get(0).getSize()) > 0L);
    }

    @Test
    public void testReplaceAttachment() throws Exception {
        MailComposeMessageModel model = createNewCompositionSpace();
        check(api.postAttachments(getSessionId(), model.getId(), attachment));
        MailComposeResponse response = api.getMailComposeById(getSessionId(), model.getId());
        check(response);
        Long size1 = response.getData().getAttachments().get(0).getSize();

        MailComposeAttachmentResponse updateresponse = api.postAttachmentsById(getSessionId(), model.getId(), response.getData().getAttachments().get(0).getId(), attachment2);
        check(updateresponse);
        response = api.getMailComposeById(getSessionId(), model.getId());
        check(response);

        assertNotNull("Expected attachments.", response.getData().getAttachments());
        assertEquals("Expected one attachment.", 1, response.getData().getAttachments().size());
        assertEquals("Wrong attachment name.", attachment2.getName(), response.getData().getAttachments().get(0).getName());
        assertTrue("Empty file.", l(response.getData().getAttachments().get(0).getSize()) > 0L);
        assertNotEquals("Expected different file size.", size1, response.getData().getAttachments().get(0).getSize());
    }

    @Test
    public void testOriginalAttachment() throws Exception {
        ComposeBody body = new ComposeBody();
        body.setFolderId(mailWithAttachment.getFolderId());
        body.setId(mailWithAttachment.getId());
        MailComposeResponse reply = api.postMailCompose(getSessionId(), "REPLY", null, Collections.singletonList(body));
        check(reply);
        MailComposeMessageModel data = reply.getData();
        compositionSpaceIds.add(data.getId());

        MailComposeResponse response = api.getMailComposeById(getSessionId(), data.getId());
        check(response);
        assertEquals("Expected no attachment.", 0, response.getData().getAttachments().size());

        check(api.postAttachmentsOriginal(getSessionId(), data.getId()));
        response = api.getMailComposeById(getSessionId(), data.getId());
        check(response);
        assertNotNull("Expected attachments.", response.getData().getAttachments());
        assertEquals("Expected one attachment.", 1, response.getData().getAttachments().size());
        assertTrue("Empty file.", l(response.getData().getAttachments().get(0).getSize()) > 0L);
    }

    @Test
    public void testVcard() throws Exception {
        MailComposeMessageModel model = createNewCompositionSpace();
        check(api.postAttachmentsVcard(getSessionId(), model.getId()));
        MailComposeResponse response = api.getMailComposeById(getSessionId(), model.getId());
        check(response);
        assertNotNull("Expected attachments.", response.getData().getAttachments());
        assertEquals("Expected one attachment.", 1, response.getData().getAttachments().size());
        assertTrue("Wrong attachment name.", response.getData().getAttachments().get(0).getName().endsWith(".vcf"));
        assertTrue("Empty file.", l(response.getData().getAttachments().get(0).getSize()) > 0L);
    }

    @Test
    public void testDeleteAttachment() throws Exception {
        MailComposeMessageModel model = createNewCompositionSpace();
        check(api.postAttachments(getSessionId(), model.getId(), attachment));
        MailComposeResponse response = api.getMailComposeById(getSessionId(), model.getId());
        check(response);
        assertNotNull("Expected attachments.", response.getData().getAttachments());
        assertEquals("Expected one attachment.", 1, response.getData().getAttachments().size());
        assertEquals("Wrong attachment name.", attachment.getName(), response.getData().getAttachments().get(0).getName());
        assertTrue("Empty file.", l(response.getData().getAttachments().get(0).getSize()) > 0L);

        api.deleteAttachmentsById(getSessionId(), model.getId(), response.getData().getAttachments().get(0).getId());
        response = api.getMailComposeById(getSessionId(), model.getId());
        check(response);
        assertEquals("Expected no attachment.", 0, response.getData().getAttachments().size());
    }

    @Test
    public void testGetAttachment() throws Exception {
        MailComposeMessageModel model = createNewCompositionSpace();
        MailComposeAttachmentResponse postAttachments = api.postAttachments(getSessionId(), model.getId(), attachment);
        check(postAttachments);
        byte[] attachmentsById = api.getAttachmentsById(getSessionId(), model.getId(), postAttachments.getData().getId());
        assertTrue("No data.", attachmentsById.length > 100);
    }
}
