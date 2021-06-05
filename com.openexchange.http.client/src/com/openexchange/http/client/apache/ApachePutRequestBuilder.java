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

package com.openexchange.http.client.apache;

import java.io.InputStream;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import com.openexchange.http.client.builder.HTTPPutRequestBuilder;
import com.openexchange.java.Streams;

public class ApachePutRequestBuilder extends CommonApacheHTTPRequest<HTTPPutRequestBuilder> implements HTTPPutRequestBuilder {

	private String stringBody;
	private InputStream isBody;
	private String cType;

	public ApachePutRequestBuilder(final ApacheClientRequestBuilder coreBuilder) {
		super(coreBuilder);
	}

	@Override
	protected HttpRequestBase createMethod(final String encodedSite) {
		final HttpPut putMethod = new HttpPut(encodedSite);
		HttpEntity entity = null;
		if (stringBody != null) {
			entity = new StringEntity(stringBody, ContentType.create((cType != null) ? cType : "text/plain", "UTF-8"));
		} else if (isBody != null) {
			entity = new InputStreamEntity(isBody, ContentType.create((cType != null) ? cType : "application/octet-stream"));
		}

		if (entity != null) {
			putMethod.setEntity(entity);
		}
		return putMethod;
	}

	@Override
    public HTTPPutRequestBuilder body(final String body) {
		clearBody();
		this.stringBody = body;
		return this;
	}

	@Override
    public HTTPPutRequestBuilder body(final InputStream body) {
		clearBody();
		this.isBody = body;
		return this;
	}

	private void clearBody() {
		stringBody = null;
		Streams.close(isBody);
	}

	@Override
    public HTTPPutRequestBuilder contentType(final String ctype) {
		this.cType = ctype;
		return this;
	}

}
