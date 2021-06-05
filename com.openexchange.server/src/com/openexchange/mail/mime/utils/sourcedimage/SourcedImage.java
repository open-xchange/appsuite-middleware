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

package com.openexchange.mail.mime.utils.sourcedimage;

/**
 * {@link SourcedImage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SourcedImage {

    private final String contentType;

    private final String transferEncoding;

    private final String data;

    private final String contentId;

    /**
     * Initializes a new {@link SourcedImage}.
     *
     * @param contentType The Content-Type
     * @param transferEncoding The transfer encoding; e.g. <code>"base64"</code>
     * @param contentId The content identifier
     * @param data The (encoded) image data
     */
    protected SourcedImage(String contentType, String transferEncoding, String contentId, String data) {
        super();
        this.contentType = contentType;
        this.transferEncoding = transferEncoding;
        this.contentId = contentId;
        this.data = data;
    }

    /**
     * Gets the content identifier.
     *
     * @return The content identifier
     */
    public String getContentId() {
        return contentId;
    }

    /**
     * Gets the content type.
     *
     * @return The content type
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Gets the transfer encoding.
     *
     * @return The transfer encoding
     */
    public String getTransferEncoding() {
        return transferEncoding;
    }

    /**
     * Gets the data.
     *
     * @return The data
     */
    public String getData() {
        return data;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(64);
        builder.append("SourcedImage [contentType=").append(contentType).append(", transferEncoding=").append(transferEncoding).append(
            ", contentId=").append(contentId).append(", data=");
        if (data.length() <= 10) {
            builder.append(data);
        } else {
            builder.append(data.substring(0, 10)).append("...");
        }

        builder.append(']');
        return builder.toString();
    }

}
