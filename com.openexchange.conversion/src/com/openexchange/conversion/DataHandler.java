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

import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link DataHandler} - A data handler that converts specified data to a certain object.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface DataHandler {

    /**
     * Gets the required arguments which must be present in corresponding instance of {@link DataArguments} when performing the
     * {@link #processData(Object, DataArguments, Session)} method
     *
     * @return The required arguments
     */
    public String[] getRequiredArguments();

    /**
     * Gets the supported types of this data handler
     *
     * @return The supported types of this data handler
     */
    public Class<?>[] getTypes();

    /**
     * Processes given data.
     *
     * @param data The data to process
     * @param dataArguments The data arguments needed for processing data
     * @param session The session providing needed user data
     * @return The result of converted data ready for being put into JSON response
     * @throws OXException If data cannot be handled by this data handler
     */
    public ConversionResult processData(Data<? extends Object> data, DataArguments dataArguments, Session session) throws OXException;
}
