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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.contact.ContactFieldOperand;
import com.openexchange.contact.SortOptions;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.log.LogFactory;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ConstantOperand;
import com.openexchange.timer.TimerService;

/**
 * {@link LdapContactCache} 
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class LdapContactCache {
    
    private static final org.apache.commons.logging.Log LOG = LogFactory.getLog(LdapContactCache.class);
    private static final EnumSet<ContactField> CACHED_FIELDS = EnumSet.of(
        ContactField.CONTEXTID, ContactField.FOLDER_ID, ContactField.OBJECT_ID, ContactField.INTERNAL_USERID, ContactField.UID, 
        ContactField.LAST_MODIFIED, ContactField.CREATION_DATE, ContactField.MODIFIED_BY, ContactField.CREATED_BY,
        ContactField.PRIVATE_FLAG, ContactField.USE_COUNT, ContactField.MARK_AS_DISTRIBUTIONLIST, ContactField.DISTRIBUTIONLIST, 
        ContactField.EMAIL1, ContactField.EMAIL2, ContactField.EMAIL3,
        ContactField.DISPLAY_NAME, ContactField.FILE_AS, ContactField.COMPANY, ContactField.YOMI_COMPANY, 
        ContactField.SUR_NAME, ContactField.GIVEN_NAME, ContactField.YOMI_LAST_NAME, ContactField.YOMI_FIRST_NAME 
    );
    private static final ContactField[] CACHED_FIELDS_ARRAY = CACHED_FIELDS.toArray(new ContactField[CACHED_FIELDS.size()]);

    private final LdapContactStorage storage;
    private final int refreshInterval;
    private final boolean incrementalSync;
    private ContactLoader loader;

    /**
     * Initializes a new {@link LdapContactCache}.
     * 
     * @param storage the storage to cache the data for
     * @param refreshInterval the refresh interval in ms
     * @throws OXException
     */
    public LdapContactCache(LdapContactStorage storage, int refreshInterval, boolean incrementalSync, Properties properties) throws OXException {
        super();
        this.storage = storage;
        this.refreshInterval = refreshInterval;
        this.loader = null;
        this.incrementalSync = incrementalSync;
        initCache(getRegionName(), properties);
    }
    
    private String getRegionName() throws OXException {
        return "CONTACT_LDAP_" + storage.getContextID() + "_" + storage.getFolderID();
    }

    /**
     * Gets the contact to which the specified object ID is mapped. 
     * 
     * @param objectID the contact's object ID
     * @return the contact, or <code>null</code> if not found.
     * @throws OXException
     */
    public Contact get(int objectID) throws OXException {
        return (Contact)getCache().get(Integer.valueOf(objectID));
    }

    /**
     * Gets a list of the contact with the specified object IDs. 
     * 
     * @param objectIDs
     * @return
     * @throws OXException
     */
    public List<Contact> list(int[] objectIDs) throws OXException {
        Cache cache = getCache();
        List<Contact> contacts = new ArrayList<Contact>(objectIDs.length);
        for (int objectID : objectIDs) {
            contacts.add((Contact)cache.get(Integer.valueOf(objectID)));
        }        
        return contacts;
    }
    
    /**
     * Gets all contacts.
     * 
     * @return the contacts
     * @throws OXException
     */
    public Collection<Contact> values() throws OXException {
        Collection<Serializable> values = getCache().values();
        if (null == values) {
            return Collections.emptyList();
        } else {
            Collection<Contact> contacts = new ArrayList<Contact>(values.size());
            for (Serializable value : values) {
                contacts.add((Contact)value);
            }
            return contacts;
        }
    }
    
    /**
     * Gets a value indicating whether all of the supplied fields are present 
     * in the cache or not.
     *  
     * @param requestedFields the contact fields
     * @return <code>true</code>, if the fields are cached, <code>false</code>,
     *         otherwise
     * @throws OXException
     */
    public static boolean isCached(ContactField[] requestedFields) throws OXException {
        return null != requestedFields && CACHED_FIELDS.containsAll(Arrays.asList(requestedFields));
    }
    
    /**
     * 
     * @param requestedFields
     * @return
     */
    public static ContactField[] getUnknownFields(ContactField[] requestedFields, ContactField...mandatoryFields) {
        if (null != requestedFields && 0 < requestedFields.length) {
            Set<ContactField> unknownFields = new HashSet<ContactField>();
            for (ContactField requestedField : requestedFields) {
                if (false == CACHED_FIELDS.contains(requestedField)) {
                    unknownFields.add(requestedField);
                }
            }
            if (null != mandatoryFields && 0 < mandatoryFields.length) {
                for (ContactField mandatoryField : mandatoryFields) {
                    unknownFields.add(mandatoryField);
                }
            }
            return unknownFields.toArray(new ContactField[unknownFields.size()]);
        } else {
            return requestedFields;
        }
    }

    private static void initCache(String regionName, Properties properties) throws OXException {
        Properties customProperties = new Properties();
        for (Entry<Object, Object> entry : properties.entrySet()) {
            if (null != entry.getKey() && String.class.isInstance(entry.getKey())) {
                customProperties.put(((String)entry.getKey()).replace("[REGIONNAME]", regionName), entry.getValue());
            }
        }
        LdapServiceLookup.getService(CacheService.class).loadConfiguration(properties);
    }
    
    private Cache getCache() throws OXException {
        if (null == this.loader) {
            synchronized (this) {
                if (null == this.loader) {
                    /*
                     * blocking load on first access, scheduled refresh afterwards
                     */
                    loader = new ContactLoader();
                    loader.reloadContacts();
                    LdapServiceLookup.getService(TimerService.class).scheduleWithFixedDelay(
                        loader, refreshInterval, refreshInterval, TimeUnit.MILLISECONDS);
                }
            }
        }
        return LdapServiceLookup.getService(CacheService.class).getCache(getRegionName());
    }
    
    private final class ContactLoader implements Runnable {
        
        private Date lastModified;
    
        public ContactLoader() {
            super();
            this.lastModified = null;
        }

        @Override
        public void run() {
            try {
                if (false == incrementalSync || null == this.lastModified || 1000 >= lastModified.getTime()) {
                    reloadContacts();
                } else {
                    refreshContacts();
                }
            } catch (OXException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        
        private void refreshContacts() throws OXException {
            Cache cache = LdapServiceLookup.getService(CacheService.class).getCache(getRegionName());
            LOG.debug("Refreshing contacts.");
            Date start = new Date();
            int deleted = 0;
            int updated = 0;
            SingleSearchTerm term = new SingleSearchTerm(SingleOperation.GREATER_THAN);
            term.addOperand(new ContactFieldOperand(ContactField.LAST_MODIFIED));
            term.addOperand(new ConstantOperand<Date>(this.lastModified));
            Date newLastModified = new Date(0);
            for (Contact contact : storage.search(null, term, CACHED_FIELDS_ARRAY, SortOptions.EMPTY, false)) {
                newLastModified = getLatestModified(newLastModified, contact);
                cache.put(Integer.valueOf(contact.getObjectID()), contact);
                updated++;
            }
            for (Contact contact : storage.search(null, term, CACHED_FIELDS_ARRAY, SortOptions.EMPTY, true)) {
                newLastModified = getLatestModified(newLastModified, contact);
                cache.remove(Integer.valueOf(contact.getObjectID()));
                deleted++;
            }
            if (0 < updated || 0 < deleted) {
                this.lastModified = newLastModified;
                LOG.debug("Contacts refreshed, got " + updated + " modified and " + deleted + 
                    " deleted contacts in "  + (new Date().getTime() - start.getTime()) + "ms.");
            } else {
                LOG.debug("No changes detected, check took " + (new Date().getTime() - start.getTime()) + "ms.");
            }
        }
        
        private void reloadContacts() throws OXException {
            Cache cache = LdapServiceLookup.getService(CacheService.class).getCache(getRegionName());
            LOG.debug("Reloading contacts.");
            Date start = new Date();
            int added = 0;
            Date newLastModified = new Date(0);
            for (Contact contact : storage.getContacts(null, CACHED_FIELDS_ARRAY, SortOptions.EMPTY, false)) {
                newLastModified = getLatestModified(newLastModified, contact);
                cache.put(Integer.valueOf(contact.getObjectID()), contact);
                added++;
            }
            this.lastModified = newLastModified;
            LOG.debug("Contacts reloaded, got " + added + " entries in " + (new Date().getTime() - start.getTime()) + "ms.");
        }
        
        private Date getLatestModified(Date lastModified, Contact contact) {
            return null != contact.getLastModified() && lastModified.after(contact.getLastModified()) ? 
                lastModified : contact.getLastModified();
        }
        
    }
    
}
