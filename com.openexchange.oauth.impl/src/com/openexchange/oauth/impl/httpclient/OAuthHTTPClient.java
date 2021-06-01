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

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.http.client.AbstractHTTPClient;
import com.openexchange.http.client.HTTPClient;
import com.openexchange.http.client.builder.HTTPRequestBuilder;
import com.openexchange.http.client.builder.HTTPResponseProcessor;
import com.openexchange.http.client.exceptions.OxHttpClientExceptionCodes;
import com.openexchange.java.Streams;
import com.openexchange.oauth.API;
import com.openexchange.oauth.OAuthAccount;

public class OAuthHTTPClient extends AbstractHTTPClient implements HTTPClient {

    private OAuthAccount account;
    private API api;
    private String apiKey;
    private String secret;

    public OAuthHTTPClient(OAuthAccount account, API api, String apiKey, String secret) {
        this.account = account;
        this.api = api;
        this.apiKey = apiKey;
        this.secret = secret;
    }

    @Override
    public HTTPRequestBuilder getBuilder() {
        return new OAuthHTTPRequestBuilder(this);
    }

    @SuppressWarnings("unchecked")
    public <R> R process(String payload, Class<R> targetFormat) throws OXException {
        if (String.class == targetFormat) {
            return (R) payload;
        } else if (InputStream.class == targetFormat) {
            try {
                return (R) Streams.newByteArrayInputStream(payload.getBytes("UTF-8"));
            } catch (@SuppressWarnings("unused") UnsupportedEncodingException e) {
                // WON'T HAPPEN!
            }
        } else if (Reader.class == targetFormat) {
            return (R) new StringReader(payload);
        }

        for (Class<?> inputType : Arrays.asList(String.class, Reader.class, InputStream.class)) {
            List<HTTPResponseProcessor> procList = processors.get(inputType);
            for (HTTPResponseProcessor processor : procList) {
                if (processor.getTypes()[1] == targetFormat) {
                    return (R) processor.process(process(payload, inputType));
                }
            }
        }

        throw OxHttpClientExceptionCodes.APACHE_CLIENT_ERROR.create();
    }

    public OAuthAccount getAccount() {
        return account;
    }

    public void setAccount(OAuthAccount account) {
        this.account = account;
    }

    public API getApi() {
        return api;
    }

    public void setApi(API api) {
        this.api = api;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

}
