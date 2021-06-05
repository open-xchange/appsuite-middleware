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
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.net.ssl.management.Certificate;
import com.openexchange.net.ssl.management.SSLCertificateManagementService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link GetSSLCertificateAction}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class GetSSLCertificateAction extends AbstractSSLCertificateManagementAction {

    /**
     * Initialises a new {@link GetSSLCertificateAction}.
     * 
     * @param services The {@link ServiceLookup} instance
     */
    public GetSSLCertificateAction(ServiceLookup services) {
        super(services);
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        String fingerprint = requestData.getParameter("fingerprint", String.class, false);
        String hostname = requestData.getParameter("hostname", String.class, true);
        SSLCertificateManagementService managementService = getService(SSLCertificateManagementService.class);

        // Get all exceptions for the specified certificate
        if (Strings.isEmpty(hostname)) {
            List<Certificate> certificates = managementService.get(session.getUserId(), session.getContextId(), fingerprint);
            return new AJAXRequestResult(parse(certificates));
        }

        // Get a specific exception
        Certificate certificate = managementService.get(session.getUserId(), session.getContextId(), hostname, fingerprint);
        return new AJAXRequestResult(parse(certificate));
    }
}
