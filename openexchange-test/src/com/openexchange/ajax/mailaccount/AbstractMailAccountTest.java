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

package com.openexchange.ajax.mailaccount;

import java.io.IOException;
import org.json.JSONException;
import com.openexchange.ajax.framework.Abstrac2UserAJAXSession;
import com.openexchange.ajax.mailaccount.actions.MailAccountDeleteRequest;
import com.openexchange.ajax.mailaccount.actions.MailAccountInsertRequest;
import com.openexchange.ajax.mailaccount.actions.MailAccountInsertResponse;
import com.openexchange.exception.OXException;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.test.common.configuration.AJAXConfig;
import com.openexchange.test.common.test.pool.TestUser;

/**
 * {@link AbstractMailAccountTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class AbstractMailAccountTest extends Abstrac2UserAJAXSession {

    protected AbstractMailAccountTest() {
        super();
    }

    protected MailAccountDescription mailAccountDescription;

    protected static MailAccountDescription createMailAccountObject() throws OXException {
        final MailAccountDescription mailAccountDescription = new MailAccountDescription();
        mailAccountDescription.setArchive("Archive");
        mailAccountDescription.setConfirmedHam("confirmedHam");
        mailAccountDescription.setConfirmedSpam("confirmedSpam");
        mailAccountDescription.setDrafts("drafts");
        mailAccountDescription.setLogin("login");
        mailAccountDescription.parseMailServerURL("imap://mail.test.invalid");
        mailAccountDescription.setName("Test Mail Account");
        mailAccountDescription.setPassword("Password");
        mailAccountDescription.setPrimaryAddress("bob" + System.currentTimeMillis() + "@test.invalid");
        mailAccountDescription.setSent("sent");
        mailAccountDescription.setSpam("Spam");
        mailAccountDescription.setSpamHandler("spamHandler");
        mailAccountDescription.parseTransportServerURL("smtp://mail.test.invalid");
        mailAccountDescription.setTransportLogin("login");
        mailAccountDescription.setTransportPassword("Password");
        mailAccountDescription.setTrash("trash");
        return mailAccountDescription;
    }

    protected void createMailAccount() throws OXException, IOException, JSONException {
        mailAccountDescription = createMailAccountObject();

        updateMailAccountDescription(mailAccountDescription, testUser2);
        final MailAccountInsertResponse response = getClient().execute(new MailAccountInsertRequest(mailAccountDescription));
        response.fillObject(mailAccountDescription);

    }

    protected void updateMailAccountDescription(final MailAccountDescription mailAccountDescription, TestUser user) {
        mailAccountDescription.setMailServer(AJAXConfig.getProperty(AJAXConfig.Property.HOSTNAME));
        mailAccountDescription.setMailPort(Integer.parseInt(AJAXConfig.getProperty(AJAXConfig.Property.MAIL_PORT)));

        mailAccountDescription.setMailProtocol("imap");
        mailAccountDescription.setMailSecure(false);
        mailAccountDescription.setLogin(user.getLogin());
        mailAccountDescription.setPassword(user.getPassword());
        mailAccountDescription.setTransportServer(AJAXConfig.getProperty(AJAXConfig.Property.HOSTNAME));
        mailAccountDescription.setTransportPort(25);
        mailAccountDescription.setTransportProtocol("smtp");
        mailAccountDescription.setTransportLogin(user.getLogin());
        mailAccountDescription.setTransportPassword(user.getPassword());
        mailAccountDescription.setTransportSecure(false);
    }

    protected void deleteMailAccount() throws OXException, IOException, JSONException {
        getClient().execute(new MailAccountDeleteRequest(mailAccountDescription.getId()));
    }
}
