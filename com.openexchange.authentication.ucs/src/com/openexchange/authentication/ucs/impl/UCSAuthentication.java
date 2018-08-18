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



package com.openexchange.authentication.ucs.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.AuthenticationService;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.authentication.LoginInfo;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.tools.ssl.TrustAllSSLSocketFactory;

/**
 *
 * Authentication Plugin for the UCS Server Product.
 * This Class implements the needed Authentication against an UCS LDAP Server:
 * 1. User enters following information on Loginscreen: username and password (NO CONTEXT, will be resolved by the LDAP Attribute)
 * 1a. Search for given "username"  (NOT with context) given by OX Loginmask with configured pattern and with configured LDAP BASE.
 * 2. If user is found, bind to LDAP Server with the found DN
 * 3. If BIND successfull, fetch the configured "context" Attribute and parse out the context name.
 * 4. Return context name and username to OX API!
 * 5. User is logged in!
 *
 * @author Manuel Kraft
 *
 */
public class UCSAuthentication implements AuthenticationService,Reloadable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UCSAuthentication.class);
    public static final String CONFIGFILE = "authplugin.properties";

    private static final String PASSWORD_CHANGE_URL_OPTION = "com.openexchange.authentication.ucs.passwordChangeURL";
    private static final String BINDDN_OPTION = "com.openexchange.authentication.ucs.bindDn";
    private static final String BINDPW_OPTION = "com.openexchange.authentication.ucs.bindPassword";
    private static final String CONTEXTIDATTR_OPTION = "com.openexchange.authentication.ucs.contextIdAttribute";
    private static final String LDAPURL_OPTION = "com.openexchange.authentication.ucs.ldapUrl";
    private static final String LDAPBASE_OPTION = "com.openexchange.authentication.ucs.baseDn";
    private static final String SEARCHFILTER_OPTION = "com.openexchange.authentication.ucs.searchFilter";
    private static final String MAILATTR_OPTION = "com.openexchange.authentication.ucs.mailAttribute";
    private static final String LOGINATTR_OPTION = "com.openexchange.authentication.ucs.loginAttribute";
    private static final String LDAPPOOL_OPTION = "com.openexchange.authentication.ucs.useLdapPool";

    private static final class AuthenticatedImpl implements Authenticated {

        private final String login;
        private final String contextIdOrName;

        AuthenticatedImpl(String login, String contextIdOrName) {
            super();
            this.login = login;
            this.contextIdOrName = contextIdOrName;
        }

        @Override
        public String getContextInfo() {
            return contextIdOrName;
        }

        @Override
        public String getUserInfo() {
            return login;
        }
    } // End of class AuthenticatedImpl

    private static class Config {

        private static class Builder {

            private Hashtable<String, String> ldapConfigDefaults;
            private URL passwordChangeURL;
            private String binddn;
            private String bindpw;
            private String contextIdAttr;
            private String ldapUrl;
            private String baseDn;
            private String searchFilter;
            private String mailAttr;
            private String loginAttr;

            Builder() {
                super();
            }

            Builder withLdapConfigDefaults(Hashtable<String, String> ldapConfigDefaults) {
                this.ldapConfigDefaults = ldapConfigDefaults;
                return this;
            }

            Builder withPasswordChangeURL(URL passwordChangeURL) {
                this.passwordChangeURL = passwordChangeURL;
                return this;
            }

            Builder withBinddn(String binddn) {
                this.binddn = binddn;
                return this;
            }

            Builder withBindpw(String bindpw) {
                this.bindpw = bindpw;
                return this;
            }

            Builder withContextIdAttr(String contextIdAttr) {
                this.contextIdAttr = contextIdAttr;
                return this;
            }

            Builder withLdapUrl(String ldapUrl) {
                this.ldapUrl = ldapUrl;
                return this;
            }

            Builder withBaseDn(String baseDn) {
                this.baseDn = baseDn;
                return this;
            }

            Builder withSearchFilter(String searchFilter) {
                this.searchFilter = searchFilter;
                return this;
            }

            Builder withMailAttr(String mailAttr) {
                this.mailAttr = mailAttr;
                return this;
            }

            Builder withLoginAttr(String loginAttr) {
                this.loginAttr = loginAttr;
                return this;
            }

            Config build() {
                return new Config(ldapConfigDefaults, passwordChangeURL, binddn, bindpw, contextIdAttr, ldapUrl, baseDn, searchFilter, mailAttr, loginAttr);
            }

        }

        final Hashtable<String, String> ldapConfigDefaults;
        final URL passwordChangeURL;
        final String binddn;
        final String bindpw;
        final String contextIdAttr;
        final String ldapUrl;
        final String baseDn;
        final String searchFilter;
        final String mailAttr;
        final String loginAttr;

        Config(Hashtable<String, String> ldapConfigDefaults, URL passwordChangeURL, String binddn, String bindpw, String contextIdAttr, String ldapUrl, String baseDn, String searchFilter, String mailAttr, String loginAttr) {
            super();
            this.ldapConfigDefaults = ldapConfigDefaults;
            this.passwordChangeURL = passwordChangeURL;
            this.binddn = binddn;
            this.bindpw = bindpw;
            this.contextIdAttr = contextIdAttr;
            this.ldapUrl = ldapUrl;
            this.baseDn = baseDn;
            this.searchFilter = searchFilter;
            this.mailAttr = mailAttr;
            this.loginAttr = loginAttr;
        }
    } // End of class Config

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final AtomicReference<Config> configReference;

    /**
     * Default constructor.
     *
     * @param configService The service to use
     * @throws OXException If initialization fails
     */
    public UCSAuthentication(Properties props) throws OXException {
        super();
        configReference = new AtomicReference<Config>(parseConfigFrom(props));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Authenticated handleLoginInfo(final LoginInfo loginInfo) throws OXException {
        DirContext ctx = null;
        try {
            final String loginString = loginInfo.getUsername();
            final String password = loginInfo.getPassword();
            if (Strings.isEmpty(loginString) || Strings.isEmpty(password)) {
                throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
            }

            Config config = configReference.get();

            // Search LDAP server without any credentials to get the users dn to bind with
            Hashtable<String, String> ldapConfig = new Hashtable<String, String>(config.ldapConfigDefaults);
            boolean doBind = null != config.binddn && config.binddn.trim().length() > 0 && null != config.bindpw && config.bindpw .trim().length() > 0;
            if (doBind) {
                ldapConfig.put(Context.SECURITY_AUTHENTICATION, "simple");
                ldapConfig.put(Context.SECURITY_PRINCIPAL, config.binddn);
                ldapConfig.put(Context.SECURITY_CREDENTIALS, config.bindpw);
            } else {
                LOG.debug("either {}, {} or both not set, doing anonymous bind", BINDDN_OPTION, BINDPW_OPTION);
                ldapConfig.put(Context.SECURITY_AUTHENTICATION, "none");
            }
            ctx = new InitialDirContext(ldapConfig);

            String user_dn = null;
            {
                SearchControls sc = new SearchControls();
                sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
                sc.setReturningAttributes(new String[]{"dn"});

                String search_pattern = Strings.replaceSequenceWith(config.searchFilter, "%s", escapeString(loginString));
                LOG.debug("Now searching on server {} for DN of User {} with BASE: {} and pattern {}", config.ldapUrl, loginString, config.baseDn, search_pattern);
                NamingEnumeration<SearchResult> result = ctx.search(config.baseDn, search_pattern, sc);
                try {
                    if ( !result.hasMoreElements() ) {
                        LOG.error("User {} not found in LDAP", loginString);
                        throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
                    }
                    final SearchResult sr = result.next();
                    LOG.debug("User found : {}", sr.getName());
                    user_dn = new StringBuilder(sr.getName()).append(',').append(config.baseDn).toString();

                    if (result.hasMoreElements()) {
                        LOG.error("More than one user for login string {} found in LDAP", loginString);
                        throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
                    }
                } finally {
                    try {
                        result.close();
                    } catch (NamingException e) {
                        LOG.error("", e);
                    }
                }
            }

            // unbind old context
            try {
                ctx.close();
            } catch (NamingException e) {
                LOG.error("", e);
            }
            ctx = null;

            // after we found the users dn, auth with this dn and given password
            if (false == doBind) {
                ldapConfig.put(Context.SECURITY_AUTHENTICATION, "simple");
            }
            ldapConfig.put(Context.SECURITY_PRINCIPAL, user_dn);
            ldapConfig.put(Context.SECURITY_CREDENTIALS, password);

            boolean hasContextIdAttr = null != config.contextIdAttr;

            String[] attribs;
            if (hasContextIdAttr) {
                attribs = new String[] { config.contextIdAttr, config.mailAttr, config.loginAttr, "shadowLastChange", "shadowMax" };
                LOG.debug("Also fetching contextId attribute {}", config.contextIdAttr);
            } else {
                attribs = new String[] { config.mailAttr, config.loginAttr, "shadowLastChange", "shadowMax" };
            }

            LOG.debug("trying to bind with DN: {}", user_dn);
            ctx = new InitialDirContext(ldapConfig);

            Attributes users_attr = ctx.getAttributes(user_dn,attribs);

            String ldapCtxId = null;
            if (hasContextIdAttr) {
                Attribute attr = users_attr.get(config.contextIdAttr);
                if (null != attr) {
                    ldapCtxId = (String) attr.get(0);
                }
            }

            String login;
            {
                Attribute attr = users_attr.get(config.loginAttr);
                if (null == attr) {
                    LOG.error("unable to get ox login name from ldap attribute {}", config.loginAttr);
                    throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
                }
                login = (String) attr.get(0);
            }

            // ### Needed for password expired check against ldap ###
            {
                Attribute shadowlastchange = users_attr.get("shadowLastChange");
                Attribute shadowmax = users_attr.get("shadowMax");
                if (shadowlastchange != null && shadowmax != null) {
                    try {
                        long shadowlastchange_days = Long.parseLong(((String) shadowlastchange.get()));
                        long shadowmax_days = Long.parseLong(((String) shadowmax.get()));
                        LOG.debug("Found shadowlastchange ({}) and shadowmax({}) in ldap! NOW calculating!", shadowlastchange_days, shadowmax_days);

                        /**
                         * Bug #12593
                         * Check if password is already expired.
                         * This is done by calculating the sum of the both shadow attributes,
                         * if the sum is lower than day count since 1.1.1970 then password is expired
                         */
                        long days_since_1970 = System.currentTimeMillis() / 86400000;
                        long sum_up = shadowlastchange_days + shadowmax_days;
                        if (sum_up < days_since_1970) {
                            LOG.info("Password for account \"{}\" seems to be expired({}<{})!", login, sum_up, days_since_1970);
                            throw LoginExceptionCodes.PASSWORD_EXPIRED.create(config.passwordChangeURL.toString());
                        }
                    } catch (Exception whatever) {
                        LOG.error("LDAP Attributes shadowlastchange or/and shadowmax contain invalid values! Considering password for account \"{}\" as expired!", login, whatever);
                        throw LoginExceptionCodes.PASSWORD_EXPIRED.create(config.passwordChangeURL.toString());
                    }
                } else {
                    LOG.debug("LDAP Attributes shadowlastchange and shadowmax not found in LDAP, no password expiry calculation will be done!");
                }
            }

            String contextIdOrName;
            if (null != ldapCtxId) {
                LOG.debug("Bind with DN successfull, using context id {} as found in ldap attribute {} as context", ldapCtxId, config.contextIdAttr);
                contextIdOrName = ldapCtxId;
            } else {
                // Fetch the users mail attribute and parse the configured attribute to get the context name (domain part of email in this case)
                LOG.debug("Bind with DN successfull, now parsing attribute {} to resolve context", config.mailAttr);
                final Attribute emailattrib = users_attr.get(config.mailAttr);

                int numOfEmailAttribs = emailattrib.size();
                if (numOfEmailAttribs != 1) {
                    if (numOfEmailAttribs == 0) {
                        // No mailAttr value found, cannot resolve correct context
                        LOG.error("Fatal, no {} value found, cannot resolve correct context", config.mailAttr);
                        throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
                    }

                    // Otherwise more than one mailAttr value found, cannot resolve correct context
                    LOG.error("Fatal, more than one {} value found, cannot resolv correct context", config.mailAttr);
                    throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
                }

                String[] data = Strings.splitBy((String) emailattrib.get(), '@', false);
                if (data.length != 2) {
                    LOG.error("Fatal, Email address {} could not be parsed!", emailattrib.get());
                    throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
                }

                contextIdOrName = data[1];
            }

            LOG.debug("Returning context={}, user={} to OX API!", contextIdOrName, login);

            // return username AND context-name to the OX API
            return new AuthenticatedImpl(login, contextIdOrName);
        } catch (final InvalidNameException e) {
            LOG.error("Invalid name error", e);
            throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
        } catch (final AuthenticationException e) {
            LOG.info("Authentication against ldap server failed", e);
            throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
        } catch (final NamingException e) {
            LOG.error("Error setup initial ldap environment!", e);
            throw LoginExceptionCodes.COMMUNICATION.create(e);
        } catch (final NullPointerException e1) {
            LOG.error("Internal error!", e1);
            throw LoginExceptionCodes.COMMUNICATION.create(e1);
        } finally {
            if (null != ctx) {
                try {
                    ctx.close();
                } catch (final NamingException e) {
                    LOG.error("", e);
                }
            }
        }
    }

    /**
     * Escape string to be used as LDAP filter expression.
     *
     * @param input The input to escape
     * @return The escaped input ready for being used as LDAP filter expression
     */
    private static final String escapeString(String input) {
        StringBuffer sb = null;
        for (int i = 0; i < input.length(); i++) {
            char cur = input.charAt(i);
            switch (cur) {
            case '\\':
                if (null == sb) {
                    sb = initStringBuilder(input, i);
                }
                sb.append("\\5c");
                break;
            case '*':
                if (null == sb) {
                    sb = initStringBuilder(input, i);
                }
                sb.append("\\2a");
                break;
            case '(':
                if (null == sb) {
                    sb = initStringBuilder(input, i);
                }
                sb.append("\\28");
                break;
            case ')':
                if (null == sb) {
                    sb = initStringBuilder(input, i);
                }
                sb.append("\\29");
                break;
            case '\u0000':
                if (null == sb) {
                    sb = initStringBuilder(input, i);
                }
                sb.append("\\00");
                break;
            default:
                if (null != sb) {
                    sb.append(cur);
                }
            }
        }
        return null == sb ? input : sb.toString();
    }

    private static StringBuffer initStringBuilder(String input, int currentPos) {
        StringBuffer sb = new StringBuffer(input.length() + 10);
        if (currentPos > 0) {
            sb.append(input, 0, currentPos);
        }
        return sb;
    }

    private Config parseConfigFrom(Properties props) throws OXException {
        Config.Builder config = new Config.Builder();

        String ldapUrl = props.getProperty(LDAPURL_OPTION);
        if (null == ldapUrl || ldapUrl.length() == 0) {
            OXException e = LoginExceptionCodes.UNKNOWN.create("Missing option " + LDAPURL_OPTION);
            LOG.error("", e);
            throw e;
        }
        config.withLdapUrl(ldapUrl);

        {
            String baseDn = props.getProperty(LDAPBASE_OPTION);
            if (null == baseDn || baseDn.length() == 0) {
                OXException e = LoginExceptionCodes.UNKNOWN.create("Missing option " + LDAPBASE_OPTION);
                LOG.error("", e);
                throw e;
            }
            config.withBaseDn(baseDn);
        }

        {
            String searchFilter = props.getProperty(SEARCHFILTER_OPTION);
            if (null == searchFilter || searchFilter.length() == 0) {
                OXException e = LoginExceptionCodes.UNKNOWN.create("Missing option " + SEARCHFILTER_OPTION);
                LOG.error("", e);
                throw e;
            }
            config.withSearchFilter(searchFilter);
        }

        {
            String mailAttr = props.getProperty(MAILATTR_OPTION);
            if (null == mailAttr || mailAttr.length() == 0) {
                OXException e = LoginExceptionCodes.UNKNOWN.create("Missing option " + MAILATTR_OPTION);
                LOG.error("", e);
                throw e;
            }
            config.withMailAttr(mailAttr);
        }

        {
            String loginAttr = props.getProperty(LOGINATTR_OPTION);
            if (null == loginAttr || loginAttr.length() == 0) {
                OXException e = LoginExceptionCodes.UNKNOWN.create("Missing option " + LOGINATTR_OPTION);
                LOG.error("", e);
                throw e;
            }
            config.withLoginAttr(loginAttr);
        }

        {
            String sURL = (String) props.get(PASSWORD_CHANGE_URL_OPTION);
            if (null == sURL || sURL.length() == 0) {
                OXException e = LoginExceptionCodes.UNKNOWN.create("Missing option " + PASSWORD_CHANGE_URL_OPTION);
                LOG.error("", e);
                throw e;
            }
            try {
                config.withPasswordChangeURL(new URL(sURL));
            } catch (MalformedURLException e) {
                throw LoginExceptionCodes.UNKNOWN.create(e, "Invalid option " + PASSWORD_CHANGE_URL_OPTION + ": " + sURL);
            }
        }

        {
            Hashtable<String, String> ldapConfigDefaults = new Hashtable<String, String>(6);
            String usepool = (String) props.get(LDAPPOOL_OPTION);
            if (null != usepool && usepool.trim().equalsIgnoreCase("true")) {
                ldapConfigDefaults.put("com.sun.jndi.ldap.connect.pool", "true");
            }

            ldapConfigDefaults.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");

            ldapConfigDefaults.put(Context.PROVIDER_URL, ldapUrl);
            if (ldapUrl.startsWith("ldaps")) {
                ldapConfigDefaults.put("java.naming.ldap.factory.socket", TrustAllSSLSocketFactory.class.getName());
            }
            config.withLdapConfigDefaults(ldapConfigDefaults);
        }

        config.withBinddn(props.getProperty(BINDDN_OPTION));
        config.withBindpw(props.getProperty(BINDPW_OPTION));

        config.withContextIdAttr(props.getProperty(CONTEXTIDATTR_OPTION));
        return config.build();
    }

    @Override
    public Authenticated handleAutoLoginInfo(LoginInfo loginInfo) throws OXException {
        throw LoginExceptionCodes.NOT_SUPPORTED.create(UCSAuthentication.class.getName());
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        Properties properties = configService.getFile(CONFIGFILE);
        try {
            configReference.set(parseConfigFrom(properties));
        } catch (OXException e) {
            LOG.error("Error reloading configuration for bundle com.openexchange.authentication.ucs: {}", e);
        }
    }

    @Override
    public Interests getInterests() {
        return DefaultInterests.builder().configFileNames(CONFIGFILE).build();
    }

}
