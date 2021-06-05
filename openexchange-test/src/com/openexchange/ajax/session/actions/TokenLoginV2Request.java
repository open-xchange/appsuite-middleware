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
import static com.openexchange.ajax.LoginServlet.ACTION_REDEEM_TOKEN;
import static com.openexchange.ajax.fields.LoginFields.AUTHID_PARAM;
import com.openexchange.ajax.fields.LoginFields;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link TokenLoginV2Request}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.0
 */
public class TokenLoginV2Request extends AbstractRequest<TokenLoginV2Response> {

    /**
     * Initializes a new {@link TokenLoginV2Request}.
     */
    public TokenLoginV2Request(String token, String secret, String authId, String client, String version, String redirectUrl) {
        super(new Parameter[] { new URLParameter(PARAMETER_ACTION, ACTION_REDEEM_TOKEN), new URLParameter(AUTHID_PARAM, authId), new FieldParameter(LoginFields.TOKEN, token), new FieldParameter(LoginFields.APPSECRET, secret), new FieldParameter(LoginFields.CLIENT_PARAM, client), new FieldParameter(LoginFields.VERSION_PARAM, version), new FieldParameter(LoginFields.REDIRECT_URL, redirectUrl)
        });
    }

    @Override
    public AbstractAJAXParser<? extends TokenLoginV2Response> getParser() {
        return new TokenLoginV2Parser();
    }

}
