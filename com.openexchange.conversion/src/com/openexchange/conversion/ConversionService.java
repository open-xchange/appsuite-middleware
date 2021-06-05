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

package com.openexchange.conversion;

import java.io.InputStream;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link ConversionService} - The conversion service which offers look-up methods for {@link DataSource data sources} and
 * {@link DataHandler data handlers}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SingletonService
public interface ConversionService {

    /**
     * Gets the data source associated with specified identifier.
     * <p>
     * The identifier should correspond to java's package naming; e.g.<br>
     * <code>&quot;my.path.to.specific.datasource&quot;</code>
     *
     * @param identifier The identifier string
     * @return The data source associated with specified identifier or <code>null</code>.
     */
    public DataSource getDataSource(String identifier);

    /**
     * Gets the data handler associated with specified identifier.
     * <p>
     * The identifier should correspond to java's package naming; e.g.<br>
     * <code>&quot;my.path.to.specific.datahandler&quot;</code>
     *
     * @param identifier The identifier string
     * @return The data handler associated with specified identifier or <code>null</code>.
     */
    public DataHandler getDataHandler(String identifier);

    /**
     * Looks-up and checks appropriate {@link DataSource data source} and {@link DataHandler data handler}. Then the
     * {@link DataHandler#processData(Object, DataArguments, Session)} method is triggered with
     * {@link DataSource#getData(Class, DataArguments, Session)} as input invoked with a matching supported type.
     *
     * @param dataSourceIdentifier The data source identifier
     * @param dataSourceArguments The data source arguments
     * @param dataHandlerIdentifier The data handler identifier
     * @param dataHandlerArguments The data handler arguments
     * @param session The session providing needed user data
     * @return The resulting object from data handler
     * @throws OXException If conversion fails
     */
    public Object convert(String dataSourceIdentifier, DataArguments dataSourceArguments, String dataHandlerIdentifier, DataArguments dataHandlerArguments, Session session) throws OXException;

    /**
     * Looks-up and checks appropriate {@link DataHandler data handler}. Then the
     * {@link DataHandler#processData(Object, DataArguments, Session)} method is triggered with specified input stream as input provided
     * that {@link DataHandler data handler} supports {@link InputStream} class.
     *
     * @param inputStream The input stream
     * @param dataHandlerIdentifier The data handler identifier
     * @param dataHandlerArguments The data handler arguments
     * @param session The session providing needed user data
     * @return The resulting object from data handler
     * @throws OXException If conversion fails
     */
    public Object convert(InputStream inputStream, String dataHandlerIdentifier, DataArguments dataHandlerArguments, Session session) throws OXException;

}
