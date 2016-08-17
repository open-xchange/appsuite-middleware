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

import java.io.IOException;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.mail.actions.DeleteRequest;
import com.openexchange.ajax.mail.actions.ForwardRequest;
import com.openexchange.ajax.mail.actions.GetRequest;
import com.openexchange.ajax.mail.actions.GetResponse;
import com.openexchange.ajax.mail.actions.MailReferenceResponse;
import com.openexchange.ajax.mail.actions.NewMailRequest;
import com.openexchange.ajax.mail.actions.NewMailRequestWithUploads;
import com.openexchange.ajax.mail.actions.NewMailResponse;
import com.openexchange.ajax.mail.actions.ReplyRequest;
import com.openexchange.ajax.mail.actions.ReplyResponse;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.dataobjects.compose.ComposeType;

/**
 * {@link Bug37247Test}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 * @since v7.8.0
 */
public class Bug37247Test extends AbstractMailTest {
    private static final String EML =
        "Return-Path: <from@example.tld>\n" +
            "        Received: from local-out (mwinf8514 [127.0.0.1])\n" +
            "         by mwinb8903 (Cyrus v2.3.13) with LMTPA;\n" +
            "         Tue, 01 Jul 2014 17:38:28 +0200\n" +
            "    X-Sieve: CMU Sieve 2.3\n" +
            "    Received: from local ([127.0.0.1])\n" +
            "        by mwinf8514-out with ME\n" +
            "        id M3dU1o00G3djdXu033dUxU; Tue, 01 Jul 2014 17:37:28 +0200\n" +
            "    X-ME-User-Auth: from foo <from@example.tld>\n" +
            "    X-bcc: bcc bar <bcc@example.tld>\n" +
            "    X-me-spamrating: 36.00\n" +
            "    X-me-spamlevel: not-spam\n" +
            "    X-ME-bounce-domain: bar.tld\n" +
            "    X-ME-Entity: oop\n" +
            "    Date: Tue, 1 Jul 2014 17:37:28 +0200 (CEST)\n" +
            "    From: from foo <from@example.tld>\n" +
            "    Reply-To: rto foo <bar@example.tld>\n" +
            "    To: to bar <to@example.tld>\n" +
            "    Cc: cc bar <cc@example.tld>\n" +
            "    Bcc: bcc bar <bcc@example.tld>\n" +
            "    Message-ID: <11140339.1095068.1404229048881.JavaMail.www@wwinf8905>\n" +
            "    Subject: plain text\n" +
            "    MIME-Version: 1.0\n" +
            "    Content-Type: text/plain; charset=UTF-8\n" +
            "    Content-Transfer-Encoding: 7bit\n" +
            "    X-SAVECOPY: false\n" +
            "    X-Wum-Nature: EMAIL-NATURE\n" +
            "    X-WUM-FROM: |~|\n" +
            "    X-WUM-TO: |~|\n" +
            "    X-WUM-REPLYTO: |~|\n" +
            "\n" +
            "    plain text\n" +
            "\n" +
            "    A test signature";

    private UserValues values;

