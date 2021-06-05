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

package com.openexchange.saml.oauth;

import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import com.openexchange.server.ServiceLookup;

/**
 * {@link OAuthAccessTokenRequest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class OAuthAccessTokenRequest extends AbstractOAuthAccessTokenRequest {

    public OAuthAccessTokenRequest(ServiceLookup services, String clientId) {
        super(services, clientId);
    }

    private static final String GRANT_TYPE = "urn:ietf:params:oauth:grant-type:saml2-bearer";

    @Override
    protected String getGrantType() {
        return GRANT_TYPE;
    }

    @Override
    protected void addAccessInfo(String accessInfo, List<NameValuePair> nvps) {
        nvps.add(new BasicNameValuePair("assertion", accessInfo));
    }

}
