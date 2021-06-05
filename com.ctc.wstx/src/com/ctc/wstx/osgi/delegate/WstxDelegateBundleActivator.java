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

package com.ctc.wstx.osgi.delegate;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * {@link WstxDelegateBundleActivator} - This class is responsible for registering OSGi service(s) that Woodstox package provides.
 * <p>
 * Currently it means registering all providers that are needed to instantiate input, output and validation schema factories; these are
 * needed since JDK service-introspection (which is the standard Stax instance instantiation mechanism) does not work with OSGi.
 * <p>
 * All resources obtained from <a href="http://wiki.fasterxml.com/WoodstoxDownload">Woodstox Download</a>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.1
 */
public final class WstxDelegateBundleActivator implements BundleActivator {

    private final com.ctc.wstx.osgi.WstxBundleActivator delegate;

    /**
     * Initializes a new {@link WstxDelegateBundleActivator}.
     */
    public WstxDelegateBundleActivator() {
        super();
        delegate = new com.ctc.wstx.osgi.WstxBundleActivator();
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(WstxDelegateBundleActivator.class);
        logger.info("Starting bundle: com.ctc.wstx...");
        try {
            delegate.start(context);
            logger.info("Bundle successfully started: com.ctc.wstx");
        } catch (Exception e) {
            logger.error("Error while starting bundle: com.ctc.wstx", e);
            throw e;
        }
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(WstxDelegateBundleActivator.class);
        logger.info("Stopping bundle: com.ctc.wstx...");
        try {
            delegate.stop(context);
            logger.info("Bundle successfully stopped: com.ctc.wstx");
        } catch (Exception e) {
            logger.error("Error while stopping bundle: com.ctc.wstx", e);
            throw e;
        }
    }

}
