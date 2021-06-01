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

package com.openexchange.oauth.impl.httpclient;

import com.openexchange.http.client.builder.HTTPDeleteRequestBuilder;
import com.openexchange.http.client.builder.HTTPGetRequestBuilder;
import com.openexchange.http.client.builder.HTTPMultipartPostRequestBuilder;
import com.openexchange.http.client.builder.HTTPPostRequestBuilder;
import com.openexchange.http.client.builder.HTTPPutRequestBuilder;
import com.openexchange.http.client.builder.HTTPRequestBuilder;
import com.openexchange.oauth.API;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.impl.httpclient.impl.scribe.ScribeHTTPDeleteRequestBuilder;
import com.openexchange.oauth.impl.httpclient.impl.scribe.ScribeHTTPGetRequestBuilder;
import com.openexchange.oauth.impl.httpclient.impl.scribe.ScribeHTTPPostRequestBuilder;
import com.openexchange.oauth.impl.httpclient.impl.scribe.ScribeHTTPPutRequestBuilder;

public class OAuthHTTPRequestBuilder implements HTTPRequestBuilder {

    private final OAuthHTTPClient client;

    public OAuthHTTPRequestBuilder(OAuthHTTPClient client) {
        this.client = client;
    }

    @Override
    public HTTPPutRequestBuilder put() {
        return new ScribeHTTPPutRequestBuilder(this);
    }

    @Override
    public HTTPPostRequestBuilder post() {
        return new ScribeHTTPPostRequestBuilder(this);
    }

    @Override
    public HTTPMultipartPostRequestBuilder multipartPost() {
        throw new UnsupportedOperationException();
        // return new ScribeHTTPMultipartPostRequestBuilder(this);
    }

    @Override
    public HTTPGetRequestBuilder get() {
        return new ScribeHTTPGetRequestBuilder(this);
    }

    @Override
    public HTTPDeleteRequestBuilder delete() {
        return new ScribeHTTPDeleteRequestBuilder(this);
    }

    public HTTPRequestBuilder getBuilder() {
        return client.getBuilder();
    }

    public OAuthAccount getAccount() {
        return client.getAccount();
    }

    public API getApi() {
        return client.getApi();
    }

    public String getApiKey() {
        return client.getApiKey();
    }

    public String getSecret() {
        return client.getSecret();
    }

}
