package com.openexchange.index;

import java.util.Map;

import com.openexchange.index.IndexDocument.Type;

public interface QueryParameters {
	
	int getOffset();
	
	int getLength();
	
	String getQueryString();
	
	Map<String, Object> getQueryParameters();
	
	String getHandler();
	
	Type getType();

}
