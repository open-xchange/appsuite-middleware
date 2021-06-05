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

package com.openexchange.ms.internal;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link HzDataUtility} - A utility class for Hazelcast-based messaging.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HzDataUtility {

    /**
     * Initializes a new {@link HzDataUtility}.
     */
    private HzDataUtility() {
        super();
    }

    // ------------------------------------- DELAY STUFF -------------------------------------------- //

    /**
     * The delay for pooled messages.
     */
    public static final long DELAY_MSEC = 5000L;

    /**
     * The frequency to check for delayed pooled messages.
     */
    public static final int DELAY_FREQUENCY = 3000;

    // ------------------------------------- CHUNK STUFF -------------------------------------------- //

    /**
     * The chunk size of a multiple message.
     */
    public static final int CHUNK_SIZE = 10;

    /**
     * The threshold when to switch to a multiple message.
     */
    public static final int CHUNK_THRESHOLD = 2;

    // ------------------------------------- MESSAGE DATA ------------------------------------------- //

    /**
     * The property name for the identifier of the sender that transmitted message data.
     */
    public static final String MESSAGE_DATA_SENDER_ID = "__senderId".intern();

    /**
     * The property name for transmitted message data object.
     */
    public static final String MESSAGE_DATA_OBJECT = "__object".intern();

    /**
     * The property to mark as a multiple transport.
     */
    public static final String MULTIPLE_MARKER = "__multiple".intern();

    /**
     * The property prefix on a multiple transport.
     */
    public static final String MULTIPLE_PREFIX = "__map".intern();

    /**
     * Generates message data for given arguments.
     *
     * @param e The message data object; POJOs preferred
     * @param senderId The sender identifier
     * @return The message data container
     */
    public static <E> Map<String, Object> generateMapFor(final E e, final String senderId) {
        final Map<String, Object> map = new LinkedHashMap<String, Object>(4);
        if (null != e) {
            map.put(MESSAGE_DATA_OBJECT, e);
        }
        if (null != senderId) {
            map.put(MESSAGE_DATA_SENDER_ID, senderId);
        }
        return map;
    }

    // ------------------------------------- OTHER STIFF ------------------------------------------- //

}
