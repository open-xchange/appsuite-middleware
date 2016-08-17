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

package com.openexchange.session.reservation.impl.osgi;

import java.util.Map;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.hazelcast.configuration.HazelcastConfigurationService;
import com.openexchange.hazelcast.serialization.CustomPortableFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.session.reservation.SessionReservationService;
import com.openexchange.session.reservation.impl.HazelcastInstanceNotActiveExceptionHandler;
import com.openexchange.session.reservation.impl.Services;
import com.openexchange.session.reservation.impl.SessionReservationServiceImpl;
import com.openexchange.session.reservation.impl.portable.PortableReservationFactory;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.user.UserService;


/**
 * {@link SessionReservationActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class SessionReservationActivator extends HousekeepingActivator implements HazelcastInstanceNotActiveExceptionHandler {

    static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SessionReservationActivator.class);

    /**
     * Initializes a new {@link SessionReservationActivator}.
     */
    public SessionReservationActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { HazelcastConfigurationService.class, SessiondService.class, ContextService.class, UserService.class, ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);
        final BundleContext context = this.context;

        // Create service instance
        final SessionReservationServiceImpl serviceImpl = new SessionReservationServiceImpl(this);

        // Check Hazelcast stuff
        {
            final HazelcastConfigurationService hazelcastConfig = getService(HazelcastConfigurationService.class);
            if (hazelcastConfig.isEnabled()) {
                // Track HazelcastInstance service
                final ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance> customizer = new ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance>() {

                    @Override
                    public void removedService(final ServiceReference<HazelcastInstance> reference, final HazelcastInstance service) {
                        removeService(HazelcastInstance.class);
                        serviceImpl.changeBackingMapToLocalMap();
                        context.ungetService(reference);
                    }

                    @Override
                    public void modifiedService(final ServiceReference<HazelcastInstance> reference, final HazelcastInstance service) {
                        // Ignore
                    }

                    @Override
                    public HazelcastInstance addingService(final ServiceReference<HazelcastInstance> reference) {
                        final HazelcastInstance hazelcastInstance = context.getService(reference);
                        try {
                            final String hzMapName = discoverHzMapName(hazelcastConfig.getConfig(),"sessionReservation");
                            if (null == hzMapName) {
                                context.ungetService(reference);
                                return null;
                            }
                            addService(HazelcastInstance.class, hazelcastInstance);
                            serviceImpl.setHzMapName(hzMapName);
                            serviceImpl.changeBackingMapToHz();
                            return hazelcastInstance;
                        } catch (final OXException e) {
                            LOG.warn("Couldn't initialize distributed reservation map.", e);
                        } catch (final RuntimeException e) {
                            LOG.warn("Couldn't initialize distributed reservation map.", e);
                        }
                        context.ungetService(reference);
                        return null;
                    }
                };
                track(HazelcastInstance.class, customizer);
            }
        }


        // Open trackers
        openTrackers();

        // Register service instance
        registerService(SessionReservationService.class, serviceImpl);
        registerService(CustomPortableFactory.class, new PortableReservationFactory());
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        Services.setServiceLookup(null);
    }

    @Override
    public <S> boolean removeService(java.lang.Class<? extends S> clazz) {
        return super.removeService(clazz);
    }

    @Override
    public <S> boolean addService(java.lang.Class<S> clazz, S service) {
        return super.addService(clazz, service);
    }

    String discoverHzMapName(final Config config, String mapPrefix) throws IllegalStateException {
        final Map<String, MapConfig> mapConfigs = config.getMapConfigs();
        if (null != mapConfigs && !mapConfigs.isEmpty()) {
            for (final String mapName : mapConfigs.keySet()) {
                if (mapName.startsWith(mapPrefix)) {
                    LOG.info("Using distributed reservation map '{}'.", mapName);
                    return mapName;
                }
            }
        }
        LOG.info("No distributed reservation map with mapPrefix {} in hazelcast configuration", mapPrefix);
        return null;
    }

    @Override
    public void propagateNotActive(HazelcastInstanceNotActiveException notActiveException) {
        final BundleContext context = this.context;
        if (null != context) {
            context.registerService(HazelcastInstanceNotActiveException.class, notActiveException, null);
        }
    }

}