    /**
     * Initializes a new {@link Bug37247Test}.
     * @param name
     */
    public Bug37247Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        values = getClient().getValues();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testBug37472Reply() throws OXException, IOException, JSONException {
        String csid = "845.1436870434189";
        NewMailRequest newMailRequest = new NewMailRequest(values.getInboxFolder(), EML, 32, true);
        NewMailResponse newMailResponse = getClient().execute(newMailRequest);
        assertNotNull("Got an empty new mail response", newMailResponse);
        String mailId = newMailResponse.getId();
        String folderId = newMailResponse.getFolder();

        try {
            ReplyRequest rReq = new ReplyRequest(folderId, mailId);
            rReq.setCsid(csid);
            ReplyResponse rResp = getClient().execute(rReq);
            assertNotNull("Got an empty reply response", rResp);
            JSONObject jsonObj = (JSONObject) rResp.getData();
            assertNotNull("Got an unexpected response", jsonObj);
            assertFalse("Got an empty json object", jsonObj.isEmpty());
            csid  = jsonObj.getString("csid");

            String subject = "Bug37472Test_testFlagsNotChangingWhenSavingDraft" + System.currentTimeMillis();
            JSONObject composedMail = createEMail(getSendAddress(), subject, "text/plain", EML);
            JSONObject mail = new JSONObject(composedMail);
            mail.put("flags", 4);
            mail.put("csid", csid);
            mail.put("initial", true);
            mail.put("sendtype", ComposeType.DRAFT_EDIT.getType());

            NewMailRequestWithUploads sendDraftRequest = new NewMailRequestWithUploads(mail);
            MailReferenceResponse sendDraftResponse = client.execute(sendDraftRequest);
            assertNotNull(sendDraftResponse);

            GetRequest gReq = new GetRequest(folderId, mailId);
            GetResponse gResp = getClient().execute(gReq);
            assertNotNull("Got an empty get mail response", gResp);
            assertFalse("Got an empty json object", jsonObj.isEmpty());
            jsonObj = (JSONObject) gResp.getData();
            int flags = jsonObj.getInt("flags");
            Set<MailFlag> transform = MailFlag.transform(flags);
            assertFalse("When saving replyed mail as a draft mail contains answered flag", transform.contains(MailFlag.ANSWERED));
        } finally {
            if(false == (Strings.isEmpty(folderId) && Strings.isEmpty(mailId))) {
                DeleteRequest dReq = new DeleteRequest(folderId, mailId, true);
                getClient().execute(dReq);
            }
        }
    }

    public void testBug37472Forward() throws OXException, IOException, JSONException {
        String csid = "845.1436870434189";
        NewMailRequest newMailRequest = new NewMailRequest(values.getInboxFolder(), EML, 32, true);
        NewMailResponse newMailResponse = getClient().execute(newMailRequest);
        assertNotNull("Got an empty new mail response", newMailResponse);
        String mailId = newMailResponse.getId();
        String folderId = newMailResponse.getFolder();

        try {
            ForwardRequest rReq = new ForwardRequest(folderId, mailId);
            rReq.setCsid(csid);
            ReplyResponse rResp = getClient().execute(rReq);
            assertNotNull("Got an empty reply response", rResp);
            JSONObject jsonObj = (JSONObject) rResp.getData();
            assertNotNull("Got an unexpected response", jsonObj);
            assertFalse("Got an empty json object", jsonObj.isEmpty());
            csid  = jsonObj.getString("csid");

            String subject = "Bug37472Test_testFlagsNotChangingWhenSavingDraft" + System.currentTimeMillis();
            JSONObject composedMail = createEMail(getSendAddress(), subject, "text/plain", EML);
            JSONObject mail = new JSONObject(composedMail);
            mail.put("flags", 4);
            mail.put("csid", csid);
            mail.put("initial", true);
            mail.put("sendtype", ComposeType.DRAFT_EDIT.getType());

            NewMailRequestWithUploads sendDraftRequest = new NewMailRequestWithUploads(mail);
            MailReferenceResponse sendDraftResponse = client.execute(sendDraftRequest);
            assertNotNull(sendDraftResponse);

            GetRequest gReq = new GetRequest(folderId, mailId);
            GetResponse gResp = getClient().execute(gReq);
            assertNotNull("Got an empty get mail response", gResp);
            assertFalse("Got an empty json object", jsonObj.isEmpty());
            jsonObj = (JSONObject) gResp.getData();
            int flags = jsonObj.getInt("flags");
            Set<MailFlag> transform = MailFlag.transform(flags);
            assertFalse("When saving replyed mail as a draft mail contains answered flag", transform.contains(MailFlag.FORWARDED));
        } finally {
            if(false == (Strings.isEmpty(folderId) && Strings.isEmpty(mailId))) {
                DeleteRequest dReq = new DeleteRequest(folderId, mailId, true);
                getClient().execute(dReq);
            }
        }
    }
}
