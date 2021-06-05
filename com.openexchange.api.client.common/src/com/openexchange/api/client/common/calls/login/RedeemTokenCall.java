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

import java.util.Map;
import java.util.Objects;
import org.apache.http.protocol.HttpContext;
import com.openexchange.annotation.NonNull;
import com.openexchange.api.client.HttpResponseParser;
import com.openexchange.api.client.common.calls.AbstractGetCall;
import com.openexchange.api.client.common.parser.AbstractHttpResponseParser;
import com.openexchange.api.client.common.parser.CommonApiResponse;
import com.openexchange.exception.OXException;

/**
 * {@link RedeemTokenCall}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class RedeemTokenCall extends AbstractGetCall<ShareLoginInformation> {

    private final String token;

    /**
     * Initializes a new {@link RedeemTokenCall}.
     *
     * @param token The token to request
     * @throws NullPointerException In case the <code>token</code> is missing
     */
    public RedeemTokenCall(String token) throws NullPointerException {
        super();
        this.token = Objects.requireNonNull(token);
    }

    @Override
    @NonNull
    public String getModule() {
        return "/share/redeem/token";
    }

    @Override
    protected String getAction() {
        return null;
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {
        parameters.put("token", token);
        parameters.put("language", "en_US");
    }

    @Override
    public HttpResponseParser<ShareLoginInformation> getParser() {
        return new AbstractHttpResponseParser<ShareLoginInformation>() {

            @Override
            public ShareLoginInformation parse(CommonApiResponse commonResponse, HttpContext httpContext) throws OXException {
                return ShareLoginInformation.parse(commonResponse.getJSONObject().asMap());
            }
        };
    }

}
