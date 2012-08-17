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
        Contact contact = null;
        if (LdapContactCache.isCached(fields)) {
            contact = cache.get(Integer.valueOf(parse(id)));
        }
        if (null == contact) {
            return super.get(session, folderId, id, fields);
        } else {
            Contact fullContact = super.get(session, folderId, id, LdapContactCache.getUnknownFields(fields));
            super.mapper.mergeDifferences(fullContact, contact);
            return fullContact;
        }
    }

    @Override
    public SearchIterator<Contact> all(Session session, String folderId, ContactField[] fields, SortOptions sortOptions) throws OXException {
        if (LdapContactCache.isCached(fields)) {
            Collection<Contact> contacts = cache.values();
            return sort(contacts, sortOptions);
        } else {
            SearchIterator<Contact> searchIterator = super.all(session, folderId,  
                LdapContactCache.getUnknownFields(fields, ContactField.OBJECT_ID), sortOptions);
            return mergeCacheData(session, searchIterator, fields);
        }
    }

    @Override
    public SearchIterator<Contact> list(Session session, String folderId, String[] ids, ContactField[] fields, SortOptions sortOptions) throws OXException {
        if (LdapContactCache.isCached(fields)) {
            return sort(cache.list(parse(ids)), sortOptions);
        } else {
            SearchIterator<Contact> searchIterator = super.list(session, folderId, ids, 
                LdapContactCache.getUnknownFields(fields, ContactField.OBJECT_ID), sortOptions);
            return mergeCacheData(session, searchIterator, fields);
        }
    }
    
    @Override
    public SearchIterator<Contact> modified(Session session, String folderID, Date since, ContactField[] fields, SortOptions sortOptions) throws OXException {
        if (LdapContactCache.isCached(fields)) {
            Collection<Contact> contacts = new ArrayList<Contact>();
            for (Contact contact : cache.values()) {
                if (contact.getLastModified().after(since)) {
                    contacts.add(contact);                                
                }
            }
            return sort(contacts, sortOptions);
        } else {
            SearchIterator<Contact> searchIterator = super.modified(session, folderID, since, 
                LdapContactCache.getUnknownFields(fields, ContactField.OBJECT_ID), sortOptions);
            return mergeCacheData(session, searchIterator, fields);
        }
    }

    @Override
    public <O> SearchIterator<Contact> search(Session session, SearchTerm<O> term, ContactField[] fields, SortOptions sortOptions) throws OXException {
        if (LdapContactCache.isCached(fields) && LdapContactCache.isCached(term)) {
            return sort(filter(cache.values(), term, Tools.getLocale(sortOptions)), sortOptions);
        } else {
            SearchIterator<Contact> searchIterator = super.search(session, term,  
                LdapContactCache.getUnknownFields(fields, ContactField.OBJECT_ID), sortOptions);
            return mergeCacheData(session, searchIterator, fields);
        }
    }
    
    private SearchIterator<Contact> mergeCacheData(Session session, SearchIterator<Contact> searchIterator, ContactField[] originalRequestedFields) throws OXException {
        Collection<Contact> contacts = 0 < searchIterator.size() ? new ArrayList<Contact>(searchIterator.size()) : new ArrayList<Contact>();
        try {
            while (searchIterator.hasNext()) {
                Contact loadedContact = searchIterator.next();
                Contact cachedContact = cache.get(loadedContact.getObjectID());
                if (null == cachedContact) {
                    // not cached, load completely
                    contacts.add(super.get(session, Integer.toString(getFolderID()), 
                        Integer.toString(loadedContact.getObjectID()), originalRequestedFields));
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
        if (null != contacts && null != term) {
            return new SearchFilter(term, locale).filter(contacts);
        } else {
            return contacts;
        }
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
