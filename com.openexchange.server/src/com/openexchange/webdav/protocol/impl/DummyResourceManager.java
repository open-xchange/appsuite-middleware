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
import java.util.Map;
import com.openexchange.session.Session;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

public final class DummyResourceManager implements WebdavFactory {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DummyResourceManager.class);
	private static final DummyResourceManager INSTANCE = new DummyResourceManager();

	public static DummyResourceManager getInstance(){
		return INSTANCE;
	}

	private DummyResourceManager(){
		try {
			resolveCollection("/").create();
		} catch (WebdavProtocolException e) {
			LOG.error("Can't resolve root", e);
        }
	}

	private static final Protocol PROTOCOL = new Protocol();

	private final Map<WebdavPath,WebdavResource> resources = new HashMap<WebdavPath,WebdavResource>();
	private final Map<WebdavPath,DummyLockNull> lockNullResources = new HashMap<WebdavPath,DummyLockNull>();

    @Override
    public WebdavResource resolveResource(final WebdavPath url) {
        if (!resources.containsKey(url)) {
			if (lockNullResources.containsKey(url)) {
				final DummyLockNull lockNull = lockNullResources.get(url);
				lockNull.setRealResource(new DummyResource(this,url)); // FIXME Multithreading?
				return lockNull;
			}
			return new DummyResource(this, url);
		}
		return resources.get(url);
    }

    @Override
    public WebdavCollection resolveCollection(final WebdavPath url) {
        if (!resources.containsKey(url)) {
			if (lockNullResources.containsKey(url)) {
				final DummyLockNull lockNull = lockNullResources.get(url);
				lockNull.setRealResource(new DummyCollection(this,url)); // FIXME Multithreading?
				return lockNull;
			}
			return new DummyCollection(this, url);
		}
		return (WebdavCollection) resources.get(url);
    }

    @Override
    public WebdavResource resolveResource(final String url) {
		return resolveResource(new WebdavPath(url));
	}

	@Override
    public WebdavCollection resolveCollection(final String url) {
		return resolveCollection(new WebdavPath(url));
	}

	public synchronized void save(final WebdavPath url, final DummyResource res) {
		getParent(url).addChild(res);
		resources.put(url,res);
	}

	public void remove(final WebdavPath url, final DummyResource res) {
		getParent(url).removeChild(res);
		resources.remove(url);
	}

	private DummyCollection getParent(final WebdavPath url) {

		return (DummyCollection) resolveCollection(url.parent());
	}

	@Override
    public Protocol getProtocol() {
		return PROTOCOL;
	}

	public WebdavResource addLockNullResource(final DummyResource resource) {
		final DummyLockNull lockNull = new DummyLockNull(this, resource.getUrl());
		lockNull.setExists(true);
		lockNullResources.put(lockNull.getUrl(), lockNull);
		return lockNull;
	}

	public void removeLockNull(final WebdavPath url) {
		lockNullResources.remove(url);
	}

	@Override
    public void beginRequest() {
		// Nothing to do

	}

	@Override
    public void endRequest(final int status) {
		// Nothing to do

	}

    @Override
    public Session getSession() {
        return null;
    }

}
