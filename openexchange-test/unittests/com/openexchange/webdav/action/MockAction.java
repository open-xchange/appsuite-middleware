package com.openexchange.webdav.action;

import com.openexchange.exception.OXException;
public class MockAction extends AbstractAction {

	private boolean activated;

	@Override
    public void perform(final WebdavRequest req, final WebdavResponse res)
			throws OXException {
		activated = true;
	}

	public boolean wasActivated() {
		return activated;
	}

	public void setActivated(final boolean activated) {
		this.activated = activated;
	}

}
