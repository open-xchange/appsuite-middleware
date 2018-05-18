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

package com.openexchange.chronos.itip.generators;

import java.util.List;
import com.openexchange.chronos.itip.ITipMethod;
import com.openexchange.chronos.itip.ITipRole;
import com.openexchange.exception.OXException;

/**
 * {@link ITipMailGenerator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public interface ITipMailGenerator {

    /**
     * Generates an invitation mail to a new event for given participant.
     * 
     * @param participant The {@link NotificationParticipant} to send a mail to
     * @return A {@link NotificationMail}
     * @throws OXException If mail can't be created or rendered
     * @see ITipMethod#REQUEST
     */
    NotificationMail generateCreateMailFor(NotificationParticipant participant) throws OXException;

    /**
     * Generates a mail to updated an existing event for given participant.
     * 
     * @param participant The {@link NotificationParticipant} to send a mail to
     * @return A {@link NotificationMail}
     * @throws OXException If mail can't be created or rendered
     * @see ITipMethod#REQUEST
     */
    NotificationMail generateUpdateMailFor(NotificationParticipant participant) throws OXException;

    /**
     * Generates a deletion mail of an deleted event for given participant.
     * 
     * @param participant The {@link NotificationParticipant} to send a mail to
     * @return A {@link NotificationMail}
     * @throws OXException If mail can't be created or rendered
     * @see ITipMethod#CANCEL
     */
    NotificationMail generateDeleteMailFor(NotificationParticipant participant) throws OXException;

    /**
     * Generates a invitation mail to a new created event exception for given participant.
     * 
     * @param participant The {@link NotificationParticipant} to send a mail to
     * @return A {@link NotificationMail}
     * @throws OXException If mail can't be created or rendered
     * @see ITipMethod#REQUEST
     */
    NotificationMail generateCreateExceptionMailFor(NotificationParticipant participant) throws OXException;

    /**
     * Generates a refresh mail for given participant.
     * 
     * @param participant The {@link NotificationParticipant} to send a mail to
     * @return A {@link NotificationMail}
     * @throws OXException If mail can't be created or rendered
     * @see ITipMethod#REFRESH
     */
    NotificationMail generateRefreshMailFor(NotificationParticipant participant) throws OXException;

    /**
     * Generates a decline counter mail to given participant.
     * 
     * @param participant The {@link NotificationParticipant} to send a mail to
     * @return A {@link NotificationMail}
     * @throws OXException If mail can't be created or rendered
     * @see ITipMethod#DECLINECOUNTER
     */
    NotificationMail generateDeclineCounterMailFor(NotificationParticipant participant) throws OXException;

    /**
     * Generates an invitation mail to a new event for given participant.
     * 
     * @param email The mail address of the participant to send a mail to
     * @return A {@link NotificationMail}
     * @throws OXException If mail can't be created or rendered
     * @see ITipMethod#REQUEST
     */
    NotificationMail generateCreateMailFor(String email) throws OXException;

    /**
     * Generates a mail to updated an existing event for given participant.
     * 
     * @param email The mail address of the participant to send a mail to
     * @return A {@link NotificationMail}
     * @throws OXException If mail can't be created or rendered
     * @see ITipMethod#REQUEST
     */
    NotificationMail generateUpdateMailFor(String email) throws OXException;

    /**
     * Generates a deletion mail of an deleted event for given participant.
     * 
     * @param email The mail address of the participant to send a mail to
     * @return A {@link NotificationMail}
     * @throws OXException If mail can't be created or rendered
     * @see ITipMethod#CANCEL
     */
    NotificationMail generateDeleteMailFor(String email) throws OXException;

    /**
     * Generates a invitation mail to a new created event exception for given participant.
     * 
     * @param email The mail address of the participant to send a mail to
     * @return A {@link NotificationMail}
     * @throws OXException If mail can't be created or rendered
     * @see ITipMethod#REQUEST
     */
    NotificationMail generateCreateExceptionMailFor(String email) throws OXException;

    /**
     * Generates a refresh mail for given participant.
     * 
     * @param email The mail address of the participant to send a mail to
     * @return A {@link NotificationMail}
     * @throws OXException If mail can't be created or rendered
     * @see ITipMethod#REFRESH
     */
    NotificationMail generateRefreshMailFor(String email) throws OXException;

    /**
     * Generates a decline counter mail to given participant.
     * 
     * @param email The mail address of the participant to send a mail to
     * @return A {@link NotificationMail}
     * @throws OXException If mail can't be created or rendered
     * @see ITipMethod#DECLINECOUNTER
     */
    NotificationMail generateDeclineCounterMailFor(String email) throws OXException;

    /**
     * Get all recipients to send a mail to.
     * 
     * @return A {@link List} of all {@link NotificationParticipant}s
     * @see NotificationParticipantResolver#resolveAllRecipients(com.openexchange.chronos.Event, com.openexchange.chronos.Event, com.openexchange.groupware.ldap.User, com.openexchange.groupware.ldap.User, com.openexchange.groupware.contexts.Context,
     *      com.openexchange.session.Session, com.openexchange.chronos.CalendarUser)
     */
    List<NotificationParticipant> getRecipients();

    /**
     * If the user has the role of the {@link ITipRole#ORGANIZER}
     * 
     * @return <code>true</code> if the user is the organizer, <code>false</code> otherwise
     */
    boolean userIsTheOrganizer();

    /**
     * Clones the current actor and set {@link NotificationParticipant#setVirtual(boolean)} to <code>true</code>.
     * 
     * Efficiently enables mail generation for the current actor/user.
     */
    void noActor();

}
