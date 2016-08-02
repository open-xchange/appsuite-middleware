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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.freebusy.publisher.ews.lookup;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import com.openexchange.ews.EWSExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.freebusy.publisher.ews.Tools;
import com.openexchange.groupware.ldap.User;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;

/**
 * {@link LdapLookup}
 *
 * Active Directory lookup implementation based on LDAP.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class LdapLookup extends Lookup {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LdapLookup.class);

    private String baseDN;
    private final String uri;
    private final String bindDN;
    private final String bindPW;
    private final String filter;
    private final boolean trustAllCerts;

    /**
     * Initializes a new {@link LdapLookup}.
     */
    public LdapLookup(String uri, String filter, String baseDN, String bindDN, String bindPW, boolean trustAllCerts) {
        super();
        this.filter = filter;
        this.baseDN = baseDN;
        this.uri = uri;
        this.bindDN = bindDN;
        this.bindPW = bindPW;
        this.trustAllCerts = trustAllCerts;
    }

    /**
     * Initializes a new {@link LdapLookup}.
     *
     * @throws OXException
     */
    public LdapLookup() throws OXException {
        this(
            Tools.getConfigProperty("com.openexchange.freebusy.publisher.ews.lookup.ldap.uri"),
            Tools.getConfigProperty("com.openexchange.freebusy.publisher.ews.lookup.ldap.filter"),
            Tools.getConfigProperty("com.openexchange.freebusy.publisher.ews.lookup.ldap.baseDN", null),
            Tools.getConfigProperty("com.openexchange.freebusy.publisher.ews.lookup.ldap.bindDN"),
            Tools.getConfigProperty("com.openexchange.freebusy.publisher.ews.lookup.ldap.bindPW"),
            Tools.getConfigPropertyBool("com.openexchange.freebusy.publisher.ews.trustAllCerts", false)
        );
    }

    @Override
    public String[] getLegacyExchangeDNs(User[] users) throws OXException  {
        LdapContext context = null;
        try {
            context = createContext();
            String[] legacyExchangeDNs = new String[users.length];
            for (int i = 0; i < users.length; i++) {
                try {
                    legacyExchangeDNs[i] = searchLegacyExchangeDN(context, users[i]);
                } catch (OXException e) {
                    LOG.warn("Error looking up legacyExchangeDN for user {}", users[i].getLoginInfo(), e);
                }
            }
            return legacyExchangeDNs;
        } catch (NamingException e) {
            throw EWSExceptionCodes.EXTERNAL_ERROR.create(e, e.getMessage());
        } finally {
            if (null != context) {
                try {
                    context.close();
                } catch (NamingException e) {
                    LOG.warn("Error closing LDAP context", e);
                }
            }
        }
    }

    private String searchLegacyExchangeDN(LdapContext context, User user) throws OXException {
        NamingEnumeration<SearchResult> results = null;
        try {
            SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            searchControls.setReturningAttributes(new String[] { "legacyExchangeDN" });
            String searchfilter = super.replaceUserAttributes(filter, user);
            String baseDN = getBaseDN(context);
            if (null == baseDN) {
                throw EWSExceptionCodes.EXTERNAL_ERROR.create("Unable to determine baseDN");
            }
            results = context.search(baseDN, searchfilter, searchControls);
            if (null == results || false == results.hasMore()) {
                throw EWSExceptionCodes.NOT_FOUND.create(searchfilter);
            }
            SearchResult searchResult = results.next();
            if (results.hasMoreElements()) {
                throw EWSExceptionCodes.AMBIGUOUS_NAME.create(searchfilter);
            }
            String legacyExchangeDN = getAttributeValue(searchResult, "legacyExchangeDN");
            if (null == legacyExchangeDN) {
                throw EWSExceptionCodes.NOT_FOUND.create("legacyExchangeDN");
            } else {
                return legacyExchangeDN;
            }
        } catch (NamingException e) {
            throw EWSExceptionCodes.EXTERNAL_ERROR.create(e, e.getMessage());
        } finally {
            if (null != results) {
                try {
                    results.close();
                } catch (NamingException e) {
                    LOG.warn("Error closing naming enumeration", e);
                }
            }
        }
    }

    private LdapContext createContext() throws NamingException {
        Hashtable<String, String> environment = new Hashtable<String, String>();
        environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        environment.put(Context.PROVIDER_URL, uri);
        environment.put(Context.REFERRAL, "follow");
        if (trustAllCerts && uri.startsWith("ldaps://")) {
            environment.put("java.naming.ldap.factory.socket", SSLSocketFactoryProvider.getDefault().getClass().getName());
        }
        if (null != bindDN) {
            environment.put(Context.SECURITY_AUTHENTICATION, "simple");
            environment.put(Context.SECURITY_PRINCIPAL, bindDN);
            environment.put(Context.SECURITY_CREDENTIALS, bindPW);
        } else {
            environment.put(Context.SECURITY_AUTHENTICATION, "none");
        }
        return new InitialLdapContext(environment, null);
    }

    private String getBaseDN(LdapContext context) throws NamingException {
        if (null == this.baseDN || 0 == baseDN.length()) {
            this.baseDN = discoverDefaultNamingContext(context);
        }
        return this.baseDN;
    }

    private static String discoverDefaultNamingContext(LdapContext context) throws NamingException {
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.OBJECT_SCOPE);
        searchControls.setReturningAttributes(new String[] { "defaultNamingContext" });
        NamingEnumeration<SearchResult> results = null;
        try {
            results = context.search("", "(objectClass=*)", searchControls);
            if (null != results && results.hasMore()) {
                String defaultNamingContext = getAttributeValue(results.next(), "defaultNamingContext");
                if (null != defaultNamingContext) {
                    return defaultNamingContext;
                }
            }
            return null;
        } finally {
            if (null != results) {
                results.close();
            }
        }
    }

    private static String getAttributeValue(SearchResult searchResult, String attributeName) throws NamingException {
        if (null != searchResult && null != searchResult.getAttributes()) {
            Attribute attribute = searchResult.getAttributes().get(attributeName);
            if (null != attribute) {
                if (1 == attribute.size()) {
                    return (String)attribute.get();
                } else {
                    return (String)attribute.get(0);
                }
            }
        }
        return null;
    }

}
