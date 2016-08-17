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

package com.openexchange.ajax.onboarding.tests;

import java.util.List;
import org.json.JSONObject;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.onboarding.actions.ExecuteRequest;
import com.openexchange.ajax.onboarding.actions.OnboardingTestResponse;
import com.openexchange.ajax.onboarding.actions.StartSMTPRequest;
import com.openexchange.ajax.onboarding.actions.StopSMTPRequest;
import com.openexchange.ajax.smtptest.actions.GetMailsRequest;
import com.openexchange.ajax.smtptest.actions.GetMailsResponse;
import com.openexchange.ajax.smtptest.actions.GetMailsResponse.Message;


/**
 * {@link MailSyncProfileTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
public class MailSyncProfileTest extends AbstractAJAXSession {

    public MailSyncProfileTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        StartSMTPRequest req = new StartSMTPRequest(true);
        req.setUpdateNoReplyForContext(client.getValues().getContextId());
        client.execute(req);
    }

    @Override
    public void tearDown() throws Exception {
        if (null != client) {
            client.execute(new StopSMTPRequest());
        }
        super.tearDown();
    }

    public void testIMAPSyncProfileViaEmail() throws Exception {
        JSONObject body = new JSONObject();
        body.put("email", client.getValues().getDefaultAddress());
        ExecuteRequest req = new ExecuteRequest("apple.mac/mailsync", "email", body, false);
        client.execute(req);
        GetMailsRequest mailReq = new GetMailsRequest();
        GetMailsResponse mailResp = client.execute(mailReq);
        List<Message> messages = mailResp.getMessages();
        assertNotNull(messages);
        assertEquals(1, messages.size());
    }

    public void testIMAPSyncProfileViaDisplay() throws Exception {
        ExecuteRequest req = new ExecuteRequest("apple.mac/mailmanual", "display", null, false);
        OnboardingTestResponse resp = client.execute(req);
        assertFalse(resp.hasError());
        JSONObject json = (JSONObject) resp.getData();
        assertTrue(json.hasAndNotNull("imapLogin"));
        assertTrue(json.hasAndNotNull("imapServer"));
        assertTrue(json.hasAndNotNull("imapPort"));
        assertTrue(json.hasAndNotNull("imapSecure"));
        assertTrue(json.hasAndNotNull("smtpLogin"));
        assertTrue(json.hasAndNotNull("smtpServer"));
        assertTrue(json.hasAndNotNull("smtpPort"));
        assertTrue(json.hasAndNotNull("smtpSecure"));
    }

}
