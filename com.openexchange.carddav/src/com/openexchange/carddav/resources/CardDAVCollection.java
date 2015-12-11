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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.openexchange.carddav.CarddavProtocol;
import com.openexchange.carddav.GroupwareCarddavFactory;
import com.openexchange.carddav.Tools;
import com.openexchange.carddav.mixins.MaxImageSize;
import com.openexchange.carddav.mixins.MaxResourceSize;
import com.openexchange.carddav.mixins.SupportedAddressData;
import com.openexchange.carddav.mixins.SupportedReportSet;
import com.openexchange.contact.ContactFieldOperand;
import com.openexchange.dav.reports.SyncStatus;
import com.openexchange.dav.resources.CommonFolderCollection;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ConstantOperand;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.helpers.AbstractResource;

/**
 * {@link CardDAVCollection} - CardDAV collection for contact folders.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CardDAVCollection extends CommonFolderCollection<Contact> {

    protected final GroupwareCarddavFactory factory;

    /**
     * Initializes a new {@link CardDAVCollection}.
     *
     * @param factory The factory
     * @param url The WebDAV path
     * @param folder The underlying folder, or <code>null</code> if it not yet exists
     */
    public CardDAVCollection(GroupwareCarddavFactory factory, WebdavPath url, UserizedFolder folder) throws OXException {
        super(factory, url, folder);
        this.factory = factory;
        includeProperties(new SupportedReportSet(), new MaxResourceSize(factory), new MaxImageSize(factory), new SupportedAddressData());
    }

    /**
     * Gets a list of one or more folders represented by the collection.
     *
     * @return The folder identifiers
     */
    protected List<UserizedFolder> getFolders() throws OXException {
        return Collections.singletonList(folder);
    }

    @Override
    public String getResourceType() throws WebdavProtocolException {
        return super.getResourceType() + CarddavProtocol.ADDRESSBOOK;
    }

    public List<WebdavResource> getFilteredObjects(SearchTerm<?> term) throws WebdavProtocolException {
        List<WebdavResource> resources = new ArrayList<WebdavResource>();
        SearchIterator<Contact> searchIterator = null;
        try {
            searchIterator  = searchContacts(term, getFolders());
            while (searchIterator.hasNext()) {
                Contact contact = searchIterator.next();
                resources.add(createResource(contact, constructPathForChildResource(contact)));
            }
        } catch (OXException e) {
            throw protocolException(e);
        } finally {
            SearchIterators.close(searchIterator);
        }
        return resources;
    }

    protected SearchIterator<Contact> searchContacts(SearchTerm<?> term, List<UserizedFolder> folders) throws OXException {
        if (null != folders && 0 < folders.size()) {
            CompositeSearchTerm compositeTerm = new CompositeSearchTerm(CompositeOperation.AND);
            for (UserizedFolder folder : folders) {
                SingleSearchTerm folderIDTerm = new SingleSearchTerm(SingleOperation.EQUALS);
                folderIDTerm.addOperand(new ContactFieldOperand(ContactField.FOLDER_ID));
                folderIDTerm.addOperand(new ConstantOperand<String>(folder.getID()));
                compositeTerm.addSearchTerm(folderIDTerm);
            }
            compositeTerm.addSearchTerm(term);
            term = compositeTerm;
        }
        return factory.getContactService().searchContacts(factory.getSession(), term);
    }

    @Override
    protected Collection<Contact> getModifiedObjects(Date since) throws OXException {
        Collection<Contact> contacts = new ArrayList<Contact>();
        for (UserizedFolder folder : getFolders()) {
            Collection<Contact> modifiedContacts = factory.getState().getModifiedContacts(since, folder.getID());
            if (null != modifiedContacts) {
                contacts.addAll(modifiedContacts);
            }
        }
        return contacts;
    }

    @Override
    protected Collection<Contact> getDeletedObjects(Date since) throws OXException {
        Collection<Contact> contacts = new ArrayList<Contact>();
        for (UserizedFolder folder : getFolders()) {
            Collection<Contact> contactList = factory.getState().getDeletedContacts(since, folder.getID());
            if (null != contactList) {
                contacts.addAll(contactList);
            }
        }
        return contacts;
    }

    @Override
    protected Collection<Contact> getObjects() throws OXException {
        Collection<Contact> contacts = new ArrayList<Contact>();
        for (UserizedFolder folder : getFolders()) {
            Collection<Contact> contactList = factory.getState().getContacts(folder.getID());
            if (null != contactList) {
                contacts.addAll(contactList);
            }
        }
        return contacts;
    }

    @Override
    protected Contact getObject(String resourceName) throws OXException {
        return factory.getState().load(resourceName);
    }

    @Override
    protected AbstractResource createResource(Contact object, WebdavPath url) throws OXException {
        return new ContactResource(factory, this, object, url);
    }

    @Override
    protected String getFileExtension() {
        return ContactResource.EXTENSION_VCF;
    }

	@Override
	public Date getLastModified() throws WebdavProtocolException {
       try {
            Date lastModified = new Date(0);
            for (UserizedFolder folder : getFolders()) {
                lastModified = Tools.getLatestModified(lastModified, factory.getState().getLastModified(folder));
            }
            return lastModified;
        } catch (OXException e) {
            throw protocolException(e);
        }
	}

    @Override
    public SyncStatus<WebdavResource> getSyncStatus(String token) throws WebdavProtocolException {
        if (null != token && 0 < token.length()) {
            /*
             * check for overridden sync-token for this client
             */
            String overrrideSyncToken = factory.getOverrideNextSyncToken();
            if (null != overrrideSyncToken && 0 < overrrideSyncToken.length()) {
                factory.setOverrideNextSyncToken(null);
                token = overrrideSyncToken;
                LOG.debug("Overriding sync token to '{}' for user '{}'.", token, this.factory.getUser());
            }
        }
        return super.getSyncStatus(token);
    }

    @Override
    public String getCTag() throws WebdavProtocolException {
        /*
         * check for overridden sync-token for this client
         */
        String overrrideSyncToken = factory.getOverrideNextSyncToken();
        if (null != overrrideSyncToken && 0 < overrrideSyncToken.length()) {
            factory.setOverrideNextSyncToken(null);
            String value = "http://www.open-xchange.com/ctags/" + folder.getID() + "-" + overrrideSyncToken;
            LOG.debug("Overriding CTag property to '{}' for user '{}'.", value, factory.getUser());
            return value;
        }
        return super.getCTag();
    }

}
