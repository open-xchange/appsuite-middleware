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

package com.openexchange.regional;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Locale;
import com.openexchange.exception.OXException;

/**
 * {@link RegionalSettingsService}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.3
 */
public interface RegionalSettingsService {

    /**
     * Gets the {@link RegionalSettingsImpl} for the given user
     *
     * @param contextId The context id
     * @param userId The user id
     * @return The {@link RegionalSettingsImpl} for the user or <code>null</code> if the user
     *         does not have any custom regional settings stored
     */
    RegionalSettings get(int contextId, int userId);

    /**
     * Creates or updates the regional settings of the user
     *
     * @param contextId The context id
     * @param userId The user id
     * @param settings The new settings
     * @param locale The locale of the user
     * @throws OXException if the operation fails
     */
    void save(int contextId, int userId, RegionalSettings settings, Locale locale) throws OXException;

    /**
     * Removes the custom regional settings for the given user
     * 
     * @param contextId The context id
     * @param id The user id
     * @throws OXException if the operation fails
     */
    void delete(int contextId, int id) throws OXException;

    /**
     * Gets a date format for the specified locale. If the user has a custom format defined this format is used instead.
     * 
     * @param contextId The context id
     * @param userId The user id
     * @param locale The locale of the user
     * @param style The formatting style. @see {@link DateFormat#getDateInstance(int, Locale)}
     * @return The {@link DateFormat}
     */
    DateFormat getDateFormat(int contextId, int userId, Locale locale, int style);

    /**
     * Gets a time format for the specified locale. If the user has a custom format defined this format is used instead.
     * 
     * @param contextId The context id
     * @param userId The user id
     * @param locale The locale of the user
     * @param style The formatting style. @see {@link DateFormat#getTimeInstance(int, Locale)}
     * @return The {@link DateFormat}
     */
    DateFormat getTimeFormat(int contextId, int userId, Locale locale, int type);

    /**
     * Gets a number format for the specified locale. If the user has a custom format defined this format is used instead.
     * 
     * @param contextId The context id
     * @param userId The user id
     * @param locale The locale of the user
     * @param format The format to use. E.g. <code>###.###,##</code>
     * @return The {@link NumberFormat}
     */
    NumberFormat getNumberFormat(int contextId, int userId, Locale locale, String format);
}
