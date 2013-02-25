package com.openexchange.templating.assets;


import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.templating.TemplateErrorMessage;
import com.openexchange.templating.assets.osgi.TemplatingAssetServices;

public class AssetProvideAction extends AbstractAssetAction {


	@Override
	public AJAXRequestResult perform(AssetRequest request) throws OXException {
		String templateDirectory = TemplatingAssetServices.getConfiguration().getProperty("com.openexchange.templating.templateDirectory");
		String prop = TemplatingAssetServices.getConfiguration().getProperty("com.openexchange.templating.templateWhitelist","");
		
		List<String> templateWhitelist = null;
		if (prop != null || prop.equals("")) {
			templateWhitelist= Arrays.asList(prop.split("\\s*,\\s*"));
		}
		
		String requestedAsset = request.getAssetName();
		
		if (templateWhitelist != null && !templateWhitelist.contains(requestedAsset)) {
			throw TemplateErrorMessage.TemplateNotWhitelisted.create();
		}
		
		File asset = new File(templateDirectory + requestedAsset);
		if (!asset.exists()) {
			throw TemplateErrorMessage.TemplateNotFound.create();
		}
		return new AJAXRequestResult(asset, "file");
	}

}
