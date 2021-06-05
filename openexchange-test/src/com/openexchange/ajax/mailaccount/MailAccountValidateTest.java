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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.IOException;
import org.json.JSONException;
import org.junit.Test;
import com.openexchange.ajax.mailaccount.actions.MailAccountValidateRequest;
import com.openexchange.ajax.mailaccount.actions.MailAccountValidateResponse;
import com.openexchange.exception.OXException;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.test.common.configuration.AJAXConfig;

/**
 * {@link MailAccountValidateTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MailAccountValidateTest extends AbstractMailAccountTest {

    @Test
    public void testValidate() throws OXException, IOException, JSONException {
        final MailAccountDescription mailAccountDescription = createMailAccountObject();
        MailAccountValidateResponse response = getClient().execute(new MailAccountValidateRequest(mailAccountDescription));
        assertFalse("Invalid IP/hostname in mail account succesfully passed validation but shouldn't", response.isValidated());

        mailAccountDescription.setMailServer("imap.open-xchange.com");
        mailAccountDescription.setMailPort(143);
        response = getClient().execute(new MailAccountValidateRequest(mailAccountDescription));
        assertFalse("Invalid credentials in mail account succesfully passed validation but shouldn't", response.isValidated());

        mailAccountDescription.setMailServer("imap.googlemail.com");
        mailAccountDescription.setMailPort(993);
        mailAccountDescription.setMailProtocol("imap");
        mailAccountDescription.setMailSecure(true);
        response = getClient().execute(new MailAccountValidateRequest(mailAccountDescription));
        assertFalse("Invalid credentials in mail account succesfully passed validation but shouldn't", response.isValidated());

        /*
         * Init test environment
         */
        try {
            AJAXConfig.init();
        } catch (OXException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        mailAccountDescription.setMailServer(AJAXConfig.getProperty(AJAXConfig.Property.HOSTNAME));
        mailAccountDescription.setMailPort(Integer.parseInt(AJAXConfig.getProperty(AJAXConfig.Property.MAIL_PORT)));
        mailAccountDescription.setMailProtocol("imap");
        mailAccountDescription.setMailSecure(false);
        mailAccountDescription.setLogin(testUser.getLogin());
        mailAccountDescription.setPassword(testUser.getPassword());
        mailAccountDescription.setTransportServer((String) null);
        response = getClient().execute(new MailAccountValidateRequest(mailAccountDescription));
        assertTrue("Valid access data in mail account do not pass validation but should: " + response.getResponse().getWarnings(), response.isValidated());

        mailAccountDescription.setMailServer(AJAXConfig.getProperty(AJAXConfig.Property.HOSTNAME));
        mailAccountDescription.setMailPort(Integer.parseInt(AJAXConfig.getProperty(AJAXConfig.Property.MAIL_PORT)));
        mailAccountDescription.setMailProtocol("imap");
        mailAccountDescription.setMailSecure(false);
        mailAccountDescription.setLogin(testUser.getLogin());
        mailAccountDescription.setPassword(testUser.getPassword());
        mailAccountDescription.setTransportLogin(testUser.getLogin());
        mailAccountDescription.setTransportPassword(testUser.getPassword());
        mailAccountDescription.setTransportServer(AJAXConfig.getProperty(AJAXConfig.Property.HOSTNAME));
        mailAccountDescription.setTransportPort(25);
        mailAccountDescription.setTransportProtocol("smtp");
        mailAccountDescription.setTransportSecure(false);

        response = getClient().execute(new MailAccountValidateRequest(mailAccountDescription));
        assertTrue("Valid access data in mail/transport account do not pass validation but should: " + response.getResponse().getWarnings(), response.isValidated());
    }

}
