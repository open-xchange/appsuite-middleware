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

package com.openexchange.webdav.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import com.openexchange.webdav.action.ifheader.IfHeader;
import com.openexchange.webdav.action.ifheader.IfHeaderEntity;
import com.openexchange.webdav.action.ifheader.IfHeaderList;
import com.openexchange.webdav.action.ifheader.IfHeaderParseException;
import com.openexchange.webdav.loader.LoadingHints;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavException;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavResource;

public class WebdavIfAction extends AbstractAction {

	private int defaultDepth;
	private boolean checkSourceLocks;
	private boolean checkDestinationLocks;

	public WebdavIfAction(){}
	
	public WebdavIfAction(int defaultDepth, boolean checkSourceLocks, boolean checkDestinationLocks) {
		this.defaultDepth = defaultDepth;
		this.checkSourceLocks = checkSourceLocks;
		this.checkDestinationLocks = checkDestinationLocks;
	}
	
	public WebdavIfAction(boolean checkSourceLocks, boolean checkDestinationLocks) {
		this(0,checkSourceLocks,checkDestinationLocks);
	}
	
	public void perform(WebdavRequest req, WebdavResponse res)
			throws WebdavException {
		int depth = getDepth(req);
		
		IfHeader ifHeader;
		try {
			ifHeader = req.getIfHeader();
			if(ifHeader != null) {
				checkIfs(ifHeader, req, depth);
			}
			List<LoadingHints> lockHints = new ArrayList<LoadingHints>();
			if(checkSourceLocks)
				lockHints.add(preloadSourceLocks(req, depth));
			if(checkDestinationLocks)
				lockHints.add(preloadDestinationLocks(req));
			
			preLoad(lockHints);
			
			if(checkSourceLocks)
				checkNeededLocks(ifHeader,req,depth);
			if(checkDestinationLocks)
				checkDestinationLocks(ifHeader, req);
		} catch (IfHeaderParseException e) {
			//Ignore
		}
		
		yield(req,res);
		
	}
	
	private LoadingHints preloadDestinationLocks(WebdavRequest req) {
		LoadingHints loadingHints = new LoadingHints();
		loadingHints.setUrl(req.getDestinationUrl());
		loadingHints.setDepth(0);
		loadingHints.setProps(LoadingHints.Property.NONE);
		loadingHints.loadLocks(true);
		return loadingHints;
	}
	
	private LoadingHints preloadSourceLocks(WebdavRequest req, int depth) {
		LoadingHints loadingHints = new LoadingHints();
		loadingHints.setUrl(req.getUrl());
		loadingHints.setDepth(depth);
		loadingHints.setProps(LoadingHints.Property.NONE);
		loadingHints.loadLocks(true);
		return loadingHints;
	}

	private void checkDestinationLocks(IfHeader ifHeader, WebdavRequest req) throws WebdavException {
		if(null == req.getDestinationUrl())
			return;
		
		
		WebdavResource destination = req.getDestination();
		if(null == destination)
			return;
		
		Set<String> locks = new HashSet<String>();
		
		for(WebdavLock lock : destination.getLocks()) {
			locks.add(lock.getToken());
		}
		removeProvidedLocks(ifHeader,locks);
		
		if(!locks.isEmpty()) {
			throw new WebdavException(req.getUrl(), Protocol.SC_LOCKED);
		}
	}

	private void checkNeededLocks(IfHeader ifHeader, WebdavRequest req, int depth) throws WebdavException {
		WebdavResource res = req.getResource();
		Iterable<WebdavResource> iter = null;
		if(res.isCollection()) {
			iter = res.toCollection().toIterable(depth);
		} else {
			iter = Collections.EMPTY_LIST;
		}
		
		Set<String> neededLocks = new HashSet<String>();
		
		for(WebdavResource resource : iter) {
			addLocks(neededLocks, resource);
		}
		
		addLocks(neededLocks, res);
		
		removeProvidedLocks(ifHeader,neededLocks);
		
		if(!neededLocks.isEmpty()) {
			throw new WebdavException(req.getUrl(), Protocol.SC_LOCKED);
		}
	}

	private void removeProvidedLocks(IfHeader ifHeader, Set<String> neededLocks) {
		if(null == ifHeader)
			return;
		for(IfHeaderList list : ifHeader.getLists()) {
			for(IfHeaderEntity entity : list) {
				if(entity.isLockToken()) {
					neededLocks.remove(entity.getPayload());
				}
			}
		}
	}

	private void addLocks(Set<String> neededLocks, WebdavResource res) throws WebdavException {
		for(WebdavLock lock : res.getLocks()) {
			neededLocks.add(lock.getToken());
		}
	}

	private void checkIfs(IfHeader ifHeader, WebdavRequest req, int depth) throws WebdavException {
		LoadingHints loadingHints = new LoadingHints();
		loadingHints.setUrl(req.getUrl());
		loadingHints.setDepth(depth);
		loadingHints.setProps(LoadingHints.Property.SOME);
		loadingHints.addProperty("DAV:", "getetag");
		loadingHints.loadLocks(true);
		preLoad(loadingHints);
		WebdavResource res = req.getResource();
		Iterable<WebdavResource> iter = null;
		if(res.isCollection()) {
			iter = res.toCollection().toIterable(depth);
		} else {
			iter = Collections.EMPTY_LIST;
		}
		
		for(WebdavResource resource : iter) {
			if( checkList(ifHeader, resource, req) ) {
				continue ;
			} else {
				throw new WebdavException(req.getUrl(), HttpServletResponse.SC_PRECONDITION_FAILED);
			}
		}
		
		if( checkList(ifHeader, res, req) ) {
			return ;
		}
		
		throw new WebdavException(req.getUrl(), HttpServletResponse.SC_PRECONDITION_FAILED);
	}

	private boolean checkList(IfHeader ifHeader, WebdavResource resource, WebdavRequest req) throws WebdavException {
		List<IfHeaderList> relevant = ifHeader.getRelevant(req.getURLPrefix()+resource.getUrl());
		for(IfHeaderList list : relevant) {
			if(matches(list, resource))
				return true;
		}
		return relevant.isEmpty();
	}

	private boolean matches(IfHeaderList list, WebdavResource resource) throws WebdavException {
		for(IfHeaderEntity entity : list) {
			boolean matches = false;
			if(entity.isETag()) {
				matches = entity.getPayload().equals(resource.getETag());
			} else {
				matches = null != resource.getLock(entity.getPayload());
			}
			if(matches != entity.mustMatch())
				return false;
		}
		return true;
	}

	private int getDepth(WebdavRequest req) throws WebdavException {
		if(!req.getResource().isCollection())
			return 0;
		String depth = req.getHeader("Depth");
		return depth == null ? defaultDepth : depth.equalsIgnoreCase("Infinity") ? WebdavCollection.INFINITY : Integer.valueOf(depth);
	}

	public void setDefaultDepth(int i) {
		this.defaultDepth = i;
	}
	
	public void checkSourceLocks(boolean b) {
		this.checkSourceLocks = b;
	}
	
	public void checkDestinationLocks(boolean b) {
		this.checkDestinationLocks = b;
	}

}
