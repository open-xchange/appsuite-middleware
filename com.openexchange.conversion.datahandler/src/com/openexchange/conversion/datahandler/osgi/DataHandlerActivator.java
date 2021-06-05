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

package com.openexchange.conversion.datahandler.osgi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.conversion.DataHandler;
import com.openexchange.conversion.datahandler.DataHandlers;
import com.openexchange.conversion.datahandler.Json2OXExceptionDataHandler;
import com.openexchange.conversion.datahandler.OXException2JsonDataHandler;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link DataHandlerActivator}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class DataHandlerActivator extends HousekeepingActivator {

    private static final Logger LOG = LoggerFactory.getLogger(DataHandlerActivator.class);

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting bundle {}", context.getBundle().getSymbolicName());
        registerService(DataHandler.class, new Json2OXExceptionDataHandler(), singletonDictionary("identifier", DataHandlers.JSON2OXEXCEPTION));
        registerService(DataHandler.class, new OXException2JsonDataHandler(), singletonDictionary("identifier", DataHandlers.OXEXCEPTION2JSON));
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("Stopping bundle {}", context.getBundle().getSymbolicName());
        super.stopBundle();
    }
}
