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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.event.impl;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.openexchange.server.services.ServerServiceRegistry;


/**
 * {@link LoginEvent}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class LoginEvent {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LoginEvent.class);

    public static final String TOPIC = "com/openexchange/login";

    private static final String USER_KEY = "USER";
    private static final String CONTEXT_KEY = "CONTEXT";
    private static final String SESSION_KEY = "SESSION";


    private final int userId;
    private final int contextId;
    private final String sessionId;

    public LoginEvent(int userId, int contextId, String sessionId) {
        super();
        this.userId = userId;
        this.contextId = contextId;
        this.sessionId = sessionId;
    }

    public LoginEvent(Event event) {
        if(!TOPIC.equals(event.getTopic())) {
            throw new IllegalArgumentException("Can only handle events with topic "+TOPIC);
        }
        this.userId = (Integer) event.getProperty(USER_KEY);
        this.contextId = (Integer) event.getProperty(CONTEXT_KEY);
        this.sessionId = (String) event.getProperty(SESSION_KEY);

    }


    public int getUserId() {
        return userId;
    }


    public int getContextId() {
        return contextId;
    }


    public String getSessionId() {
        return sessionId;
    }

    public void post() {
        final EventAdmin eventAdmin = ServerServiceRegistry.getInstance().getService(EventAdmin.class);
        if(eventAdmin == null) {
            LOG.debug("Event Admin is disabled, so skipping LoginEvent");
            return;
        }
        Dictionary ht = new Hashtable();
        ht.put(USER_KEY, userId);
        ht.put(CONTEXT_KEY, contextId);
        ht.put(SESSION_KEY, sessionId);

        Event event = new Event(TOPIC, ht);

        eventAdmin.postEvent(event);
    }

}
