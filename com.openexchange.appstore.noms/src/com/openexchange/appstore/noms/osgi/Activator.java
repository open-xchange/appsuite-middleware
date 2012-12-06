package com.openexchange.appstore.noms.osgi;

import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.appstore.noms.NOMSAppActionFactory;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.http.client.HTTPClient;

public class Activator extends AJAXModuleActivator {

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class[]{ConfigViewFactory.class, HTTPClient.class};
	}

	@Override
	protected void startBundle() throws Exception {
		registerModule(new NOMSAppActionFactory(this), "liberty/apps");
	}

}
