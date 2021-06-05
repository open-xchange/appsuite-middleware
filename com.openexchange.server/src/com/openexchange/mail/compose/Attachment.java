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

package com.openexchange.mail.compose;

import java.io.InputStream;
import java.util.UUID;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link Attachment} - Represents an attachment associated with a message.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public interface Attachment {

    /** The Content-Disposition for an attachment's content */
    public static enum ContentDisposition {
        /**
         * The <code>"attachment"</code> disposition type.
         */
        ATTACHMENT("attachment"),
        /**
         * The <code>"inline"</code> disposition type.
         */
        INLINE("inline");

        private final String id;

        private ContentDisposition(String id) {
            this.id = id;
        }

        /**
         * Gets the identifier
         *
         * @return The identifier
         */
        public String getId() {
            return id;
        }

        /**
         * Gets the Content-Disposition for given identifier
         *
         * @param disposition The identifier to look-up
         * @return The associated Content-Disposition or <code>null</code>
         */
        public static ContentDisposition dispositionFor(String disposition) {
            if (Strings.isEmpty(disposition)) {
                return null;
            }

            String lk = disposition.trim();
            for (ContentDisposition d : ContentDisposition.values()) {
                if (lk.equalsIgnoreCase(d.id)) {
                    return d;
                }
            }
            return null;
        }

    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Gets the identifier
     *
     * @return The identifier
     */
    UUID getId();

    /**
     * Gets the identifier of the composition space, this attachment is associated with
     *
     * @return The composition space identifier
     */
    UUID getCompositionSpaceId();

    /**
     * Gets the reference to the persisted attachment; e.g. <code>"fs://517616721"</code> or <code>"db://167215176"</code>
     *
     * @return The storage reference
     */
    AttachmentStorageReference getStorageReference();

    /**
     * Gets the attachment's data.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * <b>Note</b>: The input stream is supposed to be generated "on the fly"
     * </div>
     *
     * @return The data
     * @throws OXException If data cannot be returned
     */
    InputStream getData() throws OXException;

    /**
     * Closes this attachment's data provider (if any and if possible), relinquishing any underlying resources.
     */
    void close();

    /**
     * Gets the file name
     *
     * @return The name
     */
    String getName();

    /**
     * Gets the size in bytes.
     *
     * @return The size in bytes or <code>-1</code> if unknown
     */
    long getSize();

    /**
     * Gets the MIME type
     *
     * @return The MIME type or <code>null</code>
     */
    String getMimeType();

    /**
     * Gets the content identifier reference
     *
     * @return The content identifier
     */
    String getContentId();

    /**
     * Gets the content identifier reference as an object.
     *
     * @return The content identifier object
     */
    default ContentId getContentIdAsObject() {
        String contentId = getContentId();
        return contentId == null ? null : new ContentId(contentId);
    }

    /**
     * Gets the content disposition
     *
     * @return The content disposition
     */
    ContentDisposition getContentDisposition();

    /**
     * Gets the attachment's origin.
     *
     * @return The origin
     */
    AttachmentOrigin getOrigin();

}
