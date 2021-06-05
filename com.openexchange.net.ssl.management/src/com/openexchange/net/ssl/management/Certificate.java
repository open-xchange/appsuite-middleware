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

package com.openexchange.net.ssl.management;

/**
 * {@link Certificate} - Represents a user-managed certificate that is either trusted or untrusted.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.8.4
 */
public interface Certificate {

    /**
     * Gets the fingerprint
     *
     * @return The fingerprint
     */
    String getFingerprint();

    /**
     * Gets the expirationTimestamp
     *
     * @return The expirationTimestamp
     */
    long getExpirationTimestamp();

    /**
     * Gets the trusted flag
     *
     * @return The trusted flag
     */
    boolean isTrusted();

    /**
     * Gets the expired flag
     *
     * @return The expired flag
     */
    boolean isExpired();

    /**
     * Gets the issuedOnTimestamp
     *
     * @return The issuedOnTimestamp
     */
    long getIssuedOnTimestamp();

    /**
     * Gets the issuer
     *
     * @return The issuer
     */
    String getIssuer();

    /**
     * Gets the signature
     *
     * @return The signature
     */
    String getSignature();

    /**
     * Gets the serialNumber
     *
     * @return The serialNumber
     */
    String getSerialNumber();

    /**
     * Gets the failureReason
     *
     * @return The failureReason
     */
    String getFailureReason();

    /**
     * Gets the host name
     *
     * @return The host name
     */
    String getHostName();

    /**
     * Gets the commonName
     *
     * @return The commonName
     */
    String getCommonName();
}
