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

package com.openexchange.mail.cache;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.caching.CacheService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ConfigurationServices;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.server.Initialization;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link MailCacheConfiguration} - Loads the configuration for mail caches.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailCacheConfiguration implements Initialization {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailCacheConfiguration.class);

    private static final MailCacheConfiguration instance = new MailCacheConfiguration();

    private final AtomicBoolean started;

    /**
     * No instantiation.
     */
    private MailCacheConfiguration() {
        super();
        started = new AtomicBoolean();
    }

    /**
     * Initializes the singleton instance of {@link MailCacheConfiguration}.
     *
     * @return The singleton instance of {@link MailCacheConfiguration}
     */
    public static MailCacheConfiguration getInstance() {
        return instance;
    }

    private void configure() throws OXException {
        final File cacheConfigFile = ServerServiceRegistry.getInstance().getService(ConfigurationService.class).getFileByName("mailcache.ccf");
        if (cacheConfigFile == null) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create("mailcache.ccf");
        }
        try {
            ServerServiceRegistry.getInstance().getService(CacheService.class).loadConfiguration(ConfigurationServices.loadPropertiesFrom(cacheConfigFile, true));
        } catch (IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void start() throws OXException {
        if (!started.compareAndSet(false, true)) {
            LOG.warn("{} has already been started. Aborting.", MailCacheConfiguration.class.getSimpleName());
        }
        configure();
    }

    @Override
    public void stop() {
        if (!started.compareAndSet(true, false)) {
            LOG.warn("{} has already been stopped. Aborting.", MailCacheConfiguration.class.getSimpleName());
        }
        final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (null != cacheService) {
            try {
                cacheService.freeCache(MailMessageCache.REGION_NAME);
            } catch (OXException e) {
                LOG.error("", e);
            }
        }
    }

    /**
     * Checks if mail cache configuration has been started, yet.
     *
     * @return <code>true</code> if mail cache configuration has been started; otherwise <code>false</code>
     */
    public boolean isStarted() {
        return started.get();
    }
}
