/*-
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
import com.openexchange.oauth.OAuthHTTPClientFactory;
import com.openexchange.oauth.OAuthService;
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
