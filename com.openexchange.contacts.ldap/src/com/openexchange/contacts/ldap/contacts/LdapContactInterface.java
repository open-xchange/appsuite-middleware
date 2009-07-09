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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;
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
import com.openexchange.contacts.ldap.property.FolderProperties;
import com.openexchange.contacts.ldap.property.Mappings;
import com.openexchange.contacts.ldap.property.FolderProperties.ContactTypes;
import com.openexchange.contacts.ldap.property.FolderProperties.LoginSource;
import com.openexchange.contacts.ldap.property.FolderProperties.Sorting;
import com.openexchange.groupware.contact.ContactException;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.java.Autoboxing;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.ArrayIterator;
import com.openexchange.tools.iterator.SearchIterator;


public class LdapContactInterface implements ContactInterface {

    private static final ArrayIterator<Contact> EMPTY_ARRAY_ITERATOR = new ArrayIterator<Contact>(new Contact[0]);

    public class SortInfo {

        private final int field;

        private final Order sort;
        
        /**
         * Initializes a new {@link SortInfo}.
         * @param field
         * @param sort
         */
        private SortInfo(final int field, final Order sort) {
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

    /**
     * This class is used to be able to view deleted object in an Active Directory
     *
     * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
     *
     */
    private class DeletedControl implements Control {

        /**
         * For serialization
         */
        private static final long serialVersionUID = -3548239536056697658L;

        public byte[] getEncodedValue() {
            return new byte[] {};
        }

        public String getID() {
            return "1.2.840.113556.1.4.417";
        }

        public boolean isCritical() {
            return true;
        }
    }


    private enum Order {
        asc,
        desc;
    }
    
    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(LdapContactInterface.class);
    
    private static final String MAPPING_TABLE_KEYS = "CONTACT_LDAP_MAPPING_TABLE_KEYS";

    private static final String MAPPING_TABLE_VALUES = "CONTACT_LDAP_MAPPING_TABLE_VALUES";
    
    private final int admin_id;
    
    private final int context;
    
    private final int folderid;
    
    private final FolderProperties folderprop;
    
    private Session session;
    
    public LdapContactInterface(final int context, final int admin_id, final FolderProperties folderprop, final int folderid) {
        this.context = context;
        this.admin_id = admin_id;
        this.folderprop = folderprop;
        this.folderid = folderid;
    }
    
    
    private static String escapeLDAPSearchFilter(final String ldapfilter) {
        // According to RFC2254 section 4 we escape the following chars so that no LDAP injection can be made:
        // Character       ASCII value
        // ---------------------------
        // *               0x2a
        // (               0x28
        // )               0x29
        // \               0x5c
        // NUL             0x00
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ldapfilter.length(); i++) {
            char curChar = ldapfilter.charAt(i);
            switch (curChar) {
                case '\\':
                    sb.append("\\5c");
                    break;
                case '*':
                    sb.append("\\2a");
                    break;
                case '(':
                    sb.append("\\28");
                    break;
                case ')':
                    sb.append("\\29");
                    break;
                case '\u0000': 
                    sb.append("\\00"); 
                    break;
                default:
                    sb.append(curChar);
            }
        }
        return sb.toString();
    }

    private static String joinValuesWithSeparator(final NamingEnumeration<?> all, final String separator) {
        final StringBuilder sb = new StringBuilder();
        while (all.hasMoreElements()) {
            sb.append(all.nextElement());
            sb.append(separator);
        }
        sb.delete(sb.length() - separator.length(), sb.length());
        return sb.toString();
    }


    public void deleteContactObject(final int oid, final int fuid, final Date client_date) throws OXObjectNotFoundException, OXConflictException, OXException {
        throw new LdapException(Code.DELETE_NOT_POSSIBLE);
    }

