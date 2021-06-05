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

package com.openexchange.oauth.impl.httpclient;

import java.util.Map;
import org.scribe.model.Response;
import com.openexchange.exception.OXException;
import com.openexchange.http.client.builder.HTTPResponse;
import com.openexchange.oauth.OAuthExceptionCodes;

public class HttpOauthResponse implements HTTPResponse {

    private final Response delegate;

    HttpOauthResponse(Response oauthResponse) {
        delegate = oauthResponse;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> R getPayload(Class<R> klass) throws OXException {
        try {
            return (R) delegate.getBody(); // TODO: Funky
        } catch (IllegalStateException | IllegalArgumentException e) {
            throw OAuthExceptionCodes.NOT_A_VALID_RESPONSE.create(e);
        } catch (Exception e) {
            throw OAuthExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public Map<String, String> getCookies() {
        throw new UnsupportedOperationException("Implement me ;)");
        //return delegate.getHeaders(); //MAYBE?
    }

    @Override
    public Map<String, String> getHeaders() {
        return delegate.getHeaders();
    }

    @Override
    public int getStatus() {
        return delegate.getCode();
    }

}
