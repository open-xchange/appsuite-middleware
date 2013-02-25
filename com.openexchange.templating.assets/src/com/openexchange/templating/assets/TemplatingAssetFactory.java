package com.openexchange.templating.assets;

import java.util.LinkedList;

import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;


public class TemplatingAssetFactory implements AJAXActionServiceFactory {

	@Override
	public AJAXActionService createActionService(String action)
			throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkedList<AJAXActionService> getSupportedServices() {
		return new LinkedList<AJAXActionService>(){{
    		add(new AssetProvideAction());
    		add(new AssetLinkAction());
    	}};
	}

}
