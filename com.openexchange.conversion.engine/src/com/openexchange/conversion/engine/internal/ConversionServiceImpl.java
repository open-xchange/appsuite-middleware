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

package com.openexchange.conversion.engine.internal;

import static com.openexchange.conversion.engine.internal.ConversionEngineRegistry.getInstance;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataExceptionCodes;
import com.openexchange.conversion.DataHandler;
import com.openexchange.conversion.DataSource;
import com.openexchange.conversion.SimpleData;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link ConversionServiceImpl} - Implementation of {@link ConversionService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ConversionServiceImpl implements ConversionService {

    /**
     * Initializes a new {@link ConversionServiceImpl}
     */
    public ConversionServiceImpl() {
        super();
    }

    @Override
    public DataHandler getDataHandler(final String identifier) {
        return getInstance().getDataHandler(identifier);
    }

    @Override
    public DataSource getDataSource(final String identifier) {
        return getInstance().getDataSource(identifier);
    }

    @Override
    public Object convert(final String dataSourceIdentifier, final DataArguments dataSourceArguments, final String dataHandlerIdentifier, final DataArguments dataHandlerArguments, final Session session) throws OXException {
        final DataSource dataSource = lookUpAndCheckDataSource(dataSourceIdentifier, dataSourceArguments);
        final DataHandler dataHandler = lookUpAndCheckDataHandler(dataHandlerIdentifier, dataHandlerArguments);
        /*
         * Check for matching type
         */
        final Class<?> type = findMatchingType(dataSource, dataHandler);
        if (type == null) {
            throw DataExceptionCodes.NO_MATCHING_TYPE.create(dataSourceIdentifier, dataHandlerIdentifier);
        }
        /*
         * Get data from data source
         */
        final Data<?> data = dataSource.getData(type, dataSourceArguments, session);
        /*
         * ... and feed it to data handler
         */
        return dataHandler.processData(data, dataHandlerArguments, session);
    }

    @Override
    public Object convert(final InputStream inputStream, final String dataHandlerIdentifier, final DataArguments dataHandlerArguments, final Session session) throws OXException {
        final DataHandler dataHandler = lookUpAndCheckDataHandler(dataHandlerIdentifier, dataHandlerArguments);
        /*
         * Check for input stream support
         */
        if (!new HashSet<Class<?>>(Arrays.asList(dataHandler.getTypes())).contains(InputStream.class)) {
            throw DataExceptionCodes.NO_MATCHING_TYPE.create(InputStream.class.getName(), dataHandlerIdentifier);
        }
        /*
         * Feed input stream to data handler
         */
        return dataHandler.processData(new SimpleData<InputStream>(inputStream), dataHandlerArguments, session);
    }

    private static DataSource lookUpAndCheckDataSource(final String dataSourceID, final DataArguments dataSourceArguments) throws OXException {
        final DataSource dataSource = getInstance().getDataSource(dataSourceID);
        if (dataSource == null) {
            throw DataExceptionCodes.UNKNOWN_DATA_SOURCE.create(dataSourceID);
        }
        final String[] args = dataSource.getRequiredArguments();
        for (final String arg : args) {
            if (!dataSourceArguments.containsKey(arg)) {
                throw DataExceptionCodes.MISSING_ARGUMENT.create(arg);
            }
        }
        return dataSource;
    }

    private static DataHandler lookUpAndCheckDataHandler(final String dataHandlerID, final DataArguments dataHandlerArguments) throws OXException {
        final DataHandler dataHandler = getInstance().getDataHandler(dataHandlerID);
        if (dataHandler == null) {
            throw DataExceptionCodes.UNKNOWN_DATA_HANDLER.create(dataHandlerID);
        }
        final String[] args = dataHandler.getRequiredArguments();
        for (final String arg : args) {
            if (!dataHandlerArguments.containsKey(arg)) {
                throw DataExceptionCodes.MISSING_ARGUMENT.create(arg);
            }
        }
        return dataHandler;
    }

    private static Class<?> findMatchingType(final DataSource dataSource, final DataHandler dataHandler) {
        final Class<?>[] dataSourceTypes = dataSource.getTypes();
        final Set<Class<?>> dataHandlerTypes = new HashSet<Class<?>>(Arrays.asList(dataHandler.getTypes()));
        /*
         * Find matching type
         */
        Class<?> type = null;
        for (int i = 0; i < dataSourceTypes.length && type == null; i++) {
            if (dataHandlerTypes.contains(dataSourceTypes[i])) {
                type = dataSourceTypes[i];
            }
        }
        return type;
    }

}
