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

package com.openexchange.oauth.provider.authorizationserver.client;

/**
 * Default implementation of an {@link Icon}, based on a byte array.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class DefaultIcon implements Icon {

    private static final long serialVersionUID = 1951072419974173720L;

    private String mimeType = null;
    private byte[] data;

    /**
     * Initializes a new {@link DefaultIcon}.
     */
    public DefaultIcon() {
        super();
    }

    /**
     * Sets the mime type
     *
     * @param mimeType
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * Sets the icon data and size. The size is obtained via <code>data.length</code>
     *
     * @param data
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public int getSize() {
        return data.length;
    }

    @Override
    public byte[] getData() {
        return data;
    }

}
