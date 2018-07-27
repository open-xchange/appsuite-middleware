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

package com.openexchange.chronos.storage;

import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.AlarmTrigger;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.Pair;

/**
 * {@link AdministrativeAlarmTriggerStorage}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public interface AdministrativeAlarmTriggerStorage {

    /**
     * Retrieves a mapping of cic/account {@link Pair}s to a list of {@link AlarmTrigger}s which are either
     * not processed yet and have a trigger time before the given until value or are older than the given overdue time.
     *
     * @param con The connection to use
     * @param until The upper limit for the trigger time
     * @param overdueTime The overdue date
     * @return A mapping of a cid/account {@link Pair} to a list of {@link AlarmTrigger}s
     * @throws OXException
     */
    Map<Pair<Integer, Integer>, List<AlarmTrigger>> getAndLockTriggers(Connection con, Date until, Date overdueTime) throws OXException;


    /**
     * Sets the processing status to the given {@link AlarmTrigger}s
     *
     * @param con The connection to use
     * @param triggers The triggers to update
     * @param time The time to set or null to reset the status
     * @throws OXException
     */
    public void setProcessingStatus(Connection con, Map<Pair<Integer, Integer>, List<AlarmTrigger>> triggers, Long time) throws OXException;


    /**
     * Retrieves and locks mail alarm triggers for the given event which are not already taken by another worker.
     *
     * @param con The connection to use
     * @param cid The context id
     * @param account The account id
     * @param eventId The event id
     * @param lock Whether the selected triggers should be locked or not
     * @return A mapping of a cid/account {@link Pair} to a list of {@link AlarmTrigger}s
     * @throws OXException
     */
    Map<Pair<Integer, Integer>, List<AlarmTrigger>> getMailAlarmTriggers(Connection con, int cid, int account, String eventId, boolean lock) throws OXException;


    /**
     * Retrieves the {@link AlarmTrigger}
     *
     * @param con The connection to use
     * @param cid The context id
     * @param account The calendar account
     * @param alarm The id of the alarm
     * @return The {@link AlarmTrigger}
     * @throws OXException
     */
    AlarmTrigger getAlarmTrigger(Connection con, int cid, int account, int alarm) throws OXException;
}
