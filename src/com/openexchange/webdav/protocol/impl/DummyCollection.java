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
import com.openexchange.webdav.protocol.Protocol.Property;

public class DummyCollection extends DummyResource implements WebdavCollection {
	
	private List<WebdavResource> children = new ArrayList<WebdavResource>();

	public DummyCollection(DummyResourceManager manager, String url) {
		super(manager,url);
	}
	
	protected boolean isset(Property p) {
		switch(p.getId()) {
		case Protocol.GETCONTENTLANGUAGE : case Protocol.GETCONTENTLENGTH : case Protocol.GETCONTENTTYPE : case Protocol.GETETAG :
			return false;
		default: return true;
		}
	}

	public boolean isCollection(){
		return true;
	}

	@Override 
	public WebdavCollection toCollection() {
		return this;
	}

	@Override
	public String getResourceType() throws WebdavException {
		return Protocol.COLLECTION;
	}

	@Override
	public void putBody(InputStream data, boolean guessSize) throws WebdavException {
		throw new WebdavException("Collections may not have bodies", getUrl(), HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
	}
	
	public String getLanguage() throws WebdavException{
		return null;
	}
	
	@Override
	public void setLanguage(String lang) throws WebdavException{
		throw new WebdavException("Collections have no bodies", getUrl(), HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
	}
	
	@Override
	public Long getLength() throws WebdavException{
		return null;
	}
	
	@Override
	public void delete() throws WebdavException {
		List<WebdavResource> copy = new ArrayList<WebdavResource>(children);
		List<WebdavException> exceptions = new ArrayList<WebdavException>();
		for(WebdavResource res : copy) {
			try {
				res.delete();
			} catch (WebdavException x) {
				exceptions.add(x);
			}
		}
		try {
			super.delete();
		} catch (WebdavException x) {
			exceptions.add(x);
		}
		if(exceptions.size() > 0) {
			throw new WebdavMultistatusException(getUrl(), exceptions);
		}
	}
	
	public DummyCollection instance(String url) {
		return new DummyCollection(mgr,url);
	}
	
	
	@Override
	public WebdavResource copy(String dest, boolean noroot, boolean overwrite) throws WebdavException {
		checkParentExists(dest);
		List<WebdavException> exceptions = new ArrayList<WebdavException>();
		try {
			WebdavResource copy = null;
			if(!noroot)
				copy = super.copy(dest,noroot, overwrite);
			else
				copy = mgr.resolveCollection(dest);
			
			for(WebdavResource res : new ArrayList<WebdavResource>(children)) {
				String oldUri = res.getUrl();
				String name = oldUri.substring(oldUri.lastIndexOf("/")+1);
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

	@Override
	public void setLength(Long l) throws WebdavException {
		throw new WebdavException("Collections have no bodies", getUrl(), HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
	}
	
	public String getETag() throws WebdavException{
		return null;
	}
	
	public void setContentType(String s) throws WebdavException {
		throw new WebdavException("Collections have no bodies", getUrl(), HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
	}

	public WebdavResource resolveResource(String subPath) throws WebdavException {
		return mgr.resolveResource(url+"/"+subPath);
	}

	public WebdavCollection resolveCollection(String subPath) throws WebdavException {
		return mgr.resolveCollection(url+"/"+subPath);
	}
	
	public List<WebdavResource> getChildren(){
		return new ArrayList<WebdavResource>(children );
	}
	
	public void addChild(WebdavResource child) {
		children.add(child);
	}
	
	public void removeChild(WebdavResource child) {
		children.remove(child);
	}

	public Iterator<WebdavResource> iterator() {
		return new ChildTreeIterator(children.iterator());
	}
	
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
		private Iterator<WebdavResource> childIterator;
		
		public ChildTreeIterator(Iterator<WebdavResource> childIterator) {
			this.childIterator = childIterator;
		}
		
		public boolean hasNext() {
			if(subIterator != null) {
				if(subIterator.hasNext())
					return true;
				subIterator = null;
			}
			return childIterator.hasNext();
		}

		public WebdavResource next() {
			if(subIterator != null && subIterator.hasNext())
				return subIterator.next();
			WebdavResource res = childIterator.next();
			if(res.isCollection())
				subIterator = res.toCollection().iterator();
			return res;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
	
}
