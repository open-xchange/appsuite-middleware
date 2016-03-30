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

package com.openexchange.mail.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.caching.CacheService;
import com.openexchange.config.ConfigurationService;
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
            ServerServiceRegistry.getInstance().getService(CacheService.class).loadConfiguration(new FileInputStream(cacheConfigFile));
        } catch (final FileNotFoundException e) {
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
            } catch (final OXException e) {
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
