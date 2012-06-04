package com.openexchange.http.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.openexchange.http.client.builder.HTTPResponseProcessor;

public abstract class AbstractHTTPClient {
	
	protected Map<Class<?>, List<HTTPResponseProcessor>> processors = new HashMap<Class<?>, List<HTTPResponseProcessor>>();
	
	
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
	
	public void setProcessors(
			Map<Class<?>, List<HTTPResponseProcessor>> processors) {
		this.processors = processors;
	}
}
