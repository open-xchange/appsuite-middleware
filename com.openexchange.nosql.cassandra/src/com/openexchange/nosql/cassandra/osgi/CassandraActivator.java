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

package com.openexchange.nosql.cassandra.osgi;

import java.util.concurrent.TimeUnit;
import javax.management.ObjectName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.management.ManagementService;
import com.openexchange.nosql.cassandra.CassandraService;
import com.openexchange.nosql.cassandra.exceptions.CassandraServiceExceptionCodes;
import com.openexchange.nosql.cassandra.impl.CassandraServiceImpl;
import com.openexchange.nosql.cassandra.mbean.CassandraClusterMBean;
import com.openexchange.nosql.cassandra.mbean.impl.CassandraClusterMBeanImpl;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link CassandraActivator}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class CassandraActivator extends HousekeepingActivator {

    private CassandraService cassandraService;
    private ObjectName objectName;
    private static final long DELAY = TimeUnit.SECONDS.toMillis(15);
    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraActivator.class);
    private ScheduledTimerTask timerTask;

    /**
     * Initialises a new {@link CassandraActivator}.
     */
    public CassandraActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, ManagementService.class, LeanConfigurationService.class, TimerService.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        final CassandraService cassandraService = new CassandraServiceImpl(this);
        registerService(CassandraService.class, cassandraService);
        addService(CassandraService.class, cassandraService);
        this.cassandraService = cassandraService;
        try {
            ((CassandraServiceImpl) cassandraService).init();
        } catch (OXException e) {
            if (!CassandraServiceExceptionCodes.CONTACT_POINTS_NOT_REACHABLE.equals(e)) {
                throw e;
            }
            // Create a self-cancelling task to initialise the cassandra service
            LOGGER.error("Cassandra failed to initialise: {}. Will retry in {} seconds", e.getMessage(), TimeUnit.MILLISECONDS.toSeconds(DELAY), e);
            TimerService service = getService(TimerService.class);
            timerTask = service.scheduleAtFixedRate(new CassandraInitialiser((CassandraServiceImpl) cassandraService), DELAY, DELAY, TimeUnit.MILLISECONDS);
        }

        // Register the cluster mbean
        ObjectName objectName = new ObjectName(CassandraClusterMBean.DOMAIN, "name", CassandraClusterMBean.NAME);
        CassandraClusterMBean mbean = new CassandraClusterMBeanImpl(this);
        ManagementService managementService = getService(ManagementService.class);
        managementService.registerMBean(objectName, mbean);
        this.objectName = objectName;

        final Logger logger = LoggerFactory.getLogger(CassandraActivator.class);
        logger.info("Cassandra service was successfully registered");
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        // Unregister cluster mbean
        ObjectName objectName = this.objectName;
        if (null != objectName) {
            this.objectName = null;
            ManagementService managementService = getService(ManagementService.class);
            managementService.unregisterMBean(objectName);
        }

        // Unregister cassandra service
        CassandraService cassandraService = this.cassandraService;
        if (cassandraService != null) {
            this.cassandraService = null;
            unregisterService(CassandraService.class);
            // Shutdown the service
            ((CassandraServiceImpl) cassandraService).shutdown();
        }

        super.stopBundle();

        final Logger logger = LoggerFactory.getLogger(CassandraActivator.class);
        logger.info("Cassandra service was successfully shutdown and unregistered");
    }

    /**
     * {@link CassandraInitialiser} - Self-cancelling scheduled task
     */
    private final class CassandraInitialiser implements Runnable {

        private CassandraServiceImpl cassandraService;
        private final Logger LOGGER = LoggerFactory.getLogger(CassandraInitialiser.class);

        /**
         * Initialises a new {@link CassandraActivator.CassandraInitialiser}.
         */
        public CassandraInitialiser(CassandraServiceImpl cassandraService) {
            super();
            this.cassandraService = cassandraService;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            try {
                cassandraService.init();
                LOGGER.info("Cassandra successfully initialised.");
                timerTask.cancel();
            } catch (OXException e) {
                if (CassandraServiceExceptionCodes.CONTACT_POINTS_NOT_REACHABLE.equals(e)) {
                    LOGGER.error("Cassandra failed to initialise: {}. Will retry in {} seconds", e.getMessage(), TimeUnit.MILLISECONDS.toSeconds(DELAY), e);
                }
            }
        }
    }
}
