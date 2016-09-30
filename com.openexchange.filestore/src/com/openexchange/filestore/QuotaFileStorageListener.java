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

package com.openexchange.filestore;

import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link QuotaFileStorageListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public interface QuotaFileStorageListener {

    /**
     * Called right before the usage is about to be incremented.
     * <p>
     * <b>Note</b>: This call-back occurs if increment does not violate quota limitation.<br>
     * Otherwise {@link #onQuotaExceeded()} is called.
     *
     * @param id The identifier of the file in storage causing the usage increment
     * @param toIncrement The value to increment
     * @param currentUsage The current quota usage
     * @param quota The quota limit
     * @param userId The (optional) user identifier or <code>0</code> (zero) if storage is not user-specific
     * @param contextId The context identifier
     * @throws OXException If increment must not occur and operation is supposed to be aborted
     */
    void onUsageIncrement(String id, long toIncrement, long currentUsage, long quota, int userId, int contextId) throws OXException;

    /**
     * Called right before the usage is about to be decremented.
     * <p>
     * <b>Note</b>: This call-back should not throw an exception.
     *
     * @param ids The identifiers of the files in storage causing the usage decrement
     * @param toDecrement The value to decrement
     * @param currentUsage The current quota usage
     * @param quota The quota limit
     * @param userId The (optional) user identifier or <code>0</code> (zero) if storage is not user-specific
     * @param contextId The context identifier
     */
    void onUsageDecrement(List<String> ids, long toDecrement, long currentUsage, long quota, int userId, int contextId);

    /**
     * Called in case a quota increment exceeds the quota limit and the operation is about to be aborted.
     * <p>
     * <b>Note</b>: This call-back should not throw an exception.
     *
     * @param id The identifier of the file in storage causing the exceeded quota
     * @param toIncrement The value that exceeded the quota limit
     * @param currentUsage The current quota usage
     * @param quota The quota limit
     * @param userId The (optional) user identifier or <code>0</code> (zero) if storage is not user-specific
     * @param contextId The context identifier
     */
    void onQuotaExceeded(String id, long toIncrement, long currentUsage, long quota, int userId, int contextId);

}
