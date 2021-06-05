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

package com.openexchange.websockets.grizzly.impl;

import com.openexchange.websockets.IndividualWebSocketListener;

/**
 * {@link IndividualWebSocketListenerAdapter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class IndividualWebSocketListenerAdapter extends WebSocketListenerAdapter {

    /**
     * Initializes a new {@link IndividualWebSocketListenerAdapter}.
     */
    protected IndividualWebSocketListenerAdapter(IndividualWebSocketListener individualWebSocketListener) {
        super(individualWebSocketListener);
    }

    /**
     * Creates a new adapter for the associated Web Socket listener that receives individual call-backs.
     *
     * @return The new adapter
     */
    public WebSocketListenerAdapter newAdapter() {
        return new WebSocketListenerAdapter(((IndividualWebSocketListener) webSocketListener).newInstance(), this);
    }

    /**
     * Gets the runtime class of the adapted {@link com.openexchange.websockets.IndividualWebSocketListener}.
     *
     * @return The runtime class
     */
    public Class<? extends IndividualWebSocketListener> getListenerClass() {
        return ((IndividualWebSocketListener) webSocketListener).getClass();
    }

}
