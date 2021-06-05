package com.openexchange.oauth;

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

/**
 * {@link OAuthEventConstants}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class OAuthEventConstants {
    /**
     * The topic of oauth storage events.
     */
    public static final String TOPIC = "com/openexchange/oauth";

    /**
     * The topic of oauth storage delete events.
     */
    public static final String TOPIC_DELETE = "com/openexchange/oauth/delete";

    /**
     * An array of {@link String string} including all known topics.
     * <p>
     * Needed on event handler registration to a bundle context.
     */
    private static final String[] TOPICS = { TOPIC, TOPIC_DELETE};

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
     * The context ID property of an event. Property value is of type <code>java.lang.Integer</code>.
     */
    public static final String PROPERTY_CONTEXT = "context";

    /**
     * The user ID property of an event. Property value is of type <code>java.lang.Integer</code>.
     */
    public static final String PROPERTY_USER = "user";

    /**
     * The <b>optional</b> session property of an event. Property value is of type <code>com.openexchange.session.Session</code>.
     */
    public static final String PROPERTY_SESSION = "session";

    /**
     * The oauth account id property of an event. Property value is of type <code>int</code>.
     */
    public static final String PROPERTY_ID = "id";
}
