package com.openexchange.index.sketch;

import java.util.Map;

import com.openexchange.index.sketch.IndexDocument.Type;

public interface QueryParameters {
	
	int getOffset();
	
	int getLength();
	
	String getQueryString();
	
	Map<String, Object> getQueryParameters();
	
	String getHandler();
	
	Type getType();

}
