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

package com.openexchange.groupware.impl;

import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.ajp13.AJPv13Config;
import com.openexchange.ajp13.AJPv13Server;
import com.openexchange.ajp13.monitoring.AJPv13Monitors;
import com.openexchange.ajp13.najp.threadpool.AJPv13SynchronousQueueProvider;
import com.openexchange.ajp13.xajp.XAJPv13Server;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.server.Initialization;

/**
 * {@link BackendServicesInit} - Initialization for back-end services.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class BackendServicesInit implements Initialization {

    /**
     * The logger.
     */
    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(BackendServicesInit.class);

    private static final int AJP_MODE_STABLE = 1;

    private static final int AJP_MODE_THREAD_POOL = 2;

    private static final int AJP_MODE_NIO = 3;

    /**
     * The singleton instance.
     */
    private static final BackendServicesInit instance = new BackendServicesInit(AJP_MODE_THREAD_POOL);

    /**
     * Gets the singleton instance of {@link BackendServicesInit}.
     * 
     * @return The singleton instance of {@link BackendServicesInit}.
     */
    public static BackendServicesInit getInstance() {
        return instance;
    }

    private final AtomicBoolean started;

    private final Initialization ajpStarter;

    private final int ajpMode;

    /**
     * Initializes a new {@link BackendServicesInit}.
     * 
     * @param useNewAJP Whether to use the new AJP implementation or not
     */
    private BackendServicesInit(final int ajpMode) {
        super();
        started = new AtomicBoolean();
        switch (ajpMode) {
        case AJP_MODE_STABLE:
            ajpStarter = new AJPStableStarter();
            break;
        case AJP_MODE_THREAD_POOL:
            ajpStarter = new NAJPStarter();
            break;
        case AJP_MODE_NIO:
            ajpStarter = new XAJPStarter();
            break;
        default:
            throw new IllegalArgumentException("Unknown AJP mode: " + ajpMode);
        }
        this.ajpMode = ajpMode;
    }

    public void start() throws AbstractOXException {
        if (!started.compareAndSet(false, true)) {
            LOG.error(this.getClass().getName() + " already started");
            return;
        }
        ajpStarter.start();
        if (LOG.isInfoEnabled()) {
            final String prefix = ((AJP_MODE_STABLE == ajpMode) ? "Stable AJP server " : ((AJP_MODE_NIO == ajpMode) ? "NIO AJP server " : "New AJP server "));
            LOG.info(new StringBuilder(32).append(prefix).append("successfully started.").toString());
        }
    }

    public void stop() throws AbstractOXException {
        if (!started.compareAndSet(true, false)) {
            LOG.error(this.getClass().getName() + " cannot be stopped since it has not been started before");
            return;
        }
        ajpStarter.stop();
        if (LOG.isInfoEnabled()) {
            final String prefix = ((AJP_MODE_STABLE == ajpMode) ? "Stable AJP server " : ((AJP_MODE_NIO == ajpMode) ? "NIO AJP server " : "New AJP server "));
            LOG.info(new StringBuilder(32).append(prefix).append("successfully stopped.").toString());
        }
    }

    private static final class AJPStableStarter implements Initialization {

        public AJPStableStarter() {
            super();
        }

        public void start() throws AbstractOXException {
            AJPv13Server.setInstrance(new com.openexchange.ajp13.stable.AJPv13ServerImpl());
            AJPv13Config.getInstance().start();
            AJPv13Monitors.setListenerMonitor(com.openexchange.ajp13.stable.AJPv13ServerImpl.getListenerMonitor());
            AJPv13Server.startAJPServer();
        }

        public void stop() throws AbstractOXException {
            AJPv13Server.stopAJPServer();
            AJPv13Monitors.releaseListenerMonitor();
            AJPv13Config.getInstance().stop();
            AJPv13Server.releaseInstrance();
        }
    }

    private static final class NAJPStarter implements Initialization {

        public NAJPStarter() {
            super();
        }

        public void start() throws AbstractOXException {
            /*
             * Proper synchronous queue
             */
            String property = System.getProperty("java.specification.version");
            if (null == property) {
                property = System.getProperty("java.runtime.version");
                if (null == property) {
                    // JRE not detectable, use fallback
                    AJPv13SynchronousQueueProvider.initInstance(false);
                } else {
                    // "java.runtime.version=1.6.0_0-b14" OR "java.runtime.version=1.5.0_18-b02"
                    AJPv13SynchronousQueueProvider.initInstance(!property.startsWith("1.5"));
                }
            } else {
                // "java.specification.version=1.5" OR "java.specification.version=1.6"
                AJPv13SynchronousQueueProvider.initInstance("1.5".compareTo(property) < 0);
            }
            AJPv13Server.setInstrance(new com.openexchange.ajp13.najp.AJPv13ServerImpl());
            AJPv13Config.getInstance().start();
            AJPv13Server.startAJPServer();
        }

        public void stop() throws AbstractOXException {
            com.openexchange.ajp13.najp.AJPv13ServerImpl.stopAJPServer();
            AJPv13Config.getInstance().stop();
            AJPv13Server.releaseInstrance();
            AJPv13SynchronousQueueProvider.releaseInstance();
        }
    }

    private static final class XAJPStarter implements Initialization {

        public XAJPStarter() {
            super();
        }

        public void start() throws AbstractOXException {
            AJPv13Config.getInstance().start();
            XAJPv13Server.getInstance().start();
        }

        public void stop() throws AbstractOXException {
            AJPv13Config.getInstance().stop();
            XAJPv13Server.getInstance().close();
            XAJPv13Server.releaseInstance();
        }

    }
}
