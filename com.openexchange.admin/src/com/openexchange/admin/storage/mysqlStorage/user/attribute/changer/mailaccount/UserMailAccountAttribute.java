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

package com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.mailaccount;

import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.Attribute;

/**
 * {@link UserMailAccountAttribute}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public enum UserMailAccountAttribute implements Attribute {

    DRAFTS("standard-drafts-folder-name", "drafts", String.class),
    DRAFTS_FULLNAME("standard-drafts-folder-fullname", "drafts_fullname", String.class),

    SENT("standard-sent-folder-name", "sent", String.class),
    SENT_FULLNAME("standard-sent-folder-fullname", "sent_fullname", String.class),

    SPAM("standard-spam-folder-name", "spam", String.class),
    SPAM_FULLNAME("standard-spam-folder-name", "spam_fullname", String.class),

    TRASH("standard-trash-folder-name", "trash", String.class),
    TRASH_FULLNAME("standard-trash-folder-fullname", "trash_fullname", String.class),

    ARCHIVE("standard-archive-folder-name", "archive", String.class),
    ARCHIVE_FULLNAME("standard-archive-folder-fullname", "archive_fullname", String.class),

    CONFIRMED_HAM("standard-confirmed-ham-folder-name", "confirmed_ham", String.class),
    CONFIRMED_HAM_FULLNAME("standard-confirmed-ham-folder-fullname", "confirmed_ham_fullname", String.class),

    CONFIRMED_SPAM("standard-confirmed-spam-folder-name", "confirmed_spam", String.class),
    CONFIRMED_SPAM_FULLNAME("standard-confirmed-spam-folder-fullname", "confirmed_spam_fullname", String.class),
    ;

    private final String sqlFieldName;
    private final Class<?> originalType;
    private static final String TABLE = "user_mail_account";
    private final String attributeName;

    /**
     * 
     * Initialises a new {@link UserMailAccountAttribute}.
     * 
     * @param sqlFieldName the names of the attribute
     */
    private UserMailAccountAttribute(String attributeName, String sqlFieldName, Class<?> originalType) {
        this.attributeName = attributeName;
        this.sqlFieldName = sqlFieldName;
        this.originalType = originalType;
    }

    @Override
    public String getSQLFieldName() {
        return sqlFieldName;
    }

    @Override
    public Class<?> getOriginalType() {
        return originalType;
    }

    @Override
    public String getSQLTableName() {
        return TABLE;
    }

    @Override
    public String getName() {
        return attributeName;
    }
}
