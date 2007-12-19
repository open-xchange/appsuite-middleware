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

import com.openexchange.webdav.loader.LoadingHints;
import com.openexchange.webdav.protocol.*;

import javax.servlet.http.HttpServletResponse;

public abstract class WebdavStructureAction extends AbstractAction {

	private WebdavFactory factory;
	
	public WebdavStructureAction(WebdavFactory factory) {
		this.factory = factory;
	}

	// Returns the status for a successful move/copy
	protected void checkOverwrite(WebdavRequest req) throws WebdavException{
		if(req.getHeader("Overwrite") != null && "F".equals(req.getHeader("Overwrite"))){
			LoadingHints loadingHints = new LoadingHints();
			loadingHints.setUrl(req.getDestinationUrl());
			loadingHints.setDepth(WebdavCollection.INFINITY);
			loadingHints.setProps(LoadingHints.Property.NONE);
			preLoad(loadingHints);
			
			WebdavResource dest = req.getDestination();
			
			if(!dest.exists())
				return;
			
			if(dest.isCollection()) {
				int depth = req.getDepth(WebdavCollection.INFINITY);
				
				int sourceUrlLength = req.getUrl().size();
				WebdavPath destUrl = req.getDestinationUrl();
				
				for(WebdavResource res : req.getCollection().toIterable(depth)) {
					WebdavPath url = res.getUrl();
					url = destUrl.dup().append(url.subpath(sourceUrlLength));
					WebdavResource d = factory.resolveResource(url);
					if(d.exists() && !d.isCollection())
						throw new WebdavException(req.getUrl(), HttpServletResponse.SC_PRECONDITION_FAILED);
				}
				
			} else {
				throw new WebdavException(req.getUrl(), HttpServletResponse.SC_PRECONDITION_FAILED);
			}
			
		}
		return;
	}
	
	protected int chooseReturnCode(WebdavRequest req) throws WebdavException {
		return (req.getDestination().exists()) ? HttpServletResponse.SC_NO_CONTENT : HttpServletResponse.SC_CREATED;
	}
	
	protected void checkSame(WebdavRequest req) throws WebdavException {
		if(req.getUrl().equals(req.getDestinationUrl()))
			throw new WebdavException(req.getUrl(), HttpServletResponse.SC_FORBIDDEN);
	}

}
