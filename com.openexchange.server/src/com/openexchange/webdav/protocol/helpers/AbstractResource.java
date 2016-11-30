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

package com.openexchange.webdav.protocol.helpers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.Protocol.Property;
import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavMethod;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.util.PropertySwitch;
import com.openexchange.webdav.protocol.util.Utils;
import com.openexchange.webdav.xml.WebdavLockWriter;

public abstract class AbstractResource implements WebdavResource {

	private static final WebdavMethod[] OPTIONS = {WebdavMethod.GET, WebdavMethod.PUT, WebdavMethod.DELETE, WebdavMethod.HEAD, WebdavMethod.OPTIONS, WebdavMethod.TRACE, WebdavMethod.PROPPATCH, WebdavMethod.PROPFIND, WebdavMethod.MOVE, WebdavMethod.COPY, WebdavMethod.LOCK, WebdavMethod.UNLOCK, WebdavMethod.REPORT, WebdavMethod.ACL, WebdavMethod.MKCALENDAR};

	protected List<PropertyMixin> mixins = new ArrayList<PropertyMixin>();

	public void includeProperties(final PropertyMixin...mixins) {
	    for (final PropertyMixin mixin : mixins) {
	        this.mixins.add(mixin);
        }
	}

	protected void checkPath() throws WebdavProtocolException {
		checkParentExists(getUrl());
	}

	protected void checkParentExists(final WebdavPath url) throws WebdavProtocolException {
		final WebdavPath check = new WebdavPath();

        for(final String comp : url) {
			check.append(url);
            if(check.equals(url)) {
				break;
			}
            final WebdavResource res = getFactory().resolveResource(check);
			if(!res.exists()) {
			    throw WebdavProtocolException.Code.FILE_NOT_FOUND.create(getUrl(), HttpServletResponse.SC_CONFLICT, res.getUrl());
			}
			if (!res.isCollection()) {
			    throw WebdavProtocolException.Code.FILE_IS_DIRECTORY.create(getUrl(), HttpServletResponse.SC_CONFLICT, res.getUrl());
			}
		}
	}

	@Override
    public void putBody(final InputStream body) throws WebdavProtocolException {
		putBody(body,false);
	}

	@Override
    public void putBodyAndGuessLength(final InputStream body) throws WebdavProtocolException {
		putBody(body, true);
	}

	@Override
    public String getResourceType() throws WebdavProtocolException {
		return null;
	}

	@Override
    public WebdavResource move(final WebdavPath string) throws WebdavProtocolException {
		return move(string,false, true);
	}

	@Override
    public WebdavResource copy(final WebdavPath string) throws WebdavProtocolException {
		return copy(string,false, true);
	}

	@Override
    public WebdavResource reload() throws WebdavProtocolException {
		return this.getFactory().resolveResource(getUrl());
	}

	@Override
    public WebdavResource move(final WebdavPath dest, final boolean noroot, final boolean overwrite) throws WebdavProtocolException {
		final WebdavResource copy = copy(dest);
		delete();
		((AbstractResource)copy).setCreationDate(getCreationDate());
		return copy;
	}

	@Override
    public WebdavResource copy(final WebdavPath dest, final boolean noroot, final boolean overwrite) throws WebdavProtocolException {
		final AbstractResource clone = instance(dest);
		if(hasBody()) {
			InputStream body = getBody();
            try {
                clone.putBody(body);
            } finally {
                Streams.close(body);
            }
		}
		for(final WebdavProperty prop : getAllProps()) {
			clone.putProperty(prop);
		}
		clone.create();
		return clone;
	}


	public AbstractResource instance(final WebdavPath dest) throws WebdavProtocolException {
		return (AbstractResource) getFactory().resolveResource(dest);
	}

	@Override
    public void removeProperty(final String namespace, final String name) throws WebdavProtocolException {
		internalRemoveProperty(namespace,name);
	}

	@Override
    public void putProperty(final WebdavProperty prop) throws WebdavProtocolException {
		if(handleSpecialPut(prop)) {
			return;
		}
		internalPutProperty(prop);
	}