    public SearchIterator<Contact> getContactsByExtendedSearch(final ContactSearchObject searchobject, final int orderBy, final String orderDir, final int[] cols) throws OXException {
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
        final ArrayList<Contact> arrayList;

        final Mappings mappings = folderprop.getMappings();
        final ContactTypes contacttype = folderprop.getContacttypes();
        
        final boolean both = ContactTypes.both.equals(contacttype);
        if (searchobject.isStartLetter()) {
            String userfilter = null;
            String distrifilter = null;
            if (both || ContactTypes.users.equals(contacttype)) {
                userfilter = "(" + folderprop.getMappings().getSurname() + "=" + escapeLDAPSearchFilter(searchobject.getPattern()) + "*)";
            }
            if (both || ContactTypes.distributionlists.equals(contacttype)) {
                distrifilter = "(" + folderprop.getMappings().getDistributionlistname() + "=" + escapeLDAPSearchFilter(searchobject.getPattern()) + "*)";
            }
            arrayList = getLDAPContacts(folderId, columns, userfilter, distrifilter, null, false);
        } else {
            final StringBuilder user = new StringBuilder();
            final StringBuilder distri = new StringBuilder();
            if (both || ContactTypes.users.equals(contacttype)) {
                user.append("(|");
                addFilterFor(mappings.getDisplayname(), searchobject.getDisplayName(), user);
                addFilterFor(mappings.getGivenname(), searchobject.getGivenName(), user);
                addFilterFor(mappings.getSurname(), searchobject.getSurname(), user);
                addFilterFor(mappings.getEmail1(), searchobject.getEmail1(), user);
                addFilterFor(mappings.getEmail2(), searchobject.getEmail2(), user);
                addFilterFor(mappings.getEmail3(), searchobject.getEmail3(), user);
                user.append(")");
            }
            if (both || ContactTypes.distributionlists.equals(contacttype)) {
                addFilterFor(mappings.getDistributionlistname(), searchobject.getDisplayName(), distri);
            }
            arrayList = getLDAPContacts(folderId, columns, getStringFromStringBuilder(user), getStringFromStringBuilder(distri), null, false);
        }
        
        sorting(orderBy, orderDir, valueOf, arrayList);
        return new ArrayIterator<Contact>(arrayList.toArray(new Contact[arrayList.size()]));
    }


    // The all request...
    public SearchIterator<Contact> getContactsInFolder(final int folderId, final int from, final int to, final int orderBy, final String orderDir, final int[] cols) throws OXException {
        final Order valueOf = getOrder(orderDir);
        
        final Set<Integer> columns = getColumnSet(cols);
        if (0 == orderBy) {
            columns.add(Contact.SUR_NAME);
            columns.add(Contact.DISPLAY_NAME);
            columns.add(Contact.COMPANY);
            columns.add(Contact.EMAIL1);
            columns.add(Contact.EMAIL2);
        }
        final ArrayList<Contact> arrayList = getLDAPContacts(folderId, columns, null, null, null, false);
        
        // Get only the needed parts...
        final List<Contact> subList = getSubList(from, to, arrayList);
        
        sorting(orderBy, orderDir, valueOf, subList);
        final SearchIterator<Contact> searchIterator = new ArrayIterator<Contact>(subList.toArray(new Contact[subList.size()]));
        return searchIterator;
    }


    public SearchIterator<Contact> getDeletedContactsInFolder(final int folderId, final int[] cols, final Date since) throws OXException {
        if (folderprop.isOutlook_support() && folderprop.isAds_deletion_support()) {
            // Here we start to do some dirty tricks only possible with an AD which stores deleted objects in a special structure
            // this is done for a lifetime of 60 days for forests initially built using W2k and Server 2k3, and 180 days
            // for forests that were initially built with Server 2k3 SP1
            
            // TODO: Check the cols, because not all cols are available on deleted objects
            final Set<Integer> columns = getColumnSet(cols);
            final ArrayList<Contact> contacts = getLDAPContacts(folderId, columns, null, null, null, true);
            removeOlder(since, contacts);
            
            return new ArrayIterator<Contact>(contacts.toArray(new Contact[contacts.size()]));
        } else {
            return EMPTY_ARRAY_ITERATOR;
        }
    }

    public int getFolderId() {
        return folderid;
    }

    public LdapServer getLdapServer() {
        final LdapServer ldapServer = new LdapServer();
        ldapServer.setContext(String.valueOf(context));
        return ldapServer;
    }

    public SearchIterator<Contact> getModifiedContactsInFolder(final int folderId, final int[] cols, final Date since) throws OXException {
        if (folderprop.isOutlook_support()) {
            final Set<Integer> columns = getColumnSet(cols);
            final ArrayList<Contact> arrayList = getLDAPContacts(folderId, columns, null, null, null, false);
            removeOlder(since, arrayList);
            return new ArrayIterator<Contact>(arrayList.toArray(new Contact[arrayList.size()]));
        } else {
            return EMPTY_ARRAY_ITERATOR;
        }
    }


    private void removeOlder(final Date since, final List<Contact> list) {
        for (int i = 0; i < list.size(); i++) {
            final Contact obj = list.get(i);
            final Date lastModified = obj.getLastModified();
            if (null != lastModified && lastModified.before(since)) {
                list.remove(i--);
            }
        }
    }

    public int getNumberOfContacts(final int folderId) throws OXException {
        LOG.info("Called getNumberOfContacts");
        return 0;
    }

    public Contact getObjectById(final int objectId, final int inFolder) throws OXException {
        LOG.info("Called getObjectById");
        return null;
    }

