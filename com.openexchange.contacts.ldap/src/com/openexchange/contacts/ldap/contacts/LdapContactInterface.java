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

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.SortControl;
import javax.naming.ldap.SortKey;
import com.openexchange.api.OXConflictException;
import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api2.OXConcurrentModificationException;
import com.openexchange.api2.OXException;
import com.openexchange.contact.LdapServer;
import com.openexchange.contacts.ldap.exceptions.LdapException;
import com.openexchange.contacts.ldap.exceptions.LdapException.Code;
import com.openexchange.contacts.ldap.ldap.LdapGetter;
import com.openexchange.contacts.ldap.ldap.LdapUtility;
import com.openexchange.contacts.ldap.property.PropertyHandler;
import com.openexchange.contacts.ldap.property.PropertyHandler.Sorting;
import com.openexchange.groupware.contact.ContactException;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.java.Autoboxing;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.ArrayIterator;
import com.openexchange.tools.iterator.SearchIterator;


public class LdapContactInterface implements ContactInterface {

//    private static final String FILTER = "(objectclass=posixaccount)";
    private static final String FILTER = "(objectclass=person)";


    public class SortInfo {

        private final int field;

        private final Order sort;
        
        /**
         * Initializes a new {@link SortInfo}.
         * @param field
         * @param sort
         */
        private SortInfo(int field, Order sort) {
            this.field = field;
            this.sort = sort;
        }

        
        public final int getField() {
            return field;
        }

        
        public final Order getSort() {
            return sort;
        }
        
    }


    private enum Order {
        asc,
        desc;
        
        
    }
    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(LdapContactInterface.class);
    
    private final int admin_id;
    
    private final String context;
    
    private final Map<Integer, String> keys = new ConcurrentHashMap<Integer, String>();
    
    private final Map<String, Integer> values = new ConcurrentHashMap<String, Integer>();
    
    private Session session;
    
    public LdapContactInterface(final String context, final int admin_id) {
        this.context = context;
        this.admin_id = admin_id;
    }
    
    
    public void deleteContactObject(int oid, int fuid, Date client_date) throws OXObjectNotFoundException, OXConflictException, OXException {
        throw new LdapException(Code.DELETE_NOT_POSSIBLE);
    }

    public SearchIterator<ContactObject> getContactsByExtendedSearch(ContactSearchObject searchobject, int orderBy, String orderDir, int[] cols) throws OXException {
        LOG.info("Called getContactsByExtendedSearch");
        final Order valueOf = getOrder(orderDir);
        final Set<Integer> columns = getColumnSet(cols);
        final int[] folders = searchobject.getFolders();
        final int folderId;
        if (null != folders) {
            if (folders.length == 1) {
                folderId = folders[0];
            } else {
                throw new LdapException(Code.TOO_MANY_FOLDERS);
            }
        } else {
            throw new LdapException(Code.FOLDERID_OBJECT_NULL);
        }
        final ArrayList<ContactObject> arrayList;
        if (searchobject.isStartLetter()) {
            arrayList = getLDAPContacts(folderId, columns, "(&" + FILTER + "("+ PropertyHandler.getInstance().getSurname() + "=" + searchobject.getPattern() + "*))", null);
        } else {
            arrayList = getLDAPContacts(folderId, columns, FILTER, null);
        }
        
        sorting(orderBy, orderDir, valueOf, arrayList);
        return new ArrayIterator<ContactObject>(arrayList.toArray(new ContactObject[arrayList.size()]));
    }

    // The all request...
    public SearchIterator<ContactObject> getContactsInFolder(int folderId, int from, int to, int orderBy, String orderDir, int[] cols) throws OXException {
        LOG.info("Called getContactsInFolder");
        
        final Order valueOf = getOrder(orderDir);
        
        final Set<Integer> columns = getColumnSet(cols);
        final ArrayList<ContactObject> arrayList = getLDAPContacts(folderId, columns, FILTER, null);
        
        // Get only the needed parts...
        final List<ContactObject> subList = getSubList(from, to, arrayList);
        
        sorting(orderBy, orderDir, valueOf, subList);
        final SearchIterator<ContactObject> searchIterator = new ArrayIterator<ContactObject>(subList.toArray(new ContactObject[subList.size()]));
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
        
        final Set<Integer> columns = getColumnSet(cols);
        final ArrayList<ContactObject> contacts = new ArrayList<ContactObject>();
        for (final int[] object : objectIdAndInFolder) {
            final int object_id = object[0];
            final int folder_id = object[1];
            final PropertyHandler instance = PropertyHandler.getInstance();
            if (instance.isMemorymapping()) {
                contacts.addAll(getLDAPContacts(
                    folder_id,
                    columns,
                    "(&" + FILTER + "(" + instance.getUniqueid() + "=" + oxUidToLdapUid(object_id) + "))",
                    null));
            } else {
                contacts.addAll(getLDAPContacts(
                    folder_id,
                    columns,
                    "(&" + FILTER + "(" + instance.getUniqueid() + "=" + object_id + "))",
                    null));
            }
        }
        return new ArrayIterator<ContactObject>(contacts.toArray(new ContactObject[contacts.size()]));
    }

