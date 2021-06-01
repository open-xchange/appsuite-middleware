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

package com.openexchange.pns;

/**
 * {@link PushMatch} - Represents a matching push subscription of a user to a certain topic.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public interface PushMatch {

    /**
     * Gets the user identifier
     *
     * @return The user identifier
     */
    int getUserId();

    /**
     * Gets the context identifier
     *
     * @return The context identifier
     */
    int getContextId();

    /**
     * Gets the identifier of the client associated with this subscription.
     *
     * @return The client identifier
     */
    String getClient();

    /**
     * Gets the topic (colon-separated string) of this match; e.g.
     * <pre>"ox:mail:new"</pre>
     *
     * @return The topic
     */
    String getTopic();

    /**
     * Gets the identifier of the associated push transport.
     *
     * @return The transport identifier
     */
    String getTransportId();

    /**
     * Gets the subscription's token
     *
     * @return The token
     */
    String getToken();

    /**
     * Checks if this match is considered equal to specified object.
     *
     * @param other The other object
     * @return <code>true</code> if considered equal; otherwise <code>false</code>
     */
    @Override
    boolean equals(Object other);

}
