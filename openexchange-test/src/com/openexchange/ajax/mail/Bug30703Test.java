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

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.ListResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.mailaccount.actions.MailAccountDeleteRequest;
import com.openexchange.ajax.mailaccount.actions.MailAccountInsertRequest;
import com.openexchange.ajax.mailaccount.actions.MailAccountInsertResponse;
import com.openexchange.ajax.pop3.actions.StartPOP3ServerRequest;
import com.openexchange.ajax.pop3.actions.StartPOP3ServerResponse;
import com.openexchange.ajax.pop3.actions.StopPOP3ServerRequest;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.mail.mime.MimeMailExceptionCode;
import com.openexchange.mailaccount.MailAccountDescription;

/**
 * {@link Bug30703Test}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class Bug30703Test extends AbstractAJAXSession {

    private MailAccountDescription mailAccountDescription;

    public Bug30703Test(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            StopPOP3ServerRequest stopReq = new StopPOP3ServerRequest();
            client.execute(stopReq);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (mailAccountDescription != null) {
                client.execute(new MailAccountDeleteRequest(mailAccountDescription.getId()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.tearDown();
    }

    public void testProtocolError() throws Exception {
        setupServerAndAccount(true, false);
        ListRequest listRequest = new ListRequest(EnumAPI.OX_NEW, "default" + mailAccountDescription.getId());
        ListResponse listResponse = client.execute(listRequest);
        assertException(ResponseWriter.getJSON(listResponse.getResponse()), MimeMailExceptionCode.CONNECT_ERROR);
    }

    public void testWrongCredentials() throws Exception {
        setupServerAndAccount(false, true);
        ListRequest listRequest = new ListRequest(EnumAPI.OX_NEW, "default" + mailAccountDescription.getId());
        ListResponse listResponse = client.execute(listRequest);
        assertException(ResponseWriter.getJSON(listResponse.getResponse()), MimeMailExceptionCode.INVALID_CREDENTIALS_EXT);
    }

    public void testServerOffline() throws Exception {
        setupAccount("localhost", 1234);
        ListRequest listRequest = new ListRequest(EnumAPI.OX_NEW, "default" + mailAccountDescription.getId());
        ListResponse listResponse = client.execute(listRequest);
        assertException(ResponseWriter.getJSON(listResponse.getResponse()), MimeMailExceptionCode.CONNECT_ERROR);
    }

    private void assertException(JSONObject response, MimeMailExceptionCode exceptionCode) throws JSONException {
        String prefix = exceptionCode.getPrefix();
        int number = exceptionCode.getNumber();
        assertTrue("No error in response object", response.has("error"));
        String code = response.getString("code");
        String[] split = code.split("-");
        assertEquals("Wrong prefix", prefix, split[0]);
        assertEquals("Wrong number", number, Integer.parseInt(split[1]));
    }

    private void setupServerAndAccount(boolean failOnConnect, boolean failOnAuth) throws Exception {
        StartPOP3ServerRequest startReq = new StartPOP3ServerRequest(failOnConnect, failOnAuth);
        StartPOP3ServerResponse startResp = client.execute(startReq);
        String host = startResp.getHost();
        int port = startResp.getPort();
        setupAccount(host, port);
    }

    private void setupAccount(String host, int port) throws Exception {
        String user = "Bug30703_User" + Long.toString(System.currentTimeMillis());
        mailAccountDescription = new MailAccountDescription();
        mailAccountDescription.setName("Bug30703Test_Account");
        mailAccountDescription.setPrimaryAddress(user + "@test.invalid");
        mailAccountDescription.parseMailServerURL("pop3://" + host + ":" + port);
        mailAccountDescription.setLogin(user);
        mailAccountDescription.setPassword("secret");
        mailAccountDescription.parseTransportServerURL("smtp://" + host + ":" + port);
        mailAccountDescription.setTransportLogin(user);
        mailAccountDescription.setTransportPassword("secret");
        mailAccountDescription.setConfirmedSpam("confirmedSpam");
        mailAccountDescription.setConfirmedHam("confirmedHam");
        mailAccountDescription.setDrafts("Drafts");
        mailAccountDescription.setSent("Sent");
        mailAccountDescription.setTrash("Trash");
        mailAccountDescription.setSpam("Spam");
        mailAccountDescription.setSpamHandler("NoSpamHandler");
        MailAccountInsertResponse response = client.execute(new MailAccountInsertRequest(mailAccountDescription));
        assertFalse("Warning during account creation: " + response.getResponse().getWarnings().toString(), response.hasWarnings());
        response.fillObject(mailAccountDescription);
    }

}
