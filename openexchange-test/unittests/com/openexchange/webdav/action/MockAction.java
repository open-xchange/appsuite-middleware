package com.openexchange.webdav.action;

import com.openexchange.webdav.protocol.WebdavException;

public class MockAction extends AbstractAction {

	private boolean activated;

	public void perform(final WebdavRequest req, final WebdavResponse res)
			throws WebdavException {
		activated = true;
	}

	public boolean wasActivated() {
		return activated;
	}

	public void setActivated(final boolean activated) {
		this.activated = activated;
	}

}