    public SearchIterator<Contact> getObjectsById(final int[][] objectIdAndInFolder, final int[] cols) throws OXException {
        final Set<Integer> columns = getColumnSet(cols);
        final ArrayList<Contact> contacts = new ArrayList<Contact>();
        for (final int[] object : objectIdAndInFolder) {
            final int object_id = object[0];
            final int folder_id = object[1];
            
            String userfilter = null;
            String distrifilter = null;
            final boolean both = ContactTypes.both.equals(folderprop.getContacttypes());
            // Here we have to differentiate between users, distributionlists or both
            if (both || ContactTypes.users.equals(folderprop.getContacttypes())) {
                userfilter = "(" + folderprop.getMappings().getUniqueid() + "=" + oxUidToLdapUid(object_id) + ")";
            }
            if (both || ContactTypes.distributionlists.equals(folderprop.getContacttypes())) {
                distrifilter = "(" + folderprop.getMappings().getDistributionuid() + "=" + oxUidToLdapUid(object_id) + ")";
            }
            contacts.addAll(getLDAPContacts(folder_id, columns, userfilter, distrifilter, null, false));
        }
        return new ArrayIterator<Contact>(contacts.toArray(new Contact[contacts.size()]));
    }

    public Contact getUserById(final int userId) throws OXException {
        LOG.info("Called getUserById");
        return null;
    }

    public void insertContactObject(final Contact co) throws OXException {
        throw new LdapException(Code.INSERT_NOT_POSSIBLE);
    }

    public SearchIterator<Contact> searchContacts(final String searchpattern, final int folderId, final int orderBy, final String orderDir, final int[] cols) throws OXException {
        LOG.info("Called searchContacts");
        return null;
    }

    public void setSession(final Session s) throws OXException {
        session = s;
        initMappingTable();
    }
    
    public void updateContactObject(final Contact co, final int fid, final Date d) throws OXException, OXConcurrentModificationException, ContactException {
        LOG.info("Called updateContactObject");
    }

    private void addFilterFor(final String fieldname, final String searchString, final StringBuilder sb) {
        sb.append("(");
        sb.append(fieldname);
        sb.append("=*");
        sb.append(escapeLDAPSearchFilter(searchString));
        sb.append("*)");
    }

    private List<String> getAttributes(final Set<Integer> columns, boolean distributionlist) {
        final List<String> retval = new ArrayList<String>();
        for (final Integer col : columns) {
            final String fieldFromColumn = getFieldFromColumn(col, distributionlist);
            if (null != fieldFromColumn && fieldFromColumn.length() != 0) {
                retval.add(fieldFromColumn);
            }
        }
        return retval;
    }

    private Set<Integer> getColumnSet(final int[] cols) {
        final Set<Integer> columns = new HashSet<Integer>();
        for (final int col : cols) {
            columns.add(Autoboxing.I(col));
        }
        return columns;
    }

