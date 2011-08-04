package com.openexchange.ajax.importexport.actions;

import java.io.InputStream;

public class FacebookFriendsImportRequest extends AbstractFacebookImportRequest {

	public FacebookFriendsImportRequest( int folderId, InputStream upload) {
		super(Action.FacebookFriends, folderId, upload);
	}

}
