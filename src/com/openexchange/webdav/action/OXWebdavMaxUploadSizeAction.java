package com.openexchange.webdav.action;

import com.openexchange.groupware.infostore.utils.InfostoreConfigUtils;
import com.openexchange.sessiond.SessionHolder;

public class OXWebdavMaxUploadSizeAction extends WebdavMaxUploadSizeAction {
	private SessionHolder sessionHolder;

	@Override
	public boolean fits(WebdavRequest req){
		if(sessionHolder == null) {
			return true;
		}
		
		long maxSize = InfostoreConfigUtils.determineRelevantUploadSize(sessionHolder.getSessionObject().getUserSettingMail());
		if(maxSize < 1)
			return true;
		
		return maxSize >= Long.parseLong(req.getHeader("content-length"));
	}
	
	public void setSessionHolder(SessionHolder holder){
		this.sessionHolder = holder;
	}
}
