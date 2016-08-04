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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.websockets;

import java.util.Set;

/**
 * {@link WebSocketSession} - A session associated with a Web Socket to store states.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public interface WebSocketSession {

    /**
     * Gets the object bound with the specified name in this session, or
     * <code>null</code> if no object is bound under the name.
     *
     * @param name The string specifying the name of the object
     * @return The object bound to the specified name or <code>null</code>
     */
    Object getAttribute(String name);

    /**
     * Gets the set view for the names of all the objects bound to this session.
     *
     * @return The set view for attribute names
     */
    Set<String> getAttributeNames();

    /**
     * Binds an object to this session, using the name specified.
     * <p>
     * If an object of the same name is already bound to the session, the object is replaced.
     * <p>
     * If the value passed in is <code>null</code>, this has the same effect as calling <code>removeAttribute()</code>.
     *
     *
     * @param name The name to which the object is bound
     * @param value The object to be bound
     */
    void setAttribute(String name, Object value);

    /**
     * Removes the object bound with the specified name from this session.
     * <p>
     * If the session does not have an object bound with the specified name, this method does nothing.
     *
     * @param name The name of the object to remove from this session
     */
    void removeAttribute(String name);

}
