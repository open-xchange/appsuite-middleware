/*-
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

package com.openexchange.ms.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicReference;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.openexchange.hazelcast.configuration.HazelcastConfigurationService;
import com.openexchange.hazelcast.serialization.CustomPortableFactory;
import com.openexchange.ms.MsEventConstants;
import com.openexchange.ms.MsService;
import com.openexchange.ms.PortableMsService;
import com.openexchange.ms.internal.HzMsService;
import com.openexchange.ms.internal.Services;
import com.openexchange.ms.internal.Unregisterer;
import com.openexchange.ms.internal.portable.PortableHzMsService;
import com.openexchange.ms.internal.portable.PortableMessageFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.timer.TimerService;

/**
 * {@link MsActivator} - The activator for <i>"com.openexchange.ms"</i> bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MsActivator extends HousekeepingActivator implements Unregisterer {

    private volatile ServiceTracker<HazelcastInstance, HazelcastInstance> hzTracker;

    /**
     * Initializes a new {@link MsActivator}.
     */
    public MsActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { HazelcastConfigurationService.class, TimerService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);
        Unregisterer.INSTANCE_REF.set(this);
        final HazelcastConfigurationService configService = getService(HazelcastConfigurationService.class);
        final boolean enabled = configService.isEnabled();
        if (enabled) {
            /*
             * create & register portable message factory
             */
            registerService(CustomPortableFactory.class, new PortableMessageFactory());
            /*
             * start ms services based on hazelcast instance's lifecycle
             */
            final BundleContext context = this.context;
            final AtomicReference<MsService> msServiceRef = new AtomicReference<MsService>();
            final AtomicReference<PortableMsService> portableMsServiceRef = new AtomicReference<PortableMsService>();
            ServiceTracker<HazelcastInstance, HazelcastInstance> hzTracker = new ServiceTracker<HazelcastInstance, HazelcastInstance>(context, HazelcastInstance.class, new ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance>() {

                @Override
                public HazelcastInstance addingService(final ServiceReference<HazelcastInstance> reference) {
                    if (msServiceRef.get() != null) {
                        return null;
                    }
                    // Get HazelcastInstance from service reference
                    final HazelcastInstance hz = context.getService(reference);
                    final HzMsService msService = new HzMsService(hz);
                    if (msServiceRef.compareAndSet(null, msService)) {
                        registerService(MsService.class, msService);
                        PortableMsService portableMsService = new PortableHzMsService(hz);
                        portableMsServiceRef.set(portableMsService);
                        registerService(PortableMsService.class, portableMsService);
                        registerEventHandler(msService);
                        return hz;
                    }
                    context.ungetService(reference);
                    return null;
                }

                @Override
                public void modifiedService(final ServiceReference<HazelcastInstance> reference, final HazelcastInstance service) {
                    // Ignore
                }

                @Override
                public void removedService(final ServiceReference<HazelcastInstance> reference, final HazelcastInstance service) {
                    if (null != service) {
                        final MsService msService = msServiceRef.get();
                        if (null != msService) {
                            unregisterServices();
                            msServiceRef.set(null);
                            portableMsServiceRef.set(null);
                        }
                        context.ungetService(reference);
                    }
                }
            });
            hzTracker.open();
            this.hzTracker = hzTracker;

            // Open other
            openTrackers();
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        ServiceTracker<HazelcastInstance, HazelcastInstance> hzTracker = this.hzTracker;
        if (null != hzTracker) {
            hzTracker.close();
            this.hzTracker = null;
        }

        super.stopBundle();
        Services.setServiceLookup(null);
        Unregisterer.INSTANCE_REF.set(null);
    }

    @Override
    public void unregisterMsService() {
        ServiceTracker<HazelcastInstance, HazelcastInstance> hzTracker = this.hzTracker;
        if (null != hzTracker) {
            hzTracker.close();
            this.hzTracker = null;
        }
    }

    @Override
    public void propagateNotActive(HazelcastInstanceNotActiveException notActiveException) {
        BundleContext context = this.context;
        if (null != context) {
            registerService(HazelcastInstanceNotActiveException.class, notActiveException);
        }
    }

    @Override
    public <S> void registerService(final Class<S> clazz, final S service) {
        super.registerService(clazz, service);
    }

    @Override
    public void unregisterServices() {
        super.unregisterServices();
    }

    /**
     * Registers the event handler.
     *
     * @param msService The associated service used to remotely re-publish received events.
     */
    protected void registerEventHandler(final HzMsService msService) {
        // Register event handler
        final Dictionary<String, Object> dict = new Hashtable<String, Object>(2);
        dict.put(EventConstants.EVENT_TOPIC, MsEventConstants.getAllTopics());
        registerService(EventHandler.class, new MsEventHandlerImpl(msService), dict);
    }

}
