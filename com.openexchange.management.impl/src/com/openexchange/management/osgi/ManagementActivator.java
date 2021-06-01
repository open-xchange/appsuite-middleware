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

package com.openexchange.management.osgi;

import static com.openexchange.management.services.ManagementServiceRegistry.getServiceRegistry;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.management.ManagementService;
import com.openexchange.management.internal.ManagementAgentImpl;
import com.openexchange.management.internal.ManagementInit;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.password.mechanism.PasswordMechRegistry;

/**
 * {@link ManagementActivator} - Activator for management bundle
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ManagementActivator extends HousekeepingActivator {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ManagementActivator.class);

    Thread jmxStarter;
    Thread jmxStartPoller;

    /**
     * Initializes a new {@link ManagementActivator}
     */
    public ManagementActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, SSLSocketFactoryProvider.class, PasswordMechRegistry.class };
    }

    @Override
    protected synchronized void handleAvailability(final Class<?> clazz) {
        LOG.info("Re-available service: {}", clazz.getName());
        getServiceRegistry().addService(clazz, getService(clazz));
        /*
         * TODO: Should the management bundle be restarted due to re-available configuration service?
         */
        /**
         * <pre>
         * stopInternal();
         * startInternal();
         * </pre>
         */
    }

    @Override
    protected synchronized void handleUnavailability(final Class<?> clazz) {
        LOG.info("Re-available service: {}", clazz.getName());
        /*
         * Just remove absent service from service registry but do not stop management bundle
         */
        getServiceRegistry().removeService(clazz);
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        LOG.info("starting bundle: com.openexchange.management");
        /*
         * Fill service registry
         */
        {
            final ServiceRegistry registry = getServiceRegistry();
            registry.clearRegistry();
            final Class<?>[] classes = getNeededServices();
            for (final Class<?> classe : classes) {
                final Object service = getService(classe);
                if (null != service) {
                    registry.addService(classe, service);
                }
            }
        }
        /*
         * Check if JMX server should be started asynchronously
         */
        boolean async = getService(ConfigurationService.class).getBoolProperty("com.openexchange.management.asyncStartUp", false);
        if (async) {
            /*
             * Schedule JMX server start
             */
            final Object monitor = this;
            final FutureTask<Void> jmxStart = new FutureTask<Void>(new Callable<Void>() {

                @Override
                public Void call() throws OXException {
                    startInternal();
                    synchronized (monitor) {
                        ManagementActivator.this.jmxStarter = null;
                    }
                    return null;
                }
            });
            Thread jmxStarter = new Thread(jmxStart, "Open-Xchange JMX Starter");
            this.jmxStarter = jmxStarter;
            jmxStarter.start();
            /*
             * Poll for finished JMX server start-up
             */
            Runnable poll = new Runnable() {

                @Override
                public void run() {
                    boolean keepOn = true;
                    while (keepOn) {
                        keepOn = false;
                        try {
                            jmxStart.get(5, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            // Interrupted
                            LOG.trace("Polling has been interrupted", e);
                        } catch (ExecutionException e) {
                            String ls = Strings.getLineSeparator();
                            LOG.warn("{}{}\tFailed to start JMX server. MBeans will not be accessible on this node.{}", ls, ls, ls, e.getCause());
                        } catch (TimeoutException e) {
                            // Not started in time
                            String ls = Strings.getLineSeparator();
                            LOG.warn("{}{}\tJMX server still not started. MBeans will not yet be accessible on this node.{}", ls, ls, ls, e.getCause());
                            keepOn = true;
                        }
                    }
                    synchronized (monitor) {
                        ManagementActivator.this.jmxStartPoller = null;
                    }
                }
            };
            Thread jmxStartPoller = new Thread(poll, "Open-Xchange JMX Start Poller");
            this.jmxStartPoller = jmxStartPoller;
            jmxStartPoller.start();
        } else {
            startInternal();
        }
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        LOG.info("stopping bundle: com.openexchange.management");
        super.stopBundle();
        Thread jmxStartPoller = this.jmxStartPoller;
        if (null != jmxStartPoller) {
            // Interrupt thread and wait
            this.jmxStartPoller = null;
            jmxStartPoller.interrupt();
        }
        Thread jmxStarter = this.jmxStarter;
        if (null != jmxStarter) {
            // Interrupt thread and wait
            this.jmxStarter = null;
            jmxStarter.interrupt();
        }
        stopInternal();
        /*
         * Clear service registry
         */
        getServiceRegistry().clearRegistry();
    }

    void startInternal() throws OXException {
        ManagementInit.getInstance().start();
        if (ManagementInit.getInstance().isStarted()) {
            /*
             * Register management service
             */
            registerService(ManagementService.class, ManagementAgentImpl.getInstance(), null);
            registerService(ThreadMXBean.class, ManagementAgentImpl.getInstance().getThreadMXBean(), null);
        }
    }

    private void stopInternal() throws OXException {
        if (ManagementInit.getInstance().isStarted()) {
            ManagementInit.getInstance().stop();
        }
    }
}
