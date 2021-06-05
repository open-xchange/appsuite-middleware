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

package com.openexchange.ajax.share.actions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Header;
import com.openexchange.ajax.framework.Params;

/**
 * {@link RedeemRequest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.2
 */
public class RedeemRequest implements AJAXRequest<RedeemResponse> {

    private final String token;
    private final boolean failOnError;
    private final String language;

    public RedeemRequest(String token) {
        this(token, false);
    }

    public RedeemRequest(String token, boolean failOnError) {
        this(token, null, failOnError);
    }

    public RedeemRequest(String token, String language, boolean failOnError) {
        super();
        this.token = token;
        this.language = language;
        this.failOnError = failOnError;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return Method.GET;
    }

    @Override
    public String getServletPath() {
        return "/ajax/share/redeem/token";
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        Params params = new Params("token", token);
        if (null != language) {
            params.add("language", language);
        }
        return params.toArray();
    }

    @Override
    public AbstractAJAXParser<? extends RedeemResponse> getParser() {
        return new RedeemParser(failOnError);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        return null;
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

    private static final class RedeemParser extends AbstractAJAXParser<RedeemResponse> {

        public RedeemParser(boolean failOnError) {
            super(failOnError);
        }

        @Override
        protected RedeemResponse createResponse(Response response) throws JSONException {
            if (!response.hasError()) {
                Map<String, String> properties = new HashMap<String, String>();
                JSONObject data = response.getJSON();
                for (String key : data.keySet()) {
                    properties.put(key, data.getString(key));
                }
                return new RedeemResponse(response, properties);
            }
            return new RedeemResponse(response, null);
        }

    }

}
