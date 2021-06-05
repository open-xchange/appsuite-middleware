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

package com.openexchange.webdav.protocol.helpers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.exception.OXException;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavMultistatusException;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

public abstract class AbstractCollection extends AbstractResource implements
		WebdavCollection {

	@Override
	public boolean isCollection(){
		return true;
	}

	@Override
	public void putBody(final InputStream data, final boolean guessSize) throws WebdavProtocolException {
	    throw WebdavProtocolException.Code.NO_BODIES_ALLOWED.create(getUrl(), HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
	}

	@Override
    public InputStream getBody() throws WebdavProtocolException {
	    throw WebdavProtocolException.Code.NO_BODIES_ALLOWED.create(getUrl(), HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
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
    public String getLanguage() throws WebdavProtocolException{
		return null;
	}

	@Override
    public void setLanguage(final String lang) throws WebdavProtocolException{
	    throw WebdavProtocolException.Code.NO_BODIES_ALLOWED.create(getUrl(), HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
	}

	@Override
    public Long getLength() throws WebdavProtocolException{
		return null;
	}

	@Override
    public String getContentType() throws WebdavProtocolException {
		return "httpd/unix-directory";
	}


	@Override
    public void setSource(final String source) throws WebdavProtocolException {
		//IGNORE
	}

	@Override
    public void delete() throws WebdavProtocolException {
		final List<WebdavResource> copy = new ArrayList<WebdavResource>(getChildren());
		final List<WebdavProtocolException> exceptions = new ArrayList<WebdavProtocolException>();
		for(final WebdavResource res : copy) {
			try {
				res.delete();
			} catch (WebdavProtocolException e) {
                exceptions.add(e);
			}
		}
		try {
			internalDelete();
		} catch (WebdavProtocolException e) {
            exceptions.add(e);
		}
		if (exceptions.size() > 0) {
			throw WebdavMultistatusException.create(getUrl(), exceptions);
		}
	}

	protected abstract void internalDelete() throws WebdavProtocolException;

	@Override
	public AbstractCollection instance(final WebdavPath url) throws WebdavProtocolException {
		return (AbstractCollection) getFactory().resolveCollection(url);
	}

	@Override
	public WebdavResource copy(final WebdavPath dest, final boolean noroot, final boolean overwrite) throws WebdavProtocolException {
		final List<WebdavProtocolException> exceptions = new ArrayList<WebdavProtocolException>();
		try {
			WebdavResource copy = null;
			if (!noroot) {
				copy = super.copy(dest,noroot, overwrite);
			} else {
				copy = getFactory().resolveCollection(dest);
			}

			for(final WebdavResource res : new ArrayList<WebdavResource>(getChildren())) {
				try {
					res.copy(dest.dup().append(res.getUrl().name()));
				} catch (WebdavProtocolException e) {
					exceptions.add(e);
				}
			}
			return copy;
		} catch (WebdavProtocolException e) {
            exceptions.add(e);
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
		return getFactory().resolveResource(getUrl().dup().append(subPath));
	}

	@Override
    public WebdavCollection resolveCollection(final WebdavPath subPath) throws WebdavProtocolException {
		return getFactory().resolveCollection(getUrl()+"/"+subPath);
	}

	@Override
    public Iterator<WebdavResource> iterator() {
		try {
			return new ChildTreeIterator(getChildren().iterator());
		} catch (OXException e) {
			return null;
		}
	}

	@Override
	public boolean hasBody(){
		return false;
	}

	@Override
    public Iterable<WebdavResource> toIterable(final int depth) throws WebdavProtocolException {
		switch(depth) {
		case 0: return new LinkedList<WebdavResource>();
		case 1: return getChildren();
		case INFINITY : return this;
		default : 	throw new IllegalArgumentException("Depth can only be one of 0, 1 or INFINITY");
		}
	}

	protected static class ChildTreeIterator implements Iterator<WebdavResource> {

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
