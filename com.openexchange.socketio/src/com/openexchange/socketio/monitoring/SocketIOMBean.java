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

package com.openexchange.socketio.monitoring;

import java.util.List;
import javax.management.MBeanException;
import com.openexchange.management.MBeanMethodAnnotation;

/**
 * {@link SocketIOMBean} - The monitoring bean for Socket.IO.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public interface SocketIOMBean {

    /** The MBean's domain */
    public static final String DOMAIN = "com.openexchange.socketio";

    /**
     * Gets the number of open Socket.IO sessions on this node
     *
     * @return The number of open Socket.IO sessions
     * @throws MBeanException If number of open Socket.IO sessions cannot be returned
     */
    @MBeanMethodAnnotation (description="Gets the number of open Socket.IO sessions on this node", parameters={}, parameterDescriptions={})
    long getNumberOfSessions() throws MBeanException;

    /**
     * Lists the identifiers of all of currently active Socket.IO sessions.
     *
     * @return The session identifiers
     * @throws MBeanException If session identifiers cannot be returned
     */
    @MBeanMethodAnnotation (description="Lists the identifiers of all of currently active Socket.IO sessions", parameters={}, parameterDescriptions={})
    List<String> listSessionIds() throws MBeanException;

    /**
     * Gets the names of such namespaces that are in use by specified session.
     *
     * @param sessionId The session identifier
     * @return The namespace names or <code>null</code> if no such session exists
     * @throws MBeanException If namespace names cannot be returned
     */
    @MBeanMethodAnnotation (description="Gets the names of such namespaces that are in use by specified session", parameters={"sessionId"}, parameterDescriptions={"The session identifier"})
    List<String> getNamespaceNames(String sessionId) throws MBeanException;

    /**
     * Gets the number of registered Web Socket connections.
     *
     * @return The number of registered Web Socket connections
     * @throws MBeanException If number of registered Web Socket connections cannot be returned
     */
    @MBeanMethodAnnotation (description="Gets the number of registered Web Socket connections", parameters={}, parameterDescriptions={})
    long getNumberOfRegisteredWebSocketConnections() throws MBeanException;

}
