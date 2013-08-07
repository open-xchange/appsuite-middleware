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

package com.openexchange.contacts.ldap.contacts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.contact.LdapServer;
import com.openexchange.contacts.ldap.exceptions.LdapExceptionCode;
import com.openexchange.contacts.ldap.ldap.LdapGetter;
import com.openexchange.contacts.ldap.ldap.LdapInterface;
import com.openexchange.contacts.ldap.ldap.LdapInterface.FillClosure;
import com.openexchange.contacts.ldap.ldap.LdapJNDIImpl;
import com.openexchange.contacts.ldap.osgi.LDAPServiceRegistry;
import com.openexchange.contacts.ldap.property.FolderProperties;
import com.openexchange.contacts.ldap.property.FolderProperties.ContactTypes;
import com.openexchange.contacts.ldap.property.FolderProperties.LoginSource;
import com.openexchange.contacts.ldap.property.FolderProperties.Sorting;
import com.openexchange.contacts.ldap.property.Mappings;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.contact.ContactUnificationState;
import com.openexchange.groupware.contact.helpers.SpecialAlphanumSortContactComparator;
import com.openexchange.groupware.contact.helpers.UseCountComparator;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.java.Autoboxing;
import com.openexchange.search.SearchTerm;
import com.openexchange.session.Session;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.iterator.ArrayIterator;
import com.openexchange.tools.iterator.SearchIterator;


