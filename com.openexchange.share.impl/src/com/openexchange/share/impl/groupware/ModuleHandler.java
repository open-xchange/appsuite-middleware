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

package com.openexchange.share.impl.groupware;

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
