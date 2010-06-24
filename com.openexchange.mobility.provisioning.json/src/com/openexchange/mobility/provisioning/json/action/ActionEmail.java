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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.mobility.provisioning.json.action;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.mail.MailException;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.TextBodyMailPart;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mobility.provisioning.json.configuration.MobilityProvisioningConfiguration;
import com.openexchange.mobility.provisioning.json.servlet.MobilityProvisioningServlet;
import com.openexchange.server.ServiceException;
import com.openexchange.session.Session;

/**
 * 
 * @author <a href="mailto:benjamin.otterbach@open-xchange.com">Benjamin Otterbach</a>
 * 
 */
public class ActionEmail implements ActionService {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(MobilityProvisioningServlet.class);
	
    public String handleAction(String target, Session session) throws ActionException {
    	String message = "";
    	
    	try {
			Context ctx = ContextStorage.getStorageContext(session);
			User user = UserStorage.getInstance().getUser(session.getUserId(), ctx);
			
			InternetAddress fromAddress = new InternetAddress(user.getMail(), true);
			if (!MobilityProvisioningConfiguration.getProvisioningMailFrom().trim().toUpperCase().equals("USER")) {
				fromAddress = new InternetAddress(MobilityProvisioningConfiguration.getProvisioningMailFrom(), true);
			}

			final com.openexchange.mail.transport.TransportProvider provider =
				com.openexchange.mail.transport.TransportProviderRegistry.getTransportProviderBySession(session, 0);

			ComposedMailMessage msg = provider.getNewComposedMailMessage(session, ctx);
			msg.addFrom(fromAddress);
			msg.addTo(new InternetAddress(target));
			msg.setSubject(MobilityProvisioningConfiguration.getProvisioningMailSubject());

			String provisioningUrl = MobilityProvisioningConfiguration.getProvisioningURL();
			provisioningUrl = provisioningUrl.replace("%l", URLEncoder.encode(session.getLogin(), MobilityProvisioningConfiguration.getProvisioningURLEncoding()));
			provisioningUrl = provisioningUrl.replace("%c", URLEncoder.encode(String.valueOf(session.getContextId()), MobilityProvisioningConfiguration.getProvisioningURLEncoding()));
			provisioningUrl = provisioningUrl.replace("%u", URLEncoder.encode(session.getUserlogin(), MobilityProvisioningConfiguration.getProvisioningURLEncoding()));
			provisioningUrl = provisioningUrl.replace("%p", URLEncoder.encode(user.getMail(), MobilityProvisioningConfiguration.getProvisioningURLEncoding()));
			
			final TextBodyMailPart textPart = provider.getNewTextBodyPart(provisioningUrl);
			msg.setBodyPart(textPart);
			msg.setContentType("text/plain");

			final MailTransport transport = MailTransport.getInstance(session);
			try {
				transport.sendMailMessage(msg, com.openexchange.mail.dataobjects.compose.ComposeType.NEW, new Address[] { new InternetAddress(target) });
			} finally {
				transport.close();
			}
			
			message = "Provisioning mail has been send to " + target;
		} catch (MailException e) {
			message = logError("Couldn't send provisioning mail", e);
		} catch (ContextException e) {
			message = logError("Cannot find context for user", e);
		} catch (LdapException e) {
			message = logError("Cannot get user object", e);
		} catch (AddressException e) {
			message = logError("Target Spam email address cannot be parsed", e);
		} catch (ServiceException e) {
			message = logError("Cannot get configuration", e);
		} catch (UnsupportedEncodingException e) {
			message = logError("Error on correcting provisioning url", e);
		}
    	
    	return message;
    }
    
    private String logError(String message, Exception e) {
    	LOG.error(message, e);
    	return message;
    }
	
}
