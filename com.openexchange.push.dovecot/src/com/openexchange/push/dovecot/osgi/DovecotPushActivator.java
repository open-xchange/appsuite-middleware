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

package com.openexchange.push.dovecot.osgi;

import org.osgi.framework.BundleContext;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.dovecot.doveadm.client.DoveAdmClient;
import com.openexchange.lock.LockService;
import com.openexchange.mail.MailProviderRegistration;
import com.openexchange.mail.service.MailService;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.pns.PushNotificationService;
import com.openexchange.push.PushListenerService;
import com.openexchange.push.dovecot.AbstractDovecotPushListener;
import com.openexchange.push.dovecot.DefaultRegistrationPerformer;
import com.openexchange.push.dovecot.DovecotPushConfiguration;
import com.openexchange.push.dovecot.registration.RegistrationPerformer;
import com.openexchange.push.dovecot.rest.DovecotPushRESTService;
import com.openexchange.push.dovecot.stateful.ClusterLockProvider;
import com.openexchange.session.ObfuscatorService;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;
import com.openexchange.user.UserService;


/**
 * {@link DovecotPushActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.2
 */
public class DovecotPushActivator extends HousekeepingActivator {

    private DovecotPushManagerLifecycle pushManagerLifecycle;

    /**
     * Initializes a new {@link DovecotPushActivator}.
     */
    public DovecotPushActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { DatabaseService.class, TimerService.class, MailService.class, ConfigurationService.class, ConfigViewFactory.class,
            SessiondService.class, ThreadPoolService.class, ContextService.class, UserService.class, PushListenerService.class, ObfuscatorService.class,
            LockService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);

        trackService(PushNotificationService.class);
        trackService(DoveAdmClient.class);
        trackService(MailAccountStorageService.class);

        AbstractDovecotPushListener.setIfHigherRanked(new DefaultRegistrationPerformer(this));

        track(RegistrationPerformer.class, new RegistrationPerformerTracker(context));

        DovecotPushConfiguration config = new DovecotPushConfiguration(getService(ConfigurationService.class));
        if (config.useStatelessImpl()) {
            StatelessDovecotRegisteringTracker registeringTracker = new StatelessDovecotRegisteringTracker(config, this, context);
            track(MailProviderRegistration.class, registeringTracker);
            this.pushManagerLifecycle = registeringTracker;
        } else {
            ClusterLockProvider lockProvider = ClusterLockProvider.newInstance(config, this);
            DovecotRegisteringTracker registeringTracker = new DovecotRegisteringTracker(config, lockProvider, this);
            track(registeringTracker.getFilter(), registeringTracker);
            this.pushManagerLifecycle = registeringTracker;
        }

        trackService(HazelcastInstance.class);
        openTrackers();

        registerService(DovecotPushRESTService.class, new DovecotPushRESTService(this));
    }

    @Override
    protected void stopBundle() throws Exception {
        if (pushManagerLifecycle != null) {
            pushManagerLifecycle.shutDown();
        }
        Services.setServiceLookup(null);
        super.stopBundle();
    }

    public BundleContext getBundleContext() {
        return context;
    }

}
