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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.carddav;

import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import com.openexchange.tools.versit.VersitObject;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;

/**
 * {@link UndecidedResource}
 * 
 * CardDAV resource for not yet 
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class UndecidedResource extends CarddavResource {

    private CarddavResource delegate = null;
    
    private final WebdavPath url;
	
    public UndecidedResource(final AggregatedCollection parent, final GroupwareCarddavFactory factory, final WebdavPath url) {
    	super(parent, factory);
    	this.url = url;
    }
    
    private static boolean isGroup(final VersitObject versitObject) {
        com.openexchange.tools.versit.Property property = versitObject.getProperty("X-ADDRESSBOOKSERVER-KIND");
        return null != property  && "group".equals(property.getValue());
    }
    
	@Override
	public void create() throws WebdavProtocolException {
		if (null == this.delegate) {
			throw super.protocolException(HttpServletResponse.SC_NOT_FOUND);
		} 
		delegate.create();
	}

	@Override
	public boolean exists() {
		// never exists
		return false;
	}

	@Override
    public String getETag() throws WebdavProtocolException {
		return null != delegate ? delegate.getETag() : "";
	}
	
	@Override
	public void delete() throws WebdavProtocolException {
		// no deletes on undecided resources
		throw super.protocolException(HttpServletResponse.SC_CONFLICT);
	}

	@Override
	public void save() throws WebdavProtocolException {
		// no updates on undecided resources
		throw super.protocolException(HttpServletResponse.SC_FORBIDDEN);
	}

	@Override
	public Date getCreationDate() throws WebdavProtocolException {
		return null != delegate ? delegate.getCreationDate() : new Date(0);
	}

	@Override
	public Date getLastModified() throws WebdavProtocolException {
		return null != delegate ? delegate.getLastModified() : new Date(0);
	}

	@Override
	public String getDisplayName() throws WebdavProtocolException {
		return null != this.delegate ? this.delegate.getDisplayName() : null;
	}

	@Override
	public void setDisplayName(final String displayName) throws WebdavProtocolException {
		// no updates on undecided resources
		throw super.protocolException(HttpServletResponse.SC_FORBIDDEN);
	}

	@Override
	protected void applyVersitObject(final VersitObject versitObject) throws WebdavProtocolException {
		if (isGroup(versitObject)) {
			this.delegate = new FolderGroupResource(this.parent, this.factory, versitObject, this.getUrl());
		} else {
			this.delegate = new ContactResource(this.parent, this.factory, versitObject, this.getUrl());
		}
	}

	@Override
	protected String generateVCard() throws WebdavProtocolException {
		return null != this.delegate ? this.delegate.generateVCard() : null;
	}

	@Override
	protected String getUID() {
		String uid = null;		
		if (null != this.delegate) {
			uid = this.delegate.getUID();
		} 
		if (null == uid && null != this.url) {
			uid = Tools.extractUID(this.url);
		}
		return uid;
	}
	
	@Override
    public WebdavPath getUrl() {
		if (null != this.delegate) {
			return this.delegate.getUrl();
		} else if (null != this.url) {
			return this.url;
		} else {
			return super.getUrl(); 
		}
	}
}
