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

import static com.openexchange.api.client.common.ApiClientConstants.CLIENT;
import static com.openexchange.api.client.common.ApiClientConstants.GUEST;
import static com.openexchange.api.client.common.ApiClientConstants.LOGIN;
import static com.openexchange.api.client.common.ApiClientConstants.PASSWORD;
import static com.openexchange.api.client.common.ApiClientConstants.SHARE;
import static com.openexchange.api.client.common.ApiClientConstants.STAY_SIGNED_IN;
import static com.openexchange.api.client.common.ApiClientConstants.TARGET;
import java.util.Map;
import java.util.Objects;
import org.apache.http.HttpEntity;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.annotation.Nullable;
import com.openexchange.api.client.Credentials;
import com.openexchange.api.client.common.ApiClientConstants;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link GuestLoginCall}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class GuestLoginCall extends AbstractLoginCall {

    private final String share;
    private final String target;
    private final String optLoginName;

    /**
     * Initializes a new {@link GuestLoginCall}.
     * 
     * @param credentials The credentials to login with
     * @param optLoginName The optional login name the user has on the remote server as received by it
     * @param share The token of the share to access
     * @param target The path to a specific share target
     * @throws NullPointerException In case parameter is missing
     */
    public GuestLoginCall(Credentials credentials, String optLoginName, String share, String target) throws NullPointerException {
        super(credentials);

        this.optLoginName = optLoginName;
        this.share = Objects.requireNonNull(share);
        this.target = Objects.requireNonNull(target);
    }

    @Override
    protected String getAction() {
        return GUEST;
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {
        parameters.put(SHARE, share);
        parameters.put(TARGET, target);
        parameters.put(CLIENT, ApiClientConstants.CLIENT_VALUE);
        parameters.put(STAY_SIGNED_IN, Boolean.TRUE.toString());
    }

    @Override
    @Nullable
    public HttpEntity getBody() throws OXException, JSONException {
        JSONObject json = new JSONObject();
        String login;
        if (Strings.isNotEmpty(credentials.getLogin())) {
            login = credentials.getLogin();
        } else {
            login = null == optLoginName ? "" : optLoginName;
        }

        json.put(LOGIN, login);
        json.put(PASSWORD, null == credentials.getPassword() ? "" : credentials.getPassword());
        return toHttpEntity(json);
    }

}
