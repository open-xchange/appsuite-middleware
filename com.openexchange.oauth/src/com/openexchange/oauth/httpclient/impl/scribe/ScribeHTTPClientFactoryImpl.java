package com.openexchange.oauth.httpclient.impl.scribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.openexchange.exception.OXException;
import com.openexchange.http.client.HTTPClient;
import com.openexchange.http.client.builder.HTTPResponseProcessor;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthHTTPClientFactory;
import com.openexchange.oauth.httpclient.OAuthHTTPClient;

public class ScribeHTTPClientFactoryImpl implements OAuthHTTPClientFactory {

	protected Map<Class<?>, List<HTTPResponseProcessor>> processors = new HashMap<Class<?>, List<HTTPResponseProcessor>>();

	@Override
	public HTTPClient create(OAuthAccount account) throws OXException {
		OAuthHTTPClient client = new OAuthHTTPClient(account, account.getAPI(), account.getMetaData().getAPIKey(), account.getMetaData().getAPISecret());
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
