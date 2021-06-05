/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
import com.openexchange.hostname.ldap.osgi.Services;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.server.ServiceLookup;


public class LDAPHostnameService implements HostnameService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LDAPHostnameService.class);

    private static final String PLACEHOLDER = "%i";

    // ---------------------------------------------------------------------------------------------------------

    private final LDAPHostnameCache instance;
    private final ServiceLookup services;

    /**
     * Initializes a new {@link LDAPHostnameService}.
     *
     * @param services
     */
    public LDAPHostnameService(ServiceLookup services) {
        super();
        instance = LDAPHostnameCache.getInstance();
        this.services = services;
    }

    private String[] fetchFromLdap(int contextId) throws OXException, NamingException {
        LdapContext context = null;
        NamingEnumeration<SearchResult> search = null;
        try {
            final ConfigurationService service = services.getService(ConfigurationService.class);
            final String dn = LDAPHostnameProperties.getProperty(service, Property.bind_dn);
            final String password = LDAPHostnameProperties.getProperty(service, Property.bind_password);
            final String ldapReturnField = LDAPHostnameProperties.getProperty(service, Property.result_attribute);
            final String ldapGuestReturnField = LDAPHostnameProperties.getProperty(service, Property.guest_result_attribute);
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

            search = context.search(ownBaseDN, ownFilter, getSearchControls(scope, ldapReturnField, ldapGuestReturnField));
            // We will only catch the first element...
            String hostname = null;
            String guestHostname = null;
            while (null != search && search.hasMoreElements() && (null == hostname || null == guestHostname)) {
                final SearchResult next = search.next();
                final Attributes attributes = next.getAttributes();
                if (null == hostname) {
                    String attribute = getAttribute(ldapReturnField, attributes);
                    if (null != attribute) {
                        LOG.debug("Found hostname result: {}", attribute);
                        hostname = attribute;
                    }
                }
                if (null == guestHostname) {
                    String attribute = getAttribute(ldapGuestReturnField, attributes);
                    if (null != attribute) {
                        LOG.debug("Found guest hostname result: {}", attribute);
                        guestHostname = attribute;
                    }
                }
            }
            return new String[] { hostname, guestHostname };
        } finally {
            if (null != search) {
                try { search.close(); } catch (Exception x) { /* Ignore */ }
            }
            if (context != null) {
                try { context.close(); } catch (Exception x) { /* Ignore */ }
            }
        }
    }

    private String getAttribute(String attributename, Attributes attributes) throws NamingException {
        Attribute attribute = attributes.get(attributename);
        if (null != attribute) {
            if (1 < attribute.size()) {
                // If we have multi-value attributes we only pick up the first one
                return (String) attribute.get(0);
            }
            return (String) attribute.get();
        }
        return null;
    }

    private Hashtable<String, String> getBasicLDAPProperties(String uri) {
        Hashtable<String, String> env = new Hashtable<String, String>(4, 1f);
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
            SSLSocketFactoryProvider factoryProvider = Services.getService(SSLSocketFactoryProvider.class);
            env.put("java.naming.ldap.factory.socket", factoryProvider.getDefault().getClass().getName());
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
    private String getRightFilter(String filter, int contextId) {
        return filter.replace(PLACEHOLDER, String.valueOf(contextId));
    }

    private int getSearchControl(final SearchScope searchScope) {
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

    private SearchControls getSearchControls(SearchScope scope, String...ldapReturnFields) {
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(getSearchControl(scope));
        searchControls.setReturningAttributes(ldapReturnFields);
        return searchControls;
    }

    @Override
    public String getHostname(int userId, int contextId) {
        return getHostnameFromLDAP(contextId, 0);
    }

    @Override
    public String getGuestHostname(int userId, int contextId) {
        return getHostnameFromLDAP(contextId, 1);
    }

    private String getHostnameFromLDAP(int contextId, int index) {
        try {
            String[] hostnamesFromCache = instance.getHostnamesFromCache(contextId);
            if (null == hostnamesFromCache) {
                LOG.debug("Hostnames for context {} is not contained in the cache any more, fetching from LDAP", Integer.valueOf(contextId));
                final String[] hostnames = fetchFromLdap(contextId);
                instance.addHostnamesToCache(contextId, hostnames);
                return hostnames[index];
            }
            LOG.debug("Using hostnames for context {} from cache", Integer.valueOf(contextId));
            return hostnamesFromCache[index];
        } catch (InvalidNameException e) {
            LOG.error("Failed to fetch hostnames for context id {}:", Integer.valueOf(contextId), e);
        } catch (AuthenticationException e) {
            LOG.error("Failed to fetch hostnames for context id {}:", Integer.valueOf(contextId), e);
        } catch (NamingException e) {
            LOG.error("Failed to fetch hostnames for context id {}:", Integer.valueOf(contextId), e);
        } catch (OXException e) {
            LOG.error("Failed to fetch hostnames for context id {}:", Integer.valueOf(contextId), e);
        } catch (RuntimeException e) {
            LOG.error("Failed to fetch hostnames for context id {}:", Integer.valueOf(contextId), e);
        }
        return null;
    }
}