    @Override
    public WebdavProperty getProperty(WebdavProperty property) throws WebdavProtocolException {
        WebdavProperty prop = handleSpecialGet(property.getNamespace(), property.getName());
        if(prop != null) {
            return prop;
        }
        prop = getFromMixin(property.getNamespace(), property.getName());
        if (prop != null) {
            return prop;
        }
        return internalGetProperty(property);
    }

	@Override
    public WebdavProperty getProperty(final String namespace, final String name) throws WebdavProtocolException {
	    return getProperty(new WebdavProperty(namespace, name));
	}

    @Override
    public List<WebdavProperty> getAllProps() throws WebdavProtocolException{
		final List<WebdavProperty> props = new ArrayList<WebdavProperty>(internalGetAllProps());
		props.addAll(getAllFromMixin());
		for(final Property p : getFactory().getProtocol().getKnownProperties()){
			final WebdavProperty prop = getProperty(p.getNamespace(),p.getName());
			if(prop != null) {
				props.add(prop);
			}
		}

		return props;
	}

    protected List<WebdavProperty> getAllFromMixin() throws WebdavProtocolException {
        final List<WebdavProperty> allProps = new ArrayList<WebdavProperty>();
        for (final PropertyMixin mixin : mixins) {
            List<WebdavProperty> properties;
            try {
                if (ResourcePropertyMixin.class.isInstance(mixin)) {
                    properties = ((ResourcePropertyMixin) mixin).getAllProperties(this);
                } else {
                    properties = mixin.getAllProperties();
                }
            } catch (final OXException e) {
                if (e instanceof WebdavProtocolException) {
                    throw (WebdavProtocolException) e;
                }
                throw new WebdavProtocolException(getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);

            }
            allProps.addAll(properties);
        }
        return allProps;
    }

