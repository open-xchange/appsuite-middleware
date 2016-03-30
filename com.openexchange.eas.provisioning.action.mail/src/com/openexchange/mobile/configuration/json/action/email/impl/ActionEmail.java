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

package com.openexchange.mobile.configuration.json.action.email.impl;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.TextBodyMailPart;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mobile.configuration.json.action.ActionService;
import com.openexchange.mobile.configuration.json.container.ProvisioningInformation;
import com.openexchange.mobile.configuration.json.container.ProvisioningResponse;
import com.openexchange.mobile.configuration.json.servlet.MobilityProvisioningServlet;

/**
 *
 * @author <a href="mailto:benjamin.otterbach@open-xchange.com">Benjamin Otterbach</a>
 *
 */
public class ActionEmail implements ActionService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MobilityProvisioningServlet.class);

    public ProvisioningResponse handleAction(final ProvisioningInformation provisioningInformation){
    	final ProvisioningResponse provisioningResponse = new ProvisioningResponse();

    	try {
			InternetAddress fromAddress = new InternetAddress(provisioningInformation.getUser().getMail(), true);
			if (!provisioningInformation.getMailFrom().trim().toUpperCase().equals("USER")) {
				fromAddress = new InternetAddress(provisioningInformation.getMailFrom(), true);
			}

			final com.openexchange.mail.transport.TransportProvider provider =
				com.openexchange.mail.transport.TransportProviderRegistry.getTransportProviderBySession(provisioningInformation.getSession(), 0);

			final ComposedMailMessage msg = provider.getNewComposedMailMessage(provisioningInformation.getSession(), provisioningInformation.getCtx());
			msg.addFrom(fromAddress);
			msg.addTo(new InternetAddress(provisioningInformation.getTarget()));

			if (provisioningInformation.containsProvisioningEmailMessage(provisioningInformation.getUser().getLocale().toString())) {
				msg.setSubject(provisioningInformation.getProvisioningEmailMessage(provisioningInformation.getUser().getLocale().toString()).getSubject());

				final TextBodyMailPart textPart = provider.getNewTextBodyPart(provisioningInformation.getProvisioningEmailMessage(provisioningInformation.getUser().getLocale().toString()).getMessage());
				msg.setBodyPart(textPart);
			} else {
				final TextBodyMailPart textPart = provider.getNewTextBodyPart(provisioningInformation.getUrl());
				msg.setBodyPart(textPart);
			}

			msg.setContentType("text/plain");
			msg.setAccountId(MailAccount.DEFAULT_ID);

			final MailTransport transport = MailTransport.getInstance(provisioningInformation.getSession());
			try {
				transport.sendMailMessage(msg, com.openexchange.mail.dataobjects.compose.ComposeType.NEW, new Address[] { new InternetAddress(provisioningInformation.getTarget()) });
			} finally {
				transport.close();
			}

			provisioningResponse.setMessage("Provisioning mail has been send to " + provisioningInformation.getTarget());
			provisioningResponse.setSuccess(true);
		} catch (final OXException e) {
			logError("Couldn't send provisioning mail", e, provisioningResponse);
			provisioningResponse.setMessage("Couldn't send provisioning mail");
			provisioningResponse.setSuccess(false);
		} catch (final AddressException e) {
			logError("Target Spam email address cannot be parsed", e, provisioningResponse);
			provisioningResponse.setMessage("Target Spam email address cannot be parsed");
			provisioningResponse.setSuccess(false);
		}

    	return provisioningResponse;
    }

    private void logError(final String message, final Exception e, final ProvisioningResponse provisioningResponse) {
    	LOG.error(message, e);
    	provisioningResponse.setMessage(message);
    	provisioningResponse.setSuccess(false);
    }

}
