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
import com.openexchange.http.grizzly.service.atmosphere.AtmosphereService;
import com.openexchange.http.grizzly.service.atmosphere.AtmosphereServiceImpl;
import com.openexchange.http.grizzly.service.http.HttpServiceFactory;
import com.openexchange.http.grizzly.threadpool.GrizzlOXExecutorService;
import com.openexchange.http.requestwatcher.osgi.services.RequestWatcherService;
import com.openexchange.log.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;

/**
 * {@link GrizzlyActivator}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class GrizzlyActivator extends HousekeepingActivator {

    private static final org.apache.commons.logging.Log LOG = Log.valueOf(LogFactory.getLog(GrizzlyActivator.class));

    private OXHttpServer grizzly;

    private HttpServiceFactory serviceFactory;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { ConfigurationService.class, RequestWatcherService.class, ThreadPoolService.class, TimerService.class };
    }

    @Override
    protected void startBundle() throws OXException {
        try {
            if (LOG.isInfoEnabled()) {
                LOG.info("Starting Grizzly server.");
            }
            context.addFrameworkListener(new FrameworkListener() {

                @Override
                public void frameworkEvent(FrameworkEvent event) {
                    if (event.getBundle().getSymbolicName().equalsIgnoreCase("com.openexchange.http.grizzly")) {
                        int eventType = event.getType();
                        if (eventType == FrameworkEvent.ERROR) {
                            LOG.error(event.toString(), event.getThrowable());
                        } else {
                            LOG.info(event.toString(), event.getThrowable());
                        }
                    }
                }
            });

            Services.setServiceLookup(this);

            GrizzlyConfig grizzlyConfig = GrizzlyConfig.getInstance();
            grizzlyConfig.start();

            /*
             * create, configure and start server
             */
            grizzly = new OXHttpServer();

            ServerConfiguration serverConfiguration = grizzly.getServerConfiguration();
            serverConfiguration.setMaxRequestParameters(grizzlyConfig.getMaxRequestParameters());

            final NetworkListener networkListener = new NetworkListener("http-listener", grizzlyConfig.getHttpHost(), grizzlyConfig.getHttpPort());
            networkListener.setChunkingEnabled(false);

            /*
             * Set the maximum size of the PUT/POST body generated by an HTML form to honor "com.openexchange.servlet.maxBodySize"
             */
            int maxBodySize = grizzlyConfig.getMaxBodySize();
            networkListener.setMaxFormPostSize(maxBodySize);
            networkListener.setMaxBufferedPostSize(maxBodySize);

            if (grizzlyConfig.isAJPEnabled()) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Enabling AJP for Grizzly server.");
                }
                networkListener.registerAddOn(new AjpAddOn());
            }

            TCPNIOTransport configuredTcpNioTransport = buildTcpNioTransport();
            networkListener.setTransport(configuredTcpNioTransport);

            if (grizzlyConfig.isJMXEnabled()) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Enabling JMX for Grizzly server.");
                }
                grizzly.getServerConfiguration().setJmxEnabled(true);
            }

            if (grizzlyConfig.isWebsocketsEnabled()) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Enabling WebSockets for Grizzly server.");
                }
                networkListener.registerAddOn(new WebSocketAddOn());
//                WebSocketEngine.getEngine().register(new WebSocketApplication() {
//
//                    @Override
//                    public boolean isApplicationRequest(HttpRequestPacket request) {
//                        return "/echo".equals(request.getRequestURI());
//                    }
//
//                    @Override
//                    public void onMessage(WebSocket socket, String data) {
//                        socket.send(data);
//                    }
//                });
            }

            if (grizzlyConfig.isCometEnabled()) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Enabling Comet for Grizzly server.");
                }
                networkListener.registerAddOn(new CometAddOn());
            }

//            if (LOG.isInfoEnabled()) {
//                LOG.info("Enabling GrizzlOXAddon for Grizzly server.");
//            }
//            networkListener.registerAddOn(new GrizzlOXAddOn());

            /*
             * AtmosphereServiceImpl that contains the framework to handle requests dispatched from the atmosphere servlet
             */
            if (LOG.isInfoEnabled()) {
                LOG.info("Registering Atmosphere service for Grizzly server.");
            }
            AtmosphereServiceImpl atmosphereServiceImpl = new AtmosphereServiceImpl(grizzly, context.getBundle());
            registerService(AtmosphereService.class, atmosphereServiceImpl);

            if (LOG.isInfoEnabled()) {
                LOG.info(String.format(
                    "Registering Grizzly HttpNetworkListener on host: %s and port: %s",
                    grizzlyConfig.getHttpHost(),
                    grizzlyConfig.getHttpPort()));
            }
            grizzly.addListener(networkListener);
            grizzly.start();

            /*
             * Servicefactory that creates instances of the HttpService interface that grizzly implements. Each distinct bundle that uses
             * getService() will get its own instance of HttpServiceImpl
             */
            if (LOG.isInfoEnabled()) {
                LOG.info("Registering OSGi HttpService for Grizzly server.");
            }
            serviceFactory = new HttpServiceFactory(grizzly, context.getBundle());
            registerService(HttpService.class.getName(), serviceFactory);

        } catch (final Exception e) {
            throw GrizzlyExceptionCode.GRIZZLY_SERVER_NOT_STARTED.create(e, new Object[] {});
        }

    }

    @Override
    protected void stopBundle() throws Exception {
        Services.setServiceLookup(null);

        if (LOG.isInfoEnabled()) {
            LOG.info("Unregistering services.");
        }
        cleanUp();
        if (LOG.isInfoEnabled()) {
            LOG.info("Stopping Grizzly.");
        }
        grizzly.stop();
    }

    /**
     * Build a TCPNIOTransport using {c.o].threadpool
     *
     * @return The configure TCPNIOTransport
     * @throws OXException If the Transport can't be build
     */
    private TCPNIOTransport buildTcpNioTransport() throws OXException {
        ThreadPoolService threadPoolService = getService(ThreadPoolService.class);
        if(threadPoolService == null) {
            throw GrizzlyExceptionCode.NEEDED_SERVICE_MISSING.create(ThreadPoolService.class.getSimpleName());
        }
        TCPNIOTransportBuilder builder = TCPNIOTransportBuilder.newInstance();
        final TCPNIOTransport transport = builder.build();
        ExecutorService executor = GrizzlOXExecutorService.createInstance();
        transport.setWorkerThreadPool(executor);
        return transport;
    }

}
