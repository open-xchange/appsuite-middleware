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

package com.openexchange.chronos.alarm.message;

import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmAction;
import com.openexchange.chronos.Event;
import com.openexchange.exception.OXException;
import com.openexchange.ratelimit.Rate;

/**
 * {@link AlarmNotificationService} is a service which delivers event messages for a specific type of {@link AlarmAction}s. E.g. transport via mail for {@link AlarmAction#EMAIL}.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public interface AlarmNotificationService {

    /**
     * Sends the given event
     *
     * @param event The event
     * @param alarm The {@link Alarm}
     * @param contextId The context id
     * @param accountId The calendar account id
     * @param userId The user id
     * @param trigger The trigger id
     * @throws OXException
     */
    public void send(Event event, Alarm alarm, int contextId, int accountId, int userId, long trigger) throws OXException;

    /**
     * Returns the type of {@link AlarmAction} this {@link AlarmNotificationService} is responsible for.
     *
     * @return the {@link AlarmAction}
     */
    public AlarmAction getAction();

    /**
     * Returns the time in milliseconds a trigger for this {@link AlarmNotificationService} should be shifted forward to compensate the time needed to send the message.
     *
     * E.g. the average time a mail infrastructure needs to send out a mail.
     *
     * @return The time in milliseconds
     * @throws OXException
     */
    int getShift() throws OXException;

    /**
     * Checks if the {@link AlarmNotificationService} is available for the given user.
     *
     * @param userId The user id
     * @param contextId The context id
     * @return true if it is available, false otherwise
     * @throws OXException
     */
    boolean isEnabled(int userId, int contextId) throws OXException;

    /**
     * Returns the {@link Rate} in which this services allows a single user to send messages.
     *
     * @param userId The user id
     * @param contextId The context id
     * @return The {@link Rate}
     * @throws OXException
     */
    Rate getRate(int userId, int contextId) throws OXException;

}
