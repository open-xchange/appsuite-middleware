package com.openexchange.templating.assets;


import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;

public class AssetLinkAction extends AbstractAssetAction {	
	public String makeLink(String assetName) {
		return protocol + "://" + server + "/";
	}

	@Override
	public AJAXRequestResult perform(AssetRequest request) throws OXException {
		return new AJAXRequestResult(makeLink(request.getAssetName()), "string");
	}

}
