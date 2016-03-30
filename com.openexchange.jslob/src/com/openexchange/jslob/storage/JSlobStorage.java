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

package com.openexchange.jslob.storage;

import java.util.Collection;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.jslob.JSlob;
import com.openexchange.jslob.JSlobId;

/**
 * {@link JSlobStorage} - The JSlob storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface JSlobStorage {

    /**
     * Gets the identifier of this JSlob storage.
     *
     * @return The identifier of this JSlob storage.
     */
    String getIdentifier();

    /**
     * Stores an element with the given identifier.
     *
     * @param id Identifier.
     * @param t Element.
     * @return <code>true</code> if there was no such element before; other <code>false</code> if another one had been replaced
     * @throws OXException If storing fails
     */
    boolean store(JSlobId id, JSlob t) throws OXException;

    /**
     * Reads the element associated with the given identifier.
     *
     * @param id The identifier
     * @return The element.
     * @throws OXException If loading fails or no element is associated with specified identifier
     */
    JSlob load(JSlobId id) throws OXException;

    /**
     * Invalidates denoted element.
     *
     * @param id The identifier
     */
    void invalidate(JSlobId id);

    /**
     * Reads the element associated with the given identifier.
     *
     * @param id The identifier.
     * @return The element or <code>null</code>
     * @throws OXException If loading fails
     */
    JSlob opt(JSlobId id) throws OXException;

    /**
     * Reads the elements associated with the given identifiers.
     *
     * @param ids The identifiers
     * @return The elements
     * @throws OXException If loading fails
     */
    List<JSlob> list(List<JSlobId> ids) throws OXException;

    /**
     * Reads the elements associated with the given identifier.
     *
     * @param id The identifier.
     * @return The elements
     * @throws OXException If loading fails
     */
    Collection<JSlob> list(JSlobId id) throws OXException;

    /**
     * Deletes the element associated with the given identifier.
     *
     * @param id Identifier.
     * @return The deleted Element.
     * @throws OXException If removal fails
     */
    JSlob remove(JSlobId id) throws OXException;

    /**
     * Marks the entry associated with given identifier as locked.
     *
     * @param jslobId The JSlob identifier
     * @return <code>true</code> if this call successfully set the lock; otherwise <code>false</code> if already locked
     * @throws OXException If setting the lock fails
     */
    boolean lock(JSlobId jslobId) throws OXException;

    /**
     * Marks the entry associated with given identifier as unlocked.
     *
     * @param jslobId The JSlob identifier
     * @throws OXException If setting the unlock fails
     */
    void unlock(JSlobId jslobId) throws OXException;

}
