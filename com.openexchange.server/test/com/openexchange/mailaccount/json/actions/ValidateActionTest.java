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

package com.openexchange.mailaccount.json.actions;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link ValidateActionTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class ValidateActionTest {

    @Mock
    private AJAXRequestData requestData;

    @Mock
    private ServerSession session;

    private JSONValue jData;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        jData = new JSONObject("{\"unified_inbox_enabled\":true,\"transport_auth\":\"MAIL\",\"id\":0,\"login\":\"martin.schneider424242669\",\"password\":null,\"mail_url\":\"imap://mail.devel.open-xchange.com:143\",\"transport_url\":\"smtp://mail.devel.open-xchange.com:25\",\"name\":\"martin.schneider@premium\",\"primary_address\":\"martin.schneider@premium\",\"spam_handler\":\"NoSpamHandler\",\"trash\":\"Papierkorb\",\"sent\":\"Gesendete Objekte\",\"drafts\":\"Entwuerfe\",\"spam\":\"Spam\",\"confirmed_spam\":\"confirmed-spam\",\"confirmed_ham\":\"confirmed-ham\",\"mail_server\":\"mail.devel.open-xchange.com\",\"mail_port\":143,\"mail_protocol\":\"imap\",\"mail_secure\":false,\"transport_server\":\"mail.devel.open-xchange.com\",\"transport_port\":25,\"transport_protocol\":\"smtp\",\"transport_secure\":false,\"transport_login\":\"\",\"transport_password\":\"\",\"trash_fullname\":\"default0/INBOX/Papierkorb\",\"sent_fullname\":\"default0/INBOX/Gesendete Objekte\",\"drafts_fullname\":\"default0/INBOX/Entwuerfe\",\"spam_fullname\":\"default0/INBOX/Spam\",\"confirmed_spam_fullname\":\"default0/INBOX/confirmed-spam\",\"confirmed_ham_fullname\":\"default0/INBOX/confirmed-ham\",\"pop3_refresh_rate\":null,\"pop3_expunge_on_quit\":false,\"pop3_delete_write_through\":false,\"pop3_storage \":null,\"pop3_path\":\"INBOX/EMail\",\"personal\":\"martin.schneider@premium\",\"reply_to\":null,\"addresses\":\"martin.schneider@premium\",\"meta\":null,\"archive\":null,\"archive_fullname\":\"default0/INBOX/Archive\",\"accountType\":\"mail\",\"displayName\":\"E-Mail\"}");

        UserPermissionBits userPermissionBits = Mockito.mock(UserPermissionBits.class);
        Mockito.when(userPermissionBits.isMultipleMailAccounts()).thenReturn(true);
        Mockito.when(session.getUserPermissionBits()).thenReturn(userPermissionBits);
    }

    @Test
    public void testInnerPerform_mailAccountIsPrimaryAccount_doNotValidateAndReturnTrue() throws OXException, JSONException {
        ValidateAction action = new ValidateAction();
        AJAXRequestResult innerPerform = action.innerPerform(requestData, session, jData);
        Object resultObject = innerPerform.getResultObject();

        if (!(resultObject instanceof Boolean)) {
            Assert.assertTrue("Wrong result object type returned from action. Received: " + resultObject.getClass(), false);
        } else {
            Boolean result = (Boolean) resultObject;
            Assert.assertTrue("Validation did not return true for primary mail account!", result.booleanValue());
        }
    }
}
