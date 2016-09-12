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
