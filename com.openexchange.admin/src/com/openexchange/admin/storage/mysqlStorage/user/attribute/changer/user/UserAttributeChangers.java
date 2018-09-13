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

package com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.user;

import static com.openexchange.admin.storage.mysqlStorage.OXUtilMySQLStorageCommon.isEmpty;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.AbstractUserAttributeChangers;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.Attribute;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.UserAttributeChanger;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.java.Strings;
import com.openexchange.password.mechanism.PasswordDetails;
import com.openexchange.tools.net.URIDefaults;
import com.openexchange.tools.net.URIParser;

/**
 * {@link UserAttributeChangers}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class UserAttributeChangers extends AbstractUserAttributeChangers {

    private final AdminCache adminCache;
    private static final String TABLE = "user";

    /**
     * Initialises a new {@link UserAttributeChangers}.
     */
    public UserAttributeChangers(AdminCache adminCache) {
        super(TABLE, EnumSet.allOf(UserAttribute.class));
        this.adminCache = adminCache;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.AbstractUserAttributeChangers#initialiseChangers()
     */
    @Override
    protected Map<Attribute, UserAttributeChanger> initialiseChangers() {
        Map<UserAttribute, UserAttributeChanger> c = new HashMap<>();
        c.put(UserAttribute.MAIL, new AbstractUserAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                String primaryEmail = userData.getPrimaryEmail();
                if (Strings.isEmpty(primaryEmail)) {
                    return false;
                }
                return setAttributes(userId, contextId, TABLE, Collections.singletonMap(UserAttribute.MAIL, primaryEmail), connection);
            }
        });
        c.put(UserAttribute.PREFERRED_LANGUAGE, new AbstractUserAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                String language = userData.getLanguage();
                if (Strings.isEmpty(language)) {
                    return false;
                }
                return setAttributes(userId, contextId, TABLE, Collections.singletonMap(UserAttribute.PREFERRED_LANGUAGE, language), connection);
            }
        });
        c.put(UserAttribute.TIMEZONE, new AbstractUserAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                String timezone = userData.getTimezone();
                if (Strings.isEmpty(timezone)) {
                    return false;
                }
                return setAttributes(userId, contextId, TABLE, Collections.singletonMap(UserAttribute.TIMEZONE, timezone), connection);
            }
        });
        c.put(UserAttribute.MAIL_ENABLED, new AbstractUserAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                Boolean mailEnabled = userData.getMailenabled();
                if (mailEnabled == null) {
                    return false;
                }
                return setAttributes(userId, contextId, TABLE, Collections.singletonMap(UserAttribute.MAIL_ENABLED, mailEnabled), connection);
            }
        });
        c.put(UserAttribute.SHADOW_LAST_CHANGE, new AbstractUserAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                Boolean passwordExpired = userData.getPassword_expired();
                if (passwordExpired == null) {
                    return false;
                }
                int lastChange = passwordExpired.booleanValue() ? 0 : -1;
                return setAttributes(userId, contextId, TABLE, Collections.singletonMap(UserAttribute.SHADOW_LAST_CHANGE, lastChange), connection);
            }
        });
        c.put(UserAttribute.IMAP_SERVER, new AbstractUserAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                if (isEmpty(userData.getImapServerString()) && userData.isImapServerset()) {
                    return unsetAttributes(userId, contextId, TABLE, Collections.singleton(UserAttribute.IMAP_SERVER), connection);
                } else if (!isEmpty(userData.getImapServerString())) {
                    try {
                        return setAttributes(userId, contextId, TABLE, Collections.singletonMap(UserAttribute.IMAP_SERVER, URIParser.parse(userData.getImapServerString(), URIDefaults.IMAP).toString()), connection);
                    } catch (URISyntaxException e) {
                        // TODO: throw storage exception?
                    }
                }
                return false;
            }
        });
        c.put(UserAttribute.IMAP_LOGIN, new AbstractUserAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                if (isEmpty(userData.getImapLogin()) && userData.isImapLoginset()) {
                    return unsetAttributes(userId, contextId, TABLE, Collections.singleton(UserAttribute.IMAP_LOGIN), connection);
                } else if (!isEmpty(userData.getImapLogin())) {
                    return setAttributes(userId, contextId, TABLE, Collections.singletonMap(UserAttribute.IMAP_LOGIN, userData.getImapLogin()), connection);
                }
                return false;
            }
        });
        c.put(UserAttribute.SMTP_SERVER, new AbstractUserAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                if (isEmpty(userData.getSmtpServerString()) && userData.isSmtpServerset()) {
                    return unsetAttributes(userId, contextId, TABLE, Collections.singleton(UserAttribute.SMTP_SERVER), connection);
                } else if (!isEmpty(userData.getSmtpServerString())) {
                    try {
                        return setAttributes(userId, contextId, TABLE, Collections.singletonMap(UserAttribute.SMTP_SERVER, URIParser.parse(userData.getSmtpServerString(), URIDefaults.IMAP).toString()), connection);
                    } catch (URISyntaxException e) {
                        // TODO: throw storage exception?
                    }
                }
                return false;
            }
        });
        c.put(UserAttribute.USER_PASSWORD, new AbstractUserAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                if (Strings.isEmpty(userData.getPassword())) {
                    return false;
                }
                try {
                    PasswordDetails passwordDetails = adminCache.encryptPassword(userData);
                    boolean passwordSet = setAttributes(userId, contextId, TABLE, Collections.singletonMap(UserAttribute.USER_PASSWORD, passwordDetails.getEncodedPassword()), connection);
                    setAttributes(userId, contextId, TABLE, Collections.singletonMap(UserAttribute.SALT, passwordDetails.getSalt()), connection);
                    setAttributes(userId, contextId, TABLE, Collections.singletonMap(UserAttribute.PASSWORD_MECH, passwordDetails.getPasswordMech()), connection);
                    return passwordSet;
                } catch (StorageException e) {
                    // TODO: throw storage exception?
                }
                return false;
            }
        });
        c.put(UserAttribute.PASSWORD_MECH, new AbstractUserAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                String passwordMech = userData.getPasswordMech();
                if (Strings.isEmpty(passwordMech)) {
                    return false;
                }
                return setAttributes(userId, contextId, TABLE, Collections.singletonMap(UserAttribute.PASSWORD_MECH, passwordMech), connection);
            }
        });
        c.put(UserAttribute.QUOTA, new AbstractUserAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                // Check if max quota is set for user
                Long maxQuota = userData.getMaxQuota();
                if (maxQuota == null) {
                    return false;
                }
                long quota_max_temp = maxQuota.longValue();
                if (quota_max_temp != -1) {
                    quota_max_temp = quota_max_temp << 20;
                }

                return setAttributes(userId, contextId, TABLE, Collections.singletonMap(UserAttribute.QUOTA, quota_max_temp), connection);
            }
        });
        return Collections.unmodifiableMap(c);
    }
}
