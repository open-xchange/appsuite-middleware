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

package com.openexchange.drive.events.apn.osgi;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.osgi.framework.ServiceReference;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.config.lean.Property;
import com.openexchange.drive.events.DriveEventService;
import com.openexchange.drive.events.apn.APNAccess;
import com.openexchange.drive.events.apn.IOSAPNCertificateProvider;
import com.openexchange.drive.events.apn.MacOSAPNCertificateProvider;
import com.openexchange.drive.events.apn.internal.APNDriveEventPublisher;
import com.openexchange.drive.events.apn.internal.DriveEventsAPNProperty;
import com.openexchange.drive.events.apn.internal.OperationSystemType;
import com.openexchange.drive.events.subscribe.DriveSubscriptionStore;
import com.openexchange.exception.OXException;
import com.openexchange.fragment.properties.loader.FragmentPropertiesLoader;
import com.openexchange.java.Strings;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.timer.TimerService;

/**
 * {@link APNDriveEventsActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class APNDriveEventsActivator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(APNDriveEventsActivator.class);

    /**
     * Initializes a new {@link APNDriveEventsActivator}.
     */
    public APNDriveEventsActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { DriveEventService.class, DriveSubscriptionStore.class, LeanConfigurationService.class, TimerService.class };
    }

    @Override
    protected Class<?>[] getOptionalServices() {
        return new Class<?>[] { IOSAPNCertificateProvider.class, MacOSAPNCertificateProvider.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("starting bundle: com.openexchange.drive.events.apn");

        track(FragmentPropertiesLoader.class, new SimpleRegistryListener<FragmentPropertiesLoader>() {

            private volatile IOSAPNCertificateProvider iosProvider;
            private volatile IOSAPNCertificateProvider macosProvider;

            @Override
            public void added(ServiceReference<FragmentPropertiesLoader> ref, FragmentPropertiesLoader service) {
                Properties properties = service.load(DriveEventsAPNProperty.FRAGMENT_FILE_NAME);
                if(properties != null) {
                    APNAccess access = createAccess(properties, OperationSystemType.IOS);
                    if(access != null) {
                        iosProvider = () -> access;
                        registerService(IOSAPNCertificateProvider.class, iosProvider);
                    }

                    APNAccess macAccess = createAccess(properties, OperationSystemType.MACOS);
                    if(macAccess != null) {
                        macosProvider = () -> macAccess;
                        registerService(IOSAPNCertificateProvider.class, macosProvider);
                    }
                }
            }

            @Override
            public void removed(ServiceReference<FragmentPropertiesLoader> ref, FragmentPropertiesLoader service) {
                if(iosProvider != null) {
                    unregisterService(iosProvider);
                }
                if(macosProvider != null) {
                    unregisterService(macosProvider);
                }
            }
        });
        openTrackers();
        /*
         * register publishers
         */
        DriveEventService eventService = getServiceSafe(DriveEventService.class);
        eventService.registerPublisher(new APNDriveEventPublisher(this, "apn", OperationSystemType.IOS, IOSAPNCertificateProvider.class));
        eventService.registerPublisher(new APNDriveEventPublisher(this, "apn.macos", OperationSystemType.MACOS, MacOSAPNCertificateProvider.class));
    }

    /**
     * Creates an {@link APNAccess} from the given {@link Properties} object
     * @param properties The {@link Properties} object
     * @param The OS identifier
     * @return The {@link APNAccess} or null
     */
    protected APNAccess createAccess(Properties properties, OperationSystemType type) {
        Map<String, String> optionals = new HashMap<>(1);
        optionals.put(DriveEventsAPNProperty.OPTIONAL_FIELD, type.getName());
        String keystore;
        try {
            keystore = getProperty(properties, DriveEventsAPNProperty.keystore, optionals);

            if (Strings.isNotEmpty(keystore)) {
                String password = getProperty(properties, DriveEventsAPNProperty.password, optionals);
                boolean production = Boolean.parseBoolean(getProperty(properties, DriveEventsAPNProperty.production, optionals));
                return new APNAccess(keystore, password, production);
            }
        } catch (OXException e) {
            // nothing to do
        }
        return null;
    }

    /**
     * Get the given property from the {@link Properties} object
     *
     * @param properties The {@link Properties} object
     * @param prop The {@link Property} to return
     * @param optional The optional
     * @return The string value of the property
     * @throws OXException In case the property is missing
     */
    private String getProperty(Properties properties, Property prop, Map<String, String> optional) throws OXException {
        String result = properties.getProperty(prop.getFQPropertyName(optional));
        if(result == null) {
            // This should never happen as long as the shipped fragment contains a proper properties file
            LOG.error("Missing required property from fragment: "+ prop.getFQPropertyName());
            throw OXException.general("Missing property: "+ prop.getFQPropertyName());
        }
        return result;
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle: com.openexchange.drive.events.apn");
        super.stopBundle();
    }

}
