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

package com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.user;

import java.util.function.Function;
import com.openexchange.admin.rmi.dataobjects.ExtendableDataObject;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.Attribute;

/**
 * {@link UserAttribute}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public enum UserAttribute implements Attribute {

    /**
     * The 'primaryEmail' column
     */
    MAIL("mail", (user) -> user.getPrimaryEmail(), String.class),
    /**
     * The 'preferredlanguage' column
     */
    PREFERRED_LANGUAGE("preferredlanguage", (user) -> user.getLanguage(), String.class),
    /**
     * The 'timezone' column
     */
    TIMEZONE("timezone", (user) -> user.getTimezone(), String.class),
    /**
     * The 'mailEnabled' column
     */
    MAIL_ENABLED("mailEnabled", (user) -> Boolean.toString(user.getMailenabled()), Boolean.class),
    /**
     * The 'shadowLastChange' column
     */
    SHADOW_LAST_CHANGE("shadowLastChange", (user) -> Boolean.toString(user.getPassword_expired()), Boolean.class),
    /**
     * The 'imapserver' column
     */
    IMAP_SERVER("imapserver", (user) -> user.getImapServerString(), String.class),
    /**
     * The 'imapLogin' column
     */
    IMAP_LOGIN("imapLogin", (user) -> user.getImapLogin(), String.class),
    /**
     * The 'smtpserver' column
     */
    SMTP_SERVER("smtpserver", (user) -> user.getSmtpServerString(), String.class),
    /**
     * The 'userPassword' column
     */
    USER_PASSWORD("userPassword", (user) -> user.getPassword(), String.class),
    /**
     * The 'passwordMech' column
     */
    PASSWORD_MECH("passwordMech", (user) -> user.getPasswordMech(), String.class),
    ;

    private final String sqlFieldName;
    private final Function<User, String> getter;
    private final Class<?> originalType;
    private static final String TABLE_NAME = "user";

    /**
     * 
     * Initialises a new {@link UserAttribute}.
     * 
     * @param sqlFieldName the name of the attribute
     */
    private UserAttribute(String sqlFieldName, Function<User, String> getter, Class<?> originalType) {
        this.sqlFieldName = sqlFieldName;
        this.getter = getter;
        this.originalType = originalType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.Attribute#getSQLFieldName()
     */
    @Override
    public String getSQLFieldName() {
        return sqlFieldName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.Attribute#getSQLTableName()
     */
    @Override
    public String getSQLTableName() {
        return TABLE_NAME;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.Attribute#getValue(com.openexchange.admin.rmi.dataobjects.ExtendableDataObject)
     */
    @Override
    public <T extends ExtendableDataObject> String getValue(T object) {
        return getter.apply((User) object);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.Attribute#getOriginalType()
     */
    @Override
    public Class<?> getOriginalType() {
        return originalType;
    }
}
