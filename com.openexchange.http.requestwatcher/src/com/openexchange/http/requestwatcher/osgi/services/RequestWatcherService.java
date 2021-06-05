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

package com.openexchange.http.requestwatcher.osgi.services;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link RequestWatcherService} to keep track of incoming HttpServletRequests. Requests are periodically inspected for the duration of
 * processing and a warning is logged if the duration exceeds a configurable amount of time.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> JavaDoc
 */
@SingletonService
public interface RequestWatcherService {

    /**
     * Registers a request in the request registry.
     *
     * @param request The request to register
     * @param thread The thread associated with the request
     * @param propertyMap The log properties associated with the thread
     * @return A registry entry wrapping the registered request
     */
    RequestRegistryEntry registerRequest(HttpServletRequest request, HttpServletResponse response, Thread thread, Map<String, String> propertyMap);

    /**
     * Unregisters a request from the request registry
     *
     * @param The RequestRegistryEntry that was received when initially registering the request
     * @return <code>false</code> if the entry could not be found; otherwise <code>true</code>
     */
    boolean unregisterRequest(RequestRegistryEntry registryEntry);

    /**
     * Stops periodically inspecting the requests in the requestRegistry.
     *
     * @return <code>false</code> if the task could not be canceled, typically because it has already completed normally; <code>true</code>
     *         otherwise
     */
    boolean stopWatching();

}
