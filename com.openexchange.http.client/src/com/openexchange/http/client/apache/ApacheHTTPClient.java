package com.openexchange.http.client.apache;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpMethodBase;

import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.http.client.HTTPClient;
import com.openexchange.http.client.builder.HTTPRequestBuilder;
import com.openexchange.http.client.builder.HTTPResponseProcessor;
import com.openexchange.http.client.builder.HTTPStrategy;

public class ApacheHTTPClient implements HTTPClient {
	
	private Map<Class<?>, List<HTTPResponseProcessor<?,?>>> processors = new HashMap<Class<?>, List<HTTPResponseProcessor<?,?>>>();
	private ManagedFileManagement fileManager;
	
	public ApacheHTTPClient(ManagedFileManagement fileManager) {
		this.fileManager = fileManager;
	}
	
	
	public <R> HTTPRequestBuilder<R> getBuilder(Class<R> responseType) {
		if (responseType == String.class) {
			return (HTTPRequestBuilder<R>) new ApacheClientRequestBuilder<String>(fileManager) {

				@Override
				String extractPayload(HttpMethodBase method) throws AbstractOXException {
					try {
						return method.getResponseBodyAsString();
					} catch (IOException e) {
						throw new AbstractOXException(e.getMessage(), e);
					}
				}
			};
		} else if (responseType == InputStream.class) {
			return (HTTPRequestBuilder<R>) new ApacheClientRequestBuilder<InputStream>(fileManager) {

				@Override
				InputStream extractPayload(HttpMethodBase method) throws AbstractOXException {
					try {
						return method.getResponseBodyAsStream();
					} catch (IOException e) {
						throw new AbstractOXException(e.getMessage(), e);
					}
				}
			};
		} else if (responseType == Reader.class) {
			return (HTTPRequestBuilder<R>) new ApacheClientRequestBuilder<Reader>(fileManager) {

				@Override
				Reader extractPayload(HttpMethodBase method) throws AbstractOXException {
					try {
						return new InputStreamReader(method.getResponseBodyAsStream(), method.getResponseCharSet());
					} catch (IOException e) {
						throw new AbstractOXException(e.getMessage(), e);
					}
				}
			};
		}
		
		
		// TODO: Expand this to make longer paths, similar to shortest-path in ng conversion framework
		
		List<HTTPResponseProcessor<?, ?>> stringProcessors = processors.get(String.class);
		if (stringProcessors !=  null) {
			for (HTTPResponseProcessor<?, ?> p : stringProcessors) {
				if (p.getTypes()[1] == responseType) {
					return getBuilder(String.class).chain((HTTPResponseProcessor<String, R>) p);
				}
			}
		}
		
		List<HTTPResponseProcessor<?, ?>> readerProcessors = processors.get(Reader.class);
		if (readerProcessors != null) {
			for (HTTPResponseProcessor<?, ?> p : readerProcessors) {
				if (p.getTypes()[1] == responseType) {
					return getBuilder(Reader.class).chain((HTTPResponseProcessor<Reader, R>) p);
				}
			}
		}
		
		List<HTTPResponseProcessor<?, ?>> isProcessors = processors.get(InputStream.class);
		if (isProcessors != null) {
			for (HTTPResponseProcessor<?, ?> p : isProcessors) {
				if (p.getTypes()[1] == responseType) {
					return getBuilder(InputStream.class).chain((HTTPResponseProcessor<InputStream, R>) p);
				}
			}
		}
		
		return null;
	}

	public void registerProcessor(HTTPResponseProcessor<?, ?> processor) {
		Class<?>[] types = processor.getTypes();
		List<HTTPResponseProcessor<?, ?>> list = processors.get(types[0]);
		if (list == null) {
			list = new ArrayList<HTTPResponseProcessor<?,?>>();
			processors.put(types[0], list);
		}
		
		list.add(processor);
		
		
	}

	public void forgetProcessor(HTTPResponseProcessor<?, ?> processor) {
		Class<?>[] types = processor.getTypes();
		List<HTTPResponseProcessor<?, ?>> list = processors.get(types[0]);
		if (list == null) {
			return;
		}
		
		list.remove(processor);
		
	}

}
