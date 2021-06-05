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

package com.openexchange.oauth.microsoft.graph;

import java.util.Collection;
import java.util.Collections;
import org.scribe.builder.api.Api;
import org.scribe.model.Verb;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.api.MicrosoftGraphApi;
import com.openexchange.oauth.impl.AbstractExtendedScribeAwareOAuthServiceMetaData;
import com.openexchange.server.ServiceLookup;

/**
 * {@link MicrosoftGraphOAuthServiceMetaData}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class MicrosoftGraphOAuthServiceMetaData extends AbstractExtendedScribeAwareOAuthServiceMetaData {

    private static final String IDENTITY_URL = "https://graph.microsoft.com/v1.0/me";
    private static final String IDENTITY_FIELD_NAME = "id";
    private static final String PROPERTY_IDENTIFIER = "microsoft.graph";

    /**
     * Initialises a new {@link MicrosoftGraphOAuthServiceMetaData}.
     * 
     * @param services The {@link ServiceLookup} instance
     */
    public MicrosoftGraphOAuthServiceMetaData(ServiceLookup services) {
        super(services, KnownApi.MICROSOFT_GRAPH, MicrosoftGraphOAuthScope.values());
    }

    @Override
    public Class<? extends Api> getScribeService() {
        return MicrosoftGraphApi.class;
    }

    @Override
    public String getIdentityURL(String accessToken) {
        return IDENTITY_URL;
    }

    @Override
    public Verb getIdentityHTTPMethod() {
        return Verb.GET;
    }

    @Override
    public String getIdentityFieldName() {
        return IDENTITY_FIELD_NAME;
    }

    @Override
    protected String getPropertyId() {
        return PROPERTY_IDENTIFIER;
    }

    @Override
    protected Collection<OAuthPropertyID> getExtraPropertyNames() {
        return Collections.singletonList(OAuthPropertyID.redirectUrl);
    }

}
