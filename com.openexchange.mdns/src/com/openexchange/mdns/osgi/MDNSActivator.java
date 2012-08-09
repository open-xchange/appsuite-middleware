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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.mdns.osgi;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.mdns.MDNSService;
import com.openexchange.mdns.MDNSServiceInfo;
import com.openexchange.mdns.internal.MDNSCommandProvider;
import com.openexchange.mdns.internal.MDNSServiceImpl;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;

/**
 * {@link MDNSActivator} - The mDNS activator.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MDNSActivator extends HousekeepingActivator {

    volatile MDNSServiceImpl mdnsService;

    final AtomicReference<MDNSServiceInfo> serviceInfoReference;

    /**
     * Initializes a new {@link MDNSActivator}.
     */
    public MDNSActivator() {
        super();
        serviceInfoReference = new AtomicReference<MDNSServiceInfo>();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ThreadPoolService.class };
    }

    @Override
    public <S> void registerService(Class<S> clazz, S service) {
        super.registerService(clazz, service);
    }

    @Override
    public <S> ServiceTracker<S, S> track(Class<S> clazz, SimpleRegistryListener<S> listener) {
        return super.track(clazz, listener);
    }

    @Override
    public <S> boolean addService(Class<S> clazz, S service) {
        return super.addService(clazz, service);
    }

    @Override
    public <S> boolean removeService(Class<? extends S> clazz) {
        return super.removeService(clazz);
    }

    @Override
    public void openTrackers() {
        super.openTrackers();
    }

    @Override
    protected void startBundle() throws Exception {
        final Log log = com.openexchange.log.Log.valueOf(LogFactory.getLog(MDNSActivator.class));
        final ThreadPoolService threadPoolService = getService(ThreadPoolService.class);
        log.info("Starting bundle: com.openexchange.mdns");
        final Runnable starter = new Runnable() {

            @Override
            public void run() {
                try {
                    /*
                     * Create mDNS service
                     */
                    final MDNSServiceImpl mdnsService = new MDNSServiceImpl();
                    MDNSActivator.this.mdnsService = mdnsService;
                    registerService(MDNSService.class, mdnsService);
                    registerService(CommandProvider.class, new MDNSCommandProvider(mdnsService));

                    track(ThreadPoolService.class, new SimpleRegistryListener<ThreadPoolService>() {

                        @Override
                        public void added(final ServiceReference<ThreadPoolService> ref, final ThreadPoolService service) {
                            final Task<Void> task = new AbstractTask<Void>() {

                                @Override
                                public Void call() throws Exception {
                                    final String serviceId = "openexchange.service.lookup";
                                    final int port = 6666;
                                    final String info = new StringBuilder("open-xchange lookup service @").append(getHostName()).toString();
                                    serviceInfoReference.set(mdnsService.registerService(serviceId, port, info));
                                    log.info("MDNS Lookup Service successfully registered.");
                                    return null;
                                }
                            };
                            service.submit(task, CallerRunsBehavior.<Void> getInstance());
                            addService(ThreadPoolService.class, service);
                        }

                        @Override
                        public void removed(final ServiceReference<ThreadPoolService> ref, final ThreadPoolService service) {
                            removeService(ThreadPoolService.class);
                        }
                    });
                    openTrackers();
                } catch (final Exception e) {
                    log.error("Starting bundle failed: com.openexchange.mdns", e);
                }
            }
        };
        threadPoolService.submit(ThreadPools.task(starter));
    }

    protected String getHostName() {
        try {
            return InetAddress.getLocalHost().getCanonicalHostName();
        } catch (final UnknownHostException e) {
            return "<unknown>";
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        final Log log = com.openexchange.log.Log.valueOf(LogFactory.getLog(MDNSActivator.class));
        log.info("Stopping bundle: com.openexchange.mdns");
        try {
            unregisterServices();
            final MDNSServiceImpl mdnsService = this.mdnsService;
            if (mdnsService != null) {
                final MDNSServiceInfo serviceInfo = serviceInfoReference.get();
                if (null != serviceInfo) {
                    mdnsService.unregisterService(serviceInfo);
                    serviceInfoReference.set(null);
                }
                mdnsService.close();
                this.mdnsService = null;
            }
            super.stopBundle();
        } catch (final Exception e) {
            log.error("Stopping bundle failed: com.openexchange.mdns", e);
            throw e;
        }
    }

}
