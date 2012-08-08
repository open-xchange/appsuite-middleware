package com.openexchange.oauth.flickr.osgi;

import com.openexchange.config.ConfigurationService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.flickr.FlickrMetadata;
import com.openexchange.osgi.HousekeepingActivator;

public class FlickrOAuthActivator extends HousekeepingActivator {

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class[]{ConfigurationService.class};
	}

	@Override
	protected void startBundle() throws Exception {
		registerService(OAuthServiceMetaData.class, new FlickrMetadata(this));
	}


}
