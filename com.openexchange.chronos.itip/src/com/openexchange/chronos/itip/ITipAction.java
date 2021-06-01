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

package com.openexchange.chronos.itip;


/**
 * {@link ITipAction}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public enum ITipAction {

    /**
     * Cases:
     * 1. Create Appointment if not existing
     * 2. Set user accepted
     * 3. Add user to existing appointment if not participant (rights?!)
     * 4. If Attendee: Answer with REPLY
     */
    ACCEPT,

    /**
     * see accept
     * (implicit ignore conflicts)
     */
    DECLINE,

    /**
     * see accept
     * (implicit ignore conflicts)
     */
    TENTATIVE,

    /**
     *
     */
    DELEGATE,

    /**
     * Create original appointment
     * add user with status "none"
     */
    COUNTER,

    /**
     * see accept with "ignore conflicts"
     */
    ACCEPT_AND_IGNORE_CONFLICTS,

    /**
     * Cases:
     * 1. Delete Appointment or sequence: Just delete.
     * 2. Delete change exception: replace with delete exception
     * 3. Delete occurrence: Create delete exception
     */
    DELETE,

    /**
     * does nothing
     */
    IGNORE,

    /**
     * Edit change exception
     * See accept
     */
    ACCEPT_AND_REPLACE,

    /**
     * only mails
     * Send a REFRESH mail
     */
    REFRESH,

    /**
     * only mails
     * Send a REQUEST mail
     */
    SEND_APPOINTMENT,

    /**
     * for organizer:
     * add participant
     * Send a REQUEST mail
     */
    ACCEPT_PARTY_CRASHER,

    /**
     * for organizer:
     * accept a counter -> change appointment
     * Send a REQUEST mail
     */
    UPDATE,

    /**
     * just mail
     * Send a DECLINECOUNTER mail
     */
    DECLINECOUNTER,

    /**
     * Create change exception
     * If ORGANIZER: Send an ADD mail
     * If Attendee: Send a REPLY
     */
    CREATE;
}
