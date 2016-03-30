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

package com.openexchange.calendar.itip;


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
