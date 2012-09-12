package com.openexchange.rss.preprocessors;

import com.openexchange.rss.RssServices;

public class ImagePreprocessor extends AbstractPreprocessor {

	@Override
	public String process2(String payload) {
		boolean[] isModified = {false};
		return RssServices.getHtmlService().filterExternalImages(payload, isModified );
	}

}
