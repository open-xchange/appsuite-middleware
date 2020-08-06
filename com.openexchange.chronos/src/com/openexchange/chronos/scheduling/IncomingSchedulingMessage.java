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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.chronos.scheduling;

import java.util.Date;
import java.util.Optional;
import com.openexchange.annotation.NonNull;
import com.openexchange.chronos.CalendarObjectResource;

/**
 * {@link IncomingSchedulingMessage} - Object containing information about an external triggered update of an calendar resource
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
public interface IncomingSchedulingMessage {

    /**
     * The {@link SchedulingMethod} to process
     * 
     * @return The {@link SchedulingMethod}
     */
    @NonNull
    SchedulingMethod getMethod();

    /**
     * Get the user identifier for whom to apply the change for
     *
     * @return The identifier of the target user.
     */
    int getTargetUser();

    /**
     * Get the object that triggered the scheduling
     *
     * @return The object
     */
    @NonNull
    IncomingSchedulingObject getSchedulingObject();

    /**
     * Get a the {@link CalendarObjectResource} as transmitted by the external
     * entity scheduling the change.
     * 
     * @return {@link CalendarObjectResource}
     */
    @NonNull
    CalendarObjectResource getResource();

    /**
     * The date when the change was created
     *
     * @return The date of the change
     */
    @NonNull
    Date getTimeStamp();

    /**
     * Get additional information.
     * 
     * @param key The key for the value
     * @param clazz The class the value has
     * @return An Optional holding the value casted to the given class
     */
    <T> Optional<T> getAdditional(String key, Class<T> clazz);

}
