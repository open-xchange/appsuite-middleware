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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Properties;
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
import javax.security.auth.login.LoginException;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.AuthenticationService;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.authentication.LoginInfo;
import com.openexchange.exception.OXException;

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
public class UCSAuthentication implements AuthenticationService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UCSAuthentication.class);
    private static Properties props;

    private static Hashtable<String, String> LDAP_CONFIG = null;
    private final static String LDAP_PROPERTY_FILE = "/opt/open-xchange/etc/authplugin.properties";
    
    private static final String PASSWORD_CHANGE_URL_OPTION = "com.openexchange.authentication.ucs.passwordChangeURL";
    
    private static URL passwordChangeURL = null;

    /**
     * Default constructor.
     */
    public UCSAuthentication() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Authenticated handleLoginInfo(final LoginInfo loginInfo) throws OXException {

        DirContext ctx = null;
        NamingEnumeration<SearchResult> result = null;

        try {

            initConfig();
            initLdap();


            if (loginInfo.getUsername()==null || loginInfo.getPassword()==null) {
                throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
            }

            final String[] splitted = split(loginInfo.getUsername());

            LOG.debug("Splitted:{}", Arrays.toString(splitted));


            if (splitted[0] == null || splitted[0].length() == 0 || splitted[0].equals("defaultcontext")){
                splitted[0] = (String) props.get("OX_DEFAULTCONTEXT");
            }

            final String context_or_domain = splitted[0];

            LOG.debug("Context is {}", context_or_domain);

            final String uid = splitted[1];
            final String password = loginInfo.getPassword();

            if ("".equals(uid.trim()) || "".equals(password.trim())) {
                throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
            }

            // we have no context part in this auth, context is resolved from ldap later
            if (context_or_domain == null) {

                // search ldap server without any credentials to get the users dn to bind with
                LDAP_CONFIG.put(Context.SECURITY_AUTHENTICATION, "none");
                ctx = new InitialDirContext(LDAP_CONFIG);
                final SearchControls sc = new SearchControls();
                sc.setSearchScope(SearchControls.SUBTREE_SCOPE);

                String search_pattern = (String) props.get("LDAP_SEARCH");
                search_pattern = search_pattern.replaceFirst("@USER@", uid);


                result = ctx.search("",search_pattern,sc);

                LOG.debug("Now searching on server {} for DN of User {} with BASE: {} and pattern {}", LDAP_CONFIG.get(Context.PROVIDER_URL), uid, props.get("LDAP_BASE"), search_pattern);

                String user_dn = null;
                String user_part = null;
                int count = 0;
                while(result.hasMoreElements()){
                    final SearchResult sr = result.next();
                    user_part = sr.getName();
                    LOG.debug("User found : {}", sr.getName());
                    user_dn = sr.getName()+","+(String) props.get("LDAP_BASE");
                    count++;
                }

                if(count!=1){
                    // found more than 1 user or no user , this is not good :)
                    LOG.debug("User {} not found in LDAP", uid);
                    throw LoginExceptionCodes.INVALID_CREDENTIALS_MISSING_USER_MAPPING.create(uid);
                }
                if (null != ctx) {
                    try {
                        // unbind old context
                        ctx.close();
                    } catch (final NamingException e) {
                        LOG.error("", e);
                    }
                }

                // after we found the users dn, auth with this dn and given password
                LDAP_CONFIG.put(Context.SECURITY_AUTHENTICATION, "simple");
                LDAP_CONFIG.put(Context.SECURITY_PRINCIPAL,user_dn);
                LDAP_CONFIG.put(Context.SECURITY_CREDENTIALS, password);

                LOG.debug("NOW trying to bind with DN: {} to fetch Attribute {}", user_dn, props.get("LDAP_ATTRIBUTE"));
                ctx = new InitialDirContext(LDAP_CONFIG);

                final String[] attribs = {(String) props.get("LDAP_ATTRIBUTE"),"shadowLastChange","shadowMax"};
                final Attributes users_attr = ctx.getAttributes(user_part,attribs);

                // Fetch the users mail attribute and parse the configured attribute to get the context name (domain part of email in this case)
                LOG.debug("Bind with DN successfull!\nNow parsing attribute "+(String) props.get("LDAP_ATTRIBUTE")+" to resolv context!");
                final Attribute emailattrib = users_attr.get((String) props.get("LDAP_ATTRIBUTE"));

                // ### Needed for password expired check against ldap ###
                final Attribute shadowlastchange = users_attr.get("shadowLastChange");
                final Attribute shadowmax = users_attr.get("shadowMax");
                long shadowlastchange_days = 0;
                long shadowmax_days = 0;
                if(shadowlastchange != null && shadowmax != null){

                    try{
                        shadowlastchange_days = Long.parseLong(((String)shadowlastchange.get()));
                        shadowmax_days = Long.parseLong(((String)shadowmax.get()));
                        LOG.debug("Found  shadowlastchange ({}) and shadowmax({}) in ldap! NOW calculating!", shadowlastchange_days, shadowmax_days);
                    }catch(final Exception exp){
                        LOG.error("LDAP Attributes shadowlastchange or/and shadowmax contain invalid values!",exp);
                    }

                    /**
                     * Bug #12593
                     * Check if password is already expired.
                     * This is done by calculating the sum of the both shadow attributes,
                     * if the sum is lower than day count since 1.1.1970 then password is expired
                     */
                    final Calendar cal = Calendar.getInstance();
                    final long days_since_1970 = cal.getTimeInMillis()/86400000;
                    final long sum_up = shadowlastchange_days+shadowmax_days;
                    if(sum_up<days_since_1970){
                        LOG.info("Password for account \"{}\" seems to be expired({}<{})!", uid, sum_up, days_since_1970);
                        throw LoginExceptionCodes.PASSWORD_EXPIRED.create(passwordChangeURL.toString());
                    }
                }else{
                    LOG.debug("LDAP Attributes shadowlastchange and shadowmax NOT found in LDAP! No password expired calculation will be done!");
                }



                if(emailattrib.size()!=1){
                    // more than one (String) props.get("LDAP_ATTRIBUTE") value found, cannot resolve correct context
                    LOG.error("FATAL! More than one "+(String) props.get("LDAP_ATTRIBUTE")+" value found, cannot resolv correct context");
                    throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
                }else{
                    final String[] data  = ((String)emailattrib.get()).split("@");
                    if(data.length!=2){
                        LOG.error("FATAL! Email address {} could not be splitted correctly!!", emailattrib.get());
                        throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
                    }else{
                        splitted[0] = data[1];
                        splitted[1] = uid;
                        LOG.debug("Returning {} to OX API!", Arrays.toString(splitted));
                        // return username AND context-name to the OX API
                        return new Authenticated() {
                            @Override
                            public String getContextInfo() {
                                return splitted[0];
                            }
                            @Override
                            public String getUserInfo() {
                                return splitted[1];
                            }
                        };
                    }
                }
            } else {
                throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
            }
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
            if (null != result) {
                try {
                    result.close();
                } catch (NamingException e) {
                    LOG.error("", e);
                }
            }
            if (null != ctx) {
                try {
                    ctx.close();
                } catch (final NamingException e) {
                    LOG.error("", e);
                }
            }
        }





    }

    private void initLdap() throws NamingException {
        if (LDAP_CONFIG == null) {
            LDAP_CONFIG = new Hashtable<String, String>();

        }
        final String usepool = (String) props.get("USE_POOL");
        if (usepool.trim().equalsIgnoreCase("true")) {
            LDAP_CONFIG.put("com.sun.jndi.ldap.connect.pool", "true");
        }

        LDAP_CONFIG.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");

        // we choose normal ldap without secure socket,
        LDAP_CONFIG.put(Context.PROVIDER_URL, "ldap://"+ (String) props.get("LDAP_HOST") + ":"+ (String) props.get("LDAP_PORT") + "/"+ (String) props.get("LDAP_BASE"));

    }

    private static void initConfig() throws OXException {
        synchronized (UCSAuthentication.class) {

            if (null == props) {
                final File file = new File(LDAP_PROPERTY_FILE);
                if (!file.exists()) {
                    throw LoginExceptionCodes.MISSING_PROPERTY.create(file.getAbsolutePath());
                }
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(file);
                    props = new Properties();
                    props.load(fis);
                    if( props.containsKey(PASSWORD_CHANGE_URL_OPTION) && ((String)props.get(PASSWORD_CHANGE_URL_OPTION)).length() > 0 ) {
                        passwordChangeURL = new URL((String)props.get(PASSWORD_CHANGE_URL_OPTION));
                    } else {
                        OXException e = LoginExceptionCodes.UNKNOWN.create("Missing option " + PASSWORD_CHANGE_URL_OPTION);
                        LOG.error("",e);
                        throw e;
                    }
                } catch (final IOException e) {
                    LOG.error("",e);
                    throw LoginExceptionCodes.UNKNOWN.create(file.getAbsolutePath());
                } finally {
                    try {
                        if (null != fis) {
                            fis.close();
                        }
                    } catch (final IOException e) {
                        LOG.error("",e);
                        throw LoginExceptionCodes.UNKNOWN.create("Error closing stream for file:"+file.getAbsolutePath());
                    }
                }
            }
        }
    }

    /**
     * Splits user name and context.
     *
     * @param loginInfo
     * combined information seperated by an @ sign.
     * @return a string array with context and user name (in this order).
     * @throws LoginException
     *             if no seperator is found.
     */
    private String[] split(final String loginInfo) {
        return split(loginInfo, '@');
    }

    /**
     * Splits user name and context.
     *
     * @param loginInfo
     * combined information seperated by an @ sign.
     * @param separator
     *            for spliting user name and context.
     * @return a string array with context and user name (in this order).
     * @throws LoginException
     *             if no seperator is found.
     */
    private String[] split(final String loginInfo, final char separator) {
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
    public Authenticated handleAutoLoginInfo(LoginInfo loginInfo) throws OXException {
        throw LoginExceptionCodes.NOT_SUPPORTED.create(UCSAuthentication.class.getName());
    }

}
