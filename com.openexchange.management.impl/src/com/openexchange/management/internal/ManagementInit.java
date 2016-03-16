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

package com.openexchange.management.internal;

import static com.openexchange.management.services.ManagementServiceRegistry.getServiceRegistry;
import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.server.Initialization;
import com.openexchange.server.ServiceExceptionCode;

/**
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class ManagementInit implements Initialization {

    private static final AtomicBoolean started = new AtomicBoolean();

    private static final ManagementInit singleton = new ManagementInit();

    /**
     * Logger.
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ManagementInit.class);

    /**
     * Prevent instantiation.
     */
    private ManagementInit() {
        super();
    }

    /**
     * @return the singleton instance.
     */
    public static ManagementInit getInstance() {
        return singleton;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() throws OXException {
        if (started.get()) {
            LOG.error("{} already started", ManagementInit.class.getName());
            return;
        }
        final ManagementAgentImpl agent = ManagementAgentImpl.getInstance();
        final ConfigurationService c = getServiceRegistry().getService(ConfigurationService.class);
        if (c == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create( ConfigurationService.class.getName());
        }
        /*
         * Configure
         */
        {
            String bindAddress = c.getProperty("JMXBindAddress", "localhost");
            if (bindAddress == null) {
                bindAddress = "localhost";
            }
            final int jmxPort = c.getIntProperty("JMXPort", 9999);
            agent.setJmxPort(jmxPort);
            final int jmxServerPort = c.getIntProperty("JMXServerPort", -1);
            agent.setJmxServerPort(jmxServerPort);
            agent.setJmxSinglePort(c.getBoolProperty("JMXSinglePort", false));
            agent.setJmxBindAddr(bindAddress);
            String jmxLogin = c.getProperty("JMXLogin");
            if (jmxLogin != null && (jmxLogin = jmxLogin.trim()).length() > 0) {
                String jmxPassword = c.getProperty("JMXPassword");
                if (jmxPassword == null || (jmxPassword = jmxPassword.trim()).length() == 0) {
                    throw new IllegalArgumentException("JMX password not set");
                }
                agent.setJmxLogin(jmxLogin);
                agent.setJmxPassword(jmxPassword);
            }
        }
        /*
         * Run
         */
        agent.run();
        LOG.info("JMX server successfully initialized.");
        started.set(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() throws OXException {
        if (!started.get()) {
            LOG.error("{} has not been started", ManagementInit.class.getName());
            return;
        }
        final ManagementAgentImpl agent = ManagementAgentImpl.getInstance();
        agent.stop();
        started.set(false);
    }

    /**
     * @return <code>true</code> if monitoring has been started; otherwise <code>false</code>
     */
    public boolean isStarted() {
        return started.get();
    }
}
