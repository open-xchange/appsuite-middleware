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

package com.openexchange.push.imapidle.osgi;

import org.osgi.framework.ServiceReference;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.mail.service.MailService;
import com.openexchange.mailaccount.MailAccountDeleteListener;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.pns.PushNotificationService;
import com.openexchange.push.PushListenerService;
import com.openexchange.push.imapidle.ImapIdleConfiguration;
import com.openexchange.push.imapidle.ImapIdleDeleteListener;
import com.openexchange.push.imapidle.ImapIdleMailAccountDeleteListener;
import com.openexchange.push.imapidle.ImapIdlePushManagerService;
import com.openexchange.push.imapidle.locking.ImapIdleClusterLock;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessionstorage.SessionStorageService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserPermissionService;


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
            ThreadPoolService.class, ContextService.class, UserService.class, PushListenerService.class };
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

        final ImapIdleDeleteListener imapIdleDeleteListener = new ImapIdleDeleteListener();
        track(UserPermissionService.class, new SimpleRegistryListener<UserPermissionService>() {

            @Override
            public void added(ServiceReference<UserPermissionService> ref, UserPermissionService service) {
                imapIdleDeleteListener.setUserPermissionService(service);
            }

            @Override
            public void removed(ServiceReference<UserPermissionService> ref, UserPermissionService service) {
                imapIdleDeleteListener.setUserPermissionService(null);
            }
        });
        trackService(UserPermissionService.class);
        openTrackers();

        registerService(DeleteListener.class, imapIdleDeleteListener);
        registerService(MailAccountDeleteListener.class, new ImapIdleMailAccountDeleteListener());
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
