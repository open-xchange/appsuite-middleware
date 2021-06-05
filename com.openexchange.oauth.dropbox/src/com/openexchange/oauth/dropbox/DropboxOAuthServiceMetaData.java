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

package com.openexchange.oauth.dropbox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.scribe.builder.api.Api;
import org.scribe.model.Verb;
import com.openexchange.oauth.API;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.api.DropboxApi2;
import com.openexchange.oauth.impl.AbstractExtendedScribeAwareOAuthServiceMetaData;
import com.openexchange.server.ServiceLookup;

/**
 * {@link DropboxOAuthServiceMetaData}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DropboxOAuthServiceMetaData extends AbstractExtendedScribeAwareOAuthServiceMetaData {

    private static final String IDENTITY_URL = "https://api.dropboxapi.com/2/users/get_current_account";
    private static final String IDENTITY_FIELD_NAME = "account_id";

    /**
     * Initializes a new {@link DropboxOAuthServiceMetaData}.
     */
    public DropboxOAuthServiceMetaData(ServiceLookup serviceLookup) {
        super(serviceLookup, KnownApi.DROPBOX, DropboxOAuthScope.values());
    }

    @Override
    public Class<? extends Api> getScribeService() {
        return DropboxApi2.class;
    }

    @Override
    public API getAPI() {
        return KnownApi.DROPBOX;
    }

    @Override
    protected String getPropertyId() {
        return "dropbox";
    }

    @Override
    public boolean needsRequestToken() {
        return false;
    }

    @Override
    protected Collection<OAuthPropertyID> getExtraPropertyNames() {
        Collection<OAuthPropertyID> propertyNames = new ArrayList<OAuthPropertyID>(2);
        Collections.addAll(propertyNames, OAuthPropertyID.redirectUrl, OAuthPropertyID.productName);
        return propertyNames;
    }

    @Override
    public Verb getIdentityHTTPMethod() {
        return Verb.POST;
    }

    @Override
    public String getIdentityURL(String accessToken) {
        return IDENTITY_URL;
    }

    @Override
    public String getIdentityFieldName() {
        return IDENTITY_FIELD_NAME;
    }

    @Override
    public String getContentType() {
        // Empty content-type otherwise the getUserIdentity call will fail.
        // 
        // The scribe library it tries to append a body when the request verb is
        // set to POST. The Dropbox API is picky and if the 'Content-Type' is
        // set to 'application/json', it then tries to interpret the body that is send
        // with the request. However, when getting the user's identity via the getIdentityURL link
        // no body is required... and none is sent... hence the fail on Dropbox's side.
        return EMPTY_CONTENT_TYPE;
    }
}
