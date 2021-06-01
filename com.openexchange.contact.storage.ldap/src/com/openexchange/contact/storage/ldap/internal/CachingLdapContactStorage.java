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

package com.openexchange.contact.storage.ldap.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.openexchange.contact.SortOptions;
import com.openexchange.contact.storage.ldap.config.LdapConfig;
import com.openexchange.contact.storage.ldap.mapping.LdapMapper;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.search.SearchTerm;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link CachingLdapContactStorage}
 *
 * LDAP storage for contacts.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CachingLdapContactStorage extends LdapContactStorage {

    private final LdapContactCache cache;

    /**
     * Fields needed from both the cache and the storage to perform merge operations afterwards
     */
    private static final ContactField[] FIELDS_FOR_MERGE = {
        ContactField.OBJECT_ID, ContactField.MARK_AS_DISTRIBUTIONLIST, ContactField.DISTRIBUTIONLIST
    };

    /**
     * Initializes a new {@link CachingLdapContactStorage}.
     *
     * @param delegate
     * @throws OXException
     */
    public CachingLdapContactStorage(LdapConfig config) throws OXException {
        super(config);
        boolean incrementalSync = config.isAdsDeletionSupport() && null != mapper.opt(ContactField.LAST_MODIFIED);
        this.cache = new LdapContactCache(this, config.getRefreshinterval(), incrementalSync, Tools.loadProperties(config.getCacheConfigFile()));
    }

    @Override
    public Contact get(Session session, String folderId, String id, ContactField[] fields) throws OXException {
        check(session.getContextId(), folderId);
        Contact contact = null;
        if (LdapContactCache.isCached(fields)) {
            contact = cache.get(parse(id));
        }
        if (null == contact) {
            return doGet(session, folderId, id, fields);
        }
        Contact fullContact = doGet(session, folderId, id, LdapContactCache.getUnknownFields(fields, FIELDS_FOR_MERGE));
        super.mapper.mergeDifferences(fullContact, contact);
        return fullContact;
    }

    @Override
    public SearchIterator<Contact> all(Session session, String folderId, ContactField[] fields, SortOptions sortOptions) throws OXException {
        check(session.getContextId(), folderId);
        if (LdapContactCache.isCached(fields)) {
            Collection<Contact> contacts = cache.values();
            return sort(contacts, sortOptions);
        }
        ContactField[] unknownFields = LdapContactCache.getUnknownFields(fields, FIELDS_FOR_MERGE);
        SearchIterator<Contact> searchIterator = doAll(session, folderId, unknownFields, sortOptions);
        return mergeCacheData(session, searchIterator, fields);
    }

    @Override
    public SearchIterator<Contact> list(Session session, String folderId, String[] ids, ContactField[] fields, SortOptions sortOptions) throws OXException {
        check(session.getContextId(), folderId);
        if (LdapContactCache.isCached(fields)) {
            return sort(cache.list(parse(ids)), sortOptions);
        }
        ContactField[] unknownFields = LdapContactCache.getUnknownFields(fields, FIELDS_FOR_MERGE);
        SearchIterator<Contact> searchIterator = doList(session, folderId, ids, unknownFields, sortOptions);
        return mergeCacheData(session, searchIterator, fields);
    }

    @Override
    public int count(Session session, String folderId, boolean canReadAll) throws OXException {
        check(session.getContextId(), folderId);
        if (cache.isCacheReady()) {
            return cache.values().size();
        }
        return super.count(session, folderId, canReadAll);
    }

    @Override
    public SearchIterator<Contact> modified(Session session, String folderID, Date since, ContactField[] fields, SortOptions sortOptions) throws OXException {
        check(session.getContextId(), folderID);
        if (LdapContactCache.isCached(fields)) {
            Collection<Contact> contacts = new ArrayList<Contact>();
            for (Contact contact : cache.values()) {
                if (null != contact.getLastModified() && contact.getLastModified().after(since)) {
                    contacts.add(contact);
                }
            }
            return sort(contacts, sortOptions);
        }
        ContactField[] unknownFields = LdapContactCache.getUnknownFields(fields, FIELDS_FOR_MERGE);
        SearchIterator<Contact> searchIterator = doModified(session, folderID, since, unknownFields, sortOptions);
        return mergeCacheData(session, searchIterator, fields);
    }

    @Override
    public <O> SearchIterator<Contact> search(Session session, SearchTerm<O> term, ContactField[] fields, SortOptions sortOptions) throws OXException {
        checkContext(session.getContextId());
        if (LdapContactCache.isCached(fields) && LdapContactCache.isCached(term)) {
            return sort(filter(cache.values(), term, Tools.getLocale(sortOptions)), sortOptions);
        }
        ContactField[] unknownFields = LdapContactCache.getUnknownFields(fields, FIELDS_FOR_MERGE);
        SearchIterator<Contact> searchIterator = doSearch(session, term, unknownFields, sortOptions);
        return mergeCacheData(session, searchIterator, fields);
    }

    private SearchIterator<Contact> mergeCacheData(Session session, SearchIterator<Contact> searchIterator, ContactField[] originalRequestedFields) throws OXException {
        Collection<Contact> contacts = 0 < searchIterator.size() ? new ArrayList<Contact>(searchIterator.size()) : new ArrayList<Contact>();
        try {
            while (searchIterator.hasNext()) {
                Contact loadedContact = searchIterator.next();
                Contact cachedContact = cache.get(loadedContact.getObjectID());
                if (null == cachedContact) {
                    // not cached, try to load completely as fallback
                    Contact fallbackContact = null;
                    try {
                        fallbackContact = super.get(session, Integer.toString(getFolderID()),
                            Integer.toString(loadedContact.getObjectID()), originalRequestedFields);
                    } catch (OXException e) {
                        if (false == e.isNotFound()) {
                            throw e;
                        }
                    }
                    if (null != fallbackContact) {
                        contacts.add(fallbackContact);
                    }
                } else {
                    // merge information from cache
                    mapper.mergeDifferences(loadedContact, cachedContact);
                    contacts.add(loadedContact);
                }
            }
            return getSearchIterator(contacts);
        } finally {
            Tools.close(searchIterator);
        }
    }

    private static <O> Collection<Contact> filter(Collection<Contact> contacts,  SearchTerm<O> term, Locale locale) throws OXException {
        return null != contacts && null != term ? new SearchFilter(term, locale).filter(contacts) : contacts;
    }

    private static SearchIterator<Contact> sort(List<Contact> contacts, SortOptions sortOptions) {
        if (null != contacts && 1 < contacts.size() &&
            null != sortOptions && false == SortOptions.EMPTY.equals(sortOptions) &&
            null != sortOptions.getOrder() && 0 < sortOptions.getOrder().length) {
            Comparator<Contact> comparator = LdapMapper.GENERIC.getComparator(sortOptions);
            if (null != comparator) {
                Collections.sort(contacts, LdapMapper.GENERIC.getComparator(sortOptions));
            }
        }
        return getSearchIterator(contacts);
    }

    private static SearchIterator<Contact> sort(Collection<Contact> contacts, SortOptions sortOptions) {
        if (null == contacts || 2 > contacts.size()) {
            return getSearchIterator(contacts);
        } else if (List.class.isInstance(contacts)) {
            return sort((List<Contact>)contacts, sortOptions);
        } else {
            return sort(new ArrayList<Contact>(contacts), sortOptions);
        }
    }

}
