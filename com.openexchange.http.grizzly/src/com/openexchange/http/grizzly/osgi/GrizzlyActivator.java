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

package com.openexchange.http.grizzly.osgi;

import java.util.concurrent.ExecutorService;
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
import com.openexchange.config.Reloadable;
import com.openexchange.exception.OXException;
import com.openexchange.http.grizzly.GrizzlyConfig;
import com.openexchange.http.grizzly.GrizzlyExceptionCode;
import com.openexchange.http.grizzly.service.comet.CometContextService;
import com.openexchange.http.grizzly.service.comet.impl.CometContextServiceImpl;
import com.openexchange.http.grizzly.service.http.HttpServiceFactory;
import com.openexchange.http.grizzly.service.websocket.WebApplicationService;
import com.openexchange.http.grizzly.service.websocket.impl.WebApplicationServiceImpl;
import com.openexchange.http.grizzly.threadpool.GrizzlOXExecutorService;
import com.openexchange.http.requestwatcher.osgi.services.RequestWatcherService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.startup.SignalStartedService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;

/**
 * {@link GrizzlyActivator}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class GrizzlyActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { ConfigurationService.class, RequestWatcherService.class, ThreadPoolService.class, TimerService.class };
    }

    @Override
    protected void startBundle() throws OXException {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GrizzlyActivator.class);
        try {
            Services.setServiceLookup(this);

            log.info("Starting Grizzly server.");
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

            final GrizzlyConfig grizzlyConfig = GrizzlyConfig.getInstance();
            grizzlyConfig.start();

            /*
             * create, configure and start server
             */
            final OXHttpServer grizzly = new OXHttpServer();

            ServerConfiguration serverConfiguration = grizzly.getServerConfiguration();
            serverConfiguration.setMaxRequestParameters(grizzlyConfig.getMaxRequestParameters());
            serverConfiguration.setAllowPayloadForUndefinedHttpMethods(true);

            final NetworkListener networkListener = new NetworkListener("http-listener", grizzlyConfig.getHttpHost(), grizzlyConfig.getHttpPort());

            /*
             * Set the maximum size of the PUT/POST body generated by an HTML form to honor "com.openexchange.servlet.maxBodySize"
             */
            int maxBodySize = grizzlyConfig.getMaxBodySize();
            networkListener.setMaxFormPostSize(maxBodySize);
            networkListener.setMaxBufferedPostSize(maxBodySize);

            if (grizzlyConfig.isAJPEnabled()) {
                networkListener.registerAddOn(new AjpAddOn());
                log.info("Enabled AJP for Grizzly server.");
            }

            // Set the transport
            {
                TCPNIOTransport configuredTcpNioTransport = buildTcpNioTransport();
                networkListener.setTransport(configuredTcpNioTransport);
            }

            // Configure keep-alive
            /*-
             * Keep default settings for now.
             * Otherwise alter KeepAlive instance.
             *
             * {
             *     final KeepAlive keepAlive = networkListener.getKeepAlive();
             *     keepAlive.setIdleTimeoutInSeconds(-1);
             *     keepAlive.setMaxRequestsCount(-1);
             * }
             *
             */

            if (grizzlyConfig.isJMXEnabled()) {
                grizzly.getServerConfiguration().setJmxEnabled(true);
                log.info("Enabled JMX for Grizzly server.");
            }

            if (grizzlyConfig.isWebsocketsEnabled()) {
                networkListener.registerAddOn(new WebSocketAddOn());
                registerService(WebApplicationService.class, new WebApplicationServiceImpl());
                log.info("Enabled WebSockets for Grizzly server.");
            }

            if (grizzlyConfig.isCometEnabled()) {
                networkListener.registerAddOn(new CometAddOn());
                registerService(CometContextService.class, new CometContextServiceImpl());
                log.info("Enabled Comet for Grizzly server.");
            }

            grizzly.addListener(networkListener);
            grizzly.start();
            log.info("Prepared Grizzly HttpNetworkListener on host: {} and port: {}, but not yet started...", grizzlyConfig.getHttpHost(), Integer.valueOf(grizzlyConfig.getHttpPort()));

            /*
             * Servicefactory that creates instances of the HttpService interface that grizzly implements. Each distinct bundle that uses
             * getService() will get its own instance of HttpServiceImpl
             */
            registerService(HttpService.class.getName(), new HttpServiceFactory(grizzly, context.getBundle()));
            log.info("Registered OSGi HttpService for Grizzly server.");

            registerService(Reloadable.class, grizzlyConfig);

            // Finally start listeners if server start-up is completed
            track(SignalStartedService.class, new StartUpTracker(grizzly, grizzlyConfig, context));
            openTrackers();
        } catch (final Exception e) {
            throw GrizzlyExceptionCode.GRIZZLY_SERVER_NOT_STARTED.create(e, new Object[] {});
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GrizzlyActivator.class);

        Services.setServiceLookup(null);

        log.info("Unregistering services.");
        super.stopBundle();
    }

    /**
     * Build a TCPNIOTransport using {c.o}.threadpool
     *
     * @return The configure TCPNIOTransport
     * @throws OXException If the Transport can't be build
     */
    private TCPNIOTransport buildTcpNioTransport() throws OXException {
        final ThreadPoolService threadPoolService = getService(ThreadPoolService.class);
        if (threadPoolService == null) {
            throw GrizzlyExceptionCode.NEEDED_SERVICE_MISSING.create(ThreadPoolService.class.getSimpleName());
        }
        final TCPNIOTransportBuilder builder = TCPNIOTransportBuilder.newInstance().setKeepAlive(true).setTcpNoDelay(true);
        final TCPNIOTransport transport = builder.build();
        final ExecutorService executor = GrizzlOXExecutorService.createInstance();
        transport.setWorkerThreadPool(executor);
        return transport;
    }

}
