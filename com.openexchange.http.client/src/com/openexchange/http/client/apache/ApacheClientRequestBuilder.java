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

import org.apache.http.HttpResponse;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.http.client.builder.HTTPDeleteRequestBuilder;
import com.openexchange.http.client.builder.HTTPGetRequestBuilder;
import com.openexchange.http.client.builder.HTTPMultipartPostRequestBuilder;
import com.openexchange.http.client.builder.HTTPPostRequestBuilder;
import com.openexchange.http.client.builder.HTTPPutRequestBuilder;
import com.openexchange.http.client.builder.HTTPRequestBuilder;
import com.openexchange.http.client.internal.AbstractBuilder;

public class ApacheClientRequestBuilder extends AbstractBuilder implements
		HTTPRequestBuilder {

	private final ManagedFileManagement fileManager;

	private final ApacheHTTPClient client;

	public ApacheClientRequestBuilder(ManagedFileManagement mgmt, ApacheHTTPClient client) {
		fileManager = mgmt;
		this.client = client;
	}

	@Override
    public HTTPPutRequestBuilder put() {
		return new ApachePutRequestBuilder(this);
	}

	@Override
    public HTTPPostRequestBuilder post() {
		return new ApachePostRequestBuilder(this);
	}

	@Override
    public HTTPMultipartPostRequestBuilder multipartPost() {
		return new ApacheMultipartPostRequestBuilder(this, fileManager);
	}

	@Override
    public HTTPGetRequestBuilder get() {
		return new ApacheGetRequestBuilder(this);
	}

	@Override
    public HTTPDeleteRequestBuilder delete() {
		return new ApacheDeleteRequestBuilder(this);
	}

	public <R> R extractPayload(HttpResponse resp, Class<R> type) throws OXException {
		return client.extractPayload(resp, type);
	}

}
