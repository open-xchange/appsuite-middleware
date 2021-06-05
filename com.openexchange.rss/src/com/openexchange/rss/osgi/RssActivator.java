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

package com.openexchange.rss.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.html.HtmlService;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.net.ssl.config.UserAwareSSLConfigurationService;
import com.openexchange.rss.RssJsonConverter;
import com.openexchange.rss.actions.RssActionFactory;
import com.openexchange.rss.utils.RssProperties;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

public class RssActivator extends AJAXModuleActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { HtmlService.class, CapabilityService.class, ConfigViewFactory.class, ConfigurationService.class, SSLSocketFactoryProvider.class, UserAwareSSLConfigurationService.class, RssProperties.class };
    }

    @Override
    protected void startBundle() {
        Services.setServiceLookup(this);
        registerModule(new RssActionFactory(), "rss");
        registerService(ResultConverter.class, new RssJsonConverter());

        final Dictionary<String, Object> properties = new Hashtable<String, Object>(1);
        properties.put(CapabilityChecker.PROPERTY_CAPABILITIES, "rss");
        registerService(CapabilityChecker.class, new CapabilityChecker() {

            @Override
            public boolean isEnabled(String capability, Session ses) throws OXException {
                if ("rss".equals(capability)) {
                    final ServerSession session = ServerSessionAdapter.valueOf(ses);
                    if (session.isAnonymous() || session.getUser().isGuest()) {
                        return false;
                    }
                    final ConfigViewFactory factory = getService(ConfigViewFactory.class);
                    if (null != factory) {
                        final ConfigView view = factory.getView(session.getUserId(), session.getContextId());
                        return view.opt("com.openexchange.rss", boolean.class, Boolean.TRUE).booleanValue() && view.opt("com.openexchange.messaging.rss", boolean.class, Boolean.TRUE).booleanValue();
                    }
                }

                return true;
            }
        }, properties);

        getService(CapabilityService.class).declareCapability("rss");
    }

    @Override
    protected void stopBundle() throws Exception {
        Services.setServiceLookup(null);
        super.stopBundle();
    }
}
