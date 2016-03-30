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

package com.openexchange.context;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link ContextService} - Offers access method to context module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
@SingletonService
public interface ContextService {

    /**
     * Instantiates an implementation of the context interface and fill its
     * attributes according to the needs to be able to separate contexts.
     *
     * @param loginContextInfo
     *            the login info for the context.
     * @return the unique identifier of the context or <code>-1</code> if no
     *         matching context exists.
     * @throws OXException
     *             if an error occurs.
     */
    int getContextId(String loginContextInfo) throws OXException;

    /**
     * Gets the context for the given context unique identifier.
     *
     * @param contextId The unique identifier of the context.
     * @return The context
     * @throws OXException If the specified context cannot be found or the update is running/started.
     */
    Context getContext(int contextId) throws OXException;

    /**
     * This method works like {@link #getContext(int)} but it does not give a {@link OXException} if an update is running or must is
     * started.
     * @param contextId unique identifier of the context.
     * @return an implementation of the context or <code>null</code> if the context with the given identifier can't be found.
     * @throws OXException if an error occurs.
     */
    Context loadContext(int contextId) throws OXException;

    /**
     * Stores a internal context attribute.
     * <p>
     * This method might throw a {@link ContextExceptionCodes#CONCURRENT_ATTRIBUTES_UPDATE_DISPLAY} error in case a concurrent modification occurred. The
     * caller can decide to treat as an error or to simply ignore it.
     *
     * @param name Name of the attribute.
     * @param value Value of the attribute. If the value is <code>null</code>, the attribute is removed.
     * @param contextId Identifier of the context that attribute should be set.
     * @throws OXException if writing the attribute fails.
     */
    void setAttribute(String name, String value, int contextId) throws OXException;

    /**
     * Invalidates the context object in cache(s).
     *
     * @param contextId
     *            unique identifier of the context to invalidate
     * @throws OXException
     * @throws OXException
     *             if invalidating the context fails
     */
    void invalidateContext(int contextId) throws OXException;

    /**
     * Invalidates the context objects in cache(s).
     *
     * @param contextIDs unique identifiers of the contexts to invalidate
     * @throws OXException if invalidating the context fails
     */
    public void invalidateContexts(final int[] contextIDs) throws OXException;

    /**
     * Invalidates a login information in the cache.
     *
     * @param loginContextInfo
     *            login information to invalidate.
     * @throws OXException
     * @throws OXException
     *             if invalidating the login information fails.
     */
    void invalidateLoginInfo(String loginContextInfo) throws OXException;

    /**
     * Gives a list of all context ids which are stored in the config database.
     *
     * @return the list of context ids
     * @throws OXException
     *             if reading the contexts fails.
     */
    List<Integer> getAllContextIds() throws OXException;

}
