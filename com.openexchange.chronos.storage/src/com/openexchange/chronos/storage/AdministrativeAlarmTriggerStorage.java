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

package com.openexchange.chronos.storage;

import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.AlarmAction;
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
     * Retrieves a mapping of cid/account {@link Pair}s to a list of {@link AlarmTrigger}s which are either
     * not processed yet and have a trigger time before the given until value or are older than the given overdue time.
     *
     * @param con The connection to use
     * @param until The upper limit for the trigger time
     * @param overdueTime The overdue date
     * @param lock Whether the selected triggers should be locked or not
     * @param actions The list of actions to check for. Usually a list of available message {@link AlarmAction}.
     * @return A mapping of a cid/account {@link Pair} to a list of {@link AlarmTrigger}s
     * @throws OXException
     */
    Map<Pair<Integer, Integer>, List<AlarmTrigger>> getAndLockTriggers(Connection con, Date until, Date overdueTime, boolean lock, AlarmAction... actions) throws OXException;


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
     * Retrieves and locks message alarm triggers for the given event which are not already taken by another worker.
     *
     * @param con The connection to use
     * @param cid The context id
     * @param account The account id
     * @param eventId The event id
     * @param lock Whether the selected triggers should be locked or not.
     * @param actions The list of actions to check for. Usually a list of available message {@link AlarmAction}.
     * @return A mapping of a cid/account {@link Pair} to a list of {@link AlarmTrigger}s
     * @throws OXException
     */
    Map<Pair<Integer, Integer>, List<AlarmTrigger>> getMessageAlarmTriggers(Connection con, int cid, int account, String eventId, boolean lock, AlarmAction... actions) throws OXException;

}
