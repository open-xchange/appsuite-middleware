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

package com.openexchange.contacts.ldap.ldap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;
import javax.naming.ldap.SortControl;
import javax.naming.ldap.SortKey;

import com.openexchange.contacts.ldap.contacts.SortInfo;
import com.openexchange.contacts.ldap.exceptions.LdapExceptionCode;
import com.openexchange.contacts.ldap.property.FolderProperties;
import com.openexchange.contacts.ldap.property.FolderProperties.DerefAliases;
import com.openexchange.contacts.ldap.property.FolderProperties.SearchScope;
import com.openexchange.contacts.ldap.property.FolderProperties.Sorting;
import com.openexchange.contacts.ldap.property.Mappings;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.search.Order;


/**
 * An implementation of the {@link LdapInterface} which used JNDI to
 * communicate with the LDAP server
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public class LdapJNDIImpl implements LdapInterface {

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
        @Override
        public byte[] getEncodedValue() {
            return new byte[] {};
        }

        @Override
        public String getID() {
            return "1.2.840.113556.1.4.417";
        }

        @Override
        public boolean isCritical() {
            return true;
        }
    }

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(LdapJNDIImpl.class));

    private final FolderProperties folderprop;

    private final LdapContext context;

    private final boolean deleted;

    private static Map<String, String> MAPPINGTABLE_USERNAME_LDAPBIND = new ConcurrentHashMap<String, String>();

    public LdapJNDIImpl(final String login, final String password, final FolderProperties folderprop, final boolean deleted, final boolean distributionlist, final SortInfo sortField) throws OXException {
        this.folderprop = folderprop;
        this.deleted = deleted;
        try {
            this.context = createContext(login, password);
        } catch (final NamingException e1) {
            LOG.error(e1.getMessage(), e1);
            throw LdapExceptionCode.INITIAL_LDAP_ERROR.create(e1.getMessage());
        }
        // TODO Implement right check if server supports pagedResults
//      final boolean pagedResultControlSupported = isPagedResultControlSupported(context);
//      if (!pagedResultControlSupported) {
//          System.out.println("Paged results are not supported");
//      }

        try {
            this.context.setRequestControls(getControls(sortField, distributionlist, folderprop.getPagesize()));
        } catch (final NamingException e) {
            LOG.error(e.getMessage(), e);
            throw LdapExceptionCode.ERROR_GETTING_ATTRIBUTE.create(e.getMessage());
        } catch (final IOException e) {
            LOG.error(e.getMessage(), e);
            throw LdapExceptionCode.ERROR_GETTING_ATTRIBUTE.create(e.getMessage());
        }

    }

    @Override
    public void search(final String baseDN, final String filter, final boolean distributionslist, final Set<Integer> columns, final FillClosure closure) throws OXException {
        final String defaultNamingContext;
        if (deleted) {
            defaultNamingContext = getDefaultNamingContext(context);
        } else {
            defaultNamingContext = null;
        }

        final SearchControls searchControls;
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

        try {
            byte[] cookie = null;
            // Attention: It is important that we progress the objects in a second step after the search
            // otherwise we run into problems with pagedresults
            final List<LdapGetter> ldapGetterList = new ArrayList<LdapGetter>();
            do {
                final NamingEnumeration<SearchResult> search = context.search(ownBaseDN, ownFilter, searchControls);
                while (null != search && search.hasMoreElements()) {
                    final SearchResult next = search.next();
                    final Attributes attributes = next.getAttributes();
                    ldapGetterList.add(getLdapGetter(attributes, context, next.getNameInNamespace()));
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
                final int pagesize = this.folderprop.getPagesize();
                if (0 != pagesize) {
                    // Re-activate paged results
                    context.setRequestControls(new Control[] { new PagedResultsControl(pagesize, cookie, Control.CRITICAL) });
                }
            } while (null != cookie);

            context.setRequestControls(null);
            for (final LdapGetter ldapGetter : ldapGetterList) {
                try {
                    closure.execute(ldapGetter);
                } catch (final OXException e) {
                    LOG.error("Error occured on distributionlist " + ldapGetter.getObjectFullName());
                    throw e;
                }
            }

        } catch (final NamingException e) {
            LOG.error(e.getMessage(), e);
            throw LdapExceptionCode.ERROR_GETTING_ATTRIBUTE.create(e.getMessage());
        } catch (final IOException e) {
            LOG.error(e.getMessage(), e);
            throw LdapExceptionCode.ERROR_GETTING_ATTRIBUTE.create(e.getMessage());
        }

    }

    private Control[] getControls(final SortInfo sortField, final boolean distributionlist, final int pagesize) throws IOException, NamingException {
        if (this.deleted) {
            if (0 == pagesize) {
                return new Control[] { new DeletedControl() };
            } else {
                return new Control[] { new PagedResultsControl(pagesize, Control.CRITICAL), new DeletedControl() };
            }
        } else {
            final String fieldFromColumn;
            if (null != sortField && folderprop.getSorting().equals(Sorting.server)&& null != (fieldFromColumn = getFieldFromColumn(sortField.getField(), distributionlist))) {
                final SortKey sortKey = new SortKey(fieldFromColumn, sortField.getSort().equals(Order.ASCENDING), null);
                final SortKey[] sortKeyArray = new SortKey[] { sortKey };
                if (0 == pagesize) {
                    return new Control[] { new SortControl(sortKeyArray, Control.CRITICAL) };
                } else {
                    return new Control[] {
                        new SortControl(sortKeyArray, Control.CRITICAL), new PagedResultsControl(pagesize, Control.CRITICAL) };
                }
            } else {
                if (0 == pagesize) {
                    return null;
                } else {
                    return new Control[] { new PagedResultsControl(pagesize, Control.CRITICAL) };
                }
            }
        }
    }

    private SearchControls getSearchControl(final Set<Integer> columns) {
        final SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(getSearchControl(folderprop.getSearchScope()));
        searchControls.setCountLimit(0);
        final List<String> array = getAttributes(columns, false);
        searchControls.setReturningAttributes(array.toArray(new String[array.size()]));
        return searchControls;
    }

    private SearchControls getSearchControlDistri(final Set<Integer> columns, final boolean deleted) {
        final SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(getSearchControl(folderprop.getSearchScopeDistributionlist()));
        searchControls.setCountLimit(0);
        final List<String> array = getAttributes(columns, true);
        if (!deleted) {
            array.add("member");
        }
        searchControls.setReturningAttributes(array.toArray(new String[array.size()]));
        return searchControls;
    }

    private LdapGetter getLdapGetter(final Attributes attributes, final LdapContext context, final String objectfullname) {
        return new LdapGetterJNDIImpl(attributes, context, objectfullname);
    }

    private List<String> getAttributes(final Set<Integer> columns, final boolean distributionlist) {
        final List<String> retval = new ArrayList<String>();
        for (final Integer col : columns) {
            final String fieldFromColumn = getFieldFromColumn(col, distributionlist);
            if (null != fieldFromColumn && fieldFromColumn.length() != 0) {
                retval.add(fieldFromColumn);
            }
        }
        return retval;
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
            if (distributionlist) {
                return mappings.getDistributionlistname();
            } else {
                return mappings.getSurname();
            }
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

    private static String getDefaultNamingContext(final LdapContext ctx) throws OXException {
        try {
            final SearchControls searchCtls2 = new SearchControls();
            searchCtls2.setSearchScope(SearchControls.OBJECT_SCOPE);
            searchCtls2.setReturningAttributes(new String[]{"defaultnamingcontext"});
            final NamingEnumeration<SearchResult> search = ctx.search("", "(objectclass=*)", searchCtls2);
            while (search.hasMoreElements()) {
                final SearchResult sr = search.next();
                final Attributes attrs = sr.getAttributes();
                if (attrs != null) {
                    for (final NamingEnumeration<?> ae = attrs.getAll();ae.hasMoreElements();) {
                        final Attribute attr = (Attribute)ae.next();
                        if (null != attr) {
                            for (final NamingEnumeration<?> e = attr.getAll(); e.hasMore();) {
                                final Object next = e.next();
                                if (null != next) {
                                    return next.toString();
                                }
                            }
                        }
                    }
                }
            }
            throw LdapExceptionCode.ERROR_GETTING_DEFAULT_NAMING_CONTEXT.create();
        } catch (final NamingException e) {
            throw LdapExceptionCode.ERROR_GETTING_DEFAULT_NAMING_CONTEXT.create(e);
        }
    }


    @Override
    public void close() throws OXException {
        try {
            this.context.close();
        } catch (final NamingException e) {
            LOG.error(e.getMessage(), e);
            throw LdapExceptionCode.ERROR_GETTING_ATTRIBUTE.create(e.getMessage());
        }
    }

    public LdapContext createContext(final String username, final String password) throws NamingException, OXException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating new connection.");
        }
        final long start = System.currentTimeMillis();
        final Hashtable<String, String> env = getBasicLDAPProperties();
        switch (folderprop.getAuthtype()) {
        case AdminDN:
            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            env.put(Context.SECURITY_PRINCIPAL, folderprop.getAdminDN());
            env.put(Context.SECURITY_CREDENTIALS, folderprop.getAdminBindPW());
            break;
        case user:
            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            env.put(Context.SECURITY_PRINCIPAL, getUserBindDN(username));
            env.put(Context.SECURITY_CREDENTIALS, password);
            break;
        case anonymous:
            break;
        default:
            break;
        }
        final LdapContext retval = new InitialLdapContext(env, null);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Context creation time: " + (System.currentTimeMillis() - start) + " ms");
        }
        return retval;
    }

    public static int getSearchControl(final SearchScope searchScope) {
        switch (searchScope) {
        case one:
            return SearchControls.ONELEVEL_SCOPE;
        case base:
            return SearchControls.OBJECT_SCOPE;
        case sub:
            return SearchControls.SUBTREE_SCOPE;
        default:
            return -1;
        }
    }

    private Hashtable<String, String> getBasicLDAPProperties() {
        final Hashtable<String, String> env = new Hashtable<String, String>(4, 1f);
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        // Enable connection pooling
        env.put("com.sun.jndi.ldap.connect.pool", "true");
        String uri = folderprop.getUri();
        if (uri.startsWith("ldap://") || uri.startsWith("ldaps://")) {
            if (uri.endsWith("/")) {
                uri = uri.substring(0, uri.length() - 1);
            }
            env.put(Context.PROVIDER_URL, uri + "/");
        } else {
            env.put(Context.PROVIDER_URL, "ldap://" + uri + ":389/");
        }
        if (uri.startsWith("ldaps://")) {
            env.put("java.naming.ldap.factory.socket", "com.openexchange.tools.ssl.TrustAllSSLSocketFactory");
        }
        switch (folderprop.getReferrals()) {
        case follow:
            env.put(Context.REFERRAL, "follow");
            break;
        case ignore:
            env.put(Context.REFERRAL, "ignore");
            break;
        case standard:
        default:
            break;
        }
        final DerefAliases derefAliases = folderprop.getDerefAliases();
        if (null != derefAliases) {
            env.put("java.naming.ldap.derefAliases", derefAliases.toString());
        }
        final int pooltimeout = folderprop.getPooltimeout();
        if (-1 != pooltimeout) {
            env.put("com.sun.jndi.ldap.connect.pool.timeout", String.valueOf(pooltimeout));
        }

        return env;
    }

    private String getUserBindDN(final String username) throws NamingException, OXException {
        final String userbinddn = MAPPINGTABLE_USERNAME_LDAPBIND.get(username);
        if (null != userbinddn) {
            return userbinddn;
        }
        final Hashtable<String, String> basicFolderProperties = getBasicLDAPProperties();
        switch (folderprop.getUserAuthType()) {
        case AdminDN:
            basicFolderProperties.put(Context.SECURITY_AUTHENTICATION, "simple");
            basicFolderProperties.put(Context.SECURITY_PRINCIPAL, folderprop.getUserAdminDN());
            basicFolderProperties.put(Context.SECURITY_CREDENTIALS, folderprop.getUserAdminBindPW());
            break;
        case anonymous:
            basicFolderProperties.put(Context.SECURITY_AUTHENTICATION, "none");
            break;
        }
        final LdapContext retval = new InitialLdapContext(basicFolderProperties, null);
        try {
            final SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(getSearchControl(folderprop.getUserSearchScope()));
            searchControls.setCountLimit(0);
            searchControls.setReturningAttributes(new String[]{"dn"});
            final NamingEnumeration<SearchResult> search = retval.search(folderprop.getUserSearchBaseDN(), setUpFilter(username), searchControls);
            if (!search.hasMore()) {
                throw LdapExceptionCode.NO_USER_RESULTS.create(username);
            }
            final SearchResult next = search.next();
            if (search.hasMore()) {
                throw LdapExceptionCode.TOO_MANY_USER_RESULTS.create();
            }
            final String userdn = next.getNameInNamespace();
            MAPPINGTABLE_USERNAME_LDAPBIND.put(username, userdn);
            return userdn;
        } finally {
            retval.close();
        }
    }

    private String setUpFilter(final String username) {
        return "(&" + folderprop.getUserSearchFilter() + "(" + folderprop.getUserSearchAttribute() + "=" + username + "))";
    }

}
