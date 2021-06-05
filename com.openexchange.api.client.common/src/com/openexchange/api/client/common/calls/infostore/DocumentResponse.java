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

import java.io.InputStream;
import com.openexchange.annotation.NonNull;
import com.openexchange.api.client.InputStreamAwareResponse;

/**
 * {@link DocumentResponse}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class DocumentResponse implements InputStreamAwareResponse {

    private InputStream inputStream;
    private String eTag;

    /**
     * Initializes a new {@link DocumentResponse}.
     */
    public DocumentResponse() {}

    /**
     * Gets the inputStream
     *
     * @return The inputStream, or <code>null</code> if the response does not contain any data
     */
    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * Sets the inputStream
     *
     * @param inputStream The inputStream to set
     */
    @Override
    public void setInputStream(@NonNull InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * Gets the eTag
     *
     * @return The eTag
     */
    public String getETag() {
        return eTag;
    }

    /**
     * Sets the ETag
     *
     * @param eTag The ETag to set
     */
    public void setETag(String eTag) {
        this.eTag = eTag;
    }
}
