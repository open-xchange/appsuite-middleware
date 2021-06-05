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

package com.openexchange.mail.compose.impl.groupware;

import com.openexchange.database.AbstractCreateTableImpl;

/**
 * {@link CompositionSpaceCreateTableService}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.2
 */
public class CompositionSpaceCreateTableService extends AbstractCreateTableImpl {

    private static final CompositionSpaceCreateTableService INSTANCE = new CompositionSpaceCreateTableService();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static CompositionSpaceCreateTableService getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static final String COMPOSITION_SPACE_TABLE = "compositionSpace";

    // @formatter:off
    private static final String CREATE_COMPOSITION_SPACE_TABLE = "CREATE TABLE " + COMPOSITION_SPACE_TABLE + "("
        + "uuid BINARY(16) NOT NULL,"
        + "cid INT4 unsigned NOT NULL,"
        + "user INT4 unsigned NOT NULL,"
        + "lastModified BIGINT(64) unsigned NOT NULL,"
        + "clientToken VARCHAR(16) CHARACTER SET latin1 COLLATE latin1_general_ci DEFAULT NULL,"
        + "fromAddr VARCHAR(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
        + "senderAddr VARCHAR(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
        + "replyToAddr VARCHAR(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
        + "toAddr TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
        + "ccAddr TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
        + "bccAddr TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
        + "subject TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
        + "content MEDIUMTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
        + "contentType VARCHAR(32) CHARACTER SET latin1 COLLATE latin1_general_ci DEFAULT NULL,"
        + "requestReadReceipt TINYINT(1) DEFAULT NULL,"
        + "sharedAttachments TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
        + "meta TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
        + "security TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
        + "priority VARCHAR(16) CHARACTER SET latin1 COLLATE latin1_general_ci DEFAULT NULL,"
        + "attachments TEXT CHARACTER SET latin1 COLLATE latin1_general_ci DEFAULT NULL,"
        + "contentEncrypted TINYINT(1) NOT NULL DEFAULT 0,"
        + "customHeaders TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
        + "PRIMARY KEY (uuid),"
        + "KEY id (cid, user, uuid)"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
    // @formatter:on

    private static final String ATTACHMENT_META_TABLE = "compositionSpaceAttachmentMeta";

    // @formatter:off
    private static final String CREATE_ATTACHMENT_META_TABLE = "CREATE TABLE " + ATTACHMENT_META_TABLE + "("
        + "uuid BINARY(16) NOT NULL,"
        + "cid INT4 UNSIGNED NOT NULL,"
        + "user INT4 UNSIGNED NOT NULL,"
        + "csid BINARY(16) NOT NULL,"
        + "refType TINYINT unsigned NOT NULL,"
        + "refId VARCHAR(255) CHARACTER SET latin1 COLLATE latin1_general_ci NOT NULL,"
        + "dedicatedFileStorageId INT4 UNSIGNED NOT NULL DEFAULT 0,"
        + "name TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
        + "size BIGINT(64) unsigned NOT NULL,"
        + "mimeType VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
        + "contentId VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
        + "disposition VARCHAR(16) CHARACTER SET latin1 COLLATE latin1_general_ci DEFAULT NULL,"
        + "origin VARCHAR(32) CHARACTER SET latin1 COLLATE latin1_general_ci DEFAULT NULL,"
        + "PRIMARY KEY (uuid),"
        + "INDEX `userIndex` (cid, user),"
        + "INDEX `csIndex` (cid, csid)"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
    // @formatter:on

    private static final String ATTACHMENT_BINARY_TABLE = "compositionSpaceAttachmentBinary";

    // @formatter:off
    private static final String CREATE_ATTACHMENT_BINARY_TABLE = "CREATE TABLE " + ATTACHMENT_BINARY_TABLE + "("
        + "uuid BINARY(16) NOT NULL,"
        + "cid INT4 UNSIGNED NOT NULL,"
        + "user INT4 UNSIGNED NOT NULL,"
        + "data LONGBLOB NOT NULL,"
        + "PRIMARY KEY (uuid),"
        + "INDEX `userIndex` (cid, user)"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
    // @formatter:on

    private static final String KEY_STORAGE_TABLE = "compositionSpaceKeyStorage";

    // @formatter:off
    private static final String CREATE_KEY_STORAGE_TABLE = "CREATE TABLE " + KEY_STORAGE_TABLE + "("
        + "uuid BINARY(16) NOT NULL,"
        + "cid INT4 UNSIGNED NOT NULL,"
        + "user INT4 UNSIGNED NOT NULL,"
        + "refId VARCHAR(255) CHARACTER SET latin1 COLLATE latin1_general_ci NOT NULL,"
        + "dedicatedFileStorageId INT4 UNSIGNED NOT NULL DEFAULT 0,"
        + "PRIMARY KEY (uuid),"
        + "INDEX `userIndex` (cid, user)"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
    // @formatter:on

    @Override
    public String[] requiredTables() {
        return new String[0];
    }

    @Override
    public String[] tablesToCreate() {
        return new String[] { COMPOSITION_SPACE_TABLE, ATTACHMENT_META_TABLE, ATTACHMENT_BINARY_TABLE, KEY_STORAGE_TABLE };
    }

    @Override
    public String[] getCreateStatements() {
        return new String[] { CREATE_COMPOSITION_SPACE_TABLE, CREATE_ATTACHMENT_META_TABLE, CREATE_ATTACHMENT_BINARY_TABLE, CREATE_KEY_STORAGE_TABLE };
    }

}
