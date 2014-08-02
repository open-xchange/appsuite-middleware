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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.push.imapidlev2.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.hazelcast.configuration.HazelcastConfigurationService;
import com.openexchange.mail.service.MailService;
import com.openexchange.mailaccount.MailAccountDeleteListener;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.push.PushManagerService;
import com.openexchange.push.imapidlev2.ImapIdleConfiguration;
import com.openexchange.push.imapidlev2.ImapIdleDeleteListener;
import com.openexchange.push.imapidlev2.ImapIdleMailAccountDeleteListener;
import com.openexchange.push.imapidlev2.ImapIdlePushManagerService;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;


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
        return new Class<?>[] { DatabaseService.class, TimerService.class, MailService.class, ConfigurationService.class, HazelcastConfigurationService.class, SessiondService.class, ThreadPoolService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        HazelcastConfigurationService hzConfigService = getService(HazelcastConfigurationService.class);

        final boolean hzEnabled = hzConfigService.isEnabled();
        if (hzEnabled) {
            final BundleContext context = this.context;
            ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance> stc = new ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance>() {

                @Override
                public HazelcastInstance addingService(ServiceReference<HazelcastInstance> reference) {
                    HazelcastInstance hzInstance = context.getService(reference);
                    addService(HazelcastInstance.class, hzInstance);
                    return hzInstance;
                }

                @Override
                public void modifiedService(ServiceReference<HazelcastInstance> reference, HazelcastInstance service) {
                    // Nothing
                }

                @Override
                public void removedService(ServiceReference<HazelcastInstance> reference, HazelcastInstance service) {
                    removeService(HazelcastInstance.class);
                    context.ungetService(reference);
                }
            };
            track(HazelcastInstance.class, stc);
            openTrackers();
        }

        ImapIdleConfiguration configuration = new ImapIdleConfiguration();
        configuration.init(this);
        registerService(PushManagerService.class, ImapIdlePushManagerService.newInstance(configuration, this));

        registerService(MailAccountDeleteListener.class, new ImapIdleMailAccountDeleteListener());
        registerService(DeleteListener.class, new ImapIdleDeleteListener());
    }

    @Override
    public <S> boolean addService(Class<S> clazz, S service) {
        return super.addService(clazz, service);
    }

    @Override
    public <S> boolean removeService(Class<? extends S> clazz) {
        return super.removeService(clazz);
    }

}