    public ContactObject getUserById(int userId) throws OXException {
        LOG.info("Called getUserById");
        return null;
    }

    public void insertContactObject(ContactObject co) throws OXException {
        throw new LdapException(Code.INSERT_NOT_POSSIBLE);
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

    private String[] getAttributes(final Set<Integer> columns) {
        final List<String> retval = new ArrayList<String>();
        for (final Integer col : columns) {
            final String fieldFromColumn = getFieldFromColumn(col);
            if (null != fieldFromColumn && fieldFromColumn.length() != 0) {
                retval.add(fieldFromColumn);
            }
        }
        return retval.toArray(new String[retval.size()]);
    }

    private Set<Integer> getColumnSet(int[] cols) {
        final Set<Integer> columns = new HashSet<Integer>();
        for (final int col : cols) {
            columns.add(Autoboxing.I(col));
        }
        return columns;
    }

    /**
     * @param instance
     * @param col
     * @return a String value or null if no corresponding entry was found
     */
    private String getFieldFromColumn(final Integer col) {
        final PropertyHandler instance = PropertyHandler.getInstance();
        switch (col) {
        case ContactObject.ANNIVERSARY:
            return instance.getAnniversary();
        case ContactObject.ASSISTANT_NAME:
            return instance.getAssistant_name();
        case ContactObject.BIRTHDAY:
            return instance.getBirthday();
        case ContactObject.BRANCHES:
            return instance.getBranches();
        case ContactObject.BUSINESS_CATEGORY:
            return instance.getBusiness_category();
        case ContactObject.CELLULAR_TELEPHONE1:
            return instance.getCellular_telephone1();
        case ContactObject.CELLULAR_TELEPHONE2:
            return instance.getCellular_telephone2();
        case ContactObject.CITY_BUSINESS:
            return instance.getCity_business();
        case ContactObject.CITY_HOME:
            return instance.getCity_home();
        case ContactObject.CITY_OTHER:
            return instance.getCity_other();
        case ContactObject.COMMERCIAL_REGISTER:
            return instance.getCommercial_register();
        case ContactObject.COMPANY:
            return instance.getCompany();
        case ContactObject.COUNTRY_BUSINESS:
            return instance.getCountry_business();
        case ContactObject.COUNTRY_HOME:
            return instance.getCountry_home();
        case ContactObject.COUNTRY_OTHER:
            return instance.getCountry_other();
        case ContactObject.DEFAULT_ADDRESS:
            return instance.getDefaultaddress();
        case ContactObject.DEPARTMENT:
            return instance.getDepartment();
        case ContactObject.DISPLAY_NAME:
            return instance.getDisplayname();
        case ContactObject.EMAIL1:
            return instance.getEmail1();
        case ContactObject.EMAIL2:
            return instance.getEmail2();
        case ContactObject.EMAIL3:
            return instance.getEmail3();
        case ContactObject.EMPLOYEE_TYPE:
            return instance.getEmployeetype();
        case ContactObject.FAX_BUSINESS:
            return instance.getFax_business();
        case ContactObject.FAX_HOME:
            return instance.getFax_home();
        case ContactObject.FAX_OTHER:
            return instance.getFax_other();
        case ContactObject.GIVEN_NAME:
            return instance.getGivenname();
        case ContactObject.INFO:
            return instance.getInfo();
        case ContactObject.INSTANT_MESSENGER1:
            return instance.getInstant_messenger1();
        case ContactObject.INSTANT_MESSENGER2:
            return instance.getInstant_messenger2();
        case ContactObject.MANAGER_NAME:
            return instance.getManager_name();
        case ContactObject.MARITAL_STATUS:
            return instance.getMarital_status();
        case ContactObject.MIDDLE_NAME:
            return instance.getMiddle_name();
        case ContactObject.NICKNAME:
            return instance.getNickname();
        case ContactObject.NOTE:
            return instance.getNote();
        case ContactObject.NUMBER_OF_CHILDREN:
            return instance.getNumber_of_children();
        case ContactObject.NUMBER_OF_EMPLOYEE:
            return instance.getNumber_of_employee();
        case ContactObject.OBJECT_ID:
            return instance.getUniqueid();
        case ContactObject.POSITION:
            return instance.getPosition();
        case ContactObject.POSTAL_CODE_BUSINESS:
            return instance.getPostal_code_business();
        case ContactObject.POSTAL_CODE_HOME:
            return instance.getPostal_code_home();
        case ContactObject.POSTAL_CODE_OTHER:
            return instance.getPostal_code_other();
        case ContactObject.PROFESSION:
            return instance.getProfession();
        case ContactObject.ROOM_NUMBER:
            return instance.getRoom_number();
        case ContactObject.SALES_VOLUME:
            return instance.getSales_volume();
        case ContactObject.SPOUSE_NAME:
            return instance.getSpouse_name();
        case ContactObject.STATE_BUSINESS:
            return instance.getState_business();
        case ContactObject.STATE_HOME:
            return instance.getState_home();
        case ContactObject.STATE_OTHER:
            return instance.getState_other();
        case ContactObject.STREET_BUSINESS:
            return instance.getStreet_business();
        case ContactObject.STREET_HOME:
            return instance.getStreet_home();
        case ContactObject.STREET_OTHER:
            return instance.getStreet_other();
        case ContactObject.SUFFIX:
            return instance.getSuffix();
        case ContactObject.SUR_NAME:
            return instance.getSurname();
        case ContactObject.TAX_ID:
            return instance.getTax_id();
        case ContactObject.TELEPHONE_ASSISTANT:
            return instance.getTelephone_assistant();
        case ContactObject.TELEPHONE_BUSINESS1:
            return instance.getTelephone_business1();
        case ContactObject.TELEPHONE_BUSINESS2:
            return instance.getTelephone_business2();
        case ContactObject.TELEPHONE_CALLBACK:
            return instance.getTelephone_callback();
        case ContactObject.TELEPHONE_CAR:
            return instance.getTelephone_car();
        case ContactObject.TELEPHONE_COMPANY:
            return instance.getTelephone_company();
        case ContactObject.TELEPHONE_HOME1:
            return instance.getTelephone_home1();
        case ContactObject.TELEPHONE_HOME2:
            return instance.getTelephone_home2();
        case ContactObject.TELEPHONE_IP:
            return instance.getTelephone_ip();
        case ContactObject.TELEPHONE_ISDN:
            return instance.getTelephone_isdn();
        case ContactObject.TELEPHONE_OTHER:
            return instance.getTelephone_other();
        case ContactObject.TELEPHONE_PAGER:
            return instance.getTelephone_pager();
        case ContactObject.TELEPHONE_PRIMARY:
            return instance.getTelephone_primary();
        case ContactObject.TELEPHONE_RADIO:
            return instance.getTelephone_radio();
        case ContactObject.TELEPHONE_TELEX:
            return instance.getTelephone_telex();
        case ContactObject.TELEPHONE_TTYTDD:
            return instance.getTelephone_ttytdd();
        case ContactObject.TITLE:
            return instance.getTitle();
        case ContactObject.URL:
            return instance.getUrl();
        case ContactObject.USERFIELD01:
            return instance.getUserfield01();
        case ContactObject.USERFIELD02:
            return instance.getUserfield02();
        case ContactObject.USERFIELD03:
            return instance.getUserfield03();
        case ContactObject.USERFIELD04:
            return instance.getUserfield04();
        case ContactObject.USERFIELD05:
            return instance.getUserfield05();
        case ContactObject.USERFIELD06:
            return instance.getUserfield06();
        case ContactObject.USERFIELD07:
            return instance.getUserfield07();
        case ContactObject.USERFIELD08:
            return instance.getUserfield08();
        case ContactObject.USERFIELD09:
            return instance.getUserfield09();
        case ContactObject.USERFIELD10:
            return instance.getUserfield10();
        case ContactObject.USERFIELD11:
            return instance.getUserfield11();
        case ContactObject.USERFIELD12:
            return instance.getUserfield12();
        case ContactObject.USERFIELD13:
            return instance.getUserfield13();
        case ContactObject.USERFIELD14:
            return instance.getUserfield14();
        case ContactObject.USERFIELD15:
            return instance.getUserfield15();
        case ContactObject.USERFIELD16:
            return instance.getUserfield16();
        case ContactObject.USERFIELD17:
            return instance.getUserfield17();
        case ContactObject.USERFIELD18:
            return instance.getUserfield18();
        case ContactObject.USERFIELD19:
            return instance.getUserfield19();
        case ContactObject.USERFIELD20:
            return instance.getUserfield20();
        case ContactObject.LAST_MODIFIED:
            return instance.getLastmodified();
        case ContactObject.CREATION_DATE:
            return instance.getCreationdate();
        default:
            return null;
        }
    }


    private ArrayList<ContactObject> getLDAPContacts(int folderId, final Set<Integer> columns, final String filter, SortInfo sortField) throws LdapException {
        final ArrayList<ContactObject> arrayList = new ArrayList<ContactObject>();
        try {
            final PropertyHandler instance = PropertyHandler.getInstance();
            final LdapContext context = LdapUtility.createContext();
            try {

                if (null != sortField && instance.getSorting().equals(Sorting.server)) {
                    final SortKey sortKey = new SortKey(getFieldFromColumn(sortField.getField()), sortField.getSort().equals(Order.asc), null);
                    final SortKey[] sortKeyArray = new SortKey[] { sortKey };
                    context.setRequestControls(new Control[] { new SortControl(sortKeyArray, Control.CRITICAL) });
                }
                final SearchControls searchControls = new SearchControls();
                searchControls.setSearchScope(LdapUtility.getSearchControl());
                searchControls.setReturningAttributes(getAttributes(columns));
                final NamingEnumeration<SearchResult> search = context.search(instance.getBaseDN(), filter, searchControls);
                while (search.hasMore()) {
                    final SearchResult next = search.next();
                    final Attributes attributes = next.getAttributes();
                    final ContactObject contact = Mapper.getContact(getLdapGetter(attributes), columns, new UidInterface() {

                        public Integer getUid(String uid) throws LdapException {
                            return ldapUidToOxUid(uid);
                        }
                        
                    });
                    if (columns.contains(ContactObject.FOLDER_ID)) {
                        contact.setParentFolderID(folderId);
                    }
                    if (columns.contains(ContactObject.CREATED_BY)) {
                        contact.setCreatedBy(this.admin_id);
                    }
                    arrayList.add(contact);
                }
            } catch (final IOException e) {
                LOG.error(e.getMessage(), e);
                throw new LdapException(Code.ERROR_GETTING_ATTRIBUTE, e.getMessage());
            } finally {
                context.close();
            }
        } catch (NamingException e) {
            LOG.error(e.getMessage(), e);
            throw new LdapException(Code.ERROR_GETTING_ATTRIBUTE, e.getMessage());
        }
        return arrayList;
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
            
        };
    }


