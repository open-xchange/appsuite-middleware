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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.exception.OXException;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.Protocol.Property;
import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavMultistatusException;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

public class DummyCollection extends DummyResource implements WebdavCollection {

	private final List<WebdavResource> children = new ArrayList<WebdavResource>();

	public DummyCollection(final DummyResourceManager manager, final WebdavPath url) {
		super(manager,url);
	}

	@Override
	protected boolean isset(final Property p) {
		switch(p.getId()) {
		case Protocol.GETCONTENTLANGUAGE : case Protocol.GETCONTENTLENGTH : case Protocol.GETCONTENTTYPE : case Protocol.GETETAG :
			return false;
		default: return true;
		}
	}

	@Override
    public boolean isCollection(){
		return true;
	}

	@Override
    public WebdavCollection toCollection() {
		return this;
	}

	@Override
    public String getResourceType() throws WebdavProtocolException {
		return Protocol.COLLECTION;
	}

	@Override
	public void putBody(final InputStream data, final boolean guessSize) throws WebdavProtocolException {
	    throw WebdavProtocolException.Code.NO_BODIES_ALLOWED.create(getUrl(), HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
	}

	@Override
	public String getLanguage() throws WebdavProtocolException{
		return null;
	}

	@Override
	public void setLanguage(final String lang) throws WebdavProtocolException {
	    throw WebdavProtocolException.Code.NO_BODIES_ALLOWED.create(getUrl(), HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
	}

	@Override
	public Long getLength() throws WebdavProtocolException{
		return null;
	}

	@Override
	public void delete() throws WebdavProtocolException {
		final List<WebdavResource> copy = new ArrayList<WebdavResource>(children);
		final List<WebdavProtocolException> exceptions = new ArrayList<WebdavProtocolException>();
		for(final WebdavResource res : copy) {
			try {
				res.delete();
			} catch (WebdavProtocolException x) {
				exceptions.add(x);
			}
		}
		try {
			super.delete();
		} catch (WebdavProtocolException x) {
			exceptions.add(x);
		}
		if (exceptions.size() > 0) {
			throw WebdavMultistatusException.create(getUrl(), exceptions);
		}
	}

	@Override
    public DummyCollection instance(final WebdavPath url) {
		return new DummyCollection(mgr,url);
	}


	@Override
    public WebdavResource copy(final WebdavPath dest, final boolean noroot, final boolean overwrite) throws WebdavProtocolException {
		try {
            checkParentExists(dest);
        } catch (OXException e) {
            throw new WebdavProtocolException(getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }
		final List<WebdavProtocolException> exceptions = new ArrayList<WebdavProtocolException>();
		try {
			WebdavResource copy = null;
			if (!noroot) {
				try {
                    copy = super.copy(dest,noroot, overwrite);
                } catch (OXException e) {
                    throw new WebdavProtocolException(getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
                }
			} else {
				copy = mgr.resolveCollection(dest);
			}

			final List<WebdavResource> tmpList = new ArrayList<WebdavResource>(children);
			for(final WebdavResource res : tmpList) {
				try {
					res.copy(dest.dup().append(res.getUrl().name()));
				} catch (WebdavProtocolException x) {
					exceptions.add(x);
				}
			}
			return copy;
		} catch (WebdavProtocolException x) {
			exceptions.add(x);
		}
		if (exceptions.size() > 0) {
			throw WebdavMultistatusException.create(getUrl(),exceptions);
		}
		throw new IllegalStateException("Impossible");
	}

	@Override
	public void setLength(final Long l) throws WebdavProtocolException {
	    throw WebdavProtocolException.Code.NO_BODIES_ALLOWED.create(getUrl(), HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
	}

	@Override
	public String getETag() throws WebdavProtocolException{
		return null;
	}

	@Override
	public void setContentType(final String s) throws WebdavProtocolException {
	    throw WebdavProtocolException.Code.NO_BODIES_ALLOWED.create(getUrl(), HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
	}

	@Override
    public WebdavResource resolveResource(final WebdavPath subPath) throws WebdavProtocolException {
		return mgr.resolveResource(url.dup().append(subPath));
	}

	@Override
    public WebdavCollection resolveCollection(final WebdavPath subPath) throws WebdavProtocolException {
		return mgr.resolveCollection(url.dup().append(subPath));
	}

	@Override
    public List<WebdavResource> getChildren(){
		return new ArrayList<WebdavResource>(children );
	}

	public void addChild(final WebdavResource child) {
		children.add(child);
	}

	public void removeChild(final WebdavResource child) {
		children.remove(child);
	}

	@Override
    public Iterator<WebdavResource> iterator() {
		return new ChildTreeIterator(children.iterator());
	}

	@Override
    public Iterable<WebdavResource> toIterable(final int depth) {
		switch(depth) {
		case 0: return new LinkedList<WebdavResource>();
		case 1: return getChildren();
		case INFINITY : return this;
		default: throw new IllegalArgumentException("Depth can only be one of 0, 1 or INFINITY");
		}
	}

	private static class ChildTreeIterator implements Iterator<WebdavResource> {

		private Iterator<WebdavResource> subIterator;
		private final Iterator<WebdavResource> childIterator;

		public ChildTreeIterator(final Iterator<WebdavResource> childIterator) {
			this.childIterator = childIterator;
		}

		@Override
        public boolean hasNext() {
			if (subIterator != null) {
				if (subIterator.hasNext()) {
					return true;
				}
				subIterator = null;
			}
			return childIterator.hasNext();
		}

		@Override
        public WebdavResource next() {
			if (subIterator != null && subIterator.hasNext()) {
				return subIterator.next();
			}
			final WebdavResource res = childIterator.next();
			if (res.isCollection()) {
				subIterator = res.toCollection().iterator();
			}
			return res;
		}

		@Override
        public void remove() {
			throw new UnsupportedOperationException();
		}

	}

}
