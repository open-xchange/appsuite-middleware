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

package com.openexchange.ajax.customizer;

import java.util.List;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AdditionalField}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.8.0
 */
public interface AdditionalField<T> {

    /**
     * Gets the column identifier
     *
     * @return The column identifier
     */
    int getColumnID();

    /**
     * Gets the column name
     *
     * @return The column name
     */
    String getColumnName();

    /**
     * Gets the value for given folder
     *
     * @param folder The folder
     * @param session The associated session
     * @return The value
     */
    Object getValue(T item, ServerSession session);

    /**
     * Gets the multiple value for given folder
     *
     * @param folder The folder
     * @param session The associated session
     * @return The multiple values
     */
    List<Object> getValues(List<T> items, ServerSession session);

    /**
     * Renders passed value to its JSON representation
     *
     * @param requestData The underlying request data, or <code>null</code> if not available
     * @param value The value
     * @return The JSON representation
     */
    Object renderJSON(AJAXRequestData requestData, Object value);

}
