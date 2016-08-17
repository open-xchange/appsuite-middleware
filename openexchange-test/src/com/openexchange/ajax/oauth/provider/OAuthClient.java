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

package com.openexchange.ajax.oauth.provider;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import com.openexchange.ajax.contact.action.AllRequest;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CustomizedParser;
import com.openexchange.ajax.framework.Header;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.exceptions.OAuthInsufficientScopeException;
import com.openexchange.oauth.provider.exceptions.OAuthInvalidRequestException;
import com.openexchange.oauth.provider.exceptions.OAuthInvalidTokenException;
import com.openexchange.oauth.provider.exceptions.OAuthRequestException;
import com.openexchange.oauth.provider.exceptions.OAuthInvalidTokenException.Reason;
import com.openexchange.oauth.provider.resourceserver.scope.Scope;

/**
 * {@link OAuthClient}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class OAuthClient extends AJAXClient {

    public OAuthClient(String clientId, String clientSecret, String redirectURI, Scope scope) throws Exception {
        this(User.User1, clientId, clientSecret, redirectURI, scope);
    }

    public OAuthClient(User user, String clientId, String clientSecret, String redirectURI, Scope scope) throws Exception {
        super(new OAuthSession(user, clientId, clientSecret, redirectURI, scope), false);
    }

    @Override
    public <T extends AbstractAJAXResponse> T execute(AJAXRequest<T> request) throws OXException, IOException, JSONException {
        return super.execute(new OAuthRequest<>(((OAuthSession) getSession()).getAccessToken(), request));
    }

    private static final class OAuthRequest<T extends AbstractAJAXResponse> implements AJAXRequest<T> {

        private final String accessToken;

        private final AJAXRequest<T> delegate;

        public OAuthRequest(String accessToken, AJAXRequest<T> delegate) {
            super();
            this.accessToken = accessToken;
            this.delegate = delegate;
        }

        @Override
        public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
            return delegate.getMethod();
        }

        @Override
        public String getServletPath() {
            String servletPath = delegate.getServletPath();
            return "/ajax/oauth/modules/" + servletPath.substring(6);
        }

        @Override
        public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
            return delegate.getParameters();
        }

        @Override
        public AbstractAJAXParser<? extends T> getParser() {
            AbstractAJAXParser<? extends T> parser = delegate.getParser();
            return new OAuthResponseParser<>(parser);
        }

        @Override
        public Object getBody() throws IOException, JSONException {
            return delegate.getBody();
        }

        @Override
        public Header[] getHeaders() {
            Header[] headers = delegate.getHeaders();
            if (headers == null) {
                return new Header[] { new Header.SimpleHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken) };
            }

            Header[] newHeaders = new Header[headers.length + 1];
            newHeaders[headers.length] = new Header.SimpleHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
            return newHeaders;
        }
    }

    private static final class OAuthResponseParser<T extends AbstractAJAXResponse> extends CustomizedParser<T> {

        private OAuthRequestException e;

        private boolean tryParseOAuthError;

        protected OAuthResponseParser(AbstractAJAXParser<T> delegate) {
            super(delegate);
        }

        @Override
        protected String checkCustom(HttpResponse resp) throws ParseException, IOException {
            if (isPossibleOAuthError(resp)) {
                tryParseOAuthError = true;
                String body = EntityUtils.toString(resp.getEntity());
                if (body == null) {
                    fail("Response code is not okay: " + resp.getStatusLine().getStatusCode() + " (" + resp.getStatusLine().getReasonPhrase() + ")");
                }
                return body;
            }

            return null;
        }

        @Override
        public T parse(String body) throws JSONException {
            if (tryParseOAuthError) {
                try {
                    JSONObject jsonObject = new JSONObject(body);
                    String error = jsonObject.getString("error");
                    switch (error) {
                        case "invalid_token":
                            String description = jsonObject.getString("error_description");
                            Reason reason = null;
                            for (Entry<Reason, String> entry : OAuthInvalidTokenException.DESCRIPTIONS.entrySet()) {
                                if (description.equals(entry.getValue())) {
                                    reason = entry.getKey();
                                    break;
                                }
                            }
                            e = new OAuthInvalidTokenException(reason);
                            break;
                        case "insufficient_scope":
                            String requiredScope = jsonObject.optString("scope", null);
                            if (requiredScope == null) {
                                e = new OAuthInsufficientScopeException();
                            } else {
                                e = new OAuthInsufficientScopeException(requiredScope);
                            }
                            break;
                        case "invalid_request":
                            e = new OAuthInvalidRequestException();
                            break;
                    }
                } catch (JSONException e) {
                    fail("Error: " + body);
                }

                if (e == null) {
                    fail("Error: " + body);
                } else {
                    if (isFailOnError()) {
                        throw new AssertionError("OAuth error", e);
                    }
                    Response response = new Response();
                    response.setException(e);
                    return super.createResponse(response);
                }
            }

            return super.parse(body);
        }

        boolean isPossibleOAuthError(HttpResponse resp) {
            Set<Integer> codes = new HashSet<>();
            codes.add(HttpStatus.SC_BAD_REQUEST);
            codes.add(HttpStatus.SC_FORBIDDEN);
            codes.add(HttpStatus.SC_UNAUTHORIZED);
            return codes.contains(resp.getStatusLine().getStatusCode());
        }

    }

    /**
     * Asserts that this client has API access. Will fail if not.
     */
    public void assertAccess() throws Exception {
        int privateContactFolder = getValues().getPrivateContactFolder();
        CommonAllResponse allResponse = execute(new AllRequest(privateContactFolder, AllRequest.GUI_COLUMNS));
        Assert.assertFalse(allResponse.hasError());
    }

}
