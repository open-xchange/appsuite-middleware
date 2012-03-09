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

package com.openexchange.contact.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactAttributeFetcher;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.contact.helpers.ContactGetter;
import com.openexchange.groupware.contact.helpers.ContactSetter;
import com.openexchange.groupware.contact.helpers.ContactSwitcher;
import com.openexchange.groupware.container.Contact;
import com.openexchange.search.SearchService;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.internal.SearchServiceImpl;

/**
 * {@link SimContactStorage} - In-memory {@link ContactStorage} implementation.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SimContactStorage extends DefaultContactStorage {
    
    private final Map<String, Contact> contacts;
    private final List<Contact> deletedContacts;
    private final SearchService searchService;

    public SimContactStorage() {
        super();
        this.contacts = new HashMap<String, Contact>();
        this.deletedContacts = new ArrayList<Contact>();
        this.searchService = new SearchServiceImpl();
    }

    @Override
    public boolean supports(int contextID, String folderId) throws OXException {
        return true;
    }

    @Override
    public Contact get(int contextID, String folderId, String id, ContactField[] fields) throws OXException {
        final Contact contact = contacts.get(id);
        return null != contact && this.matches(contact, folderId) ? this.filter(contact, fields) : null;
    }

    @Override
    public Collection<Contact> all(final int contextID, final String folderId, final ContactField[] fields) throws OXException {
        final Collection<Contact> contacts = new ArrayList<Contact>();
        for (final Contact contact : this.contacts.values()) {
            if (this.matches(contact, folderId)) {
                contacts.add(this.filter(contact, fields));
            }
        }        
        return contacts;
    }

    @Override
    public Collection<Contact> list(final int contextID, final String folderId, final String[] ids, final ContactField[] fields) throws OXException {
        final Collection<Contact> contacts = new ArrayList<Contact>();
        for (final String id : ids) {
            final Contact contact = this.contacts.get(id);
            if (null != contact && this.matches(contact, folderId)) {
                contacts.add(this.filter(contact, fields));
            }
        }
        return contacts;
    }
    
    @Override
    public Collection<Contact> deleted(final int contextID, final String folderId, final Date since, final ContactField[] fields) throws OXException {
        final Collection<Contact> contacts = new ArrayList<Contact>();
        for (final Contact contact : this.deletedContacts) {
            if (contact.getLastModified().after(since) && this.matches(contact, folderId)) {
                contacts.add(this.filter(contact, fields));
            }
        }        
        return contacts;
    }
  
	@Override
	public Collection<Contact> modified(int contextID, String folderId, Date since, ContactField[] fields) throws OXException {
        final Collection<Contact> contacts = new ArrayList<Contact>();
        for (final Contact contact : this.contacts.values()) {
            if (contact.getLastModified().after(since) && this.matches(contact, folderId)) {
                contacts.add(this.filter(contact, fields));
            }
        }        
        return contacts;
	}

    @Override
    public <O> Collection<Contact> search(final int contextID, final SearchTerm<O> term, final ContactField[] fields, SortOptions sortOptions) throws OXException {
        return this.searchService.filter(this.contacts.values(), term, ContactAttributeFetcher.getInstance());
    }

    @Override
    public synchronized void create(final int contextID, final String folderId, final Contact contact) throws OXException {
        final Date now = new Date();
        final String id = this.nextId();
        contact.setObjectID(Integer.parseInt(id));
//        contact.setCreatedBy(session.getUserId());
//        contact.setModifiedBy(session.getUserId());
        contact.setContextId(contextID);
        contact.setParentFolderID(Integer.parseInt(folderId));            
        contact.setLastModified(now);
        contact.setCreationDate(now);
        this.contacts.put(id, contact);
    }

    @Override
    public synchronized void update(final int contextID, final String folderId, final Contact contact, final Date lastRead) throws OXException {
        final String id = Integer.toString(contact.getObjectID());
        final Contact existingContact = this.contacts.get(id);
        if (null == existingContact) {
            throw new IllegalArgumentException("contact " + id + " not found");
        } else if (existingContact.getLastModified().after(lastRead)) {
            throw new IllegalArgumentException("object changed in the meantime");
        }
//        contact.setModifiedBy(session.getUserId());
        contact.setContextId(contextID);
        contact.setParentFolderID(Integer.parseInt(folderId));
        contact.setLastModified(new Date());
        this.contacts.put(id, merge(existingContact, contact));
    }

    @Override
    public synchronized void delete(int contextID, String folderId, String id, Date lastRead) throws OXException {
        final Contact existingContact = this.contacts.get(id);
        if (null == existingContact || false == this.matches(existingContact, folderId)) {
            throw new IllegalArgumentException("contact " + id + " not found in folder " + folderId);
        } else if (existingContact.getLastModified().after(lastRead)) {
            throw new IllegalArgumentException("object changed in the meantime");
        } else {
            final Contact deleted = this.contacts.remove(id);
            final Date now = new Date();
//            deleted.setModifiedBy(session.getUserId());
            deleted.setContextId(contextID);
            deleted.setParentFolderID(Integer.parseInt(folderId));            
            deleted.setLastModified(now);
            this.deletedContacts.add(deleted);
        }
    }
    
    private Contact filter(final Contact contact, final ContactField[] fields) throws OXException {
        final Contact filteredContact = new Contact();
        final ContactSwitcher getter = new ContactGetter();
        final ContactSwitcher setter = new ContactSetter();
        for (final ContactField field : fields) {
            final Object value = field.doSwitch(getter, contact);
            field.doSwitch(setter, filteredContact, value);
        }
        return filteredContact;
    }
    
    private String nextId() {
        int maxId = 0;
        for (final String id : this.contacts.keySet()) {
            maxId = Math.max(maxId, Integer.parseInt(id));                
        }
        return Integer.toString(maxId + 1);        
    }
    
    private boolean matches(final Contact contact, final String folderId) {
        return folderId.equals(Integer.toString(contact.getParentFolderID()));
    }

}
