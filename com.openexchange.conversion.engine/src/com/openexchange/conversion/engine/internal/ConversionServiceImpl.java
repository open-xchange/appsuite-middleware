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

package com.openexchange.conversion.engine.internal;

import static com.openexchange.conversion.engine.internal.ConversionEngineRegistry.getInstance;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.exception.OXException;
import com.openexchange.conversion.DataExceptionCodes;
import com.openexchange.conversion.DataHandler;
import com.openexchange.conversion.DataSource;
import com.openexchange.conversion.SimpleData;
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
