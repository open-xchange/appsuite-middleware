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

package com.openexchange.oauth.google;

import java.util.Collection;
import java.util.Collections;
import org.scribe.builder.api.Api;
import org.scribe.builder.api.Google2Api;
import org.scribe.model.Verb;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.impl.AbstractExtendedScribeAwareOAuthServiceMetaData;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link GoogleOAuthServiceMetaData}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class GoogleOAuthServiceMetaData extends AbstractExtendedScribeAwareOAuthServiceMetaData {

    private static final String IDENTITY_URL = "https://www.googleapis.com/oauth2/v1/userinfo";
    private static final String IDENTITY_FIELD_NAME = "id";

    /**
     * Initializes a new {@link GoogleOAuthServiceMetaData}.
     *
     * @param services the service lookup instance
     */
    public GoogleOAuthServiceMetaData(final ServiceLookup services) {
        super(services, KnownApi.GOOGLE, GoogleOAuthScope.values());
    }

    @Override
    protected String getPropertyId() {
        return "google";
    }

    @Override
    protected Collection<OAuthPropertyID> getExtraPropertyNames() {
        return Collections.singletonList(OAuthPropertyID.redirectUrl);
    }

    @Override
    public Class<? extends Api> getScribeService() {
        return Google2Api.class;
    }

    @Override
    public String processAuthorizationURL(String authUrl, Session session) throws OXException {
        StringBuilder authUrlBuilder = new StringBuilder(super.processAuthorizationURL(authUrl, session));
        // Request a refresh token, too and keep the already granted scopes
        return authUrlBuilder.append("&approval_prompt=force").append("&access_type=offline").append("&include_granted_scopes=true").toString();
    }

    @Override
    public Verb getIdentityHTTPMethod() {
        return Verb.GET;
    }

    @Override
    public String getIdentityURL(String accessToken) {
        return IDENTITY_URL;
    }

    @Override
    public String getIdentityFieldName() {
        return IDENTITY_FIELD_NAME;
    }
}
