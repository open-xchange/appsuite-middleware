package com.openexchange.oauth.tumblr.osgi;


import com.openexchange.config.ConfigurationService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.tumblr.TumblrOAuthServiceMetadata;
import com.openexchange.osgi.HousekeepingActivator;

public class TumblrOAuthActivator extends HousekeepingActivator {
	@Override
	protected Class<?>[] getNeededServices() {
		return new Class[]{ConfigurationService.class};
	}

	@Override
	protected void startBundle() throws Exception {
		registerService(OAuthServiceMetaData.class, new TumblrOAuthServiceMetadata(this));
	}



}