    /**
     * @param col
     * @param distributionlist TODO
     * @param instance
     * @return a String value or null if no corresponding entry was found
     */
    private String getFieldFromColumn(final Integer col, final boolean distributionlist) {
        final Mappings mappings = folderprop.getMappings();
        switch (col) {
        case Contact.ANNIVERSARY:
            return mappings.getAnniversary();
        case Contact.ASSISTANT_NAME:
            return mappings.getAssistant_name();
        case Contact.BIRTHDAY:
            return mappings.getBirthday();
        case Contact.BRANCHES:
            return mappings.getBranches();
        case Contact.BUSINESS_CATEGORY:
            return mappings.getBusiness_category();
        case Contact.CELLULAR_TELEPHONE1:
            return mappings.getCellular_telephone1();
        case Contact.CELLULAR_TELEPHONE2:
            return mappings.getCellular_telephone2();
        case Contact.CITY_BUSINESS:
            return mappings.getCity_business();
        case Contact.CITY_HOME:
            return mappings.getCity_home();
        case Contact.CITY_OTHER:
            return mappings.getCity_other();
        case Contact.COMMERCIAL_REGISTER:
            return mappings.getCommercial_register();
        case Contact.COMPANY:
            return mappings.getCompany();
        case Contact.COUNTRY_BUSINESS:
            return mappings.getCountry_business();
        case Contact.COUNTRY_HOME:
            return mappings.getCountry_home();
        case Contact.COUNTRY_OTHER:
            return mappings.getCountry_other();
        case Contact.DEFAULT_ADDRESS:
            return mappings.getDefaultaddress();
        case Contact.DEPARTMENT:
            return mappings.getDepartment();
        case Contact.DISPLAY_NAME:
            if (distributionlist) {
                return mappings.getDistributionlistname();
            } else {
                return mappings.getDisplayname();
            }
        case Contact.EMAIL1:
            return mappings.getEmail1();
        case Contact.EMAIL2:
            return mappings.getEmail2();
        case Contact.EMAIL3:
            return mappings.getEmail3();
        case Contact.EMPLOYEE_TYPE:
            return mappings.getEmployeetype();
        case Contact.FAX_BUSINESS:
            return mappings.getFax_business();
        case Contact.FAX_HOME:
            return mappings.getFax_home();
        case Contact.FAX_OTHER:
            return mappings.getFax_other();
        case Contact.GIVEN_NAME:
            return mappings.getGivenname();
        case Contact.INFO:
            return mappings.getInfo();
        case Contact.INSTANT_MESSENGER1:
            return mappings.getInstant_messenger1();
        case Contact.INSTANT_MESSENGER2:
            return mappings.getInstant_messenger2();
        case Contact.MANAGER_NAME:
            return mappings.getManager_name();
        case Contact.MARITAL_STATUS:
            return mappings.getMarital_status();
        case Contact.MIDDLE_NAME:
            return mappings.getMiddle_name();
        case Contact.NICKNAME:
            return mappings.getNickname();
        case Contact.NOTE:
            return mappings.getNote();
        case Contact.NUMBER_OF_CHILDREN:
            return mappings.getNumber_of_children();
        case Contact.NUMBER_OF_EMPLOYEE:
            return mappings.getNumber_of_employee();
        case DataObject.OBJECT_ID:
            if (distributionlist) {
                return mappings.getDistributionuid();
            } else {
                return mappings.getUniqueid();
            }
        case Contact.POSITION:
            return mappings.getPosition();
        case Contact.POSTAL_CODE_BUSINESS:
            return mappings.getPostal_code_business();
        case Contact.POSTAL_CODE_HOME:
            return mappings.getPostal_code_home();
        case Contact.POSTAL_CODE_OTHER:
            return mappings.getPostal_code_other();
        case Contact.PROFESSION:
            return mappings.getProfession();
        case Contact.ROOM_NUMBER:
            return mappings.getRoom_number();
        case Contact.SALES_VOLUME:
            return mappings.getSales_volume();
        case Contact.SPOUSE_NAME:
            return mappings.getSpouse_name();
        case Contact.STATE_BUSINESS:
            return mappings.getState_business();
        case Contact.STATE_HOME:
            return mappings.getState_home();
        case Contact.STATE_OTHER:
            return mappings.getState_other();
        case Contact.STREET_BUSINESS:
            return mappings.getStreet_business();
        case Contact.STREET_HOME:
            return mappings.getStreet_home();
        case Contact.STREET_OTHER:
            return mappings.getStreet_other();
        case Contact.SUFFIX:
            return mappings.getSuffix();
        case Contact.SUR_NAME:
            return mappings.getSurname();
        case Contact.TAX_ID:
            return mappings.getTax_id();
        case Contact.TELEPHONE_ASSISTANT:
            return mappings.getTelephone_assistant();
        case Contact.TELEPHONE_BUSINESS1:
            return mappings.getTelephone_business1();
        case Contact.TELEPHONE_BUSINESS2:
            return mappings.getTelephone_business2();
        case Contact.TELEPHONE_CALLBACK:
            return mappings.getTelephone_callback();
        case Contact.TELEPHONE_CAR:
            return mappings.getTelephone_car();
        case Contact.TELEPHONE_COMPANY:
            return mappings.getTelephone_company();
        case Contact.TELEPHONE_HOME1:
            return mappings.getTelephone_home1();
        case Contact.TELEPHONE_HOME2:
            return mappings.getTelephone_home2();
        case Contact.TELEPHONE_IP:
            return mappings.getTelephone_ip();
        case Contact.TELEPHONE_ISDN:
            return mappings.getTelephone_isdn();
        case Contact.TELEPHONE_OTHER:
            return mappings.getTelephone_other();
        case Contact.TELEPHONE_PAGER:
            return mappings.getTelephone_pager();
        case Contact.TELEPHONE_PRIMARY:
            return mappings.getTelephone_primary();
        case Contact.TELEPHONE_RADIO:
            return mappings.getTelephone_radio();
        case Contact.TELEPHONE_TELEX:
            return mappings.getTelephone_telex();
        case Contact.TELEPHONE_TTYTDD:
            return mappings.getTelephone_ttytdd();
        case Contact.TITLE:
            return mappings.getTitle();
        case Contact.URL:
            return mappings.getUrl();
        case Contact.USERFIELD01:
            return mappings.getUserfield01();
        case Contact.USERFIELD02:
            return mappings.getUserfield02();
        case Contact.USERFIELD03:
            return mappings.getUserfield03();
        case Contact.USERFIELD04:
            return mappings.getUserfield04();
        case Contact.USERFIELD05:
            return mappings.getUserfield05();
        case Contact.USERFIELD06:
            return mappings.getUserfield06();
        case Contact.USERFIELD07:
            return mappings.getUserfield07();
        case Contact.USERFIELD08:
            return mappings.getUserfield08();
        case Contact.USERFIELD09:
            return mappings.getUserfield09();
        case Contact.USERFIELD10:
            return mappings.getUserfield10();
        case Contact.USERFIELD11:
            return mappings.getUserfield11();
        case Contact.USERFIELD12:
            return mappings.getUserfield12();
        case Contact.USERFIELD13:
            return mappings.getUserfield13();
        case Contact.USERFIELD14:
            return mappings.getUserfield14();
        case Contact.USERFIELD15:
            return mappings.getUserfield15();
        case Contact.USERFIELD16:
            return mappings.getUserfield16();
        case Contact.USERFIELD17:
            return mappings.getUserfield17();
        case Contact.USERFIELD18:
            return mappings.getUserfield18();
        case Contact.USERFIELD19:
            return mappings.getUserfield19();
        case Contact.USERFIELD20:
            return mappings.getUserfield20();
        case DataObject.LAST_MODIFIED:
            return mappings.getLastmodified();
        case DataObject.CREATION_DATE:
            return mappings.getCreationdate();
        default:
            return null;
        }
    }


