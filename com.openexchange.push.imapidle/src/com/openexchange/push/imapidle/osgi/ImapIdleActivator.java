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

package com.openexchange.push.imapidle.osgi;

import com.hazelcast.core.HazelcastInstance;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.lock.LockService;
import com.openexchange.mail.service.MailService;
import com.openexchange.mailaccount.MailAccountDeleteListener;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.pns.PushNotificationService;
import com.openexchange.push.PushListenerService;
import com.openexchange.push.imapidle.ImapIdleConfiguration;
import com.openexchange.push.imapidle.ImapIdleDeleteListener;
import com.openexchange.push.imapidle.ImapIdleMailAccountDeleteListener;
import com.openexchange.push.imapidle.ImapIdlePushManagerService;
import com.openexchange.push.imapidle.locking.ImapIdleClusterLock;
import com.openexchange.session.ObfuscatorService;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessionstorage.SessionStorageService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;
import com.openexchange.user.UserService;


/**
 * {@link ImapIdleActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.1
 */
public class ImapIdleActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link ImapIdleActivator}.
     */
    public ImapIdleActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { DatabaseService.class, TimerService.class, MailService.class, ConfigurationService.class, SessiondService.class,
            ThreadPoolService.class, ContextService.class, UserService.class, PushListenerService.class, ConfigViewFactory.class };
    }

    @Override
    protected void startBundle() throws Exception {
        ImapIdleConfiguration configuration = new ImapIdleConfiguration();
        configuration.init(this);

        // Check if Hazelcast-based locking is enabled
        if (ImapIdleClusterLock.Type.HAZELCAST.equals(configuration.getClusterLock().getType())) {
            // Start tracking for Hazelcast
            ImapIdleRegisteringTracker registeringTracker = new ImapIdleRegisteringTracker(true, configuration, this, context);
            track(registeringTracker.getFilter(), registeringTracker);
        } else {
            // Register PushManagerService instance
            ImapIdleRegisteringTracker registeringTracker = new ImapIdleRegisteringTracker(false, configuration, this, context);
            track(registeringTracker.getFilter(), registeringTracker);
            trackService(HazelcastInstance.class);
        }
        trackService(PushNotificationService.class);
        trackService(SessionStorageService.class);
        trackService(LockService.class);
        trackService(ObfuscatorService.class);
        openTrackers();

        registerService(MailAccountDeleteListener.class, new ImapIdleMailAccountDeleteListener());
        registerService(DeleteListener.class, new ImapIdleDeleteListener());
    }

    @Override
    protected void stopBundle() throws Exception {
        stopPushManagerSafe();
        super.stopBundle();
    }

    @Override
    public <S> boolean addService(Class<S> clazz, S service) {
        return super.addService(clazz, service);
    }

    @Override
    public <S> boolean removeService(Class<? extends S> clazz) {
        return super.removeService(clazz);
    }

    /**
     * Stops the push manager.
     */
    static void stopPushManagerSafe() {
        ImapIdlePushManagerService pushManager = ImapIdlePushManagerService.getInstance();
        if (null != pushManager) {
            pushManager.stopAllListeners();
        }
    }

}
