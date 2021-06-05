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

package com.openexchange.conversion.engine.osgi;

import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.DataHandler;
import com.openexchange.conversion.DataSource;
import com.openexchange.conversion.engine.internal.ConversionEngineRegistry;
import com.openexchange.conversion.engine.internal.ConversionServiceImpl;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link ConversionEngineActivator} - Activator for conversion engine
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ConversionEngineActivator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(ConversionEngineActivator.class);

    /**
     * Initializes a new {@link ConversionEngineActivator}
     */
    public ConversionEngineActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            /*
             * Clear registry
             */
            ConversionEngineRegistry.getInstance().clearAll();
            /*
             * Start-up service trackers
             */
            track(DataHandler.class, new DataHandlerTracker(context));
            track(DataSource.class, new DataSourceTracker(context));
            openTrackers();
            /*
             * Register service
             */
            registerService(ConversionService.class, new ConversionServiceImpl());
            LOG.info("Conversion engine successfully started");
        } catch (Exception e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        try {
            super.stopBundle();
            /*
             * Clear registry
             */
            ConversionEngineRegistry.getInstance().clearAll();
            LOG.info("Conversion engine successfully stopped");
        } catch (Exception e) {
            LOG.error("", e);
            throw e;
        }
    }

}
