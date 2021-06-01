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

import static com.openexchange.java.Autoboxing.I;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import java.util.Collections;
import org.junit.Test;
import com.openexchange.testing.httpclient.models.Attachment;
import com.openexchange.testing.httpclient.models.ComposeBody;
import com.openexchange.testing.httpclient.models.MailComposeResponse;
import com.openexchange.testing.httpclient.models.MailComposeResponseMessageModel;
import com.openexchange.testing.httpclient.models.MailDestinationData;

/**
 * {@link Bug67701Test}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class Bug67701Test extends AbstractMailComposeTest {


    @Test
    public void testAttachmentForwarded() throws Exception {
        MailDestinationData mailWithAttachment = importTestMail("bug67701.eml");

        ComposeBody body = new ComposeBody();
        body.setFolderId(mailWithAttachment.getFolderId());
        body.setId(mailWithAttachment.getId());
        MailComposeResponse reply = api.postMailCompose("FORWARD", null, null, Collections.singletonList(body));
        check(reply);
        MailComposeResponseMessageModel data = reply.getData();
        compositionSpaceIds.add(data.getId());

        MailComposeResponse response = api.getMailComposeById(data.getId());
        check(response);
        assertThat(I(response.getData().getAttachments().size()), is(I(1)));
        Attachment attach = response.getData().getAttachments().get(0);
        assertThat(attach.getMimeType(), is("application/pdf"));
    }


}
