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

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link FailureReasonMessage}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since 7.8.4
 */
public class FailureReasonMessage implements LocalizableStrings {

    // The certificate is self-signed
    public static final String SELF_SIGNED = "The certificate is self-signed";

    // The certificate was signed by an untrusted issuer
    public static final String UNTRUSTED_ISSUER = "The certificate was signed by an untrusted issuer";

    // The certificate is expired
    public static final String EXPIRED = "The certificate is expired";

    // The user does not trust this certificate
    public static final String NOT_TRUSTED_BY_USER = "The user does not trust this certificate";

    // Invalid common name
    public static final String INVALID_COMMON_NAME = "Invalid common name";

    // The certificate is untrusted
    public static final String UNTRUSTED_CERTIFICATE = "The certificate is untrusted";

    // The certificate is using a weak algorithm
    public static final String ALGORITHM_CONSTRAINED = "The certificate is using a weak algorithm";

    // The certificate was revoked
    public static final String REVOKED = "The certificate was revoked";
}
