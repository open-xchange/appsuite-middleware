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
import java.util.Locale;

import org.apache.commons.logging.Log;

import com.openexchange.carddav.GroupwareCarddavFactory;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.groupware.container.Contact;
import com.openexchange.log.LogFactory;
import com.openexchange.webdav.acl.mixins.CurrentUserPrivilegeSet;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;

/**
 * {@link FolderCollection} - CardDAV collection for contact folders.
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FolderCollection extends CardDAVCollection {

    private static final Log LOG = LogFactory.getLog(FolderCollection.class);

    private final UserizedFolder folder;

    public FolderCollection(GroupwareCarddavFactory factory, WebdavPath url, UserizedFolder folder) throws WebdavProtocolException {
        super(factory, url);
        this.folder = folder;
        super.includeProperties(new CurrentUserPrivilegeSet(folder.getOwnPermission()));        
        LOG.debug(getUrl() + ": initialized for folder '" + folder.getName() + "' [" + folder.getID() + "].");
    }
    
	@Override
    protected Collection<Contact> getModifiedContacts(Date since) throws OXException {
    	return factory.getState().getModifiedContacts(since, this.folder.getID());
    }

	@Override
    protected Collection<Contact> getDeletedContacts(Date since) throws OXException {
    	return factory.getState().getDeletedContacts(since, this.folder.getID());
    }

	@Override
    protected Collection<Contact> getContacts() throws OXException {
    	return factory.getState().getContacts(this.folder.getID());
    }

	@Override
	protected String getFolderID() throws OXException {
		return this.folder.getID();		
	}

	@Override
	public boolean exists() throws WebdavProtocolException {
		return true; // always exists
	}

	@Override
	public Date getCreationDate() throws WebdavProtocolException {
		return folder.getCreationDateUTC();
	}

	@Override
	public Date getLastModified() throws WebdavProtocolException {
		try {
			return factory.getState().getLastModified(folder);
		} catch (OXException e) {
			throw protocolException(e);
		}
	}

	@Override
	public String getDisplayName() throws WebdavProtocolException {
    	Locale locale = this.factory.getUser().getLocale();
    	String name = null != locale ? this.folder.getLocalizedName(locale) : this.folder.getName();
    	if (SharedType.getInstance().equals(this.folder.getType())) {
    		String ownerName = null;
            for (Permission permission : this.folder.getPermissions()) {
                if (permission.isAdmin()) {
                    int entity = permission.getEntity();
                    try {
                        ownerName = factory.resolveUser(entity).getDisplayName();
                    } catch (OXException e) {
                        LOG.error(e.getMessage(), e);
                        ownerName = new Integer(entity).toString();
                    }
                    break;
                }
            }	    		
    		return String.format("%s (%s)", name, ownerName);
    	} else {
    		return name;
    	}
	}

	@Override
	public void setDisplayName(String displayName) throws WebdavProtocolException {
		// no
	}

	@Override
	public void setCreationDate(Date date) throws WebdavProtocolException {
		// no
	}
    
	@Override
	public void create() throws WebdavProtocolException {
		// no
	}

	@Override
	public void save() throws WebdavProtocolException {
		// no
	}

}
