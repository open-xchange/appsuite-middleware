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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.hostname.ldap;

import java.util.Hashtable;
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.hostname.ldap.configuration.LDAPHostnameProperties;
import com.openexchange.hostname.ldap.configuration.Property;
import com.openexchange.hostname.ldap.configuration.SearchScope;
import com.openexchange.hostname.ldap.services.HostnameLDAPServiceRegistry;


public class LDAPHostnameService implements HostnameService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LDAPHostnameService.class);

    private static final String PLACEHOLDER = "%i";

    final LDAPHostnameCache instance;

    public LDAPHostnameService() throws OXException {
        super();
        instance = LDAPHostnameCache.getInstance();
    }

    private static String fetchFromLdap(final int contextId) throws OXException, NamingException {
        LdapContext context = null;
        NamingEnumeration<SearchResult> search = null;
        try {
            final ConfigurationService service = HostnameLDAPServiceRegistry.getServiceRegistry().getService(ConfigurationService.class);
            final String dn = LDAPHostnameProperties.getProperty(service, Property.bind_dn);
            final String password = LDAPHostnameProperties.getProperty(service, Property.bind_password);
            final String ldapReturnField = LDAPHostnameProperties.getProperty(service, Property.result_attribute);
            final String ownBaseDN = LDAPHostnameProperties.getProperty(service, Property.search_base);
            final String filter = LDAPHostnameProperties.getProperty(service, Property.query_filter);
            final SearchScope scope = LDAPHostnameProperties.getProperty(service, Property.scope);
            final Boolean bind = LDAPHostnameProperties.getProperty(service, Property.bind);
            final String url = LDAPHostnameProperties.getProperty(service, Property.ldap_url);
            final String ownFilter = getRightFilter(filter, contextId);

            context = new InitialLdapContext(getBasicLDAPProperties(url), null);
            if (bind.equals(Boolean.TRUE)) {
                context.addToEnvironment(Context.SECURITY_AUTHENTICATION, "simple");
                context.addToEnvironment(Context.SECURITY_PRINCIPAL, dn);
                context.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
                context.reconnect(null);
            } else {
                context.addToEnvironment(Context.SECURITY_AUTHENTICATION, "none");
                context.reconnect(null);
            }

            LOG.debug("\nLDAP search triggered with:\nFilter: {}BaseDN: {}Scope: {}ldapReturnField: {}\n", ownFilter, ownBaseDN, scope, ldapReturnField);

            search = context.search(ownBaseDN, ownFilter, getSearchControls(ldapReturnField, scope));
            // We will only catch the first element...
            while (null != search && search.hasMoreElements()) {
                final SearchResult next = search.next();
                final Attributes attributes = next.getAttributes();
                final String attribute = getAttribute(ldapReturnField, attributes);
                LOG.debug("Found result: {}", attribute);
                return attribute;
            }
            LOG.debug("No result found");
            return null;
        } finally {
            if (null != search) {
                search.close();
            }
            if( context != null ) {
                context.close();
            }
        }
    }

    private static String getAttribute(final String attributename, final Attributes attributes) throws NamingException {
        final Attribute attribute = attributes.get(attributename);
        if (null != attribute) {
            if (1 < attribute.size()) {
                // If we have multi-value attributes we only pick up the first one
                return (String) attribute.get(0);
            }
            return (String) attribute.get();
        }
        return null;
    }

    private static Hashtable<String, String> getBasicLDAPProperties(String uri) {
        final Hashtable<String, String> env = new Hashtable<String, String>(4, 1f);
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        // Enable connection pooling
        env.put("com.sun.jndi.ldap.connect.pool", "true");
        if (uri.startsWith("ldap://") || uri.startsWith("ldaps://")) {
            if (uri.endsWith("/")) {
                uri = uri.substring(0, uri.length() - 1);
            }
            env.put(Context.PROVIDER_URL, uri + "/");
        } else {
            env.put(Context.PROVIDER_URL, "ldap://" + uri);
        }
        if (uri.startsWith("ldaps://")) {
            env.put("java.naming.ldap.factory.socket", "com.openexchange.tools.ssl.TrustAllSSLSocketFactory");
        }

        return env;
    }

    /**
     * Sets the contextid inside the filter syntax
     *
     * @param filter
     * @param contextId
     * @return
     */
    private static String getRightFilter(final String filter, final int contextId) {
        return filter.replace(PLACEHOLDER, String.valueOf(contextId));
    }

    private static int getSearchControl(final SearchScope searchScope) {
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

    private static SearchControls getSearchControls(final String ldapReturnField, final SearchScope scope) {
        final SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(getSearchControl(scope));
        searchControls.setReturningAttributes(new String[] { ldapReturnField });
        return searchControls;
    }

    @Override
    public String getHostname(final int userId, final int contextId) {
        try {
            final String hostnameFromCache = instance.getHostnameFromCache(contextId);
            if (null == hostnameFromCache) {
                LOG.debug("Hostname for context {} is not contained in the cache any more, fetching from LDAP", contextId);
                final String hostname = fetchFromLdap(contextId);
                if (null != hostname) {
                    instance.addHostnameToCache(contextId, hostname);
                }
                return hostname;
            }
            LOG.debug("Using hostname for context {} from cache", contextId);
            return hostnameFromCache;
        } catch (final InvalidNameException e) {
            LOG.error("Failed to fetch hostname for context id {}:", contextId, e);
        } catch (final AuthenticationException e) {
            LOG.error("Failed to fetch hostname for context id {}:", contextId, e);
        } catch (final NamingException e) {
            LOG.error("Failed to fetch hostname for context id {}:", contextId, e);
        } catch (final OXException e) {
            LOG.error("Failed to fetch hostname for context id {}:", contextId, e);
        } catch (final RuntimeException e) {
            LOG.error("Failed to fetch hostname for context id {}:", contextId, e);
        }
        return null;
    }

}
