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

package com.openexchange.report;

import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.management.MBeanException;
import com.openexchange.exception.OXException;
import com.openexchange.report.internal.LoginCounterMBean;

/**
 * {@link LoginCounterMBean}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com>Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 */
public interface LoginCounterService {

    /**
     * The key to receive the summed up number of logins.
     */
    public static final String SUM = "sum";

    /**
     * Gets the number of logins happened in specified range.
     * This is a mapping "client identifier => number of logins in time range".
     * The map always contains a key "sum" that sums up all logins. If the aggregate parameter
     * is set to <code>true</code>, the map only contains the sum key. Use {@link #SUM} to receive the according number.
     *
     * @param startDate The start time
     * @param endDate The end time
     * @param aggregate Set to <code>true</code> if you want to aggregate the sum of logins by users.
     *            That means that the sum value does not contain duplicate logins caused by multiple clients of one user.
     *            This also means that the sum value likely does not match the addition of all single values.
     * @param regex A regular expression to filter results by client identifiers. May be <code>null</code> to not filter clients at all.
     * @return The number of logins happened in specified range by client identifier.
     * @throws OXException
     * @throws MBeanException If an error occurs while counting
     */
    public Map<String, Integer> getNumberOfLogins(Date startDate, Date endDate, boolean aggregate, String regex) throws OXException;

    /**
     * Gets the time stamp of last login for specified user for given client.
     * <p>
     * The number of milliseconds since January 1, 1970, 00:00:00 GMT.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param client The client identifier
     * @return The time stamp of last login as UTC <code>long</code><br>
     *         (the number of milliseconds since January 1, 1970, 00:00:00 GMT)
     * @throws OXException
     * @throws MBeanException If retrieval fails
     */
    List<Object[]> getLastLoginTimeStamp(int userId, int contextId, String client) throws OXException;

    /**
     * Gets a list of all client logins in the given timeframe.
     * 
     * @param userId, the user ID
     * @param contextId, the context ID
     * @param startDate, the start date
     * @param endDate, the end date
     *            @return, a map with the client as key and the last login date as long value, which is number of
     *            milliseconds since January 1, 1970, 00:00:00 GMT
     * @throws OXException
     */
    public Map<String, Long> getLastClientLogIns(int userId, int contextId, Date startDate, Date endDate) throws OXException;

}
