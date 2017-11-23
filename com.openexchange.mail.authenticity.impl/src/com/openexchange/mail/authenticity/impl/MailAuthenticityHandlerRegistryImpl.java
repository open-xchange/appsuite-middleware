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

package com.openexchange.mail.authenticity.impl;

import java.util.List;
import java.util.concurrent.TimeUnit;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.mail.authenticity.MailAuthenticityHandler;
import com.openexchange.mail.authenticity.MailAuthenticityHandlerRegistry;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.session.Session;
import com.openexchange.session.UserAndContext;

/**
 * {@link MailAuthenticityHandlerRegistryImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class MailAuthenticityHandlerRegistryImpl implements MailAuthenticityHandlerRegistry {

    private final ServiceListing<MailAuthenticityHandler> listing;
    private final LeanConfigurationService leanConfigService;
    private final Cache<UserAndContext, Config> configCache;

    /**
     * Initializes a new {@link MailAuthenticityHandlerRegistryImpl}.
     */
    public MailAuthenticityHandlerRegistryImpl(ServiceListing<MailAuthenticityHandler> listing, LeanConfigurationService leanConfigService) {
        super();
        this.listing = listing;
        this.leanConfigService = leanConfigService;
        configCache = CacheBuilder.newBuilder().maximumSize(65536).expireAfterWrite(30, TimeUnit.MINUTES).build();
    }

    /**
     * Clears the config cache.
     */
    public void invalidateCache() {
        configCache.invalidateAll();
    }

    private Config getConfig(Session session) {
        int userId = session.getUserId();
        int contextId = session.getContextId();
        UserAndContext key = UserAndContext.newInstance(userId, contextId);
        Config config = configCache.getIfPresent(key);
        if (null == config) {
            boolean enabled = leanConfigService.getBooleanProperty(userId, contextId, MailAuthenticityProperty.enabled);
            long dateThreshold = leanConfigService.getLongProperty(userId, contextId, MailAuthenticityProperty.threshold);
            config = new Config(enabled, dateThreshold);
            configCache.put(key, config);
        }
        return config;
    }

    @Override
    public boolean isNotEnabledFor(Session session) throws OXException {
        return false == isEnabledFor(session);
    }

    @Override
    public boolean isEnabledFor(Session session) throws OXException {
        return getConfig(session).enabled;
    }

    @Override
    public long getDateThreshold(Session session) throws OXException {
        return getConfig(session).dateThreshold;
    }

    @Override
    public MailAuthenticityHandler getHighestRankedHandlerFor(Session session) throws OXException {
        if (isNotEnabledFor(session)) {
            // Disabled per configuration
            return null;
        }

        List<MailAuthenticityHandler> snapshot = listing.getServiceList();
        if (snapshot == null || snapshot.isEmpty()) {
            // None registered
            return null;
        }

        long threshold = getDateThreshold(session);
        MailAuthenticityHandler highestRankedHandler = null;
        for (MailAuthenticityHandler handler : snapshot) {
            if (handler.isEnabled(session) && (null == highestRankedHandler || highestRankedHandler.getRanking() < handler.getRanking())) {
                highestRankedHandler = new ThresholdAwareAuthenticityHandler(handler, threshold);
            }
        }

        return highestRankedHandler;
    }

    // --------------------------------------------------------------------------------------------------------------

    private static class Config {

        final boolean enabled;
        final long dateThreshold;

        Config(boolean enabled, long dateThreshold) {
            super();
            this.enabled = enabled;
            this.dateThreshold = dateThreshold;
        }
    }

}
