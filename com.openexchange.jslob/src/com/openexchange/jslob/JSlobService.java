/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
     * <p>
     * Calling this method is the same as {@link #get(String, boolean, Session)} with <code>allowInjectingConfigTreeSettings</code> set to <code>true</code>
     *
     * @param id The identifier of the JSlob
     * @param session The session providing user data
     * @return The JSlob
     * @throws OXException If JSlob cannot be returned
     */
    JSlob get(String id, Session session) throws OXException;

    /**
     * Gets the JSlob associated with given user in given context.
     *
     * @param id The identifier of the JSlob
     * @param allowInjectingConfigTreeSettings <code>true</code> to allow injecting settings from config-tree; otherwise <code>false</code>
     * @param session The session providing user data
     * @return The JSlob
     * @throws OXException If JSlob cannot be returned
     */
    JSlob get(String id, boolean allowInjectingConfigTreeSettings, Session session) throws OXException;

    /**
     * Gets the JSlob associated with given user in given context.
     * <p>
     * Calling this method is the same as {@link #get(List, boolean, Session)} with <code>allowInjectingConfigTreeSettings</code> set to <code>true</code>
     *
     * @param ids The identifiers of the JSlobs
     * @param userId session An active session
     * @return The JSlobs
     * @throws OXException If JSlobs cannot be returned
     */
    List<JSlob> get(List<String> ids, Session session) throws OXException;

    /**
     * Gets the JSlob associated with given user in given context.
     *
     * @param ids The identifiers of the JSlobs
     * @param allowInjectingConfigTreeSettings <code>true</code> to allow injecting settings from config-tree; otherwise <code>false</code>
     * @param session The session providing user data
     * @return The JSlobs
     * @throws OXException If JSlobs cannot be returned
     */
    List<JSlob> get(List<String> ids, boolean allowInjectingConfigTreeSettings, Session session) throws OXException;

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
     * <p>
     * Calling this method is the same as {@link #get(boolean, Session)} with <code>allowInjectingConfigTreeSettings</code> set to <code>true</code>
     *
     * @param session an active session
     * @return The JSlobs
     * @throws OXException If JSlobs cannot be returned
     */
    Collection<JSlob> get(Session session) throws OXException;

    /**
     * Gets the JSlobs associated with given user in given context.
     *
     * @param allowInjectingConfigTreeSettings <code>true</code> to allow injecting settings from config-tree; otherwise <code>false</code>
     * @param session an active session
     * @return The JSlobs
     * @throws OXException If JSlobs cannot be returned
     */
    Collection<JSlob> get(boolean allowInjectingConfigTreeSettings, Session session) throws OXException;

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
