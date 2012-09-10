package com.openexchange.rss.preprocessors;

public abstract class AbstractPreprocessor implements RssPreprocessor {

	private RssPreprocessor nextProcessor;
	
	@Override
	public String process(String payload) {
		payload = process2(payload);
		if(nextProcessor != null)
			payload = nextProcessor.process(payload);
		return payload;
	}

	@Override
	public RssPreprocessor chain(RssPreprocessor nextInLine) {
		nextProcessor = nextInLine;
		return this;
	}
	
	public abstract String process2(String payload);

}
