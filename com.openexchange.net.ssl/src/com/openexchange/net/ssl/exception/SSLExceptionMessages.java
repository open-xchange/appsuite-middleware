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

package com.openexchange.net.ssl.exception;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link SSLExceptionMessages}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.3
 */
public class SSLExceptionMessages implements LocalizableStrings {

    // The certificate for domain '%2$s' is untrusted.
    public final static String UNTRUSTED_CERTIFICATE_MSG = "The certificate for domain '%2$s' is untrusted.";

    // The certificate for domain '%2$s' is untrusted. You can change your general trust level in the settings.
    public final static String UNTRUSTED_CERT_USER_CONFIG_MSG = "The certificate for domain '%2$s' is untrusted. You can change your general trust level in the settings.";

    // The certificate is not trusted by the user.
    public final static String USER_DOES_NOT_TRUST_CERTIFICATE = "The certificate with is not trusted by the user.";

    // The root certificate issued by '%2$s' is not trusted
    public final static String UNTRUSTED_ROOT_CERTIFICATE = "The root certificate issued by '%2$s' is not trusted";

    // The certificate is self-signed
    public final static String SELF_SIGNED_CERTIFICATE = "The certificate is self-signed";

    // The certificate is expired
    public final static String CERTIFICATE_IS_EXPIRED = "The certificate is expired";

    // The common name for the certificate is invalid
    public final static String INVALID_COMMON_NAME = "The common name for the certificate is invalid";

    // The root authority for the certificate is untrusted
    public final static String UNTRUSTED_ROOT_AUTHORITY = "The root authority for the certificate is untrusted";

    // The certificate is using a weak algorithm
    public final static String WEAK_ALGORITHM = "The certificate is using a weak algorithm";

    // The certificate was revoked
    public final static String CERTIFICATE_REVOKED = "The certificate was revoked";

    private SSLExceptionMessages() {
        super();
    }

}
