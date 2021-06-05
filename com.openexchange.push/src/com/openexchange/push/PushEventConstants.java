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

package com.openexchange.push;

/**
 * {@link PushEventConstants} - Provides constants for push events.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PushEventConstants {

    /**
     * Initializes a new {@link PushEventConstants}.
     */
    private PushEventConstants() {
        super();
    }

    /**
     * The topic of push events.
     */
    public static final String TOPIC = "com/openexchange/push";

    /**
     * The topic of push events for changed attributes of a folder's entry.
     */
    public static final String TOPIC_ATTR = "com/openexchange/push/attributes";

    /**
     * An array of {@link String string} including all known topics.
     * <p>
     * Needed on event handler registration to a bundle context.
     */
    private static final String[] TOPICS = { TOPIC, TOPIC_ATTR };

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

    /**
     * Whether the content or the folder itself has changed. Default is <code>true</code>; meaning if not present the folder content has
     * changed, but not the folder itself. Property value is of type <code>java.lang.Boolean</code>.
     */
    public static final String PROPERTY_CONTENT_RELATED = "com.openexchange.push.content-related";

    /**
     * The context ID property of a push event. Property value is of type <code>java.lang.Integer</code>.
     */
    public static final String PROPERTY_CONTEXT = "com.openexchange.push.context";

    /**
     * The user ID property of a push event. Property value is of type <code>java.lang.Integer</code>.
     */
    public static final String PROPERTY_USER = "com.openexchange.push.user";

    /**
     * The folder fullname property of a push event. Property value is of type <code>java.lang.String</code>.
     */
    public static final String PROPERTY_FOLDER = "com.openexchange.push.folder";

    /**
     * The session property of a push event. Property value is of type <code>com.openexchange.session.Session</code>.
     */
    public static final String PROPERTY_SESSION = "com.openexchange.push.session";

    /**
     * Force an immediate delivery of the associated event.
     */
    public static final String PROPERTY_IMMEDIATELY = "com.openexchange.push.immediately";

    /**
     * Sets that a event must not be forwarded to notification system. Property value is of type <code>java.lang.Boolean</code>.
     */
    public static final String PROPERTY_NO_FORWARD = "com.openexchange.push.noforward";

    /**
     * <b>Optional</b> property that specifies the identifiers of those messages that were newly received as a comma-separated string. e.g.
     * <code>"1234, 1235, 1236"</code>.<br>
     * Property value is of type <code>java.lang.String</code>.
     */
    public static final String PROPERTY_IDS = "com.openexchange.push.ids";

    /**
     * <b>Optional</b> property that signals that messages were deleted. Property value is of type <code>java.lang.Boolean</code>.
     */
    public static final String PROPERTY_DELETED = "com.openexchange.push.deleted";

    /**
     * <b>Optional</b> property providing an instance of type <code>com.openexchange.push.Container</code>.
     * <p>
     * Will not be remotely distributed!
     */
    public static final String PROPERTY_CONTAINER = "com.openexchange.push.container";

}
