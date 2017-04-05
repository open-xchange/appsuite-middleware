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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.config.lean;

import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link LeanConfigurationService} - A service combining the ConfigView (config cascade) and ConfigurationService
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since 7.8.4
 */
@SingletonService
public interface LeanConfigurationService {

    /**
     * Fetches the string value of the {@link Property}. If the property is not found, then the default value
     * of that property is returned.
     *
     * @param property The {@link Property} to fetch
     * @return The string value of the property
     */
    String getProperty(Property property);

    /**
     * Fetches the integer value of the {@link Property}. If the property is not found, then the default value
     * of that property is returned.
     *
     * @param property The {@link Property} to fetch
     * @return The integer value of the property
     */
    int getIntProperty(Property property);

    /**
     * Fetches the boolean value of the {@link Property}. If the property is not found, then the default value
     * of that property is returned.
     *
     * @param property The {@link Property} to fetch
     * @return The boolean value of the property
     */
    boolean getBooleanProperty(Property property);

    /**
     * Fetches the float value of the {@link Property}. If the property is not found, then the default value
     * of that property is returned.
     *
     * @param property The {@link Property} to fetch
     * @return The float value of the property
     */
    float getFloatProperty(Property property);

    /**
     * Fetches the long value of the {@link Property}. If the property is not found, then the default value
     * of that property is returned.
     *
     * @param property The {@link Property} to fetch
     * @return The long value of the property
     */
    long getLongProperty(Property property);

    /**
     * Fetches the {@link String} value of specified {@link Property} for
     * the specified user in the specified context via ConfigCascade
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param property The {@link Property} name to fetch
     * @return The {@link String} value of the property
     */
    String getProperty(int userId, int contextId, Property property);

    /**
     * Fetches the {@link Integer} value of specified {@link Property} for
     * the specified user in the specified context via ConfigCascade
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param property The {@link Property} name to fetch
     * @return The {@link Integer} value of the property
     */
    int getIntProperty(int userId, int contextId, Property property);

    /**
     * Fetches the {@link Boolean} value of specified {@link Property} for
     * the specified user in the specified context via ConfigCascade
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param property The {@link Property} name to fetch
     * @return The {@link Boolean} value of the property
     */
    boolean getBooleanProperty(int userId, int contextId, Property property);

    /**
     * Fetches the {@link Float} value of specified {@link Property} for
     * the specified user in the specified context via ConfigCascade
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param property The {@link Property} name to fetch
     * @return The {@link Float} value of the property
     */
    float getFloatProperty(int userId, int contextId, Property property);

    /**
     * Fetches the {@link Long} value of specified {@link Property} for
     * the specified user in the specified context via ConfigCascade
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param property The {@link Property} name to fetch
     * @return The {@link Long} value of the property
     */
    long getLongProperty(int userId, int contextId, Property property);
}
