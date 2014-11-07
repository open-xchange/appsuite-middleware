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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.share.groupware;

import java.sql.Connection;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.share.ShareTarget;


/**
 * Sharing requires some module-specific actions like changing permissions of targets or
 * getting required data from those. {@link ModuleSupport} is meant to provide an abstraction
 * layer that allows module-agnostic access to groupware items and services. This layer
 * consists of interfaces which allow operations that are common for all module implementations.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public interface ModuleSupport {

    /**
     * Prepares an update procedure to modify the groupware items for one or more share targets.
     * {@link TargetUpdate} behaves module-independent, i.e. you can modify items from different
     * modules within one update call.
     *
     * @param session The current session
     * @param writeCon A transactional writable database connection to use for the update operations.
     * @return A new {@link TargetUpdate} instance
     * @throws OXException if preparing the update procedure fails
     */
    TargetUpdate prepareUpdate(Session session, Connection writeCon) throws OXException;

    /**
     * Initiates an update operation by spawning an appropriate {@link TargetUpdate} using the supplied
     * writable connection to the database. The {@link TargetUpdate} instance is using administrative access
     * to the underlying module services. This method must only be used for administrative tasks when no
     * session object is available.
     *
     * @param contextID The context identifier
     * @param writeCon A transactional writable database connection to use for the update operations.
     * @return The target update
     */
    TargetUpdate prepareAdministrativeUpdate(int contextID, Connection writeCon) throws OXException;

    /**
     * Loads the groupware item for the given share target and returns an according
     * {@link TargetProxy} instance.
     *
     * @param target The target to get the proxy for
     * @param session The current session
     * @return The target proxy
     * @throws OXException if loading fails
     */
    TargetProxy load(ShareTarget target, Session session) throws OXException;

    /**
     * Loads the groupware item for the given share target and returns an according {@link TargetProxy} instance.
     * The item is loaded using administrative access to the underlying module services. This method must only
     * be used for administrative tasks when no session object is available.
     *
     * @param contextID The context identifier
     * @param target The target to get the proxy for
     * @return The proxy
     */
    TargetProxy loadAsAdmin(int contextID, ShareTarget target) throws OXException;

    /**
     * Optionally adjusts a share target to be used by a specific user. This might be required if the target identifiers are different
     * depending on the user who accesses the share target, especially if the user is a guest or not.
     *
     * @param target The share target to adjust
     * @param contextID The identifier of the context the user is located in
     * @param userID The identifier of the user to adjust the share target for
     * @param isGuest <code>true</code> if the user identifier refers to a guest user, <code>false</code>, otherwise
     * @return The adjusted target, or the supplied target if no adjustments were necessary
     */
    ShareTarget adjustTarget(ShareTarget target, int contextID, int userID, boolean isGuest) throws OXException;

}
