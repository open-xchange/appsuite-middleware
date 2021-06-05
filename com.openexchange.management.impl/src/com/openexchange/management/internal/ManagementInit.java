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

package com.openexchange.management.internal;

import static com.openexchange.management.services.ManagementServiceRegistry.getServiceRegistry;
import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.Initialization;
import com.openexchange.server.ServiceExceptionCode;

/**
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class ManagementInit implements Initialization {

    private static final AtomicBoolean started = new AtomicBoolean();

    private static final ManagementInit singleton = new ManagementInit();

    /**
     * @return the singleton instance.
     */
    public static ManagementInit getInstance() {
        return singleton;
    }

    // ---------------------------------------------------------------------------------------------------------------------------

    /**
     * Prevent instantiation.
     */
    private ManagementInit() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() throws OXException {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ManagementInit.class);
        if (started.get()) {
            logger.error("{} already started", ManagementInit.class.getName());
            return;
        }
        final ManagementAgentImpl agent = ManagementAgentImpl.getInstance();
        final ConfigurationService configurationService = getServiceRegistry().getService(ConfigurationService.class);
        if (configurationService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ConfigurationService.class.getName());
        }
        /*
         * Configure
         */
        {
            String bindAddress = configurationService.getProperty("JMXBindAddress", "localhost");
            if (bindAddress == null) {
                bindAddress = "localhost";
            }
            final int jmxPort = configurationService.getIntProperty("JMXPort", 9999);
            agent.setJmxPort(jmxPort);
            final int jmxServerPort = configurationService.getIntProperty("JMXServerPort", -1);
            agent.setJmxServerPort(jmxServerPort);
            agent.setJmxSinglePort(configurationService.getBoolProperty("JMXSinglePort", false));
            agent.setJmxBindAddr(bindAddress);
            String jmxLogin = configurationService.getProperty("JMXLogin");
            if (jmxLogin != null && (jmxLogin = jmxLogin.trim()).length() > 0) {
                String jmxPassword = configurationService.getProperty("JMXPassword");
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
        String ls = Strings.getLineSeparator();
        logger.info("{}{}\tJMX server successfully initialized.{}", ls, ls, ls);
        started.set(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() throws OXException {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ManagementInit.class);
        if (!started.get()) {
            logger.error("{} has not been started", ManagementInit.class.getName());
            return;
        }
        final ManagementAgentImpl agent = ManagementAgentImpl.getInstance();
        agent.stop();
        logger.info("JMX server successfully stopped.");
        started.set(false);
    }

    /**
     * @return <code>true</code> if monitoring has been started; otherwise <code>false</code>
     */
    public boolean isStarted() {
        return started.get();
    }
}
