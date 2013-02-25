package com.openexchange.templating.assets.osgi;

import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.config.ConfigurationService;
import com.openexchange.templating.assets.TemplatingAssetFactory;

public class TemplatingAssetActivator extends AJAXModuleActivator {

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class[]{ConfigurationService.class,};
	}

	@Override
	protected void startBundle() throws Exception {
		TemplatingAssetServices.LOOKUP.set(this);
		registerModule(new TemplatingAssetFactory(), "templatingAssets");
	}
	
	@Override
	protected void stopBundle() throws Exception {
		TemplatingAssetServices.LOOKUP.set(null);
	}
	
}