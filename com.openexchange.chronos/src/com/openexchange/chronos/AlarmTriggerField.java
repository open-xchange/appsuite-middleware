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

package com.openexchange.chronos;


/**
 * {@link AlarmTriggerField} contains the available fields of the {@link AlarmTrigger}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public enum AlarmTriggerField {
    /**
     * The calendar account id
     */
    ACCOUNT,
    /**
     * The alarm action
     */
    ACTION,
    /**
     * The id of the alarm
     */
    ALARM_ID,
    /**
     * The context id
     */
    CONTEXT_ID,
    /**
     * The id of the event the alarm belongs to
     */
    EVENT_ID,
    /**
     * A flag indicating whether the trigger is already triggered or not
     */
    PUSHED,
    /**
     * The recurrence identifier of the targeted event.
     */
    RECURRENCE_ID,
    /**
     * The trigger time
     */
    TIME,
    /**
     * The user id
     */
    USER_ID,
    /**
     * The folder of the event
     */
    FOLDER,
    /**
     * The timezone used for floating events
     */
    FLOATING_TIMEZONE,
    /**
     * The date and time the trigger time is calculated on
     */
    RELATED_TIME,
    /**
     * The processed field. Contains information about whether the trigger is already picked up by a node to perform mail delivery.
     */
    PROCESSED

}
