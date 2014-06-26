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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.index;

import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link IndexFacadeService} - Provides appropriate {@link IndexAccess} instances.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface IndexFacadeService {

    /**
     * Acquires an appropriate {@link IndexAccess} instance.
     * If the according index is locked, {@link IndexExceptionCodes#INDEX_LOCKED} is thrown.
     * <p>
     * Convenience method for:<br>
     * {@link #acquireIndexAccess(int, int, int) aquireIndexAccess(module, session.getUserId(), session.getContextId()}.
     *
     * @param module The module
     * @param session The session providing the user for whom to acquire the index access
     * @return The acquired index access.
     * @throws OXException If acquiring an index access fails for any reason
     */
    <V> IndexAccess<V> acquireIndexAccess(int module, Session session) throws OXException;

    /**
     * Acquires an appropriate {@link IndexAccess} instance.
     * If the according index is locked, {@link IndexExceptionCodes#INDEX_LOCKED} is thrown.
     *
     * @param module The module
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The acquired index access.
     * @throws OXException If acquiring an index access fails for any reason
     */
    <V> IndexAccess<V> acquireIndexAccess(int module, int userId, int contextId) throws OXException;

    /**
     * Releases specified {@link IndexAccess} instance.
     *
     * @param indexAccess The index access to release
     * @throws OXException If releasing specified index access fails
     */
    void releaseIndexAccess(IndexAccess<?> indexAccess) throws OXException;

}
