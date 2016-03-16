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

package com.openexchange.config.cascade.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.openexchange.config.cascade.ConfigProviderService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.impl.ConfigCascade;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.tools.strings.StringParser;


/**
 * {@link ConfigCascadeActivator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ConfigCascadeActivator extends HousekeepingActivator{

    private boolean configured;

    /**
     * Initializes a new {@link ConfigCascadeActivator}.
     */
    public ConfigCascadeActivator() {
        super();
        configured = false;
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[0];
    }

    @Override
    protected void startBundle() throws Exception {
        final Logger logger = org.slf4j.LoggerFactory.getLogger(ConfigCascadeActivator.class);

        final ConfigCascade configCascade = new ConfigCascade();
        final ServiceTracker<StringParser, StringParser> stringParsers = track(StringParser.class);

        configCascade.setStringParser(new StringParser() {

            @Override
            public <T> T parse(final String s, final Class<T> t) {
                final StringParser parser = stringParsers.getService();
                if(parser == null) {
                    logger.error("Could not find suitable string parser in OSGi system");
                    return null;
                }
                return parser.parse(s, t);
            }

        });

        final BundleContext context = this.context;
        track(TrackingProvider.createFilter("server", context), new ServiceTrackerCustomizer<ConfigProviderService, ConfigProviderService>() {

            @Override
            public ConfigProviderService addingService(ServiceReference<ConfigProviderService> reference) {
                ConfigProviderService provider = context.getService(reference);
                if (isServerProvider(reference)) {
                    String scopes = getScopes(provider);
                    configure(scopes, configCascade);
                    configCascade.setProvider("server", provider);
                    registerService(ConfigViewFactory.class, configCascade);
                }
                return provider;
            }

            @Override
            public void modifiedService(ServiceReference<ConfigProviderService> reference, ConfigProviderService service) {
                // IGNORE
            }

            @Override
            public void removedService(ServiceReference<ConfigProviderService> reference, ConfigProviderService service) {
                context.ungetService(reference);
            }
        });

        openTrackers();
    }

    @Override
    public <S> void registerService(java.lang.Class<S> clazz, S service) {
        super.registerService(clazz, service);
    }

    boolean isServerProvider(final ServiceReference<?> reference) {
        Object scope = reference.getProperty("scope");
        return "server".equals(scope);
    }

    String getScopes(ConfigProviderService config) {
        try {
            return config.get("com.openexchange.config.cascade.scopes", ConfigProviderService.NO_CONTEXT, ConfigProviderService.NO_USER).get();
        } catch (OXException e) {
            Logger logger = org.slf4j.LoggerFactory.getLogger(ConfigCascadeActivator.class);
            logger.error("", e);
        }
        return null;
    }

    synchronized void configure(String scopes, ConfigCascade cascade) {
        if (configured) {
            return;
        }
        final String scops = scopes == null ? "user, context, server" : scopes;
        configured = true;

        String[] searchPath = scops.split("\\s*,\\s*");
        cascade.setSearchPath(searchPath);

        for (String scope : searchPath) {
            if ("server".equals(scope)) {
                continue;
            }

            TrackingProvider trackingProvider = new TrackingProvider(scope, context);
            rememberTracker(trackingProvider);
            cascade.setProvider(scope, trackingProvider);
            trackingProvider.open();
        }
    }

}
