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

package com.openexchange.jump;

import java.util.Map;

/**
 * {@link Endpoint} - An end-point for a jump target.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface Endpoint {

    /**
     * Gets the URL for this end-point; e.g. <code>"http://127.0.0.1/identities?action=receiveIdentity"</code>
     *
     * @return The URL
     */
    String getUrl();

    /**
     * Gets the system name (in lower-case) associated with this end-point
     * <p>
     * Serves as an identifier and must therefore be unique among OSGi-wise registered and/or configured (via <i>tokens.properties</i>)
     * end-points.
     *
     * @return The system name
     */
    String getSystemName();

    /**
     * Gets arbitrary properties available for this end-point
     *
     * @return The properties
     */
    Map<String, Object> getProperties();

    /**
     * Gets the denoted property.
     *
     * @param propName The property name
     * @return The associated value or <code>null</code>
     */
    Object getProperty(String propName);

}
