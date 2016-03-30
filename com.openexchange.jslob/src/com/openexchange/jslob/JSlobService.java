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

package com.openexchange.jslob;

import java.util.Collection;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.jslob.shared.SharedJSlobService;
import com.openexchange.session.Session;

/**
 * {@link JSlobService} - The JSlob service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface JSlobService {

    /**
     * Gets the service identifier.
     *
     * @return The service identifier
     */
    String getIdentifier();

    /**
     * Gets the aliases for this service.
     *
     * @return The aliases for this service.
     */
    List<String> getAliases();

    /**
     * Gets the JSlob associated with given user in given context.
     *
     * @param id The identifier of the JSlob
     * @param userId session An active session
     * @return The JSlob
     * @throws OXException If JSlob cannot be returned
     */
    JSlob get(String id, Session session) throws OXException;

    /**
     * Gets the JSlob associated with given user in given context.
     *
     * @param ids The identifiers of the JSlobs
     * @param userId session An active session
     * @return The JSlobs
     * @throws OXException If JSlobs cannot be returned
     */
    List<JSlob> get(List<String> ids, Session session) throws OXException;

    /**
     * Gets the shared JSlob.
     *
     * @param id The identifier of the JSlob
     * @param session An active session
     * @return The JSlob
     * @throws OXException If JSlob cannot be returned
     */
    JSlob getShared(String id, Session session) throws OXException;

    /**
     * Gets the JSlobs associated with given user in given context.
     *
     * @param session an active session
     * @return The JSlobs
     * @throws OXException If JSlobs cannot be returned
     */
    Collection<JSlob> get(Session session) throws OXException;

    /**
     * Gets the shared JSlobs.
     *
     * @param session An active session
     * @return The JSlobs
     * @throws OXException If JSlobs cannot be returned
     */
    Collection<JSlob> getShared(Session session) throws OXException;

    /**
     * Sets the JSlob associated with given user in given context.
     * <p>
     * If passed JSlob is <code>null</code>, a delete is performed.
     *
     * @param id The path of the JSlob
     * @param jsonJSlob The JSlob or <code>null</code> for deletion
     * @param session an active session
     * @throws OXException If JSlob cannot be set
     */
    void set(String id, JSlob jsonJSlob, Session session) throws OXException;

    /**
     * Sets the shared JSlob.
     * <p>
     * If passed JSlob is <code>null</code>, a delete is performed.
     *
     * @param id The path of the JSlob
     * @param service The shared jslob service
     * @throws OXException If JSlob cannot be set
     */
    void setShared(String id, SharedJSlobService service) throws OXException;

    /**
     * Updates the JSlob associated with given user in given context.
     *
     * @param id The path of the JSlob
     * @param jsonUpdate The JSON update providing the data to update
     * @param session an active session
     * @throws OXException If update fails
     */
    void update(String id, JSONUpdate update, Session session) throws OXException;

}
