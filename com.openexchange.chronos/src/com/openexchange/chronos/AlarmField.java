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
 * {@link AlarmField}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public enum AlarmField {

    /**
     * The internal identifier of the alarm.
     */
    ID,
    /**
     * The universal identifier of the alarm.
     */
    UID,
    /**
     * The relationship between this and other alarms.
     */
    RELATED_TO,
    /**
     * The time when the alarm was last sent or acknowledged.
     */
    ACKNOWLEDGED,
    /**
     * The type of action invoked when the alarm is triggered.
     */
    ACTION,
    /**
     * The additional repetitions of the alarm's trigger.
     */
    REPEAT,
    /**
     * The moment the alarm will trigger.
     */
    TRIGGER,
    /**
     * Extended properties of the alarm.
     */
    EXTENDED_PROPERTIES,
    /**
     * A list of attachments. Can be used as the sound source for the audio action or as attachments for the mail action.
     */
    ATTACHMENTS,
    /**
     * A summary which can be used as the mail subject for the mail action.
     */
    SUMMARY,
    /**
     * A description which can either be used to be display for the display action or used as a text for the mail action.
     */
    DESCRIPTION,

    /**
     * A list of mail addresses for the mail action.
     */
    ATTENDEES,

    /**
     * The timestamp of this alarm.
     */
    TIMESTAMP

    ;
}
