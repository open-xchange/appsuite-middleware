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

package com.openexchange.chronos.alarm.message;

import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmAction;
import com.openexchange.chronos.Event;
import com.openexchange.exception.OXException;

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

}
