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

package com.openexchange.pns.subscription.storage.groupware;

import com.openexchange.database.AbstractCreateTableImpl;

/**
 * {@link CreatePnsSubscriptionTable}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CreatePnsSubscriptionTable extends AbstractCreateTableImpl {

    /**
     * Gets the <code>CREATE TABLE</code> statement for <code>pns_subscription</code> table.
     *
     * @return The <code>CREATE TABLE</code> statement
     */
    public static String getTableSubscription() {
        return "CREATE TABLE pns_subscription (" +
            "id BINARY(16) NOT NULL," +
            "cid INT4 UNSIGNED NOT NULL," +
            "user INT4 UNSIGNED NOT NULL," +
            "token VARCHAR(255) CHARACTER SET latin1 NOT NULL," +
            "client VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL," +
            "transport VARCHAR(32) CHARACTER SET latin1 NOT NULL," +
            "last_modified BIGINT(64) NOT NULL," +
            "all_flag TINYINT UNSIGNED NOT NULL default '0'," +
            "expires BIGINT(20) DEFAULT NULL," +
            "PRIMARY KEY (cid, user, token, client)," +
            "UNIQUE KEY `subscription_id` (`id`)" +
            // "INDEX `affiliationIndex` (cid, user, affiliation)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
    }

    /**
     * Gets the <code>CREATE TABLE</code> statement for <code>pns_subscription_topic_wildcard</code> table.
     *
     * @return The <code>CREATE TABLE</code> statement
     */
    public static String getTableTopicWildcard() {
        return "CREATE TABLE pns_subscription_topic_wildcard (" +
            "id BINARY(16) NOT NULL," +
            "cid INT4 UNSIGNED NOT NULL," +
            "topic VARCHAR(255) CHARACTER SET latin1 NOT NULL," +
            "PRIMARY KEY (id, topic)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
    }

    /**
     * Gets the <code>CREATE TABLE</code> statement for <code>pns_subscription_topic_exact</code> table.
     *
     * @return The <code>CREATE TABLE</code> statement
     */
    public static String getTableTopicExact() {
        return "CREATE TABLE pns_subscription_topic_exact (" +
            "id BINARY(16) NOT NULL," +
            "cid INT4 UNSIGNED NOT NULL," +
            "topic VARCHAR(255) CHARACTER SET latin1 NOT NULL," +
            "PRIMARY KEY (id, topic)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
    }

    // ----------------------------------------------------------------------------------------------------------------

    public CreatePnsSubscriptionTable() {
        super();
    }

    @Override
    public String[] getCreateStatements() {
        return new String[] { getTableSubscription(), getTableTopicWildcard(), getTableTopicExact() };
    }

    @Override
    public String[] requiredTables() {
        return new String[] { "user" };
    }

    @Override
    public String[] tablesToCreate() {
        return new String[] { "pns_subscription", "pns_subscription_topic_wildcard", "pns_subscription_topic_exact" };
    }

}
