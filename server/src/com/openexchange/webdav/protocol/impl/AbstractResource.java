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

import com.openexchange.webdav.protocol.*;
import com.openexchange.webdav.protocol.Protocol.Property;
import com.openexchange.webdav.protocol.Protocol.WEBDAV_METHOD;
import com.openexchange.webdav.protocol.util.PropertySwitch;
import com.openexchange.webdav.protocol.util.Utils;
import com.openexchange.webdav.xml.WebdavLockWriter;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class AbstractResource implements WebdavResource {
	
	private static final WEBDAV_METHOD[] OPTIONS = {WEBDAV_METHOD.GET, WEBDAV_METHOD.PUT, WEBDAV_METHOD.DELETE, WEBDAV_METHOD.HEAD, WEBDAV_METHOD.OPTIONS, WEBDAV_METHOD.TRACE, WEBDAV_METHOD.PROPPATCH, WEBDAV_METHOD.PROPFIND, WEBDAV_METHOD.MOVE, WEBDAV_METHOD.COPY, WEBDAV_METHOD.LOCK, WEBDAV_METHOD.UNLOCK};
	
	protected void checkPath() throws WebdavException {
		checkParentExists(getUrl());
	}
	
	protected void checkParentExists(final WebdavPath url) throws WebdavException {
		WebdavPath check = new WebdavPath();

        for(String comp : url) {
			check.append(url);
            if(check.equals(url)) break;
            final WebdavResource res = getFactory().resolveResource(check);
			if(!res.exists() || !res.isCollection()) {
				throw new WebdavException("Conflict with: "+res.getUrl()+" exists: "+res.exists()+" collection: "+res.isCollection(), getUrl(), HttpServletResponse.SC_CONFLICT);
			}
		}
	}
	
	public void putBody(final InputStream body) throws WebdavException{
		putBody(body,false);
	}
	
	public void putBodyAndGuessLength(final InputStream body) throws WebdavException{
		putBody(body, true);
	}
	
	public String getResourceType() throws WebdavException{
		return null;
	}
	
	public WebdavResource move(final WebdavPath string) throws WebdavException {
		return move(string,false, true);
	}

	public WebdavResource copy(final WebdavPath string) throws WebdavException {
		return copy(string,false, true);
	}
	
	public WebdavResource reload() throws WebdavException {
		return this.getFactory().resolveResource(getUrl());
	}
	
	public WebdavResource move(final WebdavPath dest, final boolean noroot, final boolean overwrite) throws WebdavException {
		final WebdavResource copy = copy(dest);
		delete();
		((AbstractResource)copy).setCreationDate(getCreationDate());
		return copy;
	}
	
	public WebdavResource copy(final WebdavPath dest, final boolean noroot, final boolean overwrite) throws WebdavException {
		final AbstractResource clone = instance(dest);
		if(hasBody()) {
			clone.putBody(getBody());
		}
		for(WebdavProperty prop : getAllProps()) {
			clone.putProperty(prop);
		}
		clone.create();
		return clone;
	}


	public AbstractResource instance(final WebdavPath dest) throws WebdavException {
		return (AbstractResource) getFactory().resolveResource(dest);
	}
	
	public void removeProperty(final String namespace, final String name) throws WebdavException {
		internalRemoveProperty(namespace,name);
	}
	
	public void putProperty(final WebdavProperty prop) throws WebdavException {
		if(handleSpecialPut(prop)) {
			return;
		}
		internalPutProperty(prop);
	}


	public WebdavProperty getProperty(final String namespace, final String name) throws WebdavException {
		final WebdavProperty prop = handleSpecialGet(namespace, name);
		if(prop != null) {
			return prop;
		}
		return internalGetProperty(namespace, name);
	}
	
	public List<WebdavProperty> getAllProps() throws WebdavException{
		final List<WebdavProperty> props = internalGetAllProps();
		for(Property p : getFactory().getProtocol().getKnownProperties()){
			final WebdavProperty prop = getProperty(p.getNamespace(),p.getName());
			if(prop != null) {
				props.add(prop);
			}
		}
		
		return props;
	}
	
	public boolean isCollection() {
		return false;
	}
	
	public boolean isLockNull(){
		return false;
	}
	
	public WEBDAV_METHOD[] getOptions(){
		return OPTIONS;
	}
	
	public WebdavCollection toCollection(){
		throw new IllegalStateException("This resource is no collection");
	}
	
	protected void addParentLocks(final List<WebdavLock> lockList) throws WebdavException {
		for(WebdavResource res : parents()) {
			for(WebdavLock lock : res.getOwnLocks()) {
				if(lock.locks(res, this)){
					lockList.add(lock);
				}
			}
		}
	}
	
	protected WebdavLock findParentLock(final String token) throws WebdavException {
		for(WebdavResource res : parents()) {
			final WebdavLock lock = res.getOwnLock(token);
			if(null != lock && lock.locks(res, this)) {
				return lock;
			}
		}
		return null;
	}
	
	protected WebdavCollection parent() throws WebdavException{
		return getFactory().resolveCollection(getUrl().parent());
	}
	
	protected List<WebdavCollection> parents() throws WebdavException{
		final List<WebdavCollection> parents = new ArrayList<WebdavCollection>();
		WebdavPath path = new WebdavPath();
		for(String comp : getUrl()) {
			path.append(comp);
            if(path.equals(getUrl())) break;
            final WebdavCollection res = getFactory().resolveCollection(path);
			parents.add(res);

		}
		return parents;
	}
	
	protected boolean handleSpecialPut(final WebdavProperty prop) throws WebdavException{
		final Property p = getFactory().getProtocol().get(prop.getNamespace(),prop.getName());
		if(p == null) {
			return false;
		}
		final SpecialSetSwitch setter = getSetSwitch(prop.getValue());
		
		return ((Boolean) p.doSwitch(setter)).booleanValue();
	}
	
	protected SpecialSetSwitch getSetSwitch(final String value) {
		return new SpecialSetSwitch(value);
	}

	protected WebdavProperty handleSpecialGet(final String namespace, final String name) throws WebdavException {
		final Property p = getFactory().getProtocol().get(namespace,name);
		if(p == null) {
			return null;
		}
		if(!isset(p)) {
			return null;
		}
		final String value = (String) p.doSwitch(getGetSwitch(this));
		
		final WebdavProperty retVal = p.getWebdavProperty();
		retVal.setValue(value);
		// FIXME make overridable call
		switch(p.getId()) {
		case Protocol.SUPPORTEDLOCK : 
		case Protocol.RESOURCETYPE :
		case Protocol.LOCKDISCOVERY: retVal.setXML(true); break;
		default : retVal.setXML(false); break;
		}
		return retVal;
	}

	protected PropertySwitch getGetSwitch(final AbstractResource resource) {
		return new SpecialGetSwitch();
	}
	

	@Override
	public int hashCode(){
		return getUrl().hashCode();
	}
	
	@Override
	public boolean equals(final Object o){
		if (o instanceof WebdavResource) {
			final WebdavResource res = (WebdavResource) o;
			return res.getUrl().equals(getUrl());
		}
		return false;
	}
	
	@Override
	public String toString(){
		return getUrl().toString();
	}

	public abstract void putBody(InputStream body, boolean guessSize) throws WebdavException;
	
	public abstract boolean hasBody() throws WebdavException;

	public abstract void setCreationDate(Date date) throws WebdavException;

	protected abstract List<WebdavProperty> internalGetAllProps() throws WebdavException;

	protected abstract WebdavFactory getFactory();
	
	protected abstract void internalPutProperty(WebdavProperty prop) throws WebdavException;
	
	protected abstract void internalRemoveProperty(String namespace, String name) throws WebdavException;
	
	protected abstract WebdavProperty internalGetProperty(String namespace, String name) throws WebdavException;
	
	protected abstract boolean isset(Property p);
	
	public class SpecialGetSwitch implements PropertySwitch{

		public Object creationDate() throws WebdavException {
			return Utils.convert(getCreationDate());
		}

		public Object displayName() throws WebdavException {
			return getDisplayName();
		}

		public Object contentLanguage() throws WebdavException {
			return getLanguage();
		}

		public Object contentLength() throws WebdavException {
			final Long l = getLength();
			if(l == null) {
				return null;
			}
			return l.toString();
		}

		public Object contentType() throws WebdavException {
			return getContentType();
		}

		public Object etag() throws WebdavException {
			return getETag();
		}

		public Object lastModified() throws WebdavException {
			return Utils.convert(getLastModified());
		}

		public Object resourceType() throws WebdavException {
			return getResourceType();
		}

		public Object lockDiscovery() throws WebdavException {
			final StringBuffer activeLocks = new StringBuffer();
			final WebdavLockWriter writer = new WebdavLockWriter();
			for(WebdavLock lock : getLocks()){
				activeLocks.append(writer.lock2xml(lock));
			}
			return activeLocks.toString();
		}

		public Object supportedLock() throws WebdavException {
			return "<D:lockentry><D:lockscope><D:exclusive/></D:lockscope><D:locktype><D:write/></D:locktype></D:lockentry><D:lockentry><D:lockscope><D:shared/></D:lockscope><D:locktype><D:write/></D:locktype></D:lockentry>";
		}

		public Object source() throws WebdavException {
			return getSource();
		}
		
	}
	
	public class SpecialSetSwitch implements PropertySwitch{

		private String value;

		public SpecialSetSwitch(String value) {
			this.value = value;
		}
		
		public Object creationDate() throws WebdavException {
			return Boolean.TRUE;
		}

		public Object displayName() throws WebdavException {
			setDisplayName(value);
			return Boolean.TRUE;
		}

		public Object contentLanguage() throws WebdavException {
			setLanguage(value);
			return Boolean.TRUE;
		}

		public Object contentLength() throws WebdavException {
			setLength(new Long(value));
			return Boolean.TRUE;
		}

		public Object contentType() throws WebdavException {
			setContentType(value);
			return Boolean.TRUE;
		}

		public Object etag() throws WebdavException {
			return Boolean.TRUE;
		}

		public Object lastModified() throws WebdavException {
			return Boolean.TRUE;
		}

		public Object resourceType() throws WebdavException {
			return Boolean.TRUE;
		}

		public Object lockDiscovery() throws WebdavException {
			return Boolean.TRUE;
		}

		public Object supportedLock() throws WebdavException {
			return Boolean.TRUE;
		}

		public Object source() throws WebdavException {
			setSource(value);
			return Boolean.TRUE;
		}
		
	}

}
