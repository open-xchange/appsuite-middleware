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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import com.openexchange.tools.collections.Injector;
import com.openexchange.tools.collections.OXCollections;
import com.openexchange.webdav.protocol.Protocol.Property;
import com.openexchange.webdav.protocol.*;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class DummyResource extends AbstractResource implements WebdavResource  {

	private static int lockIds;
	
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
		if(url.size() != 0) {
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
	
	public void create() throws WebdavException {
		if(exists) {
			throw new WebdavException("The directory exists already", getUrl(), HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		}
		checkPath();
		exists = true;
		creationDate = new Date();
		lastModified = new Date();
		mgr.save(url,this);
	}

	public boolean exists() throws WebdavException {
		return exists;
	}

	public void delete() throws WebdavException {
		if(!exists) {
			throw new WebdavException("This resource ("+getUrl()+") doesn't exist", getUrl(), HttpServletResponse.SC_NOT_FOUND);
		}
		exists = false;
		mgr.remove(url,this);
	}
	
	@Override
	protected boolean isset(final Property p) {
		return true;
	}

	@Override
	public List<WebdavProperty> internalGetAllProps() throws WebdavException {
		final List<WebdavProperty> props = new ArrayList<WebdavProperty>();
		for(final WebdavProperty prop : properties.values()) {
			props.add(prop);
		}
		return props;
	}
	
	@Override
	protected void internalRemoveProperty(final String namespace, final String name) throws WebdavException {
		final WebdavProperty key = new WebdavProperty();
		key.setName(name);
		key.setNamespace(namespace);
		properties.remove(key);
	}
	
	@Override
	protected void internalPutProperty(final WebdavProperty prop) throws WebdavException {
		final WebdavProperty key = new WebdavProperty();
		key.setName(prop.getName());
		key.setNamespace(prop.getNamespace());
		properties.put(key,prop);
	}

	public void save() throws WebdavException {
		lastModified = new Date();
	}

	public WebdavPath getUrl() {
		return url;
	}

	@Override
	protected WebdavProperty internalGetProperty(final String namespace, final String name) throws WebdavException {
		final WebdavProperty key = new WebdavProperty();
		key.setName(name);
		key.setNamespace(namespace);
		
		return properties.get(key);
	}
	
	public Date getCreationDate() throws WebdavException {
		return creationDate;
	}
	
	@Override
	public void setCreationDate(final Date date) throws WebdavException {
		creationDate = date;
	}
	
	
	public Date getLastModified() throws WebdavException {
		return lastModified;
	}
	
	public String getDisplayName() throws WebdavException{
		return displayName;
	}
	
	public void setDisplayName(final String dispName) throws WebdavException {
		this.displayName = dispName;
	}
		
	public String getLanguage() throws WebdavException{
		return lang;
	}
	
	public void setLanguage(final String lang) throws WebdavException {
		this.lang = lang;
	}
	
	public Long getLength() throws WebdavException {
		return length;
	}
	
	public void setLength(final Long length) throws WebdavException{
		this.length = length;
	}
	
	public String getContentType() throws WebdavException{
		return contentType;
	}
	
	public void setContentType(final String contentType) throws WebdavException {
		this.contentType = contentType;
	}
	
	public String getETag() throws WebdavException{
		return eTag;
	}
	
	public void setSource(final String source) throws WebdavException {
		this.source = source;
	}
	
	public String getSource() throws WebdavException {
		return source;
	}
	
	public boolean isLocked() throws WebdavException {
		return getLocks().size()>0;
	}
	
	public void unlock(final String token) throws WebdavException {
		locks.remove(token);
	}
	
	public void lock(final WebdavLock lock) throws WebdavException {
		if(!exists()) {
			// Create Lock Null Resource
			final WebdavResource res = this.mgr.addLockNullResource(this);
			res.lock(lock);
			return;
		}
		if(null != lock.getToken() && (null != locks.get(lock.getToken()))) {
			//if(null != locks.get(lock.getToken())) {
				locks.put(lock.getToken(),lock);
				return;
			//}
		}
		lock.setToken("opaquelocktoken:"+(lockIds++));
		locks.put(lock.getToken(),lock);
	}
	
	public void setExists(final boolean exists) {
		this.exists = exists;
	}

	public List<WebdavLock> getLocks() throws WebdavException {
		final List<WebdavLock> lockList =  getOwnLocks();
		addParentLocks(lockList);
		return lockList;
	}
	
	public List<WebdavLock> getOwnLocks() throws WebdavException {
		clearTimeoutLocks(locks, System.currentTimeMillis());
		return new ArrayList<WebdavLock>(locks.values());
	}
	
	public WebdavLock getOwnLock(final String token) throws WebdavException{
		clearTimeoutLocks(locks, System.currentTimeMillis());
		return locks.get(token);
	}

	protected synchronized void clearTimeoutLocks(final Map<String, WebdavLock> locks, final long timeout) {
		OXCollections.inject(locks,locks.values(),new Injector<Map<String, WebdavLock>, WebdavLock>(){

			public Map<String, WebdavLock> inject(final Map<String, WebdavLock> list, final WebdavLock element) {
				if(!element.isActive(timeout)) {
					list.remove(element.getToken());
				}
				return list;
			}
			
		});
	}


	public WebdavLock getLock(final String token) throws WebdavException {
		final WebdavLock lock =  locks.get(token);
		if(lock != null) {
			return lock;
		}
		return findParentLock(token);
	}

	@Override
	public void putBody(final InputStream data, final boolean guessLength) throws WebdavException {
		eTag = String.valueOf(Integer.valueOf(eTag)+1);
		final List<Integer> bytes = new ArrayList<Integer>();
		
		int b = 0;
		try {
			while((b = data.read()) != -1) {
				bytes.add(b);
			}
		} catch (final IOException e) {
			throw new WebdavException(e, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		
		int i = 0;
		final byte[] body = new byte[bytes.size()];
		for(final int by : bytes) {
			body[i++] = (byte) by;
		}
		
		this.body = body;
		if(guessLength) {
			this.length = body.length;
		}
	}
	
	
	public InputStream getBody() throws WebdavException{
		if(null == body) {
			return null;
		}
		return new ByteArrayInputStream(body);
	}
	
	@Override
	public boolean hasBody() throws WebdavException {
		return body != null;
	}

	@Override
	public boolean isLockNull() {
		return false;
	}
}
