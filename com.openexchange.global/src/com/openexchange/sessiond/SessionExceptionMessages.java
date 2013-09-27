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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.sessiond;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link SessionExceptionMessages}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class SessionExceptionMessages implements LocalizableStrings {

    public static final String SESSIOND_EXCEPTION_MSG = "Sessiond exception";

    // Maximum number of sessions elapsed
    public static final String MAX_SESSION_EXCEPTION_MSG = "Maximum number of sessions elapsed";

    public static final String SESSIOND_CONFIG_EXCEPTION_MSG = "Sessiond Config Exception";

    public static final String MISSING_PROPERTY_MSG = "Missing property '%s'";

    public static final String UNKNOWN_EVENT_TOPIC_MSG = "Unknown event topic %s";

    public static final String PASSWORD_UPDATE_FAILED_MSG = "Password could not be changed";

    public static final String MAX_SESSION_PER_USER_EXCEPTION_MSG = "Max. number of sessions exceeded for user %1$s in context %2$s";

    public static final String DUPLICATE_AUTHID_MSG = "Authentication identifier duplicate found. Existing session login: %1$s. Current denied login request: %2$s.";

    // This message is thrown if an inconsistancy in SessionD bundle is detected when it returns the wrong session for a given session identifier.
    // %1$s session identifier of returned session.
    // %2$s given session identifier.
    public static final String WRONG_SESSION_MSG = "SessionD returned wrong session with identifier %1$s for given session identifier %2$s.";

    // This message is thrown if an inconsistency in SessionD bundle is detected when a session is added and that sessions identifier is already in use.
    // %1$s is replaced with the already existing sessions login.
    // %1$s is replaced with the new sessions login.
    public static final String SESSIONID_COLLISION_MSG = "Got a collision while adding a new session to the session container. Colliding session has login %1$s and new session has login %2$s.";

    // This message is thrown if an inconsistency in SessionD bundle is detected when a session should be fetched by its random token.
    // %1$s is replaced with the session identifier that is returned from the data structures.
    // %2$s is replaced with that sessions random token.
    // %3$s is replaced with the random token for that a session should be found.
    // %4$s is replaced with the session identifier that is fetched from the random map.
    public static final String WRONG_BY_RANDOM_MSG = "Received wrong session %1$s having random %2$s when looking for random %3$s and session %4$s.";

    public static final String SESSION_PARAMETER_MISSING_MSG = "The session parameter is missing.";

    public static final String SESSION_EXPIRED_MSG = "Your session %s expired. Please start a new browser session.";

    public static final String CONTEXT_LOCKED_MSG = "Context is locked.";

    public static final String WRONG_CLIENT_IP_MSG = "Request to server was refused. Original client IP address changed. Please try again." + System.getProperty("line.separator") + "Client login IP changed from %1$s to %2$s and is not covered by IP white-list or netmask.";

    public static final String WRONG_SESSION_SECRET_MSG = "Your session was invalidated. Please try again.";

    // Max. number of sessions exceeded for client %1$s of user %2$s in context %3$s exceeded
    public static final String MAX_SESSION_PER_CLIENT_EXCEPTION_MSG = "Max. number of sessions exceeded for client %1$s of user %2$s in context %3$s";

    // This problem occurs if the session daemon is accessed before it is initialized completely.
    public static final String NOT_INITIALIZED_MSG = "Session daemon is not initialized yet.";

    // This exception is used only internally for tests.
    public static final String NOT_IMPLEMENTED_MSG = "Method not implemented.";

    // This exception is thrown if no session can be found for given server and/or client tokens. Internally 2 different codes indicate,
    // which of both tokens did not work.
    // %1$s is replaced with the server side token.
    // %2$s is replaced with the client side token.
    public static final String NO_SESSION_FOR_TOKENS_MSG = "Can not find a session for server token %1$s and client token %2$s.";

    private SessionExceptionMessages() {
        super();
    }
}
