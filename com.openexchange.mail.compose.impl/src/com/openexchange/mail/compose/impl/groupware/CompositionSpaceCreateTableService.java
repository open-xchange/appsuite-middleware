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
 *    trademarks of the OX Software GmbH. group of companies.
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
        + "fromAddr VARCHAR(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
        + "senderAddr VARCHAR(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
        + "toAddr TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
        + "ccAddr TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
        + "bccAddr TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
        + "subject VARCHAR(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
        + "content MEDIUMTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
        + "contentType VARCHAR(32) CHARACTER SET latin1 COLLATE latin1_general_ci DEFAULT NULL,"
        + "requestReadReceipt TINYINT(1) DEFAULT NULL,"
        + "sharedAttachments TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
        + "meta TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
        + "security TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
        + "priority VARCHAR(16) CHARACTER SET latin1 COLLATE latin1_general_ci DEFAULT NULL,"
        + "attachments TEXT CHARACTER SET latin1 COLLATE latin1_general_ci DEFAULT NULL,"
        + "contentEncrypted TINYINT(1) NOT NULL DEFAULT 0,"
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
        + "name VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
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
