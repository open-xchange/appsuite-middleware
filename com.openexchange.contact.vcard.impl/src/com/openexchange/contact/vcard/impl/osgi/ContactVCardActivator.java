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

package com.openexchange.contact.vcard.impl.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.config.ConfigurationService;
import com.openexchange.contact.vcard.VCardService;
import com.openexchange.contact.vcard.impl.internal.DefaultVCardService;
import com.openexchange.contact.vcard.impl.internal.VCardParametersFactoryImpl;
import com.openexchange.contact.vcard.impl.internal.VCardServiceLookup;
import com.openexchange.exception.OXException;
import com.openexchange.imagetransformation.ImageTransformationService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.version.VersionService;

/**
 * {@link ContactVCardActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ContactVCardActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    protected Class<?>[] getOptionalServices() {
        return new Class[] { VersionService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        VCardServiceLookup.set(this);
        final VCardParametersFactoryImpl vCardParametersFactory = new VCardParametersFactoryImpl();
        final BundleContext context = this.context;
        track(ConfigurationService.class, new ServiceTrackerCustomizer<ConfigurationService, ConfigurationService>() {

            @Override
            public ConfigurationService addingService(ServiceReference<ConfigurationService> reference) {
                ConfigurationService configService = context.getService(reference);
                try {
                    vCardParametersFactory.reinitialize(configService);
                } catch (OXException e) {
                    org.slf4j.LoggerFactory.getLogger(ContactVCardActivator.class).error("Error during reinitialization: {}", e.getMessage(), e);
                }
                return configService;
            }

            @Override
            public void modifiedService(ServiceReference<ConfigurationService> reference, ConfigurationService service) {
                // no
            }

            @Override
            public void removedService(ServiceReference<ConfigurationService> reference, ConfigurationService service) {
                context.ungetService(reference);
            }}
        );
        trackService(ImageTransformationService.class);
        openTrackers();
        registerService(VCardService.class, new DefaultVCardService(vCardParametersFactory));
    }

    @Override
    protected void stopBundle() throws Exception {
        VCardServiceLookup.set(null);
        closeTrackers();
        super.stopBundle();
    }

}

