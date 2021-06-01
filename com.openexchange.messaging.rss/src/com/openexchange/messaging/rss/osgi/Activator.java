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

package com.openexchange.messaging.rss.osgi;

import com.openexchange.html.HtmlService;
import com.openexchange.messaging.MessagingService;
import com.openexchange.messaging.rss.RSSMessagingService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.proxy.ProxyRegistry;
import com.openexchange.rss.utils.RssProperties;

/**
 * {@link Activator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class Activator extends HousekeepingActivator {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {

        static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Activator.class);
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { RssProperties.class };
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            track(HtmlService.class, new HTMLRegistryCustomizer(context));
            track(ProxyRegistry.class, new ProxyRegistryCustomizer(context));
            openTrackers();
            registerService(MessagingService.class, new RSSMessagingService(getService(RssProperties.class)), null);
        } catch (Exception x) {
            LoggerHolder.LOG.error("", x);
            throw x;
        }
    }

}
