/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.share.core;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.groupware.TargetProxy;


/**
 * {@link ModuleHandler}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public interface ModuleHandler extends ModuleAdjuster {

    /**
     * Loads a list of target proxies, one for every passed share target.
     *
     * @param targets The share targets
     * @param parameters The parameters
     * @return the list of proxies in the same order as the according targets
     * @throws OXException
     */
    List<TargetProxy> loadTargets(List<ShareTarget> targets, HandlerParameters parameters) throws OXException;

    /**
     * Loads a proxy object for the given share target.
     *
     * @param target The share target
     * @param session The session
     * @return The proxy
     * @throws OXException
     */
    TargetProxy loadTarget(ShareTarget target, Session session) throws OXException;

    /**
     * Loads a proxy object for the given share target.
     *
     * @param folder The folder
     * @param item The item
     * @param isPublic <code>true</code> if the items parent folder is public
     * @param context The context
     * @param guestID The guests ID from whose point of view the target shall be loaded. May be <code>&lt;= 0</code> to load it
     * from a global perspective
     * @return The proxy
     * @throws OXException
     */
    TargetProxy loadTarget(String folder, String item, Context context, int guestID) throws OXException;

    /**
     * Gets whether the passed target proxy may be shared by the user who requested to load it.
     *
     * @param proxy The proxy
     * @param parameters The parameters
     * @return <code>true</code> if sharing is allowed
     */
    boolean canShare(TargetProxy proxy, HandlerParameters parameters);

    /**
     * Writes all changes made to the passed target proxies to the underlying module-specific storage.
     *
     * @param modified The modified target proxies
     * @param parameters The parameters
     * @throws OXException
     */
    void updateObjects(List<TargetProxy> modified, HandlerParameters parameters) throws OXException;

    /**
     * Writes all changes made to the passed target proxies to the underlying module-specific storage.
     *
     * @param modified The modified target proxies
     * @param parameters The parameters
     * @throws OXException
     */
    void touchObjects(List<TargetProxy> touched, HandlerParameters parameters) throws OXException;

    /**
     * Gets a value indicating whether a share target is visible for the session's user or not, i.e. if the user has sufficient
     * permissions to read the folder or item represented by the share target.
     *
     * @param folder The folder ID; must be globally valid - not personalized in terms of the passed guest user ID
     * @param item The item ID or <code>null</code>; must be globally valid - not personalized in terms of the passed guest user ID
     * @param contextID The context ID
     * @param guestID The guest users ID
     * @return <code>true</code> if the share target is visible, <code>false</code>, otherwise
     * @throws OXException
     */
    boolean isVisible(String folder, String item, int contextID, int guestID) throws OXException;

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
     * @param folder The folder ID; must be globally valid - not personalized in terms of the passed guest user ID
     * @param item The item ID or <code>null</code>; must be globally valid - not personalized in terms of the passed guest user ID
     * @param contextID The context ID
     * @param guestID The guest users ID
     * @return <code>true</code> if the share target exists, <code>false</code>, otherwise
     * @throws OXException
     */
    boolean exists(String folder, String item, int contextID, int guestID) throws OXException;

    /**
     * Gets a list of all share targets accessible by a specific guest user.
     *
     * @param contextID The context identifier
     * @param guestID The guest identifier
     * @return The share targets, or an empty list of there are none
     */
    List<TargetProxy> listTargets(int contextID, int guestID) throws OXException;

    /**
     * Gets a value indicating whether a specific guest user has at access to at least one target or not.
     *
     * @param contextID The context identifier
     * @param guestID The guest identifier
     * @return <code>true</code>, if there's at least one targetaccessible, <code>false</code>, otherwise
     */
    boolean hasTargets(int contextID, int guestID) throws OXException;

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

}
