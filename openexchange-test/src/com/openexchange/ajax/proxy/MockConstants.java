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

package com.openexchange.ajax.proxy;

/**
 * {@link MockConstants}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class MockConstants {

    // Describes the method the mocked call will be from
    public static final String METHOD_KEY = "method";

    // Describes the URI the mocked call should target
    public static final String URI_KEY = "uri";

    // Describes a delay (in seconds) the server should have when returning the response
    public static final String DELAY_KEY = "delay";

    /* ------------ Fields for mocked response ---------------- */

    public static final String STATUS_CODE_KEY = "statusCode";

    public static final String HEADERS_KEY = "headers";

}
