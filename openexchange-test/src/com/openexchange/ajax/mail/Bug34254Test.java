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

package com.openexchange.ajax.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.mail.actions.AutosaveRequest;
import com.openexchange.ajax.mail.actions.GetRequest;
import com.openexchange.ajax.mail.actions.GetRequest.View;
import com.openexchange.ajax.mail.actions.GetResponse;
import com.openexchange.ajax.mail.actions.MailReferenceResponse;
import com.openexchange.ajax.mail.actions.NewMailRequestWithUploads;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.compose.ComposeType;

/**
 * {@link Bug34254Test}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class Bug34254Test extends AbstractMailTest {

    public Bug34254Test() {
        super();
    }

    @Test
    public void testDeleteNormalDraft() throws Exception {
        String subject = "Bug34254Test_testDeleteNormalDraft_" + System.currentTimeMillis();
        JSONObject composedMail = createEMail(getSendAddress(), subject, "text/html", MAIL_TEXT_BODY);
        JSONObject mail = new JSONObject(composedMail);
        mail.put("flags", MailMessage.FLAG_DRAFT);
        NewMailRequestWithUploads newDraftRequest = new NewMailRequestWithUploads(mail);
        MailReferenceResponse newDraftResponse = getClient().execute(newDraftRequest);
        String draftReference = newDraftResponse.getMailReference();
        String draftFolder = newDraftResponse.getFolder();
        String draftMailID = newDraftResponse.getMailID();
        assertEquals("Draft saved in wrong folder", getClient().getValues().getDraftsFolder(), draftFolder);

        GetRequest getRequest = new GetRequest(draftFolder, draftMailID, View.HTML);
        GetResponse getResponse = getClient().execute(getRequest);
        MailMessage reloaded = getResponse.getMail(getTimeZone());
        assertEquals("Wrong mail reference", subject, reloaded.getSubject());

        mail = new JSONObject(composedMail);
        mail.put("msgref", draftReference);
        mail.put("sendtype", ComposeType.DRAFT_DELETE_ON_TRANSPORT.getType());
        NewMailRequestWithUploads sendDraftRequest = new NewMailRequestWithUploads(mail);
        MailReferenceResponse sendDraftResponse = getClient().execute(sendDraftRequest);
        assertEquals("Mail not stored in sent folder", getClient().getValues().getSentFolder(), sendDraftResponse.getFolder());

        getRequest = new GetRequest(draftFolder, draftMailID, false);
        getResponse = getClient().execute(getRequest);
        assertNull("Draft was not deleted", getResponse.getData());
    }

    @Test
    public void testDeleteAutoDraft() throws Exception {
        String subject = "Bug34254Test_testDeleteAutoDraft_" + System.currentTimeMillis();
        JSONObject composedMail = createEMail(getSendAddress(), subject, "text/html", MAIL_TEXT_BODY);
        JSONObject mail = new JSONObject(composedMail);
        AutosaveRequest autosaveRequest = new AutosaveRequest(mail);
        MailReferenceResponse autosaveResponse = getClient().execute(autosaveRequest);
        String draftReference = autosaveResponse.getMailReference();
        String draftFolder = autosaveResponse.getFolder();
        String draftMailID = autosaveResponse.getMailID();
        assertEquals("Draft saved in wrong folder", getClient().getValues().getDraftsFolder(), draftFolder);

        GetRequest getRequest = new GetRequest(draftFolder, draftMailID, View.HTML);
        GetResponse getResponse = getClient().execute(getRequest);
        MailMessage reloaded = getResponse.getMail(getTimeZone());
        assertEquals("Wrong mail reference", subject, reloaded.getSubject());

        mail = new JSONObject(composedMail);
        mail.put("msgref", draftReference);
        mail.put("sendtype", ComposeType.DRAFT_DELETE_ON_TRANSPORT.getType());
        NewMailRequestWithUploads sendDraftRequest = new NewMailRequestWithUploads(mail);
        MailReferenceResponse sendDraftResponse = getClient().execute(sendDraftRequest);
        assertEquals("Mail not stored in sent folder", getClient().getValues().getSentFolder(), sendDraftResponse.getFolder());

        getRequest = new GetRequest(draftFolder, draftMailID, false);
        getResponse = getClient().execute(getRequest);
        assertNull("Draft was not deleted", getResponse.getData());
    }

}
