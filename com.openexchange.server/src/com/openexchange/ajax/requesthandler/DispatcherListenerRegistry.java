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

package com.openexchange.ajax.requesthandler;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link DispatcherListenerRegistry} - The registry for dispatcher listeners.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
@SingletonService
public interface DispatcherListenerRegistry {

    /** The no-op registry always returning <code>null</code> for <code>getDispatcherListenersFor()</code> */
    public static final DispatcherListenerRegistry NOOP_REGISTRY = new DispatcherListenerRegistry() {

        @Override
        public List<DispatcherListener> getDispatcherListenersFor(AJAXRequestData requestData) throws OXException {
            return null;
        }
    };

    /**
     * Gets the applicable dispatcher listeners for specified request data.
     *
     * @param requestData The request data
     * @return The applicable dispatcher listeners or <code>null</code> if there is none
     * @throws OXException If applicable dispatcher listeners cannot be returned
     */
    List<DispatcherListener> getDispatcherListenersFor(AJAXRequestData requestData) throws OXException;
}
