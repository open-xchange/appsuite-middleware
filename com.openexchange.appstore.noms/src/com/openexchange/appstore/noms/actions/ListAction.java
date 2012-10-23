package com.openexchange.appstore.noms.actions;

import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.appstore.noms.actions.LibertyAppStoreResponse.Status;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.http.client.HTTPClient;
import com.openexchange.http.client.builder.HTTPRequest;
import com.openexchange.http.client.builder.HTTPResponse;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

public class ListAction implements AJAXActionService {

	
	private ServiceLookup services;

	public ListAction(ServiceLookup services) {
		super();
		this.services = services;
	}

	@Override
	public AJAXRequestResult perform(AJAXRequestData requestData,
			ServerSession session) throws OXException {
		
		LibertyAppStoreConfig config = getConfig(session);
		HTTPResponse response = getApps(config, requestData);
		return parseResponse(response);
	}

	private LibertyAppStoreConfig getConfig(ServerSession session) throws OXException {
		ConfigView view = services.getService(ConfigViewFactory.class).getView(session.getUserId(), session.getContextId());
		String url = view.property("com.openexchange.liberty.appstore.url", String.class).get();
		
		String user = null, password = null;
		
		ComposedConfigProperty<String> userProp = view.property("com.openexchange.liberty.appstore.user", String.class);
		if (userProp.isDefined()) {
			user = userProp.get();
		} else {
			user = session.getUserlogin();
		}
		
		ComposedConfigProperty<String> passwordProp = view.property("com.openexchange.liberty.appstore.password", String.class);
		if (passwordProp.isDefined()) {
			password = passwordProp.get();
		} else {
			password = session.getPassword();
		}
		
		
		return new LibertyAppStoreConfig(url, user, password);
	}
	
	private HTTPResponse getApps(LibertyAppStoreConfig config,
			AJAXRequestData requestData) throws OXException {

		HTTPClient httpClient = services.getService(HTTPClient.class);
		
		HTTPRequest request = httpClient.getBuilder()
			.post()
				.url(config.getUrl())
				.parameter("opc", "getSubscriptions")
				.parameter("u", config.getUserName())
				.parameter("p", config.getPassword())
			.build();
		
		return request.execute();
	}
	
	private AJAXRequestResult parseResponse(HTTPResponse response) throws OXException {
		String payload = response.getPayload(String.class);
		LibertyAppStoreResponse appResponse = LibertyAppStoreResponse.parse(payload);
		
		if (appResponse.getStatus() == Status.ERROR) {
			throw AjaxExceptionCodes.UNEXPECTED_RESULT.create("success", "error");
		}
		
		
		return new AJAXRequestResult(appResponse.getApps(), "json");
	}


}