    private Order getOrder(String orderDir) {
        Order valueOf = null;
        if (null != orderDir) {
            final String lowerCase = orderDir.toLowerCase();
            valueOf = Order.valueOf(lowerCase);
        }
        return valueOf;
    }


    private List<ContactObject> getSubList(int from, int to, final ArrayList<ContactObject> arrayList) {
        final int size = arrayList.size();
        if (from <= 0 && to >= size) {
            return arrayList;
        } else {
            if (from < 0) {
                from = 0;
            }
            if (to >= size) {
                to = size;
            }
            return arrayList.subList(from, to);
        }
    }


    private void sorting(int orderBy, String orderDir, Order valueOf, final List<ContactObject> subList) {
        if (null != orderDir && PropertyHandler.getInstance().getSorting().equals(Sorting.groupware)) {
            Collections.sort(subList, new ContactComparator(orderBy));
        }
    }
    
    private String oxUidToLdapUid(final int uid) throws LdapException {
        final String ldapUid = keys.get(Autoboxing.I(uid));
        if (null != ldapUid) {
            return ldapUid;
        } else {
            throw new LdapException(Code.NO_SUCH_LONG_UID_IN_MAPPING_TABLE_FOUND, String.valueOf(uid));
        }
    }

    private Integer ldapUidToOxUid(final String uid) throws LdapException {
        final Integer number = values.get(uid);
        if (null != number) {
            return number;
        } else {
            // First we add to the keys table than we fetch the values one and add there too
            final Integer newvalue = Autoboxing.I(values.size()+1);
            values.put(uid, newvalue);
            keys.put(newvalue, uid);
            return newvalue;
        }
    }

}
