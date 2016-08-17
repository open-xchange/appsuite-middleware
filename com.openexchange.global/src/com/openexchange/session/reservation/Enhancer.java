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

package com.openexchange.session.reservation;

import java.util.Map;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.ResponseEnhancement;
import com.openexchange.authentication.SessionEnhancement;


/**
 * An enhancer is used to enhance the {@link Authenticated} instance that is created by the <code>redeemReservation</code> login action
 * to create a new session. Every user of the {@link SessionReservationService} may register his own enhancer as OSGi service. When
 * the logout is performed then, all enhancers are called. You should mark your reservations via a property in the state map to recognize
 * them later on.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 * @see EnhancedAuthenticated
 */
public interface Enhancer {

    /**
     * Allows customization of the {@link Authenticated} instance created by the <code>redeemReservation</code> login action.
     * Probably you want to return a copy of the passed authenticated here, which additionally extends {@link ResponseEnhancement}
     * and/or {@link SessionEnhancement} to set cookies/headers/session parameters. Multiple enhancers are called subsequently, so
     * you need to preserve already made enhancements. Best practice is to use {@link EnhancedAuthenticated}, which will do all the
     * magic for you.
     *
     * @param authenticated The authenticated based on the user and context of the according reservation
     * @param reservationState The state map that was passed when the reservation was attempted via
     *        {@link SessionReservationService#reserveSessionFor(int, int, long, java.util.concurrent.TimeUnit, Map)}
     * @return The enhanced authenticated. Never return <code>null</code> here!
     * @see SessionReservationService#reserveSessionFor(int, int, long, java.util.concurrent.TimeUnit, Map)
     */
    Authenticated enhance(Authenticated authenticated, Map<String, String> reservationState);

}
