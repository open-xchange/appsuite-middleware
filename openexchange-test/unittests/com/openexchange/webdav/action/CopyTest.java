package com.openexchange.webdav.action;

import com.openexchange.webdav.protocol.WebdavFactory;

public class CopyTest extends StructureTest {

	@Override
	public WebdavAction getAction(WebdavFactory factory) {
		return new WebdavCopyAction(factory);
	}

}
