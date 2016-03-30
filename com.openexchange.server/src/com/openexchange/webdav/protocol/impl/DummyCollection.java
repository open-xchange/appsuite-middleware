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
			} catch (final WebdavProtocolException x) {
				exceptions.add(x);
			}
		}
		try {
			super.delete();
		} catch (final WebdavProtocolException x) {
			exceptions.add(x);
		}
		if(exceptions.size() > 0) {
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
        } catch (final OXException e) {
            throw new WebdavProtocolException(getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }
		final List<WebdavProtocolException> exceptions = new ArrayList<WebdavProtocolException>();
		try {
			WebdavResource copy = null;
			if(!noroot) {
				try {
                    copy = super.copy(dest,noroot, overwrite);
                } catch (final OXException e) {
                    throw new WebdavProtocolException(getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
                }
			} else {
				copy = mgr.resolveCollection(dest);
			}

			final List<WebdavResource> tmpList = new ArrayList<WebdavResource>(children);
			for(final WebdavResource res : tmpList) {
				try {
					res.copy(dest.dup().append(res.getUrl().name()));
				} catch (final WebdavProtocolException x) {
					exceptions.add(x);
				}
			}
			return copy;
		} catch (final WebdavProtocolException x) {
			exceptions.add(x);
		}
		if(exceptions.size() > 0) {
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
			if(subIterator != null) {
				if(subIterator.hasNext()) {
					return true;
				}
				subIterator = null;
			}
			return childIterator.hasNext();
		}

		@Override
        public WebdavResource next() {
			if(subIterator != null && subIterator.hasNext()) {
				return subIterator.next();
			}
			final WebdavResource res = childIterator.next();
			if(res.isCollection()) {
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
