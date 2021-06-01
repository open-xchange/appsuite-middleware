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
 * {@link AttendeeField}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public enum AttendeeField {

    /**
     * The calendar user address of the attendee.
     */
    URI,
    /**
     * The common name of the attendee.
     */
    CN,
    /**
     * The internal identifier of the attendee.
     */
    ENTITY,
    /**
     * The calendar user who is acting on behalf of the attendee.
     */
    SENT_BY,
    /**
     * The calendar user type of the attendee.
     */
    CU_TYPE,
    /**
     * The participation role of the attendee.
     */
    ROLE,
    /**
     * The participation status of the attendee.
     */
    PARTSTAT,
    /**
     * The timestamp the attedee's participant status was changed the last time.
     */
    TIMESTAMP,
    /**
     * The attendee's comment.
     */
    COMMENT,
    /**
     * The RSVP expectation of the attendee.
     */
    RSVP,
    /**
     * The identifier of the folder where the event is located in for the attendee.
     */
    FOLDER_ID,
    /**
     * The <i>hidden</i> marker to exclude the event from the attendee's folder view.
     */
    HIDDEN,
    /**
     * The group- or list membership of the attendee.
     */
    MEMBER,
    /**
     * The e-mail address of the attendee.
     */
    EMAIL,
    /**
     * The attendee's time transparency of the event.
     */
    TRANSP,
    /**
     * Extended parameters of the attendee.
     */
    EXTENDED_PARAMETERS,

    ;
}
