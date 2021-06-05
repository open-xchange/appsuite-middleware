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

import static com.openexchange.ajax.AJAXServlet.PARAMETER_ACTION;
import static com.openexchange.ajax.LoginServlet.ACTION_TOKENS;
import static com.openexchange.ajax.fields.LoginFields.CLIENT_PARAM;
import static com.openexchange.ajax.fields.LoginFields.CLIENT_TOKEN;
import static com.openexchange.ajax.fields.LoginFields.SERVER_TOKEN;
import com.openexchange.ajax.framework.AJAXClient;

/**
 * {@link TokensRequest}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class TokensRequest extends AbstractRequest<TokensResponse> {

    private String httpSessionId;
    private final boolean failOnError;

    public TokensRequest(String httpSessionId, String clientToken, String serverToken, String client, boolean failOnError) {
        super(new Parameter[] { new URLParameter(PARAMETER_ACTION, ACTION_TOKENS), new FieldParameter(CLIENT_TOKEN, clientToken), new FieldParameter(SERVER_TOKEN, serverToken), new FieldParameter(CLIENT_PARAM, client)
        });
        this.httpSessionId = httpSessionId;
        this.failOnError = failOnError;
    }

    @Override
    public String getServletPath() {
        return super.getServletPath() + ";jsessionid=" + httpSessionId;
    }

    public TokensRequest(String httpSessionId, String clientToken, String serverToken, boolean failOnError) {
        this(httpSessionId, clientToken, serverToken, AJAXClient.class.getName(), failOnError);
    }

    public TokensRequest(String httpSessionId, String clientToken, String serverToken) {
        this(httpSessionId, clientToken, serverToken, true);
    }

    @Override
    public TokensParser getParser() {
        return new TokensParser(failOnError);
    }
}
