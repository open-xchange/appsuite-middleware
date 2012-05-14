package com.openexchange.http.client.builder;

import com.openexchange.exception.OXException;


public interface HTTPResponseProcessor {
	
	public Class<?>[] getTypes();
	
	public Object process(Object payload) throws OXException;

}
