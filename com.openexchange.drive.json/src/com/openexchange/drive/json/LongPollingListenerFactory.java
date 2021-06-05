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

package com.openexchange.drive.json;

import java.util.Comparator;
import java.util.List;
import com.openexchange.drive.DriveSession;
import com.openexchange.drive.events.subscribe.SubscriptionMode;
import com.openexchange.exception.OXException;


/**
 * {@link LongPollingListenerFactory}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface LongPollingListenerFactory {

    /**
     * Creates a new {@link LongPollingListener} for the supplied drive session.
     *
     * @param session The drive session
     * @param rootFolderIDs The root folder IDs to listen for changes in
     * @return A new long polling listener instance
     * @param mode The subscription mode
     */
    LongPollingListener create(DriveSession session, List<String> rootFolderIDs, SubscriptionMode mode) throws OXException;

    /**
     * Gets the priority of the factory. With multiple factories being present, the factory with the highest priority is chosen when
     * creating new long polling listeners.
     *
     * @return The priority
     */
    int getPriority();

    /**
     * Comparator for the priority of listener factories - "highest priority first".
     */
    static final Comparator<LongPollingListenerFactory> PRIORITY_COMPARATOR = new Comparator<LongPollingListenerFactory>() {

        /**
         * Returns a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
         * @param factory1
         * @param factory2
         * @return
         */
        @Override
        public int compare(LongPollingListenerFactory factory1, LongPollingListenerFactory factory2) {
            if (factory1 == factory2) {
                return 0;
            }
            if (null == factory1) {
                return 1;
            }
            if (null == factory2) {
                return -1;
            }
            return factory2.getPriority() - factory1.getPriority();
        }
    };

}
