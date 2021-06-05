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

package com.openexchange.mail.authenticity.mechanism.dkim;

/**
 * {@link DKIMResultHeader}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class DKIMResultHeader {

    /**
     * A free-form comment on the reason the given result was returned
     * 
     * @see <a href="https://tools.ietf.org/html/rfc7601#section-2.2">RFC 7601, Section 2.2</a>
     */
    public static final String REASON = "reason";

    /**
     * Refers to the content of the agent or user identifier (AUID) on behalf of which the
     * signing domain is taking responsibility.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc6376#section-3.5">RFC 6376, Section 3.5</a>
     */
    public static final String HEADER_I = "header.i";

    /**
     * Refers to the content of the signing domain tag from within the signature header field, and
     * not a distinct header field called "d".
     * 
     * @see <a href="https://tools.ietf.org/html/rfc6376#section-3.5">RFC 6376, Section 3.5</a>
     */
    public static final String HEADER_D = "header.d";

    /**
     * Refers to the DKIM signature data (base64)
     */
    public static final String HEADER_B = "header.b";
}
