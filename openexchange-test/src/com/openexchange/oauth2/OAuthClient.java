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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.oauth2;

import java.io.IOException;
import org.apache.http.HttpHeaders;
import org.json.JSONException;
import org.junit.Assert;
import com.openexchange.ajax.contact.action.AllRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.Header;
import com.openexchange.exception.OXException;


/**
 * {@link OAuthClient}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class OAuthClient extends AJAXClient {

    public OAuthClient(String clientId, String clientSecret, String redirectURI, String... scopes) throws Exception {
        this(User.User1, clientId, clientSecret, redirectURI, scopes);
    }

    public OAuthClient(User user, String clientId, String clientSecret, String redirectURI, String... scopes) throws Exception {
        super(new OAuthSession(user, clientId, clientSecret, redirectURI, scopes), false);
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
            return delegate.getParser();
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

    /**
     * Asserts that this client has API access. Will fail if not.
     */
    public void assertAccess() throws Exception {
        int privateContactFolder = getValues().getPrivateContactFolder();
        CommonAllResponse allResponse = execute(new AllRequest(privateContactFolder, AllRequest.GUI_COLUMNS));
        Assert.assertFalse(allResponse.hasError());
    }

}
