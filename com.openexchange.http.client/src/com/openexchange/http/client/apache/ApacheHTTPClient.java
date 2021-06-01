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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.http.client.AbstractHTTPClient;
import com.openexchange.http.client.HTTPClient;
import com.openexchange.http.client.builder.HTTPRequestBuilder;
import com.openexchange.http.client.builder.HTTPResponseProcessor;
import com.openexchange.http.client.exceptions.OxHttpClientExceptionCodes;

public class ApacheHTTPClient extends AbstractHTTPClient implements HTTPClient {

	private final ManagedFileManagement fileManager;

	public ApacheHTTPClient(ManagedFileManagement fileManager) {
		this.fileManager = fileManager;
	}

	public String extractString(HttpResponse resp) throws OXException {
		try {
			return EntityUtils.toString(resp.getEntity());
		} catch (IOException e) {
            throw OxHttpClientExceptionCodes.APACHE_CLIENT_ERROR.create(e, e.getMessage());
		}
	}

	public InputStream extractStream(HttpResponse resp) throws OXException {
		try {
		    return resp.getEntity().getContent();
		} catch (IOException e) {
            throw OxHttpClientExceptionCodes.APACHE_CLIENT_ERROR.create(e, e.getMessage());
		}
	}

	public Reader extractReader(HttpResponse resp) throws OXException {
		try {
            ContentType contentType = ContentType.getOrDefault(resp.getEntity());
            Charset charset = contentType.getCharset();
            if (charset == null) {
                try {
                    charset = Charset.forName(ServerConfig.getProperty(ServerConfig.Property.DefaultEncoding));
                } catch (Exception e) {
                    // Failed to look-up charset
                    charset = Charset.defaultCharset();
                }
            }
            return new InputStreamReader(resp.getEntity().getContent(), charset);
		} catch (IOException e) {
            throw OxHttpClientExceptionCodes.APACHE_CLIENT_ERROR.create(e, e.getMessage());
		}
	}

	public <R> R extractPayload(HttpResponse resp, Class<R> responseType) throws OXException {
		if (responseType == String.class) {
			return (R) extractString(resp);
		} else if (responseType == InputStream.class) {
			return (R) extractStream(resp);
		} else if (responseType == Reader.class) {
			return (R) extractReader(resp);
		}

		for(Class<?> inputType: Arrays.asList(InputStream.class, Reader.class, String.class)) {
			List<HTTPResponseProcessor> procList = processors.get(inputType);
			if (null != procList){
				for (HTTPResponseProcessor processor : procList) {
					if (processor.getTypes()[1] == responseType) {
						return (R) processor.process(extractPayload(resp, inputType));
					}
				}
			}
		}

		throw OxHttpClientExceptionCodes.APACHE_CLIENT_ERROR.create();
	}

	/*
	 *
	 */

	@Override
    public  HTTPRequestBuilder getBuilder() {
		return new ApacheClientRequestBuilder(fileManager, this);
	}



}
