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

package com.openexchange.server;

import com.openexchange.exception.OXException;

/**
 * Components of the server have to implement this interface if this component needs some code to be executed during startup. If the
 * {@link #start()} method of a component executed successfully the {@link #stop()} method is guaranteed to be executed on shutdown.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public interface Initialization {

    /**
     * Called to start a component.
     *
     * @throws OXException If the initialization of the component is not successfully and further start of the server should be interrupted.
     */
    void start() throws OXException;

    /**
     * Called to stop a component. This method is only called if the {@link #start()} method was executed successfully.
     *
     * @throws OXException If some problem occurs. The component then remains as stopped.
     */
    void stop() throws OXException;

}
