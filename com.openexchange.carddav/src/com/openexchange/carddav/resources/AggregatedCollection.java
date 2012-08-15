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

package com.openexchange.carddav.resources;

import java.util.Collection;
import java.util.Date;

import org.apache.commons.logging.Log;

import com.openexchange.carddav.GroupwareCarddavFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.log.LogFactory;
import com.openexchange.webdav.acl.mixins.CurrentUserPrivilegeSet;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;

/**
 * {@link AggregatedCollection} - CardDAV collection aggregating the contents 
 * of all visible folders. 
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class AggregatedCollection extends CardDAVCollection {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(AggregatedCollection.class);
    
    private String displayName = null;

    public AggregatedCollection(GroupwareCarddavFactory factory, WebdavPath url, String displayName) throws WebdavProtocolException {
        super(factory, url);
        this.displayName = displayName;
        try {
			super.includeProperties(new CurrentUserPrivilegeSet(factory.getState().getDefaultFolder().getOwnPermission()));
		} catch (OXException e) {
			throw protocolException(e);
		}
        LOG.debug(getUrl() + ": initialized.");
    }

	@Override
    protected Collection<Contact> getModifiedContacts(Date since) throws OXException {
    	return factory.getState().getModifiedContacts(since);
    }

	@Override
    protected Collection<Contact> getDeletedContacts(Date since) throws OXException {
    	return factory.getState().getDeletedContacts(since);
    }

	@Override
    protected Collection<Contact> getContacts() throws OXException {
    	return factory.getState().getContacts();
    }
	
	@Override
	protected String getFolderID() throws OXException {
		return factory.getState().getDefaultFolder().getID();		
	}

	@Override
	public void create() throws WebdavProtocolException {
	}

	@Override
	public boolean exists() throws WebdavProtocolException {
		return true;
	}

	@Override
	public void save() throws WebdavProtocolException {
	}

	@Override
	public Date getCreationDate() throws WebdavProtocolException {
		return new Date(0);
	}

	@Override
	public Date getLastModified() throws WebdavProtocolException {
		try {
			return factory.getState().getLastModified();
		} catch (OXException e) {
			throw protocolException(e);
		}
	}

	@Override
	public String getDisplayName() throws WebdavProtocolException {
		return displayName;
	}

	@Override
	public void setDisplayName(String displayName) throws WebdavProtocolException {
	}

	@Override
	public void setCreationDate(Date date) throws WebdavProtocolException {
	}

}
