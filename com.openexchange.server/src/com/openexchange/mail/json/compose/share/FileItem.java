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

package com.openexchange.mail.json.compose.share;

import java.io.InputStream;
import com.openexchange.exception.OXException;

/**
 * {@link FileItem} - Represents a file item.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class FileItem extends Item {

    /** Provides binary data for a file */
    public static interface DataProvider {

        /**
         * Gets the binary data of a file.
         *
         * @return The binary data as a stream.
         * @throws OXException If strem cannot be returned
         */
        InputStream getData() throws OXException;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final DataProvider dataProvider;
    private final long size;
    private final String mimeType;

    /**
     * Initializes a new {@link FileItem}.
     *
     * @param id The item identifier
     * @param name The name
     * @param size The size in bytes or <code>-1</code>
     * @param mimeType The MIME type
     * @param dataProvider The data provider
     */
    public FileItem(String id, String name, long size, String mimeType, DataProvider dataProvider) {
        super(id, name);
        this.dataProvider = dataProvider;
        this.size = size;
        this.mimeType = mimeType;
    }

    /**
     * Gets the MIME type.
     *
     * @return The MIME type
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Gets the size in bytes.
     *
     * @return The size in bytes or <code>-1</code>
     */
    public long getSize() {
        return size;
    }

    /**
     * Gets the binary data of the file.
     *
     * @return The binary data as a stream
     * @throws OXException If stream cannot be returned
     */
    public InputStream getData() throws OXException {
        return dataProvider.getData();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        if (getId() != null) {
            builder.append("id=").append(getId()).append(", ");
        }
        if (getName() != null) {
            builder.append("name=").append(getName()).append(", ");
        }
        if (dataProvider != null) {
            builder.append("dataProvider=").append(dataProvider).append(", ");
        }
        builder.append("size=").append(size).append(", ");
        if (mimeType != null) {
            builder.append("mimeType=").append(mimeType);
        }
        builder.append(']');
        return builder.toString();
    }

}
