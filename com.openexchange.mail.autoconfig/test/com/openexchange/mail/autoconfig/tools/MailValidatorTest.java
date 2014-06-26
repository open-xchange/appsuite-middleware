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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.mail.autoconfig.tools;

import junit.framework.TestCase;

/**
 * {@link MailValidatorTest}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class MailValidatorTest extends TestCase {

    private static final String HOST_POP3 = "pop.gmx.net";
    private static final String HOST_IMAP = "imap.gmx.net";
    private static final String HOST_SMTP = "smtp.gmx.net";

    private static final String USER = "sdfdf@gmx.net";

    private static final String PASSWORD = "blubb";

    public void testPOP3() throws Exception {
        assertTrue("Non secure POP3 Connection failed", MailValidator.validatePop3(HOST_POP3, 110, false, USER, PASSWORD));
        assertTrue("Secure POP3 Connection failed", MailValidator.validatePop3(HOST_POP3, 995, true, USER, PASSWORD));
        assertTrue("Host check failed.", MailValidator.checkForPop3(HOST_POP3, 110, false));
        assertTrue("Host check failed.", MailValidator.checkForPop3(HOST_POP3, 995, true));
    }

    public void testIMAP() throws Exception {
        assertTrue("Non secure IMAP Connection failed", MailValidator.validateImap(HOST_IMAP, 143, false, USER, PASSWORD));
        assertTrue("Secure IMAP Connection failed", MailValidator.validateImap(HOST_IMAP, 993, true, USER, PASSWORD));
        assertTrue("Host check failed.", MailValidator.checkForImap(HOST_IMAP, 143, false));
        assertTrue("Host check failed.", MailValidator.checkForImap(HOST_IMAP, 993, true));
    }

    public void testSMTP() throws Exception {
        assertTrue("Non secure SMTP Connection failed", MailValidator.validateSmtp(HOST_SMTP, 25, false, USER, PASSWORD));
        assertTrue("Secure SMTP Connection failed", MailValidator.validateSmtp(HOST_SMTP, 465, true, USER, PASSWORD));
        //assertTrue("Secure SMTP Connection failed", MailValidator.validateSmtp(HOST_SMTP, 587, true, USER, PASSWORD));
        assertTrue("Host check failed.", MailValidator.checkForSmtp(HOST_SMTP, 25, false));
        assertTrue("Host check failed.", MailValidator.checkForSmtp(HOST_SMTP, 465, true));
    }
}
