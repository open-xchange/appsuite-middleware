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

package com.openexchange.net.ssl.management.storage;

/**
 * {@link SQLStatements}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
final class SQLStatements {

    /**
     * Inserts on duplicate key updates a certificate
     */
    final static String INSERT_ON_DUPLICATE_UPDATE = "INSERT INTO user_certificate (cid, userid, host_hash, host, fingerprint, trusted) VALUES (?,?,?,?,?,?) ON DUPLICATE KEY UPDATE trusted=?";

    /**
     * Gets the certificate for a specified host
     */
    final static String GET_FOR_HOST = "SELECT * FROM  user_certificate WHERE cid=? AND userid=? AND host_hash=? AND fingerprint=?";

    /**
     * Gets the certificate
     */
    final static String GET_FOR_ALL_HOSTS = "SELECT * FROM  user_certificate WHERE cid=? AND userid=? AND fingerprint=?";

    /**
     * Gets all certificates
     */
    final static String GET_ALL = "SELECT * FROM  user_certificate WHERE cid=? AND userid=?";

    /**
     * Checks for existence
     */
    final static String CONTAINS = "SELECT 1 FROM user_certificate WHERE cid=? AND userid=? AND host_hash=? AND fingerprint=?";

    /**
     * Checks if the certificate is trusted
     */
    final static String IS_TRUSTED = "SELECT trusted from user_certificate WHERE cid=? AND userid=? AND host_hash=? AND fingerprint=?";

    /**
     * Deletes certificate for a specified host
     */
    final static String DELETE_FOR_HOST = "DELETE FROM user_certificate WHERE cid=? AND userid=? AND host_hash=? AND fingerprint=?";

    /**
     * Deletes all certificates for the specified user
     */
    final static String DELETE_ALL = "DELETE FROM user_certificate WHERE cid=? AND userid=?";

    /**
     * Deletes certificate for all hosts
     */
    final static String DELETE_FOR_ALL_HOSTS = "DELETE FROM user_certificate WHERE cid=? AND userid=? AND fingerprint=?";
}
