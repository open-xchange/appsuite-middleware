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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavException;
import com.openexchange.webdav.protocol.WebdavMultistatusException;
import com.openexchange.webdav.protocol.WebdavResource;

public abstract class AbstractCollection extends AbstractResource implements
		WebdavCollection {
	
	@Override
	public boolean isCollection(){
		return true;
	}
	
	@Override
	public void putBody(final InputStream data, final boolean guessSize) throws WebdavException {
		throw new WebdavException("Collections may not have bodies", getUrl(), HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
	}
	
	public InputStream getBody() throws WebdavException {
		throw new WebdavException("Collections may not have bodies", getUrl(), HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
	}

	@Override 
	public WebdavCollection toCollection() {
		return this;
	}

	@Override
	public String getResourceType() throws WebdavException {
		return Protocol.COLLECTION;
	}
	
	public String getLanguage() throws WebdavException{
		return null;
	}
	
	public void setLanguage(final String lang) throws WebdavException{
		throw new WebdavException("Collections have no bodies", getUrl(), HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
	}
	
	public Long getLength() throws WebdavException{
		return null;
	}
	
	public String getContentType() throws WebdavException {
		return "httpd/unix-directory";
	}
	
	
	public void setSource(final String source) throws WebdavException {
		//IGNORE
	}
	
	public void delete() throws WebdavException {
		final List<WebdavResource> copy = new ArrayList<WebdavResource>(getChildren());
		final List<WebdavException> exceptions = new ArrayList<WebdavException>();
		for(WebdavResource res : copy) {
			try {
				res.delete();
			} catch (WebdavException x) {
				exceptions.add(x);
			}
		}
		try {
			internalDelete();
		} catch (WebdavException x) {
			exceptions.add(x);
		}
		if(exceptions.size() > 0) {
			throw new WebdavMultistatusException(getUrl(), exceptions);
		}
	}

	protected abstract void internalDelete() throws WebdavException;
	
	@Override
	public AbstractCollection instance(final String url) throws WebdavException {
		return (AbstractCollection) getFactory().resolveCollection(url);
	}
	
	@Override
	public WebdavResource copy(final String dest, final boolean noroot, final boolean overwrite) throws WebdavException {
		final List<WebdavException> exceptions = new ArrayList<WebdavException>();
		try {
			WebdavResource copy = null;
			if(!noroot) {
				copy = super.copy(dest,noroot, overwrite);
			} else {
				copy = getFactory().resolveCollection(dest);
			}
			
			for(WebdavResource res : new ArrayList<WebdavResource>(getChildren())) {
				final String oldUri = res.getUrl();
				final String name = oldUri.substring(oldUri.lastIndexOf('/')+1);
				try {
					res.copy(dest+"/"+name);
				} catch (WebdavException x) {
					exceptions.add(x);
				}
			}
			return copy;
		} catch (WebdavException x) {
			exceptions.add(x);
		}
		if(exceptions.size() > 0) {
			throw new WebdavMultistatusException(getUrl(),exceptions);
		}
		throw new IllegalStateException("Impossible");
	}
	
	public void setLength(final Long l) throws WebdavException {
		throw new WebdavException("Collections have no bodies", getUrl(), HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
	}
	
	public String getETag() throws WebdavException{
		return null;
	}
	
	public void setContentType(final String s) throws WebdavException {
		throw new WebdavException("Collections have no bodies", getUrl(), HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
	}

	public WebdavResource resolveResource(final String subPath) throws WebdavException {
		return getFactory().resolveResource(getUrl()+"/"+subPath);
	}

	public WebdavCollection resolveCollection(final String subPath) throws WebdavException {
		return getFactory().resolveCollection(getUrl()+"/"+subPath);
	}

	public Iterator<WebdavResource> iterator() {
		try {
			return new ChildTreeIterator(getChildren().iterator());
		} catch (WebdavException e) {
			return null;
		}
	}
	
	@Override
	public boolean hasBody(){
		return false;
	}
	
	public Iterable<WebdavResource> toIterable(final int depth) throws WebdavException {
		switch(depth) {
		case 0: return new LinkedList<WebdavResource>();
		case 1: return getChildren();
		case INFINITY : return this;
		default : 	throw new IllegalArgumentException("Depth can only be one of 0, 1 or INFINITY");
		}
	}
	
	protected static class ChildTreeIterator implements Iterator<WebdavResource> {
		
		private Iterator<WebdavResource> subIterator;
		private Iterator<WebdavResource> childIterator;
		
		public ChildTreeIterator(Iterator<WebdavResource> childIterator) {
			this.childIterator = childIterator;
		}
		
		public boolean hasNext() {
			if(subIterator != null) {
				if(subIterator.hasNext()) {
					return true;
				}
				subIterator = null;
			}
			return childIterator.hasNext();
		}

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

		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
}
