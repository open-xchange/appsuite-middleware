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

package com.openexchange.oauth.json.proxy;

import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.http.client.HTTPClient;
import com.openexchange.http.client.builder.HTTPDeleteRequestBuilder;
import com.openexchange.http.client.builder.HTTPGenericRequestBuilder;
import com.openexchange.http.client.builder.HTTPGetRequestBuilder;
import com.openexchange.http.client.builder.HTTPPostRequestBuilder;
import com.openexchange.http.client.builder.HTTPPutRequestBuilder;
import com.openexchange.http.client.builder.HTTPRequest;
import com.openexchange.http.client.builder.HTTPResponse;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.http.OAuthHTTPClientFactory;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

public class OAuthProxyAction implements AJAXActionService {

    private final OAuthService oauthService;
    private final OAuthHTTPClientFactory clients;

    public OAuthProxyAction(OAuthService service, OAuthHTTPClientFactory clients) {
        oauthService = service;
        this.clients = clients;
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {

        OAuthProxyRequest proxyRequest = new OAuthProxyRequest(requestData, session, oauthService);

        HTTPClient client = clients.create(proxyRequest.getAccount(), session);

        final HTTPRequest httpRequest;
        switch (proxyRequest.getMethod()) {
        case GET:
            httpRequest = buildGet(proxyRequest, client);
            break;
        case DELETE:
            httpRequest = buildDelete(proxyRequest, client);
            break;
        case PUT:
            httpRequest = buildPut(proxyRequest, client);
            break;
        case POST:
            httpRequest = buildPost(proxyRequest, client);
            break;
        default:
            throw AjaxExceptionCodes.UNKNOWN_ACTION.create(proxyRequest.getMethod().toString());
        }
        HTTPResponse httpResponse = httpRequest.execute();
        String payload = httpResponse.getPayload(String.class);
        return new AJAXRequestResult(payload, "string");
    }

    private HTTPRequest buildPost(OAuthProxyRequest proxyRequest, HTTPClient client) throws OXException {

        HTTPPostRequestBuilder builder = client.getBuilder().post();

        buildCommon(proxyRequest, builder);

        return builder.build();
    }

    private HTTPRequest buildPut(OAuthProxyRequest proxyRequest, HTTPClient client) throws OXException {

        HTTPPutRequestBuilder builder = client.getBuilder().put();

        buildCommon(proxyRequest, builder);
        builder.body(proxyRequest.getBody());
        return builder.build();
    }

    private HTTPRequest buildGet(OAuthProxyRequest proxyRequest, HTTPClient client) throws OXException {

        HTTPGetRequestBuilder builder = client.getBuilder().get();

        buildCommon(proxyRequest, builder);

        return builder.build();

    }

    private HTTPRequest buildDelete(OAuthProxyRequest proxyRequest, HTTPClient client) throws OXException {

        HTTPDeleteRequestBuilder builder = client.getBuilder().delete();

        buildCommon(proxyRequest, builder);

        return builder.build();

    }

    private void buildCommon(OAuthProxyRequest proxyRequest, HTTPGenericRequestBuilder<?> builder) throws OXException {
        builder.url(proxyRequest.getUrl()).headers(proxyRequest.getHeaders()).parameters(proxyRequest.getParameters());
    }

}
