package com.openexchange.http.client.builder;

import com.openexchange.exception.OXException;


public interface HTTPResponseProcessor<T1, T2> {
	
	public Class<?>[] getTypes();
	
	public HTTPResponse<T2> process(HTTPResponse<T1> response) throws OXException;
}
