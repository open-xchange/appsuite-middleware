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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.exception.OXException;
import com.openexchange.tools.collections.Injector;
import com.openexchange.tools.collections.OXCollections;
import com.openexchange.webdav.protocol.Protocol.Property;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.helpers.AbstractResource;

public class DummyResource extends AbstractResource implements WebdavResource  {

	private static final AtomicInteger lockIds = new AtomicInteger();

	private boolean exists;
	protected DummyResourceManager mgr;
	protected WebdavPath url;
	private String displayName;
	private long length;
	private String eTag;

	private final Map<WebdavProperty, WebdavProperty> properties = new HashMap<WebdavProperty, WebdavProperty>();
	protected Map<String,WebdavLock> locks = new HashMap<String, WebdavLock>();

	private Date creationDate;
	private Date lastModified;

	private String lang;
	private String contentType;

	private String source;

	private byte[] body;

	public DummyResource(final DummyResourceManager manager, final WebdavPath url) {
		this.mgr = manager;
		this.url = url;
		if (url.size() != 0) {
			displayName = url.name();
		} else {
			displayName = "";
		}
		lang = "en";
		eTag = "1";

	}

	@Override
	protected WebdavFactory getFactory() {
		return mgr;
	}

	@Override
    public void create() throws WebdavProtocolException {
		if (exists) {
		    throw WebdavProtocolException.Code.DIRECTORY_ALREADY_EXISTS.create(getUrl(), HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		}
		try {
            checkPath();
        } catch (OXException e) {
            throw new WebdavProtocolException(getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }
		exists = true;
		creationDate = new Date();
		lastModified = new Date();
		mgr.save(url,this);
	}

	@Override
    public boolean exists() throws WebdavProtocolException {
		return exists;
	}

	@Override
    public void delete() throws WebdavProtocolException {
		if (!exists) {
		    throw WebdavProtocolException.Code.FILE_NOT_FOUND.create(getUrl(), HttpServletResponse.SC_NOT_FOUND, getUrl());
		}
		exists = false;
		mgr.remove(url,this);
	}

	@Override
	protected boolean isset(final Property p) {
		return true;
	}

	@Override
	public List<WebdavProperty> internalGetAllProps() throws WebdavProtocolException {
		final List<WebdavProperty> props = new ArrayList<WebdavProperty>();
		for(final WebdavProperty prop : properties.values()) {
			props.add(prop);
		}
		return props;
	}

	@Override
	protected void internalRemoveProperty(final String namespace, final String name) throws WebdavProtocolException {
		final WebdavProperty key = new WebdavProperty();
		key.setName(name);
		key.setNamespace(namespace);
		properties.remove(key);
	}

	@Override
	protected void internalPutProperty(final WebdavProperty prop) throws WebdavProtocolException {
		final WebdavProperty key = new WebdavProperty();
		key.setName(prop.getName());
		key.setNamespace(prop.getNamespace());
		properties.put(key,prop);
	}

	@Override
    public void save() throws WebdavProtocolException {
		lastModified = new Date();
	}

	@Override
    public WebdavPath getUrl() {
		return url;
	}

	@Override
	protected WebdavProperty internalGetProperty(final String namespace, final String name) throws WebdavProtocolException {
		final WebdavProperty key = new WebdavProperty();
		key.setName(name);
		key.setNamespace(namespace);

		return properties.get(key);
	}

	@Override
    public Date getCreationDate() throws WebdavProtocolException {
		return creationDate;
	}

	@Override
	public void setCreationDate(final Date date) throws WebdavProtocolException {
		creationDate = date;
	}


	@Override
    public Date getLastModified() throws WebdavProtocolException {
		return lastModified;
	}

	@Override
    public String getDisplayName() throws WebdavProtocolException {
		return displayName;
	}

	@Override
    public void setDisplayName(final String dispName) throws WebdavProtocolException {
		this.displayName = dispName;
	}

	@Override
    public String getLanguage() throws WebdavProtocolException {
		return lang;
	}

	@Override
    public void setLanguage(final String lang) throws WebdavProtocolException {
		this.lang = lang;
	}

	@Override
    public Long getLength() throws WebdavProtocolException {
		return L(length);
	}

	@Override
    public void setLength(final Long length) throws WebdavProtocolException {
		this.length = length.longValue();
	}

	@Override
    public String getContentType() throws WebdavProtocolException {
		return contentType;
	}

	@Override
    public void setContentType(final String contentType) throws WebdavProtocolException {
		this.contentType = contentType;
	}

	@Override
    public String getETag() throws WebdavProtocolException {
		return eTag;
	}

	@Override
    public void setSource(final String source) throws WebdavProtocolException {
		this.source = source;
	}

	@Override
    public String getSource() throws WebdavProtocolException {
		return source;
	}

	public boolean isLocked() throws WebdavProtocolException {
		return getLocks().size()>0;
	}

	@Override
    public void unlock(final String token) throws WebdavProtocolException {
		locks.remove(token);
	}

	@Override
    public void lock(final WebdavLock lock) throws WebdavProtocolException {
		if (!exists()) {
			// Create Lock Null Resource
			final WebdavResource res = this.mgr.addLockNullResource(this);
			try {
                res.lock(lock);
            } catch (OXException e) {
                throw new WebdavProtocolException(getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
            }
			return;
		}
		if (null != lock.getToken() && (null != locks.get(lock.getToken()))) {
			//if (null != locks.get(lock.getToken())) {
				locks.put(lock.getToken(),lock);
				return;
			//}
		}
		lock.setToken("opaquelocktoken:"+(lockIds.incrementAndGet()));
		locks.put(lock.getToken(),lock);
	}

	public void setExists(final boolean exists) {
		this.exists = exists;
	}

	@Override
    public List<WebdavLock> getLocks() throws WebdavProtocolException {
		final List<WebdavLock> lockList =  getOwnLocks();
		try {
            addParentLocks(lockList);
        } catch (OXException e) {
            throw new WebdavProtocolException(getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }
		return lockList;
	}

	@Override
    public List<WebdavLock> getOwnLocks() throws WebdavProtocolException {
		clearTimeoutLocks(locks, System.currentTimeMillis());
		return new ArrayList<WebdavLock>(locks.values());
	}

	@Override
    public WebdavLock getOwnLock(final String token) throws WebdavProtocolException {
		clearTimeoutLocks(locks, System.currentTimeMillis());
		return locks.get(token);
	}

	protected synchronized void clearTimeoutLocks(final Map<String, WebdavLock> locks, final long timeout) {
		OXCollections.inject(locks,locks.values(),new Injector<Map<String, WebdavLock>, WebdavLock>(){

			@Override
            public Map<String, WebdavLock> inject(final Map<String, WebdavLock> list, final WebdavLock element) {
				if (!element.isActive(timeout)) {
					list.remove(element.getToken());
				}
				return list;
			}

		});
	}


	@Override
    public WebdavLock getLock(final String token) throws WebdavProtocolException {
		final WebdavLock lock =  locks.get(token);
		if (lock != null) {
			return lock;
		}
		try {
            return findParentLock(token);
        } catch (OXException e) {
            throw new WebdavProtocolException(getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }
	}

	@Override
	public void putBody(final InputStream data, final boolean guessLength) throws WebdavProtocolException {
		eTag = Integer.toString(Integer.parseInt(eTag)+1);
		final List<Integer> bytes = new ArrayList<Integer>();

		int b = 0;
		try {
			while ((b = data.read()) != -1) {
				bytes.add(I(b));
			}
		} catch (IOException e) {
			throw WebdavProtocolException.Code.GENERAL_ERROR.create(getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

		int i = 0;
		final byte[] body = new byte[bytes.size()];
		for(final int by : bytes) {
			body[i++] = (byte) by;
		}

		this.body = body;
		if (guessLength) {
			this.length = body.length;
		}
	}

	@Override
    public InputStream getBody() throws WebdavProtocolException {
		if (null == body) {
			return null;
		}
		return new ByteArrayInputStream(body);
	}

	@Override
	public boolean hasBody() throws WebdavProtocolException {
		return body != null;
	}

	@Override
	public boolean isLockNull() {
		return false;
	}
}