    @SuppressWarnings("unchecked")
    private Map<Integer, String> getKeyMappingTable() throws LdapException {
        final Object keys = this.session.getParameter(MAPPING_TABLE_KEYS);
        if (null == keys) {
            throw new LdapException(Code.NO_KEYS_MAPPING_TABLE_FOUND);
        }
        final Map<Integer, String> table = (Map<Integer, String>) keys;
        return table;
    }


    private ArrayList<Contact> getLDAPContacts(final int folderId, final Set<Integer> columns, final String usersearchfilter, final String distributionsearchfilter, final SortInfo sortField, boolean deleted) throws LdapException {
        final ArrayList<Contact> arrayList = new ArrayList<Contact>();
        final LdapContext context;
        try {
            context = LdapUtility.createContext(getLogin(), session.getPassword(), folderprop);
        } catch (final NamingException e1) {
            LOG.error(e1.getMessage(), e1);
            throw new LdapException(Code.INITIAL_LDAP_ERROR, e1.getMessage());
        }
            // TODO Implement right check if server supports pagedResults
//            final boolean pagedResultControlSupported = isPagedResultControlSupported(context);
//            if (!pagedResultControlSupported) {
//                System.out.println("Paged results are not supported");
//            }
        try {
            try {
                final boolean both = ContactTypes.both.equals(folderprop.getContacttypes());
                final boolean distributionlist = both || ContactTypes.distributionlists.equals(folderprop.getContacttypes());
                final int pagesize = folderprop.getPagesize();
                context.setRequestControls(getControls(sortField, distributionlist, deleted, pagesize));
                final String defaultNamingContext;
                if (deleted) {
                    defaultNamingContext = getDefaultNamingContext(context);
                } else {
                    defaultNamingContext = null;
                }
                if (both || ContactTypes.users.equals(folderprop.getContacttypes())) {
                    final String filter;
                    if (null != usersearchfilter) {
                        filter = "(&" + folderprop.getSearchfilter() + usersearchfilter + ")";
                    } else {
                        filter = folderprop.getSearchfilter();
                    }
                    searchAndFetch(false, folderId, columns, folderprop.getBaseDN(), filter, arrayList, context, pagesize, defaultNamingContext);
                }
                if (distributionlist) {
                    final String filter;
                    if (null != distributionsearchfilter) {
                        filter = "(&" + folderprop.getSearchfilterDistributionlist() + distributionsearchfilter + ")";
                    } else {
                        filter = folderprop.getSearchfilterDistributionlist();
                    }
                    searchAndFetch(true, folderId, columns, folderprop.getBaseDNDistributionlist(), filter, arrayList, context, pagesize, defaultNamingContext);
                }
            } catch (final IOException e) {
                LOG.error(e.getMessage(), e);
                throw new LdapException(Code.ERROR_GETTING_ATTRIBUTE, e.getMessage());
            } catch (final NamingException e) {
                LOG.error(e.getMessage(), e);
                throw new LdapException(Code.ERROR_GETTING_ATTRIBUTE, e.getMessage());
            } finally {
                context.close();
            }
        } catch (final NamingException e) {
            LOG.error(e.getMessage(), e);
            throw new LdapException(Code.ERROR_GETTING_ATTRIBUTE, e.getMessage());
        }
        return arrayList;
    }


