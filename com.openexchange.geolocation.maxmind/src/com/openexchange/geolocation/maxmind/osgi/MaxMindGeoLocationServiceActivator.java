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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.geolocation.maxmind.osgi;

import static com.openexchange.geolocation.maxmind.MaxMindGeoLocationService.newInstance;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.geolocation.GeoLocationService;
import com.openexchange.geolocation.maxmind.MaxMindGeoLocationService;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link MaxMindGeoLocationServiceActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class MaxMindGeoLocationServiceActivator extends HousekeepingActivator implements Reloadable {

    private ServiceRegistration<GeoLocationService> serviceRegistration;
    private MaxMindGeoLocationService maxMindGeoLocationService;

    /**
     * Initializes a new {@link MaxMindGeoLocationServiceActivator}.
     */
    public MaxMindGeoLocationServiceActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        registerService(Reloadable.class, this);
        registerMaxMindGeoLocationService();
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        unregisterMaxMindGeoLocationService();
        super.stopBundle();
    }

    private void registerMaxMindGeoLocationService() throws Exception {
        MaxMindGeoLocationService maxMindGeoLocationService = newInstance(context.getBundle(), getService(ConfigurationService.class));
        this.maxMindGeoLocationService = maxMindGeoLocationService;
        serviceRegistration = context.registerService(GeoLocationService.class, maxMindGeoLocationService, null);
    }

    private void unregisterMaxMindGeoLocationService() {
        ServiceRegistration<GeoLocationService> serviceRegistration = this.serviceRegistration;
        if (null != serviceRegistration) {
            this.serviceRegistration = null;
            serviceRegistration.unregister();
        }

        MaxMindGeoLocationService maxMindGeoLocationService = this.maxMindGeoLocationService;
        if (null != maxMindGeoLocationService) {
            this.maxMindGeoLocationService = null;
            maxMindGeoLocationService.stop();
        }
    }

    @Override
    public synchronized void reloadConfiguration(ConfigurationService configService) {
        unregisterMaxMindGeoLocationService();

        try {
            registerMaxMindGeoLocationService();
        } catch (Exception e) {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MaxMindGeoLocationServiceActivator.class);
            logger.error("", e);
        }
    }

    @Override
    public Interests getInterests() {
        return Reloadables.interestsForProperties("com.openexchange.geolocation.maxmind.databasePath");
    }

}
