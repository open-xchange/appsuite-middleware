/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.sessiond.event;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;

/**
 * {@link SessiondEventConstants} - Provides both constants for {@link EventConstants#EVENT_TOPIC event topic} and property names accessible
 * by an {@link Event event}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SessiondEventConstants {

    /**
     * Initializes a new {@link SessiondEventConstants}
     */
    private SessiondEventConstants() {
        super();
    }

    /**
     * The topic on single session removal
     */
    public static final String TOPIC_REMOVE_SESSION = "com/openexchange/sessiond/remove/session";

    /**
     * The topic on session container removal
     */
    public static final String TOPIC_REMOVE_CONTAINER = "com/openexchange/sessiond/remove/container";

    /**
     * An array of {@link String string} including all known topics.
     * <p>
     * Needed on event handler registration to a bundle context.
     */
    static final String[] TOPICS = { TOPIC_REMOVE_SESSION, TOPIC_REMOVE_CONTAINER };

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
     * The property for a single session kept in event's properties
     */
    public static final String PROP_SESSION = "com.openexchange.sessiond.session";

    /**
     * The property for a session container kept in event's properties
     */
    public static final String PROP_CONTAINER = "com.openexchange.sessiond.container";
}
