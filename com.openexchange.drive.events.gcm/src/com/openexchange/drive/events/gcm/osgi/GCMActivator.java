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

package com.openexchange.drive.events.gcm.osgi;

import java.util.Properties;
import org.osgi.framework.ServiceReference;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.drive.events.DriveEventService;
import com.openexchange.drive.events.gcm.GCMKeyProvider;
import com.openexchange.drive.events.gcm.internal.DriveEventsGCMProperty;
import com.openexchange.drive.events.gcm.internal.GCMDriveEventPublisher;
import com.openexchange.drive.events.subscribe.DriveSubscriptionStore;
import com.openexchange.fragment.properties.loader.FragmentPropertiesLoader;
import com.openexchange.java.Strings;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;

/**
 * {@link GCMActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class GCMActivator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GCMActivator.class);

    /**
     * Initializes a new {@link GCMActivator}.
     */
    public GCMActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { DriveEventService.class, DriveSubscriptionStore.class, LeanConfigurationService.class};
    }

    @Override
    protected Class<?>[] getOptionalServices() {
        return new Class<?>[] { GCMKeyProvider.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("starting bundle: com.openexchange.drive.events.gcm");
        track(FragmentPropertiesLoader.class, new SimpleRegistryListener<FragmentPropertiesLoader>() {

            private volatile GCMKeyProvider provider;
            
            @Override
            public void added(ServiceReference<FragmentPropertiesLoader> ref, FragmentPropertiesLoader service) {
                Properties properties = service.load(DriveEventsGCMProperty.FRAGMENT_FILE_NAME);
                if(properties != null) {
                    String key = properties.getProperty(DriveEventsGCMProperty.KEY.getFQPropertyName());
                    if(Strings.isNotEmpty(key)) {
                        provider = () -> key;
                        registerService(GCMKeyProvider.class, provider);
                    }
                }
            }

            @Override
            public void removed(ServiceReference<FragmentPropertiesLoader> ref, FragmentPropertiesLoader service) {
                if(provider != null) {
                    unregisterService(provider);
                }
            }
        });
        openTrackers();
        /*
         * register publisher
         */
        getServiceSafe(DriveEventService.class).registerPublisher(new GCMDriveEventPublisher(this));
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle: com.openexchange.drive.events.gcm");
        super.stopBundle();
    }
}
