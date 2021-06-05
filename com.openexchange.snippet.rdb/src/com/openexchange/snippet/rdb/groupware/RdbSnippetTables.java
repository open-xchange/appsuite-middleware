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

package com.openexchange.snippet.rdb.groupware;

/**
 * {@link RdbSnippetTables}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class RdbSnippetTables {

    /*-
     * --------------------------------------------------------------------------------------------------
     */

    static String getSnippetContentName() {
        return "snippetContent";
    }

    static String getSnippetContentTable() {
        return "CREATE TABLE " + getSnippetContentName() + " (" +
            " cid INT4 unsigned NOT NULL," +
            " user INT4 unsigned NOT NULL," +
            " id INT4 unsigned NOT NULL," +
            " content TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL," +
            " PRIMARY KEY (cid, user, id)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
    }

    /*-
     * --------------------------------------------------------------------------------------------------
     */

    static String getSnippetAttachmentName() {
        return "snippetAttachment";
    }

    static String getSnippetAttachmentTable() {
        return "CREATE TABLE " + getSnippetAttachmentName() + " (" +
            " cid INT4 unsigned NOT NULL," +
            " user INT4 unsigned NOT NULL," +
            " id INT4 unsigned NOT NULL," +
            " referenceId VARCHAR(255) CHARACTER SET latin1 NOT NULL," +
            " fileName VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL," +
            " mimeType VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL," +
            " disposition VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL," +
            " PRIMARY KEY (cid, user, id, referenceId(64))" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
    }

    /*-
     * --------------------------------------------------------------------------------------------------
     */

    static String getSnippetMiscName() {
        return "snippetMisc";
    }

    static String getSnippetMiscTable() {
        return "CREATE TABLE " + getSnippetMiscName() + " (" +
            " cid INT4 unsigned NOT NULL," +
            " user INT4 unsigned NOT NULL," +
            " id INT4 unsigned NOT NULL," +
            " json TEXT CHARACTER SET latin1 NOT NULL," +
            " PRIMARY KEY (cid, user, id)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
    }

    /*-
     * --------------------------------------------------------------------------------------------------
     */

    static String getSnippetAttachmentBinaryName() {
        return "snippetAttachmentBinary";
    }

    static String getSnippetAttachmentBinaryTable() {
        return "CREATE TABLE " + getSnippetAttachmentBinaryName() + " (" +
            " cid INT4 unsigned NOT NULL," +
            " referenceId VARCHAR(64) CHARACTER SET latin1 NOT NULL," +
            " data MEDIUMBLOB NOT NULL," +
            " PRIMARY KEY (cid, referenceId)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
    }
}
