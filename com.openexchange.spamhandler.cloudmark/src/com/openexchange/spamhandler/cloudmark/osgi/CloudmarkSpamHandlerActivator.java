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

package com.openexchange.spamhandler.cloudmark.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.spamhandler.SpamHandler;
import com.openexchange.spamhandler.cloudmark.CloudmarkSpamHandler;

/**
 * {@link CloudmarkSpamHandlerActivator}
 *
 * @author <a href="mailto:benjamin.otterbach@open-xchange.com">Benjamin Otterbach</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CloudmarkSpamHandlerActivator extends HousekeepingActivator {

	/**
	 * Initializes a new {@link CloudmarkSpamHandlerActivator}
	 */
	public CloudmarkSpamHandlerActivator() {
		super();
	}

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class<?>[] { ConfigurationService.class, ConfigViewFactory.class };
	}

	@Override
	protected void startBundle() throws Exception {
	    org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CloudmarkSpamHandlerActivator.class);
		try {
            CloudmarkSpamHandler spamHandler = new CloudmarkSpamHandler(this);
            Dictionary<String, String> dictionary = new Hashtable<String, String>(2);
            dictionary.put("name", spamHandler.getSpamHandlerName());
            registerService(SpamHandler.class, spamHandler, dictionary);
            logger.info("Successfully started bundle {}", context.getBundle().getSymbolicName());
        } catch (Exception e) {
            logger.error("Failed starting bundle {}", context.getBundle().getSymbolicName(), e);
            throw e;
        }
	}

}
