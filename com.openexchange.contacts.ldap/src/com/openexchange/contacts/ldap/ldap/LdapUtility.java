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

package com.openexchange.contacts.ldap.ldap;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import com.openexchange.contacts.ldap.exceptions.LdapException;
import com.openexchange.contacts.ldap.exceptions.LdapException.Code;
import com.openexchange.contacts.ldap.property.FolderProperties;
import com.openexchange.contacts.ldap.property.FolderProperties.SearchScope;

/**
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 */
public final class LdapUtility {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(LdapUtility.class);

    private static Map<String, String> MAPPINGTABLE_USERNAME_LDAPBIND = new ConcurrentHashMap<String, String>();

    /**
     * Private constructor prevents instanciation.
     */
    private LdapUtility() {
    }

    public static LdapContext createContext(final String username, final String password, final FolderProperties folderProperties) throws NamingException, LdapException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating new connection.");
        }
        final long start = System.currentTimeMillis();
        final Hashtable<String, String> env = getBasicLDAPProperties(folderProperties);
        switch (folderProperties.getAuthtype()) {
        case AdminDN:
            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            env.put(Context.SECURITY_PRINCIPAL, folderProperties.getAdminDN());
            env.put(Context.SECURITY_CREDENTIALS, folderProperties.getAdminBindPW());
            break;
        case user:
            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            env.put(Context.SECURITY_PRINCIPAL, getUserBindDN(folderProperties, username));
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

    private static Hashtable<String, String> getBasicLDAPProperties(final FolderProperties folderProperties) {
        final Hashtable<String, String> env = new Hashtable<String, String>(4, 1f);
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        // Enable connection pooling
        env.put("com.sun.jndi.ldap.connect.pool", "true");
        String uri = folderProperties.getUri();
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
        return env;
    }

    private static String getUserBindDN(final FolderProperties folderprops, final String username) throws NamingException, LdapException {
        final String userbinddn = MAPPINGTABLE_USERNAME_LDAPBIND.get(username);
        if (null != userbinddn) {
            return userbinddn;
        } else {
            final Hashtable<String, String> basicFolderProperties = getBasicLDAPProperties(folderprops);
            switch (folderprops.getUserAuthType()) {
            case AdminDN:
                basicFolderProperties.put(Context.SECURITY_AUTHENTICATION, "simple");
                basicFolderProperties.put(Context.SECURITY_PRINCIPAL, folderprops.getUserAdminDN());
                basicFolderProperties.put(Context.SECURITY_CREDENTIALS, folderprops.getUserAdminBindPW());
                break;
            case anonymous:
                basicFolderProperties.put(Context.SECURITY_AUTHENTICATION, "none");
                break;
            }
            final LdapContext retval = new InitialLdapContext(basicFolderProperties, null);
            try {
                final SearchControls searchControls = new SearchControls();
                searchControls.setSearchScope(getSearchControl(folderprops.getUserSearchScope()));
                searchControls.setCountLimit(0);
                searchControls.setReturningAttributes(new String[]{"dn"});
                final NamingEnumeration<SearchResult> search = retval.search(folderprops.getUserSearchBaseDN(), setUpFilter(folderprops, username), searchControls);
                if (search.hasMore()) {
                    final SearchResult next = search.next();
                    if (search.hasMore()) {
                        throw new LdapException(Code.TOO_MANY_USER_RESULTS);
                    }
                    final String userdn = next.getNameInNamespace();
                    MAPPINGTABLE_USERNAME_LDAPBIND.put(username, userdn);
                    return userdn;
                } else {
                    throw new LdapException(Code.NO_USER_RESULTS);
                }
            } finally {
                retval.close();
            }
        }
    }

    private static String setUpFilter(final FolderProperties folderprops, final String username) {
        return "(&" + folderprops.getUserSearchFilter() + "(" + folderprops.getUserSearchAttribute() + "=" + username + "))";
    }

}
