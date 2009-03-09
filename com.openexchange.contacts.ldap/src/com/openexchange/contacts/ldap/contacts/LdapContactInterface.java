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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.contacts.ldap.contacts;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import com.openexchange.api.OXConflictException;
import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api2.OXConcurrentModificationException;
import com.openexchange.api2.OXException;
import com.openexchange.contact.LdapServer;
import com.openexchange.contacts.ldap.exceptions.LdapException;
import com.openexchange.contacts.ldap.exceptions.LdapException.Code;
import com.openexchange.contacts.ldap.ldap.GlobalLdapPool;
import com.openexchange.contacts.ldap.ldap.LdapGetter;
import com.openexchange.contacts.ldap.ldap.LdapUtility;
import com.openexchange.groupware.contact.ContactException;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.ArrayIterator;
import com.openexchange.tools.iterator.SearchIterator;


public class LdapContactInterface implements ContactInterface {
    
    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(LdapContactInterface.class);
    
    private final String context;
    
    private Session session;
    
    public LdapContactInterface(final String context) {
        this.context = context;
    }
    
    
    public void deleteContactObject(int oid, int fuid, Date client_date) throws OXObjectNotFoundException, OXConflictException, OXException {
        throw new LdapException(Code.DELETE_NOT_POSSIBLE);
    }

    public SearchIterator<ContactObject> getContactsByExtendedSearch(ContactSearchObject searchobject, int orderBy, String orderDir, int[] cols) throws OXException {
        LOG.info("Called getContactsByExtendedSearch");
        return null;
    }

    // The all request...
    public SearchIterator<ContactObject> getContactsInFolder(int folderId, int from, int to, int orderBy, String orderDir, int[] cols) throws OXException {
        LOG.info("Called getContactsInFolder");
        
        final ArrayList<ContactObject> arrayList = new ArrayList<ContactObject>();
//        arrayList.add(getDummyContact(folderId));
        try {
            final LdapContext context2 = GlobalLdapPool.getContext();
            final SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(LdapUtility.getSearchControl());
            final String filter = "(objectclass=posixaccount)";
            final NamingEnumeration<SearchResult> search = context2.search("dc=oxnbg,dc=int", filter, searchControls);
            while (search.hasMore()) {
                final SearchResult next = search.next();
                final Attributes attributes = next.getAttributes();
                
                final ContactObject contact = Mapper.getContact(getLdapGetter(attributes));
                arrayList.add(contact);
            }
        } catch (NamingException e) {
            // TODO Handle 
            e.printStackTrace();
        }

        
        
        final SearchIterator<ContactObject> searchIterator = new ArrayIterator<ContactObject>(arrayList.toArray(new ContactObject[arrayList.size()]));
        return searchIterator;
    }


    public SearchIterator<ContactObject> getDeletedContactsInFolder(int folderId, int[] cols, Date since) throws OXException {
        LOG.info("Called getDeletedContactsInFolder");
        return null;
    }

    public int getFolderId() {
        // TODO Auto-generated method stub
        return 0;
    }

    public LdapServer getLdapServer() {
        final LdapServer ldapServer = new LdapServer();
        ldapServer.setContext(context);
        return ldapServer;
    }

    public SearchIterator<ContactObject> getModifiedContactsInFolder(int folderId, int[] cols, Date since) throws OXException {
        LOG.info("Called getModifiedContactsInFolder");
        return new ArrayIterator<ContactObject>(new ContactObject[0]);
    }

    public int getNumberOfContacts(int folderId) throws OXException {
        LOG.info("Called getNumberOfContacts");
        return 0;
    }

    public ContactObject getObjectById(int objectId, int inFolder) throws OXException {
        LOG.info("Called getObjectById");
        return null;
    }

    public SearchIterator<ContactObject> getObjectsById(int[][] objectIdAndInFolder, int[] cols) throws OXException {
        LOG.info("Called getObjectsById");
        final ArrayList<ContactObject> arrayList = new ArrayList<ContactObject>();
        for (final int[] object : objectIdAndInFolder) {
            final int object_id = object[0];
            final int folder_id = object[1];
            if (2 == object_id) {
                arrayList.add(getDummyContact(folder_id));
            }
        }
        return new ArrayIterator<ContactObject>(arrayList.toArray(new ContactObject[arrayList.size()]));
    }

    public ContactObject getUserById(int userId) throws OXException {
        LOG.info("Called getUserById");
        return null;
    }

    public void insertContactObject(ContactObject co) throws OXException {
        // TODO Auto-generated method stub

    }

    public SearchIterator<ContactObject> searchContacts(String searchpattern, int folderId, int orderBy, String orderDir, int[] cols) throws OXException {
        LOG.info("Called searchContacts");
        return null;
    }

    public void setSession(Session s) throws OXException {
        this.session = session;
    }

    public void updateContactObject(ContactObject co, int fid, Date d) throws OXException, OXConcurrentModificationException, ContactException {
        LOG.info("Called updateContactObject");
    }


    private ContactObject getDummyContact(int folderId) {
        final ContactObject contactObject = new ContactObject();
        contactObject.setContextId(111);
        contactObject.setMiddleName("test1");
        contactObject.setParentFolderID(folderId);
        contactObject.setObjectID(2);
//        contactObject.setInternalUserId(1);
        contactObject.setNickname("test1");
        contactObject.setDisplayName("test1");
        contactObject.setGivenName("test1");
        // TODO: Put Admin user here
        contactObject.setCreatedBy(2);
        contactObject.setNumberOfAttachments(0);
        contactObject.setLastModified(new Date(System.currentTimeMillis()));
        return contactObject;
    }


    private LdapGetter getLdapGetter(final Attributes attributes) {
        return new LdapGetter() {
    
            public String getAttribute(String attributename) throws LdapException {
                try {
                    final Attribute attribute = attributes.get(attributename);
                    if (null != attribute) {
                        return (String) attribute.get();
                    } else {
                        return null;
                    }
                } catch (final NamingException e) {
                    throw new LdapException(Code.ERROR_GETTING_ATTRIBUTE, e.getMessage());
                }
            }
    
            public int getIntAttribute(String attributename) throws LdapException {
                try {
                    final Attribute attribute = attributes.get(attributename);
                    if (null != attribute) {
                        return Integer.parseInt((String) attribute.get());
                    } else {
                        return -1;
                    }
                } catch (final NumberFormatException e) {
                    throw new LdapException(Code.ERROR_GETTING_ATTRIBUTE, e.getMessage());
                } catch (final NamingException e) {
                    throw new LdapException(Code.ERROR_GETTING_ATTRIBUTE, e.getMessage());
                }
            }
    
            public Date getDateAttribute(String attributename) throws LdapException {
                try {
                    final Attribute attribute = attributes.get(attributename);
                    if (null != attribute) {
                        final DateFormat dateInstance = DateFormat.getDateInstance();
                        return dateInstance.parse((String) attribute.get());
                    } else {
                        return null;
                    }
                } catch (ParseException e) {
                    throw new LdapException(Code.ERROR_GETTING_ATTRIBUTE, e.getMessage());
                } catch (NamingException e) {
                    throw new LdapException(Code.ERROR_GETTING_ATTRIBUTE, e.getMessage());
                }
            }
            
        };
    }

}
