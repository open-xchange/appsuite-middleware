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

package com.openexchange.ajax.mailaccount;

import java.io.IOException;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.mailaccount.actions.MailAccountDeleteRequest;
import com.openexchange.ajax.mailaccount.actions.MailAccountInsertRequest;
import com.openexchange.ajax.mailaccount.actions.MailAccountInsertResponse;
import com.openexchange.configuration.MailConfig;
import com.openexchange.exception.OXException;
import com.openexchange.mailaccount.MailAccountDescription;


/**
 * {@link AbstractMailAccountTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class AbstractMailAccountTest extends AbstractAJAXSession {
    protected AbstractMailAccountTest(final String name) {
        super(name);
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
        mailAccountDescription.setPrimaryAddress("bob"+System.currentTimeMillis()+"@test.invalid");
        mailAccountDescription.setSent("sent");
        mailAccountDescription.setSpam("Spam");
        mailAccountDescription.setSpamHandler("spamHandler");
        mailAccountDescription.parseTransportServerURL("smtp://mail.test.invalid");
        mailAccountDescription.setTransportLogin("login");
        mailAccountDescription.setTransportPassword("Password");
        mailAccountDescription.setTrash("trash");
        return mailAccountDescription;
    }

    protected void createMailAccount() throws OXException, IOException, SAXException, JSONException, OXException {
        mailAccountDescription = createMailAccountObject();

        updateMailAccountDescription(mailAccountDescription, MailConfig.getProperty(MailConfig.Property.LOGIN2));
        final MailAccountInsertResponse response = getClient().execute(new MailAccountInsertRequest(mailAccountDescription));
        response.fillObject(mailAccountDescription);

    }

    protected void updateMailAccountDescription(final MailAccountDescription mailAccountDescription, String user) {
        mailAccountDescription.setMailServer(MailConfig.getProperty(MailConfig.Property.SERVER));
        mailAccountDescription.setMailPort(Integer.parseInt(MailConfig.getProperty(MailConfig.Property.PORT)));

        mailAccountDescription.setMailProtocol("imap");
        mailAccountDescription.setMailSecure(false);
        mailAccountDescription.setLogin(user);
        mailAccountDescription.setPassword(MailConfig.getProperty(MailConfig.Property.PASSWORD));
        mailAccountDescription.setTransportServer(MailConfig.getProperty(MailConfig.Property.SERVER));
        mailAccountDescription.setTransportPort(25);
        mailAccountDescription.setTransportProtocol("smtp");
        mailAccountDescription.setTransportLogin(user);
        mailAccountDescription.setTransportPassword(MailConfig.getProperty(MailConfig.Property.PASSWORD));
        mailAccountDescription.setTransportSecure(false);
    }

    protected void deleteMailAccount() throws OXException, IOException, SAXException, JSONException {
        getClient().execute(new MailAccountDeleteRequest(mailAccountDescription.getId()));
    }
}
