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
    public static TransportProvider getTransportProviderBySession(final Session session, final int accountId) throws OXException {
        final MailSessionCache mailSessionCache = MailSessionCache.getInstance(session);
        final String key = MailSessionParameterNames.getParamTransportProvider();
        TransportProvider provider;
        try {
            provider = mailSessionCache.getParameter(accountId, key);
        } catch (final ClassCastException e) {
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
    public static TransportProvider getTransportProviderByURL(final String serverUrl) {
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
    public static TransportProvider getTransportProvider(final String protocol) {
        if (null == protocol) {
            return null;
        }
        for (final Iterator<Map.Entry<Protocol, TransportProvider>> iter = providers.entrySet().iterator(); iter.hasNext();) {
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
    public static boolean registerTransportProvider(final String protocol, final TransportProvider provider) throws OXException {
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
        } catch (final OXException e) {
            throw e;
        } catch (final RuntimeException t) {
            LOG.error("", t);
            return false;
        }
    }

    /**
     * Unregisters all transport providers
     */
    public static void unregisterAll() {
        for (final Iterator<TransportProvider> iter = providers.values().iterator(); iter.hasNext();) {
            final TransportProvider provider = iter.next();
            /*
             * Perform shutdown
             */
            try {
                provider.setDeprecated(true);
                provider.shutDown();
            } catch (final OXException e) {
                LOG.error("Mail connection implementation could not be shut down", e);
            } catch (final RuntimeException t) {
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
    public static TransportProvider unregisterTransportProvider(final TransportProvider provider) throws OXException {
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
        } catch (final OXException e) {
            throw e;
        } catch (final RuntimeException t) {
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
    public static TransportProvider unregisterTransportProviderByProtocol(final String protocol) throws OXException {
        for (final Iterator<Map.Entry<Protocol, TransportProvider>> iter = providers.entrySet().iterator(); iter.hasNext();) {
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
