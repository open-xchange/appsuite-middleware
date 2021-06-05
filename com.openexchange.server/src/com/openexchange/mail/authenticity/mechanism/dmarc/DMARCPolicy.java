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

package com.openexchange.mail.authenticity.mechanism.dmarc;

/**
 * {@link DMARCPolicy} - Defines the requested Mail Receiver policy
 * (plain-text; REQUIRED for policy records). Indicates the policy
 * to be enacted by the Receiver at the request of the Domain Owner.
 * Policy applies to the domain queried and to subdomains, unless
 * subdomain policy is explicitly described using the "sp" tag.
 * This tag is mandatory for policy records only, but not for
 * third-party reporting records
 *
 * @see <a href="https://tools.ietf.org/html/rfc7489#section-6.3">RFC-7489, Section 6.3</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public enum DMARCPolicy {

    /**
     * The Domain Owner requests no specific action be taken
     * regarding delivery of messages.
     */
    none,
    /**
     * The Domain Owner wishes to have email that fails the
     * DMARC mechanism check be treated by Mail Receivers as
     * suspicious. Depending on the capabilities of the Mail
     * Receiver, this can mean "place into spam folder", "scrutinize
     * with additional intensity", and/or "flag as suspicious".
     */
    quarantine,
    /**
     * The Domain Owner wishes for Mail Receivers to reject
     * email that fails the DMARC mechanism check. Rejection SHOULD
     * occur during the SMTP transaction. See
     * <a href="https://tools.ietf.org/html/rfc7489#section-10.3">RFC-7489, Section 10.3</a>
     * for some discussion of SMTP rejection methods and their implications.
     */
    reject
}