    private Control[] getControls(final SortInfo sortField, final boolean distributionlist, boolean deleted, final int pagesize) throws IOException, NamingException {
        if (deleted) {
            return new Control[] { new PagedResultsControl(pagesize, Control.CRITICAL), new DeletedControl() };
        } else {
            if (null != sortField && folderprop.getSorting().equals(Sorting.server)) {
                final SortKey sortKey = new SortKey(getFieldFromColumn(sortField.getField(), distributionlist), sortField.getSort().equals(Order.asc), null);
                final SortKey[] sortKeyArray = new SortKey[] { sortKey };
                return new Control[] { new SortControl(sortKeyArray, Control.CRITICAL), new PagedResultsControl(pagesize, Control.CRITICAL) };
            } else {
                return new Control[] { new PagedResultsControl(pagesize, Control.CRITICAL) };
            }
        }
    }


    private LdapGetter getLdapGetter(final Attributes attributes, final LdapContext context, final String objectfullname) {
        return new LdapGetter() {
    
            public String getAttribute(final String attributename) throws LdapException {
                try {
                    final Attribute attribute = attributes.get(attributename);
                    if (null != attribute) {
                        if (1 < attribute.size()) {
                            final NamingEnumeration<?> all = attribute.getAll();
                            return joinValuesWithSeparator(all, ", ");
                        } else {
                            return (String) attribute.get();
                        }
                    } else {
                        return null;
                    }
                } catch (final NamingException e) {
                    throw new LdapException(Code.ERROR_GETTING_ATTRIBUTE, e.getMessage());
                }
            }

            public Date getDateAttribute(final String attributename) throws LdapException {
                try {
                    final Attribute attribute = attributes.get(attributename);
                    if (null != attribute) {
                        if (1 < attribute.size()) {
                            throw new LdapException(Code.MULTIVALUE_NOT_ALLOWED_DATE, attributename);
                        } else {
//                            final DirContext attributeDefinition = attribute.getAttributeDefinition();
//                            final Attributes attributes2 = attributeDefinition.getAttributes("");
//                            final Attribute syntaxattribute = attributes2.get("syntax");
//                            final String value = (String) syntaxattribute.get();
//                            if ("1.3.6.1.4.1.1466.115.121.1.24".equals(value)) {
//                                // We have a "Generalized Time syntax"
                                final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                                final Date date = simpleDateFormat.parse((String)attribute.get());
                                return date;
//                            } else {
//                                final DateFormat dateInstance = DateFormat.getDateInstance();
//                                return dateInstance.parse((String) attribute.get());
//                            }
                        }
                    } else {
                        return null;
                    }
                } catch (final ParseException e) {
                    throw new LdapException(Code.ERROR_GETTING_ATTRIBUTE, e, e.getMessage());
                } catch (final NamingException e) {
                    throw new LdapException(Code.ERROR_GETTING_ATTRIBUTE, e, e.getMessage());
                }
            }
            
            public int getIntAttribute(final String attributename) throws LdapException {
                try {
                    final Attribute attribute = attributes.get(attributename);
                    if (null != attribute) {
                        if (1 < attribute.size()) {
                            throw new LdapException(Code.MULTIVALUE_NOT_ALLOWED_INT, attributename);
                        } else {
                            return Integer.parseInt((String) attribute.get());
                        }
                    } else {
                        return -1;
                    }
                } catch (final NumberFormatException e) {
                    throw new LdapException(Code.ERROR_GETTING_ATTRIBUTE, "Attributename: " + attributename + " - " + e.getMessage());
                } catch (final NamingException e) {
                    throw new LdapException(Code.ERROR_GETTING_ATTRIBUTE, "Attributename: " + attributename + " - " + e.getMessage());
                }
            }
    
            public LdapGetter getLdapGetterForDN(final String dn) throws LdapException {
                try {
                    return getLdapGetter(context.getAttributes(dn), context, dn);
                } catch (final NamingException e) {
                    throw new LdapException(Code.ERROR_GETTING_ATTRIBUTE, "AttributeDN: " + dn + " - " + e.getMessage());
                }
            }

            public List<String> getMultiValueAttribute(final String attributename) throws LdapException {
                final List<String> retval = new ArrayList<String>();
                try {
                    final Attribute attribute = attributes.get(attributename);
                    if (null != attribute) {
                        if (1 < attribute.size()) {
                            final NamingEnumeration<?> all = attribute.getAll();
                            while (all.hasMoreElements()) {
                                retval.add((String)all.nextElement());
                            }
                        } else {
                            try {
                                retval.add((String)attribute.get());
                            } catch (final NoSuchElementException e) {
                                // We ignore this if the list has no member
                            }
                        }
                        return retval;
                    } else {
                        return retval;
                    }
                } catch (final NamingException e) {
                    throw new LdapException(Code.ERROR_GETTING_ATTRIBUTE, e.getMessage());
                }
            }

            public String getObjectFullName() throws LdapException {
                return objectfullname;
            }
            
        };
    }


