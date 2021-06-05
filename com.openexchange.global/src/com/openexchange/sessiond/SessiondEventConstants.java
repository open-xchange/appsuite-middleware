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

package com.openexchange.sessiond;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;

/**
 * {@link SessiondEventConstants} - Provides constants for {@link EventConstants#EVENT_TOPIC event topic} and property names accessible by
 * an {@link Event event}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SessiondEventConstants {

    private SessiondEventConstants() {
        super();
    }

    /**
     * The topic on last session gone for a certain context.
     * <p>
     * Provides {@link #PROP_CONTEXT_ID} property.
     */
    public static final String TOPIC_LAST_SESSION_CONTEXT = "com/openexchange/sessiond/remove/lastcontext";

    /**
     * The topic on last session gone for a certain user.
     * <p>
     * Provides {@link #PROP_CONTEXT_ID} and {@link #PROP_USER_ID} properties.
     */
    public static final String TOPIC_LAST_SESSION = "com/openexchange/sessiond/remove/last";

    /**
     * The topic on single session removal.
     * <p>
     * Provides {@link #PROP_SESSION} property.
     */
    public static final String TOPIC_REMOVE_SESSION = "com/openexchange/sessiond/remove/session";

    /**
     * The topic on session container removal.
     * <p>
     * Provides {@link #PROP_CONTAINER} property.
     */
    public static final String TOPIC_REMOVE_CONTAINER = "com/openexchange/sessiond/remove/container";

    /**
     * This event topic is used when sessions walk into the long term session life time container. If this event is emitted all temporary
     * session data should be removed. A complete UI reload is suggested to get the session back out of the long term life time container or
     * at first, we expect that to reduce the amount of used memory.
     * <p>
     * Provides {@link #PROP_CONTAINER} property.
     */
    public static final String TOPIC_REMOVE_DATA = "com/openexchange/sessiond/remove/data";

    /**
     * This event topic is used when a session is reactivated from the long term session life time container. Background tasks for the
     * session can be reactivated on this event again.
     */
    public static final String TOPIC_REACTIVATE_SESSION = "com/openexchange/sessiond/reactivate/session";

    /**
     * The topic on single session creation.
     */
    public static final String TOPIC_ADD_SESSION = "com/openexchange/sessiond/add/session";

    /**
     * The topic for a single session put into session storage.
     */
    public static final String TOPIC_STORED_SESSION = "com/openexchange/sessiond/stored/session";

    /**
     * The topic for a single session restored by a fetched one from session storage.
     */
    public static final String TOPIC_RESTORED_SESSION = "com/openexchange/sessiond/restored/session";

    /**
     * The topic on a session being 'touched', i.e. being put into the first session container.
     */
    public static final String TOPIC_TOUCH_SESSION = "com/openexchange/sessiond/touch/session";

    /**
     * An array of {@link String string} including all known topics.
     * <p>
     * Needed on event handler registration to a bundle context.
     */
    private static final String[] TOPICS = { TOPIC_LAST_SESSION_CONTEXT, TOPIC_LAST_SESSION, TOPIC_REMOVE_SESSION, TOPIC_REMOVE_CONTAINER, TOPIC_REMOVE_DATA,
        TOPIC_ADD_SESSION, TOPIC_REACTIVATE_SESSION, TOPIC_STORED_SESSION, TOPIC_TOUCH_SESSION, TOPIC_RESTORED_SESSION };

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
     * The property for a user identifier kept in event's properties.
     * <p>
     * Target object is an instance of <tt>java.lang.Integer</tt>.
     */
    public static final String PROP_USER_ID = "com.openexchange.sessiond.userId";

    /**
     * The property for a context identifier kept in event's properties.
     * <p>
     * Target object is an instance of <tt>java.lang.Integer</tt>.
     */
    public static final String PROP_CONTEXT_ID = "com.openexchange.sessiond.contextId";

    /**
     * The property for a single session kept in event's properties.
     * <p>
     * Target object is an instance of <tt>com.openexchange.session.Session</tt>.
     */
    public static final String PROP_SESSION = "com.openexchange.sessiond.session";

    /**
     * The property for a session counter kept in event's properties.
     * <p>
     * Target object is an instance of <tt>com.openexchange.sessiond.SessionCounter</tt>.
     */
    public static final String PROP_COUNTER = "com.openexchange.sessiond.counter";

    /**
     * The property for a session container kept in event's properties.
     * <p>
     * Target object is an instance of <tt>java.util.Map&lt;String, Session&gt;</tt>.
     */
    public static final String PROP_CONTAINER = "com.openexchange.sessiond.container";

}
