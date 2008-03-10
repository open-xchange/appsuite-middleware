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

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.imap.IMAPPropertiesFactory;
import com.openexchange.imap.IMAPPropertyException;
import com.openexchange.imap.connection.DefaultIMAPConnection;
import com.openexchange.sessiond.LoginException;
import com.openexchange.sessiond.LoginException.Code;

/**
 * This class implements the login by using an IMAP server for authentication.
 * @author choeger
 */
public class LoginIMAPAuth extends LoginInfo {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(LoginIMAPAuth.class);

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

        DefaultIMAPConnection con = null;
        try {
            String imapServer = 
                    IMAPPropertiesFactory.getProperties().getProperty(IMAPPropertiesFactory.PROP_IMAPSERVER);
            con = new DefaultIMAPConnection();
            int imapPort = 143;
            int pos = imapServer.indexOf(':');
            if (pos > -1) {
                    imapPort = Integer.parseInt(imapServer.substring(pos + 1));
                    imapServer = imapServer.substring(0, pos);
            }
            con.setImapServer(imapServer, imapPort);
            con.setUsername(uid);
            con.setPassword(password);
            con.connect();
        } catch (IMAPPropertyException e) {
            throw new LoginException(Code.INVALID_CREDENTIALS, e);
        } catch (NoSuchProviderException e) {
            throw new LoginException(Code.INVALID_CREDENTIALS, e);
        } catch (MessagingException e) {
            throw new LoginException(Code.INVALID_CREDENTIALS, e);
        } finally {
            if( con != null ) {
                try {
                    con.close();
                } catch (MessagingException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
        return splitted;
    }

}
