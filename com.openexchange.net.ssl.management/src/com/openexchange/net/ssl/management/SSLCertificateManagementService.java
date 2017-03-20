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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.net.ssl.management;

import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link SSLCertificateManagementService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since 7.8.4
 */
public interface SSLCertificateManagementService {

    /**
     * Checks whether the SSL {@link Certificate} with the specified fingerprint
     * is already trusted by the specified user in the specified context
     *
     * @param userId The user's identifier
     * @param contextId The context's identifier
     * @param fingerprint The SSL {@link Certificate}'s fingerprint
     * @return <code>true</code> if the {@link Certificate} is trusted; <code>false</code> otherwise
     * @throws OXException If the certificate is not found or any other error occurs
     */
    boolean isTrusted(int userId, int contextId, String hostname, String fingerprint) throws OXException;

    /**
     * Retrieves the {@link Certificate} with the specified fingerprint from the storage
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param fingerprint The fingerprint of the {@link Certificate}
     * @return The {@link Certificate}
     * @throws OXException If the {@link Certificate} does not exist, or any other error occurs
     */
    Certificate get(int userId, int contextId, String hostname, String fingerprint) throws OXException;

    /**
     * Returns an unmodifiable {@link List} with all trusted/untrusted host-name/certificate combinations
     * for the specified certificate fingerprint
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param fingerprint The fingerprint of the {@link Certificate}
     * @return an unmodifiable {@link List} with {@link Certificate}s
     * @throws OXException If fingerprint-associated certificates cannot be returned
     */
    List<Certificate> get(int userId, int contextId, String fingerprint) throws OXException;

    /**
     * Returns an unmodifiable {@link List} with all managed {@link Certificate}s for the specified
     * user in the specified context
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return an unmodifiable {@link List} with all managed {@link Certificate}s for the specified
     *         user in the specified context
     * @throws OXException If user's certificates cannot be returned
     */
    List<Certificate> getAll(int userId, int contextId) throws OXException;

    /**
     * Checks whether the SSL {@link Certificate} with the specified fingerprint
     * already exists for the specified user in the specified context
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param fingerprint The SSL {@link Certificate}'s fingerprint
     * @return <code>true</code> if the {@link Certificate} exists; <code>false</code> otherwise
     * @throws OXException If existence of denoted certificate cannot be checked
     */
    boolean contains(int userId, int contextId, String hostname, String fingerprint) throws OXException;

    /**
     * Stores the specified {@link Certificate} for the specified user in the specified context
     * If a certificate with the same fingerprint exists for the same user, then the certificate
     * is updated instead.
     *
     * @param userId The user's identifier
     * @param contextId The context's identifier
     * @param certificate The SSL {@link Certificate} to store
     * @throws OXException If specified certificate cannot be stored
     */
    void store(int userId, int contextId, Certificate certificate) throws OXException;

    /**
     * Deletes all hostname exceptions for the SSL {@link Certificate} with the specified fingerprint
     * for the specified user in the specified context
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param fingerprint The SSL {@link Certificate}'s fingerprint
     * @throws OXException If the certificate is not found or any other error occurs
     */
    void delete(int userId, int contextId, String fingerprint) throws OXException;

    /**
     * Deletes the SSL {@link Certificate} with the specified fingerprint for the specified
     * user in the specified context
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param hostname The host name
     * @param fingerprint The SSL {@link Certificate}'s fingerprint
     * @throws OXException If the certificate is not found or any other error occurs
     */
    void delete(int userId, int contextId, String hostname, String fingerprint) throws OXException;

    /**
     * Deletes all SSL {@link Certificate} exceptions for the specified user
     * 
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If an error is occurred
     */
    void deleteAll(int userId, int contextId) throws OXException;

    /**
     * Caches the specified {@link Certificate} temporarily for the specified user in the
     * specified context
     *
     * @param userId the user identifier
     * @param contextId the context identifier
     * @param certificate The SSL {@link Certificate}
     * @throws OXException If the certificate cannot be cached or any other error occurs
     */
    void cache(int userId, int contextId, Certificate certificate) throws OXException;

    /**
     * Returns the cached {@link Certificate}
     *
     * @param userId the user identifier
     * @param contextId the context identifier
     * @param fingerprint The fingerprint of the {@link Certificate}
     * @return The cached {@link Certificate}
     * @throws OXException If no such certificate exists that matches given fingerprint
     */
    Certificate requireCached(int userId, int contextId, String fingerprint) throws OXException;

    /**
     * Returns the cached {@link Certificate}
     *
     * @param userId the user identifier
     * @param contextId the context identifier
     * @param fingerprint The fingerprint of the {@link Certificate}
     * @return The cached {@link Certificate} or <code>null</code>
     * @throws OXException If certificate cannot be returned
     */
    Certificate optCached(int userId, int contextId, String fingerprint) throws OXException;
}
