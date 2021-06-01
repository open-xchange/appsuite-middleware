/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.user;

import static com.openexchange.admin.storage.mysqlStorage.OXUtilMySQLStorageCommon.isEmpty;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
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

    final AdminCache adminCache;
    private static final String TABLE = "user";

    /**
     * Initialises a new {@link UserAttributeChangers}.
     */
    public UserAttributeChangers(AdminCache adminCache) {
        super(TABLE, EnumSet.allOf(UserAttribute.class));
        this.adminCache = adminCache;
    }

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
                return setAttributes(userId, contextId, TABLE, Collections.singletonMap(UserAttribute.SHADOW_LAST_CHANGE, I(lastChange)), connection);
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
                    Map<Attribute, Object> attributes = new HashMap<>();
                    attributes.put(UserAttribute.USER_PASSWORD, passwordDetails.getEncodedPassword());
                    attributes.put(UserAttribute.SALT, passwordDetails.getSalt());
                    attributes.put(UserAttribute.PASSWORD_MECH, passwordDetails.getPasswordMech());
                    return setAttributes(userId, contextId, TABLE, attributes, connection);
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

                return setAttributes(userId, contextId, TABLE, Collections.singletonMap(UserAttribute.QUOTA, L(quota_max_temp)), connection);
            }
        });
        return Collections.unmodifiableMap(c);
    }
}
