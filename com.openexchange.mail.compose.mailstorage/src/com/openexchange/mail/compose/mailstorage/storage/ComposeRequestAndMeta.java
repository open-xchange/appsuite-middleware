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

package com.openexchange.mail.compose.mailstorage.storage;

import com.openexchange.mail.compose.Meta;
import com.openexchange.mail.json.compose.ComposeRequest;

/**
 * {@link ComposeRequestAndMeta} - A pair of compose request and meta information.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class ComposeRequestAndMeta {

    private final ComposeRequest composeRequest;
    private final Meta meta;

    /**
     * Initializes a new {@link ComposeRequestAndMeta}.
     *
     * @param composeRequest The compose request
     * @param meta The message's meta information
     */
    public ComposeRequestAndMeta(ComposeRequest composeRequest, Meta meta) {
        super();
        this.composeRequest = composeRequest;
        this.meta = meta;
    }

    /**
     * Gets the compose request.
     *
     * @return The compose request
     */
    public ComposeRequest getComposeRequest() {
        return composeRequest;
    }

    /**
     * Gets the meta information.
     *
     * @return The meta information
     */
    public Meta getMeta() {
        return meta;
    }

}
