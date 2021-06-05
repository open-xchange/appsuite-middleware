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

package com.openexchange.http.grizzly.service.http;

import org.glassfish.grizzly.http.server.HttpHandler;

/**
 * {@link HttpHandlerMatch} - A simple wrapper for a matching {@link HttpHandler} instance along-side with its registered alias.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
class HttpHandlerMatch {

    /** The matching HTTP handler */
    public final HttpHandler httpHandler;

    /** The matching alias */
    public final String alias;

    /**
     * Initializes a new {@link HttpHandlerMatch}.
     *
     * @param httpHandler The matching {@link HttpHandler} instance
     * @param match The matching alias
     */
    HttpHandlerMatch(HttpHandler httpHandler, String match) {
        super();
        this.httpHandler = httpHandler;
        this.alias = match;
    }

}
