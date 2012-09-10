package com.openexchange.rss.preprocessors;

public interface RssPreprocessor {

	public String process(String payload);
	
	public RssPreprocessor chain(RssPreprocessor nextInLine);

}
