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

package com.openexchange.sessiond.impl.container;

import com.openexchange.session.Session;
import com.openexchange.sessiond.impl.SessionImpl;

/**
 * {@link SessionControl} - Holds a {@link Session} instance and remembers life-cycle time stamps such as last-accessed, creation-time, etc.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface SessionControl {

    /** The container type; either short-term or long-term */
    public static enum ContainerType {
        /** Instance is managed in short-term container */
        SHORT_TERM,
        /** Instance is managed in long-term container */
        LONG_TERM;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Gets the type of the container, in which this instance is managed.
     *
     * @return The container type
     */
    ContainerType geContainerType();

    /**
     * Gets the session identifier
     *
     * @return The session identifier
     * @see com.openexchange.sessiond.impl.SessionImpl#getSessionID()
     */
    String getSessionID();

    /**
     * Gets the stored session
     *
     * @return The stored session
     */
    SessionImpl getSession();

    /**
     * Gets the creation-time time stamp
     *
     * @return The creation-time time stamp
     */
    long getCreationTime();

    /**
     * Checks if the session associated with this control holds specified context identifier
     *
     * @param contextId The context identifier to check against
     * @return <code>true</code> if session holds specified context identifier; otherwise <code>false</code>
     */
    boolean equalsContext(int contextId);

    /**
     * Checks if the session associated with this control holds specified user and context identifier
     *
     * @param userId The user identifier to check against
     * @param contextId The context identifier to check against
     * @return <code>true</code> if session holds specified user and context identifier; otherwise <code>false</code>
     */
    boolean equalsUserAndContext(int userId, int contextId);

    /**
     * Gets the context identifier
     *
     * @return The context identifier
     */
    int getContextId();

    /**
     * Gets the user identifier
     *
     * @return The user identifier
     */
    int getUserId();

}
