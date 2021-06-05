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
