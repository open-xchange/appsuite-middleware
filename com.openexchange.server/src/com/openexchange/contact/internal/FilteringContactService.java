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

package com.openexchange.contact.internal;

import java.util.Date;
import java.util.List;
import com.openexchange.config.admin.HideAdminService;
import com.openexchange.contact.AutocompleteParameters;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.SortOptions;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.search.SearchTerm;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link FilteringContactService}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public class FilteringContactService implements ContactService {

    private ContactService delegate;
    private ServiceLookup services;

    public FilteringContactService(ContactService lDelegate, ServiceLookup lServices) {
        this.delegate = lDelegate;
        this.services = lServices;
    }

    private SearchIterator<Contact> removeAdmin(int contextId, SearchIterator<Contact> contacts) throws OXException {
        HideAdminService hideAdminService = services.getOptionalService(HideAdminService.class);
        if (hideAdminService == null) {
            return contacts;
        }
        return hideAdminService.removeAdminFromContacts(contextId, contacts);
    }

    @Override
    public Contact getContact(Session session, String folderId, String id) throws OXException {
        return delegate.getContact(session, folderId, id);
    }

    @Override
    public Contact getContact(Session session, String folderId, String id, ContactField[] fields) throws OXException {
        return delegate.getContact(session, folderId, id, fields);
    }

    @Override
    public SearchIterator<Contact> getAllContacts(Session session, String folderId) throws OXException {
        return removeAdmin(session.getContextId(), delegate.getAllContacts(session, folderId));
    }

    @Override
    public SearchIterator<Contact> getAllContacts(Session session, String folderId, SortOptions sortOptions) throws OXException {
        return removeAdmin(session.getContextId(), delegate.getAllContacts(session, folderId, sortOptions));
    }

    @Override
    public SearchIterator<Contact> getAllContacts(Session session, String folderId, ContactField[] fields) throws OXException {
        return removeAdmin(session.getContextId(), delegate.getAllContacts(session, folderId, fields));
    }

    @Override
    public SearchIterator<Contact> getAllContacts(Session session, String folderId, ContactField[] fields, SortOptions sortOptions) throws OXException {
        return removeAdmin(session.getContextId(), delegate.getAllContacts(session, folderId, fields, sortOptions));
    }

    @Override
    public SearchIterator<Contact> getAllContacts(Session session, List<String> folderIDs, ContactField[] fields, SortOptions sortOptions) throws OXException {
        return removeAdmin(session.getContextId(), delegate.getAllContacts(session, folderIDs, fields, sortOptions));
    }

    @Override
    public SearchIterator<Contact> getAllContacts(Session session, ContactField[] fields, SortOptions sortOptions) throws OXException {
        return removeAdmin(session.getContextId(), delegate.getAllContacts(session, fields, sortOptions));
    }

    @Override
    public int countContacts(Session session, String folderId) throws OXException {
        int countContacts = delegate.countContacts(session, folderId);
        HideAdminService hideAdminService = services.getOptionalService(HideAdminService.class);
        if (hideAdminService == null) {
            return countContacts;
        }
        return hideAdminService.showAdmin(session.getContextId()) || FolderObject.SYSTEM_LDAP_FOLDER_ID == com.openexchange.contact.internal.Tools.parse(folderId) ? countContacts : countContacts - 1;
    }

    @Override
    public SearchIterator<Contact> getContacts(Session session, String folderId, String[] ids) throws OXException {
        return delegate.getContacts(session, folderId, ids);
    }

    @Override
    public SearchIterator<Contact> getContacts(Session session, String folderId, String[] ids, SortOptions sortOptions) throws OXException {
        return delegate.getContacts(session, folderId, ids, sortOptions);
    }

    @Override
    public SearchIterator<Contact> getContacts(Session session, String folderId, String[] ids, ContactField[] fields) throws OXException {
        return delegate.getContacts(session, folderId, ids, fields);
    }

    @Override
    public SearchIterator<Contact> getContacts(Session session, String folderId, String[] ids, ContactField[] fields, SortOptions sortOptions) throws OXException {
        return delegate.getContacts(session, folderId, ids, fields, sortOptions);
    }

    @Override
    public SearchIterator<Contact> getDeletedContacts(Session session, String folderId, Date since) throws OXException {
        return delegate.getDeletedContacts(session, folderId, since);
    }

    @Override
    public SearchIterator<Contact> getDeletedContacts(Session session, String folderId, Date since, ContactField[] fields) throws OXException {
        return delegate.getDeletedContacts(session, folderId, since, fields);
    }

    @Override
    public SearchIterator<Contact> getDeletedContacts(Session session, String folderId, Date since, ContactField[] fields, SortOptions sortOptions) throws OXException {
        return delegate.getDeletedContacts(session, folderId, since, fields, sortOptions);
    }

    @Override
    public SearchIterator<Contact> getModifiedContacts(Session session, String folderId, Date since) throws OXException {
        try (SearchIterator<Contact> contacts = delegate.getModifiedContacts(session, folderId, since)) {
            return removeAdmin(session.getContextId(), contacts);
        }
    }

    @Override
    public SearchIterator<Contact> getModifiedContacts(Session session, String folderId, Date since, ContactField[] fields) throws OXException {
        try (SearchIterator<Contact> contacts = delegate.getModifiedContacts(session, folderId, since, fields)) {
            return removeAdmin(session.getContextId(), contacts);
        }
    }

    @Override
    public SearchIterator<Contact> getModifiedContacts(Session session, String folderId, Date since, ContactField[] fields, SortOptions sortOptions) throws OXException {
        try (SearchIterator<Contact> contacts = delegate.getModifiedContacts(session, folderId, since, fields, sortOptions)) {
            return removeAdmin(session.getContextId(), contacts);
        }
    }

    @Override
    public <O> SearchIterator<Contact> searchContacts(Session session, SearchTerm<O> term) throws OXException {
        try (SearchIterator<Contact> contacts = delegate.searchContacts(session, term)) {
            return removeAdmin(session.getContextId(), contacts);
        }
    }

    @Override
    public <O> SearchIterator<Contact> searchContacts(Session session, SearchTerm<O> term, SortOptions sortOptions) throws OXException {
        try (SearchIterator<Contact> contacts = delegate.searchContacts(session, term, sortOptions)) {
            return removeAdmin(session.getContextId(), contacts);
        }
    }

    @Override
    public <O> SearchIterator<Contact> searchContacts(Session session, SearchTerm<O> term, ContactField[] fields) throws OXException {
        try (SearchIterator<Contact> contacts = delegate.searchContacts(session, term, fields)) {
            return removeAdmin(session.getContextId(), contacts);
        }
    }

    @Override
    public <O> SearchIterator<Contact> searchContacts(Session session, SearchTerm<O> term, ContactField[] fields, SortOptions sortOptions) throws OXException {
        try (SearchIterator<Contact> contacts = delegate.searchContacts(session, term, fields, sortOptions)) {
            return removeAdmin(session.getContextId(), contacts);
        }
    }

    @Override
    public SearchIterator<Contact> searchContacts(Session session, ContactSearchObject contactSearch) throws OXException {
        try (SearchIterator<Contact> contacts = delegate.searchContacts(session, contactSearch)) {
            return removeAdmin(session.getContextId(), contacts);
        }
    }

    @Override
    public SearchIterator<Contact> searchContacts(Session session, ContactSearchObject contactSearch, SortOptions sortOptions) throws OXException {
        try (SearchIterator<Contact> contacts = delegate.searchContacts(session, contactSearch, sortOptions)) {
            return removeAdmin(session.getContextId(), contacts);
        }
    }

    @Override
    public SearchIterator<Contact> searchContacts(Session session, ContactSearchObject contactSearch, ContactField[] fields) throws OXException {
        try (SearchIterator<Contact> contacts = delegate.searchContacts(session, contactSearch, fields)) {
            return removeAdmin(session.getContextId(), contacts);
        }
    }

    @Override
    public SearchIterator<Contact> searchContacts(Session session, ContactSearchObject contactSearch, ContactField[] fields, SortOptions sortOptions) throws OXException {
        try (SearchIterator<Contact> contacts = delegate.searchContacts(session, contactSearch, fields, sortOptions)) {
            return removeAdmin(session.getContextId(), contacts);
        }
    }

    @Override
    public SearchIterator<Contact> searchContactsWithBirthday(Session session, Date from, Date until, ContactField[] fields, SortOptions sortOptions) throws OXException {
        try (SearchIterator<Contact> contacts = delegate.searchContactsWithBirthday(session, from, until, fields, sortOptions)) {
            return removeAdmin(session.getContextId(), contacts);
        }
    }

    @Override
    public SearchIterator<Contact> searchContactsWithBirthday(Session session, List<String> folderIDs, Date from, Date until, ContactField[] fields, SortOptions sortOptions) throws OXException {
        try (SearchIterator<Contact> contacts = delegate.searchContactsWithBirthday(session, folderIDs, from, until, fields, sortOptions)) {
            return removeAdmin(session.getContextId(), contacts);
        }
    }

    @Override
    public SearchIterator<Contact> searchContactsWithAnniversary(Session session, Date from, Date until, ContactField[] fields, SortOptions sortOptions) throws OXException {
        try (SearchIterator<Contact> contacts = delegate.searchContactsWithAnniversary(session, from, until, fields, sortOptions)) {
            return removeAdmin(session.getContextId(), contacts);
        }
    }

    @Override
    public SearchIterator<Contact> searchContactsWithAnniversary(Session session, List<String> folderIDs, Date from, Date until, ContactField[] fields, SortOptions sortOptions) throws OXException {
        try (SearchIterator<Contact> contacts = delegate.searchContactsWithAnniversary(session, folderIDs, from, until, fields, sortOptions)) {
            return removeAdmin(session.getContextId(), contacts);
        }
    }

    @Override
    public SearchIterator<Contact> autocompleteContacts(Session session, List<String> folderIDs, String query, AutocompleteParameters parameters, ContactField[] fields, SortOptions sortOptions) throws OXException {
        try (SearchIterator<Contact> contacts = delegate.autocompleteContacts(session, folderIDs, query, parameters, fields, sortOptions)) {
            return removeAdmin(session.getContextId(), contacts);
        }
    }

    @Override
    public SearchIterator<Contact> autocompleteContacts(Session session, String query, AutocompleteParameters parameters, ContactField[] fields, SortOptions sortOptions) throws OXException {
        try (SearchIterator<Contact> contacts = delegate.autocompleteContacts(session, query, parameters, fields, sortOptions)) {
            return removeAdmin(session.getContextId(), contacts);
        }
    }

    @Override
    public void createContact(Session session, String folderId, Contact contact) throws OXException {
        delegate.createContact(session, folderId, contact);
    }

    @Override
    public void updateContact(Session session, String folderId, String id, Contact contact, Date lastRead) throws OXException {
        delegate.updateContact(session, folderId, id, contact, lastRead);
    }

    @Override
    public void updateUser(Session session, String folderId, String id, Contact contact, Date lastRead) throws OXException {
        delegate.updateUser(session, folderId, id, contact, lastRead);
    }

    @Override
    public void deleteContact(Session session, String folderId, String id, Date lastRead) throws OXException {
        delegate.deleteContact(session, folderId, id, lastRead);
    }

    @Override
    public void deleteContacts(Session session, String folderId) throws OXException {
        delegate.deleteContacts(session, folderId);
    }

    @Override
    public void deleteContacts(Session session, String folderId, String[] ids, Date lastRead) throws OXException {
        delegate.deleteContacts(session, folderId, ids, lastRead);
    }

    @Override
    public Contact getUser(Session session, int userID) throws OXException {
        return delegate.getUser(session, userID);
    }

    @Override
    public Contact getUser(Session session, int userID, ContactField[] fields) throws OXException {
        return delegate.getUser(session, userID, fields);
    }

    @Override
    public SearchIterator<Contact> getUsers(Session session, int[] userIDs) throws OXException {
        return delegate.getUsers(session, userIDs);
    }

    @Override
    public SearchIterator<Contact> getUsers(Session session, int[] userIDs, ContactField[] fields) throws OXException {
        return delegate.getUsers(session, userIDs, fields);
    }

    @Override
    public SearchIterator<Contact> getAllUsers(Session session, ContactField[] fields, SortOptions sortOptions) throws OXException {
        try (SearchIterator<Contact> users = delegate.getAllUsers(session, fields, sortOptions)) {
            return removeAdmin(session.getContextId(), users);
        }
    }

    @Override
    public String getOrganization(Session session) throws OXException {
        return delegate.getOrganization(session);
    }

    @Override
    public <O> SearchIterator<Contact> searchUsers(Session session, SearchTerm<O> term, ContactField[] fields, SortOptions sortOptions) throws OXException {
        try (SearchIterator<Contact> users = delegate.searchUsers(session, term, fields, sortOptions)) {
            return removeAdmin(session.getContextId(), users);
        }
    }

    @Override
    public SearchIterator<Contact> searchUsers(Session session, ContactSearchObject contactSearch, ContactField[] fields, SortOptions sortOptions) throws OXException {
        try (SearchIterator<Contact> users = delegate.searchUsers(session, contactSearch, fields, sortOptions)) {
            return removeAdmin(session.getContextId(), users);
        }
    }

    @Override
    public boolean isFolderEmpty(Session session, String folderID) throws OXException {
        return delegate.isFolderEmpty(session, folderID);
    }

    @Override
    public boolean containsForeignObjectInFolder(Session session, String folderID) throws OXException {
        return delegate.containsForeignObjectInFolder(session, folderID);
    }

    @Override
    public boolean supports(Session session, String folderID, ContactField... fields) throws OXException {
        return delegate.supports(session, folderID, fields);
    }
}
