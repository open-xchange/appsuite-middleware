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

package com.openexchange.icap;

/**
 * {@link ICAPCommons}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public final class ICAPCommons {

    /**
     * The current ICAP version.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc3507#section-4.3.2">RFC-3507, Section 4.3.2</a>
     */
    public static final String ICAP_VERSION = "1.0";

    /**
     * The user agent
     */
    public static final String USER_AGENT = "Open-Xchange ICAP Client/" + ICAPClientVersion.CLIENT_VERSION;

    /**
     * The default ICAP server port.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc3507#section-4.1">RFC-3507, Section 4.1</a>
     */
    public static final int DEFAULT_PORT = 1344;
}
