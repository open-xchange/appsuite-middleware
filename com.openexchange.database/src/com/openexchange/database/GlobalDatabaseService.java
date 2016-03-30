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

package com.openexchange.database;

import java.sql.Connection;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link GlobalDatabaseService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@SingletonService
public interface GlobalDatabaseService {

    /**
     * Gets a value indicating whether a global database is available for a specific context group or not.
     *
     * @param group The context group to check availability for, or <code>null</code> to check for the default fallback
     * @return <code>true</code> if a global database is available, <code>false</code>, otherwise
     */
    boolean isGlobalDatabaseAvailable(String group) throws OXException;

    /**
     * Gets a value indicating whether a global database is available for a specific group a context is associated with or not.
     *
     * @param group The identifier of the context to check availability for
     * @return <code>true</code> if a global database is available, <code>false</code>, otherwise
     */
    boolean isGlobalDatabaseAvailable(int contextId) throws OXException;

    /**
     * Gets a connection for read-only access to the global database of a specific context group.
     *
     * @param group The context group to get the connection for, or <code>null</code> to use the global fallback
     * @return The connection
     */
    Connection getReadOnlyForGlobal(String group) throws OXException;

    /**
     * Gets a connection for read-only access to the global database of a specific group a context is associated with.
     *
     * @param contextId The identifier of the context to get the connection for
     * @return The connection
     */
    Connection getReadOnlyForGlobal(int contextId) throws OXException;

    /**
     * Returns a read-only connection to the global database of a specific group to the pool.
     *
     * @param group The group to back the connection for
     * @param connection The connection to return
     */
    void backReadOnlyForGlobal(String group, Connection connection);

    /**
     * Returns a read-only connection to the global database of a specific group a context is associated with.
     *
     * @param contextId The identifier of the context to back the connection for
     * @param connection The connection to return
     */
    void backReadOnlyForGlobal(int contextId, Connection connection);

    /**
     * Gets a connection for read/write access to the global database of a specific context group.
     *
     * @param group The group to get the connection for
     * @return The connection
     */
    Connection getWritableForGlobal(String group) throws OXException;

    /**
     * Gets a connection for read/write access to the global database of a specific group a context is associated with.
     *
     * @param contextId The identifier of the context to get the connection for
     * @return The connection
     */
    Connection getWritableForGlobal(int contextId) throws OXException;

    /**
     * Returns a read/write connection to the global database of a specific context group to the pool.
     *
     * @param group The group to back the connection for
     * @param connection The connection to return
     */
    void backWritableForGlobal(String group, Connection connection);

    /**
     * Returns a read/write connection to the global database of a specific group a context is associated with.
     *
     * @param contextId The identifier of the context to back the connection for
     * @param connection The connection to return
     */
    void backWritableForGlobal(int contextId, Connection connection);

}
