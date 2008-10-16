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

package com.openexchange.authentication.ldap;

import java.util.Properties;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.authentication.AuthenticationService;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.authentication.LoginInfo;
import com.openexchange.authentication.LoginException;
import com.openexchange.tools.ssl.TrustAllSSLSocketFactory;

/**
 * This class implements the login by using an LDAP for authentication.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class LDAPAuthentication implements AuthenticationService {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(LDAPAuthentication.class);

    /**
     * Properties for the JNDI context.
     */
    private final Properties props;

    /**
     * attribute name and base DN.
     */
    private String uidAttribute, baseDN;

    /**
     * Default constructor.
     * @throws LoginException if setup fails.
     */
    public LDAPAuthentication(final Properties props) throws LoginException {
        super();
        this.props = props;
        init();
    }

    /**
     * {@inheritDoc}
     */
    public Authenticated handleLoginInfo(final LoginInfo loginInfo)
        throws LoginException {
        final String[] splitted = split(loginInfo.getUsername());
        final String uid = splitted[1];
        final String password = loginInfo.getPassword();
        if ("".equals(uid) || "".equals(password)) {
            throw new LoginException(LoginExceptionCodes.INVALID_CREDENTIALS);
        }
        bind(uid, password);
        return new Authenticated() {
            public String getContextInfo() {
                return splitted[0];
            }
            public String getUserInfo() {
                return splitted[1];
            }
        };
    }

    /**
     * Tries to bind.
     * @param uid login name.
     * @param password password.
     * @throws LoginException if some problem occurs.
     */
    private void bind(final String uid, final String password)
        throws LoginException {
        LdapContext context = null;
        try {
            context = createContext();
            context.addToEnvironment(Context.SECURITY_PRINCIPAL, uidAttribute
                + "=" + uid + "," + baseDN);
            context.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
            context.reconnect(null);
        } catch (InvalidNameException e) {
            throw new LoginException(LoginExceptionCodes.INVALID_CREDENTIALS, e);
        } catch (AuthenticationException e) {
            throw new LoginException(LoginExceptionCodes.INVALID_CREDENTIALS, e);
        } catch (NamingException e) {
            LOG.error(e.getMessage(), e);
            throw new LoginException(LoginExceptionCodes.COMMUNICATION, e);
        } finally {
            if (null != context) {
                try {
                    context.close();
                } catch (NamingException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Creates a new context to the ldap server.
     * @return a new context to the ldap server.
     * @throws LoginException
     *             if creating a context fails.
     */
    private LdapContext createContext() throws LoginException {
        try {
            return new InitialLdapContext(props, null);
        } catch (NamingException e) {
            throw new LoginException(LoginExceptionCodes.COMMUNICATION, e);
        }
    }

    /**
     * Initializes the properties for the ldap authentication.
     * @throws LoginException if configuration fails.
     */
    private void init() throws LoginException {
        if (!props.containsKey("uidAttribute")) {
            throw new LoginException(LoginExceptionCodes.MISSING_PROPERTY, "uidAttribute");
        }
        uidAttribute = props.getProperty("uidAttribute");
        if (!props.containsKey("baseDN")) {
            throw new LoginException(LoginExceptionCodes.MISSING_PROPERTY, "baseDN");
        }
        baseDN = props.getProperty("baseDN");
        props.put(LdapContext.INITIAL_CONTEXT_FACTORY,
            "com.sun.jndi.ldap.LdapCtxFactory");
        final String url = props.getProperty(LdapContext.PROVIDER_URL);
        if (null == url) {
            throw new LoginException(LoginExceptionCodes.MISSING_PROPERTY, LdapContext.PROVIDER_URL);
        } else if (url.startsWith("ldaps")) {
            props.put("java.naming.ldap.factory.socket",
                TrustAllSSLSocketFactory.class.getName());
        }
    }

    /**
     * Splits user name and context.
     * @param loginInfo combined information separated by an @ sign.
     * @return a string array with context and user name (in this order).
     * @throws LoginException if no separator is found.
     */
    private String[] split(final String loginInfo) {
        return split(loginInfo, '@');
    }

    /**
     * Splits user name and context.
     * @param loginInfo combined information separated by an @ sign.
     * @param character for splitting user name and context.
     * @return a string array with context and user name (in this order).
     * @throws LoginException if no separator is found.
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
}
