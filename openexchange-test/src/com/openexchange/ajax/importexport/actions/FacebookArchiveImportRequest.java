package com.openexchange.ajax.importexport.actions;

import java.io.InputStream;

public class FacebookArchiveImportRequest extends AbstractFacebookImportRequest {

	public FacebookArchiveImportRequest(int folderId, InputStream upload) {
		super(Action.FacebookArchive, folderId, upload);
	}

}
