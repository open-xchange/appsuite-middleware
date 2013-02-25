package com.openexchange.templating.assets;

import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

public class AssetRequest {

	private ServerSession session;
	private AJAXRequestData request;
	private String assetName;

	public AssetRequest(AJAXRequestData requestData, ServerSession session) throws OXException {
		this.session = session;
		this.request = requestData;
		this.assetName = request.requireParameter("assetName");
	}

	public ServerSession getSession() {
		return session;
	}

	public void setSession(ServerSession session) {
		this.session = session;
	}

	public AJAXRequestData getRequest() {
		return request;
	}

	public void setRequest(AJAXRequestData request) {
		this.request = request;
	}

	public String getAssetName() {
		return assetName;
	}

	public void setAssetName(String assetName) {
		this.assetName = assetName;
	}
	
	

}
