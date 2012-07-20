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

package com.openexchange.ajp13.osgi;

import java.util.ArrayList;
import java.util.List;
import org.osgi.service.http.HttpService;
import com.openexchange.ajp13.AJPv13Config;
import com.openexchange.ajp13.AJPv13ServiceRegistry;
import com.openexchange.ajp13.AbstractAJPv13Request;
import com.openexchange.ajp13.AJPv13Server;
import com.openexchange.ajp13.servlet.http.HttpSessionWrapper;
import com.openexchange.ajp13.servlet.http.osgi.HttpServiceImpl;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.management.ManagementService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.server.Initialization;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;

/**
 * {@link AJPv13Activator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AJPv13Activator extends HousekeepingActivator {

    /**
     * The logger.
     */
    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(AJPv13Activator.class));

    private List<Initialization> inits;

    /**
     * Initializes a new {@link AJPv13Activator}.
     */
    public AJPv13Activator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, TimerService.class, ThreadPoolService.class };
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
    protected void startBundle() throws Exception {
        try {
            /*
             * (Re-)Initialize service registry with available services
             */
            AJPv13ServiceRegistry.SERVICE_LOOKUP.set(this);
            inits = new ArrayList<Initialization>(3);
            /*
             * Set starter dependent on mode
             */
            inits.add(new NAJPStarter());
            inits.add(com.openexchange.ajp13.servlet.http.HttpManagersInit.getInstance());
            inits.add(new Initialization() {

                @Override
                public void stop() {
                    AbstractAJPv13Request.setEchoHeaderName(null);
                }

                @Override
                public void start() {
                    final ConfigurationService service = getService(ConfigurationService.class);
                    AbstractAJPv13Request.setEchoHeaderName(null == service ? null : service.getProperty("com.openexchange.servlet.echoHeaderName"));
                }
            });
            inits.add(new Initialization() {

                @Override
                public void stop() {
                    HttpSessionWrapper.resetCookieTTL();
                }

                @Override
                public void start() {
                    HttpSessionWrapper.getCookieTTL();
                }
            });
            /*
             * Start
             */
            for (final Initialization initialization : inits) {
                initialization.start();
            }
            if (LOG.isInfoEnabled()) {
                LOG.info(new StringBuilder(32).append("NIO AJP server successfully started.").toString());
            }
            /*
             * Start trackers
             */
            track(ManagementService.class, new ManagementServiceTracker(context));
            openTrackers();
            /*
             * Register services
             */
            final HttpServiceImpl http = new HttpServiceImpl();
            registerService(HttpService.class, http);
            http.registerServlet("/servlet/TestServlet", new com.openexchange.ajp13.TestServlet(), null, null);

            /*-
             * Alternative approach for HttpService:
             *
             * http://www.eclipse.org/equinox/server/
             * http://www.eclipse.org/equinox/server/http_in_equinox.php
             *
             * http://docs.codehaus.org/display/JETTY/OSGi+Tips
             */

        } catch (final Exception e) {
            com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(AJPv13Activator.class)).error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        try {
            cleanUp();
            if (inits != null) {
                while (!inits.isEmpty()) {
                    inits.remove(0).stop();
                }
                inits = null;
            }
            if (LOG.isInfoEnabled()) {
                LOG.info(new StringBuilder(32).append("NIO AJP server successfully stopped.").toString());
            }
            /*
             * Clear service registry
             */
            AJPv13ServiceRegistry.SERVICE_LOOKUP.set(null);
        } catch (final Exception e) {
            com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(AJPv13Activator.class)).error(e.getMessage(), e);
            throw e;
        }
    }

    private static final class NAJPStarter implements Initialization {

        public NAJPStarter() {
            super();
        }

        @Override
        public void start() throws OXException {
            AJPv13Server.setInstance(new com.openexchange.ajp13.najp.AJPv13ServerImpl());
            AJPv13Config.getInstance().start();
            AJPv13Server.startAJPServer();
        }

        @Override
        public void stop() throws OXException {
            AJPv13Server.stopAJPServer();
            AJPv13Config.getInstance().stop();
            AJPv13Server.releaseInstrance();
        }
    }

}
