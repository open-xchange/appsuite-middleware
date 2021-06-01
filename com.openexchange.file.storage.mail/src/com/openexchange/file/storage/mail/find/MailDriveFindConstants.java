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

package com.openexchange.file.storage.mail.find;

import java.util.Arrays;
import java.util.List;


/**
 * {@link MailDriveFindConstants}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public final class MailDriveFindConstants {

    /**
     * Initializes a new {@link MailDriveFindConstants}.
     */
    private MailDriveFindConstants() {
        super();
    }

    /** The virtual "global" field for file name and From/To */
    public static final String FIELD_GLOBAL = "global";

    /** The field for file name */
    public static final String FIELD_FILE_NAME = "filename";

    /** The field for From sender */
    public static final String FIELD_FROM = "from";

    /** The field for To recipient */
    public static final String FIELD_TO = "to";

    /** The field for file MIME type */
    public static final String FIELD_FILE_TYPE = "file_mimetype";

    /** The field for file size */
    public static final String FIELD_FILE_SIZE = "file_size";

    /** The field for subject */
    public static final String FIELD_SUBJECT = "subject";

    // ---------------------------------------------------------------------------------------------------------- //

    /** The fields to query for */
    public static final List<String> QUERY_FIELDS = Arrays.asList(new String[] { FIELD_FILE_NAME, FIELD_FROM, FIELD_TO });

    /** The patterns used match {@link Type#DOCUMENTS}. */
    public static final String[] FILETYPE_PATTERNS_DOCUMENTS = {
        "text/plain",
        "text/rtf",
        "application/vnd.ms-word",
        "application/vnd.ms-excel",
        "application/vnd.ms-powerpoint",
        "application/vnd.msword",
        "application/vnd.msexcel",
        "application/vnd.mspowerpoint",
        "application/vnd.openxmlformats",
        "application/vnd.opendocument",
        "application/pdf",
        "application/rtf"
    };

    /** The patterns used match {@link Type#IMAGES}. */
    public static final String[] FILETYPE_PATTERNS_IMAGES = {
        "image/"
    };

    /** The patterns used match {@link Type#AUDIO}. */
    public static final String[] FILETYPE_PATTERNS_AUDIO = {
        "audio/"
    };

    /** The patterns used match {@link Type#VIDEO}. */
    public static final String[] FILETYPE_PATTERNS_VIDEO = {
        "video/"
    };
}
