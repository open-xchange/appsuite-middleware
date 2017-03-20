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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.net.ssl.management.json.action;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.exception.OXException;
import com.openexchange.net.ssl.management.Certificate;
import com.openexchange.net.ssl.management.DefaultCertificate;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link AbstractSSLCertificateManagementAction}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
abstract class AbstractSSLCertificateManagementAction implements AJAXActionService {

    private final ServiceLookup services;

    /**
     * Initialises a new {@link AbstractSSLCertificateManagementAction}.
     *
     * @param services The {@link ServiceLookup} instance
     */
    AbstractSSLCertificateManagementAction(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Retrieves the specified service
     *
     * @param clazz The service {@link Class}
     * @return The Service
     * @throws OXException if the service is absent
     */
    <S> S getService(Class<S> clazz) throws OXException {
        S service = services.getService(clazz);
        if (service == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(clazz.getSimpleName());
        }
        return service;
    }

    /**
     *
     * @param certificates
     * @return
     * @throws OXException
     */
    JSONArray parse(List<Certificate> certificates) throws OXException {
        JSONArray array = new JSONArray(certificates.size());
        for (Certificate certificate : certificates) {
            array.put(parse(certificate));
        }
        return array;
    }

    /**
     * Parses the specified {@link Certificate} to a {@link JSONObject}
     *
     * @param certificate The {@link Certificate} to parse
     * @return The {@link JSONObject}
     * @throws OXException if a JSON error occurs
     */
    JSONObject parse(Certificate certificate) throws OXException {
        try {
            JSONObject json = new JSONObject();
            json.put(CertificateFields.FINGERPRINT, certificate.getFingerprint());
            json.put(CertificateFields.ISSUED_ON, certificate.getIssuedOnTimestamp());
            json.put(CertificateFields.EXPIRES_ON, certificate.getExpirationTimestamp());
            json.put(CertificateFields.HOSTNAME, certificate.getHostName());
            json.put(CertificateFields.COMMON_NAME, certificate.getCommonName());
            json.put(CertificateFields.ISSUED_BY, certificate.getIssuer());
            json.put(CertificateFields.SIGNATURE, certificate.getSignature());
            json.put(CertificateFields.SERIAL_NUMBER, certificate.getSerialNumber());
            json.put(CertificateFields.FAILURE_REASON, certificate.getFailureReason());
            json.put(CertificateFields.EXPIRED, certificate.isExpired());
            json.put(CertificateFields.TRUSTED, certificate.isTrusted());
            return json;
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e);
        }
    }

    /**
     * Parses the specified {@link JSONObject} to a {@link Certificate}
     *
     * @param jsonObject The {@link JSONObject} to parse
     * @return The {@link Certificate}
     */
    Certificate parse(JSONObject jsonObject) throws OXException {
        try {
            DefaultCertificate.Builder certificate = DefaultCertificate.builder()
                .fingerprint(jsonObject.getString(CertificateFields.FINGERPRINT))

                // Mandatory
                .hostName(jsonObject.getString(CertificateFields.HOSTNAME))
                .trusted(jsonObject.getBoolean(CertificateFields.TRUSTED))

                // Optional
                .issuedOnTimestamp(jsonObject.optLong(CertificateFields.ISSUED_ON))
                .expirationTimestamp(jsonObject.optLong(CertificateFields.EXPIRES_ON))
                .commonName(jsonObject.optString(CertificateFields.COMMON_NAME))
                .issuer(jsonObject.optString(CertificateFields.ISSUED_BY))
                .signature(jsonObject.optString(CertificateFields.SIGNATURE))
                .serialNumber(jsonObject.optString(CertificateFields.SERIAL_NUMBER))
                .failureReason(jsonObject.optString(CertificateFields.FAILURE_REASON))
                .expired(jsonObject.optBoolean(CertificateFields.EXPIRED));

            return certificate.build();
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e);
        }
    }
}
