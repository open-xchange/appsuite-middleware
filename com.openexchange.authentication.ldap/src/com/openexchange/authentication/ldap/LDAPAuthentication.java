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

package com.openexchange.authentication.ldap;

import java.util.Properties;
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
import javax.security.auth.login.LoginException;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.AuthenticationService;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.authentication.LoginInfo;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.exception.OXException;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;

/**
 * This class implements the login by using an LDAP for authentication.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class LDAPAuthentication implements AuthenticationService, Reloadable {

    private static final class AuthenticatedImpl implements Authenticated {

        private final String returnstring;
        private final String[] splitted;

        protected AuthenticatedImpl(String returnstring, String[] splitted) {
            this.returnstring = returnstring;
            this.splitted = splitted;
        }

        @Override
        public String getContextInfo() {
            return splitted[0];
        }

        @Override
        public String getUserInfo() {
            return null == returnstring ? splitted[1] : returnstring;
        }
    }

    private enum PropertyNames {
        BASE_DN("baseDN"),
        UID_ATTRIBUTE("uidAttribute"),
        LDAP_RETURN_FIELD("ldapReturnField"),
        ADS_NAME_BIND("adsBind"),
        BIND_ONLY("bindOnly"),
        LDAP_SCOPE("ldapScope"),
        SEARCH_FILTER("searchFilter"),
        BIND_DN("bindDN"),
        BIND_DN_PASSWORD("bindDNPassword"),
        PROXY_USER("proxyUser"),
        PROXY_DELIMITER("proxyDelimiter"),
        REFERRAL("referral"),
        USE_FULL_LOGIN_INFO("useFullLoginInfo");

        public String name;

        private PropertyNames(String name) {
            this.name = name;
        }
    }

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LDAPAuthentication.class);

    /**
     * Properties for the JNDI context.
     */
    private Properties props;

    /**
     * attribute name and base DN.
     */
    private String uidAttribute, baseDN, ldapReturnField, searchFilter, bindDN, bindDNPassword, proxyUser, proxyDelimiter, referral, ldapScope;

    private boolean bindOnly, useFullLoginInfo;

    private boolean adsbind;

    private int searchScope = SearchControls.SUBTREE_SCOPE;

    /**
     * Default constructor.
     * @throws LoginException if setup fails.
     */
    public LDAPAuthentication(Properties props) throws OXException {
        super();
        this.props = props;
        init();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Authenticated handleLoginInfo(LoginInfo loginInfo) throws OXException {
        final String[] splitted = split(loginInfo.getUsername());
        final String uid = splitted[1];
        final String password = loginInfo.getPassword();
        final String returnstring;
        if ("".equals(uid) || "".equals(password)) {
            throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
        }
        LOG.debug("Using full login info: {}", loginInfo.getUsername());
        returnstring = bind(useFullLoginInfo ? loginInfo.getUsername() : uid, password);
        LOG.info("User {} successfully authenticated.", useFullLoginInfo ? loginInfo.getUsername() : uid);
        return new AuthenticatedImpl(returnstring, splitted);
    }

    @Override
    public Authenticated handleAutoLoginInfo(LoginInfo loginInfo) throws OXException {
        throw LoginExceptionCodes.NOT_SUPPORTED.create(LDAPAuthentication.class.getName());
    }

    /**
     * Tries to bind.
     * @param uid login name.
     * @param password password.
     * @throws LoginException if some problem occurs.
     */
    private String bind(String uid, String password) throws OXException {
        LdapContext context = null;
        String dn = null;
        String proxyAs = null;
        if( proxyUser != null && proxyDelimiter != null && uid.contains(proxyDelimiter)) {
            proxyAs = uid.substring(uid.indexOf(proxyDelimiter)+proxyDelimiter.length(), uid.length());
            uid = uid.substring(0, uid.indexOf(proxyDelimiter));
            boolean foundProxy = false;
            for(final String pu : proxyUser.split(",")) {
                if( pu.trim().equalsIgnoreCase(uid) ) {
                    foundProxy = true;
                }
            }
            if( ! foundProxy ) {
                LOG.error("none of the proxy user is matching");
                throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
            }
        }
        try {
            String samAccountName = null;
            if (!bindOnly) {
                // get user dn from user
                final Properties aprops = (Properties)props.clone();
                aprops.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
                if( bindDN != null && bindDN.length() > 0 ) {
                    LOG.debug("Using bindDN={}", bindDN);
                    aprops.put(Context.SECURITY_PRINCIPAL, bindDN);
                    aprops.put(Context.SECURITY_CREDENTIALS, bindDNPassword);
                } else {
                    aprops.put(Context.SECURITY_AUTHENTICATION, "none");
                }
                context = new InitialLdapContext(aprops, null);
                final String filter = "(&" + searchFilter + "(" + uidAttribute + "=" + uid + "))";
                LOG.debug("Using filter={}", filter);
                LOG.debug("BaseDN      ={}", baseDN);
                SearchControls cons = new SearchControls();
                cons.setSearchScope(searchScope);
                cons.setCountLimit(0);
                cons.setReturningAttributes(new String[]{"dn"});
                NamingEnumeration<SearchResult> res = null;
                try {
                    res = context.search(baseDN, filter, cons);
                    if( res.hasMoreElements() ) {
                        dn = res.nextElement().getNameInNamespace();
                        if( res.hasMoreElements() ) {
                            final String errortext = "Found more then one user with " + uidAttribute + "=" + uid;
                            LOG.error(errortext);
                            throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
                        }
                    } else {
                        final String errortext = "No user found with " + uidAttribute + "=" + uid;
                        LOG.error(errortext);
                        throw LoginExceptionCodes.INVALID_CREDENTIALS_MISSING_USER_MAPPING.create(uid);
                    }
                } finally {
                    close(res);
                }
                context.close();
            } else {
                // Whether or not to use the samAccountName search
                if (this.adsbind) {
                    int index = uid.indexOf("\\");
                    if (-1 != index) {
                        samAccountName = uid.substring(index + 1);
                    } else {
                        samAccountName = null;
                    }
                    dn = uid;
                } else {
                    dn = uidAttribute + "=" + uid + "," + baseDN;
                    samAccountName = null;
                }

            }
            context = new InitialLdapContext(props, null);
            context.addToEnvironment(Context.REFERRAL, referral);
            context.addToEnvironment(Context.SECURITY_PRINCIPAL, dn);
            context.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
            context.reconnect(null);
            if (null != ldapReturnField && ldapReturnField.length() > 0) {
                final Attributes userDnAttributes;
                String puser = null;
                if (this.adsbind) {
                    final SearchControls searchControls = new SearchControls();
                    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
                    searchControls.setCountLimit(0);
                    searchControls.setReturningAttributes(new String[]{ldapReturnField});
                    NamingEnumeration<SearchResult> search = null;
                    NamingEnumeration<SearchResult> searchProxy = null;
                    try {
                        if (null == samAccountName) {
                            if( proxyAs != null ) {
                                search = context.search(this.baseDN, "(displayName=" + uid + ")", searchControls);
                                searchProxy = context.search(this.baseDN, "(displayName=" + proxyAs + ")", searchControls);
                            } else {
                                search = context.search(this.baseDN, "(displayName=" + uid + ")", searchControls);
                            }
                        } else {
                            search = context.search(this.baseDN, "(sAMAccountName=" + samAccountName + ")", searchControls);
                        }
                        if (null != search && search.hasMoreElements()) {
                            final SearchResult next = search.next();
                            userDnAttributes = next.getAttributes();
                            if( proxyAs != null && searchProxy != null ) {
                                puser = (String)searchProxy.next().getAttributes().get(ldapReturnField).get();
                            }
                        } else {
                            LOG.error("No user with displayname {} found.", uid);
                            throw LoginExceptionCodes.INVALID_CREDENTIALS_MISSING_USER_MAPPING.create(uid);
                        }
                    } finally {
                        close(search);
                        close(searchProxy);
                    }
                } else {
                    userDnAttributes = context.getAttributes(dn);
                }
                final Attribute attribute = userDnAttributes.get(ldapReturnField);
                if( proxyAs != null ) {
                    return (String) attribute.get()+proxyDelimiter+puser;
                } else {
                    return (String) attribute.get();
                }
            }
            return null;
        } catch (InvalidNameException e) {
            LOG.debug("Login failed for dn {}:", dn,e);
            throw LoginExceptionCodes.INVALID_CREDENTIALS.create(e);
        } catch (AuthenticationException e) {
            LOG.debug("Login failed for dn {}:", dn,e);
            throw LoginExceptionCodes.INVALID_CREDENTIALS.create(e);
        } catch (NamingException e) {
            LOG.error("", e);
            throw LoginExceptionCodes.COMMUNICATION.create(e);
        } finally {
            try {
                if( context != null ) {
                    context.close();
                }
            } catch (NamingException e) {
                LOG.error("", e);
            }
        }
    }

    /**
     * Initializes the properties for the ldap authentication.
     * @throws LoginException if configuration fails.
     */
    private void init() throws OXException {
        props.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");

        if (!props.containsKey(PropertyNames.UID_ATTRIBUTE.name)) {
            throw LoginExceptionCodes.MISSING_PROPERTY.create(PropertyNames.UID_ATTRIBUTE.name);
        }
        uidAttribute = props.getProperty(PropertyNames.UID_ATTRIBUTE.name);

        if (!props.containsKey(PropertyNames.BASE_DN.name)) {
            throw LoginExceptionCodes.MISSING_PROPERTY.create(PropertyNames.BASE_DN.name);
        }
        baseDN = props.getProperty(PropertyNames.BASE_DN.name);

        final String url = props.getProperty(Context.PROVIDER_URL);
        if (null == url) {
            throw LoginExceptionCodes.MISSING_PROPERTY.create(Context.PROVIDER_URL);
        } else if (url.startsWith("ldaps")) {
            props.put("java.naming.ldap.factory.socket", SSLSocketFactoryProvider.getDefault().getClass().getName());
        }

        this.ldapReturnField = props.getProperty(PropertyNames.LDAP_RETURN_FIELD.name);

        this.adsbind = Boolean.parseBoolean(props.getProperty(PropertyNames.ADS_NAME_BIND.name));

        if (!props.containsKey(PropertyNames.BIND_ONLY.name)) {
            throw LoginExceptionCodes.MISSING_PROPERTY.create(PropertyNames.BIND_ONLY.name);
        }
        bindOnly = Boolean.parseBoolean(props.getProperty(PropertyNames.BIND_ONLY.name));

        if (!props.containsKey(PropertyNames.LDAP_SCOPE.name)) {
            throw LoginExceptionCodes.MISSING_PROPERTY.create(PropertyNames.LDAP_SCOPE.name);
        }
        ldapScope = props.getProperty(PropertyNames.LDAP_SCOPE.name);
        if( "subtree".equals(ldapScope) ) {
            searchScope = SearchControls.SUBTREE_SCOPE;
        } else if( "onelevel".equals(ldapScope) ) {
            searchScope = SearchControls.ONELEVEL_SCOPE;
        } else if( "base".equals(ldapScope) ) {
            searchScope = SearchControls.OBJECT_SCOPE;
        } else {
            throw LoginExceptionCodes.UNKNOWN.create(PropertyNames.LDAP_SCOPE.name + " must be one of subtree, onelevel or base");
        }

        if (!props.containsKey(PropertyNames.SEARCH_FILTER.name)) {
            throw LoginExceptionCodes.MISSING_PROPERTY.create(PropertyNames.SEARCH_FILTER.name);
        }
        searchFilter = props.getProperty(PropertyNames.SEARCH_FILTER.name);

        bindDN = props.getProperty(PropertyNames.BIND_DN.name);
        bindDNPassword = props.getProperty(PropertyNames.BIND_DN_PASSWORD.name);

        if (props.containsKey(PropertyNames.PROXY_USER.name)) {
            proxyUser = props.getProperty(PropertyNames.PROXY_USER.name);
        }

        if (props.containsKey(PropertyNames.PROXY_DELIMITER.name)) {
            proxyDelimiter = props.getProperty(PropertyNames.PROXY_DELIMITER.name);
        }

        if (!props.containsKey(PropertyNames.REFERRAL.name)) {
            throw LoginExceptionCodes.MISSING_PROPERTY.create(PropertyNames.REFERRAL.name);
        }
        referral = props.getProperty(PropertyNames.REFERRAL.name);

        useFullLoginInfo = Boolean.parseBoolean(props.getProperty(PropertyNames.USE_FULL_LOGIN_INFO.name));

    }

    /**
     * Splits user name and context.
     * @param loginInfo combined information separated by an @ sign.
     * @return a string array with context and user name (in this order).
     * @throws LoginException if no separator is found.
     */
    private String[] split(String loginInfo) {
        return split(loginInfo, '@');
    }

    /**
     * Splits user name and context.
     * @param loginInfo combined information separated by an @ sign.
     * @param character for splitting user name and context.
     * @return a string array with context and user name (in this order).
     * @throws LoginException if no separator is found.
     */
    private String[] split(String loginInfo, char separator) {
        final int pos = loginInfo.lastIndexOf(separator);
        final String[] splitted = new String[2];
        if (-1 == pos) {
            splitted[1] = loginInfo;
            splitted[0] = "defaultcontext";
        } else {
            splitted[1] = loginInfo.substring(0, pos);
            splitted[0] = loginInfo.substring(pos + 1);
        }
        return splitted;
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        Properties properties = configService.getFile("ldapauth.properties");
        this.props = properties;
        try {
            init();
        } catch (OXException e) {
            LOG.error("Error reloading configuration for bundle com.openexchange.authentication.ldap: {}", e);
        }
    }

    @Override
    public Interests getInterests() {
        return DefaultInterests.builder().configFileNames("ldapauth.properties").build();
    }

    /**
     * Closes the supplied naming enumeration, swallowing a possible {@link NamingException}.
     *
     * @param namingEnumeration The naming operation to close, or <code>null</code> to do nothing for convenience
     */
    private static void close(NamingEnumeration<?> namingEnumeration) {
        if (null != namingEnumeration) {
            try {
                namingEnumeration.close();
            } catch (NamingException e) {
                LOG.warn("Error closing naming enumeration", e);
            }
        }
    }

}
