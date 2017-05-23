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
    final static String INSERT_ON_DUPLICATE_UPDATE = "INSERT INTO user_certificate (cid, userid, host, fingerprint, trusted) VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE trusted=?";

    /**
     * Gets the certificate for a specified host
     */
    final static String GET_FOR_HOST = "SELECT * FROM  user_certificate WHERE cid=? AND userid=? AND host=? AND fingerprint=?";

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
    final static String CONTAINS = "SELECT 1 FROM user_certificate WHERE cid=? AND userid=? AND host=? AND fingerprint=?";

    /**
     * Checks if the certificate is trusted
     */
    final static String IS_TRUSTED = "SELECT trusted from user_certificate WHERE cid=? AND userid=? AND host=? AND fingerprint=?";

    /**
     * Deletes certificate for a specified host
     */
    final static String DELETE_FOR_HOST = "DELETE FROM user_certificate WHERE cid=? AND userid=? AND host=? AND fingerprint=?";

    /**
     * Deletes all certificates for the specified user
     */
    final static String DELETE_ALL = "DELETE FROM user_certificate WHERE cid=? AND userid=?";

    /**
     * Deletes certificate for all hosts
     */
    final static String DELETE_FOR_ALL_HOSTS = "DELETE FROM user_certificate WHERE cid=? AND userid=? AND fingerprint=?";
}
