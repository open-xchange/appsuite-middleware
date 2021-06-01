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

import static com.openexchange.testing.httpclient.models.MailComposeRequestMessageModel.ContentTypeEnum.PLAIN;
import static org.junit.Assert.assertEquals;
import java.util.Collections;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import com.openexchange.testing.httpclient.models.MailComposeAttachmentResponse;
import com.openexchange.testing.httpclient.models.MailComposeRequestMessageModel;
import com.openexchange.testing.httpclient.models.MailComposeResponse;

/**
 * {@link ConcurrentEditTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.5
 */
public class ConcurrentEditTest extends AbstractMailComposeTest {

    /**
     * 1. Client 1 opens CS with client token
     * 2. Client 1 PATCHes with token in request => success
     * 3. Client 2 PATCHes with an own token in request => failure
     */
    @Test
    public void testFailWithWrongToken_Patch() throws Exception {
        String clientToken1 = RandomStringUtils.randomAlphanumeric(16);
        String clientToken2 = RandomStringUtils.randomAlphanumeric(16);
        MailComposeResponse openResponse = api.postMailCompose("new", Boolean.FALSE, clientToken1, Collections.emptyList());
        check(openResponse);

        MailComposeRequestMessageModel patch1 = new MailComposeRequestMessageModel();
        patch1.setContentType(PLAIN);
        patch1.setContent("patch1");
        MailComposeResponse patchResponse1 = api.patchMailComposeById(openResponse.getData().getId(), patch1, clientToken1);
        check(patchResponse1);

        // patch again with different token
        MailComposeRequestMessageModel patch2 = new MailComposeRequestMessageModel();
        patch1.setContent("patch2");
        MailComposeResponse patchResponse2 = api.patchMailComposeById(openResponse.getData().getId(), patch2, clientToken2);
        assertEquals("MSGCS-0010", patchResponse2.getCode());
    }

    @Test
    public void testFailWithWrongToken_PostAttachment() throws Exception {
        String clientToken1 = RandomStringUtils.randomAlphanumeric(16);
        String clientToken2 = RandomStringUtils.randomAlphanumeric(16);
        MailComposeResponse openResponse = api.postMailCompose("new", Boolean.FALSE, clientToken1, Collections.emptyList());
        check(openResponse);

        MailComposeAttachmentResponse attachmentResponseForCorrectToken = api.postAttachments(openResponse.getData().getId(), attachment, clientToken1);
        check(attachmentResponseForCorrectToken);

        MailComposeAttachmentResponse attachmentResponseForNoToken = api.postAttachments(openResponse.getData().getId(), attachment, null);
        check(attachmentResponseForNoToken);

        MailComposeAttachmentResponse attachmentResponseForWrongToken = api.postAttachments(openResponse.getData().getId(), attachment, clientToken2);
        assertEquals("MSGCS-0010", attachmentResponseForWrongToken.getCode());
    }

    /**
     * 1. Client 1 opens a new CS without setting a token
     * 2. On first PATCH, Client 1 sets a token
     * 3. Second patch with token 1 to verify basic functioning
     * 4. Client 2 GETs the open space
     * 5. Client 2 takes over the compose by setting another token
     * 6. Client 2 PATCHes to verify functioning
     * 7. Client 1 PATCHes but fails due to wrong token
     */
    @Test
    public void testTokenHandover() throws Exception {
        String clientToken1 = RandomStringUtils.randomAlphanumeric(16);
        String clientToken2 = RandomStringUtils.randomAlphanumeric(16);
        MailComposeResponse openResponse = api.postMailCompose("new", Boolean.FALSE, null, Collections.emptyList());
        check(openResponse);
        MailComposeRequestMessageModel patch1 = new MailComposeRequestMessageModel();
        patch1.setClaim(clientToken1);
        patch1.setContentType(PLAIN);
        patch1.setContent("patch1");
        MailComposeResponse patchResponse1 = api.patchMailComposeById(openResponse.getData().getId(), patch1, null);
        check(patchResponse1);

        // patch again with token check
        MailComposeRequestMessageModel patch2 = new MailComposeRequestMessageModel();
        patch2.setContent("patch2");
        MailComposeResponse patchResponse2 = api.patchMailComposeById(openResponse.getData().getId(), patch2, clientToken1);
        check(patchResponse2);

        // get and patch as client 2 to take over editing
        MailComposeResponse getResponse = api.getMailComposeById(openResponse.getData().getId());
        check(getResponse);

        MailComposeRequestMessageModel tokenPatch = new MailComposeRequestMessageModel();
        tokenPatch.setClaim(clientToken2);
        MailComposeResponse tokenPatchResponse = api.patchMailComposeById(openResponse.getData().getId(), tokenPatch, null);
        check(tokenPatchResponse);

        MailComposeRequestMessageModel patch3 = new MailComposeRequestMessageModel();
        patch3.setContent("patch3");
        MailComposeResponse patchResponse3 = api.patchMailComposeById(openResponse.getData().getId(), patch3, clientToken2);
        check(patchResponse3);

        // client 1 must fail now
        MailComposeRequestMessageModel patch4 = new MailComposeRequestMessageModel();
        patch4.setContent("patch4");
        MailComposeResponse patchResponse4 = api.patchMailComposeById(openResponse.getData().getId(), patch4, clientToken1);
        assertEquals("MSGCS-0010", patchResponse4.getCode());
    }

