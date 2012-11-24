package com.openexchange.capabilities.json.osgi;

import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.json.Capability2JSON;
import com.openexchange.capabilities.json.CapabilityActionFactory;

public class CapabilitiesJSONActivator extends AJAXModuleActivator {

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class<?>[]{CapabilityService.class};
	}

	@Override
	protected void startBundle() throws Exception {
		registerService(ResultConverter.class, new Capability2JSON());
		registerModule(new CapabilityActionFactory(this), "capabilities");
	}


}
