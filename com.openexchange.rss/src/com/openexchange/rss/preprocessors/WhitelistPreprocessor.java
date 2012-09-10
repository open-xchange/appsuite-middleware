package com.openexchange.rss.preprocessors;

import com.openexchange.rss.RssServices;

public class WhitelistPreprocessor extends AbstractPreprocessor {

	@Override
	public String process2(String payload) {
		return RssServices.getHtmlService().filterWhitelist(payload);
	}


}
