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

package com.openexchange.mail.attachment.storage;

/**
 * {@link MailAttachmentInfo}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public final class MailAttachmentInfo {

    private final String id;
    private final String name;
    private final String contentType;
    private final long size;

    /**
     * Initializes a new {@link MailAttachmentInfo}.
     *
     * @param id The attachment identifier in storage
     * @param contentType The MIME type
     * @param name The file name
     * @param size The size
     */
    public MailAttachmentInfo(String id, String contentType, String name, long size) {
        super();
        this.id = id;
        this.contentType = contentType;
        this.name = name;
        this.size = size;
    }

    /**
     * Gets the attachment identifier in storage
     *
     * @return The attachment identifier in storage
     */
    public String getId() {
        return id;
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
     * Gets the MIME type
     *
     * @return The MIME type
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Gets the size
     *
     * @return The size or <code>-1</code> if unknown
     */
    public long getSize() {
        return size;
    }

}
