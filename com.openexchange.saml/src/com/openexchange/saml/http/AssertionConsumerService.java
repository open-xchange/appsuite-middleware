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

package com.openexchange.saml.http;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.saml.SAMLConfig.Binding;
import com.openexchange.saml.SAMLWebSSOProvider;
import com.openexchange.saml.spi.ExceptionHandler;


/**
 * The HTTP endpoint that receives authentication responses via POST.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class AssertionConsumerService extends SAMLServlet {

    private static final Logger LOG = LoggerFactory.getLogger(AssertionConsumerService.class);

    private static final long serialVersionUID = -8019507819002031614L;

    /**
     * Initializes a new {@link AssertionConsumerService}.
     * @param provider
     * @param exceptionHandler
     */
    public AssertionConsumerService(SAMLWebSSOProvider provider, ExceptionHandler exceptionHandler) {
        super(provider, exceptionHandler);
    }

    @Override
    protected void doPost(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException, IOException {
        try {
            provider.handleAuthnResponse(httpRequest, httpResponse, Binding.HTTP_POST);
        } catch (OXException e) {
            LOG.error("Error while handling SAML authentication response", e);
            exceptionHandler.handleAuthnResponseFailed(httpRequest, httpResponse, e);
        }
    }

}
