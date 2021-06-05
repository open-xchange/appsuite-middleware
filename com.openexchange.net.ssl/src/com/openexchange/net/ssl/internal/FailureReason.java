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

package com.openexchange.net.ssl.internal;

/**
 * {@link FailureReason}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since 7.8.4
 */
enum FailureReason {
    UNTRUSTED_CERTIFICATE(FailureReasonMessage.UNTRUSTED_CERTIFICATE),
    SELF_SIGNED(FailureReasonMessage.SELF_SIGNED),
    UNTRUSTED_ISSUER(FailureReasonMessage.UNTRUSTED_ISSUER),
    EXPIRED(FailureReasonMessage.EXPIRED),
    NOT_TRUSTED_BY_USER(FailureReasonMessage.NOT_TRUSTED_BY_USER),
    INVALID_COMMON_NAME(FailureReasonMessage.INVALID_COMMON_NAME),
    ALGORITHM_CONSTRAINED(FailureReasonMessage.ALGORITHM_CONSTRAINED),
    REVOKED(FailureReasonMessage.REVOKED),
    
    ;

    private final String detail;

    /**
     * Initialises a new {@link FailureReason}.
     *
     * @param detail The detail message for the failure reason
     */
    private FailureReason(String detail) {
        this.detail = detail;
    }

    /**
     * Gets the detail
     *
     * @return The detail
     */
    public String getDetail() {
        return detail;
    }
}
