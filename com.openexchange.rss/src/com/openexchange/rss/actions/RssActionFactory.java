package com.openexchange.rss.actions;

import java.util.Arrays;
import java.util.Collection;

import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;

public class RssActionFactory implements AJAXActionServiceFactory {

	@Override
	public Collection<?> getSupportedServices() {
		return Arrays.asList(new RssAction());
	}

	@Override
	public AJAXActionService createActionService(String action) throws OXException {
		return new RssAction();
	}

}
