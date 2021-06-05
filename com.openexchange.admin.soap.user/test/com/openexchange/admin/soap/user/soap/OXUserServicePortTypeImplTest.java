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

package com.openexchange.admin.soap.user.soap;

import org.junit.Test;
import com.openexchange.admin.soap.user.dataobjects.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 * {@link OXUserServicePortTypeImplTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class OXUserServicePortTypeImplTest {
    /**
     * Initializes a new {@link OXUserServicePortTypeImplTest}.
     */
    public OXUserServicePortTypeImplTest() {
        super();
    }

         @Test
     public void testSetImapPort() {
        User soapUser = new User();
        soapUser.setImapServer("imap.invalid.com:993");
        soapUser.setImapPort(Integer.valueOf(143));

        soapUser.setSmtpServerString("smtp://smtp.invalid.com:25");
        soapUser.setSmtpPort(Integer.valueOf(587));

        final com.openexchange.admin.rmi.dataobjects.User user = com.openexchange.admin.soap.user.soap.OXUserServicePortTypeImpl.soap2User(soapUser);

        final String imapServerString = user.getImapServerString();
        assertEquals("Unexpected IMAP server string.", "imap://imap.invalid.com:143", imapServerString);

        final String smtpServerString = user.getSmtpServerString();
        assertEquals("Unexpected SMTP server string.", "smtp://smtp.invalid.com:587", smtpServerString);
    }

         @Test
     public void testSetImapPortIPv6() {
        User soapUser = new User();
        soapUser.setImapServer("fd9e:21a7:a92c:2323::1");
        soapUser.setImapPort(Integer.valueOf(143));

        final com.openexchange.admin.rmi.dataobjects.User user = com.openexchange.admin.soap.user.soap.OXUserServicePortTypeImpl.soap2User(soapUser);

        final String imapServerString = user.getImapServerString();
        assertEquals("Unexpected IMAP server string.", "imap://[fd9e:21a7:a92c:2323::1]:143", imapServerString);
    }

}
