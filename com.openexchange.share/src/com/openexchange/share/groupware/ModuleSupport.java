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

package com.openexchange.share.groupware;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.ShareTargetPath;


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
     * Resolves the underlying groupware item for the given share target and returns an according {@link TargetProxy} instance. The item
     * is loaded using administrative access to the underlying module services. This method must only be used for administrative tasks
     * when no session object is available.
     *
     * @param targetPath The share target path
     * @param contextId The context identifier
     * @param guestId The identifier of the guest user to resolve the target for
     * @return The target proxy
     */
    TargetProxy resolveTarget(ShareTargetPath targetPath, int contextId, int guestId) throws OXException;

    /**
     * Resolves the module id to module name
     *
     * @param moduleId The module id
     * @return The module name
     */
    String getShareModule(int moduleId);

    /**
     * Resolves the module name to module id
     *
     * @param module The module name
     * @return The module id
     */
    int getShareModuleId(String module);

    /**
     * Gets a value indicating whether a share target is visible for the session's user or not, i.e. if the user has sufficient
     * permissions to read the folder or item represented by the share target.
     *
     * @param module The module
     * @param folder The folder ID; must be globally valid - not personalized in terms of the passed guest user ID
     * @param item The item ID or <code>null</code>; must be globally valid - not personalized in terms of the passed guest user ID
     * @param contextID The context ID
     * @param guestID The guest users ID
     * @return <code>true</code> if the share target is visible, <code>false</code>, otherwise
     * @throws OXException
     */
    boolean isVisible(int module, String folder, String item, int contextID, int guestID) throws OXException;

    /**
     * Gets a value indicating whether a share target may be adjusted by the session's user or not, i.e. if the user has sufficient
     * permissions to read & update the folder or item represented by the share target.
     *
     * @param target The share target to check
     * @param session The session of the user trying to adjust the share target
     * @return <code>true</code> if the share target is adjustable, <code>false</code>, otherwise
     */
    boolean mayAdjust(ShareTarget target, Session session) throws OXException;

    /**
     * Gets a value indicating whether a folder/item exists.
     *
     * @param module The module
     * @param folder The folder ID; must be globally valid - not personalized in terms of the passed guest user ID
     * @param item The item ID or <code>null</code>; must be globally valid - not personalized in terms of the passed guest user ID
     * @param contextID The context ID
     * @param guestID The guest users ID
     * @return <code>true</code> if the share target exists, <code>false</code>, otherwise
     * @throws OXException
     */
    boolean exists(int module, String folder, String item, int contextID, int guestID) throws OXException;

    /**
     * Gets a list of all share targets a specific guest user has access to.
     *
     * @param contextID The context identifier
     * @param guestID The identifier of the guest user
     * @return The share target proxies, or an empty list if there are none
     */
    List<TargetProxy> listTargets(int contextID, int guestID) throws OXException;

    /**
     * Gets a list of all share targets of a certain module a specific guest user has access to.
     *
     * @param contextID The context identifier
     * @param guestID The identifier of the guest user
     * @param module The share module identifier
     * @return The share target proxies, or an empty list if there are none
     */
    List<TargetProxy> listTargets(int contextID, int guestID, int module) throws OXException;

    /**
     * Gets the identifiers of those modules a specific guest user has access to, i.e. those where at least one share target for the
     * guest exists.
     *
     * @param contextID The context identifier
     * @param guestID The identifier of the guest user
     * @return The identifiers of the modules the guest user has access to, or an empty set if there are none
     */
    Collection<Integer> getAccessibleModules(int contextID, int guestID) throws OXException;

    /**
     * Gets the path for a given target and session. The target must contain IDs from the session users point of view.
     *
     * @param target The target
     * @param session The session
     */
    ShareTargetPath getPath(ShareTarget target, Session session) throws OXException;

    /**
     * Gets the path for a given target and guest user. The target must contain IDs from the guest users point of view.
     *
     * @param target The target
     * @param contextID The context ID
     * @param guestID The guest users ID
     */
    ShareTargetPath getPath(ShareTarget target, int contextID, int guestID) throws OXException;

    /**
     * Adjusts the IDs of a target to reflect the view of the the target user (i.e. the new permission entity).
     *
     * @param The target from the sharing users point of view
     * @param session The requesting users session
     * @param targetUserId The ID of the user to adjust the target for
     */
    ShareTarget adjustTarget(ShareTarget target, Session session, int targetUserId) throws OXException;

    /**
     * Adjusts the IDs of a target to reflect the view of the the target user (i.e. the new permission entity).
     *
     * @param The target from the sharing users point of view
     * @param contextId The context ID
     * @param requestUserId The requesting users ID
     * @param targetUserId The ID of the user to adjust the target for
     */
    ShareTarget adjustTarget(ShareTarget target, int contextId, int requestUserId, int targetUserId) throws OXException;

    /**
     * Checks whether a given share target is public, i.e. a public folder oder an item located within a public folder.
     *
     * @param target The share target from the session users point of view
     * @param session The session
     * @throws OXException
     */
    boolean isPublic(ShareTarget target, Session session) throws OXException;

}
