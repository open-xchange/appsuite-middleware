package com.openexchange.webdav.action;

import javax.servlet.http.HttpServletResponse;

import com.openexchange.webdav.protocol.WebdavException;

public class WebdavMaxUploadSizeAction extends AbstractAction {

	public void perform(WebdavRequest req, WebdavResponse res) throws WebdavException {
		if(fits(req)) {
			yield(req,res);
		} else {
			throw new WebdavException(req.getUrl(), HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
		}
	}
	
	public boolean fits(WebdavRequest req){
		return true;
	}

}
