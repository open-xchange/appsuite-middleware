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

package com.openexchange.report;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
     * @return, a map with the client as key and the last login date as long value, which is number of
     *          milliseconds since January 1, 1970, 00:00:00 GMT
     * @throws OXException
     */
    public HashMap<String, Long> getLastClientLogIns(int userId, int contextId, Date startDate, Date endDate) throws OXException;

}
