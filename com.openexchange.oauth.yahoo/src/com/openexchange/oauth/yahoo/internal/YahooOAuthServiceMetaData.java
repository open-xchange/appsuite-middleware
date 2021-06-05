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

package com.openexchange.oauth.yahoo.internal;

import java.util.Collection;
import java.util.Collections;
import org.scribe.builder.api.Api;
import org.scribe.model.Verb;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.api.YahooApi2;
import com.openexchange.oauth.impl.AbstractExtendedScribeAwareOAuthServiceMetaData;
import com.openexchange.oauth.yahoo.YahooOAuthScope;
import com.openexchange.server.ServiceLookup;

/**
 * {@link YahooOAuthServiceMetaData}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class YahooOAuthServiceMetaData extends AbstractExtendedScribeAwareOAuthServiceMetaData {

    private static final String IDENTITY_URL = "https://social.yahooapis.com/v1/me/guid?format=json";
    private static final String IDENTITY_FIELD_NAME = "value";

    public YahooOAuthServiceMetaData(ServiceLookup services) {
        super(services, KnownApi.YAHOO, YahooOAuthScope.values());
    }

    @Override
    public Class<? extends Api> getScribeService() {
        return YahooApi2.class;
    }

    @Override
    public boolean needsRequestToken() {
        return false;
    }

    @Override
    protected String getPropertyId() {
        return "yahoo";
    }

    @Override
    protected Collection<OAuthPropertyID> getExtraPropertyNames() {
        return Collections.singletonList(OAuthPropertyID.redirectUrl);
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
}
