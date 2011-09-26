package com.openexchange.ajax.session.actions;

import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Params;

public class RefreshSecretRequest extends AbstractRequest<RefreshSecretResponse> {

	protected RefreshSecretRequest(String sessionID) {
		super( new Params(AJAXServlet.PARAMETER_SESSION,sessionID).toArray());
	}

	public AbstractAJAXParser<RefreshSecretResponse> getParser() {
		return new AbstractAJAXParser<RefreshSecretResponse>(true) {
			@Override
			protected RefreshSecretResponse createResponse(Response response)
					throws JSONException {
				return new RefreshSecretResponse(response);
			}
		};
	}

}
