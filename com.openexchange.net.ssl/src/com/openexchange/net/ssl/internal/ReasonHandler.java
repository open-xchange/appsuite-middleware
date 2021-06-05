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

import java.security.cert.CertPathValidatorException.BasicReason;
import java.security.cert.CertPathValidatorException.Reason;
import java.security.cert.PKIXReason;
import com.openexchange.net.ssl.exception.SSLExceptionCode;

/**
 * {@link ReasonHandler}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
enum ReasonHandler {

    /**
     * Handles all {@link BasicReason} failures
     */
    BASIC(BasicReason.class) {

        @Override
        public FailureResponse checkReason(Reason reason) {
            BasicReason br = (BasicReason) reason;
            switch (br) {
                case ALGORITHM_CONSTRAINED:
                    return new FailureResponse(FailureReason.ALGORITHM_CONSTRAINED, SSLExceptionCode.WEAK_ALGORITHM);
                case EXPIRED:
                    return new FailureResponse(FailureReason.EXPIRED, SSLExceptionCode.CERTIFICATE_IS_EXPIRED);
                case INVALID_SIGNATURE:
                    break;
                case NOT_YET_VALID:
                    break;
                case REVOKED:
                    return new FailureResponse(FailureReason.REVOKED, SSLExceptionCode.CERTIFICATE_REVOKED);
                case UNDETERMINED_REVOCATION_STATUS:
                    break;
                case UNSPECIFIED:
                    break;
            }
            return DEFAULT_RESPONSE;
        }
    },

    /**
     * Handls all {@link PKIXReason} failures
     */
    PKIX(PKIXReason.class) {

        @Override
        public FailureResponse checkReason(Reason reason) {
            PKIXReason pr = (PKIXReason) reason;
            switch (pr) {
                case INVALID_KEY_USAGE:
                    break;
                case INVALID_NAME:
                    return new FailureResponse(FailureReason.INVALID_COMMON_NAME, SSLExceptionCode.INVALID_HOSTNAME);
                case INVALID_POLICY:
                    break;
                case NAME_CHAINING:
                    break;
                case NOT_CA_CERT:
                    break;
                case NO_TRUST_ANCHOR:
                    break;
                case PATH_TOO_LONG:
                    break;
                case UNRECOGNIZED_CRIT_EXT:
                    break;
            }
            return DEFAULT_RESPONSE;
        }
    };

    /** The default failure response */
    static final FailureResponse DEFAULT_RESPONSE = new FailureResponse(FailureReason.UNTRUSTED_CERTIFICATE, SSLExceptionCode.UNTRUSTED_CERTIFICATE);

    private Class<?> clazz;

    /**
     * Initialises a new {@link ReasonHandler}.
     */
    private ReasonHandler(Class<?> clazz) {
        this.clazz = clazz;
    }

    /**
     * Handles the specified {@link Reason} of failure
     *
     * @param reason The {@link Reason} to handle
     */
    public static FailureResponse handle(Reason reason) {
        for (ReasonHandler rh : ReasonHandler.values()) {
            if (reason.getClass().equals(rh.clazz)) {
                return rh.checkReason(reason);
            }
        }
        return DEFAULT_RESPONSE;
    }

    /**
     * Checks the {@link Reason} of failure
     *
     * @param reason The {@link Reason} to check
     */
    public abstract FailureResponse checkReason(Reason reason);
}
