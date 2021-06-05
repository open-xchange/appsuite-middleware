/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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

    private final ContactService delegate;
    private final ServiceLookup services;

    public FilteringContactService(ContactService lDelegate, ServiceLookup lServices) {
        this.delegate = lDelegate;
        this.services = lServices;
    }

    private SearchIterator<Contact> removeAdmin(int contextId, SearchIterator<Contact> contacts) {
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
        return FolderObject.SYSTEM_LDAP_FOLDER_ID == com.openexchange.contact.internal.Tools.parse(folderId) && hideAdminService.showAdmin(session.getContextId()) == false ? countContacts - 1 : countContacts;
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
        return removeAdmin(session.getContextId(), delegate.getModifiedContacts(session, folderId, since));
    }

    @Override
    public SearchIterator<Contact> getModifiedContacts(Session session, String folderId, Date since, ContactField[] fields) throws OXException {
        return removeAdmin(session.getContextId(), delegate.getModifiedContacts(session, folderId, since, fields));
    }

    @Override
    public SearchIterator<Contact> getModifiedContacts(Session session, String folderId, Date since, ContactField[] fields, SortOptions sortOptions) throws OXException {
        return removeAdmin(session.getContextId(), delegate.getModifiedContacts(session, folderId, since, fields, sortOptions));
    }

    @Override
    public <O> SearchIterator<Contact> searchContacts(Session session, SearchTerm<O> term) throws OXException {
        return removeAdmin(session.getContextId(), delegate.searchContacts(session, term));
    }

    @Override
    public <O> SearchIterator<Contact> searchContacts(Session session, SearchTerm<O> term, SortOptions sortOptions) throws OXException {
        return removeAdmin(session.getContextId(), delegate.searchContacts(session, term, sortOptions));
    }

    @Override
    public <O> SearchIterator<Contact> searchContacts(Session session, SearchTerm<O> term, ContactField[] fields) throws OXException {
        return removeAdmin(session.getContextId(), delegate.searchContacts(session, term, fields));
    }

    @Override
    public <O> SearchIterator<Contact> searchContacts(Session session, SearchTerm<O> term, ContactField[] fields, SortOptions sortOptions) throws OXException {
        return removeAdmin(session.getContextId(), delegate.searchContacts(session, term, fields, sortOptions));
    }

    @Override
    public <O> SearchIterator<Contact> searchContacts(Session session, List<String> folderIds, SearchTerm<O> term, ContactField[] fields, SortOptions sortOptions) throws OXException {
        return removeAdmin(session.getContextId(), delegate.searchContacts(session, folderIds, term, fields, sortOptions));
    }

    @Override
    public SearchIterator<Contact> searchContacts(Session session, ContactSearchObject contactSearch) throws OXException {
        return removeAdmin(session.getContextId(), delegate.searchContacts(session, contactSearch));
    }

    @Override
    public SearchIterator<Contact> searchContacts(Session session, ContactSearchObject contactSearch, SortOptions sortOptions) throws OXException {
        return removeAdmin(session.getContextId(), delegate.searchContacts(session, contactSearch, sortOptions));
    }

    @Override
    public SearchIterator<Contact> searchContacts(Session session, ContactSearchObject contactSearch, ContactField[] fields) throws OXException {
        return removeAdmin(session.getContextId(), delegate.searchContacts(session, contactSearch, fields));
    }

    @Override
    public SearchIterator<Contact> searchContacts(Session session, ContactSearchObject contactSearch, ContactField[] fields, SortOptions sortOptions) throws OXException {
        return removeAdmin(session.getContextId(), delegate.searchContacts(session, contactSearch, fields, sortOptions));
    }

    @Override
    public SearchIterator<Contact> searchContactsWithBirthday(Session session, Date from, Date until, ContactField[] fields, SortOptions sortOptions) throws OXException {
        return removeAdmin(session.getContextId(), delegate.searchContactsWithBirthday(session, from, until, fields, sortOptions));
    }

    @Override
    public SearchIterator<Contact> searchContactsWithBirthday(Session session, List<String> folderIDs, Date from, Date until, ContactField[] fields, SortOptions sortOptions) throws OXException {
        return removeAdmin(session.getContextId(), delegate.searchContactsWithBirthday(session, folderIDs, from, until, fields, sortOptions));
    }

    @Override
    public SearchIterator<Contact> searchContactsWithAnniversary(Session session, Date from, Date until, ContactField[] fields, SortOptions sortOptions) throws OXException {
        return removeAdmin(session.getContextId(), delegate.searchContactsWithAnniversary(session, from, until, fields, sortOptions));
    }

    @Override
    public SearchIterator<Contact> searchContactsWithAnniversary(Session session, List<String> folderIDs, Date from, Date until, ContactField[] fields, SortOptions sortOptions) throws OXException {
        return removeAdmin(session.getContextId(), delegate.searchContactsWithAnniversary(session, folderIDs, from, until, fields, sortOptions));
    }

    @Override
    public SearchIterator<Contact> autocompleteContacts(Session session, List<String> folderIDs, String query, AutocompleteParameters parameters, ContactField[] fields, SortOptions sortOptions) throws OXException {
        return removeAdmin(session.getContextId(), delegate.autocompleteContacts(session, folderIDs, query, parameters, fields, sortOptions));
    }

    @Override
    public SearchIterator<Contact> autocompleteContacts(Session session, String query, AutocompleteParameters parameters, ContactField[] fields, SortOptions sortOptions) throws OXException {
        return removeAdmin(session.getContextId(), delegate.autocompleteContacts(session, query, parameters, fields, sortOptions));
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
    public SearchIterator<Contact> getGuestUsers(Session session, int[] userIDs, ContactField[] fields) throws OXException {
        return delegate.getGuestUsers(session, userIDs, fields);
    }

    @Override
    public SearchIterator<Contact> getAllUsers(Session session, ContactField[] fields, SortOptions sortOptions) throws OXException {
        return removeAdmin(session.getContextId(), delegate.getAllUsers(session, fields, sortOptions));
    }

    @Override
    public String getOrganization(Session session) throws OXException {
        return delegate.getOrganization(session);
    }

    @Override
    public <O> SearchIterator<Contact> searchUsers(Session session, SearchTerm<O> term, ContactField[] fields, SortOptions sortOptions) throws OXException {
        return removeAdmin(session.getContextId(), delegate.searchUsers(session, term, fields, sortOptions));
    }

    @Override
    public SearchIterator<Contact> searchUsers(Session session, ContactSearchObject contactSearch, ContactField[] fields, SortOptions sortOptions) throws OXException {
        return removeAdmin(session.getContextId(), delegate.searchUsers(session, contactSearch, fields, sortOptions));
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
