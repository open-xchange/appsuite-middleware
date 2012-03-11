package com.openexchange.ajax.importexport.actions;

import java.io.InputStream;

public abstract class AbstractFacebookImportRequest extends
		AbstractImportRequest<FacebookFriendsImportResponse> {

	public AbstractFacebookImportRequest(Action action, int folderId,
			InputStream upload) {
		super(action, folderId, upload);
	}

	@Override
    public FacebookFriendImportParser getParser() {
		return new FacebookFriendImportParser(false);
	}

}
