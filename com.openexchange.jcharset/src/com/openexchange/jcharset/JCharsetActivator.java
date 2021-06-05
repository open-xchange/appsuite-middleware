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

package com.openexchange.jcharset;

import java.nio.charset.spi.CharsetProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.LoggerFactory;


/**
 * {@link JCharsetActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JCharsetActivator implements BundleActivator {

    private ServiceRegistration<CharsetProvider> serviceRegistration;

    private final CharsetProvider charsetProvider;

    /**
     * Initializes a new {@link JCharsetActivator}.
     */
    public JCharsetActivator() {
        super();
        charsetProvider = new net.freeutils.charset.CharsetProvider();
    }

    @Override
    public synchronized void start(final BundleContext context) throws Exception {
        final org.slf4j.Logger log = LoggerFactory.getLogger(JCharsetActivator.class);
        log.info("Starting bundle: com.openexchange.freecharset");
        try {
            /*
             * Register jcharset's charset provider
             */
            serviceRegistration = context.registerService(CharsetProvider.class, charsetProvider, null);
            log.info("JCharset charset providers registered");
        } catch (Exception e) {
            log.error("Error while starting bundle: com.openexchange.freecharset", e);
        }
    }

    @Override
    public synchronized void stop(final BundleContext context) throws Exception {
        final org.slf4j.Logger log = LoggerFactory.getLogger(JCharsetActivator.class);
        try {
            /*
             * Unregister jcharset's charset provider
             */
            serviceRegistration.unregister();
            log.info("JCharset charset providers unregistered");
            log.info("Stopped bundle: com.openexchange.freecharset");
        } catch (Exception e) {
            log.error("Error while stopping bundle: com.openexchange.freecharset", e);
        }
    }

}
