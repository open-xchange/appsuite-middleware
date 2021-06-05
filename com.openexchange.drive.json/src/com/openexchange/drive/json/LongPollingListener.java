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

import java.util.List;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.drive.DriveSession;
import com.openexchange.drive.events.DriveEvent;
import com.openexchange.drive.events.subscribe.SubscriptionMode;
import com.openexchange.exception.OXException;

/**
 * {@link LongPollingListener}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface LongPollingListener {

    /**
     * Waits for a result carrying the drive actions resulting from an occurred event for the polling client.
     *
     * @param timeout The maximum timeout in milliseconds to wait for the result
     * @return The result
     * @throws OXException
     */
    AJAXRequestResult await(long timeout) throws OXException;

    /**
     * Notifies the listener about an incoming drive event.
     *
     * @param event The drive event
     */
    void onEvent(DriveEvent event);

    /**
     * Gets the drive session associated with this listener.
     *
     * @return The drive session
     */
    DriveSession getSession();

    /**
     * Gets a value indicating whether this listener's push token matches the supplied token value, trying to match either the token
     * itself or the md5 checksum of the token.
     *
     * @param tokenRef The push token reference to match
     * @return <code>true</code> if this listener's token or the md5 checksum of this listener's token matches, <code>false</code>,
     *         otherwise
     */
    boolean matches(String tokenRef);

    /**
     * Gets the root folder identifiers monitored by this listener.
     *
     * @return The root folder identifiers
     */
    List<String> getRootFolderIDs();

    /**
     * Gets the subscription mode of the listener.
     * 
     * @return The subscription mode
     */
    SubscriptionMode getSubscriptionMode();

}
