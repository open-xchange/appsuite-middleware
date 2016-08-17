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

package com.openexchange.admin.soap.user.soap;

import junit.framework.TestCase;
import com.openexchange.admin.soap.user.dataobjects.User;


/**
 * {@link OXUserServicePortTypeImplTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class OXUserServicePortTypeImplTest extends TestCase {

    /**
     * Initializes a new {@link OXUserServicePortTypeImplTest}.
     */
    public OXUserServicePortTypeImplTest() {
        super();
    }

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

    public void testSetImapPortIPv6() {
        User soapUser = new User();
        soapUser.setImapServer("fd9e:21a7:a92c:2323::1");
        soapUser.setImapPort(Integer.valueOf(143));

        final com.openexchange.admin.rmi.dataobjects.User user = com.openexchange.admin.soap.user.soap.OXUserServicePortTypeImpl.soap2User(soapUser);

        final String imapServerString = user.getImapServerString();
        assertEquals("Unexpected IMAP server string.", "imap://[fd9e:21a7:a92c:2323::1]:143", imapServerString);
    }

}
