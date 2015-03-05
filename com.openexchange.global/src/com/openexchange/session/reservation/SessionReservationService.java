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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.session.reservation;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;


/**
 * {@link SessionReservationService} - Used to reserve a session while obtaining a token and to redeem that token against a valid session
 * later on.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
@SingletonService
public interface SessionReservationService {

    /**
     * Generates a session reservation for given arguments.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param timeout The associated timeout
     * @param unit The timeout's time unit
     * @param optState An optional state that will be applied to resulting reservation
     * @return The generated reservation
     * @throws OXException If operation fails
     */
    String reserveSessionFor(int userId, int contextId, long timeout, TimeUnit unit, Map<String, String> optState) throws OXException;

    /**
     * Gets the reservation associated with given token.
     *
     * @param token The reservation's token
     * @return The reservation or <code>null</code> if there is no such reservation or reservation is elapsed
     * @throws OXException If operation fails
     */
    Reservation getReservation(String token) throws OXException;

    /**
     * Redeems specified reservation and returns associated session
     *
     * @param reservationInfo The reservation information
     * @return The associated session or <code>null</code> if there is no such reservation or reservation is elapsed
     * @throws OXException If operation fails
     */
    Session redeemReservation(ReservationInfo reservationInfo) throws OXException;

}
