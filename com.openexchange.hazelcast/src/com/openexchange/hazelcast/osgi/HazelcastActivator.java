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


package com.openexchange.hazelcast.osgi;

import java.util.concurrent.atomic.AtomicReference;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.hazelcast.configuration.HazelcastConfigurationService;
import com.openexchange.java.Strings;
import com.openexchange.management.ManagementService;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link HazelcastActivator} - The activator for Hazelcast bundle (registers a {@link HazelcastInstance} for this JVM)
 * <p>
 * When should you add node?<br>
 * 1. You reached the limits of your CPU or RAM.<br>
 * 2. You reached the limits of GC. You started seeing full-GC
 * <p>
 * When should you stop adding nodes? Should you have 10, 30, 50, 100, or 1000 nodes?<br>
 * 1. You reached the limits of your network. Your switch is not able to handle the amount of data passed around.<br>
 * 2. You reached the limits of the way application utilizing Hazelcast.<br>
 * Adding node is not increasing your total throughput and not reducing the latency.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class HazelcastActivator extends HousekeepingActivator {

    /**
     * The {@code AtomicReference} for {@code HazelcastInstance}.
     */
    public static final AtomicReference<HazelcastInstance> REF_HAZELCAST_INSTANCE = new AtomicReference<HazelcastInstance>();

    /**
     * The logger for HazelcastActivator.
     */
    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(HazelcastActivator.class);

    /**
     * Initializes a new {@link HazelcastActivator}.
     */
    public HazelcastActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { HazelcastConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        String lf = Strings.getLineSeparator();
        LOG.info("{}Hazelcast:{}    Starting...{}", lf, lf, lf);
        long bundleStart = System.currentTimeMillis();
        /*
         * Get hazelcast config service
         */
        HazelcastConfigurationService configService = getService(HazelcastConfigurationService.class);
        if (false == configService.isEnabled()) {
            LOG.info("{}Hazelcast:{}    Startup of Hazelcast clustering and data distribution platform denied per configuration.{}", lf, lf, lf);
            return;
        }
        /*
         * Create hazelcast instance from configuration
         */
        Config config = configService.getConfig();
        {
            LOG.info("{}Hazelcast:{}    Creating new hazelcast instance...{}", lf, lf, lf);
            if (config.getNetworkConfig().getJoin().getMulticastConfig().isEnabled()) {
                LOG.info("{}Hazelcast:{}    Using network join: {}{}", lf, lf, config.getNetworkConfig().getJoin().getMulticastConfig(), lf);
            }
            if (config.getNetworkConfig().getJoin().getTcpIpConfig().isEnabled()) {
                LOG.info("{}Hazelcast:{}    Using network join: {}{}", lf, lf, config.getNetworkConfig().getJoin().getTcpIpConfig(), lf);
            }
        }
        long hzStart = System.currentTimeMillis();
        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);
        LOG.info("{}Hazelcast:{}    New hazelcast instance successfully created in {} msec.{}", lf, lf, (System.currentTimeMillis() - hzStart), lf);
        /*
         * Register instance
         */
        registerService(HazelcastInstance.class, hazelcastInstance);
        REF_HAZELCAST_INSTANCE.set(hazelcastInstance);
        LOG.info("{}Hazelcast:{}    Started in {} msec.{}", lf, lf, (System.currentTimeMillis() - bundleStart), lf);
        /*
         * Register management mbean dynamically
         */
        track(ManagementService.class, new ManagementRegisterer(context));
        openTrackers();
    }

    @Override
    protected void stopBundle() throws Exception {
        String lf = Strings.getLineSeparator();
        LOG.info("{}Hazelcast:{}    Shutting down...{}", lf, lf, lf);
        long start = System.currentTimeMillis();
        HazelcastInstance instance = REF_HAZELCAST_INSTANCE.get();
        if (null != instance) {
            instance.getLifecycleService().shutdown();
            REF_HAZELCAST_INSTANCE.set(null);
        }
        LOG.info("{}Hazelcast:{}    Shutdown completed after {} msec.{}", lf, lf, (System.currentTimeMillis() - start), lf);
        super.stopBundle();
    }

}
