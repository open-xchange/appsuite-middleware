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

package com.openexchange.filestore.unified;

import com.openexchange.exception.OXException;
import com.openexchange.filestore.QuotaMode;

/**
 * {@link UnifiedQuotaService} - Provides methods to query and increment/decrement a service-associated usage as well as to query its limit that contribute to a unified quota.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public interface UnifiedQuotaService extends QuotaMode {

    /** The mode for Unified Quota */
    public static final String MODE = "unified";

    /**
     * Gets the current limit for specified user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The current limit for specified user
     * @throws OXException If current limit cannot be returned
     */
    long getLimit(int userId, int contextId) throws OXException;

    /**
     * Gets the current usage for specified user including all contributing services.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The current usage for specified user
     * @throws OXException If current usage cannot be returned
     */
    UsageResult getUsage(int userId, int contextId) throws OXException;

    /**
     * Gets the current usage for specified user, except for specified services.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param toExclude The identifier of such service contributors to exclude
     * @return The current usage for specified user
     * @throws OXException If current usage cannot be returned
     */
    UsageResult getUsageExceptFor(int userId, int contextId, String... toExclude) throws OXException;

    /**
     * Sets the usage for specified service contributor to the given value for specified user
     *
     * @param usage The usage bytes to set
     * @param serviceId The identifier of the service contributor
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if new usage has been successfully applied; otherwise <code>false</code> if there was a concurrent modification
     * @throws OXException If usage cannot be updated
     */
    boolean setUsage(long usage, String serviceId, int userId, int contextId) throws OXException;

    /**
     * Increments the usage by the given value for specified user
     *
     * @param required The required bytes to increment by
     * @param serviceId The identifier of the service contributor
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if usage has been successfully incremented; otherwise <code>false</code> if there was a concurrent modification
     * @throws OXException If usage cannot be updated
     */
    boolean incrementUsage(long required, String serviceId, int userId, int contextId) throws OXException;

    /**
     * Decrements the usage by the given value for specified user
     *
     * @param released The released bytes to decrement by
     * @param serviceId The identifier of the service contributor
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if usage has been successfully decremented; otherwise <code>false</code> if there was a concurrent modification
     * @throws OXException If usage cannot be updated
     */
    boolean decrementUsage(long released, String serviceId, int userId, int contextId) throws OXException;

    /**
     * Checks if this usage service is applicable for given user
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if applicable; otherwise <code>false</code>
     * @throws OXException If applicability cannot be checked
     */
    boolean isApplicableFor(int userId, int contextId) throws OXException;

    // ---------------------------------------------------------------------------------------------------------

    /**
     * Deletes the usage entries for specified context.
     *
     * @param contextId The context identifier
     * @throws OXException If delete attempt fails
     */
    void deleteEntryFor(int contextId) throws OXException;

    /**
     * Deletes the usage entries for specified user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If delete attempt fails
     */
    void deleteEntryFor(int userId, int contextId) throws OXException;

}
