/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
    private static final FailureResponse DEFAULT_RESPONSE = new FailureResponse(FailureReason.UNTRUSTED_CERTIFICATE, SSLExceptionCode.UNTRUSTED_CERTIFICATE);

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
