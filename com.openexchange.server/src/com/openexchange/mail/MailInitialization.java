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
        } catch (final OXException e) {
            started.set(false);
            // Revoke
            for (final Initialization startedInit : startedStack) {
                try {
                    startedInit.stop();
                } catch (final Exception e1) {
                    LOG.error("Initialization could not be revoked", e1);
                }
            }
            throw e;
        }
    }

    private void startUp(final Initialization initialization, final Stack<Initialization> startedStack) throws OXException {
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