    private String getLogin() throws LdapException {
        final User user = getUserObject();
        final LoginSource userLoginSource = folderprop.getUserLoginSource();
        switch (userLoginSource) {
        case login:
            final String imapLogin = user.getImapLogin();
            if (null == imapLogin) {
                throw new LdapException(Code.IMAP_LOGIN_NULL, user.getLoginInfo());
            } else {
                return imapLogin;
            }
        case mail:
            final String mail = user.getMail();
            if (null == mail) {
                throw new LdapException(Code.PRIMARY_MAIL_NULL, user.getLoginInfo());
            } else {
                return mail;
            }
        case name:
            return user.getLoginInfo();
        default:
            throw new LdapException(Code.GIVEN_USER_LOGIN_SOURCE_NOT_FOUND, userLoginSource);
        }
    }


    private Order getOrder(final String orderDir) {
        Order valueOf = null;
        if (null != orderDir) {
            final String lowerCase = orderDir.toLowerCase();
            valueOf = Order.valueOf(lowerCase);
        }
        return valueOf;
    }

    private SearchControls getSearchControl(final Set<Integer> columns) {
        final SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(LdapUtility.getSearchControl(folderprop.getSearchScope()));
        searchControls.setCountLimit(0);
        final List<String> array = getAttributes(columns, false);
        searchControls.setReturningAttributes(array.toArray(new String[array.size()]));
        return searchControls;
    }

    private SearchControls getSearchControlDistri(final Set<Integer> columns, boolean deleted) {
        final SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(LdapUtility.getSearchControl(folderprop.getSearchScopeDistributionlist()));
        searchControls.setCountLimit(0);
        final List<String> array = getAttributes(columns, true);
        if (!deleted) {
            array.add("member");
        }
        searchControls.setReturningAttributes(array.toArray(new String[array.size()]));
        return searchControls;
    }

//    /**
//     * Is paged result control supported?
//     *
//     * Query the rootDSE object to find out if the paged result control
//     * is supported.
//     */
//    private static boolean isPagedResultControlSupported(LdapContext ctx) throws NamingException
//    {
//        SearchControls ctl = new SearchControls();
//        ctl.setReturningAttributes(new String[]{"supportedControl"});
//        ctl.setSearchScope(SearchControls.OBJECT_SCOPE);
//
//        /* search for the rootDSE object */
//        NamingEnumeration<?> results = ctx.search("", "(objectClass=*)", ctl);
//
//        while(results.hasMore()){
//            SearchResult entry = (SearchResult)results.next();
//            NamingEnumeration<?> attrs = entry.getAttributes().getAll();
//            while (attrs.hasMore()){
//                Attribute attr = (Attribute)attrs.next();
//                NamingEnumeration<?> vals = attr.getAll();
//                while (vals.hasMore()){
//                    String value = (String) vals.next();
//                    if (value.equals("1.2.840.113556.1.4.319")) {
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
//    }
    
    private String getStringFromStringBuilder(final StringBuilder user) {
        return 0 == user.length() ? null : user.toString();
    }


