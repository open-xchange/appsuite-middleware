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

package com.openexchange.mail.authenticity.mechanism.spf;

/**
 * {@link SPFResultHeader}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class SPFResultHeader {

    /**
     * Refers to the send domain
     * 
     * @see <a href="https://tools.ietf.org/html/rfc7208#section-9.1">RFC 7208, Section 9.1</a>
     * @see <a href="https://tools.ietf.org/html/rfc7601#section-2.7.2">RFC 7601, Section 2.7.2</a>
     */
    public static final String SMTP_MAILFROM = "smtp.mailfrom";

    /**
     * @see <a href="https://tools.ietf.org/html/rfc7208#section-9.1">RFC 7208, Section 9.1</a>
     * @see <a href="https://tools.ietf.org/html/rfc7601#section-2.7.2">RFC 7601, Section 2.7.2</a>
     */
    public static final String SMTP_HELO = "smtp.helo";
}
