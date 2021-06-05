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

package com.openexchange.proxy.authenticator.impl;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.java.Strings;
import com.openexchange.proxy.authenticator.PasswordAuthenticationProvider;

/**
 * {@link ProxyAuthenticator}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class ProxyAuthenticator extends Authenticator implements ServiceTrackerCustomizer<PasswordAuthenticationProvider, PasswordAuthenticationProvider> {

    private static final Logger LOG = LoggerFactory.getLogger(ProxyAuthenticator.class);

    private final BundleContext context;
    private final Map<String, Queue<PasswordAuthenticationProvider>> providers;

    /**
     * Initializes a new {@link ProxyAuthenticator}.
     */
    public ProxyAuthenticator(BundleContext context) {
        super();
        this.context = context;
        providers = new ConcurrentHashMap<>(8, 0.9F, 1);
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        if (getRequestorType() == RequestorType.PROXY) {
            // Entity-requesting authentication is a proxy server
            String protocol = Strings.asciiLowerCase(getRequestingProtocol());
            if (null != protocol) {
                // Optionally followed by "/version", where version is a version number.
                int pos = protocol.lastIndexOf('/');
                Queue<PasswordAuthenticationProvider> providers = this.providers.get(pos < 0 ? protocol : protocol.substring(0, pos));
                if (providers != null) {
                    String host = getRequestingHost();
                    int port = getRequestingPort();

                    for (PasswordAuthenticationProvider provider : providers) {
                        PasswordAuthentication passwordAuthentication = provider.getPasswordAuthentication(host, port);
                        if (passwordAuthentication != null) {
                            return passwordAuthentication;
                        }
                    }
                }
            }
        }
        return super.getPasswordAuthentication();
    }

    // --------------------------------------------------- ServiceTracker stuff -------------------------------------------------------------

    @Override
    public PasswordAuthenticationProvider addingService(ServiceReference<PasswordAuthenticationProvider> reference) {
        PasswordAuthenticationProvider provider = context.getService(reference);
        if (provider == null) {
            context.ungetService(reference);
            return null;
        }

        String protocol = provider.getProtocol();
        if (Strings.isEmpty(protocol)) {
            LOG.error("The returned protocol of the PasswordAuthenticationProvider with the name {} is empty and will therefore not be used.");
            context.ungetService(reference);
            return null;
        }

        Queue<PasswordAuthenticationProvider> queue = providers.get(protocol);
        if (queue == null) {
            Queue<PasswordAuthenticationProvider> newqueue = new ConcurrentLinkedQueue<>();
            queue = providers.putIfAbsent(protocol, newqueue);
            if (null == queue) {
                queue = newqueue;
            }
        }

        if (false == queue.offer(provider)) {
            LOG.error("Failed to add PasswordAuthenticationProvider {} for protocol {}.", provider.getClass().getName(), protocol);
            context.ungetService(reference);
            return null;
        }

        LOG.info("Successfully added PasswordAuthenticationProvider {} for protocol {}.", provider.getClass().getName(), protocol);
        return provider;
    }

    @Override
    public void modifiedService(ServiceReference<PasswordAuthenticationProvider> reference, PasswordAuthenticationProvider provider) {
        // ignore property modifications
    }

    @Override
    public void removedService(ServiceReference<PasswordAuthenticationProvider> reference, PasswordAuthenticationProvider provider) {
        if (provider == null) {
            return;
        }

        String protocol = provider.getProtocol();
        if (Strings.isEmpty(protocol)) {
            return;
        }

        Queue<PasswordAuthenticationProvider> queue = providers.get(protocol);
        if (queue != null && queue.remove(provider)) {
            LOG.info("Successfully removed PasswordAuthenticationProvider {} for protocol {}.", provider.getClass().getName(), protocol);
        }
    }

}
