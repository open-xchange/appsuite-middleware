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

package com.openexchange.ms;




/**
 * {@link MsEventConstants}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.2
 */
public final class MsEventConstants {

    /**
     * Initializes a new {@link MsEventConstants}.
     */
    private MsEventConstants() {
        super();
    }

    // ------------------------------------------------------------------------------------------------- //

    /**
     * The topic intended to remotely republish received event.
     */
    public static final String TOPIC_REMOTE_REPUBLISH = "com/openexchange/ms/remote/republish";

    /**
     * An array of {@link String string} including all known topics.
     * <p>
     * Needed on event handler registration to a bundle context.
     */
    private static final String[] TOPICS = { TOPIC_REMOTE_REPUBLISH };

    /**
     * Gets an array of {@link String string} including all known topics.
     * <p>
     * Needed on event handler registration to a bundle context.
     *
     * @return An array of {@link String string} including all known topics.
     */
    public static String[] getAllTopics() {
        final String[] retval = new String[TOPICS.length];
        System.arraycopy(TOPICS, 0, retval, 0, TOPICS.length);
        return retval;
    }

    // ------------------------------------------------------------------------------------------------- //

    /**
     * The property name for the topic.
     */
    public static final String PROPERTY_TOPIC_NAME = "__topicName";

    /**
     * The property name for the message's data map (instance of <b><code>Map&lt;String, Object&gt;</code></b>).
     */
    public static final String PROPERTY_DATA_MAP = "__map";

}
