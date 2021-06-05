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

package com.openexchange.icap;

import java.io.InputStream;
import org.slf4j.LoggerFactory;
import com.openexchange.icap.header.ICAPRequestHeader;
import com.openexchange.java.Strings;

/**
 * {@link OperationMode} - The mode at which the Response-Modification mode will be functioning.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public enum OperationMode {
    /**
     * This mode implies that the header {@link ICAPRequestHeader#ALLOW} is not present.
     * In this case the ICAP server dumps back the entire {@link InputStream} that it was
     * passed to it in the first place. Hence the provided {@link InputStream} of the
     * {@link ICAPRequest} will be streamed to the ICAP Server and back to the original
     * client. The {@link ICAPRequestHeader#PREVIEW} (if present) it will be ignored and
     * no preview will be send to the ICAP server. The whole data in the {@link InputStream}
     * will be streamed through.
     */
    STREAMING("streaming"),
    /**
     * <p>
     * This mode implies that the header {@link ICAPRequestHeader#ALLOW} is present.
     * In this case the ICAP server will reply with either an ICAP status code of 204
     * meaning that nothing was modified (or needs to be modified) and no response body,
     * or it will reply with a status code of 200 and an HTTP response body. The encapsulated
     * header 'Content-Type' dictates the type of the response body.
     * </p>
     * <p>
     * The {@link ICAPRequestHeader#PREVIEW} (if present) it will be honoured. In that case
     * the ICAP server may respond with a status code of 100 to instruct the client to send
     * the rest of the data, or it will reply with on of the defined {@link ICAPStatusCode}s.
     * </p>
     * <p>The {@link InputStream} that contains the original data will then be thrown away.</p>
     */
    DOUBLE_FETCH("double-fetch");

    private final String name;

    /**
     * Initialises a new {@link OperationMode}.
     */
    private OperationMode(String name) {
        this.name = name;
    }

    /**
     * Gets the name
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Parses the specified string to a valid {@link OperationMode}.
     * If an unknown mode or an error is encountered then the default
     * operation mode {@link OperationMode#DOUBLE_FETCH} will be returned.
     * 
     * @param s The string to parse
     * @return The {@link OperationMode}.
     */
    public static OperationMode parse(String s) {
        if (Strings.isEmpty(s)) {
            return OperationMode.DOUBLE_FETCH;
        }
        try {
            return OperationMode.valueOf(s.toUpperCase().replaceAll("-", "_"));
        } catch (Exception e) {
            LoggerFactory.getLogger(OperationMode.class).debug("Unknown operation mode '{}'", s, e);
            return OperationMode.DOUBLE_FETCH;
        }
    }
}
