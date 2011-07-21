package com.openexchange.ajax.importexport.actions;

import java.io.InputStream;

import com.openexchange.ajax.importexport.actions.AbstractImportRequest.Action;

public abstract class AbstractFacebookImportRequest extends
		AbstractImportRequest<FacebookFriendsImportResponse> {

	public AbstractFacebookImportRequest(Action action, int folderId,
			InputStream upload) {
		super(action, folderId, upload);
	}

	public FacebookFriendImportParser getParser() {
		return new FacebookFriendImportParser(false);
	}

}