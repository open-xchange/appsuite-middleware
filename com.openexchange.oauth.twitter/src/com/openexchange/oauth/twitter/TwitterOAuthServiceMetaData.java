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

package com.openexchange.oauth.twitter;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.scribe.builder.api.Api;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.Verb;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.OAuthToken;
import com.openexchange.oauth.impl.AbstractExtendedScribeAwareOAuthServiceMetaData;
import com.openexchange.oauth.scope.OAuthScope;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link TwitterOAuthServiceMetaData}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class TwitterOAuthServiceMetaData extends AbstractExtendedScribeAwareOAuthServiceMetaData {

    private static final String IDENTITY_URL = "https://api.twitter.com/1.1/account/verify_credentials.json";
    private static final String IDENTITY_FIELD_NAME = "id_str";

    /**
     * Initializes a new {@link TwitterOAuthServiceMetaData}.
     */
    public TwitterOAuthServiceMetaData(ServiceLookup services) {
        super(services, KnownApi.TWITTER, true, true, TwitterOAuthScope.values());
    }

    @Override
    public Class<? extends Api> getScribeService() {
        return TwitterApi.class;
    }

    @Override
    protected String getPropertyId() {
        return "twitter";
    }

    @Override
    protected Collection<OAuthPropertyID> getExtraPropertyNames() {
        return Collections.emptyList();
    }

    @Override
    public String processAuthorizationURL(final String authUrl, Session session) {
        return authUrl;
    }

    @Override
    public void processArguments(final Map<String, Object> arguments, final Map<String, String> parameter, final Map<String, Object> state) throws OXException {
        // no-op
    }

    @Override
    public String getRegisterToken(String authUrl) {
        return null;
    }

    @Override
    public OAuthToken getOAuthToken(final Map<String, Object> arguments, Set<OAuthScope> scopes) throws OXException {
        return null;
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
