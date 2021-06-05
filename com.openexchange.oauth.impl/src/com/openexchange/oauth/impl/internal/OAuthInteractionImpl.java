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

package com.openexchange.oauth.impl.internal;

import com.openexchange.oauth.OAuthInteraction;
import com.openexchange.oauth.OAuthInteractionType;
import com.openexchange.oauth.OAuthToken;

/**
 * {@link OAuthInteractionImpl} - The {@link OAuthInteraction} implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OAuthInteractionImpl implements OAuthInteraction {

    private final String authorizationURL;

    private final OAuthInteractionType interactionType;

    private final OAuthToken requestToken;

    /**
     * Initializes a new {@link OAuthInteractionImpl}.
     *
     * @param requestToken The request token needed to acquire the access token
     * @param authorizationURL The base authorization URL
     * @param interactionType The interaction type
     */
    public OAuthInteractionImpl(final OAuthToken requestToken, final String authorizationURL, final OAuthInteractionType interactionType) {
        this.authorizationURL = authorizationURL;
        this.interactionType = interactionType;
        this.requestToken = requestToken;
    }

    @Override
    public OAuthToken getRequestToken() {
        return requestToken;
    }

    @Override
    public OAuthInteractionType getInteractionType() {
        return interactionType;
    }

    @Override
    public String getAuthorizationURL() {
        return authorizationURL;
    }
}
