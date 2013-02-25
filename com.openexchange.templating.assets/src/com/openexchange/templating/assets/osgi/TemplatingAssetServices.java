package com.openexchange.templating.assets.osgi;

import java.util.concurrent.atomic.AtomicReference;

import com.openexchange.config.ConfigurationService;
import com.openexchange.server.ServiceLookup;

public class TemplatingAssetServices {

	public static AtomicReference<ServiceLookup> LOOKUP = new AtomicReference<ServiceLookup>();
	
	public static ConfigurationService getConfiguration() {
		return LOOKUP.get().getService(ConfigurationService.class);
	}

}