    private List<Contact> getSubList(int from, int to, final ArrayList<Contact> arrayList) {
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


    private UidInterface getUidInterface() {
        return new UidInterface() {

            public Integer getUid(final String uid) throws LdapException {
                return ldapUidToOxUid(uid, getValuesMappingTable(), getKeyMappingTable());
            }

        };
    }

    private User getUserObject() throws LdapException {
        final User user;
        try {
            user = UserStorage.getStorageUser(session.getUserId(), ContextStorage.getStorageContext(session.getContextId()));
        } catch (final ContextException e) {
            throw new LdapException(Code.ERROR_GETTING_USER_Object, e);
        }
        return user;
    }


    @SuppressWarnings("unchecked")
    private Map<String, Integer> getValuesMappingTable() throws LdapException {
        final Object values = this.session.getParameter(MAPPING_TABLE_VALUES);
        if (null == values) {
            throw new LdapException(Code.NO_VALUES_MAPPING_TABLE_FOUND);
        }
        final Map<String, Integer> table = (Map<String, Integer>) values;
        return table;
    
    }


    private void initMappingTable() {
        // We only add the tables to the session if this is desired through the config file
        if (folderprop.isMemorymapping()) {
            final Object keys = this.session.getParameter(MAPPING_TABLE_KEYS);
            final Object values = this.session.getParameter(MAPPING_TABLE_VALUES);
            if (null == keys) {
                // Mapping table for this session was never initialized, so we do it here...
                this.session.setParameter(MAPPING_TABLE_KEYS, new ConcurrentHashMap<Integer, String>());
            }
            if (null == values) {
                // Mapping table for this session was never initialized, so we do it here...
                this.session.setParameter(MAPPING_TABLE_VALUES, new ConcurrentHashMap<String, Integer>());
            }
        }
    }


    private Integer ldapUidToOxUid(final String uid, final Map<String, Integer> values, final Map<Integer, String> keys) throws LdapException {
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
    
    private String oxUidToLdapUid(final int uid) throws LdapException {
        if (folderprop.isMemorymapping()) {
            final Map<Integer, String> keys = getKeyMappingTable();
            final String ldapUid = keys.get(Autoboxing.I(uid));
            if (null != ldapUid) {
                return ldapUid;
            } else {
                throw new LdapException(Code.NO_SUCH_LONG_UID_IN_MAPPING_TABLE_FOUND, String.valueOf(uid));
            }
        } else {
            return String.valueOf(uid);
        }
    }

    private void searchAndFetch(final boolean distributionslist, final int folderId, final Set<Integer> columns, final String baseDN, final String filter, final ArrayList<Contact> arrayList, final LdapContext context, final int pagesize, String defaultNamingContext) throws NamingException, LdapException, IOException {
        final SearchControls searchControls;
        final boolean deleted = (null != defaultNamingContext);
        if (distributionslist) {
            searchControls = getSearchControlDistri(columns, deleted);
        } else {
            searchControls = getSearchControl(columns);
        }
        final String ownBaseDN;
        final String ownFilter;
        if (deleted) {
            ownBaseDN = defaultNamingContext;
            ownFilter = "(&" + filter + "(isDeleted=TRUE))";
        } else {
            ownBaseDN = baseDN;
            ownFilter = filter;
        }
        byte[] cookie = null;
        do {
            final NamingEnumeration<SearchResult> search = context.search(ownBaseDN, ownFilter, searchControls);
            while (null != search && search.hasMoreElements()) {
                final SearchResult next = search.next();
                final Attributes attributes = next.getAttributes();
                final LdapGetter ldapGetter = getLdapGetter(attributes, context, next.getNameInNamespace());
                if (distributionslist) {
                    final Contact retval = Mapper.getDistriContact(ldapGetter, columns, this.folderprop, getUidInterface(), folderId, this.admin_id);
                    arrayList.add(retval);
                } else {
                    final Contact contact = Mapper.getContact(ldapGetter, columns, folderprop, getUidInterface(), folderId, this.admin_id);
                    arrayList.add(contact);
                }
            }
            // Examine the paged results control response 
            final Control[] controls = context.getResponseControls();
            if (controls != null) {
                for (int i = 0; i < controls.length; i++) {
                    if (controls[i] instanceof PagedResultsResponseControl) {
                        final PagedResultsResponseControl prrc = (PagedResultsResponseControl) controls[i];
                        cookie = prrc.getCookie();
                    }
                }
            }
            // Re-activate paged results
            context.setRequestControls(new Control[] { new PagedResultsControl(pagesize, cookie, Control.CRITICAL) });
        } while (null != cookie);
    }

    private static String getDefaultNamingContext(final LdapContext ctx) throws LdapException {
        try {
            SearchControls searchCtls2 = new SearchControls();
            searchCtls2.setSearchScope(SearchControls.OBJECT_SCOPE);
            searchCtls2.setReturningAttributes(new String[]{"defaultnamingcontext"});
            final NamingEnumeration<SearchResult> search = ctx.search("", "(objectclass=*)", searchCtls2);
            while (search.hasMoreElements()) {
                SearchResult sr = (SearchResult)search.next();
                Attributes attrs = sr.getAttributes();
                if (attrs != null) {
                    for (NamingEnumeration<?> ae = attrs.getAll();ae.hasMoreElements();) {
                        Attribute attr = (Attribute)ae.next();
                        if (null != attr) {
                            for (NamingEnumeration<?> e = attr.getAll(); e.hasMore();) {
                                final Object next = e.next();
                                if (null != next) {
                                    return next.toString();
                                }
                            }
                        }
                    }
                } 
            }
            throw new LdapException(Code.ERROR_GETTING_DEFAULT_NAMING_CONTEXT);
        } catch (final NamingException e) {
            throw new LdapException(Code.ERROR_GETTING_DEFAULT_NAMING_CONTEXT, e);
        }
    }


    private void sorting(final int orderBy, final String orderDir, final Order valueOf, final List<Contact> subList) {
        if (null != orderDir && folderprop.getSorting().equals(Sorting.groupware)) {
            Collections.sort(subList, new ContactComparator(orderBy));
        } else {
            // Default sorting
            Collections.sort(subList, new ContactComparator(-1));
        }
    }
}
