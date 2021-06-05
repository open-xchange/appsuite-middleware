/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.webdav.protocol.impl;

import java.util.HashMap;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.exception.OXException;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.Protocol.Property;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavMethod;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

public class DummyLockNull extends DummyCollection {

	private static final WebdavMethod[] OPTIONS = {WebdavMethod.PUT, WebdavMethod.MKCOL, WebdavMethod.OPTIONS, WebdavMethod.PROPFIND, WebdavMethod.LOCK, WebdavMethod.UNLOCK, WebdavMethod.TRACE};
	private WebdavResource realResource;

	public DummyLockNull(final DummyResourceManager manager, final WebdavPath url) {
		super(manager, url);
	}

	public void setRealResource(final WebdavResource res) {
		this.realResource = res;
	}

	@Override
	public boolean isLockNull(){
		return true;
	}

	@Override
	protected boolean isset(final Property p) {
		switch(p.getId()) {
		case Protocol.LOCKDISCOVERY : case Protocol.SUPPORTEDLOCK : case Protocol.DISPLAYNAME :
			return true;
		default: return false;
		}
	}

	@Override
	public void unlock(final String token) throws WebdavProtocolException {
		super.unlock(token);
		if (getOwnLocks().isEmpty()) {
			mgr.removeLockNull(this.getUrl());
		}
	}

	@Override
	public WebdavMethod[] getOptions(){
		return OPTIONS;
	}

	@Override
	public void delete() throws WebdavProtocolException {
		super.delete();
	}

	@Override
	public void create() throws WebdavProtocolException {
		final WebdavResource res = getRealResource();
		try {
            res.create();
        } catch (OXException e) {
            throw new WebdavProtocolException(getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }
		if (res instanceof DummyResource) {
			final DummyResource dres = (DummyResource) res;
			dres.locks = new HashMap<String, WebdavLock>(locks);
		}

	}

	private WebdavResource getRealResource() {
		return realResource;
	}


}