    /**
     * 1. Open space with token
     * 2. Patch with token check
     * 3. Patch without token check
     * 4. Check 2. patch was successful
     */
    @Test
    public void testNoTokenOverridesAlways() throws Exception {
        String clientToken1 = RandomStringUtils.randomAlphanumeric(16);
        MailComposeResponse openResponse = api.postMailCompose("new", Boolean.FALSE, clientToken1, Collections.emptyList());
        check(openResponse);
        MailComposeRequestMessageModel patch1 = new MailComposeRequestMessageModel();
        patch1.setContentType(PLAIN);
        patch1.setContent("patch1");
        MailComposeResponse patchResponse1 = api.patchMailComposeById(openResponse.getData().getId(), patch1, clientToken1);
        check(patchResponse1);

        // patch again without token => override in any case
        MailComposeRequestMessageModel patch2 = new MailComposeRequestMessageModel();
        patch2.setContent("patch2");
        MailComposeResponse patchResponse2 = api.patchMailComposeById(openResponse.getData().getId(), patch2, null);
        check(patchResponse2);

        MailComposeResponse getResponse = api.getMailComposeById(openResponse.getData().getId());
        check(getResponse);
        assertEquals("Patch without token was not applied", patch2.getContent(), getResponse.getData().getContent());
    }

    @Test
    public void testInvalidTokenSyntax_Open() throws Exception {
        String invalidToken = "123!efg$";
        MailComposeResponse openResponse = api.postMailCompose("new", Boolean.FALSE, invalidToken, Collections.emptyList());
        assertEquals("SVL-0010", openResponse.getCode());
        assertEquals("claim", openResponse.getErrorParams().get(0));
    }

    @Test
    public void testInvalidTokenSyntax_Set() throws Exception {
        String invalidToken = "123!efg$";
        MailComposeResponse openResponse = api.postMailCompose("new", Boolean.FALSE, null, Collections.emptyList());
        check(openResponse);

        MailComposeRequestMessageModel tokenPatch = new MailComposeRequestMessageModel();
        tokenPatch.setClaim(invalidToken);
        MailComposeResponse tokenPatchResponse = api.patchMailComposeById(openResponse.getData().getId(), tokenPatch, null);
        assertEquals("SVL-0010", tokenPatchResponse.getCode());
        assertEquals("claim", tokenPatchResponse.getErrorParams().get(0));
    }

    @Test
    public void testInvalidTokenSyntax_Request() throws Exception {
        String invalidToken = "123!efg$";
        MailComposeResponse openResponse = api.postMailCompose("new", Boolean.FALSE, null, Collections.emptyList());
        check(openResponse);

        MailComposeRequestMessageModel patch1 = new MailComposeRequestMessageModel();
        patch1.setContentType(PLAIN);
        patch1.setContent("patch1");

        MailComposeResponse patchResponse1 = api.patchMailComposeById(openResponse.getData().getId(), patch1, invalidToken);
        assertEquals("SVL-0010", patchResponse1.getCode());
        assertEquals("clientToken", patchResponse1.getErrorParams().get(0));
    }

}
