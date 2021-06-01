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

package com.openexchange.config.cascade.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.openexchange.config.cascade.ConfigProviderService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViewScope;
import com.openexchange.config.cascade.impl.ConfigCascade;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.tools.strings.StringParser;

/**
 * {@link ConfigCascadeActivator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ConfigCascadeActivator extends HousekeepingActivator {

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

        // Create the ConfigCascade instance
        final ConfigCascade configCascade = new ConfigCascade();

        // Initialize tracker for StringParser
        final BundleContext context = this.context;
        final ConfigCascadeActivator activator = this;
        {
            ServiceTrackerCustomizer<StringParser, StringParser> customizer = new ServiceTrackerCustomizer<StringParser, StringParser>() {

                private boolean configCascadeStarted = false;

                @Override
                public void removedService(ServiceReference<StringParser> reference, StringParser service) {
                    context.ungetService(reference);
                }

                @Override
                public void modifiedService(ServiceReference<StringParser> reference, StringParser service) {
                    // Ignore
                }

                @Override
                public StringParser addingService(ServiceReference<StringParser> reference) {
                    StringParser service = context.getService(reference);

                    // At least one StringParser instance available
                    startConfigCascade();

                    return service;
                }

                private synchronized void startConfigCascade() {
                    if (configCascadeStarted) {
                        return;
                    }

                    ServiceTracker<ConfigProviderService, ConfigProviderService> configProviders = activator.track(TrackingProvider.createFilter(ConfigViewScope.SERVER.getScopeName(), context), new ServiceTrackerCustomizer<ConfigProviderService, ConfigProviderService>() {

                        @Override
                        public ConfigProviderService addingService(ServiceReference<ConfigProviderService> reference) {
                            ConfigProviderService provider = context.getService(reference);
                            if (isServerProvider(reference)) {
                                String scopes = getScopes(provider);
                                configure(scopes, configCascade);
                                configCascade.setProvider(ConfigViewScope.SERVER.getScopeName(), provider);
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
                    configProviders.open();
                    configCascadeStarted = true;
                }
            };

            final ServiceTracker<StringParser, StringParser> stringParsers = new ServiceTracker<StringParser, StringParser>(context, StringParser.class, customizer);
            rememberTracker(stringParsers);

            configCascade.setStringParser(new StringParser() {

                @Override
                public <T> T parse(final String s, final Class<T> t) {
                    final StringParser parser = stringParsers.getService();
                    if (parser == null) {
                        logger.error("Could not find suitable string parser in OSGi system");
                        return null;
                    }
                    return parser.parse(s, t);
                }

            });
        }

        openTrackers();
    }

    @Override
    public <S> void registerService(java.lang.Class<S> clazz, S service) {
        super.registerService(clazz, service);
    }

    @Override
    public <S> ServiceTracker<S, S> track(Filter filter, ServiceTrackerCustomizer<S, S> customizer) {
        return super.track(filter, customizer);
    }

    boolean isServerProvider(final ServiceReference<?> reference) {
        Object scope = reference.getProperty("scope");
        return ConfigViewScope.SERVER.getScopeName().equals(scope);
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

        String[] searchPath;
        if (scopes == null) {
            searchPath = new String[5];
            searchPath[0] = ConfigViewScope.USER.getScopeName();
            searchPath[1] = ConfigViewScope.CONTEXT.getScopeName();
            searchPath[2] = ConfigViewScope.RESELLER.getScopeName();
            searchPath[3] = ConfigViewScope.CONTEXT_SETS.getScopeName();
            searchPath[4] = ConfigViewScope.SERVER.getScopeName();
        } else {
            searchPath = Strings.splitByComma(scopes);
        }

        for (String scope : searchPath) {
            if (ConfigViewScope.SERVER.getScopeName().equals(scope)) {
                continue;
            }

            TrackingProvider trackingProvider = new TrackingProvider(scope, context);
            rememberTracker(trackingProvider);
            cascade.setProvider(scope, trackingProvider);
            trackingProvider.open();
        }

        cascade.setSearchPath(searchPath);
        configured = true;
    }

}
