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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.http.client.HTTPClient;
import com.openexchange.http.client.builder.HTTPResponseProcessor;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.http.OAuthHTTPClientFactory;
import com.openexchange.oauth.impl.httpclient.OAuthHTTPClient;
import com.openexchange.session.Session;

public class ScribeHTTPClientFactoryImpl implements OAuthHTTPClientFactory {

	protected Map<Class<?>, List<HTTPResponseProcessor>> processors = new HashMap<Class<?>, List<HTTPResponseProcessor>>();

	@Override
	public HTTPClient create(OAuthAccount account, Session session) throws OXException {
		OAuthHTTPClient client = new OAuthHTTPClient(account, account.getAPI(), account.getMetaData().getAPIKey(session), account.getMetaData().getAPISecret(session));
		client.setProcessors(processors);
		return client;
	}

	public void registerProcessor(HTTPResponseProcessor processor) {
		Class<?>[] types = processor.getTypes();
		List<HTTPResponseProcessor> list = processors.get(types[0]);
		if (list == null) {
			list = new ArrayList<HTTPResponseProcessor>();
			processors.put(types[0], list);
		}
		list.add(processor);
	}

	public void forgetProcessor(HTTPResponseProcessor processor) {
		Class<?>[] types = processor.getTypes();
		List<HTTPResponseProcessor> list = processors.get(types[0]);
		if (list == null) {
			return;
		}

		list.remove(processor);

	}

}
