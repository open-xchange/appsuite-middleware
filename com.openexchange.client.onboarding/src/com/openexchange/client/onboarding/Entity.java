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

package com.openexchange.client.onboarding;

import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link Entity} - An on-boarding entity.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public interface Entity {

    /**
     * Gets the identifier.
     *
     * @return The identifier
     */
    String getId();

    /**
     * Checks if this entity is enabled.
     *
     * @param session The session to use
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     * @throws OXException If enabled flag cannot be returned
     */
    boolean isEnabled(Session session) throws OXException;

    /**
     * Checks if this entity is enabled.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     * @throws OXException If enabled flag cannot be returned
     */
    boolean isEnabled(int userId, int contextId) throws OXException;

    /**
     * Gets the display name appropriate for the specified user and context
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The display name
     * @throws OXException If display name cannot be returned
     */
    String getDisplayName(int userId, int contextId) throws OXException;

    /**
     * Gets the display name appropriate for the specified session
     *
     * @param session The session to use
     * @return The display name
     * @throws OXException If display name cannot be returned
     */
    String getDisplayName(Session session) throws OXException;

    /**
     * Gets the icon associated with this on-boarding entity.
     *
     * @param session The session to use
     * @return The icon
     * @throws OXException If icon cannot be returned
     */
    Icon getIcon(Session session) throws OXException;

    /**
     * Gets the optional description for this entity
     *
     * @param session The session to use
     * @return The description or <code>null</code>
     * @throws OXException If description cannot be returned
     */
    String getDescription(Session session) throws OXException;

}
