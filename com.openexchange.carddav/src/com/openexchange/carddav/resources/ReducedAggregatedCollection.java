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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import org.apache.commons.logging.Log;
import com.openexchange.carddav.GroupwareCarddavFactory;
import com.openexchange.carddav.Tools;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.container.Contact;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;

/**
 * {@link ReducedAggregatedCollection} - CardDAV collection aggregating the contents 
 * of a reduced set of folders. 
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ReducedAggregatedCollection extends AggregatedCollection {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(ReducedAggregatedCollection.class);

    public ReducedAggregatedCollection(GroupwareCarddavFactory factory, WebdavPath url, String displayName) throws WebdavProtocolException {
        super(factory, url, displayName);
        LOG.debug(getUrl() + ": initialized.");
    }
    
	@Override
    protected Collection<Contact> getModifiedContacts(Date since) throws OXException {
        Collection<Contact> contacts = new ArrayList<Contact>();
        for (UserizedFolder folder : factory.getState().getReducedFolders()) {
            Collection<Contact> modifiedContacts = factory.getState().getModifiedContacts(since, folder.getID());
            if (null != modifiedContacts) {
                contacts.addAll(modifiedContacts);
            }
        }
        return contacts;
    }

	@Override
    protected Collection<Contact> getDeletedContacts(Date since) throws OXException {
        Collection<Contact> contacts = new ArrayList<Contact>();
        for (UserizedFolder folder : factory.getState().getReducedFolders()) {
            Collection<Contact> deletedContacts = factory.getState().getDeletedContacts(since, folder.getID());
            if (null != deletedContacts) {
                contacts.addAll(deletedContacts);
            }
        }
        return contacts;
    }

	@Override
    protected Collection<Contact> getContacts() throws OXException {
        Collection<Contact> contacts = new ArrayList<Contact>();
        for (UserizedFolder folder : factory.getState().getReducedFolders()) {
            Collection<Contact> contactList = factory.getState().getContacts(folder.getID());
            if (null != contactList) {
                contacts.addAll(contactList);
            }
        }
        return contacts;
    }
	
	@Override
	public Date getLastModified() throws WebdavProtocolException {
	    try {
	        Date lastModified = new Date(0);
	        for (UserizedFolder folder : factory.getState().getReducedFolders()) {
	            lastModified = Tools.getLatestModified(lastModified, factory.getState().getLastModified(folder));
	        }
	        return lastModified;
	    } catch (OXException e) {
	        throw protocolException(e);
        }
    }

}
