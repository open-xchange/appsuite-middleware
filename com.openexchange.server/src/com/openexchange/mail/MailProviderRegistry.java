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

package com.openexchange.mail;

import static com.openexchange.mail.utils.ProviderUtility.extractProtocol;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.AllMailProvider;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.api.MailProvider;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.session.Session;

/**
 * {@link MailProviderRegistry}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailProviderRegistry {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailProviderRegistry.class);

    /** The special protocol associated with Unified Mail provider */
    private static final Protocol PROTOCOL_UNIFIED_MAIL = new Protocol(UnifiedInboxManagement.PROTOCOL_UNIFIED_INBOX);

    /**
     * Concurrent map used as set for mail providers
     */
    private static final ConcurrentMap<Protocol, MailProvider> PROVIDERS = new NonBlockingHashMap<Protocol, MailProvider>();

    private static final AtomicReference<AllMailProvider> ALL_PROVIDER = new AtomicReference<AllMailProvider>();

    /**
     * Initializes a new {@link MailProviderRegistry}
     */
    private MailProviderRegistry() {
        super();
    }

    /**
     * Checks if the special provider for Unified Mail has been registered.
     *
     * @return <code>true</code> if Unified Mail provider is registered; otherwise <code>false</code>
     */
    public static boolean isUnifiedMailAvailable() {
        return PROVIDERS.containsKey(PROTOCOL_UNIFIED_MAIL);
    }

    /**
     * Gets registered mail providers' protocols
     *
     * @return The protocols
     */
    public static List<Protocol> getProtocols() {
        return new ArrayList<Protocol>(PROVIDERS.keySet());
    }

    /**
     * Gets the mail provider appropriate for specified session.
     *
     * @param session The session
     * @param accountId The account ID
     * @return The appropriate mail provider
     * @throws OXException If no supporting mail provider can be found
     */
    public static MailProvider getMailProviderBySession(Session session, int accountId) throws OXException {
        final MailSessionCache mailSessionCache = MailSessionCache.getInstance(session);
        final String key = MailSessionParameterNames.getParamMailProvider();
        MailProvider provider;
        try {
            provider = mailSessionCache.getParameter(accountId, key);
        } catch (ClassCastException e) {
            /*
             * Probably caused by bundle update(s)
             */
            provider = null;
        }
        final String mailServerURL = MailConfig.getMailServerURL(session, accountId).getServerURL();
        final String protocol;
        if (mailServerURL == null) {
            LOG.warn("Missing mail server URL. Mail server URL not set in account {} for user {} in context {}. Using fallback protocol {}", accountId, session.getUserId(), session.getContextId(), MailProperties.getInstance().getDefaultMailProvider());
            protocol = MailProperties.getInstance().getDefaultMailProvider();
        } else {
            protocol = extractProtocol(mailServerURL, MailProperties.getInstance().getDefaultMailProvider());
        }
        if ((null != provider) && !provider.isDeprecated() && provider.supportsProtocol(protocol)) {
            return provider;
        }
        provider = getMailProvider(protocol);
        if (null == provider || !provider.supportsProtocol(protocol)) {
            throw MailExceptionCode.UNKNOWN_PROTOCOL.create(mailServerURL);
        }
        mailSessionCache.putParameter(accountId, key, provider);
        return provider;
    }

    /**
     * Gets the mail provider appropriate for specified mail server URL.
     * <p>
     * The given URL should match pattern
     *
     * <pre>
     * &lt;protocol&gt;://&lt;host&gt;(:&lt;port&gt;)?
     * </pre>
     *
     * The protocol should be present. Otherwise the configured fallback is used as protocol.
     *
     * @param serverUrl The mail server URL
     * @return The appropriate mail provider
     */
    public static MailProvider getMailProviderByURL(String serverUrl) {
        /*
         * Get appropriate provider
         */
        return getMailProvider(extractProtocol(serverUrl, MailProperties.getInstance().getDefaultMailProvider()));
    }

    /**
     * Gets the mail provider appropriate for specified protocol.
     *
     * @param protocolName The mail protocol; e.g. <code>"imap"</code>
     * @return The appropriate mail provider or <code>null</code>
     */
    public static MailProvider getMailProvider(String protocolName) {
        if (null == protocolName) {
            return null;
        }
        final AllMailProvider all = ALL_PROVIDER.get();
        if (null != all) {
            final MailProvider realMailProvider = getRealMailProvider(protocolName);
            return (null == realMailProvider) ? null : all.getDelegatingProvider(realMailProvider);
        }
        /*
         * Return real provider
         */
        return getRealMailProvider(protocolName);
    }

    /**
     * Gets the real mail provider for specified protocol name.
     *
     * @param protocolName The protocol name
     * @return The mail provider or <code>null</code>
     */
    public static MailProvider getRealMailProvider(String protocolName) {
        if (null == protocolName) {
            return null;
        }
        /*
         * Look-up
         */
        for (Map.Entry<Protocol, MailProvider> providerEntry : PROVIDERS.entrySet()) {
            if (providerEntry.getKey().isSupported(protocolName)) {
                return providerEntry.getValue();
            }
        }
        return null;
    }

    /**
     * Registers a mail provider and performs its start-up actions
     *
     * @param protocol The mail protocol's string representation; e.g. <code>"imap_imaps"</code>
     * @param provider The mail provider to register
     * @return <code>true</code> if mail provider has been successfully registered and no other mail provider supports the same protocol;
     *         otherwise <code>false</code>
     * @throws OXException If provider's start-up fails
     */
    public static boolean registerMailProvider(String protocol, MailProvider provider) throws OXException {
        try {
            final Protocol p = Protocol.parseProtocol(protocol);
            if (Protocol.PROTOCOL_ALL.equals(p)) {
                /*
                 * All provider
                 */
                if (!ALL_PROVIDER.compareAndSet(null, (AllMailProvider) provider)) {
                    return false;
                }
                /*
                 * Startup
                 */
                provider.startUp();
                provider.setDeprecated(false);
                return true;
            }
            /*
             * Non-all provider
             */
            if (null != PROVIDERS.putIfAbsent(p, provider)) {
                return false;
            }
            /*
             * Startup
             */
            provider.startUp();
            provider.setDeprecated(false);
            return true;
        } catch (OXException e) {
            throw e;
        } catch (RuntimeException t) {
            LOG.error("", t);
            return false;
        }
    }

    /**
     * Unregisters all mail providers
     */
    public static void unregisterAll() {
        for (Iterator<MailProvider> iter = PROVIDERS.values().iterator(); iter.hasNext();) {
            final MailProvider provider = iter.next();
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
        PROVIDERS.clear();
        final MailProvider all = ALL_PROVIDER.get();
        if (null != all) {
            /*
             * Perform shutdown
             */
            try {
                all.setDeprecated(true);
                all.shutDown();
                ALL_PROVIDER.set(null);
            } catch (OXException e) {
                LOG.error("Mail connection implementation could not be shut down", e);
            } catch (RuntimeException t) {
                LOG.error("Mail connection implementation could not be shut down", t);
            }
        }
    }

    /**
     * Unregisters the mail provider
     *
     * @param provider The mail provider to unregister
     * @return The unregistered mail provider, or <code>null</code>
     * @throws OXException If provider's shut-down fails
     */
    public static MailProvider unregisterMailProvider(MailProvider provider) throws OXException {
        final Protocol protocol = provider.getProtocol();
        if (Protocol.PROTOCOL_ALL.equals(protocol)) {
            AllMailProvider all;
            do {
                all = ALL_PROVIDER.get();
            } while (!ALL_PROVIDER.compareAndSet(all, null));
            if (null == all) {
                return null;
            }
            /*
             * Perform shutdown
             */
            try {
                all.setDeprecated(true);
                all.shutDown();
            } catch (OXException e) {
                throw e;
            } catch (RuntimeException t) {
                LOG.error("", t);
            }
            return all;
        }
        /*
         * Unregister
         */
        final MailProvider removed = PROVIDERS.remove(protocol);
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
     * Unregisters the mail provider supporting specified protocol
     *
     * @param protocol The protocol
     * @return The unregistered instance of {@link MailProvider}, or <code>null</code> if there was no provider supporting specified
     *         protocol
     * @throws OXException If provider's shut-down fails
     */
    public static MailProvider unregisterMailProviderByProtocol(String protocol) throws OXException {
        if (Protocol.ALL.equals(protocol)) {
            AllMailProvider all;
            do {
                all = ALL_PROVIDER.get();
            } while (!ALL_PROVIDER.compareAndSet(all, null));
            if (null == all) {
                return null;
            }
            /*
             * Perform shutdown
             */
            try {
                all.setDeprecated(true);
                all.shutDown();
            } catch (OXException e) {
                throw e;
            } catch (RuntimeException t) {
                LOG.error("", t);
            }
            return all;
        }
        /*
         * Non-all
         */
        for (Iterator<Map.Entry<Protocol, MailProvider>> iter = PROVIDERS.entrySet().iterator(); iter.hasNext();) {
            final Map.Entry<Protocol, MailProvider> entry = iter.next();
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
