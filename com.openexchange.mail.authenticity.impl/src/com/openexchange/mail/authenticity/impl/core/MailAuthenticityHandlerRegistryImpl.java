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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mail.authenticity.impl.core;

import java.util.Collection;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.mail.authenticity.MailAuthenticityHandler;
import com.openexchange.mail.authenticity.MailAuthenticityHandlerRegistry;
import com.openexchange.mail.authenticity.MailAuthenticityProperty;
import com.openexchange.mail.authenticity.impl.helper.ThresholdAwareAuthenticityHandler;
import com.openexchange.session.Session;
import com.openexchange.threadpool.ThreadPools;

/**
 * {@link MailAuthenticityHandlerRegistryImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class MailAuthenticityHandlerRegistryImpl implements MailAuthenticityHandlerRegistry, ServiceTrackerCustomizer<MailAuthenticityHandler, MailAuthenticityHandler> {

    private final Queue<MailAuthenticityHandler> handlers;
    private final LoadingCache<Key, ConfigAndHandler> cache;
    private final BundleContext context;

    /**
     * Initializes a new {@link MailAuthenticityHandlerRegistryImpl}.
     */
    public MailAuthenticityHandlerRegistryImpl(final LeanConfigurationService leanConfigService, BundleContext context) {
        super();
        this.context = context;

        // Initiate handler queue
        final ConcurrentLinkedQueue<MailAuthenticityHandler> handlers = new ConcurrentLinkedQueue<>();
        this.handlers = handlers;

        // Initiate cache
        CacheLoader<Key, ConfigAndHandler> loader = new CacheLoader<Key, MailAuthenticityHandlerRegistryImpl.ConfigAndHandler>() {

            @Override
            public ConfigAndHandler load(Key key) {
                int userId = key.getUserId();
                int contextId = key.getContextId();
                boolean enabled = leanConfigService.getBooleanProperty(userId, contextId, MailAuthenticityProperty.ENABLED);
                if (false == enabled) {
                    // Not enabled
                    return ConfigAndHandler.NOT_ENABLED;
                }

                // Look-up up appropriate handler
                MailAuthenticityHandler highestRankedHandler = null;
                for (MailAuthenticityHandler handler : handlers) {
                    // First, check ranking, then if enabled for session-associated user
                    if ((null == highestRankedHandler || highestRankedHandler.getRanking() < handler.getRanking()) && handler.isEnabled(key.getSession())) {
                        highestRankedHandler = handler;
                    }
                }
                if (null == highestRankedHandler) {
                    // No suitable handler available
                    return ConfigAndHandler.NOT_ENABLED;
                }

                // Check threshold date
                long dateThreshold = leanConfigService.getLongProperty(userId, contextId, MailAuthenticityProperty.THRESHOLD);
                return new ConfigAndHandler(dateThreshold, ThresholdAwareAuthenticityHandler.wrapIfApplicable(highestRankedHandler, dateThreshold));
            }
        };
        cache = CacheBuilder.newBuilder().maximumSize(65536).expireAfterWrite(30, TimeUnit.MINUTES).build(loader);
    }

    /**
     * Clears the cache.
     */
    public void invalidateCache() {
        cache.invalidateAll();
    }

    private ConfigAndHandler getConfigAndHandler(Session session) throws OXException {
        try {
            return cache.get(new Key(session));
        } catch (ExecutionException e) {
            throw ThreadPools.launderThrowable(e, OXException.class);
        }
    }

    @Override
    public boolean isNotEnabledFor(Session session) throws OXException {
        return false == isEnabledFor(session);
    }

    @Override
    public boolean isEnabledFor(Session session) throws OXException {
        return getConfigAndHandler(session).enabled;
    }

    @Override
    public long getDateThreshold(Session session) throws OXException {
        return getConfigAndHandler(session).dateThreshold;
    }

    @Override
    public MailAuthenticityHandler getHighestRankedHandlerFor(Session session) throws OXException {
        return getConfigAndHandler(session).highestRankedHandler;
    }

    @Override
    public Collection<MailAuthenticityHandler> getHandlers() throws OXException {
        return Collections.unmodifiableCollection(handlers);
    }

    // --------------------------------------------------------------------------------------------------------------

    @Override
    public MailAuthenticityHandler addingService(ServiceReference<MailAuthenticityHandler> reference) {
        MailAuthenticityHandler appearedHandler = context.getService(reference);
        if (handlers.offer(appearedHandler)) {
            invalidateCache();
            return appearedHandler;
        }
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<MailAuthenticityHandler> reference, MailAuthenticityHandler handler) {
        // Ignore
    }

    @Override
    public void removedService(ServiceReference<MailAuthenticityHandler> reference, MailAuthenticityHandler disappearedHandler) {
        boolean removed = handlers.remove(disappearedHandler);
        if (removed) {
            invalidateCache();
        }
        context.ungetService(reference);
    }

    // --------------------------------------------------------------------------------------------------------------

    private static class ConfigAndHandler {

        static final ConfigAndHandler NOT_ENABLED = new ConfigAndHandler(false, 0, null);

        final boolean enabled;
        final long dateThreshold;
        final MailAuthenticityHandler highestRankedHandler;

        ConfigAndHandler(long dateThreshold, MailAuthenticityHandler highestRankedHandler) {
            this(true, dateThreshold, highestRankedHandler);
        }

        private ConfigAndHandler(boolean enabled, long dateThreshold, MailAuthenticityHandler highestRankedHandler) {
            super();
            this.enabled = enabled;
            this.dateThreshold = dateThreshold;
            this.highestRankedHandler = highestRankedHandler;
        }
    }

    private static class Key {

        private final Session session; // Stored only for reference
        private final int contextId;
        private final int userId;
        private final int hash;

        /**
         * Initializes a new {@link Key}.
         */
        Key(Session session) {
            super();
            this.session = session;
            int userId = session.getUserId();
            int contextId = session.getContextId();
            this.contextId = contextId;
            this.userId = userId;
            int prime = 31;
            int result = prime * 1 + contextId;
            result = prime * result + userId;
            this.hash = result;
        }

        /**
         * Gets the session
         *
         * @return The session
         */
        public Session getSession() {
            return session;
        }

        /**
         * Gets the user identifier
         *
         * @return The user identifier
         */
        public int getUserId() {
            return userId;
        }

        /**
         * Gets the context identifier
         *
         * @return The context identifier
         */
        public int getContextId() {
            return contextId;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Key)) {
                return false;
            }
            Key other = (Key) obj;
            if (contextId != other.contextId) {
                return false;
            }
            if (userId != other.userId) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(32);
            builder.append("{contextId=").append(contextId).append(", userId=").append(userId).append("}");
            return builder.toString();
        }

    }

}
