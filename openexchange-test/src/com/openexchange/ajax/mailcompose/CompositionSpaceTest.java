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

package com.openexchange.ajax.mailcompose;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import com.openexchange.java.Strings;
import com.openexchange.testing.httpclient.models.MailComposeGetResponse;
import com.openexchange.testing.httpclient.models.MailComposeRequestMessageModel;
import com.openexchange.testing.httpclient.models.MailComposeResponse;
import com.openexchange.testing.httpclient.models.MailComposeResponseMessageModel;
import com.openexchange.testing.httpclient.models.MailComposeSendResponse;

/**
 * {@link CompositionSpaceTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.2
 */
public class CompositionSpaceTest extends AbstractMailComposeTest {

    @Test
    public void testCreateCompositionSpace() throws Exception {
        MailComposeResponseMessageModel model = createNewCompositionSpace();
        assertNotNull(model);
    }

    @Test
    public void testSendMail() throws Exception {
        MailComposeResponseMessageModel model = createNewCompositionSpace();
        MailComposeResponse loaded = api.getMailComposeById(model.getId());
        check(loaded);
        assertNotNull(loaded);
        assertEquals("Wrong Composition Space loaded.", model.getId(), loaded.getData().getId());
        model.setFrom(getSender());
        model.setTo(getRecipient());
        model.setSubject(UUID.randomUUID().toString());
        model.setContent(UUID.randomUUID().toString());
        MailComposeSendResponse postMailComposeSend = api.postMailComposeSend(model.getId(), model.toJson(), null, null);
        check(postMailComposeSend);
        assertTrue(postMailComposeSend.getErrorDesc(), Strings.isEmpty(postMailComposeSend.getError()));
        System.out.println("\n\n\t" + postMailComposeSend);
        loaded = api.getMailComposeById(model.getId());
        assertEquals("Error expected.", "MSGCS-0007", loaded.getCode());
        assertNull("No data expected", loaded.getData());
    }

    @Test
    public void testSaveCompositionSpace() throws Exception {
        MailComposeResponseMessageModel model = createNewCompositionSpace();
        MailComposeResponse loaded = api.getMailComposeById(model.getId());
        check(loaded);
        assertEquals("Wrong Composition Space loaded.", model.getId(), loaded.getData().getId());
        assertNotNull(loaded);
        String id = model.getId();
        MailComposeSendResponse mailPath = api.getSave(id, null);
        check(mailPath);
        loaded = api.getMailComposeById(model.getId());
        assertEquals("Error expected.", "MSGCS-0007", loaded.getCode());
        assertNull("No data expected", loaded.getData());
    }

    @Test
    public void testPatchCompositionSpace() throws Exception {
        MailComposeResponseMessageModel model = createNewCompositionSpace();

        MailComposeRequestMessageModel update = new MailComposeRequestMessageModel();
        update.setId(model.getId());
        String subject = "Changed subject";
        List<String> sender = getSender();
        List<List<String>> recipient = getRecipient();
        update.setSubject(subject);
        update.setFrom(sender);
        update.setTo(recipient);
        check(api.patchMailComposeById(update.getId(), update, null));
        MailComposeResponse loaded = api.getMailComposeById(model.getId());
        check(loaded);
        MailComposeResponseMessageModel loadedData = loaded.getData();
        assertEquals("Wrong id.", model.getId(), loadedData.getId());
        assertEquals("Wrong subject.", subject, loadedData.getSubject());
        assertEquals("Wrong sender.", sender.get(0), loadedData.getFrom().get(0));
        assertEquals("Wrong sender mail.", sender.get(1), loadedData.getFrom().get(1));
        assertEquals("Wrong amount of recipients.", 1, loadedData.getTo().size());
        assertEquals("Wrong recipient.", recipient.get(0).get(0), loadedData.getTo().get(0).get(0));
        assertEquals("Wrong recipient.", recipient.get(0).get(1), loadedData.getTo().get(0).get(1));
    }

    @Test
    public void testCreateMultipleCompositionSpaces() throws Exception {
        List<String> ids = new ArrayList<String>(5);
        for (int i = 0; i < 5; i++) {
            String id = createNewCompositionSpace().getId();
            ids.add(id);
        }

        MailComposeGetResponse response = api.getMailCompose(null);
        check(response);
        assertEquals("Wrong amount of open composition spaces.", ids.size(), response.getData().size());
        response.getData().stream().map(MailComposeResponseMessageModel::getId).forEach(id -> assertTrue("Wrong id.", ids.contains(id)));
    }

}
