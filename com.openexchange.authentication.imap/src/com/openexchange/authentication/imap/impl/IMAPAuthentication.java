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

package com.openexchange.authentication.imap.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.AuthenticationService;
import com.openexchange.authentication.LoginException;
import com.openexchange.authentication.LoginInfo;
import com.openexchange.authentication.LoginException.Code;
import com.openexchange.configuration.ConfigurationException;

public class IMAPAuthentication implements AuthenticationService {

    private static final Log LOG = LogFactory.getLog(IMAPAuthentication.class);

    private static Properties props;

    private final static String IMAP_AUTH_PROPERTY_FILE = "/opt/open-xchange/etc/groupware/imapauth.properties";

    /**
     * Default constructor.
     */
    public IMAPAuthentication() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public Authenticated handleLoginInfo(final LoginInfo loginInfo) throws LoginException {
        // IMAPConnection def = null;

        Session session = null;
        Store imapconnection = null;

        String host = "localhost";
        int port = 143;
        boolean use_full_login = true;
        String user = null;
        String connectiontimeout = "4000";
        String imaptimeout = "4000";

        try {
            if (props == null) {
                initConfig();
            }

            final String[] splitted = split(loginInfo.getUsername());

            final String context_or_domain = splitted[0];
            final String uid = splitted[1];
            final String password = loginInfo.getPassword();
            if ("".equals(uid.trim()) || "".equals(password.trim())) {
                throw new LoginException(Code.INVALID_CREDENTIALS);
            }

            if (props.get("IMAP_TIMEOUT") != null) {
                imaptimeout = (String) props.get("IMAP_TIMEOUT");
            }

            if (props.get("IMAP_CONNECTIONTIMEOUT") != null) {
                connectiontimeout = (String) props.get("IMAP_CONNECTIONTIMEOUT");
            }

            Properties imapprops = new Properties();
            imapprops.put("mail.imap.connectiontimeout", connectiontimeout);
            imapprops.put("mail.imap.timeout", imaptimeout);

            session = Session.getDefaultInstance(imapprops, null);
            session.setDebug(false);

            imapconnection = session.getStore("imap");

            if (props.get("USE_FULL_LOGIN_INFO") != null) {
                use_full_login = Boolean.parseBoolean((String) props.get("USE_FULL_LOGIN_INFO"));
            }

            if (props.get("IMAP_SERVER") != null) {
                host = (String) props.get("IMAP_SERVER");
            }

            if (props.get("IMAP_PORT") != null) {
                port = Integer.parseInt((String) props.get("IMAP_PORT"));
            }

            LOG.debug("Using imap server: " + host);
            LOG.debug("Using imap port: " + port);
            LOG.debug("Using full login info: " + use_full_login);

            // set imap username
            if (use_full_login) {
                user = uid + "@" + context_or_domain;
            } else {
                user = uid;
            }

            // try to connect with the credentials set above
            imapconnection.connect(host, port, user, password);
            LOG.info("Imap authentication for user " + user + " successful on host " + host + ":" + port);

            /*
             * Set the context of the user, If full login was configured, we use
             * the domain part as the context name/mapping entry. If NO full
             * login was configured, we assume that only 1 context is in the
             * system which is named "defaultcontext".
             */
            if (use_full_login) {
                LOG.debug("Using domain: " + context_or_domain + " as context name!");
                splitted[0] = context_or_domain;
            } else {
                LOG.debug("Using \"defaultcontext\" as context name!");
                splitted[0] = "defaultcontext";
            }
            return new Authenticated() {
                public String getContextInfo() {
                    return splitted[0];
                }

                public String getUserInfo() {
                    return splitted[1];
                }
            };
        } catch (ConfigurationException e) {
            LOG.error("Error reading auth plugin config!", e);
            throw new LoginException(Code.COMMUNICATION, e);
        } catch (NoSuchProviderException e) {
            LOG.error("Error setup initial imap envorinment!", e);
            throw new LoginException(Code.COMMUNICATION, e);
        } catch (MessagingException e) {
            LOG.info("Authentication error on host " + host + ":" + port + " for user " + user, e);
            LOG.debug("Debug imap authentication, e");
            throw new LoginException(Code.INVALID_CREDENTIALS, e);
        } finally {
            try {
                if (imapconnection != null) {
                    imapconnection.close();
                }
            } catch (MessagingException e) {
                LOG.error("Error closing imap connection!", e);
                throw new LoginException(Code.COMMUNICATION, e);
            }
        }
    }

    private static void initConfig() throws ConfigurationException {
        synchronized (IMAPAuthentication.class) {
            if (null == props) {
                final File file = new File(IMAP_AUTH_PROPERTY_FILE);
                if (!file.exists()) {
                    throw new ConfigurationException(com.openexchange.configuration.ConfigurationException.Code.FILE_NOT_FOUND, file.getAbsolutePath());
                }
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(file);
                    props = new Properties();
                    props.load(fis);
                } catch (IOException e) {
                    throw new ConfigurationException(com.openexchange.configuration.ConfigurationException.Code.NOT_READABLE, file.getAbsolutePath());
                } finally {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        LOG.error("Error closing file inputstream for file " + IMAP_AUTH_PROPERTY_FILE + " ", e);
                    }
                }
            }
        }
    }

    /**
     * Splits user name and context.
     * 
     * @param loginInfo
     *            combined information seperated by an @ sign.
     * @return a string array with context and user name (in this order).
     * @throws LoginException
     *             if no seperator is found.
     */
    private String[] split(final String loginInfo) throws LoginException {
        return split(loginInfo, '@');
    }

    /**
     * Splits user name and context.
     * 
     * @param loginInfo
     *            combined information seperated by an @ sign.
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

}
