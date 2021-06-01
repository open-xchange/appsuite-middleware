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

package com.openexchange.textxtraction.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.textxtraction.DelegateTextXtraction;
import com.openexchange.textxtraction.TextXtractService;
import com.openexchange.textxtraction.internal.TikaTextXtractService;

/**
 * {@link TextXtractionActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class TextXtractionActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link TextXtractionActivator}.
     */
    public TextXtractionActivator() {
        super();
    }

    @Override
    public void startBundle() throws Exception {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TextXtractionActivator.class);
        final String name = "com.openexchange.textxtraction";
        log.info("Starting bundle: {}", name);
        try {
            final TikaTextXtractService tikaTextXtractService = new TikaTextXtractService();
            final BundleContext context = this.context;
            track(DelegateTextXtraction.class, new ServiceTrackerCustomizer<DelegateTextXtraction, DelegateTextXtraction>() {

                @Override
                public DelegateTextXtraction addingService(final ServiceReference<DelegateTextXtraction> reference) {
                    final DelegateTextXtraction service = context.getService(reference);
                    if (tikaTextXtractService.addDelegateTextXtraction(service)) {
                        return service;
                    }
                    context.ungetService(reference);
                    return null;
                }

                @Override
                public void modifiedService(final ServiceReference<DelegateTextXtraction> reference, final DelegateTextXtraction service) {
                    // Nothing
                }

                @Override
                public void removedService(final ServiceReference<DelegateTextXtraction> reference, final DelegateTextXtraction service) {
                    if (null != service) {
                        tikaTextXtractService.removeDelegateTextXtraction(service);
                        context.ungetService(reference);
                    }
                }
            });
            openTrackers();
            registerService(TextXtractService.class, tikaTextXtractService);
        } catch (Exception e) {
            log.info("Starting bundle failed: {}", name, e);
            throw e;
        }
    }

    @Override
    public void stopBundle() throws Exception {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TextXtractionActivator.class);
        final String name = "com.openexchange.textxtraction";
        log.info("Stopping bundle: {}", name);
        try {
            cleanUp();
        } catch (Exception e) {
            log.info("Stopping bundle failed: {}", name, e);
            throw e;
        }
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[0];
    }

}
