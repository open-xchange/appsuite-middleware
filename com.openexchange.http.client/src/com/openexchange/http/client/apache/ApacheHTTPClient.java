/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.http.client.apache;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.httpclient.HttpMethodBase;
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

	public String extractString(HttpMethodBase method) throws OXException {
		try {
			return method.getResponseBodyAsString();
		} catch (IOException e) {
			throw OxHttpClientExceptionCodes.APACHE_CLIENT_ERROR.create(e.getMessage(), e);
		}
	}

	public InputStream extractStream(HttpMethodBase method) throws OXException {
		try {
			return method.getResponseBodyAsStream();
		} catch (IOException e) {
			throw OxHttpClientExceptionCodes.APACHE_CLIENT_ERROR.create(e.getMessage(), e);
		}
	}

	public Reader extractReader(HttpMethodBase method) throws OXException {
		try {
			return new InputStreamReader(method.getResponseBodyAsStream(), method.getResponseCharSet());
		} catch (IOException e) {
			throw OxHttpClientExceptionCodes.APACHE_CLIENT_ERROR.create(e.getMessage(), e);
		}
	}

	public <R> R extractPayload(HttpMethodBase method, Class<R> responseType) throws OXException {
		if (responseType == String.class) {
			return (R) extractString(method);
		} else if (responseType == InputStream.class) {
			return (R) extractStream(method);
		} else if (responseType == Reader.class) {
			return (R) extractReader(method);
		}

		for(Class inputType: Arrays.asList(InputStream.class, Reader.class, String.class)) {
			List<HTTPResponseProcessor> procList = processors.get(inputType);
			if (null != procList){
				for (HTTPResponseProcessor processor : procList) {
					if (processor.getTypes()[1] == responseType) {
						return (R) processor.process(extractPayload(method, inputType));
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
