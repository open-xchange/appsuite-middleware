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

package com.openexchange.rest.client.endpointpool;


/**
 * {@link Endpoint} - Represents an (API) end-point; e.g. <code>"https://my.service.invalid/v1/service"</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public interface Endpoint {

    /**
     * Gets the base URI; e.g. <code>"https://my.service.invalid/v1/service"</code>.
     *
     * @return The end-point's base URI (w/o trailing slash)
     */
    String getBaseUri();

    /**
     * Gets the concatenated URI consisting of end-point's base URI and specified path.
     *
     * @param path The path to append to base URI; e.g. <code>"/container/resource1"</code>
     * @return The concatenated URI
     */
    String getConcatenatedUri(Path path);

}
