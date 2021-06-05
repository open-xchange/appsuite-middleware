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

package com.openexchange.mail.transport;

import static com.openexchange.mail.utils.ProviderUtility.extractProtocol;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailSessionCache;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.Protocol;
import com.openexchange.mail.transport.config.TransportConfig;
import com.openexchange.mail.transport.config.TransportProperties;
import com.openexchange.session.Session;

/**
 * {@link TransportProviderRegistry}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TransportProviderRegistry {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TransportProviderRegistry.class);

    /**
     * Concurrent map used as set for transport providers
     */
    private static final Map<Protocol, TransportProvider> providers = new ConcurrentHashMap<Protocol, TransportProvider>();

    /**
     * Initializes a new {@link TransportProviderRegistry}
     */
    private TransportProviderRegistry() {
        super();
    }

    /**
     * Gets the transport provider appropriate for specified session
     *
     * @param session The session
     * @param accountId The account ID
     * @return The appropriate transport provider
     * @throws OXException If no supporting transport provider can be found
     */
    public static TransportProvider getTransportProviderBySession(Session session, int accountId) throws OXException {
        final MailSessionCache mailSessionCache = MailSessionCache.getInstance(session);
        final String key = MailSessionParameterNames.getParamTransportProvider();
        TransportProvider provider;
        try {
            provider = mailSessionCache.getParameter(accountId, key);
        } catch (ClassCastException e) {
            /*
             * Probably caused by bundle update(s)
             */
            provider = null;
        }
        final String transportServerURL = TransportConfig.getTransportServerURL(session, accountId).getServerURL();
        final String protocol;
        if (transportServerURL == null) {
            LOG.warn("Missing transport server URL. Transport server URL not set in account {} for user {} in context {}. Using fallback protocol {}", accountId, session.getUserId(), session.getContextId(), TransportProperties.getInstance().getDefaultTransportProvider());
            protocol = TransportProperties.getInstance().getDefaultTransportProvider();
        } else {
            protocol = extractProtocol(transportServerURL, TransportProperties.getInstance().getDefaultTransportProvider());
        }
        if ((null != provider) && !provider.isDeprecated() && provider.supportsProtocol(protocol)) {
            return provider;
        }
        provider = getTransportProvider(protocol);
        if (null == provider || !provider.supportsProtocol(protocol)) {
            throw MailExceptionCode.UNKNOWN_TRANSPORT_PROTOCOL.create(transportServerURL);
        }
        mailSessionCache.putParameter(accountId, key, provider);
        return provider;
    }

    /**
     * Gets the transport provider appropriate for specified mail server URL.
     * <p>
     * The given URL should match pattern
     *
     * <pre>
     * &lt;protocol&gt;://&lt;host&gt;(:&lt;port&gt;)?
     * </pre>
     *
     * The protocol should be present. Otherwise the configured fallback is used as protocol.
     *
     * @param serverUrl The transport server URL
     * @return The appropriate transport provider
     */
    public static TransportProvider getTransportProviderByURL(String serverUrl) {
        /*
         * Get appropriate provider
         */
        return getTransportProvider(extractProtocol(serverUrl, TransportProperties.getInstance().getDefaultTransportProvider()));
    }

    /**
     * Gets the transport provider appropriate for specified protocol.
     *
     * @param protocol The transport protocol
     * @return The appropriate transport provider
     */
    public static TransportProvider getTransportProvider(String protocol) {
        if (null == protocol) {
            return null;
        }
        for (Iterator<Map.Entry<Protocol, TransportProvider>> iter = providers.entrySet().iterator(); iter.hasNext();) {
            final Map.Entry<Protocol, TransportProvider> entry = iter.next();
            if (entry.getKey().isSupported(protocol)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Checks if transport provider registry is empty.
     *
     * @return <code>true</code> if empty; otherwise <code>false</code>
     */
    public static boolean isEmpty() {
        return providers.isEmpty();
    }

    /**
     * Registers a transport provider and performs its start-up actions
     *
     * @param protocol The transport protocol's string representation; e.g. <code>"smtp_smtps"</code>
     * @param provider The transport provider to register
     * @return <code>true</code> if transport provider has been successfully registered and no other transport provider supports the same
     *         protocol; otherwise <code>false</code>
     * @throws OXException If provider's start-up fails
     */
    public static boolean registerTransportProvider(String protocol, TransportProvider provider) throws OXException {
        final Protocol p = Protocol.parseProtocol(protocol);
        if (providers.containsKey(p)) {
            return false;
        }
        try {
            /*
             * Startup
             */
            provider.startUp();
            provider.setDeprecated(false);
            /*
             * Add to registry
             */
            providers.put(p, provider);
            return true;
        } catch (OXException e) {
            throw e;
        } catch (RuntimeException t) {
            LOG.error("", t);
            return false;
        }
    }

    /**
     * Unregisters all transport providers
     */
    public static void unregisterAll() {
        for (Iterator<TransportProvider> iter = providers.values().iterator(); iter.hasNext();) {
            final TransportProvider provider = iter.next();
            /*
             * Perform shutdown
             */
            try {
                provider.setDeprecated(true);
                provider.shutDown();
            } catch (OXException e) {
                LOG.error("Mail connection implementation could not be shut down", e);
            } catch (RuntimeException t) {
                LOG.error("Mail connection implementation could not be shut down", t);
            }
        }
        /*
         * Clear registry
         */
        providers.clear();
    }

    /**
     * Unregisters the transport provider
     *
     * @param provider The transport provider to unregister
     * @return The unregistered transport provider, or <code>null</code>
     * @throws OXException If provider's shut-down fails
     */
    public static TransportProvider unregisterTransportProvider(TransportProvider provider) throws OXException {
        if (!providers.containsKey(provider.getProtocol())) {
            return null;
        }
        /*
         * Unregister
         */
        final TransportProvider removed = providers.remove(provider.getProtocol());
        if (null == removed) {
            return null;
        }
        /*
         * Perform shutdown
         */
        try {
            removed.setDeprecated(true);
            removed.shutDown();
            return removed;
        } catch (OXException e) {
            throw e;
        } catch (RuntimeException t) {
            LOG.error("", t);
            return removed;
        }
    }

    /**
     * Unregisters the transport provider supporting specified protocol
     *
     * @param protocol The protocol
     * @return The unregistered instance of {@link TransportProvider}, or <code>null</code> if there was no provider supporting specified
     *         protocol
     * @throws OXException If provider's shut-down fails
     */
    public static TransportProvider unregisterTransportProviderByProtocol(String protocol) throws OXException {
        for (Iterator<Map.Entry<Protocol, TransportProvider>> iter = providers.entrySet().iterator(); iter.hasNext();) {
            final Map.Entry<Protocol, TransportProvider> entry = iter.next();
            if (entry.getKey().isSupported(protocol)) {
                iter.remove();
                entry.getValue().setDeprecated(true);
                entry.getValue().shutDown();
                return entry.getValue();
            }
        }
        return null;
    }
}
