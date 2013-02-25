package com.openexchange.templating.assets;

import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

public abstract class AbstractAssetAction implements AJAXActionService {

	public abstract AJAXRequestResult perform(AssetRequest request) throws OXException;
	
	@Override
	public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
		return perform(new AssetRequest(requestData, session));
	}

}
