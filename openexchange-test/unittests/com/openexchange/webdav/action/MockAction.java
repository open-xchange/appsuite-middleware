package com.openexchange.webdav.action;

import com.openexchange.webdav.protocol.WebdavProtocolException;

public class MockAction extends AbstractAction {

	private boolean activated;

	public void perform(final WebdavRequest req, final WebdavResponse res)
			throws WebdavProtocolException {
		activated = true;
	}

	public boolean wasActivated() {
		return activated;
	}

	public void setActivated(final boolean activated) {
		this.activated = activated;
	}

}
