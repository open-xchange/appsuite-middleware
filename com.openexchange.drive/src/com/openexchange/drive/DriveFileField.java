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

package com.openexchange.drive;

/**
 * {@link DriveFileField}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public enum DriveFileField {

    /**
     * A file's checksum (column 708, "checksum")
     */
    CHECKSUM,

    /**
     * The filename (column 702, "name")
     */
    NAME,

    /**
     * The content type / mime type of the file (column 703, "contentType")
     */
    CONTENT_TYPE,

    /**
     * The last modification timestamp of the file (column 5, "modified")
     */
    MODIFIED,

    /**
     * The creation timestamp of the file (column 4, "created")
     */
    CREATED,

    /**
     * A direct link to the file in the web interface (column 752, "directLink")
     */
    DIRECT_LINK,

    /**
     * A link to a medium-sized preview image for the file (column 750, "previewLink")
     */
    PREVIEW_LINK,

    /**
     * A link to a thumbnail image for the file (column 753, "thumbnailLink")
     */
    THUMBNAIL_LINK,

    /**
     * The fragments part of the direct link for the file (column 751, "directLinkFragments")
     */
    DIRECT_LINK_FRAGMENTS,

}
