package com.openexchange.polling.json.osgi;

import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.polling.PollService;
import com.openexchange.polling.json.actions.PollActionFactory;

public class PollingJSONActivator extends AJAXModuleActivator {

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class[]{PollService.class};
	}

	@Override
	protected void startBundle() throws Exception {
		
		registerModule(new PollActionFactory(this), "poll");
		registerService(ResultConverter.class, new PollResultConverter());
	}

}