/**
 * {@link LdapContactInterface}
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class LdapContactInterface implements ContactInterface {

    protected static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(LdapContactInterface.class));

    private class ContactLoaderTask implements Runnable {

        private final LdapContactInterfaceProvider _contactIFace;

        private final Set<Integer> columns;

        private final int folderId;

        public ContactLoaderTask(final LdapContactInterfaceProvider contactIFace, final int folderId, final Set<Integer> columns) {
            super();
            this._contactIFace = contactIFace;
            this.folderId = folderId;
            this.columns = columns;
        }

        @Override
        public void run() {
            try {
                final List<Contact> ldapContacts = getLDAPContacts(folderId, columns, null, null, null, false);
                this._contactIFace.rwlock_cached_contacts.writeLock().lock();
                try {
                    this._contactIFace.cached_contacts = ldapContacts;
                } finally {
                    this._contactIFace.rwlock_cached_contacts.writeLock().unlock();
                }
            } catch (final OXException e) {
                LOG.error(e.getMessage(), e);
            }

        }

    }

    private static final ArrayIterator<Contact> EMPTY_ARRAY_ITERATOR = new ArrayIterator<Contact>(new Contact[0]);

    private static final String MAPPING_TABLE_KEYS = "CONTACT_LDAP_MAPPING_TABLE_KEYS";

    private static final String MAPPING_TABLE_VALUES = "CONTACT_LDAP_MAPPING_TABLE_VALUES";

    private final int admin_id;

    private final String[] attributes;

    private final int context;

    private final int folderid;

    private final FolderProperties folderprop;

    private final LdapContactInterfaceProvider contactIFace;

    private Session session;

    private Locale locale;

    public LdapContactInterface(final int context, final int admin_id, final FolderProperties folderprop, final int folderid, final LdapContactInterfaceProvider contactIFace) {
        this.context = context;
        this.admin_id = admin_id;
        this.folderprop = folderprop;
        this.folderid = folderid;
        this.contactIFace = contactIFace;
        this.attributes = getDistriAttributes(folderprop);
    }

    private Locale getLocale() {
        if (null == locale) {
            locale = UserStorage.getStorageUser(session.getUserId(), session.getContextId()).getLocale();
        }
        return locale;
    }

    public static String escapeLDAPSearchFilter(final String ldapfilter) {
        // According to RFC2254 section 4 we escape the following chars so that no LDAP injection can be made:
        // Character       ASCII value
        // ---------------------------
        // *               0x2a
        // (               0x28
        // )               0x29
        // \               0x5c
        // NUL             0x00
    	if(ldapfilter == null) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ldapfilter.length(); i++) {
            final char curChar = ldapfilter.charAt(i);
            switch (curChar) {
                case '\\':
                    sb.append("\\5c");
                    break;
// We always treat "*" as wildcard
//                case '*':
//                    sb.append("\\2a");
//                    break;
                case '(':
                    sb.append("\\28");
                    break;
                case ')':
                    sb.append("\\29");
                    break;
                case '\u0000':
                    sb.append("\\00");
                    break;
                case '?':
                    sb.append('*');
                    break;
                default:
                    sb.append(curChar);
            }
        }
        return sb.toString();
    }

//    private static String joinValuesWithSeparator(final NamingEnumeration<?> all, final String separator) {
//        final StringBuilder sb = new StringBuilder();
//        while (all.hasMoreElements()) {
//            sb.append(all.nextElement());
//            sb.append(separator);
//        }
//        sb.delete(sb.length() - separator.length(), sb.length());
//        return sb.toString();
//    }


    @Override
    public void deleteContactObject(final int oid, final int fuid, final Date client_date) throws OXException {
        throw LdapExceptionCode.DELETE_NOT_POSSIBLE.create();
    }

    @Override
    public SearchIterator<Contact> getContactsByExtendedSearch(final ContactSearchObject searchobject, final int orderBy, final Order order, final String collation, final int[] cols) throws OXException {
        final Set<Integer> columns = getColumnSet(cols);
        final int folderId;
        {
            final int[] folders = searchobject.getFolders();
            if (null == folders) {
                throw LdapExceptionCode.FOLDERID_OBJECT_NULL.create();
            }
            if (folders.length != 1) {
                throw LdapExceptionCode.TOO_MANY_FOLDERS.create();
            }
            folderId = folders[0];
        }
        final ArrayList<Contact> arrayList;

        final Mappings mappings = folderprop.getMappings();
        final ContactTypes contacttype = folderprop.getContacttypes();

        final boolean specialSort;
        if (orderBy > 0 && orderBy != Contact.SPECIAL_SORTING && orderBy != Contact.USE_COUNT_GLOBAL_FIRST && !Order.NO_ORDER.equals(order)) {
            specialSort = false;
        } else {
            for (final int field : new int[] {
                Contact.YOMI_LAST_NAME, Contact.SUR_NAME, Contact.YOMI_FIRST_NAME, Contact.GIVEN_NAME, Contact.DISPLAY_NAME,
                Contact.YOMI_COMPANY, Contact.COMPANY, Contact.EMAIL1, Contact.EMAIL2, Contact.USE_COUNT }) {
                columns.add(Integer.valueOf(field));
            }
            specialSort = true;
        }

        final boolean both = ContactTypes.both.equals(contacttype);
        if (searchobject.isStartLetter()) {
            String userfilter = null;
            String distrifilter = null;
            if (both || ContactTypes.users.equals(contacttype)) {
                final StringBuilder sb = new StringBuilder();
                addFilterFor(mappings.getSurname(), searchobject.getPattern(), sb);
                userfilter = sb.toString();
            }
            if (both || ContactTypes.distributionlists.equals(contacttype)) {
                final StringBuilder sb = new StringBuilder();
                addFilterFor(mappings.getDistributionlistname(), searchobject.getPattern(), sb);
                distrifilter = sb.toString();
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

        sorting(orderBy, order, arrayList, specialSort);
        return new ArrayIterator<Contact>(arrayList.toArray(new Contact[arrayList.size()]));
    }


    // The all request...
    @Override
    public SearchIterator<Contact> getContactsInFolder(final int folderId, final int from, final int to, final int orderBy, final Order order, final String collation, final int[] cols) throws OXException {

        final Set<Integer> columns = getColumnSet(cols);
        if (0 == orderBy) {
            columns.add(Integer.valueOf(Contact.SUR_NAME));
            columns.add(Integer.valueOf(Contact.DISPLAY_NAME));
            columns.add(Integer.valueOf(Contact.COMPANY));
            columns.add(Integer.valueOf(Contact.EMAIL1));
            columns.add(Integer.valueOf(Contact.EMAIL2));
        }
        if (0 != orderBy) {
            columns.addAll(getColumnSet(new int[]{orderBy}));
        }

        final boolean specialSort;
        if (orderBy > 0 && orderBy != Contact.SPECIAL_SORTING && orderBy != Contact.USE_COUNT_GLOBAL_FIRST && !Order.NO_ORDER.equals(order)) {
            specialSort = false;
        } else {
            for (final int field : new int[] {
                Contact.YOMI_LAST_NAME, Contact.SUR_NAME, Contact.YOMI_FIRST_NAME, Contact.GIVEN_NAME, Contact.DISPLAY_NAME,
                Contact.YOMI_COMPANY, Contact.COMPANY, Contact.EMAIL1, Contact.EMAIL2, Contact.USE_COUNT }) {
                columns.add(Integer.valueOf(field));
            }
            specialSort = true;
        }

        final List<Contact> arrayList;
        // If a AdminDN is used, all users see the same contacts, so we can cache them...
        if (contentTheSameForAll()) {
            this.contactIFace.rwlock_cached_contacts.writeLock().lock();
            if (null == this.contactIFace.cached_contacts) {
                try {
                    // We have to load all columns before caching the contacts to satisfy any further requests
                    int[] allColumns = Contact.ALL_COLUMNS;
                    for (int column : allColumns) {
                        columns.add(column);
                    }
                    arrayList = getLDAPContacts(folderId, columns, null, null, new SortInfo(Contact.SUR_NAME, Order.ASCENDING), false);
                    this.contactIFace.cached_contacts = Collections.synchronizedList(new ArrayList<Contact>());
                    this.contactIFace.cached_contacts.addAll(arrayList);
                } finally {
                    this.contactIFace.rwlock_cached_contacts.writeLock().unlock();
                }
                // Start thread
                LDAPServiceRegistry.getInstance().getService(TimerService.class).scheduleWithFixedDelay(new ContactLoaderTask(this.contactIFace, folderId, columns), folderprop.getRefreshinterval(), folderprop.getRefreshinterval());
            } else {
                try {
                    try {
                        this.contactIFace.rwlock_cached_contacts.readLock().lock();
                    } finally {
                        this.contactIFace.rwlock_cached_contacts.writeLock().unlock();
                    }
                    arrayList = this.contactIFace.cached_contacts;
                } finally {
                    this.contactIFace.rwlock_cached_contacts.readLock().unlock();
                }
            }
        } else {
            arrayList = getLDAPContacts(folderId, columns, null, null, null, false);

        }

        // Get only the needed parts...
        final List<Contact> subList = from != 0 || to != 0 ? getSubList(from, to, arrayList) : arrayList;

        sorting(orderBy, order, subList, specialSort);
        final SearchIterator<Contact> searchIterator = new ArrayIterator<Contact>(subList.toArray(new Contact[subList.size()]));
        return searchIterator;
    }


    @Override
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

    @Override
    public int getFolderId() {
        return folderid;
    }

    @Override
    public LdapServer getLdapServer() {
        final LdapServer ldapServer = new LdapServer();
        ldapServer.setContext(String.valueOf(context));
        return ldapServer;
    }

    @Override
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


    @Override
    public int getNumberOfContacts(final int folderId) throws OXException {
        LOG.info("Called getNumberOfContacts");
        return 0;
    }

    @Override
    public Contact getObjectById(final int objectId, final int inFolder) throws OXException {
        final SearchIterator<Contact> objectsById = getObjectsById(new int[][]{new int[]{objectId, inFolder}}, Contact.ALL_COLUMNS);
        try {
            if (null != objectsById && objectsById.hasNext()) {
                return objectsById.next();
            } else {
                throw LdapExceptionCode.CONTACT_NOT_FOUND.create(objectId, inFolder);
            }
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
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

    @Override
    public Contact getUserById(final int userId, final boolean performReadCheck) throws OXException {
        LOG.info("Called getUserById");
        return null;
    }

    @Override
    public Contact[] getUsersById(final int[] userIds, final boolean performReadCheck) throws OXException {
        LOG.info("Called getUsersById");
        return null;
    }

    @Override
    public Contact getUserById(final int userId) throws OXException {
        LOG.info("Called getUserById");
        return null;
    }

    @Override
    public void insertContactObject(final Contact co) throws OXException {
        throw LdapExceptionCode.INSERT_NOT_POSSIBLE.create();
    }

    @Override
    public SearchIterator<Contact> searchContacts(final String searchpattern, final int folderId, final int orderBy, final Order order, final int[] cols) throws OXException {
        LOG.info("Called searchContacts");
        return null;
    }

    public void setSession(final Session s) throws OXException {
        session = s;
        initMappingTable();
    }

    @Override
    public void updateContactObject(final Contact co, final int fid, final Date d) throws OXException {
        LOG.info("Called updateContactObject");
    }

    @Override
    public void updateUserContact(final Contact contact, final Date lastmodified) throws OXException {
        LOG.info("Called updateUserContact");
    }

    private void addFilterFor(final String fieldname, final String searchString, final StringBuilder sb) {
        if (null != searchString && null != fieldname) {
            if ("*".equals(searchString)) {
                sb.append("(");
                sb.append(fieldname);
                sb.append("=*)");
            } else {
                sb.append("(");
                sb.append(fieldname);
                sb.append("=");
                sb.append(escapeLDAPSearchFilter(searchString));
                sb.append("*)");
            }
        }
    }

    private boolean contentTheSameForAll() {
        return FolderProperties.AuthType.AdminDN.equals(folderprop.getAuthtype()) || FolderProperties.AuthType.anonymous.equals(folderprop.getAuthtype());
    }


    private Set<Integer> getColumnSet(final int[] cols) {
        final Set<Integer> columns = new HashSet<Integer>();
        for (final int col : cols) {
            columns.add(Autoboxing.I(col));
        }
        columns.add(Integer.valueOf(Contact.CREATION_DATE));
        return columns;
    }

    // If changes are made to this method the method com.openexchange.contacts.ldap.contacts.Mapper.getDistriEntry(LdapGetter, int, Mappings)
    // should also be checked for changes to be made
    private String[] getDistriAttributes(final FolderProperties folderprop) {
        final Mappings mappings = folderprop.getMappings();
        final List<String> attr = new ArrayList<String>();
        final String displayname = mappings.getDisplayname();
        if (null != displayname && 0 != displayname.length()) {
            attr.add(displayname);
        }
        final String email1 = mappings.getEmail1();
        if (null != email1 && 0 != email1.length()) {
            attr.add(email1);
        }
        final String givenname = mappings.getGivenname();
        if (null != givenname && 0 != givenname.length()) {
            attr.add(givenname);
        }
        final String surname = mappings.getSurname();
        if (null != surname && 0 != surname.length()) {
            attr.add(surname);
        }
        return attr.toArray(new String[attr.size()]);
    }


    @SuppressWarnings("unchecked")
    private Map<Integer, String> getKeyMappingTable() throws OXException {
        if (contentTheSameForAll()) {
            return this.contactIFace.keytable;
        } else {
            final Object keys = this.session.getParameter(MAPPING_TABLE_KEYS);
            if (null == keys) {
                throw LdapExceptionCode.NO_KEYS_MAPPING_TABLE_FOUND.create();
            }
            final Map<Integer, String> table = (Map<Integer, String>) keys;
            return table;
        }
    }


    ArrayList<Contact> getLDAPContacts(final int folderId, final Set<Integer> columns, final String usersearchfilter, final String distributionsearchfilter, final SortInfo sortField, final boolean deleted) throws OXException {
        final ArrayList<Contact> arrayList = new ArrayList<Contact>();
        final boolean both = ContactTypes.both.equals(folderprop.getContacttypes());
        final boolean distributionlist = both || ContactTypes.distributionlists.equals(folderprop.getContacttypes());
        final LdapInterface iface = new LdapJNDIImpl(getLogin(), session.getPassword(), folderprop, deleted, distributionlist, sortField);

        try {
            if (both || ContactTypes.users.equals(folderprop.getContacttypes())) {
                final String filter;
                if (null != usersearchfilter) {
                    filter = "(&" + folderprop.getSearchfilter() + usersearchfilter + ")";
                } else {
                    filter = folderprop.getSearchfilter();
                }
                searchAndFetch(false, folderId, columns, folderprop.getBaseDN(), filter, arrayList, iface);
            }
            if (distributionlist) {
                final String filter;
                if (null != distributionsearchfilter) {
                    filter = "(&" + folderprop.getSearchfilterDistributionlist() + distributionsearchfilter + ")";
                } else {
                    filter = folderprop.getSearchfilterDistributionlist();
                }
                searchAndFetch(true, folderId, columns, folderprop.getBaseDNDistributionlist(), filter, arrayList, iface);
            }
        } finally {
            iface.close();
        }
        return arrayList;
    }


    private String getLogin() throws OXException {
        final User user = getUserObject();
        final LoginSource userLoginSource = folderprop.getUserLoginSource();
        switch (userLoginSource) {
        case login:
            final String imapLogin = user.getImapLogin();
            if (null == imapLogin) {
                throw LdapExceptionCode.IMAP_LOGIN_NULL.create(user.getLoginInfo());
            } else {
                return imapLogin;
            }
        case mail:
            final String mail = user.getMail();
            if (null == mail) {
                throw LdapExceptionCode.PRIMARY_MAIL_NULL.create( user.getLoginInfo());
            } else {
                return mail;
            }
        case name:
            return user.getLoginInfo();
        default:
            throw LdapExceptionCode.GIVEN_USER_LOGIN_SOURCE_NOT_FOUND.create(userLoginSource);
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

    private String getStringFromStringBuilder(final StringBuilder user) {
        return 0 == user.length() ? null : user.toString();
    }


    private List<Contact> getSubList(int from, int to, final List<Contact> arrayList) {
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

            @Override
            public Integer getUid(final String uid) throws OXException {
                return ldapUidToOxUid(uid, getValuesMappingTable(), getKeyMappingTable());
            }

            private Integer ldapUidToOxUid(final String uid, final Map<String, Integer> values, final Map<Integer, String> keys) throws OXException {
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

        };
    }

    private User getUserObject() throws OXException {
        final User user;
        try {
            user = UserStorage.getStorageUser(session.getUserId(), ContextStorage.getStorageContext(session.getContextId()));
        } catch (final OXException e) {
            throw LdapExceptionCode.ERROR_GETTING_USER_Object.create(e);
        }
        return user;
    }


    @SuppressWarnings("unchecked")
    private Map<String, Integer> getValuesMappingTable() throws OXException {
        if (contentTheSameForAll()) {
            return this.contactIFace.valuetable;
        } else {
            final Object values = this.session.getParameter(MAPPING_TABLE_VALUES);
            if (null == values) {
                throw LdapExceptionCode.NO_VALUES_MAPPING_TABLE_FOUND.create();
            }
            final Map<String, Integer> table = (Map<String, Integer>) values;
            return table;
        }
    }


    private void initMappingTable() {
        // We only need the mapping tables if this is desire through the config file
        if (folderprop.isMemorymapping()) {
            if (contentTheSameForAll()) {
                if (null == this.contactIFace.keytable) {
                    synchronized (this) {
                        if (null == this.contactIFace.keytable) {
                            this.contactIFace.keytable = new ConcurrentHashMap<Integer, String>();
                        }
                    }
                }
                if (null == this.contactIFace.valuetable) {
                    synchronized (this) {
                        if (null == this.contactIFace.valuetable) {
                            this.contactIFace.valuetable = new ConcurrentHashMap<String, Integer>();
                        }
                    }
                }
            } else {
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
    }


    private String oxUidToLdapUid(final int uid) throws OXException {
        if (folderprop.isMemorymapping()) {
            final Map<Integer, String> keys = getKeyMappingTable();
            final String ldapUid = keys.get(Autoboxing.I(uid));
            if (null != ldapUid) {
                return ldapUid;
            } else {
                throw LdapExceptionCode.NO_SUCH_LONG_UID_IN_MAPPING_TABLE_FOUND.create(String.valueOf(uid));
            }
        } else {
            return String.valueOf(uid);
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


    private void searchAndFetch(final boolean distributionslist, final int folderId, final Set<Integer> columns, final String baseDN, final String filter, final ArrayList<Contact> arrayList, final LdapInterface iface) throws OXException {
        iface.search(baseDN, filter, distributionslist, columns, new FillClosure() {

            @Override
            public void execute(final LdapGetter ldapGetter) throws OXException {
                if (distributionslist) {
                    final Contact retval = Mapper.getDistriContact(ldapGetter, columns, folderprop, getUidInterface(), folderId, admin_id, attributes);
                    arrayList.add(retval);
                } else {
                    final Contact contact = Mapper.getContact(ldapGetter, columns, folderprop, getUidInterface(), folderId, admin_id);
                    arrayList.add(contact);
                }
            }

        });

    }

    private void sorting(final int orderBy, final Order order, final List<Contact> subList, final boolean specialSort) {
        if (Order.NO_ORDER != order && folderprop.getSorting().equals(Sorting.groupware)) {
            if (orderBy == Contact.USE_COUNT_GLOBAL_FIRST) {
                java.util.Collections.sort(subList, new UseCountComparator(specialSort, getLocale()));
                if (Order.DESCENDING.equals(order)) {
                    java.util.Collections.reverse(subList);
                }
            } else if (specialSort) {
                java.util.Collections.sort(subList, new SpecialAlphanumSortContactComparator(getLocale()));
                if (Order.DESCENDING.equals(order)) {
                    java.util.Collections.reverse(subList);
                }
            } else {
                Collections.sort(subList, new ContactComparator(orderBy, order));
            }
        } else {
            // Default sorting
            Collections.sort(subList, new ContactComparator(-1, order));
        }
    }

    public void associateTwoContacts(final Contact master, final Contact slave) throws OXException {
        throw new UnsupportedOperationException();
    }

    public List<Contact> getAssociatedContacts(final Contact contact) throws OXException {
        throw new UnsupportedOperationException();
    }

    public ContactUnificationState getAssociationBetween(final Contact c1, final Contact c2) throws OXException {
        throw new UnsupportedOperationException();
    }

    public Contact getContactByUUID(final String uuid) throws OXException {
        throw new UnsupportedOperationException();
    }

    public void separateTwoContacts(final Contact master, final Contact slave) throws OXException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> SearchIterator<Contact> getContactsByExtendedSearch(final SearchTerm<T> searchterm, final int orderBy, final Order order, final String collation, final int[] cols) throws OXException {
        final Mappings mappings = folderprop.getMappings();
        final ContactTypes contacttype = folderprop.getContacttypes();
        final boolean both = ContactTypes.both.equals(contacttype);
        final boolean distributionlistsActive = both || ContactTypes.distributionlists.equals(contacttype);

        final ContactSearchtermLdapConverter parser = new ContactSearchtermLdapConverter();
        parser.setMappings(mappings);
        parser.setDistributionlistActive(distributionlistsActive);
        parser.parse(searchterm);

        final Set<Integer> columns = getColumnSet(cols);
        final int folderId;
        {
            final List<String> folders = parser.getFolders();
            if (null == folders || 0 == folders.size()) {
                /*
                 * fall back to default folder ID
                 */
                folderId = this.folderid;
            } else if (1 == folders.size()) {
                /*
                 * use folder parsed folder ID
                 */
                folderId = Integer.parseInt(folders.get(0));
            } else {
                throw LdapExceptionCode.TOO_MANY_FOLDERS.create();
            }
        }
        final ArrayList<Contact> arrayList;

        final boolean specialSort;
        if (orderBy > 0 && orderBy != Contact.SPECIAL_SORTING && orderBy != Contact.USE_COUNT_GLOBAL_FIRST && !Order.NO_ORDER.equals(order)) {
            specialSort = false;
        } else {
            for (final int field : new int[] {
                Contact.YOMI_LAST_NAME, Contact.SUR_NAME, Contact.YOMI_FIRST_NAME, Contact.GIVEN_NAME, Contact.DISPLAY_NAME,
                Contact.YOMI_COMPANY, Contact.COMPANY, Contact.EMAIL1, Contact.EMAIL2, Contact.USE_COUNT }) {
                columns.add(Integer.valueOf(field));
            }
            specialSort = true;
        }

        String users = null, distributions = null;
        if (both || ContactTypes.users.equals(contacttype)) {
            users = parser.getQueryString();
        }
        if (distributionlistsActive) {
            distributions = parser.getQueryString();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Using following filters for users: \"" + users + "\" and distributionlists: \"" + distributions + "\"");
        }
        arrayList = getLDAPContacts(folderId, columns, users, distributions, null, false);

        sorting(orderBy, order, arrayList, specialSort);
        return new ArrayIterator<Contact>(arrayList.toArray(new Contact[arrayList.size()]));

    }
}
