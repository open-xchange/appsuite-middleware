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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.http.grizzly.osgi;

import java.util.concurrent.ExecutorService;
import org.apache.commons.logging.Log;
import org.glassfish.grizzly.comet.CometAddOn;
import org.glassfish.grizzly.http.ajp.AjpAddOn;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.OXHttpServer;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.service.http.HttpService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.http.grizzly.GrizzlyConfig;
import com.openexchange.http.grizzly.GrizzlyExceptionCode;
import com.openexchange.http.grizzly.service.comet.CometContextService;
import com.openexchange.http.grizzly.service.comet.impl.CometContextServiceImpl;
import com.openexchange.http.grizzly.service.http.HttpServiceFactory;
import com.openexchange.http.grizzly.threadpool.GrizzlOXExecutorService;
import com.openexchange.http.requestwatcher.osgi.services.RequestWatcherService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;

/**
 * {@link GrizzlyActivator}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class GrizzlyActivator extends HousekeepingActivator {

    private volatile OXHttpServer grizzly;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { ConfigurationService.class, RequestWatcherService.class, ThreadPoolService.class, TimerService.class };
    }

    @Override
    protected void startBundle() throws OXException {
        final org.apache.commons.logging.Log log = com.openexchange.log.Log.loggerFor(GrizzlyActivator.class);
        try {
            Services.setServiceLookup(this);

            if (log.isInfoEnabled()) {
                log.info("Starting Grizzly server.");
            }
            context.addFrameworkListener(new FrameworkListener() {

                @Override
                public void frameworkEvent(FrameworkEvent event) {
                    if (event.getBundle().getSymbolicName().equalsIgnoreCase("com.openexchange.http.grizzly")) {
                        int eventType = event.getType();
                        if (eventType == FrameworkEvent.ERROR) {
                            log.error(event.toString(), event.getThrowable());
                        } else {
                            log.info(event.toString(), event.getThrowable());
                        }
                    }
                }
            });

            GrizzlyConfig grizzlyConfig = GrizzlyConfig.getInstance();
            grizzlyConfig.start();

            /*
             * create, configure and start server
             */
            final OXHttpServer grizzly = new OXHttpServer();
            this.grizzly = grizzly;

            ServerConfiguration serverConfiguration = grizzly.getServerConfiguration();
            serverConfiguration.setMaxRequestParameters(grizzlyConfig.getMaxRequestParameters());

            final NetworkListener networkListener = new NetworkListener("http-listener", grizzlyConfig.getHttpHost(), grizzlyConfig.getHttpPort());

            /*
             * Set the maximum size of the PUT/POST body generated by an HTML form to honor "com.openexchange.servlet.maxBodySize"
             */
            int maxBodySize = grizzlyConfig.getMaxBodySize();
            networkListener.setMaxFormPostSize(maxBodySize);
            networkListener.setMaxBufferedPostSize(maxBodySize);

            if (grizzlyConfig.isAJPEnabled()) {
                networkListener.registerAddOn(new AjpAddOn());
                if (log.isInfoEnabled()) {
                    log.info("Enabled AJP for Grizzly server.");
                }
            }

            TCPNIOTransport configuredTcpNioTransport = buildTcpNioTransport();
            networkListener.setTransport(configuredTcpNioTransport);

            if (grizzlyConfig.isJMXEnabled()) {
                grizzly.getServerConfiguration().setJmxEnabled(true);
                if (log.isInfoEnabled()) {
                    log.info("Enabled JMX for Grizzly server.");
                }
            }

            if (grizzlyConfig.isWebsocketsEnabled()) {
                networkListener.registerAddOn(new WebSocketAddOn());
                if (log.isInfoEnabled()) {
                    log.info("Enabled WebSockets for Grizzly server.");
                }
            }

            if (grizzlyConfig.isCometEnabled()) {
                networkListener.registerAddOn(new CometAddOn());
                registerService(CometContextService.class, new CometContextServiceImpl());
                if (log.isInfoEnabled()) {
                    log.info("Enabled Comet for Grizzly server.");
                }
            }

            grizzly.addListener(networkListener);
            grizzly.start();
            if (log.isInfoEnabled()) {
                log.info(String.format("Registered Grizzly HttpNetworkListener on host: %s and port: %s", grizzlyConfig.getHttpHost(), Integer.valueOf(grizzlyConfig.getHttpPort())));
            }

            /*
             * Servicefactory that creates instances of the HttpService interface that grizzly implements. Each distinct bundle that uses
             * getService() will get its own instance of HttpServiceImpl
             */
            registerService(HttpService.class.getName(), new HttpServiceFactory(grizzly, context.getBundle()));
            if (log.isInfoEnabled()) {
                log.info("Registered OSGi HttpService for Grizzly server.");
            }

        } catch (final Exception e) {
            throw GrizzlyExceptionCode.GRIZZLY_SERVER_NOT_STARTED.create(e, new Object[] {});
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        final Log log = com.openexchange.log.Log.loggerFor(GrizzlyActivator.class);

        Services.setServiceLookup(null);

        if (log.isInfoEnabled()) {
            log.info("Unregistering services.");
        }
        cleanUp();

        if (log.isInfoEnabled()) {
            log.info("Stopping Grizzly.");
        }
        final OXHttpServer grizzly = this.grizzly;
        if (null != grizzly) {
            grizzly.stop();
            this.grizzly = null;
        }
    }

    /**
     * Build a TCPNIOTransport using {c.o}.threadpool
     *
     * @return The configure TCPNIOTransport
     * @throws OXException If the Transport can't be build
     */
    private TCPNIOTransport buildTcpNioTransport() throws OXException {
        ThreadPoolService threadPoolService = getService(ThreadPoolService.class);
        if (threadPoolService == null) {
            throw GrizzlyExceptionCode.NEEDED_SERVICE_MISSING.create(ThreadPoolService.class.getSimpleName());
        }
        TCPNIOTransportBuilder builder = TCPNIOTransportBuilder.newInstance();
        final TCPNIOTransport transport = builder.build();
        ExecutorService executor = GrizzlOXExecutorService.createInstance();
        transport.setWorkerThreadPool(executor);
        return transport;
    }

}
