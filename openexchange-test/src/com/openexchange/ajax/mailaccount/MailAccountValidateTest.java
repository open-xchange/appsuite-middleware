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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import com.openexchange.ajax.mailaccount.actions.MailAccountValidateRequest;
import com.openexchange.ajax.mailaccount.actions.MailAccountValidateResponse;
import com.openexchange.configuration.ConfigurationException;
import com.openexchange.configuration.MailConfig;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.tools.servlet.AjaxException;

/**
 * {@link MailAccountValidateTest}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MailAccountValidateTest extends AbstractMailAccountTest {

    public MailAccountValidateTest(final String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        if (null != mailAccountDescription && 0 != mailAccountDescription.getId()) {
            deleteMailAccount();
        }
        super.tearDown();
    }

    public void testValidate() throws AjaxException, IOException, SAXException, JSONException {
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
            MailConfig.init();
        } catch (final ConfigurationException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        mailAccountDescription.setMailServer(MailConfig.getProperty(MailConfig.Property.SERVER));
        mailAccountDescription.setMailPort(Integer.parseInt(MailConfig.getProperty(MailConfig.Property.PORT)));
        mailAccountDescription.setMailProtocol("imap");
        mailAccountDescription.setMailSecure(false);
        mailAccountDescription.setLogin(MailConfig.getProperty(MailConfig.Property.LOGIN));
        mailAccountDescription.setPassword(MailConfig.getProperty(MailConfig.Property.PASSWORD));
        mailAccountDescription.setTransportServer(null);
        response = getClient().execute(new MailAccountValidateRequest(mailAccountDescription));
        assertTrue("Valid access data in mail account do not pass validation but should", response.isValidated());

        mailAccountDescription.setMailServer(MailConfig.getProperty(MailConfig.Property.SERVER));
        mailAccountDescription.setMailPort(Integer.parseInt(MailConfig.getProperty(MailConfig.Property.PORT)));
        mailAccountDescription.setMailProtocol("imap");
        mailAccountDescription.setMailSecure(false);
        mailAccountDescription.setLogin(MailConfig.getProperty(MailConfig.Property.LOGIN));
        mailAccountDescription.setPassword(MailConfig.getProperty(MailConfig.Property.PASSWORD));
        mailAccountDescription.setTransportServer(MailConfig.getProperty(MailConfig.Property.SERVER));
        mailAccountDescription.setTransportPort(25);
        mailAccountDescription.setTransportProtocol("smtp");
        mailAccountDescription.setTransportSecure(false);
        response = getClient().execute(new MailAccountValidateRequest(mailAccountDescription));
        assertTrue("Valid access data in mail/transport account do not pass validation but should", response.isValidated());

        // With tree parameter
        mailAccountDescription.setMailServer(MailConfig.getProperty(MailConfig.Property.SERVER));
        mailAccountDescription.setMailPort(Integer.parseInt(MailConfig.getProperty(MailConfig.Property.PORT)));
        mailAccountDescription.setMailProtocol("imap");
        mailAccountDescription.setMailSecure(false);
        mailAccountDescription.setLogin(MailConfig.getProperty(MailConfig.Property.LOGIN));
        mailAccountDescription.setPassword(MailConfig.getProperty(MailConfig.Property.PASSWORD));
        mailAccountDescription.setTransportServer(MailConfig.getProperty(MailConfig.Property.SERVER));
        mailAccountDescription.setTransportPort(25);
        mailAccountDescription.setTransportProtocol("smtp");
        mailAccountDescription.setTransportSecure(false);
        response = getClient().execute(new MailAccountValidateRequest(mailAccountDescription, true, true));
        assertTrue("Valid access data in mail/transport account do not pass validation but should", response.isValidated());
        final JSONObject tree = response.getTree();
        System.out.println(tree);
    }
}
