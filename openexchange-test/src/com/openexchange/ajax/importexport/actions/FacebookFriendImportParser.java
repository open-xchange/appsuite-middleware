package com.openexchange.ajax.importexport.actions;

import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.AbstractUploadParser;

public class FacebookFriendImportParser extends AbstractUploadParser {

	protected FacebookFriendImportParser(boolean failOnError) {
		super(failOnError);
	}

	@Override
	protected AbstractAJAXResponse createResponse(Response response)
			throws JSONException {
		return new FacebookFriendsImportResponse(response);
	}

}
