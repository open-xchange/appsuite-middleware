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

import java.io.IOException;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.framework.AbstractRedirectParser;

/**
 * {@link TokenLoginV2Parser}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.0
 */
public class TokenLoginV2Parser extends AbstractRedirectParser<TokenLoginV2Response> {

    private int status;
    private boolean loginSuccessful;

    public TokenLoginV2Parser() {
        super();
        loginSuccessful = false;
    }

    @Override
    public String checkResponse(HttpResponse resp, HttpRequest request) throws ParseException, IOException {
        this.status = resp.getStatusLine().getStatusCode();
        Header[] headers = resp.getHeaders("Set-Cookie");
        for (Header header : headers) {
            if (header.getValue().contains(LoginServlet.SECRET_PREFIX)) {
                loginSuccessful = true;
                break;
            }
        }
        return super.checkResponse(resp, request);
    }

    @Override
    protected TokenLoginV2Response createResponse(String myLocation) {
        int fragIndex = myLocation.indexOf('#');
        if (-1 == fragIndex) {
            return new TokenLoginV2Response(status, myLocation, loginSuccessful);
        }
        String redirectUrl = myLocation.substring(0, fragIndex);
        return new TokenLoginV2Response(status, redirectUrl, loginSuccessful);
    }

}
