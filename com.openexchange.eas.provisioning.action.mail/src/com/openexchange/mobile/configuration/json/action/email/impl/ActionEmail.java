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

    @Override
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
				msg.setSubject(provisioningInformation.getProvisioningEmailMessage(provisioningInformation.getUser().getLocale().toString()).getSubject(), true);

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
		} catch (OXException e) {
			logError("Couldn't send provisioning mail", e, provisioningResponse);
			provisioningResponse.setMessage("Couldn't send provisioning mail");
			provisioningResponse.setSuccess(false);
		} catch (AddressException e) {
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
