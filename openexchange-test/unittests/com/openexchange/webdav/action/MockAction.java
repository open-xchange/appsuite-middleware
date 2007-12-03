package com.openexchange.webdav.action;

import com.openexchange.webdav.protocol.WebdavException;

public class MockAction extends AbstractAction {

	private boolean activated;

	public void perform(WebdavRequest req, WebdavResponse res)
			throws WebdavException {
		activated = true;
	}

	public boolean wasActivated() {
		return activated;
	}

	public void setActivated(boolean activated) {
		this.activated = activated;
	}

}
