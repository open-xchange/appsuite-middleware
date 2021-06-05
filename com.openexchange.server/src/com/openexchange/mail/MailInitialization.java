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

import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.cache.registry.CacheAvailabilityListener;
import com.openexchange.cache.registry.CacheAvailabilityRegistry;
import com.openexchange.exception.OXException;
import com.openexchange.mail.cache.EnqueueingMailAccessCache;
import com.openexchange.mail.cache.MailCacheConfiguration;
import com.openexchange.mail.cache.MailMessageCache;
import com.openexchange.mail.cache.SingletonMailAccessCache;
import com.openexchange.mail.config.MailPropertiesInit;
import com.openexchange.mail.event.EventPool;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.server.Initialization;

/**
 * {@link MailInitialization} - Initializes whole mail implementation and therefore provides a central point for starting/stopping mail
 * implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailInitialization implements Initialization, CacheAvailabilityListener {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailInitialization.class);

    private static final MailInitialization INSTANCE = new MailInitialization();

    private final AtomicBoolean started;

    /**
     * No instantiation
     */
    private MailInitialization() {
        super();
        started = new AtomicBoolean();
    }

    /**
     * @return The singleton instance of {@link MailInitialization}
     */
    public static MailInitialization getInstance() {
        return INSTANCE;
    }

    @Override
    public void start() throws OXException {
        if (!started.compareAndSet(false, true)) {
            return;
        }
        final Stack<Initialization> startedStack = new Stack<Initialization>();
        try {
            /*
             * Start global mail system
             */
            startUp(MailPropertiesInit.getInstance(), startedStack);
            startUp(MailCacheConfiguration.getInstance(), startedStack);
            startUp(new Initialization() {

                @Override
                public void start() throws OXException {
                    MailAccessWatcher.init();
                }

                @Override
                public void stop() {
                    MailAccessWatcher.stop();
                }
            }, startedStack);
//            startUp(new Initialization() {
//
//                public void start() throws AbstractOXException {
//                    JSONMessageCache.initInstance();
//                }
//
//                public void stop() {
//                    JSONMessageCache.releaseInstance();
//                }
//            }, startedStack);
            startUp(new Initialization() {

                @Override
                public void start() throws OXException {
                    EventPool.initInstance();
                }

                @Override
                public void stop() {
                    EventPool.releaseInstance();
                }
            }, startedStack);
            startUp(new Initialization() {

                @Override
                public void start() {
                    MailcapInitialization.getInstance().init();
                }

                @Override
                public void stop() {
                    // Nope
                }
            }, startedStack);
            // Ensure storage instance is initialized during start-up
            UserSettingMailStorage.getInstance();
            /*
             * Add to cache availability registry
             */
            final CacheAvailabilityRegistry reg = CacheAvailabilityRegistry.getInstance();
            if (null != reg) {
                reg.registerListener(this);
                reg.registerListener(UserSettingMailStorage.getInstance());
            }
        } catch (OXException e) {
            started.set(false);
            // Revoke
            for (Initialization startedInit : startedStack) {
                try {
                    startedInit.stop();
                } catch (Exception e1) {
                    LOG.error("Initialization could not be revoked", e1);
                }
            }
            throw e;
        }
    }

    private void startUp(Initialization initialization, Stack<Initialization> startedStack) throws OXException {
        initialization.start();
        startedStack.push(initialization);
    }

    @Override
    public void stop() {
        if (!started.compareAndSet(true, false)) {
            LOG.warn("Duplicate shut-down of mail module aborted.");
            return;
        }
        /*
         * TODO: Remove Simulate bundle disappearance
         */
        // MailProvider.resetMailProvider();
        /*
         * Remove from cache availability registry
         */
        final CacheAvailabilityRegistry reg = CacheAvailabilityRegistry.getInstance();
        if (null != reg) {
            reg.unregisterListener(this);
            reg.unregisterListener(UserSettingMailStorage.getInstance());
        }
        /*
         * Stop global mail system
         */
        EventPool.releaseInstance();
        MimeType2ExtMap.reset();
        EnqueueingMailAccessCache.releaseInstance();
        SingletonMailAccessCache.releaseInstance();
        MailMessageCache.releaseInstance();
        UserSettingMailStorage.releaseInstance();
        MailAccessWatcher.stop();
        MailCacheConfiguration.getInstance().stop();
        MailPropertiesInit.getInstance().stop();
    }

    /**
     * Handles the (possibly temporary) unavailability of caching service
     *
     * @throws AbstractOXException If mail caches shut-down fails
     */
    public void shutDownCaches() throws OXException {
        MailMessageCache.getInstance().releaseCache();
        MailCacheConfiguration.getInstance().stop();
    }

    /**
     * Handles the re-availability of caching service
     *
     * @throws AbstractOXException If mail caches start-up fails
     */
    public void startUpCaches() throws OXException {
        MailCacheConfiguration.getInstance().start();
        MailMessageCache.getInstance().initCache();
    }

    @Override
    public void handleAbsence() throws OXException {
        shutDownCaches();
    }

    @Override
    public void handleAvailability() throws OXException {
        startUpCaches();
    }

    public boolean isInitialized() {
        return started.get();
    }
}
