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

package com.openexchange.mobile.configuration.json.servlet;

import java.util.HashMap;
import java.util.Iterator;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.mobile.configuration.json.container.ProvisioningEmailMessage;
import com.openexchange.mobile.configuration.json.container.ProvisioningSMSMessage;
import com.openexchange.mobile.configuration.json.osgi.MobilityProvisioningServiceRegistry;

/**
 *
 * @author <a href="mailto:benjamin.otterbach@open-xchange.com">Benjamin Otterbach</a>
 *
 */
public class MobilityProvisioningServletConfiguration {

	private static final String provisioningEmailMessageSearchString = "com.openexchange.mobile.configuration.mail.message.";
	private static final String provisioningSMSMessageSearchString = "com.openexchange.mobile.configuration.sms.message.";

	protected static String getProvisioningURL() throws OXException {
		final ConfigurationService configservice = MobilityProvisioningServiceRegistry.getInstance().getService(ConfigurationService.class,true);
		return configservice.getProperty("com.openexchange.mobile.configuration.url");
	}

	protected static String getProvisioningURLEncoding() throws OXException {
		final ConfigurationService configservice = MobilityProvisioningServiceRegistry.getInstance().getService(ConfigurationService.class,true);
		return configservice.getProperty("com.openexchange.mobile.configuration.urlencoding");
	}

	protected static String getProvisioningMailFrom() throws OXException {
		final ConfigurationService configservice = MobilityProvisioningServiceRegistry.getInstance().getService(ConfigurationService.class,true);
		return configservice.getProperty("com.openexchange.mobile.configuration.mail.from");
	}

	protected static String getProvisioningMailSubject() throws OXException {
		final ConfigurationService configservice = MobilityProvisioningServiceRegistry.getInstance().getService(ConfigurationService.class,true);
		return configservice.getProperty("com.openexchange.mobile.configuration.mail.subject");
	}

	protected static HashMap<String, ProvisioningEmailMessage> getProvisioningEmailMessages(final String url) throws OXException {
		final HashMap<String, ProvisioningEmailMessage> provisioningEmailMessages = new HashMap<String, ProvisioningEmailMessage>();

		final ConfigurationService configservice = MobilityProvisioningServiceRegistry.getInstance().getService(ConfigurationService.class,true);

		final Iterator<String> propertyNamesIterator = configservice.propertyNames();
		while (propertyNamesIterator.hasNext()) {
			final String tmp = propertyNamesIterator.next();
			if (tmp.startsWith(provisioningEmailMessageSearchString)) {
				final String locale = tmp.substring(provisioningEmailMessageSearchString.length(), tmp.lastIndexOf('.'));
				if (locale != null && locale.trim().length() > 0 && !provisioningEmailMessages.containsKey(locale)) {
					final String subject = configservice.getProperty(provisioningEmailMessageSearchString + locale + ".subject", "");
					final String text = configservice.getProperty(provisioningEmailMessageSearchString + locale + ".text", "").replace("%u", url);

					provisioningEmailMessages.put(locale, new ProvisioningEmailMessage(
							locale,
							subject,
							text));
				}
			}
		}

		return provisioningEmailMessages;
	}

	protected static HashMap<String, ProvisioningSMSMessage> getProvisioningSMSMessages(final String url) throws OXException {
		final HashMap<String, ProvisioningSMSMessage> provisioningSMSMessages = new HashMap<String, ProvisioningSMSMessage>();

		final ConfigurationService configservice = MobilityProvisioningServiceRegistry.getInstance().getService(ConfigurationService.class,true);

		final Iterator<String> propertyNamesIterator = configservice.propertyNames();
		while (propertyNamesIterator.hasNext()) {
			final String tmp = propertyNamesIterator.next();
			if (tmp.startsWith(provisioningSMSMessageSearchString)) {
				final String locale = tmp.substring(provisioningSMSMessageSearchString.length(), tmp.lastIndexOf('.'));
				if (locale != null && locale.trim().length() > 0 && !provisioningSMSMessages.containsKey(locale)) {
					final String text = configservice.getProperty(provisioningSMSMessageSearchString + locale + ".text", "").replace("%u", url);

					provisioningSMSMessages.put(locale, new ProvisioningSMSMessage(
							locale,
							text));
				}
			}
		}

		return provisioningSMSMessages;
	}

}
