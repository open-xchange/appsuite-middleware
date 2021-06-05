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
import com.openexchange.api.client.common.parser.StringParser;

/**
 * {@link MoveCall}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class MoveCall extends AbstractGetCall<String> {

    private final String id;
    private final String folder;
    private final String pushToken;
    private final long timestamp;

    /**
     * Initializes a new {@link MoveCall}.
     *
     * @param id The ID of the item to move
     * @param folder The destination folder
     * @param timestamp The sequencenumber/timestamp
     */
    public MoveCall(String id, String folder, long timestamp) {
        this(id, folder, timestamp, null);
    }

    /**
     * Initializes a new {@link MoveCall}.
     *
     * @param id The ID of the item to move
     * @param folder The destination folder
     * @param timestamp The sequencenumber/timestamp
     * @param pushToken The push token
     */
    public MoveCall(String id, String folder, long timestamp, String pushToken) {
        this.id = id;
        this.folder = folder;
        this.pushToken = pushToken;
        this.timestamp = timestamp;
    }

    @Override
    @NonNull
    public String getModule() {
        return "/infostore";
    }

    @Override
    public HttpResponseParser<String> getParser() {
        return StringParser.getInstance();
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {
        parameters.put("id", id);
        parameters.put("folder", folder);
        parameters.put("timestamp", String.valueOf(timestamp));
        putIfNotEmpty(parameters, "pushToken", pushToken);
    }

    @Override
    protected String getAction() {
        return "move";
    }
}
