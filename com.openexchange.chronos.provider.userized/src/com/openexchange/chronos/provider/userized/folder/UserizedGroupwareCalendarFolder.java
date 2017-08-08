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

package com.openexchange.chronos.provider.userized.folder;

import com.openexchange.chronos.provider.groupware.GroupwareCalendarFolder;
import java.util.Map;

/**
 * {@link UserizedGroupwareCalendarFolder} - {@link CalendarFolder} with user specific properties
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public interface UserizedGroupwareCalendarFolder extends GroupwareCalendarFolder {

    /**
     * Get the user ID the folder belongs to
     * 
     * @return The user ID
     */
    int getUserId();

    /**
     * Get the context ID of the user this folder belongs to
     * 
     * @return The context ID
     */
    int getContextId();

    /**
     * Indicates if the folder is subscribed or not
     * 
     * @return <code>true</code> if the folder is subscribed <code>false</code> otherwise
     */
    boolean isSubscribed();

    /**
     * Indicates if the folder is synchronized or not
     * 
     * @return <code>true</code> if the folder is synchronized <code>false</code> otherwise
     */
    boolean shouldSync();

    /**
     * Indicates if the folder has an user-specific name.
     * 
     * @return <code>true</code> if the user-specified an other folder name than specified in {@link CalendarFolder#getName()},
     *         <code>false</code> if there is no user-specific folder name.
     */
    boolean hasAlternativeName();

    /**
     * Indicates if the folder has an user-specific description
     * 
     * @return <code>true</code> if the user-specified an other description than specified in {@link CalendarFolder#getDescription()},
     *         <code>false</code> if there is no user-specific folder description.
     */
    boolean hasAlternativeDescription();

    /**
     * Get the user-specific folder name
     * 
     * @return The user-specific name or <code>null</code> if there is no user-specific folder name
     */
    String getAlternativeName();

    /**
     * Get the user-specific folder description
     * 
     * @return The user-specific folder description or <code>null</code> if there is no user-specific folder description
     */
    String getAlternativeDescription();

    /**
     * Get additional user-specific properties for this folder.
     * 
     * @return {@link Map} containing additional user-specific properties.
     */
    Map<String, String> additionalProperties();

}
