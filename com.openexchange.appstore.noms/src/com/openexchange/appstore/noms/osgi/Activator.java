package com.openexchange.appstore.noms.osgi;

import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.appstore.noms.NOMSAppActionFactory;

public class Activator extends AJAXModuleActivator {

	@Override
	protected Class<?>[] getNeededServices() {
		return null;
	}

	@Override
	protected void startBundle() throws Exception {
		registerModule(new NOMSAppActionFactory(this), "noms/apps");
	}

}
