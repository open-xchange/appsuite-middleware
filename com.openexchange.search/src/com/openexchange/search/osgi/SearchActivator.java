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

package com.openexchange.search.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import com.openexchange.search.SearchService;
import com.openexchange.search.internal.SearchServiceImpl;


/**
 * {@link SearchActivator} - The activator for <code>com.openexchange.search</code> bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SearchActivator implements BundleActivator {

    private ServiceRegistration<SearchService> searchServiceRegistration;

    /**
     * Initializes a new {@link SearchActivator}.
     */
    public SearchActivator() {
        super();
    }

    @Override
    public synchronized void start(final BundleContext context) throws Exception {
        final Logger logger = org.slf4j.LoggerFactory.getLogger(SearchActivator.class);
        logger.info("Starting bundle: com.openexchange.search");
        try {
            searchServiceRegistration = context.registerService(SearchService.class, new SearchServiceImpl(), null);
        } catch (Exception e) {
            logger.error("Failed starting bundle com.openexchange.search", e);
            throw e;
        }
    }

    @Override
    public synchronized void stop(final BundleContext context) throws Exception {
        final Logger logger = org.slf4j.LoggerFactory.getLogger(SearchActivator.class);
        logger.info("Stopping bundle: com.openexchange.search");
        try {
            final ServiceRegistration<SearchService> searchServiceRegistration = this.searchServiceRegistration;
            if (null != searchServiceRegistration) {
                searchServiceRegistration.unregister();
                this.searchServiceRegistration = null;
            }
        } catch (Exception e) {
            logger.error("Failed stopping bundle com.openexchange.search", e);
            throw e;
        }
    }

}
