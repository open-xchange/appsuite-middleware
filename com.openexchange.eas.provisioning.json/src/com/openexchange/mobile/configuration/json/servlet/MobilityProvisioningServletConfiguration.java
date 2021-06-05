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
