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

import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.Attribute;

/**
 * {@link UserAttribute}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public enum UserAttribute implements Attribute {

    /**
     * The 'mail' column
     */
    MAIL("primary-email", "mail", String.class),
    /**
     * The 'preferredlanguage' column
     */
    PREFERRED_LANGUAGE("preferred-language", "preferredlanguage", String.class),
    /**
     * The 'timezone' column
     */
    TIMEZONE("timezone", "timezone", String.class),
    /**
     * The 'mailEnabled' column
     */
    MAIL_ENABLED("mail-enabled", "mailEnabled", Boolean.class),
    /**
     * The 'shadowLastChange' column
     */
    SHADOW_LAST_CHANGE("shadow-last-change", "shadowLastChange", Integer.class),
    /**
     * The 'imapserver' column
     */
    IMAP_SERVER("imap-server", "imapserver", String.class),
    /**
     * The 'imapLogin' column
     */
    IMAP_LOGIN("imap-login", "imapLogin", String.class),
    /**
     * The 'smtpserver' column
     */
    SMTP_SERVER("smtp-server", "smtpserver", String.class),
    /**
     * The 'userPassword' column
     */
    USER_PASSWORD("user-password", "userPassword", String.class),
    /**
     * The 'passwordMech' column
     */
    PASSWORD_MECH("password-mechanism", "passwordMech", String.class),
    /**
     * The 'salt' column
     */
    SALT("salt", "salt", Byte.class),
    /**
     * The quota column
     */
    QUOTA("user-maximum-quota", "quota_max", Long.class),
    ;

    private final String sqlFieldName;
    private final Class<?> originalType;
    private static final String TABLE_NAME = "user";
    private final String attributeName;

    /**
     * Initialises a new {@link UserAttribute}.
     * 
     * @param sqlFieldName the name of the attribute
     */
    private UserAttribute(String attributeName, String sqlFieldName, Class<?> originalType) {
        this.attributeName = attributeName;
        this.sqlFieldName = sqlFieldName;
        this.originalType = originalType;
    }

    @Override
    public String getSQLFieldName() {
        return sqlFieldName;
    }

    @Override
    public String getSQLTableName() {
        return TABLE_NAME;
    }

    @Override
    public Class<?> getOriginalType() {
        return originalType;
    }

    @Override
    public String getName() {
        return attributeName;
    }
}
