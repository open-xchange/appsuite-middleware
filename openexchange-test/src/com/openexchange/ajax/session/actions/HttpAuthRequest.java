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

package com.openexchange.ajax.session.actions;

import com.openexchange.ajax.framework.Header;
import com.openexchange.tools.encoding.Base64;

/**
 * {@link HttpAuthRequest}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class HttpAuthRequest extends AbstractRequest<HttpAuthResponse> {

    public static final String HTTP_AUTH_URL = LOGIN_URL + "/httpAuth";

    final String login;
    final String password;

    public HttpAuthRequest(String login, String password) {
        super(new Parameter[0]);
        this.login = login;
        this.password = password;
    }

    @Override
    public Method getMethod() {
        return Method.GET;
    }

    @Override
    public String getServletPath() {
        return HTTP_AUTH_URL;
    }

    @Override
    public Header[] getHeaders() {
        return new Header[] { new Header() {

            @Override
            public String getName() {
                return "Authorization";
            }

            @Override
            public String getValue() {
                return "Basic " + Base64.encode(login + ':' + password);
            }
        }
        };
    }

    @Override
    public HttpAuthParser getParser() {
        return new HttpAuthParser(true, true, true);
    }
}
