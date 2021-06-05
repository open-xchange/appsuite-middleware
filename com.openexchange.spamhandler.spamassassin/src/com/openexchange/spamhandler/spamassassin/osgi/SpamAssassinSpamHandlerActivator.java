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

package com.openexchange.spamhandler.spamassassin.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleActivator;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.mail.service.MailService;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.net.ssl.config.SSLConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.spamhandler.SpamHandler;
import com.openexchange.spamhandler.spamassassin.SpamAssassinSpamHandler;
import com.openexchange.spamhandler.spamassassin.api.SpamdService;

/**
 * {@link SpamAssassinSpamHandlerActivator} - {@link BundleActivator Activator} for spam-assassin spam handler.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SpamAssassinSpamHandlerActivator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SpamAssassinSpamHandlerActivator.class);

    /**
     * Initializes a new {@link SpamAssassinSpamHandlerActivator}
     */
    public SpamAssassinSpamHandlerActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { MailService.class, ConfigViewFactory.class, SSLSocketFactoryProvider.class,
            SSLConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            SpamAssassinSpamHandler spamHandler = new SpamAssassinSpamHandler(this);
            final Dictionary<String, String> dictionary = new Hashtable<String, String>(2);
            dictionary.put("name", spamHandler.getSpamHandlerName());
            registerService(SpamHandler.class, spamHandler, dictionary);

            trackService(SpamdService.class);
            openTrackers();
        } catch (Exception e) {
            LOG.error("Problem while starting bundle: SpamHandler Spamassassin", e);
            throw e;
        }
    }

}
