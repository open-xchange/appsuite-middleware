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

package com.openexchange.dispatcher;


/**
 * {@link Headerizable} - Marks implementing class as parameterizable.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface Headerizable {

    /**
     * Puts given name-value-pair into this data's headers.
     * <p>
     * A <code>null</code> value removes the mapping.
     *
     * @param name The header name
     * @param value The header value
     * @throws NullPointerException If name is <code>null</code>
     */
    void setHeader(String name, String value);

    /**
     * Gets specified header's value.
     *
     * @param name The header name
     * @return The <code>String</code> representing the single value of the header
     * @throws NullPointerException If name is <code>null</code>
     */
    String getHeader(String name);

}
