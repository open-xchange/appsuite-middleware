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

package com.openexchange.ajax.requesthandler.responseRenderers;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.container.SecureContentResponse;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.ResponseRenderer;


/**
 * {@link SecureContentAPIResponseRenderer} is similiar to a normal {@link APIResponseRenderer}.
 *
 * The only addition is, that it only set the Content-Security-Policy headers from {@link HttpServletResponse} to a less strict policy.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public class SecureContentAPIResponseRenderer implements ResponseRenderer {


    private final APIResponseRenderer delegate;
    private static final String SECURITY_POLICY="unsafe-inline";

    /**
     * Initializes a new {@link SecureContentAPIResponseRenderer}.
     */
    public SecureContentAPIResponseRenderer(APIResponseRenderer delegate) {
        super();
        this.delegate=delegate;
    }

    @Override
    public boolean handles(AJAXRequestData request, AJAXRequestResult result) {

        return result.getResultObject() instanceof SecureContentResponse;
    }

    @Override
    public int getRanking() {
        return delegate.getRanking();
    }

    @Override
    public void write(AJAXRequestData request, AJAXRequestResult result, HttpServletRequest httpReq, HttpServletResponse httpResp) throws IOException {
        // deactivate Content-Security-Policy
        httpResp.setHeader("Content-Security-Policy", SECURITY_POLICY);
        httpResp.setHeader("X-WebKit-CSP", SECURITY_POLICY);
        httpResp.setHeader("X-Content-Security-Policy", SECURITY_POLICY);

        AJAXRequestResult secureResult = new AJAXRequestResult();
        secureResult.setResultObject(((SecureContentResponse)result.getResultObject()).getResponse());
        secureResult.setContinuationUuid(result.getContinuationUuid());
        delegate.write(request, secureResult, httpReq, httpResp);
    }

}
