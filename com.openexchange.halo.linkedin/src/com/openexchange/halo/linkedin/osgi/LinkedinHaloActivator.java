package com.openexchange.halo.linkedin.osgi;

import com.openexchange.halo.HaloContactDataSource;
import com.openexchange.halo.linkedin.LinkedinInboxDataSource;
import com.openexchange.halo.linkedin.LinkedinProfileDataSource;
import com.openexchange.halo.linkedin.LinkedinUpdatesDataSource;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.linkedin.LinkedInService;
import com.openexchange.server.osgiservice.HousekeepingActivator;

public class LinkedinHaloActivator extends HousekeepingActivator {

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class[]{LinkedInService.class, OAuthService.class};
	}

	@Override
	protected void startBundle() throws Exception {
		registerService(HaloContactDataSource.class, new LinkedinProfileDataSource(this));
		registerService(HaloContactDataSource.class, new LinkedinInboxDataSource(this));
		registerService(HaloContactDataSource.class, new LinkedinUpdatesDataSource(this));
	}


}
