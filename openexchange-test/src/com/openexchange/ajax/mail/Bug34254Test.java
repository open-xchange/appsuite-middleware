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

package com.openexchange.ajax.mail;

import org.json.JSONObject;
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

    public Bug34254Test(String name) {
        super(name);
    }

    public void testDeleteNormalDraft() throws Exception {
        String subject = "Bug34254Test_testDeleteNormalDraft_" + System.currentTimeMillis();
        JSONObject composedMail = createEMail(getSendAddress(), subject, "text/html", MAIL_TEXT_BODY);
        JSONObject mail = new JSONObject(composedMail);
        mail.put("flags", MailMessage.FLAG_DRAFT);
        NewMailRequestWithUploads newDraftRequest = new NewMailRequestWithUploads(mail);
        MailReferenceResponse newDraftResponse = client.execute(newDraftRequest);
        String draftReference = newDraftResponse.getMailReference();
        String draftFolder = newDraftResponse.getFolder();
        String draftMailID = newDraftResponse.getMailID();
        assertEquals("Draft saved in wrong folder", client.getValues().getDraftsFolder(), draftFolder);

        GetRequest getRequest = new GetRequest(draftFolder, draftMailID, View.HTML);
        GetResponse getResponse = client.execute(getRequest);
        MailMessage reloaded = getResponse.getMail(getTimeZone());
        assertEquals("Wrong mail reference", subject, reloaded.getSubject());

        mail = new JSONObject(composedMail);
        mail.put("msgref", draftReference);
        mail.put("sendtype", ComposeType.DRAFT_DELETE_ON_TRANSPORT.getType());
        NewMailRequestWithUploads sendDraftRequest = new NewMailRequestWithUploads(mail);
        MailReferenceResponse sendDraftResponse = client.execute(sendDraftRequest);
        assertEquals("Mail not stored in sent folder", client.getValues().getSentFolder(), sendDraftResponse.getFolder());

        getRequest = new GetRequest(draftFolder, draftMailID, false);
        getResponse = client.execute(getRequest);
        assertNull("Draft was not deleted", getResponse.getData());
    }

    public void testDeleteAutoDraft() throws Exception {
        String subject = "Bug34254Test_testDeleteAutoDraft_" + System.currentTimeMillis();
        JSONObject composedMail = createEMail(getSendAddress(), subject, "text/html", MAIL_TEXT_BODY);
        JSONObject mail = new JSONObject(composedMail);
        AutosaveRequest autosaveRequest = new AutosaveRequest(mail);
        MailReferenceResponse autosaveResponse = client.execute(autosaveRequest);
        String draftReference = autosaveResponse.getMailReference();
        String draftFolder = autosaveResponse.getFolder();
        String draftMailID = autosaveResponse.getMailID();
        assertEquals("Draft saved in wrong folder", client.getValues().getDraftsFolder(), draftFolder);

        GetRequest getRequest = new GetRequest(draftFolder, draftMailID, View.HTML);
        GetResponse getResponse = client.execute(getRequest);
        MailMessage reloaded = getResponse.getMail(getTimeZone());
        assertEquals("Wrong mail reference", subject, reloaded.getSubject());

        mail = new JSONObject(composedMail);
        mail.put("msgref", draftReference);
        mail.put("sendtype", ComposeType.DRAFT_DELETE_ON_TRANSPORT.getType());
        NewMailRequestWithUploads sendDraftRequest = new NewMailRequestWithUploads(mail);
        MailReferenceResponse sendDraftResponse = client.execute(sendDraftRequest);
        assertEquals("Mail not stored in sent folder", client.getValues().getSentFolder(), sendDraftResponse.getFolder());

        getRequest = new GetRequest(draftFolder, draftMailID, false);
        getResponse = client.execute(getRequest);
        assertNull("Draft was not deleted", getResponse.getData());
    }

}
