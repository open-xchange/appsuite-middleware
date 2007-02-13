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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.openexchange.tools.collections.Injector;
import com.openexchange.tools.collections.OXCollections;
import com.openexchange.webdav.protocol.WebdavException;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.Protocol.Property;

public class DummyResource extends AbstractResource implements WebdavResource  {

	private static int lockIds = 0;
	
	private boolean exists;
	protected DummyResourceManager mgr;
	protected String url;
	private String displayName;
	private long length;
	private String eTag;
	
	private Map<WebdavProperty, WebdavProperty> properties = new HashMap<WebdavProperty, WebdavProperty>();
	protected Map<String,WebdavLock> locks = new HashMap<String, WebdavLock>();

	private Date creationDate;
	private Date lastModified;
	
	private String lang;
	private String contentType;
	
	private String source;
	
	private byte[] body;

	public DummyResource(DummyResourceManager manager, String url) {
		this.mgr = manager;
		this.url = url;
		if(url.contains("/"))
			displayName = url.substring(url.lastIndexOf("/")+1);
		else
			displayName = url;
		lang = "en";
		eTag = "1";

	}

	@Override
	protected WebdavFactory getFactory() {
		return mgr;
	}
	
	public void create() throws WebdavException {
		if(exists)
			throw new WebdavException("The directory exists already", getUrl(), HttpServletResponse.SC_METHOD_NOT_ALLOWED);
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
		if(!exists)
			throw new WebdavException("This resource ("+getUrl()+") doesn't exist", getUrl(), HttpServletResponse.SC_NOT_FOUND);
		exists = false;
		mgr.remove(url,this);
	}
	
	protected boolean isset(Property p) {
		return true;
	}

	public List<WebdavProperty> internalGetAllProps() throws WebdavException {
		List<WebdavProperty> props = new ArrayList<WebdavProperty>();
		for(WebdavProperty prop : properties.values())
			props.add(prop);
		return props;
	}
	
	protected void internalRemoveProperty(String namespace, String name) throws WebdavException {
		WebdavProperty key = new WebdavProperty();
		key.setName(name);
		key.setNamespace(namespace);
		properties.remove(key);
	}
	
	protected void internalPutProperty(WebdavProperty prop) throws WebdavException {
		WebdavProperty key = new WebdavProperty();
		key.setName(prop.getName());
		key.setNamespace(prop.getNamespace());
		properties.put(key,prop);
	}

	public void save() throws WebdavException {
		lastModified = new Date();
	}

	public String getUrl() {
		return url;
	}

	protected WebdavProperty internalGetProperty(String namespace, String name) throws WebdavException {
		WebdavProperty key = new WebdavProperty();
		key.setName(name);
		key.setNamespace(namespace);
		
		return properties.get(key);
	}
	
	public Date getCreationDate() throws WebdavException {
		return creationDate;
	}
	
	public void setCreationDate(Date date) throws WebdavException {
		creationDate = date;
	}
	
	
	public Date getLastModified() throws WebdavException {
		return lastModified;
	}
	
	public String getDisplayName() throws WebdavException{
		return displayName;
	}
	
	public void setDisplayName(String dispName) throws WebdavException {
		this.displayName = dispName;
	}
		
	public String getLanguage() throws WebdavException{
		return lang;
	}
	
	public void setLanguage(String lang) throws WebdavException {
		this.lang = lang;
	}
	
	public Long getLength() throws WebdavException {
		return length;
	}
	
	public void setLength(Long length) throws WebdavException{
		this.length = length;
	}
	
	public String getContentType() throws WebdavException{
		return contentType;
	}
	
	public void setContentType(String contentType) throws WebdavException {
		this.contentType = contentType;
	}
	
	public String getETag() throws WebdavException{
		return eTag;
	}
	
	public void setSource(String source) throws WebdavException {
		this.source = source;
	}
	
	public String getSource() throws WebdavException {
		return source;
	}
	
	public boolean isLocked() throws WebdavException {
		return getLocks().size()>0;
	}
	
	public void unlock(String token) throws WebdavException {
		locks.remove(token);
	}
	
	public void lock(WebdavLock lock) throws WebdavException {
		if(!exists()) {
			// Create Lock Null Resource
			WebdavResource res = this.mgr.addLockNullResource(this);
			res.lock(lock);
			return;
		}
		if(null != lock.getToken()) {
			if(null != locks.get(lock.getToken())) {
				locks.put(lock.getToken(),lock);
				return;
			}
		}
		lock.setToken("opaquelocktoken:"+(lockIds++));
		locks.put(lock.getToken(),lock);
	}
	
	public void setExists(boolean exists) {
		this.exists = exists;
	}

	public List<WebdavLock> getLocks() throws WebdavException {
		List<WebdavLock> lockList =  getOwnLocks();
		addParentLocks(lockList);
		return lockList;
	}
	
	public List<WebdavLock> getOwnLocks() throws WebdavException {
		clearTimeoutLocks(locks, System.currentTimeMillis());
		return new ArrayList<WebdavLock>(locks.values());
	}
	
	public WebdavLock getOwnLock(String token) throws WebdavException{
		clearTimeoutLocks(locks, System.currentTimeMillis());
		return locks.get(token);
	}

	protected synchronized void clearTimeoutLocks(Map<String, WebdavLock> locks, final long timeout) {
		OXCollections.inject(locks,locks.values(),new Injector<Map<String, WebdavLock>, WebdavLock>(){

			public Map<String, WebdavLock> inject(Map<String, WebdavLock> list, WebdavLock element) {
				if(!element.isActive(timeout))
					list.remove(element.getToken());
				return list;
			}
			
		});
	}


	public WebdavLock getLock(String token) throws WebdavException {
		WebdavLock lock =  locks.get(token);
		if(lock != null)
			return lock;
		return findParentLock(token);
	}

	public void putBody(InputStream data, boolean guessLength) throws WebdavException {
		eTag = String.valueOf(Integer.valueOf(eTag)+1);
		List<Integer> bytes = new ArrayList<Integer>();
		
		int b = 0;
		try {
			while((b = data.read()) != -1) {
				bytes.add(b);
			}
		} catch (IOException e) {
			throw new WebdavException(e, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		
		int i = 0;
		byte[] body = new byte[bytes.size()];
		for(int by : bytes) {
			body[i++] = (byte) by;
		}
		
		this.body = body;
		if(guessLength)
			this.length = body.length;
	}
	
	
	public InputStream getBody() throws WebdavException{
		if(null == body)
			return null;
		return new ByteArrayInputStream(body);
	}
	
	public boolean hasBody() throws WebdavException {
		return body != null;
	}

	public boolean isLockNull() {
		return false;
	}
}
