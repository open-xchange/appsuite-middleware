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

package com.openexchange.api.client.common.calls.login;

import java.util.Objects;
import org.apache.http.protocol.HttpContext;
import com.openexchange.annotation.NonNull;
import com.openexchange.api.client.Credentials;
import com.openexchange.api.client.HttpMethods;
import com.openexchange.api.client.HttpResponseParser;
import com.openexchange.api.client.LoginInformation;
import com.openexchange.api.client.common.DefaultLoginInformation;
import com.openexchange.api.client.common.calls.AbstractApiCall;
import com.openexchange.api.client.common.parser.AbstractHttpResponseParser;
import com.openexchange.api.client.common.parser.CommonApiResponse;
import com.openexchange.exception.OXException;

/**
 * {@link AbstractLoginCall}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public abstract class AbstractLoginCall extends AbstractApiCall<LoginInformation> {

    protected final Credentials credentials;

    /**
     * Initializes a new {@link AbstractLoginCall}.
     *
     * @param credentials The credentials to login with
     * @throws NullPointerException In case credentials are missing
     */
    public AbstractLoginCall(Credentials credentials) throws NullPointerException {
        super();
        this.credentials = Objects.requireNonNull(credentials);
    }

    @Override
    @NonNull
    public HttpMethods getHttpMehtod() {
        return HttpMethods.POST;
    }

    @Override
    @NonNull
    public String getModule() {
        return "/login";
    }

    @Override
    public HttpResponseParser<LoginInformation> getParser() {
        return new AbstractHttpResponseParser<LoginInformation>() {

            @Override
            public LoginInformation parse(CommonApiResponse commonResponse, HttpContext httpContext) throws OXException {
                return DefaultLoginInformation.parse(commonResponse.getJSONObject().asMap());
            }
        };
    }
}
