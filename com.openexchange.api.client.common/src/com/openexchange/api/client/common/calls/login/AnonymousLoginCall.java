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

import static com.openexchange.api.client.common.ApiClientConstants.ACTION;
import static com.openexchange.api.client.common.ApiClientConstants.ANONYMOUS;
import static com.openexchange.api.client.common.ApiClientConstants.CLIENT;
import static com.openexchange.api.client.common.ApiClientConstants.CLIENT_VALUE;
import static com.openexchange.api.client.common.ApiClientConstants.NAME;
import static com.openexchange.api.client.common.ApiClientConstants.PASSWORD;
import static com.openexchange.api.client.common.ApiClientConstants.RAMP_UP;
import static com.openexchange.api.client.common.ApiClientConstants.SHARE;
import static com.openexchange.api.client.common.ApiClientConstants.STAY_SIGNED_IN;
import static com.openexchange.api.client.common.ApiClientConstants.TARGET;
import java.util.Map;
import java.util.Objects;
import org.apache.http.HttpEntity;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.annotation.Nullable;
import com.openexchange.api.client.ApiClientExceptions;
import com.openexchange.api.client.Credentials;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.version.VersionService;

/**
 * {@link AnonymousLoginCall}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class AnonymousLoginCall extends AbstractLoginCall {

    private final ServiceLookup services;

    private final String share;
    private final String target;

    /**
     * Initializes a new {@link AnonymousLoginCall}.
     * 
     * @param services The service lookup to get the {@link VersionService} from
     * @param credentials The credentials
     * @param share The token of the share to access
     * @param target The path to a specific share target
     * @throws OXException In case credentials are missing
     */
    public AnonymousLoginCall(ServiceLookup services, Credentials credentials, String share, String target) throws OXException {
        super(credentials);
        this.services = services;
        this.share = Objects.requireNonNull(share);
        this.target = Objects.requireNonNull(target);

        if (null == credentials.getPassword()) {
            throw ApiClientExceptions.MISSING_CREDENTIALS.create();
        }
    }

    @Override
    protected String getAction() {
        return ANONYMOUS;
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {
        parameters.put(SHARE, share);
        parameters.put(TARGET, target);
    }

    @Override
    @Nullable
    public HttpEntity getBody() throws OXException, JSONException {
        /*
         * Build body
         */
        JSONObject json = new JSONObject();
        json.put(ACTION, ANONYMOUS);
        json.put(NAME, null == credentials.getLogin() ? "" : credentials.getLogin());
        json.put(PASSWORD, credentials.getPassword());
        json.put(CLIENT, CLIENT_VALUE);
        json.put("locale", "en_US");
        json.put("timeout", 10000);
        json.put(RAMP_UP, false);
        json.put(SHARE, share);
        json.put(TARGET, target);
        json.put(STAY_SIGNED_IN, true);
        addVersion(json);

        return toHttpEntity(json);
    }

    private void addVersion(JSONObject json) throws JSONException {
        VersionService versionService = services.getOptionalService(VersionService.class);
        if (null != versionService) {
            String version = versionService.getVersionString();
            version = version.replace("Rev", "");
            json.put("version", version);
        }
    }

}
