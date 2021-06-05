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

package com.openexchange.spamhandler.spamexperts.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.service.http.HttpService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ForcedReloadable;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.mail.service.MailService;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.rest.client.httpclient.SpecificHttpClientConfigProvider;
import com.openexchange.rest.client.httpclient.HttpClientService;
import com.openexchange.spamhandler.SpamHandler;
import com.openexchange.spamhandler.spamexperts.SpamExpertsSpamHandler;
import com.openexchange.spamhandler.spamexperts.http.SpamExtertsHttpConfiguration;
import com.openexchange.spamhandler.spamexperts.management.SpamExpertsConfig;
import com.openexchange.spamhandler.spamexperts.servlets.SpamExpertsServlet;
import com.openexchange.tools.servlet.http.HTTPServletRegistration;
import com.openexchange.user.UserService;
import com.openexchange.version.VersionService;

public class SpamExpertsActivator extends HousekeepingActivator {

	private HTTPServletRegistration servletRegistration;

	public SpamExpertsActivator() {
		super();
	}

	@Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { UserService.class, DatabaseService.class, ContextService.class, ConfigurationService.class, ConfigViewFactory.class, 
                                HttpService.class, MailService.class, SSLSocketFactoryProvider.class, HttpClientService.class, VersionService.class };
    }

	@Override
	protected synchronized void startBundle() throws Exception {
	    org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SpamExpertsActivator.class);
        logger.info("starting bundle: \"com.openexchange.spamhandler.spamexperts\"");
        
        trackService(MailAccountStorageService.class);
        openTrackers();

	    final SpamExpertsConfig config = new SpamExpertsConfig(this);

	    SpamExpertsSpamHandler spamHandler = new SpamExpertsSpamHandler(config, this);
	    Dictionary<String, String> dictionary = new Hashtable<String, String>(2);
        dictionary.put("name", spamHandler.getSpamHandlerName());
        registerService(SpamHandler.class, spamHandler, dictionary);

        registerService(ForcedReloadable.class, new ForcedReloadable() {

            @Override
            public void reloadConfiguration(ConfigurationService configService) {
                config.clearCache();
            }
            
        });

        String alias = getService(ConfigurationService.class).getProperty("com.openexchange.custom.spamexperts.panel_servlet", "/ajax/spamexperts/panel").trim();
		SpamExpertsServlet spamExpertsServlet = new SpamExpertsServlet(config, this);
        servletRegistration = new HTTPServletRegistration(context, spamExpertsServlet, alias);
        registerService(SpecificHttpClientConfigProvider.class, new SpamExtertsHttpConfiguration(getService(VersionService.class)));
	}

	@Override
	protected synchronized void stopBundle() throws Exception {
	    org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SpamExpertsActivator.class);
        logger.info("stopping bundle: \"com.openexchange.spamhandler.spamexperts\"");

        HTTPServletRegistration servletRegistration = this.servletRegistration;
        if (servletRegistration != null) {
            this.servletRegistration = null;
            servletRegistration.unregister();
        }
        super.stopBundle();
	}

}
