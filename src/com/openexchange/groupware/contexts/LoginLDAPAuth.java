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



package com.openexchange.groupware.contexts;

import static com.openexchange.tools.io.IOUtils.closeStreamStuff;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.configuration.ConfigurationException;
import com.openexchange.configuration.SystemConfig;
import com.openexchange.configuration.SystemConfig.Property;
import com.openexchange.sessiond.LoginException;
import com.openexchange.sessiond.LoginException.Code;

/**
 * This class implements the login by using an LDAP for authentication.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class LoginLDAPAuth extends LoginInfo {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(LoginLDAPAuth.class);

    /**
     * Properties for the JNDI context.
     */
    private static Properties props;

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] handleLoginInfo(final Object... loginInfo)
        throws LoginException {
        if (loginInfo.length < 2) {
            throw new LoginException(Code.MISSING_ATTRIBUTES, loginInfo.length);
        }
        final String[] splitted = split((String) loginInfo[0]);
        final String uid = splitted[1];
        final String password = (String) loginInfo[1];
        if ("".equals(uid) || "".equals(password)) {
            throw new LoginException(Code.INVALID_CREDENTIALS);
        }
        LdapContext context = null;
        try {
            context = createContext();
            context.addToEnvironment(Context.SECURITY_PRINCIPAL, "uid=" + uid
                + ",ou=Users,ou=OxObjects,dc=open-xchange,dc=com");
            context.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
            context.reconnect(null);
        } catch (InvalidNameException e) {
            throw new LoginException(Code.INVALID_CREDENTIALS, e);
        } catch (AuthenticationException e) {
            throw new LoginException(Code.INVALID_CREDENTIALS, e);
        } catch (NamingException e) {
            LOG.error(e.getMessage(), e);
            throw new LoginException(Code.COMMUNICATION, e);
        } finally {
            if (null != context) {
                try {
                    context.close();
                } catch (NamingException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
        return splitted;
    }

    /**
     * Creates a new context to the ldap server.
     * @return a new context to the ldap server.
     * @throws LoginException
     *             if creating a context fails.
     */
    private static LdapContext createContext() throws LoginException {
        try {
            initLDAP();
        } catch (ConfigurationException e) {
            throw new LoginException(e);
        }
        try {
            return new InitialLdapContext(props, null);
        } catch (NamingException e) {
            throw new LoginException(Code.COMMUNICATION, e);
        }
    }

    /**
     * Initializes the properties for the ldap auth.
     * @throws ConfigurationException if configuration fails.
     */
    private static void initLDAP() throws ConfigurationException {
        synchronized (LoginLDAPAuth.class) {
            if (null == props) {
                props = new Properties();
                final String fileName = SystemConfig.getProperty(Property.LDAP);
                if (null == fileName) {
                    throw new ConfigurationException(com.openexchange
                        .configuration.ConfigurationException.Code
                        .PROPERTY_MISSING, Property.LDAP.getPropertyName());
                }
                final File file = new File(fileName);
                if (!file.exists()) {
                    throw new ConfigurationException(com.openexchange
                        .configuration.ConfigurationException.Code
                        .FILE_NOT_FOUND, file.getAbsolutePath());
                }
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(file);
                    props.load(fis);
                } catch (IOException e) {
                    throw new ConfigurationException(com.openexchange
                        .configuration.ConfigurationException.Code
                        .NOT_READABLE, file.getAbsolutePath());
                } finally {
                    closeStreamStuff(fis);
                }
            }
        }
    }

}
