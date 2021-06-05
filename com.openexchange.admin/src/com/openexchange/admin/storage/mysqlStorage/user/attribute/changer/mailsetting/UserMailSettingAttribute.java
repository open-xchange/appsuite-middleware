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

package com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.mailsetting;

import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.Attribute;

/**
 * {@link UserMailSettingAttribute}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public enum UserMailSettingAttribute implements Attribute {

    BITS("permission-bits", "bits", Integer.class),
    SEND_ADDRESS("default-sender-address", "send_addr", String.class),
    STD_DRAFTS("standard-drafts-folder-name", "std_drafts", String.class),
    STD_SENT("standard-sent-folder-name", "std_sent", String.class),
    STD_SPAM("standard-spam-folder-name", "std_spam", String.class),
    STD_TRASH("standard-drafts-folder-name", "std_trash", String.class),
    CONFIRMED_SPAM("standard-spam-folder-name", "confirmed_spam", String.class),
    CONFIRMED_HAM("standard-ham-folder-name", "confirmed_ham", String.class),
    UPLOAD_QUOTA("total-upload-file-size-limit", "upload_quota", Integer.class),
    UPLOAD_QUOTA_PER_FILE("upload-file-size-limit-per-file", "upload_quota_per_file", Integer.class),
    ;

    private final String sqlFieldName;
    private final Class<?> originalType;
    private static final String TABLE_NAME = "user_setting_mail";
    private final String attributeName;

    /**
     * 
     * Initialises a new {@link UserMailSettingAttribute}.
     * 
     * @param sqlFieldNames the names of the attribute
     */
    private UserMailSettingAttribute(String attributeName, String sqlFieldName, Class<?> originalType) {
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
