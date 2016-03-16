/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.webdav.protocol.impl;

import java.util.HashMap;
import java.util.Map;


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
		} catch (final WebdavProtocolException e) {
			LOG.error("Can't resolve root", e);
        }
	}

	private static final Protocol PROTOCOL = new Protocol();

	private final Map<WebdavPath,WebdavResource> resources = new HashMap<WebdavPath,WebdavResource>();
	private final Map<WebdavPath,DummyLockNull> lockNullResources = new HashMap<WebdavPath,DummyLockNull>();

    @Override
    public WebdavResource resolveResource(final WebdavPath url) {
        if(!resources.containsKey(url)) {
			if(lockNullResources.containsKey(url)) {
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
        if(!resources.containsKey(url)) {
			if(lockNullResources.containsKey(url)) {
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

	private String normalize(String url) {
		while(url.contains("//")){
			url = url.replaceAll("//","/");
		}
		return url;
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

}