    protected WebdavProperty getFromMixin(final String namespace, final String name) throws WebdavProtocolException {
        for (final PropertyMixin mixin : mixins) {
            WebdavProperty property;
            try {
                if (ResourcePropertyMixin.class.isInstance(mixin)) {
                    property = ((ResourcePropertyMixin) mixin).getProperty(this, namespace, name);
                } else {
                    property = mixin.getProperty(namespace, name);
                }
            } catch (final OXException e) {
                if (e instanceof WebdavProtocolException) {
                    throw (WebdavProtocolException) e;
                }
                throw new WebdavProtocolException(getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
            }
            if (property != null) {
                return property;
            }
        }
        return null;
    }

	@Override
    public boolean isCollection() {
		return false;
	}

	@Override
    public boolean isLockNull(){
		return false;
	}

	@Override
    public WebdavMethod[] getOptions(){
		return OPTIONS;
	}

	@Override
    public WebdavCollection toCollection(){
		throw new IllegalStateException("This resource is no collection");
	}

	protected void addParentLocks(final List<WebdavLock> lockList) throws WebdavProtocolException {
		for(final WebdavResource res : parents()) {
			for(final WebdavLock lock : res.getOwnLocks()) {
				if(lock.locks(res, this)){
					lockList.add(lock);
				}
			}
		}
	}

	protected WebdavLock findParentLock(final String token) throws WebdavProtocolException {
		for(final WebdavResource res : parents()) {
			final WebdavLock lock = res.getOwnLock(token);
			if(null != lock && lock.locks(res, this)) {
				return lock;
			}
		}
		return null;
	}

	protected WebdavCollection parent() throws WebdavProtocolException{
		return getFactory().resolveCollection(getUrl().parent());
	}

	protected List<WebdavCollection> parents() throws WebdavProtocolException{
		final List<WebdavCollection> parents = new ArrayList<WebdavCollection>();
		final WebdavPath path = new WebdavPath();
		for(final String comp : getUrl()) {
			path.append(comp);
            if(path.equals(getUrl())) {
				break;
			}
            final WebdavCollection res = getFactory().resolveCollection(path);
			parents.add(res);

		}
		return parents;
	}

	protected boolean handleSpecialPut(final WebdavProperty prop) throws WebdavProtocolException{
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

	protected WebdavProperty handleSpecialGet(final String namespace, final String name) throws WebdavProtocolException {
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

	@Override
    public Protocol getProtocol() {
	    return getFactory().getProtocol();
	}

	public abstract void putBody(InputStream body, boolean guessSize) throws WebdavProtocolException;

	public abstract boolean hasBody() throws WebdavProtocolException;

	public abstract void setCreationDate(Date date) throws WebdavProtocolException;

	protected abstract List<WebdavProperty> internalGetAllProps() throws WebdavProtocolException;

	protected abstract WebdavFactory getFactory();

	protected abstract void internalPutProperty(WebdavProperty prop) throws WebdavProtocolException;

	protected abstract void internalRemoveProperty(String namespace, String name) throws WebdavProtocolException;

    protected abstract WebdavProperty internalGetProperty(String namespace, String name) throws WebdavProtocolException;

    protected WebdavProperty internalGetProperty(WebdavProperty property) throws WebdavProtocolException {
        return internalGetProperty(property.getNamespace(), property.getName());
    }

	protected abstract boolean isset(Property p);

	public class SpecialGetSwitch implements PropertySwitch{

		@Override
        public Object creationDate() throws WebdavProtocolException {
			return Utils.convert(getCreationDate());
		}

		@Override
        public Object displayName() throws WebdavProtocolException {
			return getDisplayName();
		}

		@Override
        public Object contentLanguage() throws WebdavProtocolException {
			return getLanguage();
		}

		@Override
        public Object contentLength() throws WebdavProtocolException {
			final Long l = getLength();
			if(l == null) {
				return null;
			}
			return l.toString();
		}

		@Override
        public Object contentType() throws WebdavProtocolException {
			return getContentType();
		}

		@Override
        public Object etag() throws WebdavProtocolException {
			return getETag();
		}

		@Override
        public Object lastModified() throws WebdavProtocolException {
			return Utils.convert(getLastModified());
		}

		@Override
        public Object resourceType() throws WebdavProtocolException {
			return getResourceType();
		}

		@Override
        public Object lockDiscovery() throws WebdavProtocolException {
			final StringBuffer activeLocks = new StringBuffer();
			final WebdavLockWriter writer = new WebdavLockWriter();
			for(final WebdavLock lock : getLocks()){
				activeLocks.append(writer.lock2xml(lock));
			}
			return activeLocks.toString();
		}

		@Override
        public Object supportedLock() throws WebdavProtocolException {
			return "<D:lockentry><D:lockscope><D:exclusive/></D:lockscope><D:locktype><D:write/></D:locktype></D:lockentry><D:lockentry><D:lockscope><D:shared/></D:lockscope><D:locktype><D:write/></D:locktype></D:lockentry>";
		}

		@Override
        public Object source() throws WebdavProtocolException {
			return getSource();
		}

	}

	public class SpecialSetSwitch implements PropertySwitch{

		protected final String value;

		public SpecialSetSwitch(final String value) {
			this.value = value;
		}

		@Override
        public Object creationDate() throws WebdavProtocolException {
			return Boolean.TRUE;
		}

		@Override
        public Object displayName() throws WebdavProtocolException {
			setDisplayName(value);
			return Boolean.TRUE;
		}

		@Override
        public Object contentLanguage() throws WebdavProtocolException {
			setLanguage(value);
			return Boolean.TRUE;
		}

		@Override
        public Object contentLength() throws WebdavProtocolException {
			setLength(new Long(value));
			return Boolean.TRUE;
		}

		@Override
        public Object contentType() throws WebdavProtocolException {
			setContentType(value);
			return Boolean.TRUE;
		}

		@Override
        public Object etag() throws WebdavProtocolException {
			return Boolean.TRUE;
		}

		@Override
        public Object lastModified() throws WebdavProtocolException {
			return Boolean.TRUE;
		}

		@Override
        public Object resourceType() throws WebdavProtocolException {
			return Boolean.TRUE;
		}

		@Override
        public Object lockDiscovery() throws WebdavProtocolException {
			return Boolean.TRUE;
		}

		@Override
        public Object supportedLock() throws WebdavProtocolException {
			return Boolean.TRUE;
		}

		@Override
        public Object source() throws WebdavProtocolException {
			setSource(value);
			return Boolean.TRUE;
		}

	}

}
