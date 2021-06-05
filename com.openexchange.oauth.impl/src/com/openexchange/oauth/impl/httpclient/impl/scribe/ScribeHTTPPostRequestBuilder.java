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

package com.openexchange.oauth.impl.httpclient.impl.scribe;

import java.util.Map;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Verb;
import com.openexchange.http.client.builder.HTTPPostRequestBuilder;
import com.openexchange.oauth.impl.httpclient.OAuthHTTPRequestBuilder;

public class ScribeHTTPPostRequestBuilder extends ScribeGenericHTTPRequestBuilder<HTTPPostRequestBuilder> implements HTTPPostRequestBuilder {

	public ScribeHTTPPostRequestBuilder(OAuthHTTPRequestBuilder coreBuilder) {
		super(coreBuilder);
	}

	@Override
	public Verb getVerb() {
		return Verb.POST;
	}

	@Override
	protected void setParameters(Map<String, String> params,
			OAuthRequest request) {
		for(Map.Entry<String, String> param: params.entrySet()) {
			request.addBodyParameter(param.getKey(), param.getValue());
		}
	}

	@Override
    public void setRequestEntity(String requestEntity, String contentType) {
        // Not supported
    }

    @Override
    public void urlParameter(String parameter, String value) {
        // Not supported
    }

}
