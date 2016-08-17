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
 *     Copyright (C) 2016-2016 OX Software GmbH
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

package com.openexchange.ajax.mailaccount;

import java.util.UUID;
import com.openexchange.ajax.mailaccount.actions.MailAccountDeleteRequest;
import com.openexchange.ajax.mailaccount.actions.MailAccountGetRequest;
import com.openexchange.ajax.mailaccount.actions.MailAccountGetResponse;
import com.openexchange.ajax.mailaccount.actions.MailAccountInsertRequest;
import com.openexchange.ajax.mailaccount.actions.MailAccountInsertResponse;
import com.openexchange.mailaccount.MailAccountDescription;

/**
 * {@link MailAccountStartTlsTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.2
 */
public class MailAccountStartTlsTest extends AbstractMailAccountTest {

    private MailAccountDescription mailAccount;

    public MailAccountStartTlsTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mailAccount = null;
    }

    @Override
    public void tearDown() throws Exception {
        if (null != mailAccount) {
            MailAccountDeleteRequest req = new MailAccountDeleteRequest(mailAccount.getId());
            client.execute(req);
        }
        super.tearDown();
    }

    public void testCreateMailAccountWithStartTls() throws Exception {
        MailAccountDescription acc = createMailAccountObject();
        acc.setName(UUID.randomUUID().toString());
        acc.setMailStartTls(true);
        acc.setTransportStartTls(true);
        MailAccountInsertRequest req = new MailAccountInsertRequest(acc, false);
        MailAccountInsertResponse resp = client.execute(req);
        assertFalse(resp.getErrorMessage(), resp.hasError());
        resp.fillObject(acc);

        MailAccountGetRequest getReq = new MailAccountGetRequest(acc.getId());
        MailAccountGetResponse getResp = client.execute(getReq);
        assertFalse(getResp.getErrorMessage(), getResp.hasError());
        mailAccount = getResp.getAsDescription();
        assertTrue(mailAccount.isMailStartTls());
        assertTrue(mailAccount.isTransportStartTls());
    }

    //    public void testUpdateMailAccountWithStartTls() throws Exception {
    //        MailAccountDescription acc = createMailAccountObject();
    //        acc.setName(UUID.randomUUID().toString());
    //        MailAccountInsertRequest req = new MailAccountInsertRequest(acc, false);
    //        MailAccountInsertResponse resp = client.execute(req);
    //        assertFalse(resp.getErrorMessage(), resp.hasError());
    //        resp.fillObject(acc);
    //        
    //        MailAccountGetRequest getReq = new MailAccountGetRequest(acc.getId());
    //        MailAccountGetResponse getResp = client.execute(getReq);
    //        assertFalse(getResp.getErrorMessage(), getResp.hasError());
    //        mailAccount = getResp.getAsDescription();
    //        assertFalse(mailAccount.isMailStartTls());
    //        assertFalse(mailAccount.isTransportStartTls());
    //        mailAccount.setMailStartTls(true);
    //        mailAccount.setTransportStartTls(true);
    //        MailAccountUpdateRequest updateReq = new MailAccountUpdateRequest(mailAccount);
    //        MailAccountUpdateResponse updateResp = client.execute(updateReq);
    //        assertFalse(updateResp.getErrorMessage(), updateResp.hasError());
    //        
    //        getReq = new MailAccountGetRequest(mailAccount.getId());
    //        getResp = client.execute(getReq);
    //        assertFalse(getResp.getErrorMessage(), getResp.hasError());
    //        mailAccount = getResp.getAsDescription();
    //        assertTrue(mailAccount.isMailStartTls());
    //        assertTrue(mailAccount.isTransportStartTls());
    //    }

    public void testCreateMailAccountWithDefaults() throws Exception {
        MailAccountDescription acc = createMailAccountObject();
        acc.setName(UUID.randomUUID().toString());
        MailAccountInsertRequest req = new MailAccountInsertRequest(acc, false);
        MailAccountInsertResponse resp = client.execute(req);
        assertFalse(resp.getErrorMessage(), resp.hasError());
        resp.fillObject(acc);

        MailAccountGetRequest getReq = new MailAccountGetRequest(acc.getId());
        MailAccountGetResponse getResp = client.execute(getReq);
        assertFalse(getResp.getErrorMessage(), getResp.hasError());
        mailAccount = getResp.getAsDescription();
        assertFalse(mailAccount.isMailStartTls());
        assertFalse(mailAccount.isTransportStartTls());
    }

}
