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

package com.openexchange.realtime.presence.hazelcast.osgi;

import java.util.Map;
import org.apache.commons.logging.Log;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.hazelcast.configuration.HazelcastConfigurationService;
import com.openexchange.log.LogFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.realtime.directory.ResourceDirectory;
import com.openexchange.realtime.presence.PresenceStatusService;
import com.openexchange.realtime.presence.hazelcast.impl.HazelcastPresenceStatusServiceImpl;

/**
 * {@link RealtimeHazelcastPresenceActivator}
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class RealtimeHazelcastPresenceActivator extends HousekeepingActivator {

    private static Log LOG = LogFactory.getLog(RealtimeHazelcastPresenceActivator.class);

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ResourceDirectory.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting bundle: " + getClass().getCanonicalName());

        /*
         * presence registration is bound to directory availability
         */
        final BundleContext context = this.context;
        track(ResourceDirectory.class, new ServiceTrackerCustomizer<ResourceDirectory, ResourceDirectory>() {

            private volatile ServiceRegistration<PresenceStatusService> presenceStatusRegistration;

            @Override
            public ResourceDirectory addingService(ServiceReference<ResourceDirectory> reference) {
                ResourceDirectory resourceDirectory = context.getService(reference);
                /*
                 * create & register presence status service
                 */
                presenceStatusRegistration = context.registerService(PresenceStatusService.class,
                // new HazelcastPresenceStatusServiceImpl(discoverPresenceMapName(hazelcastInstance.getConfig())), null);
                    new HazelcastPresenceStatusServiceImpl(resourceDirectory),
                    null);
                return resourceDirectory;
            }

            @Override
            public void modifiedService(ServiceReference<ResourceDirectory> reference, ResourceDirectory service) {
                // ignore
            }

            @Override
            public void removedService(ServiceReference<ResourceDirectory> reference, ResourceDirectory service) {
                /*
                 * remove channel registration
                 */
                ServiceRegistration<PresenceStatusService> presenceStatusRegistration = this.presenceStatusRegistration;
                if (null != presenceStatusRegistration) {
                    presenceStatusRegistration.unregister();
                    this.presenceStatusRegistration = null;
                }
                context.ungetService(reference);
            }
        });
        openTrackers();
    }

    @Override
    public void stopBundle() throws Exception {
        LOG.info("Stopping bundle: " + getClass().getCanonicalName());
        super.stopBundle();
    }
}
