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

package com.openexchange.share.groupware;

import java.util.Collection;
import com.openexchange.exception.OXException;
import com.openexchange.share.ShareTarget;


/**
 * {@link TargetUpdate} allows to modify the groupware items for one or more share targets within a
 * single transaction. It behaves module-independent, i.e. you can modify items from different modules
 * within one update call.<br>
 * <br>
 * Using an instance of this class requires a specific call semantic:<br>
 * <pre>
 * TargetUpdate update = moduleSupport.prepareUpdate(session, writeCon);
 * try {
 *     update.fetch(targetsToModify);
 *     for (ShareTarget target : targetsToModify) {
 *         TargetProxy proxy = update.get(target);
 *         // apply modifications to proxy
 *     }
 *     update.run();
 * } finally {
 *     update.close();
 * }
 * </pre>
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public interface TargetUpdate {

    /**
     * Fetches all groupware items denoted by the given share targets. Subsequently a {@link TargetProxy}
     * instance can be obtained for every passed target by calling {@link #get(ShareTarget)}.
     *
     * @param targets The share targets to fetch
     * @throws OXException if fetching any target fails
     */
    void fetch(Collection<ShareTarget> targets) throws OXException;

    /**
     * Obtains a proxy object for the given share target.
     *
     * @param target The target
     * @return The proxy instance
     */
    TargetProxy get(ShareTarget target);

    /**
     * Runs the update, i.e. modifications of objects are applied to the underlying module storages for
     * every obtained {@link TargetProxy} whose {@link TargetProxy#wasModified()} method returns <code>true</code>.
     *
     * @throws OXException if any update call fails
     */
    void run() throws OXException;

    /**
     * Closes this instance and cleans up any open resources.
     */
    void close();

}
