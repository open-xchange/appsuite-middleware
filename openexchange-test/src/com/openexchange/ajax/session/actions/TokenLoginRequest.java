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
import static com.openexchange.ajax.LoginServlet.ACTION_TOKENLOGIN;
import static com.openexchange.ajax.fields.LoginFields.AUTHID_PARAM;
import static com.openexchange.ajax.fields.LoginFields.CLIENT_PARAM;
import static com.openexchange.ajax.fields.LoginFields.CLIENT_TOKEN;
import static com.openexchange.ajax.fields.LoginFields.LOGIN_PARAM;
import static com.openexchange.ajax.fields.LoginFields.PASSWORD_PARAM;
import static com.openexchange.ajax.fields.LoginFields.STAY_SIGNED_IN;
import static com.openexchange.ajax.fields.LoginFields.VERSION_PARAM;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.session.LoginTools;
import com.openexchange.java.util.UUIDs;

/**
 * {@link TokenLoginRequest}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class TokenLoginRequest extends AbstractRequest<TokenLoginResponse> {

    private final String clientToken;

    public TokenLoginRequest(String login, String password) {
        this(login, password, LoginTools.generateAuthId(), AJAXClient.class.getName(), AJAXClient.VERSION, false, UUIDs.getUnformattedString(UUID.randomUUID()));
    }

    public TokenLoginRequest(String login, String password, boolean staySignedIn) {
        this(login, password, LoginTools.generateAuthId(), AJAXClient.class.getName(), AJAXClient.VERSION, staySignedIn, UUIDs.getUnformattedString(UUID.randomUUID()));
    }

    public TokenLoginRequest(String login, String password, String authId, String client, String version, boolean autologin, String clientToken) {
        super(createParameter(login, password, authId, client, version, autologin, clientToken));
        this.clientToken = clientToken;
    }

    private static Parameter[] createParameter(String login, String password, String authId, String client, String version, boolean staySignedIn, String clientToken) {
        List<Parameter> retval = new ArrayList<Parameter>();
        retval.add(new URLParameter(PARAMETER_ACTION, ACTION_TOKENLOGIN));
        retval.add(new URLParameter(AUTHID_PARAM, authId));
        retval.add(new FieldParameter(LOGIN_PARAM, login));
        retval.add(new FieldParameter(PASSWORD_PARAM, password));
        retval.add(new FieldParameter(CLIENT_PARAM, client));
        retval.add(new FieldParameter(VERSION_PARAM, version));
        retval.add(new FieldParameter(STAY_SIGNED_IN, Boolean.toString(staySignedIn)));
        retval.add(new FieldParameter(CLIENT_TOKEN, clientToken));
        return retval.toArray(new Parameter[retval.size()]);
    }

    @Override
    public TokenLoginParser getParser() {
        return new TokenLoginParser(clientToken);
    }
}
