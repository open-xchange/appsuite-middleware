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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.admin.storage.mysqlStorage.user.attribute.changer;

import static com.openexchange.admin.storage.mysqlStorage.OXUtilMySQLStorageCommon.isEmpty;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.tools.net.URIDefaults;
import com.openexchange.tools.net.URIParser;

/**
 * {@link UserAttributeChangers}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class UserAttributeChangers {

    private static final Logger LOG = LoggerFactory.getLogger(UserAttributeChangers.class);

    private static final Map<UserAttribute, UserAttributeChanger> changers;
    static {
        changers = new HashMap<>();
        changers.put(UserAttribute.MAIL, new AbstractUserAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                return setAttribute(userId, contextId, UserAttribute.MAIL, userData.getPrimaryEmail(), connection);
            }
        });
        changers.put(UserAttribute.PREFERRED_LANGUAGE, new AbstractUserAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                return setAttribute(userId, contextId, UserAttribute.PREFERRED_LANGUAGE, userData.getLanguage(), connection);
            }
        });
        changers.put(UserAttribute.TIMEZONE, new AbstractUserAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                return setAttribute(userId, contextId, UserAttribute.TIMEZONE, userData.getTimezone(), connection);
            }
        });
        changers.put(UserAttribute.MAIL_ENABLED, new AbstractUserAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                return setAttribute(userId, contextId, UserAttribute.MAIL_ENABLED, userData.getMailenabled(), connection);
            }
        });
        changers.put(UserAttribute.SHADOW_LAST_CHANGE, new AbstractUserAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                return setAttribute(userId, contextId, UserAttribute.SHADOW_LAST_CHANGE, userData.getPassword_expired(), connection);
            }
        });
        changers.put(UserAttribute.IMAP_SERVER, new AbstractUserAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                if (isEmpty(userData.getImapServerString()) && userData.isImapServerset()) {
                    return unsetAttribute(userId, contextId, UserAttribute.IMAP_SERVER, connection);
                } else if (!isEmpty(userData.getImapServerString())) {
                    try {
                        return setAttribute(userId, contextId, UserAttribute.IMAP_SERVER, URIParser.parse(userData.getImapServerString(), URIDefaults.IMAP).toString(), connection);
                    } catch (URISyntaxException e) {
                        // TODO: throw storage exception?
                    }
                }
                return false;
            }
        });
        changers.put(UserAttribute.IMAP_LOGIN, new AbstractUserAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                if (isEmpty(userData.getImapLogin()) && userData.isImapLoginset()) {
                    return unsetAttribute(userId, contextId, UserAttribute.IMAP_LOGIN, connection);
                } else if (!isEmpty(userData.getImapLogin())) {
                    return setAttribute(userId, contextId, UserAttribute.IMAP_LOGIN, userData.getImapLogin(), connection);
                }
                return false;
            }
        });
        changers.put(UserAttribute.SMTP_SERVER, new AbstractUserAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                if (isEmpty(userData.getSmtpServerString()) && userData.isSmtpServerset()) {
                    return unsetAttribute(userId, contextId, UserAttribute.SMTP_SERVER, connection);
                } else if (!isEmpty(userData.getSmtpServerString())) {
                    try {
                        return setAttribute(userId, contextId, UserAttribute.SMTP_SERVER, URIParser.parse(userData.getSmtpServerString(), URIDefaults.IMAP).toString(), connection);
                    } catch (URISyntaxException e) {
                        // TODO: throw storage exception?
                    }
                }
                return false;
            }
        });
        changers.put(UserAttribute.USER_PASSWORD, new AbstractUserAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                try {
                    return setAttribute(userId, contextId, UserAttribute.USER_PASSWORD, ClientAdminThread.cache.encryptPassword(userData), connection);
                } catch (NoSuchAlgorithmException | UnsupportedEncodingException | StorageException e) {
                    // TODO: throw storage exception?
                }
                return false;
            }
        });
        changers.put(UserAttribute.PASSWORD_MECH, new AbstractUserAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                return setAttribute(userId, contextId, UserAttribute.PASSWORD_MECH, userData.getPasswordMech(), connection);
            }
        });
    }

    /**
     * Changes the specified {@link UserAttribute}
     * 
     * @param userAttribute The {@link UserAttribute} to change
     * @param userData The {@link User} data
     * @param userId the user identifier
     * @param contextId The context identifier
     * @param connection The {@link Connection} to use
     * @return <code>true</code> if the attribute was changed successfully; <code>false</code> otherwise
     * @throws SQLException if an SQL error is occurred
     */
    public static boolean change(UserAttribute userAttribute, User userData, int userId, int contextId, Connection connection) throws SQLException {
        UserAttributeChanger changer = changers.get(userAttribute);
        if (changer == null) {
            LOG.debug("No user attribute changer found for user attribute '{}'. The attribute will not be changed.", userAttribute.getSQLFieldName());
            return false;
        }
        return changer.changeAttribute(userId, contextId, userData, connection);
    }
}
