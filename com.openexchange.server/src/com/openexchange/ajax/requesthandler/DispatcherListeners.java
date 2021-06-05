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

/**
 * {@link DispatcherListeners} - Utility class for dispatcher listeners.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public enum DispatcherListeners {

    ;

    /**
     * Initializes a new {@link DispatcherListeners}.
     */
    private DispatcherListeners() {
        // Nothing to initialize
    }

    /**
     * Checks if specified request data targets specified module.
     *
     * @param module The module to check against
     * @param requestData The request data
     * @return <code>true</code> if request data targets specified module; otherwise <code>false</code>
     */
    public static boolean equalsModule(String module, AJAXRequestData requestData) {
        if (null == module) {
            return false;
        }

        return module.equals(null == requestData ? null : requestData.getNormalizedModule());
    }

    /**
     * Checks if specified request data targets specified action in given module.
     *
     * @param module The module to check against
     * @param requestData The request data
     * @return <code>true</code> if request data targets specified action in given module; otherwise <code>false</code>
     */
    public static boolean equalsAction(String action, String module, AJAXRequestData requestData) {
        if (null == action || null == module || null == requestData) {
            return false;
        }

        return module.equals(requestData.getNormalizedModule()) && action.equals(requestData.getAction());
    }

    /**
     * Checks if specified exception denotes a HTTP error.
     *
     * @param e The exception instance to check
     * @return The signaled HTTP error or <code>-1</code> if exception denotes no HTTP error
     */
    public static int getHttpError(Exception e) {
        return (e instanceof HttpErrorCodeException) ? ((HttpErrorCodeException) e).getStatusCode() : -1;
    }

}
