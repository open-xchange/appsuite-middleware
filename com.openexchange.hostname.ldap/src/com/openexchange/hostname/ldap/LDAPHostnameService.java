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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.cache.OXCachingException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.hostname.ldap.configuration.ConfigurationException;
import com.openexchange.hostname.ldap.configuration.LDAPHostnameProperties;
import com.openexchange.hostname.ldap.configuration.Property;
import com.openexchange.hostname.ldap.configuration.SearchScope;
import com.openexchange.hostname.ldap.services.HostnameLDAPServiceRegistry;


public class LDAPHostnameService implements HostnameService {

    private static final Log LOG = LogFactory.getLog(LDAPHostnameService.class);
    
    private static final String PLACEHOLDER = "%i";

    public LDAPHostnameService() {
        super();
    }

    public String getHostname(int userId, int contextId) {
        try {
            final LDAPHostnameCache instance = LDAPHostnameCache.getInstance();
            final String hostnameFromCache = instance.getHostnameFromCache(contextId);
            instance.outputSettings();
            if (null == hostnameFromCache) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Hostname for context " + contextId + " is not contained in the cache any more, fetching from LDAP");
                }
                final String hostname = fetchFromLdap(contextId);
                instance.addHostnameToCache(contextId, hostname);
                return hostname;
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Using hostname for context " + contextId + " from cache");
                }
                return hostnameFromCache;
            }
        } catch (OXCachingException e) {
            // TODO look for right handling
            e.printStackTrace();
            return null;
        }
    }

    private String fetchFromLdap(int contextId) {
        LdapContext context = null;
        String dn = null;
        try {
            final ConfigurationService service = HostnameLDAPServiceRegistry.getServiceRegistry().getService(ConfigurationService.class);
            dn = LDAPHostnameProperties.getProperty(service, Property.bind_dn);
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
            
            final NamingEnumeration<SearchResult> search = context.search(ownBaseDN, ownFilter, getSearchControls(ldapReturnField, scope));
            // We will only catch the first element...
            while (null != search && search.hasMoreElements()) {
                final SearchResult next = search.next();
                final Attributes attributes = next.getAttributes();
                return getAttribute(ldapReturnField, attributes);
            }
        } catch (final InvalidNameException e) {
            LOG.error("Login failed for dn " + dn + ":",e);
        } catch (final AuthenticationException e) {
            LOG.error("Login failed for dn " + dn + ":",e);
        } catch (final NamingException e) {
            LOG.error(e.getMessage(), e);
        } catch (ConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if( context != null ) {
                    context.close();
                }
            } catch (NamingException e) {
                LOG.error(e.getMessage(), e);
            }
        }

        
        return null;
    }

    private SearchControls getSearchControls(final String ldapReturnField, final SearchScope scope) {
        final SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(getSearchControl(scope));
        searchControls.setReturningAttributes(new String[] { ldapReturnField });
        return searchControls;
    }

    /**
     * Sets the contextid inside the filter syntax
     * 
     * @param filter
     * @param contextId
     * @return
     */
    private String getRightFilter(final String filter, final int contextId) {
        return filter.replace(PLACEHOLDER, String.valueOf(contextId));
    }
    
    private String getAttribute(final String attributename, final Attributes attributes) {
        try {
            final Attribute attribute = attributes.get(attributename);
            if (null != attribute) {
                if (1 < attribute.size()) {
                    // If we have multi-value attributes we only pick up the first one
                    return (String) attribute.get(0);
                } else {
                    return (String) attribute.get();
                }
            } else {
                return null;
            }
        } catch (final NamingException e) {
            // TODO Handle ex
            e.printStackTrace();
            return null;
            //throw new LdapException(Code.ERROR_GETTING_ATTRIBUTE, e.getMessage());
        }
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
    
    private Hashtable<String, String> getBasicLDAPProperties(String uri) {
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


}
