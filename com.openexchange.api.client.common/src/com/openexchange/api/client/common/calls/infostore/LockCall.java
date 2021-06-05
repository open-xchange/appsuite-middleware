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

package com.openexchange.api.client.common.calls.infostore;

import java.util.Map;
import com.openexchange.annotation.NonNull;
import com.openexchange.api.client.HttpResponseParser;
import com.openexchange.api.client.common.calls.AbstractGetCall;

/**
 * {@link LockCall}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class LockCall extends AbstractGetCall<Void> {

    private final String id;
    private final Long diff;

    /**
     * Initializes a new {@link LockCall}.
     *
     * @param id The ID of the item to lock
     */
    public LockCall(String id) {
        this(id, null);
    }

    /**
     * Initializes a new {@link LockCall}.
     *
     * @param id The ID of the item to lock
     * @param diff If present the value is added to the current time on the server (both in ms).
     *            The document will be locked until that time. If this parameter is not present,
     *            the document will be locked for a duration as configured on the server.
     */
    public LockCall(String id, Long diff) {
        this.id = id;
        this.diff = diff;
    }

    @Override
    @NonNull
    public String getModule() {
        return "/infostore";
    }

    @Override
    public HttpResponseParser<Void> getParser() {
        return null;
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {
        parameters.put("id", id);
        putIfPresent(parameters, "diff", diff);
    }

    @Override
    protected String getAction() {
        return "lock";
    }
}
